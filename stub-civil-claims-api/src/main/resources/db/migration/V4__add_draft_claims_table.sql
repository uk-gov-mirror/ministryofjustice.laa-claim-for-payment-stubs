-- Enable UUID generation in Postgres (ignored by H2)

CREATE TABLE IF NOT EXISTS draft_claims (
      id                  UUID PRIMARY KEY,
      payload             JSONB NOT NULL,
      provider_user_id    UUID NOT NULL
);