-- ====================================================================
-- VendorPlus V2 - Company-Based Multi-Tenant Migration Script
-- Run this on the renamed vendorplus_v2 database
-- Date: March 9, 2026
-- ====================================================================

USE vendorplus_v2;

-- ====================================================================
-- STEP 1: Create Company Table
-- ====================================================================

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
  KEY `idx_company_tax_id` (`tax_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Companies for multi-tenant data segregation';

-- Insert default system company
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

-- ====================================================================
-- STEP 2: Add company_id to Core Tables for Data Segregation
-- ====================================================================

-- Add company_id to user_detail table
ALTER TABLE `user_detail` 
  ADD COLUMN IF NOT EXISTS `company_id` INT(11) DEFAULT 1 AFTER `user_detail_id`,
  ADD KEY IF NOT EXISTS `idx_user_company` (`company_id`);

UPDATE `user_detail` SET `company_id` = 1 WHERE `company_id` IS NULL OR `company_id` = 0;

ALTER TABLE `user_detail` 
  MODIFY COLUMN `company_id` INT(11) NOT NULL DEFAULT 1;

-- Add company_id to store table (each company has their own stores)
ALTER TABLE `store` 
  ADD COLUMN IF NOT EXISTS `company_id` INT(11) DEFAULT 1 AFTER `store_id`,
  ADD KEY IF NOT EXISTS `idx_store_company` (`company_id`);

UPDATE `store` SET `company_id` = 1 WHERE `company_id` IS NULL OR `company_id` = 0;

ALTER TABLE `store` 
  MODIFY COLUMN `company_id` INT(11) NOT NULL DEFAULT 1;

-- Add company_id to item table (each company has their own inventory)
ALTER TABLE `item` 
  ADD COLUMN IF NOT EXISTS `company_id` INT(11) DEFAULT 1 AFTER `item_id`,
  ADD KEY IF NOT EXISTS `idx_item_company` (`company_id`);

UPDATE `item` SET `company_id` = 1 WHERE `company_id` IS NULL OR `company_id` = 0;

ALTER TABLE `item` 
  MODIFY COLUMN `company_id` INT(11) NOT NULL DEFAULT 1;

-- Add company_id to transactor table (customers/suppliers per company)
ALTER TABLE `transactor` 
  ADD COLUMN IF NOT EXISTS `company_id` INT(11) DEFAULT 1 AFTER `transactor_id`,
  ADD KEY IF NOT EXISTS `idx_transactor_company` (`company_id`);

UPDATE `transactor` SET `company_id` = 1 WHERE `company_id` IS NULL OR `company_id` = 0;

ALTER TABLE `transactor` 
  MODIFY COLUMN `company_id` INT(11) NOT NULL DEFAULT 1;

-- Add company_id to category table
ALTER TABLE `category` 
  ADD COLUMN IF NOT EXISTS `company_id` INT(11) DEFAULT 1 AFTER `category_id`,
  ADD KEY IF NOT EXISTS `idx_category_company` (`company_id`);

UPDATE `category` SET `company_id` = 1 WHERE `company_id` IS NULL OR `company_id` = 0;

ALTER TABLE `category` 
  MODIFY COLUMN `company_id` INT(11) NOT NULL DEFAULT 1;

-- Add company_id to subcategory table
ALTER TABLE `subcategory` 
  ADD COLUMN IF NOT EXISTS `company_id` INT(11) DEFAULT 1 AFTER `subcategory_id`,
  ADD KEY IF NOT EXISTS `idx_subcategory_company` (`company_id`);

UPDATE `subcategory` SET `company_id` = 1 WHERE `company_id` IS NULL OR `company_id` = 0;

ALTER TABLE `subcategory` 
  MODIFY COLUMN `company_id` INT(11) NOT NULL DEFAULT 1;

-- Add company_id to transaction table (all transactions belong to a company)
ALTER TABLE `transaction` 
  ADD COLUMN IF NOT EXISTS `company_id` INT(11) DEFAULT 1 AFTER `transaction_id`,
  ADD KEY IF NOT EXISTS `idx_transaction_company` (`company_id`);

-- Set company_id based on user who created the transaction
UPDATE `transaction` t
INNER JOIN user_detail u ON t.user_detail_id = u.user_detail_id
SET t.company_id = u.company_id
WHERE t.company_id IS NULL OR t.company_id = 0;

-- Default to 1 for any remaining transactions
UPDATE `transaction` SET `company_id` = 1 WHERE `company_id` IS NULL OR `company_id` = 0;

ALTER TABLE `transaction` 
  MODIFY COLUMN `company_id` INT(11) NOT NULL DEFAULT 1;

-- Add company_id to project table (if exists)
ALTER TABLE `project` 
  ADD COLUMN IF NOT EXISTS `company_id` INT(11) DEFAULT 1 AFTER `project_id`,
  ADD KEY IF NOT EXISTS `idx_project_company` (`company_id`);

UPDATE `project` SET `company_id` = 1 WHERE `company_id` IS NULL OR `company_id` = 0;

ALTER TABLE `project` 
  MODIFY COLUMN `company_id` INT(11) NOT NULL DEFAULT 1;

-- ====================================================================
-- STEP 3: Enhance login_session table for activity tracking
-- ====================================================================

ALTER TABLE `login_session` 
  ADD COLUMN IF NOT EXISTS `login_timestamp` DATETIME DEFAULT CURRENT_TIMESTAMP AFTER `session_id`,
  ADD COLUMN IF NOT EXISTS `logout_timestamp` DATETIME DEFAULT NULL AFTER `login_timestamp`,
  ADD COLUMN IF NOT EXISTS `browser_agent` VARCHAR(500) DEFAULT NULL AFTER `remote_user`;

-- Add indexes for performance
ALTER TABLE `login_session`
  ADD KEY IF NOT EXISTS `idx_login_timestamp` (`login_timestamp`),
  ADD KEY IF NOT EXISTS `idx_user_login` (`user_detail_id`, `login_timestamp`);

-- Set login_timestamp for existing records from add_date if available
UPDATE `login_session` SET `login_timestamp` = add_date WHERE `login_timestamp` IS NULL AND add_date IS NOT NULL;
UPDATE `login_session` SET `login_timestamp` = NOW() WHERE `login_timestamp` IS NULL;

-- ====================================================================
-- STEP 4: Create user_activity_log table for audit trail
-- ====================================================================

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

-- ====================================================================
-- STEP 5: Create/Update Default Registered Users Group
-- ====================================================================

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
  `group_detail_code` = 'REGISTERED_USERS',
  `group_detail_name` = 'Registered Users (Default)';

-- ====================================================================
-- STEP 6: Create Stored Procedures for Company Management
-- ====================================================================

DELIMITER $$

-- SP: Insert Company
DROP PROCEDURE IF EXISTS sp_insert_company$$
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
    company_code, company_name, tax_id, address, city, country,
    contact_person, contact_email, contact_phone, registration_date, is_active
  ) VALUES (
    p_company_code, p_company_name, p_tax_id, p_address, p_city, p_country,
    p_contact_person, p_contact_email, p_contact_phone, NOW(), 'Yes'
  );
  SELECT LAST_INSERT_ID() as company_id;
END$$

-- SP: Search Company by ID
DROP PROCEDURE IF EXISTS sp_search_company_by_id$$
CREATE PROCEDURE sp_search_company_by_id(IN p_company_id INT)
BEGIN
  SELECT * FROM company WHERE company_id = p_company_id;
END$$

-- SP: Search Company by Code
DROP PROCEDURE IF EXISTS sp_search_company_by_code$$
CREATE PROCEDURE sp_search_company_by_code(IN p_company_code VARCHAR(50))
BEGIN
  SELECT * FROM company WHERE company_code = p_company_code;
END$$

-- SP: Check if Tax ID exists
DROP PROCEDURE IF EXISTS sp_check_tax_id_exists$$
CREATE PROCEDURE sp_check_tax_id_exists(IN p_tax_id VARCHAR(100))
BEGIN
  SELECT COUNT(*) as count FROM company 
  WHERE tax_id = p_tax_id AND tax_id IS NOT NULL AND tax_id != '';
END$$

-- SP: Insert User with Company (for registration)
DROP PROCEDURE IF EXISTS sp_insert_user_with_company$$
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
    company_id, user_name, user_password, first_name, second_name, third_name,
    email_address, phone_no, trans_code, is_user_locked, is_user_gen_admin,
    user_category_id, language_system, language_output, add_date
  ) VALUES (
    p_company_id, p_user_name, p_user_password, p_first_name, 
    IFNULL(p_second_name, ''), IFNULL(p_third_name, ''),
    p_email_address, IFNULL(p_phone_no, ''), p_trans_code,
    'No', 'No', 1, p_language_system, p_language_output, NOW()
  );
  
  SET @new_user_id = LAST_INSERT_ID();
  
  -- Assign user to default "Registered Users" group
  INSERT INTO group_user (group_user_id, group_detail_id, user_detail_id)
  VALUES ((SELECT IFNULL(MAX(group_user_id), 0) + 1 FROM group_user g), 999, @new_user_id);
  
  SELECT @new_user_id as user_detail_id;
END$$

-- SP: Get User Count by Company
DROP PROCEDURE IF EXISTS sp_count_users_by_company$$
CREATE PROCEDURE sp_count_users_by_company(IN p_company_id INT)
BEGIN
  SELECT 
    COUNT(*) as total_users,
    SUM(CASE WHEN is_user_locked = 'No' THEN 1 ELSE 0 END) as active_users,
    SUM(CASE WHEN is_user_locked = 'Yes' THEN 1 ELSE 0 END) as locked_users
  FROM user_detail
  WHERE company_id = p_company_id;
END$$

-- SP: Get Recent Login Activity by Company
DROP PROCEDURE IF EXISTS sp_get_recent_login_activity$$
CREATE PROCEDURE sp_get_recent_login_activity(
  IN p_company_id INT,
  IN p_limit INT
)
BEGIN
  SELECT 
    ls.login_session_id, ls.user_detail_id, ud.user_name,
    CONCAT(ud.first_name, ' ', IFNULL(ud.second_name, '')) as full_name,
    ls.login_timestamp, ls.logout_timestamp, ls.remote_ip, ls.remote_host,
    s.store_name
  FROM login_session ls
  INNER JOIN user_detail ud ON ls.user_detail_id = ud.user_detail_id
  LEFT JOIN store s ON ls.store_id = s.store_id
  WHERE ud.company_id = p_company_id
  ORDER BY ls.login_timestamp DESC
  LIMIT p_limit;
END$$

-- SP: Get Registration Statistics by Company
DROP PROCEDURE IF EXISTS sp_get_registration_stats$$
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

-- SP: Search Users for CRM Dashboard
DROP PROCEDURE IF EXISTS sp_search_users_for_crm$$
CREATE PROCEDURE sp_search_users_for_crm(
  IN p_company_id INT,
  IN p_search_text VARCHAR(200),
  IN p_status_filter VARCHAR(10)
)
BEGIN
  SELECT 
    ud.user_detail_id, ud.user_name, ud.first_name, ud.second_name, ud.third_name,
    ud.email_address, ud.phone_no, ud.is_user_locked, ud.is_user_gen_admin, ud.add_date,
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

-- SP: Log User Activity
DROP PROCEDURE IF EXISTS sp_log_user_activity$$
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
    user_detail_id, company_id, activity_type, activity_module,
    activity_description, activity_timestamp, ip_address
  ) VALUES (
    p_user_detail_id, p_company_id, p_activity_type, p_activity_module,
    p_activity_description, NOW(), p_ip_address
  );
END$$

DELIMITER ;

-- ====================================================================
-- STEP 7: Verify Migration
-- ====================================================================

SELECT 'Company table created:' as status, COUNT(*) as count FROM company;
SELECT 'Users updated with company_id:' as status, COUNT(*) as count FROM user_detail WHERE company_id IS NOT NULL;
SELECT 'Stores updated with company_id:' as status, COUNT(*) as count FROM store WHERE company_id IS NOT NULL;
SELECT 'Items updated with company_id:' as status, COUNT(*) as count FROM item WHERE company_id IS NOT NULL;
SELECT 'Transactors updated with company_id:' as status, COUNT(*) as count FROM transactor WHERE company_id IS NOT NULL;

SELECT '✓ Migration Complete! Database vendorplus_v2 is ready for multi-tenant company-based operations.' as message;

-- ====================================================================
-- DONE: vendorplus_v2 is now a multi-tenant company-based system!
-- ====================================================================
