// tests/k6/last_seat.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

export const options = {
  scenarios: {
    race_for_last_seat: {
      executor: 'constant-arrival-rate',           // steady request rate regardless of response time
      rate: Number(__ENV.RATE || 50),               // requests per second
      timeUnit: '1s',
      duration: __ENV.DURATION || '30s',            // test length
      preAllocatedVUs: Number(__ENV.VUS || 50),    // initial VUs to handle load
      maxVUs: Number(__ENV.MAX_VUS || 200),         // max VUs if needed
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],                 // max 5% failed requests allowed
    http_req_duration: ['p(95)<800'],               // 95% under 800ms latency
    'booking_success': ['count<=' + (Number(__ENV.CAPACITY || 1))],  // booking success must not exceed capacity
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const EVENT_ID = Number(__ENV.EVENT_ID || 1);
const CAPACITY = Number(__ENV.CAPACITY || 1);
const TOKEN = __ENV.TOKEN || '';

const bookingSuccess = new Counter('booking_success');
const bookingDuplicate = new Counter('booking_duplicate');
const bookingRejected = new Counter('booking_rejected');

// Generate a unique idempotency key per attempt to simulate real client retries
function idemKey() {
  return `idem-${Date.now()}-${__VU}-${__ITER}`;
}

export default function () {
  const userId = (__VU % 10000) + 1;   // Vary user IDs realistically
  const url = `${BASE}/api/v1/bookings`;
  const payload = JSON.stringify({ userId, eventId: EVENT_ID });

  const headers = {
    'Content-Type': 'application/json',
    'Idempotency-Key': idemKey(),
  };
  if (TOKEN) headers['Authorization'] = `Bearer ${TOKEN}`;

  const res = http.post(url, payload, { headers });

  if (res.status >= 200 && res.status < 300) {
    bookingSuccess.add(1);
  } else if (res.status === 409 || res.status === 422) {
    bookingDuplicate.add(1);
  } else if ([400, 403, 404, 429].includes(res.status) || res.status >= 500) {
    bookingRejected.add(1);
  }

  check(res, {
    'ok or handled reject': (r) =>
      (r.status >= 200 && r.status < 300) || [400, 403, 404, 409, 422, 429].includes(r.status),
  });

  // Small random sleep to spread load slightly
  sleep(Math.random() * 0.2);
}
