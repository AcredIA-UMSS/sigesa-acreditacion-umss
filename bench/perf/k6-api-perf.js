import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

/**
 * SIGESA API performance — steady load for near-real-time Grafana/Prometheus.
 * Not an E3/Toxiproxy comparison: measures current endpoint latency & errors.
 *
 * Env:
 *   API_BASE_URL   (default http://localhost:8080)
 *   K6_LOGIN_EMAIL / K6_LOGIN_PASSWORD  (optional authenticated path)
 *   K6_VUS / K6_DURATION
 */

const base = (__ENV.API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const email = __ENV.K6_LOGIN_EMAIL || 'jd@umss.edu.bo';
const password = __ENV.K6_LOGIN_PASSWORD || 'JefeDemo2026!';

const healthDuration = new Trend('sigesa_health_duration', true);
const publicDuration = new Trend('sigesa_public_duration', true);
const authDuration = new Trend('sigesa_auth_duration', true);
const errors = new Rate('sigesa_errors');
const requests = new Counter('sigesa_requests');

export const options = {
  scenarios: {
    steady: {
      executor: 'constant-vus',
      vus: Number(__ENV.K6_VUS || 10),
      duration: __ENV.K6_DURATION || '2m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1500'],
    sigesa_errors: ['rate<0.05'],
  },
  summaryTrendStats: ['med', 'p(50)', 'p(95)', 'p(99)', 'max'],
};

let token = null;

export function setup() {
  const res = http.post(
    `${base}/api/v1/auth/login`,
    JSON.stringify({ email, password }),
    { headers: { 'Content-Type': 'application/json' }, tags: { name: 'login' } },
  );
  let accessToken = null;
  if (res.status === 200) {
    try {
      accessToken = res.json('accessToken') || res.json('token') || null;
    } catch (_) {
      accessToken = null;
    }
  }
  return { accessToken };
}

export default function (data) {
  group('public', () => {
    const health = http.get(`${base}/api/health`, { tags: { name: 'health', endpoint: 'health' } });
    healthDuration.add(health.timings.duration);
    requests.add(1);
    const healthOk = check(health, { 'health 200': (r) => r.status === 200 });
    errors.add(!healthOk);

    const news = http.get(`${base}/api/v1/public/news?page=0&size=10`, {
      tags: { name: 'public_news', endpoint: 'public_news' },
    });
    publicDuration.add(news.timings.duration);
    requests.add(1);
    errors.add(!check(news, { 'news 200': (r) => r.status === 200 }));

    const featured = http.get(`${base}/api/v1/public/news/featured`, {
      tags: { name: 'public_news_featured', endpoint: 'public_news_featured' },
    });
    publicDuration.add(featured.timings.duration);
    requests.add(1);
    // 200 or 204 (no featured)
    errors.add(!check(featured, { 'featured ok': (r) => r.status === 200 || r.status === 204 }));

    const programs = http.get(`${base}/api/v1/public/programs`, {
      tags: { name: 'public_programs', endpoint: 'public_programs' },
    });
    publicDuration.add(programs.timings.duration);
    requests.add(1);
    errors.add(!check(programs, { 'programs 200': (r) => r.status === 200 }));
  });

  if (data && data.accessToken) {
    group('authenticated', () => {
      const headers = {
        Authorization: `Bearer ${data.accessToken}`,
        'Content-Type': 'application/json',
      };
      const processes = http.get(`${base}/api/v1/processes`, {
        headers,
        tags: { name: 'processes', endpoint: 'processes' },
      });
      authDuration.add(processes.timings.duration);
      requests.add(1);
      errors.add(!check(processes, { 'processes 200': (r) => r.status === 200 }));
    });
  }

  sleep(0.3);
}
