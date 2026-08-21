-- Migración V9: Funciones PostgreSQL para flujo de aprobación y rechazo de indicadores (FSD-UC-008, FSD-UC-009)

-- 1. Función genérica de transición append-only en Postgres
CREATE OR REPLACE FUNCTION fn_indicator_transition(
  p_indicator_id UUID,
  p_to_status VARCHAR,
  p_actor_id UUID,
  p_actor_role VARCHAR,
  p_reason TEXT DEFAULT NULL,
  p_correlation_id UUID DEFAULT gen_random_uuid()
) RETURNS VOID AS $$
DECLARE
  v_from VARCHAR;
BEGIN
  IF NOT EXISTS (SELECT 1 FROM indicator WHERE id = p_indicator_id) THEN
    RAISE EXCEPTION 'INDICATOR_NOT_FOUND';
  END IF;

  SELECT new_state INTO v_from
  FROM indicator_state_history
  WHERE indicator_id = p_indicator_id
  ORDER BY created_at DESC
  LIMIT 1;

  v_from := COALESCE(v_from, 'PENDIENTE');

  INSERT INTO indicator_state_history (
    id, indicator_id, previous_state, new_state, actor_id, actor_role, created_at
  ) VALUES (
    COALESCE(p_correlation_id, gen_random_uuid()),
    p_indicator_id, v_from, p_to_status, p_actor_id, p_actor_role, NOW()
  );
END;
$$ LANGUAGE plpgsql;

-- 2. Función PostgreSQL para aprobar indicador (FSD-UC-009)
CREATE OR REPLACE FUNCTION fn_approve_indicator(
  p_indicator_id UUID,
  p_actor_id UUID,
  p_actor_role VARCHAR DEFAULT 'TD'
) RETURNS UUID AS $$
DECLARE
  v_history_id UUID := gen_random_uuid();
  v_from VARCHAR;
BEGIN
  IF p_actor_role <> 'TD' AND p_actor_role <> 'JD' THEN
    RAISE EXCEPTION 'FORBIDDEN_ROLE: Solo el Director Tecnico [TD] o Jefatura [JD] pueden aprobar indicadores';
  END IF;

  IF NOT EXISTS (SELECT 1 FROM indicator WHERE id = p_indicator_id) THEN
    RAISE EXCEPTION 'INDICATOR_NOT_FOUND';
  END IF;

  SELECT new_state INTO v_from
  FROM indicator_state_history
  WHERE indicator_id = p_indicator_id
  ORDER BY created_at DESC
  LIMIT 1;

  v_from := COALESCE(v_from, 'PENDIENTE');

  IF v_from <> 'SUBIDO' AND v_from <> 'SUBSANADO' THEN
    RAISE EXCEPTION 'INVALID_STATE: El indicador debe estar en estado SUBIDO o SUBSANADO para ser aprobado';
  END IF;

  -- Resolver observaciones previas
  UPDATE tb_observation
  SET status = 'RESOLVED'
  WHERE indicator_id = p_indicator_id::text
    AND status <> 'RESOLVED';

  -- Registrar transición append-only
  INSERT INTO indicator_state_history (
    id, indicator_id, previous_state, new_state, actor_id, actor_role, created_at
  ) VALUES (
    v_history_id, p_indicator_id, v_from, 'APROBADO', p_actor_id, p_actor_role, NOW()
  );

  RETURN v_history_id;
END;
$$ LANGUAGE plpgsql;

-- 3. Función PostgreSQL para rechazar u observar indicador (FSD-UC-008)
CREATE OR REPLACE FUNCTION fn_reject_indicator(
  p_indicator_id UUID,
  p_justification TEXT,
  p_actor_id UUID,
  p_actor_role VARCHAR DEFAULT 'TD'
) RETURNS VARCHAR AS $$
DECLARE
  v_history_id UUID := gen_random_uuid();
  v_from VARCHAR;
  v_obs_id VARCHAR;
  v_program_id UUID;
BEGIN
  IF p_actor_role <> 'TD' AND p_actor_role <> 'JD' THEN
    RAISE EXCEPTION 'FORBIDDEN_ROLE: Solo el Director Tecnico [TD] o Jefatura [JD] pueden rechazar indicadores';
  END IF;

  IF p_justification IS NULL OR length(trim(p_justification)) < 20 THEN
    RAISE EXCEPTION 'JUSTIFICATION_REQUIRED: La justificación debe tener al menos 20 caracteres';
  END IF;

  SELECT program_id INTO v_program_id
  FROM indicator
  WHERE id = p_indicator_id;

  IF v_program_id IS NULL THEN
    RAISE EXCEPTION 'INDICATOR_NOT_FOUND';
  END IF;

  SELECT new_state INTO v_from
  FROM indicator_state_history
  WHERE indicator_id = p_indicator_id
  ORDER BY created_at DESC
  LIMIT 1;

  v_from := COALESCE(v_from, 'PENDIENTE');

  IF v_from <> 'SUBIDO' AND v_from <> 'SUBSANADO' THEN
    RAISE EXCEPTION 'INVALID_STATE: El indicador debe estar en estado SUBIDO o SUBSANADO para ser rechazado';
  END IF;

  -- Generar nueva observación
  v_obs_id := 'OBS-' || upper(substring(replace(gen_random_uuid()::text, '-', ''), 1, 8));

  INSERT INTO tb_observation (
    observation_id, program_id, indicator_id, indicator_code, indicator_title,
    description, issue_date, due_date, status, remediation_url
  ) VALUES (
    v_obs_id, v_program_id, p_indicator_id::text, substring(p_indicator_id::text, 1, 8), p_indicator_id::text,
    p_justification, CURRENT_DATE, CURRENT_DATE + INTERVAL '10 days', 'PENDIENTE_SUBSANACION',
    '/coordinator/evidences/' || p_indicator_id::text || '/subsanar'
  );

  -- Registrar transición append-only
  INSERT INTO indicator_state_history (
    id, indicator_id, previous_state, new_state, actor_id, actor_role, created_at
  ) VALUES (
    v_history_id, p_indicator_id, v_from, 'OBSERVADO', p_actor_id, p_actor_role, NOW()
  );

  RETURN v_obs_id;
END;
$$ LANGUAGE plpgsql;
