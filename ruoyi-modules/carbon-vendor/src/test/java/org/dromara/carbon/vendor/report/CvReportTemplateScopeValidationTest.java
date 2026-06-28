package org.dromara.carbon.vendor.report;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.dromara.carbon.vendor.template.domain.CvReportTemplate;
import org.dromara.carbon.vendor.template.domain.CvReportTemplateScope;
import org.dromara.carbon.vendor.template.domain.bo.CvReportTemplateScopeBo;
import org.dromara.carbon.vendor.template.mapper.CvReportTemplateMapper;
import org.dromara.carbon.vendor.template.mapper.CvReportTemplateScopeMapper;
import org.dromara.carbon.vendor.template.service.impl.CvReportTemplateScopeServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvReportTemplateScopeValidationTest {

    private CvReportTemplateScopeMapper scopeMapper;
    private CvReportTemplateMapper templateMapper;
    private SysTenantPackageMapper tenantPackageMapper;
    private CvReportTemplateScopeServiceImpl service;

    @BeforeEach
    void setUp() {
        scopeMapper = mock(CvReportTemplateScopeMapper.class);
        templateMapper = mock(CvReportTemplateMapper.class);
        tenantPackageMapper = mock(SysTenantPackageMapper.class);
        when(tenantPackageMapper.selectById(1002L)).thenReturn(activePackage());
        service = new CvReportTemplateScopeServiceImpl(scopeMapper, templateMapper, tenantPackageMapper) {
            @Override
            protected CvReportTemplateScope toEntity(CvReportTemplateScopeBo bo) {
                CvReportTemplateScope scope = new CvReportTemplateScope();
                scope.setId(bo.getId());
                scope.setTemplateId(bo.getTemplateId());
                scope.setPackageId(bo.getPackageId());
                scope.setScopeStatus(bo.getScopeStatus());
                return scope;
            }
        };
    }

    @Test
    void insertsDistributionForPublishedTemplateUsingPackageOnly() {
        when(templateMapper.selectById(301L)).thenReturn(publishedTemplate());
        when(scopeMapper.selectOne(any(), any(Boolean.class))).thenReturn(null);

        service.insertReportTemplateScope(validScopeBo());

        ArgumentCaptor<CvReportTemplateScope> insertCaptor = ArgumentCaptor.forClass(CvReportTemplateScope.class);
        verify(scopeMapper).insert(insertCaptor.capture());
        CvReportTemplateScope inserted = insertCaptor.getValue();
        assertEquals(301L, inserted.getTemplateId());
        assertEquals(1002L, inserted.getPackageId());
        assertEquals("专业版", inserted.getPackageName());
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
    void normalizesScopeStatusOnUpdate() {
        when(templateMapper.selectById(301L)).thenReturn(publishedTemplate());
        CvReportTemplateScopeBo bo = validScopeBo();
        bo.setId(55L);
        bo.setScopeStatus(" ENABLED ");

        service.updateReportTemplateScope(bo);

        ArgumentCaptor<CvReportTemplateScope> updateCaptor = ArgumentCaptor.forClass(CvReportTemplateScope.class);
        verify(scopeMapper).updateById(updateCaptor.capture());
        CvReportTemplateScope updated = updateCaptor.getValue();
        assertEquals(1002L, updated.getPackageId());
        assertEquals("enabled", updated.getScopeStatus());
    }

    @Test
    void rejectsDistributionWithoutPackage() {
        when(templateMapper.selectById(301L)).thenReturn(publishedTemplate());
        CvReportTemplateScopeBo bo = validScopeBo();
        bo.setPackageId(null);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertReportTemplateScope(bo));

        assertEquals("Distribution must reference a package", exception.getMessage());
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
    void insertRestoresDeletedDistributionScopeWithSamePackageKey() {
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

    @Test
    void addGroupStillRequiresTemplateId() {
        CvReportTemplateScopeBo bo = validScopeBo();
        bo.setTemplateId(null);

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<CvReportTemplateScopeBo>> violations = validator.validate(bo, AddGroup.class);

        assertFalse(violations.isEmpty());
    }

    private CvReportTemplateScopeBo validScopeBo() {
        CvReportTemplateScopeBo bo = new CvReportTemplateScopeBo();
        bo.setTemplateId(301L);
        bo.setPackageId(1002L);
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

    private SysTenantPackage activePackage() {
        SysTenantPackage tenantPackage = new SysTenantPackage();
        tenantPackage.setPackageId(1002L);
        tenantPackage.setPackageName("专业版");
        tenantPackage.setStatus("0");
        tenantPackage.setDelFlag("0");
        return tenantPackage;
    }
}
