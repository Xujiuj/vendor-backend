package org.dromara.carbon.vendor.mapper.dimension;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.carbon.vendor.domain.dimension.CvElectricityFactorVersion;
import org.dromara.carbon.vendor.domain.vo.dimension.CvElectricityFactorVersionVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 电力因子版本 Mapper
 *
 * @author carbon
 */
@Mapper
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvElectricityFactorVersionMapper extends BaseMapperPlus<CvElectricityFactorVersion, CvElectricityFactorVersionVo> {
}
