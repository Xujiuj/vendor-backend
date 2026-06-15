package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.domain.bo.CvFactorVersionBo;
import org.dromara.carbon.vendor.domain.enums.CvFactorVersionLifecycleState;
import org.dromara.carbon.vendor.domain.vo.CvFactorVersionVo;
import org.dromara.carbon.vendor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.service.ICvFactorVersionService;
import org.dromara.common.core.enums.FormatsType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

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
    public void releaseFactorVersion(Long id, String operatedBy) {
        CvFactorVersion version = requireFactorVersion(id);
        CvFactorVersionLifecycleState currentState = CvFactorVersionLifecycleState.fromVersion(version);
        if (currentState != CvFactorVersionLifecycleState.DRAFT) {
            throw new ServiceException("Only draft factor versions can be published");
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
            throw new ServiceException("Only published factor versions can be frozen");
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
            throw new ServiceException("Only published or frozen factor versions can be retired");
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
            throw new ServiceException("Only retired factor versions can be restored");
        }
        Date operationTime = DateUtils.getNowDate();
        version.setPublishStatus(CvFactorVersionLifecycleState.DRAFT.getStatus());
        version.setFrozenFlag(Boolean.FALSE);
        version.setRemark(appendAuditRemark(version.getRemark(), "restore", operatedBy, operationTime));
        baseMapper.updateById(version);
    }

    private LambdaQueryWrapper<CvFactorVersion> buildQueryWrapper(CvFactorVersionBo bo) {
        Map<String, Object> params = bo.getParams();
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
            throw new ServiceException("Factor version id cannot be null");
        }
        CvFactorVersion version = baseMapper.selectById(id);
        if (version == null) {
            throw new ServiceException("Factor version does not exist");
        }
        return version;
    }

    private String requireOperator(String operatedBy) {
        if (StringUtils.isBlank(operatedBy)) {
            throw new ServiceException("Factor version lifecycle operator cannot be blank");
        }
        return operatedBy.trim();
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
