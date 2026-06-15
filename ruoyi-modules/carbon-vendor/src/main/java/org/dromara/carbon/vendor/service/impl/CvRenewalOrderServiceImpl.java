package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvCustomer;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.domain.bo.CvRenewalOrderBo;
import org.dromara.carbon.vendor.domain.renewal.CvRenewalCallbackRequest;
import org.dromara.carbon.vendor.domain.vo.CvRenewalOrderVo;
import org.dromara.carbon.vendor.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.mapper.CvRenewalOrderMapper;
import org.dromara.carbon.vendor.service.ICvRenewalOrderService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Vendor renewal order service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvRenewalOrderServiceImpl implements ICvRenewalOrderService {

    private static final String ORDER_STATUS_PENDING = "pending";
    private static final String ORDER_STATUS_PAID = "paid";
    private static final String ORDER_STATUS_AUTHORIZED = "authorized";
    private static final String ISSUE_STATUS_PENDING = "pending_issue";
    private static final String ISSUE_STATUS_ISSUING = "issuing";
    private static final String ISSUE_STATUS_ISSUED = "issued";
    private static final String ISSUE_STATUS_FAILED = "issue_failed";
    private static final String ISSUE_STATUS_REVOKED = "revoked";
    private static final String CUSTOMER_STATUS_DISABLED = "disabled";
    private static final String CUSTOMER_STATUS_INACTIVE = "inactive";
    private static final String CUSTOMER_STATUS_STOPPED = "stopped";
    private static final String CUSTOMER_STATUS_SUSPENDED = "suspended";
    private static final String CUSTOMER_STATUS_NUMERIC_DISABLED = "1";

    private final CvRenewalOrderMapper baseMapper;
    private final CvCustomerMapper customerMapper;
    private final CvLicenseIssueMapper licenseIssueMapper;
    private final SysTenantPackageMapper tenantPackageMapper;

    @Override
    public TableDataInfo<CvRenewalOrderVo> selectPageRenewalOrderList(CvRenewalOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CvRenewalOrder> lqw = buildQueryWrapper(bo);
        Page<CvRenewalOrderVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public CvRenewalOrderVo selectRenewalOrderById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public int insertRenewalOrder(CvRenewalOrderBo bo) {
        validateRenewalMetadata(bo);
        CvRenewalOrder renewalOrder = MapstructUtils.convert(bo, CvRenewalOrder.class);
        return baseMapper.insert(renewalOrder);
    }

    @Override
    public int updateRenewalOrder(CvRenewalOrderBo bo) {
        validateRenewalMetadata(bo);
        CvRenewalOrder renewalOrder = MapstructUtils.convert(bo, CvRenewalOrder.class);
        return baseMapper.updateById(renewalOrder);
    }

    @Override
    public int applyRenewalCallback(CvRenewalCallbackRequest request) {
        CvRenewalOrder existingOrder = findRenewalOrderForCallback(request);
        if (existingOrder == null) {
            throw new ServiceException("Vendor renewal order does not exist");
        }
        if (request.getCustomerId() != null && !Objects.equals(request.getCustomerId(), existingOrder.getCustomerId())) {
            throw new ServiceException("Renewal callback customer metadata does not match vendor order");
        }
        String targetStatus = normalizeCallbackStatus(request.getOrderStatus());
        assertCallbackTransitionAllowed(existingOrder.getOrderStatus(), targetStatus);

        validateCustomerCanRenew(existingOrder.getCustomerId());
        validateLicenseMetadata(existingOrder.getLicenseId(), existingOrder.getCustomerId(), "Original renewal license");
        if (StringUtils.isNotBlank(request.getIssuedLicenseId())) {
            validateIssuedLicenseMetadata(request.getIssuedLicenseId(), existingOrder);
        }

        boolean manualPath = isManualPath(existingOrder, request);
        CvRenewalOrder update = new CvRenewalOrder();
        update.setId(existingOrder.getId());
        update.setOrderStatus(targetStatus);
        update.setIssueStatus(resolveCallbackIssueStatus(targetStatus, request.getIssuedLicenseId(), manualPath));
        update.setPayChannel(StringUtils.blankToDefault(request.getPayChannel(), existingOrder.getPayChannel()));
        update.setPaidTime(resolveCallbackPaidTime(request.getPaidTime(), existingOrder.getPaidTime()));
        if (StringUtils.isNotBlank(request.getIssuedLicenseId())) {
            update.setIssuedLicenseId(request.getIssuedLicenseId());
        }
        update.setUpdateTime(new Date());
        if (ISSUE_STATUS_ISSUING.equals(update.getIssueStatus()) && StringUtils.isBlank(request.getIssuedLicenseId())) {
            return baseMapper.update(update, new LambdaUpdateWrapper<CvRenewalOrder>()
                .eq(CvRenewalOrder::getId, existingOrder.getId())
                .set(CvRenewalOrder::getIssuedLicenseId, null));
        }
        return baseMapper.updateById(update);
    }

    @Override
    public int deleteRenewalOrderByIds(Long[] ids) {
        return baseMapper.deleteByIds(Arrays.asList(ids));
    }

    @Override
    public int retryRenewalIssue(Long id) {
        if (id == null) {
            throw new ServiceException("Renewal order id cannot be null");
        }
        CvRenewalOrder existingOrder = baseMapper.selectById(id);
        if (existingOrder == null) {
            throw new ServiceException("Vendor renewal order does not exist");
        }
        if (!ISSUE_STATUS_FAILED.equals(normalizeIssueStatus(existingOrder.getIssueStatus()))) {
            throw new ServiceException("Only failed renewal issue can be retried");
        }
        if (!ORDER_STATUS_AUTHORIZED.equals(normalizeStatus(existingOrder.getOrderStatus()))) {
            throw new ServiceException("Only automatic failed renewal issue can be retried");
        }
        if (isManualPath(existingOrder, null)) {
            throw new ServiceException("Only automatic failed renewal issue can be retried");
        }

        validateCustomerCanRenew(existingOrder.getCustomerId());
        validateLicenseMetadata(existingOrder.getLicenseId(), existingOrder.getCustomerId(), "Original renewal license");

        CvRenewalOrder update = new CvRenewalOrder();
        update.setId(existingOrder.getId());
        update.setIssueStatus(resolveRetryIssueStatus(existingOrder));
        update.setUpdateTime(new Date());
        return baseMapper.updateById(update);
    }

    private LambdaQueryWrapper<CvRenewalOrder> buildQueryWrapper(CvRenewalOrderBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CvRenewalOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvRenewalOrder::getId, bo.getId());
        lqw.like(StringUtils.isNotBlank(bo.getOrderNo()), CvRenewalOrder::getOrderNo, bo.getOrderNo());
        lqw.eq(bo.getCustomerId() != null, CvRenewalOrder::getCustomerId, bo.getCustomerId());
        lqw.like(StringUtils.isNotBlank(bo.getLicenseId()), CvRenewalOrder::getLicenseId, bo.getLicenseId());
        lqw.like(StringUtils.isNotBlank(bo.getInstallId()), CvRenewalOrder::getInstallId, bo.getInstallId());
        lqw.eq(bo.getRequestedPackageId() != null, CvRenewalOrder::getRequestedPackageId, bo.getRequestedPackageId());
        lqw.like(StringUtils.isNotBlank(bo.getRequestedPackageName()), CvRenewalOrder::getRequestedPackageName, bo.getRequestedPackageName());
        lqw.eq(StringUtils.isNotBlank(bo.getRequestedEdition()), CvRenewalOrder::getRequestedEdition, bo.getRequestedEdition());
        lqw.eq(StringUtils.isNotBlank(bo.getRenewalPeriod()), CvRenewalOrder::getRenewalPeriod, bo.getRenewalPeriod());
        lqw.like(StringUtils.isNotBlank(bo.getContactName()), CvRenewalOrder::getContactName, bo.getContactName());
        lqw.like(StringUtils.isNotBlank(bo.getContactEmail()), CvRenewalOrder::getContactEmail, bo.getContactEmail());
        lqw.like(StringUtils.isNotBlank(bo.getContactPhone()), CvRenewalOrder::getContactPhone, bo.getContactPhone());
        lqw.eq(StringUtils.isNotBlank(bo.getIdempotencyKey()), CvRenewalOrder::getIdempotencyKey, bo.getIdempotencyKey());
        lqw.eq(StringUtils.isNotBlank(bo.getRequestSource()), CvRenewalOrder::getRequestSource, bo.getRequestSource());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderStatus()), CvRenewalOrder::getOrderStatus, bo.getOrderStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getIssueStatus()), CvRenewalOrder::getIssueStatus, bo.getIssueStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getPayChannel()), CvRenewalOrder::getPayChannel, bo.getPayChannel());
        lqw.like(StringUtils.isNotBlank(bo.getIssuedLicenseId()), CvRenewalOrder::getIssuedLicenseId, bo.getIssuedLicenseId());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            CvRenewalOrder::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(CvRenewalOrder::getCreateTime);
        lqw.orderByAsc(CvRenewalOrder::getId);
        return lqw;
    }

    private void validateRenewalMetadata(CvRenewalOrderBo bo) {
        if (bo == null) {
            throw new ServiceException("Renewal order metadata cannot be empty");
        }
        validateCustomerCanRenew(bo.getCustomerId());
        validatePackageMetadata(bo.getRequestedPackageId());
        validateLicenseMetadata(bo.getLicenseId(), bo.getCustomerId(), "Original renewal license");
        if (StringUtils.isNotBlank(bo.getIssuedLicenseId())) {
            validateLicenseMetadata(bo.getIssuedLicenseId(), bo.getCustomerId(), "Issued renewal license");
        }
    }

    private void validatePackageMetadata(Long packageId) {
        if (packageId == null) {
            return;
        }
        SysTenantPackage tenantPackage = tenantPackageMapper.selectById(packageId);
        if (tenantPackage == null || "1".equals(tenantPackage.getDelFlag())) {
            throw new ServiceException("Requested renewal package does not exist");
        }
        if (!"0".equals(tenantPackage.getStatus())) {
            throw new ServiceException("Requested renewal package is disabled");
        }
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

    private void validateLicenseMetadata(String licenseId, Long customerId, String label) {
        if (StringUtils.isBlank(licenseId)) {
            return;
        }
        CvLicenseIssue issue = findIssueByLicenseId(licenseId);
        if (issue == null) {
            throw new ServiceException(label + " metadata does not exist");
        }
        if (!Objects.equals(issue.getCustomerId(), customerId)) {
            throw new ServiceException(label + " metadata does not match vendor customer");
        }
        if (isRevokedIssue(issue)) {
            throw new ServiceException(label + " is revoked");
        }
    }

    private void validateIssuedLicenseMetadata(String licenseId, CvRenewalOrder order) {
        if (StringUtils.isBlank(licenseId)) {
            return;
        }
        CvLicenseIssue issue = findIssueByLicenseId(licenseId);
        if (issue == null) {
            throw new ServiceException("Issued renewal license metadata does not exist");
        }
        if (!Objects.equals(issue.getCustomerId(), order.getCustomerId())) {
            throw new ServiceException("Issued renewal license metadata does not match vendor customer");
        }
        if (isRevokedIssue(issue)) {
            throw new ServiceException("Issued renewal license is revoked");
        }
        if (StringUtils.isNotBlank(order.getInstallId()) && !Objects.equals(order.getInstallId(), issue.getInstallId())) {
            throw new ServiceException("Issued renewal license installId does not match renewal order");
        }
        if (order.getRequestedPackageId() != null && !Objects.equals(order.getRequestedPackageId(), issue.getPackageId())) {
            throw new ServiceException("Issued renewal license package does not match renewal order");
        }
        if (StringUtils.isNotBlank(order.getRequestedEdition()) && !Objects.equals(order.getRequestedEdition(), issue.getEdition())) {
            throw new ServiceException("Issued renewal license edition does not match renewal order");
        }
        if (StringUtils.isNotBlank(order.getRequestedPackageName()) && !Objects.equals(order.getRequestedPackageName(), issue.getPackageName())) {
            throw new ServiceException("Issued renewal license package name does not match renewal order");
        }
    }

    private CvRenewalOrder findRenewalOrderForCallback(CvRenewalCallbackRequest request) {
        if (request == null) {
            throw new ServiceException("Renewal callback metadata cannot be empty");
        }
        if (request.getId() != null) {
            return baseMapper.selectById(request.getId());
        }
        if (StringUtils.isNotBlank(request.getOrderNo())) {
            List<CvRenewalOrder> orders = baseMapper.selectList(new LambdaQueryWrapper<CvRenewalOrder>()
                .eq(CvRenewalOrder::getOrderNo, request.getOrderNo())
                .last("limit 2"));
            if (orders.size() > 1) {
                throw new ServiceException("Duplicate renewal order metadata");
            }
            return orders.isEmpty() ? null : orders.get(0);
        }
        throw new ServiceException("Renewal callback must reference id or orderNo");
    }

    private String resolveCallbackIssueStatus(String targetStatus, String issuedLicenseId, boolean manualPath) {
        if (StringUtils.isNotBlank(issuedLicenseId)) {
            return ISSUE_STATUS_ISSUED;
        }
        if (manualPath) {
            return ISSUE_STATUS_PENDING;
        }
        if (ORDER_STATUS_AUTHORIZED.equals(targetStatus)) {
            return ISSUE_STATUS_ISSUING;
        }
        return ISSUE_STATUS_PENDING;
    }

    private String resolveRetryIssueStatus(CvRenewalOrder order) {
        if (StringUtils.isBlank(order.getIssuedLicenseId())) {
            return ISSUE_STATUS_ISSUING;
        }
        validateIssuedLicenseMetadata(order.getIssuedLicenseId(), order);
        return ISSUE_STATUS_ISSUED;
    }

    private boolean isManualPath(CvRenewalOrder order, CvRenewalCallbackRequest request) {
        String payChannel = normalizeStatus(request != null && StringUtils.isNotBlank(request.getPayChannel())
            ? request.getPayChannel()
            : order.getPayChannel());
        String requestSource = normalizeStatus(order.getRequestSource());
        return "manual".equals(payChannel) || "open-api".equals(requestSource);
    }

    private Date resolveCallbackPaidTime(Date requestPaidTime, Date existingPaidTime) {
        return requestPaidTime != null ? requestPaidTime : existingPaidTime;
    }

    private String normalizeIssueStatus(String status) {
        String normalized = normalizeStatus(status);
        if ("pending".equals(normalized)) {
            return ISSUE_STATUS_PENDING;
        }
        if ("failed".equals(normalized)) {
            return ISSUE_STATUS_FAILED;
        }
        if (ISSUE_STATUS_PENDING.equals(normalized)
            || ISSUE_STATUS_ISSUING.equals(normalized)
            || ISSUE_STATUS_ISSUED.equals(normalized)
            || ISSUE_STATUS_FAILED.equals(normalized)) {
            return normalized;
        }
        return normalized;
    }

    private String normalizeCallbackStatus(String orderStatus) {
        String normalized = normalizeStatus(orderStatus);
        if (StringUtils.isBlank(normalized)) {
            throw new ServiceException("Renewal callback must include explicit orderStatus");
        }
        if (!ORDER_STATUS_PAID.equals(normalized) && !ORDER_STATUS_AUTHORIZED.equals(normalized)) {
            throw new ServiceException("Unsupported renewal callback status");
        }
        return normalized;
    }

    private void assertCallbackTransitionAllowed(String currentStatus, String targetStatus) {
        String normalizedCurrentStatus = normalizeStatus(currentStatus);
        if (!ORDER_STATUS_PAID.equals(targetStatus) && !ORDER_STATUS_AUTHORIZED.equals(targetStatus)) {
            throw new ServiceException("Unsupported renewal callback status");
        }
        if (ORDER_STATUS_PENDING.equals(normalizedCurrentStatus)) {
            return;
        }
        if (ORDER_STATUS_PAID.equals(normalizedCurrentStatus) && ORDER_STATUS_AUTHORIZED.equals(targetStatus)) {
            return;
        }
        throw new ServiceException("Renewal callback cannot overwrite terminal order status");
    }

    private CvLicenseIssue findIssueByLicenseId(String licenseId) {
        List<CvLicenseIssue> issues = licenseIssueMapper.selectList(new LambdaQueryWrapper<CvLicenseIssue>()
            .eq(CvLicenseIssue::getLicenseId, licenseId)
            .last("limit 2"));
        if (issues.size() > 1) {
            throw new ServiceException("Duplicate license issue metadata");
        }
        return issues.isEmpty() ? null : issues.get(0);
    }

    private boolean isRevokedIssue(CvLicenseIssue issue) {
        return issue.getRevokedTime() != null || ISSUE_STATUS_REVOKED.equals(normalizeStatus(issue.getIssueStatus()));
    }

    private boolean isDisabledCustomer(String customerStatus) {
        String normalizedStatus = normalizeStatus(customerStatus);
        return CUSTOMER_STATUS_DISABLED.equals(normalizedStatus)
            || CUSTOMER_STATUS_INACTIVE.equals(normalizedStatus)
            || CUSTOMER_STATUS_STOPPED.equals(normalizedStatus)
            || CUSTOMER_STATUS_SUSPENDED.equals(normalizedStatus)
            || CUSTOMER_STATUS_NUMERIC_DISABLED.equals(normalizedStatus);
    }

    private String normalizeStatus(String status) {
        return StringUtils.isBlank(status) ? null : status.trim().toLowerCase();
    }
}
