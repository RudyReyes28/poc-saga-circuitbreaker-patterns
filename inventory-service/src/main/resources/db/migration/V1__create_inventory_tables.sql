CREATE TABLE inventory_items (
                                 product_id          VARCHAR(100) PRIMARY KEY,
                                 available_quantity  INTEGER      NOT NULL CHECK (available_quantity >= 0)
);

CREATE TABLE reservations (
                              id          UUID          PRIMARY KEY,
                              order_id    UUID          NOT NULL UNIQUE,
                              product_id  VARCHAR(100)  NOT NULL REFERENCES inventory_items (product_id),
                              quantity    INTEGER       NOT NULL CHECK (quantity > 0),
                              status      VARCHAR(20)   NOT NULL,
                              created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);