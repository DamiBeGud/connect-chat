INSERT INTO users (
    id,
    phone_number,
    first_name,
    last_name,
    nickname,
    date_of_birth,
    country,
    created_at,
    updated_at,
    verified_at,
    is_verified,
    verification_code,
    verification_code_expires_at,
    is_validation_code_sent
)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    '+10000000000',
    'Connect',
    'AI',
    'AI Bot',
    DATE '2000-01-01',
    'US',
    NOW(),
    NOW(),
    NOW(),
    TRUE,
    NULL,
    NULL,
    TRUE
)
ON CONFLICT (id) DO UPDATE
SET
    phone_number = EXCLUDED.phone_number,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    nickname = EXCLUDED.nickname,
    date_of_birth = EXCLUDED.date_of_birth,
    country = EXCLUDED.country,
    updated_at = NOW(),
    verified_at = COALESCE(users.verified_at, EXCLUDED.verified_at),
    is_verified = TRUE,
    verification_code = NULL,
    verification_code_expires_at = NULL,
    is_validation_code_sent = TRUE;
