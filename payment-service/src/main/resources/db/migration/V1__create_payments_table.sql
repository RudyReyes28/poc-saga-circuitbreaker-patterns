CREATE TABLE payments (
                          id           UUID            PRIMARY KEY,
                          order_id     UUID            NOT NULL UNIQUE,
                          customer_id  VARCHAR(100)    NOT NULL,
                          amount       NUMERIC(12, 2)  NOT NULL CHECK (amount > 0),
                          status       VARCHAR(20)     NOT NULL,
                          created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);