package org.dromara.carbon.vendor.openapi.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor announcement exposed to enterprise home page.
 */
@Data
public class CvOpenAnnouncementVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long noticeId;

    private String noticeTitle;

    private String noticeType;

    private String noticeContent;

    private String status;

    private String remark;

    private Date createTime;
}
