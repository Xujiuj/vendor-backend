package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.domain.bo.SysNoticeBo;
import org.dromara.system.domain.vo.SysNoticeVo;
import org.dromara.system.service.ISysNoticeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vendor announcement management API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/announcement")
public class CvAnnouncementController extends BaseController {

    private final ISysNoticeService noticeService;

    /**
     * List vendor announcements exposed to enterprise workbenches.
     */
    @SaCheckPermission("vendor:announcement:list")
    @GetMapping("/list")
    public TableDataInfo<SysNoticeVo> list(SysNoticeBo notice, PageQuery pageQuery) {
        return noticeService.selectPageNoticeList(notice, pageQuery);
    }

    /**
     * Get vendor announcement details.
     *
     * @param noticeId notice id
     */
    @SaCheckPermission("vendor:announcement:query")
    @GetMapping("/{noticeId}")
    public R<SysNoticeVo> getInfo(@PathVariable Long noticeId) {
        return R.ok(noticeService.selectNoticeById(noticeId));
    }

    /**
     * Add vendor announcement.
     */
    @Log(title = "公告管理", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @SaCheckPermission("vendor:announcement:add")
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysNoticeBo notice) {
        return toAjax(noticeService.insertNotice(notice));
    }

    /**
     * Edit vendor announcement.
     */
    @Log(title = "公告管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @SaCheckPermission("vendor:announcement:edit")
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysNoticeBo notice) {
        return toAjax(noticeService.updateNotice(notice));
    }

    /**
     * Delete vendor announcements.
     *
     * @param noticeIds notice ids
     */
    @Log(title = "公告管理", businessType = BusinessType.DELETE)
    @SaCheckPermission("vendor:announcement:remove")
    @DeleteMapping("/{noticeIds}")
    public R<Void> remove(@PathVariable Long[] noticeIds) {
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }
}
