-- Align existing vendor databases with the current report-template entitlement model.
-- Old imports may still have a CHECK constraint that only accepts customer/license
-- scopes, while the business flow now also supports package and edition scopes.

set @schema_name = database();

set @constraint_exists = (
    select count(1)
    from information_schema.check_constraints
    where constraint_schema = @schema_name
      and constraint_name = 'chk_cv_report_template_scope_entitlement'
);

set @sql = if(
    @constraint_exists > 0,
    'alter table cv_report_template_scope drop check chk_cv_report_template_scope_entitlement',
    'select 1'
);
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

alter table cv_report_template_scope
    add constraint chk_cv_report_template_scope_entitlement
    check (customer_id is not null or package_id is not null or edition is not null or license_id is not null);
