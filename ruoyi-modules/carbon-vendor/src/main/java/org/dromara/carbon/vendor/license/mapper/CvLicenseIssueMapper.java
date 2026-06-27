package org.dromara.carbon.vendor.license.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.license.domain.vo.CvLicenseIssueVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvLicenseIssueMapper extends BaseMapperPlus<CvLicenseIssue, CvLicenseIssueVo> {
}
