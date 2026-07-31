-- ============================================================
-- V2__RENAME_ACCOUNTS_TO_ACCOUNT_PROFILES.sql
-- Renames the accounts table to account_profiles and updates
-- all associated constraints and indexes accordingly
-- ============================================================

ALTER TABLE accounts RENAME TO account_profiles;

ALTER INDEX pk_accounts                  RENAME TO pk_account_profiles;
ALTER INDEX accounts_phone_number_key    RENAME TO account_profiles_phone_number_key;