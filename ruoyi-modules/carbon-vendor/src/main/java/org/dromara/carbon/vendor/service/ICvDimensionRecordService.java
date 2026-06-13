package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.bo.CvDimensionRecordBo;
import org.dromara.carbon.vendor.domain.vo.CvDimensionRecordVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.List;

/**
 * Vendor dimension record service.
 */
public interface ICvDimensionRecordService {

    TableDataInfo<CvDimensionRecordVo> queryPageList(CvDimensionRecordBo bo, PageQuery pageQuery);

    List<CvDimensionRecordVo> queryList(CvDimensionRecordBo bo);

    CvDimensionRecordVo queryById(Long id);

    Boolean insertByBo(CvDimensionRecordBo bo);

    Boolean updateByBo(CvDimensionRecordBo bo);

    Boolean deleteByIds(Long[] ids);
}
