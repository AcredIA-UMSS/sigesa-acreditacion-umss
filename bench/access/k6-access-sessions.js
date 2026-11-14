import http from 'k6/http';
import { check } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const listDuration = new Trend('access_list_duration', true);
const summaryDuration = new Trend('access_summary_duration', true);
const listFailed = new Rate('access_list_failed');
const summaryFailed = new Rate('access_summary_failed');

export const options = {
  vus: 10,
  duration: __ENV.DURATION || '20s',
  summaryTrendStats: ['med', 'p(50)', 'p(95)', 'p(99)', 'max'],
};

const base = __ENV.BASE_URL || 'http://127.0.0.1:8080';

export function setup() {
  const login = http.post(
    `${base}/api/v1/auth/login`,
    JSON.stringify({
      email: __ENV.JD_EMAIL || 'jd@umss.edu.bo',
      password: __ENV.JD_PASSWORD || 'JefeDemo2026!',
    }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  const ok = check(login, { 'login 200': (response) => response.status === 200 });
  if (!ok) {
    throw new Error(`login JD falló: ${login.status} ${login.body}`);
  }
  return { token: login.json('accessToken') };
}

export default function (data) {
  const headers = { Authorization: `Bearer ${data.token}` };

  const list = http.get(`${base}/api/v1/access-sessions?size=20`, {
    headers,
    tags: { name: 'access_list' },
  });
  listDuration.add(list.timings.duration);
  listFailed.add(list.status !== 200);
  check(list, { 'list 200': (response) => response.status === 200 });

  const summary = http.get(`${base}/api/v1/access-sessions/summary`, {
    headers,
    tags: { name: 'access_summary' },
  });
  summaryDuration.add(summary.timings.duration);
  summaryFailed.add(summary.status !== 200);
  check(summary, { 'summary 200': (response) => response.status === 200 });
}
