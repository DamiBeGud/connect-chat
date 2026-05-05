ALTER TABLE users
    DROP CONSTRAINT users_verification_code_six_digits;

ALTER TABLE users
    ALTER COLUMN verification_code TYPE VARCHAR(6)
        USING verification_code::TEXT,
    ADD COLUMN verification_code_expires_at TIMESTAMPTZ;

ALTER TABLE users
    ADD CONSTRAINT users_verification_code_six_chars
        CHECK (
            verification_code IS NULL
            OR CHAR_LENGTH(verification_code) = 6
        );
