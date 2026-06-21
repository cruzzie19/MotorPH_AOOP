-- run in terminal after motorph_schema and import_employee:
-- & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -d motorph -f scripts/import_attendance.sql

BEGIN;

CREATE TEMP TABLE attendance_stage (
    employee_id VARCHAR(10),
    work_date_text VARCHAR(20),
    log_in_text VARCHAR(20),
    log_out_text VARCHAR(20)
);

\copy attendance_stage FROM 'data/MotorPH Attendance Record.csv' CSV HEADER

INSERT INTO attendance (
    employee_id,
    work_date,
    log_in,
    log_out
)
SELECT
    TRIM(employee_id),
    to_date(TRIM(work_date_text), 'MM/DD/YYYY'), 
    NULLIF(NULLIF(TRIM(log_in_text), ''), 'N/A')::TIME,
    NULLIF(NULLIF(TRIM(log_out_text), ''), 'N/A')::TIME
FROM attendance_stage

ON CONFLICT (employee_id, work_date) DO UPDATE SET
    log_in = EXCLUDED.log_in,
    log_out = EXCLUDED.log_out;

COMMIT;