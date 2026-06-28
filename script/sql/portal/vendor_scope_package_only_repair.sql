-- Align factor/template distribution scopes with the package-only entitlement model.
-- Run after backing up the vendor database. Non-package scopes are intentionally
-- removed because unpublished compatibility paths must not grant open API access.

DELETE FROM cv_factor_customer_scope
 WHERE package_id IS NULL
    OR customer_id IS NOT NULL
    OR edition IS NOT NULL
    OR license_id IS NOT NULL;

DELETE FROM cv_report_template_scope
 WHERE package_id IS NULL
    OR customer_id IS NOT NULL
    OR edition IS NOT NULL
    OR license_id IS NOT NULL;

ALTER TABLE cv_factor_customer_scope
    DROP INDEX uk_cv_factor_scope_entitlement,
    DROP INDEX idx_cv_factor_scope_lookup,
    DROP FOREIGN KEY fk_cv_factor_scope_customer,
    DROP COLUMN customer_id,
    DROP COLUMN edition,
    DROP COLUMN license_id,
    DROP COLUMN scope_customer_key,
    DROP COLUMN scope_package_key,
    DROP COLUMN scope_edition_key,
    DROP COLUMN scope_license_key,
    MODIFY package_id BIGINT NOT NULL,
    ADD UNIQUE KEY uk_cv_factor_scope_package (version_id, package_id),
    ADD KEY idx_cv_factor_scope_lookup (version_id, scope_status, package_id);

ALTER TABLE cv_report_template_scope
    DROP CHECK chk_cv_report_template_scope_entitlement;

ALTER TABLE cv_report_template_scope
    DROP INDEX uk_cv_report_template_scope,
    DROP INDEX idx_cv_report_template_scope_lookup,
    DROP FOREIGN KEY fk_cv_report_scope_customer,
    DROP COLUMN customer_id,
    DROP COLUMN edition,
    DROP COLUMN license_id,
    DROP COLUMN scope_customer_key,
    DROP COLUMN scope_package_key,
    DROP COLUMN scope_edition_key,
    DROP COLUMN scope_license_key,
    MODIFY package_id BIGINT NOT NULL,
    ADD UNIQUE KEY uk_cv_report_template_scope (template_id, package_id),
    ADD KEY idx_cv_report_template_scope_lookup (template_id, scope_status, package_id);
