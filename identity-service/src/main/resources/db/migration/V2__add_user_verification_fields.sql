ALTER TABLE users
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN verified_at TIMESTAMPTZ,
    ADD COLUMN is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN verification_code INTEGER;

ALTER TABLE users
    ADD CONSTRAINT users_verification_code_six_digits
        CHECK (
            verification_code IS NULL
            OR verification_code BETWEEN 100000 AND 999999
        );
