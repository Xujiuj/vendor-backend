package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvSigningKey;
import org.dromara.carbon.vendor.domain.bo.CvSigningKeyBo;
import org.dromara.carbon.vendor.domain.vo.CvSigningKeyVo;
import org.dromara.carbon.vendor.mapper.CvSigningKeyMapper;
import org.dromara.carbon.vendor.service.ICvSigningKeyService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

/**
 * Vendor signing key service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvSigningKeyServiceImpl implements ICvSigningKeyService {

    private final CvSigningKeyMapper baseMapper;

    @Override
    public TableDataInfo<CvSigningKeyVo> selectPageSigningKeyList(CvSigningKeyBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CvSigningKey> lqw = buildQueryWrapper(bo);
        Page<CvSigningKeyVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public CvSigningKeyVo selectSigningKeyById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public int insertSigningKey(CvSigningKeyBo bo) {
        CvSigningKey signingKey = MapstructUtils.convert(bo, CvSigningKey.class);
        return baseMapper.insert(signingKey);
    }

    @Override
    public int updateSigningKey(CvSigningKeyBo bo) {
        CvSigningKey signingKey = MapstructUtils.convert(bo, CvSigningKey.class);
        return baseMapper.updateById(signingKey);
    }

    @Override
    public int deleteSigningKeyByIds(Long[] ids) {
        return baseMapper.deleteByIds(Arrays.asList(ids));
    }

    private LambdaQueryWrapper<CvSigningKey> buildQueryWrapper(CvSigningKeyBo bo) {
        Map<String, Object> params = bo.getParams() == null ? Map.of() : bo.getParams();
        LambdaQueryWrapper<CvSigningKey> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvSigningKey::getId, bo.getId());
        lqw.like(StringUtils.isNotBlank(bo.getKeyId()), CvSigningKey::getKeyId, bo.getKeyId());
        lqw.eq(StringUtils.isNotBlank(bo.getAlgorithm()), CvSigningKey::getAlgorithm, bo.getAlgorithm());
        lqw.eq(StringUtils.isNotBlank(bo.getKeyStatus()), CvSigningKey::getKeyStatus, bo.getKeyStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            CvSigningKey::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(CvSigningKey::getCreateTime);
        lqw.orderByAsc(CvSigningKey::getId);
        return lqw;
    }
}
