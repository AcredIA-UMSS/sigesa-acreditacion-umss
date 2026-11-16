-- V29: campos opcionales del certificado PDF (adición FSD-UC-016 / PRD-US-020).
-- Sin issued_at: publication_snapshot no tiene fecha de publicación.

ALTER TABLE publication_snapshot
    ADD COLUMN IF NOT EXISTS agency VARCHAR(255),
    ADD COLUMN IF NOT EXISTS resolution_number VARCHAR(64);
