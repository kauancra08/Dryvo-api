CREATE TABLE company_profiles (
    id            BIGSERIAL PRIMARY KEY,
    owner_id      BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    company_name  VARCHAR(120),
    phone         VARCHAR(30),
    email         VARCHAR(255),
    address       VARCHAR(255),
    city          VARCHAR(100),
    state         VARCHAR(2),
    zip_code      VARCHAR(9),
    logo_url      VARCHAR(500),
    payment_terms VARCHAR(2000),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_company_profiles_owner UNIQUE (owner_id)
);
