CREATE TABLE clients (
    id         BIGSERIAL PRIMARY KEY,
    owner_id   BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name       VARCHAR(150) NOT NULL,
    phone      VARCHAR(30),
    email      VARCHAR(255),
    document   VARCHAR(18),
    address    VARCHAR(255),
    city       VARCHAR(100),
    state      VARCHAR(2),
    zip_code   VARCHAR(9),
    notes      VARCHAR(2000),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_clients_owner ON clients (owner_id);
CREATE INDEX idx_clients_owner_name ON clients (owner_id, lower(name));
