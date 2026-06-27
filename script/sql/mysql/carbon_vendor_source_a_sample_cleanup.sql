-- Remove obsolete vendor demo rows that conflict with Source(A) master data.
-- Boundary: vendor database only.
--
-- Source(A) defines 203 as effective_year -> factor_version, so year 2025
-- maps to factor_version 2023. The old EF-ELEC-2025 demo row is not
-- customer-provided data and blocks the strict effective_year key.

DELETE FROM cv_electricity_factor
WHERE factor_version = 'EF-ELEC-2025';

DELETE FROM cv_electricity_factor_version
WHERE factor_version = 'EF-ELEC-2025'
  AND effective_year = 2025;

SELECT 'cv_electricity_factor_version.effective_year.duplicate' AS check_code,
       COUNT(*) AS duplicate_groups
FROM (
    SELECT effective_year
    FROM cv_electricity_factor_version
    WHERE effective_year IS NOT NULL
    GROUP BY effective_year
    HAVING COUNT(*) > 1
) d;
