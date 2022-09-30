create table "MERCHANT_RISK_SCORE"(
"PARTNER_ID" NUMERIC(4,0),
"MERCHANT_ID" VARCHAR(50),
"LOW_PAYMENTS_ALLOWED" numeric (5,4),
"LOW_PAYMENTS_BLOCKED" NUMERIC(5,4),
"LOW_PAYMENTS_REVIEW" NUMERIC(5,4),
"MED_PAYMENTS_ALLOWED" numeric (5,4),
"MED_PAYMENTS_BLOCKED" NUMERIC(5,4),
"MED_PAYMENTS_REVIEW" NUMERIC(5,4),
"HIGH_PAYMENTS_ALLOWED" numeric (5,4),
"HIGH_PAYMENTS_BLOCKED" NUMERIC(5,4),
"HIGH_PAYMENTS_REVIEW" NUMERIC(5,4),
"UPDATED_AT" TIMESTAMP,
primary key ("PARTNER_ID","MERCHANT_ID")
);

ALTER TABLE "IFRM_UDS"."MERCHANT_RISK_SCORE_DATA" ADD CONSTRAINT "MERCHANT_RISK_SCORE_DATA_PARTNER_ID_fkey" FOREIGN KEY ("PARTNER_ID")
REFERENCES "IFRM_UAM"."PARTNER_MASTER"("ID");
ALTER TABLE "IFRM_UDS"."MERCHANT_RISK_SCORE_DATA" ADD CONSTRAINT "MERCHANT_RISK_SCORE_DATA_MERCHANT_ID_fkey" FOREIGN KEY ("MERCHANT_ID")
REFERENCES "IFRM_UDS"."MERCHANT"("ID");


create table "MERCHANT_RISK_SETTING"(
"REQUEST_ID" NUMBER  GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
"MERCHANT_ID" VARCHAR(50),
"OLD_RISK" NUMERIC (5,4),
"UPDATED_RISK" NUMERIC(5,4),
"APPROVAL_FLAG"  VARCHAR(50),
"UPDATED_TIMESTAMP" TIMESTAMP
);

CREATE TABLE IF NOT EXISTS MERCHANT_LOGIN (
ID SERIAL PRIMARY KEY,
MERCHANT_ID VARCHAR(20) NOT NULL UNIQUE,
MERCHANT_NAME VARCHAR(20),
MERCHANT_CONTACT VARCHAR(20),
MERCHANT_EMAIL VARCHAR(100),
IS_MERCHANT_ACTIVE BOOLEAN DEFAULT FALSE,
PASSWORD VARCHAR(256),
SALT VARCHAR(256),
PROVISIONAL_1 VARCHAR(256),
PROVISIONAL_2 VARCHAR(256)
)

CREATE TABLE IF NOT EXISTS MERCHANT_LOGIN_ACTIVITY (
ACTIVITY_ID SERIAL PRIMARY KEY,
MERCHANT_ID VARCHAR(20) NOT NULL UNIQUE,
LOGIN_TIME TIMESTAMP,
LOGOUT_TIME TIMESTAMP
)

