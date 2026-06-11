-- Run this with psql after creating the PostgreSQL schema.
-- Example:
--   psql -d motorph -f scripts/import_employee.sql
-- This script only seeds the employee table.

BEGIN;

CREATE TEMP TABLE employee_stage (
    employee_id VARCHAR(10),
    last_name VARCHAR(100),
    first_name VARCHAR(100),
    birthday_text VARCHAR(20),
    address TEXT,
    phone_number VARCHAR(20),
    sss_number VARCHAR(20),
    philhealth_number VARCHAR(20),
    tin_number VARCHAR(20),
    pag_ibig_number VARCHAR(20),
    status VARCHAR(20),
    position VARCHAR(100),
    supervisor_name VARCHAR(100),
    basic_salary NUMERIC(12, 2),
    rice_subsidy NUMERIC(10, 2),
    phone_allowance NUMERIC(10, 2),
    clothing_allowance NUMERIC(10, 2),
    gross_semi_monthly_rate NUMERIC(12, 2),
    hourly_rate NUMERIC(10, 4),
    role VARCHAR(50)
);

\copy employee_stage FROM 'data/MotorPH Employee Record.csv' CSV HEADER

INSERT INTO employee (
    employee_id,
    last_name,
    first_name,
    birthday,
    address,
    phone_number,
    sss_number,
    philhealth_number,
    tin_number,
    pag_ibig_number,
    status,
    position,
    supervisor_id,
    role,
    basic_salary,
    rice_subsidy,
    phone_allowance,
    clothing_allowance,
    gross_semi_monthly_rate,
    hourly_rate
)
SELECT
    employee_id,
    last_name,
    first_name,
    to_date(birthday_text, 'MM/DD/YYYY'),
    address,
    phone_number,
    sss_number,
    philhealth_number,
    tin_number,
    pag_ibig_number,
    status,
    position,
    NULL,
    role,
    basic_salary,
    rice_subsidy,
    phone_allowance,
    clothing_allowance,
    gross_semi_monthly_rate,
    hourly_rate
  FROM employee_stage
  ON CONFLICT (employee_id) DO UPDATE SET
    last_name = EXCLUDED.last_name,
    first_name = EXCLUDED.first_name,
    birthday = EXCLUDED.birthday,
    address = EXCLUDED.address,
    phone_number = EXCLUDED.phone_number,
    sss_number = EXCLUDED.sss_number,
    philhealth_number = EXCLUDED.philhealth_number,
    tin_number = EXCLUDED.tin_number,
    pag_ibig_number = EXCLUDED.pag_ibig_number,
    status = EXCLUDED.status,
    position = EXCLUDED.position,
    supervisor_id = EXCLUDED.supervisor_id,
    role = EXCLUDED.role,
    basic_salary = EXCLUDED.basic_salary,
    rice_subsidy = EXCLUDED.rice_subsidy,
    phone_allowance = EXCLUDED.phone_allowance,
    clothing_allowance = EXCLUDED.clothing_allowance,
    gross_semi_monthly_rate = EXCLUDED.gross_semi_monthly_rate,
    hourly_rate = EXCLUDED.hourly_rate;

UPDATE employee e
SET supervisor_id = sup.employee_id
FROM employee_stage s
JOIN employee sup
  ON regexp_replace(lower(trim(sup.last_name || ' ' || sup.first_name)), '[^a-z0-9]+', ' ', 'g') =
     regexp_replace(lower(trim(COALESCE(NULLIF(s.supervisor_name, 'N/A'), ''))), '[^a-z0-9]+', ' ', 'g')
WHERE e.employee_id = s.employee_id
  AND COALESCE(NULLIF(TRIM(s.supervisor_name), ''), 'N/A') <> 'N/A';

COMMIT;