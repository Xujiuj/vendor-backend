package org.dromara.carbon.vendor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("cv_factor_version")
public class CvFactorVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private String versionCode;
    private String versionName;
    private String publishStatus;
    private Boolean frozenFlag;
    private String publishedBy;
    private Date publishedTime;
    private Date createTime;
    private String remark;
}
