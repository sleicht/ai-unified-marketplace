CREATE TABLE record (
    id BIGSERIAL PRIMARY KEY,
    external_reference VARCHAR(50) NOT NULL UNIQUE CHECK (external_reference ~ '[^[:space:]]'),
    category VARCHAR(50) NOT NULL CHECK (category ~ '[^[:space:]]'),
    display_name VARCHAR(100) NOT NULL CHECK (display_name ~ '[^[:space:]]'),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

