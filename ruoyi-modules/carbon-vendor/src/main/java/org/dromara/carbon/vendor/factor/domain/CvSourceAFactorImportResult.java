package org.dromara.carbon.vendor.factor.domain;

import lombok.Data;

/**
 * Source(A) factor import result.
 *
 * @author Claude
 */
@Data
public class CvSourceAFactorImportResult {

    /** 201EF 排放因子维度表导入行数 */
    private int imported201EfCount;

    /** 202EF 电力因子维度表导入行数 */
    private int imported202EfCount;

    /** 203EF 电力因子版本对应导入行数 */
    private int imported203EfCount;

    /** 204EF 燃料因子计算导入行数 */
    private int imported204EfCount;

    /** 205EF 电力因子口径维度导入行数 */
    private int imported205EfCount;

    /** 206 温室气体维度导入行数 */
    private int imported206GasCount;

    /** 是否导入成功 */
    private boolean imported;

    /** 结果消息 */
    private String message;
}
