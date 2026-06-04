Vendor SQL Server migration scripts
===================================

Use this directory for vendor-side SQL Server migration and acceptance scripts.

Current status:
- `carbon_vendor_schema_v1.sql` mirrors the MySQL development foundation for
  customers, license issuance, factor versions, report templates, scopes, and
  renewal placeholders.
- Vendor SQL Server scripts must not add enterprise-local data capture,
  green-power proof detail, or intensity denominator fact tables.
- Keep private signing-key material outside source control. The schema stores
  only `private_key_ref` metadata.
