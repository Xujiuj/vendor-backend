package org.dromara.carbon.vendor.factor;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.dromara.carbon.vendor.factor.domain.CvFactorCustomerScope;
import org.dromara.carbon.vendor.factor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.factor.domain.bo.CvFactorCustomerScopeBo;
import org.dromara.carbon.vendor.factor.mapper.CvFactorCustomerScopeMapper;
import org.dromara.carbon.vendor.factor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.factor.service.impl.CvFactorCustomerScopeServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvFactorCustomerScopeTest {

    private CvFactorCustomerScopeMapper scopeMapper;
    private CvFactorVersionMapper factorVersionMapper;
    private SysTenantPackageMapper tenantPackageMapper;
    private CvFactorCustomerScopeServiceImpl service;

    @BeforeEach
    void setUp() {
        scopeMapper = mock(CvFactorCustomerScopeMapper.class);
        factorVersionMapper = mock(CvFactorVersionMapper.class);
        tenantPackageMapper = mock(SysTenantPackageMapper.class);
        when(factorVersionMapper.selectById(401L)).thenReturn(publishedVersion());
        when(tenantPackageMapper.selectById(1002L)).thenReturn(activePackage());
        when(scopeMapper.selectCount(any())).thenReturn(0L);
        when(scopeMapper.selectOne(any(), any(Boolean.class))).thenReturn(null);
        service = new CvFactorCustomerScopeServiceImpl(scopeMapper, factorVersionMapper, tenantPackageMapper) {
            @Override
            protected CvFactorCustomerScope toEntity(CvFactorCustomerScopeBo bo) {
                CvFactorCustomerScope scope = new CvFactorCustomerScope();
                scope.setId(bo.getId());
                scope.setVersionId(bo.getVersionId());
                scope.setPackageId(bo.getPackageId());
                scope.setScopeStatus(bo.getScopeStatus());
                return scope;
            }
        };
    }

    @Test
    void insertsPackageScopedMetadataForReleasedVersion() {
        service.insertFactorCustomerScope(validPackageScopeBo());

        ArgumentCaptor<CvFactorCustomerScope> insertCaptor = ArgumentCaptor.forClass(CvFactorCustomerScope.class);
        verify(scopeMapper).insert(insertCaptor.capture());
        CvFactorCustomerScope inserted = insertCaptor.getValue();
        assertEquals(401L, inserted.getVersionId());
        assertEquals(1002L, inserted.getPackageId());
        assertEquals("专业版", inserted.getPackageName());
        assertEquals("enabled", inserted.getScopeStatus());
    }

    @Test
    void authorizesOnlyMatchingPackageScope() {
        when(scopeMapper.selectCount(any())).thenReturn(1L);

        assertTrue(service.isFactorVersionAuthorized(401L, 1002L));
    }

    @Test
    void deniesMissingPackageDuringAuthorization() {
        assertFalse(service.isFactorVersionAuthorized(401L, null));
    }

    @Test
    void rejectsScopeWithoutPackage() {
        CvFactorCustomerScopeBo bo = validPackageScopeBo();
        bo.setPackageId(null);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertFactorCustomerScope(bo));

        assertEquals("Factor scope must reference a package", exception.getMessage());
    }

    @Test
    void rejectsDuplicateScopeMetadata() {
        when(scopeMapper.selectCount(any())).thenReturn(1L);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertFactorCustomerScope(validPackageScopeBo()));

        assertEquals("Factor scope already exists for this version and package", exception.getMessage());
    }

    @Test
    void removeSoftDeletesFactorScopes() {
        CvFactorCustomerScope existing = enabledPackageScope();
        existing.setId(55L);
        when(scopeMapper.selectList(any())).thenReturn(List.of(existing));
        when(scopeMapper.updateBatchById(any())).thenReturn(true);

        int removed = service.deleteFactorCustomerScopeByIds(new Long[] {55L});

        assertEquals(1, removed);
        ArgumentCaptor<List<CvFactorCustomerScope>> updateCaptor = ArgumentCaptor.forClass(List.class);
        verify(scopeMapper).updateBatchById(updateCaptor.capture());
        assertEquals("deleted", updateCaptor.getValue().get(0).getScopeStatus());
        verify(scopeMapper, never()).deleteByIds(any());
    }

    @Test
    void insertRestoresDeletedFactorScopeWithSamePackageKey() {
        CvFactorCustomerScope deletedScope = enabledPackageScope();
        deletedScope.setId(55L);
        deletedScope.setScopeStatus("deleted");
        when(scopeMapper.selectOne(any(), any(Boolean.class))).thenReturn(deletedScope);

        service.insertFactorCustomerScope(validPackageScopeBo());

        ArgumentCaptor<CvFactorCustomerScope> updateCaptor = ArgumentCaptor.forClass(CvFactorCustomerScope.class);
        verify(scopeMapper).updateById(updateCaptor.capture());
        CvFactorCustomerScope restored = updateCaptor.getValue();
        assertEquals(55L, restored.getId());
        assertEquals("enabled", restored.getScopeStatus());
        verify(scopeMapper, never()).insert(any(CvFactorCustomerScope.class));
    }

    @Test
    void normalizesScopeStatusForListQueries() {
        CvFactorCustomerScopeBo bo = new CvFactorCustomerScopeBo();
        bo.setScopeStatus(" ENABLED ");
        when(scopeMapper.selectVoPage(any(), any())).thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());

        service.selectPageFactorCustomerScopeList(bo, new org.dromara.common.mybatis.core.page.PageQuery(1, 10));

        verify(scopeMapper).selectVoPage(any(), any());
        assertEquals("enabled", normalizeScopeStatusFilter(" ENABLED "));
    }

    @Test
    void treatsInvalidScopeStatusFilterAsNoMatchQuery() {
        CvFactorCustomerScopeBo bo = new CvFactorCustomerScopeBo();
        bo.setScopeStatus(" archived ");
        when(scopeMapper.selectVoPage(any(), any())).thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());

        service.selectPageFactorCustomerScopeList(bo, new org.dromara.common.mybatis.core.page.PageQuery(1, 10));

        verify(scopeMapper).selectVoPage(any(), any());
        assertEquals("__invalid_scope_status__", normalizeScopeStatusFilter(" archived "));
    }

    @Test
    void rejectsScopingDraftVersion() {
        when(factorVersionMapper.selectById(401L)).thenReturn(draftVersion());

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertFactorCustomerScope(validPackageScopeBo()));

        assertEquals("Only published or frozen factor versions can be scoped", exception.getMessage());
    }

    @Test
    void addGroupStillRequiresVersionId() {
        CvFactorCustomerScopeBo bo = validPackageScopeBo();
        bo.setVersionId(null);

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<CvFactorCustomerScopeBo>> violations = validator.validate(bo, AddGroup.class);

        assertFalse(violations.isEmpty());
    }

    private CvFactorCustomerScopeBo validPackageScopeBo() {
        CvFactorCustomerScopeBo bo = new CvFactorCustomerScopeBo();
        bo.setVersionId(401L);
        bo.setPackageId(1002L);
        bo.setScopeStatus(null);
        return bo;
    }

    private String normalizeScopeStatusFilter(String scopeStatus) {
        try {
            Method method = CvFactorCustomerScopeServiceImpl.class
                .getDeclaredMethod("normalizeScopeStatusFilter", String.class);
            method.setAccessible(true);
            return (String) method.invoke(service, scopeStatus);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private CvFactorCustomerScope enabledPackageScope() {
        CvFactorCustomerScope scope = new CvFactorCustomerScope();
        scope.setVersionId(401L);
        scope.setPackageId(1002L);
        scope.setScopeStatus("enabled");
        return scope;
    }

    private CvFactorVersion publishedVersion() {
        CvFactorVersion version = new CvFactorVersion();
        version.setId(401L);
        version.setVersionCode("FACTOR-2026-02");
        version.setPublishStatus("published");
        version.setFrozenFlag(Boolean.FALSE);
        return version;
    }

    private CvFactorVersion draftVersion() {
        CvFactorVersion version = publishedVersion();
        version.setPublishStatus("draft");
        return version;
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
