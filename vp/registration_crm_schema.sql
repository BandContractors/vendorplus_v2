-- ====================================================================
-- VendorPlus Self-Service Registration + CRM System
-- Database Schema Changes
-- Date: March 9, 2026
-- ====================================================================

-- --------------------------------------------------------------------
-- 1. Create company table for multi-tenant data segregation
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `company`;
CREATE TABLE `company` (
  `company_id` INT(11) NOT NULL AUTO_INCREMENT,
  `company_code` VARCHAR(50) NOT NULL,
  `company_name` VARCHAR(200) NOT NULL,
  `tax_id` VARCHAR(100) DEFAULT NULL,
  `address` VARCHAR(500) DEFAULT NULL,
  `city` VARCHAR(100) DEFAULT NULL,
  `country` VARCHAR(100) DEFAULT NULL,
  `registration_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_active` VARCHAR(3) NOT NULL DEFAULT 'Yes',
  `contact_person` VARCHAR(200) DEFAULT NULL,
  `contact_email` VARCHAR(200) DEFAULT NULL,
  `contact_phone` VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (`company_id`),
  UNIQUE KEY `u_company_code` (`company_code`),
  UNIQUE KEY `u_company_tax_id` (`tax_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Companies for multi-tenant data segregation';

-- Insert default/system company for existing users
INSERT INTO `company` (
  `company_id`, 
  `company_code`, 
  `company_name`, 
  `is_active`
) VALUES (
  1, 
  'SYSTEM', 
  'System Default Company', 
  'Yes'
);

-- --------------------------------------------------------------------
-- 2. Add company_id to user_detail table
-- --------------------------------------------------------------------
ALTER TABLE `user_detail` 
  ADD COLUMN `company_id` INT(11) DEFAULT 1 AFTER `user_detail_id`,
  ADD KEY `idx_user_company` (`company_id`);

-- Update all existing users to belong to default company
UPDATE `user_detail` SET `company_id` = 1 WHERE `company_id` IS NULL;

-- Make company_id NOT NULL after setting defaults
ALTER TABLE `user_detail` 
  MODIFY COLUMN `company_id` INT(11) NOT NULL;

-- --------------------------------------------------------------------
-- 3. Enhance login_session table for activity tracking
-- --------------------------------------------------------------------
ALTER TABLE `login_session` 
  ADD COLUMN `login_timestamp` DATETIME DEFAULT CURRENT_TIMESTAMP AFTER `session_id`,
  ADD COLUMN `logout_timestamp` DATETIME DEFAULT NULL AFTER `login_timestamp`,
  ADD COLUMN `browser_agent` VARCHAR(500) DEFAULT NULL AFTER `remote_user`,
  ADD KEY `idx_login_timestamp` (`login_timestamp`),
  ADD KEY `idx_user_login` (`user_detail_id`, `login_timestamp`);

-- --------------------------------------------------------------------
-- 4. Create user_activity_log table for audit trail
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `user_activity_log`;
CREATE TABLE `user_activity_log` (
  `activity_log_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `user_detail_id` INT(11) NOT NULL,
  `company_id` INT(11) NOT NULL,
  `activity_type` VARCHAR(50) NOT NULL COMMENT 'LOGIN, LOGOUT, CREATE, UPDATE, DELETE, VIEW',
  `activity_module` VARCHAR(100) NOT NULL COMMENT 'USER, ITEM, TRANSACTION, etc',
  `activity_description` TEXT,
  `activity_timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ip_address` VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`activity_log_id`),
  KEY `idx_user_activity` (`user_detail_id`, `activity_timestamp`),
  KEY `idx_company_activity` (`company_id`, `activity_timestamp`),
  KEY `idx_activity_type` (`activity_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User activity audit log';

-- --------------------------------------------------------------------
-- 5. Create default "Registered Users" group for new registrations
-- --------------------------------------------------------------------
-- Check if group_detail table exists and insert default group
INSERT INTO `group_detail` (
  `group_detail_id`,
  `group_detail_code`,
  `group_detail_name`,
  `group_detail_is_system`
) VALUES (
  999,
  'REGISTERED_USERS',
  'Registered Users (Default)',
  'Yes'
) ON DUPLICATE KEY UPDATE 
  `group_detail_code` = 'REGISTERED_USERS';

-- Create default view-only permissions for registered users group
-- This will be populated based on function_name table
-- For now, just create the structure - permissions will be managed via UI

-- ====================================================================
-- STORED PROCEDURES
-- ====================================================================

-- --------------------------------------------------------------------
-- SP: Insert Company
-- --------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_insert_company;
DELIMITER $$
CREATE PROCEDURE sp_insert_company(
  IN p_company_code VARCHAR(50),
  IN p_company_name VARCHAR(200),
  IN p_tax_id VARCHAR(100),
  IN p_address VARCHAR(500),
  IN p_city VARCHAR(100),
  IN p_country VARCHAR(100),
  IN p_contact_person VARCHAR(200),
  IN p_contact_email VARCHAR(200),
  IN p_contact_phone VARCHAR(50)
)
BEGIN
  INSERT INTO company (
    company_code,
    company_name,
    tax_id,
    address,
    city,
    country,
    contact_person,
    contact_email,
    contact_phone,
    registration_date,
    is_active
  ) VALUES (
    p_company_code,
    p_company_name,
    p_tax_id,
    p_address,
    p_city,
    p_country,
    p_contact_person,
    p_contact_email,
    p_contact_phone,
    NOW(),
    'Yes'
  );
  
  SELECT LAST_INSERT_ID() as company_id;
END$$
DELIMITER ;

-- --------------------------------------------------------------------
-- SP: Search Company by ID
-- --------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_search_company_by_id;
DELIMITER $$
CREATE PROCEDURE sp_search_company_by_id(
  IN p_company_id INT
)
BEGIN
  SELECT * FROM company WHERE company_id = p_company_id;
END$$
DELIMITER ;

-- --------------------------------------------------------------------
-- SP: Search Company by Code
-- --------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_search_company_by_code;
DELIMITER $$
CREATE PROCEDURE sp_search_company_by_code(
  IN p_company_code VARCHAR(50)
)
BEGIN
  SELECT * FROM company WHERE company_code = p_company_code;
END$$
DELIMITER ;

-- --------------------------------------------------------------------
-- SP: Check if Tax ID exists (for validation)
-- --------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_check_tax_id_exists;
DELIMITER $$
CREATE PROCEDURE sp_check_tax_id_exists(
  IN p_tax_id VARCHAR(100)
)
BEGIN
  SELECT COUNT(*) as count FROM company 
  WHERE tax_id = p_tax_id AND tax_id IS NOT NULL AND tax_id != '';
END$$
DELIMITER ;

-- --------------------------------------------------------------------
-- SP: Insert User with Company (for registration)
-- --------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_insert_user_with_company;
DELIMITER $$
CREATE PROCEDURE sp_insert_user_with_company(
  IN p_company_id INT,
  IN p_user_name VARCHAR(50),
  IN p_user_password VARCHAR(200),
  IN p_first_name VARCHAR(50),
  IN p_second_name VARCHAR(50),
  IN p_third_name VARCHAR(50),
  IN p_email_address VARCHAR(200),
  IN p_phone_no VARCHAR(50),
  IN p_trans_code VARCHAR(200),
  IN p_language_system VARCHAR(20),
  IN p_language_output VARCHAR(20)
)
BEGIN
  INSERT INTO user_detail (
    company_id,
    user_name,
    user_password,
    first_name,
    second_name,
    third_name,
    email_address,
    phone_no,
    trans_code,
    is_user_locked,
    is_user_gen_admin,
    user_category_id,
    language_system,
    language_output,
    add_date
  ) VALUES (
    p_company_id,
    p_user_name,
    p_user_password,
    p_first_name,
    IFNULL(p_second_name, ''),
    IFNULL(p_third_name, ''),
    p_email_address,
    IFNULL(p_phone_no, ''),
    p_trans_code,
    'No',  -- Not locked
    'No',  -- Not admin
    1,     -- Default user category
    p_language_system,
    p_language_output,
    NOW()
  );
  
  SET @new_user_id = LAST_INSERT_ID();
  
  -- Assign user to default "Registered Users" group
  INSERT INTO group_user (group_user_id, group_detail_id, user_detail_id)
  VALUES ((SELECT IFNULL(MAX(group_user_id), 0) + 1 FROM group_user g), 999, @new_user_id);
  
  SELECT @new_user_id as user_detail_id;
END$$
DELIMITER ;

-- --------------------------------------------------------------------
-- SP: Get User Count by Company
-- --------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_count_users_by_company;
DELIMITER $$
CREATE PROCEDURE sp_count_users_by_company(
  IN p_company_id INT
)
BEGIN
  SELECT 
    COUNT(*) as total_users,
    SUM(CASE WHEN is_user_locked = 'No' THEN 1 ELSE 0 END) as active_users,
    SUM(CASE WHEN is_user_locked = 'Yes' THEN 1 ELSE 0 END) as locked_users
  FROM user_detail
  WHERE company_id = p_company_id;
END$$
DELIMITER ;

-- --------------------------------------------------------------------
-- SP: Get Recent Login Activity
-- --------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_get_recent_login_activity;
DELIMITER $$
CREATE PROCEDURE sp_get_recent_login_activity(
  IN p_company_id INT,
  IN p_limit INT
)
BEGIN
  SELECT 
    ls.login_session_id,
    ls.user_detail_id,
    ud.user_name,
    CONCAT(ud.first_name, ' ', ud.second_name) as full_name,
    ls.login_timestamp,
    ls.logout_timestamp,
    ls.remote_ip,
    ls.remote_host,
    s.store_name
  FROM login_session ls
  INNER JOIN user_detail ud ON ls.user_detail_id = ud.user_detail_id
  LEFT JOIN store s ON ls.store_id = s.store_id
  WHERE ud.company_id = p_company_id
  ORDER BY ls.login_timestamp DESC
  LIMIT p_limit;
END$$
DELIMITER ;

-- --------------------------------------------------------------------
-- SP: Get Registration Statistics
-- --------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_get_registration_stats;
DELIMITER $$
CREATE PROCEDURE sp_get_registration_stats(
  IN p_company_id INT,
  IN p_days_back INT
)
BEGIN
  SELECT 
    DATE(add_date) as registration_date,
    COUNT(*) as registrations_count
  FROM user_detail
  WHERE company_id = p_company_id
    AND add_date >= DATE_SUB(CURDATE(), INTERVAL p_days_back DAY)
  GROUP BY DATE(add_date)
  ORDER BY registration_date DESC;
END$$
DELIMITER ;

-- --------------------------------------------------------------------
-- SP: Get User List with Filters (for CRM)
-- --------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_search_users_for_crm;
DELIMITER $$
CREATE PROCEDURE sp_search_users_for_crm(
  IN p_company_id INT,
  IN p_search_text VARCHAR(200),
  IN p_status_filter VARCHAR(10)  -- 'ALL', 'ACTIVE', 'LOCKED'
)
BEGIN
  SELECT 
    ud.user_detail_id,
    ud.user_name,
    ud.first_name,
    ud.second_name,
    ud.third_name,
    ud.email_address,
    ud.phone_no,
    ud.is_user_locked,
    ud.is_user_gen_admin,
    ud.add_date,
    c.company_name,
    (SELECT MAX(login_timestamp) FROM login_session WHERE user_detail_id = ud.user_detail_id) as last_login
  FROM user_detail ud
  LEFT JOIN company c ON ud.company_id = c.company_id
  WHERE ud.company_id = p_company_id
    AND (
      p_search_text IS NULL OR p_search_text = '' OR
      ud.user_name LIKE CONCAT('%', p_search_text, '%') OR
      ud.first_name LIKE CONCAT('%', p_search_text, '%') OR
      ud.second_name LIKE CONCAT('%', p_search_text, '%') OR
      ud.email_address LIKE CONCAT('%', p_search_text, '%')
    )
    AND (
      p_status_filter = 'ALL' OR
      (p_status_filter = 'ACTIVE' AND ud.is_user_locked = 'No') OR
      (p_status_filter = 'LOCKED' AND ud.is_user_locked = 'Yes')
    )
  ORDER BY ud.add_date DESC;
END$$
DELIMITER ;

-- --------------------------------------------------------------------
-- SP: Log User Activity
-- --------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_log_user_activity;
DELIMITER $$
CREATE PROCEDURE sp_log_user_activity(
  IN p_user_detail_id INT,
  IN p_company_id INT,
  IN p_activity_type VARCHAR(50),
  IN p_activity_module VARCHAR(100),
  IN p_activity_description TEXT,
  IN p_ip_address VARCHAR(100)
)
BEGIN
  INSERT INTO user_activity_log (
    user_detail_id,
    company_id,
    activity_type,
    activity_module,
    activity_description,
    activity_timestamp,
    ip_address
  ) VALUES (
    p_user_detail_id,
    p_company_id,
    p_activity_type,
    p_activity_module,
    p_activity_description,
    NOW(),
    p_ip_address
  );
END$$
DELIMITER ;

-- ====================================================================
-- END OF SCHEMA CHANGES
-- ====================================================================
