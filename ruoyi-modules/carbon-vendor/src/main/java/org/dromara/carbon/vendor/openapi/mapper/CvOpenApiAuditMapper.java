package org.dromara.carbon.vendor.openapi.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.dromara.carbon.vendor.openapi.domain.CvOpenApiAudit;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Vendor open API audit mapper.
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvOpenApiAuditMapper extends BaseMapperPlus<CvOpenApiAudit, CvOpenApiAudit> {
}
