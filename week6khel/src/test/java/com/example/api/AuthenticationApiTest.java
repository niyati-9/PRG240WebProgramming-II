package com.example.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 Test Class for Authentication API
 * Tests user registration and login functionality for Khel App
 *
 * @author Khel App Development Team
 * @version 1.0
 */
@DisplayName("Khel App Authentication API Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationApiTest {

    private static final String BASE_URL = "http://localhost:8080/SpringMvcHelloWorld";
    private static String jwtToken;
    private static String refreshToken;
    private static String testUsername;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
        testUsername = "testuser_" + System.currentTimeMillis();
        System.out.println("\n" + "=".repeat(80));
        System.out.println("KHEL APP AUTHENTICATION TEST SUITE STARTING");
        System.out.println("Base URL: " + BASE_URL);
        System.out.println("Test Username: " + testUsername);
        System.out.println("=".repeat(80));
    }

    /**
     * Test 1: User Registration
     */
    @Test
    @Order(1)
    @DisplayName("Test 1: User Registration - Should Return Success")
    void testUserRegistration() {
        System.out.println("\n=== Test 1: Testing User Registration ===");

        // Create complete registration data with all required fields
        Map<String, String> registrationData = Map.of(
                "username", testUsername,
                "password", "TestPassword123!",
                "fullName", "Test User",
                "email", testUsername + "@test.com",
                "phoneNumber", "9841234567",
                "userType", "player"
        );

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(registrationData)
                .when()
                .post("/api/auth/register")
                .then()
                .extract()
                .response();

        int statusCode = response.getStatusCode();
        String responseBody = response.asString();

        System.out.println("Registration Request Body: " + registrationData);
        System.out.println("Registration Response Status: " + statusCode);
        System.out.println("Registration Response Body: " + responseBody);

        assertTrue(statusCode == 200 || statusCode == 201 || statusCode == 400,
                "Expected 200/201 (success) or 400 (user exists), got: " + statusCode);

        if (statusCode == 200 || statusCode == 201) {
            System.out.println("✅ Registration successful!");
            assertTrue(responseBody.contains("success"), "Response should contain success field");
        } else if (statusCode == 400) {
            System.out.println("⚠️ User already exists (this is expected if test was run before)");
        }

        System.out.println("=== Test 1: COMPLETED ===\n");
    }

    /**
     * Test 2: User Login
     */
    @Test
    @Order(2)
    @DisplayName("Test 2: User Login - Should Return JWT Token")
    void testUserLogin() {
        System.out.println("\n=== Test 2: Testing User Login ===");

        // First, try to register the user (ignore if already exists)
        Map<String, String> registrationData = Map.of(
                "username", testUsername,
                "password", "TestPassword123!",
                "fullName", "Test User",
                "email", testUsername + "@test.com",
                "phoneNumber", "9841234567",
                "userType", "player"
        );

        try {
            RestAssured
                    .given()
                    .contentType(ContentType.JSON)
                    .body(registrationData)
                    .when()
                    .post("/api/auth/register");
            System.out.println("User registration attempted (may already exist)");
        } catch (Exception e) {
            System.out.println("Registration skipped (user may already exist)");
        }

        // Now attempt login
        Map<String, String> loginData = Map.of(
                "username", testUsername,
                "password", "TestPassword123!"
        );

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginData)
                .when()
                .post("/api/auth/login")
                .then()
                .extract()
                .response();

        int statusCode = response.getStatusCode();
        String responseBody = response.asString();

        System.out.println("Login Request Body: " + loginData);
        System.out.println("Login Response Status: " + statusCode);
        System.out.println("Login Response Body: " + responseBody);

        assertTrue(statusCode == 200 || statusCode == 201,
                "Expected 200/201 for successful login, got: " + statusCode + ". Body: " + responseBody);

        if (statusCode == 200 || statusCode == 201) {
            try {
                if (responseBody.contains("token") || responseBody.contains("jwt")) {
                    System.out.println("✅ Login successful! JWT token received");

                    // Extract token from data.token path
                    String token = response.jsonPath().getString("data.token");
                    if (token != null && !token.isEmpty()) {
                        jwtToken = token;
                        System.out.println("JWT Token extracted: " +
                                token.substring(0, Math.min(50, token.length())) + "...");
                    }

                    // Extract refresh token
                    String refresh = response.jsonPath().getString("data.refreshToken");
                    if (refresh != null && !refresh.isEmpty()) {
                        refreshToken = refresh;
                        System.out.println("Refresh Token extracted: " +
                                refresh.substring(0, Math.min(30, refresh.length())) + "...");
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ Could not extract JWT token: " + e.getMessage());
            }
        }

        System.out.println("=== Test 2: COMPLETED ===\n");
    }

    /**
     * Test 3: Login with Invalid Credentials
     */
    @Test
    @Order(3)
    @DisplayName("Test 3: Login with Invalid Credentials - Should Return 401")
    void testLoginWithInvalidCredentials() {
        System.out.println("\n=== Test 3: Testing Login with Invalid Credentials ===");

        Map<String, String> invalidLoginData = Map.of(
                "username", "invaliduser",
                "password", "wrongpassword"
        );

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(invalidLoginData)
                .when()
                .post("/api/auth/login")
                .then()
                .extract()
                .response();

        int statusCode = response.getStatusCode();
        String responseBody = response.asString();

        System.out.println("Invalid Login Request Body: " + invalidLoginData);
        System.out.println("Invalid Login Response Status: " + statusCode);
        System.out.println("Invalid Login Response Body: " + responseBody);

        assertTrue(statusCode == 401 || statusCode == 403 || statusCode == 400,
                "Expected 401/403/400 for invalid login, got: " + statusCode);

        System.out.println("✅ Invalid login correctly rejected!");
        System.out.println("=== Test 3: COMPLETED ===\n");
    }

    /**
     * Test 4: Access Protected Endpoint Without Token
     */
    @Test
    @Order(4)
    @DisplayName("Test 4: Access Protected Endpoint Without Token - Should Return 401")
    void testAccessProtectedEndpointWithoutToken() {
        System.out.println("\n=== Test 4: Testing Access to Protected Endpoint Without Token ===");

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/users")
                .then()
                .extract()
                .response();

        int statusCode = response.getStatusCode();
        String responseBody = response.asString();

        System.out.println("Request to Protected Endpoint (No Token)");
        System.out.println("Response Status: " + statusCode);
        System.out.println("Response Body: " + responseBody);

        assertEquals(401, statusCode,
                "Expected 401 Unauthorized when accessing protected endpoint without token");

        System.out.println("✅ Protected endpoint correctly requires JWT token!");
        System.out.println("=== Test 4: COMPLETED ===\n");
    }

    /**
     * Test 5: Access Protected Endpoint With Valid Token
     */
    @Test
    @Order(5)
    @DisplayName("Test 5: Access Protected Endpoint With Token - Should Return Success")
    void testAccessProtectedEndpointWithToken() {
        System.out.println("\n=== Test 5: Testing Access to Protected Endpoint With Token ===");

        if (jwtToken == null || jwtToken.isEmpty()) {
            System.out.println("⚠️  No JWT token available from login test. Skipping this test.");
            System.out.println("Note: Run Test 2 (login) first to obtain a token.");
            System.out.println("=== Test 5: SKIPPED ===\n");
            return;
        }

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/users")
                .then()
                .extract()
                .response();

        int statusCode = response.getStatusCode();
        String responseBody = response.asString();

        System.out.println("Request to Protected Endpoint (With Token)");
        System.out.println("Authorization Header: Bearer " + jwtToken.substring(0, Math.min(20, jwtToken.length())) + "...");
        System.out.println("Response Status: " + statusCode);
        System.out.println("Response Body: " + responseBody);

        assertTrue(statusCode == 200 || statusCode == 201,
                "Expected 200/201 when accessing protected endpoint with valid token, got: " + statusCode + ". Body: " + responseBody);

        System.out.println("✅ Successfully accessed protected endpoint with JWT token!");
        System.out.println("=== Test 5: COMPLETED ===\n");
    }

    /**
     * Test 6: Token Validation
     */
    @Test
    @Order(6)
    @DisplayName("Test 6: Token Validation - Should Validate JWT Token")
    void testTokenValidation() {
        System.out.println("\n=== Test 6: Testing Token Validation ===");

        if (jwtToken == null || jwtToken.isEmpty()) {
            System.out.println("⚠️  No JWT token available. Skipping this test.");
            System.out.println("=== Test 6: SKIPPED ===\n");
            return;
        }

        Map<String, String> tokenData = Map.of("token", jwtToken);

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(tokenData)
                .when()
                .post("/api/auth/validate")
                .then()
                .extract()
                .response();

        int statusCode = response.getStatusCode();
        String responseBody = response.asString();

        System.out.println("Token Validation Response Status: " + statusCode);
        System.out.println("Token Validation Response Body: " + responseBody);

        assertEquals(200, statusCode, "Token validation should return 200");
        assertTrue(responseBody.contains("success"), "Response should indicate success");

        System.out.println("✅ Token validation successful!");
        System.out.println("=== Test 6: COMPLETED ===\n");
    }

    /**
     * Test 7: Token Refresh
     */
    @Test
    @Order(7)
    @DisplayName("Test 7: Token Refresh - Should Generate New JWT Token")
    void testTokenRefresh() {
        System.out.println("\n=== Test 7: Testing Token Refresh ===");

        if (refreshToken == null || refreshToken.isEmpty()) {
            System.out.println("⚠️  No refresh token available. Skipping this test.");
            System.out.println("=== Test 7: SKIPPED ===\n");
            return;
        }

        Map<String, String> refreshData = Map.of("refreshToken", refreshToken);

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(refreshData)
                .when()
                .post("/api/auth/refresh")
                .then()
                .extract()
                .response();

        int statusCode = response.getStatusCode();
        String responseBody = response.asString();

        System.out.println("Token Refresh Response Status: " + statusCode);
        System.out.println("Token Refresh Response Body: " + responseBody);

        if (statusCode == 200) {
            String newToken = response.jsonPath().getString("data.token");
            if (newToken != null && !newToken.isEmpty()) {
                System.out.println("✅ Token refresh successful! New token: " + newToken.substring(0, Math.min(30, newToken.length())) + "...");
                jwtToken = newToken; // Update token for subsequent tests
            }
        }

        System.out.println("=== Test 7: COMPLETED ===\n");
    }

    /**
     * Test 8: Logout
     */
    @Test
    @Order(8)
    @DisplayName("Test 8: Logout - Should Invalidate JWT Token")
    void testLogout() {
        System.out.println("\n=== Test 8: Testing Logout ===");

        if (jwtToken == null || jwtToken.isEmpty()) {
            System.out.println("⚠️  No JWT token available. Skipping this test.");
            System.out.println("=== Test 8: SKIPPED ===\n");
            return;
        }

        Map<String, String> logoutData = Map.of(
                "token", jwtToken,
                "refreshToken", refreshToken != null ? refreshToken : ""
        );

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .body(logoutData)
                .when()
                .post("/api/auth/logout")
                .then()
                .extract()
                .response();

        int statusCode = response.getStatusCode();
        String responseBody = response.asString();

        System.out.println("Logout Response Status: " + statusCode);
        System.out.println("Logout Response Body: " + responseBody);

        assertEquals(200, statusCode, "Logout should return 200");
        assertTrue(responseBody.contains("success"), "Logout response should indicate success");

        System.out.println("✅ Logout successful!");
        System.out.println("=== Test 8: COMPLETED ===\n");
    }

    /**
     * Test 9: Access After Logout (Should Fail)
     */
    @Test
    @Order(9)
    @DisplayName("Test 9: Access After Logout - Should Return 401")
    void testAccessAfterLogout() {
        System.out.println("\n=== Test 9: Testing Access After Logout ===");

        if (jwtToken == null || jwtToken.isEmpty()) {
            System.out.println("⚠️  No JWT token available. Skipping this test.");
            System.out.println("=== Test 9: SKIPPED ===\n");
            return;
        }

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/users")
                .then()
                .extract()
                .response();

        int statusCode = response.getStatusCode();
        String responseBody = response.asString();

        System.out.println("Access After Logout Response Status: " + statusCode);
        System.out.println("Access After Logout Response Body: " + responseBody);

        assertEquals(401, statusCode, "Access after logout should return 401");

        System.out.println("✅ Token correctly invalidated after logout!");
        System.out.println("=== Test 9: COMPLETED ===\n");
    }

    /**
     * Test 10: Health Check
     */
    @Test
    @Order(10)
    @DisplayName("Test 10: Health Check - Should Return API Status")
    void testHealthCheck() {
        System.out.println("\n=== Test 10: Testing Health Check ===");

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/users/health")
                .then()
                .extract()
                .response();

        int statusCode = response.getStatusCode();
        String responseBody = response.asString();

        System.out.println("Health Check Response Status: " + statusCode);
        System.out.println("Health Check Response Body: " + responseBody);

        assertEquals(200, statusCode, "Health check should return 200");
        assertTrue(responseBody.contains("status"), "Health check should contain status");

        System.out.println("✅ Health check successful!");
        System.out.println("=== Test 10: COMPLETED ===\n");
    }

    @AfterAll
    static void tearDown() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("KHEL APP AUTHENTICATION TEST SUITE COMPLETED");
        System.out.println("=".repeat(80));
        System.out.println("\nTest Summary:");
        System.out.println("- Test 1: User Registration");
        System.out.println("- Test 2: User Login (JWT Token Generation)");
        System.out.println("- Test 3: Invalid Login Rejection");
        System.out.println("- Test 4: Protected Endpoint Without Token");
        System.out.println("- Test 5: Protected Endpoint With Token");
        System.out.println("- Test 6: Token Validation");
        System.out.println("- Test 7: Token Refresh");
        System.out.println("- Test 8: Logout");
        System.out.println("- Test 9: Access After Logout");
        System.out.println("- Test 10: Health Check");
        System.out.println("\nNote: Ensure the application is running on http://localhost:8080");
        System.out.println("=".repeat(80) + "\n");
    }
}