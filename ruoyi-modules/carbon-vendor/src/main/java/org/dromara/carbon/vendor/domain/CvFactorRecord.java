package org.dromara.carbon.vendor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Vendor factor library record cv_factor_record.
 */
@Data
@TableName("cv_factor_record")
public class CvFactorRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(value = "id")
    private Long id;

    /**
     * Factor version identifier.
     */
    private Long versionId;

    /**
     * Customer sample factor table code.
     */
    private String factorTableCode;

    /**
     * Stable factor code.
     */
    private String factorCode;

    /**
     * Factor display name.
     */
    private String factorName;

    /**
     * Factor category.
     */
    private String factorCategory;

    /**
     * Factor value.
     */
    private BigDecimal factorValue;

    /**
     * Factor unit.
     */
    private String factorUnit;

    private String factorKey;

    private String emissionSourceName;

    private String emissionSourceNameEn;

    private String fuelMaterialCategory;

    private String sourceUnit;

    private BigDecimal co2;

    private BigDecimal ch4;

    private BigDecimal n2o;

    private BigDecimal hfcs;

    private BigDecimal pfcs;

    private BigDecimal sf6;

    private BigDecimal nf3;

    private String applicableScope;

    private String factorSource;

    private BigDecimal gwpCh4;

    private BigDecimal gwpN2o;

    private BigDecimal gwpHfcs;

    private BigDecimal gwpPfcs;

    private BigDecimal gwpSf6;

    private BigDecimal gwpNf3;

    private BigDecimal factorGwp;

    private String versionProvinceCode;

    private String factorVersion;

    private String divisionCode;

    private String divisionName;

    private String regionName;

    private BigDecimal provinceFactor;

    private BigDecimal regionFactor;

    private BigDecimal nationalFactor;

    private BigDecimal nonFossilExcludedFactor;

    private BigDecimal nationalFossilPowerFactor;

    private Integer rowNo;

    private String fuelLevel1;

    private String fuelLevel2;

    private String fuelLevel3;

    private String fuelLevel4;

    private BigDecimal lowerHeatValue;

    private BigDecimal lowerHeatValueCv;

    private BigDecimal co2Factor;

    private BigDecimal co2FactorCv;

    private BigDecimal gwpValue;

    private BigDecimal convertedFactor;

    /**
     * Source reference.
     */
    private String sourceRef;

    /**
     * Whether this factor is enabled.
     */
    private Boolean enabledFlag;

    /**
     * Creation time.
     */
    private Date createTime;

    private Date updateTime;

    private String remark;
}
