package org.dromara.carbon.vendor.domain.open;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * License-scoped table field definition exposed to enterprise backend.
 */
@Data
public class CvOpenTableFieldDefinitionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String tableGroup;

    private String tableCode;

    private String fieldKey;

    private String fieldLabel;

    private String fieldType;

    private Integer fieldPrecision;

    private Integer fieldWidth;

    private Boolean requiredFlag;

    private Integer sortOrder;
}
