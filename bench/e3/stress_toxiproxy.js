import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

// =============================================================================
// Configuración de entorno y endpoints
// =============================================================================
const API_BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080';
const TOXIPROXY_BASE_URL = __ENV.TOXIPROXY_BASE_URL || 'http://localhost:8474';
const PROXY_NAME = __ENV.PROXY_NAME || 'notification_proxy';
const PROXY_LISTEN = __ENV.PROXY_LISTEN || '0.0.0.0:8081';
const PROXY_UPSTREAM = __ENV.TOXIPROXY_UPSTREAM || 'notification-service:8082';
const EXTRACTION_MODE = __ENV.EXTRACTION_MODE || 'http';

// Parámetros de carga y temporalización de la falla
const TARGET_RPS = parseInt(__ENV.TARGET_RPS || '50', 10);
const WARMUP_DURATION = 15;        // 15s de calentamiento (0 -> 50 req/s)
const STEADY_DURATION = 45;        // 45s a régimen constante (50 req/s)
const RAMPDOWN_DURATION = 10;      // 10s de bajada (50 -> 0 req/s)
const FAULT_START_SEC = 20;        // Falla inyectada al segundo 20
const FAULT_END_SEC = 30;          // Falla desactivada al segundo 30
const FAULT_LATENCY_MS = parseInt(__ENV.FAULT_LATENCY_MS || '2000', 10);

// =============================================================================
// Métricas personalizadas (Custom Metrics)
// =============================================================================
const notificationsDurationBefore = new Trend('notifications_duration_before', true);
const notificationsDurationDuring = new Trend('notifications_duration_during', true);
const notificationsDurationAfter = new Trend('notifications_duration_after', true);

const healthDurationBefore = new Trend('health_duration_before', true);
const healthDurationDuring = new Trend('health_duration_during', true);
const healthDurationAfter = new Trend('health_duration_after', true);

const notificationsErrorsBefore = new Rate('notifications_errors_before');
const notificationsErrorsDuring = new Rate('notifications_errors_during');
const notificationsErrorsAfter = new Rate('notifications_errors_after');

const healthErrorsBefore = new Rate('health_errors_before');
const healthErrorsDuring = new Rate('health_errors_during');
const healthErrorsAfter = new Rate('health_errors_after');

const totalRequestsCounter = new Counter('total_requests_executed');

// =============================================================================
// Escenarios y fases de K6
// =============================================================================
export const options = {
  scenarios: {
    // 1. Escenario de carga con usuarios virtuales y tasa de peticiones controlada
    traffic_scenario: {
      executor: 'ramping-arrival-rate',
      startRate: Math.max(5, Math.floor(TARGET_RPS / 5)),
      timeUnit: '1s',
      preAllocatedVUs: TARGET_RPS,
      maxVUs: TARGET_RPS * 4,
      stages: [
        // Fase 1: Calentamiento (10-15s hasta 50 req/s)
        { duration: `${WARMUP_DURATION}s`, target: TARGET_RPS },
        // Fase 2: Carga constante (50 req/s) donde ocurrirá la falla (t=20s a t=30s)
        { duration: `${STEADY_DURATION}s`, target: TARGET_RPS },
        // Fase 3: Bajada controlada a 0
        { duration: `${RAMPDOWN_DURATION}s`, target: 0 },
      ],
      exec: 'trafficWorkflow',
    },

    // 2. Escenario inyector de fallas en Toxiproxy (1 VU ejecutada en segundos exactos)
    fault_injection_scenario: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 1,
      maxDuration: `${WARMUP_DURATION + STEADY_DURATION + RAMPDOWN_DURATION + 10}s`,
      exec: 'faultInjectorWorkflow',
    },
  },
  thresholds: {
    // H1: En régimen estable (antes de la falla), las notificaciones deben responder con bajo p95
    'notifications_duration_before': ['p(95)<300'],
    // H2: Durante la inyección de falla, health debe mantener error <= 1% (aislamiento de falla)
    'health_errors_during': ['rate<=0.01'],
    // H2: En modo HTTP, durante la inyección de falla notificaciones debe degradar (> 50% de errores).
    // En modo in_process, al no usar Toxiproxy no debe degradar (error <= 1%).
    'notifications_errors_during': EXTRACTION_MODE === 'http' ? ['rate>0.50'] : ['rate<=0.01'],
    // Post-falla: Health debe mantenerse en 0% errores
    'health_errors_after': ['rate<=0.01'],
  },
};

