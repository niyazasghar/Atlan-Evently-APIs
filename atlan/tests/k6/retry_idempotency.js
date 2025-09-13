import http from 'k6/http';
import { check } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.1.0/index.js';

export const options = {
  vus: 1,
  iterations: 3,
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const TOKEN = __ENV.TOKEN || '';
const USER_ID = Number(__ENV.USER_ID || 1);
const EVENT_ID = Number(__ENV.EVENT_ID || 1);

export default function () {
  const idemKey = uuidv4(); // generate random idempotency key (UUID v4)

  const payload = JSON.stringify({ userId: USER_ID, eventId: EVENT_ID });
  const headers = {
    'Content-Type': 'application/json',
    'Idempotency-Key': idemKey,
  };
  if (TOKEN) headers['Authorization'] = `Bearer ${TOKEN}`;

  // First request - should create a new booking
  let res1 = http.post(`${BASE}/api/v1/bookings`, payload, { headers });
  check(res1, { 'first request success': (r) => r.status === 200 || r.status === 201 });

  // Retry request - same key and payload, should return same result, no duplicates
  let res2 = http.post(`${BASE}/api/v1/bookings`, payload, { headers });
  check(res2, {
    'retry status matches': (r) => r.status === res1.status,
    'retry body matches': (r) => r.body === res1.body,
  });

  // Retry with same key but different payload - expect conflict or error (4xx)
  const alteredPayload = JSON.stringify({ userId: USER_ID, eventId: EVENT_ID + 1 });
  let res3 = http.post(`${BASE}/api/v1/bookings`, alteredPayload, { headers });
  check(res3, { 'conflict on payload change': (r) => r.status >= 400 && r.status < 500 });
}
