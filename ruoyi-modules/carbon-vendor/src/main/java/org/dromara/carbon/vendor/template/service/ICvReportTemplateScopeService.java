package org.dromara.carbon.vendor.template.service;

import org.dromara.carbon.vendor.template.domain.bo.CvReportTemplateScopeBo;
import org.dromara.carbon.vendor.template.domain.vo.CvReportTemplateScopeVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * Vendor report template scope service.
 */
public interface ICvReportTemplateScopeService {

    TableDataInfo<CvReportTemplateScopeVo> selectPageReportTemplateScopeList(CvReportTemplateScopeBo bo, PageQuery pageQuery);

    CvReportTemplateScopeVo selectReportTemplateScopeById(Long id);

    int insertReportTemplateScope(CvReportTemplateScopeBo bo);

    int updateReportTemplateScope(CvReportTemplateScopeBo bo);

    int deleteReportTemplateScopeByIds(Long[] ids);
}
