package org.dromara.carbon.vendor.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.domain.CvFactorRecord;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Vendor factor record view object.
 */
@Data
@AutoMapper(target = CvFactorRecord.class)
public class CvFactorRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
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
     * Vendor-defined custom field values as JSON object.
     */
    private String customFields;

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
