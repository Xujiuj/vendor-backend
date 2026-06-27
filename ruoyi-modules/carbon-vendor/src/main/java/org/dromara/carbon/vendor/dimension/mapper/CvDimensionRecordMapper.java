package org.dromara.carbon.vendor.dimension.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.dromara.carbon.vendor.dimension.domain.CvDimensionRecord;
import org.dromara.carbon.vendor.dimension.domain.vo.CvDimensionRecordVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvDimensionRecordMapper extends BaseMapperPlus<CvDimensionRecord, CvDimensionRecordVo> {
}
