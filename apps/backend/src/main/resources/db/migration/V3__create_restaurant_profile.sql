CREATE TABLE establishment_type (
    code VARCHAR(50) PRIMARY KEY,
    display_name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO establishment_type (code, display_name) VALUES
    ('RESTAURANT', 'Restaurante'),
    ('BAKERY', 'Padaria'),
    ('PIZZERIA', 'Pizzaria'),
    ('SNACK_BAR', 'Lanchonete');

CREATE TABLE restaurant_address (
    restaurant_id UUID PRIMARY KEY REFERENCES restaurant (id),
    postal_code VARCHAR(9) NOT NULL,
    street VARCHAR(200) NOT NULL,
    number VARCHAR(30) NOT NULL,
    complement VARCHAR(120),
    neighborhood VARCHAR(120) NOT NULL,
    city VARCHAR(120) NOT NULL,
    state VARCHAR(2) NOT NULL,
    latitude NUMERIC(9, 6),
    longitude NUMERIC(9, 6),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE restaurant_establishment_type (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurant (id),
    establishment_type_code VARCHAR(50) NOT NULL REFERENCES establishment_type (code),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_restaurant_establishment_type
        UNIQUE (restaurant_id, establishment_type_code)
);

CREATE INDEX idx_restaurant_establishment_type_restaurant
    ON restaurant_establishment_type (restaurant_id);

CREATE TABLE restaurant_member (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurant (id),
    user_subject VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_restaurant_member UNIQUE (restaurant_id, user_subject),
    CONSTRAINT ck_restaurant_member_role CHECK (role IN ('OWNER', 'MANAGER', 'OPERATOR'))
);

CREATE INDEX idx_restaurant_member_restaurant ON restaurant_member (restaurant_id);
CREATE INDEX idx_restaurant_member_subject ON restaurant_member (user_subject);

INSERT INTO restaurant_member (id, restaurant_id, user_subject, role, created_at)
SELECT gen_random_uuid(), id, owner_subject, 'OWNER', created_at
FROM restaurant;
