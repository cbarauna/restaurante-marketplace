CREATE TABLE restaurant (
    id UUID PRIMARY KEY,
    owner_subject VARCHAR(255) NOT NULL,
    trade_name VARCHAR(150) NOT NULL,
    legal_name VARCHAR(200) NOT NULL,
    tax_id VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(254) NOT NULL,
    phone VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    review_note VARCHAR(2000),
    submitted_at TIMESTAMPTZ,
    reviewed_at TIMESTAMPTZ,
    activated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_restaurant_status CHECK (
        status IN (
            'DRAFT',
            'UNDER_REVIEW',
            'CHANGES_REQUESTED',
            'APPROVED',
            'REJECTED',
            'ACTIVE',
            'SUSPENDED'
        )
    )
);

CREATE INDEX idx_restaurant_status ON restaurant (status);
CREATE INDEX idx_restaurant_owner_subject ON restaurant (owner_subject);

CREATE TABLE restaurant_status_history (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurant (id),
    previous_status VARCHAR(32),
    new_status VARCHAR(32) NOT NULL,
    reason VARCHAR(2000),
    changed_by VARCHAR(255) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_restaurant_status_history_restaurant
    ON restaurant_status_history (restaurant_id, changed_at);
