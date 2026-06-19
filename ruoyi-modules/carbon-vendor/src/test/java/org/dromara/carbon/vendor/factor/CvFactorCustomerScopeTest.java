package org.dromara.carbon.vendor.factor;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.dromara.carbon.vendor.domain.CvCustomer;
import org.dromara.carbon.vendor.domain.CvFactorCustomerScope;
import org.dromara.carbon.vendor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.bo.CvFactorCustomerScopeBo;
import org.dromara.carbon.vendor.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.mapper.CvFactorCustomerScopeMapper;
import org.dromara.carbon.vendor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.service.impl.CvFactorCustomerScopeServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.validate.AddGroup;
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
    private CvCustomerMapper customerMapper;
    private CvLicenseIssueMapper licenseIssueMapper;
    private SysTenantPackageMapper tenantPackageMapper;
    private CvFactorCustomerScopeServiceImpl service;

    @BeforeEach
    void setUp() {
        scopeMapper = mock(CvFactorCustomerScopeMapper.class);
        factorVersionMapper = mock(CvFactorVersionMapper.class);
        customerMapper = mock(CvCustomerMapper.class);
        licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        tenantPackageMapper = mock(SysTenantPackageMapper.class);
        when(factorVersionMapper.selectById(401L)).thenReturn(publishedVersion());
        when(customerMapper.selectById(1001L)).thenReturn(activeCustomer());
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(issuedLicense());
        when(scopeMapper.selectCount(any())).thenReturn(0L);
        when(scopeMapper.selectOne(any(), any(Boolean.class))).thenReturn(null);
        service = new CvFactorCustomerScopeServiceImpl(scopeMapper, factorVersionMapper, customerMapper, licenseIssueMapper, tenantPackageMapper) {
            @Override
            protected CvFactorCustomerScope toEntity(CvFactorCustomerScopeBo bo) {
                CvFactorCustomerScope scope = new CvFactorCustomerScope();
                scope.setId(bo.getId());
                scope.setVersionId(bo.getVersionId());
                scope.setCustomerId(bo.getCustomerId());
                scope.setEdition(bo.getEdition());
                scope.setLicenseId(bo.getLicenseId());
                scope.setScopeStatus(bo.getScopeStatus());
                return scope;
            }
        };
    }

    @Test
    void insertsCustomerScopedMetadataForReleasedVersion() {
        service.insertFactorCustomerScope(validCustomerScopeBo());

        ArgumentCaptor<CvFactorCustomerScope> insertCaptor = ArgumentCaptor.forClass(CvFactorCustomerScope.class);
        verify(scopeMapper).insert(insertCaptor.capture());
        CvFactorCustomerScope inserted = insertCaptor.getValue();
        assertEquals(401L, inserted.getVersionId());
        assertEquals(1001L, inserted.getCustomerId());
        assertNull(inserted.getEdition());
        assertNull(inserted.getLicenseId());
        assertEquals("enabled", inserted.getScopeStatus());
    }

    @Test
    void allowsEditionScopedMetadataWithoutLicenseAssignmentAndAuthorizesPurchasedEdition() {
        CvFactorCustomerScopeBo bo = validEditionScopeBo();
        bo.setLicenseId(" LIC-001 ");

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<CvFactorCustomerScopeBo>> violations = validator.validate(bo, AddGroup.class);
        assertTrue(violations.isEmpty());

        service.insertFactorCustomerScope(bo);
        when(scopeMapper.selectList(any())).thenReturn(List.of(enabledEditionScope()));

        boolean authorized = service.isFactorVersionAuthorized(401L, null, null, "PRO", "LIC-001");

        ArgumentCaptor<CvFactorCustomerScope> insertCaptor = ArgumentCaptor.forClass(CvFactorCustomerScope.class);
        verify(scopeMapper).insert(insertCaptor.capture());
        CvFactorCustomerScope inserted = insertCaptor.getValue();
        assertNull(inserted.getCustomerId());
        assertEquals("pro", inserted.getEdition());
        assertNull(inserted.getLicenseId());
        assertTrue(authorized);
    }

    @Test
    void authorizesCustomerScopeUsingLicenseEntitlementMetadata() {
        when(scopeMapper.selectList(any())).thenReturn(List.of(enabledCustomerScope()));

        boolean authorized = service.isFactorVersionAuthorized(401L, null, null, null, " LIC-001 ");

        assertTrue(authorized);
    }

    @Test
    void treatsBlankCustomerAndEditionAsVersionWideScopeForValidLicenseEntitlement() {
        CvFactorCustomerScopeBo bo = validVersionWideScopeBo();
        service.insertFactorCustomerScope(bo);
        when(scopeMapper.selectList(any())).thenReturn(List.of(enabledVersionWideScope()));

        boolean authorized = service.isFactorVersionAuthorized(401L, null, null, null, "LIC-001");

        ArgumentCaptor<CvFactorCustomerScope> insertCaptor = ArgumentCaptor.forClass(CvFactorCustomerScope.class);
        verify(scopeMapper).insert(insertCaptor.capture());
        CvFactorCustomerScope inserted = insertCaptor.getValue();
        assertNull(inserted.getCustomerId());
        assertNull(inserted.getEdition());
        assertNull(inserted.getLicenseId());
        assertTrue(authorized);
    }

    @Test
    void deniesUnauthorizedCustomerForReleasedVersion() {
        when(scopeMapper.selectList(any())).thenReturn(List.of(enabledCustomerScope()));

        boolean authorized = service.isFactorVersionAuthorized(401L, 2002L, null, null, "LIC-001");

        assertFalse(authorized);
    }

    @Test
    void rejectsForgedCustomerOrEditionWithoutLicenseEntitlement() {
        when(scopeMapper.selectList(any())).thenReturn(List.of(enabledEditionScope(), enabledCustomerScope()));

        boolean forgedCustomer = service.isFactorVersionAuthorized(401L, 1001L, null, null, null);
        boolean forgedEdition = service.isFactorVersionAuthorized(401L, null, null, "pro", null);

        assertFalse(forgedCustomer);
        assertFalse(forgedEdition);
    }

    @Test
    void rejectsForgedCustomerOrEditionWhenLicenseEntitlementDiffers() {
        when(scopeMapper.selectList(any())).thenReturn(List.of(enabledEditionScope(), enabledCustomerScope()));

        boolean forgedCustomer = service.isFactorVersionAuthorized(401L, 2002L, null, null, "LIC-001");
        boolean forgedEdition = service.isFactorVersionAuthorized(401L, null, null, "enterprise", "LIC-001");

        assertFalse(forgedCustomer);
        assertFalse(forgedEdition);
    }

    @Test
    void returnsFalseForUnknownLicenseDuringAuthorization() {
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(null);

        boolean authorized = service.isFactorVersionAuthorized(401L, null, null, null, "LIC-UNKNOWN");

        assertFalse(authorized);
    }

    @Test
    void deniesAuthorizationForRevokedLicenseEntitlement() {
        CvLicenseIssue revoked = issuedLicense();
        revoked.setIssueStatus("revoked");
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(revoked);
        when(scopeMapper.selectList(any())).thenReturn(List.of(enabledCustomerScope()));

        boolean authorized = service.isFactorVersionAuthorized(401L, null, null, null, "LIC-001");

        assertFalse(authorized);
    }

    @Test
    void rejectsDuplicateScopeMetadata() {
        when(scopeMapper.selectCount(any())).thenReturn(1L);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertFactorCustomerScope(validCustomerScopeBo()));

        assertEquals("Factor scope already exists for this version, customer, and package", exception.getMessage());
    }

    @Test
    void ignoresSoftDeletedScopeWhenCheckingDuplicates() {
        when(scopeMapper.selectCount(any())).thenReturn(0L);

        service.insertFactorCustomerScope(validCustomerScopeBo());

        ArgumentCaptor<CvFactorCustomerScope> insertCaptor = ArgumentCaptor.forClass(CvFactorCustomerScope.class);
        verify(scopeMapper).insert(insertCaptor.capture());
        assertEquals("enabled", insertCaptor.getValue().getScopeStatus());
    }

    @Test
    void removeSoftDeletesFactorScopes() {
        CvFactorCustomerScope existing = enabledCustomerScope();
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
    void insertRestoresDeletedFactorScopeWithSameEntitlementKey() {
        CvFactorCustomerScope deletedScope = enabledCustomerScope();
        deletedScope.setId(55L);
        deletedScope.setScopeStatus("deleted");
        when(scopeMapper.selectOne(any(), any(Boolean.class))).thenReturn(deletedScope);

        service.insertFactorCustomerScope(validCustomerScopeBo());

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

        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CvFactorCustomerScope>> queryCaptor =
            ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class);
        verify(scopeMapper).selectVoPage(any(), queryCaptor.capture());
        assertEquals("enabled", normalizeScopeStatusFilter(" ENABLED "));
    }

    @Test
    void treatsInvalidScopeStatusFilterAsNoMatchQuery() {
        CvFactorCustomerScopeBo bo = new CvFactorCustomerScopeBo();
        bo.setScopeStatus(" archived ");
        when(scopeMapper.selectVoPage(any(), any())).thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());

        service.selectPageFactorCustomerScopeList(bo, new org.dromara.common.mybatis.core.page.PageQuery(1, 10));

        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CvFactorCustomerScope>> queryCaptor =
            ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class);
        verify(scopeMapper).selectVoPage(any(), queryCaptor.capture());
        assertEquals("__invalid_scope_status__", normalizeScopeStatusFilter(" archived "));
    }

    @Test
    void normalizesLicenseIdForListQueries() {
        CvFactorCustomerScopeBo bo = new CvFactorCustomerScopeBo();
        bo.setLicenseId(" LIC-001 ");
        when(scopeMapper.selectVoPage(any(), any())).thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());

        service.selectPageFactorCustomerScopeList(bo, new org.dromara.common.mybatis.core.page.PageQuery(1, 10));

        verify(scopeMapper).selectVoPage(any(), any());
        assertEquals("LIC-001", normalizeLicenseId(" LIC-001 "));
    }

    @Test
    void rejectsScopingDraftVersion() {
        when(factorVersionMapper.selectById(401L)).thenReturn(draftVersion());

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertFactorCustomerScope(validCustomerScopeBo()));

        assertEquals("Only published or frozen factor versions can be scoped", exception.getMessage());
    }

    @Test
    void allowsScopeWithoutLicenseEntitlementMetadata() {
        CvFactorCustomerScopeBo bo = validCustomerScopeBo();
        bo.setLicenseId(" ");

        service.insertFactorCustomerScope(bo);

        ArgumentCaptor<CvFactorCustomerScope> insertCaptor = ArgumentCaptor.forClass(CvFactorCustomerScope.class);
        verify(scopeMapper).insert(insertCaptor.capture());
        assertNull(insertCaptor.getValue().getLicenseId());
    }

    private CvFactorCustomerScopeBo validCustomerScopeBo() {
        CvFactorCustomerScopeBo bo = new CvFactorCustomerScopeBo();
        bo.setVersionId(401L);
        bo.setCustomerId(1001L);
        bo.setLicenseId(" LIC-001 ");
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

    private String normalizeLicenseId(String licenseId) {
        try {
            Method method = CvFactorCustomerScopeServiceImpl.class
                .getDeclaredMethod("normalizeLicenseId", String.class);
            method.setAccessible(true);
            return (String) method.invoke(service, licenseId);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private CvFactorCustomerScopeBo validEditionScopeBo() {
        CvFactorCustomerScopeBo bo = new CvFactorCustomerScopeBo();
        bo.setVersionId(401L);
        bo.setEdition(" Pro ");
        bo.setLicenseId(" LIC-001 ");
        bo.setScopeStatus(" ENABLED ");
        return bo;
    }

    private CvFactorCustomerScopeBo validVersionWideScopeBo() {
        CvFactorCustomerScopeBo bo = new CvFactorCustomerScopeBo();
        bo.setVersionId(401L);
        bo.setLicenseId(" LIC-001 ");
        return bo;
    }

    private CvFactorCustomerScope enabledCustomerScope() {
        CvFactorCustomerScope scope = new CvFactorCustomerScope();
        scope.setVersionId(401L);
        scope.setCustomerId(1001L);
        scope.setScopeStatus("enabled");
        return scope;
    }

    private CvFactorCustomerScope enabledEditionScope() {
        CvFactorCustomerScope scope = new CvFactorCustomerScope();
        scope.setVersionId(401L);
        scope.setEdition("pro");
        scope.setScopeStatus("enabled");
        return scope;
    }

    private CvFactorCustomerScope enabledVersionWideScope() {
        CvFactorCustomerScope scope = new CvFactorCustomerScope();
        scope.setVersionId(401L);
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

    private CvCustomer activeCustomer() {
        CvCustomer customer = new CvCustomer();
        customer.setId(1001L);
        customer.setCustomerStatus("active");
        return customer;
    }

    private CvLicenseIssue issuedLicense() {
        CvLicenseIssue licenseIssue = new CvLicenseIssue();
        licenseIssue.setLicenseId("LIC-001");
        licenseIssue.setCustomerId(1001L);
        licenseIssue.setEdition("pro");
        licenseIssue.setIssueStatus("issued");
        return licenseIssue;
    }
}
