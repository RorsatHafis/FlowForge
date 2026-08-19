CREATE TABLE inventory (
    item_id UUID PRIMARY KEY,
    sku VARCHAR(100) NOT NULL,
    stock INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_inventory_sku UNIQUE (sku),

    -- The actual safety boundary. Even if a future application bug tries to
    -- push stock negative (a logic error in decrementStockIfAvailable's
    -- WHERE clause, a bad manual UPDATE, anything), Postgres refuses the
    -- write outright. The application-level check in the atomic UPDATE is
    -- the primary defense; this constraint is what happens if that defense
    -- is ever wrong.
    CONSTRAINT chk_inventory_stock_non_negative CHECK (stock >= 0)
);