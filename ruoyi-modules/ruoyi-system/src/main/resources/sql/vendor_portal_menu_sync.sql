-- Vendor portal business menus are returned by /system/menu/getRouters.
-- Frontend routers must not handwrite these business menu entries.
-- This script replaces the vendor portal menu id range, removes enterprise
-- portal business menus if they were accidentally seeded into the vendor DB,
-- and keeps only the final vendor operation surface visible.

delete from sys_role_menu where menu_id between 900100 and 900199;
delete from sys_menu where menu_id between 900100 and 900199;
delete from sys_role_menu where menu_id between 910100 and 910199;
delete from sys_menu where menu_id between 910100 and 910199;

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910100, '厂商运营', 0, 1, 'vendor', 'Layout', '', 1, 0, 'M', '0', '0', '', 'guide', 103, 1, sysdate(), null, null, '厂商端 portal 运营菜单');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910136, '数据管理', 0, 2, 'data-management', 'Layout', '', 1, 0, 'M', '0', '0', '', 'database', 103, 1, sysdate(), null, null, '企业端申请与同步的数据源管理目录');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910108, '运营总览', 910100, 0, 'overview', '', '', 1, 0, 'F', '1', '0', 'vendor:overview:query', '#', 103, 1, sysdate(), null, null, '厂商首页运营总览接口权限');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910101, '客户档案', 910100, 1, 'customer', 'vendor/customer/index', '', 1, 0, 'C', '0', '0', 'vendor:customer:list', 'peoples', 103, 1, sysdate(), null, null, '厂商客户档案，连接 vendor/customer 真实后端接口');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910102, 'License 授权管理', 910100, 2, 'license', 'vendor/licenseIssue/index', '', 1, 0, 'C', '0', '0', 'vendor:licenseIssue:list', 'lock', 103, 1, sysdate(), null, null, '厂商 License 授权签发、吊销与重签入口');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910103, '因子版本', 910136, 1, 'factor-version', 'vendor/factorVersion/index', '', 1, 0, 'C', '0', '0', 'vendor:factorVersion:list', 'tree-table', 103, 1, sysdate(), null, null, '厂商因子版本发布、冻结与生命周期管理');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910104, '因子明细', 910136, 2, 'factor-record', 'vendor/factorRecord/index', '', 1, 0, 'C', '0', '0', 'vendor:factorRecord:list', 'tree-table', 103, 1, sysdate(), null, null, '厂商因子版本下的因子明细维护，供企业端授权同步');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910121, '因子开放范围', 910136, 3, 'factor-scope', 'vendor/factorScope/index', '', 1, 0, 'C', '0', '0', 'vendor:factorCustomerScope:list', 'tree', 103, 1, sysdate(), null, null, '厂商因子按版本、客户与套餐版本控制开放范围，License 通过企业购买版本继承范围');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910105, '模板库', 910136, 4, 'report-template', 'vendor/reportTemplate/index', '', 1, 0, 'C', '0', '0', 'vendor:reportTemplate:list', 'form', 103, 1, sysdate(), null, null, '厂商报表模板库与发布管理');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910106, '模板分发', 910136, 5, 'template-scope', 'vendor/templateScope/index', '', 1, 0, 'C', '0', '0', 'vendor:reportTemplateScope:list', 'share', 103, 1, sysdate(), null, null, '厂商模板按客户与 License 分发范围');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910131, '维表管理', 910136, 6, 'dimension', 'vendor/dimension/index', '', 1, 0, 'C', '0', '0', 'vendor:dimension:list', 'tree', 103, 1, sysdate(), null, null, '厂商维表管理，供企业端通过开放接口读取厂商来源基础数据');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910126, '公告管理', 910136, 7, 'announcement', 'vendor/announcement/index', '', 1, 0, 'C', '0', '0', 'vendor:announcement:list', 'message', 103, 1, sysdate(), null, null, '厂商公告管理，供企业端工作台按 License 授权同步读取');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910107, '续费订单', 910100, 10, 'renewal-order', 'vendor/renewalOrder/index', '', 1, 0, 'C', '0', '0', 'vendor:renewalOrder:list', 'money', 103, 1, sysdate(), null, null, '厂商续费订单与支付回调运营');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910109, '客户档案详情', 910101, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:customer:query', '#', 103, 1, sysdate(), null, null, '客户档案详情权限'),
(910110, '因子版本详情', 910103, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:query', '#', 103, 1, sysdate(), null, null, '因子版本详情权限'),
(910111, '因子明细详情', 910104, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorRecord:query', '#', 103, 1, sysdate(), null, null, '因子明细详情权限'),
(910112, '因子明细新增', 910104, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorRecord:add', '#', 103, 1, sysdate(), null, null, '因子明细新增权限'),
(910122, '因子明细编辑', 910104, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorRecord:edit', '#', 103, 1, sysdate(), null, null, '因子明细编辑权限'),
(910123, '因子明细删除', 910104, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorRecord:remove', '#', 103, 1, sysdate(), null, null, '因子明细删除权限'),
(910124, '因子开放范围详情', 910121, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorCustomerScope:query', '#', 103, 1, sysdate(), null, null, '因子开放范围详情权限'),
(910137, '因子开放范围新增', 910121, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorCustomerScope:add', '#', 103, 1, sysdate(), null, null, '因子开放范围新增权限'),
(910138, '因子开放范围编辑', 910121, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorCustomerScope:edit', '#', 103, 1, sysdate(), null, null, '因子开放范围编辑权限'),
(910125, '因子开放范围删除', 910121, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorCustomerScope:remove', '#', 103, 1, sysdate(), null, null, '因子开放范围删除权限'),
(910113, '模板库详情', 910105, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:query', '#', 103, 1, sysdate(), null, null, '模板库详情权限'),
(910114, '模板库新增', 910105, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:add', '#', 103, 1, sysdate(), null, null, '模板库新增权限'),
(910115, '模板库编辑', 910105, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:edit', '#', 103, 1, sysdate(), null, null, '模板库编辑权限'),
(910116, '模板库删除', 910105, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:remove', '#', 103, 1, sysdate(), null, null, '模板库删除权限'),
(910117, '模板分发详情', 910106, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplateScope:query', '#', 103, 1, sysdate(), null, null, '模板分发详情权限'),
(910139, '模板分发新增', 910106, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplateScope:add', '#', 103, 1, sysdate(), null, null, '模板分发新增权限'),
(910140, '模板分发编辑', 910106, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplateScope:edit', '#', 103, 1, sysdate(), null, null, '模板分发编辑权限'),
(910118, '模板分发删除', 910106, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplateScope:remove', '#', 103, 1, sysdate(), null, null, '模板分发删除权限'),
(910132, '维表详情', 910131, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:query', '#', 103, 1, sysdate(), null, null, '维表详情权限'),
(910133, '维表新增', 910131, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:add', '#', 103, 1, sysdate(), null, null, '维表新增权限'),
(910134, '维表编辑', 910131, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:edit', '#', 103, 1, sysdate(), null, null, '维表编辑权限'),
(910135, '维表删除', 910131, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:remove', '#', 103, 1, sysdate(), null, null, '维表删除权限'),
(910127, '公告详情', 910126, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:query', '#', 103, 1, sysdate(), null, null, '公告详情权限'),
(910128, '公告新增', 910126, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:add', '#', 103, 1, sysdate(), null, null, '公告新增权限'),
(910129, '公告编辑', 910126, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:edit', '#', 103, 1, sysdate(), null, null, '公告编辑权限'),
(910130, '公告删除', 910126, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:remove', '#', 103, 1, sysdate(), null, null, '公告删除权限'),
(910119, '续费订单详情', 910107, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:query', '#', 103, 1, sysdate(), null, null, '续费订单详情权限'),
(910141, '续费订单新增', 910107, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:add', '#', 103, 1, sysdate(), null, null, '续费订单新增权限'),
(910142, '续费订单编辑', 910107, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:edit', '#', 103, 1, sysdate(), null, null, '续费订单编辑权限'),
(910143, '续费订单回调', 910107, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:callback', '#', 103, 1, sysdate(), null, null, '续费订单回调权限'),
(910144, '续费订单重试签发', 910107, 5, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:retryIssue', '#', 103, 1, sysdate(), null, null, '续费订单重试签发权限'),
(910120, '续费订单删除', 910107, 6, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:remove', '#', 103, 1, sysdate(), null, null, '续费订单删除权限');

-- Keep selected RuoYi native system management and log routes available.
-- Some vendor databases are initialized without the native RuoYi menu seed.
-- Insert the basic operation surface first, then normalize existing rows below.
insert ignore into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(1, '系统管理', 0, 3, 'system', null, '', 1, 0, 'M', '0', '0', '', 'system', 103, 1, sysdate(), null, null, '厂商端系统管理目录'),
(100, '用户管理', 1, 1, 'user', 'system/user/index', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 103, 1, sysdate(), null, null, '厂商端用户管理菜单'),
(101, '角色管理', 1, 2, 'role', 'system/role/index', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 103, 1, sysdate(), null, null, '厂商端角色管理菜单'),
(102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', 1, 0, 'C', '1', '0', 'system:menu:list', 'tree-table', 103, 1, sysdate(), null, null, '厂商端菜单管理菜单'),
(103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 103, 1, sysdate(), null, null, '厂商端部门管理菜单'),
(104, '岗位管理', 1, 5, 'post', 'system/post/index', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 103, 1, sysdate(), null, null, '厂商端岗位管理菜单'),
(105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', 1, 0, 'C', '1', '0', 'system:dict:list', 'dict', 103, 1, sysdate(), null, null, '厂商端字典管理菜单'),
(106, '参数设置', 1, 7, 'config', 'system/config/index', '', 1, 0, 'C', '1', '0', 'system:config:list', 'edit', 103, 1, sysdate(), null, null, '厂商端参数设置菜单'),
(122, '套餐管理', 1, 8, 'tenantPackage', 'system/tenantPackage/index', '', 1, 0, 'C', '0', '0', 'system:tenantPackage:list', 'form', 103, 1, sysdate(), null, null, '厂商端系统套餐管理，用于配置角色可用菜单范围'),
(107, '公告配置', 1, 9, 'notice', 'system/notice/index', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 103, 1, sysdate(), null, null, '厂商端公告配置菜单'),
(108, '日志管理', 0, 4, 'monitor', 'Layout', '', 1, 0, 'M', '0', '0', '', 'log', 103, 1, sysdate(), null, null, '厂商端日志管理目录'),
(500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list', 'form', 103, 1, sysdate(), null, null, '厂商端操作日志菜单'),
(501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 103, 1, sysdate(), null, null, '厂商端登录日志菜单'),
(115, '代码生成', 0, 5, 'gen', 'tool/gen/index', '', 1, 0, 'C', '1', '0', 'tool:gen:list', 'code', 103, 1, sysdate(), null, null, '厂商端代码生成菜单'),
(130, '分配用户', 101, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 103, 1, sysdate(), null, null, '角色分配用户权限'),
(131, '分配角色', 100, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 103, 1, sysdate(), null, null, '用户分配角色权限'),
(132, '字典数据', 105, 1, 'data', 'system/dict/data', '', 1, 0, 'C', '0', '0', 'system:dict:list', '#', 103, 1, sysdate(), null, null, '字典数据菜单'),
(1001, '用户查询', 100, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 103, 1, sysdate(), null, null, '用户查询权限'),
(1002, '用户新增', 100, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 103, 1, sysdate(), null, null, '用户新增权限'),
(1003, '用户修改', 100, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 103, 1, sysdate(), null, null, '用户修改权限'),
(1004, '用户删除', 100, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 103, 1, sysdate(), null, null, '用户删除权限'),
(1005, '用户导出', 100, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 103, 1, sysdate(), null, null, '用户导出权限'),
(1006, '用户导入', 100, 6, '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 103, 1, sysdate(), null, null, '用户导入权限'),
(1007, '重置密码', 100, 7, '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 103, 1, sysdate(), null, null, '重置密码权限'),
(1008, '角色查询', 101, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 103, 1, sysdate(), null, null, '角色查询权限'),
(1009, '角色新增', 101, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 103, 1, sysdate(), null, null, '角色新增权限'),
(1010, '角色修改', 101, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 103, 1, sysdate(), null, null, '角色修改权限'),
(1011, '角色删除', 101, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 103, 1, sysdate(), null, null, '角色删除权限'),
(1012, '角色导出', 101, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 103, 1, sysdate(), null, null, '角色导出权限'),
(1013, '菜单查询', 102, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 103, 1, sysdate(), null, null, '菜单查询权限'),
(1014, '菜单新增', 102, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 103, 1, sysdate(), null, null, '菜单新增权限'),
(1015, '菜单修改', 102, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 103, 1, sysdate(), null, null, '菜单修改权限'),
(1016, '菜单删除', 102, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 103, 1, sysdate(), null, null, '菜单删除权限'),
(1017, '部门查询', 103, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 103, 1, sysdate(), null, null, '部门查询权限'),
(1018, '部门新增', 103, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 103, 1, sysdate(), null, null, '部门新增权限'),
(1019, '部门修改', 103, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 103, 1, sysdate(), null, null, '部门修改权限'),
(1020, '部门删除', 103, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 103, 1, sysdate(), null, null, '部门删除权限'),
(1021, '岗位查询', 104, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 103, 1, sysdate(), null, null, '岗位查询权限'),
(1022, '岗位新增', 104, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 103, 1, sysdate(), null, null, '岗位新增权限'),
(1023, '岗位修改', 104, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 103, 1, sysdate(), null, null, '岗位修改权限'),
(1024, '岗位删除', 104, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 103, 1, sysdate(), null, null, '岗位删除权限'),
(1025, '岗位导出', 104, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 103, 1, sysdate(), null, null, '岗位导出权限'),
(1026, '字典查询', 105, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 103, 1, sysdate(), null, null, '字典查询权限'),
(1027, '字典新增', 105, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 103, 1, sysdate(), null, null, '字典新增权限'),
(1028, '字典修改', 105, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 103, 1, sysdate(), null, null, '字典修改权限'),
(1029, '字典删除', 105, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 103, 1, sysdate(), null, null, '字典删除权限'),
(1030, '字典导出', 105, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 103, 1, sysdate(), null, null, '字典导出权限'),
(1031, '参数查询', 106, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 103, 1, sysdate(), null, null, '参数查询权限'),
(1032, '参数新增', 106, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 103, 1, sysdate(), null, null, '参数新增权限'),
(1033, '参数修改', 106, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 103, 1, sysdate(), null, null, '参数修改权限'),
(1034, '参数删除', 106, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 103, 1, sysdate(), null, null, '参数删除权限'),
(1035, '参数导出', 106, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 103, 1, sysdate(), null, null, '参数导出权限'),
(1036, '公告查询', 107, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 103, 1, sysdate(), null, null, '公告查询权限'),
(1037, '公告新增', 107, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 103, 1, sysdate(), null, null, '公告新增权限'),
(1038, '公告修改', 107, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 103, 1, sysdate(), null, null, '公告修改权限'),
(1039, '公告删除', 107, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 103, 1, sysdate(), null, null, '公告删除权限'),
(1040, '操作查询', 500, 1, '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 103, 1, sysdate(), null, null, '操作日志查询权限'),
(1041, '操作删除', 500, 2, '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 103, 1, sysdate(), null, null, '操作日志删除权限'),
(1042, '日志导出', 500, 3, '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 103, 1, sysdate(), null, null, '操作日志导出权限'),
(1043, '登录查询', 501, 1, '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 103, 1, sysdate(), null, null, '登录日志查询权限'),
(1044, '登录删除', 501, 2, '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 103, 1, sysdate(), null, null, '登录日志删除权限'),
(1045, '日志导出', 501, 3, '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 103, 1, sysdate(), null, null, '登录日志导出权限'),
(1055, '生成查询', 115, 1, '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 103, 1, sysdate(), null, null, '代码生成查询权限'),
(1056, '生成修改', 115, 2, '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 103, 1, sysdate(), null, null, '代码生成修改权限'),
(1057, '生成删除', 115, 3, '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 103, 1, sysdate(), null, null, '代码生成删除权限'),
(1058, '导入代码', 115, 4, '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 103, 1, sysdate(), null, null, '代码生成导入权限'),
(1059, '预览代码', 115, 5, '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 103, 1, sysdate(), null, null, '代码生成预览权限'),
(1060, '生成代码', 115, 6, '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 103, 1, sysdate(), null, null, '代码生成权限'),
(1611, '套餐查询', 122, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:query', '#', 103, 1, sysdate(), null, null, '套餐查询权限'),
(1612, '套餐新增', 122, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:add', '#', 103, 1, sysdate(), null, null, '套餐新增权限'),
(1613, '套餐修改', 122, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:edit', '#', 103, 1, sysdate(), null, null, '套餐修改权限'),
(1614, '套餐删除', 122, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:remove', '#', 103, 1, sysdate(), null, null, '套餐删除权限'),
(1615, '套餐导出', 122, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:export', '#', 103, 1, sysdate(), null, null, '套餐导出权限');

update sys_menu set menu_name = '系统管理', path = 'system', component = null, visible = '0', status = '0', remark = '厂商端系统管理目录' where menu_id = 1;
update sys_menu set menu_name = '用户管理', parent_id = 1, order_num = 1, path = 'user', component = 'system/user/index', perms = 'system:user:list', icon = 'user', visible = '0', status = '0', remark = '厂商端用户管理菜单' where menu_id = 100;
update sys_menu set menu_name = '角色管理', parent_id = 1, order_num = 2, path = 'role', component = 'system/role/index', perms = 'system:role:list', icon = 'peoples', visible = '0', status = '0', remark = '厂商端角色管理菜单' where menu_id = 101;
update sys_menu set menu_name = '菜单管理', parent_id = 1, order_num = 3, path = 'menu', component = 'system/menu/index', perms = 'system:menu:list', icon = 'tree-table', visible = '1', status = '0', remark = '厂商端菜单管理菜单' where menu_id = 102;
update sys_menu set menu_name = '部门管理', parent_id = 1, order_num = 4, path = 'dept', component = 'system/dept/index', perms = 'system:dept:list', icon = 'tree', visible = '0', status = '0', remark = '厂商端部门管理菜单' where menu_id = 103;
update sys_menu set menu_name = '岗位管理', parent_id = 1, order_num = 5, path = 'post', component = 'system/post/index', perms = 'system:post:list', icon = 'post', visible = '0', status = '0', remark = '厂商端岗位管理菜单' where menu_id = 104;
update sys_menu set menu_name = '字典管理', parent_id = 1, order_num = 6, path = 'dict', component = 'system/dict/index', perms = 'system:dict:list', icon = 'dict', visible = '1', status = '0', remark = '厂商端字典管理菜单' where menu_id = 105;
update sys_menu set menu_name = '参数设置', parent_id = 1, order_num = 7, path = 'config', component = 'system/config/index', perms = 'system:config:list', icon = 'edit', visible = '1', status = '0', remark = '厂商端参数设置菜单' where menu_id = 106;
update sys_menu set menu_name = '公告配置', parent_id = 1, order_num = 9, path = 'notice', component = 'system/notice/index', perms = 'system:notice:list', icon = 'message', visible = '0', status = '0', remark = '厂商端公告配置菜单' where menu_id = 107;
update sys_menu set menu_name = '套餐管理', parent_id = 1, order_num = 8, path = 'tenantPackage', component = 'system/tenantPackage/index', perms = 'system:tenantPackage:list', icon = 'form', visible = '0', status = '0', remark = '厂商端系统套餐管理，用于配置角色可用菜单范围' where menu_id = 122;
update sys_menu set menu_name = '套餐查询', parent_id = 122, order_num = 1, perms = 'system:tenantPackage:query', visible = '0', status = '0' where menu_id = 1611;
update sys_menu set menu_name = '套餐新增', parent_id = 122, order_num = 2, perms = 'system:tenantPackage:add', visible = '0', status = '0' where menu_id = 1612;
update sys_menu set menu_name = '套餐修改', parent_id = 122, order_num = 3, perms = 'system:tenantPackage:edit', visible = '0', status = '0' where menu_id = 1613;
update sys_menu set menu_name = '套餐删除', parent_id = 122, order_num = 4, perms = 'system:tenantPackage:remove', visible = '0', status = '0' where menu_id = 1614;
update sys_menu set menu_name = '套餐导出', parent_id = 122, order_num = 5, perms = 'system:tenantPackage:export', visible = '0', status = '0' where menu_id = 1615;
update sys_menu set menu_name = '日志管理', parent_id = 0, order_num = 4, path = 'monitor', component = 'Layout', perms = '', icon = 'log', visible = '0', status = '0', remark = '厂商端日志管理目录' where menu_id = 108;
update sys_menu set menu_name = '操作日志', parent_id = 108, order_num = 1, path = 'operlog', component = 'monitor/operlog/index', perms = 'monitor:operlog:list', icon = 'form', visible = '0', status = '0', remark = '厂商端操作日志菜单' where menu_id = 500;
update sys_menu set menu_name = '登录日志', parent_id = 108, order_num = 2, path = 'logininfor', component = 'monitor/logininfor/index', perms = 'monitor:logininfor:list', icon = 'logininfor', visible = '0', status = '0', remark = '厂商端登录日志菜单' where menu_id = 501;
update sys_menu set menu_name = '代码生成', parent_id = 0, order_num = 5, path = 'gen', component = 'tool/gen/index', perms = 'tool:gen:list', icon = 'code', visible = '1', status = '0', remark = '厂商端代码生成菜单' where menu_id = 115;

-- Production portal menu policy:
-- RuoYi uses visible='0' for shown routes and visible='1' for hidden routes.
update sys_menu
set visible = '1',
    update_time = sysdate()
where menu_id in (
  102, 105, 106, 115, 116, 132,
  1013, 1014, 1015, 1016,
  1026, 1027, 1028, 1029, 1030,
  1031, 1032, 1033, 1034, 1035,
  1055, 1056, 1057, 1058, 1059, 1060
);

delete from sys_role_menu
where menu_id in (
  102, 105, 106, 115, 116, 132,
  1013, 1014, 1015, 1016,
  1026, 1027, 1028, 1029, 1030,
  1031, 1032, 1033, 1034, 1035,
  1055, 1056, 1057, 1058, 1059, 1060
)
and role_id <> 1;

-- Vendor business logic no longer exposes RuoYi tenant/customer management as a
-- customer-facing module. Keep the inherited records for framework
-- compatibility, but hide the customer records from the vendor navigation tree.
-- Package management is still an administrator system function and is moved
-- under System Management above.
update sys_menu set visible = '1' where menu_id in (6, 121);

-- Non-super-admin users only receive menus through sys_role_menu.
-- Seed every enabled vendor role with the vendor-owned business menus and the
-- administrative operation surface needed in the vendor portal.
insert ignore into sys_role_menu (role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
cross join sys_menu m
where r.status = '0'
  and (
    m.menu_id between 910100 and 910199
    or m.menu_id in (
      1, 100, 101, 103, 104, 107, 108, 122, 130, 131, 500, 501,
      1001, 1002, 1003, 1004, 1005, 1006, 1007,
      1008, 1009, 1010, 1011, 1012,
      1017, 1018, 1019, 1020,
      1021, 1022, 1023, 1024, 1025,
      1036, 1037, 1038, 1039,
      1040, 1041, 1042, 1043, 1044, 1045,
      1611, 1612, 1613, 1614, 1615
    )
  );

-- Keep the built-in superadmin role fully assigned for role/menu
-- management screens and seeded database consistency.
insert ignore into sys_role_menu (role_id, menu_id)
select 1, menu_id
from sys_menu;
