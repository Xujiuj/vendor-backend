package org.dromara.carbon.vendor.portal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class VendorPortalMenuContractTest {

    private static final String MENU_SQL_RELATIVE_PATH = "script/sql/portal/vendor_portal_menu.sql";
    private static final String MENU_SYNC_SQL_RELATIVE_PATH =
        "ruoyi-modules/ruoyi-system/src/main/resources/sql/vendor_portal_menu_sync.sql";
    private static final String SYSTEM_RUNNER_RELATIVE_PATH =
        "ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/runner/SystemApplicationRunner.java";
    private static final String MYSQL_SCHEMA_RELATIVE_PATH = "script/sql/mysql/carbon_vendor_schema_v1.sql";
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
        "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?([a-zA-Z0-9_]+)",
        Pattern.CASE_INSENSITIVE
    );
    private static final Set<String> REQUIRED_VENDOR_TABLES = Set.of(
        "cv_customer",
        "cv_signing_key",
        "cv_license_issue",
        "cv_factor_version",
        "cv_dimension_record",
        "cv_vendor_table_field",
        "cv_factor_customer_scope",
        "cv_report_template",
        "cv_report_template_scope",
        "cv_report_template_download_token",
        "cv_renewal_order",
        "cv_open_api_audit"
    );

    @Test
    void vendorMenuSqlKeepsCurrentVendorMenusAndBlocksEnterpriseLocalMenus() throws Exception {
        String sql = Files.readString(resolveProjectFile(MENU_SQL_RELATIVE_PATH));

        assertContainsAll(sql, List.of(
            "vendor/customer/index",
            "vendor/licenseIssue/index",
            "vendor/factorVersion/index",
            "vendor/factorScope/index",
            "vendor/reportTemplate/index",
            "vendor/templateScope/index",
            "vendor/dimension/index",
            "vendor/announcement/index",
            "vendor/renewalOrder/index",
            "(910136,",
            "'data-management', 'Layout'",
            "(910103,",
            "'factor-version', 'vendor/factorVersion/index'",
            "(910121,",
            "'factor-scope', 'vendor/factorScope/index'",
            "(910131,",
            "'dimension', 'vendor/dimension/index'",
            "vendor:customer:list",
            "vendor:licenseIssue:list",
            "vendor:factorVersion:list",
            "vendor:factorVersion:publish",
            "vendor:factorVersion:freeze",
            "vendor:factorVersion:retire",
            "vendor:factorVersion:restore",
            "vendor:factorCustomerScope:list",
            "vendor:reportTemplate:list",
            "vendor:reportTemplateScope:list",
            "vendor:dimension:list",
            "vendor:announcement:list",
            "vendor:renewalOrder:list",
            "vendor:factorCustomerScope:query",
            "vendor:factorCustomerScope:add",
            "vendor:factorCustomerScope:edit",
            "vendor:factorCustomerScope:remove",
            "vendor:dimension:remove",
            "vendor:renewalOrder:retryIssue"
        ));

        assertContainsNone(sql, List.of(
            "enterprise:",
            "enterprise/",
            "system/license/index",
            "vendor/factorRecord/index",
            "vendor:factorRecord:",
            "license-import",
            "license-state",
            "activity-data"
        ));
    }

    @Test
    void vendorStartupMenuSyncUsesTheSameSqlAsPortalScript() throws Exception {
        String portalSql = Files.readString(resolveProjectFile(MENU_SQL_RELATIVE_PATH));
        String startupSql = Files.readString(resolveProjectFile(MENU_SYNC_SQL_RELATIVE_PATH));
        String runner = Files.readString(resolveProjectFile(SYSTEM_RUNNER_RELATIVE_PATH));

        assertEquals(portalSql, startupSql,
            "Startup menu sync SQL must match the manually executable vendor portal menu SQL");
        assertContainsAll(runner, List.of(
            "syncVendorPortalMenu()",
            "sql/vendor_portal_menu_sync.sql",
            "setSqlScriptEncoding(\"UTF-8\")"
        ));
    }

    @Test
    void vendorMenuSqlKeepsOnlyRequiredRuoyiSystemSurfaces() throws Exception {
        String sql = Files.readString(resolveProjectFile(MENU_SQL_RELATIVE_PATH));

        assertContainsAll(sql, List.of(
            "insert ignore into sys_menu",
            "(1,",
            "'system'",
            "(100,",
            "'user', 'system/user/index'",
            "(101,",
            "'role', 'system/role/index'",
            "(103,",
            "'dept', 'system/dept/index'",
            "(104,",
            "'post', 'system/post/index'",
            "(107,",
            "'notice', 'system/notice/index'",
            "(108,",
            "'monitor', 'Layout'",
            "(500,",
            "'operlog', 'monitor/operlog/index'",
            "(501,",
            "'logininfor', 'monitor/logininfor/index'",
            "(122,",
            "'tenantPackage', 'system/tenantPackage/index'",
            "system:user:list",
            "system:role:list",
            "system:dept:list",
            "system:post:list",
            "system:notice:list",
            "system:tenantPackage:list",
            "monitor:operlog:list",
            "monitor:logininfor:list"
        ));

        assertContainsAll(sql, List.of(
            "Superadmin routing uses all enabled M/C menus",
            "set visible = '1',\n    status = '1'",
            "menu_id in (2, 3, 4, 5, 6, 115, 116, 121)",
            "'tenant', 'demo'",
            "'oss', 'oss-config/index', 'client'",
            "component like 'demo/%'",
            "delete from sys_menu",
            "system/menu/index",
            "system/dict/index",
            "system/config/index"
        ));

        assertContainsNone(sql, List.of(
            "system:menu:list",
            "system:dict:list",
            "system:config:list",
            "tool:gen:list",
            "update sys_menu set visible = '1' where menu_id in (6, 121, 122)"
        ));
    }

    @Test
    void vendorMenuSqlOnlyReplacesReservedVendorMenuRange() throws Exception {
        String sql = Files.readString(resolveProjectFile(MENU_SQL_RELATIVE_PATH)).toLowerCase();

        assertTrue(sql.contains("delete from sys_role_menu where menu_id between 900100 and 900199"));
        assertTrue(sql.contains("delete from sys_menu where menu_id between 900100 and 900199"));
        assertTrue(sql.contains("delete from sys_role_menu where menu_id between 910100 and 910199"));
        assertTrue(sql.contains("delete from sys_menu where menu_id between 910100 and 910199"));
        assertFalse(sql.contains("delete from sys_menu where menu_id < 910100"));
        assertFalse(sql.contains("truncate table sys_menu"));
    }

    @Test
    void vendorMenuSqlSeedsEveryEnabledVendorRole() throws Exception {
        String sql = Files.readString(resolveProjectFile(MENU_SQL_RELATIVE_PATH));
        int enabledRoleSeedStart = sql.indexOf("where r.status = '0'");
        int enabledRoleSeedEnd = sql.indexOf("-- Keep the built-in superadmin role");
        assertTrue(enabledRoleSeedStart >= 0, "Expected enabled-role menu seed block");
        assertTrue(enabledRoleSeedEnd > enabledRoleSeedStart, "Expected superadmin seed block after enabled-role seed block");
        String enabledRoleSeedSql = sql.substring(enabledRoleSeedStart, enabledRoleSeedEnd);

        assertContainsAll(sql, List.of(
            "insert ignore into sys_role_menu (role_id, menu_id)",
            "select 1, menu_id"
        ));

        assertContainsAll(enabledRoleSeedSql, List.of(
            "where r.status = '0'",
            "m.menu_id between 910100 and 910199",
            "1, 100, 101, 103, 104, 107, 108, 122, 130, 131, 500, 501",
            "1008, 1009, 1010, 1011, 1012",
            "1017, 1018, 1019, 1020",
            "1021, 1022, 1023, 1024, 1025",
            "1036, 1037, 1038, 1039",
            "1040, 1041, 1042, 1043, 1044, 1045",
            "1611, 1612, 1613, 1614, 1615"
        ));
        assertContainsNone(enabledRoleSeedSql, List.of(
            "r.role_key in ('test1', 'test2')",
            "1, 100, 101, 102, 103",
            "1013, 1014, 1015, 1016",
            "1026, 1027, 1028, 1029, 1030",
            "1031, 1032, 1033, 1034, 1035"
        ));
    }

    @Test
    void vendorSchemaSqlContainsOnlyVendorBusinessTables() throws Exception {
        String mysql = Files.readString(resolveProjectFile(MYSQL_SCHEMA_RELATIVE_PATH));

        assertEquals(REQUIRED_VENDOR_TABLES, createTableNames(mysql),
            "Vendor MySQL schema should contain only current vendor business tables");

        assertContainsNone(mysql, List.of(
            "CREATE TABLE ce_",
            "REFERENCES ce_",
            "enterprise_",
            "sheet_656",
            "capture_",
            "activity_data",
            "green_certificate",
            "denominator_fact",
            "enterprise_local"
        ));
    }

    @Test
    void vendorSchemaSqlKeepsLicenseFactorTemplateAndRenewalBoundaries() throws Exception {
        String mysql = Files.readString(resolveProjectFile(MYSQL_SCHEMA_RELATIVE_PATH));

        assertContainsAll(mysql, List.of(
            "customer_code",
            "private_key_ref",
            "source_license_id",
            "edition",
            "feature_codes",
            "scope_license_key",
            "template_version",
            "license_id",
            "download_token",
            "token_status",
            "expires_time",
            "pay_channel",
            "issue_status",
            "issued_license_id",
            "api_path",
            "response_status",
            "request_summary"
        ));
    }

    private static void assertContainsAll(String text, List<String> expectedFragments) {
        for (String expectedFragment : expectedFragments) {
            assertTrue(text.contains(expectedFragment), "Expected SQL to contain: " + expectedFragment);
        }
    }

    private static void assertContainsNone(String text, List<String> forbiddenFragments) {
        for (String forbiddenFragment : forbiddenFragments) {
            assertFalse(text.contains(forbiddenFragment), "Expected SQL not to contain: " + forbiddenFragment);
        }
    }

    private static Set<String> createTableNames(String sql) {
        Matcher matcher = CREATE_TABLE_PATTERN.matcher(sql);
        Set<String> tableNames = new LinkedHashSet<>();
        while (matcher.find()) {
            tableNames.add(matcher.group(1).toLowerCase());
        }
        return tableNames;
    }

    private static Path resolveProjectFile(String relativePath) {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve(relativePath);
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Unable to find " + relativePath);
    }
}
