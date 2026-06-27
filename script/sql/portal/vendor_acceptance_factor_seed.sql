-- Acceptance-only vendor factor seed.
-- Source(A) factor master data is stored in strong typed vendor dimension
-- tables; /open/factors projects these tables into enterprise sync records.

insert into cv_customer
(customer_code, customer_name, contact_name, contact_email, contact_phone, customer_status, remark)
values
('ENT-DEMO-001', '演示企业', '碳数据管理员', 'carbon-admin@example.com', '13800000000', 'active', 'Enterprise demo account for open factor synchronization')
on duplicate key update
  customer_name = values(customer_name),
  contact_name = values(contact_name),
  contact_email = values(contact_email),
  contact_phone = values(contact_phone),
  customer_status = values(customer_status),
  remark = values(remark);

insert into cv_license_issue
(license_id, customer_id, package_id, package_name, key_id, algorithm, schema_version, edition, feature_codes,
 install_id, valid_from, valid_to, issue_status, issue_type, source_license_id, issued_by, issued_time,
 revoked_time, revoked_by, revoke_reason, license_payload, signature_text)
select
  'LIC-ENT-DEMO-001',
  customer.id,
  null,
  null,
  'acceptance-key',
  'none',
  '1.0',
  'standard',
  'factor-sync,dimension-sync,report-template-download',
  'INSTALL-ENT-DEMO-001',
  '2025-01-01 00:00:00',
  '2035-12-31 23:59:59',
  'issued',
  'manual',
  null,
  'seed',
  now(),
  null,
  null,
  null,
  '{"licenseId":"LIC-ENT-DEMO-001","features":["factor-sync","dimension-sync","report-template-download"]}',
  'seed-signature'
from cv_customer customer
where customer.customer_code = 'ENT-DEMO-001'
on duplicate key update
  customer_id = values(customer_id),
  package_id = values(package_id),
  package_name = values(package_name),
  key_id = values(key_id),
  algorithm = values(algorithm),
  schema_version = values(schema_version),
  edition = values(edition),
  feature_codes = values(feature_codes),
  install_id = values(install_id),
  valid_from = values(valid_from),
  valid_to = values(valid_to),
  issue_status = values(issue_status),
  issue_type = values(issue_type),
  issued_by = values(issued_by),
  revoked_time = values(revoked_time),
  revoked_by = values(revoked_by),
  revoke_reason = values(revoke_reason),
  license_payload = values(license_payload),
  signature_text = values(signature_text);

insert into cv_factor_version
(version_code, version_name, publish_status, frozen_flag, published_by, published_time, remark)
values
('EF-OPEN-2025', '企业端开放同步因子库 2025', 'published', 0, 'seed', now(), 'Published factor version for enterprise open synchronization')
on duplicate key update
  version_name = values(version_name),
  publish_status = values(publish_status),
  frozen_flag = values(frozen_flag),
  published_by = values(published_by),
  published_time = coalesce(cv_factor_version.published_time, values(published_time)),
  remark = values(remark);

insert into cv_factor_customer_scope
(version_id, customer_id, package_id, package_name, edition, license_id, scope_status)
select
  version.id,
  customer.id,
  null,
  null,
  'standard',
  null,
  'enabled'
from cv_factor_version version
join cv_customer customer on customer.customer_code = 'ENT-DEMO-001'
where version.version_code = 'EF-OPEN-2025'
on duplicate key update
  scope_status = values(scope_status),
  package_name = values(package_name);

insert into cv_admin_division
(division_code, division_name, parent_code, level_type, sort_order, status, remark)
values
('330000', '浙江省', null, 'province', 1, '0', 'acceptance seed')
on duplicate key update
  division_name = values(division_name),
  level_type = values(level_type),
  sort_order = values(sort_order),
  status = values(status),
  remark = values(remark);

insert into cv_electricity_factor_version
(factor_version, effective_year, sort_order, status, remark)
values
('EF-ELEC-2025', 2025, 1, '0', 'acceptance seed')
on duplicate key update
  sort_order = values(sort_order),
  status = values(status),
  remark = values(remark);

insert into cv_electricity_factor
(factor_version, division_code, division_name, region_name, province_factor, region_factor, national_factor,
 non_fossil_excluded_factor, national_fossil_power_factor, sort_order, status, remark)
values
('EF-ELEC-2025', '330000', '浙江省', '华东区域电网', 0.5366000000, 0.5703000000, 0.5703000000,
 0.5942000000, 0.6101000000, 1, '0', 'acceptance seed')
on duplicate key update
  division_name = values(division_name),
  region_name = values(region_name),
  province_factor = values(province_factor),
  region_factor = values(region_factor),
  national_factor = values(national_factor),
  non_fossil_excluded_factor = values(non_fossil_excluded_factor),
  national_fossil_power_factor = values(national_fossil_power_factor),
  sort_order = values(sort_order),
  status = values(status),
  remark = values(remark);

insert into cv_electricity_factor_scope
(scope_key, scope_name, sort_order, status, remark)
values
('province_factor', '省级电网排放因子', 1, '0', 'acceptance seed')
on duplicate key update
  scope_name = values(scope_name),
  sort_order = values(sort_order),
  status = values(status),
  remark = values(remark);

insert into cv_greenhouse_gas
(gas_code, gas_name, gas_name_en, gwp_value, gwp_version, chemical_formula, sort_order, status, remark)
values
('CO2', '二氧化碳', 'Carbon dioxide', 1.0000000000, 'IPCC AR6', 'CO2', 1, '0', 'acceptance seed')
on duplicate key update
  gas_name = values(gas_name),
  gas_name_en = values(gas_name_en),
  gwp_value = values(gwp_value),
  gwp_version = values(gwp_version),
  chemical_formula = values(chemical_formula),
  sort_order = values(sort_order),
  status = values(status),
  remark = values(remark);
