-- Create new admin user 'chen' with BCrypt password
-- Password: chen20040209 -> $2b$12$0AYIGbFyQNIuK1SYRzmOzumz4sM7Bn1rUq7nyXelJifaraAKX2Dv2

SET NAMES utf8mb4;
INSERT INTO `sys_user` (`user_name`, `nick_name`, `password`, `status`, `user_type`, `login_date`) 
VALUES ('chen', '陈总', '$2b$12$0AYIGbFyQNIuK1SYRzmOzumz4sM7Bn1rUq7nyXelJifaraAKX2Dv2', '0', '00', NOW());
