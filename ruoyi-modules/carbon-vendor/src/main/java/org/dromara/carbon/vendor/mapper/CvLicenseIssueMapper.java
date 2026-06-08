package org.dromara.carbon.vendor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;

import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.vo.CvLicenseIssueVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Vendor license issue mapper.
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvLicenseIssueMapper extends BaseMapperPlus<CvLicenseIssue, CvLicenseIssueVo> {
}
