package steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber Step Definitions for Authentication Features
 * ONLY contains authentication-specific steps
 *
 * @author Khel App Development Team
 * @version 8.0 - Fixed Email Uniqueness Issues
 */
public class AuthenticationSteps {

    private static final String BASE_URL = "http://localhost:8080/SpringMvcHelloWorld";
    private Response response;
    private String jwtToken;
    private String refreshToken;
    private String currentUsername;
    private String currentPassword;

    private static final Map<String, String> usernameMap = new HashMap<>();
    private static final Map<String, UserInfo> userInfoMap = new HashMap<>();
    private static long usernameCounter = System.currentTimeMillis();

    static class UserInfo {
        String uniqueUsername;
        String password;
        String email;
        Long userId;
    }

    private String makeUsernameUnique(String username) {
        if (usernameMap.containsKey(username)) {
            return usernameMap.get(username);
        }

        String uniqueUsername = username + "_" + (usernameCounter++);
        usernameMap.put(username, uniqueUsername);

        System.out.println("🔄 Mapped '" + username + "' to unique '" + uniqueUsername + "'");
        return uniqueUsername;
    }

    /**
     * Generate unique email by prepending timestamp
     */
    private String makeEmailUnique(String email) {
        long timestamp = System.currentTimeMillis();

        // If email contains @, insert timestamp before @
        if (email.contains("@")) {
            int atIndex = email.indexOf("@");
            return timestamp + "_" + email.substring(0, atIndex) + "@" + email.substring(atIndex + 1);
        }

        // Otherwise just prepend timestamp
        return timestamp + "_" + email;
    }

    @Given("the authentication API is available at {string}")
    public void theAuthenticationAPIIsAvailableAt(String baseUrl) {
        RestAssured.baseURI = baseUrl;
        System.out.println("🔗 API Base URL set to: " + baseUrl);
    }

    @Given("a user exists with username {string}, password {string}, email {string}, full name {string}, phone {string}, and user type {string}")
    public void aUserExistsWithCompleteData(String username, String password, String email, String fullName, String phone, String userType) {
        String uniqueUsername = makeUsernameUnique(username);
        String uniqueEmail = makeEmailUnique(email);  // MAKE EMAIL UNIQUE

        Map<String, String> userData = new HashMap<>();
        userData.put("username", uniqueUsername);
        userData.put("password", password);
        userData.put("email", uniqueEmail);
        userData.put("fullName", fullName);
        userData.put("phoneNumber", phone);
        userData.put("userType", userType);

        try {
            Response regResponse = RestAssured
                    .given()
                    .contentType(ContentType.JSON)
                    .body(userData)
                    .when()
                    .post("/api/auth/register");

            System.out.println("Pre-registering user: " + uniqueUsername + " (Status: " + regResponse.getStatusCode() + ")");

            if (regResponse.getStatusCode() == 200 || regResponse.getStatusCode() == 201) {
                System.out.println("✅ User registered: " + uniqueUsername);

                try {
                    Long userId = regResponse.jsonPath().getLong("data.user.userId");
                    UserInfo info = new UserInfo();
                    info.uniqueUsername = uniqueUsername;
                    info.password = password;
                    info.email = uniqueEmail;
                    info.userId = userId;
                    userInfoMap.put(username, info);

                    // Share with EnhancedCrudSteps
                    syncToEnhancedSteps();
                } catch (Exception e) {
                    System.out.println("⚠️ Could not extract user ID: " + e.getMessage());
                }
            } else {
                System.out.println("⚠️ Registration status: " + regResponse.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("⚠️ Pre-registration error: " + e.getMessage());
        }

        currentUsername = uniqueUsername;
        currentPassword = password;
    }

    @Given("I have a valid JWT token for user {string}")
    public void iHaveAValidJWTTokenForUser(String username) {
        UserInfo info = userInfoMap.get(username);
        String actualUsername = info != null ? info.uniqueUsername : usernameMap.getOrDefault(username, username);
        String actualPassword = info != null ? info.password : currentPassword;

        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", actualUsername);
        loginData.put("password", actualPassword);

        System.out.println("Getting token for: " + actualUsername);

        response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginData)
                .when()
                .post("/api/auth/login");

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            try {
                jwtToken = response.jsonPath().getString("data.token");
                refreshToken = response.jsonPath().getString("data.refreshToken");
                Long userId = response.jsonPath().getLong("data.userId");

                currentUsername = actualUsername;

                // Update user info with userId
                if (info == null) {
                    info = new UserInfo();
                    info.uniqueUsername = actualUsername;
                    info.password = actualPassword;
                    userInfoMap.put(username, info);
                }
                info.userId = userId;

                System.out.println("🔑 JWT Token obtained for user ID: " + userId);

                // Share with EnhancedCrudSteps
                syncToEnhancedSteps();
            } catch (Exception e) {
                System.out.println("⚠️ Could not extract tokens: " + e.getMessage());
            }
        }
    }

