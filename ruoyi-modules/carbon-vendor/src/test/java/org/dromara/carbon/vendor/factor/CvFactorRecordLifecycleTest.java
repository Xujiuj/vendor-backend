package org.dromara.carbon.vendor.factor;

import org.dromara.carbon.vendor.domain.CvFactorRecord;
import org.dromara.carbon.vendor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.domain.bo.CvFactorRecordBo;
import org.dromara.carbon.vendor.mapper.CvFactorRecordMapper;
import org.dromara.carbon.vendor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.service.impl.CvFactorRecordServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvFactorRecordLifecycleTest {

    private CvFactorRecordMapper factorRecordMapper;
    private CvFactorVersionMapper factorVersionMapper;
    private CvFactorRecordServiceImpl service;

    @BeforeEach
    void setUp() {
        factorRecordMapper = mock(CvFactorRecordMapper.class);
        factorVersionMapper = mock(CvFactorVersionMapper.class);
        service = new CvFactorRecordServiceImpl(factorRecordMapper, factorVersionMapper) {
            @Override
            protected CvFactorRecord toEntity(CvFactorRecordBo bo) {
                CvFactorRecord record = new CvFactorRecord();
                record.setId(bo.getId());
                record.setVersionId(bo.getVersionId());
                record.setFactorCode(bo.getFactorCode());
                record.setFactorName(bo.getFactorName());
                record.setFactorCategory(bo.getFactorCategory());
                record.setFactorValue(bo.getFactorValue());
                record.setFactorUnit(bo.getFactorUnit());
                record.setSourceRef(bo.getSourceRef());
                record.setEnabledFlag(bo.getEnabledFlag());
                return record;
            }
        };
    }

    @Test
    void allowsDraftVersionFactorRecordInsert() {
        when(factorVersionMapper.selectById(501L)).thenReturn(draftVersion());

        service.insertFactorRecord(validRecordBo(501L));

        ArgumentCaptor<CvFactorRecord> insertCaptor = ArgumentCaptor.forClass(CvFactorRecord.class);
        verify(factorRecordMapper).insert(insertCaptor.capture());
        assertEquals(501L, insertCaptor.getValue().getVersionId());
    }

    @Test
    void rejectsInsertIntoFrozenVersion() {
        when(factorVersionMapper.selectById(501L)).thenReturn(frozenVersion());

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertFactorRecord(validRecordBo(501L)));

        assertEquals("Frozen or retired factor versions cannot modify core factor records", exception.getMessage());
        verify(factorRecordMapper, never()).insert(any(CvFactorRecord.class));
    }

    @Test
    void rejectsInsertIntoRetiredVersion() {
        when(factorVersionMapper.selectById(501L)).thenReturn(retiredVersion());

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.insertFactorRecord(validRecordBo(501L)));

        assertEquals("Frozen or retired factor versions cannot modify core factor records", exception.getMessage());
        verify(factorRecordMapper, never()).insert(any(CvFactorRecord.class));
    }

    @Test
    void rejectsUpdateWhenExistingRecordBelongsToFrozenVersion() {
        when(factorRecordMapper.selectById(9001L)).thenReturn(existingRecord(501L));
        when(factorVersionMapper.selectById(501L)).thenReturn(frozenVersion());

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.updateFactorRecord(validRecordBo(501L)));

        assertEquals("Frozen or retired factor versions cannot modify core factor records", exception.getMessage());
        verify(factorRecordMapper, never()).updateById(any(CvFactorRecord.class));
    }

    @Test
    void rejectsUpdateWhenExistingRecordBelongsToRetiredVersion() {
        when(factorRecordMapper.selectById(9001L)).thenReturn(existingRecord(501L));
        when(factorVersionMapper.selectById(501L)).thenReturn(retiredVersion());

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.updateFactorRecord(validRecordBo(501L)));

        assertEquals("Frozen or retired factor versions cannot modify core factor records", exception.getMessage());
        verify(factorRecordMapper, never()).updateById(any(CvFactorRecord.class));
    }

    @Test
    void rejectsDeleteWhenAnyRecordBelongsToFrozenVersion() {
        when(factorRecordMapper.selectList(any())).thenReturn(List.of(existingRecord(501L)));
        when(factorVersionMapper.selectById(501L)).thenReturn(frozenVersion());

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.deleteFactorRecordByIds(new Long[] {9001L}));

        assertEquals("Frozen or retired factor versions cannot modify core factor records", exception.getMessage());
        verify(factorRecordMapper, never()).deleteByIds(any());
    }

    @Test
    void rejectsDeleteWhenAnyRecordBelongsToRetiredVersion() {
        when(factorRecordMapper.selectList(any())).thenReturn(List.of(existingRecord(501L)));
        when(factorVersionMapper.selectById(501L)).thenReturn(retiredVersion());

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.deleteFactorRecordByIds(new Long[] {9001L}));

        assertEquals("Frozen or retired factor versions cannot modify core factor records", exception.getMessage());
        verify(factorRecordMapper, never()).deleteByIds(any());
    }

    private CvFactorRecordBo validRecordBo(Long versionId) {
        CvFactorRecordBo bo = new CvFactorRecordBo();
        bo.setId(9001L);
        bo.setVersionId(versionId);
        bo.setFactorCode("EF-ELEC");
        bo.setFactorName("Electricity");
        bo.setFactorCategory("energy");
        bo.setFactorValue(new BigDecimal("0.5700000000"));
        bo.setFactorUnit("kgCO2e/kWh");
        bo.setEnabledFlag(Boolean.TRUE);
        return bo;
    }

    private CvFactorRecord existingRecord(Long versionId) {
        CvFactorRecord record = new CvFactorRecord();
        record.setId(9001L);
        record.setVersionId(versionId);
        return record;
    }

    private CvFactorVersion draftVersion() {
        CvFactorVersion version = new CvFactorVersion();
        version.setId(501L);
        version.setPublishStatus("draft");
        version.setFrozenFlag(Boolean.FALSE);
        return version;
    }

    private CvFactorVersion frozenVersion() {
        CvFactorVersion version = new CvFactorVersion();
        version.setId(501L);
        version.setPublishStatus("frozen");
        version.setFrozenFlag(Boolean.TRUE);
        return version;
    }

    private CvFactorVersion retiredVersion() {
        CvFactorVersion version = new CvFactorVersion();
        version.setId(501L);
        version.setPublishStatus("retired");
        version.setFrozenFlag(Boolean.FALSE);
        return version;
    }
}
