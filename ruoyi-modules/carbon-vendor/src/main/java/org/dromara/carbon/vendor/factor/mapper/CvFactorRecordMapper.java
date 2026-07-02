package org.dromara.carbon.vendor.factor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.carbon.vendor.factor.domain.CvFactorRecord;

/**
 * Vendor versioned factor record mapper.
 */
@Mapper
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvFactorRecordMapper extends BaseMapper<CvFactorRecord> {
}
