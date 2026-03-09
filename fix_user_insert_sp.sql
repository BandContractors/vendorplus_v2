-- Fix sp_insert_user_with_company to include edit_date field
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
    company_id, user_name, user_password, first_name, second_name, third_name,
    email_address, phone_no, trans_code, is_user_locked, is_user_gen_admin,
    user_category_id, language_system, language_output, add_date, edit_date
  ) VALUES (
    p_company_id, p_user_name, p_user_password, p_first_name, 
    IFNULL(p_second_name, ''), IFNULL(p_third_name, ''),
    p_email_address, IFNULL(p_phone_no, ''), p_trans_code, 
    'No', 'No', 1, p_language_system, p_language_output, NOW(), NOW()
  );

  SET @new_user_id = LAST_INSERT_ID();

  INSERT INTO group_user (group_user_id, group_detail_id, user_detail_id)
  VALUES ((SELECT IFNULL(MAX(group_user_id), 0) + 1 FROM group_user g), 999, @new_user_id);

  SELECT @new_user_id as user_detail_id;
END$$

DELIMITER ;
