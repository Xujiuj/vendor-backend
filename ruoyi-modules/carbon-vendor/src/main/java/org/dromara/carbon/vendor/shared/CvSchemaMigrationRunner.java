package org.dromara.carbon.vendor.shared;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Vendor SQL Server schema migration runner for delivery-owned carbon tables.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CvSchemaMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        createReportContentTableIfMissing();
        backfillElectricityFactorVersionProvinceCode();
    }

    private void createReportContentTableIfMissing() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = SCHEMA_NAME() AND TABLE_NAME = ?",
                Integer.class, "cv_report_content");
            if (count != null && count > 0) {
                return;
            }
            jdbcTemplate.execute("""
                CREATE TABLE cv_report_content (
                    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                    directory_no INT NOT NULL,
                    directory_name NVARCHAR(128) NOT NULL,
                    subdirectory_no INT NOT NULL,
                    subdirectory_name NVARCHAR(128) NOT NULL,
                    chart_names NVARCHAR(MAX) NOT NULL,
                    display_order INT NOT NULL CONSTRAINT df_cv_report_content_display_order DEFAULT 0,
                    status CHAR(1) NOT NULL CONSTRAINT df_cv_report_content_status DEFAULT '0',
                    create_time DATETIME2 NULL CONSTRAINT df_cv_report_content_create_time DEFAULT SYSDATETIME(),
                    update_time DATETIME2 NULL CONSTRAINT df_cv_report_content_update_time DEFAULT SYSDATETIME(),
                    remark NVARCHAR(500) NULL
                )
                """);
            jdbcTemplate.execute("CREATE INDEX idx_cv_report_content_order ON cv_report_content (display_order, directory_no, subdirectory_no)");
            log.info("[SchemaMigration] created cv_report_content");
        } catch (Exception e) {
            log.warn("[SchemaMigration] cv_report_content creation skipped: {}", e.getMessage());
        }
    }

    private void backfillElectricityFactorVersionProvinceCode() {
        try {
            Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = SCHEMA_NAME() AND TABLE_NAME = ?",
                Integer.class, "cv_electricity_factor");
            if (tableCount == null || tableCount == 0) {
                return;
            }
            Integer columnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = SCHEMA_NAME() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class, "cv_electricity_factor", "version_province_code");
            if (columnCount == null || columnCount == 0) {
                jdbcTemplate.execute("ALTER TABLE cv_electricity_factor ADD version_province_code NVARCHAR(64) NULL");
                log.info("[SchemaMigration] added cv_electricity_factor.version_province_code");
            }
            jdbcTemplate.update("""
                UPDATE cv_electricity_factor
                   SET version_province_code = CONCAT(factor_version, division_code)
                 WHERE (version_province_code IS NULL OR version_province_code = '')
                   AND factor_version IS NOT NULL
                   AND factor_version <> ''
                   AND division_code IS NOT NULL
                   AND division_code <> ''
                """);
            jdbcTemplate.update("""
                UPDATE cv_factor_record
                   SET factor_code = version_province_code,
                       factor_key = version_province_code
                 WHERE factor_table_code = '202ef'
                   AND version_province_code IS NOT NULL
                   AND version_province_code <> ''
                   AND (
                        factor_code IS NULL
                        OR factor_code = ''
                        OR factor_code LIKE '%:%'
                        OR factor_key IS NULL
                        OR factor_key = ''
                        OR factor_key LIKE '%:%'
                   )
                """);
        } catch (Exception e) {
            log.warn("[SchemaMigration] cv_electricity_factor.version_province_code backfill skipped: {}", e.getMessage());
        }
    }
}
