-- Enable UUID generation in Postgres (ignored by H2)
DROP TABLE IF EXISTS line_item_claim_evidence;
DROP TABLE IF EXISTS claim_evidence;
DROP TABLE IF EXISTS line_items;
DROP TABLE IF EXISTS claims;


CREATE TABLE IF NOT EXISTS claims (
    id                  UUID PRIMARY KEY,
    ufn                 VARCHAR(20)    NOT NULL,
    client              VARCHAR(50)    NOT NULL,
    category            VARCHAR(50)    NOT NULL,
    concluded           DATE           NOT NULL,
    fee_type            VARCHAR(50)    NOT NULL,
    escaped             BOOLEAN        NOT NULL      DEFAULT FALSE, 
    counsel_payment     VARCHAR(50),
    claimed             DECIMAL(10, 2) NOT NULL,
    provider_user_id    UUID
);

CREATE TABLE IF NOT EXISTS line_items (
    id          UUID PRIMARY KEY,
    claim_id    UUID NOT NULL,
    title VARCHAR(255),
    category VARCHAR(50),
    date DATE,

    CONSTRAINT fk_line_items_claim
        FOREIGN KEY (claim_id)
        REFERENCES claims (id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS claim_evidence (
    id          UUID PRIMARY KEY,
    claim_id    UUID NOT NULL,
    file_key VARCHAR(255),
    file_size BIGINT NOT NULL DEFAULT 0,
    submitted_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_claim_evidence_claim
        FOREIGN KEY (claim_id)
        REFERENCES claims (id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS line_item_claim_evidence (
    line_item_id      UUID NOT NULL,
    claim_evidence_id UUID NOT NULL,

    CONSTRAINT pk_line_item_claim_evidence
        PRIMARY KEY (line_item_id, claim_evidence_id),

    CONSTRAINT fk_lice_line_item
        FOREIGN KEY (line_item_id)
        REFERENCES line_items (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_lice_claim_evidence
        FOREIGN KEY (claim_evidence_id)
        REFERENCES claim_evidence (id)
        ON DELETE CASCADE
);
