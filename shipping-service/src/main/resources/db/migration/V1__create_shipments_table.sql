CREATE TABLE shipments (
                           id             UUID          PRIMARY KEY,
                           order_id       UUID          NOT NULL UNIQUE,
                           customer_id    VARCHAR(100)  NOT NULL,
                           product_id     VARCHAR(100)  NOT NULL,
                           quantity       INTEGER       NOT NULL CHECK (quantity > 0),
                           tracking_code  VARCHAR(50)   NOT NULL,
                           status         VARCHAR(20)   NOT NULL,
                           created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);