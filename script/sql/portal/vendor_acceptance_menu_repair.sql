-- Repair browser-acceptance menu drift after local vendor DB resets.
-- Scope: vendor database only.

update sys_menu
set component = 'vendor/licenseIssue/index',
    perms = 'vendor:licenseIssue:list',
    update_time = sysdate()
where menu_id = 910102;

-- Production portal menu policy:
-- hide development/admin configuration utilities from final navigation.
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

select menu_id, menu_name, parent_id, path, component, perms, visible
from sys_menu
where menu_id in (102, 105, 106, 115, 116, 132, 910102)
order by menu_id;
