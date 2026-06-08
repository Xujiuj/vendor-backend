-- Vendor portal business menus are returned by /system/menu/getRouters.
-- Frontend routers must not handwrite these business menu entries.
-- This script replaces the vendor portal menu id range, removes enterprise
-- portal business menus if they were accidentally seeded into the vendor DB,
-- and keeps RuoYi native system management, logs, and code-generation menus intact.

delete from sys_role_menu where menu_id between 900100 and 900199;
delete from sys_menu where menu_id between 900100 and 900199;
delete from sys_role_menu where menu_id between 910100 and 910199;
delete from sys_menu where menu_id between 910100 and 910199;

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910100, '厂商运营', 0, 1, 'vendor', 'Layout', '', 1, 0, 'M', '0', '0', '', 'guide', 103, 1, sysdate(), null, null, '厂商端 portal 根菜单');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910101, '客户档案', 910100, 1, 'customer', 'system/tenant/index', '', 1, 0, 'C', '0', '0', 'vendor:customer:list', 'peoples', 103, 1, sysdate(), null, null, '厂商客户档案，优先复用 RuoYi 租户/客户生成页');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910102, 'License 授权管理', 910100, 2, 'license', 'system/license/index', '', 1, 0, 'C', '0', '0', 'vendor:licenseIssue:list', 'lock', 103, 1, sysdate(), null, null, '厂商 License 授权签发、吊销与重签入口');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910103, '因子版本', 910100, 3, 'factor-version', 'vendor/factorVersion/index', '', 1, 0, 'C', '0', '0', 'vendor:factorVersion:list', 'tree-table', 103, 1, sysdate(), null, null, '厂商因子版本发布、冻结与生命周期管理');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910104, '因子开放范围', 910100, 4, 'factor-scope', 'vendor/factorScope/index', '', 1, 0, 'C', '0', '0', 'vendor:factorCustomerScope:list', 'tree', 103, 1, sysdate(), null, null, '厂商因子按客户与 License 授权开放范围');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910105, '模板库', 910100, 5, 'report-template', 'vendor/reportTemplate/index', '', 1, 0, 'C', '0', '0', 'vendor:reportTemplate:list', 'form', 103, 1, sysdate(), null, null, '厂商报表模板库与发布管理');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910106, '模板分发', 910100, 6, 'template-scope', 'vendor/templateScope/index', '', 1, 0, 'C', '0', '0', 'vendor:reportTemplateScope:list', 'share', 103, 1, sysdate(), null, null, '厂商模板按客户与 License 分发范围');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910107, '续费订单', 910100, 7, 'renewal-order', 'vendor/renewalOrder/index', '', 1, 0, 'C', '0', '0', 'vendor:renewalOrder:list', 'money', 103, 1, sysdate(), null, null, '厂商续费订单与支付回调运营');

-- Keep RuoYi native system management, logs, and generator routes available.
update sys_menu set component = 'system/user/index', perms = 'system:user:list' where menu_id = 100;
update sys_menu set component = 'system/role/index', perms = 'system:role:list' where menu_id = 101;
update sys_menu set component = 'system/menu/index', perms = 'system:menu:list' where menu_id = 102;
update sys_menu set component = 'system/dept/index', perms = 'system:dept:list' where menu_id = 103;
update sys_menu set component = 'system/post/index', perms = 'system:post:list' where menu_id = 104;
update sys_menu set component = 'system/dict/index', perms = 'system:dict:list' where menu_id = 105;
update sys_menu set component = 'system/config/index', perms = 'system:config:list' where menu_id = 106;
update sys_menu set component = 'system/notice/index', perms = 'system:notice:list' where menu_id = 107;
update sys_menu set component = 'monitor/operlog/index', perms = 'monitor:operlog:list' where menu_id = 500;
update sys_menu set component = 'monitor/logininfor/index', perms = 'monitor:logininfor:list' where menu_id = 501;
update sys_menu set component = 'tool/gen/index', perms = 'tool:gen:list' where menu_id = 115;
