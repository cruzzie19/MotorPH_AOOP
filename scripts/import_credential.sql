-- run in terminal after motorph_schema and import_employee:
-- & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -d motorph -f scripts/import_credential.sql

BEGIN;

CREATE TEMP TABLE credential_stage (
    employee_id VARCHAR(10),
    password_hash_hex TEXT,
    password_salt_hex TEXT,
    last_password_change_text TEXT
);

\copy credential_stage FROM 'data/MotorPH Credential Record.csv' CSV HEADER

INSERT INTO credential (
    employee_id,
    password_hash,
    password_salt,
    last_password_change
)
SELECT
    TRIM(employee_id),
    decode(TRIM(password_hash_hex), 'hex'),
    decode(TRIM(password_salt_hex), 'hex'),
    to_timestamp(TRIM(last_password_change_text), 'YYYY-MM-DD HH24:MI:SS"++"TZ') 
FROM credential_stage
ON CONFLICT (employee_id) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    password_salt = EXCLUDED.password_salt,
    last_password_change = EXCLUDED.last_password_change;


COMMIT;