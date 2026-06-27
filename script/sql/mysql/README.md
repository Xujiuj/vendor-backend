Vendor backend MySQL development scripts
========================================

Use this directory for vendor-side central operations DDL and seed data.
Vendor tables may cover customers, license issuance, factor versions, report
templates, and renewal records, but must not contain enterprise activity data,
green power certificate details, or intensity denominator facts.

Current status:
- `carbon_vendor_schema_v1.sql` is the first development DDL slice. It covers
  customer accounts, signing-key metadata, manual license issue records, factor
  versions, report template versions, customer scopes, and renewal placeholders.
- The schema intentionally excludes enterprise-local capture data, green-power
  proof details, and intensity denominator facts.
- MySQL is the only maintained vendor-side delivery database target.
