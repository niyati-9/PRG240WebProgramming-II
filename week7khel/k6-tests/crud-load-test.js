/**
 * K6 Load Test - User CRUD Operations (Lightweight - 10 VUs)
 *
 * Simulates 10 VUs performing CRUD operations for 50 seconds
 * Operations: 30% Create, 40% Read, 20% Update, 10% Delete
 *
 * @author Khel App Load Testing
 * @version 1.0
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter, Trend } from 'k6/metrics';

// ========== METRICS ==========
const crudSuccess = new Rate('crud_success_rate');
const opDuration = new Trend('operation_duration');
const createOps = new Counter('create_ops');
const readOps = new Counter('read_ops');
const updateOps = new Counter('update_ops');
const deleteOps = new Counter('delete_ops');

// ========== CONFIG - LIGHTWEIGHT ==========
export const options = {
  stages: [
    { duration: '10s', target: 10 },   // Ramp up to 10 users over 10s
    { duration: '30s', target: 10 },   // Hold at 10 users for 30s
    { duration: '10s', target: 0 },    // Ramp down to 0 over 10s
  ],
  thresholds: {
    'http_req_duration': ['p(95)<5000'],  // 95% requests under 5s
    'crud_success_rate': ['rate>0.65'],    // 65% success rate
  },
};

const BASE_URL = 'http://localhost:8080/SpringMvcHelloWorld';
let token = null;
let uid = null;

// ========== HELPERS ==========
const genUser = () => `k6_${Date.now()}_${__VU}_${Math.random().toString(36).substr(2, 5)}`;
const genPass = () => `Pass${Math.floor(Math.random() * 9000) + 1000}!`;
const genPhone = () => `984${Math.floor(Math.random() * 9000000) + 1000000}`;

// ========== OPERATIONS ==========
function create() {
  const user = genUser();
  const payload = JSON.stringify({
    username: user,
    password: genPass(),
    email: `${user}@test.com`,
    fullName: `User ${__VU}`,
    phoneNumber: genPhone(),
    userType: 'player',
  });

  const start = Date.now();
  const res = http.post(`${BASE_URL}/api/auth/register`, payload, {
    headers: { 'Content-Type': 'application/json' }
  });
  opDuration.add(Date.now() - start);
  createOps.add(1);

  const ok = check(res, { 'CREATE OK': r => r.status === 200 || r.status === 201 });
  crudSuccess.add(ok ? 1 : 0);

  if (ok) {
    try {
      const data = res.json('data');
      token = data.token;
      uid = data.userId || data.user?.userId;
      console.log(`✅ [VU ${__VU}] CREATE: ${user} - ID: ${uid}`);
    } catch(e) {
      console.log(`⚠️ [VU ${__VU}] CREATE: Success but couldn't extract token`);
    }
  } else {
    console.log(`❌ [VU ${__VU}] CREATE failed: ${res.status}`);
  }
}

function read() {
  if (!token) return create();

  const start = Date.now();
  const res = http.get(`${BASE_URL}/api/users`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  opDuration.add(Date.now() - start);
  readOps.add(1);

  const ok = check(res, { 'READ OK': r => r.status === 200 });
  crudSuccess.add(ok ? 1 : 0);

  if (ok) {
    console.log(`✅ [VU ${__VU}] READ: Retrieved users`);
  } else {
    console.log(`❌ [VU ${__VU}] READ failed: ${res.status}`);
  }
}

function update() {
  if (!token || !uid) return create();

  const payload = JSON.stringify({
    username: genUser(),
    fullName: `Updated ${__VU}`,
    email: `upd_${Date.now()}@test.com`,
    phoneNumber: genPhone(),
    userType: 'player',
  });

  const start = Date.now();
  const res = http.put(`${BASE_URL}/api/users/${uid}`, payload, {
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` }
  });
  opDuration.add(Date.now() - start);
  updateOps.add(1);

  const ok = check(res, { 'UPDATE OK': r => r.status === 200 });
  crudSuccess.add(ok ? 1 : 0);

  if (ok) {
    console.log(`✅ [VU ${__VU}] UPDATE: User ${uid}`);
  } else {
    console.log(`❌ [VU ${__VU}] UPDATE failed: ${res.status}`);
  }
}

function deleteUser() {
  if (!token || !uid) return create();

  const start = Date.now();
  const res = http.del(`${BASE_URL}/api/users/${uid}`, null, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  opDuration.add(Date.now() - start);
  deleteOps.add(1);

  const ok = check(res, { 'DELETE OK': r => r.status === 200 });
  crudSuccess.add(ok ? 1 : 0);

  if (ok) {
    console.log(`✅ [VU ${__VU}] DELETE: User ${uid}`);
  } else {
    console.log(`❌ [VU ${__VU}] DELETE failed: ${res.status}`);
  }

  token = null;
  uid = null;
}

// ========== MAIN ==========
export default function () {
  const rand = Math.random();

  if (rand < 0.3) create();
  else if (rand < 0.7) read();
  else if (rand < 0.9) update();
  else deleteUser();

  sleep(Math.random() * 1.5 + 0.5);
}

// ========== SETUP/TEARDOWN ==========
export function setup() {
  console.log('====================================================================');
  console.log('🚀 K6 CRUD Load Test - LIGHTWEIGHT CONFIG');
  console.log('====================================================================');
  console.log(`📍 Base URL: ${BASE_URL}`);
  console.log('⚙️  Configuration:');
  console.log('   - Virtual Users: 10 concurrent users');
  console.log('   - Duration: 50 seconds total');
  console.log('   - Ramp-up: 10s → Steady: 30s → Ramp-down: 10s');
  console.log('📊 Operations Mix:');
  console.log('   - 30% CREATE (Registration)');
  console.log('   - 40% READ (Get users)');
  console.log('   - 20% UPDATE (User info)');
  console.log('   - 10% DELETE (User)');
  console.log('====================================================================\n');

  // Health check
  const healthCheck = http.get(`${BASE_URL}/api/users/health`);
  if (healthCheck.status === 200) {
    console.log('✅ Server is ready for CRUD testing\n');
  } else {
    console.error(`⚠️ WARNING: Health check failed - Status ${healthCheck.status}`);
    console.error('⚠️ Make sure Spring MVC server is running on port 8080\n');
  }

  return { startTime: new Date().toISOString() };
}

export function teardown(data) {
  console.log('\n====================================================================');
  console.log('✅ K6 CRUD LOAD TEST COMPLETED');
  console.log('====================================================================');
  console.log(`🕐 Test started: ${data.startTime}`);
  console.log(`🕐 Test ended: ${new Date().toISOString()}`);
  console.log('====================================================================');
  console.log('📊 Review the metrics above to analyze performance');
  console.log('📈 Check success rates, response times, and throughput');
  console.log('====================================================================\n');
}
