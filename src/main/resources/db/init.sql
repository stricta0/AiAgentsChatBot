CREATE TABLE IF NOT EXISTS customers (
                                         id SERIAL PRIMARY KEY,
                                         email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS subscriptions (
                                             id SERIAL PRIMARY KEY,
                                             customer_id INT NOT NULL REFERENCES customers(id),
    plan_name VARCHAR(100) NOT NULL,
    monthly_price DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (customer_id, plan_name)
    );

CREATE TABLE IF NOT EXISTS support_cases (
                                             id SERIAL PRIMARY KEY,
                                             case_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id INT NOT NULL REFERENCES customers(id),
    case_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS refund_policies (
                                               id SERIAL PRIMARY KEY,
                                               plan_name VARCHAR(100) NOT NULL UNIQUE,
    refund_available BOOLEAN NOT NULL,
    refund_window_days INT NOT NULL,
    processing_time_days INT NOT NULL,
    policy_description TEXT NOT NULL
    );

INSERT INTO customers (email, full_name)
VALUES
    ('john.doe@example.com', 'John Doe'),
    ('alice.smith@example.com', 'Alice Smith')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO subscriptions (customer_id, plan_name, monthly_price, status)
SELECT c.id, 'PRO', 29.99, 'ACTIVE'
FROM customers c
WHERE c.email = 'john.doe@example.com'
    ON CONFLICT (customer_id, plan_name) DO NOTHING;

INSERT INTO subscriptions (customer_id, plan_name, monthly_price, status)
SELECT c.id, 'BASIC', 9.99, 'ACTIVE'
FROM customers c
WHERE c.email = 'alice.smith@example.com'
    ON CONFLICT (customer_id, plan_name) DO NOTHING;

INSERT INTO refund_policies (
    plan_name,
    refund_available,
    refund_window_days,
    processing_time_days,
    policy_description
)
VALUES
    ('BASIC', true, 7, 5, 'Basic plan can be refunded within 7 days of purchase.'),
    ('PRO', true, 14, 3, 'Pro plan can be refunded within 14 days of purchase.'),
    ('ENTERPRISE', false, 0, 0, 'Enterprise plan is not eligible for standard refunds.')
    ON CONFLICT (plan_name) DO NOTHING;