package org.dromara.carbon.vendor.mapper.dimension;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.carbon.vendor.domain.dimension.CvAdminDivision;
import org.dromara.carbon.vendor.domain.vo.dimension.CvAdminDivisionVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 行政区划 Mapper
 *
 * @author carbon
 */
@Mapper
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvAdminDivisionMapper extends BaseMapperPlus<CvAdminDivision, CvAdminDivisionVo> {
}
