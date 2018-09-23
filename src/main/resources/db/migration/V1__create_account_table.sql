create table ACCOUNT
(
  ID INTEGER not null,
  LOCKED BOOLEAN,
  MONEY_VALUE DECIMAL(19,4),
  MONEY_CURRENCY VARCHAR(10),
  constraint ACCOUNT_PK
  primary key (ID)
)
;