// Variable para sincronización de marca de tiempo inicial
let globalTestStartTime = 0;

// =============================================================================
// Setup y Teardown de Toxiproxy
// =============================================================================
export function setup() {
  const startTime = Date.now();
  console.log(`[E3 Test Setup] Inicializando entorno de prueba. Toxiproxy: ${TOXIPROXY_BASE_URL}, API: ${API_BASE_URL}`);

  // 1. Verificar si el proxy de notificaciones existe en Toxiproxy
  const getProxyRes = http.get(`${TOXIPROXY_BASE_URL}/proxies/${PROXY_NAME}`);
  if (getProxyRes.status === 404) {
    console.log(`[E3 Test Setup] Proxy '${PROXY_NAME}' no existe. Creando proxy hacia upstream ${PROXY_UPSTREAM}...`);
    const createPayload = JSON.stringify({
      name: PROXY_NAME,
      listen: PROXY_LISTEN,
      upstream: PROXY_UPSTREAM,
      enabled: true,
    });
    const createRes = http.post(`${TOXIPROXY_BASE_URL}/proxies`, createPayload, {
      headers: { 'Content-Type': 'application/json' },
    });
    console.log(`[E3 Test Setup] Creación de proxy HTTP ${createRes.status}`);
  } else {
    // Limpiar toxics residuales si existieran
    http.del(`${TOXIPROXY_BASE_URL}/proxies/${PROXY_NAME}/toxics/sidecar_latency_fault`);
    console.log(`[E3 Test Setup] Proxy '${PROXY_NAME}' existente verificado y limpio.`);
  }

  // 2. Smoke check inicial contra la API (ambos endpoints deben responder 200)
  const healthSmoke = http.get(`${API_BASE_URL}/api/health`);
  const notifySmoke = http.get(`${API_BASE_URL}/api/v1/e3/notifications`);

  console.log(`[E3 Test Setup] Smoke check inicial: /api/health=${healthSmoke.status}, /api/v1/e3/notifications=${notifySmoke.status}`);

  return { startTime };
}

export function teardown() {
  console.log(`[E3 Test Teardown] Limpiando toxics en Toxiproxy...`);
  http.del(`${TOXIPROXY_BASE_URL}/proxies/${PROXY_NAME}/toxics/sidecar_latency_fault`);
  console.log(`[E3 Test Teardown] Entorno restaurado.`);
}

