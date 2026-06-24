package org.dromara.carbon.vendor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.dromara.carbon.vendor.domain.CvDimensionRecord;
import org.dromara.carbon.vendor.domain.vo.CvDimensionRecordVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvDimensionRecordMapper extends BaseMapperPlus<CvDimensionRecord, CvDimensionRecordVo> {
}
