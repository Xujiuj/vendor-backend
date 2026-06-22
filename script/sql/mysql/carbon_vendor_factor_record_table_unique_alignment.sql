-- Vendor factor record unique key alignment.
-- Database boundary: vendor only.

CREATE INDEX idx_cv_factor_record_version ON cv_factor_record (version_id);
ALTER TABLE cv_factor_record DROP INDEX uk_cv_factor_record;
ALTER TABLE cv_factor_record
    ADD CONSTRAINT uk_cv_factor_record UNIQUE (version_id, factor_table_code, factor_code);
