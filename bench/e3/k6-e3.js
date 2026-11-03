import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const notificationsDuration = new Trend('notifications_duration', true);
const healthDuration = new Trend('health_duration', true);
const notificationsFailed = new Rate('notifications_failed');
const healthFailed = new Rate('health_failed');
const notificationsHung = new Counter('notifications_hung');
const healthHung = new Counter('health_hung');

const timeout = __ENV.TIMEOUT || '5s';

export const options = {
  vus: 10,
  duration: __ENV.DURATION || '20s',
  summaryTrendStats: ['med', 'p(50)', 'p(95)', 'p(99)', 'max'],
};

const base = __ENV.BASE_URL;

export default function () {
  const notifications = http.get(`${base}/api/v1/e3/notifications`, {
    tags: { name: 'notifications' },
    timeout,
  });
  notificationsDuration.add(notifications.timings.duration);
  notificationsFailed.add(notifications.status !== 200);
  if (notifications.status === 0) {
    notificationsHung.add(1);
  }
  check(notifications, {
    'notifications 200': (response) => response.status === 200,
  });

  const health = http.get(`${base}/api/health`, {
    tags: { name: 'health' },
    timeout,
  });
  healthDuration.add(health.timings.duration);
  healthFailed.add(health.status !== 200);
  if (health.status === 0) {
    healthHung.add(1);
  }
  check(health, {
    'health 200': (response) => response.status === 200,
  });
}
