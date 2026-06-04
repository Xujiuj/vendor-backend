package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.bo.CvFactorRecordBo;
import org.dromara.carbon.vendor.domain.vo.CvFactorRecordVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * Vendor factor record service.
 */
public interface ICvFactorRecordService {

    TableDataInfo<CvFactorRecordVo> selectPageFactorRecordList(CvFactorRecordBo bo, PageQuery pageQuery);

    CvFactorRecordVo selectFactorRecordById(Long id);

    int insertFactorRecord(CvFactorRecordBo bo);

    int updateFactorRecord(CvFactorRecordBo bo);

    int deleteFactorRecordByIds(Long[] ids);
}
