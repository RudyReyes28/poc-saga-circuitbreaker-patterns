CREATE TABLE orders (
                        id              UUID            PRIMARY KEY,
                        customer_id     VARCHAR(100)    NOT NULL,
                        product_id      VARCHAR(100)    NOT NULL,
                        quantity        INTEGER         NOT NULL CHECK (quantity > 0),
                        total_amount    NUMERIC(12, 2)  NOT NULL CHECK (total_amount >= 0),
                        status          VARCHAR(30)     NOT NULL,
                        failure_reason  VARCHAR(500),
                        created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_customer ON orders (customer_id);