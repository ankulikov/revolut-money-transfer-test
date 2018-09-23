create table ACCOUNT
(
	ID BIGINT auto_increment primary key,
	LOCKED BOOLEAN default false,
	MONEY_VALUE DECIMAL(19,4) default 0,
	MONEY_CURRENCY VARCHAR(10) default 'USD'
)
;

