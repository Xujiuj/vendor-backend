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
    private static final String MYSQL_SCHEMA_RELATIVE_PATH = "script/sql/mysql/carbon_vendor_schema_v1.sql";
    private static final String SQLSERVER_SCHEMA_RELATIVE_PATH = "script/sql/sqlserver/carbon_vendor_schema_v1.sql";
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
        "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?([a-zA-Z0-9_]+)",
        Pattern.CASE_INSENSITIVE
    );
    private static final Set<String> REQUIRED_VENDOR_TABLES = Set.of(
        "cv_customer",
        "cv_signing_key",
        "cv_license_issue",
        "cv_factor_version",
        "cv_factor_record",
        "cv_factor_customer_scope",
        "cv_report_template",
        "cv_report_template_scope",
        "cv_renewal_order"
    );

    @Test
    void vendorMenuSqlKeepsVendorMenusAndBlocksEnterpriseLocalMenus() throws Exception {
        String sql = Files.readString(resolveProjectFile(MENU_SQL_RELATIVE_PATH));

        assertContainsAll(sql, List.of(
            "厂商运营",
            "客户档案",
            "License 授权管理",
            "因子版本",
            "因子开放范围",
            "模板库",
            "模板分发",
            "续费订单",
            "system/license/index",
            "vendor/factorVersion/index",
            "vendor/factorScope/index",
            "vendor/reportTemplate/index",
            "vendor/templateScope/index",
            "vendor/renewalOrder/index",
            "vendor:customer:list",
            "vendor:licenseIssue:list",
            "vendor:factorVersion:list",
            "vendor:factorCustomerScope:list",
            "vendor:reportTemplate:list",
            "vendor:reportTemplateScope:list",
            "vendor:renewalOrder:list"
        ));

        assertContainsNone(sql, List.of(
            "01 配置排放源",
            "02 确认排放因子",
            "03 活动数据",
            "04 绿电绿证",
            "05 强度管理",
            "License 导入",
            "运行状态",
            "企业本地填报",
            "企业本地校验",
            "enterprise:",
            "enterprise/",
            "license-import",
            "license-state",
            "activity-data",
            "企业本地业务"
        ));
    }

    @Test
    void vendorMenuSqlPreservesRuoyiSystemManagementLogsAndGenerator() throws Exception {
        String sql = Files.readString(resolveProjectFile(MENU_SQL_RELATIVE_PATH));

        assertContainsAll(sql, List.of(
            "system/user/index",
            "system/role/index",
            "system/menu/index",
            "system/dept/index",
            "system/post/index",
            "system/dict/index",
            "system/config/index",
            "system/notice/index",
            "monitor/logininfor/index",
            "monitor/operlog/index",
            "tool/gen/index",
            "tool:gen:list"
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
    void vendorSchemaSqlContainsOnlyVendorBusinessTables() throws Exception {
        String mysql = Files.readString(resolveProjectFile(MYSQL_SCHEMA_RELATIVE_PATH));
        String sqlServer = Files.readString(resolveProjectFile(SQLSERVER_SCHEMA_RELATIVE_PATH));

        assertEquals(REQUIRED_VENDOR_TABLES, createTableNames(mysql),
            "Vendor MySQL schema should contain only current vendor business tables");
        assertEquals(REQUIRED_VENDOR_TABLES, createTableNames(sqlServer),
            "Vendor SQL Server schema should contain only current vendor business tables");

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
        assertContainsNone(sqlServer, List.of(
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
        String sqlServer = Files.readString(resolveProjectFile(SQLSERVER_SCHEMA_RELATIVE_PATH));

        assertContainsAll(mysql, List.of(
            "customer_code",
            "private_key_ref",
            "source_license_id",
            "edition",
            "feature_codes",
            "scope_license_key",
            "template_version",
            "license_id",
            "pay_channel",
            "issued_license_id"
        ));
        assertContainsAll(sqlServer, List.of(
            "customer_code",
            "private_key_ref",
            "source_license_id",
            "edition",
            "feature_codes",
            "scope_license_key",
            "template_version",
            "license_id",
            "pay_channel",
            "issued_license_id"
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
