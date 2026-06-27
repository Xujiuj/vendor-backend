package org.dromara.carbon.vendor.dimension.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.carbon.vendor.dimension.domain.CvEmissionSourceCategory;
import org.dromara.carbon.vendor.dimension.domain.vo.CvEmissionSourceCategoryVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 排放源分类 Mapper
 *
 * @author carbon
 */
@Mapper
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvEmissionSourceCategoryMapper extends BaseMapperPlus<CvEmissionSourceCategory, CvEmissionSourceCategoryVo> {
}
