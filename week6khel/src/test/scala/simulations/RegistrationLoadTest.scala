package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import java.util.concurrent.ThreadLocalRandom

/**
 * Gatling Load Test for Khel App - User Registration
 *
 * This simulation focuses on testing the user registration endpoint
 * with concurrent users to measure performance and stability.
 *
 * Test Scenario:
 * - 200 users attempt to register simultaneously
 * - Each user gets a unique username with timestamp
 * - Measures response times and success rates
 *
 * @author Khel App Development Team
 * @version 2.0 - Fixed with complete registration data
 */
class RegistrationLoadTest extends Simulation {

  // ========== HTTP PROTOCOL CONFIGURATION ==========
  val httpProtocol = http
    .baseUrl("http://localhost:8080/SpringMvcHelloWorld")
    .contentTypeHeader("application/json")
    .acceptHeader("application/json")
    .userAgentHeader("Gatling-KhelApp-LoadTest/1.0")

  // ========== HELPER FUNCTIONS ==========
  def generateUsername(): String = {
    s"loadtest_${System.currentTimeMillis()}_${ThreadLocalRandom.current().nextInt(10000, 99999)}"
  }

  def generateEmail(username: String): String = {
    s"${username}@loadtest.com"
  }

  // ========== USER REGISTRATION SCENARIO ==========
  val userRegistrationScenario = scenario("200 Concurrent User Registrations")
    .exec { session =>
      // Generate unique credentials for each user
      val username = generateUsername()
      val password = s"LoadTest${ThreadLocalRandom.current().nextInt(1000, 9999)}!"
      val email = generateEmail(username)
      val fullName = s"Load Test User ${ThreadLocalRandom.current().nextInt(1000, 9999)}"
      val phone = s"984${ThreadLocalRandom.current().nextInt(1000000, 9999999)}"

      session
        .set("username", username)
        .set("password", password)
        .set("email", email)
        .set("fullName", fullName)
        .set("phoneNumber", phone)
    }
    .exec(
      http("Register User")
        .post("/api/auth/register")
        .body(StringBody("""{
          "username":"${username}",
          "password":"${password}",
          "email":"${email}",
          "fullName":"${fullName}",
          "phoneNumber":"${phoneNumber}",
          "userType":"player"
        }""")).asJson
        .check(status.in(200, 201))
        .check(jsonPath("$.success").is("true"))
        .check(jsonPath("$.message").exists)
    )
    .exec { session =>
      val username = session("username").as[String]
      println(s"✅ Registered: ${username}")
      session
    }

  // ========== LOAD TEST SETUP ==========
  /**
   * 200 CONCURRENT USERS
   * All users attempt to register at the same time
   */
  setUp(
    userRegistrationScenario.inject(
      atOnceUsers(200)
    )
  ).protocols(httpProtocol)
    .assertions(
      // At least 60% of requests should succeed
      global.successfulRequests.percent.gt(60),
      // Mean response time should be under 5 seconds
      global.responseTime.mean.lt(5000),
      // 95th percentile should be under 10 seconds
      global.responseTime.percentile(95).lt(10000)
    )
}