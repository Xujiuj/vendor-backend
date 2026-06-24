ALTER TABLE cv_factor_record
    ADD COLUMN custom_fields TEXT DEFAULT NULL AFTER source_ref;
