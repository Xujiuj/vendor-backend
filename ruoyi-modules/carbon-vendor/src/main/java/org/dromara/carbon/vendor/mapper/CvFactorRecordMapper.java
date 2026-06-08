package org.dromara.carbon.vendor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;

import org.dromara.carbon.vendor.domain.CvFactorRecord;
import org.dromara.carbon.vendor.domain.vo.CvFactorRecordVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Vendor factor record mapper.
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvFactorRecordMapper extends BaseMapperPlus<CvFactorRecord, CvFactorRecordVo> {
}
