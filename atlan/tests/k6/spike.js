// tests/k6/spike.js
import http from 'k6/http';
import { sleep, check, group } from 'k6';
import { Trend } from 'k6/metrics';

export const options = {
  stages: [
    { duration: '30s', target: 0 },
    { duration: '60s', target: 1000 },
    { duration: '60s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<500'],
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

// Using environment variables to be flexible
const LIST_PATH = __ENV.LIST_PATH || '/api/v1/events?page=0&size=20';
const DETAIL_PATH = __ENV.DETAIL_PATH || '/api/v1/events/1';
const TOKEN = __ENV.TOKEN || '';

const headers = TOKEN ? { Authorization: `Bearer ${TOKEN}` } : {};

const tEventList = new Trend('event_list_ms');
const tEventDetail = new Trend('event_detail_ms');

export function setup() {
  const health = http.get(`${BASE}/actuator/health`);
  if (health.status !== 200) {
    throw new Error(`Health check failed with status ${health.status}`);
  }
}

export default function () {
  group('events:list', () => {
    const res = http.get(`${BASE}${LIST_PATH}`, { headers });
    tEventList.add(res.timings.duration);
    check(res, {
      'status is 200 (list)': (r) => r.status === 200,
    });
  });

  group('events:detail', () => {
    const res = http.get(`${BASE}${DETAIL_PATH}`, { headers });
    tEventDetail.add(res.timings.duration);
    check(res, {
      'status is 200 or 404 (detail)': (r) => r.status === 200 || r.status === 404,
    });
  });

  sleep(1);
}
