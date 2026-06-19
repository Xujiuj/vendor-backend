package org.dromara.carbon.vendor.report;

import org.dromara.carbon.vendor.domain.CvCustomer;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.CvReportTemplate;
import org.dromara.carbon.vendor.domain.CvReportTemplateScope;
import org.dromara.carbon.vendor.domain.bo.CvReportTemplateScopeBo;
import org.dromara.carbon.vendor.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.mapper.CvReportTemplateMapper;
import org.dromara.carbon.vendor.mapper.CvReportTemplateScopeMapper;
import org.dromara.carbon.vendor.service.impl.CvReportTemplateScopeServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@Tag("dev")
class CvReportTemplateScopeValidationTest {

    private CvReportTemplateScopeMapper scopeMapper;
    private CvReportTemplateMapper templateMapper;
    private CvCustomerMapper customerMapper;
    private CvLicenseIssueMapper licenseIssueMapper;
    private SysTenantPackageMapper tenantPackageMapper;
    private CvReportTemplateScopeServiceImpl service;

    @BeforeEach
    void setUp() {
        scopeMapper = mock(CvReportTemplateScopeMapper.class);
        templateMapper = mock(CvReportTemplateMapper.class);
        customerMapper = mock(CvCustomerMapper.class);
        licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        tenantPackageMapper = mock(SysTenantPackageMapper.class);
        when(customerMapper.selectById(1001L)).thenReturn(activeCustomer());
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(issuedLicense());
        service = new CvReportTemplateScopeServiceImpl(scopeMapper, templateMapper, customerMapper, licenseIssueMapper, tenantPackageMapper) {
            @Override
            protected CvReportTemplateScope toEntity(CvReportTemplateScopeBo bo) {
                CvReportTemplateScope scope = new CvReportTemplateScope();
                scope.setId(bo.getId());
                scope.setTemplateId(bo.getTemplateId());
                scope.setCustomerId(bo.getCustomerId());
                scope.setLicenseId(bo.getLicenseId());
                scope.setEdition(bo.getEdition());
                scope.setScopeStatus(bo.getScopeStatus());
                return scope;
            }
        };
    }

    @Test
    void insertsDistributionForPublishedTemplateUsingVendorMetadataOnly() {
        when(templateMapper.selectById(301L)).thenReturn(publishedTemplate());
        when(scopeMapper.selectOne(any(), any(Boolean.class))).thenReturn(null);

        service.insertReportTemplateScope(validScopeBo());

        ArgumentCaptor<CvReportTemplateScope> insertCaptor = ArgumentCaptor.forClass(CvReportTemplateScope.class);
        verify(scopeMapper).insert(insertCaptor.capture());
        CvReportTemplateScope inserted = insertCaptor.getValue();
        assertEquals(301L, inserted.getTemplateId());
        assertEquals(1001L, inserted.getCustomerId());
        assertEquals("LIC-001", inserted.getLicenseId());
        assertEquals("professional", inserted.getEdition());
        assertEquals("enabled", inserted.getScopeStatus());
    }

    @Test
    void rejectsDistributionForDraftTemplate() {
        when(templateMapper.selectById(301L)).thenReturn(draftTemplate());

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertReportTemplateScope(validScopeBo()));

