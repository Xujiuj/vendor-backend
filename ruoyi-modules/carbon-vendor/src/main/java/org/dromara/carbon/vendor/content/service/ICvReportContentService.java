package org.dromara.carbon.vendor.content.service;

import org.dromara.carbon.vendor.content.domain.bo.CvReportContentBo;
import org.dromara.carbon.vendor.content.domain.vo.CvReportContentVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * Vendor report content catalog service.
 */
public interface ICvReportContentService {

    TableDataInfo<CvReportContentVo> selectPageReportContentList(CvReportContentBo bo, PageQuery pageQuery);

    CvReportContentVo selectReportContentById(Long id);

    Boolean insertReportContent(CvReportContentBo bo);

    Boolean updateReportContent(CvReportContentBo bo);

    Boolean deleteReportContentByIds(Long[] ids);
}
