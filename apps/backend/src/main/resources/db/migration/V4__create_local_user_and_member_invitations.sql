CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    oidc_subject VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(254),
    display_name VARCHAR(150) NOT NULL,
    status VARCHAR(32) NOT NULL,
    last_authenticated_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_app_user_status CHECK (status IN ('ACTIVE', 'BLOCKED'))
);

INSERT INTO app_user (
    id,
    oidc_subject,
    email,
    display_name,
    status,
    last_authenticated_at,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    subject,
    CASE WHEN position('@' IN subject) > 1 THEN lower(subject) ELSE NULL END,
    subject,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM (
    SELECT owner_subject AS subject FROM restaurant
    UNION
    SELECT user_subject AS subject FROM restaurant_member
) existing_identity;

CREATE UNIQUE INDEX uk_app_user_email
    ON app_user (lower(email))
    WHERE email IS NOT NULL;

ALTER TABLE restaurant ADD COLUMN owner_user_id UUID;

UPDATE restaurant restaurant_row
SET owner_user_id = app_user.id
FROM app_user
WHERE app_user.oidc_subject = restaurant_row.owner_subject;

ALTER TABLE restaurant ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE restaurant
    ADD CONSTRAINT fk_restaurant_owner_user
    FOREIGN KEY (owner_user_id) REFERENCES app_user (id);
CREATE INDEX idx_restaurant_owner_user ON restaurant (owner_user_id);
DROP INDEX idx_restaurant_owner_subject;
ALTER TABLE restaurant DROP COLUMN owner_subject;

ALTER TABLE restaurant_member ADD COLUMN user_id UUID;

UPDATE restaurant_member member_row
SET user_id = app_user.id
FROM app_user
WHERE app_user.oidc_subject = member_row.user_subject;

ALTER TABLE restaurant_member ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE restaurant_member
    ADD CONSTRAINT fk_restaurant_member_user
    FOREIGN KEY (user_id) REFERENCES app_user (id);
ALTER TABLE restaurant_member DROP CONSTRAINT uk_restaurant_member;
ALTER TABLE restaurant_member
    ADD CONSTRAINT uk_restaurant_member UNIQUE (restaurant_id, user_id);
DROP INDEX idx_restaurant_member_subject;
CREATE INDEX idx_restaurant_member_user ON restaurant_member (user_id);
ALTER TABLE restaurant_member DROP COLUMN user_subject;

CREATE TABLE restaurant_member_invitation (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurant (id),
    email VARCHAR(254) NOT NULL,
    role VARCHAR(32) NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    invited_by_user_id UUID NOT NULL REFERENCES app_user (id),
    accepted_by_user_id UUID REFERENCES app_user (id),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    responded_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_restaurant_invitation_role CHECK (role IN ('MANAGER', 'OPERATOR')),
    CONSTRAINT ck_restaurant_invitation_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REVOKED', 'EXPIRED'))
);

CREATE UNIQUE INDEX uk_pending_restaurant_invitation
    ON restaurant_member_invitation (restaurant_id, lower(email))
    WHERE status = 'PENDING';

CREATE INDEX idx_restaurant_invitation_restaurant
    ON restaurant_member_invitation (restaurant_id, created_at);
