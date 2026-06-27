package org.dromara.carbon.vendor.domain.open;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Vendor dimension record exposed to enterprise backends.
 * <p>Contains both generic fields (for backward compatibility) and
 * strong-typed fields from the new dimension tables.</p>
 */
@Data
public class CvOpenDimensionRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String dimensionCode;
    private String recordCode;
    private String recordName;
    private String parentCode;
    private Integer sortOrder;
    private String status;
    private Date createTime;
    private Date updateTime;
    private String remark;

    // Generic fields (backward compatibility for report-template-download etc.)
    private String field01;
    private String field02;
    private String field03;
    private String field04;
    private String field05;
    private String field06;
    private String field07;
    private String field08;
    private String field09;
    private String field10;
    private String field11;
    private String field12;
    private String field13;
    private String field14;
    private String field15;
    private String field16;
    private String field17;
    private String field18;
    private String field19;
    private String field20;
    private String field21;
    private String field22;

    // Strong-typed fields (populated from new dimension tables)
    /** 101 行政区划: level_type */
    private String levelType;
    /** 103 排放源分类: category_name_en */
    private String categoryNameEn;
    /** 103 排放源分类: ghg_scope */
    private String ghgScope;
    /** 103 排放源分类: ghg_scope_category */
    private String ghgScopeCategory;
    /** 103 排放源分类: iso_category */
    private String isoCategory;
    /** 103 排放源分类: iso_category_en */
    private String isoCategoryEn;
    /** 103 排放源分类: iso_category_description */
    private String isoCategoryDescription;
    /** 103 排放源分类: gb_scope_category */
    private String gbScopeCategory;
    /** 103 排放源分类: gb_subcategory */
    private String gbSubcategory;
    /** 103 排放源分类: business_key */
    private String businessKey;
    /** 103 排放源分类: effective_date */
    private java.time.LocalDate effectiveDate;
    /** 103 排放源分类: expire_date */
    private java.time.LocalDate expireDate;
    /** 103 排放源分类: current_flag */
    private String currentFlag;
    /** 103 排放源分类: version_no */
    private String versionNo;
    /** 103 排放源分类: standard_category */
    private String standardCategory;
    /** 106 基准年: base_year_key */
    private String baseYearKey;
    /** 106 基准年: description */
    private String description;
    /** 106 基准年: base_year */
    private Integer baseYear;
    /** 106 基准年: is_current */
    private Integer isCurrent;
    /** 202 电力因子: factor_version */
    private String factorVersion;
    /** 202 电力因子: division_code */
    private String divisionCode;
    /** 202 电力因子: division_name */
    private String divisionName;
    /** 202 电力因子: region_name */
    private String regionName;
    /** 202 电力因子: province_factor */
    private BigDecimal provinceFactor;
    /** 202 电力因子: region_factor */
    private BigDecimal regionFactor;
    /** 202 电力因子: national_factor */
    private BigDecimal nationalFactor;
    /** 202 电力因子: non_fossil_excluded_factor */
    private BigDecimal nonFossilExcludedFactor;
    /** 202 电力因子: national_fossil_power_factor */
    private BigDecimal nationalFossilPowerFactor;
    /** 203 电力因子版本: effective_year */
    private Integer effectiveYear;
    /** 205 电力因子口径: scope_key */
    private String scopeKey;
    /** 205 电力因子口径: scope_name */
    private String scopeName;
    /** 206 温室气体: gas_code */
    private String gasCode;
    /** 206 温室气体: gas_name */
    private String gasName;
    /** 206 温室气体: gas_name_en */
    private String gasNameEn;
    /** 206 温室气体: gwp_value */
    private BigDecimal gwpValue;
    /** 206 温室气体: gwp_version */
    private String gwpVersion;
    /** 206 温室气体: chemical_formula */
    private String chemicalFormula;
}
