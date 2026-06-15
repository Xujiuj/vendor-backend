-- Repair vendor-side RuoYi infrastructure text that was stored as question marks
-- after a wrong character-set import. This is a data repair, not a schema reset.

update sys_role set role_name = '超级管理员', remark = '超级管理员' where role_key = 'superadmin';
update sys_role set role_name = '本部门及以下' where role_key = 'test1';
update sys_role set role_name = '仅本人' where role_key = 'test2';

update sys_user set nick_name = '疯狂的狮子Li', remark = '管理员' where user_name = 'admin';
update sys_user set nick_name = '本部门及以下 密码666666' where user_name = 'test';
update sys_user set nick_name = '仅本人 密码666666' where user_name = 'test1';

update sys_dept set dept_name = 'XXX科技' where dept_id = 100;
update sys_dept set dept_name = '深圳总公司' where dept_id = 101;
update sys_dept set dept_name = '长沙分公司' where dept_id = 102;
update sys_dept set dept_name = '研发部门' where dept_id = 103;
update sys_dept set dept_name = '市场部门' where dept_id = 104;
update sys_dept set dept_name = '测试部门' where dept_id = 105;
update sys_dept set dept_name = '财务部门' where dept_id = 106;
update sys_dept set dept_name = '运维部门' where dept_id = 107;
update sys_dept set dept_name = '市场部门' where dept_id = 108;
update sys_dept set dept_name = '财务部门' where dept_id = 109;

update sys_post set post_name = '董事长' where post_id = 1;
update sys_post set post_name = '项目经理' where post_id = 2;
update sys_post set post_name = '人力资源' where post_id = 3;
update sys_post set post_name = '普通员工' where post_id = 4;

update sys_menu set menu_name = '系统管理', remark = '系统管理目录' where menu_id = 1;
update sys_menu set menu_name = '系统监控', remark = '系统监控目录' where menu_id = 2;
update sys_menu set menu_name = '系统工具', remark = '系统工具目录' where menu_id = 3;
update sys_menu set menu_name = 'PLUS官网', remark = 'RuoYi-Vue-Plus官网地址' where menu_id = 4;
update sys_menu set menu_name = '测试菜单', remark = '测试菜单' where menu_id = 5;
update sys_menu set menu_name = '客户档案兼容', remark = '厂商端隐藏的 RuoYi 兼容目录', visible = '1' where menu_id = 6;
update sys_menu set menu_name = '用户管理', remark = '用户管理菜单' where menu_id = 100;
update sys_menu set menu_name = '角色管理', remark = '角色管理菜单' where menu_id = 101;
update sys_menu set menu_name = '菜单管理', remark = '菜单管理菜单' where menu_id = 102;
update sys_menu set menu_name = '部门管理', remark = '部门管理菜单' where menu_id = 103;
update sys_menu set menu_name = '岗位管理', remark = '岗位管理菜单' where menu_id = 104;
update sys_menu set menu_name = '字典管理', remark = '字典管理菜单' where menu_id = 105;
update sys_menu set menu_name = '参数设置', remark = '参数设置菜单' where menu_id = 106;
update sys_menu set menu_name = '通知公告', remark = '通知公告菜单' where menu_id = 107;
update sys_menu set menu_name = '日志管理', remark = '日志管理菜单' where menu_id = 108;
update sys_menu set menu_name = '在线用户', remark = '在线用户菜单' where menu_id = 109;
update sys_menu set menu_name = '缓存监控', remark = '缓存监控菜单' where menu_id = 113;
update sys_menu set menu_name = '代码生成', remark = '代码生成菜单' where menu_id = 115;
update sys_menu set menu_name = '修改生成配置' where menu_id = 116;
update sys_menu set menu_name = 'Admin监控', remark = 'Admin监控菜单' where menu_id = 117;
update sys_menu set menu_name = '文件管理', remark = '文件管理菜单' where menu_id = 118;
update sys_menu set menu_name = '任务调度中心', remark = 'SnailJob控制台菜单' where menu_id = 120;
update sys_menu set menu_name = '客户档案兼容', remark = '厂商端隐藏的 RuoYi 客户兼容菜单', visible = '1' where menu_id = 121;
update sys_menu set menu_name = '套餐管理', parent_id = 1, order_num = 8, path = 'tenantPackage', component = 'system/tenantPackage/index', perms = 'system:tenantPackage:list', icon = 'form', remark = '厂商端系统套餐管理，用于配置角色可用菜单范围', visible = '0', status = '0' where menu_id = 122;
update sys_menu set menu_name = '客户端管理', remark = '客户端管理菜单' where menu_id = 123;
update sys_menu set menu_name = '分配用户' where menu_id = 130;
update sys_menu set menu_name = '分配角色' where menu_id = 131;
update sys_menu set menu_name = '字典数据' where menu_id = 132;
update sys_menu set menu_name = '文件配置管理' where menu_id = 133;
update sys_menu set menu_name = '操作日志', remark = '操作日志菜单' where menu_id = 500;
update sys_menu set menu_name = '登录日志', remark = '登录日志菜单' where menu_id = 501;

