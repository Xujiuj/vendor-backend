-- 厂商端门户菜单由 /system/menu/getRouters 返回。
-- 前端不手写业务菜单，启动时用该脚本保证菜单与当前交付范围一致。

delete from sys_role_menu where menu_id between 900100 and 900199;
delete from sys_menu where menu_id between 900100 and 900199;
delete from sys_role_menu where menu_id between 910100 and 910199;
delete from sys_menu where menu_id between 910100 and 910199;

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910100, '厂商运营', 0, 1, 'vendor', 'Layout', '', 1, 0, 'M', '0', '0', '', 'guide', 103, 1, SYSDATETIME(), null, null, '厂商端运营菜单'),
(910136, '数据管理', 0, 2, 'data-management', 'Layout', '', 1, 0, 'M', '0', '0', '', 'tree-table', 103, 1, SYSDATETIME(), null, null, '企业端申请与同步的数据源管理目录'),
(910108, '运营总览', 910100, 0, 'overview', '', '', 1, 0, 'F', '1', '0', 'vendor:overview:query', '#', 103, 1, SYSDATETIME(), null, null, '厂商首页运营总览接口权限'),
(910101, '客户档案', 910100, 1, 'customer', 'vendor/customer/index', '', 1, 0, 'C', '0', '0', 'vendor:customer:list', 'peoples', 103, 1, SYSDATETIME(), null, null, '厂商客户档案，连接真实后端接口'),
(910102, '授权管理', 910100, 2, 'license', 'vendor/licenseIssue/index', '', 1, 0, 'C', '0', '0', 'vendor:licenseIssue:list', 'lock', 103, 1, SYSDATETIME(), null, null, '厂商授权签发、撤销与重签入口'),
(910103, '因子版本', 910136, 1, 'factor-version', 'vendor/factorVersion/index', '', 1, 0, 'C', '0', '0', 'vendor:factorVersion:list', 'tree-table', 103, 1, SYSDATETIME(), null, null, '厂商因子版本发布、冻结与生命周期管理'),
(910121, '因子开放范围', 910136, 2, 'factor-scope', 'vendor/factorScope/index', '', 1, 0, 'C', '0', '0', 'vendor:factorCustomerScope:list', 'tree', 103, 1, SYSDATETIME(), null, null, '厂商按版本、客户与套餐控制因子开放范围'),
(910105, '模板库', 910136, 3, 'report-template', 'vendor/reportTemplate/index', '', 1, 0, 'C', '0', '0', 'vendor:reportTemplate:list', 'form', 103, 1, SYSDATETIME(), null, null, '厂商报表模板库与发布管理'),
(910106, '模板分发', 910136, 4, 'template-scope', 'vendor/templateScope/index', '', 1, 0, 'C', '0', '0', 'vendor:reportTemplateScope:list', 'share', 103, 1, SYSDATETIME(), null, null, '厂商模板按客户与授权分发范围'),
(910156, '报表内容', 910136, 5, 'report-content', 'vendor/reportContent/index', '', 1, 0, 'C', '0', '0', 'vendor:reportContent:list', 'documentation', 103, 1, SYSDATETIME(), null, null, '厂商报表内容目录配置，供企业端自动同步'),
(910131, '维表管理', 910136, 6, 'dimension', 'vendor/dimension/index', '', 1, 0, 'C', '0', '0', 'vendor:dimension:list', 'tree', 103, 1, SYSDATETIME(), null, null, '厂商维表管理，供企业端通过开放接口读取基础数据'),
(910126, '公告管理', 910136, 7, 'announcement', 'vendor/announcement/index', '', 1, 0, 'C', '0', '0', 'vendor:announcement:list', 'message', 103, 1, SYSDATETIME(), null, null, '厂商公告管理，供企业端工作台按授权同步读取'),
(910107, '续费订单', 910100, 10, 'renewal-order', 'vendor/renewalOrder/index', '', 1, 0, 'C', '0', '0', 'vendor:renewalOrder:list', 'money', 103, 1, SYSDATETIME(), null, null, '厂商续费订单与支付回调运营');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910109, '客户档案详情', 910101, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:customer:query', '#', 103, 1, SYSDATETIME(), null, null, '客户档案详情权限'),
(910148, '客户档案新增', 910101, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:customer:add', '#', 103, 1, SYSDATETIME(), null, null, '客户档案新增权限'),
(910149, '客户档案编辑', 910101, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:customer:edit', '#', 103, 1, SYSDATETIME(), null, null, '客户档案编辑权限'),
(910150, '客户档案删除', 910101, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:customer:remove', '#', 103, 1, SYSDATETIME(), null, null, '客户档案删除权限'),
(910151, '客户档案导出', 910101, 5, '', '', '', 1, 0, 'F', '0', '0', 'vendor:customer:export', '#', 103, 1, SYSDATETIME(), null, null, '客户档案导出权限'),
(910162, '授权管理详情', 910102, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:licenseIssue:query', '#', 103, 1, SYSDATETIME(), null, null, '授权管理详情权限'),
(910163, '授权管理签发', 910102, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:licenseIssue:issue', '#', 103, 1, SYSDATETIME(), null, null, '授权管理签发权限'),
(910164, '授权管理撤销', 910102, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:licenseIssue:revoke', '#', 103, 1, SYSDATETIME(), null, null, '授权管理撤销权限'),
(910161, '授权管理删除', 910102, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:licenseIssue:remove', '#', 103, 1, SYSDATETIME(), null, null, '授权管理删除权限'),
(910110, '因子版本详情', 910103, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:query', '#', 103, 1, SYSDATETIME(), null, null, '因子版本详情权限'),
(910145, '因子版本新增', 910103, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:add', '#', 103, 1, SYSDATETIME(), null, null, '因子版本新增权限'),
(910146, '因子版本编辑', 910103, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:edit', '#', 103, 1, SYSDATETIME(), null, null, '因子版本编辑权限'),
(910147, '因子版本删除', 910103, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:remove', '#', 103, 1, SYSDATETIME(), null, null, '因子版本删除权限'),
(910152, '因子版本发布', 910103, 5, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:publish', '#', 103, 1, SYSDATETIME(), null, null, '因子版本发布权限'),
(910153, '因子版本冻结', 910103, 6, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:freeze', '#', 103, 1, SYSDATETIME(), null, null, '因子版本冻结权限'),
(910154, '因子版本退役', 910103, 7, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:retire', '#', 103, 1, SYSDATETIME(), null, null, '因子版本退役权限'),
(910155, '因子版本恢复', 910103, 8, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:restore', '#', 103, 1, SYSDATETIME(), null, null, '因子版本恢复权限'),
(910124, '因子开放范围详情', 910121, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorCustomerScope:query', '#', 103, 1, SYSDATETIME(), null, null, '因子开放范围详情权限'),
(910137, '因子开放范围新增', 910121, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorCustomerScope:add', '#', 103, 1, SYSDATETIME(), null, null, '因子开放范围新增权限'),
(910138, '因子开放范围编辑', 910121, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorCustomerScope:edit', '#', 103, 1, SYSDATETIME(), null, null, '因子开放范围编辑权限'),
(910125, '因子开放范围删除', 910121, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorCustomerScope:remove', '#', 103, 1, SYSDATETIME(), null, null, '因子开放范围删除权限'),
(910113, '模板库详情', 910105, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:query', '#', 103, 1, SYSDATETIME(), null, null, '模板库详情权限'),
(910114, '模板库新增', 910105, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:add', '#', 103, 1, SYSDATETIME(), null, null, '模板库新增权限'),
(910115, '模板库编辑', 910105, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:edit', '#', 103, 1, SYSDATETIME(), null, null, '模板库编辑权限'),
(910116, '模板库删除', 910105, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:remove', '#', 103, 1, SYSDATETIME(), null, null, '模板库删除权限'),
(910117, '模板分发详情', 910106, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplateScope:query', '#', 103, 1, SYSDATETIME(), null, null, '模板分发详情权限'),
(910139, '模板分发新增', 910106, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplateScope:add', '#', 103, 1, SYSDATETIME(), null, null, '模板分发新增权限'),
(910140, '模板分发编辑', 910106, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplateScope:edit', '#', 103, 1, SYSDATETIME(), null, null, '模板分发编辑权限'),
(910118, '模板分发删除', 910106, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplateScope:remove', '#', 103, 1, SYSDATETIME(), null, null, '模板分发删除权限'),
(910157, '报表内容详情', 910156, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportContent:query', '#', 103, 1, SYSDATETIME(), null, null, '报表内容详情权限'),
(910158, '报表内容新增', 910156, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportContent:add', '#', 103, 1, SYSDATETIME(), null, null, '报表内容新增权限'),
(910159, '报表内容编辑', 910156, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportContent:edit', '#', 103, 1, SYSDATETIME(), null, null, '报表内容编辑权限'),
(910160, '报表内容删除', 910156, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportContent:remove', '#', 103, 1, SYSDATETIME(), null, null, '报表内容删除权限'),
(910132, '维表详情', 910131, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:query', '#', 103, 1, SYSDATETIME(), null, null, '维表详情权限'),
(910133, '维表新增', 910131, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:add', '#', 103, 1, SYSDATETIME(), null, null, '维表新增权限'),
(910134, '维表编辑', 910131, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:edit', '#', 103, 1, SYSDATETIME(), null, null, '维表编辑权限'),
(910135, '维表删除', 910131, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:remove', '#', 103, 1, SYSDATETIME(), null, null, '维表删除权限'),
(910127, '公告详情', 910126, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:query', '#', 103, 1, SYSDATETIME(), null, null, '公告详情权限'),
(910128, '公告新增', 910126, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:add', '#', 103, 1, SYSDATETIME(), null, null, '公告新增权限'),
(910129, '公告编辑', 910126, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:edit', '#', 103, 1, SYSDATETIME(), null, null, '公告编辑权限'),
(910130, '公告删除', 910126, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:remove', '#', 103, 1, SYSDATETIME(), null, null, '公告删除权限'),
(910119, '续费订单详情', 910107, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:query', '#', 103, 1, SYSDATETIME(), null, null, '续费订单详情权限'),
(910141, '续费订单新增', 910107, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:add', '#', 103, 1, SYSDATETIME(), null, null, '续费订单新增权限'),
(910142, '续费订单编辑', 910107, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:edit', '#', 103, 1, SYSDATETIME(), null, null, '续费订单编辑权限'),
(910143, '续费订单回调', 910107, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:callback', '#', 103, 1, SYSDATETIME(), null, null, '续费订单回调权限'),
(910144, '续费订单重试签发', 910107, 5, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:retryIssue', '#', 103, 1, SYSDATETIME(), null, null, '续费订单重试签发权限'),
(910120, '续费订单删除', 910107, 6, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:remove', '#', 103, 1, SYSDATETIME(), null, null, '续费订单删除权限');

-- 保留厂商端需要的若依系统管理和日志菜单。
delete from sys_role_menu
where menu_id in (
  1, 100, 101, 103, 104, 107, 108, 122, 130, 131, 500, 501,
  1001, 1002, 1003, 1004, 1005, 1006, 1007,
  1008, 1009, 1010, 1011, 1012,
  1017, 1018, 1019, 1020,
  1021, 1022, 1023, 1024, 1025,
  1036, 1037, 1038, 1039,
  1040, 1041, 1042, 1043, 1044, 1045,
  1611, 1612, 1613, 1614, 1615
);

delete from sys_menu
where menu_id in (
  1, 100, 101, 103, 104, 107, 108, 122, 130, 131, 500, 501,
  1001, 1002, 1003, 1004, 1005, 1006, 1007,
  1008, 1009, 1010, 1011, 1012,
  1017, 1018, 1019, 1020,
  1021, 1022, 1023, 1024, 1025,
  1036, 1037, 1038, 1039,
  1040, 1041, 1042, 1043, 1044, 1045,
  1611, 1612, 1613, 1614, 1615
);

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(1, '系统管理', 0, 3, 'system', null, '', 1, 0, 'M', '0', '0', '', 'system', 103, 1, SYSDATETIME(), null, null, '系统管理目录'),
(100, '用户管理', 1, 1, 'user', 'system/user/index', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 103, 1, SYSDATETIME(), null, null, '用户管理菜单'),
(101, '角色管理', 1, 2, 'role', 'system/role/index', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 103, 1, SYSDATETIME(), null, null, '角色管理菜单'),
(103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 103, 1, SYSDATETIME(), null, null, '部门管理菜单'),
(104, '岗位管理', 1, 5, 'post', 'system/post/index', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 103, 1, SYSDATETIME(), null, null, '岗位管理菜单'),
(122, '套餐管理', 1, 8, 'tenantPackage', 'system/tenantPackage/index', '', 1, 0, 'C', '0', '0', 'system:tenantPackage:list', 'form', 103, 1, SYSDATETIME(), null, null, '授权套餐管理菜单'),
(107, '通知公告', 1, 9, 'notice', 'system/notice/index', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 103, 1, SYSDATETIME(), null, null, '通知公告菜单'),
(108, '日志管理', 0, 4, 'monitor', 'Layout', '', 1, 0, 'M', '0', '0', '', 'log', 103, 1, SYSDATETIME(), null, null, '日志管理目录'),
(500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list', 'form', 103, 1, SYSDATETIME(), null, null, '操作日志菜单'),
(501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 103, 1, SYSDATETIME(), null, null, '登录日志菜单'),
(130, '角色新增', 101, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 103, 1, SYSDATETIME(), null, null, '角色新增权限'),
(131, '用户编辑', 100, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 103, 1, SYSDATETIME(), null, null, '用户编辑权限'),
(1001, '用户查询', 100, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 103, 1, SYSDATETIME(), null, null, '用户查询权限'),
(1002, '用户新增', 100, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 103, 1, SYSDATETIME(), null, null, '用户新增权限'),
(1003, '用户修改', 100, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 103, 1, SYSDATETIME(), null, null, '用户修改权限'),
(1004, '用户删除', 100, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 103, 1, SYSDATETIME(), null, null, '用户删除权限'),
(1005, '用户导出', 100, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 103, 1, SYSDATETIME(), null, null, '用户导出权限'),
(1006, '用户导入', 100, 6, '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 103, 1, SYSDATETIME(), null, null, '用户导入权限'),
(1007, '重置密码', 100, 7, '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 103, 1, SYSDATETIME(), null, null, '重置密码权限'),
(1008, '角色查询', 101, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 103, 1, SYSDATETIME(), null, null, '角色查询权限'),
(1009, '角色新增', 101, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 103, 1, SYSDATETIME(), null, null, '角色新增权限'),
(1010, '角色修改', 101, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 103, 1, SYSDATETIME(), null, null, '角色修改权限'),
(1011, '角色删除', 101, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 103, 1, SYSDATETIME(), null, null, '角色删除权限'),
(1012, '角色导出', 101, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 103, 1, SYSDATETIME(), null, null, '角色导出权限'),
(1017, '部门查询', 103, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 103, 1, SYSDATETIME(), null, null, '部门查询权限'),
(1018, '部门新增', 103, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 103, 1, SYSDATETIME(), null, null, '部门新增权限'),
(1019, '部门修改', 103, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 103, 1, SYSDATETIME(), null, null, '部门修改权限'),
(1020, '部门删除', 103, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 103, 1, SYSDATETIME(), null, null, '部门删除权限'),
(1021, '岗位查询', 104, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 103, 1, SYSDATETIME(), null, null, '岗位查询权限'),
(1022, '岗位新增', 104, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 103, 1, SYSDATETIME(), null, null, '岗位新增权限'),
(1023, '岗位修改', 104, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 103, 1, SYSDATETIME(), null, null, '岗位修改权限'),
(1024, '岗位删除', 104, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 103, 1, SYSDATETIME(), null, null, '岗位删除权限'),
(1025, '岗位导出', 104, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 103, 1, SYSDATETIME(), null, null, '岗位导出权限'),
(1036, '公告查询', 107, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 103, 1, SYSDATETIME(), null, null, '公告查询权限'),
(1037, '公告新增', 107, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 103, 1, SYSDATETIME(), null, null, '公告新增权限'),
(1038, '公告修改', 107, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 103, 1, SYSDATETIME(), null, null, '公告修改权限'),
(1039, '公告删除', 107, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 103, 1, SYSDATETIME(), null, null, '公告删除权限'),
(1040, '操作日志查询', 500, 1, '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 103, 1, SYSDATETIME(), null, null, '操作日志查询权限'),
(1041, '操作日志删除', 500, 2, '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 103, 1, SYSDATETIME(), null, null, '操作日志删除权限'),
(1042, '操作日志导出', 500, 3, '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 103, 1, SYSDATETIME(), null, null, '操作日志导出权限'),
(1043, '登录日志查询', 501, 1, '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 103, 1, SYSDATETIME(), null, null, '登录日志查询权限'),
(1044, '登录日志删除', 501, 2, '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 103, 1, SYSDATETIME(), null, null, '登录日志删除权限'),
(1045, '登录日志导出', 501, 3, '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 103, 1, SYSDATETIME(), null, null, '登录日志导出权限'),
(1611, '套餐查询', 122, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:query', '#', 103, 1, SYSDATETIME(), null, null, '套餐查询权限'),
(1612, '套餐新增', 122, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:add', '#', 103, 1, SYSDATETIME(), null, null, '套餐新增权限'),
(1613, '套餐修改', 122, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:edit', '#', 103, 1, SYSDATETIME(), null, null, '套餐修改权限'),
(1614, '套餐删除', 122, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:remove', '#', 103, 1, SYSDATETIME(), null, null, '套餐删除权限'),
(1615, '套餐导出', 122, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:export', '#', 103, 1, SYSDATETIME(), null, null, '套餐导出权限');

-- 隐藏或删除交付无关的若依原生菜单，避免超级管理员解析到已删除前端组件。
update sys_menu
set visible = '1',
    update_time = SYSDATETIME()
where menu_id in (
  102, 105, 106, 115, 116, 132,
  1013, 1014, 1015, 1016,
  1026, 1027, 1028, 1029, 1030,
  1031, 1032, 1033, 1034, 1035,
  1055, 1056, 1057, 1058, 1059, 1060
);

update sys_menu
set visible = '1',
    status = '1',
    update_time = SYSDATETIME()
where menu_type in ('M', 'C')
  and (
    menu_id in (2, 3, 4, 5, 6, 115, 116, 121)
    or path in ('tenant', 'demo', 'oss', 'oss-config/index', 'client')
    or path like 'http%'
    or component in (
      'system/oss/index',
      'system/oss/config',
      'system/client/index'
    )
    or component like 'demo/%'
  );

delete from sys_role_menu
where menu_id in (
  6, 102, 105, 106, 115, 116, 121, 132,
  1013, 1014, 1015, 1016,
  1026, 1027, 1028, 1029, 1030,
  1031, 1032, 1033, 1034, 1035,
  1055, 1056, 1057, 1058, 1059, 1060
);

delete from sys_menu
where menu_id in (
  6, 102, 105, 106, 115, 116, 121, 132,
  1013, 1014, 1015, 1016,
  1026, 1027, 1028, 1029, 1030,
  1031, 1032, 1033, 1034, 1035,
  1055, 1056, 1057, 1058, 1059, 1060
)
or path in ('tenant', 'menu', 'dict', 'config', 'oss', 'oss-config/index', 'client', 'demo')
or component in (
  'system/tenant/index',
  'system/menu/index',
  'system/dict/index',
  'system/dict/data',
  'system/config/index',
  'system/oss/index',
  'system/oss/config',
  'system/client/index'
)
or component like 'demo/%';

-- 给所有启用角色授予当前厂商端需要的业务和基础系统菜单。
insert into sys_role_menu (role_id, menu_id)
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
  )
  and not exists (
    select 1
    from sys_role_menu existing
    where existing.role_id = r.role_id
      and existing.menu_id = m.menu_id
  );

insert into sys_role_menu (role_id, menu_id)
select 1, menu_id
from sys_menu m
where not exists (
  select 1
  from sys_role_menu existing
  where existing.role_id = 1
    and existing.menu_id = m.menu_id
);
