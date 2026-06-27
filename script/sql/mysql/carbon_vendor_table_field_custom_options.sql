DELIMITER //

DROP PROCEDURE IF EXISTS cv_add_table_field_options//
CREATE PROCEDURE cv_add_table_field_options()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'cv_vendor_table_field'
          AND column_name = 'field_options'
    ) THEN
        ALTER TABLE cv_vendor_table_field
            ADD COLUMN field_options TEXT DEFAULT NULL COMMENT '字段类型为选项时的选项 JSON' AFTER field_width;
    END IF;
END//

DELIMITER ;

CALL cv_add_table_field_options();
DROP PROCEDURE IF EXISTS cv_add_table_field_options;
