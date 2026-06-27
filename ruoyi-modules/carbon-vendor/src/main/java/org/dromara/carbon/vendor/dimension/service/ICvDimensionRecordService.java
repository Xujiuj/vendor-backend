package org.dromara.carbon.vendor.dimension.service;

import org.dromara.carbon.vendor.dimension.domain.bo.CvDimensionRecordBo;
import org.dromara.carbon.vendor.dimension.domain.vo.CvDimensionRecordVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

public interface ICvDimensionRecordService {

    TableDataInfo<CvDimensionRecordVo> queryPageList(CvDimensionRecordBo bo, PageQuery pageQuery);

    CvDimensionRecordVo queryById(Long id);

    boolean insertByBo(CvDimensionRecordBo bo);

    boolean updateByBo(CvDimensionRecordBo bo);

    boolean deleteByIds(Long[] ids);
}