update sys_menu set menu_name = '用户查询' where menu_id = 1001;
update sys_menu set menu_name = '用户新增' where menu_id = 1002;
update sys_menu set menu_name = '用户修改' where menu_id = 1003;
update sys_menu set menu_name = '用户删除' where menu_id = 1004;
update sys_menu set menu_name = '用户导出' where menu_id = 1005;
update sys_menu set menu_name = '用户导入' where menu_id = 1006;
update sys_menu set menu_name = '重置密码' where menu_id = 1007;
update sys_menu set menu_name = '角色查询' where menu_id = 1008;
update sys_menu set menu_name = '角色新增' where menu_id = 1009;
update sys_menu set menu_name = '角色修改' where menu_id = 1010;
update sys_menu set menu_name = '角色删除' where menu_id = 1011;
update sys_menu set menu_name = '角色导出' where menu_id = 1012;
update sys_menu set menu_name = '菜单查询' where menu_id = 1013;
update sys_menu set menu_name = '菜单新增' where menu_id = 1014;
update sys_menu set menu_name = '菜单修改' where menu_id = 1015;
update sys_menu set menu_name = '菜单删除' where menu_id = 1016;
update sys_menu set menu_name = '部门查询' where menu_id = 1017;
update sys_menu set menu_name = '部门新增' where menu_id = 1018;
update sys_menu set menu_name = '部门修改' where menu_id = 1019;
update sys_menu set menu_name = '部门删除' where menu_id = 1020;
update sys_menu set menu_name = '岗位查询' where menu_id = 1021;
update sys_menu set menu_name = '岗位新增' where menu_id = 1022;
update sys_menu set menu_name = '岗位修改' where menu_id = 1023;
update sys_menu set menu_name = '岗位删除' where menu_id = 1024;
update sys_menu set menu_name = '岗位导出' where menu_id = 1025;
update sys_menu set menu_name = '字典查询' where menu_id = 1026;
update sys_menu set menu_name = '字典新增' where menu_id = 1027;
update sys_menu set menu_name = '字典修改' where menu_id = 1028;
update sys_menu set menu_name = '字典删除' where menu_id = 1029;
update sys_menu set menu_name = '字典导出' where menu_id = 1030;
update sys_menu set menu_name = '参数查询' where menu_id = 1031;
update sys_menu set menu_name = '参数新增' where menu_id = 1032;
update sys_menu set menu_name = '参数修改' where menu_id = 1033;
update sys_menu set menu_name = '参数删除' where menu_id = 1034;
update sys_menu set menu_name = '参数导出' where menu_id = 1035;
update sys_menu set menu_name = '公告查询' where menu_id = 1036;
update sys_menu set menu_name = '公告新增' where menu_id = 1037;
update sys_menu set menu_name = '公告修改' where menu_id = 1038;
update sys_menu set menu_name = '公告删除' where menu_id = 1039;
update sys_menu set menu_name = '操作查询' where menu_id = 1040;
update sys_menu set menu_name = '操作删除' where menu_id = 1041;
update sys_menu set menu_name = '日志导出' where menu_id = 1042;
update sys_menu set menu_name = '登录查询' where menu_id = 1043;
update sys_menu set menu_name = '登录删除' where menu_id = 1044;
update sys_menu set menu_name = '日志导出' where menu_id = 1045;
update sys_menu set menu_name = '在线查询' where menu_id = 1046;
update sys_menu set menu_name = '批量强退' where menu_id = 1047;
update sys_menu set menu_name = '单条强退' where menu_id = 1048;
update sys_menu set menu_name = '账户解锁' where menu_id = 1050;
update sys_menu set menu_name = '生成查询' where menu_id = 1055;
update sys_menu set menu_name = '生成修改' where menu_id = 1056;
update sys_menu set menu_name = '生成删除' where menu_id = 1057;
update sys_menu set menu_name = '导入代码' where menu_id = 1058;
update sys_menu set menu_name = '预览代码' where menu_id = 1059;
update sys_menu set menu_name = '生成代码' where menu_id = 1060;
update sys_menu set menu_name = '客户端管理查询' where menu_id = 1061;
update sys_menu set menu_name = '客户端管理新增' where menu_id = 1062;
update sys_menu set menu_name = '客户端管理修改' where menu_id = 1063;
update sys_menu set menu_name = '客户端管理删除' where menu_id = 1064;
update sys_menu set menu_name = '客户端管理导出' where menu_id = 1065;
update sys_menu set menu_name = '测试单表' where menu_id = 1500;
update sys_menu set menu_name = '测试树表' where menu_id = 1506;
update sys_menu set menu_name = '文件查询' where menu_id = 1600;
update sys_menu set menu_name = '文件上传' where menu_id = 1601;
update sys_menu set menu_name = '文件下载' where menu_id = 1602;
update sys_menu set menu_name = '文件删除' where menu_id = 1603;
update sys_menu set menu_name = '客户档案查询' where menu_id = 1606;
update sys_menu set menu_name = '客户档案新增' where menu_id = 1607;
update sys_menu set menu_name = '客户档案修改' where menu_id = 1608;
update sys_menu set menu_name = '客户档案删除' where menu_id = 1609;
update sys_menu set menu_name = '客户档案导出' where menu_id = 1610;
update sys_menu set menu_name = '套餐查询', parent_id = 122, perms = 'system:tenantPackage:query', visible = '0', status = '0' where menu_id = 1611;
update sys_menu set menu_name = '套餐新增', parent_id = 122, perms = 'system:tenantPackage:add', visible = '0', status = '0' where menu_id = 1612;
update sys_menu set menu_name = '套餐修改', parent_id = 122, perms = 'system:tenantPackage:edit', visible = '0', status = '0' where menu_id = 1613;
update sys_menu set menu_name = '套餐删除', parent_id = 122, perms = 'system:tenantPackage:remove', visible = '0', status = '0' where menu_id = 1614;
update sys_menu set menu_name = '套餐导出', parent_id = 122, perms = 'system:tenantPackage:export', visible = '0', status = '0' where menu_id = 1615;
update sys_menu set menu_name = '配置列表' where menu_id = 1620;
update sys_menu set menu_name = '配置添加' where menu_id = 1621;
update sys_menu set menu_name = '配置编辑' where menu_id = 1622;
update sys_menu set menu_name = '配置删除' where menu_id = 1623;

