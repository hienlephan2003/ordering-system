DROP SCHEMA IF EXISTS "ordering" CASCADE;

CREATE SCHEMA "ordering";

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- DROP TYPE IF EXISTS order_status CASCADE;
-- CREATE TYPE order_status AS ENUM ('PENDING', 'PAID', 'APPROVED', 'CANCELLED', 'CANCELLING');
-- CREATE CAST ( varchar AS order_status ) with inout as implicit;
DROP CAST IF EXISTS (varchar AS jsonb);
CREATE CAST (varchar AS jsonb) WITH INOUT AS IMPLICIT;

DROP TABLE IF EXISTS "ordering".orders CASCADE;

CREATE TABLE "ordering".orders
(
    id uuid NOT NULL,
    customer_id uuid NOT NULL,
    restaurant_id uuid NOT NULL,
    tracking_id uuid NOT NULL,
    price numeric(10,2) NOT NULL,
    order_status varchar NOT NULL,
    failure_messages character varying COLLATE pg_catalog."default",
    CONSTRAINT orders_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "ordering".order_items CASCADE;

CREATE TABLE "ordering".order_items
(
    id bigint NOT NULL,
    order_id uuid NOT NULL,
    product_id uuid NOT NULL,
    price numeric(10,2) NOT NULL,
    quantity integer NOT NULL,
    sub_total numeric(10,2) NOT NULL,
    CONSTRAINT order_items_pkey PRIMARY KEY (id, order_id)
);

ALTER TABLE "ordering".order_items
    ADD CONSTRAINT "FK_ORDER_ID" FOREIGN KEY (order_id)
        REFERENCES "ordering".orders (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
    NOT VALID;

DROP TABLE IF EXISTS "ordering".order_address CASCADE;

CREATE TABLE "ordering".order_address
(
    id uuid NOT NULL,
    order_id uuid UNIQUE NOT NULL,
    street character varying COLLATE pg_catalog."default" NOT NULL,
    postal_code character varying COLLATE pg_catalog."default" NOT NULL,
    city character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT order_address_pkey PRIMARY KEY (id, order_id)
);

ALTER TABLE "ordering".order_address
    ADD CONSTRAINT "FK_ORDER_ID" FOREIGN KEY (order_id)
        REFERENCES "ordering".orders (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
    NOT VALID;
-- DROP TYPE IF EXISTS saga_status CASCADE;
-- CREATE TYPE saga_status AS ENUM ('STARTED', 'FAILED', 'SUCCEEDED', 'PROCESSING', 'COMPENSATING', 'COMPENSATED');
-- CREATE CAST ( varchar AS saga_status ) with inout as implicit;

-- DROP TYPE IF EXISTS outbox_status CASCADE;
-- CREATE TYPE outbox_status AS ENUM ('STARTED', 'COMPLETED', 'FAILED');
-- CREATE CAST ( varchar AS outbox_status ) with inout as implicit;
-- CREATE CAST ( char varying AS outbox_status ) with inout as implicit;

DROP TABLE IF EXISTS "ordering".payment_outbox CASCADE;

CREATE TABLE "ordering".payment_outbox
(
    id uuid NOT NULL,
    saga_id uuid NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    type character varying COLLATE pg_catalog."default" NOT NULL,
    payload jsonb NOT NULL,
    outbox_status varchar NOT NULL,
    saga_status varchar NOT NULL,
    order_status varchar NOT NULL,
    version integer NOT NULL,
    CONSTRAINT payment_outbox_pkey PRIMARY KEY (id)
);

CREATE INDEX "payment_outbox_saga_status"
    ON "ordering".payment_outbox
        (type, outbox_status, saga_status);

--CREATE UNIQUE INDEX "payment_outbox_saga_id"
--    ON "ordering".payment_outbox
--    (type, saga_id, saga_status);

DROP TABLE IF EXISTS "ordering".restaurant_approval_outbox CASCADE;

CREATE TABLE "ordering".restaurant_approval_outbox
(
    id uuid NOT NULL,
    saga_id uuid NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    type character varying COLLATE pg_catalog."default" NOT NULL,
    outbox_status varchar NOT NULL,
    saga_status varchar NOT NULL,
    order_status varchar NOT NULL,
    payload jsonb NOT NULL,
    version integer NOT NULL,
    CONSTRAINT restaurant_approval_outbox_pkey PRIMARY KEY (id)
);

CREATE INDEX "restaurant_approval_outbox_saga_status"
    ON "ordering".restaurant_approval_outbox
        (type, outbox_status, saga_status);

--CREATE UNIQUE INDEX "restaurant_approval_outbox_saga_id"
--    ON "ordering".restaurant_approval_outbox
--    (type, saga_id, saga_status);

DROP TABLE IF EXISTS "ordering".customers CASCADE;

CREATE TABLE "ordering".customers
(
    id uuid NOT NULL,
    username character varying COLLATE pg_catalog."default" NOT NULL,
    first_name character varying COLLATE pg_catalog."default" NOT NULL,
    last_name character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT customers_pkey PRIMARY KEY (id)
);
