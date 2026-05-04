CREATE TABLE users (
    id UUID PRIMARY KEY,
    phone_number VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    nickname VARCHAR(100),
    date_of_birth DATE NOT NULL,
    country VARCHAR(100) NOT NULL
);
