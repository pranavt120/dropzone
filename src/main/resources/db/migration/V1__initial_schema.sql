-- Create drops table
CREATE TABLE drops (
    id UUID PRIMARY KEY,
    sku VARCHAR(255) NOT NULL,
    total_units INT NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL
);

-- Create stock table (one row per drop)
CREATE TABLE stock (
    id UUID primary key,
    drop_id UUID not null,
    remaining INT NOT NULL,
    as_of TIMESTAMP NOT NULL,
    FOREIGN KEY (drop_id) REFERENCES drops(id)
);

-- Create reservations table
CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    drop_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    state VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (drop_id) REFERENCES drops(id),
    UNIQUE (drop_id, user_id, idempotency_key)
);

-- Create indexes
CREATE INDEX idx_drop_state ON reservations(drop_id, state);
CREATE INDEX idx_created_at ON reservations(created_at);
CREATE INDEX idx_user_drop ON reservations(user_id, drop_id, state);
