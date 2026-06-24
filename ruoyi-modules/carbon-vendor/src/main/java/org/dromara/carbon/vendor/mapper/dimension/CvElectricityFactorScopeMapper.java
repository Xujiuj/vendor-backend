package org.dromara.carbon.vendor.mapper.dimension;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.carbon.vendor.domain.dimension.CvElectricityFactorScope;
import org.dromara.carbon.vendor.domain.vo.dimension.CvElectricityFactorScopeVo;
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