update sys_dict_type set dict_name = '用户性别', remark = '用户性别列表' where dict_type = 'sys_user_sex';
update sys_dict_type set dict_name = '菜单状态', remark = '菜单状态列表' where dict_type = 'sys_show_hide';
update sys_dict_type set dict_name = '系统开关', remark = '系统开关列表' where dict_type = 'sys_normal_disable';
update sys_dict_type set dict_name = '系统是否', remark = '系统是否列表' where dict_type = 'sys_yes_no';
update sys_dict_type set dict_name = '通知类型', remark = '通知类型列表' where dict_type = 'sys_notice_type';
update sys_dict_type set dict_name = '通知状态', remark = '通知状态列表' where dict_type = 'sys_notice_status';
update sys_dict_type set dict_name = '操作类型', remark = '操作类型列表' where dict_type = 'sys_oper_type';
update sys_dict_type set dict_name = '系统状态', remark = '登录状态列表' where dict_type = 'sys_common_status';
update sys_dict_type set dict_name = '授权类型', remark = '认证授权类型' where dict_type = 'sys_grant_type';
update sys_dict_type set dict_name = '设备类型', remark = '客户端设备类型' where dict_type = 'sys_device_type';
update sys_dict_type set dict_name = '业务状态', remark = '工作流业务状态' where dict_type = 'wf_business_status';
update sys_dict_type set dict_name = '表单类型', remark = '工作流表单类型' where dict_type = 'wf_form_type';
update sys_dict_type set dict_name = '任务状态', remark = '工作流任务状态' where dict_type = 'wf_task_status';

