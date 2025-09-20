package com.example.dao.impl;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.model.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of UserDAO
 * Provides database operations using Spring JDBC Template
 */
@Repository
public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // SQL queries
    private static final String INSERT_SQL =
            "INSERT INTO users (username, email, password, full_name, phone_number, user_type, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT user_id, username, email, password, full_name, phone_number, user_type, is_active, created_at, updated_at " +
                    "FROM users WHERE user_id = ?";

    private static final String SELECT_BY_USERNAME_SQL =
            "SELECT user_id, username, email, password, full_name, phone_number, user_type, is_active, created_at, updated_at " +
                    "FROM users WHERE username = ?";

    private static final String SELECT_BY_EMAIL_SQL =
            "SELECT user_id, username, email, password, full_name, phone_number, user_type, is_active, created_at, updated_at " +
                    "FROM users WHERE email = ?";

    private static final String EXISTS_BY_USERNAME_SQL = "SELECT COUNT(*) FROM users WHERE username = ?";

    private static final String EXISTS_BY_EMAIL_SQL = "SELECT COUNT(*) FROM users WHERE email = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT user_id, username, email, password, full_name, phone_number, user_type, is_active, created_at, updated_at " +
                    "FROM users ORDER BY created_at DESC";

    private static final String UPDATE_SQL =
            "UPDATE users SET username = ?, email = ?, full_name = ?, phone_number = ?, user_type = ?, is_active = ?, updated_at = ? " +
                    "WHERE user_id = ?";

    private static final String DELETE_SQL = "DELETE FROM users WHERE user_id = ?";

    // RowMapper for User
    private static final RowMapper<User> USER_ROW_MAPPER = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setUserId(rs.getLong("user_id"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setFullName(rs.getString("full_name"));
            user.setPhoneNumber(rs.getString("phone_number"));
            user.setUserType(UserType.valueOf(rs.getString("user_type")));
            user.setActive(rs.getBoolean("is_active"));

            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                user.setCreatedAt(createdAt.toLocalDateTime());
            }

            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                user.setUpdatedAt(updatedAt.toLocalDateTime());
            }

            return user;
        }
    };

    @Override
    public User save(User user) {
        logger.info("=== USER REGISTRATION EVENT STARTED ===");
        logger.info("Attempting to save user: Username={}, Email={}, UserType={}",
                user.getUsername(), user.getEmail(), user.getUserType());

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int rowsAffected = jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getFullName());
                ps.setString(5, user.getPhoneNumber());
                ps.setString(6, user.getUserType().name());
                ps.setBoolean(7, user.isActive());
                return ps;
            }, keyHolder);

            if (rowsAffected > 0) {
                Long generatedId = keyHolder.getKey().longValue();
                user.setUserId(generatedId);

                logger.info("=== USER REGISTRATION EVENT SUCCESSFUL ===");
                logger.info("User saved successfully with ID: {}", generatedId);
                logger.info("User details - ID: {}, Username: {}, Email: {}, UserType: {}",
                        generatedId, user.getUsername(), user.getEmail(), user.getUserType());
                return user;
            } else {
                logger.error("=== USER REGISTRATION EVENT FAILED ===");
                logger.error("No rows affected while saving user: {}", user.getUsername());
                throw new RuntimeException("Failed to save user - no rows affected");
            }

        } catch (DataAccessException e) {
            logger.error("=== USER REGISTRATION EVENT ERROR ===");
            logger.error("Database error while saving user: {}", user.getUsername());
            logger.error("Error message: {}", e.getMessage());
            logger.error("Error details: ", e);
            throw new RuntimeException("Database error while saving user", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        logger.info("Finding user by ID: {}", id);

        try {
            User user = jdbcTemplate.queryForObject(SELECT_BY_ID_SQL, USER_ROW_MAPPER, id);
            logger.info("User found: {}", user != null ? user.getUsername() : "null");
            return Optional.ofNullable(user);

        } catch (EmptyResultDataAccessException e) {
            logger.info("User with ID {} not found", id);
            return Optional.empty();
        } catch (DataAccessException e) {
            logger.error("Database error while finding user by ID: {}", id, e);
            throw new RuntimeException("Database error while finding user", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        logger.info("Finding user by username: {}", username);

        try {
            User user = jdbcTemplate.queryForObject(SELECT_BY_USERNAME_SQL, USER_ROW_MAPPER, username);
            logger.info("User found: {}", user != null ? user.getUsername() : "null");
            return Optional.ofNullable(user);

        } catch (EmptyResultDataAccessException e) {
            logger.info("User with username {} not found", username);
            return Optional.empty();
        } catch (DataAccessException e) {
            logger.error("Database error while finding user by username: {}", username, e);
            throw new RuntimeException("Database error while finding user", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        logger.info("Finding user by email: {}", email);

        try {
            User user = jdbcTemplate.queryForObject(SELECT_BY_EMAIL_SQL, USER_ROW_MAPPER, email);
            logger.info("User found: {}", user != null ? user.getUsername() : "null");
            return Optional.ofNullable(user);

        } catch (EmptyResultDataAccessException e) {
            logger.info("User with email {} not found", email);
            return Optional.empty();
        } catch (DataAccessException e) {
            logger.error("Database error while finding user by email: {}", email, e);
            throw new RuntimeException("Database error while finding user", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        try {
            Integer count = jdbcTemplate.queryForObject(EXISTS_BY_USERNAME_SQL, Integer.class, username);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            logger.error("Database error while checking username existence: {}", username, e);
            throw new RuntimeException("Database error while checking username", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try {
            Integer count = jdbcTemplate.queryForObject(EXISTS_BY_EMAIL_SQL, Integer.class, email);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            logger.error("Database error while checking email existence: {}", email, e);
            throw new RuntimeException("Database error while checking email", e);
        }
    }

    @Override
    public List<User> findAll() {
        logger.info("Finding all users");

        try {
            List<User> users = jdbcTemplate.query(SELECT_ALL_SQL, USER_ROW_MAPPER);
            logger.info("Found {} users", users.size());
            return users;

        } catch (DataAccessException e) {
            logger.error("Database error while finding all users", e);
            throw new RuntimeException("Database error while finding all users", e);
        }
    }

    @Override
    public User update(User user) {
        logger.info("Updating user with ID: {}", user.getUserId());

        try {
            user.setUpdatedAt(LocalDateTime.now());

            int rowsAffected = jdbcTemplate.update(UPDATE_SQL,
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getPhoneNumber(),
                    user.getUserType().name(),
                    user.isActive(),
                    Timestamp.valueOf(user.getUpdatedAt()),
                    user.getUserId()
            );

            if (rowsAffected > 0) {
                logger.info("User updated successfully: {}", user.getUsername());
                return user;
            } else {
                logger.error("User with ID {} not found for update", user.getUserId());
                throw new RuntimeException("User not found for update");
            }

        } catch (DataAccessException e) {
            logger.error("Database error while updating user: {}", user.getUserId(), e);
            throw new RuntimeException("Database error while updating user", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        logger.info("Deleting user with ID: {}", id);

        try {
            int rowsAffected = jdbcTemplate.update(DELETE_SQL, id);

            if (rowsAffected > 0) {
                logger.info("User with ID {} deleted successfully", id);
                return true;
            } else {
                logger.info("User with ID {} not found for deletion", id);
                return false;
            }

        } catch (DataAccessException e) {
            logger.error("Database error while deleting user: {}", id, e);
            throw new RuntimeException("Database error while deleting user", e);
        }
    }
}
