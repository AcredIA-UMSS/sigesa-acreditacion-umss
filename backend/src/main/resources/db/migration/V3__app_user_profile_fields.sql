-- Perfil extendido de usuario (FSD-UC-002 / gestión usuarios JD)
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS first_name VARCHAR(100);
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS last_name VARCHAR(100);
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20);
