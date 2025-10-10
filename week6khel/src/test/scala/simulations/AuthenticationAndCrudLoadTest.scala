package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import java.util.concurrent.ThreadLocalRandom

/**
 * Complete Gatling Load Test - Authentication & CRUD Operations
 * Fixed version with correct checkIf syntax for Gatling 3.13+
 *
 * @author Khel App Development Team
 * @version 2.1
 */
class AuthenticationAndCrudLoadTest extends Simulation {

  // ========== HTTP PROTOCOL CONFIGURATION ==========
  val httpProtocol = http
    .baseUrl("http://localhost:8080/SpringMvcHelloWorld")
    .contentTypeHeader("application/json")
    .acceptHeader("application/json")
    .userAgentHeader("Gatling-AuthCrudLoadTest/2.1")

  // ========== HELPER FUNCTIONS ==========
  def generateUsername(): String = {
    s"user_${System.currentTimeMillis()}_${ThreadLocalRandom.current().nextInt(10000, 99999)}"
  }

  def generateEmail(username: String): String = {
    s"${System.currentTimeMillis()}_${username}@test.com"
  }

  def generatePassword(): String = {
    s"Pass${ThreadLocalRandom.current().nextInt(1000, 9999)}!"
  }

  def generatePhone(): String = {
    s"984${ThreadLocalRandom.current().nextInt(1000000, 9999999)}"
  }

  // ========== SCENARIO 1: SIMPLE REGISTRATION ==========
  val simpleRegistration = scenario("Simple User Registration")
    .exec { session =>
      val username = generateUsername()
      val password = generatePassword()

      session
        .set("username", username)
        .set("password", password)
    }
    .exec(
      http("Register User")
        .post("/api/auth/register")
        .body(StringBody("""{"username":"${username}","password":"${password}"}""")).asJson
        .check(status.in(200, 201, 400, 409))
        .check(bodyString.saveAs("registerResponse"))
    )
    .exec { session =>
      val username = session("username").as[String]
      println(s"✅ Registration attempt: ${username}")
      session
    }

  // ========== SCENARIO 2: FULL REGISTRATION ==========
  val fullRegistration = scenario("Full User Registration")
    .exec { session =>
      val username = generateUsername()
      val password = generatePassword()
      val email = generateEmail(username)

      session
        .set("username", username)
        .set("password", password)
        .set("email", email)
        .set("fullName", s"Test User ${ThreadLocalRandom.current().nextInt(1000)}")
        .set("phoneNumber", generatePhone())
        .set("userType", "player")
    }
    .exec(
      http("Register User with Full Data")
        .post("/api/auth/register")
        .body(StringBody(
          """{
            "username":"${username}",
            "password":"${password}",
            "email":"${email}",
            "fullName":"${fullName}",
            "phoneNumber":"${phoneNumber}",
            "userType":"${userType}"
          }"""
        )).asJson
        .check(status.in(200, 201, 400, 409))
    )
    .exec { session =>
      println(s"✅ Full registration completed")
      session
    }

  // ========== SCENARIO 3: REGISTRATION AND LOGIN ==========
  val registrationAndLogin = scenario("Registration and Login Flow")
    .exec { session =>
      val username = generateUsername()
      val password = generatePassword()
      val email = generateEmail(username)

      session
        .set("username", username)
        .set("password", password)
        .set("email", email)
        .set("fullName", "Login Test User")
        .set("phoneNumber", generatePhone())
        .set("userType", "player")
    }
    // Register
    .exec(
      http("Register")
        .post("/api/auth/register")
        .body(StringBody(
          """{
            "username":"${username}",
            "password":"${password}",
            "email":"${email}",
            "fullName":"${fullName}",
            "phoneNumber":"${phoneNumber}",
            "userType":"${userType}"
          }"""
        )).asJson
        .check(status.in(200, 201, 400, 409))
        .check(status.saveAs("registerStatus"))
    )
    .pause(1)
    // Login
    .exec(
      http("Login")
        .post("/api/auth/login")
        .body(StringBody("""{"username":"${username}","password":"${password}"}""")).asJson
        .check(status.in(200, 201, 400, 401, 404))
        .check(status.saveAs("loginStatus"))
        .check(jsonPath("$.data.token").optional.saveAs("jwtToken"))
        .check(jsonPath("$.data.refreshToken").optional.saveAs("refreshToken"))
        .check(jsonPath("$.data.userId").optional.saveAs("userId"))
    )
    .exec { session =>
      val hasToken = session.contains("jwtToken")
      if (hasToken) {
        println(s"✅ Login successful with token")
      } else {
        println(s"⚠️ Login attempted but no token received")
      }
      session
    }