        assertEquals("Only published report templates can be distributed", exception.getMessage());
    }

    @Test
    void normalizesBlankLicenseIdToNullOnUpdate() {
        when(templateMapper.selectById(301L)).thenReturn(publishedTemplate());
        CvReportTemplateScopeBo bo = validScopeBo();
        bo.setId(55L);
        bo.setLicenseId("   ");
        bo.setEdition(" GROUP ");
        bo.setScopeStatus(" ENABLED ");

        service.updateReportTemplateScope(bo);

        ArgumentCaptor<CvReportTemplateScope> updateCaptor = ArgumentCaptor.forClass(CvReportTemplateScope.class);
        verify(scopeMapper).updateById(updateCaptor.capture());
        CvReportTemplateScope updated = updateCaptor.getValue();
        assertNull(updated.getLicenseId());
        assertEquals("group", updated.getEdition());
        assertEquals("enabled", updated.getScopeStatus());
    }

    @Test
    void allowsLicenseOnlyDistributionContractAndPersistsLicenseReference() {
        when(templateMapper.selectById(301L)).thenReturn(publishedTemplate());
        when(scopeMapper.selectOne(any(), any(Boolean.class))).thenReturn(null);
        CvReportTemplateScopeBo bo = validScopeBo();
        bo.setCustomerId(null);

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<CvReportTemplateScopeBo>> violations = validator.validate(bo, AddGroup.class);
        assertTrue(violations.isEmpty());

        service.insertReportTemplateScope(bo);

        ArgumentCaptor<CvReportTemplateScope> insertCaptor = ArgumentCaptor.forClass(CvReportTemplateScope.class);
        verify(scopeMapper).insert(insertCaptor.capture());
        CvReportTemplateScope inserted = insertCaptor.getValue();
        assertNull(inserted.getCustomerId());
        assertEquals("LIC-001", inserted.getLicenseId());
    }

    @Test
    void allowsEditionOnlyDistributionContractAndPersistsEditionReference() {
        when(templateMapper.selectById(301L)).thenReturn(publishedTemplate());
        when(scopeMapper.selectOne(any(), any(Boolean.class))).thenReturn(null);
        CvReportTemplateScopeBo bo = validScopeBo();
        bo.setCustomerId(null);
        bo.setLicenseId(" ");

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<CvReportTemplateScopeBo>> violations = validator.validate(bo, AddGroup.class);
        assertTrue(violations.isEmpty());

        service.insertReportTemplateScope(bo);

        ArgumentCaptor<CvReportTemplateScope> insertCaptor = ArgumentCaptor.forClass(CvReportTemplateScope.class);
        verify(scopeMapper).insert(insertCaptor.capture());
        CvReportTemplateScope inserted = insertCaptor.getValue();
        assertNull(inserted.getCustomerId());
        assertNull(inserted.getLicenseId());
        assertEquals("professional", inserted.getEdition());
    }

    @Test
    void rejectsDistributionWithoutCustomerEditionOrLicenseMetadata() {
        when(templateMapper.selectById(301L)).thenReturn(publishedTemplate());
        CvReportTemplateScopeBo bo = validScopeBo();
        bo.setCustomerId(null);
        bo.setEdition(" ");
        bo.setLicenseId(" ");

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertReportTemplateScope(bo));

        assertEquals("Distribution must reference customer, package, edition, or license entitlement metadata", exception.getMessage());
    }

    @Test
    void rejectsUnknownCustomerReference() {
        when(templateMapper.selectById(301L)).thenReturn(publishedTemplate());
        when(customerMapper.selectById(1001L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertReportTemplateScope(validScopeBo()));

        assertEquals("Referenced vendor customer does not exist", exception.getMessage());
    }

    @Test
    void rejectsUnknownLicenseReference() {
        when(templateMapper.selectById(301L)).thenReturn(publishedTemplate());
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertReportTemplateScope(validScopeBo()));

        assertEquals("Referenced vendor license entitlement does not exist", exception.getMessage());
    }

    @Test
    void rejectsUnsupportedScopeStatus() {
        when(templateMapper.selectById(301L)).thenReturn(publishedTemplate());
        CvReportTemplateScopeBo bo = validScopeBo();
        bo.setScopeStatus("typo");

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertReportTemplateScope(bo));

        assertEquals("Unsupported report template distribution status", exception.getMessage());
    }

    @Test
    void removeSoftDeletesDistributionScopes() {
        CvReportTemplateScope existing = new CvReportTemplateScope();
        existing.setId(77L);
        existing.setScopeStatus("enabled");
        when(scopeMapper.selectList(any())).thenReturn(List.of(existing));
        when(scopeMapper.updateBatchById(any())).thenReturn(true);

        int removed = service.deleteReportTemplateScopeByIds(new Long[] {77L});

        assertEquals(1, removed);
        ArgumentCaptor<List<CvReportTemplateScope>> updateCaptor = ArgumentCaptor.forClass(List.class);
        verify(scopeMapper).updateBatchById(updateCaptor.capture());
        assertEquals("deleted", updateCaptor.getValue().get(0).getScopeStatus());
        verify(scopeMapper, never()).deleteByIds(any());
    }

    @Test
    void insertRestoresDeletedDistributionScopeWithSameEntitlementKey() {
        when(templateMapper.selectById(301L)).thenReturn(publishedTemplate());
        CvReportTemplateScope deletedScope = new CvReportTemplateScope();
        deletedScope.setId(77L);
        deletedScope.setScopeStatus("deleted");
        when(scopeMapper.selectOne(any(), any(Boolean.class))).thenReturn(deletedScope);

        service.insertReportTemplateScope(validScopeBo());

        ArgumentCaptor<CvReportTemplateScope> updateCaptor = ArgumentCaptor.forClass(CvReportTemplateScope.class);
        verify(scopeMapper).updateById(updateCaptor.capture());
        CvReportTemplateScope restored = updateCaptor.getValue();
        assertEquals(77L, restored.getId());
        assertEquals("enabled", restored.getScopeStatus());
        verify(scopeMapper, never()).insert(any(CvReportTemplateScope.class));
    }

    private CvReportTemplateScopeBo validScopeBo() {
        CvReportTemplateScopeBo bo = new CvReportTemplateScopeBo();
        bo.setTemplateId(301L);
        bo.setCustomerId(1001L);
        bo.setLicenseId(" LIC-001 ");
        bo.setEdition(" Professional ");
        bo.setScopeStatus(null);
        return bo;
    }

    private CvReportTemplate publishedTemplate() {
        CvReportTemplate template = new CvReportTemplate();
        template.setId(301L);
        template.setPublishStatus("published");
        return template;
    }

    private CvReportTemplate draftTemplate() {
        CvReportTemplate template = new CvReportTemplate();
        template.setId(301L);
        template.setPublishStatus("draft");
        return template;
    }

    private CvCustomer activeCustomer() {
        CvCustomer customer = new CvCustomer();
        customer.setId(1001L);
        customer.setCustomerStatus("active");
        return customer;
    }

    private CvLicenseIssue issuedLicense() {
        CvLicenseIssue licenseIssue = new CvLicenseIssue();
        licenseIssue.setLicenseId("LIC-001");
        licenseIssue.setIssueStatus("issued");
        return licenseIssue;
    }
}