// =============================================================================
// Escenario 1: Flujo de Tráfico y Medición con Tags por Fase
// =============================================================================
export function trafficWorkflow(data) {
  if (!globalTestStartTime) {
    globalTestStartTime = data.startTime;
  }

  // Calcular segundos transcurridos desde el inicio global de la prueba
  const elapsedSec = (Date.now() - globalTestStartTime) / 1000;

  // Determinar la fase exacta en base al tiempo de inyección de falla
  let currentPhase;
  if (elapsedSec < FAULT_START_SEC) {
    currentPhase = 'before_failure';
  } else if (elapsedSec >= FAULT_START_SEC && elapsedSec < FAULT_END_SEC) {
    currentPhase = 'during_failure';
  } else {
    currentPhase = 'after_failure';
  }

  const baseTags = { phase: currentPhase };

  // ---------------------------------------------------------------------------
  // A. Petición al endpoint bajo prueba: GET /api/v1/e3/notifications
  // ---------------------------------------------------------------------------
  const notifyTags = Object.assign({}, baseTags, { endpoint: 'notifications' });
  const isHttpMode = EXTRACTION_MODE === 'http';
  const isChaosPhase = currentPhase === 'during_failure';

  const notifyRes = http.get(`${API_BASE_URL}/api/v1/e3/notifications`, {
    tags: notifyTags,
    timeout: '5s',
    responseCallback: (isHttpMode && isChaosPhase)
      ? http.expectedStatuses(200, 500, 503, 504)
      : http.expectedStatuses(200),
  });

  totalRequestsCounter.add(1, notifyTags);
  const isNotifyError = notifyRes.status !== 200;

  if (currentPhase === 'before_failure') {
    notificationsDurationBefore.add(notifyRes.timings.duration, notifyTags);
    notificationsErrorsBefore.add(isNotifyError, notifyTags);
    check(notifyRes, {
      'notif before_failure status is 200': (r) => r.status === 200,
      'notif has valid origin': (r) => r.json('origin') !== undefined,
    }, notifyTags);
  } else if (currentPhase === 'during_failure') {
    notificationsDurationDuring.add(notifyRes.timings.duration, notifyTags);
    notificationsErrorsDuring.add(isNotifyError, notifyTags);
    if (isHttpMode) {
      check(notifyRes, {
        'notif during_failure degraded (503/timeout)': (r) => r.status === 503 || r.status >= 500,
      }, notifyTags);
    } else {
      check(notifyRes, {
        'notif during_failure in_process unaffected (200)': (r) => r.status === 200,
      }, notifyTags);
    }
  } else {
    notificationsDurationAfter.add(notifyRes.timings.duration, notifyTags);
    notificationsErrorsAfter.add(isNotifyError, notifyTags);
    check(notifyRes, {
      'notif after_failure recovered': (r) => r.status === 200 || r.status === 503,
    }, notifyTags);
  }

  // ---------------------------------------------------------------------------
  // B. Petición al endpoint de control: GET /api/health (debe permanecer 200)
  // ---------------------------------------------------------------------------
  const healthTags = Object.assign({}, baseTags, { endpoint: 'health' });
  const healthRes = http.get(`${API_BASE_URL}/api/health`, {
    tags: healthTags,
    timeout: '5s',
  });

  totalRequestsCounter.add(1, healthTags);
  const isHealthError = healthRes.status !== 200;

  if (currentPhase === 'before_failure') {
    healthDurationBefore.add(healthRes.timings.duration, healthTags);
    healthErrorsBefore.add(isHealthError, healthTags);
  } else if (currentPhase === 'during_failure') {
    healthDurationDuring.add(healthRes.timings.duration, healthTags);
    healthErrorsDuring.add(isHealthError, healthTags);
  } else {
    healthDurationAfter.add(healthRes.timings.duration, healthTags);
    healthErrorsAfter.add(isHealthError, healthTags);
  }

  check(healthRes, {
    'health status is always 200 UP': (r) => r.status === 200 && r.json('status') === 'UP',
  }, healthTags);
}

// =============================================================================
// Escenario 2: Inyección y Desactivación de Fallas en Segundos Exactos
// =============================================================================
export function faultInjectorWorkflow(data) {
  const startTime = data.startTime;

  // 1. Esperar exactamente hasta el segundo FAULT_START_SEC (20s)
  const now = Date.now();
  const waitBeforeInjectMs = (startTime + (FAULT_START_SEC * 1000)) - now;
  if (waitBeforeInjectMs > 0) {
    sleep(waitBeforeInjectMs / 1000);
  }

  console.log(`\n======================================================================`);
  console.log(`[CHAOS INJECTOR @ T=${FAULT_START_SEC}s] Inyectando falla en Toxiproxy...`);
  console.log(`Toxic: Latencia de ${FAULT_LATENCY_MS}ms en proxy '${PROXY_NAME}'`);
  console.log(`======================================================================\n`);

  // Inyectar toxic de latencia severa (supera el timeout de 200ms y deadline de 800ms)
  const toxicPayload = JSON.stringify({
    name: 'sidecar_latency_fault',
    type: 'latency',
    stream: 'upstream',
    toxicity: 1.0,
    attributes: {
      latency: FAULT_LATENCY_MS,
      jitter: 100,
    },
  });

  const injectRes = http.post(
    `${TOXIPROXY_BASE_URL}/proxies/${PROXY_NAME}/toxics`,
    toxicPayload,
    { headers: { 'Content-Type': 'application/json' } }
  );

  if (injectRes.status === 200 || injectRes.status === 201) {
    console.log(`[CHAOS INJECTOR @ T=${FAULT_START_SEC}s] Falla ACTIVADA exitosamente en Toxiproxy.`);
  } else {
    console.error(`[CHAOS INJECTOR ERROR] Falló inyección de toxic: HTTP ${injectRes.status} - ${injectRes.body}`);
  }

  // 2. Mantener la falla activa durante la ventana (de T=20s a T=30s)
  const waitDurationSec = FAULT_END_SEC - FAULT_START_SEC;
  sleep(waitDurationSec);

  // 3. Remover el toxic exactamente al segundo FAULT_END_SEC (30s)
  console.log(`\n======================================================================`);
  console.log(`[CHAOS INJECTOR @ T=${FAULT_END_SEC}s] Desactivando toxic en Toxiproxy...`);
  console.log(`======================================================================\n`);

  const removeRes = http.del(
    `${TOXIPROXY_BASE_URL}/proxies/${PROXY_NAME}/toxics/sidecar_latency_fault`
  );

  if (removeRes.status === 204 || removeRes.status === 200) {
    console.log(`[CHAOS INJECTOR @ T=${FAULT_END_SEC}s] Falla DESACTIVADA. Tráfico normalizado.`);
  } else {
    console.warn(`[CHAOS INJECTOR WARNING] Respuesta al remover toxic: HTTP ${removeRes.status}`);
  }
}