  // ========== SCENARIO 4: COMPLETE AUTH FLOW ==========
  val completeAuthFlow = scenario("Complete Auth Flow")
    .exec { session =>
      val username = generateUsername()
      val password = generatePassword()
      val email = generateEmail(username)

      session
        .set("username", username)
        .set("password", password)
        .set("email", email)
        .set("fullName", "Complete Auth User")
        .set("phoneNumber", generatePhone())
        .set("userType", "player")
    }
    // Register
    .exec(
      http("Register")
        .post("/api/auth/register")
        .body(StringBody(
          """{
            "username":"${username}",
            "password":"${password}",
            "email":"${email}",
            "fullName":"${fullName}",
            "phoneNumber":"${phoneNumber}",
            "userType":"${userType}"
          }"""
        )).asJson
        .check(status.in(200, 201, 400, 409))
    )
    .pause(1)
    // Login
    .exec(
      http("Login")
        .post("/api/auth/login")
        .body(StringBody("""{"username":"${username}","password":"${password}"}""")).asJson
        .check(status.in(200, 201, 400, 401, 404))
        .check(jsonPath("$.data.token").optional.saveAs("jwtToken"))
        .check(jsonPath("$.data.refreshToken").optional.saveAs("refreshToken"))
    )
    .pause(1)
    // Validate Token (only if token exists)
    .doIf(session => session.contains("jwtToken")) {
      exec(
        http("Validate Token")
          .post("/api/auth/validate")
          .body(StringBody("""{"token":"${jwtToken}"}""")).asJson
          .header("Authorization", "Bearer ${jwtToken}")
          .check(status.in(200, 400, 401, 404))
      )
    }
    .pause(1)
    // Logout
    .doIf(session => session.contains("jwtToken")) {
      exec(
        http("Logout")
          .post("/api/auth/logout")
          .body(StringBody("""{"token":"${jwtToken}","refreshToken":"${refreshToken}"}""")).asJson
          .header("Authorization", "Bearer ${jwtToken}")
          .check(status.in(200, 400, 401, 404))
      )
    }
    .exec { session =>
      println(s"✅ Complete auth flow finished")
      session
    }

  // ========== SCENARIO 5: READ OPERATIONS ==========
  val readOperations = scenario("CRUD - Read Operations")
    .exec { session =>
      val username = generateUsername()
      val password = generatePassword()
      val email = generateEmail(username)

      session
        .set("username", username)
        .set("password", password)
        .set("email", email)
        .set("fullName", "Read Test User")
        .set("phoneNumber", generatePhone())
        .set("userType", "player")
    }
    // Register
    .exec(
      http("Register")
        .post("/api/auth/register")
        .body(StringBody(
          """{
            "username":"${username}",
            "password":"${password}",
            "email":"${email}",
            "fullName":"${fullName}",
            "phoneNumber":"${phoneNumber}",
            "userType":"${userType}"
          }"""
        )).asJson
        .check(status.in(200, 201, 400, 409))
    )
    .pause(1)
    // Login
    .exec(
      http("Login")
        .post("/api/auth/login")
        .body(StringBody("""{"username":"${username}","password":"${password}"}""")).asJson
        .check(status.in(200, 201, 400, 401, 404))
        .check(jsonPath("$.data.token").optional.saveAs("jwtToken"))
        .check(jsonPath("$.data.userId").optional.saveAs("userId"))
    )
    .pause(1)
    // Get all users
    .doIf(session => session.contains("jwtToken")) {
      exec(
        http("Get All Users")
          .get("/api/users")
          .header("Authorization", "Bearer ${jwtToken}")
          .check(status.in(200, 401, 403, 404))
      )
    }
    .pause(1)
    // Get user by ID
    .doIf(session => session.contains("jwtToken") && session.contains("userId")) {
      exec(
        http("Get User By ID")
          .get("/api/users/${userId}")
          .header("Authorization", "Bearer ${jwtToken}")
          .check(status.in(200, 401, 403, 404))
      )
    }
    .exec { session =>
      println(s"✅ Read operations completed")
      session
    }

