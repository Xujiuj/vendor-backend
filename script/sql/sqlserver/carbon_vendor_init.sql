-- Vendor SQL Server data initialization.
-- Target: the connected vendor database only.
-- Scope: clear all current data, then seed the minimal vendor runtime baseline.
-- This script intentionally does not create or alter table structures.

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
SET NOCOUNT ON;
GO

IF DB_NAME() <> N'vendor'
    THROW 51000, 'Refusing to initialize a database other than vendor.', 1;

IF OBJECT_ID(N'dbo.sys_user', N'U') IS NULL
    THROW 51000, 'Missing dbo.sys_user. Run the base schema before carbon_vendor_init.sql.', 1;
IF OBJECT_ID(N'dbo.sys_menu', N'U') IS NULL
    THROW 51000, 'Missing dbo.sys_menu. Run the base schema before carbon_vendor_init.sql.', 1;
IF OBJECT_ID(N'dbo.sys_tenant_package', N'U') IS NULL
    THROW 51000, 'Missing dbo.sys_tenant_package. Run the vendor schema before carbon_vendor_init.sql.', 1;
IF OBJECT_ID(N'dbo.cv_signing_key', N'U') IS NULL
    THROW 51000, 'Missing dbo.cv_signing_key. Run the vendor schema before carbon_vendor_init.sql.', 1;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @now DATETIME2 = SYSDATETIME();
    DECLARE @tenantId NVARCHAR(20) = N'000000';
    DECLARE @createDept BIGINT = 103;
    DECLARE @createBy BIGINT = 1;
    DECLARE @adminPassword NVARCHAR(100) = N'$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2';
    DECLARE @menuIds NVARCHAR(MAX);
    DECLARE @sql NVARCHAR(MAX) = N'';

    SELECT @sql = @sql + N'DELETE FROM ' + QUOTENAME(SCHEMA_NAME(schema_id)) + N'.' + QUOTENAME(name) + N';' + CHAR(10)
      FROM sys.tables
     WHERE is_ms_shipped = 0
       AND SCHEMA_NAME(schema_id) = N'dbo'
     ORDER BY name;

    EXEC sp_executesql @sql;

    DECLARE @identitySql NVARCHAR(MAX) = N'';
    SELECT @identitySql = @identitySql
        + N'IF OBJECTPROPERTY(OBJECT_ID(N''' + QUOTENAME(SCHEMA_NAME(t.schema_id)) + N'.' + QUOTENAME(t.name) + N'''), ''TableHasIdentity'') = 1 '
        + N'DBCC CHECKIDENT (N''' + QUOTENAME(SCHEMA_NAME(t.schema_id)) + N'.' + QUOTENAME(t.name) + N''', RESEED, 0) WITH NO_INFOMSGS;' + CHAR(10)
      FROM sys.tables t
     WHERE EXISTS (SELECT 1 FROM sys.columns c WHERE c.object_id = t.object_id AND c.is_identity = 1);
    EXEC sp_executesql @identitySql;

    INSERT INTO dbo.sys_tenant
        (id, tenant_id, contact_user_name, contact_phone, company_name, license_number, address, intro, domain, remark,
         package_id, expire_time, account_count, status, del_flag, create_dept, create_by, create_time, update_by, update_time)
    VALUES
        (1, @tenantId, N'厂商管理员', N'15888888888', N'厂商碳运营管理平台', NULL, NULL, N'厂商端独立运营租户',
         NULL, NULL, NULL, NULL, NULL, N'0', N'0', @createDept, @createBy, @now, NULL, NULL);

    INSERT INTO dbo.sys_dept
        (dept_id, tenant_id, parent_id, ancestors, dept_name, dept_category, order_num, leader, phone, email,
         status, del_flag, create_dept, create_by, create_time, update_by, update_time)
    VALUES
        (100, @tenantId, 0, N'0', N'厂商总部', NULL, 0, 1, N'15888888888', N'admin@example.com',
         N'0', N'0', @createDept, @createBy, @now, NULL, NULL),
        (103, @tenantId, 100, N'0,100', N'运营管理部', NULL, 1, 1, N'15888888888', N'admin@example.com',
         N'0', N'0', @createDept, @createBy, @now, NULL, NULL);

    INSERT INTO dbo.sys_post
        (post_id, tenant_id, dept_id, post_code, post_category, post_name, post_sort, status, create_dept,
         create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, @tenantId, 103, N'vendor_admin', NULL, N'厂商管理员', 1, N'0', @createDept, @createBy, @now, NULL, NULL, NULL);

    INSERT INTO dbo.sys_role
        (role_id, tenant_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly,
         status, del_flag, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, @tenantId, N'超级管理员', N'superadmin', 1, N'1', 1, 1, N'0', N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化超级管理员');

    INSERT INTO dbo.sys_user
        (user_id, tenant_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password,
         status, del_flag, login_ip, login_date, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, @tenantId, 103, N'admin', N'厂商管理员', N'sys_user', N'admin@example.com', N'15888888888', N'1',
         NULL, @adminPassword, N'0', N'0', N'127.0.0.1', @now, @createDept, @createBy, @now, NULL, NULL,
         N'初始化超级管理员，密码 admin123');

    INSERT INTO dbo.sys_user_role (user_id, role_id) VALUES (1, 1);
    INSERT INTO dbo.sys_user_post (user_id, post_id) VALUES (1, 1);

    INSERT INTO dbo.sys_client
        (id, client_id, client_key, client_secret, grant_type, device_type, active_timeout, timeout, status,
         del_flag, create_dept, create_by, create_time, update_by, update_time)
    VALUES
        (1, N'e5cd7e4891bf95d1d19206ce24a7b32e', N'pc', N'pc123', N'password,social', N'pc', 1800, 604800, N'0', N'0', @createDept, @createBy, @now, NULL, NULL),
        (2, N'428a8310cd442757ae699df5d894f051', N'app', N'app123', N'password,sms,social', N'android', 1800, 604800, N'0', N'0', @createDept, @createBy, @now, NULL, NULL);

    INSERT INTO dbo.sys_config
        (config_id, tenant_id, config_name, config_key, config_value, config_type, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, @tenantId, N'主框架页-默认皮肤样式名称', N'sys.index.skinName', N'skin-blue', N'Y', @createDept, @createBy, @now, NULL, NULL, N'蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow'),
        (2, @tenantId, N'用户管理-账号初始密码', N'sys.user.initPassword', N'123456', N'Y', @createDept, @createBy, @now, NULL, NULL, N'初始化密码 123456'),
        (3, @tenantId, N'主框架页-侧边栏主题', N'sys.index.sideTheme', N'theme-dark', N'Y', @createDept, @createBy, @now, NULL, NULL, N'深色主题 theme-dark，浅色主题 theme-light'),
        (5, @tenantId, N'账号自助-是否开启用户注册功能', N'sys.account.registerUser', N'false', N'Y', @createDept, @createBy, @now, NULL, NULL, N'是否开启注册用户功能'),
        (11, @tenantId, N'OSS预览列表资源开关', N'sys.oss.previewListResource', N'true', N'Y', @createDept, @createBy, @now, NULL, NULL, N'true:开启 false:关闭');

    INSERT INTO dbo.sys_dict_type
        (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, @tenantId, N'用户性别', N'sys_user_sex', @createDept, @createBy, @now, NULL, NULL, N'用户性别列表'),
        (2, @tenantId, N'菜单状态', N'sys_show_hide', @createDept, @createBy, @now, NULL, NULL, N'菜单状态列表'),
        (3, @tenantId, N'系统开关', N'sys_normal_disable', @createDept, @createBy, @now, NULL, NULL, N'系统开关列表'),
        (6, @tenantId, N'系统是否', N'sys_yes_no', @createDept, @createBy, @now, NULL, NULL, N'系统是否列表'),
        (7, @tenantId, N'通知类型', N'sys_notice_type', @createDept, @createBy, @now, NULL, NULL, N'通知类型列表'),
        (8, @tenantId, N'通知状态', N'sys_notice_status', @createDept, @createBy, @now, NULL, NULL, N'通知状态列表'),
        (9, @tenantId, N'操作类型', N'sys_oper_type', @createDept, @createBy, @now, NULL, NULL, N'操作类型列表'),
        (10, @tenantId, N'系统状态', N'sys_common_status', @createDept, @createBy, @now, NULL, NULL, N'登录状态列表');

    INSERT INTO dbo.sys_dict_data
        (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default,
         create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, @tenantId, 1, N'男', N'0', N'sys_user_sex', NULL, NULL, N'Y', @createDept, @createBy, @now, NULL, NULL, N'性别男'),
        (2, @tenantId, 2, N'女', N'1', N'sys_user_sex', NULL, NULL, N'N', @createDept, @createBy, @now, NULL, NULL, N'性别女'),
        (3, @tenantId, 3, N'未知', N'2', N'sys_user_sex', NULL, NULL, N'N', @createDept, @createBy, @now, NULL, NULL, N'性别未知'),
        (4, @tenantId, 1, N'显示', N'0', N'sys_show_hide', NULL, N'primary', N'Y', @createDept, @createBy, @now, NULL, NULL, N'显示菜单'),
        (5, @tenantId, 2, N'隐藏', N'1', N'sys_show_hide', NULL, N'danger', N'N', @createDept, @createBy, @now, NULL, NULL, N'隐藏菜单'),
        (6, @tenantId, 1, N'正常', N'0', N'sys_normal_disable', NULL, N'primary', N'Y', @createDept, @createBy, @now, NULL, NULL, N'正常状态'),
        (7, @tenantId, 2, N'停用', N'1', N'sys_normal_disable', NULL, N'danger', N'N', @createDept, @createBy, @now, NULL, NULL, N'停用状态'),
        (12, @tenantId, 1, N'是', N'Y', N'sys_yes_no', NULL, N'primary', N'Y', @createDept, @createBy, @now, NULL, NULL, N'系统默认是'),
        (13, @tenantId, 2, N'否', N'N', N'sys_yes_no', NULL, N'danger', N'N', @createDept, @createBy, @now, NULL, NULL, N'系统默认否'),
        (14, @tenantId, 1, N'通知', N'1', N'sys_notice_type', NULL, N'warning', N'Y', @createDept, @createBy, @now, NULL, NULL, N'通知'),
        (15, @tenantId, 2, N'公告', N'2', N'sys_notice_type', NULL, N'success', N'N', @createDept, @createBy, @now, NULL, NULL, N'公告'),
        (16, @tenantId, 1, N'正常', N'0', N'sys_notice_status', NULL, N'primary', N'Y', @createDept, @createBy, @now, NULL, NULL, N'正常状态'),
        (17, @tenantId, 2, N'关闭', N'1', N'sys_notice_status', NULL, N'danger', N'N', @createDept, @createBy, @now, NULL, NULL, N'关闭状态'),
        (18, @tenantId, 1, N'新增', N'1', N'sys_oper_type', NULL, N'info', N'N', @createDept, @createBy, @now, NULL, NULL, N'新增操作'),
        (19, @tenantId, 2, N'修改', N'2', N'sys_oper_type', NULL, N'info', N'N', @createDept, @createBy, @now, NULL, NULL, N'修改操作'),
        (20, @tenantId, 3, N'删除', N'3', N'sys_oper_type', NULL, N'danger', N'N', @createDept, @createBy, @now, NULL, NULL, N'删除操作'),
        (21, @tenantId, 4, N'授权', N'4', N'sys_oper_type', NULL, N'primary', N'N', @createDept, @createBy, @now, NULL, NULL, N'授权操作'),
        (22, @tenantId, 5, N'导出', N'5', N'sys_oper_type', NULL, N'warning', N'N', @createDept, @createBy, @now, NULL, NULL, N'导出操作'),
        (23, @tenantId, 6, N'导入', N'6', N'sys_oper_type', NULL, N'warning', N'N', @createDept, @createBy, @now, NULL, NULL, N'导入操作'),
        (24, @tenantId, 7, N'强退', N'7', N'sys_oper_type', NULL, N'danger', N'N', @createDept, @createBy, @now, NULL, NULL, N'强退操作'),
        (25, @tenantId, 8, N'生成代码', N'8', N'sys_oper_type', NULL, N'warning', N'N', @createDept, @createBy, @now, NULL, NULL, N'生成代码'),
        (26, @tenantId, 9, N'清空数据', N'9', N'sys_oper_type', NULL, N'danger', N'N', @createDept, @createBy, @now, NULL, NULL, N'清空操作'),
        (27, @tenantId, 1, N'成功', N'0', N'sys_common_status', NULL, N'primary', N'Y', @createDept, @createBy, @now, NULL, NULL, N'正常状态'),
        (28, @tenantId, 2, N'失败', N'1', N'sys_common_status', NULL, N'danger', N'N', @createDept, @createBy, @now, NULL, NULL, N'停用状态');

    INSERT INTO dbo.sys_oss_config
        (oss_config_id, tenant_id, config_key, access_key, secret_key, bucket_name, prefix, endpoint, domain,
         is_https, region, access_policy, status, ext1, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, @tenantId, N'minio', N'ruoyi', N'ruoyi123', N'ruoyi', NULL, N'127.0.0.1:9000', NULL, N'N', NULL, N'1', N'0', NULL, @createDept, @createBy, @now, NULL, NULL, NULL),
        (5, @tenantId, N'image', N'ruoyi', N'ruoyi123', N'ruoyi', N'image', N'127.0.0.1:9000', NULL, N'N', NULL, N'1', N'1', NULL, @createDept, @createBy, @now, NULL, NULL, NULL);

    INSERT INTO dbo.sj_namespace (id, name, unique_id, description, deleted, create_dt, update_dt)
    VALUES
        (1, N'Development', N'dev', N'', 0, @now, @now),
        (2, N'Production', N'prod', N'', 0, @now, @now);

    INSERT INTO dbo.sj_group_config
        (id, namespace_id, group_name, description, token, group_status, version, group_partition, id_generator_mode, init_scene, create_dt, update_dt)
    VALUES
        (1, N'dev', N'ruoyi_group', N'', N'SJ_cKqBTPzCsWA3VyuCfFoccmuIEGXjr5KT', 1, 1, 0, 1, 1, @now, @now),
        (2, N'prod', N'ruoyi_group', N'', N'SJ_cKqBTPzCsWA3VyuCfFoccmuIEGXjr5KT', 1, 1, 0, 1, 1, @now, @now);

    INSERT INTO dbo.sj_system_user (id, username, password, role, create_dt, update_dt)
    VALUES (1, N'admin', N'465c194afb65670f38322df087f0a9bb225cc257e43eb4ac5a0c98ef5b3173ac', 2, @now, @now);

    INSERT INTO dbo.flow_category
        (category_id, tenant_id, parent_id, ancestors, category_name, order_num, del_flag, create_dept, create_by, create_time, update_by, update_time)
    VALUES
        (100, @tenantId, 0, N'0', N'OA流程', 0, N'0', @createDept, @createBy, @now, NULL, NULL),
        (101, @tenantId, 100, N'0,100', N'行政办公', 0, N'0', @createDept, @createBy, @now, NULL, NULL),
        (102, @tenantId, 100, N'0,100', N'人事管理', 1, N'0', @createDept, @createBy, @now, NULL, NULL);

    INSERT INTO dbo.flow_spel
        (id, component_name, method_name, method_params, view_spel, remark, status, del_flag, create_dept, create_by, create_time, update_by, update_time)
    VALUES
        (1, N'spelRuleComponent', N'selectDeptLeaderById', N'initiatorDeptId', N'#{@spelRuleComponent.selectDeptLeaderById(#initiatorDeptId)}', N'按部门 id 查询部门负责人', N'0', N'0', @createDept, @createBy, @now, NULL, NULL),
        (2, NULL, NULL, N'initiator', N'${initiator}', N'发起人', N'0', N'0', @createDept, @createBy, @now, NULL, NULL);

    DECLARE @menus TABLE (
        menu_id BIGINT NOT NULL PRIMARY KEY,
        menu_name NVARCHAR(100) NOT NULL,
        parent_id BIGINT NULL,
        order_num INT NULL,
        path NVARCHAR(200) NULL,
        component NVARCHAR(255) NULL,
        query_param NVARCHAR(255) NULL,
        is_frame INT NULL,
        is_cache INT NULL,
        menu_type NVARCHAR(1) NULL,
        visible NVARCHAR(1) NULL,
        status NVARCHAR(1) NULL,
        perms NVARCHAR(100) NULL,
        icon NVARCHAR(100) NULL,
        remark NVARCHAR(500) NULL
    );

    INSERT INTO @menus
        (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type,
         visible, status, perms, icon, remark)
    VALUES
        (910100, N'厂商运营', 0, 1, N'vendor', N'Layout', N'', 1, 0, N'M', N'0', N'0', N'', N'guide', N'厂商端运营菜单'),
        (910136, N'数据管理', 0, 2, N'data-management', N'Layout', N'', 1, 0, N'M', N'0', N'0', N'', N'database', N'厂商端基础数据管理'),
        (1, N'系统管理', 0, 3, N'system', N'Layout', N'', 1, 0, N'M', N'0', N'0', N'', N'system', N'系统管理目录'),
        (108, N'日志管理', 0, 4, N'monitor', N'Layout', N'', 1, 0, N'M', N'0', N'0', N'', N'log', N'日志管理目录'),
        (910108, N'运营总览', 910100, 0, N'overview', N'', N'', 1, 0, N'F', N'1', N'0', N'vendor:overview:query', N'#', N'厂商首页运营总览接口权限'),
        (910101, N'客户档案', 910100, 1, N'customer', N'vendor/customer/index', N'', 1, 0, N'C', N'0', N'0', N'vendor:customer:list', N'peoples', N'厂商客户档案'),
        (910102, N'授权管理', 910100, 2, N'license', N'vendor/licenseIssue/index', N'', 1, 0, N'C', N'0', N'0', N'vendor:licenseIssue:list', N'lock', N'厂商授权签发与撤销'),
        (910107, N'续费订单', 910100, 10, N'renewal-order', N'vendor/renewalOrder/index', N'', 1, 0, N'C', N'0', N'0', N'vendor:renewalOrder:list', N'money', N'厂商续费订单'),
        (910103, N'因子版本', 910136, 1, N'factor-version', N'vendor/factorVersion/index', N'', 1, 0, N'C', N'0', N'0', N'vendor:factorVersion:list', N'tree-table', N'厂商因子版本管理'),
        (910121, N'因子开放范围', 910136, 2, N'factor-scope', N'vendor/factorScope/index', N'', 1, 0, N'C', N'0', N'0', N'vendor:factorCustomerScope:list', N'tree', N'厂商因子开放范围'),
        (910105, N'模板库', 910136, 3, N'report-template', N'vendor/reportTemplate/index', N'', 1, 0, N'C', N'0', N'0', N'vendor:reportTemplate:list', N'form', N'厂商报表模板库'),
        (910106, N'模板分发', 910136, 4, N'template-scope', N'vendor/templateScope/index', N'', 1, 0, N'C', N'0', N'0', N'vendor:reportTemplateScope:list', N'share', N'厂商模板分发范围'),
        (910156, N'报表内容', 910136, 5, N'report-content', N'vendor/reportContent/index', N'', 1, 0, N'C', N'0', N'0', N'vendor:reportContent:list', N'documentation', N'厂商报表内容配置'),
        (910131, N'维表管理', 910136, 6, N'dimension', N'vendor/dimension/index', N'', 1, 0, N'C', N'0', N'0', N'vendor:dimension:list', N'tree', N'厂商维表管理'),
        (910126, N'公告管理', 910136, 7, N'announcement', N'vendor/announcement/index', N'', 1, 0, N'C', N'0', N'0', N'vendor:announcement:list', N'message', N'厂商公告管理'),
        (100, N'用户管理', 1, 1, N'user', N'system/user/index', N'', 1, 0, N'C', N'0', N'0', N'system:user:list', N'user', N'用户管理'),
        (101, N'角色管理', 1, 2, N'role', N'system/role/index', N'', 1, 0, N'C', N'0', N'0', N'system:role:list', N'peoples', N'角色管理'),
        (103, N'部门管理', 1, 4, N'dept', N'system/dept/index', N'', 1, 0, N'C', N'0', N'0', N'system:dept:list', N'tree', N'部门管理'),
        (104, N'岗位管理', 1, 5, N'post', N'system/post/index', N'', 1, 0, N'C', N'0', N'0', N'system:post:list', N'post', N'岗位管理'),
        (122, N'套餐管理', 1, 8, N'tenantPackage', N'system/tenantPackage/index', N'', 1, 0, N'C', N'0', N'0', N'system:tenantPackage:list', N'form', N'授权套餐管理'),
        (107, N'通知公告', 1, 9, N'notice', N'system/notice/index', N'', 1, 0, N'C', N'0', N'0', N'system:notice:list', N'message', N'通知公告'),
        (500, N'操作日志', 108, 1, N'operlog', N'monitor/operlog/index', N'', 1, 0, N'C', N'0', N'0', N'monitor:operlog:list', N'form', N'操作日志'),
        (501, N'登录日志', 108, 2, N'logininfor', N'monitor/logininfor/index', N'', 1, 0, N'C', N'0', N'0', N'monitor:logininfor:list', N'logininfor', N'登录日志');

    INSERT INTO @menus
        (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type,
         visible, status, perms, icon, remark)
    VALUES
        (910109, N'客户档案详情', 910101, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:customer:query', N'#', N'客户档案详情权限'),
        (910148, N'客户档案新增', 910101, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:customer:add', N'#', N'客户档案新增权限'),
        (910149, N'客户档案编辑', 910101, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:customer:edit', N'#', N'客户档案编辑权限'),
        (910150, N'客户档案删除', 910101, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:customer:remove', N'#', N'客户档案删除权限'),
        (910151, N'客户档案导出', 910101, 5, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:customer:export', N'#', N'客户档案导出权限'),
        (910161, N'授权管理删除', 910102, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:licenseIssue:remove', N'#', N'授权管理删除权限'),
        (910110, N'因子版本详情', 910103, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:factorVersion:query', N'#', N'因子版本详情权限'),
        (910145, N'因子版本新增', 910103, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:factorVersion:add', N'#', N'因子版本新增权限'),
        (910146, N'因子版本编辑', 910103, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:factorVersion:edit', N'#', N'因子版本编辑权限'),
        (910147, N'因子版本删除', 910103, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:factorVersion:remove', N'#', N'因子版本删除权限'),
        (910152, N'因子版本发布', 910103, 5, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:factorVersion:publish', N'#', N'因子版本发布权限'),
        (910153, N'因子版本冻结', 910103, 6, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:factorVersion:freeze', N'#', N'因子版本冻结权限'),
        (910154, N'因子版本退役', 910103, 7, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:factorVersion:retire', N'#', N'因子版本退役权限'),
        (910155, N'因子版本恢复', 910103, 8, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:factorVersion:restore', N'#', N'因子版本恢复权限'),
        (910124, N'因子开放范围详情', 910121, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:factorCustomerScope:query', N'#', N'因子开放范围详情权限'),
        (910137, N'因子开放范围新增', 910121, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:factorCustomerScope:add', N'#', N'因子开放范围新增权限'),
        (910138, N'因子开放范围编辑', 910121, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:factorCustomerScope:edit', N'#', N'因子开放范围编辑权限'),
        (910125, N'因子开放范围删除', 910121, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:factorCustomerScope:remove', N'#', N'因子开放范围删除权限'),
        (910113, N'模板库详情', 910105, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:reportTemplate:query', N'#', N'模板库详情权限'),
        (910114, N'模板库新增', 910105, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:reportTemplate:add', N'#', N'模板库新增权限'),
        (910115, N'模板库编辑', 910105, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:reportTemplate:edit', N'#', N'模板库编辑权限'),
        (910116, N'模板库删除', 910105, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:reportTemplate:remove', N'#', N'模板库删除权限'),
        (910117, N'模板分发详情', 910106, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:reportTemplateScope:query', N'#', N'模板分发详情权限'),
        (910139, N'模板分发新增', 910106, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:reportTemplateScope:add', N'#', N'模板分发新增权限'),
        (910140, N'模板分发编辑', 910106, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:reportTemplateScope:edit', N'#', N'模板分发编辑权限'),
        (910118, N'模板分发删除', 910106, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:reportTemplateScope:remove', N'#', N'模板分发删除权限'),
        (910157, N'报表内容详情', 910156, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:reportContent:query', N'#', N'报表内容详情权限'),
        (910158, N'报表内容新增', 910156, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:reportContent:add', N'#', N'报表内容新增权限'),
        (910159, N'报表内容编辑', 910156, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:reportContent:edit', N'#', N'报表内容编辑权限'),
        (910160, N'报表内容删除', 910156, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:reportContent:remove', N'#', N'报表内容删除权限'),
        (910132, N'维表详情', 910131, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:dimension:query', N'#', N'维表详情权限'),
        (910133, N'维表新增', 910131, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:dimension:add', N'#', N'维表新增权限'),
        (910134, N'维表编辑', 910131, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:dimension:edit', N'#', N'维表编辑权限'),
        (910135, N'维表删除', 910131, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:dimension:remove', N'#', N'维表删除权限'),
        (910127, N'公告详情', 910126, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:announcement:query', N'#', N'公告详情权限'),
        (910128, N'公告新增', 910126, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:announcement:add', N'#', N'公告新增权限'),
        (910129, N'公告编辑', 910126, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:announcement:edit', N'#', N'公告编辑权限'),
        (910130, N'公告删除', 910126, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:announcement:remove', N'#', N'公告删除权限'),
        (910119, N'续费订单详情', 910107, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:renewalOrder:query', N'#', N'续费订单详情权限'),
        (910141, N'续费订单新增', 910107, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:renewalOrder:add', N'#', N'续费订单新增权限'),
        (910142, N'续费订单编辑', 910107, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:renewalOrder:edit', N'#', N'续费订单编辑权限'),
        (910143, N'续费订单回调', 910107, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:renewalOrder:callback', N'#', N'续费订单回调权限'),
        (910144, N'续费订单重试签发', 910107, 5, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:renewalOrder:retryIssue', N'#', N'续费订单重试签发权限'),
        (910120, N'续费订单删除', 910107, 6, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'vendor:renewalOrder:remove', N'#', N'续费订单删除权限'),
        (1001, N'用户查询', 100, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:user:query', N'#', N'用户查询权限'),
        (1002, N'用户新增', 100, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:user:add', N'#', N'用户新增权限'),
        (1003, N'用户修改', 100, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:user:edit', N'#', N'用户修改权限'),
        (1004, N'用户删除', 100, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:user:remove', N'#', N'用户删除权限'),
        (1007, N'重置密码', 100, 7, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:user:resetPwd', N'#', N'重置密码权限'),
        (1008, N'角色查询', 101, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:role:query', N'#', N'角色查询权限'),
        (1009, N'角色新增', 101, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:role:add', N'#', N'角色新增权限'),
        (1010, N'角色修改', 101, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:role:edit', N'#', N'角色修改权限'),
        (1011, N'角色删除', 101, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:role:remove', N'#', N'角色删除权限'),
        (1017, N'部门查询', 103, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:dept:query', N'#', N'部门查询权限'),
        (1018, N'部门新增', 103, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:dept:add', N'#', N'部门新增权限'),
        (1019, N'部门修改', 103, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:dept:edit', N'#', N'部门修改权限'),
        (1020, N'部门删除', 103, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:dept:remove', N'#', N'部门删除权限'),
        (1021, N'岗位查询', 104, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:post:query', N'#', N'岗位查询权限'),
        (1022, N'岗位新增', 104, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:post:add', N'#', N'岗位新增权限'),
        (1023, N'岗位修改', 104, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:post:edit', N'#', N'岗位修改权限'),
        (1024, N'岗位删除', 104, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:post:remove', N'#', N'岗位删除权限'),
        (1036, N'公告查询', 107, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:notice:query', N'#', N'公告查询权限'),
        (1037, N'公告新增', 107, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:notice:add', N'#', N'公告新增权限'),
        (1038, N'公告修改', 107, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:notice:edit', N'#', N'公告修改权限'),
        (1039, N'公告删除', 107, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:notice:remove', N'#', N'公告删除权限'),
        (1040, N'操作日志查询', 500, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'monitor:operlog:query', N'#', N'操作日志查询权限'),
        (1041, N'操作日志删除', 500, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'monitor:operlog:remove', N'#', N'操作日志删除权限'),
        (1043, N'登录日志查询', 501, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'monitor:logininfor:query', N'#', N'登录日志查询权限'),
        (1044, N'登录日志删除', 501, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'monitor:logininfor:remove', N'#', N'登录日志删除权限'),
        (1611, N'套餐查询', 122, 1, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:tenantPackage:query', N'#', N'套餐查询权限'),
        (1612, N'套餐新增', 122, 2, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:tenantPackage:add', N'#', N'套餐新增权限'),
        (1613, N'套餐修改', 122, 3, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:tenantPackage:edit', N'#', N'套餐修改权限'),
        (1614, N'套餐删除', 122, 4, N'', N'', N'', 1, 0, N'F', N'0', N'0', N'system:tenantPackage:remove', N'#', N'套餐删除权限');

    INSERT INTO dbo.sys_menu
        (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type,
         visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
    SELECT menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type,
           visible, status, perms, icon, @createDept, @createBy, @now, NULL, NULL, remark
      FROM @menus;

    INSERT INTO dbo.sys_role_menu (role_id, menu_id)
    SELECT 1, menu_id FROM @menus;

    SELECT @menuIds = STRING_AGG(CONVERT(NVARCHAR(MAX), menu_id), N',') WITHIN GROUP (ORDER BY menu_id)
      FROM @menus
     WHERE visible = N'0' OR menu_type = N'F';

    INSERT INTO dbo.sys_tenant_package
        (package_id, package_name, menu_ids, remark, menu_check_strictly, price_amount, price_currency, billing_cycle,
         online_purchase_enabled, license_auto_issue_enabled, license_key_id, license_validity_days, license_feature_codes,
         license_template_entitlements, status, del_flag, create_dept, create_by, create_time, update_by, update_time)
    VALUES
        (1001, N'基础套餐', @menuIds, N'适用于单组织基础填报和 Source(A) 模板下载。', 1, 1999.00, N'CNY', N'YEAR',
         1, 0, NULL, NULL, N'capture,report-template-download',
         N'[{"templateCode":"source-a-emission-source-identification","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-emission-factor","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-report-content","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-activity-10101","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-activity-10102","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-activity-10103","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-activity-10104","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-green-power","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-intensity-dimension","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-denominator-fact","templateVersion":"source(A)","scope":"download"}]',
         N'0', N'0', @createDept, @createBy, @now, NULL, NULL),
        (1002, N'专业套餐', @menuIds, N'适用于多部门协同、因子同步和 Source(A) 报表模板同步。', 1, 0.00, N'CNY', N'YEAR',
         0, 0, NULL, NULL, N'capture,factor-sync,report-template-sync,report-template-download',
         N'[{"templateCode":"source-a-emission-source-identification","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-emission-factor","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-report-content","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-activity-10101","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-activity-10102","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-activity-10103","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-activity-10104","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-green-power","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-intensity-dimension","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-denominator-fact","templateVersion":"source(A)","scope":"download"}]',
         N'0', N'0', @createDept, @createBy, @now, NULL, NULL),
        (1003, N'集团套餐', @menuIds, N'适用于集团客户、报表门禁和 Source(A) 全量模板能力。', 1, 0.00, N'CNY', N'YEAR',
         0, 0, NULL, NULL, N'capture,factor-sync,report-template-sync,report-template-download,report-gate,premium-support',
         N'[{"templateCode":"source-a-emission-source-identification","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-emission-factor","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-report-content","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-activity-10101","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-activity-10102","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-activity-10103","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-activity-10104","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-green-power","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-intensity-dimension","templateVersion":"source(A)","scope":"download"},{"templateCode":"source-a-denominator-fact","templateVersion":"source(A)","scope":"download"}]',
         N'0', N'0', @createDept, @createBy, @now, NULL, NULL);

    INSERT INTO dbo.cv_signing_key
        (id, key_id, algorithm, public_key_pem, private_key_ref, key_status, valid_from, valid_to, create_time)
    VALUES
        (1, N'local-key-2026-06-08', N'RS256',
         N'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArb/hTFPic/UB/Y8FCyyTJe/Qqi822swUHsp8auQjYpzsq+DS0CYeAl8gFVaeOfjRx3PlaTJKP3DvYXi3+gyDQ165EcrNceQoOn7jeEr0Sy35pr5S7PlvDmk8AiL3e5MfU/jTjjQO+ueAQXstf4m6cf3p1jYsGvwYLJG5Qse1lfw6m9bU2XlkkaSAq6pUkvngSqSp1ai9B8lk6YsMv6zK',
         N'env:LOCAL_VENDOR_LICENSE_PRIVATE_KEY', N'active', '2026-01-01T00:00:00', NULL, @now);

    INSERT INTO dbo.cv_admin_division
        (id, division_code, division_name, parent_code, level_type, sort_order, status, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, N'110000', N'北京市', NULL, N'province', 1, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (2, N'120000', N'天津市', NULL, N'province', 2, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (3, N'130000', N'河北省', NULL, N'province', 3, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (4, N'140000', N'山西省', NULL, N'province', 4, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (5, N'150001', N'蒙西', NULL, N'province', 5, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (6, N'150002', N'蒙东', NULL, N'province', 6, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (7, N'210000', N'辽宁省', NULL, N'province', 7, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (8, N'220000', N'吉林省', NULL, N'province', 8, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (9, N'230000', N'黑龙江省', NULL, N'province', 9, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (10, N'310000', N'上海市', NULL, N'province', 10, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (11, N'320000', N'江苏省', NULL, N'province', 11, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (12, N'330000', N'浙江省', NULL, N'province', 12, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (13, N'340000', N'安徽省', NULL, N'province', 13, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (14, N'350000', N'福建省', NULL, N'province', 14, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (15, N'360000', N'江西省', NULL, N'province', 15, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (16, N'370000', N'山东省', NULL, N'province', 16, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (17, N'410000', N'河南省', NULL, N'province', 17, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (18, N'420000', N'湖北省', NULL, N'province', 18, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (19, N'430000', N'湖南省', NULL, N'province', 19, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (20, N'440000', N'广东省', NULL, N'province', 20, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (21, N'450000', N'广西壮族自治区', NULL, N'province', 21, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (22, N'460000', N'海南省', NULL, N'province', 22, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (23, N'500000', N'重庆市', NULL, N'province', 23, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (24, N'510000', N'四川省', NULL, N'province', 24, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (25, N'520000', N'贵州省', NULL, N'province', 25, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (26, N'530000', N'云南省', NULL, N'province', 26, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (27, N'610000', N'陕西省', NULL, N'province', 27, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (28, N'620000', N'甘肃省', NULL, N'province', 28, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (29, N'630000', N'青海省', NULL, N'province', 29, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (30, N'640000', N'宁夏回族自治区', NULL, N'province', 30, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (31, N'650000', N'新疆维吾尔自治区', NULL, N'province', 31, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)');

    INSERT INTO dbo.cv_base_year
        (id, base_year_key, base_year, is_current, description, sort_order, status, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, N'1', 2023, 1, N'国家 / ISSB / 双碳基准', 10, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (2, N'2', 2024, 0, N'备用', 20, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (3, N'3', 2025, 0, N'备用', 30, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (4, N'4', 2026, 0, N'备用', 40, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (5, N'5', 2027, 0, N'备用', 50, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (6, N'6', 2028, 0, N'备用', 60, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (7, N'7', 2029, 0, N'备用', 70, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (8, N'8', 2030, 0, N'备用', 80, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)');

    INSERT INTO dbo.cv_emission_source_category
        (id, category_code, business_key, category_name, category_name_en, ghg_scope, ghg_scope_category, iso_category, iso_category_en, iso_category_description, gb_scope_category, gb_subcategory, parent_code, effective_date, expire_date, current_flag, version_no, standard_category, category_current_key, sort_order, status, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, N'101', N'101', N'1.1 固定源燃烧', N'1.1 Stationary Combustion', N'范围1', N'1.1 固定源燃烧', N'类别1', N'Category 1', N'直接温室气体排放', N'1直接排放', N'化石燃料燃烧排放-固定燃烧源', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U1-固定燃烧', N'101', 101, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (2, N'102', N'102', N'1.2 移动源燃烧', N'1.2 Mobile Combustion', N'范围1', N'1.2 移动源燃烧', N'类别1', N'Category 1', N'直接温室气体排放', N'1直接排放', N'化石燃料燃烧排放-移动燃烧源', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U1-移动燃烧', N'102', 102, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (3, N'103', N'103', N'1.3 工艺排放', N'1.3 Process Emissions', N'范围1', N'1.3 工艺排放', N'类别1', N'Category 1', N'直接温室气体排放', N'1直接排放', N'过程排放-生产过程排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U1-工艺排放', N'103', 103, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (4, N'104', N'104', N'1.4 无组织逸散排放', N'1.4 Fugitive Emissions', N'范围1', N'1.4 无组织逸散排放', N'类别1', N'Category 1', N'直接温室气体排放', N'1直接排放', N'过程排放-逸散排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U1-逸散排放', N'104', 104, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (5, N'105', N'105', N'1.5 温室气体清除', N'1.5 Carbon Removals', N'范围1', N'1.5 温室气体清除', N'类别1', N'Category 1', N'直接温室气体排放', N'1直接排放', N'温室气体清除', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U1-碳清除', N'105', 105, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (6, N'201', N'201', N'2.1 外购电力', N'2.1 Purchased Electricity', N'范围2', N'2.1 外购电力', N'类别2', N'Category 2', N'输入能源的间接温室气体排放', N'2能源间接排放', N'购入的电力产生的排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U2-外购电力', N'201', 201, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (7, N'202', N'202', N'2.2 外购蒸汽', N'2.2 Purchased Steam', N'范围2', N'2.2 外购蒸汽', N'类别2', N'Category 2', N'输入能源的间接温室气体排放', N'2能源间接排放', N'购入的热力产生的排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U2-外购蒸汽', N'202', 202, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (8, N'203', N'203', N'2.3 外购热力', N'2.3 Purchased Heat', N'范围2', N'2.3 外购热力', N'类别2', N'Category 2', N'输入能源的间接温室气体排放', N'2能源间接排放', N'购入的热力产生的排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U2-外购热力', N'203', 203, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (9, N'204', N'204', N'2.4 外购冷量', N'2.4 Purchased Cooling', N'范围2', N'2.4 外购冷量', N'类别2', N'Category 2', N'输入能源的间接温室气体排放', N'2能源间接排放', N'购入的热力产生的排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U2-外购冷量', N'204', 204, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (10, N'301', N'301', N'3.1 采购的商品和服务', N'Category 1: Purchased Goods and Services', N'范围3', N'3.1 采购的商品和服务', N'类别4', N'Category 4', N'组织使用的产品或服务产生的间接温室气体排放', N'3其他间接排放', N'所使用的产品和服务隐含的排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-采购商品服务', N'301', 301, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (11, N'302', N'302', N'3.2 资本货物', N'Category 2: Capital Goods', N'范围3', N'3.2 资本货物', N'类别4', N'Category 4', N'组织使用的产品或服务产生的间接温室气体排放', N'3其他间接排放', N'所使用的产品和服务隐含的排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-资本货物', N'302', 302, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (12, N'303', N'303', N'3.3 燃料和能源相关活动', N'Category 3: Fuel and Energy Related Activities', N'范围3', N'3.3 燃料和能源相关活动', N'类别4', N'Category 4', N'组织使用的产品或服务产生的间接温室气体排放', N'3其他间接排放', N'所使用的产品和服务隐含的排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-燃料能源活动', N'303', 303, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (13, N'304', N'304', N'3.4 上游运输和配送', N'Category 4: Upstream Transportation and Distribution', N'范围3', N'3.4 上游运输和配送', N'类别3', N'Category 3', N'运输产生的间接温室气体排放', N'3其他间接排放', N'交通运输产生排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-上游运输配送', N'304', 304, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (14, N'305', N'305', N'3.5 运营中产生的废物', N'Category 5: Waste Generated in Operations', N'范围3', N'3.5 运营中产生的废物', N'类别6', N'Category 6', N'其他间接温室气体排放', N'3其他间接排放', N'废弃物处理处置排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-运营废物', N'305', 305, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (15, N'306', N'306', N'3.6 商务旅行', N'Category 6: Business Travel', N'范围3', N'3.6 商务旅行', N'类别3', N'Category 3', N'运输产生的间接温室气体排放', N'3其他间接排放', N'交通运输产生排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-商务旅行', N'306', 306, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (16, N'307', N'307', N'3.7 员工通勤', N'Category 7: Employee Commuting', N'范围3', N'3.7 员工通勤', N'类别3', N'Category 3', N'运输产生的间接温室气体排放', N'3其他间接排放', N'交通运输产生排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-员工通勤', N'307', 307, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (17, N'308', N'308', N'3.8 上游租赁资产', N'Category 8: Upstream Leased Assets', N'范围3', N'3.8 上游租赁资产', N'类别6', N'Category 6', N'其他间接温室气体排放', N'3其他间接排放', N'所使用的产品和服务隐含的排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-上游租赁', N'308', 308, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (18, N'309', N'309', N'3.9 下游运输和配送', N'Category 9: Downstream Transportation and Distribution', N'范围3', N'3.9 下游运输和配送', N'类别3', N'Category 3', N'运输产生的间接温室气体排放', N'3其他间接排放', N'交通运输产生排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-下游运输配送', N'309', 309, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (19, N'310', N'310', N'3.10 销售产品的加工', N'Category 10: Processing of Sold Products', N'范围3', N'3.10 销售产品的加工', N'类别5', N'Category 5', N'与使用组织产品相关的间接温室气体排放', N'3其他间接排放', N'所生产的产品和服务的排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-产品加工', N'310', 310, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (20, N'311', N'311', N'3.11 销售产品的使用', N'Category 11: Use of Sold Products', N'范围3', N'3.11 销售产品的使用', N'类别5', N'Category 5', N'与使用组织产品相关的间接温室气体排放', N'3其他间接排放', N'所生产的产品和服务的排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-产品使用', N'311', 311, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (21, N'312', N'312', N'3.12 销售产品的报废处理', N'Category 12: End-of-Life Treatment of Sold Products', N'范围3', N'3.12 销售产品的报废处理', N'类别5', N'Category 5', N'与使用组织产品相关的间接温室气体排放', N'3其他间接排放', N'所生产的产品和服务的排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-产品报废', N'312', 312, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (22, N'313', N'313', N'3.13 下游租赁资产', N'Category 13: Downstream Leased Assets', N'范围3', N'3.13 下游租赁资产', N'类别6', N'Category 6', N'其他间接温室气体排放', N'3其他间接排放', N'所生产的产品和服务的排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-下游租赁', N'313', 313, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (23, N'314', N'314', N'3.14 特许经营', N'Category 14: Franchises', N'范围3', N'3.14 特许经营', N'类别6', N'Category 6', N'其他间接温室气体排放', N'3其他间接排放', N'所生产的产品和服务的排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-特许经营', N'314', 314, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (24, N'315', N'315', N'3.15 投资', N'Category 15: Investments', N'范围3', N'3.15 投资', N'类别6', N'Category 6', N'其他间接温室气体排放', N'3其他间接排放', N'其他间接排放', NULL, '2024-01-01T00:00:00', '9999-12-31T00:00:00', N'1', N'1', N'U3-投资', N'315', 315, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)');

    INSERT INTO dbo.cv_electricity_factor_scope
        (id, scope_key, scope_name, sort_order, status, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, N'1', N'全国因子', 1, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (2, N'2', N'区域因子', 2, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (3, N'3', N'省级因子', 3, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)');

    INSERT INTO dbo.cv_electricity_factor_version
        (id, factor_version, effective_year, sort_order, status, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, N'2022', 2023, 1, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (2, N'2023', 2025, 2, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)');

    INSERT INTO dbo.cv_electricity_factor
        (id, factor_version, division_code, division_name, region_name, province_factor, region_factor, national_factor, non_fossil_excluded_factor, national_fossil_power_factor, sort_order, status, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, 2022, N'110000', N'北京市', N'华北', 0.558, 0.6776, 0.5366, 0.5856, 0.8325, 1, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (2, 2022, N'120000', N'天津市', N'华北', 0.7041, 0.6776, 0.5366, 0.5856, 0.8325, 2, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (3, 2022, N'130000', N'河北省', N'华北', 0.7252, 0.6776, 0.5366, 0.5856, 0.8325, 3, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (4, 2022, N'140000', N'山西省', N'华北', 0.7096, 0.6776, 0.5366, 0.5856, 0.8325, 4, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (5, 2022, N'150001', N'蒙西', N'华北', 0.6849, 0.6776, 0.5366, 0.5856, 0.8325, 5, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (6, 2022, N'150002', N'蒙东', N'东北', 0.6849, 0.5564, 0.5366, 0.5856, 0.8325, 6, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (7, 2022, N'210000', N'辽宁省', N'东北', 0.5626, 0.5564, 0.5366, 0.5856, 0.8325, 7, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (8, 2022, N'220000', N'吉林省', N'东北', 0.4932, 0.5564, 0.5366, 0.5856, 0.8325, 8, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (9, 2022, N'230000', N'黑龙江省', N'东北', 0.5368, 0.5564, 0.5366, 0.5856, 0.8325, 9, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (10, 2022, N'310000', N'上海市', N'华东', 0.5849, 0.5617, 0.5366, 0.5856, 0.8325, 10, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (11, 2022, N'320000', N'江苏省', N'华东', 0.5978, 0.5617, 0.5366, 0.5856, 0.8325, 11, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (12, 2022, N'330000', N'浙江省', N'华东', 0.5153, 0.5617, 0.5366, 0.5856, 0.8325, 12, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (13, 2022, N'340000', N'安徽省', N'华东', 0.6782, 0.5617, 0.5366, 0.5856, 0.8325, 13, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (14, 2022, N'350000', N'福建省', N'华东', 0.4092, 0.5617, 0.5366, 0.5856, 0.8325, 14, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (15, 2022, N'360000', N'江西省', N'华中', 0.5752, 0.5395, 0.5366, 0.5856, 0.8325, 15, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (16, 2022, N'370000', N'山东省', N'华北', 0.641, 0.6776, 0.5366, 0.5856, 0.8325, 16, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (17, 2022, N'410000', N'河南省', N'华中', 0.6058, 0.5395, 0.5366, 0.5856, 0.8325, 17, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (18, 2022, N'420000', N'湖北省', N'华中', 0.4364, 0.5395, 0.5366, 0.5856, 0.8325, 18, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (19, 2022, N'430000', N'湖南省', N'华中', 0.49, 0.5395, 0.5366, 0.5856, 0.8325, 19, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (20, 2022, N'440000', N'广东省', N'南方', 0.4403, 0.3869, 0.5366, 0.5856, 0.8325, 20, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (21, 2022, N'450000', N'广西壮族自治区', N'南方', 0.4044, 0.3869, 0.5366, 0.5856, 0.8325, 21, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (22, 2022, N'460000', N'海南省', N'南方', 0.4184, 0.3869, 0.5366, 0.5856, 0.8325, 22, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (23, 2022, N'500000', N'重庆市', N'西南', 0.5227, 0.2268, 0.5366, 0.5856, 0.8325, 23, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (24, 2022, N'510000', N'四川省', N'西南', 0.1404, 0.2268, 0.5366, 0.5856, 0.8325, 24, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (25, 2022, N'520000', N'贵州省', N'南方', 0.4989, 0.3869, 0.5366, 0.5856, 0.8325, 25, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (26, 2022, N'530000', N'云南省', N'南方', 0.1073, 0.3869, 0.5366, 0.5856, 0.8325, 26, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (27, 2022, N'610000', N'陕西省', N'西北', 0.6558, 0.5857, 0.5366, 0.5856, 0.8325, 27, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (28, 2022, N'620000', N'甘肃省', N'西北', 0.4772, 0.5857, 0.5366, 0.5856, 0.8325, 28, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (29, 2022, N'630000', N'青海省', N'西北', 0.1567, 0.5857, 0.5366, 0.5856, 0.8325, 29, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (30, 2022, N'640000', N'宁夏回族自治区', N'西北', 0.6423, 0.5857, 0.5366, 0.5856, 0.8325, 30, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (31, 2022, N'650000', N'新疆维吾尔自治区', N'西北', 0.6231, 0.5857, 0.5366, 0.5856, 0.8325, 31, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (32, 2023, N'110000', N'北京市', N'华北', 0.5554, 0.6361, 0.5306, 0.6096, 0.8273, 32, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (33, 2023, N'120000', N'天津市', N'华北', 0.6796, 0.6361, 0.5306, 0.6096, 0.8273, 33, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (34, 2023, N'130000', N'河北省', N'华北', 0.6516, 0.6361, 0.5306, 0.6096, 0.8273, 34, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (35, 2023, N'140000', N'山西省', N'华北', 0.6634, 0.6361, 0.5306, 0.6096, 0.8273, 35, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (36, 2023, N'150001', N'蒙西', N'华北', 0.6479, 0.6361, 0.5306, 0.6096, 0.8273, 36, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (37, 2023, N'150002', N'蒙东', N'东北', 0.6479, 0.5122, 0.5306, 0.6096, 0.8273, 37, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (38, 2023, N'210000', N'辽宁省', N'东北', 0.4878, 0.5122, 0.5306, 0.6096, 0.8273, 38, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (39, 2023, N'220000', N'吉林省', N'东北', 0.4671, 0.5122, 0.5306, 0.6096, 0.8273, 39, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (40, 2023, N'230000', N'黑龙江省', N'东北', 0.5229, 0.5122, 0.5306, 0.6096, 0.8273, 40, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (41, 2023, N'310000', N'上海市', N'华东', 0.5737, 0.55, 0.5306, 0.6096, 0.8273, 41, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (42, 2023, N'320000', N'江苏省', N'华东', 0.5827, 0.55, 0.5306, 0.6096, 0.8273, 42, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (43, 2023, N'330000', N'浙江省', N'华东', 0.4974, 0.55, 0.5306, 0.6096, 0.8273, 43, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (44, 2023, N'340000', N'安徽省', N'华东', 0.6553, 0.55, 0.5306, 0.6096, 0.8273, 44, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (45, 2023, N'350000', N'福建省', N'华东', 0.4211, 0.55, 0.5306, 0.6096, 0.8273, 45, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (46, 2023, N'360000', N'江西省', N'华中', 0.5836, 0.5271, 0.5306, 0.6096, 0.8273, 46, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (47, 2023, N'370000', N'山东省', N'华北', 0.6191, 0.6361, 0.5306, 0.6096, 0.8273, 47, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (48, 2023, N'410000', N'河南省', N'华中', 0.5897, 0.5271, 0.5306, 0.6096, 0.8273, 48, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (49, 2023, N'420000', N'湖北省', N'华中', 0.4044, 0.5271, 0.5306, 0.6096, 0.8273, 49, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (50, 2023, N'430000', N'湖南省', N'华中', 0.4976, 0.5271, 0.5306, 0.6096, 0.8273, 50, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (51, 2023, N'440000', N'广东省', N'南方', 0.4419, 0.4042, 0.5306, 0.6096, 0.8273, 51, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (52, 2023, N'450000', N'广西壮族自治区', N'南方', 0.4476, 0.4042, 0.5306, 0.6096, 0.8273, 52, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (53, 2023, N'460000', N'海南省', N'南方', 0.3648, 0.4042, 0.5306, 0.6096, 0.8273, 53, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (54, 2023, N'500000', N'重庆市', N'西南', 0.5581, 0.2472, 0.5306, 0.6096, 0.8273, 54, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (55, 2023, N'510000', N'四川省', N'西南', 0.1564, 0.2472, 0.5306, 0.6096, 0.8273, 55, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (56, 2023, N'520000', N'贵州省', N'南方', 0.5683, 0.4042, 0.5306, 0.6096, 0.8273, 56, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (57, 2023, N'530000', N'云南省', N'南方', 0.1333, 0.4042, 0.5306, 0.6096, 0.8273, 57, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (58, 2023, N'610000', N'陕西省', N'西北', 0.6335, 0.5543, 0.5306, 0.6096, 0.8273, 58, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (59, 2023, N'620000', N'甘肃省', N'西北', 0.4471, 0.5543, 0.5306, 0.6096, 0.8273, 59, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (60, 2023, N'630000', N'青海省', N'西北', 0.1796, 0.5543, 0.5306, 0.6096, 0.8273, 60, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (61, 2023, N'640000', N'宁夏回族自治区', N'西北', 0.6187, 0.5543, 0.5306, 0.6096, 0.8273, 61, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (62, 2023, N'650000', N'新疆维吾尔自治区', N'西北', 0.6021, 0.5543, 0.5306, 0.6096, 0.8273, 62, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)');

    INSERT INTO dbo.cv_greenhouse_gas
        (id, gas_code, gas_name, gas_name_en, gwp_value, gwp_version, chemical_formula, sort_order, status, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, 1, N'CO2', NULL, NULL, N'source(A)', 1, 1, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (2, 2, N'CH4', NULL, NULL, N'source(A)', 2, 2, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (3, 3, N'N2O', NULL, NULL, N'source(A)', 3, 3, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (4, 4, N'HFCs', NULL, NULL, N'source(A)', 4, 4, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (5, 5, N'PFCs', NULL, NULL, N'source(A)', 5, 5, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (6, 6, N'SF6', NULL, NULL, N'source(A)', 6, 6, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (7, 7, N'NF3', NULL, NULL, N'source(A)', 7, 7, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)');

    INSERT INTO dbo.cv_factor_version
        (id, version_code, version_name, publish_status, frozen_flag, published_by, published_time, create_time, remark)
    VALUES
        (1, N'source-a', N'Source(A) 排放因子库', N'published', 1, N'admin', @now, @now, N'source(A)');

    INSERT INTO dbo.cv_factor_record
        (id, version_id, factor_table_code, factor_code, factor_name, factor_category, factor_value, factor_unit, factor_key, emission_source_name, emission_source_name_en, fuel_material_category, source_unit, co2, ch4, n2o, hfcs, pfcs, sf6, nf3, applicable_scope, factor_source, gwp_ch4, gwp_n2o, gwp_hfcs, gwp_pfcs, gwp_sf6, gwp_nf3, factor_gwp, version_province_code, factor_version, division_code, division_name, region_name, province_factor, region_factor, national_factor, non_fossil_excluded_factor, national_fossil_power_factor, row_no, fuel_level1, fuel_level2, fuel_level3, fuel_level4, lower_heat_value, lower_heat_value_cv, co2_factor, co2_factor_cv, gwp_value, converted_factor, source_ref, custom_fields, enabled_flag, create_time, update_time, remark)
    VALUES
        (1, 1, N'201ef', N'1', N'天然气', N'化石燃料-气体', 7.966180699999999, N'kgCO2e/m³', N'1', N'天然气', N'Natural Gas', N'化石燃料-气体', N'm³', 2.0369492, 0.178055, 0.0035611, NULL, NULL, NULL, NULL, N'范围1', N'国家温室气体排放因子数据库', 28, 265, NULL, NULL, 23500, 17200, 7.966180699999999, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 7.966180699999999, 7.966180699999999, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (2, 1, N'201ef', N'2', N'柴油', N'化石燃料-液体', 3.1108000000000002, N'kgCO2e/kg', N'2', N'柴油', N'Diesel', N'化石燃料-液体', N'kg', 3.096, 0.00015, 0.00004, NULL, NULL, NULL, NULL, N'范围1', N'IPCC 2006', 28, 265, NULL, NULL, 23500, 17200, 3.1108000000000002, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 3.1108000000000002, 3.1108000000000002, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (3, 1, N'201ef', N'3', N'汽油', N'化石燃料-液体', 2.9532, N'kgCO2e/kg', N'3', N'汽油', N'Gasoline', N'化石燃料-液体', N'kg', 2.925, 0.00025, 0.00008, NULL, NULL, NULL, NULL, N'范围1', N'IPCC 2006', 28, 265, NULL, NULL, 23500, 17200, 2.9532, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 2.9532, 2.9532, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (4, 1, N'201ef', N'4', N'一般烟煤', N'化石燃料-固体', 1.9045550000000002, N'kgCO2e/kg', N'4', N'一般烟煤', N'Bituminous Coal', N'化石燃料-固体', N'kg', 1.9003, 0.00001, 0.000015, NULL, NULL, NULL, NULL, N'范围1', N'中国温室气体清单', 28, 265, NULL, NULL, 23500, 17200, 1.9045550000000002, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1.9045550000000002, 1.9045550000000002, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (5, 1, N'201ef', N'5', N'废轮胎', N'替代燃料', 2.8528000000000002, N'kgCO2e/kg', N'5', N'废轮胎', N'Waste Tyres', N'替代燃料', N'kg', 2.85, 0.0001, NULL, NULL, NULL, NULL, NULL, N'范围1', N'IPCC 2006', 28, 265, NULL, NULL, 23500, 17200, 2.8528000000000002, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 2.8528000000000002, 2.8528000000000002, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (6, 1, N'201ef', N'6', N'废塑料', N'替代燃料', 2.7328, N'kgCO2e/kg', N'6', N'废塑料', N'Waste Plastics', N'替代燃料', N'kg', 2.73, 0.0001, NULL, NULL, NULL, NULL, NULL, N'范围1', N'IPCC 2006', 28, 265, NULL, NULL, 23500, 17200, 2.7328, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 2.7328, 2.7328, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (7, 1, N'201ef', N'7', N'工艺过程', N'工艺排放', 1, N'kgCO2e/kg', N'7', N'工艺过程', N'Process Emissions', N'工艺排放', N'kg', 1, NULL, NULL, NULL, NULL, NULL, NULL, N'范围1', N'实测/物料平衡', 28, 265, NULL, NULL, 23500, 17200, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 1, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (8, 1, N'201ef', N'8', N'石灰石分解', N'工艺排放', 0.44, N'kgCO2e/kg石灰石', N'8', N'石灰石分解', N'Ite Decomposition', N'工艺排放', N'kg石灰石', 0.44, NULL, NULL, NULL, NULL, NULL, NULL, N'范围1', N'IPCC 2006', 28, 265, NULL, NULL, 23500, 17200, 0.44, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 8, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.44, 0.44, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (9, 1, N'201ef', N'9', N'R410A', N'制冷剂-HFC', 2088, N'kgCO2e/kg', N'9', N'R410A', N'R410A (HFC)', N'制冷剂-HFC', N'kg', NULL, NULL, NULL, 1, NULL, NULL, NULL, N'范围1', N'IPCC AR5', 28, 265, 2088, NULL, 23500, 17200, 2088, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 2088, 2088, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (10, 1, N'201ef', N'10', N'R407C', N'制冷剂-HFC', 1774, N'kgCO2e/kg', N'10', N'R407C', N'R407C (HFC)', N'制冷剂-HFC', N'kg', NULL, NULL, NULL, 1, NULL, NULL, NULL, N'范围1', N'IPCC AR5', 28, 265, 1774, NULL, 23500, 17200, 1774, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1774, 1774, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (11, 1, N'201ef', N'11', N'R507A', N'制冷剂-HFC', 3985, N'kgCO2e/kg', N'11', N'R507A', N'R507A (HFC)', N'制冷剂-HFC', N'kg', NULL, NULL, NULL, 1, NULL, NULL, NULL, N'范围1', N'IPCC AR5', 28, 265, 3985, NULL, 23500, 17200, 3985, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 3985, 3985, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (12, 1, N'201ef', N'12', N'R134a', N'制冷剂-HFC', 1430, N'kgCO2e/kg', N'12', N'R134a', N'R134a (HFC)', N'制冷剂-HFC', N'kg', NULL, NULL, NULL, 1, NULL, NULL, NULL, N'范围1', N'IPCC AR5', 28, 265, 1430, NULL, 23500, 17200, 1430, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1430, 1430, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (13, 1, N'201ef', N'13', N'HFC-227ea', N'灭火剂-HFC', 3220, N'kgCO2e/kg', N'13', N'HFC-227ea', N'HFC-227ea', N'灭火剂-HFC', N'kg', NULL, NULL, NULL, 1, NULL, NULL, NULL, N'范围1', N'IPCC AR5', 28, 265, 3220, NULL, 23500, 17200, 3220, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 3220, 3220, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (14, 1, N'201ef', N'14', N'SF6', N'绝缘气体', 23500, N'kgCO2e/m³', N'14', N'SF6', N'Sulfur Hexafluoride', N'绝缘气体', N'm³', NULL, NULL, NULL, NULL, NULL, 1, NULL, N'范围1', N'IPCC AR5', 28, 265, NULL, NULL, 23500, 17200, 23500, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 14, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 23500, 23500, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (15, 1, N'201ef', N'15', N'灭火剂', N'灭火剂', 1, N'kgCO2e/kg', N'15', N'灭火剂', N'Carbon Dioxide', N'灭火剂', N'kg', 1, NULL, NULL, NULL, NULL, NULL, NULL, N'范围1', N'直接排放', 28, 265, NULL, NULL, 23500, 17200, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 15, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 1, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (16, 1, N'201ef', N'16', N'生物分解', N'生物分解', 28, N'kgCO2e/kg', N'16', N'生物分解', N'Methane', N'生物分解', N'kg', NULL, 1, NULL, NULL, NULL, NULL, NULL, N'范围1', N'IPCC AR5', 28, 265, NULL, NULL, 23500, 17200, 28, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 16, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 28, 28, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (17, 1, N'201ef', N'17', N'蒸汽', N'外购能源', 0.11, N'kgCO2e/MJ', N'17', N'蒸汽', N'Steam', N'外购能源', N'MJ', 0.11, NULL, NULL, NULL, NULL, NULL, NULL, N'范围2', N'地区供热', 28, 265, NULL, NULL, 23500, 17200, 0.11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 17, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.11, 0.11, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (18, 1, N'201ef', N'18', N'天然气上游', N'上游能源', 0.35, N'kgCO2e/m³', N'18', N'天然气上游', N'Upstream Natural Gas', N'上游能源', N'm³', 0.35, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'IPCC 2006', 28, 265, NULL, NULL, 23500, 17200, 0.35, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 18, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.35, 0.35, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (19, 1, N'201ef', N'19', N'一般烟煤上游', N'上游能源', 0.48000000000000004, N'kgCO2e/kg', N'19', N'一般烟煤上游', N'Upstream Bituminous Coal', N'上游能源', N'kg', 0.2, 0.01, NULL, NULL, NULL, NULL, NULL, N'范围3', N'IPCC 2006', 28, 265, NULL, NULL, 23500, 17200, 0.48000000000000004, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 19, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.48000000000000004, 0.48000000000000004, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (20, 1, N'201ef', N'20', N'公路货运-轻型汽油(2t)', N'运输-公路', 0.334, N'kgCO2e/t·km', N'20', N'公路货运-轻型汽油(2t)', N'Road Freight-Light Gasoline(2t)', N'运输-公路', N't·km', 0.334, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.334, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 20, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.334, 0.334, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (21, 1, N'201ef', N'21', N'公路货运-中型汽油(8t)', N'运输-公路', 0.115, N'kgCO2e/t·km', N'21', N'公路货运-中型汽油(8t)', N'Road Freight-Medium Gasoline(8t)', N'运输-公路', N't·km', 0.115, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.115, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.115, 0.115, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (22, 1, N'201ef', N'22', N'公路货运-重型汽油(10t)', N'运输-公路', 0.104, N'kgCO2e/t·km', N'22', N'公路货运-重型汽油(10t)', N'Road Freight-Heavy Gasoline(10t)', N'运输-公路', N't·km', 0.104, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.104, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 22, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.104, 0.104, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (23, 1, N'201ef', N'23', N'公路货运-重型汽油(18t)', N'运输-公路', 0.104, N'kgCO2e/t·km', N'23', N'公路货运-重型汽油(18t)', N'Road Freight-Heavy Gasoline(18t)', N'运输-公路', N't·km', 0.104, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.104, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 23, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.104, 0.104, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (24, 1, N'201ef', N'24', N'公路货运-轻型柴油(2t)', N'运输-公路', 0.286, N'kgCO2e/t·km', N'24', N'公路货运-轻型柴油(2t)', N'Road Freight-Light Diesel(2t)', N'运输-公路', N't·km', 0.286, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.286, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 24, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.286, 0.286, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (25, 1, N'201ef', N'25', N'公路货运-中型柴油(8t)', N'运输-公路', 0.179, N'kgCO2e/t·km', N'25', N'公路货运-中型柴油(8t)', N'Road Freight-Medium Diesel(8t)', N'运输-公路', N't·km', 0.179, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.179, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 25, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.179, 0.179, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (26, 1, N'201ef', N'26', N'公路货运-重型柴油(10t)', N'运输-公路', 0.162, N'kgCO2e/t·km', N'26', N'公路货运-重型柴油(10t)', N'Road Freight-Heavy Diesel(10t)', N'运输-公路', N't·km', 0.162, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.162, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 26, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.162, 0.162, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (27, 1, N'201ef', N'27', N'公路货运-重型柴油(18t)', N'运输-公路', 0.129, N'kgCO2e/t·km', N'27', N'公路货运-重型柴油(18t)', N'Road Freight-Heavy Diesel(18t)', N'运输-公路', N't·km', 0.129, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.129, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 27, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.129, 0.129, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (28, 1, N'201ef', N'28', N'公路货运-重型柴油(30t)', N'运输-公路', 0.078, N'kgCO2e/t·km', N'28', N'公路货运-重型柴油(30t)', N'Road Freight-Heavy Diesel(30t)', N'运输-公路', N't·km', 0.078, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.078, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 28, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.078, 0.078, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (29, 1, N'201ef', N'29', N'公路货运-重型柴油(46t)', N'运输-公路', 0.057, N'kgCO2e/t·km', N'29', N'公路货运-重型柴油(46t)', N'Road Freight-Heavy Diesel(46t)', N'运输-公路', N't·km', 0.057, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.057, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 29, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.057, 0.057, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (30, 1, N'201ef', N'30', N'铁路货运', N'运输-铁路', 0.01, N'kgCO2e/t·km', N'30', N'铁路货运', N'Rail Freight', N'运输-铁路', N't·km', 0.01, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.01, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 30, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.01, 0.01, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (31, 1, N'201ef', N'31', N'电力机车货运', N'运输-铁路', 0.01, N'kgCO2e/t·km', N'31', N'电力机车货运', N'Electric Locomotive Freight', N'运输-铁路', N't·km', 0.01, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.01, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 31, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.01, 0.01, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (32, 1, N'201ef', N'32', N'内燃机车货运', N'运输-铁路', 0.011, N'kgCO2e/t·km', N'32', N'内燃机车货运', N'Diesel Locomotive Freight', N'运输-铁路', N't·km', 0.011, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CLCD 0.8', 28, 265, NULL, NULL, 23500, 17200, 0.011, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 32, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.011, 0.011, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (33, 1, N'201ef', N'33', N'海运货运', N'运输-海运', 0.012, N'kgCO2e/t·km', N'33', N'海运货运', N'Sea Freight', N'运输-海运', N't·km', 0.012, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.012, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 33, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.012, 0.012, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (34, 1, N'201ef', N'34', N'液货船运', N'运输-海运', 0.019, N'kgCO2e/t·km', N'34', N'液货船运', N'Tanker Freight', N'运输-海运', N't·km', 0.019, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.019, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 34, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.019, 0.019, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (35, 1, N'201ef', N'35', N'干散货船运', N'运输-海运', 0.015, N'kgCO2e/t·km', N'35', N'干散货船运', N'Bulk Carrier Freight', N'运输-海运', N't·km', 0.015, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.015, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 35, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.015, 0.015, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (36, 1, N'201ef', N'36', N'集装箱船运', N'运输-海运', 0.012, N'kgCO2e/t·km', N'36', N'集装箱船运', N'Container Ship Freight', N'运输-海运', N't·km', 0.012, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.012, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 36, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.012, 0.012, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (37, 1, N'201ef', N'37', N'大型飞机货运', N'运输-航空', 0.096, N'kgCO2e/t·km', N'37', N'大型飞机货运', N'Large Aircraft Freight', N'运输-航空', N't·km', 0.096, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 0.096, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 37, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.096, 0.096, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (38, 1, N'201ef', N'38', N'中型飞机货运', N'运输-航空', 1.15, N'kgCO2e/t·km', N'38', N'中型飞机货运', N'Medium Aircraft Freight', N'运输-航空', N't·km', 1.15, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 1.15, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 38, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1.15, 1.15, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (39, 1, N'201ef', N'39', N'小型飞机货运', N'运输-航空', 1.47, N'kgCO2e/t·km', N'39', N'小型飞机货运', N'Small Aircraft Freight', N'运输-航空', N't·km', 1.47, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'CPCD', 28, 265, NULL, NULL, 23500, 17200, 1.47, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 39, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1.47, 1.47, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (40, 1, N'201ef', N'40', N'航空客运', N'运输-航空', 0.255056, N'kgCO2e/人·km', N'40', N'航空客运', N'Aviation Passenger', N'运输-航空', N'人·km', 0.255, 0.000002, NULL, NULL, NULL, NULL, NULL, N'范围3', N'ICAO', 28, 265, NULL, NULL, 23500, 17200, 0.255056, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 40, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.255056, 0.255056, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (41, 1, N'201ef', N'41', N'铁路客运', N'运输-铁路', 0.033, N'kgCO2e/人·km', N'41', N'铁路客运', N'Rail Passenger', N'运输-铁路', N'人·km', 0.033, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'中国铁路', 28, 265, NULL, NULL, 23500, 17200, 0.033, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 41, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.033, 0.033, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (42, 1, N'201ef', N'42', N'酒店住宿', N'商务旅行', 20.9, N'kgCO2e/间夜', N'42', N'酒店住宿', N'Hotel Accommodation', N'商务旅行', N'间夜', 20.9, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'行业平均', 28, 265, NULL, NULL, 23500, 17200, 20.9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 42, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 20.9, 20.9, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (43, 1, N'201ef', N'43', N'通勤-汽油', N'员工通勤', 0.21, N'kgCO2e/人·km', N'43', N'通勤-汽油', N'Commuting-Gasoline', N'员工通勤', N'人·km', 0.21, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'IPCC 2006', 28, 265, NULL, NULL, 23500, 17200, 0.21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 43, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.21, 0.21, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (44, 1, N'201ef', N'44', N'通勤-柴油', N'员工通勤', 0.08, N'kgCO2e/人·km', N'44', N'通勤-柴油', N'Commuting-Diesel', N'员工通勤', N'人·km', 0.08, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'IPCC 2006', 28, 265, NULL, NULL, 23500, 17200, 0.08, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 44, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.08, 0.08, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (45, 1, N'201ef', N'45', N'通勤-综合', N'员工通勤', 0.05, N'kgCO2e/人·km', N'45', N'通勤-综合', N'Commuting-Mixed', N'员工通勤', N'人·km', 0.05, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'综合估算', 28, 265, NULL, NULL, 23500, 17200, 0.05, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 45, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.05, 0.05, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (46, 1, N'201ef', N'46', N'固废-填埋', N'废弃物处理', 1.9000000000000001, N'kgCO2e/kg', N'46', N'固废-填埋', N'MSW-Landfill', N'废弃物处理', N'kg', 0.5, 0.05, NULL, NULL, NULL, NULL, NULL, N'范围3', N'IPCC 2006', 28, 265, NULL, NULL, 23500, 17200, 1.9000000000000001, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 46, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1.9000000000000001, 1.9000000000000001, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (47, 1, N'201ef', N'47', N'工业固废', N'废弃物处理', 0.02, N'kgCO2e/kg', N'47', N'工业固废', N'Industrial Waste', N'废弃物处理', N'kg', 0.02, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'IPCC 2006', 28, 265, NULL, NULL, 23500, 17200, 0.02, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 47, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.02, 0.02, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (48, 1, N'201ef', N'48', N'危废处置', N'废弃物处理', 2.5, N'kgCO2e/kg', N'48', N'危废处置', N'Hazardous Waste', N'废弃物处理', N'kg', 2.5, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'IPCC 2006', 28, 265, NULL, NULL, 23500, 17200, 2.5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 48, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 2.5, 2.5, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (49, 1, N'201ef', N'49', N'废水处理', N'废弃物处理', 1.2650000000000001, N'kgCO2e/m³', N'49', N'废水处理', N'Wastewater Treatment', N'废弃物处理', N'm³', 0.3, 0.025, 0.001, NULL, NULL, NULL, NULL, N'范围3', N'IPCC 2006', 28, 265, NULL, NULL, 23500, 17200, 1.2650000000000001, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 49, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1.2650000000000001, 1.2650000000000001, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (50, 1, N'201ef', N'50', N'粉尘', N'废弃物处理', 0.01, N'kgCO2e/kg', N'50', N'粉尘', N'Dust', N'废弃物处理', N'kg', 0.01, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'行业估算', 28, 265, NULL, NULL, 23500, 17200, 0.01, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 50, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.01, 0.01, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (51, 1, N'201ef', N'51', N'电子废弃物', N'废弃物处理', 1.5, N'kgCO2e/kg', N'51', N'电子废弃物', N'E-waste', N'废弃物处理', N'kg', 1.5, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'WEEE指令', 28, 265, NULL, NULL, 23500, 17200, 1.5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 51, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1.5, 1.5, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (52, 1, N'201ef', N'52', N'建筑废弃物', N'废弃物处理', 0.015, N'kgCO2e/kg', N'52', N'建筑废弃物', N'Construction Waste', N'废弃物处理', N'kg', 0.015, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'行业估算', 28, 265, NULL, NULL, 23500, 17200, 0.015, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 52, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.015, 0.015, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (53, 1, N'201ef', N'53', N'采购-工业硅', N'采购-原材料', 11.5, N'kgCO2e/kg', N'53', N'采购-工业硅', N'Purchased-Industrial Silicon', N'采购-原材料', N'kg', 11.5, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'行业LCA', 28, 265, NULL, NULL, 23500, 17200, 11.5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 53, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 11.5, 11.5, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (54, 1, N'201ef', N'54', N'采购-化学品', N'采购-化学品', 2.5, N'kgCO2e/kg', N'54', N'采购-化学品', N'Purchased-Chemicals', N'采购-化学品', N'kg', 2.5, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'行业平均', 28, 265, NULL, NULL, 23500, 17200, 2.5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 54, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 2.5, 2.5, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (55, 1, N'201ef', N'55', N'采购-原材料', N'采购-原材料', 0.05, N'kgCO2e/kg', N'55', N'采购-原材料', N'Purchased-Raw Materials', N'采购-原材料', N'kg', 0.05, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'行业平均', 28, 265, NULL, NULL, 23500, 17200, 0.05, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 55, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.05, 0.05, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (56, 1, N'201ef', N'56', N'采购-一般烟煤', N'采购-燃料', 0.2, N'kgCO2e/kg', N'56', N'采购-一般烟煤', N'Purchased-Bituminous Coal', N'采购-燃料', N'kg', 0.2, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'IPCC 2006', 28, 265, NULL, NULL, 23500, 17200, 0.2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 56, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.2, 0.2, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (57, 1, N'201ef', N'57', N'采购-石灰石', N'采购-原材料', 0.012, N'kgCO2e/kg', N'57', N'采购-石灰石', N'Purchased-Limestone', N'采购-原材料', N'kg', 0.012, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'行业平均', 28, 265, NULL, NULL, 23500, 17200, 0.012, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 57, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.012, 0.012, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (58, 1, N'201ef', N'58', N'采购-润滑油', N'采购-辅材', 1.2, N'kgCO2e/kg', N'58', N'采购-润滑油', N'Purchased-Lubricants', N'采购-辅材', N'kg', 1.2, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'行业平均', 28, 265, NULL, NULL, 23500, 17200, 1.2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 58, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1.2, 1.2, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (59, 1, N'201ef', N'59', N'采购-包装材料', N'采购-辅材', 1.8, N'kgCO2e/kg', N'59', N'采购-包装材料', N'Purchased-Packaging', N'采购-辅材', N'kg', 1.8, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'行业平均', 28, 265, NULL, NULL, 23500, 17200, 1.8, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 59, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1.8, 1.8, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (60, 1, N'201ef', N'60', N'采购-办公用品', N'采购-办公', 3, N'kgCO2e/kg', N'60', N'采购-办公用品', N'Purchased-Office Supplies', N'采购-办公', N'kg', 3, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'行业平均', 28, 265, NULL, NULL, 23500, 17200, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 60, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 3, 3, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (61, 1, N'201ef', N'61', N'采购-电子设备', N'采购-办公', 150, N'kgCO2e/台', N'61', N'采购-电子设备', N'Purchased-Electronics', N'采购-办公', N'台', 150, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'行业LCA', 28, 265, NULL, NULL, 23500, 17200, 150, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 61, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 150, 150, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (62, 1, N'201ef', N'62', N'采购-蒸汽', N'采购-服务', 0.05, N'kgCO2e/MJ', N'62', N'采购-蒸汽', N'Purchased-Steam Service', N'采购-服务', N'MJ', 0.05, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'供热行业', 28, 265, NULL, NULL, 23500, 17200, 0.05, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 62, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.05, 0.05, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (63, 1, N'201ef', N'63', N'采购-综合', N'采购-综合', 1, N'kgCO2e/元', N'63', N'采购-综合', N'Purchased-General', N'采购-综合', N'元', 1, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'IO-LCA', 28, 265, NULL, NULL, 23500, 17200, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 63, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 1, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (64, 1, N'201ef', N'64', N'资本-设备', N'资本货物', 2.5, N'kgCO2e/kg', N'64', N'资本-设备', N'Capital-Equipment', N'资本货物', N'kg', 2.5, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'行业LCA', 28, 265, NULL, NULL, 23500, 17200, 2.5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 64, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 2.5, 2.5, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (65, 1, N'201ef', N'65', N'资本-车辆', N'资本货物', 6000, N'kgCO2e/辆', N'65', N'资本-车辆', N'Capital-Vehicle', N'资本货物', N'辆', 6000, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'行业LCA', 28, 265, NULL, NULL, 23500, 17200, 6000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 65, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 6000, 6000, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (66, 1, N'201ef', N'66', N'直接排放数据', N'直接数据输入', 1, N'kgCO2e/tCO2e', N'66', N'直接排放数据', N'Direct Emission Data', N'直接数据输入', N'tCO2e', 1, NULL, NULL, NULL, NULL, NULL, NULL, N'通用', N'直接输入', 28, 265, NULL, NULL, 23500, 17200, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 66, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 1, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (67, 1, N'201ef', N'67', N'电力输配损耗', N'外购能源', 0.02452, N'kgCO2e/kWh', N'67', N'电力输配损耗', N'T&D Loss', N'外购能源', N'kWh', 0.02452, NULL, NULL, NULL, NULL, NULL, NULL, N'范围3', N'GHG Protocol/零碳实验室(2024)', 28, 265, NULL, NULL, 23500, 17200, 0.02452, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 67, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.02452, 0.02452, N'201EF排放因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (68, 1, N'202ef', N'2022:110000', N'北京市', N'ef-electricity-factor', 0.558, N'kgCO2/kWh', N'2022:110000', N'电力', N'Electricity', N'电力', N'kWh', 0.558, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.558, N'2022110000', N'2022', N'110000', N'北京市', N'华北', 0.558, 0.6776, 0.5366, 0.5856, 0.8325, 68, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.558, 0.558, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (69, 1, N'202ef', N'2022:120000', N'天津市', N'ef-electricity-factor', 0.7041, N'kgCO2/kWh', N'2022:120000', N'电力', N'Electricity', N'电力', N'kWh', 0.7041, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.7041, N'2022120000', N'2022', N'120000', N'天津市', N'华北', 0.7041, 0.6776, 0.5366, 0.5856, 0.8325, 69, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.7041, 0.7041, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (70, 1, N'202ef', N'2022:130000', N'河北省', N'ef-electricity-factor', 0.7252, N'kgCO2/kWh', N'2022:130000', N'电力', N'Electricity', N'电力', N'kWh', 0.7252, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.7252, N'2022130000', N'2022', N'130000', N'河北省', N'华北', 0.7252, 0.6776, 0.5366, 0.5856, 0.8325, 70, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.7252, 0.7252, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (71, 1, N'202ef', N'2022:140000', N'山西省', N'ef-electricity-factor', 0.7096, N'kgCO2/kWh', N'2022:140000', N'电力', N'Electricity', N'电力', N'kWh', 0.7096, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.7096, N'2022140000', N'2022', N'140000', N'山西省', N'华北', 0.7096, 0.6776, 0.5366, 0.5856, 0.8325, 71, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.7096, 0.7096, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (72, 1, N'202ef', N'2022:150001', N'蒙西', N'ef-electricity-factor', 0.6849, N'kgCO2/kWh', N'2022:150001', N'电力', N'Electricity', N'电力', N'kWh', 0.6849, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6849, N'2022150001', N'2022', N'150001', N'蒙西', N'华北', 0.6849, 0.6776, 0.5366, 0.5856, 0.8325, 72, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6849, 0.6849, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (73, 1, N'202ef', N'2022:150002', N'蒙东', N'ef-electricity-factor', 0.6849, N'kgCO2/kWh', N'2022:150002', N'电力', N'Electricity', N'电力', N'kWh', 0.6849, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6849, N'2022150002', N'2022', N'150002', N'蒙东', N'东北', 0.6849, 0.5564, 0.5366, 0.5856, 0.8325, 73, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6849, 0.6849, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (74, 1, N'202ef', N'2022:210000', N'辽宁省', N'ef-electricity-factor', 0.5626, N'kgCO2/kWh', N'2022:210000', N'电力', N'Electricity', N'电力', N'kWh', 0.5626, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5626, N'2022210000', N'2022', N'210000', N'辽宁省', N'东北', 0.5626, 0.5564, 0.5366, 0.5856, 0.8325, 74, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5626, 0.5626, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (75, 1, N'202ef', N'2022:220000', N'吉林省', N'ef-electricity-factor', 0.4932, N'kgCO2/kWh', N'2022:220000', N'电力', N'Electricity', N'电力', N'kWh', 0.4932, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4932, N'2022220000', N'2022', N'220000', N'吉林省', N'东北', 0.4932, 0.5564, 0.5366, 0.5856, 0.8325, 75, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4932, 0.4932, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (76, 1, N'202ef', N'2022:230000', N'黑龙江省', N'ef-electricity-factor', 0.5368, N'kgCO2/kWh', N'2022:230000', N'电力', N'Electricity', N'电力', N'kWh', 0.5368, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5368, N'2022230000', N'2022', N'230000', N'黑龙江省', N'东北', 0.5368, 0.5564, 0.5366, 0.5856, 0.8325, 76, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5368, 0.5368, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (77, 1, N'202ef', N'2022:310000', N'上海市', N'ef-electricity-factor', 0.5849, N'kgCO2/kWh', N'2022:310000', N'电力', N'Electricity', N'电力', N'kWh', 0.5849, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5849, N'2022310000', N'2022', N'310000', N'上海市', N'华东', 0.5849, 0.5617, 0.5366, 0.5856, 0.8325, 77, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5849, 0.5849, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (78, 1, N'202ef', N'2022:320000', N'江苏省', N'ef-electricity-factor', 0.5978, N'kgCO2/kWh', N'2022:320000', N'电力', N'Electricity', N'电力', N'kWh', 0.5978, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5978, N'2022320000', N'2022', N'320000', N'江苏省', N'华东', 0.5978, 0.5617, 0.5366, 0.5856, 0.8325, 78, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5978, 0.5978, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (79, 1, N'202ef', N'2022:330000', N'浙江省', N'ef-electricity-factor', 0.5153, N'kgCO2/kWh', N'2022:330000', N'电力', N'Electricity', N'电力', N'kWh', 0.5153, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5153, N'2022330000', N'2022', N'330000', N'浙江省', N'华东', 0.5153, 0.5617, 0.5366, 0.5856, 0.8325, 79, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5153, 0.5153, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (80, 1, N'202ef', N'2022:340000', N'安徽省', N'ef-electricity-factor', 0.6782, N'kgCO2/kWh', N'2022:340000', N'电力', N'Electricity', N'电力', N'kWh', 0.6782, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6782, N'2022340000', N'2022', N'340000', N'安徽省', N'华东', 0.6782, 0.5617, 0.5366, 0.5856, 0.8325, 80, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6782, 0.6782, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (81, 1, N'202ef', N'2022:350000', N'福建省', N'ef-electricity-factor', 0.4092, N'kgCO2/kWh', N'2022:350000', N'电力', N'Electricity', N'电力', N'kWh', 0.4092, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4092, N'2022350000', N'2022', N'350000', N'福建省', N'华东', 0.4092, 0.5617, 0.5366, 0.5856, 0.8325, 81, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4092, 0.4092, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (82, 1, N'202ef', N'2022:360000', N'江西省', N'ef-electricity-factor', 0.5752, N'kgCO2/kWh', N'2022:360000', N'电力', N'Electricity', N'电力', N'kWh', 0.5752, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5752, N'2022360000', N'2022', N'360000', N'江西省', N'华中', 0.5752, 0.5395, 0.5366, 0.5856, 0.8325, 82, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5752, 0.5752, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (83, 1, N'202ef', N'2022:370000', N'山东省', N'ef-electricity-factor', 0.641, N'kgCO2/kWh', N'2022:370000', N'电力', N'Electricity', N'电力', N'kWh', 0.641, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.641, N'2022370000', N'2022', N'370000', N'山东省', N'华北', 0.641, 0.6776, 0.5366, 0.5856, 0.8325, 83, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.641, 0.641, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (84, 1, N'202ef', N'2022:410000', N'河南省', N'ef-electricity-factor', 0.6058, N'kgCO2/kWh', N'2022:410000', N'电力', N'Electricity', N'电力', N'kWh', 0.6058, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6058, N'2022410000', N'2022', N'410000', N'河南省', N'华中', 0.6058, 0.5395, 0.5366, 0.5856, 0.8325, 84, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6058, 0.6058, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (85, 1, N'202ef', N'2022:420000', N'湖北省', N'ef-electricity-factor', 0.4364, N'kgCO2/kWh', N'2022:420000', N'电力', N'Electricity', N'电力', N'kWh', 0.4364, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4364, N'2022420000', N'2022', N'420000', N'湖北省', N'华中', 0.4364, 0.5395, 0.5366, 0.5856, 0.8325, 85, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4364, 0.4364, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (86, 1, N'202ef', N'2022:430000', N'湖南省', N'ef-electricity-factor', 0.49, N'kgCO2/kWh', N'2022:430000', N'电力', N'Electricity', N'电力', N'kWh', 0.49, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.49, N'2022430000', N'2022', N'430000', N'湖南省', N'华中', 0.49, 0.5395, 0.5366, 0.5856, 0.8325, 86, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.49, 0.49, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (87, 1, N'202ef', N'2022:440000', N'广东省', N'ef-electricity-factor', 0.4403, N'kgCO2/kWh', N'2022:440000', N'电力', N'Electricity', N'电力', N'kWh', 0.4403, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4403, N'2022440000', N'2022', N'440000', N'广东省', N'南方', 0.4403, 0.3869, 0.5366, 0.5856, 0.8325, 87, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4403, 0.4403, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (88, 1, N'202ef', N'2022:450000', N'广西壮族自治区', N'ef-electricity-factor', 0.4044, N'kgCO2/kWh', N'2022:450000', N'电力', N'Electricity', N'电力', N'kWh', 0.4044, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4044, N'2022450000', N'2022', N'450000', N'广西壮族自治区', N'南方', 0.4044, 0.3869, 0.5366, 0.5856, 0.8325, 88, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4044, 0.4044, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (89, 1, N'202ef', N'2022:460000', N'海南省', N'ef-electricity-factor', 0.4184, N'kgCO2/kWh', N'2022:460000', N'电力', N'Electricity', N'电力', N'kWh', 0.4184, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4184, N'2022460000', N'2022', N'460000', N'海南省', N'南方', 0.4184, 0.3869, 0.5366, 0.5856, 0.8325, 89, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4184, 0.4184, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (90, 1, N'202ef', N'2022:500000', N'重庆市', N'ef-electricity-factor', 0.5227, N'kgCO2/kWh', N'2022:500000', N'电力', N'Electricity', N'电力', N'kWh', 0.5227, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5227, N'2022500000', N'2022', N'500000', N'重庆市', N'西南', 0.5227, 0.2268, 0.5366, 0.5856, 0.8325, 90, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5227, 0.5227, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (91, 1, N'202ef', N'2022:510000', N'四川省', N'ef-electricity-factor', 0.1404, N'kgCO2/kWh', N'2022:510000', N'电力', N'Electricity', N'电力', N'kWh', 0.1404, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.1404, N'2022510000', N'2022', N'510000', N'四川省', N'西南', 0.1404, 0.2268, 0.5366, 0.5856, 0.8325, 91, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.1404, 0.1404, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (92, 1, N'202ef', N'2022:520000', N'贵州省', N'ef-electricity-factor', 0.4989, N'kgCO2/kWh', N'2022:520000', N'电力', N'Electricity', N'电力', N'kWh', 0.4989, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4989, N'2022520000', N'2022', N'520000', N'贵州省', N'南方', 0.4989, 0.3869, 0.5366, 0.5856, 0.8325, 92, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4989, 0.4989, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (93, 1, N'202ef', N'2022:530000', N'云南省', N'ef-electricity-factor', 0.1073, N'kgCO2/kWh', N'2022:530000', N'电力', N'Electricity', N'电力', N'kWh', 0.1073, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.1073, N'2022530000', N'2022', N'530000', N'云南省', N'南方', 0.1073, 0.3869, 0.5366, 0.5856, 0.8325, 93, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.1073, 0.1073, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (94, 1, N'202ef', N'2022:610000', N'陕西省', N'ef-electricity-factor', 0.6558, N'kgCO2/kWh', N'2022:610000', N'电力', N'Electricity', N'电力', N'kWh', 0.6558, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6558, N'2022610000', N'2022', N'610000', N'陕西省', N'西北', 0.6558, 0.5857, 0.5366, 0.5856, 0.8325, 94, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6558, 0.6558, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (95, 1, N'202ef', N'2022:620000', N'甘肃省', N'ef-electricity-factor', 0.4772, N'kgCO2/kWh', N'2022:620000', N'电力', N'Electricity', N'电力', N'kWh', 0.4772, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4772, N'2022620000', N'2022', N'620000', N'甘肃省', N'西北', 0.4772, 0.5857, 0.5366, 0.5856, 0.8325, 95, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4772, 0.4772, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (96, 1, N'202ef', N'2022:630000', N'青海省', N'ef-electricity-factor', 0.1567, N'kgCO2/kWh', N'2022:630000', N'电力', N'Electricity', N'电力', N'kWh', 0.1567, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.1567, N'2022630000', N'2022', N'630000', N'青海省', N'西北', 0.1567, 0.5857, 0.5366, 0.5856, 0.8325, 96, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.1567, 0.1567, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (97, 1, N'202ef', N'2022:640000', N'宁夏回族自治区', N'ef-electricity-factor', 0.6423, N'kgCO2/kWh', N'2022:640000', N'电力', N'Electricity', N'电力', N'kWh', 0.6423, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6423, N'2022640000', N'2022', N'640000', N'宁夏回族自治区', N'西北', 0.6423, 0.5857, 0.5366, 0.5856, 0.8325, 97, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6423, 0.6423, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (98, 1, N'202ef', N'2022:650000', N'新疆维吾尔自治区', N'ef-electricity-factor', 0.6231, N'kgCO2/kWh', N'2022:650000', N'电力', N'Electricity', N'电力', N'kWh', 0.6231, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6231, N'2022650000', N'2022', N'650000', N'新疆维吾尔自治区', N'西北', 0.6231, 0.5857, 0.5366, 0.5856, 0.8325, 98, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6231, 0.6231, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (99, 1, N'202ef', N'2023:110000', N'北京市', N'ef-electricity-factor', 0.5554, N'kgCO2/kWh', N'2023:110000', N'电力', N'Electricity', N'电力', N'kWh', 0.5554, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5554, N'2023110000', N'2023', N'110000', N'北京市', N'华北', 0.5554, 0.6361, 0.5306, 0.6096, 0.8273, 99, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5554, 0.5554, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (100, 1, N'202ef', N'2023:120000', N'天津市', N'ef-electricity-factor', 0.6796, N'kgCO2/kWh', N'2023:120000', N'电力', N'Electricity', N'电力', N'kWh', 0.6796, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6796, N'2023120000', N'2023', N'120000', N'天津市', N'华北', 0.6796, 0.6361, 0.5306, 0.6096, 0.8273, 100, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6796, 0.6796, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (101, 1, N'202ef', N'2023:130000', N'河北省', N'ef-electricity-factor', 0.6516, N'kgCO2/kWh', N'2023:130000', N'电力', N'Electricity', N'电力', N'kWh', 0.6516, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6516, N'2023130000', N'2023', N'130000', N'河北省', N'华北', 0.6516, 0.6361, 0.5306, 0.6096, 0.8273, 101, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6516, 0.6516, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (102, 1, N'202ef', N'2023:140000', N'山西省', N'ef-electricity-factor', 0.6634, N'kgCO2/kWh', N'2023:140000', N'电力', N'Electricity', N'电力', N'kWh', 0.6634, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6634, N'2023140000', N'2023', N'140000', N'山西省', N'华北', 0.6634, 0.6361, 0.5306, 0.6096, 0.8273, 102, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6634, 0.6634, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (103, 1, N'202ef', N'2023:150001', N'蒙西', N'ef-electricity-factor', 0.6479, N'kgCO2/kWh', N'2023:150001', N'电力', N'Electricity', N'电力', N'kWh', 0.6479, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6479, N'2023150001', N'2023', N'150001', N'蒙西', N'华北', 0.6479, 0.6361, 0.5306, 0.6096, 0.8273, 103, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6479, 0.6479, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (104, 1, N'202ef', N'2023:150002', N'蒙东', N'ef-electricity-factor', 0.6479, N'kgCO2/kWh', N'2023:150002', N'电力', N'Electricity', N'电力', N'kWh', 0.6479, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6479, N'2023150002', N'2023', N'150002', N'蒙东', N'东北', 0.6479, 0.5122, 0.5306, 0.6096, 0.8273, 104, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6479, 0.6479, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (105, 1, N'202ef', N'2023:210000', N'辽宁省', N'ef-electricity-factor', 0.4878, N'kgCO2/kWh', N'2023:210000', N'电力', N'Electricity', N'电力', N'kWh', 0.4878, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4878, N'2023210000', N'2023', N'210000', N'辽宁省', N'东北', 0.4878, 0.5122, 0.5306, 0.6096, 0.8273, 105, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4878, 0.4878, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (106, 1, N'202ef', N'2023:220000', N'吉林省', N'ef-electricity-factor', 0.4671, N'kgCO2/kWh', N'2023:220000', N'电力', N'Electricity', N'电力', N'kWh', 0.4671, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4671, N'2023220000', N'2023', N'220000', N'吉林省', N'东北', 0.4671, 0.5122, 0.5306, 0.6096, 0.8273, 106, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4671, 0.4671, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (107, 1, N'202ef', N'2023:230000', N'黑龙江省', N'ef-electricity-factor', 0.5229, N'kgCO2/kWh', N'2023:230000', N'电力', N'Electricity', N'电力', N'kWh', 0.5229, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5229, N'2023230000', N'2023', N'230000', N'黑龙江省', N'东北', 0.5229, 0.5122, 0.5306, 0.6096, 0.8273, 107, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5229, 0.5229, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (108, 1, N'202ef', N'2023:310000', N'上海市', N'ef-electricity-factor', 0.5737, N'kgCO2/kWh', N'2023:310000', N'电力', N'Electricity', N'电力', N'kWh', 0.5737, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5737, N'2023310000', N'2023', N'310000', N'上海市', N'华东', 0.5737, 0.55, 0.5306, 0.6096, 0.8273, 108, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5737, 0.5737, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (109, 1, N'202ef', N'2023:320000', N'江苏省', N'ef-electricity-factor', 0.5827, N'kgCO2/kWh', N'2023:320000', N'电力', N'Electricity', N'电力', N'kWh', 0.5827, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5827, N'2023320000', N'2023', N'320000', N'江苏省', N'华东', 0.5827, 0.55, 0.5306, 0.6096, 0.8273, 109, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5827, 0.5827, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (110, 1, N'202ef', N'2023:330000', N'浙江省', N'ef-electricity-factor', 0.4974, N'kgCO2/kWh', N'2023:330000', N'电力', N'Electricity', N'电力', N'kWh', 0.4974, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4974, N'2023330000', N'2023', N'330000', N'浙江省', N'华东', 0.4974, 0.55, 0.5306, 0.6096, 0.8273, 110, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4974, 0.4974, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (111, 1, N'202ef', N'2023:340000', N'安徽省', N'ef-electricity-factor', 0.6553, N'kgCO2/kWh', N'2023:340000', N'电力', N'Electricity', N'电力', N'kWh', 0.6553, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6553, N'2023340000', N'2023', N'340000', N'安徽省', N'华东', 0.6553, 0.55, 0.5306, 0.6096, 0.8273, 111, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6553, 0.6553, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (112, 1, N'202ef', N'2023:350000', N'福建省', N'ef-electricity-factor', 0.4211, N'kgCO2/kWh', N'2023:350000', N'电力', N'Electricity', N'电力', N'kWh', 0.4211, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4211, N'2023350000', N'2023', N'350000', N'福建省', N'华东', 0.4211, 0.55, 0.5306, 0.6096, 0.8273, 112, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4211, 0.4211, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (113, 1, N'202ef', N'2023:360000', N'江西省', N'ef-electricity-factor', 0.5836, N'kgCO2/kWh', N'2023:360000', N'电力', N'Electricity', N'电力', N'kWh', 0.5836, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5836, N'2023360000', N'2023', N'360000', N'江西省', N'华中', 0.5836, 0.5271, 0.5306, 0.6096, 0.8273, 113, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5836, 0.5836, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (114, 1, N'202ef', N'2023:370000', N'山东省', N'ef-electricity-factor', 0.6191, N'kgCO2/kWh', N'2023:370000', N'电力', N'Electricity', N'电力', N'kWh', 0.6191, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6191, N'2023370000', N'2023', N'370000', N'山东省', N'华北', 0.6191, 0.6361, 0.5306, 0.6096, 0.8273, 114, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6191, 0.6191, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (115, 1, N'202ef', N'2023:410000', N'河南省', N'ef-electricity-factor', 0.5897, N'kgCO2/kWh', N'2023:410000', N'电力', N'Electricity', N'电力', N'kWh', 0.5897, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5897, N'2023410000', N'2023', N'410000', N'河南省', N'华中', 0.5897, 0.5271, 0.5306, 0.6096, 0.8273, 115, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5897, 0.5897, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (116, 1, N'202ef', N'2023:420000', N'湖北省', N'ef-electricity-factor', 0.4044, N'kgCO2/kWh', N'2023:420000', N'电力', N'Electricity', N'电力', N'kWh', 0.4044, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4044, N'2023420000', N'2023', N'420000', N'湖北省', N'华中', 0.4044, 0.5271, 0.5306, 0.6096, 0.8273, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4044, 0.4044, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (117, 1, N'202ef', N'2023:430000', N'湖南省', N'ef-electricity-factor', 0.4976, N'kgCO2/kWh', N'2023:430000', N'电力', N'Electricity', N'电力', N'kWh', 0.4976, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4976, N'2023430000', N'2023', N'430000', N'湖南省', N'华中', 0.4976, 0.5271, 0.5306, 0.6096, 0.8273, 117, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4976, 0.4976, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (118, 1, N'202ef', N'2023:440000', N'广东省', N'ef-electricity-factor', 0.4419, N'kgCO2/kWh', N'2023:440000', N'电力', N'Electricity', N'电力', N'kWh', 0.4419, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4419, N'2023440000', N'2023', N'440000', N'广东省', N'南方', 0.4419, 0.4042, 0.5306, 0.6096, 0.8273, 118, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4419, 0.4419, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (119, 1, N'202ef', N'2023:450000', N'广西壮族自治区', N'ef-electricity-factor', 0.4476, N'kgCO2/kWh', N'2023:450000', N'电力', N'Electricity', N'电力', N'kWh', 0.4476, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4476, N'2023450000', N'2023', N'450000', N'广西壮族自治区', N'南方', 0.4476, 0.4042, 0.5306, 0.6096, 0.8273, 119, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4476, 0.4476, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (120, 1, N'202ef', N'2023:460000', N'海南省', N'ef-electricity-factor', 0.3648, N'kgCO2/kWh', N'2023:460000', N'电力', N'Electricity', N'电力', N'kWh', 0.3648, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.3648, N'2023460000', N'2023', N'460000', N'海南省', N'南方', 0.3648, 0.4042, 0.5306, 0.6096, 0.8273, 120, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.3648, 0.3648, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (121, 1, N'202ef', N'2023:500000', N'重庆市', N'ef-electricity-factor', 0.5581, N'kgCO2/kWh', N'2023:500000', N'电力', N'Electricity', N'电力', N'kWh', 0.5581, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5581, N'2023500000', N'2023', N'500000', N'重庆市', N'西南', 0.5581, 0.2472, 0.5306, 0.6096, 0.8273, 121, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5581, 0.5581, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (122, 1, N'202ef', N'2023:510000', N'四川省', N'ef-electricity-factor', 0.1564, N'kgCO2/kWh', N'2023:510000', N'电力', N'Electricity', N'电力', N'kWh', 0.1564, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.1564, N'2023510000', N'2023', N'510000', N'四川省', N'西南', 0.1564, 0.2472, 0.5306, 0.6096, 0.8273, 122, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.1564, 0.1564, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (123, 1, N'202ef', N'2023:520000', N'贵州省', N'ef-electricity-factor', 0.5683, N'kgCO2/kWh', N'2023:520000', N'电力', N'Electricity', N'电力', N'kWh', 0.5683, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.5683, N'2023520000', N'2023', N'520000', N'贵州省', N'南方', 0.5683, 0.4042, 0.5306, 0.6096, 0.8273, 123, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.5683, 0.5683, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (124, 1, N'202ef', N'2023:530000', N'云南省', N'ef-electricity-factor', 0.1333, N'kgCO2/kWh', N'2023:530000', N'电力', N'Electricity', N'电力', N'kWh', 0.1333, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.1333, N'2023530000', N'2023', N'530000', N'云南省', N'南方', 0.1333, 0.4042, 0.5306, 0.6096, 0.8273, 124, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.1333, 0.1333, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (125, 1, N'202ef', N'2023:610000', N'陕西省', N'ef-electricity-factor', 0.6335, N'kgCO2/kWh', N'2023:610000', N'电力', N'Electricity', N'电力', N'kWh', 0.6335, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6335, N'2023610000', N'2023', N'610000', N'陕西省', N'西北', 0.6335, 0.5543, 0.5306, 0.6096, 0.8273, 125, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6335, 0.6335, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (126, 1, N'202ef', N'2023:620000', N'甘肃省', N'ef-electricity-factor', 0.4471, N'kgCO2/kWh', N'2023:620000', N'电力', N'Electricity', N'电力', N'kWh', 0.4471, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.4471, N'2023620000', N'2023', N'620000', N'甘肃省', N'西北', 0.4471, 0.5543, 0.5306, 0.6096, 0.8273, 126, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.4471, 0.4471, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (127, 1, N'202ef', N'2023:630000', N'青海省', N'ef-electricity-factor', 0.1796, N'kgCO2/kWh', N'2023:630000', N'电力', N'Electricity', N'电力', N'kWh', 0.1796, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.1796, N'2023630000', N'2023', N'630000', N'青海省', N'西北', 0.1796, 0.5543, 0.5306, 0.6096, 0.8273, 127, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.1796, 0.1796, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (128, 1, N'202ef', N'2023:640000', N'宁夏回族自治区', N'ef-electricity-factor', 0.6187, N'kgCO2/kWh', N'2023:640000', N'电力', N'Electricity', N'电力', N'kWh', 0.6187, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6187, N'2023640000', N'2023', N'640000', N'宁夏回族自治区', N'西北', 0.6187, 0.5543, 0.5306, 0.6096, 0.8273, 128, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6187, 0.6187, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)'),
        (129, 1, N'202ef', N'2023:650000', N'新疆维吾尔自治区', N'ef-electricity-factor', 0.6021, N'kgCO2/kWh', N'2023:650000', N'电力', N'Electricity', N'电力', N'kWh', 0.6021, 0, 0, NULL, NULL, NULL, NULL, N'电力因子', N'source(A)', NULL, NULL, NULL, NULL, NULL, NULL, 0.6021, N'2023650000', N'2023', N'650000', N'新疆维吾尔自治区', N'西北', 0.6021, 0.5543, 0.5306, 0.6096, 0.8273, 129, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.6021, 0.6021, N'202EF电力因子维度表', NULL, 1, @now, NULL, N'source(A)');

    INSERT INTO dbo.cv_factor_customer_scope
        (id, version_id, package_id, package_name, scope_status, create_time)
    VALUES
        (1, 1, 1001, N'基础套餐', N'enabled', @now),
        (2, 1, 1002, N'专业套餐', N'enabled', @now),
        (3, 1, 1003, N'集团套餐', N'enabled', @now);

    INSERT INTO dbo.cv_report_template
        (id, template_code, template_name, template_version, file_name, file_uri, publish_status, published_by, published_time, create_time, remark)
    VALUES
        (1, N'source-a-emission-source-identification', N'Source(A) 排放源识别表', N'source(A)', N'1 排放源识别表.xlsx', N'template://source(A)/ALL/1 排放源识别表.xlsx', N'published', N'admin', @now, @now, N'source(A)'),
        (2, N'source-a-emission-factor', N'Source(A) 排放因子表', N'source(A)', N'2 排放因子表.xlsx', N'template://source(A)/ALL/2 排放因子表.xlsx', N'published', N'admin', @now, @now, N'source(A)'),
        (3, N'source-a-report-content', N'Source(A) 报告内容模板', N'source(A)', N'Content.xlsx', N'template://source(A)/Content(A)/Content.xlsx', N'published', N'admin', @now, @now, N'source(A)'),
        (4, N'source-a-activity-10101', N'Source(A) 排放活动数据表 10101', N'source(A)', N'3 排放活动数据表10101.xlsx', N'template://source(A)/活动数据表/3 排放活动数据表10101.xlsx', N'published', N'admin', @now, @now, N'source(A)'),
        (5, N'source-a-activity-10102', N'Source(A) 排放活动数据表 10102', N'source(A)', N'3 排放活动数据表10102.xlsx', N'template://source(A)/活动数据表/3 排放活动数据表10102.xlsx', N'published', N'admin', @now, @now, N'source(A)'),
        (6, N'source-a-activity-10103', N'Source(A) 排放活动数据表 10103', N'source(A)', N'3 排放活动数据表10103.xlsx', N'template://source(A)/活动数据表/3 排放活动数据表10103.xlsx', N'published', N'admin', @now, @now, N'source(A)'),
        (7, N'source-a-activity-10104', N'Source(A) 排放活动数据表 10104', N'source(A)', N'3 排放活动数据表10104.xlsx', N'template://source(A)/活动数据表/3 排放活动数据表10104.xlsx', N'published', N'admin', @now, @now, N'source(A)'),
        (8, N'source-a-green-power', N'Source(A) 绿证绿电表', N'source(A)', N'4 绿证绿电表.xlsx', N'template://source(A)/绿证绿电表/4 绿证绿电表.xlsx', N'published', N'admin', @now, @now, N'source(A)'),
        (9, N'source-a-intensity-dimension', N'Source(A) 碳排放强度维度表', N'source(A)', N'5碳排放强度维度表.xlsx', N'template://source(A)/强度数据表/5碳排放强度维度表.xlsx', N'published', N'admin', @now, @now, N'source(A)'),
        (10, N'source-a-denominator-fact', N'Source(A) 分母事实表', N'source(A)', N'503分母事实表.xlsx', N'template://source(A)/强度数据表/503分母事实表/503分母事实表.xlsx', N'published', N'admin', @now, @now, N'source(A)');

    INSERT INTO dbo.cv_report_template_scope
        (id, template_id, package_id, package_name, scope_status, create_time)
    VALUES
        (1, 1, 1001, N'基础套餐', N'enabled', @now),
        (2, 1, 1002, N'专业套餐', N'enabled', @now),
        (3, 1, 1003, N'集团套餐', N'enabled', @now),
        (4, 2, 1001, N'基础套餐', N'enabled', @now),
        (5, 2, 1002, N'专业套餐', N'enabled', @now),
        (6, 2, 1003, N'集团套餐', N'enabled', @now),
        (7, 3, 1001, N'基础套餐', N'enabled', @now),
        (8, 3, 1002, N'专业套餐', N'enabled', @now),
        (9, 3, 1003, N'集团套餐', N'enabled', @now),
        (10, 4, 1001, N'基础套餐', N'enabled', @now),
        (11, 4, 1002, N'专业套餐', N'enabled', @now),
        (12, 4, 1003, N'集团套餐', N'enabled', @now),
        (13, 5, 1001, N'基础套餐', N'enabled', @now),
        (14, 5, 1002, N'专业套餐', N'enabled', @now),
        (15, 5, 1003, N'集团套餐', N'enabled', @now),
        (16, 6, 1001, N'基础套餐', N'enabled', @now),
        (17, 6, 1002, N'专业套餐', N'enabled', @now),
        (18, 6, 1003, N'集团套餐', N'enabled', @now),
        (19, 7, 1001, N'基础套餐', N'enabled', @now),
        (20, 7, 1002, N'专业套餐', N'enabled', @now),
        (21, 7, 1003, N'集团套餐', N'enabled', @now),
        (22, 8, 1001, N'基础套餐', N'enabled', @now),
        (23, 8, 1002, N'专业套餐', N'enabled', @now),
        (24, 8, 1003, N'集团套餐', N'enabled', @now),
        (25, 9, 1001, N'基础套餐', N'enabled', @now),
        (26, 9, 1002, N'专业套餐', N'enabled', @now),
        (27, 9, 1003, N'集团套餐', N'enabled', @now),
        (28, 10, 1001, N'基础套餐', N'enabled', @now),
        (29, 10, 1002, N'专业套餐', N'enabled', @now),
        (30, 10, 1003, N'集团套餐', N'enabled', @now);

    INSERT INTO dbo.cv_report_content
        (directory_no, directory_name, subdirectory_no, subdirectory_name, chart_names, display_order, status, create_time, update_time, remark)
    VALUES
        (1, N'1 总览｜集团碳排放驾驶舱', 1, N'1.1 排放总览', N'["CO2e排放总量（按照GHG Protocol体系分类）", "CO2e排放总量TOP10排放源", "范围占比", "范围二外购电力CO2e排放总量", "CO2e排放总量（按照工厂分类）", "CO2e排放总量（按照行业分类）"]', 1, N'0', @now, NULL, N'source(A)'),
        (1, N'1 总览｜集团碳排放驾驶舱', 2, N'1.2 时间趋势', N'["CO2e排放总量趋势分析（按照年份）", "CO2e排放总量趋势分析（按照月份）", "CO2e排放总量趋势分析（同比）", "CO2e排放总量趋势分析（环比）"]', 2, N'0', @now, NULL, N'source(A)'),
        (1, N'1 总览｜集团碳排放驾驶舱', 3, N'1.3 数据导出', N'["CO2e排放总量（按照GHG Protocol体系分类）", "CO2e排放总量（按照ISO 14064‑1体系分类）", "CO2e排放总量（按照GB/T 32150‑2025体系分类）", "范围二外购电力CO2e排放总量"]', 3, N'0', @now, NULL, N'source(A)'),
        (2, N'2 核算组织/边界', 4, N'2.1 核算组织/边界', N'["CO2e排放总量（按照GHG Protocol体系分类）"]', 4, N'0', @now, NULL, N'source(A)'),
        (3, N'3 排放源识别与标准体系', 5, N'3.1 标准体系', N'["排放源分类体系"]', 5, N'0', @now, NULL, N'source(A)'),
        (3, N'3 排放源识别与标准体系', 6, N'3.2 排放源识别', N'["排放源识别清单"]', 6, N'0', @now, NULL, N'source(A)'),
        (4, N'4 活动数据管理', 7, N'4.1 活动数据汇总', N'["活动数据汇总值（通用）", "活动数据汇总值（电力）", "活动数据汇总值kwh（电力）趋势分析（按照年份）"]', 7, N'0', @now, NULL, N'source(A)'),
        (5, N'5 排放因子库', 8, N'5.1 通用因子库', N'["通用排放因子表"]', 8, N'0', @now, NULL, N'source(A)'),
        (5, N'5 排放因子库', 9, N'5.2 电力因子库', N'["电力排放因子表", "年份和电力因子对应表"]', 9, N'0', @now, NULL, N'source(A)'),
        (6, N'6 排放量核算结果', 10, N'6.1 排放量总表', N'["CO2e排放总量（按照GHG Protocol体系分类）", "CO2e排放总量（按照ISO 14064‑1体系分类）", "CO2e排放总量（按照GB/T 32150‑2025体系分类）"]', 10, N'0', @now, NULL, N'source(A)'),
        (6, N'6 排放量核算结果', 11, N'6.2 细分排放', N'["CO2e排放总量（按照工厂分类）", "CO2e排放总量（按照行业分类）", "范围二外购电力CO2e排放总量"]', 11, N'0', @now, NULL, N'source(A)'),
        (6, N'6 排放量核算结果', 12, N'6.3 基准年对比', N'["年度排放量 vs 基准年（绝对量）", "相对基准年的增减量（Δ 排放）", "相对基准年的减排率（%）", "按照排放范围对比", "报告年份 vs 基准年", "同比 vs 基准年差值"]', 12, N'0', @now, NULL, N'source(A)'),
        (6, N'6 排放量核算结果', 13, N'6.4 GHGP视图', N'["CO2e排放总量（按照GHG Protocol体系分类）"]', 13, N'0', @now, NULL, N'source(A)'),
        (6, N'6 排放量核算结果', 14, N'6.5 ISO视图', N'["CO2e排放总量（按照ISO 14064‑1体系分类）"]', 14, N'0', @now, NULL, N'source(A)'),
        (6, N'6 排放量核算结果', 15, N'6.6 国标视图', N'["CO2e排放总量（按照GB/T 32150‑2025体系分类）"]', 15, N'0', @now, NULL, N'source(A)'),
        (6, N'6 排放量核算结果', 16, N'6.7 温室气体构成', N'["温室气体排放量", "温室气体排放趋势", "温室气体排放量（换算成CO2）"]', 16, N'0', @now, NULL, N'source(A)'),
        (7, N'7 行业对比分析', 17, N'7.1 工厂对比分析', N'["（预留页面）"]', 17, N'0', @now, NULL, N'source(A)'),
        (7, N'7 行业对比分析', 18, N'7.2 行业对标分析', N'["（预留页面）"]', 18, N'0', @now, NULL, N'source(A)'),
        (8, N'8 碳排放强度管理', 19, N'8.1 强度总览', N'["碳排放强度汇总表", "碳排放强度执行情况（实际 vs 目标）", "工厂碳排放强度单位表", "碳排放强度趋势", "碳排放强度 vs 基准年（绝对量）"]', 19, N'0', @now, NULL, N'source(A)'),
        (8, N'8 碳排放强度管理', 20, N'8.2 强度对比', N'["工厂碳排放强度单位表", "碳排放强度 vs 产量", "碳排放强度vs基准年 差值", "碳排放强度 vs CO2e排放量"]', 20, N'0', @now, NULL, N'source(A)'),
        (8, N'8 碳排放强度管理', 21, N'8.3 行业强度对标', N'["（预留页面）"]', 21, N'0', @now, NULL, N'source(A)'),
        (9, N'9 数据验证', 22, N'9.1 数据验证', N'["（数据准确性验证页面）"]', 22, N'0', @now, NULL, N'source(A)'),
        (10, N'10 模型与方法学说明', 23, N'10.1 数据模型说明', N'["10.1.1 数据模型总体设计原则", "10.1.2 事实表与维度表设计", "10.1.3 统一口径与集中计算设计", "10.1.4 数据追溯与分析一致性"]', 23, N'0', @now, NULL, N'source(A)'),
        (10, N'10 模型与方法学说明', 24, N'10.2 方法学说明', N'["10.2.1 排放核算基本方法", "10.2.2 多气体核算与 CO2e 折算", "10.2.3 排放因子与口径控制方法", "10.2.4 基准年与变化分析方法", "10.2.5 碳排放强度计算方法"]', 24, N'0', @now, NULL, N'source(A)');

    INSERT INTO dbo.cv_report_content_catalog
        (id, catalog_no, catalog_name, subcatalog_no, subcatalog_name, chart_list, sort_order, status, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, N'1', N'1 总览｜集团碳排放驾驶舱', N'1', N'1.1 排放总览', N'["CO2e排放总量（按照GHG Protocol体系分类）", "CO2e排放总量TOP10排放源", "范围占比", "范围二外购电力CO2e排放总量", "CO2e排放总量（按照工厂分类）", "CO2e排放总量（按照行业分类）"]', 1, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (2, N'1', N'1 总览｜集团碳排放驾驶舱', N'2', N'1.2 时间趋势', N'["CO2e排放总量趋势分析（按照年份）", "CO2e排放总量趋势分析（按照月份）", "CO2e排放总量趋势分析（同比）", "CO2e排放总量趋势分析（环比）"]', 2, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (3, N'1', N'1 总览｜集团碳排放驾驶舱', N'3', N'1.3 数据导出', N'["CO2e排放总量（按照GHG Protocol体系分类）", "CO2e排放总量（按照ISO 14064‑1体系分类）", "CO2e排放总量（按照GB/T 32150‑2025体系分类）", "范围二外购电力CO2e排放总量"]', 3, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (4, N'2', N'2 核算组织/边界', N'4', N'2.1 核算组织/边界', N'["CO2e排放总量（按照GHG Protocol体系分类）"]', 4, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (5, N'3', N'3 排放源识别与标准体系', N'5', N'3.1 标准体系', N'["排放源分类体系"]', 5, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (6, N'3', N'3 排放源识别与标准体系', N'6', N'3.2 排放源识别', N'["排放源识别清单"]', 6, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (7, N'4', N'4 活动数据管理', N'7', N'4.1 活动数据汇总', N'["活动数据汇总值（通用）", "活动数据汇总值（电力）", "活动数据汇总值kwh（电力）趋势分析（按照年份）"]', 7, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (8, N'5', N'5 排放因子库', N'8', N'5.1 通用因子库', N'["通用排放因子表"]', 8, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (9, N'5', N'5 排放因子库', N'9', N'5.2 电力因子库', N'["电力排放因子表", "年份和电力因子对应表"]', 9, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (10, N'6', N'6 排放量核算结果', N'10', N'6.1 排放量总表', N'["CO2e排放总量（按照GHG Protocol体系分类）", "CO2e排放总量（按照ISO 14064‑1体系分类）", "CO2e排放总量（按照GB/T 32150‑2025体系分类）"]', 10, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (11, N'6', N'6 排放量核算结果', N'11', N'6.2 细分排放', N'["CO2e排放总量（按照工厂分类）", "CO2e排放总量（按照行业分类）", "范围二外购电力CO2e排放总量"]', 11, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (12, N'6', N'6 排放量核算结果', N'12', N'6.3 基准年对比', N'["年度排放量 vs 基准年（绝对量）", "相对基准年的增减量（Δ 排放）", "相对基准年的减排率（%）", "按照排放范围对比", "报告年份 vs 基准年", "同比 vs 基准年差值"]', 12, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (13, N'6', N'6 排放量核算结果', N'13', N'6.4 GHGP视图', N'["CO2e排放总量（按照GHG Protocol体系分类）"]', 13, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (14, N'6', N'6 排放量核算结果', N'14', N'6.5 ISO视图', N'["CO2e排放总量（按照ISO 14064‑1体系分类）"]', 14, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (15, N'6', N'6 排放量核算结果', N'15', N'6.6 国标视图', N'["CO2e排放总量（按照GB/T 32150‑2025体系分类）"]', 15, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (16, N'6', N'6 排放量核算结果', N'16', N'6.7 温室气体构成', N'["温室气体排放量", "温室气体排放趋势", "温室气体排放量（换算成CO2）"]', 16, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (17, N'7', N'7 行业对比分析', N'17', N'7.1 工厂对比分析', N'["（预留页面）"]', 17, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (18, N'7', N'7 行业对比分析', N'18', N'7.2 行业对标分析', N'["（预留页面）"]', 18, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (19, N'8', N'8 碳排放强度管理', N'19', N'8.1 强度总览', N'["碳排放强度汇总表", "碳排放强度执行情况（实际 vs 目标）", "工厂碳排放强度单位表", "碳排放强度趋势", "碳排放强度 vs 基准年（绝对量）"]', 19, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (20, N'8', N'8 碳排放强度管理', N'20', N'8.2 强度对比', N'["工厂碳排放强度单位表", "碳排放强度 vs 产量", "碳排放强度vs基准年 差值", "碳排放强度 vs CO2e排放量"]', 20, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (21, N'8', N'8 碳排放强度管理', N'21', N'8.3 行业强度对标', N'["（预留页面）"]', 21, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (22, N'9', N'9 数据验证', N'22', N'9.1 数据验证', N'["（数据准确性验证页面）"]', 22, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (23, N'10', N'10 模型与方法学说明', N'23', N'10.1 数据模型说明', N'["10.1.1 数据模型总体设计原则", "10.1.2 事实表与维度表设计", "10.1.3 统一口径与集中计算设计", "10.1.4 数据追溯与分析一致性"]', 23, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)'),
        (24, N'10', N'10 模型与方法学说明', N'24', N'10.2 方法学说明', N'["10.2.1 排放核算基本方法", "10.2.2 多气体核算与 CO2e 折算", "10.2.3 排放因子与口径控制方法", "10.2.4 基准年与变化分析方法", "10.2.5 碳排放强度计算方法"]', 24, N'0', @createDept, @createBy, @now, NULL, NULL, N'source(A)');

    COMMIT TRANSACTION;

    SELECT N'carbon_vendor_init_ok' AS result;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    DECLARE @message NVARCHAR(4000) = ERROR_MESSAGE();
    DECLARE @severity INT = ERROR_SEVERITY();
    DECLARE @state INT = ERROR_STATE();
    RAISERROR(@message, @severity, @state);
END CATCH;
GO
