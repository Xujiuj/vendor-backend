package org.dromara.carbon.vendor.dimension;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.carbon.vendor.dimension.mapper.CvAdminDivisionMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvBaseYearMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityFactorScopeMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityFactorVersionMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvEmissionSourceCategoryMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvGreenhouseGasMapper;
import org.dromara.carbon.vendor.dimension.service.ICvEmissionSourcePublicationService;
import org.dromara.carbon.vendor.dimension.domain.vo.CvEmissionSourcePublicationVo;
import org.dromara.carbon.vendor.dimension.domain.CvEmissionSourceCategory;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.openapi.domain.CvOpenDimensionListResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenDimensionRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenDimensionRecordVo;
import org.dromara.carbon.vendor.openapi.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.openapi.service.impl.CvOpenDimensionServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvVendorDimensionScopeTest {

    @Test
    void openDimensionApiRejectsEnterpriseFilledDimensions() {
        CvLicenseIssueMapper licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        ICvOpenApiAuditService auditService = mock(ICvOpenApiAuditService.class);
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        CvOpenDimensionServiceImpl service = new CvOpenDimensionServiceImpl(
            licenseIssueMapper,
            mock(CvAdminDivisionMapper.class),
            mock(CvEmissionSourceCategoryMapper.class),
            mock(CvBaseYearMapper.class),
            mock(CvElectricityMapper.class),
            mock(CvElectricityFactorVersionMapper.class),
            mock(CvElectricityFactorScopeMapper.class),
            mock(CvGreenhouseGasMapper.class),
            publicationService(),
            auditService
        );

        assertThrows(ServiceException.class, () -> service.listDimensions(openRequest("company")));
        assertThrows(ServiceException.class, () -> service.listDimensions(openRequest("intensity-tolerance")));
    }

    @Test
    void openEmissionSourceCategoryDoesNotInventMissingSemanticFields() {
        CvLicenseIssueMapper licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        CvEmissionSourceCategoryMapper categoryMapper = mock(CvEmissionSourceCategoryMapper.class);
        ICvOpenApiAuditService auditService = mock(ICvOpenApiAuditService.class);
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());

        Page<CvEmissionSourceCategory> mapperPage = new Page<>(1, 10);
        mapperPage.setRecords(java.util.List.of(emissionSourceCategory("1"), emissionSourceCategory("2")));
        mapperPage.setTotal(2);
        when(categoryMapper.selectPage(any(), any())).thenReturn(mapperPage);

        CvOpenDimensionServiceImpl service = new CvOpenDimensionServiceImpl(
            licenseIssueMapper,
            mock(CvAdminDivisionMapper.class),
            categoryMapper,
            mock(CvBaseYearMapper.class),
            mock(CvElectricityMapper.class),
            mock(CvElectricityFactorVersionMapper.class),
            mock(CvElectricityFactorScopeMapper.class),
            mock(CvGreenhouseGasMapper.class),
            publicationService(),
            auditService
        );

        CvOpenDimensionListResponse response = service.listDimensions(openRequest("emission-source-category"));

        assertEquals(2, response.getTotal());
        assertEquals(2, response.getRecords().size());
        CvOpenDimensionRecordVo record = response.getRecords().get(0);
        assertEquals("Category EN", record.getCategoryNameEn());
        assertEquals("ISO EN", record.getIsoCategoryEn());
        assertNull(record.getGhgScopeEn());
        assertNull(record.getGhgScopeCategoryEn());
        assertNull(record.getIsoCategoryDescriptionEn());
        assertNull(record.getIsoCustomSubcategory());
        assertEquals("1", response.getRecords().get(0).getVersionNo());
        assertEquals("2", response.getRecords().get(1).getVersionNo());
    }

    private CvOpenDimensionRequest openRequest(String dimensionCode) {
        CvOpenDimensionRequest request = new CvOpenDimensionRequest();
        request.setLicenseId("LIC-001");
        request.setInstallId("INSTALL-001");
        request.setDimensionCode(dimensionCode);
        return request;
    }

    private ICvEmissionSourcePublicationService publicationService() {
        ICvEmissionSourcePublicationService service = mock(ICvEmissionSourcePublicationService.class);
        CvEmissionSourcePublicationVo policy = new CvEmissionSourcePublicationVo();
        policy.setPublicationId("publication-1");
        policy.setPublishMode("ALL");
        policy.setPublishedVersions(java.util.List.of("1", "2"));
        when(service.queryPolicy()).thenReturn(policy);
        return service;
    }

    private CvEmissionSourceCategory emissionSourceCategory(String versionNo) {
        CvEmissionSourceCategory category = new CvEmissionSourceCategory();
        category.setId(1L);
        category.setCategoryCode("CAT-001");
        category.setBusinessKey("BK-001");
        category.setCategoryName("Category CN");
        category.setCategoryNameEn("Category EN");
        category.setGhgScope("Scope 1");
        category.setGhgScopeCategory("Stationary combustion");
        category.setIsoCategory("ISO");
        category.setIsoCategoryEn("ISO EN");
        category.setIsoCategoryDescription("ISO description CN");
        category.setVersionNo(versionNo);
        category.setStatus("0");
        return category;
    }

    private CvLicenseIssue activeLicense() {
        CvLicenseIssue license = new CvLicenseIssue();
        license.setLicenseId("LIC-001");
        license.setInstallId("INSTALL-001");
        license.setCustomerId(1001L);
        license.setValidFrom(Date.from(Instant.parse("2026-01-01T00:00:00Z")));
        license.setValidTo(Date.from(Instant.parse("2027-01-01T00:00:00Z")));
        return license;
    }
}
