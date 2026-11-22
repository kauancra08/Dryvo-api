ALTER TABLE quotes DROP CONSTRAINT ck_quotes_status;

ALTER TABLE quotes
    ADD CONSTRAINT ck_quotes_status
    CHECK (status IN ('DRAFT', 'SENT', 'APPROVED', 'REJECTED', 'CANCELLED', 'COMPLETED'));
