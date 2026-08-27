-- Rol evaluador externo [EE] (FSD-UC-020 / PR-IMPL-014)
ALTER TABLE app_user DROP CONSTRAINT IF EXISTS app_user_role_check;

ALTER TABLE app_user ADD CONSTRAINT app_user_role_check
    CHECK (role IN ('CC', 'TD', 'JD', 'EE'));
