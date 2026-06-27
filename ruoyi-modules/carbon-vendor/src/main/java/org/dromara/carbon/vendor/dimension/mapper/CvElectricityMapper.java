package org.dromara.carbon.vendor.dimension.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.carbon.vendor.dimension.domain.CvElectricityFactor;
import org.dromara.carbon.vendor.dimension.domain.vo.CvElectricityFactorVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 电力排放因子 Mapper
 *
 * @author carbon
 */
@Mapper
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvElectricityMapper extends BaseMapperPlus<CvElectricityFactor, CvElectricityFactorVo> {
}
