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
"REQUEST_ID" INT  GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
"MERCHANT_ID" VARCHAR(50),
"OLD_RISK" VARCHAR(50),
"UPDATED_RISK" VARCHAR(50),
"APPROVAL_FLAG"  VARCHAR(50),
"UPDATED_TIMESTAMP" TIMESTAMP
);

insert into  "MERCHANT_RISK_SETTING"("MERCHANT_ID", "OLD_RISK","UPDATED_RISK", "APPROVAL_FLAG", "UPDATED_TIMESTAMP") values('merchant00101', 'Low', 'Medium', 'Approve', '')