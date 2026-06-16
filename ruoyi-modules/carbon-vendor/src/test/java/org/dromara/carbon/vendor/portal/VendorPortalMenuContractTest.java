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
        "cv_dimension_record",
        "cv_factor_customer_scope",
        "cv_report_template",
        "cv_report_template_scope",
        "cv_report_template_download_token",
        "cv_renewal_order",
        "cv_open_api_audit"
    );

    @Test
    void vendorMenuSqlKeepsVendorMenusAndBlocksEnterpriseLocalMenus() throws Exception {
        String sql = Files.readString(resolveProjectFile(MENU_SQL_RELATIVE_PATH));

        assertContainsAll(sql, List.of(
            "厂商运营",
            "数据管理",
            "客户档案",
            "License 授权管理",
            "因子版本",
            "因子明细",
            "因子开放范围",
            "模板库",
            "模板分发",
            "维表管理",
            "公告管理",
            "续费订单",
            "vendor/customer/index",
            "vendor/licenseIssue/index",
            "vendor/factorVersion/index",
            "vendor/factorRecord/index",
            "vendor/factorScope/index",
            "vendor/reportTemplate/index",
            "vendor/templateScope/index",
            "vendor/dimension/index",
            "vendor/announcement/index",
            "vendor/renewalOrder/index",
            "(910136, '数据管理', 0, 2, 'data-management', 'Layout'",
            "(910103, '因子版本', 910136",
            "(910104, '因子明细', 910136",
            "(910121, '因子开放范围', 910136",
            "(910105, '模板库', 910136",
            "(910106, '模板分发', 910136",
            "(910131, '维表管理', 910136",
            "(910126, '公告管理', 910136",
            "vendor:customer:list",
            "vendor:licenseIssue:list",
            "vendor:factorVersion:list",
            "vendor:factorRecord:list",
            "vendor:factorCustomerScope:list",
            "厂商因子按版本、客户与套餐版本控制开放范围，License 通过企业购买版本继承范围",
            "vendor:reportTemplate:list",
            "vendor:reportTemplateScope:list",
            "vendor:dimension:list",
            "vendor:announcement:list",
            "vendor:renewalOrder:list",
            "vendor:customer:query",
            "vendor:factorRecord:remove",
            "vendor:factorCustomerScope:query",
            "vendor:factorCustomerScope:add",
            "vendor:factorCustomerScope:edit",
            "vendor:factorCustomerScope:remove",
            "vendor:reportTemplate:remove",
            "vendor:reportTemplateScope:add",
            "vendor:reportTemplateScope:edit",
            "vendor:reportTemplateScope:remove",
            "vendor:dimension:remove",
            "vendor:announcement:remove",
            "vendor:renewalOrder:add",
            "vendor:renewalOrder:edit",
            "vendor:renewalOrder:callback",
            "vendor:renewalOrder:retryIssue",
            "vendor:renewalOrder:remove"
        ));

        assertContainsNone(sql, List.of(
            "system/tenant/index",
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
            "system/license/index",
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
            "insert ignore into sys_menu",
            "(1, '系统管理', 0, 3, 'system'",
            "(100, '用户管理', 1, 1, 'user', 'system/user/index'",
            "(101, '角色管理', 1, 2, 'role', 'system/role/index'",
            "(102, '菜单管理', 1, 3, 'menu', 'system/menu/index'",
            "(122, '套餐管理', 1, 8, 'tenantPackage', 'system/tenantPackage/index'",
            "(108, '日志管理', 0, 4, 'monitor', 'Layout'",
            "(500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index'",
            "(501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index'",
            "(115, '代码生成', 0, 5, 'gen', 'tool/gen/index'",
            "system/user/index",
            "system/role/index",
            "system/menu/index",
            "system/tenantPackage/index",
            "system/dept/index",
            "system/post/index",
            "system/dict/index",
            "system/config/index",
            "system/notice/index",
            "monitor/logininfor/index",
            "monitor/operlog/index",
            "tool/gen/index",
            "tool:gen:list",
            "菜单管理",
            "角色管理",
            "套餐管理",
            "用户管理",
            "部门管理",
            "岗位管理",
            "字典管理",
            "参数设置",
            "公告配置",
            "system:menu:list",
            "system:role:list",
            "system:user:list",
            "system:dept:list",
            "system:post:list",
            "system:dict:list",
            "system:config:list",
            "system:notice:list",
            "system:tenantPackage:list",
            "system:tenantPackage:add",
            "system:tenantPackage:edit",
            "system:tenantPackage:remove"
        ));

        assertContainsAll(sql, List.of(
            "menu_name = '套餐管理', parent_id = 1, order_num = 8, path = 'tenantPackage', component = 'system/tenantPackage/index', perms = 'system:tenantPackage:list', icon = 'form'",
            "visible = '0', status = '0', remark =",
            "where menu_id = 122",
            "menu_name = '日志管理', parent_id = 0, order_num = 4, path = 'monitor', component = 'Layout'",
            "where menu_id = 108",
            "menu_name = '代码生成', parent_id = 0, order_num = 5, path = 'gen', component = 'tool/gen/index'",
            "where menu_id = 115",
            "(102,",
            "'menu', 'system/menu/index', '', 1, 0, 'C', '1', '0'",
            "'dict', 'system/dict/index', '', 1, 0, 'C', '1', '0'",
            "'config', 'system/config/index', '', 1, 0, 'C', '1', '0'",
            "'gen', 'tool/gen/index', '', 1, 0, 'C', '1', '0'",
            "path = 'menu', component = 'system/menu/index', perms = 'system:menu:list', icon = 'tree-table', visible = '1'",
            "path = 'dict', component = 'system/dict/index', perms = 'system:dict:list', icon = 'dict', visible = '1'",
            "path = 'config', component = 'system/config/index', perms = 'system:config:list', icon = 'edit', visible = '1'",
            "path = 'gen', component = 'tool/gen/index', perms = 'tool:gen:list', icon = 'code', visible = '1'",
            "update sys_menu set visible = '1' where menu_id in (6, 121);"
        ));
        assertContainsNone(sql, List.of(
            "'menu', 'system/menu/index', '', 1, 0, 'C', '0', '0'",
            "'dict', 'system/dict/index', '', 1, 0, 'C', '0', '0'",
            "'config', 'system/config/index', '', 1, 0, 'C', '0', '0'",
            "'gen', 'tool/gen/index', '', 1, 0, 'C', '0', '0'",
            "path = 'menu', component = 'system/menu/index', perms = 'system:menu:list', icon = 'tree-table', visible = '0'",
            "path = 'dict', component = 'system/dict/index', perms = 'system:dict:list', icon = 'dict', visible = '0'",
            "path = 'config', component = 'system/config/index', perms = 'system:config:list', icon = 'edit', visible = '0'",
            "path = 'gen', component = 'tool/gen/index', perms = 'tool:gen:list', icon = 'code', visible = '0'",
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
        assertContainsAll(sqlServer, List.of(
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
