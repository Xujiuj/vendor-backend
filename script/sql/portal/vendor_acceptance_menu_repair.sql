-- Repair browser-acceptance menu drift after local vendor DB resets.
-- Scope: vendor database only.

update sys_menu
set component = 'vendor/licenseIssue/index',
    perms = 'vendor:licenseIssue:list',
    update_time = sysdate()
where menu_id = 910102;

select menu_id, menu_name, parent_id, path, component, perms
from sys_menu
where menu_id = 910102;
