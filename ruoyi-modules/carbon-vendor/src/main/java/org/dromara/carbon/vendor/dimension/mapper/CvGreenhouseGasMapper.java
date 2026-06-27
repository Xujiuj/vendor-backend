package org.dromara.carbon.vendor.dimension.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.carbon.vendor.dimension.domain.CvGreenhouseGas;
import org.dromara.carbon.vendor.dimension.domain.vo.CvGreenhouseGasVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 温室气体维度 Mapper
 *
 * @author carbon
 */
@Mapper
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvGreenhouseGasMapper extends BaseMapperPlus<CvGreenhouseGas, CvGreenhouseGasVo> {
}
