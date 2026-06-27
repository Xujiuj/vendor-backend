package org.dromara.carbon.vendor.dimension.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.carbon.vendor.dimension.domain.CvBaseYear;
import org.dromara.carbon.vendor.dimension.domain.vo.CvBaseYearVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 基准年 Mapper
 *
 * @author carbon
 */
@Mapper
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvBaseYearMapper extends BaseMapperPlus<CvBaseYear, CvBaseYearVo> {
}
