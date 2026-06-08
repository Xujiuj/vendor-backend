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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvFactorCustomerScopeTest {

    private CvFactorCustomerScopeMapper scopeMapper;
    private CvFactorVersionMapper factorVersionMapper;
    private CvCustomerMapper customerMapper;
    private CvLicenseIssueMapper licenseIssueMapper;
    private CvFactorCustomerScopeServiceImpl service;

    @BeforeEach
    void setUp() {
        scopeMapper = mock(CvFactorCustomerScopeMapper.class);
        factorVersionMapper = mock(CvFactorVersionMapper.class);
        customerMapper = mock(CvCustomerMapper.class);
        licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        when(factorVersionMapper.selectById(401L)).thenReturn(publishedVersion());
        when(customerMapper.selectById(1001L)).thenReturn(activeCustomer());
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(issuedLicense());
        when(scopeMapper.selectCount(any())).thenReturn(0L);
        service = new CvFactorCustomerScopeServiceImpl(scopeMapper, factorVersionMapper, customerMapper, licenseIssueMapper) {
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
        assertEquals("LIC-001", inserted.getLicenseId());
        assertEquals("enabled", inserted.getScopeStatus());
    }

    @Test
    void allowsEditionScopedMetadataWithoutCustomerAndAuthorizesMatchingEdition() {
        CvFactorCustomerScopeBo bo = validEditionScopeBo();
        bo.setLicenseId(" LIC-001 ");

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<CvFactorCustomerScopeBo>> violations = validator.validate(bo, AddGroup.class);
        assertTrue(violations.isEmpty());

        service.insertFactorCustomerScope(bo);
        when(scopeMapper.selectList(any())).thenReturn(List.of(enabledEditionScope()));

        boolean authorized = service.isFactorVersionAuthorized(401L, null, "PRO", "LIC-001");

        ArgumentCaptor<CvFactorCustomerScope> insertCaptor = ArgumentCaptor.forClass(CvFactorCustomerScope.class);
        verify(scopeMapper).insert(insertCaptor.capture());
        CvFactorCustomerScope inserted = insertCaptor.getValue();
        assertNull(inserted.getCustomerId());
        assertEquals("pro", inserted.getEdition());
        assertTrue(authorized);
    }

    @Test
    void authorizesCustomerScopeUsingLicenseEntitlementMetadata() {
        when(scopeMapper.selectList(any())).thenReturn(List.of(enabledCustomerScope()));

        boolean authorized = service.isFactorVersionAuthorized(401L, null, null, " LIC-001 ");

        assertTrue(authorized);
    }

    @Test
    void allowsLicenseOnlyScopeAndRequiresMatchingLicenseEntitlement() {
        CvFactorCustomerScopeBo bo = validLicenseOnlyScopeBo();
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(issuedLicense(), issuedLicense(), null);
        service.insertFactorCustomerScope(bo);
        when(scopeMapper.selectList(any())).thenReturn(List.of(enabledLicenseScope()));

        boolean authorized = service.isFactorVersionAuthorized(401L, null, null, "LIC-001");
        boolean unauthorized = service.isFactorVersionAuthorized(401L, 1001L, "pro", "LIC-UNKNOWN");

        ArgumentCaptor<CvFactorCustomerScope> insertCaptor = ArgumentCaptor.forClass(CvFactorCustomerScope.class);
        verify(scopeMapper).insert(insertCaptor.capture());
        CvFactorCustomerScope inserted = insertCaptor.getValue();
        assertNull(inserted.getCustomerId());
        assertNull(inserted.getEdition());
        assertEquals("LIC-001", inserted.getLicenseId());
        assertTrue(authorized);
        assertFalse(unauthorized);
    }

    @Test
    void deniesUnauthorizedCustomerForReleasedVersion() {
        when(scopeMapper.selectList(any())).thenReturn(List.of(enabledCustomerScope()));

        boolean authorized = service.isFactorVersionAuthorized(401L, 2002L, null, "LIC-001");

        assertFalse(authorized);
    }

    @Test
    void rejectsForgedCustomerOrEditionWithoutLicenseEntitlement() {
        when(scopeMapper.selectList(any())).thenReturn(List.of(enabledEditionScope(), enabledCustomerScope()));

        boolean forgedCustomer = service.isFactorVersionAuthorized(401L, 1001L, null, null);
        boolean forgedEdition = service.isFactorVersionAuthorized(401L, null, "pro", null);

        assertFalse(forgedCustomer);
        assertFalse(forgedEdition);
    }

    @Test
    void rejectsForgedCustomerOrEditionWhenLicenseEntitlementDiffers() {
        when(scopeMapper.selectList(any())).thenReturn(List.of(enabledEditionScope(), enabledCustomerScope()));

        boolean forgedCustomer = service.isFactorVersionAuthorized(401L, 2002L, null, "LIC-001");
        boolean forgedEdition = service.isFactorVersionAuthorized(401L, null, "enterprise", "LIC-001");

        assertFalse(forgedCustomer);
        assertFalse(forgedEdition);
    }

    @Test
    void returnsFalseForUnknownLicenseDuringAuthorization() {
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(null);

        boolean authorized = service.isFactorVersionAuthorized(401L, null, null, "LIC-UNKNOWN");

        assertFalse(authorized);
    }

    @Test
    void rejectsRevokedLicenseDuringScopeBinding() {
        CvLicenseIssue revoked = issuedLicense();
        revoked.setIssueStatus("revoked");
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(revoked);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertFactorCustomerScope(validCustomerScopeBo()));

        assertEquals("Referenced vendor license entitlement is revoked", exception.getMessage());
    }

    @Test
    void rejectsRevokedTimeDuringScopeBinding() {
        CvLicenseIssue revoked = issuedLicense();
        revoked.setRevokedTime(new java.util.Date());
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(revoked);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertFactorCustomerScope(validCustomerScopeBo()));

        assertEquals("Referenced vendor license entitlement is revoked", exception.getMessage());
    }

    @Test
    void deniesAuthorizationForRevokedLicenseEntitlement() {
        CvLicenseIssue revoked = issuedLicense();
        revoked.setIssueStatus("revoked");
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(revoked);
        when(scopeMapper.selectList(any())).thenReturn(List.of(enabledCustomerScope()));

        boolean authorized = service.isFactorVersionAuthorized(401L, null, null, "LIC-001");

        assertFalse(authorized);
    }

    @Test
    void rejectsDuplicateScopeMetadata() {
        when(scopeMapper.selectCount(any())).thenReturn(1L);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertFactorCustomerScope(validCustomerScopeBo()));

        assertEquals("Factor scope already exists for this version and entitlement metadata", exception.getMessage());
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
    void rejectsScopeWithoutLicenseEntitlementMetadata() {
        CvFactorCustomerScopeBo bo = validCustomerScopeBo();
        bo.setLicenseId(" ");

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertFactorCustomerScope(bo));

        assertEquals("Factor scope must reference vendor license entitlement metadata", exception.getMessage());
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

    private CvFactorCustomerScopeBo validLicenseOnlyScopeBo() {
        CvFactorCustomerScopeBo bo = new CvFactorCustomerScopeBo();
        bo.setVersionId(401L);
        bo.setLicenseId(" LIC-001 ");
        return bo;
    }

    private CvFactorCustomerScope enabledCustomerScope() {
        CvFactorCustomerScope scope = new CvFactorCustomerScope();
        scope.setVersionId(401L);
        scope.setCustomerId(1001L);
        scope.setLicenseId("LIC-001");
        scope.setScopeStatus("enabled");
        return scope;
    }

    private CvFactorCustomerScope enabledEditionScope() {
        CvFactorCustomerScope scope = new CvFactorCustomerScope();
        scope.setVersionId(401L);
        scope.setEdition("pro");
        scope.setLicenseId("LIC-001");
        scope.setScopeStatus("enabled");
        return scope;
    }

    private CvFactorCustomerScope enabledLicenseScope() {
        CvFactorCustomerScope scope = new CvFactorCustomerScope();
        scope.setVersionId(401L);
        scope.setLicenseId("LIC-001");
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