update sys_dict_data set dict_label = '男' where dict_type = 'sys_user_sex' and dict_value = '0';
update sys_dict_data set dict_label = '女' where dict_type = 'sys_user_sex' and dict_value = '1';
update sys_dict_data set dict_label = '未知' where dict_type = 'sys_user_sex' and dict_value = '2';
update sys_dict_data set dict_label = '显示' where dict_type = 'sys_show_hide' and dict_value = '0';
update sys_dict_data set dict_label = '隐藏' where dict_type = 'sys_show_hide' and dict_value = '1';
update sys_dict_data set dict_label = '正常' where dict_type in ('sys_normal_disable', 'sys_common_status') and dict_value = '0';
update sys_dict_data set dict_label = '停用' where dict_type in ('sys_normal_disable', 'sys_common_status') and dict_value = '1';
update sys_dict_data set dict_label = '是' where dict_type = 'sys_yes_no' and dict_value = 'Y';
update sys_dict_data set dict_label = '否' where dict_type = 'sys_yes_no' and dict_value = 'N';
update sys_dict_data set dict_label = '通知' where dict_type = 'sys_notice_type' and dict_value = '1';
update sys_dict_data set dict_label = '公告' where dict_type = 'sys_notice_type' and dict_value = '2';
update sys_dict_data set dict_label = '正常' where dict_type = 'sys_notice_status' and dict_value = '0';
update sys_dict_data set dict_label = '关闭' where dict_type = 'sys_notice_status' and dict_value = '1';
update sys_dict_data set dict_label = '其它' where dict_type = 'sys_oper_type' and dict_value = '0';
update sys_dict_data set dict_label = '新增' where dict_type = 'sys_oper_type' and dict_value = '1';
update sys_dict_data set dict_label = '修改' where dict_type = 'sys_oper_type' and dict_value = '2';
update sys_dict_data set dict_label = '删除' where dict_type = 'sys_oper_type' and dict_value = '3';
update sys_dict_data set dict_label = '授权' where dict_type = 'sys_oper_type' and dict_value = '4';
update sys_dict_data set dict_label = '导出' where dict_type = 'sys_oper_type' and dict_value = '5';
update sys_dict_data set dict_label = '导入' where dict_type = 'sys_oper_type' and dict_value = '6';
update sys_dict_data set dict_label = '强退' where dict_type = 'sys_oper_type' and dict_value = '7';
update sys_dict_data set dict_label = '生成代码' where dict_type = 'sys_oper_type' and dict_value = '8';
update sys_dict_data set dict_label = '清空数据' where dict_type = 'sys_oper_type' and dict_value = '9';
update sys_dict_data set dict_label = '授权码模式' where dict_type = 'sys_grant_type' and dict_value = 'authorization_code';
update sys_dict_data set dict_label = '密码模式' where dict_type = 'sys_grant_type' and dict_value = 'password';
update sys_dict_data set dict_label = '客户端模式' where dict_type = 'sys_grant_type' and dict_value = 'client_credentials';
update sys_dict_data set dict_label = '小程序模式' where dict_type = 'sys_grant_type' and dict_value = 'xcx';
update sys_dict_data set dict_label = 'PC' where dict_type = 'sys_device_type' and dict_value = 'pc';
update sys_dict_data set dict_label = 'APP' where dict_type = 'sys_device_type' and dict_value = 'app';

update sys_notice set notice_title = '温馨提醒：2018-07-01 新版本发布啦', notice_content = '新版本内容', remark = '管理员' where notice_id = 1;
update sys_notice set notice_title = '维护通知：2018-07-01 系统凌晨维护', notice_content = '维护内容', remark = '管理员' where notice_id = 2;

