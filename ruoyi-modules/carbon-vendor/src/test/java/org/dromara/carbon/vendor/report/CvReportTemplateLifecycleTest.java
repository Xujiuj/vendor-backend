package org.dromara.carbon.vendor.report;

import org.dromara.carbon.vendor.domain.CvReportTemplate;
import org.dromara.carbon.vendor.domain.bo.CvReportTemplateBo;
import org.dromara.carbon.vendor.mapper.CvReportTemplateMapper;
import org.dromara.carbon.vendor.service.impl.CvReportTemplateServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvReportTemplateLifecycleTest {

    private CvReportTemplateMapper reportTemplateMapper;
    private CvReportTemplateServiceImpl service;

    @BeforeEach
    void setUp() {
        reportTemplateMapper = mock(CvReportTemplateMapper.class);
        service = new CvReportTemplateServiceImpl(reportTemplateMapper) {
            @Override
            protected CvReportTemplate toEntity(CvReportTemplateBo bo) {
                CvReportTemplate template = new CvReportTemplate();
                template.setId(bo.getId());
                template.setTemplateCode(bo.getTemplateCode());
                template.setTemplateName(bo.getTemplateName());
                template.setTemplateVersion(bo.getTemplateVersion());
                template.setFileName(bo.getFileName());
                template.setFileUri(bo.getFileUri());
                template.setPublishStatus(bo.getPublishStatus());
                template.setPublishedBy(bo.getPublishedBy());
                template.setPublishedTime(bo.getPublishedTime());
                template.setRemark(bo.getRemark());
                return template;
            }
        };
    }

    @Test
    void publishesDraftTemplateAndPersistsAuditMetadata() {
        when(reportTemplateMapper.selectById(301L)).thenReturn(draftTemplate());

        service.publishReportTemplate(301L, "vendor-admin");

        ArgumentCaptor<CvReportTemplate> updateCaptor = ArgumentCaptor.forClass(CvReportTemplate.class);
        verify(reportTemplateMapper).updateById(updateCaptor.capture());
        CvReportTemplate updated = updateCaptor.getValue();
        assertEquals("published", updated.getPublishStatus());
        assertEquals("vendor-admin", updated.getPublishedBy());
        assertNotNull(updated.getPublishedTime());
        assertTrue(updated.getRemark().contains("report-template-publish"));
        assertTrue(updated.getRemark().contains("vendor-admin"));
    }

    @Test
    void rejectsPublishingAnAlreadyPublishedTemplate() {
        when(reportTemplateMapper.selectById(301L)).thenReturn(publishedTemplate());

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.publishReportTemplate(301L, "vendor-admin"));

        assertEquals("Only draft or disabled report templates can be published", exception.getMessage());
    }

    @Test
    void disablesPublishedTemplateAndAppendsAuditRemark() {
        when(reportTemplateMapper.selectById(301L)).thenReturn(publishedTemplate());

        service.disableReportTemplate(301L, "ops-user");

        ArgumentCaptor<CvReportTemplate> updateCaptor = ArgumentCaptor.forClass(CvReportTemplate.class);
        verify(reportTemplateMapper).updateById(updateCaptor.capture());
        CvReportTemplate updated = updateCaptor.getValue();
        assertEquals("disabled", updated.getPublishStatus());
        assertEquals("release-user", updated.getPublishedBy());
        assertNotNull(updated.getPublishedTime());
        assertTrue(updated.getRemark().contains("existing remark"));
        assertTrue(updated.getRemark().contains("report-template-disable"));
        assertTrue(updated.getRemark().contains("ops-user"));
    }

    @Test
    void rejectsDisablingDraftTemplate() {
        when(reportTemplateMapper.selectById(301L)).thenReturn(draftTemplate());

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.disableReportTemplate(301L, "ops-user"));

        assertEquals("Only published report templates can be disabled", exception.getMessage());
    }

    @Test
    void genericUpdatePreservesLifecycleMetadata() {
        when(reportTemplateMapper.selectById(301L)).thenReturn(publishedTemplate());
        CvReportTemplateBo bo = templateBo();
        bo.setPublishStatus("disabled");
        bo.setPublishedBy("payload-user");
        bo.setPublishedTime(new Date(0));

        service.updateReportTemplate(bo);

        ArgumentCaptor<CvReportTemplate> updateCaptor = ArgumentCaptor.forClass(CvReportTemplate.class);
        verify(reportTemplateMapper).updateById(updateCaptor.capture());
        CvReportTemplate updated = updateCaptor.getValue();
        assertEquals("published", updated.getPublishStatus());
        assertEquals("release-user", updated.getPublishedBy());
        assertNotNull(updated.getPublishedTime());
    }

    private CvReportTemplate draftTemplate() {
        CvReportTemplate template = baseTemplate();
        template.setPublishStatus("draft");
        template.setPublishedBy(null);
        template.setPublishedTime(null);
        template.setRemark(null);
        return template;
    }

    private CvReportTemplate publishedTemplate() {
        CvReportTemplate template = baseTemplate();
        template.setPublishStatus("published");
        template.setPublishedBy("release-user");
        template.setPublishedTime(new Date());
        template.setRemark("existing remark");
        return template;
    }

    private CvReportTemplate baseTemplate() {
        CvReportTemplate template = new CvReportTemplate();
        template.setId(301L);
        template.setTemplateCode("PBI-CARBON-MAIN");
        template.setTemplateName("Carbon Main");
        template.setTemplateVersion("2026.1");
        template.setFileName("main.pbix");
        template.setFileUri("oss://vendor/templates/main.pbix");
        return template;
    }

    private CvReportTemplateBo templateBo() {
        CvReportTemplateBo bo = new CvReportTemplateBo();
        bo.setId(301L);
        bo.setTemplateCode("PBI-CARBON-MAIN");
        bo.setTemplateName("Carbon Main Updated");
        bo.setTemplateVersion("2026.1");
        bo.setFileName("main.pbix");
        bo.setFileUri("oss://vendor/templates/main.pbix");
        return bo;
    }
}
