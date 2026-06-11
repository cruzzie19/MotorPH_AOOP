CREATE TABLE employee (
    employee_id VARCHAR(10) PRIMARY KEY,
    last_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    birthday DATE NOT NULL,
    address TEXT,
    phone_number VARCHAR(20),
    sss_number VARCHAR(20),
    philhealth_number VARCHAR(20),
    tin_number VARCHAR(20),
    pag_ibig_number VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    position VARCHAR(100) NOT NULL,
    supervisor_id VARCHAR(10),
    role VARCHAR(50) NOT NULL,
    basic_salary NUMERIC(12, 2) NOT NULL DEFAULT 0,
    rice_subsidy NUMERIC(10, 2) NOT NULL DEFAULT 0,
    phone_allowance NUMERIC(10, 2) NOT NULL DEFAULT 0,
    clothing_allowance NUMERIC(10, 2) NOT NULL DEFAULT 0,
    gross_semi_monthly_rate NUMERIC(12, 2),
    hourly_rate NUMERIC(10, 4) NOT NULL DEFAULT 0,
    CONSTRAINT ck_employee_status CHECK (status IN ('Regular', 'Probationary')),
    CONSTRAINT ck_employee_role CHECK (role IN ('EXECUTIVE', 'HR', 'PAYROLL', 'ACCOUNTING', 'IT', 'SALES')),
    CONSTRAINT ck_employee_compensation CHECK (
        basic_salary >= 0
        AND rice_subsidy >= 0
        AND phone_allowance >= 0
        AND clothing_allowance >= 0
        AND hourly_rate >= 0
        AND (gross_semi_monthly_rate IS NULL OR gross_semi_monthly_rate >= 0)
    ),
    CONSTRAINT fk_employee_supervisor
        FOREIGN KEY (supervisor_id)
        REFERENCES employee (employee_id)
        ON DELETE SET NULL
);

CREATE TABLE attendance (
    attendance_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    employee_id VARCHAR(10) NOT NULL,
    work_date DATE NOT NULL,
    log_in TIME,
    log_out TIME,
    CONSTRAINT fk_attendance_employee
        FOREIGN KEY (employee_id)
        REFERENCES employee (employee_id)
        ON DELETE CASCADE,
    CONSTRAINT uq_attendance_employee_date UNIQUE (employee_id, work_date)
);

CREATE TABLE leave_request (
    leave_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    employee_id VARCHAR(10) NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    reviewed_by VARCHAR(10),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_request_employee
        FOREIGN KEY (employee_id)
        REFERENCES employee (employee_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_leave_request_reviewed_by
        FOREIGN KEY (reviewed_by)
        REFERENCES employee (employee_id)
        ON DELETE SET NULL,
    CONSTRAINT ck_leave_request_date_range CHECK (end_date >= start_date)
);

CREATE TABLE payroll (
    payroll_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    employee_id VARCHAR(10) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    gross_pay NUMERIC(12, 2) NOT NULL DEFAULT 0,
    sss_deduction NUMERIC(10, 2) NOT NULL DEFAULT 0,
    philhealth_deduction NUMERIC(10, 2) NOT NULL DEFAULT 0,
    pag_ibig_deduction NUMERIC(10, 2) NOT NULL DEFAULT 0,
    tax_withheld NUMERIC(10, 2) NOT NULL DEFAULT 0,
    net_pay NUMERIC(12, 2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_payroll_employee
        FOREIGN KEY (employee_id)
        REFERENCES employee (employee_id)
        ON DELETE CASCADE,
    CONSTRAINT uq_payroll_employee_period UNIQUE (employee_id, period_start, period_end),
    CONSTRAINT ck_payroll_date_range CHECK (period_end >= period_start)
);

CREATE TABLE credential (
    employee_id VARCHAR(10) PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    last_password_change TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_credential_employee
        FOREIGN KEY (employee_id)
        REFERENCES employee (employee_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_attendance_employee_date ON attendance (employee_id, work_date);
CREATE INDEX idx_leave_request_employee_status ON leave_request (employee_id, status);
CREATE INDEX idx_payroll_employee_period ON payroll (employee_id, period_start, period_end);
CREATE INDEX idx_employee_supervisor_id ON employee (supervisor_id);
CREATE INDEX idx_employee_role ON employee (role);
