package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import java.util.concurrent.ThreadLocalRandom

class KhelAppAuthAndCrudLoadTest extends Simulation {

  // ========== HTTP CONFIGURATION ==========
  val httpProtocol = http
    .baseUrl("http://localhost:8080/SpringMvcHelloWorld")
    .contentTypeHeader("application/json")
    .acceptHeader("application/json")

  // ========== UNIQUE DATA GENERATION ==========
  def generateUniqueUsername(): String = {
    val nanoTime = System.nanoTime()
    val random = ThreadLocalRandom.current().nextInt(100000, 999999)
    s"user_${nanoTime}_${random}"
  }

  def generateEmail(username: String): String = s"${username}@khelapp.test"
  def generatePassword(): String = s"Pass${ThreadLocalRandom.current().nextInt(1000, 9999)}!"
  def generatePhone(): String = s"984${ThreadLocalRandom.current().nextInt(1000000, 9999999)}"

  // ========== REUSABLE CHAINS ==========

  // Initialize user data
  def initializeUserData(fullNamePrefix: String) = exec { session =>
    val username = generateUniqueUsername()
    val password = generatePassword()
    session
      .set("username", username)
      .set("password", password)
      .set("email", generateEmail(username))
      .set("fullName", s"${fullNamePrefix} ${ThreadLocalRandom.current().nextInt(1000)}")
      .set("phoneNumber", generatePhone())
      .set("userType", "player")
  }

  // Register chain
  val registerChain = exec(
    http("Register")
      .post("/api/auth/register")
      .body(StringBody("""{"username":"#{username}","password":"#{password}","email":"#{email}","fullName":"#{fullName}","phoneNumber":"#{phoneNumber}","userType":"#{userType}"}""")).asJson
      .check(status.in(200, 201))
      .check(jsonPath("$.success").is("true"))
      .check(jsonPath("$.data.userId").optional.saveAs("userId"))
      .check(jsonPath("$.data.token").optional.saveAs("jwtToken"))
  )

  // Login chain
  val loginChain = exec(
    http("Login")
      .post("/api/auth/login")
      .body(StringBody("""{"username":"#{username}","password":"#{password}"}""")).asJson
      .check(status.is(200))
      .check(jsonPath("$.success").is("true"))
      .check(jsonPath("$.data.token").saveAs("jwtToken"))
      .check(jsonPath("$.data.userId").saveAs("userId"))
      .check(jsonPath("$.data.refreshToken").optional.saveAs("refreshToken"))
  )

  // Combined register and login chain
  def registerAndLoginChain =
    registerChain
      .pause(1)
      .exec(loginChain)

  // ========== SCENARIO 1: REGISTER USER ==========
  val registerUser = scenario("Register User")
    .exec(initializeUserData("Test User"))
    .exec(
      http("POST /api/auth/register")
        .post("/api/auth/register")
        .body(StringBody("""{"username":"#{username}","password":"#{password}","email":"#{email}","fullName":"#{fullName}","phoneNumber":"#{phoneNumber}","userType":"#{userType}"}""")).asJson
        .check(status.in(200, 201))
        .check(jsonPath("$.success").is("true"))
        .check(jsonPath("$.data.userId").saveAs("userId"))
        .check(jsonPath("$.data.token").optional.saveAs("jwtToken"))
    )

  // ========== SCENARIO 2: LOGIN USER ==========
  val loginUser = scenario("Login User")
    .exec(initializeUserData("Login Test User"))
    .exec(
      http("Register before login")
        .post("/api/auth/register")
        .body(StringBody("""{"username":"#{username}","password":"#{password}","email":"#{email}","fullName":"#{fullName}","phoneNumber":"#{phoneNumber}","userType":"#{userType}"}""")).asJson
        .check(status.in(200, 201, 409))
    )
    .pause(1)
    .exec(
      http("POST /api/auth/login")
        .post("/api/auth/login")
        .body(StringBody("""{"username":"#{username}","password":"#{password}"}""")).asJson
        .check(status.is(200))
        .check(jsonPath("$.success").is("true"))
        .check(jsonPath("$.data.token").saveAs("jwtToken"))
        .check(jsonPath("$.data.userId").saveAs("userId"))
        .check(jsonPath("$.data.refreshToken").optional.saveAs("refreshToken"))
    )

  // ========== SCENARIO 3: READ ALL USERS ==========
  val readAllUsers = scenario("Read All Users")
    .exec(initializeUserData("Read Test User"))
    .exec(registerAndLoginChain)
    .pause(1)
    .exec(
      http("GET /api/users")
        .get("/api/users")
        .header("Authorization", "Bearer #{jwtToken}")
        .check(status.is(200))
        .check(jsonPath("$.success").is("true"))
    )

  // ========== SCENARIO 4: READ USER BY ID ==========
  val readUserById = scenario("Read User By ID")
    .exec(initializeUserData("Read Single User"))
    .exec(registerAndLoginChain)
    .pause(1)
    .exec(
      http("GET /api/users/#{userId}")
        .get("/api/users/#{userId}")
        .header("Authorization", "Bearer #{jwtToken}")
        .check(status.is(200))
        .check(jsonPath("$.success").is("true"))
    )

  // ========== SCENARIO 5: UPDATE USER ==========
  val updateUser = scenario("Update User")
    .exec(initializeUserData("Update Test User"))
    .exec(registerAndLoginChain)
    .pause(1)
    .exec { session =>
      session
        .set("newFullName", s"Updated User Name ${ThreadLocalRandom.current().nextInt(1000)}")
        .set("newEmail", generateEmail(session("username").as[String]))
        .set("newPhone", generatePhone())
    }
    .exec(
      http("PUT /api/users/#{userId}")
        .put("/api/users/#{userId}")
        .header("Authorization", "Bearer #{jwtToken}")
        .body(StringBody("""{"username":"#{username}","fullName":"#{newFullName}","email":"#{newEmail}","phoneNumber":"#{newPhone}","userType":"#{userType}"}""")).asJson
        .check(status.is(200))
        .check(jsonPath("$.success").is("true"))
    )

  // ========== SCENARIO 6: DELETE USER ==========
  val deleteUser = scenario("Delete User")
    .exec(initializeUserData("Delete Test User"))
    .exec(registerAndLoginChain)
    .pause(1)
    .exec(
      http("DELETE /api/users/#{userId}")
        .delete("/api/users/#{userId}")
        .header("Authorization", "Bearer #{jwtToken}")
        .check(status.is(200))
        .check(jsonPath("$.success").is("true"))
    )

  // ========== LOAD TEST SETUP ==========
  setUp(
    registerUser.inject(
      rampUsers(10).during(15.seconds)
    ).protocols(httpProtocol),

    loginUser.inject(
      rampUsers(10).during(15.seconds)
    ).protocols(httpProtocol),

    readAllUsers.inject(
      rampUsers(8).during(15.seconds)
    ).protocols(httpProtocol),

    readUserById.inject(
      rampUsers(8).during(15.seconds)
    ).protocols(httpProtocol),

    updateUser.inject(
      rampUsers(5).during(15.seconds)
    ).protocols(httpProtocol),

    deleteUser.inject(
      rampUsers(5).during(15.seconds)
    ).protocols(httpProtocol)

  ).assertions(
    global.successfulRequests.percent.gte(95),
    global.responseTime.mean.lt(2000),
    global.responseTime.percentile(95).lt(5000),
    forAll.failedRequests.count.lte(10)
  )
}
