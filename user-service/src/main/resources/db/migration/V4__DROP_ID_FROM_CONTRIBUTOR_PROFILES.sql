-- ============================================================
-- V4__DROP_ID_FROM_CONTRIBUTOR_PROFILES.sql
-- Removes the 'id' column from contributor_profiles and
-- sets 'account_id' as the new primary key.
-- ============================================================

-- 1. Drop existing primary key constraint on 'id'
ALTER TABLE contributor_profiles
DROP CONSTRAINT pk_contributor_profiles;

-- 2. Drop the 'id' column
ALTER TABLE contributor_profiles
DROP COLUMN id;

-- 3. Drop the redundant unique constraint on 'account_id'
ALTER TABLE contributor_profiles
DROP CONSTRAINT uq_contributor_account_id;

-- 4. Set 'account_id' as the new primary key
ALTER TABLE contributor_profiles
    ADD CONSTRAINT pk_contributor_profiles PRIMARY KEY (account_id);

-- 5. Drop redundant index on 'account_id' (now automatically indexed by PK)
DROP INDEX IF EXISTS idx_contributor_profiles_account_id;