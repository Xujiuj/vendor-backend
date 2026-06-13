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
(910102, 'License 授权管理', 910100, 2, 'license', 'system/license/index', '', 1, 0, 'C', '0', '0', 'vendor:licenseIssue:list', 'lock', 103, 1, sysdate(), null, null, '厂商 License 授权签发、吊销与重签入口');

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
(910121, '因子开放范围', 910136, 3, 'factor-scope', 'vendor/factorScope/index', '', 1, 0, 'C', '0', '0', 'vendor:factorCustomerScope:list', 'tree', 103, 1, sysdate(), null, null, '厂商因子按客户与 License 授权开放范围');

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
(910125, '因子开放范围删除', 910121, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorCustomerScope:remove', '#', 103, 1, sysdate(), null, null, '因子开放范围删除权限'),
(910113, '模板库详情', 910105, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:query', '#', 103, 1, sysdate(), null, null, '模板库详情权限'),
(910114, '模板库新增', 910105, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:add', '#', 103, 1, sysdate(), null, null, '模板库新增权限'),
(910115, '模板库编辑', 910105, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:edit', '#', 103, 1, sysdate(), null, null, '模板库编辑权限'),
(910116, '模板库删除', 910105, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:remove', '#', 103, 1, sysdate(), null, null, '模板库删除权限'),
(910117, '模板分发详情', 910106, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplateScope:query', '#', 103, 1, sysdate(), null, null, '模板分发详情权限'),
(910118, '模板分发删除', 910106, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplateScope:remove', '#', 103, 1, sysdate(), null, null, '模板分发删除权限'),
(910132, '维表详情', 910131, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:query', '#', 103, 1, sysdate(), null, null, '维表详情权限'),
(910133, '维表新增', 910131, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:add', '#', 103, 1, sysdate(), null, null, '维表新增权限'),
(910134, '维表编辑', 910131, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:edit', '#', 103, 1, sysdate(), null, null, '维表编辑权限'),
(910135, '维表删除', 910131, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:remove', '#', 103, 1, sysdate(), null, null, '维表删除权限'),
(910127, '公告详情', 910126, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:query', '#', 103, 1, sysdate(), null, null, '公告详情权限'),
(910128, '公告新增', 910126, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:add', '#', 103, 1, sysdate(), null, null, '公告新增权限'),
(910129, '公告编辑', 910126, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:edit', '#', 103, 1, sysdate(), null, null, '公告编辑权限'),
(910130, '公告删除', 910126, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:remove', '#', 103, 1, sysdate(), null, null, '公告删除权限'),
(910119, '续费订单详情', 910107, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:query', '#', 103, 1, sysdate(), null, null, '续费订单详情权限'),
(910120, '续费订单删除', 910107, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:remove', '#', 103, 1, sysdate(), null, null, '续费订单删除权限');

-- Keep RuoYi native system management, logs, and generator routes available.
update sys_menu set component = 'system/user/index', perms = 'system:user:list' where menu_id = 100;
update sys_menu set component = 'system/role/index', perms = 'system:role:list' where menu_id = 101;
update sys_menu set component = 'system/menu/index', perms = 'system:menu:list' where menu_id = 102;
update sys_menu set component = 'system/dept/index', perms = 'system:dept:list' where menu_id = 103;
update sys_menu set component = 'system/post/index', perms = 'system:post:list' where menu_id = 104;
update sys_menu set component = 'system/dict/index', perms = 'system:dict:list' where menu_id = 105;
update sys_menu set component = 'system/config/index', perms = 'system:config:list' where menu_id = 106;
update sys_menu set menu_name = '公告配置', component = 'system/notice/index', perms = 'system:notice:list', icon = 'message' where menu_id = 107;
update sys_menu set component = 'monitor/operlog/index', perms = 'monitor:operlog:list' where menu_id = 500;
update sys_menu set component = 'monitor/logininfor/index', perms = 'monitor:logininfor:list' where menu_id = 501;
update sys_menu set component = 'tool/gen/index', perms = 'tool:gen:list' where menu_id = 115;

-- Vendor business logic no longer exposes RuoYi tenant management as a
-- customer-facing module. Keep the inherited records for framework
-- compatibility, but hide them from the vendor navigation tree.
update sys_menu set visible = '1' where menu_id in (6, 121, 122);

-- Non-super-admin users only receive menus through sys_role_menu.
-- Seed the built-in RuoYi test roles with vendor-owned business menus.
insert ignore into sys_role_menu (role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
cross join sys_menu m
where r.role_key in ('test1', 'test2')
  and m.menu_id between 910100 and 910199;

-- Keep the built-in superadmin role fully assigned for role/menu
-- management screens and seeded database consistency.
insert ignore into sys_role_menu (role_id, menu_id)
select 1, menu_id
from sys_menu;
