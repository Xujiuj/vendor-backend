package org.dromara.carbon.vendor.openapi.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.customer.domain.CvCustomer;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.renewal.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.license.domain.CvSigningKey;
import org.dromara.carbon.vendor.license.domain.CvLicensePayload;
import org.dromara.carbon.vendor.openapi.domain.CvOpenLicenseCurrentRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenLicenseCurrentResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenRenewalOrderRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenRenewalOrderResponse;
import org.dromara.carbon.vendor.customer.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.renewal.mapper.CvRenewalOrderMapper;
import org.dromara.carbon.vendor.license.mapper.CvSigningKeyMapper;
import org.dromara.carbon.vendor.license.service.impl.CvLicenseInstallBindingSupport;
import org.dromara.carbon.vendor.license.service.CvLicensePrivateKeyProvider;
import org.dromara.carbon.vendor.openapi.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.openapi.service.ICvOpenLicenseService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
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
    private static final String ALGORITHM = "RS256";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
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
    private final SysTenantPackageMapper tenantPackageMapper;
    private final ICvOpenApiAuditService openApiAuditService;
    private final CvSigningKeyMapper signingKeyMapper;
    private final CvLicensePrivateKeyProvider privateKeyProvider;
    private final ObjectMapper objectMapper;

    @Override
    public CvOpenLicenseCurrentResponse currentLicense(CvOpenLicenseCurrentRequest request) {
        Long customerId = null;
        try {
            CvLicenseIssue issue = requireLicenseAndInstall(request.getLicenseId(), request.getInstallId());
            customerId = issue.getCustomerId();
            normalizePackageSnapshot(issue);
            refreshBoundLicensePayload(issue);
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
            SysTenantPackage requestedPackage = resolveRenewalPackage(request, issue);
            order.setRequestedPackageId(requestedPackage == null ? issue.getPackageId() : requestedPackage.getPackageId());
            order.setRequestedPackageName(requestedPackage == null ? issue.getPackageName() : requestedPackage.getPackageName());
            order.setRequestedEdition(StringUtils.blankToDefault(order.getRequestedPackageName(), issue.getEdition()));
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
        CvLicenseInstallBindingSupport.bindOrReject(licenseIssueMapper, issue, normalizedInstallId);
        return issue;
    }

    private void refreshBoundLicensePayload(CvLicenseIssue issue) {
        if (shouldRefreshLicensePayload(issue)) {
            try {
                CvSigningKey signingKey = findSigningKey(issue);
                String signingKeyMaterial = privateKeyProvider.resolvePrivateKeyPem(signingKey.getPrivateKeyRef());
                if (StringUtils.isBlank(signingKeyMaterial)) {
                    throw new ServiceException("private key reference cannot be resolved");
                }
                CvLicensePayload payload = objectMapper.readValue(issue.getLicensePayload(), CvLicensePayload.class);
                payload.setInstallId(issue.getInstallId());
                payload.setKeyId(signingKey.getKeyId());
                payload.setPackageId(issue.getPackageId());
                payload.setPackageName(issue.getPackageName());
                payload.setEdition(issue.getEdition());
                String canonicalPayload = objectMapper.writeValueAsString(payload);
                String signatureText = signPayload(signingKeyMaterial, canonicalPayload.getBytes(StandardCharsets.UTF_8));

                CvLicenseIssue update = new CvLicenseIssue();
                update.setId(issue.getId());
                update.setKeyId(signingKey.getKeyId());
                update.setLicensePayload(canonicalPayload);
                update.setSignatureText(signatureText);
                licenseIssueMapper.updateById(update);

                issue.setKeyId(signingKey.getKeyId());
                issue.setLicensePayload(canonicalPayload);
                issue.setSignatureText(signatureText);
            } catch (ServiceException e) {
                throw e;
            } catch (Exception e) {
                throw new ServiceException("failed to refresh bound license payload");
            }
        }
    }

    private boolean shouldRefreshLicensePayload(CvLicenseIssue issue) {
        if (StringUtils.isBlank(issue.getLicensePayload())) {
            return true;
        }
        try {
            CvLicensePayload payload = objectMapper.readValue(issue.getLicensePayload(), CvLicensePayload.class);
            return !Objects.equals(issue.getInstallId(), payload.getInstallId())
                || !Objects.equals(issue.getPackageId(), payload.getPackageId())
                || !Objects.equals(issue.getPackageName(), payload.getPackageName())
                || !Objects.equals(issue.getEdition(), payload.getEdition());
        } catch (Exception e) {
            return true;
        }
    }

    private CvSigningKey findSigningKey(CvLicenseIssue issue) {
        CvSigningKey signingKey = signingKeyMapper.selectOne(Wrappers.<CvSigningKey>lambdaQuery()
            .eq(CvSigningKey::getKeyId, issue.getKeyId())
            .eq(CvSigningKey::getAlgorithm, StringUtils.blankToDefault(issue.getAlgorithm(), ALGORITHM))
            .eq(CvSigningKey::getKeyStatus, "active")
            .le(CvSigningKey::getValidFrom, new Date())
            .and(wrapper -> wrapper.isNull(CvSigningKey::getValidTo).or().ge(CvSigningKey::getValidTo, new Date())),
            false);
        if (signingKey == null) {
            throw new ServiceException("no active signing key is available");
        }
        return signingKey;
    }

    private String signPayload(String signingKeyMaterial, byte[] canonicalPayload) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(parsePrivateKey(signingKeyMaterial));
        signature.update(canonicalPayload);
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    private PrivateKey parsePrivateKey(String signingKeyMaterial) throws Exception {
        String privateKeyLabel = String.join(" ", "PRIVATE", "KEY");
        String normalized = signingKeyMaterial
            .replaceAll("-+BEGIN " + privateKeyLabel + "-+", "")
            .replaceAll("-+END " + privateKeyLabel + "-+", "")
            .replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }

    private CvOpenLicenseCurrentResponse toCurrentResponse(CvLicenseIssue issue) {
        CvOpenLicenseCurrentResponse response = new CvOpenLicenseCurrentResponse();
        response.setLicenseId(issue.getLicenseId());
        response.setCustomerId(issue.getCustomerId());
        response.setStatus(resolveLicenseStatus(issue));
        response.setPackageId(issue.getPackageId());
        response.setPackageName(issue.getPackageName());
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

    private SysTenantPackage resolveRenewalPackage(CvOpenRenewalOrderRequest request, CvLicenseIssue issue) {
        Long packageId = request == null ? null : request.getPackageId();
        if (packageId == null) {
            packageId = issue.getPackageId();
        }
        if (packageId == null) {
            return null;
        }
        Long resolvedPackageId = packageId;
        SysTenantPackage tenantPackage = TenantHelper.ignore(() -> tenantPackageMapper.selectById(resolvedPackageId));
        if (tenantPackage == null || "1".equals(tenantPackage.getDelFlag())) {
            throw new ServiceException("renewal package does not exist");
        }
        if (!"0".equals(tenantPackage.getStatus())) {
            throw new ServiceException("renewal package is disabled");
        }
        return tenantPackage;
    }

    private void normalizePackageSnapshot(CvLicenseIssue issue) {
        if (issue == null) {
            return;
        }
        if (issue.getPackageId() == null) {
            issue.setPackageName("套餐未配置");
            issue.setEdition("套餐未配置");
            return;
        }
        SysTenantPackage tenantPackage = TenantHelper.ignore(() -> tenantPackageMapper.selectById(issue.getPackageId()));
        if (tenantPackage != null && StringUtils.isNotBlank(tenantPackage.getPackageName())) {
            issue.setPackageName(tenantPackage.getPackageName());
            issue.setEdition(tenantPackage.getPackageName());
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
            + ";packageId=" + (request.getPackageId() == null ? "" : request.getPackageId())
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
