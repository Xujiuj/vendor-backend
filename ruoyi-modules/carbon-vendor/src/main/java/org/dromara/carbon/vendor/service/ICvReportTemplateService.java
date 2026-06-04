package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.bo.CvReportTemplateBo;
import org.dromara.carbon.vendor.domain.vo.CvReportTemplateVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * Vendor report template service.
 */
public interface ICvReportTemplateService {

    TableDataInfo<CvReportTemplateVo> selectPageReportTemplateList(CvReportTemplateBo bo, PageQuery pageQuery);

    CvReportTemplateVo selectReportTemplateById(Long id);

    int insertReportTemplate(CvReportTemplateBo bo);

    int updateReportTemplate(CvReportTemplateBo bo);

    int deleteReportTemplateByIds(Long[] ids);
}