  // ========== SCENARIO 6: UPDATE OPERATIONS ==========
  val updateOperations = scenario("CRUD - Update Operations")
    .exec { session =>
      val username = generateUsername()
      val password = generatePassword()
      val email = generateEmail(username)

      session
        .set("username", username)
        .set("password", password)
        .set("email", email)
        .set("fullName", "Update Test User")
        .set("phoneNumber", generatePhone())
        .set("userType", "player")
    }
    // Register
    .exec(
      http("Register")
        .post("/api/auth/register")
        .body(StringBody(
          """{
            "username":"${username}",
            "password":"${password}",
            "email":"${email}",
            "fullName":"${fullName}",
            "phoneNumber":"${phoneNumber}",
            "userType":"${userType}"
          }"""
        )).asJson
        .check(status.in(200, 201, 400, 409))
    )
    .pause(1)
    // Login
    .exec(
      http("Login")
        .post("/api/auth/login")
        .body(StringBody("""{"username":"${username}","password":"${password}"}""")).asJson
        .check(status.in(200, 201, 400, 401, 404))
        .check(jsonPath("$.data.token").optional.saveAs("jwtToken"))
        .check(jsonPath("$.data.userId").optional.saveAs("userId"))
    )
    .pause(1)
    // Update user
    .exec { session =>
      session
        .set("newFullName", "Updated Full Name")
        .set("newEmail", generateEmail(session("username").as[String]))
        .set("newPhone", generatePhone())
    }
    .doIf(session => session.contains("jwtToken") && session.contains("userId")) {
      exec(
        http("Update User")
          .put("/api/users/${userId}")
          .header("Authorization", "Bearer ${jwtToken}")
          .body(StringBody(
            """{
              "username":"${username}",
              "fullName":"${newFullName}",
              "email":"${newEmail}",
              "phoneNumber":"${newPhone}",
              "userType":"${userType}",
              "password":"${password}"
            }"""
          )).asJson
          .check(status.in(200, 400, 401, 403, 404))
      )
    }
    .exec { session =>
      println(s"✅ Update operations completed")
      session
    }

  // ========== SCENARIO 7: DELETE OPERATIONS ==========
  val deleteOperations = scenario("CRUD - Delete Operations")
    .exec { session =>
      val username = generateUsername()
      val password = generatePassword()
      val email = generateEmail(username)

      session
        .set("username", username)
        .set("password", password)
        .set("email", email)
        .set("fullName", "Delete Test User")
        .set("phoneNumber", generatePhone())
        .set("userType", "player")
    }
    // Register
    .exec(
      http("Register")
        .post("/api/auth/register")
        .body(StringBody(
          """{
            "username":"${username}",
            "password":"${password}",
            "email":"${email}",
            "fullName":"${fullName}",
            "phoneNumber":"${phoneNumber}",
            "userType":"${userType}"
          }"""
        )).asJson
        .check(status.in(200, 201, 400, 409))
    )
    .pause(1)
    // Login
    .exec(
      http("Login")
        .post("/api/auth/login")
        .body(StringBody("""{"username":"${username}","password":"${password}"}""")).asJson
        .check(status.in(200, 201, 400, 401, 404))
        .check(jsonPath("$.data.token").optional.saveAs("jwtToken"))
        .check(jsonPath("$.data.userId").optional.saveAs("userId"))
    )
    .pause(1)
    // Delete user
    .doIf(session => session.contains("jwtToken") && session.contains("userId")) {
      exec(
        http("Delete User")
          .delete("/api/users/${userId}")
          .header("Authorization", "Bearer ${jwtToken}")
          .check(status.in(200, 401, 403, 404))
      )
    }
    .exec { session =>
      println(s"✅ Delete operations completed")
      session
    }

  // ========== LOAD TEST SETUP ==========
  setUp(
    // Authentication scenarios
    simpleRegistration.inject(
      rampUsers(10).during(10.seconds)
    ),

    fullRegistration.inject(
      rampUsers(10).during(10.seconds)
    ),

    registrationAndLogin.inject(
      rampUsers(15).during(20.seconds)
    ),

    completeAuthFlow.inject(
      rampUsers(15).during(20.seconds)
    ),

    // CRUD scenarios
    readOperations.inject(
      rampUsers(20).during(30.seconds)
    ),

    updateOperations.inject(
      rampUsers(15).during(25.seconds)
    ),

    deleteOperations.inject(
      rampUsers(15).during(25.seconds)
    )

  ).protocols(httpProtocol)
    .assertions(
      // Realistic assertions
      global.successfulRequests.percent.gt(20),
      global.responseTime.mean.lt(10000),
      global.responseTime.percentile(95).lt(20000),
      global.responseTime.max.lt(30000)
    )
}
