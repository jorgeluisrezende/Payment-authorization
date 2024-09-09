CREATE TABLE IF NOT EXISTS transaction (
  id          UUID    PRIMARY KEY,
  account     VARCHAR          NOT NULL,
  total_amount DOUBLE PRECISION NOT NULL,
  mcc         VARCHAR          NOT NULL,
  merchant    VARCHAR          NOT NULL
);

CREATE TABLE IF NOT EXISTS account (
  id            UUID    PRIMARY KEY,
  account       VARCHAR            NOT NULL,
  total_balance DOUBLE PRECISION   NOT NULL
);

CREATE TABLE IF NOT EXISTS account_balance (
  id             UUID    PRIMARY KEY,
  account_id     UUID    REFERENCES account (id),
  category       VARCHAR NOT NULL,
  balance        DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS account_transaction (
  id             UUID    PRIMARY KEY,
  account_id     UUID    REFERENCES account (id),
  transaction_id UUID    REFERENCES transaction (id),
  amount        DOUBLE PRECISION NOT NULL
);

INSERT INTO account (
  id,
  account,
  total_balance
) VALUES (
  'db5438df-a1ef-47fc-b22e-2f0a9c25259b',
  '123',
  300
);

INSERT INTO account_balance (
  id,
  account_id,
  category,
  balance
) VALUES (
  gen_random_uuid (),
  'db5438df-a1ef-47fc-b22e-2f0a9c25259b',
  'FOOD',
  200
);

INSERT INTO account_balance (
  id,
  account_id,
  category,
  balance
) VALUES (
  gen_random_uuid (),
  'db5438df-a1ef-47fc-b22e-2f0a9c25259b',
  'MEAL',
  50
);

INSERT INTO account_balance (
  id,
  account_id,
  category,
  balance
) VALUES (
  gen_random_uuid (),
  'db5438df-a1ef-47fc-b22e-2f0a9c25259b',
  'CASH',
  50
);