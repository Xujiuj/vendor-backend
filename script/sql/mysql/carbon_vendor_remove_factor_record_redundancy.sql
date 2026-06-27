-- Remove legacy vendor factor-record maintenance surface.
-- Source(A) factor master data now lives in strong typed dimension tables:
-- cv_electricity_factor, cv_electricity_factor_version,
-- cv_electricity_factor_scope and cv_greenhouse_gas.

DELETE FROM sys_role_menu
WHERE menu_id IN (910104, 910111, 910112, 910122, 910123);

DELETE FROM sys_menu
WHERE menu_id IN (910104, 910111, 910112, 910122, 910123)
   OR path = 'factor-record'
   OR component = 'vendor/factorRecord/index'
   OR perms LIKE 'vendor:factorRecord:%';

UPDATE sys_menu SET order_num = 2 WHERE menu_id = 910121;
UPDATE sys_menu SET order_num = 3 WHERE menu_id = 910105;
UPDATE sys_menu SET order_num = 4 WHERE menu_id = 910106;
UPDATE sys_menu SET order_num = 5 WHERE menu_id = 910131;
UPDATE sys_menu SET order_num = 6 WHERE menu_id = 910126;

DROP TABLE IF EXISTS cv_factor_record;
