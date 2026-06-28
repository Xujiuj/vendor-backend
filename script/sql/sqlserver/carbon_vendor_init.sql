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
        (1001, N'标准版', @menuIds, N'适用于单组织基础填报和模板下载。', 1, 1999.00, N'CNY', N'YEAR',
         1, 0, NULL, NULL, N'capture',
         N'[{"templateCode":"carbon-report-standard","templateVersion":"2026.1","scope":"download"}]',
         N'0', N'0', @createDept, @createBy, @now, NULL, NULL),
        (1002, N'专业版', @menuIds, N'适用于多部门协同、因子同步和报表模板同步。', 1, 0.00, N'CNY', N'YEAR',
         0, 0, NULL, NULL, N'capture,factor-sync,report-template-sync,report-template-download',
         N'[{"templateCode":"carbon-report-standard","templateVersion":"2026.1","scope":"download"}]',
         N'0', N'0', @createDept, @createBy, @now, NULL, NULL),
        (1003, N'集团版', @menuIds, N'适用于集团客户、报表门禁和高级支持。', 1, 0.00, N'CNY', N'YEAR',
         0, 0, NULL, NULL, N'capture,factor-sync,report-template-sync,report-template-download,report-gate,premium-support',
         N'[{"templateCode":"carbon-report-standard","templateVersion":"2026.1","scope":"download"}]',
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
        (1, N'330000', N'浙江省', NULL, N'省级', 1, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化行政区划'),
        (2, N'330200', N'宁波市', N'330000', N'市级', 2, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化行政区划');

    INSERT INTO dbo.cv_base_year
        (id, base_year_key, base_year, is_current, description, sort_order, status, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (20, N'2023', 2023, 1, N'默认基准年', 10, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化基准年'),
        (21, N'2024', 2024, 0, N'备用基准年', 20, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化基准年'),
        (22, N'2025', 2025, 0, N'备用基准年', 30, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化基准年'),
        (23, N'2026', 2026, 0, N'备用基准年', 40, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化基准年');

    INSERT INTO dbo.cv_dimension_record
        (id, dimension_code, record_code, record_name, parent_code, field01, field02, sort_order, status, create_time, update_time, remark)
    VALUES
        (1, N'admin-division', N'330000', N'浙江省', NULL, N'省级', N'华东电网', 1, N'0', @now, NULL, N'初始化维表'),
        (2, N'admin-division', N'330200', N'宁波市', N'330000', N'市级', N'华东电网', 2, N'0', @now, NULL, N'初始化维表'),
        (3, N'emission-source-category', N'SCOPE2-PURCHASED-ELEC', N'外购电力', NULL, N'范围二', N'kWh', 1, N'0', @now, NULL, N'初始化维表'),
        (4, N'base-year', N'BASE-2023', N'2023基准年', NULL, N'2023', N'通用企业', 1, N'0', @now, NULL, N'初始化维表'),
        (5, N'ef-electricity-factor', N'EF-ELEC-ZJ-2025', N'浙江电力排放因子', NULL, N'330000', N'0.5703', 1, N'0', @now, NULL, N'初始化维表'),
        (6, N'ef-electricity-version', N'EV-2025-ZJ', N'2025浙江电力因子对应', NULL, N'2025', N'2025版', 1, N'0', @now, NULL, N'初始化维表'),
        (7, N'ef-electricity-scope', N'GRID-REGIONAL', N'区域电网口径', NULL, N'区域', N'按区域电网平均排放因子核算', 1, N'0', @now, NULL, N'初始化维表'),
        (8, N'greenhouse-gas', N'CO2', N'二氧化碳', NULL, N'1', N'AR6', 1, N'0', @now, NULL, N'初始化维表'),
        (9, N'report-template-download', N'TPL-PBI-001', N'企业碳报表 Power BI 模板', NULL, N'Power BI', N'v1.0', 1, N'0', @now, NULL, N'初始化维表');

    INSERT INTO dbo.cv_emission_source_category
        (id, category_code, business_key, category_name, category_name_en, ghg_scope, ghg_scope_category,
         iso_category, iso_category_en, iso_category_description, gb_scope_category, gb_subcategory, parent_code,
         effective_date, expire_date, current_flag, version_no, standard_category, category_current_key, sort_order,
         status, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, N'SCOPE2-PURCHASED-ELEC', N'purchased-electricity', N'外购电力', N'Purchased electricity', N'Scope 2',
         N'外购电力', N'Category 2', N'Indirect energy emissions', N'间接能源排放', N'范围二', N'外购电力',
         NULL, '2026-01-01', '2099-12-31', N'1', N'2026.1', N'GHG Protocol', N'SCOPE2-PURCHASED-ELEC', 1,
         N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化排放源分类');

    INSERT INTO dbo.cv_electricity_factor_scope
        (id, scope_key, scope_name, sort_order, status, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, N'province', N'省级', 1, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化电力因子口径'),
        (2, N'region', N'区域电网', 2, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化电力因子口径'),
        (3, N'national', N'全国', 3, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化电力因子口径');

    INSERT INTO dbo.cv_electricity_factor_version
        (id, factor_version, effective_year, sort_order, status, create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, N'2025', 2025, 1, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化电力因子版本');

    INSERT INTO dbo.cv_electricity_factor
        (id, factor_version, division_code, division_name, region_name, province_factor, region_factor, national_factor,
         non_fossil_excluded_factor, national_fossil_power_factor, sort_order, status, create_dept, create_by, create_time,
         update_by, update_time, remark)
    VALUES
        (1, N'2025', N'330000', N'浙江省', N'华东电网', 0.5703000000, 0.5703000000, 0.5703000000,
         NULL, NULL, 1, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化电力排放因子');

    INSERT INTO dbo.cv_greenhouse_gas
        (id, gas_code, gas_name, gas_name_en, gwp_value, gwp_version, chemical_formula, sort_order, status,
         create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, N'CO2', N'二氧化碳', N'Carbon dioxide', 1.0000000000, N'IPCC AR6', N'CO2', 1, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化温室气体'),
        (2, N'CH4', N'甲烷', N'Methane', 27.9000000000, N'IPCC AR6', N'CH4', 2, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化温室气体'),
        (3, N'N2O', N'氧化亚氮', N'Nitrous oxide', 273.0000000000, N'IPCC AR6', N'N2O', 3, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化温室气体');

    INSERT INTO dbo.cv_factor_version
        (id, version_code, version_name, publish_status, frozen_flag, published_by, published_time, create_time, remark)
    VALUES
        (1, N'EF-2026.1', N'2026.1 默认排放因子库', N'published', 0, N'admin', @now, @now, N'初始化默认因子版本');

    INSERT INTO dbo.cv_factor_record
        (id, version_id, factor_table_code, factor_code, factor_name, factor_category, factor_value, factor_unit, factor_key,
         emission_source_name, emission_source_name_en, fuel_material_category, source_unit, co2, ch4, n2o, hfcs, pfcs, sf6,
         nf3, applicable_scope, factor_source, gwp_ch4, gwp_n2o, gwp_hfcs, gwp_pfcs, gwp_sf6, gwp_nf3, factor_gwp,
         version_province_code, factor_version, division_code, division_name, region_name, province_factor, region_factor,
         national_factor, non_fossil_excluded_factor, national_fossil_power_factor, row_no, fuel_level1, fuel_level2,
         fuel_level3, fuel_level4, lower_heat_value, lower_heat_value_cv, co2_factor, co2_factor_cv, gwp_value,
         converted_factor, source_ref, custom_fields, enabled_flag, create_time, update_time, remark)
    VALUES
        (1, 1, N'electricity', N'EF-ELEC-ZJ-2025', N'浙江省电力排放因子', N'电力', 0.5703000000, N'tCO2/MWh',
         N'EF-ELEC-ZJ-2025', N'外购电力', N'Purchased electricity', N'电力', N'MWh', 0.5703000000, 0, 0, NULL, NULL, NULL,
         NULL, N'Scope 2', N'初始化默认因子', 27.9000000000, 273.0000000000, NULL, NULL, NULL, NULL, 0.5703000000,
         N'330000-2025', N'2025', N'330000', N'浙江省', N'华东电网', 0.5703000000, 0.5703000000,
         0.5703000000, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1.0000000000,
         0.5703000000, N'初始化默认因子', NULL, 1, @now, NULL, N'初始化默认因子');

    INSERT INTO dbo.cv_factor_customer_scope
        (id, version_id, package_id, package_name, scope_status, create_time)
    VALUES
        (1, 1, 1001, N'标准版', N'enabled', @now),
        (2, 1, 1002, N'专业版', N'enabled', @now),
        (3, 1, 1003, N'集团版', N'enabled', @now);

    INSERT INTO dbo.cv_report_template
        (id, template_code, template_name, template_version, file_name, file_uri, publish_status, published_by, published_time, create_time, remark)
    VALUES
        (1, N'carbon-report-standard', N'企业碳报表标准模板', N'2026.1', N'carbon-report-standard-2026.1.xlsx',
         N'template://carbon-report-standard-2026.1.xlsx', N'published', N'admin', @now, @now, N'初始化标准模板');

    INSERT INTO dbo.cv_report_template_scope
        (id, template_id, package_id, package_name, scope_status, create_time)
    VALUES
        (1, 1, 1001, N'标准版', N'enabled', @now),
        (2, 1, 1002, N'专业版', N'enabled', @now),
        (3, 1, 1003, N'集团版', N'enabled', @now);

    INSERT INTO dbo.cv_report_content
        (directory_no, directory_name, subdirectory_no, subdirectory_name, chart_names, display_order, status, create_time, update_time, remark)
    VALUES
        (1, N'组织与边界', 1, N'组织概览', N'["组织边界","排放边界"]', 1, '0', @now, NULL, N'初始化报表内容');

    INSERT INTO dbo.cv_report_content_catalog
        (id, catalog_no, catalog_name, subcatalog_no, subcatalog_name, chart_list, sort_order, status,
         create_dept, create_by, create_time, update_by, update_time, remark)
    VALUES
        (1, N'1', N'组织与边界', N'1.1', N'组织概览', N'["组织边界","排放边界"]', 1, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化报表目录'),
        (2, N'2', N'排放核算', N'2.1', N'范围二电力', N'["外购电力排放"]', 2, N'0', @createDept, @createBy, @now, NULL, NULL, N'初始化报表目录');

    INSERT INTO dbo.cv_vendor_table_field
        (id, table_group, table_code, field_key, field_label, field_type, field_precision, field_width, field_options,
         required_flag, sort_order, status, create_time, update_time, remark)
    VALUES
        (2069717047359856642, N'dimension', N'admin-division', N'field01', N'行政区划级别', N'text', NULL, NULL, NULL, 1, 1, N'0', @now, NULL, N'初始化维表字段'),
        (2069717047649263618, N'dimension', N'admin-division', N'field02', N'所属电网', N'text', NULL, NULL, NULL, 0, 2, N'0', @now, NULL, N'初始化维表字段'),
        (2069717342383005697, N'dimension', N'base-year', N'field01', N'基准年', N'number', 0, NULL, NULL, 1, 1, N'0', @now, NULL, N'初始化维表字段'),
        (2069717342664024066, N'dimension', N'base-year', N'field02', N'适用说明', N'text', NULL, NULL, NULL, 0, 2, N'0', @now, NULL, N'初始化维表字段'),
        (2069717166469701634, N'dimension', N'emission-source-category', N'field01', N'GHG Protocol范围', N'text', NULL, NULL, NULL, 1, 1, N'0', @now, NULL, N'初始化维表字段'),
        (2069717166759108610, N'dimension', N'emission-source-category', N'field02', N'计量单位', N'text', NULL, NULL, NULL, 0, 2, N'0', @now, NULL, N'初始化维表字段'),
        (2069717400000000001, N'dimension', N'ef-electricity-factor', N'field01', N'行政区划代码', N'text', NULL, NULL, NULL, 1, 1, N'0', @now, NULL, N'初始化维表字段'),
        (2069717400000000002, N'dimension', N'ef-electricity-factor', N'field02', N'排放因子', N'decimal', 10, NULL, NULL, 1, 2, N'0', @now, NULL, N'初始化维表字段'),
        (2069717500000000001, N'dimension', N'greenhouse-gas', N'field01', N'GWP', N'decimal', 10, NULL, NULL, 1, 1, N'0', @now, NULL, N'初始化维表字段'),
        (2069717600000000001, N'dimension', N'report-template-download', N'field01', N'模板类型', N'text', NULL, NULL, NULL, 1, 1, N'0', @now, NULL, N'初始化维表字段');

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
