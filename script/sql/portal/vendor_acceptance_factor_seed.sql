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
