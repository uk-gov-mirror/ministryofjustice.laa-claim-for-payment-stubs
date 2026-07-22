ALTER TABLE line_items
    ADD COLUMN draft_claim_id UUID;

ALTER TABLE line_items
    ALTER COLUMN claim_id DROP NOT NULL;

ALTER TABLE line_items
    ADD CONSTRAINT fk_line_items_draft_claim
        FOREIGN KEY (draft_claim_id)
            REFERENCES draft_claims (id)
            ON DELETE CASCADE;

ALTER TABLE claim_evidence
    ADD COLUMN draft_claim_id UUID;

ALTER TABLE claim_evidence
    ALTER COLUMN claim_id DROP NOT NULL;

ALTER TABLE claim_evidence
    ADD CONSTRAINT fk_claim_evidence_draft_claim
        FOREIGN KEY (draft_claim_id)
            REFERENCES draft_claims (id)
            ON DELETE CASCADE;
