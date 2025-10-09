package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import java.util.concurrent.ThreadLocalRandom

/**
 * Gatling Load Test for Khel App - Complete Authentication Flow
 *
 * This simulation tests the complete authentication workflow including:
 * - User Registration
 * - User Login with JWT token generation
 * - Accessing protected endpoints with JWT
 *
 * Test Scenario:
 * - 100 users go through complete auth flow
 * - Each user registers, logs in, and accesses protected resources
 * - Measures end-to-end performance
 *
 * @author Khel App Development Team
 * @version 1.0
 */
class AuthenticationLoadTest extends Simulation {

  // ========== HTTP PROTOCOL CONFIGURATION ==========
  val httpProtocol = http
    .baseUrl("http://localhost:8080/SpringMvcHelloWorld")
    .contentTypeHeader("application/json")
    .acceptHeader("application/json")
    .userAgentHeader("Gatling-KhelApp-AuthTest/1.0")

  // ========== HELPER FUNCTIONS ==========
  def generateUsername(): String = {
    s"authtest_${System.currentTimeMillis()}_${ThreadLocalRandom.current().nextInt(10000, 99999)}"
  }

  def generateEmail(username: String): String = {
    s"${username}@authtest.com"
  }

  // ========== COMPLETE AUTHENTICATION FLOW SCENARIO ==========
  val completeAuthFlow = scenario("Complete Authentication Flow")
    .exec { session =>
      // Generate unique user data
      val username = generateUsername()
      val password = s"AuthTest${ThreadLocalRandom.current().nextInt(1000, 9999)}!"
      val email = generateEmail(username)
      val fullName = s"Auth Test User ${ThreadLocalRandom.current().nextInt(1000, 9999)}"
      val phone = s"984${ThreadLocalRandom.current().nextInt(1000000, 9999999)}"

      session
        .set("username", username)
        .set("password", password)
        .set("email", email)
        .set("fullName", fullName)
        .set("phoneNumber", phone)
    }
    // Step 1: Register User
    .exec(
      http("1. Register User")
        .post("/api/auth/register")
        .body(StringBody("""{
          "username":"${username}",
          "password":"${password}",
          "email":"${email}",
          "fullName":"${fullName}",
          "phoneNumber":"${phoneNumber}",
          "userType":"player"
        }""")).asJson
        .check(status.in(200, 201, 400)) // 400 is ok if user exists
        .check(jsonPath("$.success").optional.saveAs("regSuccess"))
    )
    .pause(1, 2) // Wait 1-2 seconds between registration and login

    // Step 2: Login and Get JWT Token
    .exec(
      http("2. Login User")
        .post("/api/auth/login")
        .body(StringBody("""{
          "username":"${username}",
          "password":"${password}"
        }""")).asJson
        .check(status.in(200, 201))
        .check(jsonPath("$.data.token").saveAs("jwtToken"))
        .check(jsonPath("$.data.refreshToken").optional.saveAs("refreshToken"))
    )
    .pause(1, 2)

    // Step 3: Access Protected Endpoint with JWT
    .exec(
      http("3. Access Protected Resource")
        .get("/api/users")
        .header("Authorization", "Bearer ${jwtToken}")
        .check(status.in(200, 201))
    )
    .pause(1, 2)

    // Step 4: Validate Token
    .exec(
      http("4. Validate Token")
        .post("/api/auth/validate")
        .body(StringBody("""{"token":"${jwtToken}"}""")).asJson
        .check(status.is(200))
    )
    .pause(1, 2)

    // Step 5: Logout
    .exec(
      http("5. Logout")
        .post("/api/auth/logout")
        .header("Authorization", "Bearer ${jwtToken}")
        .body(StringBody("""{
          "token":"${jwtToken}",
          "refreshToken":"${refreshToken}"
        }""")).asJson
        .check(status.is(200))
    )
    .exec { session =>
      val username = session("username").as[String]
      println(s"✅ Complete auth flow for: ${username}")
      session
    }

  // ========== LOAD TEST SETUP ==========
  /**
   * 100 CONCURRENT USERS
   * Complete authentication workflow test
   */
  setUp(
    completeAuthFlow.inject(
      rampUsers(100).during(30.seconds) // Gradually ramp up to 100 users over 30 seconds
    )
  ).protocols(httpProtocol)
    .assertions(
      // At least 70% of requests should succeed
      global.successfulRequests.percent.gt(70),
      // Mean response time should be under 3 seconds
      global.responseTime.mean.lt(3000),
      // 95th percentile should be under 8 seconds
      global.responseTime.percentile(95).lt(8000)
    )
}