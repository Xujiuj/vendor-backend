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
(910100, '鍘傚晢杩愯惀', 0, 1, 'vendor', 'Layout', '', 1, 0, 'M', '0', '0', '', 'guide', 103, 1, sysdate(), null, null, '鍘傚晢绔?portal 杩愯惀鑿滃崟');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910136, '鏁版嵁绠＄悊', 0, 2, 'data-management', 'Layout', '', 1, 0, 'M', '0', '0', '', 'database', 103, 1, sysdate(), null, null, '浼佷笟绔敵璇蜂笌鍚屾鐨勬暟鎹簮绠＄悊鐩綍');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910108, '杩愯惀鎬昏', 910100, 0, 'overview', '', '', 1, 0, 'F', '1', '0', 'vendor:overview:query', '#', 103, 1, sysdate(), null, null, '鍘傚晢棣栭〉杩愯惀鎬昏鎺ュ彛鏉冮檺');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910101, '瀹㈡埛妗ｆ', 910100, 1, 'customer', 'vendor/customer/index', '', 1, 0, 'C', '0', '0', 'vendor:customer:list', 'peoples', 103, 1, sysdate(), null, null, '鍘傚晢瀹㈡埛妗ｆ锛岃繛鎺?vendor/customer 鐪熷疄鍚庣鎺ュ彛');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910102, 'License 鎺堟潈绠＄悊', 910100, 2, 'license', 'vendor/licenseIssue/index', '', 1, 0, 'C', '0', '0', 'vendor:licenseIssue:list', 'lock', 103, 1, sysdate(), null, null, '鍘傚晢 License 鎺堟潈绛惧彂銆佸悐閿€涓庨噸绛惧叆鍙?);

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910103, '鍥犲瓙鐗堟湰', 910136, 1, 'factor-version', 'vendor/factorVersion/index', '', 1, 0, 'C', '0', '0', 'vendor:factorVersion:list', 'tree-table', 103, 1, sysdate(), null, null, '鍘傚晢鍥犲瓙鐗堟湰鍙戝竷銆佸喕缁撲笌鐢熷懡鍛ㄦ湡绠＄悊');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910121, '鍥犲瓙寮€鏀捐寖鍥?, 910136, 2, 'factor-scope', 'vendor/factorScope/index', '', 1, 0, 'C', '0', '0', 'vendor:factorCustomerScope:list', 'tree', 103, 1, sysdate(), null, null, '鍘傚晢鍥犲瓙鎸夌増鏈€佸鎴蜂笌濂楅鐗堟湰鎺у埗寮€鏀捐寖鍥达紝License 閫氳繃浼佷笟璐拱鐗堟湰缁ф壙鑼冨洿');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910105, '妯℃澘搴?, 910136, 3, 'report-template', 'vendor/reportTemplate/index', '', 1, 0, 'C', '0', '0', 'vendor:reportTemplate:list', 'form', 103, 1, sysdate(), null, null, '鍘傚晢鎶ヨ〃妯℃澘搴撲笌鍙戝竷绠＄悊');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910106, '妯℃澘鍒嗗彂', 910136, 4, 'template-scope', 'vendor/templateScope/index', '', 1, 0, 'C', '0', '0', 'vendor:reportTemplateScope:list', 'share', 103, 1, sysdate(), null, null, '鍘傚晢妯℃澘鎸夊鎴蜂笌 License 鍒嗗彂鑼冨洿');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910131, '缁磋〃绠＄悊', 910136, 5, 'dimension', 'vendor/dimension/index', '', 1, 0, 'C', '0', '0', 'vendor:dimension:list', 'tree', 103, 1, sysdate(), null, null, '鍘傚晢缁磋〃绠＄悊锛屼緵浼佷笟绔€氳繃寮€鏀炬帴鍙ｈ鍙栧巶鍟嗘潵婧愬熀纭€鏁版嵁');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910126, '鍏憡绠＄悊', 910136, 6, 'announcement', 'vendor/announcement/index', '', 1, 0, 'C', '0', '0', 'vendor:announcement:list', 'message', 103, 1, sysdate(), null, null, '鍘傚晢鍏憡绠＄悊锛屼緵浼佷笟绔伐浣滃彴鎸?License 鎺堟潈鍚屾璇诲彇');

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910107, '缁垂璁㈠崟', 910100, 10, 'renewal-order', 'vendor/renewalOrder/index', '', 1, 0, 'C', '0', '0', 'vendor:renewalOrder:list', 'money', 103, 1, sysdate(), null, null, '鍘傚晢缁垂璁㈠崟涓庢敮浠樺洖璋冭繍钀?);

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values
(910109, '瀹㈡埛妗ｆ璇︽儏', 910101, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:customer:query', '#', 103, 1, sysdate(), null, null, '瀹㈡埛妗ｆ璇︽儏鏉冮檺'),
(910148, '瀹㈡埛妗ｆ鏂板', 910101, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:customer:add', '#', 103, 1, sysdate(), null, null, '瀹㈡埛妗ｆ鏂板鏉冮檺'),
(910149, '瀹㈡埛妗ｆ缂栬緫', 910101, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:customer:edit', '#', 103, 1, sysdate(), null, null, '瀹㈡埛妗ｆ缂栬緫鏉冮檺'),
(910150, '瀹㈡埛妗ｆ鍒犻櫎', 910101, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:customer:remove', '#', 103, 1, sysdate(), null, null, '瀹㈡埛妗ｆ鍒犻櫎鏉冮檺'),
(910151, '瀹㈡埛妗ｆ瀵煎嚭', 910101, 5, '', '', '', 1, 0, 'F', '0', '0', 'vendor:customer:export', '#', 103, 1, sysdate(), null, null, '瀹㈡埛妗ｆ瀵煎嚭鏉冮檺'),
(910110, '鍥犲瓙鐗堟湰璇︽儏', 910103, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:query', '#', 103, 1, sysdate(), null, null, '鍥犲瓙鐗堟湰璇︽儏鏉冮檺'),
(910145, '鍥犲瓙鐗堟湰鏂板', 910103, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:add', '#', 103, 1, sysdate(), null, null, '鍥犲瓙鐗堟湰鏂板鏉冮檺'),
(910146, '鍥犲瓙鐗堟湰缂栬緫', 910103, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:edit', '#', 103, 1, sysdate(), null, null, '鍥犲瓙鐗堟湰缂栬緫鏉冮檺'),
(910147, '鍥犲瓙鐗堟湰鍒犻櫎', 910103, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:remove', '#', 103, 1, sysdate(), null, null, '鍥犲瓙鐗堟湰鍒犻櫎鏉冮檺'),
(910152, '鍥犲瓙鐗堟湰鍙戝竷', 910103, 5, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:publish', '#', 103, 1, sysdate(), null, null, '鍥犲瓙鐗堟湰鍙戝竷鏉冮檺'),
(910153, '鍥犲瓙鐗堟湰鍐荤粨', 910103, 6, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:freeze', '#', 103, 1, sysdate(), null, null, '鍥犲瓙鐗堟湰鍐荤粨鏉冮檺'),
(910154, '鍥犲瓙鐗堟湰閫€褰?, 910103, 7, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:retire', '#', 103, 1, sysdate(), null, null, '鍥犲瓙鐗堟湰閫€褰规潈闄?),
(910155, '鍥犲瓙鐗堟湰鎭㈠', 910103, 8, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorVersion:restore', '#', 103, 1, sysdate(), null, null, '鍥犲瓙鐗堟湰鎭㈠鏉冮檺'),
(910124, '鍥犲瓙寮€鏀捐寖鍥磋鎯?, 910121, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorCustomerScope:query', '#', 103, 1, sysdate(), null, null, '鍥犲瓙寮€鏀捐寖鍥磋鎯呮潈闄?),
(910137, '鍥犲瓙寮€鏀捐寖鍥存柊澧?, 910121, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorCustomerScope:add', '#', 103, 1, sysdate(), null, null, '鍥犲瓙寮€鏀捐寖鍥存柊澧炴潈闄?),
(910138, '鍥犲瓙寮€鏀捐寖鍥寸紪杈?, 910121, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorCustomerScope:edit', '#', 103, 1, sysdate(), null, null, '鍥犲瓙寮€鏀捐寖鍥寸紪杈戞潈闄?),
(910125, '鍥犲瓙寮€鏀捐寖鍥村垹闄?, 910121, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:factorCustomerScope:remove', '#', 103, 1, sysdate(), null, null, '鍥犲瓙寮€鏀捐寖鍥村垹闄ゆ潈闄?),
(910113, '妯℃澘搴撹鎯?, 910105, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:query', '#', 103, 1, sysdate(), null, null, '妯℃澘搴撹鎯呮潈闄?),
(910114, '妯℃澘搴撴柊澧?, 910105, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:add', '#', 103, 1, sysdate(), null, null, '妯℃澘搴撴柊澧炴潈闄?),
(910115, '妯℃澘搴撶紪杈?, 910105, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:edit', '#', 103, 1, sysdate(), null, null, '妯℃澘搴撶紪杈戞潈闄?),
(910116, '妯℃澘搴撳垹闄?, 910105, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplate:remove', '#', 103, 1, sysdate(), null, null, '妯℃澘搴撳垹闄ゆ潈闄?),
(910117, '妯℃澘鍒嗗彂璇︽儏', 910106, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplateScope:query', '#', 103, 1, sysdate(), null, null, '妯℃澘鍒嗗彂璇︽儏鏉冮檺'),
(910139, '妯℃澘鍒嗗彂鏂板', 910106, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplateScope:add', '#', 103, 1, sysdate(), null, null, '妯℃澘鍒嗗彂鏂板鏉冮檺'),
(910140, '妯℃澘鍒嗗彂缂栬緫', 910106, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplateScope:edit', '#', 103, 1, sysdate(), null, null, '妯℃澘鍒嗗彂缂栬緫鏉冮檺'),
(910118, '妯℃澘鍒嗗彂鍒犻櫎', 910106, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:reportTemplateScope:remove', '#', 103, 1, sysdate(), null, null, '妯℃澘鍒嗗彂鍒犻櫎鏉冮檺'),
(910132, '缁磋〃璇︽儏', 910131, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:query', '#', 103, 1, sysdate(), null, null, '缁磋〃璇︽儏鏉冮檺'),
(910133, '缁磋〃鏂板', 910131, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:add', '#', 103, 1, sysdate(), null, null, '缁磋〃鏂板鏉冮檺'),
(910134, '缁磋〃缂栬緫', 910131, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:edit', '#', 103, 1, sysdate(), null, null, '缁磋〃缂栬緫鏉冮檺'),
(910135, '缁磋〃鍒犻櫎', 910131, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:dimension:remove', '#', 103, 1, sysdate(), null, null, '缁磋〃鍒犻櫎鏉冮檺'),
(910127, '鍏憡璇︽儏', 910126, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:query', '#', 103, 1, sysdate(), null, null, '鍏憡璇︽儏鏉冮檺'),
(910128, '鍏憡鏂板', 910126, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:add', '#', 103, 1, sysdate(), null, null, '鍏憡鏂板鏉冮檺'),
(910129, '鍏憡缂栬緫', 910126, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:edit', '#', 103, 1, sysdate(), null, null, '鍏憡缂栬緫鏉冮檺'),
(910130, '鍏憡鍒犻櫎', 910126, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:announcement:remove', '#', 103, 1, sysdate(), null, null, '鍏憡鍒犻櫎鏉冮檺'),
(910119, '缁垂璁㈠崟璇︽儏', 910107, 1, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:query', '#', 103, 1, sysdate(), null, null, '缁垂璁㈠崟璇︽儏鏉冮檺'),
(910141, '缁垂璁㈠崟鏂板', 910107, 2, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:add', '#', 103, 1, sysdate(), null, null, '缁垂璁㈠崟鏂板鏉冮檺'),
(910142, '缁垂璁㈠崟缂栬緫', 910107, 3, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:edit', '#', 103, 1, sysdate(), null, null, '缁垂璁㈠崟缂栬緫鏉冮檺'),
(910143, '缁垂璁㈠崟鍥炶皟', 910107, 4, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:callback', '#', 103, 1, sysdate(), null, null, '缁垂璁㈠崟鍥炶皟鏉冮檺'),
(910144, '缁垂璁㈠崟閲嶈瘯绛惧彂', 910107, 5, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:retryIssue', '#', 103, 1, sysdate(), null, null, '缁垂璁㈠崟閲嶈瘯绛惧彂鏉冮檺'),
(910120, '缁垂璁㈠崟鍒犻櫎', 910107, 6, '', '', '', 1, 0, 'F', '0', '0', 'vendor:renewalOrder:remove', '#', 103, 1, sysdate(), null, null, '缁垂璁㈠崟鍒犻櫎鏉冮檺');

-- Keep selected RuoYi native system management and log routes available.
-- Some vendor databases are initialized without the native RuoYi menu seed.
-- Insert the basic operation surface first, then normalize existing rows below.
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
(1, '绯荤粺绠＄悊', 0, 3, 'system', null, '', 1, 0, 'M', '0', '0', '', 'system', 103, 1, sysdate(), null, null, '鍘傚晢绔郴缁熺鐞嗙洰褰?),
(100, '鐢ㄦ埛绠＄悊', 1, 1, 'user', 'system/user/index', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 103, 1, sysdate(), null, null, '鍘傚晢绔敤鎴风鐞嗚彍鍗?),
(101, '瑙掕壊绠＄悊', 1, 2, 'role', 'system/role/index', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 103, 1, sysdate(), null, null, '鍘傚晢绔鑹茬鐞嗚彍鍗?),
(103, '閮ㄩ棬绠＄悊', 1, 4, 'dept', 'system/dept/index', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 103, 1, sysdate(), null, null, '鍘傚晢绔儴闂ㄧ鐞嗚彍鍗?),
(104, '宀椾綅绠＄悊', 1, 5, 'post', 'system/post/index', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 103, 1, sysdate(), null, null, '鍘傚晢绔矖浣嶇鐞嗚彍鍗?),
(122, '濂楅绠＄悊', 1, 8, 'tenantPackage', 'system/tenantPackage/index', '', 1, 0, 'C', '0', '0', 'system:tenantPackage:list', 'form', 103, 1, sysdate(), null, null, '鍘傚晢绔郴缁熷椁愮鐞嗭紝鐢ㄤ簬閰嶇疆瑙掕壊鍙敤鑿滃崟鑼冨洿'),
(107, '鍏憡閰嶇疆', 1, 9, 'notice', 'system/notice/index', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 103, 1, sysdate(), null, null, '鍘傚晢绔叕鍛婇厤缃彍鍗?),
(108, '鏃ュ織绠＄悊', 0, 4, 'monitor', 'Layout', '', 1, 0, 'M', '0', '0', '', 'log', 103, 1, sysdate(), null, null, '鍘傚晢绔棩蹇楃鐞嗙洰褰?),
(500, '鎿嶄綔鏃ュ織', 108, 1, 'operlog', 'monitor/operlog/index', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list', 'form', 103, 1, sysdate(), null, null, '鍘傚晢绔搷浣滄棩蹇楄彍鍗?),
(501, '鐧诲綍鏃ュ織', 108, 2, 'logininfor', 'monitor/logininfor/index', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 103, 1, sysdate(), null, null, '鍘傚晢绔櫥褰曟棩蹇楄彍鍗?),
(130, '鍒嗛厤鐢ㄦ埛', 101, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 103, 1, sysdate(), null, null, '瑙掕壊鍒嗛厤鐢ㄦ埛鏉冮檺'),
(131, '鍒嗛厤瑙掕壊', 100, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 103, 1, sysdate(), null, null, '鐢ㄦ埛鍒嗛厤瑙掕壊鏉冮檺'),
(1001, '鐢ㄦ埛鏌ヨ', 100, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 103, 1, sysdate(), null, null, '鐢ㄦ埛鏌ヨ鏉冮檺'),
(1002, '鐢ㄦ埛鏂板', 100, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 103, 1, sysdate(), null, null, '鐢ㄦ埛鏂板鏉冮檺'),
(1003, '鐢ㄦ埛淇敼', 100, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 103, 1, sysdate(), null, null, '鐢ㄦ埛淇敼鏉冮檺'),
(1004, '鐢ㄦ埛鍒犻櫎', 100, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 103, 1, sysdate(), null, null, '鐢ㄦ埛鍒犻櫎鏉冮檺'),
(1005, '鐢ㄦ埛瀵煎嚭', 100, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 103, 1, sysdate(), null, null, '鐢ㄦ埛瀵煎嚭鏉冮檺'),
(1006, '鐢ㄦ埛瀵煎叆', 100, 6, '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 103, 1, sysdate(), null, null, '鐢ㄦ埛瀵煎叆鏉冮檺'),
(1007, '閲嶇疆瀵嗙爜', 100, 7, '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 103, 1, sysdate(), null, null, '閲嶇疆瀵嗙爜鏉冮檺'),
(1008, '瑙掕壊鏌ヨ', 101, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 103, 1, sysdate(), null, null, '瑙掕壊鏌ヨ鏉冮檺'),
(1009, '瑙掕壊鏂板', 101, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 103, 1, sysdate(), null, null, '瑙掕壊鏂板鏉冮檺'),
(1010, '瑙掕壊淇敼', 101, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 103, 1, sysdate(), null, null, '瑙掕壊淇敼鏉冮檺'),
(1011, '瑙掕壊鍒犻櫎', 101, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 103, 1, sysdate(), null, null, '瑙掕壊鍒犻櫎鏉冮檺'),
(1012, '瑙掕壊瀵煎嚭', 101, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 103, 1, sysdate(), null, null, '瑙掕壊瀵煎嚭鏉冮檺'),
(1017, '閮ㄩ棬鏌ヨ', 103, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 103, 1, sysdate(), null, null, '閮ㄩ棬鏌ヨ鏉冮檺'),
(1018, '閮ㄩ棬鏂板', 103, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 103, 1, sysdate(), null, null, '閮ㄩ棬鏂板鏉冮檺'),
(1019, '閮ㄩ棬淇敼', 103, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 103, 1, sysdate(), null, null, '閮ㄩ棬淇敼鏉冮檺'),
(1020, '閮ㄩ棬鍒犻櫎', 103, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 103, 1, sysdate(), null, null, '閮ㄩ棬鍒犻櫎鏉冮檺'),
(1021, '宀椾綅鏌ヨ', 104, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 103, 1, sysdate(), null, null, '宀椾綅鏌ヨ鏉冮檺'),
(1022, '宀椾綅鏂板', 104, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 103, 1, sysdate(), null, null, '宀椾綅鏂板鏉冮檺'),
(1023, '宀椾綅淇敼', 104, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 103, 1, sysdate(), null, null, '宀椾綅淇敼鏉冮檺'),
(1024, '宀椾綅鍒犻櫎', 104, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 103, 1, sysdate(), null, null, '宀椾綅鍒犻櫎鏉冮檺'),
(1025, '宀椾綅瀵煎嚭', 104, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 103, 1, sysdate(), null, null, '宀椾綅瀵煎嚭鏉冮檺'),
(1036, '鍏憡鏌ヨ', 107, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 103, 1, sysdate(), null, null, '鍏憡鏌ヨ鏉冮檺'),
(1037, '鍏憡鏂板', 107, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 103, 1, sysdate(), null, null, '鍏憡鏂板鏉冮檺'),
(1038, '鍏憡淇敼', 107, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 103, 1, sysdate(), null, null, '鍏憡淇敼鏉冮檺'),
(1039, '鍏憡鍒犻櫎', 107, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 103, 1, sysdate(), null, null, '鍏憡鍒犻櫎鏉冮檺'),
(1040, '鎿嶄綔鏌ヨ', 500, 1, '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 103, 1, sysdate(), null, null, '鎿嶄綔鏃ュ織鏌ヨ鏉冮檺'),
(1041, '鎿嶄綔鍒犻櫎', 500, 2, '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 103, 1, sysdate(), null, null, '鎿嶄綔鏃ュ織鍒犻櫎鏉冮檺'),
(1042, '鏃ュ織瀵煎嚭', 500, 3, '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 103, 1, sysdate(), null, null, '鎿嶄綔鏃ュ織瀵煎嚭鏉冮檺'),
(1043, '鐧诲綍鏌ヨ', 501, 1, '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 103, 1, sysdate(), null, null, '鐧诲綍鏃ュ織鏌ヨ鏉冮檺'),
(1044, '鐧诲綍鍒犻櫎', 501, 2, '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 103, 1, sysdate(), null, null, '鐧诲綍鏃ュ織鍒犻櫎鏉冮檺'),
(1045, '鏃ュ織瀵煎嚭', 501, 3, '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 103, 1, sysdate(), null, null, '鐧诲綍鏃ュ織瀵煎嚭鏉冮檺'),
(1611, '濂楅鏌ヨ', 122, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:query', '#', 103, 1, sysdate(), null, null, '濂楅鏌ヨ鏉冮檺'),
(1612, '濂楅鏂板', 122, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:add', '#', 103, 1, sysdate(), null, null, '濂楅鏂板鏉冮檺'),
(1613, '濂楅淇敼', 122, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:edit', '#', 103, 1, sysdate(), null, null, '濂楅淇敼鏉冮檺'),
(1614, '濂楅鍒犻櫎', 122, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:remove', '#', 103, 1, sysdate(), null, null, '濂楅鍒犻櫎鏉冮檺'),
(1615, '濂楅瀵煎嚭', 122, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:tenantPackage:export', '#', 103, 1, sysdate(), null, null, '濂楅瀵煎嚭鏉冮檺');

update sys_menu set menu_name = '绯荤粺绠＄悊', path = 'system', component = null, visible = '0', status = '0', remark = '鍘傚晢绔郴缁熺鐞嗙洰褰? where menu_id = 1;
update sys_menu set menu_name = '鐢ㄦ埛绠＄悊', parent_id = 1, order_num = 1, path = 'user', component = 'system/user/index', perms = 'system:user:list', icon = 'user', visible = '0', status = '0', remark = '鍘傚晢绔敤鎴风鐞嗚彍鍗? where menu_id = 100;
update sys_menu set menu_name = '瑙掕壊绠＄悊', parent_id = 1, order_num = 2, path = 'role', component = 'system/role/index', perms = 'system:role:list', icon = 'peoples', visible = '0', status = '0', remark = '鍘傚晢绔鑹茬鐞嗚彍鍗? where menu_id = 101;
update sys_menu set menu_name = '閮ㄩ棬绠＄悊', parent_id = 1, order_num = 4, path = 'dept', component = 'system/dept/index', perms = 'system:dept:list', icon = 'tree', visible = '0', status = '0', remark = '鍘傚晢绔儴闂ㄧ鐞嗚彍鍗? where menu_id = 103;
update sys_menu set menu_name = '宀椾綅绠＄悊', parent_id = 1, order_num = 5, path = 'post', component = 'system/post/index', perms = 'system:post:list', icon = 'post', visible = '0', status = '0', remark = '鍘傚晢绔矖浣嶇鐞嗚彍鍗? where menu_id = 104;
update sys_menu set menu_name = '鍏憡閰嶇疆', parent_id = 1, order_num = 9, path = 'notice', component = 'system/notice/index', perms = 'system:notice:list', icon = 'message', visible = '0', status = '0', remark = '鍘傚晢绔叕鍛婇厤缃彍鍗? where menu_id = 107;
update sys_menu set menu_name = '濂楅绠＄悊', parent_id = 1, order_num = 8, path = 'tenantPackage', component = 'system/tenantPackage/index', perms = 'system:tenantPackage:list', icon = 'form', visible = '0', status = '0', remark = '鍘傚晢绔郴缁熷椁愮鐞嗭紝鐢ㄤ簬閰嶇疆瑙掕壊鍙敤鑿滃崟鑼冨洿' where menu_id = 122;
update sys_menu set menu_name = '濂楅鏌ヨ', parent_id = 122, order_num = 1, perms = 'system:tenantPackage:query', visible = '0', status = '0' where menu_id = 1611;
update sys_menu set menu_name = '濂楅鏂板', parent_id = 122, order_num = 2, perms = 'system:tenantPackage:add', visible = '0', status = '0' where menu_id = 1612;
update sys_menu set menu_name = '濂楅淇敼', parent_id = 122, order_num = 3, perms = 'system:tenantPackage:edit', visible = '0', status = '0' where menu_id = 1613;
update sys_menu set menu_name = '濂楅鍒犻櫎', parent_id = 122, order_num = 4, perms = 'system:tenantPackage:remove', visible = '0', status = '0' where menu_id = 1614;
update sys_menu set menu_name = '濂楅瀵煎嚭', parent_id = 122, order_num = 5, perms = 'system:tenantPackage:export', visible = '0', status = '0' where menu_id = 1615;
update sys_menu set menu_name = '鏃ュ織绠＄悊', parent_id = 0, order_num = 4, path = 'monitor', component = 'Layout', perms = '', icon = 'log', visible = '0', status = '0', remark = '鍘傚晢绔棩蹇楃鐞嗙洰褰? where menu_id = 108;
update sys_menu set menu_name = '鎿嶄綔鏃ュ織', parent_id = 108, order_num = 1, path = 'operlog', component = 'monitor/operlog/index', perms = 'monitor:operlog:list', icon = 'form', visible = '0', status = '0', remark = '鍘傚晢绔搷浣滄棩蹇楄彍鍗? where menu_id = 500;
update sys_menu set menu_name = '鐧诲綍鏃ュ織', parent_id = 108, order_num = 2, path = 'logininfor', component = 'monitor/logininfor/index', perms = 'monitor:logininfor:list', icon = 'logininfor', visible = '0', status = '0', remark = '鍘傚晢绔櫥褰曟棩蹇楄彍鍗? where menu_id = 501;
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

-- Superadmin routing uses all enabled M/C menus and ignores role grants and
-- visible flags. Keep only the current vendor delivery directory active.
update sys_menu
set visible = '1',
    status = '1',
    update_time = sysdate()
where menu_type in ('M', 'C')
  and (
    menu_id in (2, 3, 4, 5, 6, 115, 116, 121)
    or path in (
      'tenant', 'demo',
      'oss', 'oss-config/index', 'client'
    )
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
  102, 105, 106, 115, 116, 132,
  1013, 1014, 1015, 1016,
  1026, 1027, 1028, 1029, 1030,
  1031, 1032, 1033, 1034, 1035,
  1055, 1056, 1057, 1058, 1059, 1060
)
and role_id <> 1;

-- Removed delivery-external RuoYi pages must not remain as database-backed
-- routes. Superadmin routing is status-based and can otherwise resolve deleted
-- frontend components.
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

-- Non-super-admin users only receive menus through sys_role_menu.
-- Seed every enabled vendor role with the vendor-owned business menus and the
-- administrative operation surface needed in the vendor portal.
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

-- Keep the built-in superadmin role fully assigned for role/menu
-- management screens and seeded database consistency.
insert into sys_role_menu (role_id, menu_id)
select 1, menu_id
from sys_menu m
where not exists (
  select 1
  from sys_role_menu existing
  where existing.role_id = 1
    and existing.menu_id = m.menu_id
);
