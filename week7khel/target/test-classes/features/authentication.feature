Feature: Khel App User Authentication
  As a user of the Khel sports booking application
  I want to register, login, and manage my authentication
  So that I can securely access the platform and book sports venues

  Background:
    Given the authentication API is available at "http://localhost:8080/SpringMvcHelloWorld"

  @smoke @registration
  Scenario: Successful User Registration
    When I register a new user with username "testuser_cucumber", password "TestPass123!", email "testuser@test.com", full name "Test User", phone "9841234567", and user type "player"
    Then the registration response status code should be 200 or 201
    And the registration response should indicate success

  @smoke @login
  Scenario: Successful User Login
    Given a user exists with username "loginuser_cucumber", password "LoginPass123!", email "loginuser@test.com", full name "Login User", phone "9841234567", and user type "player"
    When I login with username "loginuser_cucumber" and password "LoginPass123!"
    Then the login response status code should be 200 or 201
    And the login response should contain a JWT token
    And the login response should indicate success

  @negative @security
  Scenario: Failed Login with Invalid Credentials
    When I attempt to login with username "invaliduser" and password "wrongpassword"
    Then the login response status code should be 401
    And the login response should contain message "Invalid"

  @negative @security
  Scenario: Failed Login with Incorrect Password
    Given a user exists with username "secureuser_cucumber", password "SecurePass123!", email "secure@test.com", full name "Secure User", phone "9841234567", and user type "player"
    When I attempt to login with username "secureuser_cucumber" and password "WrongPassword"
    Then the login response status code should be 401

  @security @authorization
  Scenario: Access Protected Endpoint Without Token
    When I attempt to access protected endpoint "/api/users" without a token
    Then the protected endpoint response status code should be 401

  @security @authorization
  Scenario: Access Protected Endpoint With Valid Token
    Given a user exists with username "protecteduser_cucumber", password "ProtectedPass123!", email "protected@test.com", full name "Protected User", phone "9841234567", and user type "player"
    And I have a valid JWT token for user "protecteduser_cucumber"
    When I access protected endpoint "/api/users" with valid token
    Then the protected endpoint response status code should be 200
    And the protected endpoint response should indicate success

  @integration @complete-flow
  Scenario: Complete Authentication Flow - Register, Login, and Access
    When I register a new user with username "flowuser", password "FlowPass123!", email "flowuser@test.com", full name "Flow User", phone "9841234567", and user type "player"
    Then the registration should be successful
    When I login with username "flowuser" and password "FlowPass123!"
    Then the login should be successful
    And I should receive a JWT token
    When I use the JWT token to access protected endpoint "/api/users"
    Then I should be able to access the protected resource

  @jwt @token-validation
  Scenario: JWT Token Validation
    Given a user exists with username "tokenuser_cucumber", password "TokenPass123!", email "tokenuser@test.com", full name "Token User", phone "9841234567", and user type "player"
    And I have a valid JWT token for user "tokenuser_cucumber"
    When I validate the JWT token
    Then the token validation response status code should be 200
    And the token validation response should indicate success

  @jwt @token-refresh
  Scenario: JWT Token Refresh
    Given a user exists with username "refreshuser_cucumber", password "RefreshPass123!", email "refreshuser@test.com", full name "Refresh User", phone "9841234567", and user type "player"
    And I have a valid JWT token for user "refreshuser_cucumber"
    When I request a token refresh
    Then the token refresh response status code should be 200
    And the token refresh response should indicate success

  @logout @session-management
  Scenario: User Logout with Token Invalidation
    Given a user exists with username "logoutuser_cucumber", password "LogoutPass123!", email "logoutuser@test.com", full name "Logout User", phone "9841234567", and user type "player"
    And I have a valid JWT token for user "logoutuser_cucumber"
    When I logout using the JWT token
    Then the logout response status code should be 200
    And the logout response should indicate success
    When I attempt to access protected endpoint "/api/users" with the invalidated token
    Then the protected endpoint response status code should be 401

  @negative @validation
  Scenario: Registration with Missing Email
    When I attempt to register with username "testuser_noemail", password "TestPass123!", but missing email
    Then the registration response status code should be 400 or 404

  @negative @validation
  Scenario: Registration with Missing Password
    When I attempt to register with username "testuser_nopass", email "nopass@test.com", but missing password
    Then the registration response status code should be 400 or 404

  @negative @duplicate
  Scenario: Registration with Duplicate Username
    Given a user exists with username "duplicateuser_cucumber", password "DuplicatePass123!", email "duplicate@test.com", full name "Duplicate User", phone "9841234567", and user type "player"
    When I attempt to register with username "duplicateuser_cucumber", password "NewPassword123!", email "newduplicate@test.com", full name "New User", phone "9841234568", and user type "player"
    Then the registration response status code should be 400
    And the registration response should contain message "already"

  @health-check @monitoring
  Scenario: API Health Check
    When I check the API health status at "/api/users/health"
    Then the health check response status code should be 200
    And the health check response should contain status "UP"

  @regression @user-types
  Scenario Outline: Registration for Different User Types
    When I register a new user with username "<username>", password "<password>", email "<email>", full name "<fullName>", phone "<phone>", and user type "<userType>"
    Then the registration response status code should be 200 or 201
    And the registration response should indicate success

    Examples:
      | username             | password        | email              | fullName      | phone      | userType |
      | player_cucumber_1    | PlayerPass123!  | player1@test.com   | Player One    | 9841234567 | player   |
      | coach_cucumber_1     | CoachPass123!   | coach1@test.com    | Coach One     | 9841234568 | coach    |
      | admin_cucumber_1     | AdminPass123!   | admin1@test.com    | Admin One     | 9841234569 | admin    |