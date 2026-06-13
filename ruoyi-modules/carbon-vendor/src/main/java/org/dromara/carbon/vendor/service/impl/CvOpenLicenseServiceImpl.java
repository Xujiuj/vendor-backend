package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvCustomer;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.domain.open.CvOpenLicenseCurrentRequest;
import org.dromara.carbon.vendor.domain.open.CvOpenLicenseCurrentResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenRenewalOrderRequest;
import org.dromara.carbon.vendor.domain.open.CvOpenRenewalOrderResponse;
import org.dromara.carbon.vendor.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.mapper.CvRenewalOrderMapper;
import org.dromara.carbon.vendor.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.service.ICvOpenLicenseService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * License-scoped vendor open license API service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvOpenLicenseServiceImpl implements ICvOpenLicenseService {

    private static final String API_LICENSE_CURRENT = "/open/licenses/current";
    private static final String API_RENEWAL_ORDERS = "/open/renewal-orders";
    private static final String METHOD_POST = "POST";
    private static final String ISSUE_STATUS_REVOKED = "revoked";
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_EXPIRED = "expired";
    private static final String STATUS_NOT_YET_VALID = "not_yet_valid";
    private static final String STATUS_REVOKED = "revoked";
    private static final String ORDER_STATUS_PENDING = "pending";
    private static final String PAY_CHANNEL_MANUAL = "manual";
    private static final String REQUEST_SOURCE_OPEN_API = "open-api";
    private static final String CUSTOMER_STATUS_DISABLED = "disabled";
    private static final String CUSTOMER_STATUS_INACTIVE = "inactive";
    private static final String CUSTOMER_STATUS_STOPPED = "stopped";
    private static final String CUSTOMER_STATUS_SUSPENDED = "suspended";
    private static final String CUSTOMER_STATUS_NUMERIC_DISABLED = "1";
    private static final DateTimeFormatter ORDER_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CvLicenseIssueMapper licenseIssueMapper;
    private final CvCustomerMapper customerMapper;
    private final CvRenewalOrderMapper renewalOrderMapper;
    private final ICvOpenApiAuditService openApiAuditService;

    @Override
    public CvOpenLicenseCurrentResponse currentLicense(CvOpenLicenseCurrentRequest request) {
        Long customerId = null;
        try {
            CvLicenseIssue issue = requireLicenseAndInstall(request.getLicenseId(), request.getInstallId());
            customerId = issue.getCustomerId();
            CvOpenLicenseCurrentResponse response = toCurrentResponse(issue);
            openApiAuditService.recordSuccess(API_LICENSE_CURRENT, METHOD_POST, request.getLicenseId(),
                request.getInstallId(), customerId, licenseCurrentSummary(request));
            return response;
        } catch (RuntimeException ex) {
            openApiAuditService.recordFailure(API_LICENSE_CURRENT, METHOD_POST, request == null ? null : request.getLicenseId(),
                request == null ? null : request.getInstallId(), customerId, licenseCurrentSummary(request), ex.getMessage());
            throw ex;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CvOpenRenewalOrderResponse createRenewalOrder(CvOpenRenewalOrderRequest request) {
        Long customerId = null;
        try {
            CvLicenseIssue issue = requireLicenseAndInstall(request.getLicenseId(), request.getInstallId());
            customerId = issue.getCustomerId();
            if (isRevokedIssue(issue)) {
                throw new ServiceException("license entitlement is revoked");
            }
            validateCustomerCanRenew(customerId);
            CvRenewalOrder existingOrder = findExistingOrder(request);
            if (existingOrder != null) {
                CvOpenRenewalOrderResponse response = toRenewalResponse(existingOrder, true);
                openApiAuditService.recordSuccess(API_RENEWAL_ORDERS, METHOD_POST, request.getLicenseId(),
                    request.getInstallId(), customerId, renewalOrderSummary(request));
                return response;
            }

            CvRenewalOrder order = new CvRenewalOrder();
            order.setOrderNo(generateOrderNo());
            order.setCustomerId(customerId);
            order.setLicenseId(issue.getLicenseId());
            order.setInstallId(issue.getInstallId());
            order.setRequestedEdition(trimToNull(request.getEdition()));
            order.setRenewalPeriod(trimToNull(request.getRenewalPeriod()));
            order.setContactName(trimToNull(request.getContactName()));
            order.setContactEmail(trimToNull(request.getContactEmail()));
            order.setContactPhone(trimToNull(request.getContactPhone()));
            order.setIdempotencyKey(trimToNull(request.getIdempotencyKey()));
            order.setRequestSource(REQUEST_SOURCE_OPEN_API);
            order.setOrderStatus(ORDER_STATUS_PENDING);
            order.setPayChannel(PAY_CHANNEL_MANUAL);
            order.setAmount(BigDecimal.ZERO);
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            renewalOrderMapper.insert(order);

            CvOpenRenewalOrderResponse response = toRenewalResponse(order, false);
            openApiAuditService.recordSuccess(API_RENEWAL_ORDERS, METHOD_POST, request.getLicenseId(),
                request.getInstallId(), customerId, renewalOrderSummary(request));
            return response;
        } catch (RuntimeException ex) {
            openApiAuditService.recordFailure(API_RENEWAL_ORDERS, METHOD_POST, request == null ? null : request.getLicenseId(),
                request == null ? null : request.getInstallId(), customerId, renewalOrderSummary(request), ex.getMessage());
            throw ex;
        }
    }

    private CvRenewalOrder findExistingOrder(CvOpenRenewalOrderRequest request) {
        if (request == null || StringUtils.isBlank(request.getIdempotencyKey())) {
            return null;
        }
        return renewalOrderMapper.selectOne(Wrappers.<CvRenewalOrder>lambdaQuery()
            .eq(CvRenewalOrder::getIdempotencyKey, request.getIdempotencyKey().trim())
            .eq(CvRenewalOrder::getLicenseId, request.getLicenseId().trim())
            .eq(CvRenewalOrder::getInstallId, request.getInstallId().trim()), false);
    }

    private CvOpenRenewalOrderResponse toRenewalResponse(CvRenewalOrder order, boolean reused) {
        CvOpenRenewalOrderResponse response = new CvOpenRenewalOrderResponse();
        response.setOrderNo(order.getOrderNo());
        response.setCustomerId(order.getCustomerId());
        response.setLicenseId(order.getLicenseId());
        response.setOrderStatus(order.getOrderStatus());
        response.setPayChannel(order.getPayChannel());
        response.setAmount(order.getAmount());
        response.setReused(reused);
        response.setMessage(reused ? "renewal order already exists" : "renewal order created for manual processing");
        return response;
    }

    private CvLicenseIssue requireLicenseAndInstall(String licenseId, String installId) {
        String normalizedLicenseId = normalizeRequired(licenseId, "licenseId cannot be blank");
        String normalizedInstallId = normalizeRequired(installId, "installId cannot be blank");
        CvLicenseIssue issue = licenseIssueMapper.selectOne(Wrappers.<CvLicenseIssue>lambdaQuery()
            .eq(CvLicenseIssue::getLicenseId, normalizedLicenseId), false);
        if (issue == null) {
            throw new ServiceException("license entitlement does not exist");
        }
        if (!normalizedInstallId.equals(issue.getInstallId())) {
            throw new ServiceException("license installId does not match");
        }
        return issue;
    }

    private CvOpenLicenseCurrentResponse toCurrentResponse(CvLicenseIssue issue) {
        CvOpenLicenseCurrentResponse response = new CvOpenLicenseCurrentResponse();
        response.setLicenseId(issue.getLicenseId());
        response.setCustomerId(issue.getCustomerId());
        response.setStatus(resolveLicenseStatus(issue));
        response.setEdition(issue.getEdition());
        response.setFeatureCodes(issue.getFeatureCodes());
        response.setKeyId(issue.getKeyId());
        response.setAlgorithm(issue.getAlgorithm());
        response.setSchemaVersion(issue.getSchemaVersion());
        response.setValidFrom(issue.getValidFrom());
        response.setValidTo(issue.getValidTo());
        if (STATUS_ACTIVE.equals(response.getStatus())) {
            response.setLicensePayload(issue.getLicensePayload());
            response.setSignatureText(issue.getSignatureText());
        }
        return response;
    }

    private String resolveLicenseStatus(CvLicenseIssue issue) {
        if (isRevokedIssue(issue)) {
            return STATUS_REVOKED;
        }
        Date now = new Date();
        if (issue.getValidFrom() != null && issue.getValidFrom().after(now)) {
            return STATUS_NOT_YET_VALID;
        }
        if (issue.getValidTo() != null && issue.getValidTo().before(now)) {
            return STATUS_EXPIRED;
        }
        return STATUS_ACTIVE;
    }

    private boolean isRevokedIssue(CvLicenseIssue issue) {
        return issue.getRevokedTime() != null || ISSUE_STATUS_REVOKED.equals(normalizeStatus(issue.getIssueStatus()));
    }

    private void validateCustomerCanRenew(Long customerId) {
        if (customerId == null) {
            throw new ServiceException("Renewal customerId cannot be null");
        }
        CvCustomer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new ServiceException("Vendor customer does not exist");
        }
        if (isDisabledCustomer(customer.getCustomerStatus())) {
            throw new ServiceException("Disabled customer cannot create renewal metadata");
        }
    }

    private boolean isDisabledCustomer(String customerStatus) {
        String normalizedStatus = normalizeStatus(customerStatus);
        return CUSTOMER_STATUS_DISABLED.equals(normalizedStatus)
            || CUSTOMER_STATUS_INACTIVE.equals(normalizedStatus)
            || CUSTOMER_STATUS_STOPPED.equals(normalizedStatus)
            || CUSTOMER_STATUS_SUSPENDED.equals(normalizedStatus)
            || CUSTOMER_STATUS_NUMERIC_DISABLED.equals(normalizedStatus);
    }

    private String generateOrderNo() {
        String timePart = ORDER_TIME_FORMAT.format(LocalDateTime.now());
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        return "REN-" + timePart + "-" + randomPart;
    }

    private String licenseCurrentSummary(CvOpenLicenseCurrentRequest request) {
        if (request == null) {
            return "request=null";
        }
        return "keyId=" + nullToBlank(request.getKeyId())
            + ";currentSummary=" + nullToBlank(request.getCurrentSummary());
    }

    private String renewalOrderSummary(CvOpenRenewalOrderRequest request) {
        if (request == null) {
            return "request=null";
        }
        return "edition=" + nullToBlank(request.getEdition())
            + ";renewalPeriod=" + nullToBlank(request.getRenewalPeriod())
            + ";contactName=" + nullToBlank(request.getContactName())
            + ";contactEmail=" + nullToBlank(request.getContactEmail())
            + ";contactPhone=" + nullToBlank(request.getContactPhone())
            + ";idempotencyKey=" + nullToBlank(request.getIdempotencyKey());
    }

    private String normalizeRequired(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new ServiceException(message);
        }
        return value.trim();
    }

    private String normalizeStatus(String status) {
        return StringUtils.isBlank(status) ? null : status.trim().toLowerCase(Locale.ROOT);
    }

    private String nullToBlank(String value) {
        return StringUtils.isBlank(value) ? "" : value.trim();
    }

    private String trimToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }
}