insert into sys_tenant_package
(package_id, package_name, menu_ids, remark, menu_check_strictly, status, del_flag, create_dept, create_by, create_time, update_by, update_time)
values
(1001, '标准版', '910100,910101,910102,910107,910136,910103,910104,910121,910105,910106,910131,910126', '默认业务套餐：开放基础客户、License、因子、模板与维表同步能力', 1, '0', '0', 103, 1, sysdate(), null, null),
(1002, '专业版', '910100,910101,910102,910107,910136,910103,910104,910121,910105,910106,910131,910126', '默认业务套餐：在标准版基础上承载更完整的数据开放范围', 1, '0', '0', 103, 1, sysdate(), null, null),
(1003, '集团版', '910100,910101,910102,910107,910136,910103,910104,910121,910105,910106,910131,910126,1,100,101,102,103,104,105,106,107,122', '默认业务套餐：集团客户使用，开放全部厂商数据管理能力', 1, '0', '0', 103, 1, sysdate(), null, null)
on duplicate key update
    package_name = values(package_name),
    menu_ids = values(menu_ids),
    remark = values(remark),
    menu_check_strictly = values(menu_check_strictly),
    status = values(status),
    del_flag = values(del_flag),
    update_by = 1,
    update_time = sysdate();

set @schema_name = database();

