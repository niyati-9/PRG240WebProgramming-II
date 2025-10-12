/**
 * K6 Load Test - User Registration API
 *
 * This test simulates 1000 virtual users making registration requests
 * continuously for 5 minutes.
 *
 * Test Configuration:
 * - Virtual Users (VUs): 1000 concurrent users
 * - Duration: 5 minutes (300 seconds)
 * - Target API: POST /api/auth/register
 * - Expected Load: ~1000 requests/second initially
 *
 * Metrics Collected:
 * - HTTP request duration
 * - Success/failure rates
 * - Throughput (requests per second)
 * - Response times (min, max, avg, percentiles)
 *
 * @author Khel App Load Testing
 * @version 1.0
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// ========== CUSTOM METRICS ==========
const successRate = new Rate('successful_registrations');
const registrationDuration = new Trend('registration_duration');
const failedRegistrations = new Counter('failed_registrations');
const serverErrors = new Counter('server_500_errors');

// ========== TEST CONFIGURATION ==========
export const options = {
  // Test stages
  stages: [
    { duration: '30s', target: 1000 },  // Ramp-up to 1000 users in 30 seconds
    { duration: '4m', target: 1000 },   // Stay at 1000 users for 4 minutes
    { duration: '30s', target: 0 },     // Ramp-down to 0 users in 30 seconds
  ],

  // Thresholds (SLA requirements)
  thresholds: {
    'http_req_duration': ['p(95)<5000', 'p(99)<10000'], // 95% of requests should be below 5s
    'http_req_failed': ['rate<0.20'],  // Less than 20% of requests should fail
    'successful_registrations': ['rate>0.70'], // At least 70% should succeed
    'http_reqs': ['rate>100'], // At least 100 requests per second
  },

  // Additional options
  noConnectionReuse: false,
  userAgent: 'K6-LoadTest-Registration/1.0',

  // Summary configuration
  summaryTrendStats: ['min', 'avg', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

// ========== BASE URL ==========
const BASE_URL = 'http://localhost:8080/SpringMvcHelloWorld';

// ========== HELPER FUNCTIONS ==========

/**
 * Generate unique username with timestamp and random number
 */
function generateUsername() {
  const timestamp = Date.now();
  const random = Math.floor(Math.random() * 90000) + 10000;
  const vuid = __VU; // K6 virtual user ID
  return `k6user_${timestamp}_${vuid}_${random}`;
}

/**
 * Generate random secure password
 */
function generatePassword() {
  const random = Math.floor(Math.random() * 9000) + 1000;
  return `K6Pass${random}!`;
}

/**
 * Generate random email
 */
function generateEmail(username) {
  return `${username}@khelapp.test`;
}

/**
 * Generate random phone number
 */
function generatePhone() {
  const random = Math.floor(Math.random() * 9000000) + 1000000;
  return `984${random}`;
}

// ========== MAIN TEST FUNCTION ==========
export default function () {
  // Generate unique credentials
  const username = generateUsername();
  const password = generatePassword();
  const email = generateEmail(username);
  const phone = generatePhone();

  // Prepare request payload
  const payload = JSON.stringify({
    username: username,
    password: password,
    email: email,
    fullName: `K6 Load User ${__VU}`,
    phoneNumber: phone,
    userType: 'player',
  });

  // Request headers
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    },
    tags: {
      name: 'UserRegistration',
    },
  };

  // Make POST request to registration endpoint
  const startTime = new Date();
  const response = http.post(`${BASE_URL}/api/auth/register`, payload, params);
  const duration = new Date() - startTime;

  // Record custom metrics
  registrationDuration.add(duration);

  // Validate response
  const checkResult = check(response, {
    'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'response has success field': (r) => r.json('success') !== undefined,
    'success is true': (r) => r.json('success') === true,
    'response has message': (r) => r.json('message') !== undefined,
    'response time < 10s': (r) => r.timings.duration < 10000,
  });

  // Record success/failure
  if (response.status === 200 || response.status === 201) {
    successRate.add(1);
    console.log(`✅ [VU ${__VU}] Registered: ${username} - Duration: ${duration}ms`);
  } else {
    successRate.add(0);
    failedRegistrations.add(1);

    if (response.status === 500) {
      serverErrors.add(1);
      console.error(`❌ [VU ${__VU}] HTTP 500 - Server Error for: ${username}`);
    } else {
      console.error(`❌ [VU ${__VU}] HTTP ${response.status} - Failed: ${username} - ${response.body}`);
    }
  }

  // Random think time between 0.5 to 2 seconds (simulates real user behavior)
  sleep(Math.random() * 1.5 + 0.5);
}

// ========== SETUP FUNCTION ==========
export function setup() {
  console.log('====================================================================');
  console.log('K6 LOAD TEST - USER REGISTRATION API');
  console.log('====================================================================');
  console.log(`Base URL: ${BASE_URL}`);
  console.log('Test Configuration:');
  console.log('  - Virtual Users: 1000 concurrent users');
  console.log('  - Duration: 5 minutes (300 seconds)');
  console.log('  - Ramp-up: 30 seconds');
  console.log('  - Steady state: 4 minutes');
  console.log('  - Ramp-down: 30 seconds');
  console.log('====================================================================');
  console.log('Starting load test...\n');

  // Verify server is accessible
  const healthCheck = http.get(`${BASE_URL}/api/users/health`);
  if (healthCheck.status !== 200) {
    console.error('⚠️  WARNING: Server may not be ready!');
    console.error(`   Health check returned status: ${healthCheck.status}`);
  } else {
    console.log('✅ Server is accessible and ready for testing\n');
  }

  return { startTime: new Date().toISOString() };
}

// ========== TEARDOWN FUNCTION ==========
export function teardown(data) {
  console.log('\n====================================================================');
  console.log('K6 LOAD TEST COMPLETED');
  console.log('====================================================================');
  console.log(`Test started at: ${data.startTime}`);
  console.log(`Test ended at: ${new Date().toISOString()}`);
  console.log('====================================================================');
  console.log('📊 Please review the summary statistics above');
  console.log('📈 For detailed analysis, check the generated HTML report (if enabled)');
  console.log('====================================================================\n');
}
