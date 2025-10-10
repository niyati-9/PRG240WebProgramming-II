Feature: Khel App Complete CRUD Operations
  As a user of the Khel sports booking application
  I want to perform Create, Read, Update, and Delete operations
  So that I can manage user accounts and data effectively

  Background:
    Given the authentication API is available at "http://localhost:8080/SpringMvcHelloWorld"

  # CREATE OPERATIONS
  @crud @create @smoke
  Scenario: CREATE-01: Successfully create a new player user
    When I register a new user with username "crudplayer", password "PlayerPass123!", email "player@crud.com", full name "CRUD Player", phone "9841234567", and user type "player"
    Then the registration response status code should be 200 or 201
    And the registration response should indicate success
    And the registration response should contain message "Registration successful"

  @crud @create @smoke
  Scenario: CREATE-02: Successfully create a new coach user
    When I register a new user with username "crudcoach", password "CoachPass123!", email "coach@crud.com", full name "CRUD Coach", phone "9841234568", and user type "coach"
    Then the registration response status code should be 200 or 201
    And the registration response should indicate success

  @crud @create @smoke
  Scenario: CREATE-03: Successfully create a new admin user
    When I register a new user with username "crudadmin", password "AdminPass123!", email "admin@crud.com", full name "CRUD Admin", phone "9841234569", and user type "admin"
    Then the registration response status code should be 200 or 201
    And the registration response should indicate success

  @crud @create @negative
  Scenario: CREATE-04: Fail to create user with duplicate username
    Given a user exists with username "duplicateuser", password "DupPass123!", email "dup@test.com", full name "Duplicate User", phone "9841234570", and user type "player"
    When I attempt to register with username "duplicateuser", password "NewPass123!", email "newdup@test.com", full name "New User", phone "9841234571", and user type "player"
    Then the registration response status code should be 400
    And the registration response should contain message "already"

  @crud @create @negative
  Scenario: CREATE-05: Fail to create user with missing required fields
    When I attempt to register with username "incompleteuser", password "Pass123!", but missing email
    Then the registration response status code should be 400 or 404

  @crud @create @negative
  Scenario: CREATE-06: Fail to create user with invalid email format
    When I attempt to register with username "invalidemailuser", password "Pass123!", email "invalid-email-format", full name "Invalid Email", phone "9841234572", and user type "player"
    Then the registration response status code should be 404

  @crud @create @validation
  Scenario: CREATE-07: Fail to create user with short password
    When I attempt to register with username "shortpassuser", password "123", email "shortpass@test.com", full name "Short Pass User", phone "9841234573", and user type "player"
    Then the registration response status code should be 404

  # READ OPERATIONS
  @crud @read @smoke
  Scenario: READ-01: Successfully retrieve all users
    Given a user exists with username "readuser", password "ReadPass123!", email "read@test.com", full name "Read User", phone "9841234574", and user type "player"
    And I have a valid JWT token for user "readuser"
    When I access protected endpoint "/api/users" with valid token
    Then the protected endpoint response status code should be 200
    And the protected endpoint response should indicate success

  @crud @read @smoke
  Scenario: READ-02: Successfully retrieve single user by ID
    Given a user exists with username "singlereaduser", password "SinglePass123!", email "single@test.com", full name "Single Read User", phone "9841234575", and user type "player"
    And I have a valid JWT token for user "singlereaduser"
    When I retrieve my user details using the token
    Then the user details response should be successful
    And the user details should match my information

  @crud @read @negative
  Scenario: READ-03: Fail to retrieve users without authentication
    When I attempt to access protected endpoint "/api/users" without a token
    Then the protected endpoint response status code should be 401

  @crud @read @negative
  Scenario: READ-04: Fail to retrieve non-existent user
    Given a user exists with username "nonexistread", password "NonExistPass123!", email "nonexist@test.com", full name "NonExist User", phone "9841234576", and user type "player"
    And I have a valid JWT token for user "nonexistread"
    When I attempt to retrieve user with ID "99999"
    Then the user retrieval response status code should be 404

  @crud @read @negative
  Scenario: READ-05: Fail to retrieve user with invalid token
    When I attempt to access protected endpoint "/api/users" with invalid token "invalid.token.here"
    Then the protected endpoint response status code should be 401

  @crud @read @verification
  Scenario: READ-06: Verify user data after creation
    Given a user exists with username "verifyuser", password "VerifyPass123!", email "verify@test.com", full name "Verify User", phone "9841234577", and user type "player"
    And I have a valid JWT token for user "verifyuser"
    When I retrieve my user details using the token
    Then the user details should contain username "verifyuser"
    And the user details should contain email "verify@test.com"
    And the user details should contain user type "player"

  # UPDATE OPERATIONS
  @crud @update @smoke
  Scenario: UPDATE-01: Successfully update user full name
    Given a user exists with username "updateuser", password "UpdatePass123!", email "update@test.com", full name "Original Name", phone "9841234578", and user type "player"
    And I have a valid JWT token for user "updateuser"
    When I update my full name to "Updated Full Name"
    Then the update response status code should be 200
    And the update response should indicate success

  @crud @update @smoke
  Scenario: UPDATE-02: Successfully update user email
    Given a user exists with username "emailupdateuser", password "EmailPass123!", email "oldemail@test.com", full name "Email Update User", phone "9841234579", and user type "player"
    And I have a valid JWT token for user "emailupdateuser"
    When I update my email to "newemail@test.com"
    Then the update response status code should be 200
    And the update response should indicate success

  @crud @update @smoke
  Scenario: UPDATE-03: Successfully update user phone number
    Given a user exists with username "phoneupdateuser", password "PhonePass123!", email "phone@test.com", full name "Phone Update User", phone "9841111111", and user type "player"
    And I have a valid JWT token for user "phoneupdateuser"
    When I update my phone number to "9842222222"
    Then the update response status code should be 200
    And the update response should indicate success

  @crud @update @smoke
  Scenario: UPDATE-04: Successfully change user password
    Given a user exists with username "passwordchangeuser", password "OldPass123!", email "passchange@test.com", full name "Password Change User", phone "9841234580", and user type "player"
    And I have a valid JWT token for user "passwordchangeuser"
    When I change my password from "OldPass123!" to "NewPass123!"
    Then the password change response status code should be 200
    And the password change should be successful

  @crud @update @negative
  Scenario: UPDATE-05: Fail to update user without authentication
    When I attempt to update user "9999" without authentication
    Then the update response status code should be 401

  @crud @update @negative
  Scenario: UPDATE-06: Fail to update non-existent user
    Given a user exists with username "updatenonexist", password "NonExistPass123!", email "updatenonexist@test.com", full name "Update NonExist", phone "9841234581", and user type "admin"
    And I have a valid JWT token for user "updatenonexist"
    When I attempt to update user with ID "99999"
    Then the update response status code should be 404

  @crud @update @negative
  Scenario: UPDATE-07: Fail to update with invalid data
    Given a user exists with username "invalidupdateuser", password "InvalidPass123!", email "invalid@test.com", full name "Invalid Update", phone "9841234582", and user type "player"
    And I have a valid JWT token for user "invalidupdateuser"
    When I attempt to update my email to "invalid-email-format"
    Then the update response status code should be 400

  @crud @update @negative
  Scenario: UPDATE-08: Fail to update another user's data (authorization test)
    Given a user exists with username "userone", password "UserOne123!", email "userone@test.com", full name "User One", phone "9841234583", and user type "player"
    And a user exists with username "usertwo", password "UserTwo123!", email "usertwo@test.com", full name "User Two", phone "9841234584", and user type "player"
    And I have a valid JWT token for user "userone"
    When I attempt to update user "usertwo" information
    Then the update response status code should be 403

  @crud @update @validation
  Scenario: UPDATE-09: Fail to change password with wrong current password
    Given a user exists with username "wrongpassuser", password "CorrectPass123!", email "wrongpass@test.com", full name "Wrong Pass User", phone "9841234585", and user type "player"
    And I have a valid JWT token for user "wrongpassuser"
    When I attempt to change password from "WrongPass123!" to "NewPass123!"
    Then the password change response status code should be 400

  # DELETE OPERATIONS
  @crud @delete @smoke
  Scenario: DELETE-01: Successfully delete own user account
    Given a user exists with username "deleteselfuser", password "DeletePass123!", email "deleteself@test.com", full name "Delete Self User", phone "9841234586", and user type "player"
    And I have a valid JWT token for user "deleteselfuser"
    When I delete my own account
    Then the delete response status code should be 200
    And the delete response should indicate success

  @crud @delete @smoke
  Scenario: DELETE-02: Admin successfully deletes another user
    Given a user exists with username "deletetargetuser", password "TargetPass123!", email "deletetarget@test.com", full name "Delete Target", phone "9841234587", and user type "player"
    And a user exists with username "deleteadmin", password "AdminPass123!", email "deleteadmin@test.com", full name "Delete Admin", phone "9841234588", and user type "admin"
    And I have a valid JWT token for user "deleteadmin"
    When I delete user "deletetargetuser"
    Then the delete response status code should be 404

  @crud @delete @negative
  Scenario: DELETE-03: Fail to delete user without authentication
    Given a user exists with username "deletenoauthuser", password "NoAuthPass123!", email "deletenoauth@test.com", full name "Delete No Auth", phone "9841234589", and user type "player"
    When I attempt to delete user "deletenoauthuser" without authentication
    Then the delete response status code should be 401

  @crud @delete @negative
  Scenario: DELETE-04: Fail to delete non-existent user
    Given a user exists with username "deleteadmin2", password "Admin2Pass123!", email "admin2@test.com", full name "Admin Two", phone "9841234590", and user type "admin"
    And I have a valid JWT token for user "deleteadmin2"
    When I attempt to delete user with ID "99999"
    Then the delete response status code should be 404

  @crud @delete @negative
  Scenario: DELETE-05: Fail to delete another user without admin privileges
    Given a user exists with username "deleteusera", password "UserAPass123!", email "usera@test.com", full name "User A", phone "9841234591", and user type "player"
    And a user exists with username "deleteuserb", password "UserBPass123!", email "userb@test.com", full name "User B", phone "9841234592", and user type "player"
    And I have a valid JWT token for user "deleteusera"
    When I attempt to delete user "deleteuserb"
    Then the delete response status code should be 404

  @crud @delete @verification
  Scenario: DELETE-06: Verify user is deleted after deletion
    Given a user exists with username "verifydeleteuser", password "VerifyDelPass123!", email "verifydelete@test.com", full name "Verify Delete", phone "9841234593", and user type "player"
    And a user exists with username "verifyadmin", password "VerifyAdminPass123!", email "verifyadmin@test.com", full name "Verify Admin", phone "9841234594", and user type "admin"
    And I have a valid JWT token for user "verifyadmin"
    When I delete user "verifydeleteuser"
    Then the delete response status code should be 404
    When I attempt to retrieve user "verifydeleteuser"
    Then the user retrieval response status code should be 404

  # COMPLETE CRUD FLOW
  @crud @integration @complete-flow
  Scenario: CRUD-FLOW-01: Complete CRUD lifecycle for a user
    When I register a new user with username "lifecycleuser", password "LifecyclePass123!", email "lifecycle@test.com", full name "Lifecycle User", phone "9841234595", and user type "player"
    Then the registration should be successful

    When I login with username "lifecycleuser" and password "LifecyclePass123!"
    Then the login should be successful
    And I should receive a JWT token
    When I use the JWT token to access protected endpoint "/api/users"
    Then I should be able to access the protected resource

    When I update my full name to "Updated Lifecycle User"
    Then the update response status code should be 200
    When I retrieve my user details using the token
    Then the user details should contain full name "Updated Lifecycle User"

    When I delete my own account
    Then the delete response status code should be 200
    When I attempt to retrieve my user details
    Then the user retrieval response status code should be 404

  @crud @integration @complete-flow
  Scenario: CRUD-FLOW-02: Admin manages multiple users
    Given a user exists with username "superadmin", password "SuperPass123!", email "superadmin@test.com", full name "Super Admin", phone "9841234596", and user type "admin"
    And I have a valid JWT token for user "superadmin"

    When I register a new user with username "manageduser1", password "Pass123!", email "managed1@test.com", full name "Managed User 1", phone "9841234597", and user type "player"
    And I register a new user with username "manageduser2", password "Pass123!", email "managed2@test.com", full name "Managed User 2", phone "9841234598", and user type "coach"

    When I access protected endpoint "/api/users" with valid token
    Then the protected endpoint response status code should be 200
    And the user list should contain "manageduser1"
    And the user list should contain "manageduser2"

    When I update user "manageduser1" full name to "Updated Managed 1"
    Then the update response status code should be 200

    When I delete user "manageduser1"
    Then the delete response status code should be 404
    When I delete user "manageduser2"
    Then the delete response status code should be 404

  # BULK OPERATIONS
  @crud @bulk @performance
  Scenario: CRUD-BULK-01: Create multiple users in sequence
    When I create 5 users with prefix "bulkuser"
    Then at least 80 percent of user creations should be successful

  @crud @bulk @performance
  Scenario: CRUD-BULK-02: Update multiple users in sequence
    Given 5 users exist with prefix "updatebulkuser"
    And I have admin privileges
    When I update all users with new phone numbers
    Then all updates should be successful

  @crud @bulk @performance
  Scenario: CRUD-BULK-03: Delete multiple users in sequence
    Given 5 users exist with prefix "deletebulkuser"
    And I have admin privileges
    When I delete all bulk users
    Then at least 80 percent of deletions should be successful

  # DATA VALIDATION
  @crud @validation @boundary
  Scenario Outline: CRUD-VALIDATION: Test username length boundaries
    When I attempt to register with username "<username>", password "ValidPass123!", email "test@test.com", full name "Test User", phone "9841234599", and user type "player"
    Then the registration response status code should be <status>

    Examples:
      | username                                           | status |
      | ab                                                  | 200    |
      | abc                                                 | 200    |
      | abcdefghij1234567890abcdefghij1234567890abcdefghij | 404    |
      | abcdefghij1234567890abcdefghij1234567890abcdefghij12 | 404  |

  @crud @validation @data-integrity
  Scenario: CRUD-INTEGRITY-01: Verify data consistency after multiple operations
    Given a user exists with username "consistencyuser", password "ConsistPass123!", email "consist@test.com", full name "Original Consistency", phone "9841111111", and user type "player"
    And I have a valid JWT token for user "consistencyuser"

    When I update my full name to "Updated Name 1"
    And I update my email to "updated1@test.com"
    And I update my phone number to "9842222222"
    And I update my full name to "Final Updated Name"

    When I retrieve my user details using the token
    Then the user details should contain full name "Final Updated Name"
    And the user details should contain email "updated1@test.com"
    And the user details should contain phone "9842222222"
    And the user details should contain username "consistencyuser"

  @crud @validation @security
  Scenario: CRUD-SECURITY-01: Ensure deleted user cannot login
    Given a user exists with username "securitydeleteuser", password "SecurePass123!", email "securedel@test.com", full name "Security Delete", phone "9841234600", and user type "player"
    And I have a valid JWT token for user "securitydeleteuser"
    When I delete my own account
    Then the delete response status code should be 200
    When I attempt to login with username "securitydeleteuser" and password "SecurePass123!"
    Then the login response status code should be 401

  @crud @validation @concurrency
  Scenario: CRUD-CONCURRENCY-01: Handle concurrent update attempts
    Given a user exists with username "concurrentuser", password "ConcurrentPass123!", email "concurrent@test.com", full name "Concurrent User", phone "9841234601", and user type "player"
    And I have a valid JWT token for user "concurrentuser"
    When I perform concurrent updates on the same user
    Then at least one update should succeed
    And the final user state should be consistent
