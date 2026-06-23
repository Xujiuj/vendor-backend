package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.bo.CvVendorTableFieldBo;
import org.dromara.carbon.vendor.domain.vo.CvVendorTableFieldVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.List;

/**
 * Vendor table field definition service.
 */
public interface ICvVendorTableFieldService {

    TableDataInfo<CvVendorTableFieldVo> queryPageList(CvVendorTableFieldBo bo, PageQuery pageQuery);

    List<CvVendorTableFieldVo> queryList(CvVendorTableFieldBo bo);

    CvVendorTableFieldVo queryById(Long id);

    Boolean insertByBo(CvVendorTableFieldBo bo);

    Boolean updateByBo(CvVendorTableFieldBo bo);

    Boolean deleteByIds(Long[] ids);
}
