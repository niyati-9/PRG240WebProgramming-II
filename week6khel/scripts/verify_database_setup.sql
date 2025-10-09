-- Added database verification script to check if table exists and is properly configured
USE khel_app;

-- Check if users table exists
SHOW TABLES LIKE 'users';

-- Check table structure
DESCRIBE users;

-- Check if there are any existing users
SELECT COUNT(*) as user_count FROM users;

-- Show sample of existing data (if any)
SELECT user_id, username, email, user_type, is_active, created_at FROM users LIMIT 5;
