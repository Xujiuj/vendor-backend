-- Acceptance-only vendor factor seed.
-- Keep this script idempotent. It provides a mutable draft factor version for
-- online CRUD verification without weakening frozen-version protections.

insert into cv_factor_version
(version_code, version_name, publish_status, frozen_flag, published_by, published_time, remark)
select
  'EF-DRAFT-ACCEPTANCE-2026',
  'Acceptance draft emission factor version',
  'draft',
  0,
  null,
  null,
  'Online acceptance draft version for factor record CRUD'
where not exists (
  select 1
  from cv_factor_version
  where version_code = 'EF-DRAFT-ACCEPTANCE-2026'
);

update cv_factor_version
set publish_status = 'draft',
    frozen_flag = 0,
    published_by = null,
    published_time = null,
    remark = 'Online acceptance draft version for factor record CRUD'
where version_code = 'EF-DRAFT-ACCEPTANCE-2026'
  and (publish_status <> 'draft' or frozen_flag <> 0);

-- Enterprise-open factor seed.
-- This is the data chain consumed by /open/factors:
-- active license -> factor-sync feature -> published factor version -> enabled scope -> enabled factor records.
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

insert into cv_factor_record
(version_id, factor_table_code, factor_code, factor_name, factor_category, factor_value, factor_unit, factor_key,
 emission_source_name, emission_source_name_en, fuel_material_category, source_unit, co2, ch4, n2o,
 applicable_scope, factor_source, gwp_ch4, gwp_n2o, factor_gwp, version_province_code, factor_version,
 division_code, division_name, region_name, province_factor, region_factor, national_factor,
 non_fossil_excluded_factor, national_fossil_power_factor, row_no, fuel_level1, fuel_level2, lower_heat_value,
 co2_factor, gwp_value, converted_factor, source_ref, enabled_flag, remark)
select
  version.id,
  seed.factor_table_code,
  seed.factor_code,
  seed.factor_name,
  seed.factor_category,
  seed.factor_value,
  seed.factor_unit,
  seed.factor_key,
  seed.emission_source_name,
  seed.emission_source_name_en,
  seed.fuel_material_category,
  seed.source_unit,
  seed.co2,
  seed.ch4,
  seed.n2o,
  seed.applicable_scope,
  seed.factor_source,
  seed.gwp_ch4,
  seed.gwp_n2o,
  seed.factor_gwp,
  seed.version_province_code,
  seed.factor_version,
  seed.division_code,
  seed.division_name,
  seed.region_name,
  seed.province_factor,
  seed.region_factor,
  seed.national_factor,
  seed.non_fossil_excluded_factor,
  seed.national_fossil_power_factor,
  seed.row_no,
  seed.fuel_level1,
  seed.fuel_level2,
  seed.lower_heat_value,
  seed.co2_factor,
  seed.gwp_value,
  seed.converted_factor,
  seed.source_ref,
  1,
  seed.remark
from cv_factor_version version
join (
  select '202ef' factor_table_code, 'ELEC-ZJ-2025' factor_code, '浙江省外购电力排放因子' factor_name,
         '外购电力' factor_category, 0.5366000000 factor_value, 'tCO2/MWh' factor_unit,
         'EF-ELEC-2025:330000:province_factor' factor_key, '外购电力' emission_source_name,
         'Purchased electricity' emission_source_name_en, null fuel_material_category, 'MWh' source_unit,
         0.5366000000 co2, 0.0000000000 ch4, 0.0000000000 n2o, 'Scope 2' applicable_scope,
         '厂商端开放同步样例' factor_source, null gwp_ch4, null gwp_n2o, 0.5366000000 factor_gwp,
         '330000' version_province_code, 'EF-ELEC-2025' factor_version, '330000' division_code,
         '浙江省' division_name, '华东区域电网' region_name, 0.5366000000 province_factor,
         0.5703000000 region_factor, 0.5703000000 national_factor, 0.5942000000 non_fossil_excluded_factor,
         0.6101000000 national_fossil_power_factor, 1 row_no, null fuel_level1, null fuel_level2,
         null lower_heat_value, null co2_factor, null gwp_value, null converted_factor,
         'EF-ELEC-2025' source_ref, '企业端电力活动数据可同步因子' remark
  union all
  select '203ef', 'ELEC-VERSION-2025', '电力因子版本 2025', '电力因子版本', 2025.0000000000, 'year',
         'EF-ELEC-2025', null, null, null, null, null, null, null, 'Scope 2', '厂商端开放同步样例',
         null, null, null, null, 'EF-ELEC-2025', null, null, null, null, null, null, null, null,
         1, null, null, null, null, null, null, 'EF-ELEC-2025', '企业端电力因子版本可同步数据'
  union all
  select '205ef', 'province_factor', '省级电网排放因子', '电力因子口径', 0.5366000000, 'tCO2/MWh',
         'province_factor', null, null, null, null, null, null, null, 'Scope 2', '厂商端开放同步样例',
         null, null, null, null, 'EF-ELEC-2025', null, null, null, 0.5366000000, null, null, null, null,
         1, null, null, null, null, null, null, 'province_factor', '企业端电力因子口径可同步数据'
  union all
  select '206', 'CO2', '二氧化碳', '温室气体', 1.0000000000, 'GWP', 'CO2',
         null, 'Carbon dioxide', null, null, 1.0000000000, 0.0000000000, 0.0000000000,
         'Scope 1/2/3', 'IPCC AR6', null, null, 1.0000000000, null, null, null, null, null,
         null, null, null, null, null, 1, null, null, null, null, 1.0000000000, 1.0000000000,
         'IPCC AR6', '企业端温室气体可同步数据'
) seed
where version.version_code = 'EF-OPEN-2025'
on duplicate key update
  factor_name = values(factor_name),
  factor_category = values(factor_category),
  factor_value = values(factor_value),
  factor_unit = values(factor_unit),
  factor_key = values(factor_key),
  emission_source_name = values(emission_source_name),
  emission_source_name_en = values(emission_source_name_en),
  fuel_material_category = values(fuel_material_category),
  source_unit = values(source_unit),
  co2 = values(co2),
  ch4 = values(ch4),
  n2o = values(n2o),
  applicable_scope = values(applicable_scope),
  factor_source = values(factor_source),
  gwp_ch4 = values(gwp_ch4),
  gwp_n2o = values(gwp_n2o),
  factor_gwp = values(factor_gwp),
  version_province_code = values(version_province_code),
  factor_version = values(factor_version),
  division_code = values(division_code),
  division_name = values(division_name),
  region_name = values(region_name),
  province_factor = values(province_factor),
  region_factor = values(region_factor),
  national_factor = values(national_factor),
  non_fossil_excluded_factor = values(non_fossil_excluded_factor),
  national_fossil_power_factor = values(national_fossil_power_factor),
  row_no = values(row_no),
  fuel_level1 = values(fuel_level1),
  fuel_level2 = values(fuel_level2),
  lower_heat_value = values(lower_heat_value),
  co2_factor = values(co2_factor),
  gwp_value = values(gwp_value),
  converted_factor = values(converted_factor),
  source_ref = values(source_ref),
  enabled_flag = values(enabled_flag),
  remark = values(remark);
