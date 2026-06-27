package org.dromara.carbon.vendor.tablefield.service;

import org.dromara.carbon.vendor.tablefield.domain.bo.CvVendorTableFieldBo;
import org.dromara.carbon.vendor.tablefield.domain.vo.CvVendorTableFieldVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Collection;
import java.util.List;

public interface ICvVendorTableFieldService {

    TableDataInfo<CvVendorTableFieldVo> queryPageList(CvVendorTableFieldBo bo, PageQuery pageQuery);

    List<CvVendorTableFieldVo> queryList(CvVendorTableFieldBo bo);

    CvVendorTableFieldVo queryById(Long id);

    boolean insertByBo(CvVendorTableFieldBo bo);

    boolean updateByBo(CvVendorTableFieldBo bo);

    boolean deleteByIds(Collection<Long> ids);
}
