package org.dromara.carbon.vendor.shared;

import java.util.Set;

/**
 * Catalog of vendor-owned data surfaces that may be managed in the cloud.
 */
public final class VendorManagedTableCatalog {

    private static final String TABLE_GROUP_DIMENSION = "dimension";
    private static final String TABLE_GROUP_FACTOR = "factor";

    private static final Set<String> STRONG_DIMENSION_CODES = Set.of(
        "admin-division",
        "emission-source-category",
        "base-year",
        "ef-electricity-factor",
        "ef-electricity-version",
        "ef-electricity-scope",
        "greenhouse-gas"
    );

    private static final Set<String> OPEN_DIMENSION_CODES = Set.of(
        "admin-division",
        "emission-source-category",
        "base-year",
        "ef-electricity-factor",
        "ef-electricity-version",
        "ef-electricity-scope",
        "greenhouse-gas",
        "report-template-download"
    );

    private static final Set<String> FACTOR_TABLE_CODES = Set.of(
        "ef-electricity-factor",
        "ef-electricity-version",
        "ef-electricity-scope",
        "greenhouse-gas"
    );

    private VendorManagedTableCatalog() {
    }

    public static boolean isStrongDimensionCode(String code) {
        return STRONG_DIMENSION_CODES.contains(code);
    }

    public static boolean isOpenDimensionCode(String code) {
        return OPEN_DIMENSION_CODES.contains(code);
    }

    public static boolean isManagedTable(String tableGroup, String tableCode) {
        return switch (tableGroup) {
            case TABLE_GROUP_DIMENSION -> isStrongDimensionCode(tableCode);
            case TABLE_GROUP_FACTOR -> FACTOR_TABLE_CODES.contains(tableCode);
            default -> false;
        };
    }

    public static String physicalTableName(String tableGroup, String tableCode) {
        if (!isManagedTable(tableGroup, tableCode)) {
            return null;
        }
        return switch (tableCode) {
            case "admin-division" -> "cv_admin_division";
            case "emission-source-category" -> "cv_emission_source_category";
            case "base-year" -> "cv_base_year";
            case "ef-electricity-factor" -> "cv_electricity_factor";
            case "ef-electricity-version" -> "cv_electricity_factor_version";
            case "ef-electricity-scope" -> "cv_electricity_factor_scope";
            case "greenhouse-gas" -> "cv_greenhouse_gas";
            default -> null;
        };
    }
}
