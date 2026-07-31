-- ============================================================
-- V3__CREATE_CONTRIBUTOR_PROFILES.sql
-- Creates the contributor_profiles table, linked to
-- account_profiles via FK (one profile per account)
-- ============================================================

CREATE TABLE IF NOT EXISTS contributor_profiles
(
    id                   UUID         NOT NULL,
    account_id           UUID         NOT NULL,
    identity_card_number VARCHAR(50)  NOT NULL,
    bank_name            VARCHAR(100) NOT NULL,
    bank_account_number  VARCHAR(50)  NOT NULL,
    tax_id               VARCHAR(50)  NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_contributor_profiles
        PRIMARY KEY (id),

    CONSTRAINT fk_contributor_profiles_account
        FOREIGN KEY (account_id)
        REFERENCES account_profiles (id)
        ON DELETE CASCADE,

    CONSTRAINT uq_contributor_account_id
        UNIQUE (account_id),

    CONSTRAINT uq_contributor_identity_card
        UNIQUE (identity_card_number),

    CONSTRAINT uq_contributor_bank_account
        UNIQUE (bank_account_number),

    CONSTRAINT uq_contributor_tax_id
        UNIQUE (tax_id)
);

CREATE INDEX idx_contributor_profiles_account_id ON contributor_profiles (account_id);
