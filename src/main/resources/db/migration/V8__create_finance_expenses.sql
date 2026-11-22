CREATE TABLE finance_expenses (
    id BIGSERIAL PRIMARY KEY,

    owner_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    amount NUMERIC(15, 2) NOT NULL,
    expense_date DATE NOT NULL,
    notes VARCHAR(2000),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_finance_expenses_owner
        FOREIGN KEY (owner_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT ck_finance_expenses_amount
        CHECK (amount >= 0)
);

CREATE INDEX idx_finance_expenses_owner_date
    ON finance_expenses (owner_id, expense_date);

CREATE INDEX idx_finance_expenses_owner_category
    ON finance_expenses (owner_id, category);