    @When("I register a new user with username {string}, password {string}, email {string}, full name {string}, phone {string}, and user type {string}")
    public void iRegisterNewUserWithCompleteData(String username, String password, String email, String fullName, String phone, String userType) {
        String uniqueUsername = makeUsernameUnique(username);
        String uniqueEmail = makeEmailUnique(email);  // MAKE EMAIL UNIQUE

        Map<String, String> userData = new HashMap<>();
        userData.put("username", uniqueUsername);
        userData.put("password", password);
        userData.put("email", uniqueEmail);
        userData.put("fullName", fullName);
        userData.put("phoneNumber", phone);
        userData.put("userType", userType);

        System.out.println("📝 Registration attempt: " + uniqueUsername + " with email: " + uniqueEmail);

        response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(userData)
                .when()
                .post("/api/auth/register");

        currentUsername = uniqueUsername;
        currentPassword = password;

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            try {
                Long userId = response.jsonPath().getLong("data.user.userId");
                UserInfo info = new UserInfo();
                info.uniqueUsername = uniqueUsername;
                info.password = password;
                info.email = uniqueEmail;
                info.userId = userId;
                userInfoMap.put(username, info);

                syncToEnhancedSteps();
            } catch (Exception e) {
                System.out.println("⚠️ Could not extract user ID: " + e.getMessage());
            }
        }

