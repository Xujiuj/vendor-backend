package org.dromara.carbon.vendor.dimension.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.carbon.vendor.dimension.domain.CvElectricityFactorScope;
import org.dromara.carbon.vendor.dimension.domain.vo.CvElectricityFactorScopeVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 电力因子口径维度 Mapper
 *
 * @author carbon
 */
@Mapper
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvElectricityFactorScopeMapper extends BaseMapperPlus<CvElectricityFactorScope, CvElectricityFactorScopeVo> {
}
