CREATE TABLE IF NOT EXISTS medicines (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    price DECIMAL(10,2),
    is_discontinued BOOLEAN DEFAULT FALSE,
    manufacturer_name VARCHAR(500),
    type VARCHAR(100),
    pack_size_label VARCHAR(200),
    short_composition1 VARCHAR(500),
    short_composition2 VARCHAR(500)
);

CREATE INDEX idx_composition1 ON medicines(short_composition1);
CREATE INDEX idx_composition2 ON medicines(short_composition2);
CREATE INDEX idx_name ON medicines(name);