        System.out.println("Registration Status: " + response.getStatusCode());
    }

    @When("I attempt to register with username {string}, password {string}, email {string}, full name {string}, phone {string}, and user type {string}")
    public void iAttemptToRegisterWithCompleteData(String username, String password, String email, String fullName, String phone, String userType) {
        iRegisterNewUserWithCompleteData(username, password, email, fullName, phone, userType);
    }

    @When("I attempt to register with username {string}, password {string}, but missing email")
    public void iAttemptToRegisterMissingEmail(String username, String password) {
        Map<String, String> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("password", password);

        System.out.println("📝 Registration with missing fields (should fail with 400 or 404)");

        response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(userData)
                .when()
                .post("/api/auth/register");

        System.out.println("Status: " + response.getStatusCode() + " (expecting 400 or 404)");
    }

    @When("I attempt to register with username {string}, email {string}, but missing password")
    public void iAttemptToRegisterMissingPassword(String username, String email) {
        Map<String, String> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);

        System.out.println("📝 Registration with missing password (should fail with 400 or 404)");

        response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(userData)
                .when()
                .post("/api/auth/register");

        System.out.println("Status: " + response.getStatusCode() + " (expecting 400 or 404)");
    }

    @When("I login with username {string} and password {string}")
    public void iLoginWithUsernameAndPassword(String username, String password) {
        UserInfo info = userInfoMap.get(username);
        String actualUsername = info != null ? info.uniqueUsername : usernameMap.getOrDefault(username, username);

        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", actualUsername);
        loginData.put("password", password);

        System.out.println("🔐 Login: " + actualUsername);

        response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginData)
                .when()
                .post("/api/auth/login");

        System.out.println("Login Status: " + response.getStatusCode());

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            try {
                jwtToken = response.jsonPath().getString("data.token");
                refreshToken = response.jsonPath().getString("data.refreshToken");
                Long userId = response.jsonPath().getLong("data.userId");

                currentUsername = actualUsername;

                if (info == null) {
                    info = new UserInfo();
                    info.uniqueUsername = actualUsername;
                    info.password = password;
                    userInfoMap.put(username, info);
                }
                info.userId = userId;

                System.out.println("✅ Tokens extracted, User ID: " + userId);

                syncToEnhancedSteps();
            } catch (Exception e) {
                System.out.println("⚠️ Could not extract tokens: " + e.getMessage());
            }
        }
    }

    @When("I attempt to login with username {string} and password {string}")
    public void iAttemptToLoginWithUsernameAndPassword(String username, String password) {
        iLoginWithUsernameAndPassword(username, password);
    }

    @When("I attempt to access protected endpoint {string} without a token")
    public void iAttemptToAccessProtectedEndpointWithoutToken(String endpoint) {
        System.out.println("🚫 Accessing without token: " + endpoint);

        response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get(endpoint);

        System.out.println("Status: " + response.getStatusCode());
    }

    @When("I access protected endpoint {string} with valid token")
    public void iAccessProtectedEndpointWithValidToken(String endpoint) {
        System.out.println("✅ Accessing with token: " + endpoint);

        response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(endpoint);

        System.out.println("Status: " + response.getStatusCode());
    }

    @When("I use the JWT token to access protected endpoint {string}")
    public void iUseJWTTokenToAccessProtectedEndpoint(String endpoint) {
        iAccessProtectedEndpointWithValidToken(endpoint);
    }

    @When("I attempt to access protected endpoint {string} with the invalidated token")
    public void iAttemptToAccessProtectedEndpointWithInvalidatedToken(String endpoint) {
        iAccessProtectedEndpointWithValidToken(endpoint);
    }

    @When("I attempt to access protected endpoint {string} with invalid token {string}")
    public void iAttemptToAccessProtectedEndpointWithInvalidToken(String endpoint, String invalidToken) {
        System.out.println("🚫 Accessing with invalid token: " + endpoint);

        response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + invalidToken)
                .when()
                .get(endpoint);
    }

    @When("I validate the JWT token")
    public void iValidateTheJWTToken() {
        if (jwtToken == null) {
            System.out.println("⚠️ No token to validate");
            response = RestAssured.given().contentType(ContentType.JSON).body("{}").post("/api/auth/validate");
            return;
        }

        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("token", jwtToken);

        response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(tokenData)
                .when()
                .post("/api/auth/validate");

        System.out.println("Token validation Status: " + response.getStatusCode());
    }

    @When("I request a token refresh")
    public void iRequestATokenRefresh() {
        if (refreshToken == null) {
            System.out.println("⚠️ No refresh token");
            response = RestAssured.given().contentType(ContentType.JSON).body("{}").post("/api/auth/refresh");
            return;
        }

        Map<String, String> refreshData = new HashMap<>();
        refreshData.put("refreshToken", refreshToken);

        response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(refreshData)
                .when()
                .post("/api/auth/refresh");

        System.out.println("Token refresh Status: " + response.getStatusCode());
    }

    @When("I logout using the JWT token")
    public void iLogoutUsingTheJWTToken() {
        Map<String, String> logoutData = new HashMap<>();
        if (jwtToken != null) logoutData.put("token", jwtToken);
        if (refreshToken != null) logoutData.put("refreshToken", refreshToken);

        response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .body(logoutData)
                .when()
                .post("/api/auth/logout");

        System.out.println("Logout Status: " + response.getStatusCode());
    }

    @When("I check the API health status at {string}")
    public void iCheckAPIHealthStatusAt(String endpoint) {
        response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get(endpoint);

        System.out.println("Health check Status: " + response.getStatusCode());
    }

    // ==================== THEN STEPS ====================

    @Then("the registration response status code should be {int}")
    public void theRegistrationResponseStatusCodeShouldBe(int expectedStatus) {
        int actualStatus = response.getStatusCode();
        assertEquals(expectedStatus, actualStatus,
                "Expected " + expectedStatus + " but got " + actualStatus + ". Response: " + response.asString());
        System.out.println("✅ Status verified: " + expectedStatus);
    }

    @Then("the registration response status code should be {int} or {int}")
    public void theRegistrationResponseStatusCodeShouldBeOr(int status1, int status2) {
        int actualStatus = response.getStatusCode();
        assertTrue(actualStatus == status1 || actualStatus == status2,
                "Expected " + status1 + " or " + status2 + " but got " + actualStatus + ". Response: " + response.asString());
        System.out.println("✅ Status verified: " + actualStatus + " (expected " + status1 + " or " + status2 + ")");
    }

    @Then("the registration response should indicate success")
    public void theRegistrationResponseShouldIndicateSuccess() {
        String body = response.getBody().asString();
        int status = response.getStatusCode();

        boolean isSuccess = body.contains("\"success\":true") ||
                body.contains("\"success\": true") ||
                (status >= 200 && status < 300);

        assertTrue(isSuccess, "Should indicate success. Status: " + status + ", Body: " + body);
        System.out.println("✅ Success indicated");
    }

    @Then("the registration response should contain message {string}")
    public void theRegistrationResponseShouldContainMessage(String message) {
        String body = response.getBody().asString().toLowerCase();
        assertTrue(body.contains(message.toLowerCase()),
                "Response should contain '" + message + "'. Body: " + body);
        System.out.println("✅ Message verified");
    }

    @Then("the registration should be successful")
    public void theRegistrationShouldBeSuccessful() {
        int status = response.getStatusCode();
        assertTrue(status == 200 || status == 201,
                "Registration should succeed (200/201), got: " + status + ". Response: " + response.asString());
        System.out.println("✅ Registration successful");
    }

    @Then("the login response status code should be {int} or {int}")
    public void theLoginResponseStatusCodeShouldBeOr(int status1, int status2) {
        theRegistrationResponseStatusCodeShouldBeOr(status1, status2);
    }

    @Then("the login response status code should be {int}")
    public void theLoginResponseStatusCodeShouldBe(int expectedStatus) {
        theRegistrationResponseStatusCodeShouldBe(expectedStatus);
    }

    @Then("the login response should contain a JWT token")
    public void theLoginResponseShouldContainJWTToken() {
        String body = response.getBody().asString();
        assertTrue(body.contains("token") || body.contains("jwt"),
                "Response should contain JWT token. Body: " + body);
        System.out.println("✅ JWT token present");
    }

    @Then("the login response should indicate success")
    public void theLoginResponseShouldIndicateSuccess() {
        theRegistrationResponseShouldIndicateSuccess();
    }

    @Then("the login response should contain message {string}")
    public void theLoginResponseShouldContainMessage(String message) {
        theRegistrationResponseShouldContainMessage(message);
    }

    @Then("the login should be successful")
    public void theLoginShouldBeSuccessful() {
        theRegistrationShouldBeSuccessful();
    }

    @Then("I should receive a JWT token")
    public void iShouldReceiveJWTToken() {
        theLoginResponseShouldContainJWTToken();
        assertNotNull(jwtToken, "JWT token should be stored");
        assertFalse(jwtToken.isEmpty(), "JWT token should not be empty");
        System.out.println("✅ JWT token received");
    }

    @Then("I should be able to access the protected resource")
    public void iShouldBeAbleToAccessProtectedResource() {
        int status = response.getStatusCode();
        assertEquals(200, status, "Should access protected resource. Response: " + response.asString());
        System.out.println("✅ Protected resource accessed");
    }

    @Then("the protected endpoint response status code should be {int}")
    public void theProtectedEndpointResponseStatusCodeShouldBe(int expectedStatus) {
        theLoginResponseStatusCodeShouldBe(expectedStatus);
    }

    @Then("the protected endpoint response should indicate success")
    public void theProtectedEndpointResponseShouldIndicateSuccess() {
        theRegistrationResponseShouldIndicateSuccess();
    }

    @Then("the token validation response status code should be {int}")
    public void theTokenValidationResponseStatusCodeShouldBe(int expectedStatus) {
        theLoginResponseStatusCodeShouldBe(expectedStatus);
    }

    @Then("the token validation response should indicate success")
    public void theTokenValidationResponseShouldIndicateSuccess() {
        theRegistrationResponseShouldIndicateSuccess();
    }

    @Then("the token refresh response status code should be {int}")
    public void theTokenRefreshResponseStatusCodeShouldBe(int expectedStatus) {
        theLoginResponseStatusCodeShouldBe(expectedStatus);
    }

    @Then("the token refresh response should indicate success")
    public void theTokenRefreshResponseShouldIndicateSuccess() {
        theRegistrationResponseShouldIndicateSuccess();
    }

    @Then("the logout response status code should be {int}")
    public void theLogoutResponseStatusCodeShouldBe(int expectedStatus) {
        theLoginResponseStatusCodeShouldBe(expectedStatus);
    }

    @Then("the logout response should indicate success")
    public void theLogoutResponseShouldIndicateSuccess() {
        theRegistrationResponseShouldIndicateSuccess();
    }

    @Then("the logout should be successful")
    public void theLogoutShouldBeSuccessful() {
        theRegistrationShouldBeSuccessful();
    }

    @Then("the health check response status code should be {int}")
    public void theHealthCheckResponseStatusCodeShouldBe(int expectedStatus) {
        theLoginResponseStatusCodeShouldBe(expectedStatus);
    }

    @Then("the health check response should contain status {string}")
    public void theHealthCheckResponseShouldContainStatus(String status) {
        String body = response.getBody().asString();
        assertTrue(body.contains(status),
                "Health check should contain status: " + status + ". Body: " + body);
        System.out.println("✅ Health status verified");
    }

    // ==================== HELPER METHOD ====================

    /**
     * Sync data to EnhancedCrudSteps using reflection to avoid circular dependency
     */
    private void syncToEnhancedSteps() {
        try {
            // Get current user info
            UserInfo currentInfo = null;
            for (UserInfo info : userInfoMap.values()) {
                if (info.uniqueUsername != null && info.uniqueUsername.equals(currentUsername)) {
                    currentInfo = info;
                    break;
                }
            }

            Long userId = currentInfo != null ? currentInfo.userId : null;
            String email = currentInfo != null ? currentInfo.email : null;

            // Convert UserInfo map to compatible format
            Map<String, Object> userDataMap = new HashMap<>();
            for (Map.Entry<String, UserInfo> entry : userInfoMap.entrySet()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("username", entry.getValue().uniqueUsername);
                userData.put("password", entry.getValue().password);
                userData.put("email", entry.getValue().email);
                userData.put("userId", entry.getValue().userId);
                userDataMap.put(entry.getKey(), userData);
            }

            // Use reflection to call EnhancedCrudSteps.setSharedData
            Class<?> enhancedClass = Class.forName("steps.EnhancedCrudSteps");
            java.lang.reflect.Method setSharedData = enhancedClass.getMethod(
                    "setSharedData", String.class, Long.class, String.class, String.class, String.class, Map.class
            );

            setSharedData.invoke(null, jwtToken, userId, currentUsername, currentPassword, email, userDataMap);
            System.out.println("✅ Data synced to EnhancedCrudSteps");
        } catch (Exception e) {
            // If EnhancedCrudSteps not found or method fails, just continue
            System.out.println("ℹ️ Could not sync to EnhancedCrudSteps: " + e.getMessage());
        }
    }
}
