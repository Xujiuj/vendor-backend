package org.dromara.carbon.vendor.openapi.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.template.domain.CvReportTemplateDownloadToken;
import org.dromara.carbon.vendor.template.domain.CvReportTemplate;
import org.dromara.carbon.vendor.template.domain.CvReportTemplateScope;
import org.dromara.carbon.vendor.template.domain.enums.CvReportTemplateLifecycleState;
import org.dromara.carbon.vendor.openapi.domain.CvOpenReportTemplateDownloadResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenReportTemplateListResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenReportTemplateRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenReportTemplateVo;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.license.service.impl.CvLicenseInstallBindingSupport;
import org.dromara.carbon.vendor.template.mapper.CvReportTemplateDownloadTokenMapper;
import org.dromara.carbon.vendor.template.mapper.CvReportTemplateMapper;
import org.dromara.carbon.vendor.template.mapper.CvReportTemplateScopeMapper;
import org.dromara.carbon.vendor.openapi.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.openapi.service.ICvOpenReportTemplateService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.file.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Vendor open report template service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvOpenReportTemplateServiceImpl implements ICvOpenReportTemplateService {

    private static final String API_LIST = "/open/report-templates";
    private static final String API_DOWNLOAD = "/open/report-templates/{id}/download";
    private static final String API_DOWNLOAD_TOKEN = "/open/report-templates/download-tokens/{token}";
    private static final String HTTP_METHOD = "GET";
    private static final String FEATURE_REPORT_TEMPLATE_SYNC = "report-template-sync";
    private static final String ISSUE_STATUS_REVOKED = "revoked";
    private static final String SCOPE_STATUS_ENABLED = "enabled";
    private static final String TOKEN_STATUS_ISSUED = "issued";
    private static final String TOKEN_STATUS_CONSUMED = "consumed";
    private static final String VENDOR_URI_PREFIX = "vendor://";
    private static final String FILE_URI_PREFIX = "file://";
    private static final long DOWNLOAD_TOKEN_TTL_MILLIS = 10 * 60 * 1000L;
    private static final String DEFAULT_TEMPLATE_ROOT = "vendor/report-templates";

    private final CvLicenseIssueMapper licenseIssueMapper;
    private final CvReportTemplateMapper reportTemplateMapper;
    private final CvReportTemplateScopeMapper reportTemplateScopeMapper;
    private final CvReportTemplateDownloadTokenMapper downloadTokenMapper;
    private final ICvOpenApiAuditService openApiAuditService;

    @Value("${carbon.vendor.report-template-root:" + DEFAULT_TEMPLATE_ROOT + "}")
    private String reportTemplateRoot = DEFAULT_TEMPLATE_ROOT;

    @Override
    public CvOpenReportTemplateListResponse listTemplates(CvOpenReportTemplateRequest request) {
        Long customerId = null;
        try {
            CvLicenseIssue entitlement = requireActiveLicense(request);
            customerId = entitlement.getCustomerId();
            CvOpenLicenseFeatureSupport.requireFeature(entitlement, FEATURE_REPORT_TEMPLATE_SYNC);
            List<CvReportTemplate> authorizedTemplates = findAuthorizedTemplates(entitlement);

            CvOpenReportTemplateListResponse response = new CvOpenReportTemplateListResponse();
            response.setLicenseId(entitlement.getLicenseId());
            response.setTemplates(authorizedTemplates.stream()
                .map(this::toTemplateVo)
                .toList());
            openApiAuditService.recordSuccess(API_LIST, HTTP_METHOD, request.getLicenseId(), request.getInstallId(),
                customerId, "action=list");
            return response;
        } catch (RuntimeException ex) {
            openApiAuditService.recordFailure(API_LIST, HTTP_METHOD, request == null ? null : request.getLicenseId(),
                request == null ? null : request.getInstallId(), customerId, "action=list", ex.getMessage());
            throw ex;
        }
    }

    @Override
    public CvOpenReportTemplateDownloadResponse downloadTemplate(Long templateId, CvOpenReportTemplateRequest request) {
        Long customerId = null;
        try {
            if (templateId == null) {
                throw new ServiceException("report template id cannot be null");
            }
            CvLicenseIssue entitlement = requireActiveLicense(request);
            customerId = entitlement.getCustomerId();
            CvOpenLicenseFeatureSupport.requireFeature(entitlement, FEATURE_REPORT_TEMPLATE_SYNC);
            CvReportTemplate template = findAuthorizedTemplates(entitlement).stream()
                .filter(item -> templateId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("report template is not authorized for license entitlement"));

            CvOpenReportTemplateDownloadResponse response = new CvOpenReportTemplateDownloadResponse();
            response.setLicenseId(entitlement.getLicenseId());
            response.setTemplateId(template.getId());
            response.setTemplateCode(template.getTemplateCode());
            response.setTemplateName(template.getTemplateName());
            response.setTemplateVersion(template.getTemplateVersion());
            response.setFileName(template.getFileName());
            CvReportTemplateDownloadToken token = issueDownloadToken(entitlement, template);
            response.setDownloadToken(token.getDownloadToken());
            response.setDownloadTokenExpiresTime(token.getExpiresTime());
            response.setPublishedTime(template.getPublishedTime());
            openApiAuditService.recordSuccess(API_DOWNLOAD, HTTP_METHOD, request.getLicenseId(), request.getInstallId(),
                customerId, downloadSummary(templateId));
            return response;
        } catch (RuntimeException ex) {
            openApiAuditService.recordFailure(API_DOWNLOAD, HTTP_METHOD, request == null ? null : request.getLicenseId(),
                request == null ? null : request.getInstallId(), customerId, downloadSummary(templateId), ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void consumeDownloadToken(String downloadToken, HttpServletResponse response) throws IOException {
        CvReportTemplateDownloadToken token = null;
        try {
            String normalizedToken = normalizeRequired(downloadToken, "download token cannot be blank");
            token = downloadTokenMapper.selectOne(Wrappers.<CvReportTemplateDownloadToken>lambdaQuery()
                .eq(CvReportTemplateDownloadToken::getDownloadToken, normalizedToken), false);
            if (token == null) {
                throw new ServiceException("download token does not exist");
            }
            if (!TOKEN_STATUS_ISSUED.equals(token.getTokenStatus())) {
                throw new ServiceException("download token has been consumed");
            }
            Date now = new Date();
            if (token.getExpiresTime() == null || token.getExpiresTime().before(now)) {
                throw new ServiceException("download token has expired");
            }

            Path file = resolveTemplateFile(token.getFileUri());
            token.setTokenStatus(TOKEN_STATUS_CONSUMED);
            token.setConsumedTime(now);
            downloadTokenMapper.updateById(token);

            FileUtils.setAttachmentResponseHeader(response, token.getFileName());
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE + "; charset=UTF-8");
            response.setContentLengthLong(Files.size(file));
            try (InputStream inputStream = Files.newInputStream(file, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
                inputStream.transferTo(response.getOutputStream());
            }
            openApiAuditService.recordSuccess(API_DOWNLOAD_TOKEN, HTTP_METHOD, token.getLicenseId(), token.getInstallId(),
                token.getCustomerId(), tokenSummary(token));
        } catch (RuntimeException | IOException ex) {
            openApiAuditService.recordFailure(API_DOWNLOAD_TOKEN, HTTP_METHOD,
                token == null ? null : token.getLicenseId(),
                token == null ? null : token.getInstallId(),
                token == null ? null : token.getCustomerId(),
                tokenSummary(token), ex.getMessage());
            throw ex;
        }
    }

    private CvLicenseIssue requireActiveLicense(CvOpenReportTemplateRequest request) {
        String licenseId = normalizeRequired(request.getLicenseId(), "licenseId cannot be blank");
        String installId = normalizeRequired(request.getInstallId(), "installId cannot be blank");
        CvLicenseIssue entitlement = licenseIssueMapper.selectOne(Wrappers.<CvLicenseIssue>lambdaQuery()
            .eq(CvLicenseIssue::getLicenseId, licenseId), false);
        if (entitlement == null) {
            throw new ServiceException("license entitlement does not exist");
        }
        CvLicenseInstallBindingSupport.bindOrReject(licenseIssueMapper, entitlement, installId);
        if (entitlement.getRevokedTime() != null || ISSUE_STATUS_REVOKED.equalsIgnoreCase(entitlement.getIssueStatus())) {
            throw new ServiceException("license entitlement is revoked");
        }
        Date now = new Date();
        if ((entitlement.getValidFrom() != null && entitlement.getValidFrom().after(now))
            || (entitlement.getValidTo() != null && entitlement.getValidTo().before(now))) {
            throw new ServiceException("license entitlement is not currently valid");
        }
        return entitlement;
    }

    private List<CvReportTemplate> findAuthorizedTemplates(CvLicenseIssue entitlement) {
        String normalizedEdition = normalizeEdition(entitlement.getEdition());
        List<CvReportTemplateScope> scopes = reportTemplateScopeMapper.selectList(Wrappers.<CvReportTemplateScope>lambdaQuery()
            .eq(CvReportTemplateScope::getScopeStatus, SCOPE_STATUS_ENABLED)
            .and(wrapper -> {
                wrapper.eq(CvReportTemplateScope::getCustomerId, entitlement.getCustomerId());
                if (entitlement.getPackageId() != null) {
                    wrapper.or().eq(CvReportTemplateScope::getPackageId, entitlement.getPackageId());
                }
                if (normalizedEdition != null) {
                    wrapper.or().eq(CvReportTemplateScope::getEdition, normalizedEdition);
                }
                wrapper.or().eq(CvReportTemplateScope::getLicenseId, entitlement.getLicenseId());
            })
            .orderByAsc(CvReportTemplateScope::getTemplateId)
            .orderByAsc(CvReportTemplateScope::getId))
            .stream()
            .filter(scope -> isScopeAuthorized(scope, entitlement))
            .toList();
        if (scopes.isEmpty()) {
            return List.of();
        }

        Collection<Long> templateIds = scopes.stream()
            .map(CvReportTemplateScope::getTemplateId)
            .collect(Collectors.toSet());
        Map<Long, CvReportTemplate> templatesById = reportTemplateMapper.selectByIds(templateIds).stream()
            .filter(template -> CvReportTemplateLifecycleState.fromTemplate(template) == CvReportTemplateLifecycleState.PUBLISHED)
            .collect(Collectors.toMap(CvReportTemplate::getId, Function.identity()));

        return scopes.stream()
            .map(scope -> templatesById.get(scope.getTemplateId()))
            .filter(template -> template != null)
            .distinct()
            .sorted(Comparator
                .comparing(CvReportTemplate::getTemplateCode, Comparator.nullsLast(String::compareTo))
                .thenComparing(CvReportTemplate::getTemplateVersion, Comparator.nullsLast(String::compareTo))
                .thenComparing(CvReportTemplate::getId))
            .toList();
    }

    private boolean isScopeAuthorized(CvReportTemplateScope scope, CvLicenseIssue entitlement) {
        boolean hasCustomerScope = scope.getCustomerId() != null;
        boolean hasPackageScope = scope.getPackageId() != null;
        boolean hasEditionScope = StringUtils.isNotBlank(scope.getEdition());
        boolean hasLicenseScope = StringUtils.isNotBlank(scope.getLicenseId());
        boolean customerMatches = entitlement.getCustomerId() != null
            && entitlement.getCustomerId().equals(scope.getCustomerId());
        boolean packageMatches = entitlement.getPackageId() != null
            && entitlement.getPackageId().equals(scope.getPackageId());
        boolean editionMatches = hasEditionScope
            && Objects.equals(normalizeEdition(entitlement.getEdition()), normalizeEdition(scope.getEdition()));
        boolean licenseMatches = hasLicenseScope
            && entitlement.getLicenseId().equals(scope.getLicenseId().trim());
        return (hasCustomerScope || hasPackageScope || hasEditionScope || hasLicenseScope)
            && (!hasCustomerScope || customerMatches)
            && (!hasPackageScope || packageMatches)
            && (!hasEditionScope || editionMatches)
            && (!hasLicenseScope || licenseMatches);
    }

    private CvOpenReportTemplateVo toTemplateVo(CvReportTemplate template) {
        CvOpenReportTemplateVo vo = new CvOpenReportTemplateVo();
        vo.setTemplateId(template.getId());
        vo.setTemplateCode(template.getTemplateCode());
        vo.setTemplateName(template.getTemplateName());
        vo.setTemplateVersion(template.getTemplateVersion());
        vo.setFileName(template.getFileName());
        vo.setPublishedTime(template.getPublishedTime());
        return vo;
    }

    private CvReportTemplateDownloadToken issueDownloadToken(CvLicenseIssue entitlement, CvReportTemplate template) {
        if (StringUtils.isBlank(template.getFileName()) || StringUtils.isBlank(template.getFileUri())) {
            throw new ServiceException("report template file metadata is incomplete");
        }
        Date now = new Date();
        CvReportTemplateDownloadToken token = new CvReportTemplateDownloadToken();
        token.setDownloadToken(UUID.randomUUID().toString().replace("-", ""));
        token.setLicenseId(entitlement.getLicenseId());
        token.setInstallId(entitlement.getInstallId());
        token.setCustomerId(entitlement.getCustomerId());
        token.setTemplateId(template.getId());
        token.setFileName(template.getFileName());
        token.setFileUri(template.getFileUri());
        token.setTokenStatus(TOKEN_STATUS_ISSUED);
        token.setExpiresTime(new Date(now.getTime() + DOWNLOAD_TOKEN_TTL_MILLIS));
        token.setCreateTime(now);
        downloadTokenMapper.insert(token);
        return token;
    }

    private Path resolveTemplateFile(String fileUri) {
        if (StringUtils.isBlank(fileUri)) {
            throw new ServiceException("report template file uri cannot be blank");
        }
        Path templateRoot = resolveTemplateRoot();
        String normalizedUri = fileUri.trim();
        String pathText;
        if (normalizedUri.startsWith(VENDOR_URI_PREFIX)) {
            pathText = normalizedUri.substring(VENDOR_URI_PREFIX.length());
        } else if (normalizedUri.startsWith(FILE_URI_PREFIX)) {
            pathText = normalizedUri.substring(FILE_URI_PREFIX.length());
        } else if (normalizedUri.contains("://")) {
            throw new ServiceException("report template file uri is not locally readable");
        } else {
            pathText = normalizedUri;
        }
        try {
            Path configuredPath = Path.of(pathText).normalize();
            Path resolvedPath = configuredPath.isAbsolute() ? configuredPath : templateRoot.resolve(configuredPath).normalize();
            if (!resolvedPath.startsWith(templateRoot)) {
                throw new ServiceException("report template file path is outside template root");
            }
            if (!Files.isRegularFile(resolvedPath, LinkOption.NOFOLLOW_LINKS) || !Files.isReadable(resolvedPath)) {
                throw new ServiceException("report template file does not exist");
            }
            return resolvedPath;
        } catch (InvalidPathException ex) {
            throw new ServiceException("report template file uri is invalid");
        }
    }

    private Path resolveTemplateRoot() {
        if (StringUtils.isBlank(reportTemplateRoot)) {
            throw new ServiceException("report template root is invalid");
        }
        try {
            Path configuredRoot = Path.of(reportTemplateRoot.trim());
            if (configuredRoot.isAbsolute()) {
                return configuredRoot.normalize();
            }
            return Path.of("").toAbsolutePath().normalize().resolve(configuredRoot).normalize();
        } catch (InvalidPathException ex) {
            throw new ServiceException("report template root is invalid");
        }
    }

    private String normalizeRequired(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new ServiceException(message);
        }
        return value.trim();
    }

    private String normalizeEdition(String edition) {
        return StringUtils.isBlank(edition) ? null : edition.trim().toLowerCase(Locale.ROOT);
    }

    private String downloadSummary(Long templateId) {
        return "templateId=" + (templateId == null ? "" : templateId);
    }

    private String tokenSummary(CvReportTemplateDownloadToken token) {
        if (token == null) {
            return "token=";
        }
        String tokenText = token.getDownloadToken();
        String prefix = StringUtils.isBlank(tokenText) ? "" : tokenText.substring(0, Math.min(8, tokenText.length()));
        return "templateId=" + token.getTemplateId() + ";tokenPrefix=" + prefix;
    }
}
