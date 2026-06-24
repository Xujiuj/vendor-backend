package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.CvVendorTableField;
import org.dromara.carbon.vendor.domain.bo.CvVendorTableFieldBo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Collection;
import java.util.List;

public interface ICvVendorTableFieldService {

    TableDataInfo<CvVendorTableField> queryPageList(CvVendorTableFieldBo bo, PageQuery pageQuery);

    List<CvVendorTableField> queryList(CvVendorTableFieldBo bo);

    CvVendorTableField queryById(Long id);

    boolean insertByBo(CvVendorTableFieldBo bo);

    boolean updateByBo(CvVendorTableFieldBo bo);

    boolean deleteByIds(Collection<Long> ids);
}