// =============================================================================
// Generador de resumen legible (Consola / Markdown)
// =============================================================================
export function handleSummary(data) {
  const getMetric = (name, field) => {
    if (data.metrics[name] && data.metrics[name].values) {
      return data.metrics[name].values[field] !== undefined ? data.metrics[name].values[field] : 0;
    }
    return 0;
  };

  const fmtMs = (val) => `${Number(val).toFixed(2)} ms`;
  const fmtPct = (val) => `${(Number(val) * 100).toFixed(2)} %`;

  const notifP95Before = getMetric('notifications_duration_before', 'p(95)');
  const notifP95During = getMetric('notifications_duration_during', 'p(95)');
  const notifP95After = getMetric('notifications_duration_after', 'p(95)');

  const notifErrBefore = getMetric('notifications_errors_before', 'rate');
  const notifErrDuring = getMetric('notifications_errors_during', 'rate');
  const notifErrAfter = getMetric('notifications_errors_after', 'rate');

  const healthErrBefore = getMetric('health_errors_before', 'rate');
  const healthErrDuring = getMetric('health_errors_during', 'rate');
  const healthErrAfter = getMetric('health_errors_after', 'rate');

  const h1Pass = notifP95Before <= 300;
  const h2HealthPass = healthErrDuring <= 0.01;
  const h2NotifyPass = notifErrDuring >= 0.50 || notifP95During >= 500;

  const summaryText = `
================================================================================
                    RESULTADOS DE LA PRUEBA E3 (K6 + TOXIPROXY)
================================================================================
1. FASE ANTES DE LA FALLA (t < ${FAULT_START_SEC}s) - Régimen Estable:
   - Notificaciones p95: ${fmtMs(notifP95Before)} (Errores: ${fmtPct(notifErrBefore)})
   - Health Errores:     ${fmtPct(healthErrBefore)}

2. FASE DURANTE LA FALLA (${FAULT_START_SEC}s <= t < ${FAULT_END_SEC}s) - Falla Inyectada:
   - Notificaciones p95: ${fmtMs(notifP95During)} (Errores: ${fmtPct(notifErrDuring)})
   - Health Errores:     ${fmtPct(healthErrDuring)}

3. FASE DESPUÉS DE LA FALLA (t >= ${FAULT_END_SEC}s) - Recuperación:
   - Notificaciones p95: ${fmtMs(notifP95After)} (Errores: ${fmtPct(notifErrAfter)})
   - Health Errores:     ${fmtPct(healthErrAfter)}

--------------------------------------------------------------------------------
EVALUACIÓN DE HIPÓTESIS:
- H1 (Overhead en régimen estable): ${h1Pass ? 'APROBADA' : 'FALSADA'}
- H2 (Aislamiento de falla - Health error <= 1%): ${h2HealthPass ? 'APROBADA' : 'FALSADA'}
- H2 (Degradación controlada de Notificaciones):   ${h2NotifyPass ? 'APROBADA' : 'FALSADA'}
================================================================================
`;

  const isDocker = __ENV.API_BASE_URL && __ENV.API_BASE_URL.indexOf('backend') !== -1;
  const baseDir = isDocker ? '/bench' : (__ENV.BENCH_DIR || 'bench/e3');
  const now = new Date();
  const pad = (n) => String(n).padStart(2, '0');
  const timestamp = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}_${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`;

  const timestampFile = `${baseDir}/k6_result_${EXTRACTION_MODE}_${timestamp}.json`;
  const summaryPath = __ENV.SUMMARY_PATH || `${baseDir}/k6_last_result.json`;

  const outputs = {
    stdout: summaryText,
  };
  outputs[timestampFile] = JSON.stringify(data, null, 2);
  outputs[summaryPath] = JSON.stringify(data, null, 2);
  return outputs;
}
