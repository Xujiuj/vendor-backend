package org.dromara.carbon.vendor.license.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.carbon.vendor.customer.domain.CvCustomer;
import org.dromara.carbon.vendor.license.domain.CvSigningKey;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.license.domain.bo.CvLicenseIssueBo;
import org.dromara.carbon.vendor.license.domain.CvLicenseEnvelope;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssueRequest;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssueResult;
import org.dromara.carbon.vendor.license.domain.CvLicensePayload;
import org.dromara.carbon.vendor.license.domain.CvLicenseReissueRequest;
import org.dromara.carbon.vendor.license.domain.CvLicenseRevokeRequest;
import org.dromara.carbon.vendor.license.domain.CvTemplateEntitlement;
import org.dromara.carbon.vendor.license.domain.vo.CvLicenseIssueVo;
import org.dromara.carbon.vendor.customer.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.license.mapper.CvSigningKeyMapper;
import org.dromara.carbon.vendor.license.service.CvLicensePrivateKeyProvider;
import org.dromara.carbon.vendor.license.service.ICvLicenseIssueService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Vendor license issue service implementation.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class CvLicenseIssueServiceImpl implements ICvLicenseIssueService {

    private static final String SCHEMA_VERSION = "license.v1";
    private static final String ALGORITHM = "RS256";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String ISSUE_STATUS_ISSUED = "issued";
    private static final String ISSUE_STATUS_REVOKED = "revoked";
    private static final String ISSUE_TYPE_MANUAL = "manual";
    private static final String ISSUE_TYPE_REISSUE = "reissue";
    private static final String PENDING_INSTALL_ID = CvLicenseInstallBindingSupport.PENDING_INSTALL_ID;
    private static final String CUSTOMER_STATUS_DISABLED = "disabled";
    private static final String CUSTOMER_STATUS_INACTIVE = "inactive";
    private static final String CUSTOMER_STATUS_STOPPED = "stopped";
    private static final String CUSTOMER_STATUS_SUSPENDED = "suspended";
    private static final String CUSTOMER_STATUS_NUMERIC_DISABLED = "1";

    private final CvLicenseIssueMapper baseMapper;
    private final CvCustomerMapper customerMapper;
    private final CvSigningKeyMapper signingKeyMapper;
    private final SysTenantPackageMapper tenantPackageMapper;
    private final CvLicensePrivateKeyProvider privateKeyProvider;
    private final ObjectMapper objectMapper;

    @Override
    public TableDataInfo<CvLicenseIssueVo> selectPageLicenseIssueList(CvLicenseIssueBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CvLicenseIssue> lqw = buildQueryWrapper(bo);
        Page<CvLicenseIssueVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        page.getRecords().forEach(this::prepareIssueView);
        return TableDataInfo.build(page);
    }

    @Override
    public CvLicenseIssueVo selectLicenseIssueById(Long id) {
        CvLicenseIssueVo issue = baseMapper.selectVoById(id);
        prepareIssueView(issue);
        return issue;
    }

    @Override
    public CvLicenseIssueResult issueManualLicense(CvLicenseIssueRequest request) {
        return issueLicense(request, ISSUE_TYPE_MANUAL, false);
    }

    @Override
    public CvLicenseIssueResult reissueRevokedLicense(CvLicenseReissueRequest request) {
        if (request == null) {
            return CvLicenseIssueResult.failed("MALFORMED_REQUEST", "重签请求不能为空");
        }
        if (StringUtils.isBlank(request.getSourceLicenseId())) {
            return CvLicenseIssueResult.failed("MALFORMED_REQUEST", "重签请求缺少原授权编号");
        }

        CvLicenseIssue sourceIssue = findIssueByLicenseId(request.getSourceLicenseId());
        if (sourceIssue == null) {
            return CvLicenseIssueResult.failed("SOURCE_LICENSE_NOT_FOUND", "原撤销授权不存在");
        }
        if (!isRevokedIssue(sourceIssue)) {
            return CvLicenseIssueResult.failed("SOURCE_LICENSE_NOT_REVOKED", "原授权尚未撤销，不能重签");
        }
        if (request.getCustomerId() != null && !Objects.equals(request.getCustomerId(), sourceIssue.getCustomerId())) {
            return CvLicenseIssueResult.failed("SOURCE_LICENSE_CUSTOMER_MISMATCH", "原授权不属于当前客户");
        }
        if (request.getPackageId() == null) {
            request.setPackageId(sourceIssue.getPackageId());
        }
        if (StringUtils.equals(request.getLicenseId(), sourceIssue.getLicenseId())) {
            return CvLicenseIssueResult.failed("DUPLICATE_LICENSE_ID", "重签授权必须使用新的授权编号");
        }
        if (hasReissueFromSource(sourceIssue.getLicenseId())) {
            return CvLicenseIssueResult.failed("SOURCE_LICENSE_ALREADY_REISSUED", "原撤销授权已重签，不能重复重签");
        }

        String effectiveInstallId = resolveReissueInstallId(request, sourceIssue);
        if (effectiveInstallId == null) {
            return CvLicenseIssueResult.failed(
                "INSTALL_ID_CHANGE_NOT_ALLOWED",
                "重签部署指纹必须与原撤销授权一致，除非明确允许变更部署指纹");
        }

        request.setInstallId(effectiveInstallId);
        request.setIssueType(ISSUE_TYPE_REISSUE);
        return issueLicense(request, ISSUE_TYPE_REISSUE, true);
    }

    @Override
    public int revokeLicense(CvLicenseRevokeRequest request) {
        if (request == null || StringUtils.isBlank(request.getLicenseId())) {
            throw new ServiceException("授权编号不能为空");
        }
        CvLicenseIssue issue = findIssueByLicenseId(request.getLicenseId());
        if (issue == null) {
            throw new ServiceException("厂商端授权签发记录不存在");
        }
        if (isRevokedIssue(issue)) {
            throw new ServiceException("授权已撤销，不能重复撤销");
        }
        CvLicenseIssue update = new CvLicenseIssue();
        update.setId(issue.getId());
        update.setIssueStatus(ISSUE_STATUS_REVOKED);
        update.setRevokedTime(Objects.requireNonNullElseGet(request.getRevokedAt(), Date::new));
        update.setRevokedBy(StringUtils.blankToDefault(request.getRevokedBy(), "vendor-system"));
        update.setRevokeReason(request.getRevokeReason());
        return baseMapper.updateById(update);
    }

    @Override
    public int deleteLicenseIssueByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return 0;
        }
        return baseMapper.deleteByIds(Arrays.asList(ids));
    }

    private CvLicenseIssueResult issueLicense(CvLicenseIssueRequest request, String defaultIssueType, boolean allowRevokedHistory) {
        CvLicenseIssueResult validation = validateIssueRequest(request);
        if (!validation.isIssued()) {
            return validation;
        }

        CvCustomer customer = customerMapper.selectById(request.getCustomerId());
        if (customer == null) {
            return CvLicenseIssueResult.failed("CUSTOMER_NOT_FOUND", "厂商客户不存在");
        }
        if (isDisabledCustomer(customer.getCustomerStatus())) {
            return CvLicenseIssueResult.failed("CUSTOMER_DISABLED", "客户已停用，不能签发授权");
        }
        SysTenantPackage tenantPackage;
        try {
            tenantPackage = resolveTenantPackage(request);
        } catch (ServiceException e) {
            return CvLicenseIssueResult.failed("PACKAGE_INVALID", e.getMessage());
        }

        request.setCustomerCode(customer.getCustomerCode());
        request.setCustomerName(customer.getCustomerName());
        request.setPackageId(tenantPackage.getPackageId());
        request.setPackageName(tenantPackage.getPackageName());
        request.setEdition(tenantPackage.getPackageName());
        try {
            applyLicensePackageEntitlements(request, tenantPackage);
        } catch (ServiceException e) {
            return CvLicenseIssueResult.failed("PACKAGE_ENTITLEMENT_INVALID", e.getMessage());
        }
        applyIssueDefaults(request, tenantPackage);

        List<CvLicenseIssue> existingIssues = findIssuesForCustomerInstall(request.getCustomerId(), request.getInstallId());
        if (!allowRevokedHistory && hasRevokedHistory(existingIssues)) {
            return CvLicenseIssueResult.failed(
                "REVOKED_LICENSE_REISSUE_BLOCKED",
                "该客户与部署指纹存在撤销历史，需人工复核后重签");
        }
        if (hasExactDuplicate(existingIssues, request, !allowRevokedHistory)) {
            return CvLicenseIssueResult.failed(
                "DUPLICATE_LICENSE_ISSUE",
                "相同客户、部署指纹和有效期已存在授权，不能重复签发");
        }
        CvLicenseIssue overlap = findOverlappingCustomerIssue(request);
        if (overlap != null) {
            return CvLicenseIssueResult.failed(
                "CUSTOMER_LICENSE_PERIOD_OVERLAP",
                "同一客户已存在有效期重叠的授权，不能为多个套餐签发重叠生效时间");
        }

        Date issuedTime = Objects.requireNonNullElseGet(request.getIssuedAt(), Date::new);
        CvSigningKey signingKey = findActiveSigningKey(request, issuedTime);
        if (signingKey == null) {
            return CvLicenseIssueResult.failed("SIGNING_KEY_UNAVAILABLE", "没有可用的启用签名密钥");
        }

        String signingKeyMaterial = privateKeyProvider.resolvePrivateKeyPem(signingKey.getPrivateKeyRef());
        if (StringUtils.isBlank(signingKeyMaterial)) {
            return CvLicenseIssueResult.failed("PRIVATE_KEY_UNAVAILABLE", "签名私钥不可用");
        }

        try {
            CvLicensePayload payload = buildPayload(request, signingKey, issuedTime);
            String canonicalPayload = objectMapper.writeValueAsString(payload);
            String signatureText = signPayload(signingKeyMaterial, canonicalPayload.getBytes(StandardCharsets.UTF_8));
            CvLicenseEnvelope envelope = buildEnvelope(payload, signingKey, signatureText);
            String licenseContent = objectMapper.writeValueAsString(envelope);
            CvLicenseIssue issue = buildIssueRecord(request, signingKey, issuedTime, canonicalPayload, signatureText, payload, defaultIssueType);
            try {
                baseMapper.insert(issue);
            } catch (DuplicateKeyException e) {
                if (ISSUE_TYPE_REISSUE.equals(issue.getIssueType())
                    && StringUtils.isNotBlank(issue.getSourceLicenseId())
                    && isSourceLicenseUniqueViolation(e)) {
                    log.warn("Rejected duplicate reissue for revoked sourceLicenseId={}", issue.getSourceLicenseId());
                    return CvLicenseIssueResult.failed(
                        "SOURCE_LICENSE_ALREADY_REISSUED",
                        "原撤销授权已重签，不能重复重签");
                }
                throw e;
            }
            return CvLicenseIssueResult.issued(licenseContent, issue);
        } catch (Exception e) {
            log.error("Failed to issue vendor license for customerId={}, installId={}, keyId={}",
                request.getCustomerId(), request.getInstallId(), request.getKeyId(), e);
            return CvLicenseIssueResult.failed("ISSUE_FAILED", "授权签发失败");
        }
    }

    private LambdaQueryWrapper<CvLicenseIssue> buildQueryWrapper(CvLicenseIssueBo bo) {
        Map<String, Object> params = bo.getParams() == null ? Map.of() : bo.getParams();
        LambdaQueryWrapper<CvLicenseIssue> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvLicenseIssue::getId, bo.getId());
        lqw.like(StringUtils.isNotBlank(bo.getLicenseId()), CvLicenseIssue::getLicenseId, bo.getLicenseId());
        lqw.eq(bo.getCustomerId() != null, CvLicenseIssue::getCustomerId, bo.getCustomerId());
        lqw.like(StringUtils.isNotBlank(bo.getKeyId()), CvLicenseIssue::getKeyId, bo.getKeyId());
        lqw.eq(StringUtils.isNotBlank(bo.getAlgorithm()), CvLicenseIssue::getAlgorithm, bo.getAlgorithm());
        lqw.eq(StringUtils.isNotBlank(bo.getSchemaVersion()), CvLicenseIssue::getSchemaVersion, bo.getSchemaVersion());
        lqw.eq(bo.getPackageId() != null, CvLicenseIssue::getPackageId, bo.getPackageId());
        lqw.like(StringUtils.isNotBlank(bo.getPackageName()), CvLicenseIssue::getPackageName, bo.getPackageName());
        lqw.eq(StringUtils.isNotBlank(bo.getEdition()), CvLicenseIssue::getEdition, bo.getEdition());
        lqw.like(StringUtils.isNotBlank(bo.getInstallId()), CvLicenseIssue::getInstallId, bo.getInstallId());
        lqw.eq(StringUtils.isNotBlank(bo.getIssueStatus()), CvLicenseIssue::getIssueStatus, bo.getIssueStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getIssueType()), CvLicenseIssue::getIssueType, bo.getIssueType());
        lqw.eq(StringUtils.isNotBlank(bo.getSourceLicenseId()), CvLicenseIssue::getSourceLicenseId, bo.getSourceLicenseId());
        lqw.like(StringUtils.isNotBlank(bo.getIssuedBy()), CvLicenseIssue::getIssuedBy, bo.getIssuedBy());
        lqw.like(StringUtils.isNotBlank(bo.getRevokedBy()), CvLicenseIssue::getRevokedBy, bo.getRevokedBy());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            CvLicenseIssue::getIssuedTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(CvLicenseIssue::getIssuedTime);
        lqw.orderByAsc(CvLicenseIssue::getId);
        return lqw;
    }

    private CvLicenseIssueResult validateIssueRequest(CvLicenseIssueRequest request) {
        if (request == null) {
            return CvLicenseIssueResult.failed("MALFORMED_REQUEST", "签发请求不能为空");
        }
        if (request.getCustomerId() == null
            || request.getPackageId() == null
            || StringUtils.isBlank(request.getIssuedBy())
            || request.getValidFrom() == null || request.getValidTo() == null) {
            return CvLicenseIssueResult.failed("MALFORMED_REQUEST", "签发请求缺少必要字段");
        }
        String schemaVersion = Objects.requireNonNullElse(request.getSchemaVersion(), SCHEMA_VERSION);
        String algorithm = Objects.requireNonNullElse(request.getAlgorithm(), ALGORITHM);
        if (!SCHEMA_VERSION.equals(schemaVersion)) {
            return CvLicenseIssueResult.failed("UNSUPPORTED_SCHEMA", "授权文件版本不受支持");
        }
        if (!ALGORITHM.equals(algorithm)) {
            return CvLicenseIssueResult.failed("UNSUPPORTED_ALGORITHM", "授权签名算法不受支持");
        }
        if (!request.getValidFrom().before(request.getValidTo())) {
            return CvLicenseIssueResult.failed("INVALID_VALIDITY_WINDOW", "有效期开始日期必须早于结束日期");
        }
        return CvLicenseIssueResult.issued(null, null);
    }

    private SysTenantPackage resolveTenantPackage(CvLicenseIssueRequest request) {
        SysTenantPackage tenantPackage = TenantHelper.ignore(() -> tenantPackageMapper.selectById(request.getPackageId()));
        if (tenantPackage == null || "1".equals(tenantPackage.getDelFlag())) {
            throw new ServiceException("授权套餐不存在，不能签发授权");
        }
        if (!"0".equals(tenantPackage.getStatus())) {
            throw new ServiceException("授权套餐已停用，不能签发授权");
        }
        if (StringUtils.isBlank(tenantPackage.getPackageName())) {
            throw new ServiceException("授权套餐名称为空，不能签发授权");
        }
        return tenantPackage;
    }

    private void applyLicensePackageEntitlements(CvLicenseIssueRequest request, SysTenantPackage tenantPackage) {
        List<String> features = parseFeatureCodes(tenantPackage.getLicenseFeatureCodes());
        if (features.isEmpty()) {
            throw new ServiceException("授权套餐未配置功能码，不能签发授权");
        }
        List<CvTemplateEntitlement> entitlements = parseTemplateEntitlements(tenantPackage.getLicenseTemplateEntitlements());
        if (entitlements.isEmpty()) {
            throw new ServiceException("授权套餐未配置模板授权，不能签发授权");
        }
        request.setFeatures(features);
        request.setTemplateEntitlements(entitlements);
    }

    private List<String> parseFeatureCodes(String featureCodes) {
        if (StringUtils.isBlank(featureCodes)) {
            return Collections.emptyList();
        }
        return Arrays.stream(featureCodes.split("[,;\\s]+"))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .distinct()
            .toList();
    }

    private List<CvTemplateEntitlement> parseTemplateEntitlements(String value) {
        if (StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<CvTemplateEntitlement>>() {
            });
        } catch (Exception e) {
            throw new ServiceException("授权套餐模板授权 JSON 无效");
        }
    }

    private void applyIssueDefaults(CvLicenseIssueRequest request, SysTenantPackage tenantPackage) {
        request.setKeyId(StringUtils.blankToDefault(request.getKeyId(), tenantPackage.getLicenseKeyId()));
        if (StringUtils.isBlank(request.getKeyId())) {
            CvSigningKey signingKey = findAnyActiveSigningKey(Objects.requireNonNullElseGet(request.getIssuedAt(), Date::new));
            if (signingKey != null) {
                request.setKeyId(signingKey.getKeyId());
            }
        }
        if (StringUtils.isBlank(request.getInstallId())) {
            request.setInstallId(PENDING_INSTALL_ID);
        }
    }

    private List<CvLicenseIssue> findIssuesForCustomerInstall(Long customerId, String installId) {
        if (PENDING_INSTALL_ID.equals(installId)) {
            return List.of();
        }
        return baseMapper.selectList(new LambdaQueryWrapper<CvLicenseIssue>()
            .eq(CvLicenseIssue::getCustomerId, customerId)
            .eq(CvLicenseIssue::getInstallId, installId)
            .orderByDesc(CvLicenseIssue::getIssuedTime)
            .orderByDesc(CvLicenseIssue::getId));
    }

    private CvLicenseIssue findIssueByLicenseId(String licenseId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<CvLicenseIssue>()
            .eq(CvLicenseIssue::getLicenseId, licenseId), false);
    }

    private boolean hasReissueFromSource(String sourceLicenseId) {
        return baseMapper.selectCount(new LambdaQueryWrapper<CvLicenseIssue>()
            .eq(CvLicenseIssue::getIssueType, ISSUE_TYPE_REISSUE)
            .eq(CvLicenseIssue::getSourceLicenseId, sourceLicenseId)) > 0;
    }

    private boolean hasRevokedHistory(List<CvLicenseIssue> existingIssues) {
        return existingIssues.stream().anyMatch(this::isRevokedIssue);
    }

    private boolean hasExactDuplicate(List<CvLicenseIssue> existingIssues, CvLicenseIssueRequest request, boolean includeRevokedIssues) {
        return existingIssues.stream()
            .filter(issue -> includeRevokedIssues || !isRevokedIssue(issue))
            .anyMatch(issue ->
            Objects.equals(issue.getValidFrom(), request.getValidFrom())
                && Objects.equals(issue.getValidTo(), request.getValidTo()));
    }

    private CvLicenseIssue findOverlappingCustomerIssue(CvLicenseIssueRequest request) {
        return baseMapper.selectOne(new LambdaQueryWrapper<CvLicenseIssue>()
            .eq(CvLicenseIssue::getCustomerId, request.getCustomerId())
            .ne(StringUtils.isNotBlank(request.getLicenseId()), CvLicenseIssue::getLicenseId, request.getLicenseId())
            .lt(CvLicenseIssue::getValidFrom, request.getValidTo())
            .gt(CvLicenseIssue::getValidTo, request.getValidFrom())
            .and(wrapper -> wrapper.isNull(CvLicenseIssue::getRevokedTime)
                .and(statusWrapper -> statusWrapper.isNull(CvLicenseIssue::getIssueStatus)
                    .or()
                    .ne(CvLicenseIssue::getIssueStatus, ISSUE_STATUS_REVOKED))),
            false);
    }

    private boolean isRevokedIssue(CvLicenseIssue issue) {
        return issue.getRevokedTime() != null || ISSUE_STATUS_REVOKED.equals(normalizeStatus(issue.getIssueStatus()));
    }

    private boolean isSourceLicenseUniqueViolation(DuplicateKeyException exception) {
        String message = exception.getMessage();
        return StringUtils.isNotBlank(message)
            && (message.contains("uk_cv_license_reissue_source") || message.contains("source_license_id"));
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

    private void normalizePackageSnapshot(CvLicenseIssueVo issue) {
        if (issue == null) {
            return;
        }
        if (issue.getPackageId() == null) {
            issue.setPackageName("套餐未配置");
            issue.setEdition("套餐未配置");
            return;
        }
        if (issue.getPackageId() != null) {
            SysTenantPackage tenantPackage = TenantHelper.ignore(() -> tenantPackageMapper.selectById(issue.getPackageId()));
            if (tenantPackage != null && StringUtils.isNotBlank(tenantPackage.getPackageName())) {
                issue.setPackageName(tenantPackage.getPackageName());
            } else {
                issue.setPackageName("套餐未配置#" + issue.getPackageId());
            }
        }
        String packageName = issue.getPackageName();
        if (StringUtils.isNotBlank(packageName)) {
            issue.setPackageName(packageName);
            issue.setEdition(packageName);
        }
    }

    private void prepareIssueView(CvLicenseIssueVo issue) {
        normalizePackageSnapshot(issue);
        populateLicenseContent(issue);
    }

    private void populateLicenseContent(CvLicenseIssueVo issue) {
        if (issue == null
            || StringUtils.isAnyBlank(issue.getSchemaVersion(), issue.getAlgorithm(), issue.getKeyId(),
            issue.getLicensePayload(), issue.getSignatureText())) {
            return;
        }
        try {
            CvLicenseEnvelope envelope = new CvLicenseEnvelope();
            envelope.setSchemaVersion(issue.getSchemaVersion());
            envelope.setAlgorithm(issue.getAlgorithm());
            envelope.setKeyId(issue.getKeyId());
            envelope.setPayload(objectMapper.readValue(issue.getLicensePayload(), CvLicensePayload.class));
            envelope.setSignature(issue.getSignatureText());
            issue.setLicenseContent(objectMapper.writeValueAsString(envelope));
        } catch (Exception e) {
            log.warn("Failed to rebuild license content for licenseId={}", issue.getLicenseId(), e);
        }
    }

    private CvSigningKey findActiveSigningKey(CvLicenseIssueRequest request, Date issuedTime) {
        return signingKeyMapper.selectOne(new LambdaQueryWrapper<CvSigningKey>()
            .eq(CvSigningKey::getKeyId, request.getKeyId())
            .eq(CvSigningKey::getAlgorithm, Objects.requireNonNullElse(request.getAlgorithm(), ALGORITHM))
            .eq(CvSigningKey::getKeyStatus, "active")
            .le(CvSigningKey::getValidFrom, issuedTime)
            .and(wrapper -> wrapper.isNull(CvSigningKey::getValidTo).or().ge(CvSigningKey::getValidTo, issuedTime)),
            false);
    }

    private CvSigningKey findAnyActiveSigningKey(Date issuedTime) {
        return signingKeyMapper.selectOne(new LambdaQueryWrapper<CvSigningKey>()
            .eq(CvSigningKey::getAlgorithm, ALGORITHM)
            .eq(CvSigningKey::getKeyStatus, "active")
            .le(CvSigningKey::getValidFrom, issuedTime)
            .and(wrapper -> wrapper.isNull(CvSigningKey::getValidTo).or().ge(CvSigningKey::getValidTo, issuedTime))
            .orderByDesc(CvSigningKey::getValidFrom)
            .orderByDesc(CvSigningKey::getId),
            false);
    }

    private CvLicensePayload buildPayload(CvLicenseIssueRequest request, CvSigningKey signingKey, Date issuedTime) {
        CvLicensePayload payload = new CvLicensePayload();
        payload.setLicenseId(resolveLicenseId(request));
        payload.setCustomerId(request.getCustomerCode());
        payload.setCustomerName(request.getCustomerName());
        payload.setPackageId(request.getPackageId());
        payload.setPackageName(request.getPackageName());
        payload.setEdition(request.getEdition());
        payload.setFeatures(request.getFeatures());
        payload.setInstallId(request.getInstallId());
        payload.setValidFrom(formatInstant(request.getValidFrom()));
        payload.setValidTo(formatInstant(request.getValidTo()));
        payload.setIssuedAt(formatInstant(issuedTime));
        payload.setIssuer(request.getIssuedBy());
        payload.setKeyId(signingKey.getKeyId());
        payload.setTemplateEntitlements(request.getTemplateEntitlements());
        return payload;
    }

    private CvLicenseEnvelope buildEnvelope(CvLicensePayload payload, CvSigningKey signingKey, String signatureText) {
        CvLicenseEnvelope envelope = new CvLicenseEnvelope();
        envelope.setSchemaVersion(SCHEMA_VERSION);
        envelope.setAlgorithm(ALGORITHM);
        envelope.setKeyId(signingKey.getKeyId());
        envelope.setPayload(payload);
        envelope.setSignature(signatureText);
        return envelope;
    }

    private CvLicenseIssue buildIssueRecord(CvLicenseIssueRequest request, CvSigningKey signingKey, Date issuedTime,
                                            String canonicalPayload, String signatureText, CvLicensePayload payload,
                                            String defaultIssueType)
        throws Exception {
        CvLicenseIssue issue = new CvLicenseIssue();
        issue.setLicenseId(payload.getLicenseId());
        issue.setCustomerId(request.getCustomerId());
        issue.setPackageId(request.getPackageId());
        issue.setPackageName(request.getPackageName());
        issue.setKeyId(signingKey.getKeyId());
        issue.setAlgorithm(ALGORITHM);
        issue.setSchemaVersion(SCHEMA_VERSION);
        issue.setEdition(request.getEdition());
        issue.setFeatureCodes(objectMapper.writeValueAsString(request.getFeatures()));
        issue.setInstallId(request.getInstallId());
        issue.setValidFrom(request.getValidFrom());
        issue.setValidTo(request.getValidTo());
        issue.setIssueStatus(ISSUE_STATUS_ISSUED);
        issue.setIssueType(Objects.requireNonNullElse(request.getIssueType(), defaultIssueType));
        if (request instanceof CvLicenseReissueRequest reissueRequest) {
            issue.setSourceLicenseId(reissueRequest.getSourceLicenseId());
        }
        issue.setIssuedBy(request.getIssuedBy());
        issue.setIssuedTime(issuedTime);
        issue.setLicensePayload(canonicalPayload);
        issue.setSignatureText(signatureText);
        return issue;
    }

    private String resolveReissueInstallId(CvLicenseReissueRequest request, CvLicenseIssue sourceIssue) {
        String sourceInstallId = sourceIssue.getInstallId();
        String targetInstallId = request.getTargetInstallId();
        if (!Boolean.TRUE.equals(request.getAllowInstallIdChange())) {
            if (StringUtils.isNotBlank(targetInstallId) && !Objects.equals(sourceInstallId, targetInstallId)) {
                return null;
            }
            return sourceInstallId;
        }
        if (StringUtils.isBlank(targetInstallId)) {
            return null;
        }
        return targetInstallId;
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

    private String resolveLicenseId(CvLicenseIssueRequest request) {
        if (StringUtils.isNotBlank(request.getLicenseId())) {
            return request.getLicenseId();
        }
        return "LIC-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    private String formatInstant(Date date) {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(date.getTime()).atOffset(ZoneOffset.UTC));
    }
}
