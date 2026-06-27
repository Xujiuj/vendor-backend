package org.dromara.carbon.vendor.shared;

import java.util.Set;

/**
 * Catalog of vendor-owned data surfaces that may be managed in the cloud.
 */
public final class VendorManagedTableCatalog {

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
            case "dimension" -> isStrongDimensionCode(tableCode);
            case "factor" -> FACTOR_TABLE_CODES.contains(tableCode);
            default -> false;
        };
    }
}
