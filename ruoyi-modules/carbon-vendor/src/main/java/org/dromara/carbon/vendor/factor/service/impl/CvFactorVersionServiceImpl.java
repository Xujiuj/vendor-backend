package org.dromara.carbon.vendor.factor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.factor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.factor.domain.bo.CvFactorVersionBo;
import org.dromara.carbon.vendor.factor.domain.enums.CvFactorVersionLifecycleState;
import org.dromara.carbon.vendor.factor.domain.vo.CvFactorVersionVo;
import org.dromara.carbon.vendor.factor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.factor.service.ICvFactorVersionService;
import org.dromara.common.core.enums.FormatsType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * Vendor factor version service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvFactorVersionServiceImpl implements ICvFactorVersionService {

    private final CvFactorVersionMapper baseMapper;

    @Override
    public TableDataInfo<CvFactorVersionVo> selectPageFactorVersionList(CvFactorVersionBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CvFactorVersion> lqw = buildQueryWrapper(bo);
        Page<CvFactorVersionVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public CvFactorVersionVo selectFactorVersionById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public Boolean insertFactorVersion(CvFactorVersionBo bo) {
        normalizeEditableFields(bo, true);
        ensureVersionCodeUnique(bo);
        CvFactorVersion add = toEntity(bo);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateFactorVersion(CvFactorVersionBo bo) {
        CvFactorVersion existing = requireFactorVersion(bo.getId());
        normalizeEditableFields(bo, false);
        ensureVersionCodeUnique(bo);
        CvFactorVersion update = toEntity(bo);
        update.setPublishStatus(existing.getPublishStatus());
        update.setFrozenFlag(existing.getFrozenFlag());
        update.setPublishedBy(existing.getPublishedBy());
        update.setPublishedTime(existing.getPublishedTime());
        update.setCreateTime(existing.getCreateTime());
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteFactorVersionByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return false;
        }
        for (Long id : ids) {
            CvFactorVersion version = requireFactorVersion(id);
            CvFactorVersionLifecycleState currentState = CvFactorVersionLifecycleState.fromVersion(version);
            if (currentState != CvFactorVersionLifecycleState.DRAFT
                && currentState != CvFactorVersionLifecycleState.RETIRED) {
                throw new ServiceException("仅草稿或已退役的因子版本允许删除");
            }
        }
        return baseMapper.deleteByIds(Arrays.asList(ids)) > 0;
    }

    @Override
    public void releaseFactorVersion(Long id, String operatedBy) {
        CvFactorVersion version = requireFactorVersion(id);
        CvFactorVersionLifecycleState currentState = CvFactorVersionLifecycleState.fromVersion(version);
        if (currentState != CvFactorVersionLifecycleState.DRAFT) {
            throw new ServiceException("仅草稿状态的因子版本允许发布");
        }
        Date operationTime = DateUtils.getNowDate();
        version.setPublishStatus(CvFactorVersionLifecycleState.PUBLISHED.getStatus());
        version.setFrozenFlag(Boolean.FALSE);
        version.setPublishedBy(requireOperator(operatedBy));
        version.setPublishedTime(operationTime);
        version.setRemark(appendAuditRemark(version.getRemark(), "publish", operatedBy, operationTime));
        baseMapper.updateById(version);
    }

    @Override
    public void freezeFactorVersion(Long id, String operatedBy) {
        CvFactorVersion version = requireFactorVersion(id);
        CvFactorVersionLifecycleState currentState = CvFactorVersionLifecycleState.fromVersion(version);
        if (currentState != CvFactorVersionLifecycleState.PUBLISHED) {
            throw new ServiceException("仅已发布的因子版本允许冻结");
        }
        Date operationTime = DateUtils.getNowDate();
        version.setPublishStatus(CvFactorVersionLifecycleState.FROZEN.getStatus());
        version.setFrozenFlag(Boolean.TRUE);
        version.setRemark(appendAuditRemark(version.getRemark(), "freeze", operatedBy, operationTime));
        baseMapper.updateById(version);
    }

    @Override
    public void retireFactorVersion(Long id, String operatedBy) {
        CvFactorVersion version = requireFactorVersion(id);
        CvFactorVersionLifecycleState currentState = CvFactorVersionLifecycleState.fromVersion(version);
        if (currentState != CvFactorVersionLifecycleState.PUBLISHED
            && currentState != CvFactorVersionLifecycleState.FROZEN) {
            throw new ServiceException("仅已发布或已冻结的因子版本允许退役");
        }
        Date operationTime = DateUtils.getNowDate();
        version.setPublishStatus(CvFactorVersionLifecycleState.RETIRED.getStatus());
        version.setFrozenFlag(Boolean.FALSE);
        version.setRemark(appendAuditRemark(version.getRemark(), "retire", operatedBy, operationTime));
        baseMapper.updateById(version);
    }

    @Override
    public void restoreFactorVersion(Long id, String operatedBy) {
        CvFactorVersion version = requireFactorVersion(id);
        String normalizedStatus = CvFactorVersionLifecycleState.normalizeStatus(version.getPublishStatus());
        boolean frozen = Boolean.TRUE.equals(version.getFrozenFlag());
        if (!CvFactorVersionLifecycleState.RETIRED.getStatus().equals(normalizedStatus) || frozen) {
            throw new ServiceException("仅已退役且未冻结的因子版本允许恢复");
        }
        Date operationTime = DateUtils.getNowDate();
        version.setPublishStatus(CvFactorVersionLifecycleState.DRAFT.getStatus());
        version.setFrozenFlag(Boolean.FALSE);
        version.setRemark(appendAuditRemark(version.getRemark(), "restore", operatedBy, operationTime));
        baseMapper.updateById(version);
    }

    private LambdaQueryWrapper<CvFactorVersion> buildQueryWrapper(CvFactorVersionBo bo) {
        Map<String, Object> params = bo.getParams() == null ? Map.of() : bo.getParams();
        LambdaQueryWrapper<CvFactorVersion> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvFactorVersion::getId, bo.getId());
        lqw.like(StringUtils.isNotBlank(bo.getVersionCode()), CvFactorVersion::getVersionCode, bo.getVersionCode());
        lqw.like(StringUtils.isNotBlank(bo.getVersionName()), CvFactorVersion::getVersionName, bo.getVersionName());
        lqw.eq(StringUtils.isNotBlank(bo.getPublishStatus()), CvFactorVersion::getPublishStatus, bo.getPublishStatus());
        lqw.eq(bo.getFrozenFlag() != null, CvFactorVersion::getFrozenFlag, bo.getFrozenFlag());
        lqw.like(StringUtils.isNotBlank(bo.getPublishedBy()), CvFactorVersion::getPublishedBy, bo.getPublishedBy());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            CvFactorVersion::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(CvFactorVersion::getCreateTime);
        lqw.orderByAsc(CvFactorVersion::getId);
        return lqw;
    }

    private CvFactorVersion requireFactorVersion(Long id) {
        if (id == null) {
            throw new ServiceException("因子版本ID不能为空");
        }
        CvFactorVersion version = baseMapper.selectById(id);
        if (version == null) {
            throw new ServiceException("因子版本不存在");
        }
        return version;
    }

    private String requireOperator(String operatedBy) {
        if (StringUtils.isBlank(operatedBy)) {
            throw new ServiceException("因子版本操作人不能为空");
        }
        return operatedBy.trim();
    }

    private void normalizeEditableFields(CvFactorVersionBo bo, boolean create) {
        bo.setVersionCode(StringUtils.trim(bo.getVersionCode()));
        bo.setVersionName(StringUtils.trim(bo.getVersionName()));
        if (create) {
            bo.setPublishStatus(CvFactorVersionLifecycleState.DRAFT.getStatus());
            bo.setFrozenFlag(Boolean.FALSE);
        }
    }

    private CvFactorVersion toEntity(CvFactorVersionBo bo) {
        CvFactorVersion version = new CvFactorVersion();
        version.setId(bo.getId());
        version.setVersionCode(bo.getVersionCode());
        version.setVersionName(bo.getVersionName());
        version.setPublishStatus(bo.getPublishStatus());
        version.setFrozenFlag(bo.getFrozenFlag());
        version.setPublishedBy(bo.getPublishedBy());
        version.setRemark(bo.getRemark());
        return version;
    }

    private void ensureVersionCodeUnique(CvFactorVersionBo bo) {
        Long count = baseMapper.selectCount(Wrappers.lambdaQuery(CvFactorVersion.class)
            .eq(CvFactorVersion::getVersionCode, bo.getVersionCode())
            .ne(bo.getId() != null, CvFactorVersion::getId, bo.getId()));
        if (count != null && count > 0) {
            throw new ServiceException("版本编码不能重复");
        }
    }

    private String appendAuditRemark(String currentRemark, String action, String operatedBy, Date operationTime) {
        String auditEntry = String.format(
            "[%s] factor-version-%s by %s",
            DateUtils.parseDateToStr(FormatsType.YYYY_MM_DD_HH_MM_SS, operationTime),
            action,
            requireOperator(operatedBy)
        );
        return StringUtils.isBlank(currentRemark) ? auditEntry : currentRemark + System.lineSeparator() + auditEntry;
    }
}
