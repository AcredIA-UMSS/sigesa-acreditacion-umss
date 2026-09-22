-- M5: Remove v1 Phase/Subphase legacy schema
DROP TABLE IF EXISTS subphase_observation CASCADE;
DROP TABLE IF EXISTS subphases CASCADE;
DROP TABLE IF EXISTS phases CASCADE;
DROP TABLE IF EXISTS template_subphases CASCADE;
DROP TABLE IF EXISTS template_phases CASCADE;
ALTER TABLE normative_indicators DROP COLUMN IF EXISTS legacy_subphase_id;
ALTER TABLE level1_nodes DROP COLUMN IF EXISTS legacy_phase_id;
ALTER TABLE evidence DROP COLUMN IF EXISTS subphase_id;
