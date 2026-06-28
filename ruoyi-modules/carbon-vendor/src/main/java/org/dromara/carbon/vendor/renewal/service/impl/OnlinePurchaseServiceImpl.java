package org.dromara.carbon.vendor.renewal.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.renewal.config.VendorPaymentProperties;
import org.dromara.carbon.vendor.customer.domain.CvCustomer;
import org.dromara.carbon.vendor.renewal.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.renewal.domain.bo.OnlinePurchaseCreateBo;
import org.dromara.carbon.vendor.renewal.domain.bo.PaymentNotifyBo;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssueRequest;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssueResult;
import org.dromara.carbon.vendor.license.domain.CvTemplateEntitlement;
import org.dromara.carbon.vendor.renewal.domain.vo.OnlinePurchaseOrderVo;
import org.dromara.carbon.vendor.customer.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.renewal.mapper.CvRenewalOrderMapper;
import org.dromara.carbon.vendor.license.service.ICvLicenseIssueService;
import org.dromara.carbon.vendor.renewal.service.IOnlinePurchaseService;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.domain.vo.SysTenantPackageVo;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class OnlinePurchaseServiceImpl implements IOnlinePurchaseService {

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_PAID = "paid";
    private static final String ISSUE_PENDING = "pending_issue";
    private static final String ISSUE_READY = "ready_to_issue";
    private static final String ISSUE_ISSUED = "issued";
    private static final String ISSUE_FAILED = "issue_failed";
    private static final String ISSUE_REVOKED = "revoked";
    private static final String CHANNEL_WECHAT = "WECHAT";
    private static final String CHANNEL_ALIPAY = "ALIPAY";
    private static final String ONLINE_ISSUER = "online-purchase";

    private final CvRenewalOrderMapper renewalOrderMapper;
    private final CvCustomerMapper customerMapper;
    private final CvLicenseIssueMapper licenseIssueMapper;
    private final SysTenantPackageMapper tenantPackageMapper;
    private final VendorPaymentProperties paymentProperties;
    private final ICvLicenseIssueService licenseIssueService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OnlinePurchaseOrderVo createOrder(OnlinePurchaseCreateBo bo) {
        String channel = normalizeChannel(bo.getPayChannel());
        assertChannelEnabled(channel);

        if (StrUtil.isNotBlank(bo.getIdempotencyKey())) {
            CvRenewalOrder existing = renewalOrderMapper.selectOne(new LambdaQueryWrapper<CvRenewalOrder>()
                .eq(CvRenewalOrder::getIdempotencyKey, bo.getIdempotencyKey()));
            if (existing != null) {
                return toVo(existing);
            }
        }

        SysTenantPackageVo tenantPackage = TenantHelper.ignore(() -> tenantPackageMapper.selectVoOne(new LambdaQueryWrapper<SysTenantPackage>()
            .eq(SysTenantPackage::getPackageId, bo.getPackageId())
            .eq(SysTenantPackage::getStatus, SystemConstants.NORMAL)
            .eq(SysTenantPackage::getOnlinePurchaseEnabled, Boolean.TRUE)));
        if (tenantPackage == null) {
            throw new ServiceException("套餐不可在线购买");
        }

        CvCustomer customer = resolveCustomer(bo);
        CvRenewalOrder order = new CvRenewalOrder();
        order.setOrderNo("PO" + IdUtil.getSnowflakeNextIdStr());
        order.setCustomerId(customer.getId());
        order.setLicenseId(bo.getLicenseId());
        order.setInstallId(bo.getInstallId());
        order.setRequestedPackageId(tenantPackage.getPackageId());
        order.setRequestedPackageName(tenantPackage.getPackageName());
        order.setRequestedEdition(tenantPackage.getPackageName());
        order.setRenewalPeriod(tenantPackage.getBillingCycle());
        order.setContactName(firstNotBlank(bo.getContactName(), customer.getContactName()));
        order.setContactEmail(firstNotBlank(bo.getContactEmail(), customer.getContactEmail()));
        order.setContactPhone(firstNotBlank(bo.getContactPhone(), customer.getContactPhone()));
        order.setIdempotencyKey(bo.getIdempotencyKey());
        order.setRequestSource("enterprise_local");
        order.setOrderStatus(STATUS_PENDING);
        order.setIssueStatus(ISSUE_PENDING);
        order.setPayChannel(channel);
        order.setAmount(defaultAmount(tenantPackage.getPriceAmount()));
        order.setCurrency(firstNotBlank(tenantPackage.getPriceCurrency(), "CNY"));
        order.setPayUrl(buildCashierUrl(order, bo.getReturnUrl()));
        Date now = new Date();
        order.setCreateTime(now);
        order.setUpdateTime(now);
        renewalOrderMapper.insert(order);
        return toVo(order);
    }

    @Override
    public OnlinePurchaseOrderVo queryOrder(String orderNo) {
        CvRenewalOrder order = renewalOrderMapper.selectOne(new LambdaQueryWrapper<CvRenewalOrder>()
            .eq(CvRenewalOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        return toVo(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OnlinePurchaseOrderVo markPaid(PaymentNotifyBo bo) {
        String channel = normalizeChannel(bo.getPayChannel());
        CvRenewalOrder order = renewalOrderMapper.selectOne(new LambdaQueryWrapper<CvRenewalOrder>()
            .eq(CvRenewalOrder::getOrderNo, bo.getOrderNo()));
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (STATUS_PAID.equals(order.getOrderStatus())) {
            return toVo(order);
        }
        if (!StrUtil.equals(channel, order.getPayChannel())) {
            throw new ServiceException("支付渠道不匹配");
        }
        if (bo.getAmount() == null || bo.getAmount().compareTo(order.getAmount()) != 0) {
            throw new ServiceException("支付金额不匹配");
        }
        if (StrUtil.isNotBlank(bo.getCurrency()) && !StrUtil.equalsIgnoreCase(bo.getCurrency(), order.getCurrency())) {
            throw new ServiceException("支付币种不匹配");
        }
        if (paymentProperties.isRequireSignature() && StrUtil.isBlank(bo.getSignature())) {
            throw new ServiceException("支付回调缺少签名");
        }
        if (!isPaidStatus(channel, bo.getTradeStatus())) {
            throw new ServiceException("支付状态未完成");
        }

        order.setOrderStatus(STATUS_PAID);
        order.setIssueStatus(ISSUE_READY);
        order.setPayTradeNo(bo.getTradeNo());
        order.setPaidTime(new Date());
        order.setUpdateTime(new Date());
        renewalOrderMapper.updateById(order);
        issueLicenseIfConfigured(order);
        return toVo(order);
    }

    private void issueLicenseIfConfigured(CvRenewalOrder order) {
        SysTenantPackageVo tenantPackage = TenantHelper.ignore(() -> tenantPackageMapper.selectVoById(order.getRequestedPackageId()));
        if (tenantPackage == null || !Boolean.TRUE.equals(tenantPackage.getLicenseAutoIssueEnabled())) {
            return;
        }
        try {
            CvLicenseIssueResult result = licenseIssueService.issueManualLicense(buildLicenseIssueRequest(order, tenantPackage));
            CvRenewalOrder update = new CvRenewalOrder();
            update.setId(order.getId());
            update.setUpdateTime(new Date());
            if (result.isIssued() && result.getLicenseIssue() != null) {
                update.setIssueStatus(ISSUE_ISSUED);
                update.setIssuedLicenseId(result.getLicenseIssue().getLicenseId());
                order.setIssueStatus(ISSUE_ISSUED);
                order.setIssuedLicenseId(result.getLicenseIssue().getLicenseId());
            } else {
                update.setIssueStatus(ISSUE_FAILED);
                order.setIssueStatus(ISSUE_FAILED);
            }
            renewalOrderMapper.updateById(update);
        } catch (Exception e) {
            CvRenewalOrder update = new CvRenewalOrder();
            update.setId(order.getId());
            update.setIssueStatus(ISSUE_FAILED);
            update.setUpdateTime(new Date());
            renewalOrderMapper.updateById(update);
            order.setIssueStatus(ISSUE_FAILED);
        }
    }

    private CvLicenseIssueRequest buildLicenseIssueRequest(CvRenewalOrder order, SysTenantPackageVo tenantPackage) {
        if (StrUtil.isBlank(order.getInstallId())) {
            throw new ServiceException("在线购买订单缺少 installId，无法自动签发 License");
        }
        if (StrUtil.isBlank(tenantPackage.getLicenseKeyId())) {
            throw new ServiceException("套餐未配置 License 签名 keyId");
        }
        List<String> features = parseFeatureCodes(tenantPackage.getLicenseFeatureCodes());
        if (features.isEmpty()) {
            throw new ServiceException("套餐未配置 License 功能码");
        }
        List<CvTemplateEntitlement> entitlements = parseTemplateEntitlements(tenantPackage.getLicenseTemplateEntitlements());
        if (entitlements.isEmpty()) {
            throw new ServiceException("套餐未配置 License 模板授权");
        }

        Date validFrom = resolveNextValidFrom(order);
        Date validTo = Date.from(validFrom.toInstant().plus(resolveValidityDays(tenantPackage), ChronoUnit.DAYS));
        CvLicenseIssueRequest request = new CvLicenseIssueRequest();
        request.setCustomerId(order.getCustomerId());
        request.setKeyId(tenantPackage.getLicenseKeyId());
        request.setPackageId(order.getRequestedPackageId());
        request.setPackageName(order.getRequestedPackageName());
        request.setEdition(order.getRequestedEdition());
        request.setFeatures(features);
        request.setTemplateEntitlements(entitlements);
        request.setInstallId(order.getInstallId());
        request.setValidFrom(validFrom);
        request.setValidTo(validTo);
        request.setIssuedAt(Date.from(Instant.now()));
        request.setIssuedBy(ONLINE_ISSUER);
        return request;
    }

    private Date resolveNextValidFrom(CvRenewalOrder order) {
        Date paidTime = Objects.requireNonNullElseGet(order.getPaidTime(), Date::new);
        CvLicenseIssue latestIssue = licenseIssueMapper.selectOne(new LambdaQueryWrapper<CvLicenseIssue>()
            .eq(CvLicenseIssue::getCustomerId, order.getCustomerId())
            .gt(CvLicenseIssue::getValidTo, paidTime)
            .and(wrapper -> wrapper.isNull(CvLicenseIssue::getRevokedTime)
                .and(statusWrapper -> statusWrapper.isNull(CvLicenseIssue::getIssueStatus)
                    .or()
                    .ne(CvLicenseIssue::getIssueStatus, ISSUE_REVOKED)))
            .orderByDesc(CvLicenseIssue::getValidTo)
            .orderByDesc(CvLicenseIssue::getId),
            false);
        if (latestIssue == null || latestIssue.getValidTo() == null) {
            return paidTime;
        }
        return latestIssue.getValidTo();
    }

    private CvCustomer resolveCustomer(OnlinePurchaseCreateBo bo) {
        String customerCode = firstNotBlank(bo.getCustomerCode(), bo.getLicenseId(), bo.getInstallId(), "CUST-" + IdUtil.getSnowflakeNextIdStr());
        CvCustomer customer = customerMapper.selectOne(new LambdaQueryWrapper<CvCustomer>()
            .eq(CvCustomer::getCustomerCode, customerCode));
        if (customer != null) {
            return customer;
        }

        customer = new CvCustomer();
        customer.setCustomerCode(customerCode);
        customer.setCustomerName(bo.getCustomerName());
        customer.setContactName(bo.getContactName());
        customer.setContactEmail(bo.getContactEmail());
        customer.setContactPhone(bo.getContactPhone());
        customer.setCustomerStatus("active");
        Date now = new Date();
        customer.setCreateTime(now);
        customer.setUpdateTime(now);
        customerMapper.insert(customer);
        return customer;
    }

    private String buildCashierUrl(CvRenewalOrder order, String returnUrl) {
        return UriComponentsBuilder.fromUriString(paymentProperties.getCashierBaseUrl())
            .queryParam("orderNo", order.getOrderNo())
            .queryParam("channel", order.getPayChannel())
            .queryParam("amount", order.getAmount())
            .queryParam("currency", order.getCurrency())
            .queryParam("returnUrl", firstNotBlank(returnUrl, ""))
            .build()
            .encode()
            .toUriString();
    }

    private String normalizeChannel(String channel) {
        String normalized = StrUtil.trimToEmpty(channel).toUpperCase(Locale.ROOT);
        if (!CHANNEL_WECHAT.equals(normalized) && !CHANNEL_ALIPAY.equals(normalized)) {
            throw new ServiceException("仅支持微信和支付宝支付");
        }
        return normalized;
    }

    private void assertChannelEnabled(String channel) {
        if (CHANNEL_WECHAT.equals(channel) && !paymentProperties.getWechat().isEnabled()) {
            throw new ServiceException("微信支付未启用");
        }
        if (CHANNEL_ALIPAY.equals(channel) && !paymentProperties.getAlipay().isEnabled()) {
            throw new ServiceException("支付宝支付未启用");
        }
    }

    private boolean isPaidStatus(String channel, String status) {
        String normalized = StrUtil.trimToEmpty(status).toUpperCase(Locale.ROOT);
        if (CHANNEL_WECHAT.equals(channel)) {
            return "SUCCESS".equals(normalized);
        }
        return "TRADE_SUCCESS".equals(normalized) || "TRADE_FINISHED".equals(normalized);
    }

    private OnlinePurchaseOrderVo toVo(CvRenewalOrder order) {
        OnlinePurchaseOrderVo vo = new OnlinePurchaseOrderVo();
        vo.setOrderNo(order.getOrderNo());
        vo.setPackageId(order.getRequestedPackageId());
        vo.setPackageName(order.getRequestedPackageName());
        vo.setPayChannel(order.getPayChannel());
        vo.setAmount(order.getAmount());
        vo.setCurrency(order.getCurrency());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setIssueStatus(order.getIssueStatus());
        vo.setPayUrl(order.getPayUrl());
        vo.setPayForm(order.getPayForm());
        vo.setIssuedLicenseId(order.getIssuedLicenseId());
        return vo;
    }

    private List<String> parseFeatureCodes(String featureCodes) {
        if (StrUtil.isBlank(featureCodes)) {
            return Collections.emptyList();
        }
        return Arrays.stream(featureCodes.split(","))
            .map(String::trim)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .toList();
    }

    private List<CvTemplateEntitlement> parseTemplateEntitlements(String value) {
        if (StrUtil.isBlank(value)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<CvTemplateEntitlement>>() {
            });
        } catch (Exception e) {
            throw new ServiceException("套餐 License 模板授权 JSON 无效");
        }
    }

    private long resolveValidityDays(SysTenantPackageVo tenantPackage) {
        if (tenantPackage.getLicenseValidityDays() != null && tenantPackage.getLicenseValidityDays() > 0) {
            return tenantPackage.getLicenseValidityDays();
        }
        String cycle = StrUtil.trimToEmpty(tenantPackage.getBillingCycle()).toUpperCase(Locale.ROOT);
        return switch (cycle) {
            case "MONTH" -> 31L;
            case "QUARTER" -> 93L;
            case "HALF_YEAR" -> 183L;
            case "YEAR" -> 365L;
            default -> 365L;
        };
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }

}
