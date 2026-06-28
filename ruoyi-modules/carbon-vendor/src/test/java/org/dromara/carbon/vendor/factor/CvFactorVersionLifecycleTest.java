package org.dromara.carbon.vendor.factor;

import org.dromara.carbon.vendor.factor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.factor.domain.bo.CvFactorVersionBo;
import org.dromara.carbon.vendor.factor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.factor.service.impl.CvFactorVersionServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvFactorVersionLifecycleTest {

    private CvFactorVersionMapper factorVersionMapper;
    private CvFactorVersionServiceImpl service;

    @BeforeEach
    void setUp() {
        factorVersionMapper = mock(CvFactorVersionMapper.class);
        service = new CvFactorVersionServiceImpl(factorVersionMapper);
    }

    @Test
    void publishesDraftVersionAndPersistsAuditMetadata() {
        when(factorVersionMapper.selectById(101L)).thenReturn(draftVersion());

        service.releaseFactorVersion(101L, "vendor-admin");

        ArgumentCaptor<CvFactorVersion> updateCaptor = ArgumentCaptor.forClass(CvFactorVersion.class);
        verify(factorVersionMapper).updateById(updateCaptor.capture());
        CvFactorVersion updated = updateCaptor.getValue();
        assertEquals("published", updated.getPublishStatus());
        assertFalse(updated.getFrozenFlag());
        assertEquals("vendor-admin", updated.getPublishedBy());
        assertNotNull(updated.getPublishedTime());
        assertTrue(updated.getRemark().contains("factor-version-publish"));
        assertTrue(updated.getRemark().contains("vendor-admin"));
    }

    @Test
    void rejectsReleaseFromFrozenState() {
        when(factorVersionMapper.selectById(101L)).thenReturn(frozenVersion());

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.releaseFactorVersion(101L, "vendor-admin"));

        assertEquals("仅草稿状态的因子版本允许发布", exception.getMessage());
    }

    @Test
    void updatesPublishedVersionMetadataWithoutChangingLifecycle() {
        CvFactorVersion existing = publishedVersion();
        Date publishedTime = existing.getPublishedTime();
        when(factorVersionMapper.selectById(101L)).thenReturn(existing);
        when(factorVersionMapper.selectCount(org.mockito.ArgumentMatchers.any())).thenReturn(0L);
        when(factorVersionMapper.updateById(org.mockito.ArgumentMatchers.any(CvFactorVersion.class))).thenReturn(1);

        CvFactorVersionBo bo = new CvFactorVersionBo();
        bo.setId(101L);
        bo.setVersionCode("FACTOR-2026-01-UPDATED");
        bo.setVersionName("Updated published factor");
        bo.setRemark("updated remark");

        assertTrue(service.updateFactorVersion(bo));

        ArgumentCaptor<CvFactorVersion> updateCaptor = ArgumentCaptor.forClass(CvFactorVersion.class);
        verify(factorVersionMapper).updateById(updateCaptor.capture());
        CvFactorVersion updated = updateCaptor.getValue();
        assertEquals("FACTOR-2026-01-UPDATED", updated.getVersionCode());
        assertEquals("Updated published factor", updated.getVersionName());
        assertEquals("updated remark", updated.getRemark());
        assertEquals("published", updated.getPublishStatus());
        assertFalse(updated.getFrozenFlag());
        assertEquals("release-user", updated.getPublishedBy());
        assertEquals(publishedTime, updated.getPublishedTime());
    }

    @Test
    void updatesLifecycleMetadataFromEditForm() {
        CvFactorVersion existing = publishedVersion();
        when(factorVersionMapper.selectById(101L)).thenReturn(existing);
        when(factorVersionMapper.selectCount(org.mockito.ArgumentMatchers.any())).thenReturn(0L);
        when(factorVersionMapper.updateById(org.mockito.ArgumentMatchers.any(CvFactorVersion.class))).thenReturn(1);

        CvFactorVersionBo bo = new CvFactorVersionBo();
        bo.setId(101L);
        bo.setVersionCode("FACTOR-2026-01");
        bo.setVersionName("Factor 2026.01");
        bo.setPublishStatus("frozen");
        bo.setFrozenFlag(Boolean.TRUE);

        assertTrue(service.updateFactorVersion(bo));

        ArgumentCaptor<CvFactorVersion> updateCaptor = ArgumentCaptor.forClass(CvFactorVersion.class);
        verify(factorVersionMapper).updateById(updateCaptor.capture());
        CvFactorVersion updated = updateCaptor.getValue();
        assertEquals("frozen", updated.getPublishStatus());
        assertTrue(updated.getFrozenFlag());
    }

    @Test
    void freezesPublishedVersionAndAppendsAuditRemark() {
        when(factorVersionMapper.selectById(101L)).thenReturn(publishedVersion());

        service.freezeFactorVersion(101L, "auditor");

        ArgumentCaptor<CvFactorVersion> updateCaptor = ArgumentCaptor.forClass(CvFactorVersion.class);
        verify(factorVersionMapper).updateById(updateCaptor.capture());
        CvFactorVersion updated = updateCaptor.getValue();
        assertTrue(updated.getFrozenFlag());
        assertEquals("frozen", updated.getPublishStatus());
        assertTrue(updated.getRemark().contains("existing remark"));
        assertTrue(updated.getRemark().contains("factor-version-freeze"));
        assertTrue(updated.getRemark().contains("auditor"));
    }

    @Test
    void unfreezesFrozenVersionAndAppendsAuditRemark() {
        when(factorVersionMapper.selectById(101L)).thenReturn(frozenVersion());

        service.unfreezeFactorVersion(101L, "auditor");

        ArgumentCaptor<CvFactorVersion> updateCaptor = ArgumentCaptor.forClass(CvFactorVersion.class);
        verify(factorVersionMapper).updateById(updateCaptor.capture());
        CvFactorVersion updated = updateCaptor.getValue();
        assertFalse(updated.getFrozenFlag());
        assertEquals("published", updated.getPublishStatus());
        assertTrue(updated.getRemark().contains("existing remark"));
        assertTrue(updated.getRemark().contains("factor-version-unfreeze"));
        assertTrue(updated.getRemark().contains("auditor"));
    }

    @Test
    void rejectsUnfreezeFromPublishedState() {
        when(factorVersionMapper.selectById(101L)).thenReturn(publishedVersion());

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.unfreezeFactorVersion(101L, "auditor"));

        assertEquals("仅已冻结的因子版本允许解冻", exception.getMessage());
    }

    @Test
    void rejectsFreezeFromDraftState() {
        when(factorVersionMapper.selectById(101L)).thenReturn(draftVersion());

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.freezeFactorVersion(101L, "auditor"));

        assertEquals("仅已发布的因子版本允许冻结", exception.getMessage());
    }

    @Test
    void retiresFrozenVersionWithoutReadingEnterpriseData() {
        when(factorVersionMapper.selectById(101L)).thenReturn(frozenVersion());

        service.retireFactorVersion(101L, "vendor-admin");

        ArgumentCaptor<CvFactorVersion> updateCaptor = ArgumentCaptor.forClass(CvFactorVersion.class);
        verify(factorVersionMapper).updateById(updateCaptor.capture());
        CvFactorVersion updated = updateCaptor.getValue();
        assertEquals("retired", updated.getPublishStatus());
        assertFalse(updated.getFrozenFlag());
        assertTrue(updated.getRemark().contains("factor-version-retire"));
    }

    @Test
    void rejectsRetireWhenDraftVersionHasFrozenFlag() {
        CvFactorVersion version = draftVersion();
        version.setFrozenFlag(Boolean.TRUE);
        when(factorVersionMapper.selectById(101L)).thenReturn(version);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.retireFactorVersion(101L, "vendor-admin"));

        assertEquals("Inconsistent factor version lifecycle metadata", exception.getMessage());
    }

    @Test
    void restoresRetiredVersionToDraftAndAppendsAuditRemark() {
        when(factorVersionMapper.selectById(101L)).thenReturn(retiredVersion());

        service.restoreFactorVersion(101L, "vendor-admin");

        ArgumentCaptor<CvFactorVersion> updateCaptor = ArgumentCaptor.forClass(CvFactorVersion.class);
        verify(factorVersionMapper).updateById(updateCaptor.capture());
        CvFactorVersion updated = updateCaptor.getValue();
        assertEquals("draft", updated.getPublishStatus());
        assertFalse(updated.getFrozenFlag());
        assertTrue(updated.getRemark().contains("existing remark"));
        assertTrue(updated.getRemark().contains("factor-version-restore"));
        assertTrue(updated.getRemark().contains("vendor-admin"));
        assertTrue(updated.getRemark().matches("(?s).*\\[[^]]+].*"));
    }

    @Test
    void rejectsRestoreFromDraftStateWithoutPersistence() {
        assertRejectsRestoreWithoutPersistence(draftVersion());
    }

    @Test
    void rejectsRestoreFromPublishedStateWithoutPersistence() {
        assertRejectsRestoreWithoutPersistence(publishedVersion());
    }

    @Test
    void rejectsRestoreFromFrozenStateWithoutPersistence() {
        assertRejectsRestoreWithoutPersistence(frozenVersion());
    }

    @Test
    void rejectsRestoreFromInconsistentRetiredMetadataWithoutPersistence() {
        CvFactorVersion version = retiredVersion();
        version.setFrozenFlag(Boolean.TRUE);

        assertRejectsRestoreWithoutPersistence(version);
    }

    @Test
    void rejectsBlankStatusWhenFrozenFlagIsSet() {
        CvFactorVersion version = draftVersion();
        version.setPublishStatus(" ");
        version.setFrozenFlag(Boolean.TRUE);
        when(factorVersionMapper.selectById(101L)).thenReturn(version);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.freezeFactorVersion(101L, "auditor"));

        assertEquals("Inconsistent factor version lifecycle metadata", exception.getMessage());
    }

    private void assertRejectsRestoreWithoutPersistence(CvFactorVersion invalidVersion) {
        when(factorVersionMapper.selectById(101L)).thenReturn(invalidVersion);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.restoreFactorVersion(101L, "vendor-admin"));

        assertEquals("仅已退役且未冻结的因子版本允许恢复", exception.getMessage());
        verify(factorVersionMapper, never()).updateById(org.mockito.ArgumentMatchers.any(CvFactorVersion.class));
    }

    private CvFactorVersion draftVersion() {
        CvFactorVersion version = baseVersion();
        version.setPublishStatus("draft");
        version.setFrozenFlag(Boolean.FALSE);
        version.setPublishedBy(null);
        version.setPublishedTime(null);
        version.setRemark(null);
        return version;
    }

    private CvFactorVersion publishedVersion() {
        CvFactorVersion version = baseVersion();
        version.setPublishStatus("published");
        version.setFrozenFlag(Boolean.FALSE);
        version.setPublishedBy("release-user");
        version.setPublishedTime(new Date());
        version.setRemark("existing remark");
        return version;
    }

    private CvFactorVersion frozenVersion() {
        CvFactorVersion version = publishedVersion();
        version.setPublishStatus("frozen");
        version.setFrozenFlag(Boolean.TRUE);
        return version;
    }

    private CvFactorVersion retiredVersion() {
        CvFactorVersion version = publishedVersion();
        version.setPublishStatus("retired");
        version.setFrozenFlag(Boolean.FALSE);
        return version;
    }

    private CvFactorVersion baseVersion() {
        CvFactorVersion version = new CvFactorVersion();
        version.setId(101L);
        version.setVersionCode("FACTOR-2026-01");
        version.setVersionName("Factor 2026.01");
        return version;
    }
}