set @sql = if(
    exists(select 1 from information_schema.columns where table_schema = @schema_name and table_name = 'cv_license_issue' and column_name = 'package_id'),
    'select 1',
    'alter table cv_license_issue add column package_id bigint default null after customer_id'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.columns where table_schema = @schema_name and table_name = 'cv_license_issue' and column_name = 'package_name'),
    'select 1',
    'alter table cv_license_issue add column package_name varchar(64) default null after package_id'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.statistics where table_schema = @schema_name and table_name = 'cv_license_issue' and index_name = 'idx_cv_license_issue_package'),
    'select 1',
    'alter table cv_license_issue add key idx_cv_license_issue_package (package_id)'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.columns where table_schema = @schema_name and table_name = 'cv_factor_customer_scope' and column_name = 'package_id'),
    'select 1',
    'alter table cv_factor_customer_scope add column package_id bigint default null after customer_id'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.columns where table_schema = @schema_name and table_name = 'cv_factor_customer_scope' and column_name = 'package_name'),
    'select 1',
    'alter table cv_factor_customer_scope add column package_name varchar(64) default null after package_id'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.columns where table_schema = @schema_name and table_name = 'cv_factor_customer_scope' and column_name = 'scope_package_key'),
    'select 1',
    'alter table cv_factor_customer_scope add column scope_package_key bigint generated always as (ifnull(package_id, 0)) stored after scope_customer_key'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.statistics where table_schema = @schema_name and table_name = 'cv_factor_customer_scope' and index_name = 'idx_cv_factor_scope_package_lookup'),
    'select 1',
    'alter table cv_factor_customer_scope add key idx_cv_factor_scope_package_lookup (version_id, scope_status, customer_id, package_id, edition)'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.columns where table_schema = @schema_name and table_name = 'cv_report_template_scope' and column_name = 'package_id'),
    'select 1',
    'alter table cv_report_template_scope add column package_id bigint default null after customer_id'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.columns where table_schema = @schema_name and table_name = 'cv_report_template_scope' and column_name = 'package_name'),
    'select 1',
    'alter table cv_report_template_scope add column package_name varchar(64) default null after package_id'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.columns where table_schema = @schema_name and table_name = 'cv_report_template_scope' and column_name = 'edition'),
    'select 1',
    'alter table cv_report_template_scope add column edition varchar(64) default null after license_id'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.columns where table_schema = @schema_name and table_name = 'cv_report_template_scope' and column_name = 'scope_package_key'),
    'select 1',
    'alter table cv_report_template_scope add column scope_package_key bigint generated always as (ifnull(package_id, 0)) stored after scope_customer_key'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.columns where table_schema = @schema_name and table_name = 'cv_report_template_scope' and column_name = 'scope_edition_key'),
    'select 1',
    'alter table cv_report_template_scope add column scope_edition_key varchar(64) generated always as (ifnull(edition, '''')) stored after scope_package_key'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.statistics where table_schema = @schema_name and table_name = 'cv_report_template_scope' and index_name = 'idx_cv_report_template_scope_package_lookup'),
    'select 1',
    'alter table cv_report_template_scope add key idx_cv_report_template_scope_package_lookup (template_id, scope_status, customer_id, package_id, edition, license_id)'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.columns where table_schema = @schema_name and table_name = 'cv_renewal_order' and column_name = 'requested_package_id'),
    'select 1',
    'alter table cv_renewal_order add column requested_package_id bigint default null after install_id'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.columns where table_schema = @schema_name and table_name = 'cv_renewal_order' and column_name = 'requested_package_name'),
    'select 1',
    'alter table cv_renewal_order add column requested_package_name varchar(64) default null after requested_package_id'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.columns where table_schema = @schema_name and table_name = 'cv_renewal_order' and column_name = 'issue_status'),
    'select 1',
    'alter table cv_renewal_order add column issue_status varchar(32) not null default ''pending_issue'' after order_status'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @sql = if(
    exists(select 1 from information_schema.statistics where table_schema = @schema_name and table_name = 'cv_renewal_order' and index_name = 'idx_cv_renewal_order_package'),
    'select 1',
    'alter table cv_renewal_order add key idx_cv_renewal_order_package (requested_package_id)'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update cv_license_issue issue
join sys_tenant_package pkg on lower(pkg.package_name) = lower(issue.edition) and pkg.del_flag = '0'
set issue.package_id = pkg.package_id,
    issue.package_name = pkg.package_name
where issue.package_id is null;

update cv_license_issue issue
join sys_tenant_package pkg on pkg.package_name = case lower(issue.edition)
    when 'standard' then '标准版'
    when 'professional' then '专业版'
    when 'pro' then '专业版'
    when 'enterprise' then '集团版'
    when 'group' then '集团版'
    else null
end and pkg.del_flag = '0'
set issue.package_id = pkg.package_id,
    issue.package_name = pkg.package_name,
    issue.edition = pkg.package_name
where issue.package_id is null
  and issue.edition is not null;

update cv_factor_customer_scope scope
join sys_tenant_package pkg on lower(pkg.package_name) = lower(scope.edition) and pkg.del_flag = '0'
set scope.package_id = pkg.package_id,
    scope.package_name = pkg.package_name
where scope.package_id is null;

update cv_factor_customer_scope scope
join sys_tenant_package pkg on pkg.package_name = case lower(scope.edition)
    when 'standard' then '标准版'
    when 'professional' then '专业版'
    when 'pro' then '专业版'
    when 'enterprise' then '集团版'
    when 'group' then '集团版'
    else null
end and pkg.del_flag = '0'
set scope.package_id = pkg.package_id,
    scope.package_name = pkg.package_name,
    scope.edition = pkg.package_name
where scope.package_id is null
  and scope.edition is not null;

update cv_report_template_scope scope
join sys_tenant_package pkg on lower(pkg.package_name) = lower(scope.edition) and pkg.del_flag = '0'
set scope.package_id = pkg.package_id,
    scope.package_name = pkg.package_name
where scope.package_id is null;

update cv_report_template_scope scope
join sys_tenant_package pkg on pkg.package_name = case lower(scope.edition)
    when 'standard' then '标准版'
    when 'professional' then '专业版'
    when 'pro' then '专业版'
    when 'enterprise' then '集团版'
    when 'group' then '集团版'
    else null
end and pkg.del_flag = '0'
set scope.package_id = pkg.package_id,
    scope.package_name = pkg.package_name,
    scope.edition = pkg.package_name
where scope.package_id is null
  and scope.edition is not null;

update cv_renewal_order renewal
join sys_tenant_package pkg on lower(pkg.package_name) = lower(renewal.requested_edition) and pkg.del_flag = '0'
set renewal.requested_package_id = pkg.package_id,
    renewal.requested_package_name = pkg.package_name
where renewal.requested_package_id is null;

update cv_renewal_order renewal
join sys_tenant_package pkg on pkg.package_name = case lower(renewal.requested_edition)
    when 'standard' then '标准版'
    when 'professional' then '专业版'
    when 'pro' then '专业版'
    when 'enterprise' then '集团版'
    when 'group' then '集团版'
    else null
end and pkg.del_flag = '0'
set renewal.requested_package_id = pkg.package_id,
    renewal.requested_package_name = pkg.package_name,
    renewal.requested_edition = pkg.package_name
where renewal.requested_package_id is null
  and renewal.requested_edition is not null;
