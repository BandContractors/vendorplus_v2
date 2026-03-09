-- run all these below
DROP PROCEDURE IF EXISTS sp_get_db_timezone;
DELIMITER //
CREATE PROCEDURE sp_get_db_timezone
(
	OUT out_db_timezone varchar(100) 
)
BEGIN 
	set @db_timezone=out_db_timezone;
	set @sql_text = CONCAT("SELECT IF(@@session.time_zone = 'SYSTEM', @@system_time_zone, @@session.time_zone) INTO @db_timezone","");
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
	set out_db_timezone=@db_timezone;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_user_timezone;
DELIMITER //
CREATE PROCEDURE sp_get_user_timezone
(
	OUT out_user_timezone varchar(100) 
)
BEGIN 
	set @user_timezone=out_user_timezone;
	set @sql_text = CONCAT("SELECT time_zone INTO @user_timezone FROM company_setting WHERE company_setting_id=1","");
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
	set out_user_timezone=@user_timezone;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_convert_timezone;
DELIMITER //
CREATE PROCEDURE sp_convert_timezone
(
	IN in_from_date datetime,
	IN in_from_timezone varchar(100),
	IN in_to_timezone varchar(100),
	OUT out_to_date datetime 
)
BEGIN 
	set @to_date=out_to_date;
	set @sql_text = CONCAT("SELECT CONVERT_TZ('",in_from_date,"','",in_from_timezone,"',","'",in_to_timezone,"') INTO @to_date");
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
	set out_to_date=@to_date;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_current_user_datetime;
DELIMITER //
CREATE PROCEDURE sp_get_current_user_datetime
(
	OUT out_current_user_datetime datetime 
)
BEGIN 
	SET @dt=out_current_user_datetime;
	SET @from_tz='';
	CALL sp_get_db_timezone(@from_tz);
	SET @to_tz='';
	CALL sp_get_user_timezone(@to_tz);
	CALL sp_convert_timezone(Now(),@from_tz,@to_tz,@dt);
	set out_current_user_datetime=@dt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_current_system_datetime;
DELIMITER //
CREATE PROCEDURE sp_get_current_system_datetime
(
	OUT out_current_system_datetime datetime 
)
BEGIN 
		-- set out_current_system_datetime=Now(); 
		SET @dt=out_current_system_datetime;
		SET @from_tz='';
		CALL sp_get_db_timezone(@from_tz);
		SET @to_tz='';
		CALL sp_get_user_timezone(@to_tz);
		CALL sp_convert_timezone(Now(),@from_tz,@to_tz,@dt);
		set out_current_system_datetime=@dt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_current_system_datetime2;
DELIMITER //
CREATE PROCEDURE sp_get_current_system_datetime2
()
BEGIN 
		-- set @cur_sys_datetime=Now(); 
		SET @cur_sys_datetime=Now();
		SET @from_tz='';
		CALL sp_get_db_timezone(@from_tz);
		SET @to_tz='';
		CALL sp_get_user_timezone(@to_tz);
		CALL sp_convert_timezone(Now(),@from_tz,@to_tz,@cur_sys_datetime);
		SELECT @cur_sys_datetime AS cur_sys_datetime;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_new_card_number;
DELIMITER //
CREATE PROCEDURE sp_get_new_card_number
(
	OUT out_new_card_number varchar(10)
)
BEGIN 
	set @new_no=out_new_card_number;

	set @sql_text = concat('SELECT ',
	lpad(FLOOR(1+RAND()*9),1,'0'),
	lpad(FLOOR(10+RAND()*90),2,'0'),
	lpad(FLOOR(100+RAND()*899),3,'0'),
	lpad(FLOOR(1000+RAND()*8999),4,'0'),
	') INTO @new_no');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
	set out_new_card_number=@new_no;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_new_id;
DELIMITER //
CREATE PROCEDURE sp_get_new_id
(
	IN in_table_name varchar(50),
	IN in_id_column_name varchar(50),
	OUT out_new_id bigint
)
BEGIN 
	set @table=in_table_name;
	set @column=in_id_column_name;
	set @max_id=out_new_id;
	set @sql_text = concat('SELECT MAX(',@column,') INTO @max_id',' FROM ',@table);
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
	if (@max_id is not null) then
		set out_new_id=(@max_id+1);
	else
		set out_new_id=1;
	end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_transactor_segment;
DELIMITER //
CREATE PROCEDURE sp_get_transactor_segment
(
	IN in_transactor_id bigint,
	OUT out_transactor_segment_id int 
)
BEGIN 
	set @segid=out_transactor_segment_id;
	set @sql_text = CONCAT('SELECT ifnull(transactor_segment_id,0) as transactor_segment_id INTO @segid FROM transactor WHERE transactor_id=',in_transactor_id);
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
	set out_transactor_segment_id=@segid;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_category;
DELIMITER //
CREATE PROCEDURE sp_insert_category
(
	IN in_category_name varchar(50),
	IN in_display_quick_order varchar(50),
	IN in_list_rank varchar(50),
	IN in_store_quick_order int
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("category","category_id",@new_id);
	INSERT INTO category
	(
		category_id,
		category_name,
		display_quick_order,
		list_rank,
		store_quick_order
	) 
    VALUES
	(
		@new_id,
		in_category_name,
		in_display_quick_order,
		in_list_rank,
		in_store_quick_order
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_category;
DELIMITER //
CREATE PROCEDURE sp_update_category
(
	IN in_category_id int,
	IN in_category_name varchar(50),
	IN in_display_quick_order varchar(50),
	IN in_list_rank varchar(50),
	IN in_store_quick_order int
) 
BEGIN 
	UPDATE category SET 
		category_name=in_category_name,display_quick_order=in_display_quick_order,list_rank=in_list_rank,store_quick_order=in_store_quick_order 
	WHERE category_id=in_category_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_sub_category;
DELIMITER //
CREATE PROCEDURE sp_insert_sub_category
(
	IN in_category_id int,
	IN in_sub_category_name varchar(50)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("sub_category","sub_category_id",@new_id);
	INSERT INTO sub_category
	(
		sub_category_id,
		category_id,
		sub_category_name
	) 
    VALUES
	(
		@new_id,
		in_category_id,
		in_sub_category_name
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_sub_category;
DELIMITER //
CREATE PROCEDURE sp_update_sub_category
(
	IN in_sub_category_id int,
	IN in_category_id int,
	IN in_sub_category_name varchar(50)
) 
BEGIN 
	UPDATE sub_category SET 
		category_id=in_category_id,
		sub_category_name=in_sub_category_name
	WHERE sub_category_id=in_sub_category_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_unit;
DELIMITER //
CREATE PROCEDURE sp_insert_unit
(
	IN in_unit_name varchar(50),
	IN in_unit_symbol varchar(5),
	IN in_unit_symbol_tax varchar(50)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("unit","unit_id",@new_id);
	INSERT INTO unit
	(
		unit_id,
		unit_name,
		unit_symbol,
		unit_symbol_tax
	) 
    VALUES
	(
		@new_id,
		in_unit_name,
		in_unit_symbol,
		in_unit_symbol_tax
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_unit;
DELIMITER //
CREATE PROCEDURE sp_update_unit
(
	IN in_unit_id int,
	IN in_unit_name varchar(50),
	IN in_unit_symbol varchar(5),
	IN in_unit_symbol_tax varchar(50)
) 
BEGIN 
	UPDATE unit SET 
		unit_name=in_unit_name,
		unit_symbol=in_unit_symbol,
		unit_symbol_tax=in_unit_symbol_tax 
	WHERE unit_id=in_unit_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_store;
DELIMITER //
CREATE PROCEDURE sp_insert_store
(
	IN in_store_name varchar(20),
	IN in_store_code varchar(10),
    IN in_shift_mode int
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("store","store_id",@new_id);
	INSERT INTO store
	(
		store_id,
		store_name,
		store_code,
        shift_mode
	) 
    VALUES
	(
		@new_id,
		in_store_name,
		in_store_code,
        in_shift_mode
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_store;
DELIMITER //
CREATE PROCEDURE sp_update_store
(
	IN in_store_id int,
	IN in_store_name varchar(20),
	IN in_store_code varchar(10),
    IN in_shift_mode int
) 
BEGIN 
	UPDATE store SET store_name=in_store_name,store_code=in_store_code,shift_mode=in_shift_mode 
	WHERE store_id=in_store_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_store_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_store_by_id
(
	IN in_store_id int
) 
BEGIN 
	SELECT * FROM store 
	WHERE store_id=in_store_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_store_by_name_equal;
DELIMITER //
CREATE PROCEDURE sp_search_store_by_name_equal
(
	IN in_store_name varchar(20)
) 
BEGIN 
	SELECT * FROM store 
	WHERE store_name=in_store_name; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_store_by_name;
DELIMITER //
CREATE PROCEDURE sp_search_store_by_name
(
	IN in_store_name varchar(20)
) 
BEGIN 
	SELECT * FROM store 
	WHERE store_name LIKE concat('%',in_store_name,'%') 
	ORDER BY store_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_store_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_store_by_none() 
BEGIN 
	SELECT * FROM store ORDER BY store_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_store_by_user_detail;
DELIMITER //
CREATE PROCEDURE sp_search_store_by_user_detail
(
	IN in_user_detail_id int
) 
BEGIN 
	SELECT * FROM store WHERE store_id IN(
		SELECT gr.store_id FROM group_right gr WHERE gr.group_detail_id IN(
			SELECT gu.group_detail_id FROM group_user gu WHERE gu.user_detail_id=in_user_detail_id
		) AND (allow_view='Yes' or allow_add='Yes' or allow_edit='Yes' or allow_delete='Yes')
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_user_detail;
DELIMITER //
CREATE PROCEDURE sp_insert_user_detail
(
	IN in_user_name varchar(20),
	IN in_user_password varchar(255),
	IN in_first_name varchar(100),
	IN in_second_name varchar(100),
	IN in_third_name varchar(100),
	IN in_is_user_locked varchar(3),
	IN in_is_user_gen_admin varchar(3),
	IN in_user_category_id int,
	IN in_user_img_url varchar(255),
	IN in_email_address varchar(100),
	IN in_phone_no varchar(50),
	IN in_trans_code varchar(255),
    IN in_language_system varchar(50),
    IN in_language_output varchar(50)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("user_detail","user_detail_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	INSERT INTO user_detail
	(
		user_detail_id,
		user_name,
		user_password,
		first_name,
		second_name,
		third_name,
		is_user_locked,
		is_user_gen_admin,
		user_category_id,
		add_date,
		edit_date,
		user_img_url,
		email_address,
		phone_no,
		trans_code,
        language_system,
		language_output
	) 
    VALUES
	(
		@new_id,
		in_user_name,
		in_user_password,
		in_first_name,
		in_second_name,
		in_third_name,
		in_is_user_locked,
		in_is_user_gen_admin,
		in_user_category_id,
		@cur_sys_datetime,
		@cur_sys_datetime,
		in_user_img_url,
		in_email_address,
		in_phone_no,
		in_trans_code,
        in_language_system,
		in_language_output
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_user_detail;
DELIMITER //
CREATE PROCEDURE sp_update_user_detail
(
	IN in_user_detail_id int,
	IN in_user_name varchar(20),
	IN in_user_password varchar(255),
	IN in_first_name varchar(100),
	IN in_second_name varchar(100),
	IN in_third_name varchar(100),
	IN in_is_user_locked varchar(3),
	IN in_is_user_gen_admin varchar(3),
	IN in_user_category_id int,
	IN in_user_img_url varchar(255),
	IN in_email_address varchar(100),
	IN in_phone_no varchar(50),
	IN in_trans_code varchar(255),
    IN in_language_system varchar(50),
    IN in_language_output varchar(50)
) 
BEGIN 

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	UPDATE user_detail SET 
		user_name=in_user_name,
		user_password=in_user_password,
		first_name=in_first_name,
		second_name=in_second_name,
		third_name=in_third_name,
		is_user_locked=in_is_user_locked,
		is_user_gen_admin=in_is_user_gen_admin,
		user_category_id=in_user_category_id,
		edit_date=@cur_sys_datetime,
		user_img_url=in_user_img_url,
		email_address=in_email_address,
		phone_no=in_phone_no,
		trans_code=in_trans_code,
        language_system=in_language_system,
		language_output=in_language_output 
	WHERE user_detail_id=in_user_detail_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_user_detail_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_user_detail_by_id
(
	IN in_user_detail_id int
) 
BEGIN 
	SELECT * FROM user_detail 
	WHERE user_detail_id=in_user_detail_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_user_detail_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_user_detail_by_none
() 
BEGIN 
	SELECT * FROM user_detail ORDER BY user_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_user_detail_by_names;
DELIMITER //
CREATE PROCEDURE sp_search_user_detail_by_names
(
	IN in_names varchar(100) 
) 
BEGIN 
	SELECT * FROM user_detail 
	WHERE user_name LIKE concat('%', in_names, '%') 
	OR first_name LIKE concat('%', in_names, '%') 
	OR second_name LIKE concat('%', in_names, '%') 
	OR third_name LIKE concat('%', in_names, '%') 
	ORDER BY first_name,second_name,third_name ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_user_detail_by_names_active;
DELIMITER //
CREATE PROCEDURE sp_search_user_detail_by_names_active
(
	IN in_names varchar(100) 
) 
BEGIN 
	SELECT * FROM user_detail 
	WHERE 
	(is_user_locked='No' AND user_name<>'system') AND 
	(
	user_name LIKE concat('%', in_names, '%') 
	OR first_name LIKE concat('%', in_names, '%') 
	OR second_name LIKE concat('%', in_names, '%') 
	OR third_name LIKE concat('%', in_names, '%')
	)
	ORDER BY first_name,second_name,third_name ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_user_detail_by_username;
DELIMITER //
CREATE PROCEDURE sp_search_user_detail_by_username
(
	IN in_user_name varchar(20) 
) 
BEGIN 
	SELECT * FROM user_detail 
	WHERE user_name=in_user_name;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_user_detail_all_active;
DELIMITER //
CREATE PROCEDURE sp_search_user_detail_all_active() 
BEGIN 
	SELECT COUNT(user_detail_id) as total_user_count 
	FROM user_detail 
	WHERE is_user_locked='No' AND user_name<>'system';
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_user_detail_not_locked;
DELIMITER //
CREATE PROCEDURE sp_search_user_detail_not_locked() 
BEGIN 
	SELECT * FROM user_detail 
	WHERE is_user_locked='No' AND user_name<>'system';
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_user_right;
DELIMITER //
CREATE PROCEDURE sp_insert_user_right
(
	IN in_store_id int,
	IN in_user_detail_id int,
	IN in_function_name varchar(50),
	IN in_allow_view varchar(3),
	IN in_allow_add varchar(3),
	IN in_allow_edit varchar(3),
	IN in_allow_delete varchar(3)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("user_right","user_right_id",@new_id);
	INSERT INTO user_right
	(
		user_right_id,
		store_id,
		user_detail_id,
		function_name,
		allow_view,
		allow_add,
		allow_edit,
		allow_delete
	) 
    VALUES
	(
		@new_id,
		in_store_id,
		in_user_detail_id,
		in_function_name,
		in_allow_view,
		in_allow_add,
		in_allow_edit,
		in_allow_delete
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_user_right;
DELIMITER //
CREATE PROCEDURE sp_update_user_right
(
	IN in_user_right_id int,
	IN in_store_id int,
	IN in_user_detail_id int,
	IN in_function_name varchar(50),
	IN in_allow_view varchar(3),
	IN in_allow_add varchar(3),
	IN in_allow_edit varchar(3),
	IN in_allow_delete varchar(3)
) 
BEGIN 
	UPDATE user_right SET 
		store_id=in_store_id,
		user_detail_id=in_user_detail_id,
		function_name=in_function_name,
		allow_view=in_allow_view,
		allow_add=in_allow_add,
		allow_edit=in_allow_edit,
		allow_delete=in_allow_delete
	WHERE user_right_id=in_user_right_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_item;
DELIMITER //
CREATE PROCEDURE sp_insert_item
(
	IN in_item_code varchar(50),
	IN in_description varchar(100),
	IN in_category_id int,
	IN in_sub_category_id integer,
	IN in_unit_id int,
	IN in_reorder_level double,
	IN in_unit_retailsale_price double,
	IN in_unit_wholesale_price double,
	IN in_is_suspended varchar(3),
	IN in_vat_rated varchar(50),
	IN in_item_img_url varchar(100),
	IN in_item_type varchar(7),
	IN in_currency_code varchar(10),
	IN in_is_general int,
	IN in_asset_type varchar(50),
	IN in_is_buy int,
	IN in_is_sale int,
	IN in_is_track int,
	IN in_is_asset int,
	IN in_asset_account_code varchar(20),
	IN in_expense_account_code varchar(20),
	IN in_is_hire int,
	IN in_duration_type varchar(20),
	IN in_unit_hire_price double,
	IN in_unit_special_price double,
	IN in_unit_weight double,
	IN in_unit_cost_price double,
	IN in_expense_type varchar(50),
	IN in_alias_name varchar(50),
	In in_display_alias_name int,
	IN in_is_free int,
	IN in_specify_size int,
	IN in_size_to_specific_name int,
	IN in_expiry_band varchar(50),
	IN in_override_gen_name int,
	IN in_hide_unit_price_invoice int
) 
BEGIN 
	SET @new_id=0;
	SET @sub_category_id=NULL;
	if (in_sub_category_id!=0) then
		set @sub_category_id=in_sub_category_id;
	end if;
	CALL sp_get_new_id("item","item_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @currency_code=NULL;
	if (in_currency_code!='') then
		set @currency_code=in_currency_code;
	end if;
	SET @is_general=NULL;
	if (in_is_general!=0) then
		set @is_general=in_is_general;
	end if;
	SET @asset_type=NULL;
	if (in_asset_type!='') then
		set @asset_type=in_asset_type;
	end if;
	SET @in_asset_account_code=NULL;
	if (in_asset_account_code!='') then
		set @in_asset_account_code=in_asset_account_code;
	end if;
	SET @in_expense_account_code=NULL;
	if (in_expense_account_code!='') then
		set @in_expense_account_code=in_expense_account_code;
	end if;

	INSERT INTO item
	(
		item_id,
		item_code,
		description,
		category_id,
		sub_category_id,
		unit_id,
		reorder_level,
		unit_retailsale_price,
		unit_wholesale_price,
		is_suspended,
		vat_rated,
		item_img_url,
		add_date,
		edit_date,
		item_type,
		currency_code,
		is_general,
		asset_type,
		is_buy,
		is_sale,
		is_track,
		is_asset,
		asset_account_code,
		expense_account_code,
		is_hire,
		duration_type,
		unit_hire_price,
		unit_special_price,
		unit_weight,
		unit_cost_price,
		expense_type,
		alias_name,
		display_alias_name,
		is_free,
		specify_size,
		size_to_specific_name,
		expiry_band,
		override_gen_name,
		hide_unit_price_invoice
	) 
    VALUES
	(
		@new_id,
		in_item_code,
		in_description,
		in_category_id,
		@sub_category_id,
		in_unit_id,
		in_reorder_level,
		in_unit_retailsale_price,
		in_unit_wholesale_price,
		in_is_suspended,
		in_vat_rated,
		in_item_img_url,
		@cur_sys_datetime,
		@cur_sys_datetime,
		in_item_type,
		@currency_code,
		@is_general,
		@asset_type,
		in_is_buy,
		in_is_sale,
		in_is_track,
		in_is_asset,
		@in_asset_account_code,
		@in_expense_account_code,
		in_is_hire,
		in_duration_type,
		in_unit_hire_price,
		in_unit_special_price,
		in_unit_weight,
		in_unit_cost_price,
		in_expense_type,
		in_alias_name,
		in_display_alias_name,
		in_is_free,
		in_specify_size,
		in_size_to_specific_name,
		in_expiry_band,
		in_override_gen_name,
		in_hide_unit_price_invoice
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_item;
DELIMITER //
CREATE PROCEDURE sp_update_item
(
	IN in_item_id int,
	IN in_item_code varchar(50),
	IN in_description varchar(100),
	IN in_category_id int,
	IN in_sub_category_id integer,
	IN in_unit_id int,
	IN in_reorder_level double,
	IN in_unit_retailsale_price double,
	IN in_unit_wholesale_price double,
	IN in_is_suspended varchar(3),
	IN in_vat_rated varchar(50),
	IN in_item_img_url varchar(100),
	IN in_item_type varchar(7),
	IN in_currency_code varchar(10),
	IN in_is_general int,
	IN in_asset_type varchar(50),
	IN in_is_buy int,
	IN in_is_sale int,
	IN in_is_track int,
	IN in_is_asset int,
	IN in_asset_account_code varchar(20),
	IN in_expense_account_code varchar(20),
	IN in_is_hire int,
	IN in_duration_type varchar(20),
	IN in_unit_hire_price double,
	IN in_unit_special_price double,
	IN in_unit_weight double,
	IN in_unit_cost_price double,
	IN in_expense_type varchar(50),
	IN in_alias_name varchar(50),
	IN in_display_alias_name int,
	IN in_is_free int,
	IN in_specify_size int,
	IN in_size_to_specific_name int,
	IN in_expiry_band varchar(50),
	IN in_override_gen_name int,
	IN in_hide_unit_price_invoice int
) 
BEGIN 
	SET @sub_category_id=NULL;
	if (in_sub_category_id!=0) then
		set @sub_category_id=in_sub_category_id;
	end if;

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @currency_code=NULL;
	if (in_currency_code!='') then
		set @currency_code=in_currency_code;
	end if;
	SET @is_general=NULL;
	if (in_is_general!=0) then
		set @is_general=in_is_general;
	end if;
	SET @asset_type=NULL;
	if (in_asset_type!='') then
		set @asset_type=in_asset_type;
	end if;
	SET @in_asset_account_code=NULL;
	if (in_asset_account_code!='') then
		set @in_asset_account_code=in_asset_account_code;
	end if;
	SET @in_expense_account_code=NULL;
	if (in_expense_account_code!='') then
		set @in_expense_account_code=in_expense_account_code;
	end if;

	UPDATE item SET 
		item_code=in_item_code,
		description=in_description,
		category_id=in_category_id,
		sub_category_id=@sub_category_id,
		unit_id=in_unit_id,
		reorder_level=in_reorder_level,
		unit_retailsale_price=in_unit_retailsale_price,
		unit_wholesale_price=in_unit_wholesale_price,
		is_suspended=in_is_suspended,
		vat_rated=in_vat_rated,
		item_img_url=in_item_img_url,
		edit_date=@cur_sys_datetime,
		item_type=in_item_type,
		currency_code=@currency_code,
		is_general=@is_general,
		asset_type=@asset_type,
		is_buy=in_is_buy,
		is_sale=in_is_sale,
		is_track=in_is_track,
		is_asset=in_is_asset,
		asset_account_code=@in_asset_account_code,
		expense_account_code=@in_expense_account_code,
		is_hire=in_is_hire,
		duration_type=in_duration_type,
		unit_hire_price=in_unit_hire_price,
		unit_special_price=in_unit_special_price,
		unit_weight=in_unit_weight,
		unit_cost_price=in_unit_cost_price,
		expense_type=in_expense_type,
		alias_name=in_alias_name,
		display_alias_name=in_display_alias_name,
		is_free=in_is_free,
		specify_size=in_specify_size,
		size_to_specific_name=in_size_to_specific_name,
		expiry_band=in_expiry_band,
		override_gen_name=in_override_gen_name,
		hide_unit_price_invoice=in_hide_unit_price_invoice 
	WHERE item_id=in_item_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_pay_method;
DELIMITER //
CREATE PROCEDURE sp_insert_pay_method
(
	IN in_pay_method_name varchar(50),
	IN in_display_order int,
	IN in_is_default int,
	IN in_is_active int,
	IN in_is_deleted int
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("pay_method","pay_method_id",@new_id);
	INSERT INTO pay_method
	(
		pay_method_id,
		pay_method_name,
		display_order,
		is_default,
		is_active,
		is_deleted
	) 
    VALUES
	(
		@new_id,
		in_pay_method_name,
		in_display_order,
		in_is_default,
		in_is_active,
		in_is_deleted
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_pay_method;
DELIMITER //
CREATE PROCEDURE sp_update_pay_method
(
	IN in_pay_method_id int,
	IN in_pay_method_name varchar(50),
	IN in_display_order int,
	IN in_is_default int,
	IN in_is_active int,
	IN in_is_deleted int
) 
BEGIN 
	UPDATE pay_method SET 
		pay_method_name=in_pay_method_name,
		display_order=in_display_order,
		is_default=in_is_default,
		is_active=in_is_active,
		is_deleted=in_is_deleted 
	WHERE pay_method_id=in_pay_method_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_type;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_type
(
	IN in_transaction_type_name varchar(50)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("transaction_type","transaction_type_id",@new_id);
	INSERT INTO transaction_type
	(
		transaction_type_id,
		transaction_type_name
	) 
    VALUES
	(
		@new_id,
		in_transaction_type_name
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_transaction_type;
DELIMITER //
CREATE PROCEDURE sp_update_transaction_type
(
	IN in_transaction_type_id int,
	IN in_transaction_type_name varchar(50),
	IN in_transactor_label varchar(50),
	IN in_transaction_number_label varchar(50),
	IN in_transaction_output_label varchar(50),
	IN in_bill_transactor_label varchar(50),
	IN in_transaction_ref_label varchar(50),
	IN in_transaction_date_label varchar(50),
	IN in_transaction_user_label varchar(50),
	IN in_is_transactor_mandatory varchar(3),
	IN in_is_transaction_user_mandatory varchar(3),
	IN in_is_transaction_ref_mandatory varchar(3),
	IN in_is_authorise_user_mandatory varchar(3),
	IN in_is_authorise_date_mandatory varchar(3),
	IN in_is_delivery_address_mandatory varchar(3),
	IN in_is_delivery_date_mandatory varchar(3),
	IN in_is_pay_due_date_mandatory varchar(3),
	IN in_is_expiry_date_mandatory varchar(3),
	IN in_description varchar(100),
	IN in_group_name varchar(50),
	IN in_print_file_name1 varchar(50),
	IN in_print_file_name2 varchar(50),
	IN in_default_print_file int,
	IN in_transaction_type_code varchar(4),
	IN in_default_currency_code varchar(10),
	IN in_trans_number_format varchar(20),
	IN in_output_footer_message varchar(250),
	IN in_default_term_condition varchar(250)
) 
BEGIN 
	UPDATE transaction_type SET 
		transactor_label=in_transactor_label,
		transaction_number_label=in_transaction_number_label,
		transaction_output_label=in_transaction_output_label,
		bill_transactor_label=in_bill_transactor_label,
		transaction_ref_label=in_transaction_ref_label,
		transaction_date_label=in_transaction_date_label,
		transaction_user_label=in_transaction_user_label,
		is_transactor_mandatory=in_is_transactor_mandatory,
		is_transaction_user_mandatory=in_is_transaction_user_mandatory,
		is_transaction_ref_mandatory=in_is_transaction_ref_mandatory,
		is_authorise_user_mandatory=in_is_authorise_user_mandatory,
		is_authorise_date_mandatory=in_is_authorise_date_mandatory,
		is_delivery_address_mandatory=in_is_delivery_address_mandatory,
		is_delivery_date_mandatory=in_is_delivery_date_mandatory,
		is_pay_due_date_mandatory=in_is_pay_due_date_mandatory,
		is_expiry_date_mandatory =in_is_expiry_date_mandatory,
		description=in_description,
		group_name=in_group_name,
		print_file_name1=in_print_file_name1,
		print_file_name2=in_print_file_name2,
		default_print_file=in_default_print_file,
		transaction_type_code=in_transaction_type_code,
		default_currency_code=in_default_currency_code,
		trans_number_format=in_trans_number_format,
		output_footer_message=in_output_footer_message,
		default_term_condition=in_default_term_condition 
	WHERE transaction_type_id=in_transaction_type_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_reason;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_reason
(
	IN in_transaction_type_id int,
	IN in_transaction_reason_name varchar(50)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("transaction_reason","transaction_reason_id",@new_id);
	INSERT INTO transaction_reason
	(
		transaction_reason_id,
		transaction_type_id,
		transaction_reason_name
	) 
    VALUES
	(
		@new_id,
		in_transaction_type_id,
		in_transaction_reason_name
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_transaction_reason;
DELIMITER //
CREATE PROCEDURE sp_update_transaction_reason
(
	IN in_transaction_reason_id int,
	IN in_transaction_type_id int,
	IN in_transaction_reason_name varchar(50)
) 
BEGIN 
	UPDATE transaction_reason SET 
		transaction_type_id=in_transaction_type_id,
		transaction_reason_name=in_transaction_reason_name
	WHERE transaction_reason_id=in_transaction_reason_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transactor;
DELIMITER //
CREATE PROCEDURE sp_insert_transactor
(
	IN in_transactor_type varchar(20),
	IN in_transactor_names varchar(100),
	IN in_phone varchar(100),
	IN in_email varchar(100),
	IN in_website varchar(100),
	IN in_cpname varchar(100),
	IN in_cptitle varchar(100),
	IN in_cpphone varchar(100),
	IN in_cpemail varchar(100),
	IN in_physical_address varchar(255),
	IN in_tax_identity varchar(100),
	IN in_account_details varchar(255),
	IN in_card_number varchar(10),
	IN in_dob date,
	IN in_is_suspended varchar(3),
	IN in_suspended_reason varchar(50),
	IN in_category varchar(20),
	IN in_sex varchar(10),
	IN in_occupation varchar(50),
	IN in_loc_country varchar(100),
	IN in_loc_district varchar(100),
	IN in_loc_town varchar(100),
	IN in_first_date date,
	IN in_file_reference varchar(100),
	IN in_id_type varchar(50),
	IN in_id_number varchar(50),
	IN in_id_expiry_date date,
	IN in_transactor_ref varchar(50),
	IN in_title varchar(50),
	IN in_position varchar(50),
	IN in_month_gross_pay double,
	IN in_month_net_pay double,
	IN in_store_id int,
	IN in_transactor_segment_id int,
    IN in_is_credit_limit int(1),
	IN in_credit_limit double,
	OUT out_transactor_id bigint,
    IN in_trade_name varchar(100)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("transactor","transactor_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @in_dob=NULL;
	if (in_dob is not null) then
		set @in_dob=in_dob;
	end if;

	SET @in_transactor_segment_id=NULL;
	if (in_transactor_segment_id!=0) then
		set @in_transactor_segment_id=in_transactor_segment_id;
	end if;

	INSERT INTO transactor
	(
		transactor_id,
		transactor_type,
		transactor_names,
		phone,
		email,
		website,
		cpname,
		cptitle,
		cpphone,
		cpemail,
		physical_address,
		tax_identity,
		account_details,
		card_number,
		add_date,
		edit_date,
		dob,
		is_suspended,
		suspended_reason,
		category,
		sex,
		occupation,
		loc_country,
		loc_district,
		loc_town,
		first_date,
		file_reference,
		id_type,
		id_number,
		id_expiry_date,
		transactor_ref,
		title,
		position,
		month_gross_pay,
		month_net_pay,
		store_id,
		transactor_segment_id,
        is_credit_limit,
		credit_limit,
        trade_name
	) 
    VALUES
	(
		@new_id,
		in_transactor_type,
		in_transactor_names,
		in_phone,
		in_email,
		in_website,
		in_cpname,
		in_cptitle,
		in_cpphone,
		in_cpemail,
		in_physical_address,
		in_tax_identity,
		in_account_details,
		in_card_number,
		@cur_sys_datetime,
		@cur_sys_datetime,
		@in_dob,
		in_is_suspended,
		in_suspended_reason,
		in_category,
		in_sex,
		in_occupation,
		in_loc_country,
		in_loc_district,
		in_loc_town,
		in_first_date,
		in_file_reference,
		in_id_type,
		in_id_number,
		in_id_expiry_date,
		in_transactor_ref,
		in_title,
		in_position,
		in_month_gross_pay,
		in_month_net_pay,
		in_store_id,
		in_transactor_segment_id,
        in_is_credit_limit,
		in_credit_limit,
        in_trade_name
	); 
SET out_transactor_id=@new_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_transactor;
DELIMITER //
CREATE PROCEDURE sp_update_transactor
(
	IN in_transactor_id bigint,
	IN in_transactor_type varchar(20),
	IN in_transactor_names varchar(100),
	IN in_phone varchar(100),
	IN in_email varchar(100),
	IN in_website varchar(100),
	IN in_cpname varchar(100),
	IN in_cptitle varchar(100),
	IN in_cpphone varchar(100),
	IN in_cpemail varchar(100),
	IN in_physical_address varchar(255),
	IN in_tax_identity varchar(100),
	IN in_account_details varchar(255),
	IN in_card_number varchar(10),
	IN in_dob date,
	IN in_is_suspended varchar(3),
	IN in_suspended_reason varchar(50),
	IN in_category varchar(20),
	IN in_sex varchar(10),
	IN in_occupation varchar(50),
	IN in_loc_country varchar(100),
	IN in_loc_district varchar(100),
	IN in_loc_town varchar(100),
	IN in_first_date date,
	IN in_file_reference varchar(100),
	IN in_id_type varchar(50),
	IN in_id_number varchar(50),
	IN in_id_expiry_date date,
	IN in_transactor_ref varchar(50),
	IN in_title varchar(50),
	IN in_position varchar(50),
	IN in_month_gross_pay double,
	IN in_month_net_pay double,
	IN in_transactor_segment_id int,
    IN in_is_credit_limit int(1),
	IN in_credit_limit double,
    IN in_trade_name varchar(100)
) 
BEGIN 

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @in_dob=NULL;
	if (in_dob is not null) then
		set @in_dob=in_dob;
	end if;

	SET @in_transactor_segment_id=NULL;
	if (in_transactor_segment_id!=0) then
		set @in_transactor_segment_id=in_transactor_segment_id;
	end if;

	UPDATE transactor SET 
		transactor_type=in_transactor_type,
		transactor_names=in_transactor_names,
		phone=in_phone,
		email=in_email,
		website=in_website,
		cpname=in_cpname,
		cptitle=in_cptitle,
		cpphone=in_cpphone,
		cpemail=in_cpemail,
		physical_address=in_physical_address,
		tax_identity=in_tax_identity,
		account_details=in_account_details,
		card_number=in_card_number,
		edit_date=@cur_sys_datetime,
		dob=@in_dob,
		is_suspended=in_is_suspended,
		suspended_reason=in_suspended_reason,
		category=in_category,
		sex=in_sex,
		occupation=in_occupation,
		loc_country=in_loc_country,
		loc_district=in_loc_district,
		loc_town=in_loc_town,
		first_date=in_first_date,
		file_reference=in_file_reference,
		id_type=in_id_type,
		id_number=in_id_number,
		id_expiry_date=in_id_expiry_date,
		transactor_ref=in_transactor_ref,
		title=in_title,
		position=in_position,
		month_gross_pay=in_month_gross_pay,
		month_net_pay=in_month_net_pay,
		transactor_segment_id=in_transactor_segment_id,
        is_credit_limit=in_is_credit_limit,
		credit_limit=in_credit_limit,
        trade_name=in_trade_name 
	WHERE transactor_id=in_transactor_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transactor_by_name;
DELIMITER //
CREATE PROCEDURE sp_search_transactor_by_name
(
	IN in_transactor_names varchar(100) 
) 
BEGIN 
	SELECT * FROM transactor t 
	WHERE t.transactor_names LIKE concat('%',in_transactor_names,'%') OR t.transactor_ref LIKE concat('%',in_transactor_names,'%') OR t.file_reference LIKE concat('%',in_transactor_names,'%')
	ORDER BY t.transactor_names ASC LIMIT 10; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transactor_by_loc_district;
DELIMITER //
CREATE PROCEDURE sp_search_transactor_by_loc_district
(
	IN in_loc_district varchar(100) 
) 
BEGIN 
	SELECT * FROM transactor t 
	WHERE t.loc_district = in_loc_district
	ORDER BY t.loc_district ASC LIMIT 10; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transactor_by_tax_identity;
DELIMITER //
CREATE PROCEDURE sp_search_transactor_by_tax_identity
(
	IN in_tax_identity varchar(100) 
) 
BEGIN 
	SELECT * FROM transactor t 
	WHERE t.tax_identity = in_tax_identity
	ORDER BY t.tax_identity ASC LIMIT 10; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transactor_by_transactor_ref;
DELIMITER //
CREATE PROCEDURE sp_search_transactor_by_transactor_ref
(
	IN in_transactor_ref varchar(20) 
) 
BEGIN 
	SELECT * FROM transactor t 
	WHERE t.transactor_ref = in_transactor_ref
	ORDER BY t.transactor_ref ASC LIMIT 10; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transactor_by_names_equal;
DELIMITER //
CREATE PROCEDURE sp_search_transactor_by_names_equal
(
	IN in_transactor_names varchar(20) 
) 
BEGIN 
	SELECT * FROM transactor t 
	WHERE t.transactor_names = in_transactor_names; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transactor_active_by_name;
DELIMITER //
CREATE PROCEDURE sp_search_transactor_active_by_name
(
	IN in_transactor_names varchar(100) 
) 
BEGIN 
	SELECT * FROM transactor t 
	WHERE t.is_suspended='No' AND (t.transactor_names LIKE concat('%',in_transactor_names,'%') OR t.trade_name LIKE concat('%',in_transactor_names,'%') OR t.transactor_ref LIKE concat('%',in_transactor_names,'%') OR t.file_reference LIKE concat('%',in_transactor_names,'%')) 
	ORDER BY t.transactor_names ASC LIMIT 10; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transactor_by_name_ref_file;
DELIMITER //
CREATE PROCEDURE sp_search_transactor_by_name_ref_file
(
	IN in_text varchar(100),
	IN in_type varchar(100)
) 
BEGIN 
	SELECT * FROM transactor t 
	WHERE t.transactor_type=in_type and (t.transactor_names LIKE concat('%',in_text,'%') OR t.trade_name LIKE concat('%',in_text,'%') OR t.transactor_ref LIKE concat('%',in_text,'%') OR t.file_reference LIKE concat('%',in_text,'%')) 
	ORDER BY t.transactor_names ASC LIMIT 10; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transactor_by_name_type;
DELIMITER //
CREATE PROCEDURE sp_search_transactor_by_name_type
(
	IN in_transactor_names varchar(100),
	IN in_transactor_type varchar(20)
) 
BEGIN 
	SELECT * FROM transactor t 
	WHERE t.transactor_type=in_transactor_type AND 
	t.transactor_names LIKE concat('%',in_transactor_names,'%') 
	ORDER BY t.transactor_names ASC LIMIT 10; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transactor_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_transactor_by_id
(
	IN in_transactor_id bigint
) 
BEGIN 
	SELECT * FROM transactor t 
	WHERE t.transactor_id=in_transactor_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_item;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_item
(
	IN in_transaction_id bigint,
	IN in_item_id bigint,
	IN in_batchno varchar(100),
	IN in_item_qty double,
	IN in_unit_price double,
	IN in_unit_trade_discount double,
	IN in_unit_vat double,
	IN in_amount double,
	IN in_item_expiry_date date,
	IN in_item_mnf_date date,
	IN in_vat_rated varchar(50),
	IN in_vat_perc double,
	IN in_unit_price_inc_vat double,
	IN in_unit_price_exc_vat double,
	IN in_amount_inc_vat double,
	IN in_amount_exc_vat double,
	IN in_stock_effect varchar(1),
	IN in_is_trade_discount_vat_liable varchar(3),
	IN in_unit_cost_price double,
	IN in_unit_profit_margin double,
	IN in_earn_perc double,
	IN in_earn_amount double,
	IN in_code_specific varchar(250),
	IN in_desc_specific varchar(250),
	IN in_desc_more varchar(250),
	IN in_warranty_desc varchar(150),
	IN in_warranty_expiry_date date,
	IN in_account_code varchar(20),
	IN in_purchase_date date,
	IN in_dep_start_date date,
	IN in_dep_method_id int,
	IN in_dep_rate double,
	IN in_average_method_id int,
	IN in_effective_life int,
	IN in_residual_value double,
	IN in_narration varchar(100),
	IN in_qty_balance double,
	IN in_duration_value double,
	IN in_qty_damage double,
	IN in_duration_passed double,
	IN in_specific_size double
) 
BEGIN 
	-- SET @new_id=0;
	-- CALL sp_get_new_id("transaction_item","transaction_item_id",@new_id);

	SET @item_expiry_date=NULL;
	if (in_item_expiry_date is not null) then
		set @item_expiry_date=in_item_expiry_date;
	end if;

	SET @item_mnf_date=NULL;
	if (in_item_mnf_date is not null) then
		set @item_mnf_date=in_item_mnf_date;
	end if;

	SET @in_batchno='';
	if (in_batchno is not null) then
		set @in_batchno=in_batchno;
	end if;
	SET @code_specific='';
	if (in_code_specific is not null) then
		set @code_specific=in_code_specific;
	end if;
	SET @desc_specific='';
	if (in_desc_specific is not null) then
		set @desc_specific=in_desc_specific;
	end if;
	SET @desc_more=null;
	if (in_desc_more!='' and in_desc_more is not null) then
		set @desc_more=in_desc_more;
	end if;
	SET @warranty_desc=null;
	if (in_warranty_desc!="" and in_warranty_desc is not null) then
		set @warranty_desc=in_warranty_desc;
	end if;
	SET @warranty_expiry_date=NULL;
	if (in_warranty_expiry_date is not null) then
		set @warranty_expiry_date=in_warranty_expiry_date;
	end if;
	SET @account_code=null;
	if (in_account_code!='' and in_account_code is not null) then
		set @account_code=in_account_code;
	end if;
		SET @in_purchase_date=NULL;
		if (in_purchase_date is not null) then
			set @in_purchase_date=in_purchase_date;
		end if;
		SET @in_dep_start_date=NULL;
		if (in_dep_start_date is not null) then
			set @in_dep_start_date=in_dep_start_date;
		end if;
		SET @in_dep_method_id=NULL;
		if (in_dep_method_id!=0) then
			set @in_dep_method_id=in_dep_method_id;
		end if;
		SET @in_average_method_id=NULL;
		if (in_average_method_id!=0) then
			set @in_average_method_id=in_average_method_id;
		end if;
		SET @in_item_id=NULL;
		if (in_item_id!=0) then
			set @in_item_id=in_item_id;
		end if;
		SET @in_specific_size=1;
		if (in_specific_size!=0) then
			set @in_specific_size=in_specific_size;
		end if;

	INSERT INTO transaction_item
	(
		transaction_id,
		item_id,
		batchno,
		item_qty,
		unit_price,
		unit_trade_discount,
		unit_vat,
		amount,
		item_expiry_date,
		item_mnf_date,
		vat_rated,
		vat_perc,
		unit_price_inc_vat,
		unit_price_exc_vat,
		amount_inc_vat,
		amount_exc_vat,
		stock_effect,
		is_trade_discount_vat_liable,
		unit_cost_price,
		unit_profit_margin,
		earn_perc,
		earn_amount,
		code_specific,
		desc_specific,
		desc_more,
		warranty_desc,
		warranty_expiry_date,
		account_code,
		purchase_date,
		dep_start_date,
		dep_method_id,
		dep_rate,
		average_method_id,
		effective_life,
		residual_value,
		narration,
		qty_balance,
		duration_value,
		qty_damage,
		duration_passed,
		specific_size
	) 
    VALUES
	(
		in_transaction_id,
		@in_item_id,
		@in_batchno,
		in_item_qty,
		in_unit_price,
		in_unit_trade_discount,
		in_unit_vat,
		in_amount,
		@item_expiry_date,
		@item_mnf_date,
		in_vat_rated,
		in_vat_perc,
		in_unit_price_inc_vat,
		in_unit_price_exc_vat,
		in_amount_inc_vat,
		in_amount_exc_vat,
		in_stock_effect,
		in_is_trade_discount_vat_liable,
		in_unit_cost_price,
		in_unit_profit_margin,
		in_earn_perc,
		in_earn_amount,
		@code_specific,
		@desc_specific,
		@desc_more,
		@warranty_desc,
		@warranty_expiry_date,
		@account_code,
		@in_purchase_date,
		@in_dep_start_date,
		@in_dep_method_id,
		in_dep_rate,
		@in_average_method_id,
		in_effective_life,
		in_residual_value,
		in_narration,
		in_qty_balance,
		in_duration_value,
		in_qty_damage,
		in_duration_passed,
		@in_specific_size
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_item_out;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_item_out
(
	IN in_transaction_id bigint,
	IN in_item_id bigint,
	IN in_batchno varchar(100),
	IN in_item_qty double,
	IN in_unit_price double,
	IN in_unit_trade_discount double,
	IN in_unit_vat double,
	IN in_amount double,
	IN in_item_expiry_date date,
	IN in_item_mnf_date date,
	IN in_vat_rated varchar(50),
	IN in_vat_perc double,
	IN in_unit_price_inc_vat double,
	IN in_unit_price_exc_vat double,
	IN in_amount_inc_vat double,
	IN in_amount_exc_vat double,
	IN in_stock_effect varchar(1),
	IN in_is_trade_discount_vat_liable varchar(3),
	IN in_unit_cost_price double,
	IN in_unit_profit_margin double,
	IN in_earn_perc double,
	IN in_earn_amount double,
	IN in_code_specific varchar(250),
	IN in_desc_specific varchar(250),
	IN in_desc_more varchar(250),
	IN in_warranty_desc varchar(150),
	IN in_warranty_expiry_date date,
	IN in_account_code varchar(20),
	IN in_purchase_date date,
	IN in_dep_start_date date,
	IN in_dep_method_id int,
	IN in_dep_rate double,
	IN in_average_method_id int,
	IN in_effective_life int,
	IN in_residual_value double,
	IN in_narration varchar(100),
	IN in_qty_balance double,
	IN in_duration_value double,
	IN in_qty_damage double,
	IN in_duration_passed double,
	IN in_specific_size double,
    OUT out_transaction_item_id bigint
) 
BEGIN 
	-- SET @new_id=0;
	-- CALL sp_get_new_id("transaction_item","transaction_item_id",@new_id);

	SET @item_expiry_date=NULL;
	if (in_item_expiry_date is not null) then
		set @item_expiry_date=in_item_expiry_date;
	end if;

	SET @item_mnf_date=NULL;
	if (in_item_mnf_date is not null) then
		set @item_mnf_date=in_item_mnf_date;
	end if;

	SET @in_batchno='';
	if (in_batchno is not null) then
		set @in_batchno=in_batchno;
	end if;
	SET @code_specific='';
	if (in_code_specific is not null) then
		set @code_specific=in_code_specific;
	end if;
	SET @desc_specific='';
	if (in_desc_specific is not null) then
		set @desc_specific=in_desc_specific;
	end if;
	SET @desc_more=null;
	if (in_desc_more!='' and in_desc_more is not null) then
		set @desc_more=in_desc_more;
	end if;
	SET @warranty_desc=null;
	if (in_warranty_desc!="" and in_warranty_desc is not null) then
		set @warranty_desc=in_warranty_desc;
	end if;
	SET @warranty_expiry_date=NULL;
	if (in_warranty_expiry_date is not null) then
		set @warranty_expiry_date=in_warranty_expiry_date;
	end if;
	SET @account_code=null;
	if (in_account_code!='' and in_account_code is not null) then
		set @account_code=in_account_code;
	end if;
		SET @in_purchase_date=NULL;
		if (in_purchase_date is not null) then
			set @in_purchase_date=in_purchase_date;
		end if;
		SET @in_dep_start_date=NULL;
		if (in_dep_start_date is not null) then
			set @in_dep_start_date=in_dep_start_date;
		end if;
		SET @in_dep_method_id=NULL;
		if (in_dep_method_id!=0) then
			set @in_dep_method_id=in_dep_method_id;
		end if;
		SET @in_average_method_id=NULL;
		if (in_average_method_id!=0) then
			set @in_average_method_id=in_average_method_id;
		end if;
		SET @in_item_id=NULL;
		if (in_item_id!=0) then
			set @in_item_id=in_item_id;
		end if;
		SET @in_specific_size=1;
		if (in_specific_size!=0) then
			set @in_specific_size=in_specific_size;
		end if;

	INSERT INTO transaction_item
	(
		transaction_id,
		item_id,
		batchno,
		item_qty,
		unit_price,
		unit_trade_discount,
		unit_vat,
		amount,
		item_expiry_date,
		item_mnf_date,
		vat_rated,
		vat_perc,
		unit_price_inc_vat,
		unit_price_exc_vat,
		amount_inc_vat,
		amount_exc_vat,
		stock_effect,
		is_trade_discount_vat_liable,
		unit_cost_price,
		unit_profit_margin,
		earn_perc,
		earn_amount,
		code_specific,
		desc_specific,
		desc_more,
		warranty_desc,
		warranty_expiry_date,
		account_code,
		purchase_date,
		dep_start_date,
		dep_method_id,
		dep_rate,
		average_method_id,
		effective_life,
		residual_value,
		narration,
		qty_balance,
		duration_value,
		qty_damage,
		duration_passed,
		specific_size
	) 
    VALUES
	(
		in_transaction_id,
		@in_item_id,
		@in_batchno,
		in_item_qty,
		in_unit_price,
		in_unit_trade_discount,
		in_unit_vat,
		in_amount,
		@item_expiry_date,
		@item_mnf_date,
		in_vat_rated,
		in_vat_perc,
		in_unit_price_inc_vat,
		in_unit_price_exc_vat,
		in_amount_inc_vat,
		in_amount_exc_vat,
		in_stock_effect,
		in_is_trade_discount_vat_liable,
		in_unit_cost_price,
		in_unit_profit_margin,
		in_earn_perc,
		in_earn_amount,
		@code_specific,
		@desc_specific,
		@desc_more,
		@warranty_desc,
		@warranty_expiry_date,
		@account_code,
		@in_purchase_date,
		@in_dep_start_date,
		@in_dep_method_id,
		in_dep_rate,
		@in_average_method_id,
		in_effective_life,
		in_residual_value,
		in_narration,
		in_qty_balance,
		in_duration_value,
		in_qty_damage,
		in_duration_passed,
		@in_specific_size
	); 
    SET out_transaction_item_id=LAST_INSERT_ID();
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_item_old;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_item_old
(
	IN in_transaction_id bigint,
	IN in_item_id bigint,
	IN in_batchno varchar(100),
	IN in_item_qty double,
	IN in_unit_price double,
	IN in_unit_trade_discount double,
	IN in_unit_vat double,
	IN in_amount double,
	IN in_item_expiry_date date,
	IN in_item_mnf_date date,
	IN in_vat_rated varchar(50),
	IN in_vat_perc double,
	IN in_unit_price_inc_vat double,
	IN in_unit_price_exc_vat double,
	IN in_amount_inc_vat double,
	IN in_amount_exc_vat double,
	IN in_stock_effect varchar(1),
	IN in_is_trade_discount_vat_liable varchar(3),
	IN in_unit_cost_price double,
	IN in_unit_profit_margin double,
	IN in_earn_perc double,
	IN in_earn_amount double,
	IN in_code_specific varchar(250),
	IN in_desc_specific varchar(250),
	IN in_desc_more varchar(250),
	IN in_warranty_desc varchar(150),
	IN in_warranty_expiry_date date,
	IN in_account_code varchar(20),
	IN in_purchase_date date,
	IN in_dep_start_date date,
	IN in_dep_method_id int,
	IN in_dep_rate double,
	IN in_average_method_id int,
	IN in_effective_life int,
	IN in_residual_value double,
	IN in_narration varchar(100),
	IN in_qty_balance double,
	IN in_duration_value double,
	IN in_qty_damage double,
	IN in_duration_passed double,
	IN in_specific_size double
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("transaction_item","transaction_item_id",@new_id);

	SET @item_expiry_date=NULL;
	if (in_item_expiry_date is not null) then
		set @item_expiry_date=in_item_expiry_date;
	end if;

	SET @item_mnf_date=NULL;
	if (in_item_mnf_date is not null) then
		set @item_mnf_date=in_item_mnf_date;
	end if;

	SET @in_batchno='';
	if (in_batchno is not null) then
		set @in_batchno=in_batchno;
	end if;
	SET @code_specific='';
	if (in_code_specific is not null) then
		set @code_specific=in_code_specific;
	end if;
	SET @desc_specific='';
	if (in_desc_specific is not null) then
		set @desc_specific=in_desc_specific;
	end if;
	SET @desc_more=null;
	if (in_desc_more!='' and in_desc_more is not null) then
		set @desc_more=in_desc_more;
	end if;
	SET @warranty_desc=null;
	if (in_warranty_desc!="" and in_warranty_desc is not null) then
		set @warranty_desc=in_warranty_desc;
	end if;
	SET @warranty_expiry_date=NULL;
	if (in_warranty_expiry_date is not null) then
		set @warranty_expiry_date=in_warranty_expiry_date;
	end if;
	SET @account_code=null;
	if (in_account_code!='' and in_account_code is not null) then
		set @account_code=in_account_code;
	end if;
		SET @in_purchase_date=NULL;
		if (in_purchase_date is not null) then
			set @in_purchase_date=in_purchase_date;
		end if;
		SET @in_dep_start_date=NULL;
		if (in_dep_start_date is not null) then
			set @in_dep_start_date=in_dep_start_date;
		end if;
		SET @in_dep_method_id=NULL;
		if (in_dep_method_id!=0) then
			set @in_dep_method_id=in_dep_method_id;
		end if;
		SET @in_average_method_id=NULL;
		if (in_average_method_id!=0) then
			set @in_average_method_id=in_average_method_id;
		end if;
		SET @in_item_id=NULL;
		if (in_item_id!=0) then
			set @in_item_id=in_item_id;
		end if;
		SET @in_specific_size=1;
		if (in_specific_size!=0) then
			set @in_specific_size=in_specific_size;
		end if;

	INSERT INTO transaction_item
	(
		transaction_item_id,
		transaction_id,
		item_id,
		batchno,
		item_qty,
		unit_price,
		unit_trade_discount,
		unit_vat,
		amount,
		item_expiry_date,
		item_mnf_date,
		vat_rated,
		vat_perc,
		unit_price_inc_vat,
		unit_price_exc_vat,
		amount_inc_vat,
		amount_exc_vat,
		stock_effect,
		is_trade_discount_vat_liable,
		unit_cost_price,
		unit_profit_margin,
		earn_perc,
		earn_amount,
		code_specific,
		desc_specific,
		desc_more,
		warranty_desc,
		warranty_expiry_date,
		account_code,
		purchase_date,
		dep_start_date,
		dep_method_id,
		dep_rate,
		average_method_id,
		effective_life,
		residual_value,
		narration,
		qty_balance,
		duration_value,
		qty_damage,
		duration_passed,
		specific_size
	) 
    VALUES
	(
		@new_id,
		in_transaction_id,
		@in_item_id,
		@in_batchno,
		in_item_qty,
		in_unit_price,
		in_unit_trade_discount,
		in_unit_vat,
		in_amount,
		@item_expiry_date,
		@item_mnf_date,
		in_vat_rated,
		in_vat_perc,
		in_unit_price_inc_vat,
		in_unit_price_exc_vat,
		in_amount_inc_vat,
		in_amount_exc_vat,
		in_stock_effect,
		in_is_trade_discount_vat_liable,
		in_unit_cost_price,
		in_unit_profit_margin,
		in_earn_perc,
		in_earn_amount,
		@code_specific,
		@desc_specific,
		@desc_more,
		@warranty_desc,
		@warranty_expiry_date,
		@account_code,
		@in_purchase_date,
		@in_dep_start_date,
		@in_dep_method_id,
		in_dep_rate,
		@in_average_method_id,
		in_effective_life,
		in_residual_value,
		in_narration,
		in_qty_balance,
		in_duration_value,
		in_qty_damage,
		in_duration_passed,
		@in_specific_size
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_transaction_item;
DELIMITER //
CREATE PROCEDURE sp_update_transaction_item
(
	IN in_transaction_item_id bigint,
	IN in_transaction_id bigint,
	IN in_item_id bigint,
	IN in_batchno varchar(100),
	IN in_item_qty double,
	IN in_unit_price double,
	IN in_unit_trade_discount double,
	IN in_unit_vat double,
	IN in_amount double,
	IN in_item_expry_date date,
	IN in_item_mnf_date date,
	IN in_vat_rated varchar(50),
	IN in_vat_perc double,
	IN in_unit_price_inc_vat double,
	IN in_unit_price_exc_vat double,
	IN in_amount_inc_vat double,
	IN in_amount_exc_vat double,
	IN in_stock_effect varchar(1),
	IN in_is_trade_discount_vat_liable varchar(3),
	IN in_unit_cost_price double,
	IN in_unit_profit_margin double,
	IN in_earn_perc double,
	IN in_earn_amount double,
	IN in_code_specific varchar(250),
	IN in_desc_specific varchar(250),
	IN in_desc_more varchar(250),
	IN in_warranty_desc varchar(150),
	IN in_warranty_expiry_date date,
	IN in_account_code varchar(20),
	IN in_purchase_date date,
	IN in_dep_start_date date,
	IN in_dep_method_id int,
	IN in_dep_rate double,
	IN in_average_method_id int,
	IN in_effective_life int,
	IN in_residual_value double,
	IN in_narration varchar(100),
	IN in_qty_balance double,
	IN in_duration_value double,
	IN in_qty_damage double,
	IN in_duration_passed double,
	IN in_specific_size double
) 
BEGIN 
	SET @item_expiry_date=NULL;
	if (in_item_expry_date is not null) then
		set @item_expiry_date=in_item_expry_date;
	end if;

	SET @item_mnf_date=NULL;
	if (in_item_mnf_date is not null) then
		set @item_mnf_date=in_item_mnf_date;
	end if;

	SET @in_batchno='';
	if (in_batchno is not null) then
		set @in_batchno=in_batchno;
	end if;
	SET @code_specific='';
	if (in_code_specific is not null) then
		set @code_specific=in_code_specific;
	end if;
	SET @desc_specific='';
	if (in_desc_specific is not null) then
		set @desc_specific=in_desc_specific;
	end if;
	SET @desc_more=null;
	if (in_desc_more!='' and in_desc_more is not null) then
		set @desc_more=in_desc_more;
	end if;
	SET @warranty_desc=null;
	if (in_warranty_desc!="" and in_warranty_desc is not null) then
		set @warranty_desc=in_warranty_desc;
	end if;
	SET @warranty_expiry_date=NULL;
	if (in_warranty_expiry_date is not null) then
		set @warranty_expiry_date=in_warranty_expiry_date;
	end if;
	SET @account_code=null;
	if (in_account_code!='' and in_account_code is not null) then
		set @account_code=in_account_code;
	end if;
		SET @in_purchase_date=NULL;
		if (in_purchase_date is not null) then
			set @in_purchase_date=in_purchase_date;
		end if;
		SET @in_dep_start_date=NULL;
		if (in_dep_start_date is not null) then
			set @in_dep_start_date=in_dep_start_date;
		end if;
		SET @in_dep_method_id=NULL;
		if (in_dep_method_id!=0) then
			set @in_dep_method_id=in_dep_method_id;
		end if;
		SET @in_average_method_id=NULL;
		if (in_average_method_id!=0) then
			set @in_average_method_id=in_average_method_id;
		end if;
		SET @in_item_id=NULL;
		if (in_item_id!=0) then
			set @in_item_id=in_item_id;
		end if;
		SET @in_specific_size=1;
		if (in_specific_size!=0) then
			set @in_specific_size=in_specific_size;
		end if;

	UPDATE transaction_item SET 
		transaction_id=in_transaction_id,
		item_id=@in_item_id,
		batchno=@in_batchno,
		item_qty=in_item_qty,
		unit_price=in_unit_price,
		unit_trade_discount=in_unit_trade_discount,
		unit_vat=in_unit_vat,
		amount=in_amount,
		item_expry_date=@item_expry_date,
		item_mnf_date=@item_mnf_date,
		vat_rated=in_vat_rated,
		vat_perc=in_vat_perc,
		unit_price_inc_vat =in_unit_price_inc_vat,
		unit_price_exc_vat =in_unit_price_exc_vat,
		amount_inc_vat =in_amount_inc_vat,
		amount_exc_vat =in_amount_exc_vat,
		stock_effect=in_stock_effect,
		is_trade_discount_vat_liable=in_is_trade_discount_vat_liable,
		unit_cost_price=in_unit_cost_price,
		unit_profit_margin=in_unit_profit_margin,
		earn_perc=in_earn_perc,
		earn_amount=in_earn_amount,
		code_specific=@code_specific,
		desc_specific=@desc_specific,
		desc_more=@desc_more,
		warranty_desc=@warranty_desc,
		warranty_expiry_date=@warranty_expiry_date,
		account_code=@account_code,
		purchase_date=@in_purchase_date,
		dep_start_date=@in_dep_start_date,
		dep_method_id=@in_dep_method_id,
		dep_rate=in_dep_rate,
		average_method_id=@in_average_method_id,
		effective_life=in_effective_life,
		residual_value=in_residual_value,
		narration=in_narration,
		qty_balance=in_qty_balance,
		duration_value=in_duration_value,
		qty_damage=in_qty_damage,
		duration_passed=in_duration_passed,
		specific_size=@in_specific_size 
	WHERE transaction_item_id=in_transaction_item_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_transaction_item2;
DELIMITER //
CREATE PROCEDURE sp_update_transaction_item2
(
	IN in_transaction_item_id bigint,
	IN in_item_qty double,
	IN in_unit_price double,
	IN in_unit_trade_discount double,
	IN in_unit_vat double,
	IN in_amount double,
	IN in_unit_price_inc_vat double,
	IN in_unit_price_exc_vat double,
	IN in_amount_inc_vat double,
	IN in_amount_exc_vat double,
	IN in_unit_cost_price double,
	IN in_unit_profit_margin double,
	IN in_earn_perc double,
	IN in_earn_amount double
) 
BEGIN 
	UPDATE transaction_item SET 
		item_qty=in_item_qty,
		unit_price=in_unit_price,
		unit_trade_discount=in_unit_trade_discount,
		unit_vat=in_unit_vat,
		amount=in_amount,
		unit_price_inc_vat =in_unit_price_inc_vat,
		unit_price_exc_vat =in_unit_price_exc_vat,
		amount_inc_vat =in_amount_inc_vat,
		amount_exc_vat =in_amount_exc_vat,
		unit_cost_price=in_unit_cost_price,
		unit_profit_margin=in_unit_profit_margin,
		earn_perc=in_earn_perc,
		earn_amount=in_earn_amount 
	WHERE transaction_item_id=in_transaction_item_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_transaction_itemCEC;
DELIMITER //
CREATE PROCEDURE sp_update_transaction_itemCEC
(
	IN in_transaction_item_id bigint,
	IN in_item_qty double,
	IN in_unit_price double,
	IN in_unit_trade_discount double,
	IN in_unit_vat double,
	IN in_amount double,
	IN in_unit_price_inc_vat double,
	IN in_unit_price_exc_vat double,
	IN in_amount_inc_vat double,
	IN in_amount_exc_vat double,
	IN in_unit_cost_price double,
	IN in_unit_profit_margin double,
	IN in_earn_perc double,
	IN in_earn_amount double,
	IN in_qty_balance double,
	IN in_duration_value double,
	IN in_qty_damage double,
	IN in_duration_passed double
) 
BEGIN 
	UPDATE transaction_item SET 
		item_qty=in_item_qty,
		unit_price=in_unit_price,
		unit_trade_discount=in_unit_trade_discount,
		unit_vat=in_unit_vat,
		amount=in_amount,
		unit_price_inc_vat =in_unit_price_inc_vat,
		unit_price_exc_vat =in_unit_price_exc_vat,
		amount_inc_vat =in_amount_inc_vat,
		amount_exc_vat =in_amount_exc_vat,
		unit_cost_price=in_unit_cost_price,
		unit_profit_margin=in_unit_profit_margin,
		earn_perc=in_earn_perc,
		earn_amount=in_earn_amount,
		qty_balance=in_qty_balance,
		duration_value=in_duration_value,
		qty_damage=in_qty_damage,
		duration_passed=in_duration_passed 
	WHERE transaction_item_id=in_transaction_item_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_item_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_item_by_id
(
	IN in_transaction_item_id bigint 
) 
BEGIN 
		SELECT ti.*,tu.unit_id,tu.base_unit_qty FROM transaction_item ti INNER JOIN transaction_item_unit tu ON ti.transaction_item_id=tu.transaction_item_id 
		WHERE ti.transaction_item_id=in_transaction_item_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_item_by_transaction_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_item_by_transaction_id
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT ti.*,tiu.unit_id,tiu.base_unit_qty FROM transaction_item ti 
        INNER JOIN transaction_item_unit tiu ON ti.transaction_item_id=tiu.transaction_item_id 
		WHERE ti.transaction_id=in_transaction_id ORDER BY ti.transaction_item_id ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_trans_item_summary_by_account;
DELIMITER //
CREATE PROCEDURE sp_search_trans_item_summary_by_account
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT account_code,sum(amount_exc_vat) as amount_exc_vat,sum(amount_inc_vat) as amount_inc_vat FROM transaction_item ti 
		WHERE ti.transaction_id=in_transaction_id GROUP BY account_code;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_trans_item_summary_by_item_type;
DELIMITER //
CREATE PROCEDURE sp_search_trans_item_summary_by_item_type
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT i.item_type,sum(ti.amount_exc_vat) as amount_exc_vat,sum(ti.amount_inc_vat) as amount_inc_vat,sum(ifnull(ed.calc_excise_tax_amount,0)) as calc_excise_tax_amount FROM transaction_item ti 
		INNER JOIN item i ON ti.item_id=i.item_id 
        LEFT JOIN transaction_item_excise ed ON ti.transaction_item_id=ed.transaction_item_id 
		WHERE ti.transaction_id=in_transaction_id GROUP BY i.item_type;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_trans_item_summary_by_item_type_cr_note;
DELIMITER //
CREATE PROCEDURE sp_search_trans_item_summary_by_item_type_cr_note
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT sum(ti.amount_exc_vat) as amount_exc_vat,sum(ti.amount_inc_vat) as amount_inc_vat FROM transaction_item_cr_dr_note ti 
		INNER JOIN item i ON ti.item_id=i.item_id  
		WHERE ti.transaction_id=in_transaction_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_trans_item_summary_by_item_type_dr_note;
DELIMITER //
CREATE PROCEDURE sp_search_trans_item_summary_by_item_type_dr_note
(
	IN in_transaction_id bigint 
) 
BEGIN 
        SELECT i.item_type,sum(ti.amount_exc_vat) as amount_exc_vat,sum(ti.amount_inc_vat) as amount_inc_vat FROM transaction_item_cr_dr_note ti 
		INNER JOIN item i ON ti.item_id=i.item_id  
		WHERE ti.transaction_id=in_transaction_id GROUP BY i.item_type;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_inventory_cost_by_trans;
DELIMITER //
CREATE PROCEDURE sp_search_inventory_cost_by_trans
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT IFNULL(ti.account_code,'') as account_code,sum(ti.item_qty*(CASE WHEN ti.unit_vat>0 THEN (ti.unit_cost_price/(1 + (ti.vat_perc/100))) ELSE ti.unit_cost_price END)) as unit_cost_price FROM transaction_item ti 
		WHERE ti.transaction_id=in_transaction_id AND IFNULL(ti.account_code,'')<>'' AND IFNULL(ti.account_code,'') NOT LIKE '5%'  
		GROUP BY IFNULL(ti.account_code,'');
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_inventory_cost_by_trans_cr_note;
DELIMITER //
CREATE PROCEDURE sp_search_inventory_cost_by_trans_cr_note
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT IFNULL(ti.account_code,'') as account_code,sum(ti.item_qty*(CASE WHEN ti.unit_vat>0 THEN (ti.unit_cost_price/(1 + (ti.vat_perc/100))) ELSE ti.unit_cost_price END)) as unit_cost_price FROM transaction_item_cr_dr_note ti 
		WHERE ti.transaction_id=in_transaction_id AND IFNULL(ti.account_code,'')<>'' AND IFNULL(ti.account_code,'') NOT LIKE '5%'  
		GROUP BY IFNULL(ti.account_code,'');
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_inventory_cost_by_trans_dr_note;
DELIMITER //
CREATE PROCEDURE sp_search_inventory_cost_by_trans_dr_note
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT IFNULL(ti.account_code,'') as account_code,sum(ti.item_qty*(CASE WHEN ti.unit_vat>0 THEN (ti.unit_cost_price/(1 + (ti.vat_perc/100))) ELSE ti.unit_cost_price END)) as unit_cost_price FROM transaction_item_cr_dr_note ti 
		WHERE ti.transaction_id=in_transaction_id AND IFNULL(ti.account_code,'')<>'' AND IFNULL(ti.account_code,'') NOT LIKE '5%'  
		GROUP BY IFNULL(ti.account_code,'');
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_inventory_item_type_cost_by_trans;
DELIMITER //
CREATE PROCEDURE sp_search_inventory_item_type_cost_by_trans
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT i.item_type,sum(ti.item_qty*(CASE WHEN ti.unit_vat>0 THEN (ti.unit_cost_price/(1 + (ti.vat_perc/100))) ELSE ti.unit_cost_price END)) as unit_cost_price FROM transaction_item ti 
		INNER JOIN item i ON ti.item_id=i.item_id 
		WHERE ti.transaction_id=in_transaction_id AND IFNULL(ti.account_code,'')<>'' AND IFNULL(ti.account_code,'') NOT LIKE '5%'  
		GROUP BY i.item_type;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_inventory_item_type_cost_by_trans_cr_note;
DELIMITER //
CREATE PROCEDURE sp_search_inventory_item_type_cost_by_trans_cr_note
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT i.item_type,sum(ti.item_qty*(CASE WHEN ti.unit_vat>0 THEN (ti.unit_cost_price/(1 + (ti.vat_perc/100))) ELSE ti.unit_cost_price END)) as unit_cost_price FROM transaction_item_cr_dr_note ti 
		INNER JOIN item i ON ti.item_id=i.item_id 
		WHERE ti.transaction_id=in_transaction_id AND IFNULL(ti.account_code,'')<>'' AND IFNULL(ti.account_code,'') NOT LIKE '5%'  
		GROUP BY i.item_type;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_inventory_item_type_cost_by_trans_dr_note;
DELIMITER //
CREATE PROCEDURE sp_search_inventory_item_type_cost_by_trans_dr_note
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT i.item_type,sum(ti.item_qty*(CASE WHEN ti.unit_vat>0 THEN (ti.unit_cost_price/(1 + (ti.vat_perc/100))) ELSE ti.unit_cost_price END)) as unit_cost_price FROM transaction_item_cr_dr_note ti 
		INNER JOIN item i ON ti.item_id=i.item_id 
		WHERE ti.transaction_id=in_transaction_id AND IFNULL(ti.account_code,'')<>'' AND IFNULL(ti.account_code,'') NOT LIKE '5%'  
		GROUP BY i.item_type;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_inventory_item_type_cost_by_trans_consume;
DELIMITER //
CREATE PROCEDURE sp_search_inventory_item_type_cost_by_trans_consume
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT i.item_type,sum(ti.amount_exc_vat) as amount_exc_vat FROM transaction_item ti 
		INNER JOIN item i ON ti.item_id=i.item_id 
		WHERE ti.transaction_id=in_transaction_id AND IFNULL(ti.account_code,'')<>'' AND IFNULL(ti.account_code,'') NOT LIKE '5%'  
		GROUP BY i.item_type;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_inventory_cost_by_trans_dispose;
DELIMITER //
CREATE PROCEDURE sp_search_inventory_cost_by_trans_dispose
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT IFNULL(ti.account_code,'') as account_code,sum(ti.amount_exc_vat) as amount_exc_vat FROM transaction_item ti 
		WHERE ti.transaction_id=in_transaction_id AND IFNULL(ti.account_code,'')<>'' AND IFNULL(ti.account_code,'') NOT LIKE '5%' 
		GROUP BY IFNULL(ti.account_code,'');
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_inventory_cost_by_trans_consume;
DELIMITER //
CREATE PROCEDURE sp_search_inventory_cost_by_trans_consume
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT IFNULL(ti.account_code,'') as account_code,sum(ti.amount_exc_vat) as amount_exc_vat FROM transaction_item ti 
		WHERE ti.transaction_id=in_transaction_id AND IFNULL(ti.account_code,'')<>'' AND IFNULL(ti.account_code,'') NOT LIKE '5%' 
		GROUP BY IFNULL(ti.account_code,'');
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_inventory_cost_by_trans_input;
DELIMITER //
CREATE PROCEDURE sp_search_inventory_cost_by_trans_input
(
	IN in_transaction_id bigint 
) 
BEGIN 
	SELECT IFNULL(i.expense_account_code,'') as account_code,sum(ti.input_qty*ti.input_unit_cost) as input_total_cost FROM trans_production_item ti 
	INNER JOIN item i ON ti.input_item_id=i.item_id 
	WHERE ti.transaction_id=in_transaction_id AND IFNULL(i.expense_account_code,'')<>'' 
	GROUP BY IFNULL(i.expense_account_code,'');
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_inventory_cost_by_trans_output;
DELIMITER //
CREATE PROCEDURE sp_search_inventory_cost_by_trans_output
(
	IN in_transaction_id bigint 
) 
BEGIN 
	SELECT IFNULL(i.expense_account_code,'') as account_code,sum(t.output_total_cost) as output_total_cost FROM trans_production t 
	INNER JOIN item i ON t.output_item_id=i.item_id 
	WHERE t.transaction_id=in_transaction_id AND IFNULL(i.expense_account_code,'')<>'' 
	GROUP BY IFNULL(i.expense_account_code,'');
END//
DELIMITER ;


DROP PROCEDURE IF EXISTS sp_search_transaction_item_by_transaction_number;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_item_by_transaction_number
(
	IN in_transaction_number varchar(50) 
) 
BEGIN 
		SELECT ti.* FROM transaction_item ti,transaction t 
		WHERE ti.transaction_id=t.transaction_id and t.transaction_number=in_transaction_number ORDER BY ti.transaction_item_id ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_item_by_transaction_id2;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_item_by_transaction_id2
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT ti.* FROM transaction_item ti, item i,category c 
		WHERE ti.transaction_id=in_transaction_id AND 
		ti.item_id=i.item_id AND c.category_id=i.category_id 
		ORDER BY c.category_name ASC,ti.transaction_item_id ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_item_by_transaction_id3;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_item_by_transaction_id3
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT ti.*,i.description FROM transaction_item ti, item i 
		WHERE ti.transaction_id=in_transaction_id AND 
		ti.item_id=i.item_id  
		ORDER BY ti.transaction_item_id ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_item_by_transaction_id4;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_item_by_transaction_id4
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT ti.*,i.description,u.unit_symbol,i.alias_name,i.display_alias_name,i.override_gen_name,i.hide_unit_price_invoice,i.item_code,tu.unit_id,tu.base_unit_qty FROM transaction_item ti 
        INNER JOIN item i ON ti.item_id=i.item_id 
        INNER JOIN transaction_item_unit tu ON ti.transaction_item_id=tu.transaction_item_id 
        INNER JOIN unit u ON tu.unit_id=u.unit_id 
		WHERE ti.transaction_id=in_transaction_id 
		ORDER BY ti.transaction_item_id ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction
(
	IN in_transaction_date date,
	IN in_store_id int,
	IN in_store2_id int,
	IN in_transactor_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_cash_discount double,
	IN in_total_vat double,
	IN in_transaction_comment varchar(255),
	IN in_add_user_detail_id int,
	IN in_add_date datetime,
	IN in_edit_user_detail_id int,
	IN in_edit_date datetime,
	IN in_transaction_ref varchar(100),
	OUT out_transaction_id bigint,
	IN in_sub_total double,
	IN in_grand_total double,
	IN in_total_trade_discount double,
	IN in_points_awarded double,
	IN in_card_number varchar(10),
	IN in_total_std_vatable_amount double,
	IN in_total_zero_vatable_amount double,
	IN in_total_exempt_vatable_amount double,
	IN in_vat_perc double,
	IN in_amount_tendered double,
	IN in_change_amount double,
	IN in_is_cash_discount_vat_liable varchar(3),
	IN in_total_profit_margin double,
	IN in_transaction_user_detail_id int,
	IN in_bill_transactor_id bigint,
	IN in_scheme_transactor_id bigint,
	IN in_princ_scheme_member varchar(100),
	IN in_scheme_card_number varchar(100),
	IN in_transaction_number varchar(50),
	IN in_delivery_date date,
	IN in_delivery_address varchar(250),
	IN in_pay_terms varchar(250),
	IN in_terms_conditions varchar(250),
	IN in_authorised_by_user_detail_id int,
	IN in_authorise_date date,
	IN in_pay_due_date date,
	IN in_expiry_date date,
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_xrate double,
	IN in_from_date date,
	IN in_to_date date,
	IN in_duration_type  varchar(20),
	IN in_site_id bigint,
	IN in_transactor_rep  varchar(50),
	IN in_transactor_vehicle  varchar(20),
	IN in_transactor_driver  varchar(50),
	IN in_duration_value double,
	IN in_location_id bigint,
	IN in_status_code  varchar(20),
	IN in_status_date datetime,
	IN in_delivery_mode varchar(20),
	IN in_is_processed int,
	IN in_is_paid int,
	IN in_is_cancel int,
	IN in_spent_points_amount double
) 
BEGIN 
	-- SET @new_id=0;
	-- CALL sp_get_new_id("transaction","transaction_id",@new_id);
	
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @edit_datetime=null;
	SET @edit_user_detail_id=null;
	
	SET @store2_id=NULL;
	if (in_store2_id!=0) then
		set @store2_id=in_store2_id;
	end if;

	SET @transactor_id=NULL;
	if (in_transactor_id!=0) then
		set @transactor_id=in_transactor_id;
	end if;

	SET @transaction_user_detail_id=NULL;
	if (in_transaction_user_detail_id!=0) then
		set @transaction_user_detail_id=in_transaction_user_detail_id;
	end if;

	SET @bill_transactor_id=NULL;
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;

	SET @scheme_transactor_id=NULL;
	if (in_scheme_transactor_id!=0) then
		set @scheme_transactor_id=in_scheme_transactor_id;
	end if;

	SET @scheme_transactor_id=NULL;
	if (in_scheme_transactor_id!=0) then
		set @scheme_transactor_id=in_scheme_transactor_id;
	end if;

	SET @authorised_by_user_detail_id=NULL;
	if (in_authorised_by_user_detail_id!=0) then
		set @authorised_by_user_detail_id=in_authorised_by_user_detail_id;
	end if;
	/*
	SET @transaction_number=@new_id;
	if (in_transaction_number!='') then
		set @transaction_number=in_transaction_number;
	end if;
    */
    set @transaction_number=in_transaction_number;

	SET @acc_child_account_id=NULL;
	if (in_acc_child_account_id!=0) then
		set @acc_child_account_id=in_acc_child_account_id;
	end if;

	SET @location_id=NULL;
	if (in_location_id!=0) then
		set @location_id=in_location_id;
	end if;

	INSERT INTO transaction
	(
		transaction_date,
		store_id,
		store2_id,
		transactor_id,
		transaction_type_id,
		transaction_reason_id,
		cash_discount,
		total_vat,
		transaction_comment,
		add_user_detail_id,
		add_date,
		edit_user_detail_id,
		edit_date,
		transaction_ref,
		sub_total,
		grand_total,
		total_trade_discount,
		points_awarded,
		card_number,
		total_std_vatable_amount,
		total_zero_vatable_amount,
		total_exempt_vatable_amount,
		vat_perc,
		amount_tendered,
		change_amount,
		is_cash_discount_vat_liable,
		total_profit_margin,
		transaction_user_detail_id,
		bill_transactor_id,
		scheme_transactor_id,
		princ_scheme_member,
		scheme_card_number,
		transaction_number,
		delivery_date,
		delivery_address,
		pay_terms,
		terms_conditions,
		authorised_by_user_detail_id,
		authorise_date,
		pay_due_date,
		expiry_date,
		acc_child_account_id,
		currency_code,
		xrate,
		from_date,
		to_date,
		duration_type,
		site_id,
		transactor_rep,
		transactor_vehicle,
		transactor_driver,
		duration_value,
		location_id,
		status_code,
		status_date,
		delivery_mode,
		is_processed,
		is_paid,
		is_cancel,
        spent_points_amount
	) 
    VALUES
	(
		in_transaction_date,
		in_store_id,
		@store2_id,
		@transactor_id,
		in_transaction_type_id,
		in_transaction_reason_id,
		in_cash_discount,
		in_total_vat,
		in_transaction_comment,
		in_add_user_detail_id,
		@cur_sys_datetime,
		@edit_user_detail_id,
		@edit_datetime,
		in_transaction_ref,
		in_sub_total,
		in_grand_total,
		in_total_trade_discount,
		in_points_awarded,
		in_card_number,
		in_total_std_vatable_amount,
		in_total_zero_vatable_amount,
		in_total_exempt_vatable_amount,
		in_vat_perc,
		in_amount_tendered,
		in_change_amount,
		in_is_cash_discount_vat_liable,
		in_total_profit_margin,
		@transaction_user_detail_id,
		@bill_transactor_id,
		@scheme_transactor_id,
		in_princ_scheme_member,
		in_scheme_card_number,
		@transaction_number,
		in_delivery_date,
		in_delivery_address,
		in_pay_terms,
		in_terms_conditions,
		@authorised_by_user_detail_id,
		in_authorise_date,
		in_pay_due_date,
		in_expiry_date,
		@acc_child_account_id,
		in_currency_code,
		in_xrate,
		in_from_date,
		in_to_date,
		in_duration_type,
		in_site_id,
		in_transactor_rep,
		in_transactor_vehicle,
		in_transactor_driver,
		in_duration_value,
		@location_id,
		in_status_code,
		in_status_date,
		in_delivery_mode,
		in_is_processed,
		in_is_paid,
		in_is_cancel,
        in_spent_points_amount
	); 
SET out_transaction_id=LAST_INSERT_ID();
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_old;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_old
(
	IN in_transaction_date date,
	IN in_store_id int,
	IN in_store2_id int,
	IN in_transactor_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_cash_discount double,
	IN in_total_vat double,
	IN in_transaction_comment varchar(255),
	IN in_add_user_detail_id int,
	IN in_add_date datetime,
	IN in_edit_user_detail_id int,
	IN in_edit_date datetime,
	IN in_transaction_ref varchar(100),
	OUT out_transaction_id bigint,
	IN in_sub_total double,
	IN in_grand_total double,
	IN in_total_trade_discount double,
	IN in_points_awarded double,
	IN in_card_number varchar(10),
	IN in_total_std_vatable_amount double,
	IN in_total_zero_vatable_amount double,
	IN in_total_exempt_vatable_amount double,
	IN in_vat_perc double,
	IN in_amount_tendered double,
	IN in_change_amount double,
	IN in_is_cash_discount_vat_liable varchar(3),
	IN in_total_profit_margin double,
	IN in_transaction_user_detail_id int,
	IN in_bill_transactor_id bigint,
	IN in_scheme_transactor_id bigint,
	IN in_princ_scheme_member varchar(100),
	IN in_scheme_card_number varchar(100),
	IN in_transaction_number varchar(50),
	IN in_delivery_date date,
	IN in_delivery_address varchar(250),
	IN in_pay_terms varchar(250),
	IN in_terms_conditions varchar(250),
	IN in_authorised_by_user_detail_id int,
	IN in_authorise_date date,
	IN in_pay_due_date date,
	IN in_expiry_date date,
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_xrate double,
	IN in_from_date date,
	IN in_to_date date,
	IN in_duration_type  varchar(20),
	IN in_site_id bigint,
	IN in_transactor_rep  varchar(50),
	IN in_transactor_vehicle  varchar(20),
	IN in_transactor_driver  varchar(50),
	IN in_duration_value double,
	IN in_location_id bigint,
	IN in_status_code  varchar(20),
	IN in_status_date datetime,
	IN in_delivery_mode varchar(20),
	IN in_is_processed int,
	IN in_is_paid int,
	IN in_is_cancel int,
	IN in_spent_points_amount double
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("transaction","transaction_id",@new_id);
	
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @edit_datetime=null;
	SET @edit_user_detail_id=null;
	
	SET @store2_id=NULL;
	if (in_store2_id!=0) then
		set @store2_id=in_store2_id;
	end if;

	SET @transactor_id=NULL;
	if (in_transactor_id!=0) then
		set @transactor_id=in_transactor_id;
	end if;

	SET @transaction_user_detail_id=NULL;
	if (in_transaction_user_detail_id!=0) then
		set @transaction_user_detail_id=in_transaction_user_detail_id;
	end if;

	SET @bill_transactor_id=NULL;
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;

	SET @scheme_transactor_id=NULL;
	if (in_scheme_transactor_id!=0) then
		set @scheme_transactor_id=in_scheme_transactor_id;
	end if;

	SET @scheme_transactor_id=NULL;
	if (in_scheme_transactor_id!=0) then
		set @scheme_transactor_id=in_scheme_transactor_id;
	end if;

	SET @authorised_by_user_detail_id=NULL;
	if (in_authorised_by_user_detail_id!=0) then
		set @authorised_by_user_detail_id=in_authorised_by_user_detail_id;
	end if;

	SET @transaction_number=@new_id;
	if (in_transaction_number!='') then
		set @transaction_number=in_transaction_number;
	end if;

	SET @acc_child_account_id=NULL;
	if (in_acc_child_account_id!=0) then
		set @acc_child_account_id=in_acc_child_account_id;
	end if;

	SET @location_id=NULL;
	if (in_location_id!=0) then
		set @location_id=in_location_id;
	end if;

	INSERT INTO transaction
	(
		transaction_id,
		transaction_date,
		store_id,
		store2_id,
		transactor_id,
		transaction_type_id,
		transaction_reason_id,
		cash_discount,
		total_vat,
		transaction_comment,
		add_user_detail_id,
		add_date,
		edit_user_detail_id,
		edit_date,
		transaction_ref,
		sub_total,
		grand_total,
		total_trade_discount,
		points_awarded,
		card_number,
		total_std_vatable_amount,
		total_zero_vatable_amount,
		total_exempt_vatable_amount,
		vat_perc,
		amount_tendered,
		change_amount,
		is_cash_discount_vat_liable,
		total_profit_margin,
		transaction_user_detail_id,
		bill_transactor_id,
		scheme_transactor_id,
		princ_scheme_member,
		scheme_card_number,
		transaction_number,
		delivery_date,
		delivery_address,
		pay_terms,
		terms_conditions,
		authorised_by_user_detail_id,
		authorise_date,
		pay_due_date,
		expiry_date,
		acc_child_account_id,
		currency_code,
		xrate,
		from_date,
		to_date,
		duration_type,
		site_id,
		transactor_rep,
		transactor_vehicle,
		transactor_driver,
		duration_value,
		location_id,
		status_code,
		status_date,
		delivery_mode,
		is_processed,
		is_paid,
		is_cancel,
        spent_points_amount
	) 
    VALUES
	(
		@new_id,
		in_transaction_date,
		in_store_id,
		@store2_id,
		@transactor_id,
		in_transaction_type_id,
		in_transaction_reason_id,
		in_cash_discount,
		in_total_vat,
		in_transaction_comment,
		in_add_user_detail_id,
		@cur_sys_datetime,
		@edit_user_detail_id,
		@edit_datetime,
		in_transaction_ref,
		in_sub_total,
		in_grand_total,
		in_total_trade_discount,
		in_points_awarded,
		in_card_number,
		in_total_std_vatable_amount,
		in_total_zero_vatable_amount,
		in_total_exempt_vatable_amount,
		in_vat_perc,
		in_amount_tendered,
		in_change_amount,
		in_is_cash_discount_vat_liable,
		in_total_profit_margin,
		@transaction_user_detail_id,
		@bill_transactor_id,
		@scheme_transactor_id,
		in_princ_scheme_member,
		in_scheme_card_number,
		@transaction_number,
		in_delivery_date,
		in_delivery_address,
		in_pay_terms,
		in_terms_conditions,
		@authorised_by_user_detail_id,
		in_authorise_date,
		in_pay_due_date,
		in_expiry_date,
		@acc_child_account_id,
		in_currency_code,
		in_xrate,
		in_from_date,
		in_to_date,
		in_duration_type,
		in_site_id,
		in_transactor_rep,
		in_transactor_vehicle,
		in_transactor_driver,
		in_duration_value,
		@location_id,
		in_status_code,
		in_status_date,
		in_delivery_mode,
		in_is_processed,
		in_is_paid,
		in_is_cancel,
        in_spent_points_amount
	); 
SET out_transaction_id=@new_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_transaction;
DELIMITER //
CREATE PROCEDURE sp_update_transaction
(
	IN in_transaction_id bigint,
	IN in_transaction_date date,
	IN in_store_id int,
	IN in_store2_id int,
	IN in_transactor_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_cash_discount double,
	IN in_total_vat double,
	IN in_transaction_comment varchar(255),
	IN in_edit_user_detail_id int,
	IN in_transaction_ref varchar(100),
	IN in_sub_total double,
	IN in_grand_total double,
	IN in_total_trade_discount double,
	IN in_points_awarded double,
	IN in_card_number varchar(10),
	IN in_total_std_vatable_amount double,
	IN in_total_zero_vatable_amount double,
	IN in_total_exempt_vatable_amount double,
	IN in_vat_perc double,
	IN in_amount_tendered double,
	IN in_change_amount double, 
	IN in_is_cash_discount_vat_liable varchar(3),
	IN in_total_profit_margin double,
	IN in_transaction_user_detail_id int,
	IN in_bill_transactor_id bigint,
	IN in_scheme_transactor_id bigint,
	IN in_princ_scheme_member varchar(100),
	IN in_scheme_card_number varchar(100),
	IN in_transaction_number varchar(50),
	IN in_delivery_date date,
	IN in_delivery_address varchar(250),
	IN in_pay_terms varchar(250),
	IN in_terms_conditions varchar(250),
	IN in_authorised_by_user_detail_id int,
	IN in_authorise_date date,
	IN in_pay_due_date date,
	IN in_expiry_date date,
	IN in_from_date date,
	IN in_to_date date,
	IN in_duration_type  varchar(20),
	IN in_site_id bigint,
	IN in_transactor_rep  varchar(50),
	IN in_transactor_vehicle  varchar(20),
	IN in_transactor_driver  varchar(50),
	IN in_duration_value double,
	IN in_location_id bigint,
	IN in_status_code  varchar(20),
	IN in_status_date datetime,
	IN in_delivery_mode varchar(20),
	IN in_is_processed int,
	IN in_is_paid int,
	IN in_is_cancel int,
    IN in_spent_points_amount double
) 
BEGIN 

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @store2_id=NULL;
	if (in_store2_id!=0) then
		set @store2_id=in_store2_id;
	end if;

	SET @transactor_id=NULL;
	if (in_transactor_id!=0) then
		set @transactor_id=in_transactor_id;
	end if;

	SET @transaction_user_detail_id=NULL;
	if (in_transaction_user_detail_id!=0) then
		set @transaction_user_detail_id=in_transaction_user_detail_id;
	end if;

	SET @bill_transactor_id=NULL;
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;

	SET @scheme_transactor_id=NULL;
	if (in_scheme_transactor_id!=0) then
		set @scheme_transactor_id=in_scheme_transactor_id;
	end if;

	SET @authorised_by_user_detail_id=NULL;
	if (in_authorised_by_user_detail_id!=0) then
		set @authorised_by_user_detail_id=in_authorised_by_user_detail_id;
	end if;

	SET @location_id=NULL;
	if (in_location_id!=0) then
		set @location_id=in_location_id;
	end if;

	UPDATE transaction SET 
		transaction_date=in_transaction_date,
		store_id=in_store_id,
		store2_id=@store2_id,
		transactor_id=@transactor_id,
		transaction_type_id=in_transaction_type_id,
		transaction_reason_id=in_transaction_reason_id,
		cash_discount=in_cash_discount,
		total_vat=in_total_vat,
		transaction_comment=in_transaction_comment,
		edit_user_detail_id=in_edit_user_detail_id,
		edit_date=@cur_sys_datetime,
		transaction_ref=in_transaction_ref,
		sub_total=in_sub_total,
		grand_total=in_grand_total,
		total_trade_discount=in_total_trade_discount,
		points_awarded=in_points_awarded,
		card_number=in_card_number,
		total_std_vatable_amount=in_total_std_vatable_amount,
		total_zero_vatable_amount=in_total_zero_vatable_amount,
		total_exempt_vatable_amount=in_total_exempt_vatable_amount,
		in_vat_perc=in_vat_perc,
		amount_tendered=in_amount_tendered,
		change_amount=in_change_amount,
		is_cash_discount_vat_liable=in_is_cash_discount_vat_liable,
		total_profit_margin=in_total_profit_margin,
		transaction_user_detail_id=in_transaction_user_detail_id,
		bill_transactor_id=@bill_transactor_id,
		scheme_transactor_id=@scheme_transactor_id,
		princ_scheme_member=in_princ_scheme_member,
		scheme_card_number=in_scheme_card_number,
		transaction_number=in_transaction_number,
		delivery_date=in_delivery_date,
		delivery_address=in_delivery_address,
		pay_terms=in_pay_terms,
		terms_conditions=in_terms_conditions,
		authorised_by_user_detail_id=@authorised_by_user_detail_id,
		authorise_date=in_authorise_date,
		pay_due_date=in_pay_due_date,
		expiry_date=in_expiry_date,
		from_date=in_from_date,
		to_date=in_to_date,
		duration_type=in_duration_type,
		site_id=in_site_id,
		transactor_rep=in_transactor_rep,
		transactor_vehicle=in_transactor_vehicle,
		transactor_driver=in_transactor_driver,
		duration_value=in_duration_value,
		location_id=@location_id,
		status_code=in_status_code,
		status_date=in_status_date,
		delivery_mode=in_delivery_mode,
		is_processed=in_is_processed,
		is_paid=in_is_paid,
		is_cancel=in_is_cancel,
        spent_points_amount=in_spent_points_amount 
	WHERE transaction_id=in_transaction_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_transaction2;
DELIMITER //
CREATE PROCEDURE sp_update_transaction2
(
	IN in_transaction_id bigint,
	IN in_cash_discount double,
	IN in_total_vat double,
	IN in_edit_user_detail_id int,
	IN in_sub_total double,
	IN in_grand_total double,
	IN in_total_trade_discount double,
	IN in_points_awarded double,
	IN in_card_number varchar(10),
	IN in_total_std_vatable_amount double,
	IN in_total_zero_vatable_amount double,
	IN in_total_exempt_vatable_amount double,
	IN in_amount_tendered double,
	IN in_change_amount double,
	IN in_total_profit_margin double,
    IN in_spent_points_amount double
) 
BEGIN 

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	UPDATE transaction SET 
		cash_discount=in_cash_discount,
		total_vat=in_total_vat,
		edit_user_detail_id=in_edit_user_detail_id,
		edit_date=@cur_sys_datetime,
		sub_total=in_sub_total,
		grand_total=in_grand_total,
		total_trade_discount=in_total_trade_discount,
		points_awarded=in_points_awarded,
		card_number=in_card_number,
		total_std_vatable_amount=in_total_std_vatable_amount,
		total_zero_vatable_amount=in_total_zero_vatable_amount,
		total_exempt_vatable_amount=in_total_exempt_vatable_amount,
		amount_tendered=in_amount_tendered,
		change_amount=in_change_amount,
		total_profit_margin=in_total_profit_margin,
        spent_points_amount=in_spent_points_amount 
	WHERE transaction_id=in_transaction_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_by_id
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT * FROM transaction t 
		WHERE t.transaction_id=in_transaction_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_by_id_type;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_by_id_type
(
	IN in_transaction_id bigint,
	IN in_transaction_type_id int
) 
BEGIN 
		SELECT * FROM transaction t 
		WHERE t.transaction_id=in_transaction_id AND t.transaction_type_id=in_transaction_type_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_by_number_type;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_by_number_type
(
	IN in_transaction_number varchar(50),
	IN in_transaction_type_id int
) 
BEGIN 
		SELECT * FROM transaction t 
		WHERE t.transaction_number=in_transaction_number AND t.transaction_type_id=in_transaction_type_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_by_store_id_type;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_by_store_id_type
(
	IN in_store_id int,
	IN in_transaction_id bigint,
	IN in_transaction_type_id int
) 
BEGIN 
		SELECT * FROM transaction t 
		WHERE t.transaction_id=in_transaction_id AND t.transaction_type_id=in_transaction_type_id AND t.store_id=in_store_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_by_store_number_type;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_by_store_number_type
(
	IN in_store_id int,
	IN in_transaction_number varchar(50),
	IN in_transaction_type_id int
) 
BEGIN 
		SELECT * FROM transaction t 
		WHERE t.transaction_number=in_transaction_number AND t.transaction_type_id=in_transaction_type_id AND t.store_id=in_store_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_by_transactor_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_by_transactor_id
(
	IN in_transactor_id bigint 
) 
BEGIN 
		SELECT * FROM transaction t 
		WHERE t.transactor_id=in_transactor_id 
		ORDER BY t.transaction_id DESC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_by_transactor_transtype;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_by_transactor_transtype
(
	IN in_transactor_id bigint,
	IN in_transaction_type_id int
) 
BEGIN 
		SELECT * FROM transaction t 
		WHERE t.transactor_id=in_transactor_id AND t.transaction_type_id=in_transaction_type_id 
		ORDER BY t.transaction_id DESC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_by_bill_transactor_transtype;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_by_bill_transactor_transtype
(
	IN in_bill_transactor_id bigint,
	IN in_transaction_type_id int
) 
BEGIN 
		SELECT * FROM transaction t 
		WHERE t.bill_transactor_id=in_bill_transactor_id AND t.transaction_type_id=in_transaction_type_id 
		ORDER BY t.transaction_id DESC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_by_number;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_by_number
(
	IN in_transaction_number varchar(50) 
) 
BEGIN 
		SELECT * FROM transaction t 
		WHERE t.transaction_number=in_transaction_number;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_item_by_id
(
	IN in_item_id bigint 
) 
BEGIN 
		SELECT * FROM view_item 
		WHERE item_id=in_item_id 
		ORDER BY description ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_by_id_and_code;
DELIMITER //
CREATE PROCEDURE sp_search_item_by_id_and_code
(
	IN in_item_id bigint,
	IN in_item_code varchar(50)  
) 
BEGIN 
		SELECT * FROM view_item 
		WHERE item_id=in_item_id OR item_code=in_item_code 
		ORDER BY description ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_by_code_desc;
DELIMITER //
CREATE PROCEDURE sp_search_item_by_code_desc
(
	IN in_code_desc varchar(100) 
) 
BEGIN  
		SET @spaces=length(in_code_desc)-length(replace(in_code_desc,' ',''));
		SET @word1=SUBSTRING_INDEX(in_code_desc, ' ', 1);
        SET @word2=SUBSTRING_INDEX(in_code_desc, ' ', -1);
        
        if (@spaces=1) then 
			SELECT distinct i.* FROM view_item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE (i.description LIKE concat('%',@word1,'%') AND i.description LIKE concat('%',@word2,'%')) OR 
            (i.item_code=in_code_desc OR ic.item_code=in_code_desc OR i.alias_name LIKE concat('%',in_code_desc,'%')) 
			ORDER BY i.description ASC LIMIT 15;
        else 
			SELECT distinct i.* FROM view_item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.description LIKE concat('%',in_code_desc,'%') OR i.item_code=in_code_desc OR ic.item_code=in_code_desc OR i.alias_name LIKE concat('%',in_code_desc,'%') 
			ORDER BY i.description ASC LIMIT 15;
		end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_by_code_desc_old;
DELIMITER //
CREATE PROCEDURE sp_search_item_by_code_desc_old
(
	IN in_code_desc varchar(100) 
) 
BEGIN  
		SELECT * FROM view_item 
		WHERE description LIKE concat('%',in_code_desc,'%') OR item_code=in_code_desc OR alias_name LIKE concat('%',in_code_desc,'%') 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_by_code_desc_purpose;
DELIMITER //
CREATE PROCEDURE sp_search_item_by_code_desc_purpose
(
	IN in_code_desc varchar(100),
	IN in_is_asset int,
	IN in_is_sale int
) 
BEGIN  
		SET @spaces=length(in_code_desc)-length(replace(in_code_desc,' ',''));
		SET @word1=SUBSTRING_INDEX(in_code_desc, ' ', 1);
        SET @word2=SUBSTRING_INDEX(in_code_desc, ' ', -1);
        
        if (@spaces=1) then 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_asset=in_is_asset AND i.is_sale=in_is_sale AND (
            (i.description LIKE concat('%',@word1,'%') AND i.description LIKE concat('%',@word2,'%')) OR  
			(i.item_code=in_code_desc OR i.alias_name LIKE concat('%',in_code_desc,'%') OR ic.item_code=in_code_desc)
            ) 
			ORDER BY i.description ASC LIMIT 15;
        else 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_asset=in_is_asset AND i.is_sale=in_is_sale AND 
			(i.description LIKE concat('%',in_code_desc,'%') OR i.item_code=in_code_desc OR i.alias_name LIKE concat('%',in_code_desc,'%') OR ic.item_code=in_code_desc) 
			ORDER BY i.description ASC LIMIT 15;
        end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_by_code_desc_purpose_old;
DELIMITER //
CREATE PROCEDURE sp_search_item_by_code_desc_purpose_old
(
	IN in_code_desc varchar(100),
	IN in_is_asset int,
	IN in_is_sale int
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_asset=in_is_asset AND is_sale=in_is_sale AND (description LIKE concat('%',in_code_desc,'%') OR item_code=in_code_desc OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_active_by_code_desc;
DELIMITER //
CREATE PROCEDURE sp_search_item_active_by_code_desc
(
	IN in_code_desc varchar(100) 
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND (description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%') OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_active_by_code_desc2;
DELIMITER //
CREATE PROCEDURE sp_search_item_active_by_code_desc2
(
	IN in_code_desc varchar(100) 
) 
BEGIN  
		SELECT i.*,c.category_name,sc.sub_category_name,u.unit_symbol 
		FROM item i 
		INNER JOIN category c ON i.category_id=c.category_id 
		INNER JOIN unit u ON i.unit_id=u.unit_id 
		LEFT JOIN sub_category sc ON i.sub_category_id=sc.sub_category_id 
		WHERE i.is_suspended='No' AND (i.description LIKE concat('%',in_code_desc,'%') OR i.item_code=in_code_desc OR i.alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_sale_old;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_sale_old
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND is_sale=1 AND is_asset=0 AND 
		(description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%') OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_sale;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_sale
(
	IN in_code_desc varchar(100)
) 
BEGIN 
		SET @spaces=length(in_code_desc)-length(replace(in_code_desc,' ',''));
		SET @word1=SUBSTRING_INDEX(in_code_desc, ' ', 1);
        SET @word2=SUBSTRING_INDEX(in_code_desc, ' ', -1);
        
        if (@spaces=1) then 
		    SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_sale=1 AND i.is_asset=0 AND (
            (i.description LIKE concat('%',@word1,'%') AND i.description LIKE concat('%',@word2,'%')) OR 
			(i.item_code LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR i.item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%') OR i.alias_name LIKE concat('%',in_code_desc,'%') 
			OR ic.item_code LIKE concat('%',in_code_desc,'%') OR ic.item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR ic.item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%'))
            )
			ORDER BY i.description ASC LIMIT 15;
		else 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_sale=1 AND i.is_asset=0 AND 
			(i.description LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR i.item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%') OR i.alias_name LIKE concat('%',in_code_desc,'%') 
			OR ic.item_code LIKE concat('%',in_code_desc,'%') OR ic.item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR ic.item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%')) 
			ORDER BY i.description ASC LIMIT 15;
		end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_sale_limit100;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_sale_limit100
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SET @spaces=length(in_code_desc)-length(replace(in_code_desc,' ',''));
		SET @word1=SUBSTRING_INDEX(in_code_desc, ' ', 1);
        SET @word2=SUBSTRING_INDEX(in_code_desc, ' ', -1);
        if (@spaces=1) then 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_sale=1 AND i.is_asset=0  AND (
            (i.description LIKE concat('%',@word1,'%') AND i.description LIKE concat('%',@word2,'%')) OR  
			(i.item_code LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR i.item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%') OR i.alias_name LIKE concat('%',in_code_desc,'%') 
			OR ic.item_code LIKE concat('%',in_code_desc,'%') OR ic.item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR ic.item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%'))
            ) 
			ORDER BY description ASC LIMIT 100;
        else 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_sale=1 AND i.is_asset=0 AND 
			(i.description LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR i.item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%') OR i.alias_name LIKE concat('%',in_code_desc,'%') 
			OR ic.item_code LIKE concat('%',in_code_desc,'%') OR ic.item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR ic.item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%')) 
			ORDER BY description ASC LIMIT 100;
        end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_sale_limit100_old;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_sale_limit100_old
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND is_sale=1 AND is_asset=0 AND 
		(description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%') OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 100;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_hire;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_hire
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND is_hire=1 AND 
		(description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%') OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_stock_dispose;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_stock_dispose
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SET @spaces=length(in_code_desc)-length(replace(in_code_desc,' ',''));
		SET @word1=SUBSTRING_INDEX(in_code_desc, ' ', 1);
        SET @word2=SUBSTRING_INDEX(in_code_desc, ' ', -1);
        
        if (@spaces=1) then 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_asset=0 AND i.is_track=1 AND (
            (i.description LIKE concat('%',@word1,'%') AND i.description LIKE concat('%',@word2,'%')) OR  
			(i.item_code LIKE concat('%',in_code_desc,'%') OR i.alias_name LIKE concat('%',in_code_desc,'%') OR ic.item_code LIKE concat('%',in_code_desc,'%')) 
            )
			ORDER BY i.description ASC LIMIT 15;
        else 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_asset=0 AND i.is_track=1 AND 
			(i.description LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',in_code_desc,'%') OR i.alias_name LIKE concat('%',in_code_desc,'%') OR ic.item_code LIKE concat('%',in_code_desc,'%')) 
			ORDER BY i.description ASC LIMIT 15;
        end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_consumption;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_consumption
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND is_asset=0 AND is_track=1 AND is_sale=0 AND is_buy=1 AND expense_type='Consumption' AND 
		(description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%') OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_transfer;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_transfer
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND is_track=1 AND is_asset=0 AND is_sale=1 AND 
		(description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%') OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_unpack;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_unpack
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SET @spaces=length(in_code_desc)-length(replace(in_code_desc,' ',''));
		SET @word1=SUBSTRING_INDEX(in_code_desc, ' ', 1);
        SET @word2=SUBSTRING_INDEX(in_code_desc, ' ', -1);
        
        if (@spaces=1) then 
			SELECT  distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE is_suspended='No' AND is_track=1 AND is_asset=0 AND (
            (i.description LIKE concat('%',@word1,'%') AND i.description LIKE concat('%',@word2,'%')) OR  
			(i.item_code LIKE concat('%',in_code_desc,'%') OR i.alias_name LIKE concat('%',in_code_desc,'%') OR ic.item_code LIKE concat('%',in_code_desc,'%')) 
            ) 
			ORDER BY i.description ASC LIMIT 10;
        else 
			SELECT  distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE is_suspended='No' AND is_track=1 AND is_asset=0 AND 
			(i.description LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',in_code_desc,'%') OR i.alias_name LIKE concat('%',in_code_desc,'%') OR ic.item_code LIKE concat('%',in_code_desc,'%')) 
			ORDER BY i.description ASC LIMIT 10;
		end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_purchase_old;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_purchase_old
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND is_buy=1 AND 
		(description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%') OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_purchase;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_purchase
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SET @spaces=length(in_code_desc)-length(replace(in_code_desc,' ',''));
		SET @word1=SUBSTRING_INDEX(in_code_desc, ' ', 1);
        SET @word2=SUBSTRING_INDEX(in_code_desc, ' ', -1);
        
        if (@spaces=1) then 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_buy=1 AND (
            (i.description LIKE concat('%',@word1,'%') AND i.description LIKE concat('%',@word2,'%')) OR 
			(i.item_code LIKE concat('%',in_code_desc,'%') OR i.alias_name LIKE concat('%',in_code_desc,'%') 
			OR ic.item_code LIKE concat('%',in_code_desc,'%'))
            ) 
			ORDER BY i.description ASC LIMIT 10;
        else 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_buy=1 AND 
			(i.description LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',in_code_desc,'%') OR i.alias_name LIKE concat('%',in_code_desc,'%') 
			OR ic.item_code LIKE concat('%',in_code_desc,'%')) 
			ORDER BY i.description ASC LIMIT 10;
        end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_purchase_goods;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_purchase_goods
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SET @spaces=length(in_code_desc)-length(replace(in_code_desc,' ',''));
		SET @word1=SUBSTRING_INDEX(in_code_desc, ' ', 1);
        SET @word2=SUBSTRING_INDEX(in_code_desc, ' ', -1);
        
        if (@spaces=1) then 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_buy=1 AND i.is_sale=1 AND i.is_asset=0 AND (
            (i.description LIKE concat('%',@word1,'%') AND i.description LIKE concat('%',@word2,'%')) OR 
			(i.item_code LIKE concat('%',in_code_desc,'%') OR i.alias_name LIKE concat('%',in_code_desc,'%') 
			OR ic.item_code LIKE concat('%',in_code_desc,'%'))
            ) 
			ORDER BY i.description ASC LIMIT 15;
        else 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_buy=1 AND i.is_sale=1 AND i.is_asset=0 AND 
			(i.description LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',in_code_desc,'%') OR i.alias_name LIKE concat('%',in_code_desc,'%') 
			OR ic.item_code LIKE concat('%',in_code_desc,'%')) 
			ORDER BY i.description ASC LIMIT 15;
        end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_purchase_goods_old;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_purchase_goods_old
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND is_buy=1 AND is_sale=1 AND is_asset=0 AND 
		(description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%') OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_purchase_expense;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_purchase_expense
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND is_buy=1 AND is_sale=0 AND is_asset=0 AND 
		(description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%') OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_asset_fixed;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_asset_fixed
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND is_asset=1 AND is_buy=1 AND is_sale=0 AND 
		(description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%') OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_asset_fixed_hire;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_asset_fixed_hire
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND is_asset=1 AND is_buy=1 AND is_sale=0 AND is_hire=1 AND 
		(description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%') OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_receive_goods;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_receive_goods
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SET @spaces=length(in_code_desc)-length(replace(in_code_desc,' ',''));
		SET @word1=SUBSTRING_INDEX(in_code_desc, ' ', 1);
        SET @word2=SUBSTRING_INDEX(in_code_desc, ' ', -1);
        
        if (@spaces=1) then 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_buy=1 AND i.is_sale=1 AND i.is_asset=0 AND i.is_track=1 AND (
            (i.description LIKE concat('%',@word1,'%') AND i.description LIKE concat('%',@word2,'%')) OR  
			(i.item_code LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR i.item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%') OR i.alias_name LIKE concat('%',in_code_desc,'%') 
			OR ic.item_code LIKE concat('%',in_code_desc,'%') OR ic.item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR ic.item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%'))
            ) 
			ORDER BY i.description ASC LIMIT 15;
        else 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_buy=1 AND i.is_sale=1 AND i.is_asset=0 AND i.is_track=1 AND 
			(i.description LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR i.item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%') OR i.alias_name LIKE concat('%',in_code_desc,'%') 
			OR ic.item_code LIKE concat('%',in_code_desc,'%') OR ic.item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR ic.item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%')) 
			ORDER BY i.description ASC LIMIT 15;
        end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_receive_goods_old;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_receive_goods_old
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND is_buy=1 AND is_sale=1 AND is_asset=0 AND is_track=1 AND 
		(description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',SUBSTRING(in_code_desc,2),'%') OR item_code LIKE concat('%',SUBSTRING(in_code_desc,3),'%') OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_receive_expenses;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_receive_expenses
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND is_buy=1 AND is_sale=0 AND is_asset=0 AND is_track=1 AND 
		(description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%') OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_receive_assets;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_receive_assets
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND is_buy=1 AND is_sale=0 AND is_asset=1 AND is_track=1 AND 
		(description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%') OR alias_name LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_by_code;
DELIMITER //
CREATE PROCEDURE sp_search_item_by_code
(
	IN in_item_code varchar(255)
) 
BEGIN 
		SELECT * FROM view_item 
		WHERE item_code=in_item_code 
		ORDER BY description ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_by_unit;
DELIMITER //
CREATE PROCEDURE sp_search_item_by_unit
(
	IN in_unit_id int 
) 
BEGIN 
		SELECT * FROM view_item 
		WHERE unit_id=in_unit_id 
		ORDER BY description ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_by_category;
DELIMITER //
CREATE PROCEDURE sp_search_item_by_category
(
	IN in_category_id int 
) 
BEGIN 
		SELECT * FROM view_item 
		WHERE category_id=in_category_id 
		ORDER BY description ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_active_by_code;
DELIMITER //
CREATE PROCEDURE sp_search_item_active_by_code
(
	IN in_item_code varchar(255),
    IN in_item_code_error int
) 
BEGIN 
	if(in_item_code_error=0) then
		SELECT * FROM view_item 
		WHERE item_code=in_item_code AND is_suspended='No' 
		ORDER BY description ASC; 
    else 
		SELECT * FROM view_item 
		WHERE (item_code=in_item_code OR item_code=SUBSTRING(in_item_code,2) OR item_code=SUBSTRING(in_item_code,3)) AND is_suspended='No' 
		ORDER BY description ASC; 
    end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_active_by_code_old;
DELIMITER //
CREATE PROCEDURE sp_search_item_active_by_code_old
(
	IN in_item_code varchar(255)
) 
BEGIN 
		SELECT * FROM view_item 
		WHERE (item_code=in_item_code OR item_code=SUBSTRING(in_item_code,2) OR item_code=SUBSTRING(in_item_code,3)) AND is_suspended='No' 
		ORDER BY description ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_active_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_item_active_by_id
(
	IN in_item_id bigint
) 
BEGIN 
		SELECT * FROM view_item 
		WHERE item_id=in_item_id AND is_suspended='No';
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_stock_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_stock_by_id
(
	IN in_stock_id bigint 
) 
BEGIN 
		SELECT * FROM stock s 
		WHERE s.stock_id=in_stock_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_stock_by_store_item;
DELIMITER //
CREATE PROCEDURE sp_search_stock_by_store_item
(
		IN in_store_id int,
		IN in_item_id bigint 
) 
BEGIN 
		SELECT * FROM stock s 
		WHERE s.store_id=in_store_id AND s.item_id=in_item_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_stock_distinct_batch_by_store_item;
DELIMITER //
CREATE PROCEDURE sp_search_stock_distinct_batch_by_store_item
(
		IN in_store_id int,
		IN in_item_id bigint 
) 
BEGIN 
		SELECT distinct batchno,item_exp_date FROM stock s 
		WHERE s.store_id=in_store_id AND s.item_id=in_item_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_stock_by_store_id;
DELIMITER //
CREATE PROCEDURE sp_search_stock_by_store_id
(
		IN in_store_id int
) 
BEGIN 
		SELECT * FROM stock s 
		WHERE s.store_id=in_store_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_stock_by_item_id;
DELIMITER //
CREATE PROCEDURE sp_search_stock_by_item_id
(
		IN in_item_id bigint 
) 
BEGIN 
		SELECT * FROM stock s 
		WHERE s.item_id=in_item_id ORDER BY s.store_id,s.item_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_delete_zero_qty_stock;
DELIMITER //
CREATE PROCEDURE sp_delete_zero_qty_stock() 
BEGIN 
		DELETE FROM stock WHERE currentqty=0;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_stock_by_store_item_batch;
DELIMITER //
CREATE PROCEDURE sp_search_stock_by_store_item_batch
(
		IN in_store_id int,
		IN in_item_id bigint,
		IN in_batchno varchar(100) 
) 
BEGIN 

		SET @BatchNo='';
		if (in_batchno is not null) then 
			SET @BatchNo=in_batchno;
		end if;
		SELECT * FROM stock s 
		WHERE s.store_id=in_store_id AND s.item_id=in_item_id AND s.batchno=@BatchNo;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_stock_bms;
DELIMITER //
CREATE PROCEDURE sp_search_stock_bms
(
		IN in_store_id int,
		IN in_item_id bigint,
		IN in_batchno varchar(100),
		IN in_code_specific varchar(250),
		IN in_desc_specific varchar(250)
) 
BEGIN 

		SET @BatchNo='';
		if (in_batchno is not null) then 
			SET @BatchNo=in_batchno;
		end if;
		SET @in_code_specific='';
		if (in_code_specific is not null) then 
			SET @in_code_specific=in_code_specific;
		end if;
		SET @in_desc_specific='';
		if (in_desc_specific is not null) then 
			SET @in_desc_specific=in_desc_specific;
		end if;
		SELECT * FROM stock s 
		WHERE s.store_id=in_store_id AND s.item_id=in_item_id AND s.batchno=@BatchNo 
		AND s.code_specific=@in_code_specific AND s.desc_specific=@in_desc_specific;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_stock_bms_any_store;
DELIMITER //
CREATE PROCEDURE sp_search_stock_bms_any_store
(
		IN in_item_id bigint,
		IN in_batchno varchar(100),
		IN in_code_specific varchar(250),
		IN in_desc_specific varchar(250)
) 
BEGIN 

		SET @BatchNo='';
		if (in_batchno is not null) then 
			SET @BatchNo=in_batchno;
		end if;
		SET @in_code_specific='';
		if (in_code_specific is not null) then 
			SET @in_code_specific=in_code_specific;
		end if;
		SET @in_desc_specific='';
		if (in_desc_specific is not null) then 
			SET @in_desc_specific=in_desc_specific;
		end if;
		SELECT * FROM stock s 
		WHERE s.item_id=in_item_id AND s.batchno=@BatchNo 
		AND s.code_specific=@in_code_specific AND s.desc_specific=@in_desc_specific LIMIT 1;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_stock;
DELIMITER //
CREATE PROCEDURE sp_insert_stock
(
		IN in_store_id int,
		IN in_item_id bigint,
		IN in_batchno varchar(100),
		IN in_currentqty double,
		IN in_item_mnf_date date,
		IN in_item_exp_date date,
		IN in_unit_cost double,
		IN in_code_specific varchar(250),
		IN in_desc_specific varchar(250),
		IN in_desc_more varchar(250),
		IN in_warranty_desc varchar(150),
		IN in_warranty_expiry_date date,
		IN in_purchase_date date,
		IN in_dep_start_date date,
		IN in_dep_method_id int,
		IN in_dep_rate double,
		IN in_average_method_id int,
		IN in_effective_life int,
		IN in_account_code varchar(20),
		IN in_residual_value double,
		IN in_asset_status_id int,
		IN in_asset_status_desc varchar(100),
		IN in_qty_damage double,
		IN in_specific_size double
) 
BEGIN 
		SET @new_id=0;
		CALL sp_get_new_id("stock","stock_id",@new_id);

		SET @in_item_exp_date=NULL;
		if (in_item_exp_date is not null) then
			set @in_item_exp_date=in_item_exp_date;
		end if;
		SET @in_item_mnf_date=NULL;
		if (in_item_mnf_date is not null) then
			set @in_item_mnf_date=in_item_mnf_date;
		end if;
		SET @in_warranty_desc=NULL;
		if (in_warranty_desc!="") then
			set @in_warranty_desc=in_warranty_desc;
		end if;
		SET @in_warranty_expiry_date=NULL;
		if (in_warranty_expiry_date is not null) then
			set @in_warranty_expiry_date=in_warranty_expiry_date;
		end if;
		SET @in_purchase_date=NULL;
		if (in_purchase_date is not null) then
			set @in_purchase_date=in_purchase_date;
		end if;
		SET @in_dep_start_date=NULL;
		if (in_dep_start_date is not null) then
			set @in_dep_start_date=in_dep_start_date;
		end if;
		SET @in_dep_method_id=NULL;
		if (in_dep_method_id!=0) then
			set @in_dep_method_id=in_dep_method_id;
		end if;
		SET @in_average_method_id=NULL;
		if (in_average_method_id!=0) then
			set @in_average_method_id=in_average_method_id;
		end if;
		SET @in_asset_status_id=NULL;
		if (in_asset_status_id!=0) then
			set @in_asset_status_id=in_asset_status_id;
		end if;
		SET @in_code_specific='';
		if (in_code_specific is not null) then 
			set @in_code_specific=in_code_specific;
		end if;
		SET @in_desc_specific='';
		if (in_desc_specific is not null) then 
			set @in_desc_specific=in_desc_specific;
		end if;
		SET @in_specific_size=1;
		if (in_specific_size!=0) then
			set @in_specific_size=in_specific_size;
		end if;

		INSERT INTO stock
		(
		stock_id,
		store_id,
		item_id,
		batchno,
		currentqty,
		item_mnf_date,
		item_exp_date,
		unit_cost,
		code_specific,
		desc_specific,
		desc_more,
		warranty_desc,
		warranty_expiry_date,
		purchase_date,
		dep_start_date,
		dep_method_id,
		dep_rate,
		average_method_id,
		effective_life,
		account_code,
		residual_value,
		asset_status_id,
		asset_status_desc,
		qty_damage,
		specific_size
	) 
    VALUES
	(
		@new_id,
		in_store_id,
		in_item_id,
		in_batchno,
		in_currentqty,
		@in_item_mnf_date,
		@in_item_exp_date,
		in_unit_cost,
		@in_code_specific,
		@in_desc_specific,
		in_desc_more,
		@in_warranty_desc,
		@in_warranty_expiry_date,
		@in_purchase_date,
		@in_dep_start_date,
		@in_dep_method_id,
		in_dep_rate,
		@in_average_method_id,
		in_effective_life,
		in_account_code,
		in_residual_value,
		@in_asset_status_id,
		in_asset_status_desc,
		in_qty_damage,
		@in_specific_size
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_stock;
DELIMITER //
CREATE PROCEDURE sp_update_stock
(
		IN in_stock_id bigint,
		IN in_store_id int,
		IN in_item_id bigint,
		IN in_batchno varchar(100),
		IN in_currentqty double,
		IN in_item_mnf_date date,
		IN in_item_exp_date date,
		IN in_unit_cost double,
		IN in_code_specific varchar(250),
		IN in_desc_specific varchar(250),
		IN in_desc_more varchar(250),
		IN in_warranty_desc varchar(150),
		IN in_warranty_expiry_date date,
		IN in_purchase_date date,
		IN in_dep_start_date date,
		IN in_dep_method_id int,
		IN in_dep_rate double,
		IN in_average_method_id int,
		IN in_effective_life int,
		IN in_account_code varchar(20),
		IN in_residual_value double,
		IN in_asset_status_id int,
		IN in_asset_status_desc varchar(100),
		IN in_qty_damage double,
		IN in_specific_size double
) 
BEGIN 
		SET @in_item_exp_date=NULL;
		if (in_item_exp_date is not null) then
			set @in_item_exp_date=in_item_exp_date;
		end if;
		SET @in_item_mnf_date=NULL;
		if (in_item_mnf_date is not null) then
			set @in_item_mnf_date=in_item_mnf_date;
		end if;
		SET @in_warranty_desc=NULL;
		if (in_warranty_desc!="") then
			set @in_warranty_desc=in_warranty_desc;
		end if;
		SET @in_warranty_expiry_date=NULL;
		if (in_warranty_expiry_date is not null) then
			set @in_warranty_expiry_date=in_warranty_expiry_date;
		end if;
		SET @in_purchase_date=NULL;
		if (in_purchase_date is not null) then
			set @in_purchase_date=in_purchase_date;
		end if;
		SET @in_dep_start_date=NULL;
		if (in_dep_start_date is not null) then
			set @in_dep_start_date=in_dep_start_date;
		end if;
		SET @in_dep_method_id=NULL;
		if (in_dep_method_id!=0) then
			set @in_dep_method_id=in_dep_method_id;
		end if;
		SET @in_average_method_id=NULL;
		if (in_average_method_id!=0) then
			set @in_average_method_id=in_average_method_id;
		end if;
		SET @in_asset_status_id=NULL;
		if (in_asset_status_id!=0) then
			set @in_asset_status_id=in_asset_status_id;
		end if;
		SET @in_specific_size=1;
		if (in_specific_size!=0) then
			set @in_specific_size=in_specific_size;
		end if;

		UPDATE stock SET 
		store_id=in_store_id,
		item_id=in_item_id,
		batchno=in_batchno,
		currentqty=in_currentqty,
		item_mnf_date=@in_item_mnf_date,
		item_exp_date=@in_item_exp_date,
		unit_cost=in_unit_cost,
		code_specific=in_code_specific,
		desc_specific=in_desc_specific,
		desc_more=in_desc_more,
		warranty_desc=@in_warranty_desc,
		warranty_expiry_date=@in_warranty_expiry_date,
		purchase_date=@in_purchase_date,
		dep_start_date=@in_dep_start_date,
		dep_method_id=@in_dep_method_id,
		dep_rate=in_dep_rate,
		average_method_id=@in_average_method_id,
		effective_life=in_effective_life,
		account_code=in_account_code,
		residual_value=in_residual_value,
		asset_status_id=@in_asset_status_id,
		asset_status_desc=in_asset_status_desc,
		qty_damage=in_qty_damage,
		specific_size=@in_specific_size 
		WHERE stock_id=in_stock_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_add_stock_by_store_item_batch;
DELIMITER //
CREATE PROCEDURE sp_add_stock_by_store_item_batch
(
		IN in_store_id int,
		IN in_item_id bigint,
		IN in_batchno varchar(100),
		IN in_qty double 
) 
BEGIN 
		UPDATE stock s SET currentqty=currentqty+in_qty 
		WHERE s.store_id=in_store_id AND s.item_id=in_item_id AND s.batchno=in_batchno;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_subtract_stock_by_store_item_batch;
DELIMITER //
CREATE PROCEDURE sp_subtract_stock_by_store_item_batch
(
		IN in_store_id int,
		IN in_item_id bigint,
		IN in_batchno varchar(100),
		IN in_qty double 
) 
BEGIN 
		UPDATE stock s SET currentqty=currentqty-in_qty 
		WHERE s.store_id=in_store_id AND s.item_id=in_item_id AND s.batchno=in_batchno;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_add_stock_bms;
DELIMITER //
CREATE PROCEDURE sp_add_stock_bms
(
		IN in_store_id int,
		IN in_item_id bigint,
		IN in_batchno varchar(100),
		IN in_code_specific varchar(250),
		IN in_desc_specific varchar(250),
		IN in_qty double,
		IN in_unit_cost double
) 
BEGIN 
		SET @in_code_specific='';
		if (in_code_specific is not null) then 
			set @in_code_specific=in_code_specific;
		end if;
		SET @in_desc_specific='';
		if (in_desc_specific is not null) then 
			set @in_desc_specific=in_desc_specific;
		end if;
	
		if (in_unit_cost<=0) then 
			UPDATE stock s SET currentqty=currentqty+in_qty 
			WHERE s.store_id=in_store_id AND s.item_id=in_item_id AND s.batchno=in_batchno 
			AND s.code_specific=@in_code_specific AND s.desc_specific=@in_desc_specific;
		end if;
		if (in_unit_cost>0) then 
			UPDATE stock s SET currentqty=currentqty+in_qty,unit_cost=in_unit_cost 
			WHERE s.store_id=in_store_id AND s.item_id=in_item_id AND s.batchno=in_batchno 
			AND s.code_specific=@in_code_specific AND s.desc_specific=@in_desc_specific;
		end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_subtract_stock_bms;
DELIMITER //
CREATE PROCEDURE sp_subtract_stock_bms
(
		IN in_store_id int,
		IN in_item_id bigint,
		IN in_batchno varchar(100),
		IN in_code_specific varchar(250),
		IN in_desc_specific varchar(250),
		IN in_qty double 
) 
BEGIN 
		SET @in_code_specific='';
		if (in_code_specific is not null) then 
			set @in_code_specific=in_code_specific;
		end if;
		SET @in_desc_specific='';
		if (in_desc_specific is not null) then 
			set @in_desc_specific=in_desc_specific;
		end if;
		UPDATE stock s SET currentqty=currentqty-in_qty 
		WHERE s.store_id=in_store_id AND s.item_id=in_item_id AND s.batchno=in_batchno 
		AND s.code_specific=@in_code_specific AND s.desc_specific=@in_desc_specific;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_stock_damage_add;
DELIMITER //
CREATE PROCEDURE sp_update_stock_damage_add
(
		IN in_store_id int,
		IN in_item_id bigint,
		IN in_batchno varchar(100),
		IN in_code_specific varchar(250),
		IN in_desc_specific varchar(250),
		IN in_qty double
) 
BEGIN 
		SET @in_code_specific='';
		if (in_code_specific is not null) then 
			set @in_code_specific=in_code_specific;
		end if;
		SET @in_desc_specific='';
		if (in_desc_specific is not null) then 
			set @in_desc_specific=in_desc_specific;
		end if;
	
		UPDATE stock s SET qty_damage=qty_damage+in_qty 
		WHERE s.store_id=in_store_id AND s.item_id=in_item_id AND s.batchno=in_batchno 
		AND s.code_specific=@in_code_specific AND s.desc_specific=@in_desc_specific AND qty_damage is not null;

		UPDATE stock s SET qty_damage=in_qty 
		WHERE s.store_id=in_store_id AND s.item_id=in_item_id AND s.batchno=in_batchno 
		AND s.code_specific=@in_code_specific AND s.desc_specific=@in_desc_specific AND qty_damage is null;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_stock_damage_subtract;
DELIMITER //
CREATE PROCEDURE sp_update_stock_damage_subtract
(
		IN in_store_id int,
		IN in_item_id bigint,
		IN in_batchno varchar(100),
		IN in_code_specific varchar(250),
		IN in_desc_specific varchar(250),
		IN in_qty double
) 
BEGIN 
		SET @in_code_specific='';
		if (in_code_specific is not null) then 
			set @in_code_specific=in_code_specific;
		end if;
		SET @in_desc_specific='';
		if (in_desc_specific is not null) then 
			set @in_desc_specific=in_desc_specific;
		end if;
	
		UPDATE stock s SET qty_damage=qty_damage-in_qty 
		WHERE s.store_id=in_store_id AND s.item_id=in_item_id AND s.batchno=in_batchno 
		AND s.code_specific=@in_code_specific AND s.desc_specific=@in_desc_specific AND qty_damage is not null;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_pay;
DELIMITER //
CREATE PROCEDURE sp_insert_pay
(
	IN in_pay_date date,
	IN in_paid_amount double,
	IN in_pay_method_id int,
	IN in_add_user_detail_id int,
	IN in_edit_user_detail_id int,
	IN in_add_date datetime,
	IN in_edit_date datetime,
	IN in_points_spent double,
	IN in_points_spent_amount double,
	IN in_delete_pay_id bigint,
	IN in_pay_ref_no varchar(100),
	IN in_pay_category varchar(10),
	IN in_bill_transactor_id bigint,
	IN in_pay_type_id int,
	IN in_pay_reason_id int,
	IN in_store_id int,
	IN in_acc_child_account_id int,
	IN in_acc_child_account_id2 int,
	IN in_currency_code varchar(10),
	IN in_xrate double,
	IN in_status int,
	IN in_status_desc varchar(100),
	IN in_principal_amount double,
	IN in_interest_amount double,
	OUT out_pay_id bigint,
	IN in_pay_number varchar(50)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("pay","pay_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @edit_datetime=null;
	SET @add_user_detail_id=null;
	SET @edit_user_detail_id=null;

	SET @bill_transactor_id=null;
	if (in_bill_transactor_id!=0) then 
		SET @bill_transactor_id=in_bill_transactor_id;
	end if;
	SET @delete_pay_id=null;
	if (in_delete_pay_id!=0) then 
		SET @delete_pay_id=in_delete_pay_id;
	end if;
	SET @acc_child_account_id=null;
	if (in_acc_child_account_id!=0) then 
		SET @acc_child_account_id=in_acc_child_account_id;
	end if;
	SET @acc_child_account_id2=null;
	if (in_acc_child_account_id2!=0) then 
		SET @acc_child_account_id2=in_acc_child_account_id2;
	end if;
	if (in_add_user_detail_id!=0) then 
		SET @add_user_detail_id=in_add_user_detail_id;
	end if;

	INSERT INTO pay
	(
		pay_id,
		pay_date,
		paid_amount,
		pay_method_id,
		add_user_detail_id,
		edit_user_detail_id,
		add_date,
		edit_date,
		points_spent,
		points_spent_amount,
		delete_pay_id,
		pay_ref_no,
		pay_category,
		bill_transactor_id,
		pay_type_id,
		pay_reason_id,
		store_id,
		acc_child_account_id,
		acc_child_account_id2,
		currency_code,
		xrate,
		status,
		status_desc,
		principal_amount,
		interest_amount,
		pay_number
	) 
    VALUES
	(
		@new_id,
		in_pay_date,
		in_paid_amount,
		in_pay_method_id,
		@add_user_detail_id,
		@edit_user_detail_id,
		@cur_sys_datetime,
		@edit_datetime,
		in_points_spent,
		in_points_spent_amount,
		@delete_pay_id,
		in_pay_ref_no,
		in_pay_category,
		@bill_transactor_id,
		in_pay_type_id,
		in_pay_reason_id,
		in_store_id,
		@acc_child_account_id,
		@acc_child_account_id2,
		in_currency_code,
		in_xrate,
		in_status,
		in_status_desc,
		in_principal_amount,
		in_interest_amount,
		in_pay_number
	); 
SET out_pay_id=@new_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_pay_fix;
DELIMITER //
CREATE PROCEDURE sp_insert_pay_fix
(
	IN in_pay_date date,
	IN in_paid_amount double,
	IN in_pay_method_id int,
	IN in_add_user_detail_id int,
	IN in_edit_user_detail_id int,
	IN in_add_date datetime,
	IN in_edit_date datetime,
	IN in_points_spent double,
	IN in_points_spent_amount double,
	IN in_delete_pay_id bigint,
	IN in_pay_ref_no varchar(100),
	IN in_pay_category varchar(10),
	IN in_bill_transactor_id bigint,
	IN in_pay_type_id int,
	IN in_pay_reason_id int,
	IN in_store_id int,
	IN in_acc_child_account_id int,
	IN in_acc_child_account_id2 int,
	IN in_currency_code varchar(10),
	IN in_xrate double,
	IN in_status int,
	IN in_status_desc varchar(100),
	IN in_principal_amount double,
	IN in_interest_amount double,
	OUT out_pay_id bigint,
	IN in_pay_number varchar(50)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("pay","pay_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @edit_datetime=null;
	SET @add_user_detail_id=null;
	SET @edit_user_detail_id=null;

	SET @bill_transactor_id=null;
	if (in_bill_transactor_id!=0) then 
		SET @bill_transactor_id=in_bill_transactor_id;
	end if;
	SET @delete_pay_id=null;
	if (in_delete_pay_id!=0) then 
		SET @delete_pay_id=in_delete_pay_id;
	end if;
	SET @acc_child_account_id=null;
	if (in_acc_child_account_id!=0) then 
		SET @acc_child_account_id=in_acc_child_account_id;
	end if;
	SET @acc_child_account_id2=null;
	if (in_acc_child_account_id2!=0) then 
		SET @acc_child_account_id2=in_acc_child_account_id2;
	end if;
	if (in_add_user_detail_id!=0) then 
		SET @add_user_detail_id=in_add_user_detail_id;
	end if;

	INSERT INTO pay
	(
		pay_id,
		pay_date,
		paid_amount,
		pay_method_id,
		add_user_detail_id,
		edit_user_detail_id,
		add_date,
		edit_date,
		points_spent,
		points_spent_amount,
		delete_pay_id,
		pay_ref_no,
		pay_category,
		bill_transactor_id,
		pay_type_id,
		pay_reason_id,
		store_id,
		acc_child_account_id,
		acc_child_account_id2,
		currency_code,
		xrate,
		status,
		status_desc,
		principal_amount,
		interest_amount,
		pay_number
	) 
    VALUES
	(
		@new_id,
		in_pay_date,
		in_paid_amount,
		in_pay_method_id,
		@add_user_detail_id,
		@edit_user_detail_id,
		in_add_date,
		@edit_datetime,
		in_points_spent,
		in_points_spent_amount,
		@delete_pay_id,
		in_pay_ref_no,
		in_pay_category,
		@bill_transactor_id,
		in_pay_type_id,
		in_pay_reason_id,
		in_store_id,
		@acc_child_account_id,
		@acc_child_account_id2,
		in_currency_code,
		in_xrate,
		in_status,
		in_status_desc,
		in_principal_amount,
		in_interest_amount,
		in_pay_number
	); 
SET out_pay_id=@new_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_pay;
DELIMITER //
CREATE PROCEDURE sp_update_pay
(
	IN in_pay_id bigint,
	IN in_pay_date date,
	IN in_paid_amount double,
	IN in_pay_method_id int,
	IN in_edit_user_detail_id int,
	IN in_edit_date datetime,
	IN in_points_spent double,
	IN in_points_spent_amount double,
	IN in_delete_pay_id bigint,
	IN in_pay_ref_no varchar(100),
	IN in_pay_category varchar(10),
	IN in_bill_transactor_id bigint,
	IN in_pay_type_id int,
	IN in_pay_reason_id int,
	IN in_store_id int,
	IN in_acc_child_account_id int,
	IN in_acc_child_account_id2 int,
	IN in_currency_code varchar(10),
	IN in_xrate double,
	IN in_status int,
	IN in_status_desc varchar(100),
	IN in_principal_amount double,
	IN in_interest_amount double,
	IN in_pay_number varchar(50)
) 
BEGIN 
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @delete_pay_id=null;
	if (in_delete_pay_id!=0) then 
		SET @delete_pay_id=in_delete_pay_id;
	end if;
	SET @bill_transactor_id=null;
	if (in_bill_transactor_id!=0) then 
		SET @bill_transactor_id=in_bill_transactor_id;
	end if;
	SET @acc_child_account_id=null;
	if (in_acc_child_account_id!=0) then 
		SET @acc_child_account_id=in_acc_child_account_id;
	end if;
	SET @acc_child_account_id2=null;
	if (in_acc_child_account_id2!=0) then 
		SET @acc_child_account_id2=in_acc_child_account_id2;
	end if;

	UPDATE pay SET 
		pay_date=in_pay_date,
		paid_amount=in_paid_amount,
		pay_method_id=in_pay_method_id,
		edit_user_detail_id=in_edit_user_detail_id,
		edit_date=@cur_sys_datetime,
		points_spent=in_points_spent,
		points_spent_amount=in_points_spent_amount,
		delete_pay_id=@delete_pay_id,
		pay_ref_no=in_pay_ref_no,
		pay_category=in_pay_category,
		bill_transactor_id=@bill_transactor_id,
		pay_type_id=in_pay_type_id,
		pay_reason_id=in_pay_reason_id,
		store_id=in_store_id,
		acc_child_account_id=@acc_child_account_id,
		acc_child_account_id2=@acc_child_account_id2,
		currency_code=in_currency_code,
		xrate=in_xrate,
		status=in_status,
		status_desc=in_status_desc,
		principal_amount=in_principal_amount,
		interest_amount=in_interest_amount,
		pay_number=in_pay_number 
	WHERE pay_id=in_pay_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_by_pay_id;
DELIMITER //
CREATE PROCEDURE sp_search_pay_by_pay_id
(
	IN in_pay_id bigint
) 
BEGIN 
	SELECT * FROM pay 
	WHERE pay_id=in_pay_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_by_transaction_id;
DELIMITER //
CREATE PROCEDURE sp_search_pay_by_transaction_id
(
	IN in_transaction_id bigint
) 
BEGIN 
	SELECT * FROM pay 
	WHERE transaction_id=in_transaction_id ORDER BY pay_id ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_by_transaction_id_type;
DELIMITER //
CREATE PROCEDURE sp_search_pay_by_transaction_id_type
(
	IN in_transaction_id bigint,
	IN in_transaction_type_id int
) 
BEGIN 
	SELECT * FROM pay p,transaction t 
	WHERE p.transaction_id=in_transaction_id AND p.transaction_id=t.transaction_id AND t.transaction_type_id=in_transaction_type_id 
	ORDER BY pay_id DESC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_by_transaction_id_paycat;
DELIMITER //
CREATE PROCEDURE sp_search_pay_by_transaction_id_paycat
(
	IN in_transaction_id bigint,
	IN in_pay_category varchar(10)
) 
BEGIN 
	SELECT * FROM pay p,transaction t 
	WHERE p.transaction_id=in_transaction_id AND p.transaction_id=t.transaction_id AND p.pay_category=in_pay_category 
	ORDER BY pay_id DESC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_by_transaction_number_paycat;
DELIMITER //
CREATE PROCEDURE sp_search_pay_by_transaction_number_paycat
(
	IN in_transaction_number varchar(50),
	IN in_pay_category varchar(10)
) 
BEGIN 
	SELECT * FROM pay p,transaction t 
	WHERE t.transaction_number=in_transaction_number AND p.transaction_id=t.transaction_id AND p.pay_category=in_pay_category 
	ORDER BY pay_id DESC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_first_by_transaction_id;
DELIMITER //
CREATE PROCEDURE sp_search_pay_first_by_transaction_id
(
	IN in_transaction_id bigint
) 
BEGIN 
	SELECT * FROM pay 
	WHERE transaction_id=in_transaction_id having min(pay_id);
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_first_by_transaction_number;
DELIMITER //
CREATE PROCEDURE sp_search_pay_first_by_transaction_number
(
	IN in_transaction_number varchar(50)
) 
BEGIN 
	SELECT p.* FROM pay p,pay_trans pt 
	WHERE p.pay_id=pt.pay_id and pt.transaction_number=in_transaction_number 
	having min(p.pay_id);
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_by_transactor_id;
DELIMITER //
CREATE PROCEDURE sp_search_pay_by_transactor_id
(
	IN in_transactor_id bigint
) 
BEGIN 
	SELECT * FROM pay p 
	INNER JOIN transaction t ON p.transaction_id=t.transaction_id AND t.transactor_id=in_transactor_id 
	ORDER BY p.pay_id DESC,p.transaction_id DESC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_by_transactor_transtype;
DELIMITER //
CREATE PROCEDURE sp_search_pay_by_transactor_transtype
(
	IN in_transactor_id bigint,
	IN in_transaction_type_id int
) 
BEGIN 
	SELECT * FROM pay p 
	INNER JOIN transaction t ON p.transaction_id=t.transaction_id AND t.transactor_id=in_transactor_id AND t.transaction_type_id=in_transaction_type_id 
	ORDER BY p.transaction_id DESC,p.pay_id DESC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_by_bill_transactor_transtype;
DELIMITER //
CREATE PROCEDURE sp_search_pay_by_bill_transactor_transtype
(
	IN in_bill_transactor_id bigint,
	IN in_transaction_type_id int
) 
BEGIN 
	SELECT * FROM pay p 
	INNER JOIN transaction t ON p.transaction_id=t.transaction_id AND t.bill_transactor_id=in_bill_transactor_id AND t.transaction_type_id=in_transaction_type_id 
	ORDER BY p.transaction_id DESC,p.pay_id DESC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_by_bill_transactor_paycat;
DELIMITER //
CREATE PROCEDURE sp_search_pay_by_bill_transactor_paycat
(
	IN in_bill_transactor_id bigint,
	IN in_pay_category varchar(10)
) 
BEGIN 
	SELECT * FROM pay p 
	WHERE bill_transactor_id=in_bill_transactor_id AND pay_category=in_pay_category 
	ORDER BY p.pay_id DESC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_total_trans_bal;
DELIMITER //
CREATE PROCEDURE sp_search_pay_total_trans_bal
(
	IN in_bill_transactor_id bigint,
	IN in_pay_category varchar(10)
) 
BEGIN 
	select p.bill_transactor_id,p.pay_id,p.pay_category,p.paid_amount as PaidAmt,
	(
		select IFNULL(sum(pt.trans_paid_amount),0) from pay_trans pt where pt.pay_id=p.pay_id
	) as TotTransPaidAmt,p.paid_amount-
	(
		select IFNULL(sum(pt.trans_paid_amount),0) from pay_trans pt where pt.pay_id=p.pay_id
	) as balance 
	from pay p where  p.bill_transactor_id=in_bill_transactor_id and p.pay_category=in_pay_category and 
	p.paid_amount-
	(
		select IFNULL(sum(pt.trans_paid_amount),0) from pay_trans pt where pt.pay_id=p.pay_id
	)>0 
	order by p.pay_id asc; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_total_by_transaction_id;
DELIMITER //
CREATE PROCEDURE sp_search_pay_total_by_transaction_id
(
	IN in_transaction_id bigint
) 
BEGIN 
	SELECT sum(paid_amount+points_spent_amount) AS sum_paid_amount FROM pay 
	WHERE transaction_id=in_transaction_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_count_by_delete_pay_id;
DELIMITER //
CREATE PROCEDURE sp_search_pay_count_by_delete_pay_id
(
	IN in_delete_pay_id bigint
) 
BEGIN 
	SELECT count(*) AS count_delete_pay FROM pay 
	WHERE delete_pay_id=in_delete_pay_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_branch;
DELIMITER //
CREATE PROCEDURE sp_insert_branch
(
	IN in_branch_code varchar(20),
	IN in_branch_name varchar(100) 
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("branch","branch_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	INSERT INTO branch
	(
		branch_id,
		branch_code,
		branch_name,
		add_date,
		edit_date
	) 
    VALUES
	(
		@new_id,
		in_branch_code,
		in_branch_name,
		@cur_sys_datetime,
		@cur_sys_datetime
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_branch;
DELIMITER //
CREATE PROCEDURE sp_update_branch
(
	IN in_branch_id bigint,
	IN in_branch_code varchar(20),
	IN in_branch_name varchar(100)  
) 
BEGIN 
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	UPDATE branch SET 
		branch_id=in_branch_id,
		branch_code=in_branch_code,
		branch_name=in_branch_name,
		edit_date=@cur_sys_datetime 
	WHERE branch_id=in_branch_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_branch_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_branch_by_id
(
	IN in_branch_id bigint 
) 
BEGIN 
		SELECT * FROM branch b 
		WHERE b.branch_id=in_branch_id 
		ORDER BY branch_name ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_branch_by_code;
DELIMITER //
CREATE PROCEDURE sp_search_branch_by_code
(
	IN in_branch_code varchar(20)
) 
BEGIN 
		SELECT * FROM branch b 
		WHERE b.branch_code=in_branch_code 
		ORDER BY branch_name ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_branch_by_name;
DELIMITER //
CREATE PROCEDURE sp_search_branch_by_name
(
	IN in_branch_name varchar(100)
) 
BEGIN 
		SELECT * FROM branch b 
		WHERE b.branch_name=in_branch_name 
		ORDER BY branch_name ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_branch_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_branch_by_none()
BEGIN 
		SELECT * FROM branch b 
		ORDER BY branch_name ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_points_card;
DELIMITER //
CREATE PROCEDURE sp_insert_points_card
(
	IN in_card_number varchar(10),
	IN in_card_holder varchar(100),
	IN in_email varchar(100),
	IN in_phone varchar(100),
	IN in_physical_address varchar(255),
	IN in_tax_identity varchar(100),
	IN in_account_details varchar(255),
	IN in_website varchar(100),
	IN in_cpname varchar(100),
	IN in_cptitle varchar(100),
	IN in_cpphone varchar(100),
	IN in_cpemail varchar(100),
	IN in_reg_branch_id int,
	IN in_points_balance double,
	IN in_add_date datetime,
	IN in_add_user varchar(100),
	IN in_edit_date datetime,
	IN in_edit_user varchar(100)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("points_card","points_card_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	INSERT INTO points_card
	(
		points_card_id,
		card_number,
		card_holder,
		email,
		phone,
		website,
		cpname,
		cptitle,
		cpphone,
		cpemail,
		physical_address,
		tax_identity,
		account_details,
		reg_branch_id,
		points_balance,
		add_date,
		add_user,
		edit_date,
		edit_user
	) 
    VALUES
	(
		@new_id,
		in_card_number,
		in_card_holder,
		in_email,
		in_phone,
		in_website,
		in_cpname,
		in_cptitle,
		in_cpphone,
		in_cpemail,
		in_physical_address,
		in_tax_identity,
		in_account_details,
		in_reg_branch_id,
		in_points_balance,
		@cur_sys_datetime,
		in_add_user,
		@cur_sys_datetime,
		in_edit_user
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_points_card;
DELIMITER //
CREATE PROCEDURE sp_update_points_card
(
	IN in_points_card_id bigint,
	IN in_card_number varchar(10),
	IN in_card_holder varchar(100),
	IN in_email varchar(100),
	IN in_phone varchar(100),
	IN in_physical_address varchar(255),
	IN in_tax_identity varchar(100),
	IN in_account_details varchar(255),
	IN in_website varchar(100),
	IN in_cpname varchar(100),
	IN in_cptitle varchar(100),
	IN in_cpphone varchar(100),
	IN in_cpemail varchar(100),
	IN in_reg_branch_id int,
	IN in_points_balance double,
	IN in_edit_date datetime,
	IN in_edit_user varchar(100)
) 
BEGIN 
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	UPDATE points_card SET 
		points_card_id=in_points_card_id,
		card_number=in_card_number,
		card_holder=in_card_holder,
		email=in_email,
		phone=in_phone,
		website=in_website,
		cpname=in_cpname,
		cptitle=in_cptitle,
		cpphone=in_cpphone,
		cpemail=in_cpemail,
		physical_address=in_physical_address,
		tax_identity=in_tax_identity,
		account_details=in_account_details,
		reg_branch_id=in_reg_branch_id,
		points_balance=in_points_balance,
		edit_date=@cur_sys_datetime,
		edit_user=in_edit_user
	WHERE points_card_id=in_points_card_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_points_card_balance;
DELIMITER //
CREATE PROCEDURE sp_update_points_card_balance
(
	IN in_card_number varchar(10),
	IN in_points double 
) 
BEGIN 
		UPDATE points_card p 
		SET p.points_balance=in_points 
		WHERE p.card_number=in_card_number;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_add_points_card_balance_by_card_no;
DELIMITER //
CREATE PROCEDURE sp_add_points_card_balance_by_card_no
(
	IN in_card_number varchar(10),
	IN in_points double 
) 
BEGIN 
		UPDATE points_card p 
		SET p.points_balance=p.points_balance + in_points 
		WHERE p.card_number=in_card_number;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_add_points_card_balance_by_card_id;
DELIMITER //
CREATE PROCEDURE sp_add_points_card_balance_by_card_id
(
	IN in_points_card_id bigint,
	IN in_points double 
) 
BEGIN 
		UPDATE points_card p 
		SET p.points_balance=p.points_balance + in_points 
		WHERE p.points_card_id=in_points_card_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_subtract_points_card_balance;
DELIMITER //
CREATE PROCEDURE sp_subtract_points_card_balance
(
	IN in_card_number varchar(10),
	IN in_points double 
) 
BEGIN 
		UPDATE points_card p 
		SET p.points_balance=p.points_balance - in_points 
		WHERE p.card_number=in_card_number;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_points_card_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_points_card_by_id
(
	IN in_points_card_id bigint 
) 
BEGIN 
		SELECT * FROM points_card p 
		WHERE p.points_card_id=in_points_card_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_points_card_by_card_number;
DELIMITER //
CREATE PROCEDURE sp_search_points_card_by_card_number
(
	IN in_card_number varchar(10) 
) 
BEGIN 
		SELECT * FROM points_card p 
		WHERE p.card_number=in_card_number;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_points_card_by_card_number_holder_names;
DELIMITER //
CREATE PROCEDURE sp_search_points_card_by_card_number_holder_names
(
	IN in_card_number varchar(10) 
) 
BEGIN 
		SELECT * FROM points_card p 
		WHERE p.card_number=in_card_number OR p.card_holder LIKE concat('%',in_card_number,'%') 
		ORDER BY card_holder ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_points_card_by_holder_names;
DELIMITER //
CREATE PROCEDURE sp_search_points_card_by_holder_names
(
	IN in_card_holder varchar(100) 
) 
BEGIN 
		SELECT * FROM points_card p 
		WHERE p.card_holder=in_card_holder;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_points_transaction;
DELIMITER //
CREATE PROCEDURE sp_insert_points_transaction
(
	IN in_points_card_id bigint,
	IN in_transaction_date date,
	IN in_points_awarded double,
	IN in_points_spent double,
	IN in_transaction_id bigint,
	IN in_trans_branch_id int,
	IN in_add_date datetime,
	IN in_add_user varchar(100),
	IN in_edit_date datetime,
	IN in_edit_user varchar(100),
	IN in_points_spent_amount double
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("points_transaction","points_transaction_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	INSERT INTO points_transaction
	(
		points_transaction_id,
		points_card_id,
		transaction_date,
		points_awarded,
		points_spent,
		transaction_id,
		trans_branch_id,
		add_date,
		add_user,
		edit_date,
		edit_user,
		points_spent_amount 
	) 
    VALUES
	(
		@new_id,
		in_points_card_id,
		in_transaction_date,
		in_points_awarded,
		in_points_spent,
		in_transaction_id,
		in_trans_branch_id,
		@cur_sys_datetime,
		in_add_user,
		@cur_sys_datetime,
		in_edit_user,
		in_points_spent_amount
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_points_transaction;
DELIMITER //
CREATE PROCEDURE sp_update_points_transaction
(
	IN in_points_transaction_id bigint,
	IN in_points_card_id bigint,
	IN in_transaction_date date,
	IN in_points_awarded double,
	IN in_points_spent double,
	IN in_transaction_id bigint,
	IN in_trans_branch_id int,
	IN in_edit_date datetime,
	IN in_edit_user varchar(100),
	IN in_points_spent_amount double
) 
BEGIN 
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	UPDATE points_transaction SET 
		points_card_id=in_points_card_id,
		transaction_date=in_transaction_date,
		points_awarded=in_points_awarded,
		points_spent=in_points_spent,
		transaction_id=in_transaction_id,
		trans_branch_id=in_trans_branch_id,
		edit_date=@cur_sys_datetime,
		edit_user=@cur_sys_datetime,
		points_spent_amount=in_points_spent_amount 
	WHERE points_transaction_id=in_points_transaction_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_points_transaction_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_points_transaction_by_id
(
	IN in_points_transaction_id bigint 
) 
BEGIN 
		SELECT * FROM points_transaction p 
		WHERE p.points_transaction_id=in_points_transaction_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_points_transaction_by_card_id;
DELIMITER //
CREATE PROCEDURE sp_search_points_transaction_by_card_id
(
	IN in_points_card_id bigint 
) 
BEGIN 
		SELECT * FROM points_transaction p 
		WHERE p.points_card_id=in_points_card_id 
		ORDER BY points_transaction_id DESC;
END//
DELIMITER ;


DROP PROCEDURE IF EXISTS sp_insert_group_detail;
DELIMITER //
CREATE PROCEDURE sp_insert_group_detail
(
	IN in_group_name varchar(50),
	IN in_is_active varchar(3) 
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("group_detail","group_detail_id",@new_id);

	INSERT INTO group_detail
	(
		group_detail_id,
		group_name,
		is_active 
	) 
    VALUES
	(
		@new_id,
		in_group_name,
		in_is_active
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_group_detail;
DELIMITER //
CREATE PROCEDURE sp_update_group_detail
(
	IN in_group_detail_id bigint,
	IN in_group_name varchar(50),
	IN in_is_active varchar(3)  
) 
BEGIN 
	UPDATE group_detail SET 
		group_detail_id=in_group_detail_id,
		group_name=in_group_name,
		is_active=in_is_active 
	WHERE group_detail_id=in_group_detail_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_group_detail_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_group_detail_by_id
(
	IN in_group_detail_id int 
) 
BEGIN 
		SELECT * FROM group_detail g 
		WHERE g.group_detail_id=in_group_detail_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_group_detail_by_name;
DELIMITER //
CREATE PROCEDURE sp_search_group_detail_by_name
(
	IN in_group_name varchar(20)
) 
BEGIN 
		SELECT * FROM group_detail g 
		WHERE g.group_name=in_group_name;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_group_detail_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_group_detail_by_none() 
BEGIN 
		SELECT * FROM group_detail g 
		ORDER BY group_name ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_group_user;
DELIMITER //
CREATE PROCEDURE sp_insert_group_user
(
	IN in_group_detail_id int,
	IN in_user_detail_id int 
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("group_user","group_user_id",@new_id);

	INSERT INTO group_user
	(
		group_user_id,
		group_detail_id,
		user_detail_id 
	) 
    VALUES
	(
		@new_id,
		in_group_detail_id,
		in_user_detail_id
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_group_right;
DELIMITER //
CREATE PROCEDURE sp_insert_group_right
(
	IN in_store_id int,
	IN in_group_detail_id int,
	IN in_function_name varchar(50),
	IN in_allow_view varchar(3),
	IN in_allow_add varchar(3),
	IN in_allow_edit varchar(3),
	IN in_allow_delete varchar(3)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("group_right","group_right_id",@new_id);
	INSERT INTO group_right
	(
		group_right_id,
		store_id,
		group_detail_id,
		function_name,
		allow_view,
		allow_add,
		allow_edit,
		allow_delete
	) 
    VALUES
	(
		@new_id,
		in_store_id,
		in_group_detail_id,
		in_function_name,
		in_allow_view,
		in_allow_add,
		in_allow_edit,
		in_allow_delete
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_group_right;
DELIMITER //
CREATE PROCEDURE sp_update_group_right
(
	IN in_group_right_id int,
	IN in_store_id int,
	IN in_group_detail_id int,
	IN in_function_name varchar(50),
	IN in_allow_view varchar(3),
	IN in_allow_add varchar(3),
	IN in_allow_edit varchar(3),
	IN in_allow_delete varchar(3)
) 
BEGIN 
	UPDATE group_right SET 
		store_id=in_store_id,
		group_detail_id=in_group_detail_id,
		function_name=in_function_name,
		allow_view=in_allow_view,
		allow_add=in_allow_add,
		allow_edit=in_allow_edit,
		allow_delete=in_allow_delete
	WHERE group_right_id=in_group_right_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_group_right_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_group_right_by_id
(
	IN in_group_right_id int
) 
BEGIN 
	SELECT * FROM group_right 
	WHERE group_right_id=in_group_right_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_group_right_by_group_id;
DELIMITER //
CREATE PROCEDURE sp_search_group_right_by_group_id
(
	IN in_group_detail_id int
) 
BEGIN 
	SELECT * FROM group_right 
	WHERE group_detail_id=in_group_detail_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_group_right_by_store_id;
DELIMITER //
CREATE PROCEDURE sp_search_group_right_by_store_id
(
	IN in_store_id int
) 
BEGIN 
	SELECT * FROM group_right 
	WHERE store_id=in_store_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_group_right_by_group_id_store_id;
DELIMITER //
CREATE PROCEDURE sp_search_group_right_by_group_id_store_id
(
	IN in_group_detail_id int,
	IN in_store_id int
) 
BEGIN 
	SELECT * FROM group_right 
	WHERE store_id=in_store_id AND group_detail_id=in_group_detail_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_group_right_by_group_id_store_id_function;
DELIMITER //
CREATE PROCEDURE sp_search_group_right_by_group_id_store_id_function
(
	IN in_group_detail_id int,
	IN in_store_id int,
	IN in_function_name varchar(50) 
) 
BEGIN 
	SELECT * FROM group_right 
	WHERE store_id=in_store_id AND group_detail_id=in_group_detail_id AND function_name=in_function_name; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_group_right_get_active_rights_by_store_user;
DELIMITER //
CREATE PROCEDURE sp_search_group_right_get_active_rights_by_store_user
(
	IN in_store_id int,
	IN in_user_detail_id bigint
) 
BEGIN 
	SELECT * FROM group_right gr 
	INNER JOIN group_detail gd ON gr.group_detail_id=gd.group_detail_id 
	AND gr.store_id=in_store_id AND gd.is_active='Yes' AND gd.group_detail_id IN
	(
		SELECT gu.group_detail_id FROM group_user gu 
		INNER JOIN user_detail ud ON gu.user_detail_id=ud.user_detail_id 
		AND ud.is_user_gen_admin='No' AND ud.is_user_locked='No' AND ud.user_detail_id=in_user_detail_id
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_item_map;
DELIMITER //
CREATE PROCEDURE sp_insert_item_map
(
	IN in_big_item_id bigint,
	IN in_small_item_id bigint,
	IN in_fraction_qty double,
	IN in_position int,
	IN in_map_group_id bigint 
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("item_map","item_map_id",@new_id);
	INSERT INTO item_map
	(
		item_map_id,
		big_item_id,
		small_item_id,
		fraction_qty,
		position,
		map_group_id 
	) 
    VALUES
	(
		@new_id,
		in_big_item_id,
		in_small_item_id,
		in_fraction_qty,
		in_position,
		in_map_group_id
	);  
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_item_map;
DELIMITER //
CREATE PROCEDURE sp_update_item_map
(
	IN in_item_map_id bigint,
	IN in_big_item_id bigint,
	IN in_small_item_id bigint,
	IN in_fraction_qty double,
	IN in_position int,
	IN in_map_group_id bigint 
) 
BEGIN 
	UPDATE item_map SET 
	big_item_id=in_big_item_id,
	small_item_id=in_small_item_id,
	fraction_qty=in_fraction_qty,
	position=in_position,
	map_group_id=in_map_group_id 
	WHERE item_map_id=in_item_map_id;  
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_delete_item_map;
DELIMITER //
CREATE PROCEDURE sp_delete_item_map
(
	IN in_item_map_id bigint
) 
BEGIN 
	DELETE FROM item_map 
	WHERE item_map_id=in_item_map_id;  
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_map_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_item_map_by_id
(
	IN in_item_map_id bigint
) 
BEGIN 
	SELECT * FROM item_map 
	WHERE item_map_id=in_item_map_id;  
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_map_by_small_item_id;
DELIMITER //
CREATE PROCEDURE sp_search_item_map_by_small_item_id
(
	IN in_small_item_id bigint
) 
BEGIN 
	SELECT * FROM item_map 
	WHERE small_item_id=in_small_item_id;  
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_map_by_big_item_id;
DELIMITER //
CREATE PROCEDURE sp_search_item_map_by_big_item_id
(
	IN in_big_item_id bigint
) 
BEGIN 
	SELECT * FROM item_map 
	WHERE big_item_id=in_big_item_id;  
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_map_by_small_item_id;
DELIMITER //
CREATE PROCEDURE sp_search_item_map_by_small_item_id
(
	IN in_small_item_id bigint
) 
BEGIN 
	SELECT * FROM item_map 
	WHERE small_item_id=in_small_item_id;  
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_map_by_big_small_item_id;
DELIMITER //
CREATE PROCEDURE sp_search_item_map_by_big_small_item_id
(
	IN in_big_item_id bigint,
	IN in_small_item_id bigint
) 
BEGIN 
	SELECT * FROM item_map 
	WHERE in_big_item_id IN(big_item_id,small_item_id) OR in_small_item_id IN(small_item_id,big_item_id) 
	ORDER BY position ASC;  
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_map_by_map_group_id;
DELIMITER //
CREATE PROCEDURE sp_search_item_map_by_map_group_id
(
	IN in_map_group_id bigint
) 
BEGIN 
	SELECT * FROM item_map 
	WHERE map_group_id=in_map_group_id 
	ORDER BY position ASC;  
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_map_get_count_map_group_id;
DELIMITER //
CREATE PROCEDURE sp_search_item_map_get_count_map_group_id
(
	IN in_map_group_id bigint
) 
BEGIN 
	SELECT * FROM item_map 
	WHERE map_group_id=in_map_group_id;  
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_discount_package;
DELIMITER //
CREATE PROCEDURE sp_insert_discount_package
(
	IN in_package_name varchar(50),
	IN in_start_date datetime,
	IN in_end_date datetime,
	IN in_store_scope varchar(500),
	IN in_transactor_scope varchar(500),
	IN in_segment_scope varchar(500),
	IN in_day_scope varchar(20),
	IN in_time_scope_from varchar(10),
	IN in_time_scope_to varchar(10)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("discount_package","discount_package_id",@new_id);

	INSERT INTO discount_package
	(
		discount_package_id,
		package_name,
		start_date,
		end_date,
		store_scope,
		transactor_scope,
		segment_scope,
		day_scope,
		time_scope_from,
		time_scope_to
	) 
    VALUES
	(
		@new_id,
		in_package_name,
		in_start_date,
		in_end_date,
		in_store_scope,
		in_transactor_scope,
		in_segment_scope,
		in_day_scope,
		in_time_scope_from,
		in_time_scope_to
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_discount_package;
DELIMITER //
CREATE PROCEDURE sp_update_discount_package
(
	IN in_discount_package_id int,
	IN in_package_name varchar(50),
	IN in_start_date datetime,
	IN in_end_date datetime,
	IN in_store_scope varchar(500),
	IN in_transactor_scope varchar(500),
	IN in_segment_scope varchar(500),
	IN in_day_scope varchar(20),
	IN in_time_scope_from varchar(10),
	IN in_time_scope_to varchar(10)
) 
BEGIN 
	UPDATE discount_package SET 
		package_name=in_package_name,
		start_date=in_start_date,
		end_date=in_end_date,
		store_scope=in_store_scope,
		transactor_scope=in_transactor_scope,
		segment_scope=in_segment_scope,
		day_scope=in_day_scope,
		time_scope_from=in_time_scope_from,
		time_scope_to=in_time_scope_to 
	WHERE discount_package_id=in_discount_package_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_discount_package_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_discount_package_by_id
(
	IN in_discount_package_id int 
) 
BEGIN 
		SELECT * FROM discount_package d 
		WHERE d.discount_package_id=in_discount_package_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_discount_package_by_name;
DELIMITER //
CREATE PROCEDURE sp_search_discount_package_by_name
(
	IN in_discount_package_name varchar(50)
) 
BEGIN 
		SELECT * FROM discount_package d 
		WHERE d.package_name=in_package_name OR d.package_name LIKE concat('%',in_package_name,'%') 
		ORDER BY d.package_name ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_discount_package_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_discount_package_by_none() 
BEGIN 
		SELECT * FROM discount_package 
		ORDER BY end_date DESC,package_name ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_discount_package_item;
DELIMITER //
CREATE PROCEDURE sp_insert_discount_package_item
(
	IN in_discount_package_id bigint,
	IN in_store_id int,
	IN in_item_id int,
	IN in_item_qty double,
	IN in_wholesale_discount_amt double,
	IN in_retailsale_discount_amt double,
	IN in_hire_price_discount_amt double,
	IN in_category_scope varchar(500),
	IN in_sub_category_scope varchar(500),
	IN in_item_scope varchar(500)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("discount_package_item","discount_package_item_id",@new_id);
	SET @in_item_id=NULL;
	if (in_item_id!=0) then
		set @in_item_id=in_item_id;
	end if;
	SET @in_store_id=NULL;
	if (in_store_id!=0) then
		set @in_store_id=in_store_id;
	end if;

	INSERT INTO discount_package_item
	(
		discount_package_item_id,
		discount_package_id,
		store_id,
		item_id,
		item_qty,
		wholesale_discount_amt,
		retailsale_discount_amt,
		hire_price_discount_amt,
		category_scope,
		sub_category_scope,
		item_scope
	) 
    VALUES
	(
		@new_id,
		in_discount_package_id,
		@in_store_id,
		@in_item_id,
		in_item_qty,
		in_wholesale_discount_amt,
		in_retailsale_discount_amt,
		in_hire_price_discount_amt,
		in_category_scope,
		in_sub_category_scope,
		in_item_scope
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_discount_package_item;
DELIMITER //
CREATE PROCEDURE sp_update_discount_package_item
(
	IN in_discount_package_item_id bigint,
	IN in_discount_package_id int,
	IN in_store_id int,
	IN in_item_id int,
	IN in_item_qty double,
	IN in_wholesale_discount_amt double,
	IN in_retailsale_discount_amt double,
	IN in_hire_price_discount_amt double,
	IN in_category_scope varchar(500),
	IN in_sub_category_scope varchar(500),
	IN in_item_scope varchar(500)
) 
BEGIN 
	SET @in_item_id=NULL;
	if (in_item_id!=0) then
		set @in_item_id=in_item_id;
	end if;
	SET @in_store_id=NULL;
	if (in_store_id!=0) then
		set @in_store_id=in_store_id;
	end if;

	UPDATE discount_package_item SET 
		discount_package_id=in_discount_package_id,
		store_id=@in_store_id,
		item_id=@in_item_id,
		item_qty=in_item_qty,
		wholesale_discount_amt=in_wholesale_discount_amt,
		retailsale_discount_amt=in_retailsale_discount_amt,
		hire_price_discount_amt=in_hire_price_discount_amt,
		category_scope=in_category_scope,
		sub_category_scope=in_sub_category_scope,
		item_scope=in_item_scope 
	WHERE discount_package_item_id=in_discount_package_item_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_discount_package_item_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_discount_package_item_by_id
(
	IN in_discount_package_item_id bigint 
) 
BEGIN 
		SELECT * FROM discount_package_item d 
		WHERE d.discount_package_item_id=in_discount_package_item_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_discount_package_item_by_package_id;
DELIMITER //
CREATE PROCEDURE sp_search_discount_package_item_by_package_id
(
	IN in_discount_package_id int 
) 
BEGIN 
		SELECT * FROM discount_package_item d 
		WHERE d.discount_package_id=in_discount_package_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_discount_package_item_by_package_store;
DELIMITER //
CREATE PROCEDURE sp_search_discount_package_item_by_package_store
(
	IN in_discount_package_id int,
	In in_store_id int
) 
BEGIN 
		SELECT * FROM discount_package_item d 
		WHERE d.discount_package_id=in_discount_package_id AND d.store_id=in_store_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_discount_package_item_by_store_item;
DELIMITER //
CREATE PROCEDURE sp_search_discount_package_item_by_store_item
(
	In in_store_id int,
	IN in_item_id bigint
) 
BEGIN 
		SELECT * FROM discount_package_item d 
		WHERE d.store_id=in_store_id AND d.item_id=in_item_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_discount_package_item_by_package_store_item;
DELIMITER //
CREATE PROCEDURE sp_search_discount_package_item_by_package_store_item
(
	IN in_discount_package_id int,
	In in_store_id int,
	IN in_item_id bigint
) 
BEGIN 
		SELECT * FROM discount_package_item d 
		WHERE d.discount_package_id=in_discount_package_id AND d.store_id=in_store_id AND d.item_id=in_item_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_discount_package_item_by_item_id;
DELIMITER //
CREATE PROCEDURE sp_search_discount_package_item_by_item_id
(
	IN in_item_id bigint 
) 
BEGIN 
		SELECT * FROM discount_package_item d 
		WHERE d.item_id=in_item_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_discount_package_item_by_store_id;
DELIMITER //
CREATE PROCEDURE sp_search_discount_package_item_by_store_id
(
	IN in_store_id int 
) 
BEGIN 
		SELECT * FROM discount_package_item d 
		WHERE d.store_id=in_store_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_discount_package_item_by_store_item_qty_all;
DELIMITER //
CREATE PROCEDURE sp_search_discount_package_item_by_store_item_qty_all
(
	IN in_store_id int,
	IN in_item_id bigint,
	IN in_item_qty double
) 
BEGIN 
		SELECT * FROM discount_package_item d 
		WHERE d.store_id=in_store_id AND d.item_id=in_item_id AND d.item_qty=in_item_qty;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_discount_package_item_by_store_item_qty_active;
DELIMITER //
CREATE PROCEDURE sp_search_discount_package_item_by_store_item_qty_active
(
	IN in_store_id int,
	IN in_item_id bigint,
	IN in_item_qty double
) 
BEGIN 
		SET @cur_sys_datetime=null;
		CALL sp_get_current_system_datetime(@cur_sys_datetime);
		
		SELECT d.*,dd.start_date,dd.end_date FROM discount_package_item d 
		INNER JOIN discount_package dd ON d.discount_package_id=dd.discount_package_id 
		AND d.store_id=in_store_id AND d.item_id=in_item_id AND d.item_qty=in_item_qty AND 
		@cur_sys_datetime BETWEEN dd.start_date AND dd.end_date;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_discount_package_item_active;
DELIMITER //
CREATE PROCEDURE sp_search_discount_package_item_active
(
	IN in_store_id int,
	IN in_item_id bigint,
	IN in_item_qty double,
	IN in_transactor_id bigint,
	IN in_category_id int,
	IN in_sub_category_id int
) 
BEGIN 
		SET @cur_sys_datetime=null;
		CALL sp_get_current_system_datetime(@cur_sys_datetime);
		SET @segid=0;
		CALL sp_get_transactor_segment(in_transactor_id,@segid);
		SET @dayno=0;
		CALL sp_get_day_of_week(DAYNAME(@cur_sys_datetime),@dayno);

		SELECT dpi.* FROM discount_package_item dpi 
		INNER JOIN discount_package dp ON dpi.discount_package_id=dp.discount_package_id 
		WHERE 
		(ifnull(store_scope,'')='' OR find_in_set(in_store_id,dp.store_scope)<>0) AND 
		(ifnull(transactor_scope,'')='' OR find_in_set(in_transactor_id,dp.transactor_scope)<>0) AND 
		(ifnull(segment_scope,'')='' OR find_in_set(@segid,dp.segment_scope)<>0) AND 
		(ifnull(day_scope,'')='' OR find_in_set(@dayno,dp.day_scope)<>0) AND 
		(ifnull(time_scope_from,'')='' OR HOUR(@cur_sys_datetime)>HOUR(time_scope_from) OR (HOUR(@cur_sys_datetime)=HOUR(time_scope_from) AND MINUTE(@cur_sys_datetime)>=MINUTE(time_scope_from))) AND 
		(ifnull(time_scope_to,'')='' OR HOUR(@cur_sys_datetime)<HOUR(time_scope_to) OR (HOUR(@cur_sys_datetime)=HOUR(time_scope_to) AND MINUTE(@cur_sys_datetime)<=MINUTE(time_scope_to))) AND 
		(find_in_set(in_category_id,dpi.category_scope)<>0 OR find_in_set(in_sub_category_id,dpi.sub_category_scope)<>0 OR find_in_set(in_item_id,dpi.item_scope)<>0) AND 
		dpi.item_qty<=in_item_qty AND 
		@cur_sys_datetime BETWEEN dp.start_date AND dp.end_date;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_company_setting_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_company_setting_by_id
(
	IN in_company_setting_id int
) 
BEGIN 
		SELECT * FROM company_setting c 
		WHERE c.company_setting_id=in_company_setting_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_company_setting;
DELIMITER //
CREATE PROCEDURE sp_update_company_setting
(
	IN in_ECompanyName varchar(100),
	IN in_EPhysicalAddress varchar(250),
	IN in_EPhone varchar(100),
	IN in_EFax varchar(100),
	IN in_EEmail varchar(100),
	IN in_EWebsite varchar(100),
	IN in_ELogoUrl varchar(100),
	IN in_ESloghan varchar(100),
	IN in_ECurrencyUsed varchar(10),
	IN in_EVatPerc double,
	IN in_EIsAllowDiscount varchar(3),
	IN in_EIsAllowDebt varchar(3),
	IN in_EIsCustomerMandatory varchar(3),
	IN in_EIsSupplierMandatory varchar(3),
	IN in_EIsVatInclusive varchar(3),
	IN in_EIsTradeDiscountVatLiable varchar(3),
	IN in_EIsCashDiscountVatLiable varchar(3),
	IN in_EIsMapItemsActive varchar(3),
	IN in_EBranchCode varchar(20),
	IN in_EBranchId int,
	IN in_EAwardAmountPerPoint double,
	IN in_ESpendAmountPerPoint double,
	IN in_ETaxIdentity varchar(50),
	IN in_ESalesReceiptName varchar(20),
	IN in_EIsShowDeveloper varchar(3),
	IN in_EDeveloperEmail varchar(100),
	IN in_EDeveloperPhone varchar(100),
	IN in_EShowLogoInvoice varchar(3),
	IN in_EIsAllowAutoUnpack varchar(3),
	IN in_ETimeZone varchar(20),
	IN in_EDateFormat varchar(20),
	IN in_ELicenseKey varchar(254),
	IN in_ESalesReceiptVersion int,
	IN in_EEnforceTransUserSelect varchar(3),
	IN in_EShowBranchInvoice varchar(3),
	IN in_EShowStoreInvoice varchar(3),
	IN in_EShowVatAnalysisInvoice varchar(3),
	IN in_EStoreEquivName varchar(20)
) 
BEGIN 
	UPDATE company_setting SET 
		company_name=in_ECompanyName,
		physical_address=in_EPhysicalAddress,
		phone=in_EPhone,
		fax=in_EFax,
		email=in_EEmail,
		website=in_EWebsite,
		logo_url=in_ELogoUrl,
		sloghan=in_ESloghan,
		currency_used=in_ECurrencyUsed,
		vat_perc=in_EVatPerc,
		is_allow_discount=in_EIsAllowDiscount,
		is_allow_debt=in_EIsAllowDebt,
		is_customer_mandatory=in_EIsCustomerMandatory,
		is_supplier_mandatory=in_EIsSupplierMandatory,
		is_vat_inclusive=in_EIsVatInclusive,
		is_trade_discount_vat_liable=in_EIsTradeDiscountVatLiable,
		is_cash_discount_vat_liable=in_EIsCashDiscountVatLiable,
		is_map_items_active=in_EIsMapItemsActive,
		branch_code=in_EBranchCode,
		branch_id=in_EBranchId,
		award_amount_per_point=in_EAwardAmountPerPoint,
		spend_amount_per_point=in_ESpendAmountPerPoint,
		tax_identity=in_ETaxIdentity,
		sales_receipt_name=in_ESalesReceiptName,
		is_show_developer=in_EIsShowDeveloper,
		developer_email=in_EDeveloperEmail,
		developer_phone=in_EDeveloperPhone,
		show_logo_invoice=in_EShowLogoInvoice,
		show_branch_invoice=in_EShowBranchInvoice,
		show_store_invoice=in_EShowStoreInvoice,
		is_allow_auto_unpack=in_EIsAllowAutoUnpack,
		time_zone=in_ETimeZone,
		date_format=in_EDateFormat,
		license_key=in_ELicenseKey,
		sales_receipt_version=in_ESalesReceiptVersion,
		enforce_trans_user_select=in_EEnforceTransUserSelect,
		show_vat_analysis_invoice=in_EShowVatAnalysisInvoice,
		store_equiv_name=in_EStoreEquivName 
	WHERE company_setting_id=1; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_company_setting_logo;
DELIMITER //
CREATE PROCEDURE sp_update_company_setting_logo
(
	IN in_logo blob 
) 
BEGIN 
	UPDATE company_setting SET logo=in_logo 
	WHERE company_setting_id=1; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_stage_points_transaction;
DELIMITER //
CREATE PROCEDURE sp_insert_stage_points_transaction
(
	IN in_points_card_id bigint,
	IN in_transaction_date date,
	IN in_points_awarded double,
	IN in_points_spent double,
	IN in_transaction_id bigint,
	IN in_trans_branch_id int,
	IN in_add_date datetime,
	IN in_add_user varchar(100),
	IN in_edit_date datetime,
	IN in_edit_user varchar(100),
	IN in_points_spent_amount double
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("stage_points_transaction","stage_points_transaction_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	INSERT INTO stage_points_transaction
	(
		stage_points_transaction_id,
		points_card_id,
		transaction_date,
		points_awarded,
		points_spent,
		transaction_id,
		trans_branch_id,
		add_date,
		add_user,
		edit_date,
		edit_user,
		points_spent_amount 
	) 
    VALUES
	(
		@new_id,
		in_points_card_id,
		in_transaction_date,
		in_points_awarded,
		in_points_spent,
		in_transaction_id,
		in_trans_branch_id,
		@cur_sys_datetime,
		in_add_user,
		@cur_sys_datetime,
		in_edit_user,
		in_points_spent_amount
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_stage_points_transaction_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_stage_points_transaction_by_none() 
BEGIN 
		SELECT * FROM stage_points_transaction; 
END// 
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_stage_points_transaction_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_stage_points_transaction_by_id
(
	IN in_stage_points_transaction_id bigint 
) 
BEGIN 
		SELECT * FROM stage_points_transaction 
		WHERE stage_points_transaction_id=in_stage_points_transaction_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_stage_points_transaction_by_transaction_id;
DELIMITER //
CREATE PROCEDURE sp_search_stage_points_transaction_by_transaction_id
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT * FROM stage_points_transaction 
		WHERE transaction_id=in_transaction_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_delete_stage_points_transaction_by_id;
DELIMITER //
CREATE PROCEDURE sp_delete_stage_points_transaction_by_id
(
	IN in_stage_points_transaction_id bigint 
) 
BEGIN 
		DELETE FROM stage_points_transaction 
		WHERE stage_points_transaction_id=in_stage_points_transaction_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_delete_stage_points_transaction_by_trans_id;
DELIMITER //
CREATE PROCEDURE sp_delete_stage_points_transaction_by_trans_id
(
	IN in_transaction_id bigint 
) 
BEGIN 
		DELETE FROM stage_points_transaction 
		WHERE transaction_id=in_transaction_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_approve;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_approve
(
	IN in_transaction_id bigint,
	IN in_function_name varchar(50),
	IN in_user_detail_id int
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("transaction_approve","transaction_approve_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	INSERT INTO transaction_approve
	(
		transaction_approve_id,
		transaction_id,
		function_name,
		user_detail_id,
		approve_date
	) 
    VALUES
	(
		@new_id,
		in_transaction_id,
		in_function_name,
		in_user_detail_id,
		@cur_sys_datetime
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_transaction;
DELIMITER //
CREATE PROCEDURE sp_report_transaction
(
	IN in_transaction_date_from date,
	IN in_transaction_date_to date,
	IN in_store_id int,
	IN in_store2_id int,
	IN in_transactor_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_add_user_detail_id int,
	IN in_add_date_from datetime,
	IN in_add_date_to datetime,
	IN in_edit_user_detail_id int,
	IN in_edit_date_from datetime,
	IN in_edit_date_to datetime,
	IN in_transaction_user_detail_id int,
	IN in_bill_transactor_id bigint 
) 
BEGIN 
	SET @TransDate='';
	SET @FromStore='';
	SET @ToStore='';
	SET @Transactor='';
	SET @TransType='';
	SET @TransReason='';
	SET @AddUser='';
	SET @AddDate='';
	SET @EditUser='';
	SET @EditDate='';
	SET @TransactionUser='';
	SET @BillTransactor='';

	if ((in_transaction_date_from is not null) and (in_transaction_date_to is not null)) then 
		SET @TransDate=concat(" AND (t.transaction_date BETWEEN '",in_transaction_date_from,"' AND '",in_transaction_date_to,"')");
	end if;
	if ((in_add_date_from is not null) and (in_add_date_to is not null)) then 
		SET @AddDate=concat(" AND (t.add_date BETWEEN '",in_add_date_from,"' AND '",in_add_date_to,"')");
	end if;
	if ((in_edit_date_from is not null) and (in_edit_date_to is not null)) then 
		SET @EditDate=concat(" AND (t.edit_date BETWEEN '",in_edit_date_from,"' AND '",in_edit_date_to,"')");
	end if;
	if (in_store_id!=0) then 
		SET @FromStore=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_store2_id!=0) then 
		SET @ToStore=concat(' AND t.store2_id=',in_store2_id);
	end if;
	if (in_transactor_id!=0) then 
		SET @Transactor=concat(' AND t.transactor_id=',in_transactor_id);
	end if;
	if (in_bill_transactor_id!=0) then 
		SET @BillTransactor=concat(' AND t.bill_transactor_id=',in_bill_transactor_id);
	end if;
	if (in_transaction_type_id!=0) then 
		SET @TransType=concat(' AND t.transaction_type_id=',in_transaction_type_id);
	end if;
	if (in_transaction_reason_id!=0) then 
		SET @TransReason=concat(' AND t.transaction_reason_id=',in_transaction_reason_id);
	end if;
	if (in_add_user_detail_id!=0) then 
		SET @AddUser=concat(' AND t.add_user_detail_id=',in_add_user_detail_id);
	end if;
	if (in_edit_user_detail_id!=0) then 
		SET @EditUser=concat(' AND t.edit_user_detail_id=',in_edit_user_detail_id);
	end if;
	if (in_transaction_user_detail_id!=0) then 
		SET @TransactionUser=concat(' AND t.transaction_user_detail_id=',in_transaction_user_detail_id);
	end if;
	
	SET @sql_text=concat('SELECT * FROM view_transaction t WHERE 1=1 ',@TransDate,@AddDate,@EditDate,@FromStore,@ToStore,@AddUser,@EditUser,@TransactionUser,@Transactor,@BillTransactor,@TransType,@TransReason,' ORDER BY t.add_date DESC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_transaction_summary;
DELIMITER //
CREATE PROCEDURE sp_report_transaction_summary
(
	IN in_transaction_date_from date,
	IN in_transaction_date_to date,
	IN in_store_id int,
	IN in_store2_id int,
	IN in_transactor_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_add_user_detail_id int,
	IN in_add_date_from datetime,
	IN in_add_date_to datetime,
	IN in_edit_user_detail_id int,
	IN in_edit_date_from datetime,
	IN in_edit_date_to datetime,
	IN in_group_by_field varchar(100),
	IN in_transaction_user_detail_id int,
	IN in_bill_transactor_id bigint 
) 
BEGIN 
	SET @TransDate='';
	SET @FromStore='';
	SET @ToStore='';
	SET @Transactor='';
	SET @BillTransactor='';
	SET @TransType='';
	SET @TransReason='';
	SET @AddUser='';
	SET @AddDate='';
	SET @EditUser='';
	SET @TransactionUser='';
	SET @EditDate='';
	SET @GroupBy1='';
	SET @GroupBy2='';

	if ((in_transaction_date_from is not null) and (in_transaction_date_to is not null)) then 
		SET @TransDate=concat(" AND (t.transaction_date BETWEEN '",in_transaction_date_from,"' AND '",in_transaction_date_to,"')");
	end if;
	if ((in_add_date_from is not null) and (in_add_date_to is not null)) then 
		SET @AddDate=concat(" AND (t.add_date BETWEEN '",in_add_date_from,"' AND '",in_add_date_to,"')");
	end if;
	if ((in_edit_date_from is not null) and (in_edit_date_to is not null)) then 
		SET @EditDate=concat(" AND (t.edit_date BETWEEN '",in_edit_date_from,"' AND '",in_edit_date_to,"')");
	end if;
	if (in_store_id!=0) then 
		SET @FromStore=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_store2_id!=0) then 
		SET @ToStore=concat(' AND t.store2_id=',in_store2_id);
	end if;
	if (in_transactor_id!=0) then 
		SET @Transactor=concat(' AND t.transactor_id=',in_transactor_id);
	end if;
	if (in_bill_transactor_id!=0) then 
		SET @BillTransactor=concat(' AND t.bill_transactor_id=',in_bill_transactor_id);
	end if;
	if (in_transaction_type_id!=0) then 
		SET @TransType=concat(' AND t.transaction_type_id=',in_transaction_type_id);
	end if;
	if (in_transaction_reason_id!=0) then 
		SET @TransReason=concat(' AND t.transaction_reason_id=',in_transaction_reason_id);
	end if;
	if (in_add_user_detail_id!=0) then 
		SET @AddUser=concat(' AND t.add_user_detail_id=',in_add_user_detail_id);
	end if;
	if (in_edit_user_detail_id!=0) then 
		SET @EditUser=concat(' AND t.edit_user_detail_id=',in_edit_user_detail_id);
	end if;
	if (in_transaction_user_detail_id!=0) then 
		SET @TransactionUser=concat(' AND t.transaction_user_detail_id=',in_transaction_user_detail_id);
	end if;
	if (in_group_by_field!='') then 
		SET @GroupBy1=concat('store_id,transaction_type_id,',in_group_by_field,' AS field_name,',
		'SUM(t.total_trade_discount) AS sum_total_trade_discount,',
		'SUM(t.total_vat) AS sum_total_vat,',
		'SUM(t.cash_discount) AS sum_cash_discount,',
		'SUM(t.grand_total) AS sum_grand_total,',
		'SUM(t.total_std_vatable_amount) AS sum_total_std_vatable_amount,',
		'SUM(t.total_zero_vatable_amount) AS sum_total_zero_vatable_amount,',
		'SUM(t.total_exempt_vatable_amount) AS sum_total_exempt_vatable_amount,',
		'SUM(t.total_profit_margin) AS sum_total_profit_margin');
		SET @GroupBy2=concat(' GROUP BY store_id,transaction_type_id,',in_group_by_field,' ORDER BY store_id,transaction_type_id,',in_group_by_field);
	else
		SET @GroupBy1=concat('store_id,transaction_type_id,',
		'SUM(t.total_trade_discount) AS sum_total_trade_discount,',
		'SUM(t.total_vat) AS sum_total_vat,',
		'SUM(t.cash_discount) AS sum_cash_discount,',
		'SUM(t.grand_total) AS sum_grand_total,',
		'SUM(t.total_std_vatable_amount) AS sum_total_std_vatable_amount,',
		'SUM(t.total_zero_vatable_amount) AS sum_total_zero_vatable_amount,',
		'SUM(t.total_exempt_vatable_amount) AS sum_total_exempt_vatable_amount,',
		'SUM(t.total_profit_margin) AS sum_total_profit_margin');
		SET @GroupBy2=' GROUP BY store_id,transaction_type_id';
	end if;
	
	SET @sql_text=concat('SELECT ',@GroupBy1,' FROM view_transaction t WHERE 1=1 ',@TransDate,@AddDate,@EditDate,@FromStore,@ToStore,@AddUser,@EditUser,@TransactionUser,@Transactor,@BillTransactor,@TransType,@TransReason,' ',@GroupBy2);
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_transaction_item;
DELIMITER //
CREATE PROCEDURE sp_report_transaction_item
(
	IN in_transaction_date_from date,
	IN in_transaction_date_to date,
	IN in_store_id int,
	IN in_store2_id int,
	IN in_transactor_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_add_user_detail_id int,
	IN in_add_date_from datetime,
	IN in_add_date_to datetime,
	IN in_edit_user_detail_id int,
	IN in_edit_date_from datetime,
	IN in_edit_date_to datetime,
	IN in_transaction_id bigint,
	IN in_item_id bigint,
	IN in_transaction_user_detail_id int,
	IN in_bill_transactor_id bigint 
) 
BEGIN 
	SET @TransDate='';
	SET @FromStore='';
	SET @ToStore='';
	SET @Transactor='';
	SET @BillTransactor='';
	SET @TransType='';
	SET @TransReason='';
	SET @AddUser='';
	SET @AddDate='';
	SET @EditUser='';
	SET @EditDate='';
	SET @TransactionUser='';
	SET @TransId='';
	SET @ItemId='';

	if ((in_transaction_date_from is not null) and (in_transaction_date_to is not null)) then 
		SET @TransDate=concat(" AND (ti.transaction_date BETWEEN '",in_transaction_date_from,"' AND '",in_transaction_date_to,"')");
	end if;
	if ((in_add_date_from is not null) and (in_add_date_to is not null)) then 
		SET @AddDate=concat(" AND (ti.add_date BETWEEN '",in_add_date_from,"' AND '",in_add_date_to,"')");
	end if;
	if ((in_edit_date_from is not null) and (in_edit_date_to is not null)) then 
		SET @EditDate=concat(" AND (ti.edit_date BETWEEN '",in_edit_date_from,"' AND '",in_edit_date_to,"')");
	end if;
	if (in_store_id!=0) then 
		SET @FromStore=concat(' AND ti.store_id=',in_store_id);
	end if;
	if (in_store2_id!=0) then 
		SET @ToStore=concat(' AND ti.store2_id=',in_store2_id);
	end if;
	if (in_transactor_id!=0) then 
		SET @Transactor=concat(' AND ti.transactor_id=',in_transactor_id);
	end if;
	if (in_bill_transactor_id!=0) then 
		SET @BillTransactor=concat(' AND ti.bill_transactor_id=',in_bill_transactor_id);
	end if;
	if (in_transaction_type_id!=0) then 
		SET @TransType=concat(' AND ti.transaction_type_id=',in_transaction_type_id);
	end if;
	if (in_transaction_reason_id!=0) then 
		SET @TransReason=concat(' AND ti.transaction_reason_id=',in_transaction_reason_id);
	end if;
	if (in_add_user_detail_id!=0) then 
		SET @AddUser=concat(' AND ti.add_user_detail_id=',in_add_user_detail_id);
	end if;
	if (in_edit_user_detail_id!=0) then 
		SET @EditUser=concat(' AND ti.edit_user_detail_id=',in_edit_user_detail_id);
	end if;
	if (in_transaction_user_detail_id!=0) then 
		SET @TransactionUser=concat(' AND ti.transaction_user_detail_id=',in_transaction_user_detail_id);
	end if;
	if (in_transaction_id!=0) then 
		SET @TransId=concat(' AND ti.transaction_id=',in_transaction_id);
	end if;
	if (in_item_id!=0) then 
		SET @ItemId=concat(' AND ti.item_id=',in_item_id);
	end if;
	
	SET @sql_text=concat('SELECT * FROM view_transaction_item ti WHERE 1=1 ',
		@TransId,@ItemId,@TransDate,@AddDate,@EditDate,@FromStore,@ToStore,@AddUser,@EditUser,@TransactionUser,@Transactor,@BillTransactor,@TransType,@TransReason,
		' ORDER BY add_date DESC');
	-- SELECT @sql_text;
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_transaction_item_user_earn;
DELIMITER //
CREATE PROCEDURE sp_report_transaction_item_user_earn
(
	IN in_transaction_date_from date,
	IN in_transaction_date_to date,
	IN in_store_id int,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_transaction_user_detail_id int
) 
BEGIN 
	SET @TransDate='';
	SET @FromStore='';
	SET @TransType='';
	SET @TransReason='';
	SET @TransactionUser='';

	if ((in_transaction_date_from is not null) and (in_transaction_date_to is not null)) then 
		SET @TransDate=concat(" AND (ti.transaction_date BETWEEN '",in_transaction_date_from,"' AND '",in_transaction_date_to,"')");
	end if;
	if (in_store_id!=0) then 
		SET @FromStore=concat(' AND ti.store_id=',in_store_id);
	end if;
	if (in_transaction_type_id!=0) then 
		SET @TransType=concat(' AND ti.transaction_type_id=',in_transaction_type_id);
	end if;
	if (in_transaction_reason_id!=0) then 
		SET @TransReason=concat(' AND ti.transaction_reason_id=',in_transaction_reason_id);
	end if;
	if (in_transaction_user_detail_id!=0) then 
		SET @TransactionUser=concat(' AND ti.transaction_user_detail_id=',in_transaction_user_detail_id);
	end if;
	
	SET @sql_text=concat('SELECT * FROM view_transaction_item ti WHERE ti.earn_amount>0 ',
		@TransDate,@FromStore,@TransactionUser,@TransType,@TransReason,
		' ORDER BY add_date DESC');
	-- SELECT @sql_text;
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_transaction_user_earn_summary;
DELIMITER //
CREATE PROCEDURE sp_report_transaction_user_earn_summary
(
	IN in_transaction_date_from date,
	IN in_transaction_date_to date,
	IN in_store_id int,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_transaction_user_detail_id int
) 
BEGIN 
	SET @TransDate='';
	SET @FromStore='';
	SET @TransType='';
	SET @TransReason='';
	SET @TransactionUser='';
	SET @GroupBy1='';
	SET @GroupBy2='';

	if ((in_transaction_date_from is not null) and (in_transaction_date_to is not null)) then 
		SET @TransDate=concat(" AND (t.transaction_date BETWEEN '",in_transaction_date_from,"' AND '",in_transaction_date_to,"')");
	end if;
	if (in_store_id!=0) then 
		SET @FromStore=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_transaction_type_id!=0) then 
		SET @TransType=concat(' AND t.transaction_type_id=',in_transaction_type_id);
	end if;
	if (in_transaction_reason_id!=0) then 
		SET @TransReason=concat(' AND t.transaction_reason_id=',in_transaction_reason_id);
	end if;
	if (in_transaction_user_detail_id!=0) then 
		SET @TransactionUser=concat(' AND t.transaction_user_detail_id=',in_transaction_user_detail_id);
	end if;
	
	SET @sql_text=concat('SELECT transaction_user_detail_id AS EarnUserId,SUM(earn_amount) AS TotalEarnAmount FROM view_transaction_item t WHERE t.earn_amount>0 ',@TransDate,@FromStore,@TransactionUser,@TransType,@TransReason,' ','GROUP BY transaction_user_detail_id');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_pay;
DELIMITER //
CREATE PROCEDURE sp_report_pay
(
	IN in_pay_date_from date,
	IN in_pay_date_to date,
	IN in_store_id int,
	IN in_bill_transactor_id bigint,
	IN in_transaction_type_id int,
	IN in_add_user_detail_id int,
	IN in_add_date_from datetime,
	IN in_add_date_to datetime,
	IN in_edit_user_detail_id int,
	IN in_edit_date_from datetime,
	IN in_edit_date_to datetime,
	IN in_transaction_id bigint,
	IN in_pay_method_id int,
	IN in_pay_id bigint
) 
BEGIN 
	SET @PayDate='';
	SET @Store='';
	SET @BillTransactor='';
	SET @TransType='';
	SET @AddUser='';
	SET @AddDate='';
	SET @EditUser='';
	SET @EditDate='';
	SET @TransId='';
	SET @PayMethodId='';
	SET @PayId='';

	if ((in_pay_date_from is not null) and (in_pay_date_to is not null)) then 
		SET @PayDate=concat(" AND (t.pay_date BETWEEN '",in_pay_date_from,"' AND '",in_pay_date_to,"')");
	end if;
	if ((in_add_date_from is not null) and (in_add_date_to is not null)) then 
		SET @AddDate=concat(" AND (t.add_date BETWEEN '",in_add_date_from,"' AND '",in_add_date_to,"')");
	end if;
	if ((in_edit_date_from is not null) and (in_edit_date_to is not null)) then 
		SET @EditDate=concat(" AND (t.edit_date BETWEEN '",in_edit_date_from,"' AND '",in_edit_date_to,"')");
	end if;
	if (in_store_id!=0) then 
		SET @Store=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_bill_transactor_id!=0) then 
		SET @BillTransactor=concat(' AND t.bill_transactor_id=',in_bill_transactor_id);
	end if;
	if (in_transaction_type_id!=0) then 
		SET @TransType=concat(' AND t.transaction_type_id=',in_transaction_type_id);
	end if;
	if (in_add_user_detail_id!=0) then 
		SET @AddUser=concat(' AND t.add_user_detail_id=',in_add_user_detail_id);
	end if;
	if (in_edit_user_detail_id!=0) then 
		SET @EditUser=concat(' AND t.edit_user_detail_id=',in_edit_user_detail_id);
	end if;
	if (in_transaction_id!=0) then 
		SET @TransId=concat(' AND t.transaction_id=',in_transaction_id);
	end if;
	if (in_pay_method_id!=0) then 
		SET @PayMethodId=concat(' AND t.pay_method_id=',in_pay_method_id);
	end if;
	if (in_pay_id!=0) then 
		SET @PayId=concat(' AND t.pay_id=',in_pay_id);
	end if;
	
	SET @sql_text=concat('SELECT * FROM view_pay AS t WHERE 1=1 ',
		@TransId,@PayDate,@AddDate,@EditDate,@Store,@AddUser,@EditUser,@BillTransactor,@TransType,@PayMethodId,@PayId);
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_pay_summary;
DELIMITER //
CREATE PROCEDURE sp_report_pay_summary
(
	IN in_pay_date_from date,
	IN in_pay_date_to date,
	IN in_store_id int,
	IN in_bill_transactor_id bigint,
	IN in_transaction_type_id int,
	IN in_add_user_detail_id int,
	IN in_add_date_from datetime,
	IN in_add_date_to datetime,
	IN in_edit_user_detail_id int,
	IN in_edit_date_from datetime,
	IN in_edit_date_to datetime,
	IN in_transaction_id bigint,
	IN in_pay_method_id int,
	IN in_pay_id bigint,
	IN in_group_by_field varchar(100) 
) 
BEGIN 
	SET @PayDate='';
	SET @Store='';
	SET @BillTransactor='';
	SET @TransType='';
	SET @AddUser='';
	SET @AddDate='';
	SET @EditUser='';
	SET @EditDate='';
	SET @TransId='';
	SET @PayMethodId='';
	SET @PayId='';
	SET @GroupBy1='';
	SET @GroupBy2='';

	if ((in_pay_date_from is not null) and (in_pay_date_to is not null)) then 
		SET @PayDate=concat(" AND (t.pay_date BETWEEN '",in_pay_date_from,"' AND '",in_pay_date_to,"')");
	end if;
	if ((in_add_date_from is not null) and (in_add_date_to is not null)) then 
		SET @AddDate=concat(" AND (t.add_date BETWEEN '",in_add_date_from,"' AND '",in_add_date_to,"')");
	end if;
	if ((in_edit_date_from is not null) and (in_edit_date_to is not null)) then 
		SET @EditDate=concat(" AND (t.edit_date BETWEEN '",in_edit_date_from,"' AND '",in_edit_date_to,"')");
	end if;
	if (in_store_id!=0) then 
		SET @Store=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_bill_transactor_id!=0) then 
		SET @BillTransactor=concat(' AND t.bill_transactor_id=',in_bill_transactor_id);
	end if;
	if (in_transaction_type_id!=0) then 
		SET @TransType=concat(' AND t.transaction_type_id=',in_transaction_type_id);
	end if;
	if (in_add_user_detail_id!=0) then 
		SET @AddUser=concat(' AND t.add_user_detail_id=',in_add_user_detail_id);
	end if;
	if (in_edit_user_detail_id!=0) then 
		SET @EditUser=concat(' AND t.edit_user_detail_id=',in_edit_user_detail_id);
	end if;
	if (in_transaction_id!=0) then 
		SET @TransId=concat(' AND t.transaction_id=',in_transaction_id);
	end if;
	if (in_pay_method_id!=0) then 
		SET @PayMethodId=concat(' AND t.pay_method_id=',in_pay_method_id);
	end if;
	if (in_pay_id!=0) then 
		SET @PayId=concat(' AND t.pay_id=',in_pay_id);
	end if;
	
	if (in_group_by_field!='') then 
		SET @GroupBy1=concat('store_id,transaction_type_id,',in_group_by_field,' AS field_name,',
		'SUM(t.paid_amount) AS sum_paid_amount,',
		'SUM(t.points_spent_amount) AS sum_points_spent_amount');
		SET @GroupBy2=concat(' GROUP BY store_id,transaction_type_id,',in_group_by_field,' ORDER BY store_id,transaction_type_id,',in_group_by_field);
	else
		SET @GroupBy1=concat('store_id,transaction_type_id,',
		'SUM(t.paid_amount) AS sum_paid_amount,',
		'SUM(t.points_spent_amount) AS sum_points_spent_amount');
		SET @GroupBy2=' GROUP BY store_id,transaction_type_id';
	end if;
	
	SET @sql_text=concat('SELECT store_id,transaction_type_id,add_user_names,edit_user_names,bill_transactor_names,store_name,transaction_type_name,pay_method_name,',@GroupBy1,' FROM view_pay t WHERE 1=1 ',
		@TransId,@PayDate,@AddDate,@EditDate,@Store,@AddUser,@EditUser,@BillTransactor,@TransType,@PayMethodId,@PayId,
		' ',@GroupBy2);
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_stock_in;
DELIMITER //
CREATE PROCEDURE sp_report_stock_in
(
	IN in_store_id int,
	IN in_category_id int,
	IN in_sub_category_id int,
	IN in_expiry_date_from date,
	IN in_expiry_date_to date
) 
BEGIN 
	SET @Store='';
	SET @Category='';
	SET @SubCategory='';
	SET @ExpiryDate='';

	if (in_store_id!=0) then 
		SET @Store=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_category_id!=0) then 
		SET @Category=concat(' AND t.category_id=',in_category_id);
	end if;
	if (in_sub_category_id!=0) then 
		SET @SubCategory=concat(' AND t.sub_category_id=',in_sub_category_id);
	end if;
	if ((in_expiry_date_from is not null) and (in_expiry_date_to is not null)) then 
		SET @ExpiryDate=concat(" AND (t.item_exp_date BETWEEN '",in_expiry_date_from,"' AND '",in_expiry_date_to,"')");
	end if;
	
	SET @sql_text=concat('SELECT * FROM view_stock_in AS t WHERE 1=1 ',
		@Store,@Category,@SubCategory,@ExpiryDate, ' ORDER BY t.description ASC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_stock_in_summary;
DELIMITER //
CREATE PROCEDURE sp_report_stock_in_summary
(
	IN in_store_id int,
	IN in_category_id int,
	IN in_sub_category_id int,
	IN in_expiry_date_from date,
	IN in_expiry_date_to date
) 
BEGIN 
	SET @Store='';
	SET @Category='';
	SET @SubCategory='';
	SET @ExpiryDate='';

	if (in_store_id!=0) then 
		SET @Store=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_category_id!=0) then 
		SET @Category=concat(' AND t.category_id=',in_category_id);
	end if;
	if (in_sub_category_id!=0) then 
		SET @SubCategory=concat(' AND t.sub_category_id=',in_sub_category_id);
	end if;
	if ((in_expiry_date_from is not null) and (in_expiry_date_to is not null)) then 
		SET @ExpiryDate=concat(" AND (t.item_exp_date BETWEEN '",in_expiry_date_from,"' AND '",in_expiry_date_to,"')");
	end if;
	
	SET @sql_text=concat('SELECT store_id,store_name,category_id,category_name,sub_category_id,sub_category_name,
	sum(currentqty) AS sum_currentqty	FROM view_stock_in AS t WHERE 1=1 ',@Store,@Category,@SubCategory,@ExpiryDate,
	' GROUP BY store_id,category_id,sub_category_id',' ORDER BY store_name,category_name,sub_category_name ASC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_stock_total;
DELIMITER //
CREATE PROCEDURE sp_report_stock_total
(
	IN in_store_id int,
	IN in_category_id int,
	IN in_sub_category_id int
) 
BEGIN 
	SET @Store='';
	SET @Category='';
	SET @SubCategory='';

	if (in_store_id!=0) then 
		SET @Store=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_category_id!=0) then 
		SET @Category=concat(' AND t.category_id=',in_category_id);
	end if;
	if (in_sub_category_id!=0) then 
		SET @SubCategory=concat(' AND t.sub_category_id=',in_sub_category_id);
	end if;
	
	SET @sql_text=concat('SELECT * FROM view_stock_total AS t WHERE 1=1 ',
		@Store,@Category,@SubCategory, ' ORDER BY t.description ASC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_stock_total_summary;
DELIMITER //
CREATE PROCEDURE sp_report_stock_total_summary
(
	IN in_store_id int,
	IN in_category_id int,
	IN in_sub_category_id int
) 
BEGIN 
	SET @Store='';
	SET @Category='';
	SET @SubCategory='';

	if (in_store_id!=0) then 
		SET @Store=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_category_id!=0) then 
		SET @Category=concat(' AND t.category_id=',in_category_id);
	end if;
	if (in_sub_category_id!=0) then 
		SET @SubCategory=concat(' AND t.sub_category_id=',in_sub_category_id);
	end if;
	
	SET @sql_text=concat('SELECT store_id,store_name,category_id,category_name,sub_category_id,sub_category_name,
	sum(currentqty) AS sum_currentqty,sum(cost_value) AS sum_cost_value,
	sum(wholesale_value) AS sum_wholesale_value,sum(retailsale_value) AS sum_retailsale_value 
	FROM view_stock_total AS t WHERE 1=1 ',@Store,@Category,@SubCategory,
	' GROUP BY store_id,category_id,sub_category_id',' ORDER BY store_name,category_name,sub_category_name ASC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_stock_all;
DELIMITER //
CREATE PROCEDURE sp_report_stock_all
(
	IN in_category_id int,
	IN in_sub_category_id int,
	IN in_reorder_filter varchar(3)
) 
BEGIN 
	SET @Category='';
	SET @SubCategory='';
	SET @ReorderFilter='';

	if (in_category_id!=0) then 
		SET @Category=concat(' AND t.category_id=',in_category_id);
	end if;
	if (in_sub_category_id!=0) then 
		SET @SubCategory=concat(' AND t.sub_category_id=',in_sub_category_id);
	end if;
	if(in_reorder_filter='Yes') then 
		SET @ReorderFilter=' AND t.currentqty<=t.reorder_level';
	end if;
	if(in_reorder_filter='No') then 
		SET @ReorderFilter=' AND t.currentqty>t.reorder_level';
	end if;

	SET @sql_text=concat('SELECT * FROM view_stock_all AS t WHERE 1=1 ',
	@Category,@SubCategory,@ReorderFilter,' ORDER BY t.description ASC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_day_of_week;
DELIMITER //
CREATE PROCEDURE sp_get_day_of_week
(
	IN in_day_of_week varchar(50),
	OUT out_day_of_week int
) 
BEGIN 
	SET @dayno=0;
	if (in_day_of_week='Monday') then 
		SET @dayno=1;
	end if;
	if (in_day_of_week='Tuesday') then 
		SET @dayno=2;
	end if;
	if (in_day_of_week='Wednesday') then 
		SET @dayno=3;
	end if;
	if (in_day_of_week='Thursday') then 
		SET @dayno=4;
	end if;
	if (in_day_of_week='Friday') then 
		SET @dayno=5;
	end if;
	if (in_day_of_week='Saturday') then 
		SET @dayno=6;
	end if;
	if (in_day_of_week='Sunday') then 
		SET @dayno=7;
	end if;
	SET out_day_of_week=@dayno;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_stock_all_summary;
DELIMITER //
CREATE PROCEDURE sp_report_stock_all_summary
(
	IN in_category_id int,
	IN in_sub_category_id int,
	IN in_reorder_filter varchar(3)
) 
BEGIN 
	SET @Category='';
	SET @SubCategory='';
	SET @ReorderFilter='';

	if (in_category_id!=0) then 
		SET @Category=concat(' AND t.category_id=',in_category_id);
	end if;
	if (in_sub_category_id!=0) then 
		SET @SubCategory=concat(' AND t.sub_category_id=',in_sub_category_id);
	end if;
	if(in_reorder_filter='Yes') then 
		SET @ReorderFilter=' AND t.currentqty<=t.reorder_level';
	end if;
	if(in_reorder_filter='No') then 
		SET @ReorderFilter=' AND t.currentqty>t.reorder_level';
	end if;
	
	SET @sql_text=concat('SELECT category_id,category_name,sub_category_id,sub_category_name,
	sum(currentqty) AS sum_currentqty FROM view_stock_all AS t WHERE 1=1 ',@Category,@SubCategory,@ReorderFilter,
	' GROUP BY category_id,sub_category_id',' ORDER BY category_name,sub_category_name ASC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_item;
DELIMITER //
CREATE PROCEDURE sp_report_item
(
	IN in_category_id int,
	IN in_sub_category_id int,
	IN in_is_suspended varchar(3)
) 
BEGIN 
	SET @Category='';
	SET @SubCategory='';
	SET @Suspended='';

	if (in_category_id!=0) then 
		SET @Category=concat(' AND i.category_id=',in_category_id);
	end if;
	if (in_sub_category_id!=0) then 
		SET @SubCategory=concat(' AND i.sub_category_id=',in_sub_category_id);
	end if;
	if (in_is_suspended!='') then 
		SET @Suspended=concat(" AND i.is_suspended='",in_is_suspended,"'");
	end if;

	SET @sql_text=concat('SELECT * FROM view_item AS i WHERE 1=1 ',
	@Category,@SubCategory,@Suspended,' ORDER BY i.description ASC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_item_summary;
DELIMITER //
CREATE PROCEDURE sp_report_item_summary
(
	IN in_category_id int,
	IN in_sub_category_id int,
	IN in_is_suspended varchar(3)
) 
BEGIN 
	SET @Category='';
	SET @SubCategory='';
	SET @Suspended='';

	if (in_category_id!=0) then 
		SET @Category=concat(' AND i.category_id=',in_category_id);
	end if;
	if (in_sub_category_id!=0) then 
		SET @SubCategory=concat(' AND i.sub_category_id=',in_sub_category_id);
	end if;
	if (in_is_suspended!='') then 
		SET @Suspended=concat(" AND i.is_suspended='",in_is_suspended,"'");
	end if;
	
	SET @sql_text=concat('SELECT item_id,category_id,category_name,sub_category_id,sub_category_name,
	count(item_id) AS count_items FROM view_item AS i WHERE 1=1 ',@Category,@SubCategory,@Suspended,
	' GROUP BY category_id,sub_category_id',' ORDER BY category_name,sub_category_name ASC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_transactor;
DELIMITER //
CREATE PROCEDURE sp_report_transactor
(
	IN in_transactor_type varchar(20)
) 
BEGIN 
	SET @TransactorType='';
	if (in_transactor_type!='') then 
		SET @TransactorType=concat(" AND t.transactor_type='",in_transactor_type,"'");
	end if;

	SET @sql_text=concat('SELECT * FROM transactor AS t WHERE 1=1 ',
	@TransactorType,' ORDER BY t.transactor_names ASC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_location;
DELIMITER //
CREATE PROCEDURE sp_insert_location
(
	IN in_store_id int,
	IN in_location_name varchar(20)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("location","location_id",@new_id);
	INSERT INTO location
	(
		location_id,
		store_id,
		location_name
	) 
    VALUES
	(
		@new_id,
		in_store_id,
		in_location_name
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_location;
DELIMITER //
CREATE PROCEDURE sp_update_location
(
	IN in_location_id bigint,
	IN in_store_id int,
	IN in_location_name varchar(20)
) 
BEGIN 
	UPDATE location SET 
		store_id=in_store_id,
		location_name=in_location_name
	WHERE location_id=in_location_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_location;
DELIMITER //
CREATE PROCEDURE sp_search_location
(
	IN in_store_id int,
	IN in_search_name varchar(20),
	IN in_limit_no int 
) 
BEGIN 
	SET @StoreId='';
	SET @SearchName='';
	SET @LimitNo='';

	if (in_store_id!=0) then 
		SET @StoreId=concat(' AND store_id=',in_store_id);
	end if;
	if (in_search_name!='') then 
		SET @SearchName=concat(" AND (store_name LIKE '%",in_search_name,"%' OR location_name LIKE '%",in_search_name,"%')");
	end if;
	if (in_limit_no!=0) then 
		SET @LimitNo=concat(' LIMIT ',in_limit_no);
	end if;
	
	SET @sql_text=concat('SELECT * FROM view_location WHERE 1=1 ',
		@StoreId,@SearchName,' ORDER BY store_name,location_name ASC ',@LimitNo);
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_location_by_item_id;
DELIMITER //
CREATE PROCEDURE sp_search_location_by_item_id
(
	IN in_item_id bigint
) 
BEGIN 
	SELECT * FROM view_item_location WHERE item_id=in_item_id ORDER BY store_name,location_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_location_by_store_item;
DELIMITER //
CREATE PROCEDURE sp_search_location_by_store_item
(
	IN in_store_id int,
	IN in_item_id bigint
) 
BEGIN 
	SELECT l.* FROM item_location il INNER JOIN location l ON il.location_id=l.location_id 
	WHERE il.item_id=in_item_id AND l.store_id=in_store_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_item_location;
DELIMITER //
CREATE PROCEDURE sp_report_item_location
(
	IN in_store_id int,
	IN in_location_id bigint,
	IN in_item_id bigint
) 
BEGIN 
	SET @Store='';
	SET @Location='';
	SET @Item='';

	if (in_store_id!=0) then 
		SET @Store=concat(' AND store_id=',in_store_id);
	end if;
	if (in_location_id!=0) then 
		SET @Location=concat(' AND location_id=',in_location_id);
	end if;
	if (in_item_id!=0) then 
		SET @Item=concat(' AND item_id=',in_item_id);
	end if;

	SET @sql_text=concat('SELECT * FROM view_item_location WHERE 1=1 ',
	@Store,@Location,@Item,' ORDER BY store_name,location_name,description ASC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_item_location;
DELIMITER //
CREATE PROCEDURE sp_insert_item_location
(
	IN in_item_id bigint,
	IN in_location_id bigint
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("item_location","item_location_id",@new_id);
	INSERT INTO item_location
	(
		item_location_id,
		item_id,
		location_id
	) 
    VALUES
	(
		@new_id,
		in_item_id,
		in_location_id
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_item_location;
DELIMITER //
CREATE PROCEDURE sp_update_item_location
(
	IN in_item_location_id bigint,
	IN in_item_id bigint,
	IN in_location_id bigint
) 
BEGIN 
	UPDATE item_location SET 
		item_id=in_item_id,
		location_id=in_location_id 
	WHERE item_location_id=in_item_location_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_location;
DELIMITER //
CREATE PROCEDURE sp_search_item_location
(
	IN in_item_location_id bigint,
	IN in_store_id int,
	IN in_item_id bigint,
	IN in_location_id bigint,
	IN in_limit_no int 
) 
BEGIN 
	SET @ItemLocationId='';
	SET @StoreId='';
	SET @ItemId='';
	SET @LocationId='';
	SET @LimitNo='';

	if (in_item_location_id!=0) then 
		SET @ItemLocationId=concat(' AND item_location_id=',in_item_location_id);
	end if;
	if (in_store_id!=0) then 
		SET @StoreId=concat(' AND store_id=',in_store_id);
	end if;
	if (in_item_id!=0) then 
		SET @ItemId=concat(' AND item_id=',in_item_id);
	end if;
	if (in_location_id!=0) then 
		SET @LocationId=concat(' AND location_id=',in_location_id);
	end if;
	if (in_limit_no!=0) then 
		SET @LimitNo=concat(' LIMIT ',in_limit_no);
	end if;
	
	SET @sql_text=concat('SELECT * FROM view_item_location WHERE 1=1 ',@ItemLocationId,@StoreId,
		@ItemId,@LocationId,' ORDER BY store_name,description,location_name ASC ',@LimitNo);
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_category_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_category_by_id
(
	IN in_category_id int
) 
BEGIN 
	SELECT * FROM category 
	WHERE category_id=in_category_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_category_by_name;
DELIMITER //
CREATE PROCEDURE sp_search_category_by_name
(
	IN in_category_name varchar(50)
) 
BEGIN 
	SELECT * FROM category 
	WHERE category_name LIKE concat('%',in_category_name,'%') 
	ORDER BY category_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_category_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_category_by_none() 
BEGIN 
	SELECT * FROM category ORDER BY category_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sub_category_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_sub_category_by_id
(
	IN in_sub_category_id int
) 
BEGIN 
	SELECT * FROM view_sub_category 
	WHERE sub_category_id=in_sub_category_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sub_category_by_category_id;
DELIMITER //
CREATE PROCEDURE sp_search_sub_category_by_category_id
(
	IN in_category_id int
) 
BEGIN 
	SELECT * FROM view_sub_category 
	WHERE category_id=in_category_id 
	ORDER BY category_name,sub_category_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sub_category_by_name;
DELIMITER //
CREATE PROCEDURE sp_search_sub_category_by_name
(
	IN in_search_name varchar(50)
) 
BEGIN 
	SELECT * FROM view_sub_category 
	WHERE sub_category_name LIKE concat('%',in_search_name,'%') OR category_name LIKE concat('%',in_search_name,'%') 
	ORDER BY category_name,sub_category_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sub_category_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_sub_category_by_none() 
BEGIN 
	SELECT * FROM view_sub_category ORDER BY category_name,sub_category_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_unit_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_unit_by_id
(
	IN in_unit_id int
) 
BEGIN 
	SELECT * FROM unit 
	WHERE unit_id=in_unit_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_unit_by_name;
DELIMITER //
CREATE PROCEDURE sp_search_unit_by_name
(
	IN in_unit_name varchar(50)
) 
BEGIN 
	SELECT * FROM unit 
	WHERE unit_name LIKE concat('%',in_unit_name,'%') 
	ORDER BY unit_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_unit_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_unit_by_none() 
BEGIN 
	SELECT * FROM unit 
	ORDER BY unit_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_method_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_pay_method_by_id
(
	IN in_pay_method_id int
) 
BEGIN 
	SELECT * FROM pay_method 
	WHERE pay_method_id=in_pay_method_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_method_default;
DELIMITER //
CREATE PROCEDURE sp_search_pay_method_default() 
BEGIN 
	SELECT * FROM pay_method 
	WHERE is_default=1 AND is_active=1 AND is_deleted=0; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_method_by_name;
DELIMITER //
CREATE PROCEDURE sp_search_pay_method_by_name
(
	IN in_pay_method_name varchar(50)
) 
BEGIN 
	SELECT * FROM pay_method 
	WHERE pay_method_name LIKE concat('%',in_pay_method_name,'%') 
	ORDER BY pay_method_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_method_by_equal_name;
DELIMITER //
CREATE PROCEDURE sp_search_pay_method_by_equal_name
(
	IN in_pay_method_name varchar(50)
) 
BEGIN 
	SELECT * FROM pay_method 
	WHERE pay_method_name=in_pay_method_name 
	ORDER BY pay_method_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_method_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_pay_method_by_none() 
BEGIN 
	SELECT * FROM pay_method 
	ORDER BY display_order ASC,pay_method_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_method_active;
DELIMITER //
CREATE PROCEDURE sp_search_pay_method_active() 
BEGIN 
	SELECT * FROM pay_method WHERE is_active=1 AND is_deleted=0 
	ORDER BY display_order ASC,pay_method_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_method_active_in;
DELIMITER //
CREATE PROCEDURE sp_search_pay_method_active_in
(
	IN in_pay_method_IDs varchar(100)
) 
BEGIN 
	SET @sql_text=concat('SELECT * FROM pay_method WHERE is_active=1 AND is_deleted=0 AND pay_method_id IN(',in_pay_method_IDs,') ORDER BY display_order ASC,pay_method_name ASC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_method_active_sales;
DELIMITER //
CREATE PROCEDURE sp_search_pay_method_active_sales() 
BEGIN 
	SELECT * FROM pay_method WHERE pay_method_id!=7 AND is_active=1 AND is_deleted=0 
	ORDER BY display_order ASC,pay_method_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_method_active_purchases;
DELIMITER //
CREATE PROCEDURE sp_search_pay_method_active_purchases() 
BEGIN 
	SELECT * FROM pay_method WHERE pay_method_id!=6 AND is_active=1 AND is_deleted=0 
	ORDER BY display_order ASC,pay_method_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_method_active_no_prepaid;
DELIMITER //
CREATE PROCEDURE sp_search_pay_method_active_no_prepaid() 
BEGIN 
	SELECT * FROM pay_method WHERE pay_method_id!=6 AND pay_method_id!=7 AND is_active=1 AND is_deleted=0 
	ORDER BY display_order ASC,pay_method_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transactor_ledger;
DELIMITER //
CREATE PROCEDURE sp_insert_transactor_ledger
(
	IN in_store_id int,
	IN in_store_name varchar(20),
	IN in_transaction_id bigint,
	IN in_pay_id bigint,
	IN in_transaction_type_name varchar(50),
	IN in_description varchar(50),
	IN in_transaction_date date,
	IN in_transactor_id bigint,
	IN in_transactor_names varchar(100),
	IN in_ledger_entry_type varchar(2),
	IN in_amount_debit double,
	IN in_amount_credit double,
	IN in_bill_transactor_id bigint,
	IN in_bill_transactor_names varchar(100)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("transactor_ledger","transactor_ledger_id",@new_id);
	
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);
	SET @transaction_id=null;
	if (in_transaction_id!=0) then 
		SET @transaction_id=in_transaction_id;
	end if;
	SET @transactor_id=null;
	if (in_transactor_id!=0) then 
		SET @transactor_id=in_transactor_id;
	end if;

	INSERT INTO transactor_ledger
	(
		transactor_ledger_id,
		store_id,
		store_name,
		transaction_id,
		pay_id,
		transaction_type_name,
		description,
		transaction_date,
		add_date,
		transactor_id,
		transactor_names,
		ledger_entry_type,
		amount_debit,
		amount_credit,
		bill_transactor_id,
		bill_transactor_names
	) 
    VALUES
	(
		@new_id,
		in_store_id,
		in_store_name,
		@transaction_id,
		in_pay_id,
		in_transaction_type_name,
		in_description,
		in_transaction_date,
		@cur_sys_datetime,
		@transactor_id,
		in_transactor_names,
		in_ledger_entry_type,
		in_amount_debit,
		in_amount_credit,
		in_bill_transactor_id,
		in_bill_transactor_names
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_transactor_ledger;
DELIMITER //
CREATE PROCEDURE sp_update_transactor_ledger
(
	IN in_transactor_ledger_id bigint,
	IN in_store_id int,
	IN in_store_name varchar(20),
	IN in_transaction_id bigint,
	IN in_pay_id bigint,
	IN in_transaction_type_name varchar(50),
	IN in_description varchar(50),
	IN in_transaction_date date,
	IN in_add_date datetime,
	IN in_transactor_id bigint,
	IN in_transactor_names varchar(100),
	IN in_ledger_entry_type varchar(2),
	IN in_amount_debit double,
	IN in_amount_credit double,
	IN in_bill_transactor_id bigint,
	IN in_bill_transactor_names varchar(100)
) 
BEGIN 
	SET @transaction_id=null;
	if (in_transaction_id!=0) then 
		SET @transaction_id=in_transaction_id;
	end if;
	SET @transactor_id=null;
	if (in_transactor_id!=0) then 
		SET @transactor_id=in_transactor_id;
	end if;

	UPDATE transactor_ledger SET 
		store_id=in_store_id,
		store_name=in_store_name,
		transaction_id=@transaction_id,
		pay_id=in_pay_id,
		transaction_type_name=in_transaction_type_name,
		description=in_description,
		transaction_date=in_transaction_date,
		add_date=in_add_date,
		transactor_id=@transactor_id,
		transactor_names=in_transactor_names,
		ledger_entry_type=in_ledger_entry_type,
		amount_debit=in_amount_debit,
		amount_credit=in_amount_credit,
		bill_transactor_id=in_bill_transactor_id,
		bill_transactor_names=in_bill_transactor_names 
	WHERE transactor_ledger_id=in_transactor_ledger_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transactor_ledger_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_transactor_ledger_by_id
(
	IN in_transactor_ledger_id bigint
) 
BEGIN 
	SELECT * FROM transactor_ledger
	WHERE transactor_ledger_id=in_transactor_ledger_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transactor_ledger_by_transid_transtype;
DELIMITER //
CREATE PROCEDURE sp_search_transactor_ledger_by_transid_transtype
(
	IN in_transaction_id bigint,
	IN in_transaction_type_name varchar(50)
) 
BEGIN 
	SELECT * FROM transactor_ledger l1 WHERE l1.transactor_ledger_id=(
		SELECT MIN(transactor_ledger_id) FROM transactor_ledger l2 
		WHERE l2.transaction_id=in_transaction_id AND l2.transaction_type_name=in_transaction_type_name); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transactor_ledger_by_min_transid_type_desc;
DELIMITER //
CREATE PROCEDURE sp_search_transactor_ledger_by_min_transid_type_desc
(
	IN in_transaction_id bigint,
	IN in_transaction_type_name varchar(50),
	IN in_description varchar(50)
) 
BEGIN 
	SELECT * FROM transactor_ledger l1 WHERE l1.transactor_ledger_id=(
		SELECT MIN(transactor_ledger_id) FROM transactor_ledger l2 
		WHERE l2.transaction_id=in_transaction_id AND l2.transaction_type_name=in_transaction_type_name AND 
		l2.description=in_description); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_transactor_ledger;
DELIMITER //
CREATE PROCEDURE sp_report_transactor_ledger
(
	IN in_store_id int,
	IN in_transaction_date_from date,
	IN in_transaction_date_to date,
	IN in_add_date_from datetime,
	IN in_add_date_to datetime,
	IN in_bill_transactor_id bigint,
	IN in_transaction_type_name varchar(50)
) 
BEGIN 
	SET @Store='';
	SET @TransDate='';
	SET @AddDate='';
	SET @BillTransactor='';
	SET @TransactionTypeName='';

	if (in_store_id!=0) then 
		SET @Store=concat(' AND store_id=',in_store_id);
	end if;
	if ((in_transaction_date_from is not null) and (in_transaction_date_to is not null)) then 
		SET @TransDate=concat(" AND (transaction_date BETWEEN '",in_transaction_date_from,"' AND '",in_transaction_date_to,"')");
	end if;
	if ((in_add_date_from is not null) and (in_add_date_to is not null)) then 
		SET @AddDate=concat(" AND (add_date BETWEEN '",in_add_date_from,"' AND '",in_add_date_to,"')");
	end if;
	if (in_bill_transactor_id!=0) then 
		SET @BillTransactor=concat(' AND bill_transactor_id=',in_bill_transactor_id);
	end if;
	if (in_transaction_type_name!='') then 
		SET @TransactionTypeName=concat(" AND transaction_type_name='",in_transaction_type_name,"'");
	end if;

	SET @sql_text=concat('SELECT * FROM transactor_ledger WHERE 1=1 ',@Store,@TransactionTypeName,@TransDate,@AddDate,@BillTransactor,' ORDER BY add_date DESC,transactor_ledger_id DESC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_transactor_ledger_summary_all;
DELIMITER //
CREATE PROCEDURE sp_report_transactor_ledger_summary_all
() 
BEGIN 
	SET @sql_text="SELECT SUM(amount_debit) as sum_amount_debit,SUM(amount_credit) as sum_amount_credit FROM transactor_ledger";
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_transactor_ledger_summary_all_individual;
DELIMITER //
CREATE PROCEDURE sp_report_transactor_ledger_summary_all_individual
() 
BEGIN 
	SET @sql_text=concat("SELECT bill_transactor_id,bill_transactor_names,SUM(amount_debit) as sum_amount_debit,SUM(amount_credit) as sum_amount_credit FROM transactor_ledger GROUP BY bill_transactor_id ORDER BY bill_transactor_names ASC");
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_transactor_ledger_summary_single_individual;
DELIMITER //
CREATE PROCEDURE sp_report_transactor_ledger_summary_single_individual
(
	IN in_bill_transactor_id bigint
) 
BEGIN 
	SET @sql_text=concat("SELECT SUM(amount_debit) as sum_amount_debit,
	SUM(amount_credit) as sum_amount_credit FROM transactor_ledger 
	WHERE bill_transactor_id=",in_bill_transactor_id);
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_copy_transaction;
DELIMITER //
CREATE PROCEDURE sp_copy_transaction
(
	IN in_transaction_id bigint,
	IN in_hist_flag varchar(10),
	OUT out_transaction_hist_id bigint
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("transaction_hist","transaction_hist_id",@new_id);
	
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	INSERT INTO transaction_hist(transaction_hist_id,hist_flag,hist_add_date,
	transaction_id,transaction_date,store_id,store2_id,transactor_id,transaction_type_id,
	transaction_reason_id,total_trade_discount,total_vat,add_user_detail_id,add_date,
	edit_user_detail_id,edit_date,transaction_ref,transaction_comment,sub_total,grand_total,
	cash_discount,points_awarded,card_number,total_std_vatable_amount,total_zero_vatable_amount,
	total_exempt_vatable_amount,vat_perc,amount_tendered,change_amount,is_cash_discount_vat_liable,
	total_profit_margin,transaction_user_detail_id,bill_transactor_id,scheme_transactor_id,
	princ_scheme_member,scheme_card_number,transaction_number,delivery_date,delivery_address,
	pay_terms,terms_conditions,authorised_by_user_detail_id,authorise_date,pay_due_date,expiry_date,
	acc_child_account_id,currency_code,xrate,from_date,to_date,duration_type,site_id,transactor_rep,
	transactor_vehicle,transactor_driver,duration_value,
    location_id,status_code,status_date,delivery_mode,is_processed,is_paid,is_cancel,is_invoiced,is_delivered,source_code,total_paid,spent_points_amount
	) SELECT @new_id,in_hist_flag,@cur_sys_datetime,
	transaction_id,transaction_date,store_id,store2_id,transactor_id,transaction_type_id,
	transaction_reason_id,total_trade_discount,total_vat,add_user_detail_id,add_date,
	edit_user_detail_id,edit_date,transaction_ref,transaction_comment,sub_total,grand_total,
	cash_discount,points_awarded,card_number,total_std_vatable_amount,total_zero_vatable_amount,
	total_exempt_vatable_amount,vat_perc,amount_tendered,change_amount,is_cash_discount_vat_liable,
	total_profit_margin,transaction_user_detail_id,bill_transactor_id,scheme_transactor_id,
	princ_scheme_member,scheme_card_number,transaction_number,delivery_date,delivery_address,
	pay_terms,terms_conditions,authorised_by_user_detail_id,authorise_date,pay_due_date,expiry_date,
	acc_child_account_id,currency_code,xrate,from_date,to_date,duration_type,site_id,transactor_rep,
	transactor_vehicle,transactor_driver,duration_value,
    location_id,status_code,status_date,delivery_mode,is_processed,is_paid,is_cancel,is_invoiced,is_delivered,source_code,total_paid,spent_points_amount 
	FROM transaction WHERE transaction_id=in_transaction_id;
	SET out_transaction_hist_id=@new_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_copy_transaction_item;
DELIMITER //
CREATE PROCEDURE sp_copy_transaction_item
(
	IN in_transaction_id bigint,
	IN in_transaction_hist_id bigint,
	IN in_transaction_item_id bigint
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("transaction_item_hist","transaction_item_hist_id",@new_id);

	INSERT INTO transaction_item_hist(transaction_item_hist_id,transaction_hist_id,
	transaction_item_id,transaction_id,item_id,batchno,item_qty,unit_price,item_expiry_date,
    item_mnf_date,unit_trade_discount,unit_vat,amount,vat_rated,vat_perc,unit_price_exc_vat,
    amount_inc_vat,amount_exc_vat,unit_price_inc_vat,stock_effect,is_trade_discount_vat_liable,
	unit_cost_price,unit_profit_margin,earn_perc,earn_amount,
	code_specific,desc_specific,desc_more,warranty_expiry_date,warranty_desc,account_code,purchase_date,
	dep_start_date,dep_method_id,dep_rate,average_method_id,effective_life,residual_value,narration,qty_balance,duration_value,
	qty_damage,duration_passed,specific_size
	) SELECT @new_id,in_transaction_hist_id,
	transaction_item_id,transaction_id,item_id,batchno,item_qty,unit_price,item_expiry_date,
    item_mnf_date,unit_trade_discount,unit_vat,amount,vat_rated,vat_perc,unit_price_exc_vat,
    amount_inc_vat,amount_exc_vat,unit_price_inc_vat,stock_effect,is_trade_discount_vat_liable,
	unit_cost_price,unit_profit_margin,earn_perc,earn_amount,
	code_specific,desc_specific,desc_more,warranty_expiry_date,warranty_desc,account_code,purchase_date,
	dep_start_date,dep_method_id,dep_rate,average_method_id,effective_life,residual_value,narration,qty_balance,duration_value,
	qty_damage,duration_passed,specific_size 
	FROM transaction_item WHERE transaction_item_id=in_transaction_item_id AND transaction_id=in_transaction_id;
    
    INSERT INTO transaction_item_hist_unit(transaction_item_hist_id,unit_id,base_unit_qty) 
	SELECT @new_id,unit_id,base_unit_qty   
	FROM transaction_item_unit WHERE transaction_item_id=in_transaction_item_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_deleted_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_deleted_by_id
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT * FROM transaction_hist t 
		WHERE t.transaction_id=in_transaction_id AND t.hist_flag='Delete';
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_void_transaction;
DELIMITER //
CREATE PROCEDURE sp_void_transaction
(
	IN in_transaction_id bigint,
	IN in_edit_user_detail_id int
) 
BEGIN 

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	UPDATE transaction SET 
		cash_discount=0,
		total_vat=0,
		edit_user_detail_id=in_edit_user_detail_id,
		edit_date=@cur_sys_datetime,
		sub_total=0,
		grand_total=0,
		total_trade_discount=0,
		total_std_vatable_amount=0,
		total_zero_vatable_amount=0,
		total_exempt_vatable_amount=0 
	WHERE transaction_id=in_transaction_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_void_transaction_item;
DELIMITER //
CREATE PROCEDURE sp_void_transaction_item
(
	IN in_transaction_item_id bigint
) 
BEGIN 

	UPDATE transaction_item SET 
		item_qty=0,
		unit_price=0,
		unit_trade_discount=0,
		unit_vat=0,
		amount=0,
		unit_price_inc_vat =0,
		unit_price_exc_vat =0,
		amount_inc_vat =0,
		amount_exc_vat =0  
	WHERE transaction_item_id=in_transaction_item_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_item_hist_by_ids;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_item_hist_by_ids
(
	IN in_transaction_id bigint,
	IN in_transaction_hist_id bigint
) 
BEGIN 
		SELECT tih.*,tiu.unit_id,tiu.base_unit_qty FROM transaction_item_hist tih 
        INNER JOIN transaction_item_hist_unit tiu ON tih.transaction_item_hist_id=tiu.transaction_item_hist_id 
		WHERE tih.transaction_hist_id=in_transaction_hist_id AND tih.transaction_id=in_transaction_id ORDER BY tih.transaction_item_id ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_by_add_user_detail_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_by_add_user_detail_id
(
	IN in_add_user_detail_id int,
	IN in_transaction_type_id int,
	IN in_store_id int
) 
BEGIN 
		SELECT * FROM transaction t 
		WHERE t.store_id=in_store_id AND t.transaction_type_id=in_transaction_type_id AND 
		t.add_user_detail_id=in_add_user_detail_id 
		ORDER BY t.transaction_id DESC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_copy_pay;
DELIMITER //
CREATE PROCEDURE sp_copy_pay
(
	IN in_pay_id bigint,
	IN in_hist_flag varchar(10),
	OUT out_pay_hist_id bigint
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("pay_hist","pay_hist_id",@new_id);
	
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	INSERT INTO pay_hist(pay_hist_id,hist_flag,hist_add_date,
	pay_id,transaction_id,pay_date,paid_amount,pay_method_id,add_user_detail_id,
	edit_user_detail_id,add_date,edit_date,points_spent,points_spent_amount,delete_pay_id,
	pay_ref_no,pay_category,bill_transactor_id,transaction_type_id,transaction_reason_id,store_id
	) SELECT @new_id,in_hist_flag,@cur_sys_datetime,
	pay_id,transaction_id,pay_date,paid_amount,pay_method_id,add_user_detail_id,
	edit_user_detail_id,add_date,edit_date,points_spent,points_spent_amount,delete_pay_id,
	pay_ref_no,pay_category,bill_transactor_id,transaction_type_id,transaction_reason_id,store_id 
	FROM pay WHERE pay_id=in_pay_id;
	SET out_pay_hist_id=@new_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_login_session;
DELIMITER //
CREATE PROCEDURE sp_insert_login_session
(
	IN in_user_detail_id int,
	IN in_store_id int,
	IN in_session_id varchar(100),
	IN in_remote_ip varchar(50),
	IN in_remote_host varchar(50),
	IN in_remote_user varchar(50)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("login_session","login_session_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	INSERT INTO login_session
	(
		login_session_id,
		user_detail_id,
		store_id,
		session_id,
		add_date,
		remote_ip,
		remote_host,
		remote_user
	) 
    VALUES
	(
		@new_id,
		in_user_detail_id,
		in_store_id,
		in_session_id,
		@cur_sys_datetime,
		in_remote_ip,
		in_remote_host,
		in_remote_user
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_delete_login_session_by_user;
DELIMITER //
CREATE PROCEDURE sp_delete_login_session_by_user
(
	IN in_user_detail_id int
) 
BEGIN 
	DELETE FROM login_session WHERE user_detail_id=in_user_detail_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_delete_login_session_by_session;
DELIMITER //
CREATE PROCEDURE sp_delete_login_session_by_session
(
	IN in_session_id varchar(100)
) 
BEGIN 
	DELETE FROM login_session WHERE session_id=in_session_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_login_session;
DELIMITER //
CREATE PROCEDURE sp_search_login_session() 
BEGIN 
	SELECT * FROM login_session ORDER BY add_date DESC LIMIT 100;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_login_session_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_login_session_by_id
(
	IN in_login_session_id int
) 
BEGIN 
	SELECT * FROM login_session WHERE login_session_id=in_login_session_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_login_session_by_user;
DELIMITER //
CREATE PROCEDURE sp_search_login_session_by_user
(
	IN in_user_detail_id int
) 
BEGIN 
	SELECT * FROM login_session WHERE user_detail_id=in_user_detail_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_login_session_by_session;
DELIMITER //
CREATE PROCEDURE sp_search_login_session_by_session
(
	IN in_session_id varchar(100)
) 
BEGIN 
	SELECT * FROM login_session WHERE session_id=in_session_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_delete_login_session_unlogged_out;
DELIMITER //
CREATE PROCEDURE sp_delete_login_session_unlogged_out
() 
BEGIN 
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);
	DELETE FROM login_session where timediff(@cur_sys_datetime,add_date)>'12:00:00';
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_user_category;
DELIMITER //
CREATE PROCEDURE sp_insert_user_category
(
	IN in_user_category_name varchar(50)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("user_category","user_category_id",@new_id);
	INSERT INTO user_category
	(
		user_category_id,
		user_category_name
	) 
    VALUES
	(
		@new_id,
		in_user_category_name
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_user_category;
DELIMITER //
CREATE PROCEDURE sp_update_user_category
(
	IN in_user_category_id int,
	IN in_user_category_name varchar(50)
) 
BEGIN 
	UPDATE user_category SET 
		user_category_name=in_user_category_name
	WHERE user_category_id=in_user_category_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_user_category_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_user_category_by_id
(
	IN in_user_category_id int
) 
BEGIN 
	SELECT * FROM user_category 
	WHERE user_category_id=in_user_category_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_user_category_by_name;
DELIMITER //
CREATE PROCEDURE sp_search_user_category_by_name
(
	IN in_search_name varchar(50)
) 
BEGIN 
	SELECT * FROM user_category 
	WHERE user_category_name LIKE concat('%',in_search_name,'%')  
	ORDER BY user_category_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_user_category_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_user_category_by_none() 
BEGIN 
	SELECT * FROM user_category ORDER BY user_category_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_user_item_earn;
DELIMITER //
CREATE PROCEDURE sp_insert_user_item_earn
(
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_user_category_id int,
	IN in_item_category_id int,
	IN in_item_sub_category_id int,
	IN in_earn_perc double
) 
BEGIN 
	SET @new_id=0;
	SET @item_sub_category_id=NULL;
	if (in_item_sub_category_id!=0) then
		set @item_sub_category_id=in_item_sub_category_id;
	end if;
	CALL sp_get_new_id("user_item_earn","user_item_earn_id",@new_id);

	INSERT INTO user_item_earn
	(
		user_item_earn_id,
		transaction_type_id,
		transaction_reason_id,
		user_category_id,
		item_category_id,
		item_sub_category_id,
		earn_perc
	) 
    VALUES
	(
		@new_id,
		in_transaction_type_id,
		in_transaction_reason_id,
		in_user_category_id,
		in_item_category_id,
		in_item_sub_category_id,
		in_earn_perc
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_user_item_earn;
DELIMITER //
CREATE PROCEDURE sp_update_user_item_earn
(
	IN in_user_item_earn_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_user_category_id int,
	IN in_item_category_id int,
	IN in_item_sub_category_id int,
	IN in_earn_perc double
) 
BEGIN 
	SET @item_sub_category_id=NULL;
	if (in_item_sub_category_id!=0) then
		set @item_sub_category_id=in_item_sub_category_id;
	end if;

	UPDATE user_item_earn SET 
		transaction_type_id=in_transaction_type_id,
		transaction_reason_id=in_transaction_reason_id,
		user_category_id=in_user_category_id,
		item_category_id=in_item_category_id,
		item_sub_category_id=in_item_sub_category_id,
		earn_perc=in_earn_perc 
	WHERE user_item_earn_id=in_user_item_earn_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_user_item_earn_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_user_item_earn_by_id
(
	IN in_user_item_earn_id bigint 
) 
BEGIN 
		SELECT * FROM user_item_earn 
		WHERE user_item_earn_id=in_user_item_earn_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_user_item_earn_by_user_cat;
DELIMITER //
CREATE PROCEDURE sp_search_user_item_earn_by_user_cat
(
	IN in_user_category_id int 
) 
BEGIN 
		SELECT * FROM user_item_earn 
		WHERE user_category_id=in_user_category_id 
		ORDER BY transaction_type_id,user_category_id,item_category_id,item_sub_category_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_user_item_earn_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_user_item_earn_by_none() 
BEGIN 
		SELECT * FROM user_item_earn 
		ORDER BY transaction_type_id,user_category_id,item_category_id,item_sub_category_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_user_item_earn_by_ttype_treas_icat_isubcat_ucat;
DELIMITER //
CREATE PROCEDURE sp_search_user_item_earn_by_ttype_treas_icat_isubcat_ucat
(
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_item_category_id int,
	IN in_item_sub_category_id int,
	IN in_user_category_id int
) 
BEGIN 
	if (in_item_sub_category_id>0) then
		SELECT * FROM user_item_earn WHERE 
		transaction_type_id=in_transaction_type_id AND transaction_reason_id=in_transaction_reason_id AND 
		item_category_id=in_item_category_id AND item_sub_category_id=in_item_sub_category_id AND 
		user_category_id=in_user_category_id;
	end if;
	if (in_item_sub_category_id<=0) then
		SELECT * FROM user_item_earn WHERE 
		transaction_type_id=in_transaction_type_id AND transaction_reason_id=in_transaction_reason_id AND 
		item_category_id=in_item_category_id AND user_category_id=in_user_category_id;
	end if;
	
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_bill;
DELIMITER //
CREATE PROCEDURE sp_report_bill
(
	IN in_transaction_date_from date,
	IN in_transaction_date_to date,
	IN in_store_id int,
	IN in_bill_transactor_id bigint 
) 
BEGIN 
	SET @TransDate='';
	SET @FromStore='';
	SET @BillTransactor='';

	if ((in_transaction_date_from is not null) and (in_transaction_date_to is not null)) then 
		SET @TransDate=concat(" AND (t.transaction_date BETWEEN '",in_transaction_date_from,"' AND '",in_transaction_date_to,"')");
	end if;
	if (in_store_id!=0) then 
		SET @FromStore=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_bill_transactor_id!=0) then 
		SET @BillTransactor=concat(' AND t.bill_transactor_id=',in_bill_transactor_id);
	end if;
	
	SET @sql_text=concat('SELECT * FROM view_transaction t WHERE transaction_type_id=2 ',@TransDate,@FromStore,@BillTransactor,' ORDER BY t.transaction_id DESC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_bill_summary;
DELIMITER //
CREATE PROCEDURE sp_report_bill_summary
(
	IN in_transaction_date_from date,
	IN in_transaction_date_to date,
	IN in_store_id int,
	IN in_bill_transactor_id bigint 
) 
BEGIN 
	SET @TransDate='';
	SET @FromStore='';
	SET @BillTransactor='';

	if ((in_transaction_date_from is not null) and (in_transaction_date_to is not null)) then 
		SET @TransDate=concat(" AND (t.transaction_date BETWEEN '",in_transaction_date_from,"' AND '",in_transaction_date_to,"')");
	end if;
	if (in_store_id!=0) then 
		SET @FromStore=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_bill_transactor_id!=0) then 
		SET @BillTransactor=concat(' AND t.bill_transactor_id=',in_bill_transactor_id);
	end if;
	
	SET @sql_text=concat('SELECT currency_code,sum(grand_total) as sum_grand_total FROM view_transaction t WHERE transaction_type_id=2 ',@TransDate,@FromStore,@BillTransactor,' GROUP BY currency_code');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;


DROP PROCEDURE IF EXISTS sp_report_bill_items_summary;
DELIMITER //
CREATE PROCEDURE sp_report_bill_items_summary
(
	IN in_transaction_id bigint 
) 
BEGIN 
	SELECT category_id,category_name,sum(amount_exc_vat) as sum_amount_exc_vat,sum(amount_inc_vat) as sum_amount_inc_vat 
	FROM view_transaction_item 
	WHERE transaction_id=in_transaction_id 
	GROUP BY category_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_customer_card;
DELIMITER //
CREATE PROCEDURE sp_report_customer_card
(
	IN in_transaction_date_from date,
	IN in_transaction_date_to date,
	IN in_store_id int,
	IN in_bill_transactor_id bigint 
) 
BEGIN 
	SET @TransDate='';
	SET @FromStore='';
	SET @BillTransactor='';

	if ((in_transaction_date_from is not null) and (in_transaction_date_to is not null)) then 
		SET @TransDate=concat(" AND (t.transaction_date BETWEEN '",in_transaction_date_from,"' AND '",in_transaction_date_to,"')");
	end if;
	if (in_store_id!=0) then 
		SET @FromStore=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_bill_transactor_id!=0) then 
		SET @BillTransactor=concat(' AND t.bill_transactor_id=',in_bill_transactor_id);
	end if;
	
	SET @sql_text=concat('SELECT t.*,ud.user_name,tr.transactor_names,
		(select sum(pt.trans_paid_amount) from pay_trans pt where pt.transaction_id=t.transaction_id) as total_paid2 
		FROM transaction t  
		INNER JOIN user_detail ud ON t.add_user_detail_id=ud.user_detail_id 
		INNER JOIN transactor tr ON t.transactor_id=tr.transactor_id WHERE (t.transaction_type_id IN(2,65,68) OR t.transaction_reason_id IN(117)) ',@TransDate,@FromStore,@BillTransactor,' ORDER BY t.transaction_id DESC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_supplier_card;
DELIMITER //
CREATE PROCEDURE sp_report_supplier_card
(
	IN in_transaction_date_from date,
	IN in_transaction_date_to date,
	IN in_store_id int,
	IN in_bill_transactor_id bigint 
) 
BEGIN 
	SET @TransDate='';
	SET @FromStore='';
	SET @BillTransactor='';

	if ((in_transaction_date_from is not null) and (in_transaction_date_to is not null)) then 
		SET @TransDate=concat(" AND (t.transaction_date BETWEEN '",in_transaction_date_from,"' AND '",in_transaction_date_to,"')");
	end if;
	if (in_store_id!=0) then 
		SET @FromStore=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_bill_transactor_id!=0) then 
		SET @BillTransactor=concat(' AND t.bill_transactor_id=',in_bill_transactor_id);
	end if;
	
	SET @sql_text=concat('SELECT t.*,ud.user_name,tr.transactor_names,
		(select sum(pt.trans_paid_amount) from pay_trans pt where pt.transaction_id=t.transaction_id) as total_paid2 FROM transaction t  
		INNER JOIN user_detail ud ON t.add_user_detail_id=ud.user_detail_id 
		INNER JOIN transactor tr ON t.transactor_id=tr.transactor_id WHERE (t.transaction_type_id=1 OR t.transaction_reason_id IN(118)) ',@TransDate,@FromStore,@BillTransactor,' ORDER BY t.transaction_id DESC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_customer_card_totals;
DELIMITER //
CREATE PROCEDURE sp_report_customer_card_totals
(
	IN in_transaction_date_from date,
	IN in_transaction_date_to date,
	IN in_store_id int,
	IN in_bill_transactor_id bigint 
) 
BEGIN 
	SET @TransDate='';
	SET @FromStore='';
	SET @BillTransactor='';

	if ((in_transaction_date_from is not null) and (in_transaction_date_to is not null)) then 
		SET @TransDate=concat(" AND (t.transaction_date BETWEEN '",in_transaction_date_from,"' AND '",in_transaction_date_to,"')");
	end if;
	if (in_store_id!=0) then 
		SET @FromStore=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_bill_transactor_id!=0) then 
		SET @BillTransactor=concat(' AND t.bill_transactor_id=',in_bill_transactor_id);
	end if;
	
	SET @sql_inner=concat('SELECT t.*,ud.user_name,tr.transactor_names,
		(select sum(pt.trans_paid_amount) from pay_trans pt where pt.transaction_id=t.transaction_id) as total_paid2 FROM transaction t  
		INNER JOIN user_detail ud ON t.add_user_detail_id=ud.user_detail_id 
		INNER JOIN transactor tr ON t.transactor_id=tr.transactor_id WHERE (t.transaction_type_id=2 OR t.transaction_reason_id IN(117)) ',@TransDate,@FromStore,@BillTransactor,' ORDER BY t.transaction_id DESC');
	SET @sql_text=concat('select CC.currency_code,sum(CC.grand_total) as grand_total,sum(CC.total_paid2) as total_paid2 from (',
		@sql_inner,') as CC group by CC.currency_code');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_supplier_card_totals;
DELIMITER //
CREATE PROCEDURE sp_report_supplier_card_totals
(
	IN in_transaction_date_from date,
	IN in_transaction_date_to date,
	IN in_store_id int,
	IN in_bill_transactor_id bigint 
) 
BEGIN 
	SET @TransDate='';
	SET @FromStore='';
	SET @BillTransactor='';

	if ((in_transaction_date_from is not null) and (in_transaction_date_to is not null)) then 
		SET @TransDate=concat(" AND (t.transaction_date BETWEEN '",in_transaction_date_from,"' AND '",in_transaction_date_to,"')");
	end if;
	if (in_store_id!=0) then 
		SET @FromStore=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_bill_transactor_id!=0) then 
		SET @BillTransactor=concat(' AND t.bill_transactor_id=',in_bill_transactor_id);
	end if;
	
	SET @sql_inner=concat('SELECT t.*,ud.user_name,tr.transactor_names,
		(select sum(pt.trans_paid_amount) from pay_trans pt where pt.transaction_id=t.transaction_id) as total_paid2 FROM transaction t  
		INNER JOIN user_detail ud ON t.add_user_detail_id=ud.user_detail_id 
		INNER JOIN transactor tr ON t.transactor_id=tr.transactor_id WHERE (t.transaction_type_id=1 OR t.transaction_reason_id IN(118)) ',@TransDate,@FromStore,@BillTransactor,' ORDER BY t.transaction_id DESC');
	SET @sql_text=concat('select CC.currency_code,sum(CC.grand_total) as grand_total,sum(CC.total_paid2) as total_paid2 from (',
		@sql_inner,') as CC group by CC.currency_code');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_report_customer_card_old;
DELIMITER //
CREATE PROCEDURE sp_report_customer_card_old
(
	IN in_transaction_date_from date,
	IN in_transaction_date_to date,
	IN in_store_id int,
	IN in_bill_transactor_id bigint 
) 
BEGIN 
	SET @TransDate='';
	SET @FromStore='';
	SET @BillTransactor='';

	if ((in_transaction_date_from is not null) and (in_transaction_date_to is not null)) then 
		SET @TransDate=concat(" AND (t.transaction_date BETWEEN '",in_transaction_date_from,"' AND '",in_transaction_date_to,"')");
	end if;
	if (in_store_id!=0) then 
		SET @FromStore=concat(' AND t.store_id=',in_store_id);
	end if;
	if (in_bill_transactor_id!=0) then 
		SET @BillTransactor=concat(' AND t.bill_transactor_id=',in_bill_transactor_id);
	end if;
	
	SET @sql_text=concat('SELECT * FROM view_transaction_total_paid t WHERE transaction_type_id=2 ',@TransDate,@FromStore,@BillTransactor,' ORDER BY t.transaction_id DESC');
	PREPARE stmt FROM @sql_text;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_type_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_type_by_id
(
	IN in_transaction_type_id int
) 
BEGIN 
	SELECT * FROM transaction_type 
	WHERE transaction_type_id=in_transaction_type_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_reason_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_reason_by_id
(
	IN in_transaction_reason_id int
) 
BEGIN 
	SELECT * FROM transaction_reason 
	WHERE transaction_reason_id=in_transaction_reason_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_reason_by_transtype_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_reason_by_transtype_id
(
	IN in_transaction_type_id int
) 
BEGIN 
	SELECT * FROM transaction_reason 
	WHERE transaction_type_id=in_transaction_type_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_acc_journal;
DELIMITER //
CREATE PROCEDURE sp_insert_acc_journal
(
	IN in_journal_date date,
	IN in_transaction_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_pay_id bigint,
	IN in_pay_type_id int,
	IN in_pay_reason_id int,
	IN in_store_id int,
	IN in_bill_transactor_id bigint,
	IN in_ledger_folio varchar(3),
	IN in_acc_coa_id int,
	IN in_account_code varchar(20),
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_narration varchar(200),
	IN in_acc_period_id int,
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_xrate double,
	IN in_add_by int,
	IN in_job_id bigint
) 
BEGIN 
	-- SET @new_id=0;
	-- CALL sp_get_new_id("transaction","transaction_id",@new_id);
	
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @bill_transactor_id=NULL;
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;
	SET @acc_child_account_id=NULL;
	if (in_acc_child_account_id!=0) then
		set @acc_child_account_id=in_acc_child_account_id;
	end if;

	SET @transaction_id=NULL;
	if (in_transaction_id!=0) then
		set @transaction_id=in_transaction_id;
	end if;
	SET @transaction_type_id=NULL;
	if (in_transaction_type_id!=0) then
		set @transaction_type_id=in_transaction_type_id;
	end if;
	SET @transaction_reason_id=NULL;
	if (in_transaction_reason_id!=0) then
		set @transaction_reason_id=in_transaction_reason_id;
	end if;

	SET @pay_id=NULL;
	if (in_pay_id!=0) then
		set @pay_id=in_pay_id;
	end if;
	SET @pay_type_id=NULL;
	if (in_pay_type_id!=0) then
		set @pay_type_id=in_pay_type_id;
	end if;
	SET @pay_reason_id=NULL;
	if (in_pay_reason_id!=0) then
		set @pay_reason_id=in_pay_reason_id;
	end if;

	INSERT INTO acc_journal
	(
		journal_date,
		transaction_id,
		transaction_type_id,
		transaction_reason_id,
		pay_id,
		pay_type_id,
		pay_reason_id,
		store_id,
		bill_transactor_id,
		ledger_folio,
		acc_coa_id,
		account_code,
		debit_amount,
		credit_amount,
		narration,
		acc_period_id,
		acc_child_account_id,
		currency_code,
		xrate,
		add_date,
		add_by,
		is_active,
		is_deleted,
		job_id
	) 
    VALUES
	(
		in_journal_date,
		@transaction_id,
		@transaction_type_id,
		@transaction_reason_id,
		@pay_id,
		@pay_type_id,
		@pay_reason_id,
		in_store_id,
		@bill_transactor_id,
		in_ledger_folio,
		in_acc_coa_id,
		in_account_code,
		in_debit_amount,
		in_credit_amount,
		in_narration,
		in_acc_period_id,
		@acc_child_account_id,
		in_currency_code,
		in_xrate,
		@cur_sys_datetime,
		in_add_by,
		1,
		0,
		in_job_id
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_acc_journal_specify;
DELIMITER //
CREATE PROCEDURE sp_insert_acc_journal_specify
(
	IN in_journal_date date,
	IN in_transaction_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_pay_id bigint,
	IN in_pay_type_id int,
	IN in_pay_reason_id int,
	IN in_store_id int,
	IN in_bill_transactor_id bigint,
	IN in_ledger_folio varchar(3),
	IN in_acc_coa_id int,
	IN in_account_code varchar(20),
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_narration varchar(200),
	IN in_acc_period_id int,
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_xrate double,
	IN in_add_by int,
	IN in_job_id bigint,
	IN in_table_name varchar(100)
) 
BEGIN 
	-- SET @new_id=0;
	-- CALL sp_get_new_id("transaction","transaction_id",@new_id);
	
	SET @cur_sys_datetime='NULL';
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @bill_transactor_id='NULL';
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;
	SET @acc_child_account_id='NULL';
	if (in_acc_child_account_id!=0) then
		set @acc_child_account_id=in_acc_child_account_id;
	end if;

	SET @transaction_id='NULL';
	if (in_transaction_id!=0) then
		set @transaction_id=in_transaction_id;
	end if;
	SET @transaction_type_id='NULL';
	if (in_transaction_type_id!=0) then
		set @transaction_type_id=in_transaction_type_id;
	end if;
	SET @transaction_reason_id='NULL';
	if (in_transaction_reason_id!=0) then
		set @transaction_reason_id=in_transaction_reason_id;
	end if;

	SET @pay_id='NULL';
	if (in_pay_id!=0) then
		set @pay_id=in_pay_id;
	end if;
	SET @pay_type_id='NULL';
	if (in_pay_type_id!=0) then
		set @pay_type_id=in_pay_type_id;
	end if;
	SET @pay_reason_id='NULL';
	if (in_pay_reason_id!=0) then
		set @pay_reason_id=in_pay_reason_id;
	end if;

	SET @sql1=CONCAT("INSERT INTO ",in_table_name,"
	(
		journal_date,
		transaction_id,
		transaction_type_id,
		transaction_reason_id,
		pay_id,
		pay_type_id,
		pay_reason_id,
		store_id,
		bill_transactor_id,
		ledger_folio,
		acc_coa_id,
		account_code,
		debit_amount,
		credit_amount,
		narration,
		acc_period_id,
		acc_child_account_id,
		currency_code,
		xrate,
		add_date,
		add_by,
		is_active,
		is_deleted,
		job_id
	) 
    VALUES
	(
		'",in_journal_date,"',",
		@transaction_id,",",
		@transaction_type_id,",",
		@transaction_reason_id,",",
		@pay_id,",",
		@pay_type_id,",",
		@pay_reason_id,",",
		in_store_id,",",
		@bill_transactor_id,",'",
		in_ledger_folio,"',",
		in_acc_coa_id,",'",
		in_account_code,"',",
		in_debit_amount,",",
		in_credit_amount,",'",
		in_narration,"',",
		in_acc_period_id,",",
		@acc_child_account_id,",'",
		in_currency_code,"',",
		in_xrate,",'",
		@cur_sys_datetime,"',",
		in_add_by,",",
		1,",",
		0,",",
		in_job_id,")");
	 PREPARE stmt FROM @sql1;
	 EXECUTE stmt;
	 DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_acc_journal_receivable;
DELIMITER //
CREATE PROCEDURE sp_insert_acc_journal_receivable
(
	IN in_journal_date date,
	IN in_transaction_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_pay_id bigint,
	IN in_pay_type_id int,
	IN in_pay_reason_id int,
	IN in_store_id int,
	IN in_bill_transactor_id bigint,
	IN in_ledger_folio varchar(3),
	IN in_acc_coa_id int,
	IN in_account_code varchar(20),
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_narration varchar(200),
	IN in_acc_period_id int,
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_xrate double,
	IN in_add_by int,
	IN in_job_id bigint
) 
BEGIN 
	SET @cur_sys_datetime=NULL;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @bill_transactor_id=NULL;
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;
	SET @acc_child_account_id=NULL;
	if (in_acc_child_account_id!=0) then
		set @acc_child_account_id=in_acc_child_account_id;
	end if;

	SET @transaction_id=NULL;
	if (in_transaction_id!=0) then
		set @transaction_id=in_transaction_id;
	end if;
	SET @transaction_type_id=NULL;
	if (in_transaction_type_id!=0) then
		set @transaction_type_id=in_transaction_type_id;
	end if;
	SET @transaction_reason_id=NULL;
	if (in_transaction_reason_id!=0) then
		set @transaction_reason_id=in_transaction_reason_id;
	end if;

	SET @pay_id=NULL;
	if (in_pay_id!=0) then
		set @pay_id=in_pay_id;
	end if;
	SET @pay_type_id=NULL;
	if (in_pay_type_id!=0) then
		set @pay_type_id=in_pay_type_id;
	end if;
	SET @pay_reason_id=NULL;
	if (in_pay_reason_id!=0) then
		set @pay_reason_id=in_pay_reason_id;
	end if;

	INSERT INTO acc_journal_receivable
	(
		journal_date,
		transaction_id,
		transaction_type_id,
		transaction_reason_id,
		pay_id,
		pay_type_id,
		pay_reason_id,
		store_id,
		bill_transactor_id,
		ledger_folio,
		acc_coa_id,
		account_code,
		debit_amount,
		credit_amount,
		narration,
		acc_period_id,
		acc_child_account_id,
		currency_code,
		xrate,
		add_date,
		add_by,
		is_active,
		is_deleted,
		job_id
	) 
    VALUES
	(
		in_journal_date,
		@transaction_id,
		@transaction_type_id,
		@transaction_reason_id,
		@pay_id,
		@pay_type_id,
		@pay_reason_id,
		in_store_id,
		@bill_transactor_id,
		in_ledger_folio,
		in_acc_coa_id,
		in_account_code,
		in_debit_amount,
		in_credit_amount,
		in_narration,
		in_acc_period_id,
		@acc_child_account_id,
		in_currency_code,
		in_xrate,
		@cur_sys_datetime,
		in_add_by,
		1,
		0,
		in_job_id
	);
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_acc_journal_payable;
DELIMITER //
CREATE PROCEDURE sp_insert_acc_journal_payable
(
	IN in_journal_date date,
	IN in_transaction_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_pay_id bigint,
	IN in_pay_type_id int,
	IN in_pay_reason_id int,
	IN in_store_id int,
	IN in_bill_transactor_id bigint,
	IN in_ledger_folio varchar(3),
	IN in_acc_coa_id int,
	IN in_account_code varchar(20),
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_narration varchar(200),
	IN in_acc_period_id int,
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_xrate double,
	IN in_add_by int,
	IN in_job_id bigint
) 
BEGIN 
	SET @cur_sys_datetime=NULL;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @bill_transactor_id=NULL;
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;
	SET @acc_child_account_id=NULL;
	if (in_acc_child_account_id!=0) then
		set @acc_child_account_id=in_acc_child_account_id;
	end if;

	SET @transaction_id=NULL;
	if (in_transaction_id!=0) then
		set @transaction_id=in_transaction_id;
	end if;
	SET @transaction_type_id=NULL;
	if (in_transaction_type_id!=0) then
		set @transaction_type_id=in_transaction_type_id;
	end if;
	SET @transaction_reason_id=NULL;
	if (in_transaction_reason_id!=0) then
		set @transaction_reason_id=in_transaction_reason_id;
	end if;

	SET @pay_id=NULL;
	if (in_pay_id!=0) then
		set @pay_id=in_pay_id;
	end if;
	SET @pay_type_id=NULL;
	if (in_pay_type_id!=0) then
		set @pay_type_id=in_pay_type_id;
	end if;
	SET @pay_reason_id=NULL;
	if (in_pay_reason_id!=0) then
		set @pay_reason_id=in_pay_reason_id;
	end if;

	INSERT INTO acc_journal_payable
	(
		journal_date,
		transaction_id,
		transaction_type_id,
		transaction_reason_id,
		pay_id,
		pay_type_id,
		pay_reason_id,
		store_id,
		bill_transactor_id,
		ledger_folio,
		acc_coa_id,
		account_code,
		debit_amount,
		credit_amount,
		narration,
		acc_period_id,
		acc_child_account_id,
		currency_code,
		xrate,
		add_date,
		add_by,
		is_active,
		is_deleted,
		job_id
	) 
    VALUES
	(
		in_journal_date,
		@transaction_id,
		@transaction_type_id,
		@transaction_reason_id,
		@pay_id,
		@pay_type_id,
		@pay_reason_id,
		in_store_id,
		@bill_transactor_id,
		in_ledger_folio,
		in_acc_coa_id,
		in_account_code,
		in_debit_amount,
		in_credit_amount,
		in_narration,
		in_acc_period_id,
		@acc_child_account_id,
		in_currency_code,
		in_xrate,
		@cur_sys_datetime,
		in_add_by,
		1,
		0,
		in_job_id
	);
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_acc_journal_prepaid;
DELIMITER //
CREATE PROCEDURE sp_insert_acc_journal_prepaid
(
	IN in_journal_date date,
	IN in_transaction_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_pay_id bigint,
	IN in_pay_type_id int,
	IN in_pay_reason_id int,
	IN in_store_id int,
	IN in_bill_transactor_id bigint,
	IN in_ledger_folio varchar(3),
	IN in_acc_coa_id int,
	IN in_account_code varchar(20),
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_narration varchar(200),
	IN in_acc_period_id int,
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_xrate double,
	IN in_add_by int,
	IN in_job_id bigint
) 
BEGIN 
	SET @cur_sys_datetime=NULL;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @bill_transactor_id=NULL;
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;
	SET @acc_child_account_id=NULL;
	if (in_acc_child_account_id!=0) then
		set @acc_child_account_id=in_acc_child_account_id;
	end if;

	SET @transaction_id=NULL;
	if (in_transaction_id!=0) then
		set @transaction_id=in_transaction_id;
	end if;
	SET @transaction_type_id=NULL;
	if (in_transaction_type_id!=0) then
		set @transaction_type_id=in_transaction_type_id;
	end if;
	SET @transaction_reason_id=NULL;
	if (in_transaction_reason_id!=0) then
		set @transaction_reason_id=in_transaction_reason_id;
	end if;

	SET @pay_id=NULL;
	if (in_pay_id!=0) then
		set @pay_id=in_pay_id;
	end if;
	SET @pay_type_id=NULL;
	if (in_pay_type_id!=0) then
		set @pay_type_id=in_pay_type_id;
	end if;
	SET @pay_reason_id=NULL;
	if (in_pay_reason_id!=0) then
		set @pay_reason_id=in_pay_reason_id;
	end if;

	INSERT INTO acc_journal_prepaid
	(
		journal_date,
		transaction_id,
		transaction_type_id,
		transaction_reason_id,
		pay_id,
		pay_type_id,
		pay_reason_id,
		store_id,
		bill_transactor_id,
		ledger_folio,
		acc_coa_id,
		account_code,
		debit_amount,
		credit_amount,
		narration,
		acc_period_id,
		acc_child_account_id,
		currency_code,
		xrate,
		add_date,
		add_by,
		is_active,
		is_deleted,
		job_id
	) 
    VALUES
	(
		in_journal_date,
		@transaction_id,
		@transaction_type_id,
		@transaction_reason_id,
		@pay_id,
		@pay_type_id,
		@pay_reason_id,
		in_store_id,
		@bill_transactor_id,
		in_ledger_folio,
		in_acc_coa_id,
		in_account_code,
		in_debit_amount,
		in_credit_amount,
		in_narration,
		in_acc_period_id,
		@acc_child_account_id,
		in_currency_code,
		in_xrate,
		@cur_sys_datetime,
		in_add_by,
		1,
		0,
		in_job_id
	);
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_trans_by_transaction_id;
DELIMITER //
CREATE PROCEDURE sp_search_pay_trans_by_transaction_id
(
	IN in_transaction_id bigint
) 
BEGIN 
	SELECT * FROM pay_trans 
	WHERE transaction_id=in_transaction_id ORDER BY pay_id ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_trans_by_pay_trans_id;
DELIMITER //
CREATE PROCEDURE sp_search_pay_trans_by_pay_trans_id
(
	IN in_pay_trans_id bigint
) 
BEGIN 
	SELECT * FROM pay_trans 
	WHERE pay_trans_id=in_pay_trans_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_trans_by_pay_id;
DELIMITER //
CREATE PROCEDURE sp_search_pay_trans_by_pay_id
(
	IN in_pay_id bigint
) 
BEGIN 
	SELECT * FROM pay_trans 
	WHERE pay_id=in_pay_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sum_sale_pay_trans_by_transno;
DELIMITER //
CREATE PROCEDURE sp_search_sum_sale_pay_trans_by_transno
(
	IN in_transaction_number varchar(50),
	In in_currency_code varchar(10)
) 
BEGIN 
	select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
	t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
	t.grand_total as grand_total,t.transaction_ref as transaction_ref,
	(select sum(pt.trans_paid_amount) from pay_trans pt inner join pay p on 
	pt.pay_id=p.pay_id and p.pay_category='IN' where pt.transaction_id=t.transaction_id) 
	as sum_trans_paid_amount  from transaction t 
	where t.transaction_number=in_transaction_number and t.transaction_type_id=2 and t.currency_code=in_currency_code; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sum_sale_pay_trans_by_transno2;
DELIMITER //
CREATE PROCEDURE sp_search_sum_sale_pay_trans_by_transno2
(
	IN in_transaction_number varchar(50),
	IN in_currency_code varchar(10),
	IN in_date1 date,
	IN in_date2 date
) 
BEGIN 
 if(in_date1 is not null and in_date2 is not null) then 
	select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
	t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
	t.grand_total as grand_total,t.transaction_ref as transaction_ref,
	(select sum(pt.trans_paid_amount) from pay_trans pt inner join pay p on 
	pt.pay_id=p.pay_id and p.pay_category='IN' where pt.transaction_id=t.transaction_id) 
	as sum_trans_paid_amount  from transaction t 
	where t.transaction_number=in_transaction_number and (t.transaction_type_id=2 or t.transaction_reason_id=117) and t.currency_code=in_currency_code and 
	t.transaction_date between in_date1 and in_date2; 
 else 
	select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
	t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
	t.grand_total as grand_total,t.transaction_ref as transaction_ref,
	(select sum(pt.trans_paid_amount) from pay_trans pt inner join pay p on 
	pt.pay_id=p.pay_id and p.pay_category='IN' where pt.transaction_id=t.transaction_id) 
	as sum_trans_paid_amount  from transaction t 
	where t.transaction_number=in_transaction_number and (t.transaction_type_id=2 or t.transaction_reason_id=117) and t.currency_code=in_currency_code;
 end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sum_sale_pay_trans_by_transactor;
DELIMITER //
CREATE PROCEDURE sp_search_sum_sale_pay_trans_by_transactor
(
	IN in_transactor_id bigint,
	In in_currency_code varchar(10)
) 
BEGIN 
	select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
	t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
	t.grand_total as grand_total,t.transaction_ref as transaction_ref,
	(select sum(pt.trans_paid_amount) from pay_trans pt inner join pay p on 
	pt.pay_id=p.pay_id and p.pay_category='IN' where pt.transaction_id=t.transaction_id) 
	as sum_trans_paid_amount  from transaction t 
	where t.bill_transactor_id=in_transactor_id and t.transaction_type_id=2 and t.currency_code=in_currency_code; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sum_sale_pay_trans_by_transactor2;
DELIMITER //
CREATE PROCEDURE sp_search_sum_sale_pay_trans_by_transactor2
(
	IN in_transactor_id bigint,
	IN in_currency_code varchar(10),
	IN in_date1 date,
	IN in_date2 date
) 
BEGIN 
	if (in_date1 is not null and in_date2 is not null ) then 
		select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
		t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
		t.grand_total as grand_total,t.transaction_ref as transaction_ref,
		(select sum(pt.trans_paid_amount) from pay_trans pt inner join pay p on 
		pt.pay_id=p.pay_id and p.pay_category='IN' and p.bill_transactor_id=in_transactor_id where pt.transaction_id=t.transaction_id) 
		as sum_trans_paid_amount  from transaction t 
		where t.bill_transactor_id=in_transactor_id and t.transaction_type_id=2 and t.currency_code=in_currency_code and 
		t.transaction_date between in_date1 and in_date2; 
	else 
		select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
		t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
		t.grand_total as grand_total,t.transaction_ref as transaction_ref,
		(select sum(pt.trans_paid_amount) from pay_trans pt inner join pay p on 
		pt.pay_id=p.pay_id and p.pay_category='IN' and p.bill_transactor_id=in_transactor_id where pt.transaction_id=t.transaction_id) 
		as sum_trans_paid_amount  from transaction t 
		where t.bill_transactor_id=in_transactor_id and t.transaction_type_id=2 and t.currency_code=in_currency_code; 
	end if;
	
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sum_sale_pay_trans_by_transactor3;
DELIMITER //
CREATE PROCEDURE sp_search_sum_sale_pay_trans_by_transactor3
(
	IN in_transactor_id bigint,
	IN in_currency_code varchar(10),
	IN in_date1 date,
	IN in_date2 date
) 
BEGIN 
	if (in_date1 is not null and in_date2 is not null ) then 
		select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
		t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
		t.grand_total as grand_total,t.transaction_ref as transaction_ref,ifnull(TP.sum_trans_paid_amount,0) as sum_trans_paid_amount 
		from transaction as t left join 
		(select pt.transaction_id,sum(pt.trans_paid_amount) as sum_trans_paid_amount from pay_trans pt 
		inner join pay p on p.pay_id=pt.pay_id and p.pay_category='IN' and p.bill_transactor_id=in_transactor_id 
		group by pt.transaction_id) as TP on t.transaction_id=TP.transaction_id 
		where t.bill_transactor_id=in_transactor_id and (t.transaction_type_id IN(2,65,68) or t.transaction_reason_id=117) 
		and t.currency_code=in_currency_code 
		and  t.grand_total>ifnull(TP.sum_trans_paid_amount,0) and t.transaction_date between in_date1 and in_date2; 
	else 
		select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
		t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
		t.grand_total as grand_total,t.transaction_ref as transaction_ref,ifnull(TP.sum_trans_paid_amount,0) as sum_trans_paid_amount 
		from transaction as t left join  		
		(select pt.transaction_id,sum(pt.trans_paid_amount) as sum_trans_paid_amount from pay_trans pt 
		inner join pay p on p.pay_id=pt.pay_id and p.pay_category='IN' and p.bill_transactor_id=in_transactor_id 
		group by pt.transaction_id) as TP on t.transaction_id=TP.transaction_id 
		where t.bill_transactor_id=in_transactor_id and (t.transaction_type_id IN(2,65,68) or t.transaction_reason_id=117) 
		and t.currency_code=in_currency_code 
		and t.grand_total>ifnull(TP.sum_trans_paid_amount,0); 
	end if;
	
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_trans_for_cash_receipt_credit_sale;
DELIMITER //
CREATE PROCEDURE sp_search_trans_for_cash_receipt_credit_sale
(
	IN in_transactor_id bigint,
    IN in_store_id int,
	IN in_currency_code varchar(10),
    IN in_transaction_number varchar(50),
	IN in_date1 date,
	IN in_date2 date
) 
BEGIN 
	SET @CUST="";
	if (in_transactor_id>0) then 
		set @CUST=CONCAT(" AND t.bill_transactor_id=",in_transactor_id," ");
	end if;
    SET @STORE="";
	if (in_store_id>0) then 
		set @STORE=CONCAT(' AND t.store_id=',in_store_id," ");
	end if;
    SET @CUR="";
	if (length(in_currency_code)>0) then 
		set @CUR=CONCAT(" AND t.currency_code='",in_currency_code,"' ");
	end if;
    SET @TRANSNO="";
	if (length(in_transaction_number)>0) then 
		set @TRANSNO=CONCAT(" AND t.transaction_number='",in_transaction_number,"' ");
	end if;
    SET @DAT="";
	if (in_date1 is not null and in_date2 is not null) then 
		set @DAT=CONCAT(" AND t.transaction_date BETWEEN '",in_date1,"' AND '",in_date2,"' ");
	end if;
    
   SET @sql1=CONCAT("
		select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
		t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
		t.grand_total as grand_total,t.transaction_ref as transaction_ref,ifnull(TP.sum_trans_paid_amount,0) as sum_trans_paid_amount,
        IFNULL(tc.grand_total,0) as cr_dr_amount,IFNULL(tc.transaction_comment,'') as cr_dr_type 
		from transaction as t 
        left join transaction_cr_dr_note tc ON t.transaction_number=tc.transaction_ref and tc.mode_code=1 and tc.is_cancel=0 
        left join 
			(select pt.transaction_id,sum(pt.trans_paid_amount) as sum_trans_paid_amount from pay_trans pt 
			inner join pay p on p.pay_id=pt.pay_id and p.pay_category='IN' 
			group by pt.transaction_id
			) as TP on t.transaction_id=TP.transaction_id 
		where (t.transaction_type_id IN(2,65,68) or t.transaction_reason_id=117) and (t.grand_total+IFNULL(tc.grand_total,0))>ifnull(TP.sum_trans_paid_amount,0) ",
		@CUST,@STORE,@CUR,@TRANSNO,@DAT); 
	PREPARE stmt FROM @sql1;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sum_sale_pay_trans_by_transactor3_old;
DELIMITER //
CREATE PROCEDURE sp_search_sum_sale_pay_trans_by_transactor3_old
(
	IN in_transactor_id bigint,
	IN in_currency_code varchar(10),
	IN in_date1 date,
	IN in_date2 date
) 
BEGIN 
	if (in_date1 is not null and in_date2 is not null ) then 
		select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
		t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
		t.grand_total as grand_total,t.transaction_ref as transaction_ref,TP.sum_trans_paid_amount 
		from transaction t, 		
		(select pt.transaction_id,sum(pt.trans_paid_amount) as sum_trans_paid_amount from pay_trans pt 
		inner join pay p on p.pay_id=pt.pay_id and p.pay_category='IN' and p.bill_transactor_id=in_transactor_id 
		group by pt.transaction_id) as TP 
		where t.transaction_id=TP.transaction_id and t.bill_transactor_id=in_transactor_id and t.transaction_type_id=2 and t.currency_code=in_currency_code 
		and  t.grand_total>TP.sum_trans_paid_amount and t.transaction_date between in_date1 and in_date2; 
	else 
		select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
		t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
		t.grand_total as grand_total,t.transaction_ref as transaction_ref,TP.sum_trans_paid_amount 
		from transaction t, 		
		(select pt.transaction_id,sum(pt.trans_paid_amount) as sum_trans_paid_amount from pay_trans pt 
		inner join pay p on p.pay_id=pt.pay_id and p.pay_category='IN' and p.bill_transactor_id=in_transactor_id 
		group by pt.transaction_id) as TP 
		where t.transaction_id=TP.transaction_id and t.bill_transactor_id=in_transactor_id and t.transaction_type_id=2 and t.currency_code=in_currency_code 
		and t.grand_total>TP.sum_trans_paid_amount; 
	end if;
	
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sum_purchase_pay_trans_by_transactor;
DELIMITER //
CREATE PROCEDURE sp_search_sum_purchase_pay_trans_by_transactor
(
	IN in_transactor_id bigint,
	In in_currency_code varchar(10)
) 
BEGIN 
	select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
	t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
	t.grand_total as grand_total,t.transaction_ref as transaction_ref,
	(select sum(pt.trans_paid_amount) from pay_trans pt inner join pay p on 
	pt.pay_id=p.pay_id and p.pay_category='OUT' where pt.transaction_id=t.transaction_id) 
	as sum_trans_paid_amount  from transaction t 
	where t.bill_transactor_id=in_transactor_id and t.transaction_type_id=1 and t.currency_code=in_currency_code; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sum_purchase_pay_trans_by_transactor2;
DELIMITER //
CREATE PROCEDURE sp_search_sum_purchase_pay_trans_by_transactor2
(
	IN in_transactor_id bigint,
	In in_currency_code varchar(10),
	IN in_date1 date,
	IN in_date2 date
) 
BEGIN 
 if (in_date1 is not null and in_date2 is not null ) then 
	select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
	t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
	t.grand_total as grand_total,t.transaction_ref as transaction_ref,
	(select sum(pt.trans_paid_amount) from pay_trans pt where pt.transaction_id=t.transaction_id) 
	as sum_trans_paid_amount  from transaction t 
	where t.bill_transactor_id=in_transactor_id and (t.transaction_type_id=1 or t.transaction_reason_id=118) and t.currency_code=in_currency_code and 
	t.transaction_date between in_date1 and in_date2; 
 else 
	select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
	t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
	t.grand_total as grand_total,t.transaction_ref as transaction_ref,
	(select sum(pt.trans_paid_amount) from pay_trans pt where pt.transaction_id=t.transaction_id) 
	as sum_trans_paid_amount  from transaction t 
	where t.bill_transactor_id=in_transactor_id and (t.transaction_type_id=1 or t.transaction_reason_id=118) and t.currency_code=in_currency_code; 
 end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sum_purchase_pay_trans_by_transactor2_old;
DELIMITER //
CREATE PROCEDURE sp_search_sum_purchase_pay_trans_by_transactor2_old
(
	IN in_transactor_id bigint,
	In in_currency_code varchar(10),
	IN in_date1 date,
	IN in_date2 date
) 
BEGIN 
 if (in_date1 is not null and in_date2 is not null ) then 
	select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
	t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
	t.grand_total as grand_total,t.transaction_ref as transaction_ref,
	(select sum(pt.trans_paid_amount) from pay_trans pt inner join pay p on 
	pt.pay_id=p.pay_id and p.pay_category='OUT' where pt.transaction_id=t.transaction_id) 
	as sum_trans_paid_amount  from transaction t 
	where t.bill_transactor_id=in_transactor_id and t.transaction_type_id=1 and t.currency_code=in_currency_code and 
	t.transaction_date between in_date1 and in_date2; 
 else 
	select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
	t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
	t.grand_total as grand_total,t.transaction_ref as transaction_ref,
	(select sum(pt.trans_paid_amount) from pay_trans pt inner join pay p on 
	pt.pay_id=p.pay_id and p.pay_category='OUT' where pt.transaction_id=t.transaction_id) 
	as sum_trans_paid_amount  from transaction t 
	where t.bill_transactor_id=in_transactor_id and t.transaction_type_id=1 and t.currency_code=in_currency_code; 
 end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sum_purchase_pay_trans_by_transno;
DELIMITER //
CREATE PROCEDURE sp_search_sum_purchase_pay_trans_by_transno
(
	IN in_transaction_number varchar(50),
	In in_currency_code varchar(10)
) 
BEGIN 
	select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
	t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
	t.grand_total as grand_total,t.transaction_ref as transaction_ref,
	(select sum(pt.trans_paid_amount) from pay_trans pt inner join pay p on 
	pt.pay_id=p.pay_id and p.pay_category='OUT' where pt.transaction_id=t.transaction_id) 
	as sum_trans_paid_amount  from transaction t 
	where t.transaction_number=in_transaction_number and t.transaction_type_id=1 and t.currency_code=in_currency_code; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sum_purchase_pay_trans_by_transno2;
DELIMITER //
CREATE PROCEDURE sp_search_sum_purchase_pay_trans_by_transno2
(
	IN in_transaction_number varchar(50),
	IN in_currency_code varchar(10),
	IN in_date1 date,
	IN in_date2 date
) 
BEGIN 
  if(in_date1 is not null and in_date2 is not null) then 
	select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
	t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
	t.grand_total as grand_total,t.transaction_ref as transaction_ref,
	(select sum(pt.trans_paid_amount) from pay_trans pt where pt.transaction_id=t.transaction_id) 
	as sum_trans_paid_amount  from transaction t 
	where t.transaction_number=in_transaction_number and (t.transaction_type_id=1 or t.transaction_reason_id=118) and t.currency_code=in_currency_code and 
	t.transaction_date between in_date1 and in_date2; 
  else 
	select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
	t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
	t.grand_total as grand_total,t.transaction_ref as transaction_ref,
	(select sum(pt.trans_paid_amount) from pay_trans pt where pt.transaction_id=t.transaction_id) 
	as sum_trans_paid_amount  from transaction t 
	where t.transaction_number=in_transaction_number and (t.transaction_type_id=1 or t.transaction_reason_id=118) and t.currency_code=in_currency_code;
  end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_sum_purchase_pay_trans_by_transno2_old;
DELIMITER //
CREATE PROCEDURE sp_search_sum_purchase_pay_trans_by_transno2_old
(
	IN in_transaction_number varchar(50),
	IN in_currency_code varchar(10),
	IN in_date1 date,
	IN in_date2 date
) 
BEGIN 
  if(in_date1 is not null and in_date2 is not null) then 
	select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
	t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
	t.grand_total as grand_total,t.transaction_ref as transaction_ref,
	(select sum(pt.trans_paid_amount) from pay_trans pt inner join pay p on 
	pt.pay_id=p.pay_id and p.pay_category='OUT' where pt.transaction_id=t.transaction_id) 
	as sum_trans_paid_amount  from transaction t 
	where t.transaction_number=in_transaction_number and t.transaction_type_id=1 and t.currency_code=in_currency_code and 
	t.transaction_date between in_date1 and in_date2; 
  else 
	select t.transaction_id as transaction_id,t.transaction_number as transaction_number,
	t.transaction_type_id as transaction_type_id,t.transaction_reason_id as transaction_reason_id,
	t.grand_total as grand_total,t.transaction_ref as transaction_ref,
	(select sum(pt.trans_paid_amount) from pay_trans pt inner join pay p on 
	pt.pay_id=p.pay_id and p.pay_category='OUT' where pt.transaction_id=t.transaction_id) 
	as sum_trans_paid_amount  from transaction t 
	where t.transaction_number=in_transaction_number and t.transaction_type_id=1 and t.currency_code=in_currency_code;
  end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_pay_trans;
DELIMITER //
CREATE PROCEDURE sp_insert_pay_trans
(
	IN in_pay_id bigint,
	IN in_transaction_id bigint,
	IN in_transaction_number varchar(50),
	IN in_trans_paid_amount double,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_account_code  varchar(20)
) 
BEGIN 
	-- SET @new_id=0;
	-- CALL sp_get_new_id("pay_trans","pay_trans_id",@new_id);

	INSERT INTO pay_trans
	(
		pay_id,
		transaction_id,
		transaction_number,
		trans_paid_amount,
		transaction_type_id,
		transaction_reason_id,
		account_code
	) 
    VALUES
	(
		in_pay_id,
		in_transaction_id,
		in_transaction_number,
		in_trans_paid_amount,
		in_transaction_type_id,
		in_transaction_reason_id,
		in_account_code
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_pay_trans;
DELIMITER //
CREATE PROCEDURE sp_update_pay_trans
(
	IN in_pay_trans_id bigint,
	IN in_pay_id bigint,
	IN in_transaction_id bigint,
	IN in_transaction_number varchar(50),
	IN in_trans_paid_amount double,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_account_code varchar(20)
) 
BEGIN 
	UPDATE pay_trans SET 
		pay_id=in_pay_id,
		transaction_id=in_transaction_id,
		transaction_number=in_transaction_number,
		trans_paid_amount=in_trans_paid_amount,
		transaction_type_id=in_transaction_type_id,
		transaction_reason_id=in_transaction_reason_id,
		account_code =in_account_code  
	WHERE pay_trans_id=in_pay_trans_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_coa_for_expense;
DELIMITER //
CREATE PROCEDURE sp_search_coa_for_expense
(
	IN in_desc varchar(100)
) 
BEGIN  
		SELECT * FROM acc_coa 
		WHERE is_active=1 AND is_deleted=0 AND 
		(account_code LIKE '5-20%' OR account_code LIKE '5-30%') AND account_name LIKE concat('%',in_desc,'%') 
		ORDER BY account_name ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_coa_all_active;
DELIMITER //
CREATE PROCEDURE sp_search_coa_all_active
(
	IN in_desc varchar(100)
) 
BEGIN  
		SELECT * FROM acc_coa 
		WHERE is_active=1 AND is_deleted=0 AND 
		account_code LIKE concat('%',in_desc,'%') OR account_name LIKE concat('%',in_desc,'%') 
		ORDER BY account_name ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_coa_all;
DELIMITER //
CREATE PROCEDURE sp_search_coa_all
(
	IN in_desc varchar(100)
) 
BEGIN  
		SELECT * FROM acc_coa 
		WHERE is_deleted=0 AND 
		account_code LIKE concat('%',in_desc,'%') OR account_name LIKE concat('%',in_desc,'%') 
		ORDER BY account_name ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_coa_begin_with1;
DELIMITER //
CREATE PROCEDURE sp_search_coa_begin_with1
(
	IN in_begin_with1 varchar(20)
) 
BEGIN  
		SELECT * FROM acc_coa 
		WHERE is_active=1 AND is_deleted=0 AND 
		account_code LIKE concat(in_begin_with1,'%') ORDER BY account_code ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_coa_begin_with2;
DELIMITER //
CREATE PROCEDURE sp_search_coa_begin_with2
(
	IN in_begin_with1 varchar(20),
	IN in_begin_with2 varchar(20)
) 
BEGIN  
		SELECT * FROM acc_coa 
		WHERE is_active=1 AND is_deleted=0 AND 
		(account_code LIKE concat(in_begin_with1,'%') OR account_code LIKE concat(in_begin_with2,'%')) 
		ORDER BY acc_category_id ASC,account_name ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_acc_dep_schedule;
DELIMITER //
CREATE PROCEDURE sp_insert_acc_dep_schedule
(
		IN in_stock_id bigint,
		IN in_dep_for_acc_period_id int,
		IN in_dep_from_date date,
		IN in_dep_to_date date,
		IN in_year_number int,
		IN in_dep_amount double
) 
BEGIN 
		SET @in_dep_from_date=NULL;
		if (in_dep_from_date is not null) then
			set @in_dep_from_date=in_dep_from_date;
		end if;
		SET @in_dep_to_date=NULL;
		if (in_dep_to_date is not null) then
			set @in_dep_to_date=in_dep_to_date;
		end if;
		
		INSERT INTO acc_dep_schedule
		(
			dep_for_acc_period_id,
			stock_id,
			dep_from_date,
			dep_to_date,
			year_number,
			dep_amount
		) 
		VALUES
		(
		in_dep_for_acc_period_id,
		in_stock_id,
		@in_dep_from_date,
		@in_dep_to_date,
		in_year_number,
		in_dep_amount
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_acc_dep_schedule;
DELIMITER //
CREATE PROCEDURE sp_update_acc_dep_schedule
(
		IN in_acc_dep_schedule_id bigint,
		IN in_stock_id bigint,
		IN in_dep_for_acc_period_id int,
		IN in_dep_from_date date,
		IN in_dep_to_date date,
		IN in_year_number int,
		IN in_dep_amount double,
		IN in_post_status int
) 
BEGIN 
		SET @in_dep_from_date=NULL;
		if (in_dep_from_date is not null) then
			set @in_dep_from_date=in_dep_from_date;
		end if;
		SET @in_dep_to_date=NULL;
		if (in_dep_to_date is not null) then
			set @in_dep_to_date=in_dep_to_date;
		end if;
		
		UPDATE acc_dep_schedule SET 
			dep_for_acc_period_id=in_dep_for_acc_period_id,
			stock_id=in_stock_id,
			dep_from_date=@in_dep_from_date,
			dep_to_date=@in_dep_to_date,
			year_number=in_year_number,
			dep_amount=in_dep_amount,
			post_status=in_post_status 
		WHERE acc_dep_schedule_id=in_acc_dep_schedule_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_delete_acc_dep_schedule;
DELIMITER //
CREATE PROCEDURE sp_delete_acc_dep_schedule
(
		IN in_stock_id bigint
) 
BEGIN 
		DELETE FROM acc_dep_schedule WHERE acc_dep_schedule_id>0 AND stock_id=in_stock_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_delete_acc_dep_schedule_unposted;
DELIMITER //
CREATE PROCEDURE sp_delete_acc_dep_schedule_unposted
(
		IN in_stock_id bigint
) 
BEGIN 
		DELETE FROM acc_dep_schedule WHERE acc_dep_schedule_id>0 AND stock_id=in_stock_id AND ifnull(post_status,0)=0; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_acc_dep_schedule_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_acc_dep_schedule_by_id
(
	IN in_acc_dep_schedule_id int 
) 
BEGIN 
		SELECT * FROM acc_dep_schedule WHERE acc_dep_schedule_id=in_acc_dep_schedule_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_acc_dep_schedule_by_year;
DELIMITER //
CREATE PROCEDURE sp_search_acc_dep_schedule_by_year
(
	IN in_stock_id bigint,
	IN in_year_number int
) 
BEGIN 
		SELECT * FROM acc_dep_schedule WHERE stock_id=in_stock_id AND year_number=in_year_number;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_acc_dep_schedule_by_year_posted;
DELIMITER //
CREATE PROCEDURE sp_search_acc_dep_schedule_by_year_posted
(
	IN in_stock_id bigint,
	IN in_year_number int
) 
BEGIN 
		SELECT * FROM acc_dep_schedule WHERE stock_id=in_stock_id AND year_number=in_year_number AND ifnull(post_status,0)=1;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_acc_dep_schedule_by_stock;
DELIMITER //
CREATE PROCEDURE sp_search_acc_dep_schedule_by_stock
(
	IN in_stock_id bigint
) 
BEGIN 
		SELECT * FROM acc_dep_schedule WHERE stock_id=in_stock_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_salary_deduction_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_salary_deduction_by_id
(
	IN in_salary_deduction_id bigint
) 
BEGIN 
		SELECT * FROM salary_deduction WHERE salary_deduction_id=in_salary_deduction_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_salary_deduction_by_trctor;
DELIMITER //
CREATE PROCEDURE sp_search_salary_deduction_by_trctor
(
	IN in_transactor_id bigint
) 
BEGIN 
		SELECT * FROM salary_deduction WHERE transactor_id=in_transactor_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_salary_deduction;
DELIMITER //
CREATE PROCEDURE sp_insert_salary_deduction
(
		IN in_transactor_id bigint,
		IN in_account_code varchar(20),
		IN in_perc double,
		IN in_amount double,
		IN in_deduction_name varchar(50)
) 
BEGIN 		
		INSERT INTO salary_deduction
		(
			transactor_id,
			account_code,
			perc,
			amount,
			deduction_name
		) 
		VALUES
		(
		in_transactor_id,
		in_account_code,
		in_perc,
		in_amount,
		in_deduction_name
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_salary_deduction;
DELIMITER //
CREATE PROCEDURE sp_update_salary_deduction
(
		IN in_salary_deduction_id int,
		IN in_transactor_id bigint,
		IN in_account_code varchar(20),
		IN in_perc double,
		IN in_amount double,
		IN in_deduction_name varchar(50)
) 
BEGIN 		
		UPDATE salary_deduction SET 
		transactor_id=in_transactor_id,
		account_code=in_account_code,
		perc=in_perc,
		amount=in_amount,
		deduction_name=in_deduction_name 
		WHERE salary_deduction_id=in_salary_deduction_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_acc_ledger;
DELIMITER //
CREATE PROCEDURE sp_insert_acc_ledger
(
	IN in_acc_period_id int,
	IN in_bill_transactor_id bigint,
	IN in_account_code varchar(20),
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_debit_amount_lc double,
	IN in_credit_amount_lc double
) 
BEGIN 
	SET @bill_transactor_id=NULL;
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;
	SET @acc_child_account_id=NULL;
	if (in_acc_child_account_id!=0) then
		set @acc_child_account_id=in_acc_child_account_id;
	end if;
	INSERT INTO acc_ledger
	(
		acc_period_id,
		bill_transactor_id,
		account_code,
		acc_child_account_id,
		currency_code,
		debit_amount,
		credit_amount,
		debit_amount_lc,
		credit_amount_lc
	) 
    VALUES
	(
		in_acc_period_id,
		@bill_transactor_id,
		in_account_code,
		@acc_child_account_id,
		in_currency_code,
		in_debit_amount,
		in_credit_amount,
		in_debit_amount_lc,
		in_credit_amount_lc
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_acc_ledger_specify;
DELIMITER //
CREATE PROCEDURE sp_insert_acc_ledger_specify
(
	IN in_acc_period_id int,
	IN in_bill_transactor_id bigint,
	IN in_account_code varchar(20),
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_debit_amount_lc double,
	IN in_credit_amount_lc double,
	IN in_table_name varchar(100)
) 
BEGIN 
	SET @bill_transactor_id='NULL';
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;
	SET @acc_child_account_id='NULL';
	if (in_acc_child_account_id!=0) then
		set @acc_child_account_id=in_acc_child_account_id;
	end if;

	SET @sql1=CONCAT("
	INSERT INTO ",in_table_name,"
	(
		acc_period_id,
		bill_transactor_id,
		account_code,
		acc_child_account_id,
		currency_code,
		debit_amount,
		credit_amount,
		debit_amount_lc,
		credit_amount_lc
	) 
    VALUES
	(
		",in_acc_period_id,",",
		@bill_transactor_id,",'",
		in_account_code,"',",
		@acc_child_account_id,",'",
		in_currency_code,"',",
		in_debit_amount,",",
		in_credit_amount,",",
		in_debit_amount_lc,",",
		in_credit_amount_lc,")"); 

	PREPARE stmt FROM @sql1;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_acc_ledger_receivable;
DELIMITER //
CREATE PROCEDURE sp_insert_acc_ledger_receivable
(
	IN in_acc_period_id int,
	IN in_bill_transactor_id bigint,
	IN in_account_code varchar(20),
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_debit_amount_lc double,
	IN in_credit_amount_lc double
) 
BEGIN 
	SET @bill_transactor_id=NULL;
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;
	SET @acc_child_account_id=NULL;
	if (in_acc_child_account_id!=0) then
		set @acc_child_account_id=in_acc_child_account_id;
	end if;

	INSERT INTO acc_ledger_receivable
	(
		acc_period_id,
		bill_transactor_id,
		account_code,
		acc_child_account_id,
		currency_code,
		debit_amount,
		credit_amount,
		debit_amount_lc,
		credit_amount_lc
	) 
    VALUES
	(
		in_acc_period_id,
		@bill_transactor_id,
		in_account_code,
		@acc_child_account_id,
		in_currency_code,
		in_debit_amount,
		in_credit_amount,
		in_debit_amount_lc,
		in_credit_amount_lc
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_acc_ledger_payable;
DELIMITER //
CREATE PROCEDURE sp_insert_acc_ledger_payable
(
	IN in_acc_period_id int,
	IN in_bill_transactor_id bigint,
	IN in_account_code varchar(20),
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_debit_amount_lc double,
	IN in_credit_amount_lc double
) 
BEGIN 
	SET @bill_transactor_id=NULL;
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;
	SET @acc_child_account_id=NULL;
	if (in_acc_child_account_id!=0) then
		set @acc_child_account_id=in_acc_child_account_id;
	end if;

	INSERT INTO acc_ledger_payable
	(
		acc_period_id,
		bill_transactor_id,
		account_code,
		acc_child_account_id,
		currency_code,
		debit_amount,
		credit_amount,
		debit_amount_lc,
		credit_amount_lc
	) 
    VALUES
	(
		in_acc_period_id,
		@bill_transactor_id,
		in_account_code,
		@acc_child_account_id,
		in_currency_code,
		in_debit_amount,
		in_credit_amount,
		in_debit_amount_lc,
		in_credit_amount_lc
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_acc_ledger_prepaid;
DELIMITER //
CREATE PROCEDURE sp_insert_acc_ledger_prepaid
(
	IN in_acc_period_id int,
	IN in_bill_transactor_id bigint,
	IN in_account_code varchar(20),
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_debit_amount_lc double,
	IN in_credit_amount_lc double
) 
BEGIN 
	SET @bill_transactor_id=NULL;
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;
	SET @acc_child_account_id=NULL;
	if (in_acc_child_account_id!=0) then
		set @acc_child_account_id=in_acc_child_account_id;
	end if;

	INSERT INTO acc_ledger_prepaid
	(
		acc_period_id,
		bill_transactor_id,
		account_code,
		acc_child_account_id,
		currency_code,
		debit_amount,
		credit_amount,
		debit_amount_lc,
		credit_amount_lc
	) 
    VALUES
	(
		in_acc_period_id,
		@bill_transactor_id,
		in_account_code,
		@acc_child_account_id,
		in_currency_code,
		in_debit_amount,
		in_credit_amount,
		in_debit_amount_lc,
		in_credit_amount_lc
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_acc_ledger;
DELIMITER //
CREATE PROCEDURE sp_update_acc_ledger
(
	IN in_acc_ledger_id bigint,
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_debit_amount_lc double,
	IN in_credit_amount_lc double
) 
BEGIN 
	UPDATE acc_ledger SET 
		debit_amount=debit_amount+in_debit_amount,
		credit_amount=credit_amount+in_credit_amount,
		debit_amount_lc=debit_amount_lc+in_debit_amount_lc,
		credit_amount_lc=credit_amount_lc+in_credit_amount_lc 
	WHERE 
		acc_ledger_id=in_acc_ledger_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_acc_ledger_specify;
DELIMITER //
CREATE PROCEDURE sp_update_acc_ledger_specify
(
	IN in_acc_ledger_id bigint,
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_debit_amount_lc double,
	IN in_credit_amount_lc double,
	IN in_table_name varchar(100)
) 
BEGIN 
	SET @sql1=CONCAT("UPDATE ",in_table_name," SET 
		debit_amount=debit_amount+",in_debit_amount,",
		credit_amount=credit_amount+",in_credit_amount,",
		debit_amount_lc=debit_amount_lc+",in_debit_amount_lc,",
		credit_amount_lc=credit_amount_lc+",in_credit_amount_lc," 
	WHERE 
		acc_ledger_id=",in_acc_ledger_id); 
	PREPARE stmt FROM @sql1;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_acc_ledger_receivable;
DELIMITER //
CREATE PROCEDURE sp_update_acc_ledger_receivable
(
	IN in_acc_ledger_id bigint,
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_debit_amount_lc double,
	IN in_credit_amount_lc double
) 
BEGIN 
	UPDATE acc_ledger_receivable SET 
		debit_amount=debit_amount+in_debit_amount,
		credit_amount=credit_amount+in_credit_amount,
		debit_amount_lc=debit_amount_lc+in_debit_amount_lc,
		credit_amount_lc=credit_amount_lc+in_credit_amount_lc 
	WHERE 
		acc_ledger_id=in_acc_ledger_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_acc_ledger_payable;
DELIMITER //
CREATE PROCEDURE sp_update_acc_ledger_payable
(
	IN in_acc_ledger_id bigint,
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_debit_amount_lc double,
	IN in_credit_amount_lc double
) 
BEGIN 
	UPDATE acc_ledger_payable SET 
		debit_amount=debit_amount+in_debit_amount,
		credit_amount=credit_amount+in_credit_amount,
		debit_amount_lc=debit_amount_lc+in_debit_amount_lc,
		credit_amount_lc=credit_amount_lc+in_credit_amount_lc 
	WHERE 
		acc_ledger_id=in_acc_ledger_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_acc_ledger_prepaid;
DELIMITER //
CREATE PROCEDURE sp_update_acc_ledger_prepaid
(
	IN in_acc_ledger_id bigint,
	IN in_debit_amount double,
	IN in_credit_amount double,
	IN in_debit_amount_lc double,
	IN in_credit_amount_lc double
) 
BEGIN 
	UPDATE acc_ledger_prepaid SET 
		debit_amount=debit_amount+in_debit_amount,
		credit_amount=credit_amount+in_credit_amount,
		debit_amount_lc=debit_amount_lc+in_debit_amount_lc,
		credit_amount_lc=credit_amount_lc+in_credit_amount_lc 
	WHERE 
		acc_ledger_id=in_acc_ledger_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_acc_ledger;
DELIMITER //
CREATE PROCEDURE sp_search_acc_ledger
(
	IN in_acc_period_id int,
	IN in_bill_transactor_id bigint,
	IN in_account_code varchar(20),
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10)
) 
BEGIN 
	SELECT * FROM acc_ledger WHERE 
		acc_period_id=in_acc_period_id AND 
		IFNULL(bill_transactor_id,0)=in_bill_transactor_id AND 
		account_code=in_account_code AND 
		IFNULL(acc_child_account_id,0)=in_acc_child_account_id AND 
		currency_code=in_currency_code; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_acc_ledger_specify;
DELIMITER //
CREATE PROCEDURE sp_search_acc_ledger_specify
(
	IN in_acc_period_id int,
	IN in_bill_transactor_id bigint,
	IN in_account_code varchar(20),
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_table_name varchar(100)
) 
BEGIN 
	SET @sql1=CONCAT("SELECT * FROM ",in_table_name," WHERE 
		acc_period_id=",in_acc_period_id," AND 
		IFNULL(bill_transactor_id,0)=",in_bill_transactor_id," AND 
		account_code='",in_account_code,"' AND 
		IFNULL(acc_child_account_id,0)=",in_acc_child_account_id," AND 
		currency_code='",in_currency_code,"'");
	PREPARE stmt FROM @sql1;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_acc_ledger_receivable;
DELIMITER //
CREATE PROCEDURE sp_search_acc_ledger_receivable
(
	IN in_acc_period_id int,
	IN in_bill_transactor_id bigint,
	IN in_account_code varchar(20),
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10)
) 
BEGIN 
	SELECT * FROM acc_ledger_receivable WHERE 
		acc_period_id=in_acc_period_id AND 
		IFNULL(bill_transactor_id,0)=in_bill_transactor_id AND 
		account_code=in_account_code AND 
		IFNULL(acc_child_account_id,0)=in_acc_child_account_id AND 
		currency_code=in_currency_code;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_acc_ledger_payable;
DELIMITER //
CREATE PROCEDURE sp_search_acc_ledger_payable
(
	IN in_acc_period_id int,
	IN in_bill_transactor_id bigint,
	IN in_account_code varchar(20),
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10)
) 
BEGIN 
	SELECT * FROM acc_ledger_payable WHERE 
		acc_period_id=in_acc_period_id AND 
		IFNULL(bill_transactor_id,0)=in_bill_transactor_id AND 
		account_code=in_account_code AND 
		IFNULL(acc_child_account_id,0)=in_acc_child_account_id AND 
		currency_code=in_currency_code;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_acc_ledger_prepaid;
DELIMITER //
CREATE PROCEDURE sp_search_acc_ledger_prepaid
(
	IN in_acc_period_id int,
	IN in_bill_transactor_id bigint,
	IN in_account_code varchar(20),
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10)
) 
BEGIN 
	SELECT * FROM acc_ledger_prepaid WHERE 
		acc_period_id=in_acc_period_id AND 
		IFNULL(bill_transactor_id,0)=in_bill_transactor_id AND 
		account_code=in_account_code AND 
		IFNULL(acc_child_account_id,0)=in_acc_child_account_id AND 
		currency_code=in_currency_code;
END//
DELIMITER ;


DROP PROCEDURE IF EXISTS sp_close_account_period;
DELIMITER //
CREATE PROCEDURE sp_close_account_period
(
		IN in_acc_period_id int
) 
BEGIN 		
		DELETE FROM acc_ledger_close WHERE acc_ledger_close_id>0 AND acc_period_id=in_acc_period_id;

		INSERT INTO acc_ledger_close(acc_period_id,account_code,currency_code,debit_bal,debit_bal_lc,credit_bal,credit_bal_lc) 
		SELECT bal.acc_period_id,bal.account_code,bal.currency_code,0,0,bal.debit_bal,bal.debit_bal_lc 
		FROM view_ledger_temp_acc_balances bal WHERE bal.acc_period_id=in_acc_period_id AND bal.debit_bal>0;

		INSERT INTO acc_ledger_close(acc_period_id,account_code,currency_code,debit_bal,debit_bal_lc,credit_bal,credit_bal_lc) 
		SELECT bal.acc_period_id,bal.account_code,bal.currency_code,bal.credit_bal,bal.credit_bal_lc,0,0 
		FROM view_ledger_temp_acc_balances bal WHERE bal.acc_period_id=in_acc_period_id AND bal.credit_bal>0;
		/*
		INSERT INTO acc_ledger_close(acc_period_id,account_code,currency_code,debit_bal,debit_bal_lc,credit_bal,credit_bal_lc) 
		SELECT cl.acc_period_id,'3-10-000-060',cl.currency_code,0,0,cl.debit_bal,cl.debit_bal_lc 
		FROM acc_ledger_close cl WHERE cl.acc_period_id=in_acc_period_id AND cl.debit_bal>0;
		*/
		INSERT INTO acc_ledger_close(acc_period_id,account_code,currency_code,debit_bal,debit_bal_lc,credit_bal,credit_bal_lc) 
		SELECT bal.acc_period_id,'3-10-000-060',bal.currency_code,bal.debit_bal,bal.debit_bal_lc,0,0 
		FROM view_ledger_temp_acc_balances bal WHERE bal.acc_period_id=in_acc_period_id AND bal.debit_bal>0;
		/*
		INSERT INTO acc_ledger_close(acc_period_id,account_code,currency_code,debit_bal,debit_bal_lc,credit_bal,credit_bal_lc) 
		SELECT cl.acc_period_id,'3-10-000-060',cl.currency_code,cl.credit_bal,cl.credit_bal_lc,0,0 
		FROM acc_ledger_close cl WHERE cl.acc_period_id=in_acc_period_id AND cl.credit_bal>0;
		*/
		INSERT INTO acc_ledger_close(acc_period_id,account_code,currency_code,debit_bal,debit_bal_lc,credit_bal,credit_bal_lc) 
		SELECT bal.acc_period_id,'3-10-000-060',bal.currency_code,0,0,bal.credit_bal,bal.credit_bal_lc 
		FROM view_ledger_temp_acc_balances bal WHERE bal.acc_period_id=in_acc_period_id AND bal.credit_bal>0;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_reopen_account_period;
DELIMITER //
CREATE PROCEDURE sp_reopen_account_period
(
		IN in_acc_period_id int
) 
BEGIN 		
		DELETE FROM acc_ledger_close WHERE acc_ledger_close_id>0 AND acc_period_id=in_acc_period_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_take_snapshot_stock_value;
DELIMITER //
CREATE PROCEDURE sp_take_snapshot_stock_value
(
		IN in_acc_period_id int
) 
BEGIN 		
		SET @new_id=0;
		CALL sp_get_new_id("snapshot_stock_value","snapshot_no",@new_id);

		SET @cur_sys_datetime=null;
		CALL sp_get_current_system_datetime(@cur_sys_datetime);

		INSERT INTO snapshot_stock_value(snapshot_no,snapshot_date,acc_period_id,item_id,batchno,code_specific,desc_specific,
		currency_code,currentqty,unit_cost_price,cp_value,wp_value,rp_value) 
		select @new_id,@cur_sys_datetime,in_acc_period_id,s.item_id,s.batchno,s.code_specific,s.desc_specific,i.currency_code,s.currentqty,s.unit_cost as unit_cost_price,
		(s.currentqty*s.unit_cost) as cp_value,(s.currentqty*i.unit_wholesale_price) as wp_value,(s.currentqty*i.unit_retailsale_price) as rp_value 
		from stock s inner join item i on s.item_id=i.item_id where i.is_suspended!='Yes' and i.is_track=1 and i.is_sale=1 and i.is_asset=0;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_snapshot_stock_value;
DELIMITER //
CREATE PROCEDURE sp_insert_snapshot_stock_value
(
		IN in_acc_period_id int,
		IN in_snapshot_no long,
		IN in_snapshot_date datetime,
		IN in_cdc_id varchar(20)
) 
BEGIN 
		INSERT INTO snapshot_stock_value(snapshot_no,snapshot_date,acc_period_id,item_id,batchno,code_specific,desc_specific,
		currency_code,currentqty,unit_cost_price,cp_value,wp_value,rp_value,cdc_id,specific_size,qty_damage,store_id) 
		select in_snapshot_no,in_snapshot_date,in_acc_period_id,s.item_id,s.batchno,s.code_specific,
		s.desc_specific,i.currency_code,s.currentqty,s.unit_cost as unit_cost_price,
		(s.currentqty*s.unit_cost) as cp_value,(s.currentqty*i.unit_wholesale_price) as wp_value,
		(s.currentqty*i.unit_retailsale_price) as rp_value,in_cdc_id,s.specific_size,s.qty_damage,s.store_id 
		from stock s inner join item i on s.item_id=i.item_id where i.is_suspended!='Yes' and i.is_track=1 and i.is_asset=0;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_take_snapshot_xrate;
DELIMITER //
CREATE PROCEDURE sp_take_snapshot_xrate
(
		IN in_acc_period_id int
) 
BEGIN 		
		SET @new_id=0;
		CALL sp_get_new_id("snapshot_xrate","snapshot_no",@new_id);

		SET @cur_sys_datetime=null;
		CALL sp_get_current_system_datetime(@cur_sys_datetime);

		INSERT INTO snapshot_xrate(snapshot_no,snapshot_date,acc_period_id,local_currency_id,foreign_currency_id,
		local_currency_code,foreign_currency_code,buying,selling,is_active,is_deleted) 
		select @new_id,@cur_sys_datetime,in_acc_period_id,xr.local_currency_id,xr.foreign_currency_id,xr.local_currency_code,
		xr.foreign_currency_code,xr.buying,selling,xr.is_active,xr.is_deleted from acc_xrate xr;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_post_ledger_open_balances;
DELIMITER //
CREATE PROCEDURE sp_post_ledger_open_balances
(
		IN in_closed_acc_period_id int,
		IN in_opened_acc_period_id int
) 
BEGIN 		
		DELETE FROM acc_ledger_open_bal WHERE acc_ledger_open_bal_id>0 AND acc_period_id=in_opened_acc_period_id;

		INSERT INTO acc_ledger_open_bal(
		acc_period_id,bill_transactor_id,account_code,acc_child_account_id,currency_code,
		debit_amount,credit_amount,debit_amount_lc,credit_amount_lc) 
		SELECT in_opened_acc_period_id as acc_period_id,bal.bill_transactor_id,bal.account_code,bal.acc_child_account_id,bal.currency_code,
		bal.debit_bal as debit_amount,0 as credit_amount,bal.debit_bal_lc as debit_amount_lc,0 as credit_amount_lc 
		FROM view_ledger_union_close_balances bal WHERE bal.acc_period_id=in_closed_acc_period_id AND bal.debit_bal>0;

		INSERT INTO acc_ledger_open_bal(
		acc_period_id,bill_transactor_id,account_code,acc_child_account_id,currency_code,
		debit_amount,credit_amount,debit_amount_lc,credit_amount_lc) 
		SELECT in_opened_acc_period_id as acc_period_id,bal.bill_transactor_id,bal.account_code,bal.acc_child_account_id,bal.currency_code,
		0 as debit_amount,bal.credit_bal as credit_amount,0 as debit_amount_lc,bal.credit_bal_lc as credit_amount_lc 
		FROM view_ledger_union_close_balances bal WHERE bal.acc_period_id=in_closed_acc_period_id AND bal.credit_bal>0;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_save_acc_period;
DELIMITER //
CREATE PROCEDURE sp_save_acc_period
(
	IN in_acc_period_id int,
	IN in_acc_period_name varchar(50),
	IN in_start_date date,
	IN in_end_date date,
	IN in_order_no int,
	IN in_is_current int,
	IN in_is_open int,
	IN in_is_closed int,
	IN in_is_active int,
	IN in_is_deleted int,
	IN in_user_detail_id int,
	IN in_is_reopen int
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("acc_period","acc_period_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @in_user_detail_id=null;
	if (in_user_detail_id!=0) then 
		set @in_user_detail_id=in_user_detail_id;
	end if;

	if (in_acc_period_id=0) then 
		INSERT INTO acc_period(acc_period_name,start_date,end_date,order_no,is_current,is_open,is_closed,is_active,is_deleted,
		add_by,add_date,last_edit_by,last_edit_date,is_reopen) 
		VALUES(in_acc_period_name,in_start_date,in_end_date,in_order_no,in_is_current,in_is_open,in_is_closed,in_is_active,in_is_deleted,
		@in_user_detail_id,@cur_sys_datetime,null,null,in_is_reopen);
	end if;

	if (in_acc_period_id>0) then 
		UPDATE acc_period SET acc_period_name=in_acc_period_name,start_date=in_start_date,end_date=in_end_date,
		order_no=in_order_no,is_current=in_is_current,is_open=in_is_open,is_closed=in_is_closed,is_active=in_is_active,is_deleted=in_is_deleted,
		last_edit_by=@in_user_detail_id,last_edit_date=@cur_sys_datetime,is_reopen=in_is_reopen WHERE acc_period_id=in_acc_period_id;
	end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_copy_pay;
DELIMITER //
CREATE PROCEDURE sp_copy_pay
(
	IN in_pay_id bigint,
	IN in_hist_flag varchar(10),
	OUT out_pay_hist_id bigint
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("pay_hist","pay_hist_id",@new_id);
	
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	INSERT INTO pay_hist(pay_hist_id,hist_flag,hist_add_date,
	pay_id,pay_date,paid_amount,pay_method_id,add_user_detail_id,edit_user_detail_id,add_date,edit_date,points_spent,
	points_spent_amount,delete_pay_id,pay_ref_no,pay_category,bill_transactor_id,pay_type_id,pay_reason_id,store_id,
	acc_child_account_id,acc_child_account_id2,currency_code,xrate,status,status_desc,principal_amount,interest_amount 
	) SELECT @new_id,in_hist_flag,@cur_sys_datetime,
	pay_id,pay_date,paid_amount,pay_method_id,add_user_detail_id,edit_user_detail_id,add_date,edit_date,points_spent,
	points_spent_amount,delete_pay_id,pay_ref_no,pay_category,bill_transactor_id,pay_type_id,pay_reason_id,store_id,
	acc_child_account_id,acc_child_account_id2,currency_code,xrate,status,status_desc,principal_amount,interest_amount 
	FROM pay WHERE pay_id=in_pay_id;
	SET out_pay_hist_id=@new_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_copy_pay_trans;
DELIMITER //
CREATE PROCEDURE sp_copy_pay_trans
(
	IN in_pay_id bigint,
	IN in_pay_hist_id bigint,
	IN in_pay_trans_id bigint
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("pay_trans_hist","pay_trans_hist_id",@new_id);

	INSERT INTO pay_trans_hist(pay_trans_hist_id,pay_hist_id,
	pay_trans_id,pay_id,trans_paid_amount,transaction_id,transaction_number,transaction_type_id,transaction_reason_id
	) SELECT @new_id,in_pay_hist_id,
	pay_trans_id,pay_id,trans_paid_amount,transaction_id,transaction_number,transaction_type_id,transaction_reason_id 
	FROM pay_trans WHERE pay_trans_id=in_pay_trans_id AND pay_id=in_pay_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_journal_by_trans;
DELIMITER //
CREATE PROCEDURE sp_search_journal_by_trans
(
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_transaction_id bigint,
	IN in_pay_type_id int,
	IN in_pay_reason_id int,
	IN in_pay_id bigint
) 
BEGIN 
	SET @in_transaction_type_id=null;
	if (in_transaction_type_id!=0) then 
		set @in_transaction_type_id=in_transaction_type_id;
	end if;
	SET @in_transaction_reason_id=null;
	if (in_transaction_reason_id!=0) then 
		set @in_transaction_reason_id=in_transaction_reason_id;
	end if;
	SET @in_transaction_id=null;
	if (in_transaction_id!=0) then 
		set @in_transaction_id=in_transaction_id;
	end if;
	SET @in_pay_type_id=null;
	if (in_pay_type_id!=0) then 
		set @in_pay_type_id=in_pay_type_id;
	end if;
	SET @in_pay_reason_id=null;
	if (in_pay_reason_id!=0) then 
		set @in_pay_reason_id=in_pay_reason_id;
	end if;
	SET @in_pay_id=null;
	if (in_pay_id!=0) then 
		set @in_pay_id=in_pay_id;
	end if;
	SELECT * FROM acc_journal WHERE transaction_type_id=in_transaction_type_id AND transaction_reason_id=@in_transaction_reason_id 
	AND transaction_id=@in_transaction_id AND pay_type_id=@in_pay_type_id AND pay_reason_id=@in_pay_reason_id AND pay_id=@in_pay_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_acc_child_account;
DELIMITER //
CREATE PROCEDURE sp_insert_acc_child_account
(
	IN in_acc_coa_id int,
	IN in_acc_coa_account_code varchar(20),
	IN in_child_account_code varchar(50),
	IN in_child_account_name varchar(100),
	IN in_child_account_desc varchar(200),
	IN in_user_detail_id int,
	IN in_store_id int,
	IN in_currency_id int,
	IN in_currency_code varchar(20),
	IN in_is_active int,
	IN in_is_deleted int,
	IN in_user_id int,
	IN in_balance_checker_on int
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("acc_child_account","acc_child_account_id",@new_id);
	
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @in_user_detail_id=NULL;
	if (in_user_detail_id!=0) then
		set @in_user_detail_id=in_user_detail_id;
	end if;
	SET @in_store_id=NULL;
	if (in_store_id!=0) then
		set @in_store_id=in_store_id;
	end if;
	SET @in_currency_id=NULL;
	if (in_currency_id!=0) then
		set @in_currency_id=in_currency_id;
		set @in_currency_code=in_currency_code;
	end if;

	INSERT INTO acc_child_account
	(
		acc_child_account_id,
		acc_coa_id,
		acc_coa_account_code,
		child_account_code,
		child_account_name,
		child_account_desc,
		user_detail_id,
		store_id,
		currency_id,
		currency_code,
		is_active,
		is_deleted,
		add_date,
		add_by,
		balance_checker_on
	) 
    VALUES
	(
		@new_id,
		in_acc_coa_id,
		in_acc_coa_account_code,
		in_child_account_code,
		in_child_account_name,
		in_child_account_desc,
		@in_user_detail_id,
		@in_store_id,
		@in_currency_id,
		@in_currency_code,
		in_is_active,
		in_is_deleted,
		@cur_sys_datetime,
		in_user_id,
		in_balance_checker_on
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_acc_child_account;
DELIMITER //
CREATE PROCEDURE sp_update_acc_child_account
(
	IN in_acc_child_account_id int,
	IN in_acc_coa_id int,
	IN in_acc_coa_account_code varchar(20),
	IN in_child_account_code varchar(50),
	IN in_child_account_name varchar(100),
	IN in_child_account_desc varchar(200),
	IN in_user_detail_id int,
	IN in_store_id int,
	IN in_currency_id int,
	IN in_currency_code varchar(20),
	IN in_is_active int,
	IN in_is_deleted int,
	IN in_user_id int,
	IN in_balance_checker_on int
) 
BEGIN 
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @in_user_detail_id=NULL;
	if (in_user_detail_id!=0) then
		set @in_user_detail_id=in_user_detail_id;
	end if;
	SET @in_store_id=NULL;
	if (in_store_id!=0) then
		set @in_store_id=in_store_id;
	end if;
	SET @in_currency_id=NULL;
	SET @in_currency_code=NULL;
	if (in_currency_id!=0) then
		set @in_currency_id=in_currency_id;
		set @in_currency_code=in_currency_code;
	end if;

	UPDATE acc_child_account SET 
		acc_coa_id=in_acc_coa_id,
		acc_coa_account_code=in_acc_coa_account_code,
		child_account_code=in_child_account_code,
		child_account_name=in_child_account_name,
		child_account_desc=in_child_account_desc,
		user_detail_id=@in_user_detail_id,
		store_id=@in_store_id,
		currency_id=@in_currency_id,
		currency_code=@in_currency_code,
		is_active=in_is_active,
		is_deleted=in_is_deleted,
		last_edit_date=@cur_sys_datetime,
		last_edit_by=in_user_id,
		balance_checker_on=in_balance_checker_on 
	WHERE acc_child_account_id=in_acc_child_account_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_save_acc_currency;
DELIMITER //
CREATE PROCEDURE sp_save_acc_currency
(
	IN in_acc_currency_id int,
	IN in_currency_name varchar(100),
	IN in_currency_code varchar(50),
	IN in_currency_no int,
	IN in_is_local_currency int,
	IN in_is_active int,
	IN in_is_deleted int,
	IN in_user_detail_id int,
	IN in_decimal_places int,
	IN in_rounding_mode int,
	IN in_currency_unit varchar(50),
	IN in_decimal_unit varchar(50),
	IN in_currency_code_tax varchar(50)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("acc_currency","acc_currency_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @in_user_detail_id=null;
	if (in_user_detail_id!=0) then 
		set @in_user_detail_id=in_user_detail_id;
	end if;

	if (in_acc_currency_id=0) then 
		INSERT INTO acc_currency(currency_name,currency_code,currency_no,is_local_currency,is_active,is_deleted,
		add_by,add_date,last_edit_by,last_edit_date,decimal_places,rounding_mode,currency_unit,decimal_unit,currency_code_tax) 
		VALUES(in_currency_name,in_currency_code,in_currency_no,in_is_local_currency,in_is_active,in_is_deleted,
		@in_user_detail_id,@cur_sys_datetime,null,null,in_decimal_places,in_rounding_mode,in_currency_unit,in_decimal_unit,in_currency_code_tax);
	end if;

	if (in_acc_currency_id>0) then 
		UPDATE acc_currency SET currency_name=in_currency_name,currency_code=in_currency_code,currency_no=in_currency_no,
		is_local_currency=in_is_local_currency,is_active=in_is_active,is_deleted=in_is_deleted,
		last_edit_by=@in_user_detail_id,last_edit_date=@cur_sys_datetime,decimal_places=in_decimal_places,rounding_mode=in_rounding_mode,
		currency_unit=in_currency_unit,decimal_unit=in_decimal_unit,currency_code_tax=in_currency_code_tax 
		WHERE acc_currency_id=in_acc_currency_id;
	end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_save_acc_xrate;
DELIMITER //
CREATE PROCEDURE sp_save_acc_xrate
(
	IN in_acc_xrate_id int,
	IN in_local_currency_id int,
	IN in_foreign_currency_id int,
	IN in_local_currency_code varchar(50),
	IN in_foreign_currency_code varchar(50),
	IN in_buying double,
	IN in_selling double,
	IN in_is_active int,
	IN in_is_deleted int,
	IN in_user_detail_id int
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("acc_xrate","acc_xrate_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @in_user_detail_id=null;
	if (in_user_detail_id!=0) then 
		set @in_user_detail_id=in_user_detail_id;
	end if;

	if (in_acc_xrate_id=0) then 
		INSERT INTO acc_xrate(local_currency_id,foreign_currency_id,local_currency_code,foreign_currency_code,selling,buying,is_active,is_deleted,
		add_by,add_date,last_edit_by,last_edit_date) 
		VALUES(in_local_currency_id,in_foreign_currency_id,in_local_currency_code,in_foreign_currency_code,in_selling,in_buying,in_is_active,in_is_deleted,
		@in_user_detail_id,@cur_sys_datetime,null,null);
	end if;

	if (in_acc_xrate_id>0) then 
		UPDATE acc_xrate SET local_currency_id=in_local_currency_id,foreign_currency_id=in_foreign_currency_id,
		local_currency_code=in_local_currency_code,foreign_currency_code=in_foreign_currency_code,selling=in_selling,
		buying=in_buying,is_active=in_is_active,is_deleted=in_is_deleted,
		last_edit_by=@in_user_detail_id,last_edit_date=@cur_sys_datetime WHERE acc_xrate_id=in_acc_xrate_id;
	end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_item_unit_cost_price_latest_trans_item_id;
DELIMITER //
CREATE PROCEDURE sp_item_unit_cost_price_latest_trans_item_id
(
		IN in_transaction_type_id int,
		IN in_transaction_reason_id int,
		IN in_store_id int,
		IN in_item_id bigint,
		IN in_batchno varchar(100),
		IN in_code_specific varchar(250),
		IN in_desc_specific varchar(250)
) 
BEGIN 
		SET @in_code_specific='';
		if (in_code_specific is not null) then 
			set @in_code_specific=in_code_specific;
		end if;
		SET @in_desc_specific='';
		if (in_desc_specific is not null) then 
			set @in_desc_specific=in_desc_specific;
		end if;

		SELECT max(transaction_item_id) AS transaction_item_id FROM transaction_item ti,transaction t WHERE ti.transaction_id=t.transaction_id 
		AND t.transaction_type_id=in_transaction_type_id AND t.transaction_reason_id=in_transaction_reason_id 
		AND ti.item_id=in_item_id AND ti.batchno=in_batchno 
		AND ti.code_specific=@in_code_specific AND ti.desc_specific=@in_desc_specific AND ti.unit_cost_price>0;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_item_unit_cost_price_latest_trans_item_id2;
DELIMITER //
CREATE PROCEDURE sp_item_unit_cost_price_latest_trans_item_id2
(
		IN in_transaction_type_id int,
		IN in_transaction_reason_id int,
		IN in_store_id int,
		IN in_item_id bigint,
		IN in_batchno varchar(100),
		IN in_code_specific varchar(250),
		IN in_desc_specific varchar(250)
) 
BEGIN 
		SET @in_batchno='';
		if (in_batchno!='' and in_batchno is not null) then 
			set @in_batchno=concat(" AND batchno='",in_batchno,"'");
		end if;
		SET @in_code_specific='';
		if (in_code_specific!='' and in_code_specific is not null) then 
			set @in_code_specific=concat(" AND code_specific='",in_code_specific,"'");
		end if;
		SET @in_desc_specific='';
		if (in_desc_specific!='' and in_desc_specific is not null) then 
			set @in_desc_specific=concat(" AND desc_specific='",in_desc_specific,"'");
		end if;
        SET @in_transaction_reason_id='';
		if (in_transaction_reason_id!=0 and in_transaction_reason_id is not null) then 
			set @in_transaction_reason_id=concat(" AND t.transaction_reason_id=",in_transaction_reason_id);
		end if;

		SET @in_where=concat(@in_transaction_reason_id,@in_batchno,@in_code_specific,@in_desc_specific);

		SELECT max(transaction_item_id) AS transaction_item_id FROM transaction_item ti,transaction t WHERE ti.transaction_id=t.transaction_id 
		AND t.transaction_type_id=in_transaction_type_id 
		AND ti.item_id=in_item_id AND ti.unit_cost_price>0 AND concat(' 1=1 ',@in_where);
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_item_received_latest_trans_item_id;
DELIMITER //
CREATE PROCEDURE sp_get_item_received_latest_trans_item_id
(
		IN in_item_id bigint,
		IN in_batchno varchar(100),
		IN in_code_specific varchar(250),
		IN in_desc_specific varchar(250),
		IN in_transaction_type_id int
) 
BEGIN 
		SET @in_batchno='';
		if (in_batchno!='' and in_batchno is not null) then 
			set @in_batchno=concat(" AND batchno='",in_batchno,"'");
		end if;
		SET @in_code_specific='';
		if (in_code_specific!='' and in_code_specific is not null) then 
			set @in_code_specific=concat(" AND code_specific='",in_code_specific,"'");
		end if;
		SET @in_desc_specific='';
		if (in_desc_specific!='' and in_desc_specific is not null) then 
			set @in_desc_specific=concat(" AND desc_specific='",in_desc_specific,"'");
		end if;

		SET @in_where=concat(@in_batchno,@in_code_specific,@in_desc_specific);

		SELECT max(transaction_item_id) AS transaction_item_id FROM transaction_item ti,transaction t WHERE ti.transaction_id=t.transaction_id 
		AND t.transaction_type_id=in_transaction_type_id AND ti.item_id=in_item_id AND ti.unit_cost_price>0 AND concat(' 1=1 ',@in_where);
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_item_unit_cost_price_latest_production;
DELIMITER //
CREATE PROCEDURE sp_item_unit_cost_price_latest_production
(
		IN in_store_id int,
		IN in_item_id bigint,
		IN in_batchno varchar(100),
		IN in_code_specific varchar(250),
		IN in_desc_specific varchar(250)
) 
BEGIN 
		SET @in_batchno='';
		if (in_batchno!='' and in_batchno is not null) then 
			set @in_batchno=concat(" AND batchno='",in_batchno,"'");
		end if;
		SET @in_code_specific='';
		if (in_code_specific!='' and in_code_specific is not null) then 
			set @in_code_specific=concat(" AND code_specific='",in_code_specific,"'");
		end if;
		SET @in_desc_specific='';
		if (in_desc_specific!='' and in_desc_specific is not null) then 
			set @in_desc_specific=concat(" AND desc_specific='",in_desc_specific,"'");
		end if;

		SET @in_where=concat(@in_batchno,@in_code_specific,@in_desc_specific);

		SELECT max(transaction_id) AS transaction_id FROM trans_production WHERE output_item_id=in_item_id 
		AND concat(' 1=1 ',@in_where);
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_hist;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_hist
(
	IN in_hist_flag varchar(10),
	OUT out_transaction_hist_id bigint,
	IN in_transaction_date date,
	IN in_store_id int,
	IN in_store2_id int,
	IN in_transactor_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_cash_discount double,
	IN in_total_vat double,
	IN in_transaction_comment varchar(255),
	IN in_add_user_detail_id int,
	IN in_add_date datetime,
	IN in_edit_user_detail_id int,
	IN in_edit_date datetime,
	IN in_transaction_ref varchar(100),
	IN in_sub_total double,
	IN in_grand_total double,
	IN in_total_trade_discount double,
	IN in_points_awarded double,
	IN in_card_number varchar(10),
	IN in_total_std_vatable_amount double,
	IN in_total_zero_vatable_amount double,
	IN in_total_exempt_vatable_amount double,
	IN in_vat_perc double,
	IN in_amount_tendered double,
	IN in_change_amount double,
	IN in_is_cash_discount_vat_liable varchar(3),
	IN in_total_profit_margin double,
	IN in_transaction_user_detail_id int,
	IN in_bill_transactor_id bigint,
	IN in_scheme_transactor_id bigint,
	IN in_princ_scheme_member varchar(100),
	IN in_scheme_card_number varchar(100),
	IN in_transaction_number varchar(50),
	IN in_delivery_date date,
	IN in_delivery_address varchar(250),
	IN in_pay_terms varchar(250),
	IN in_terms_conditions varchar(250),
	IN in_authorised_by_user_detail_id int,
	IN in_authorise_date date,
	IN in_pay_due_date date,
	IN in_expiry_date date,
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_xrate double,
	IN in_from_date date,
	IN in_to_date date,
	IN in_duration_type  varchar(20),
	IN in_site_id bigint,
	IN in_transactor_rep  varchar(50),
	IN in_transactor_vehicle  varchar(20),
	IN in_transactor_driver  varchar(50),
	IN in_duration_value double
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("transaction_hist","transaction_hist_id",@new_id);
	
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @edit_datetime=null;
	SET @edit_user_detail_id=null;
	
	SET @store2_id=NULL;
	if (in_store2_id!=0) then
		set @store2_id=in_store2_id;
	end if;

	SET @transactor_id=NULL;
	if (in_transactor_id!=0) then
		set @transactor_id=in_transactor_id;
	end if;

	SET @transaction_user_detail_id=NULL;
	if (in_transaction_user_detail_id!=0) then
		set @transaction_user_detail_id=in_transaction_user_detail_id;
	end if;

	SET @bill_transactor_id=NULL;
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;

	SET @scheme_transactor_id=NULL;
	if (in_scheme_transactor_id!=0) then
		set @scheme_transactor_id=in_scheme_transactor_id;
	end if;

	SET @scheme_transactor_id=NULL;
	if (in_scheme_transactor_id!=0) then
		set @scheme_transactor_id=in_scheme_transactor_id;
	end if;

	SET @authorised_by_user_detail_id=NULL;
	if (in_authorised_by_user_detail_id!=0) then
		set @authorised_by_user_detail_id=in_authorised_by_user_detail_id;
	end if;

	SET @transaction_number='';
	if (in_transaction_number!='') then
		set @transaction_number=in_transaction_number;
	end if;

	SET @acc_child_account_id=NULL;
	if (in_acc_child_account_id!=0) then
		set @acc_child_account_id=in_acc_child_account_id;
	end if;

	INSERT INTO transaction_hist
	(
		transaction_hist_id,
		hist_flag,
		hist_add_date,
		transaction_id,
		transaction_date,
		store_id,
		store2_id,
		transactor_id,
		transaction_type_id,
		transaction_reason_id,
		cash_discount,
		total_vat,
		transaction_comment,
		add_user_detail_id,
		add_date,
		edit_user_detail_id,
		edit_date,
		transaction_ref,
		sub_total,
		grand_total,
		total_trade_discount,
		points_awarded,
		card_number,
		total_std_vatable_amount,
		total_zero_vatable_amount,
		total_exempt_vatable_amount,
		vat_perc,
		amount_tendered,
		change_amount,
		is_cash_discount_vat_liable,
		total_profit_margin,
		transaction_user_detail_id,
		bill_transactor_id,
		scheme_transactor_id,
		princ_scheme_member,
		scheme_card_number,
		transaction_number,
		delivery_date,
		delivery_address,
		pay_terms,
		terms_conditions,
		authorised_by_user_detail_id,
		authorise_date,
		pay_due_date,
		expiry_date,
		acc_child_account_id,
		currency_code,
		xrate,
		from_date,
		to_date,
		duration_type,
		site_id,
		transactor_rep,
		transactor_vehicle,
		transactor_driver,
		duration_value
	) 
    VALUES
	(
		@new_id,
		in_hist_flag,
		@cur_sys_datetime,
		0,
		in_transaction_date,
		in_store_id,
		@store2_id,
		@transactor_id,
		in_transaction_type_id,
		in_transaction_reason_id,
		in_cash_discount,
		in_total_vat,
		in_transaction_comment,
		in_add_user_detail_id,
		@cur_sys_datetime,
		@edit_user_detail_id,
		@edit_datetime,
		in_transaction_ref,
		in_sub_total,
		in_grand_total,
		in_total_trade_discount,
		in_points_awarded,
		in_card_number,
		in_total_std_vatable_amount,
		in_total_zero_vatable_amount,
		in_total_exempt_vatable_amount,
		in_vat_perc,
		in_amount_tendered,
		in_change_amount,
		in_is_cash_discount_vat_liable,
		in_total_profit_margin,
		@transaction_user_detail_id,
		@bill_transactor_id,
		@scheme_transactor_id,
		in_princ_scheme_member,
		in_scheme_card_number,
		@transaction_number,
		in_delivery_date,
		in_delivery_address,
		in_pay_terms,
		in_terms_conditions,
		@authorised_by_user_detail_id,
		in_authorise_date,
		in_pay_due_date,
		in_expiry_date,
		@acc_child_account_id,
		in_currency_code,
		in_xrate,
		in_from_date,
		in_to_date,
		in_duration_type,
		in_site_id,
		in_transactor_rep,
		in_transactor_vehicle,
		in_transactor_driver,
		in_duration_value
	); 
SET out_transaction_hist_id=@new_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_item_hist;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_item_hist
(
	IN in_transaction_hist_id bigint,
	IN in_transaction_id bigint,
	IN in_item_id bigint,
	IN in_batchno varchar(100),
	IN in_item_qty double,
	IN in_unit_price double,
	IN in_unit_trade_discount double,
	IN in_unit_vat double,
	IN in_amount double,
	IN in_item_expiry_date date,
	IN in_item_mnf_date date,
	IN in_vat_rated varchar(50),
	IN in_vat_perc double,
	IN in_unit_price_inc_vat double,
	IN in_unit_price_exc_vat double,
	IN in_amount_inc_vat double,
	IN in_amount_exc_vat double,
	IN in_stock_effect varchar(1),
	IN in_is_trade_discount_vat_liable varchar(3),
	IN in_unit_cost_price double,
	IN in_unit_profit_margin double,
	IN in_earn_perc double,
	IN in_earn_amount double,
	IN in_code_specific varchar(250),
	IN in_desc_specific varchar(250),
	IN in_desc_more varchar(250),
	IN in_warranty_desc varchar(150),
	IN in_warranty_expiry_date date,
	IN in_account_code varchar(20),
	IN in_purchase_date date,
	IN in_dep_start_date date,
	IN in_dep_method_id int,
	IN in_dep_rate double,
	IN in_average_method_id int,
	IN in_effective_life int,
	IN in_residual_value double,
	IN in_narration varchar(100),
	IN in_qty_balance double,
	IN in_duration_value double,
	IN in_qty_damage double,
	IN in_duration_passed double,
	IN in_specific_size double
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("transaction_item_hist","transaction_item_hist_id",@new_id);

	SET @item_expiry_date=NULL;
	if (in_item_expiry_date is not null) then
		set @item_expiry_date=in_item_expiry_date;
	end if;

	SET @item_mnf_date=NULL;
	if (in_item_mnf_date is not null) then
		set @item_mnf_date=in_item_mnf_date;
	end if;

	SET @in_batchno='';
	if (in_batchno is not null) then
		set @in_batchno=in_batchno;
	end if;
	SET @code_specific='';
	if (in_code_specific is not null) then
		set @code_specific=in_code_specific;
	end if;
	SET @desc_specific='';
	if (in_desc_specific is not null) then
		set @desc_specific=in_desc_specific;
	end if;
	SET @desc_more=null;
	if (in_desc_more!='' and in_desc_more is not null) then
		set @desc_more=in_desc_more;
	end if;
	SET @warranty_desc=null;
	if (in_warranty_desc!="" and in_warranty_desc is not null) then
		set @warranty_desc=in_warranty_desc;
	end if;
	SET @warranty_expiry_date=NULL;
	if (in_warranty_expiry_date is not null) then
		set @warranty_expiry_date=in_warranty_expiry_date;
	end if;
	SET @account_code=null;
	if (in_account_code!='' and in_account_code is not null) then
		set @account_code=in_account_code;
	end if;
		SET @in_purchase_date=NULL;
		if (in_purchase_date is not null) then
			set @in_purchase_date=in_purchase_date;
		end if;
		SET @in_dep_start_date=NULL;
		if (in_dep_start_date is not null) then
			set @in_dep_start_date=in_dep_start_date;
		end if;
		SET @in_dep_method_id=NULL;
		if (in_dep_method_id!=0) then
			set @in_dep_method_id=in_dep_method_id;
		end if;
		SET @in_average_method_id=NULL;
		if (in_average_method_id!=0) then
			set @in_average_method_id=in_average_method_id;
		end if;
		SET @in_item_id=NULL;
		if (in_item_id!=0) then
			set @in_item_id=in_item_id;
		end if;
		SET @in_specific_size=1;
		if (in_specific_size!=0) then
			set @in_specific_size=in_specific_size;
		end if;

	INSERT INTO transaction_item_hist
	(
		transaction_item_hist_id,
		transaction_hist_id,
		transaction_item_id,
		transaction_id,
		item_id,
		batchno,
		item_qty,
		unit_price,
		unit_trade_discount,
		unit_vat,
		amount,
		item_expiry_date,
		item_mnf_date,
		vat_rated,
		vat_perc,
		unit_price_inc_vat,
		unit_price_exc_vat,
		amount_inc_vat,
		amount_exc_vat,
		stock_effect,
		is_trade_discount_vat_liable,
		unit_cost_price,
		unit_profit_margin,
		earn_perc,
		earn_amount,
		code_specific,
		desc_specific,
		desc_more,
		warranty_desc,
		warranty_expiry_date,
		account_code,
		purchase_date,
		dep_start_date,
		dep_method_id,
		dep_rate,
		average_method_id,
		effective_life,
		residual_value,
		narration,
		qty_balance,
		duration_value,
		qty_damage,
		duration_passed,
		specific_size
	) 
    VALUES
	(
		@new_id,
		in_transaction_hist_id,
		0,
		0,
		@in_item_id,
		@in_batchno,
		in_item_qty,
		in_unit_price,
		in_unit_trade_discount,
		in_unit_vat,
		in_amount,
		@item_expiry_date,
		@item_mnf_date,
		in_vat_rated,
		in_vat_perc,
		in_unit_price_inc_vat,
		in_unit_price_exc_vat,
		in_amount_inc_vat,
		in_amount_exc_vat,
		in_stock_effect,
		in_is_trade_discount_vat_liable,
		in_unit_cost_price,
		in_unit_profit_margin,
		in_earn_perc,
		in_earn_amount,
		@code_specific,
		@desc_specific,
		@desc_more,
		@warranty_desc,
		@warranty_expiry_date,
		@account_code,
		@in_purchase_date,
		@in_dep_start_date,
		@in_dep_method_id,
		in_dep_rate,
		@in_average_method_id,
		in_effective_life,
		in_residual_value,
		in_narration,
		in_qty_balance,
		in_duration_value,
		in_qty_damage,
		in_duration_passed,
		in_specific_size
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_item_hist_out;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_item_hist_out
(
	IN in_transaction_hist_id bigint,
	IN in_transaction_id bigint,
	IN in_item_id bigint,
	IN in_batchno varchar(100),
	IN in_item_qty double,
	IN in_unit_price double,
	IN in_unit_trade_discount double,
	IN in_unit_vat double,
	IN in_amount double,
	IN in_item_expiry_date date,
	IN in_item_mnf_date date,
	IN in_vat_rated varchar(50),
	IN in_vat_perc double,
	IN in_unit_price_inc_vat double,
	IN in_unit_price_exc_vat double,
	IN in_amount_inc_vat double,
	IN in_amount_exc_vat double,
	IN in_stock_effect varchar(1),
	IN in_is_trade_discount_vat_liable varchar(3),
	IN in_unit_cost_price double,
	IN in_unit_profit_margin double,
	IN in_earn_perc double,
	IN in_earn_amount double,
	IN in_code_specific varchar(250),
	IN in_desc_specific varchar(250),
	IN in_desc_more varchar(250),
	IN in_warranty_desc varchar(150),
	IN in_warranty_expiry_date date,
	IN in_account_code varchar(20),
	IN in_purchase_date date,
	IN in_dep_start_date date,
	IN in_dep_method_id int,
	IN in_dep_rate double,
	IN in_average_method_id int,
	IN in_effective_life int,
	IN in_residual_value double,
	IN in_narration varchar(100),
	IN in_qty_balance double,
	IN in_duration_value double,
	IN in_qty_damage double,
	IN in_duration_passed double,
	IN in_specific_size double,
    OUT out_transaction_item_hist_id bigint
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("transaction_item_hist","transaction_item_hist_id",@new_id);

	SET @item_expiry_date=NULL;
	if (in_item_expiry_date is not null) then
		set @item_expiry_date=in_item_expiry_date;
	end if;

	SET @item_mnf_date=NULL;
	if (in_item_mnf_date is not null) then
		set @item_mnf_date=in_item_mnf_date;
	end if;

	SET @in_batchno='';
	if (in_batchno is not null) then
		set @in_batchno=in_batchno;
	end if;
	SET @code_specific='';
	if (in_code_specific is not null) then
		set @code_specific=in_code_specific;
	end if;
	SET @desc_specific='';
	if (in_desc_specific is not null) then
		set @desc_specific=in_desc_specific;
	end if;
	SET @desc_more=null;
	if (in_desc_more!='' and in_desc_more is not null) then
		set @desc_more=in_desc_more;
	end if;
	SET @warranty_desc=null;
	if (in_warranty_desc!="" and in_warranty_desc is not null) then
		set @warranty_desc=in_warranty_desc;
	end if;
	SET @warranty_expiry_date=NULL;
	if (in_warranty_expiry_date is not null) then
		set @warranty_expiry_date=in_warranty_expiry_date;
	end if;
	SET @account_code=null;
	if (in_account_code!='' and in_account_code is not null) then
		set @account_code=in_account_code;
	end if;
		SET @in_purchase_date=NULL;
		if (in_purchase_date is not null) then
			set @in_purchase_date=in_purchase_date;
		end if;
		SET @in_dep_start_date=NULL;
		if (in_dep_start_date is not null) then
			set @in_dep_start_date=in_dep_start_date;
		end if;
		SET @in_dep_method_id=NULL;
		if (in_dep_method_id!=0) then
			set @in_dep_method_id=in_dep_method_id;
		end if;
		SET @in_average_method_id=NULL;
		if (in_average_method_id!=0) then
			set @in_average_method_id=in_average_method_id;
		end if;
		SET @in_item_id=NULL;
		if (in_item_id!=0) then
			set @in_item_id=in_item_id;
		end if;
		SET @in_specific_size=1;
		if (in_specific_size!=0) then
			set @in_specific_size=in_specific_size;
		end if;

	INSERT INTO transaction_item_hist
	(
		transaction_item_hist_id,
		transaction_hist_id,
		transaction_item_id,
		transaction_id,
		item_id,
		batchno,
		item_qty,
		unit_price,
		unit_trade_discount,
		unit_vat,
		amount,
		item_expiry_date,
		item_mnf_date,
		vat_rated,
		vat_perc,
		unit_price_inc_vat,
		unit_price_exc_vat,
		amount_inc_vat,
		amount_exc_vat,
		stock_effect,
		is_trade_discount_vat_liable,
		unit_cost_price,
		unit_profit_margin,
		earn_perc,
		earn_amount,
		code_specific,
		desc_specific,
		desc_more,
		warranty_desc,
		warranty_expiry_date,
		account_code,
		purchase_date,
		dep_start_date,
		dep_method_id,
		dep_rate,
		average_method_id,
		effective_life,
		residual_value,
		narration,
		qty_balance,
		duration_value,
		qty_damage,
		duration_passed,
		specific_size
	) 
    VALUES
	(
		@new_id,
		in_transaction_hist_id,
		0,
		0,
		@in_item_id,
		@in_batchno,
		in_item_qty,
		in_unit_price,
		in_unit_trade_discount,
		in_unit_vat,
		in_amount,
		@item_expiry_date,
		@item_mnf_date,
		in_vat_rated,
		in_vat_perc,
		in_unit_price_inc_vat,
		in_unit_price_exc_vat,
		in_amount_inc_vat,
		in_amount_exc_vat,
		in_stock_effect,
		in_is_trade_discount_vat_liable,
		in_unit_cost_price,
		in_unit_profit_margin,
		in_earn_perc,
		in_earn_amount,
		@code_specific,
		@desc_specific,
		@desc_more,
		@warranty_desc,
		@warranty_expiry_date,
		@account_code,
		@in_purchase_date,
		@in_dep_start_date,
		@in_dep_method_id,
		in_dep_rate,
		@in_average_method_id,
		in_effective_life,
		in_residual_value,
		in_narration,
		in_qty_balance,
		in_duration_value,
		in_qty_damage,
		in_duration_passed,
		in_specific_size
	); 
    SET out_transaction_item_hist_id=@new_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_sales_by_user;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_sales_by_user
(
	IN in_add_user_detail_id int 
) 
BEGIN 
		SELECT * FROM transaction t
		WHERE t.add_user_detail_id=in_add_user_detail_id AND t.transaction_type_id=2 
		ORDER BY t.add_date DESC,t.transaction_id DESC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_hist_draft_by_user;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_hist_draft_by_user
(
	IN in_add_user_detail_id int 
) 
BEGIN 
		SET @cur_sys_datetime=null;
		CALL sp_get_current_system_datetime(@cur_sys_datetime);

		SELECT * FROM transaction_hist th 
		WHERE th.add_user_detail_id=in_add_user_detail_id AND th.hist_flag='Draft' AND 
		DATE_FORMAT(@cur_sys_datetime,'%Y-%m-%d')=DATE_FORMAT(th.hist_add_date,'%Y-%m-%d') 
		ORDER BY transaction_hist_id DESC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_hist_draft_by_user_type;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_hist_draft_by_user_type
(
	IN in_store_id int,
	IN in_add_user_detail_id int,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int
) 
BEGIN 
		SET @cur_sys_datetime=null;
		CALL sp_get_current_system_datetime(@cur_sys_datetime);

		SELECT * FROM transaction_hist th 
		WHERE th.transaction_type_id=in_transaction_type_id AND th.transaction_reason_id=in_transaction_reason_id AND
		th.store_id=in_store_id AND th.add_user_detail_id=in_add_user_detail_id AND th.hist_flag='Draft' AND 
		DATE_FORMAT(@cur_sys_datetime,'%Y-%m-%d')=DATE_FORMAT(th.hist_add_date,'%Y-%m-%d') 
		ORDER BY transaction_hist_id DESC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_hist_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_hist_by_id
(
	IN in_transaction_hist_id bigint 
) 
BEGIN 
		SELECT * FROM transaction_hist th 
		WHERE th.transaction_hist_id=in_transaction_hist_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_item_hist_by_hist_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_item_hist_by_hist_id
(
	IN in_transaction_hist_id bigint 
) 
BEGIN 
		SELECT tih.*,tihu.unit_id,tihu.base_unit_qty FROM transaction_item_hist tih 
        INNER JOIN transaction_item_hist_unit tihu ON tih.transaction_item_hist_id=tihu.transaction_item_hist_id 
		WHERE tih.transaction_hist_id=in_transaction_hist_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_delete_transaction_hist_by_id;
DELIMITER //
CREATE PROCEDURE sp_delete_transaction_hist_by_id
(
	IN in_transaction_hist_id bigint 
) 
BEGIN 
	DELETE FROM transaction_item_hist_unit WHERE transaction_item_hist_id IN(SELECT I2.transaction_item_hist_id FROM transaction_item_hist I2 where I2.transaction_hist_id=in_transaction_hist_id);
    DELETE FROM transaction_item_hist_excise WHERE transaction_item_hist_excise_id>0 AND transaction_item_hist_id IN(SELECT I2.transaction_item_hist_id FROM transaction_item_hist I2 where I2.transaction_hist_id=in_transaction_hist_id);
	DELETE FROM transaction_item_hist WHERE transaction_hist_id=in_transaction_hist_id;
	DELETE FROM transaction_hist WHERE transaction_hist_id=in_transaction_hist_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_item_by_tt_ii;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_item_by_tt_ii
(
	IN in_item_id bigint,
	IN in_transaction_type_id int
) 
BEGIN 
	SELECT * FROM transaction_item ti 
	INNER JOIN transaction t ON ti.transaction_id=t.transaction_id 
	WHERE ti.item_id=in_item_id AND t.transaction_type_id=in_transaction_type_id 
	ORDER BY t.transaction_date DESC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_item_by_tt_tr_ii;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_item_by_tt_tr_ii
(
	IN in_item_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_limit int
) 
BEGIN 
	SELECT ti.*,tiu.unit_id,tiu.base_unit_qty FROM transaction_item ti 
	INNER JOIN transaction t ON ti.transaction_id=t.transaction_id 
    INNER JOIN transaction_item_unit tiu ON ti.transaction_item_id=tiu.transaction_item_id 
	WHERE ti.item_id=in_item_id AND t.transaction_type_id=in_transaction_type_id 
	AND t.transaction_reason_id=in_transaction_reason_id 
	ORDER BY t.transaction_date DESC 
	LIMIT in_limit;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_trans_production;
DELIMITER //
CREATE PROCEDURE sp_insert_trans_production
(
	IN in_transaction_date date,
	IN in_store_id int,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_transaction_ref varchar(100),
	IN in_output_item_id bigint,
	IN in_output_qty double,
	IN in_output_unit_cost double,
	IN in_output_total_cost double,
	IN in_batchno varchar(100),
	OUT out_transaction_id bigint,
	OUT out_output_qty double,
	OUT out_store_id int,
	IN in_item_expiry_date date,
	IN in_item_mnf_date date,
	IN in_code_specific varchar(250),
	IN in_desc_specific varchar(250),
	IN in_desc_more varchar(250),
	IN in_transaction_comment varchar(255),
	IN in_add_user_detail_id int,
	IN in_add_date datetime,
	IN in_transaction_user_detail_id int,
	IN in_edit_user_detail_id int,
	IN in_edit_date datetime,
	IN in_currency_code varchar(10),
	IN in_account_code varchar(20),
	IN in_transaction_number varchar(50),
	IN in_transactor_id bigint,
	IN in_specific_size double,
    IN in_unit_id int,
    IN in_base_unit_qty double
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("trans_production","transaction_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);
	
	SET @item_expiry_date=null;
	SET @item_mnf_date=null;
	
	SET @edit_datetime=null;
	SET @edit_user_detail_id=null;

	SET @in_batchno='';
	if (in_batchno is not null) then
		set @in_batchno=in_batchno;
	end if;
	SET @transaction_user_detail_id=NULL;
	if (in_transaction_user_detail_id!=0) then
		set @transaction_user_detail_id=in_transaction_user_detail_id;
	end if;
	SET @code_specific='';
	if (in_code_specific is not null) then
		set @code_specific=in_code_specific;
	end if;
	SET @desc_specific='';
	if (in_desc_specific is not null) then
		set @desc_specific=in_desc_specific;
	end if;
	SET @desc_more='';
	if (in_desc_more is not null) then
		set @desc_more=in_desc_more;
	end if;
	SET @in_transactor_id=NULL;
	if (in_transactor_id!=0) then
		set @in_transactor_id=in_transactor_id;
	end if;
	SET @in_transaction_number=@new_id;
	if (in_transaction_number!='') then
		set @in_transaction_number=in_transaction_number;
	end if;
	SET @in_specific_size=1;
		if (in_specific_size!=0) then
		set @in_specific_size=in_specific_size;
	end if;

	INSERT INTO trans_production
	(
		transaction_id,
		transaction_date,
		store_id,
		transaction_type_id,
		transaction_reason_id,
		transaction_ref,
		output_item_id,
		output_qty,
		output_unit_cost,
		output_total_cost,
		batchno,
		item_expiry_date,
		item_mnf_date,
		code_specific,
		desc_specific,
		desc_more,
		transaction_comment,
		add_user_detail_id,
		add_date,
		transaction_user_detail_id,
		edit_user_detail_id,
		edit_date,
		currency_code,
		account_code,
		transaction_number,
		transactor_id,
		specific_size,
        unit_id,
		base_unit_qty
	) 
    VALUES
	(
		@new_id,
		in_transaction_date,
		in_store_id,
		in_transaction_type_id,
		in_transaction_reason_id,
		in_transaction_ref,
		in_output_item_id,
		in_output_qty,
		in_output_unit_cost,
		in_output_total_cost,
		@in_batchno,
		@item_expiry_date,
		@item_mnf_date,
		@code_specific,
		@desc_specific,
		@desc_more,
		in_transaction_comment,
		in_add_user_detail_id,
		@cur_sys_datetime,
		@transaction_user_detail_id,
		@edit_user_detail_id,
		@edit_date,
		in_currency_code,
		in_account_code,
		@in_transaction_number,
		@in_transactor_id,
		@in_specific_size,
        in_unit_id,
		in_base_unit_qty
	); 
SET out_transaction_id=@new_id;	
SET out_output_qty=in_output_qty;
SET out_store_id=in_store_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_trans_production_item;
DELIMITER //
CREATE PROCEDURE sp_insert_trans_production_item
(
	IN in_transaction_id bigint,
	IN in_input_item_id bigint,	
	IN in_input_qty double,
	IN in_input_unit_cost double,
	IN in_batchno varchar(100),
	IN in_code_specific varchar(250),
	IN in_desc_specific varchar(250),
	IN in_desc_more varchar(250),
	IN in_input_unit_qty double,
	IN in_input_qty_bfr_prod double,
	IN in_input_qty_afr_prod double,
    IN in_unit_id int,
    IN in_base_unit_qty double,
    OUT out_trans_production_item_id bigint
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("trans_production_item","trans_production_item_id",@new_id);

	SET @in_batchno='';
	if (in_batchno is not null) then
		set @in_batchno=in_batchno;
	end if;
	SET @code_specific='';
	if (in_code_specific is not null) then
		set @code_specific=in_code_specific;
	end if;
	SET @desc_specific='';
	if (in_desc_specific is not null) then
		set @desc_specific=in_desc_specific;
	end if;
	SET @desc_more=null;
	if (in_desc_more!='' and in_desc_more is not null) then
		set @desc_more=in_desc_more;
	end if;
		SET @in_input_item_id=NULL;
		if (in_input_item_id!=0) then
			set @in_input_item_id=in_input_item_id;
		end if;

	INSERT INTO trans_production_item
	(
		trans_production_item_id,
		transaction_id,
		input_item_id,	
		input_qty,
		input_unit_cost,
		batchno,
		code_specific,
		desc_specific,
		desc_more,
		input_unit_qty,
		input_qty_bfr_prod,
		input_qty_afr_prod,
        unit_id,
		base_unit_qty
		) 
    VALUES
	(
		@new_id,
		in_transaction_id,
		in_input_item_id,	
		in_input_qty,
		in_input_unit_cost,
		in_batchno,
		in_code_specific,
		in_desc_specific,
		in_desc_more,
		in_input_unit_qty,
		in_input_qty_bfr_prod,
		in_input_qty_afr_prod,
        in_unit_id,
		in_base_unit_qty
		); 
	SET out_trans_production_item_id=@new_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_production_old;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_production_old
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SELECT * FROM item 
		WHERE is_suspended='No' AND is_sale=0 AND is_buy=1 AND is_asset=0 AND expense_type='Raw Material' AND 
		(description LIKE concat('%',in_code_desc,'%') OR item_code LIKE concat('%',in_code_desc,'%')) 
		ORDER BY description ASC LIMIT 10;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_production;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_production
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SET @spaces=length(in_code_desc)-length(replace(in_code_desc,' ',''));
		SET @word1=SUBSTRING_INDEX(in_code_desc, ' ', 1);
        SET @word2=SUBSTRING_INDEX(in_code_desc, ' ', -1);
        
        if (@spaces=1) then 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_track=1 AND i.is_asset=0 AND (
            (i.description LIKE concat('%',@word1,'%') AND i.description LIKE concat('%',@word2,'%')) OR  
			(i.item_code LIKE concat('%',in_code_desc,'%') OR ic.item_code LIKE concat('%',in_code_desc,'%'))
            ) 
			ORDER BY i.description ASC LIMIT 15;
        else 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_track=1 AND i.is_asset=0 AND 
			(i.description LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',in_code_desc,'%') OR ic.item_code LIKE concat('%',in_code_desc,'%')) 
			ORDER BY i.description ASC LIMIT 15;
        end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_for_raw_material;
DELIMITER //
CREATE PROCEDURE sp_search_item_for_raw_material
(
	IN in_code_desc varchar(100)
) 
BEGIN  
		SET @spaces=length(in_code_desc)-length(replace(in_code_desc,' ',''));
		SET @word1=SUBSTRING_INDEX(in_code_desc, ' ', 1);
        SET @word2=SUBSTRING_INDEX(in_code_desc, ' ', -1);
        
        if (@spaces=1) then 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_track=1 AND i.is_asset=0 AND (
            (i.description LIKE concat('%',@word1,'%') AND i.description LIKE concat('%',@word2,'%')) OR  
			(i.item_code LIKE concat('%',in_code_desc,'%') OR ic.item_code LIKE concat('%',in_code_desc,'%'))
            ) 
			ORDER BY i.description ASC LIMIT 15;
        else 
			SELECT distinct i.* FROM item i left join item_code_other ic on i.item_id=ic.item_id 
			WHERE i.is_suspended='No' AND i.is_track=1 AND i.is_asset=0 AND 
			(i.description LIKE concat('%',in_code_desc,'%') OR i.item_code LIKE concat('%',in_code_desc,'%') OR ic.item_code LIKE concat('%',in_code_desc,'%')) 
			ORDER BY i.description ASC LIMIT 15;
        end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_item_production_map_by_output_item_id;
DELIMITER //
CREATE PROCEDURE sp_search_item_production_map_by_output_item_id
(
	IN in_output_item_id bigint
) 
BEGIN 
	SELECT * FROM item_production_map 
	WHERE output_item_id=in_output_item_id;  
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_trans_production_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_trans_production_by_id
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT * FROM trans_production t 
		WHERE t.transaction_id=in_transaction_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_trans_production_item_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_trans_production_item_by_id
(
	IN in_trans_production_item_id bigint 
) 
BEGIN 
		SELECT * FROM trans_production_item t 
		WHERE t.trans_production_item_id=in_trans_production_item_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_trans_production_item_by_trans_production_id;
DELIMITER //
CREATE PROCEDURE sp_search_trans_production_item_by_trans_production_id
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT * FROM trans_production_item ti 
		WHERE ti.transaction_id=in_transaction_id ORDER BY ti.trans_production_item_id ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_contact_list;
DELIMITER //
CREATE PROCEDURE sp_insert_contact_list
(
	IN in_category varchar(255),
	IN in_subcategory varchar(255),
	IN in_company_name varchar(255),
	IN in_title varchar(255),
	IN in_first_name varchar(255),
	IN in_second_name varchar(255),
	IN in_email1 varchar(255),
	IN in_email2 varchar(255),
	IN in_phone1 varchar(255),
	IN in_phone2 varchar(255),
	IN in_source varchar(255),
	IN in_type varchar(255),
	IN in_loc_level_1 varchar(255),
	IN in_loc_level_3 varchar(255),
	IN in_loc_level_2 varchar(255),
	IN in_loc_level_4 varchar(255),
	IN in_address varchar(255)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("contact_list","contact_list_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	INSERT INTO contact_list
	(
		contact_list_id,
		category,
		subcategory,
		company_name,
		title,
		first_name,
		second_name,
		email1,
		email2,
		phone1,
		phone2,
		source,
		type,
		loc_level_1,
		loc_level_2,
		loc_level_3,
		loc_level_4,
		address
	) 
    VALUES
	(
		@new_id,
		in_category,
		in_subcategory,
		in_company_name,
		in_title,
		in_first_name,
		in_second_name,
		in_email1,
		in_email2,
		in_phone1,
		in_phone2,
		in_source,
		in_type,
		in_loc_level_1,
		in_loc_level_2,
		in_loc_level_3,
		in_loc_level_4,
		in_address
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_contact_list;
DELIMITER //
CREATE PROCEDURE sp_update_contact_list
(
	IN in_contact_list_id int,
	IN in_category varchar(255),
	IN in_subcategory varchar(255),
	IN in_company_name varchar(255),
	IN in_title varchar(255),
	IN in_first_name varchar(255),
	IN in_second_name varchar(255),
	IN in_email1 varchar(255),
	IN in_email2 varchar(255),
	IN in_phone1 varchar(255),
	IN in_phone2 varchar(255),
	IN in_source varchar(255),
	IN in_type varchar(255),
	IN in_loc_level_1 varchar(255),
	IN in_loc_level_3 varchar(255),
	IN in_loc_level_2 varchar(255),
	IN in_loc_level_4 varchar(255),
	IN in_address varchar(255)
) 
BEGIN 

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	UPDATE contact_list SET 
		category=in_category,
		subcategory=in_subcategory,
		company_name=in_company_name,
		title=in_title,
		first_name=in_first_name,
		second_name=in_second_name,
		email1=in_email1,
		email2=in_email2,
		phone1=in_phone1,
		phone2=in_phone2,
		source=in_source,
		type=in_type,
		loc_level_1=in_loc_level_1,
		loc_level_2=in_loc_level_2,
		loc_level_3=in_loc_level_3,
		loc_level_4=in_loc_level_4,
		address=in_address
	WHERE contact_list_id=in_contact_list_id; 
END//
DELIMITER ;


DROP PROCEDURE IF EXISTS sp_search_contact_list_by_company_email_firstname;
DELIMITER //
CREATE PROCEDURE sp_search_contact_list_by_company_email_firstname
(
	IN in_text varchar(100)
) 
BEGIN 
	SELECT * FROM contact_list t 
	WHERE t.company_name LIKE concat('%',in_text,'%') OR t.email1 LIKE concat('%',in_text,'%') OR t.email2 LIKE concat('%',in_text,'%') OR t.first_name LIKE concat('%',in_text,'%') OR t.second_name LIKE concat('%',in_text,'%') 
	ORDER BY t.company_name ASC LIMIT 20; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_parameter_list;
DELIMITER //
CREATE PROCEDURE sp_insert_parameter_list
(
	IN in_context varchar(255),
	IN in_parameter_name varchar(255),
	IN in_parameter_value varchar(255),
	IN in_description varchar(255),
	IN in_store_id int
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("parameter_list","parameter_list_id",@new_id);

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	INSERT INTO parameter_list
	(
		parameter_list_id,
		context,
		parameter_name,
		parameter_value,
		description,
		store_id
	) 
    VALUES
	(
		@new_id,
		in_context,
		in_parameter_name,
		in_parameter_value,
		in_description,
		in_store_id
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_parameter_list;
DELIMITER //
CREATE PROCEDURE sp_update_parameter_list
(
	IN in_parameter_list_id int,
	IN in_context varchar(255),
	IN in_parameter_name varchar(255),
	IN in_parameter_value varchar(255),
	IN in_description varchar(255),
	IN in_store_id int
) 
BEGIN 

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	UPDATE parameter_list SET 
		context=in_context,
		parameter_name=in_parameter_name,
		parameter_value=in_parameter_value,
		description=in_description,
		store_id=in_store_id
	WHERE parameter_list_id=in_parameter_list_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_records_by_transactor;
DELIMITER //
CREATE PROCEDURE sp_search_records_by_transactor
(
	IN in_transactor_id bigint
) 
BEGIN 
	SELECT 'transaction' as table_name,count(*) as records FROM transaction t1 where t1.transactor_id=in_transactor_id or t1.bill_transactor_id=in_transactor_id 
	UNION 
	SELECT 'trans_production' as table_name,count(*) as records FROM trans_production t2 where t2.transactor_id=in_transactor_id 
	UNION 
	SELECT 'pay' as table_name,count(*) as records FROM pay t3 where t3.bill_transactor_id=in_transactor_id 
	UNION 
	SELECT 'acc_journal' as table_name,count(*) as records FROM acc_journal t4 where t4.bill_transactor_id=in_transactor_id 
	UNION 
	SELECT 'acc_ledger' as table_name,count(*) as records FROM acc_ledger t5 where t5.bill_transactor_id=in_transactor_id 
	UNION 
	SELECT 'site' as table_name,count(*) as records FROM site t6 where t6.transactor_id=in_transactor_id 
	UNION 
	SELECT 'stock_out' as table_name,count(*) as records FROM stock_out t7 where t7.transactor_id=in_transactor_id 
	; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_merge_records_by_transactor;
DELIMITER //
CREATE PROCEDURE sp_merge_records_by_transactor
(
	IN in_from_transactor_id bigint,
	IN in_to_transactor_id bigint
) 
BEGIN 
	UPDATE transaction SET transactor_id=in_to_transactor_id WHERE transaction_id>0 AND transactor_id=in_from_transactor_id;
	UPDATE transaction SET bill_transactor_id=in_to_transactor_id WHERE transaction_id>0 AND bill_transactor_id=in_from_transactor_id;
	
	UPDATE trans_production SET transactor_id=in_to_transactor_id WHERE transaction_id>0 AND transactor_id=in_from_transactor_id;
	
	UPDATE pay SET bill_transactor_id=in_to_transactor_id WHERE pay_id>0 AND bill_transactor_id=in_from_transactor_id;

	UPDATE acc_journal SET bill_transactor_id=in_to_transactor_id WHERE acc_journal_id>0 AND bill_transactor_id=in_from_transactor_id;
	UPDATE acc_journal_payable SET bill_transactor_id=in_to_transactor_id WHERE acc_journal_id>0 AND bill_transactor_id=in_from_transactor_id;
	UPDATE acc_journal_prepaid SET bill_transactor_id=in_to_transactor_id WHERE acc_journal_id>0 AND bill_transactor_id=in_from_transactor_id;
	UPDATE acc_journal_receivable SET bill_transactor_id=in_to_transactor_id WHERE acc_journal_id>0 AND bill_transactor_id=in_from_transactor_id;

	UPDATE acc_ledger SET bill_transactor_id=in_to_transactor_id WHERE acc_ledger_id>0 AND bill_transactor_id=in_from_transactor_id; 
	UPDATE acc_ledger_open_bal SET bill_transactor_id=in_to_transactor_id WHERE acc_ledger_open_bal_id>0 AND bill_transactor_id=in_from_transactor_id;
	UPDATE acc_ledger_payable SET bill_transactor_id=in_to_transactor_id WHERE acc_ledger_id>0 AND bill_transactor_id=in_from_transactor_id;
	UPDATE acc_ledger_prepaid SET bill_transactor_id=in_to_transactor_id WHERE acc_ledger_id>0 AND bill_transactor_id=in_from_transactor_id;
	UPDATE acc_ledger_receivable SET bill_transactor_id=in_to_transactor_id WHERE acc_ledger_id>0 AND bill_transactor_id=in_from_transactor_id;
	
	UPDATE site SET transactor_id=in_to_transactor_id WHERE site_id>0 AND transactor_id=in_from_transactor_id;
	UPDATE stock_out SET transactor_id=in_to_transactor_id WHERE stock_out_id>0 AND transactor_id=in_from_transactor_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_upgrade_control;
DELIMITER //
CREATE PROCEDURE sp_insert_upgrade_control
(
	IN in_script_name varchar(50),
	IN in_line_no int,
	IN in_upgrade_date timestamp,
	IN in_version_no varchar(10),
	IN in_upgrade_detail varchar(250)
) 
BEGIN 
	INSERT INTO upgrade_control
	(
		script_name,
		line_no,
		upgrade_date,
		version_no,
		upgrade_detail
	) 
    VALUES
	(
		in_script_name,
		in_line_no,
		in_upgrade_date,
		in_version_no,
		in_upgrade_detail
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_save_transaction_tax_map;
DELIMITER //
CREATE PROCEDURE sp_save_transaction_tax_map
(
	IN in_transaction_tax_map_id bigint,
	IN in_transaction_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_transaction_number varchar(50),
    IN in_reference_number_tax varchar(50),
	IN in_transaction_number_tax varchar(50),
	IN in_verification_code_tax varchar(50),
	IN in_qr_code_tax varchar(1000),
	IN in_is_updated_more_than_once int,
	IN in_more_than_once_update_reconsiled int,
	IN in_fdn_ref varchar(50)
) 
BEGIN 
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	if (in_transaction_tax_map_id=0) then 
		INSERT INTO transaction_tax_map(transaction_id,transaction_type_id,transaction_reason_id,
		transaction_number,transaction_number_tax,verification_code_tax,qr_code_tax,add_date,is_updated_more_than_once,more_than_once_update_reconsiled,reference_number_tax,fdn_ref) 
		VALUES(in_transaction_id,in_transaction_type_id,in_transaction_reason_id,in_transaction_number,in_transaction_number_tax,
		in_verification_code_tax,in_qr_code_tax,@cur_sys_datetime,in_is_updated_more_than_once,in_more_than_once_update_reconsiled,in_reference_number_tax,in_fdn_ref);
	end if;

	if (in_transaction_tax_map_id>0) then 
		UPDATE transaction_tax_map SET transaction_id=in_transaction_id,transaction_type_id=in_transaction_type_id,
		transaction_reason_id=in_transaction_reason_id,transaction_number=in_transaction_number,transaction_number_tax=in_transaction_number_tax,
		verification_code_tax=in_verification_code_tax,qr_code_tax=in_qr_code_tax,is_updated_more_than_once=in_is_updated_more_than_once,
        more_than_once_update_reconsiled=in_more_than_once_update_reconsiled,reference_number_tax=in_reference_number_tax,fdn_ref=in_fdn_ref  
		WHERE transaction_tax_map_id=in_transaction_tax_map_id;
	end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_save_item_tax_map;
DELIMITER //
CREATE PROCEDURE sp_save_item_tax_map
(
	IN in_item_tax_map_id bigint,
	IN in_item_id bigint,
	IN in_item_id_tax varchar(50),
	IN in_item_code_tax varchar(50),
	IN in_is_synced int
) 
BEGIN 
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	if (in_item_tax_map_id=0) then 
		INSERT INTO item_tax_map(item_id,item_id_tax,item_code_tax,add_date,is_synced) 
		VALUES(in_item_id,in_item_id_tax,in_item_code_tax,@cur_sys_datetime,in_is_synced);
	end if;

	if (in_item_tax_map_id>0) then 
		UPDATE item_tax_map SET item_id=in_item_id,item_id_tax=in_item_id_tax,item_code_tax=in_item_code_tax,is_synced=in_is_synced 
		WHERE item_tax_map_id=in_item_tax_map_id;
	end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_snapshot_cash_balance;
DELIMITER //
CREATE PROCEDURE sp_insert_snapshot_cash_balance
(
		IN in_acc_period_id int,
		IN in_snapshot_no long,
		IN in_snapshot_date datetime,
		IN in_cdc_id varchar(20)
) 
BEGIN 
		INSERT INTO snapshot_cash_balance(snapshot_no,snapshot_date,acc_period_id,cdc_id,
		account_code,acc_child_account_id,currency_code,
		debit_amount,credit_amount,debit_amount_lc,credit_amount_lc,debit_balance,debit_balance_lc) 
		SELECT in_snapshot_no,in_snapshot_date,in_acc_period_id,in_cdc_id,
		al.account_code,al.acc_child_account_id,al.currency_code,sum(al.debit_amount) as debit_amount,
		sum(al.credit_amount) as credit_amount,sum(al.debit_amount_lc) as debit_amount_lc,sum(al.credit_amount_lc) as credit_amount_lc,
		(sum(al.debit_amount)-sum(al.credit_amount)) as  debit_balance,(sum(al.debit_amount_lc)-sum(al.credit_amount_lc)) as debit_balance_lc 
		FROM view_ledger_union_open_balances al INNER JOIN acc_child_account ac ON al.acc_child_account_id=ac.acc_child_account_id 
		WHERE al.account_code LIKE '1-00-000%' GROUP BY al.acc_child_account_id,al.currency_code;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_cash_account_for_balancing;
DELIMITER //
CREATE PROCEDURE sp_search_cash_account_for_balancing
(
	IN in_balancing_date1 date,
	IN in_balancing_date2 date
) 
BEGIN  
	SELECT 
	0 as cash_balancing_daily_id,0 as balancing_user_id,0 as cash_balance,0 as actual_cash_count,0 as cash_over,
	0 as cash_short,0 as add_user_detail_id,0 as edit_user_detail_id,
	balancing_date,acc_child_account_id,currency_code,NULL as add_date,NULL as edit_date,
	GROUP_CONCAT(DISTINCT if(category = 'cash_begin', cash_begin, '') SEPARATOR '') AS 'cash_begin',
	GROUP_CONCAT(DISTINCT if(category = 'cash_receipts', cash_receipts, '') SEPARATOR '') AS 'cash_receipts',
	GROUP_CONCAT(DISTINCT if(category = 'cash_payments', cash_payments, '') SEPARATOR '') AS 'cash_payments',
	GROUP_CONCAT(DISTINCT if(category = 'cash_transfer_in', cash_transfer_in, '') SEPARATOR '') AS 'cash_transfer_in',
	GROUP_CONCAT(DISTINCT if(category = 'cash_transfer_out', cash_transfer_out, '') SEPARATOR '') AS 'cash_transfer_out',
	GROUP_CONCAT(DISTINCT if(category = 'cash_adjustment_pos', cash_adjustment_pos, '') SEPARATOR '') AS 'cash_adjustment_pos',
	GROUP_CONCAT(DISTINCT if(category = 'cash_adjustment_neg', cash_adjustment_neg, '') SEPARATOR '') AS 'cash_adjustment_neg' 
	FROM 
	(
		SELECT 'cash_receipts' as category,pay_date as balancing_date,acc_child_account_id,currency_code,
		0 as cash_begin,sum(paid_amount) as cash_receipts,0 as cash_payments,0 as cash_transfer_in,0 as cash_transfer_out,0 as cash_adjustment_pos,0 as cash_adjustment_neg 
		FROM pay WHERE pay_type_id=14 AND pay_method_id!=6 AND pay_date between in_balancing_date1 AND in_balancing_date2 
		GROUP BY pay_date,acc_child_account_id,currency_code 
		UNION 
		SELECT 'cash_payments' as category,pay_date as balancing_date,acc_child_account_id,currency_code,
		0 as cash_begin,0 as cash_receipts,sum(paid_amount) as cash_payments,0 as cash_transfer_in,0 as cash_transfer_out,0 as cash_adjustment_pos,0 as cash_adjustment_neg 
		FROM pay WHERE pay_type_id=15 AND pay_method_id!=7 AND pay_date between in_balancing_date1 AND in_balancing_date2 
		GROUP BY pay_date,acc_child_account_id,currency_code 
		UNION 
		select 'cash_transfer_in' as category,journal_date as balancing_date,acc_child_account_id,currency_code,
		0 as cash_begin,0 as cash_receipts,0 as cash_payments,sum(debit_amount) as cash_transfer_in,0 as cash_transfer_out,0 as cash_adjustment_pos,0 as cash_adjustment_neg  
		from acc_journal 
		where transaction_type_id=18 and transaction_reason_id=41 and debit_amount>0 AND journal_date between in_balancing_date1 AND in_balancing_date2  
		group by journal_date,acc_child_account_id,currency_code 
		UNION 
		select 'cash_transfer_out' as category,journal_date as balancing_date,acc_child_account_id,currency_code,
		0 as cash_begin,0 as cash_receipts,0 as cash_payments,0 as cash_transfer_in,sum(credit_amount) as cash_transfer_out,0 as cash_adjustment_pos,0 as cash_adjustment_neg 
		from acc_journal 
		where transaction_type_id=18 and transaction_reason_id=41 and credit_amount>0 AND journal_date between in_balancing_date1 AND in_balancing_date2   
		group by journal_date,acc_child_account_id,currency_code 
		UNION 
		select 'cash_adjustment_pos' as category,journal_date as balancing_date,acc_child_account_id,currency_code,
		0 as cash_begin,0 as cash_receipts,0 as cash_payments,0 as cash_transfer_in,0 as cash_transfer_out,sum(debit_amount) as cash_adjustment_pos,0 as cash_adjustment_neg 
		from acc_journal 
		where transaction_type_id=75 and transaction_reason_id=116 and debit_amount>0 AND journal_date between in_balancing_date1 AND in_balancing_date2   
		group by journal_date,acc_child_account_id,currency_code 
		UNION 
		select 'cash_adjustment_neg' as category,journal_date as balancing_date,acc_child_account_id,currency_code,
		0 as cash_begin,0 as cash_receipts,0 as cash_payments,0 as cash_transfer_in,0 as cash_transfer_out,0 as cash_adjustment_pos,sum(credit_amount) as cash_adjustment_neg   
		from acc_journal 
		where transaction_type_id=75 and transaction_reason_id=116 and credit_amount>0 AND journal_date between in_balancing_date1 AND in_balancing_date2   
		group by journal_date,acc_child_account_id,currency_code 
		UNION 
		select 'cash_begin' as category,cast(snapshot_date as date) as balancing_date,acc_child_account_id,currency_code,
		debit_balance as cash_begin,0 as cash_receipts,0 as cash_payments,0 as cash_transfer_in,0 as cash_transfer_out,0 as cash_adjustment_pos,0 as cash_adjustment_neg 
		from snapshot_cash_balance where cast(snapshot_date as date) between in_balancing_date1 AND in_balancing_date2
	) as d 
	GROUP BY balancing_date,acc_child_account_id,currency_code 
	ORDER BY balancing_date DESC,acc_child_account_id,currency_code;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_save_cash_balancing_daily;
DELIMITER //
CREATE PROCEDURE sp_save_cash_balancing_daily
(
	IN in_cash_balancing_daily_id bigint(20),
  	IN in_balancing_date date,
  	IN in_acc_child_account_id int,
  	IN in_currency_code varchar(10),
  	IN in_cash_begin double,
  	IN in_cash_transfer_in double,
  	IN in_cash_adjustment_pos double,
  	IN in_cash_receipts double,
  	IN in_cash_transfer_out double,
  	IN in_cash_adjustment_neg double,
  	IN in_cash_payments double,
  	IN in_cash_balance double,
  	IN in_actual_cash_count double,
  	IN in_cash_over double, 
  	IN in_cash_short double,
  	IN in_user_detail_id int
) 
BEGIN 
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	if (in_cash_balancing_daily_id=0) then 
		INSERT INTO cash_balancing_daily(balancing_date,acc_child_account_id,currency_code,cash_begin,cash_transfer_in,
		cash_adjustment_pos,cash_receipts,cash_transfer_out,cash_adjustment_neg,cash_payments,cash_balance,actual_cash_count,
		cash_over,cash_short,add_user_detail_id,add_date) 
		VALUES(in_balancing_date,in_acc_child_account_id,in_currency_code,in_cash_begin,in_cash_transfer_in,
		in_cash_adjustment_pos,in_cash_receipts,in_cash_transfer_out,in_cash_adjustment_neg,in_cash_payments,in_cash_balance,in_actual_cash_count,
		in_cash_over,in_cash_short,in_user_detail_id,@cur_sys_datetime);
	end if;

	if (in_cash_balancing_daily_id>0) then 
		UPDATE cash_balancing_daily SET cash_begin=in_cash_begin,cash_transfer_in=in_cash_transfer_in,
		cash_adjustment_pos=in_cash_adjustment_pos,cash_receipts=in_cash_receipts,cash_transfer_out=in_cash_transfer_out,cash_adjustment_neg=in_cash_adjustment_neg,
		cash_payments=in_cash_payments,cash_balance=in_cash_balance,actual_cash_count=in_actual_cash_count,
		cash_over=in_cash_over,cash_short=in_cash_short,edit_user_detail_id=in_user_detail_id,edit_date=@cur_sys_datetime 
		WHERE cash_balancing_daily_id=in_cash_balancing_daily_id;
	end if;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_cr_dr_note;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_cr_dr_note
(
	IN in_transaction_date date,
	IN in_store_id int,
	IN in_store2_id int,
	IN in_transactor_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_cash_discount double,
	IN in_total_vat double,
	IN in_transaction_comment varchar(255),
	IN in_add_user_detail_id int,
	IN in_add_date datetime,
	IN in_edit_user_detail_id int,
	IN in_edit_date datetime,
	IN in_transaction_ref varchar(100),
	OUT out_transaction_id bigint,
	IN in_sub_total double,
	IN in_grand_total double,
	IN in_total_trade_discount double,
	IN in_points_awarded double,
	IN in_card_number varchar(10),
	IN in_total_std_vatable_amount double,
	IN in_total_zero_vatable_amount double,
	IN in_total_exempt_vatable_amount double,
	IN in_vat_perc double,
	IN in_amount_tendered double,
	IN in_change_amount double,
	IN in_is_cash_discount_vat_liable varchar(3),
	IN in_total_profit_margin double,
	IN in_transaction_user_detail_id int,
	IN in_bill_transactor_id bigint,
	IN in_scheme_transactor_id bigint,
	IN in_princ_scheme_member varchar(100),
	IN in_scheme_card_number varchar(100),
	IN in_transaction_number varchar(50),
	IN in_delivery_date date,
	IN in_delivery_address varchar(250),
	IN in_pay_terms varchar(250),
	IN in_terms_conditions varchar(250),
	IN in_authorised_by_user_detail_id int,
	IN in_authorise_date date,
	IN in_pay_due_date date,
	IN in_expiry_date date,
	IN in_acc_child_account_id int,
	IN in_currency_code varchar(10),
	IN in_xrate double,
	IN in_from_date date,
	IN in_to_date date,
	IN in_duration_type  varchar(20),
	IN in_site_id bigint,
	IN in_transactor_rep  varchar(50),
	IN in_transactor_vehicle  varchar(20),
	IN in_transactor_driver  varchar(50),
	IN in_duration_value double,
	IN in_location_id bigint,
	IN in_status_code  varchar(20),
	IN in_status_date datetime,
	IN in_delivery_mode varchar(20),
	IN in_is_processed int,
	IN in_is_paid int,
	IN in_is_cancel int,
    In in_spent_points_amount double,
	IN in_mode_code int
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("transaction_cr_dr_note","transaction_id",@new_id);
	
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @edit_datetime=null;
	SET @edit_user_detail_id=null;
	
	SET @store2_id=NULL;
	if (in_store2_id!=0) then
		set @store2_id=in_store2_id;
	end if;

	SET @transactor_id=NULL;
	if (in_transactor_id!=0) then
		set @transactor_id=in_transactor_id;
	end if;

	SET @transaction_user_detail_id=NULL;
	if (in_transaction_user_detail_id!=0) then
		set @transaction_user_detail_id=in_transaction_user_detail_id;
	end if;

	SET @bill_transactor_id=NULL;
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;

	SET @scheme_transactor_id=NULL;
	if (in_scheme_transactor_id!=0) then
		set @scheme_transactor_id=in_scheme_transactor_id;
	end if;

	SET @scheme_transactor_id=NULL;
	if (in_scheme_transactor_id!=0) then
		set @scheme_transactor_id=in_scheme_transactor_id;
	end if;

	SET @authorised_by_user_detail_id=NULL;
	if (in_authorised_by_user_detail_id!=0) then
		set @authorised_by_user_detail_id=in_authorised_by_user_detail_id;
	end if;

	SET @transaction_number=@new_id;
	if (in_transaction_number!='') then
		set @transaction_number=in_transaction_number;
	end if;

	SET @acc_child_account_id=NULL;
	if (in_acc_child_account_id!=0) then
		set @acc_child_account_id=in_acc_child_account_id;
	end if;

	SET @location_id=NULL;
	if (in_location_id!=0) then
		set @location_id=in_location_id;
	end if;

	INSERT INTO transaction_cr_dr_note
	(
		transaction_id,
		transaction_date,
		store_id,
		store2_id,
		transactor_id,
		transaction_type_id,
		transaction_reason_id,
		cash_discount,
		total_vat,
		transaction_comment,
		add_user_detail_id,
		add_date,
		edit_user_detail_id,
		edit_date,
		transaction_ref,
		sub_total,
		grand_total,
		total_trade_discount,
		points_awarded,
		card_number,
		total_std_vatable_amount,
		total_zero_vatable_amount,
		total_exempt_vatable_amount,
		vat_perc,
		amount_tendered,
		change_amount,
		is_cash_discount_vat_liable,
		total_profit_margin,
		transaction_user_detail_id,
		bill_transactor_id,
		scheme_transactor_id,
		princ_scheme_member,
		scheme_card_number,
		transaction_number,
		delivery_date,
		delivery_address,
		pay_terms,
		terms_conditions,
		authorised_by_user_detail_id,
		authorise_date,
		pay_due_date,
		expiry_date,
		acc_child_account_id,
		currency_code,
		xrate,
		from_date,
		to_date,
		duration_type,
		site_id,
		transactor_rep,
		transactor_vehicle,
		transactor_driver,
		duration_value,
		location_id,
		status_code,
		status_date,
		delivery_mode,
		is_processed,
		is_paid,
		is_cancel,
        spent_points_amount,
        mode_code
	) 
    VALUES
	(
		@new_id,
		in_transaction_date,
		in_store_id,
		@store2_id,
		@transactor_id,
		in_transaction_type_id,
		in_transaction_reason_id,
		in_cash_discount,
		in_total_vat,
		in_transaction_comment,
		in_add_user_detail_id,
		@cur_sys_datetime,
		@edit_user_detail_id,
		@edit_datetime,
		in_transaction_ref,
		in_sub_total,
		in_grand_total,
		in_total_trade_discount,
		in_points_awarded,
		in_card_number,
		in_total_std_vatable_amount,
		in_total_zero_vatable_amount,
		in_total_exempt_vatable_amount,
		in_vat_perc,
		in_amount_tendered,
		in_change_amount,
		in_is_cash_discount_vat_liable,
		in_total_profit_margin,
		@transaction_user_detail_id,
		@bill_transactor_id,
		@scheme_transactor_id,
		in_princ_scheme_member,
		in_scheme_card_number,
		@transaction_number,
		in_delivery_date,
		in_delivery_address,
		in_pay_terms,
		in_terms_conditions,
		@authorised_by_user_detail_id,
		in_authorise_date,
		in_pay_due_date,
		in_expiry_date,
		@acc_child_account_id,
		in_currency_code,
		in_xrate,
		in_from_date,
		in_to_date,
		in_duration_type,
		in_site_id,
		in_transactor_rep,
		in_transactor_vehicle,
		in_transactor_driver,
		in_duration_value,
		@location_id,
		in_status_code,
		in_status_date,
		in_delivery_mode,
		in_is_processed,
		in_is_paid,
		in_is_cancel,
        in_spent_points_amount,
        in_mode_code
	); 
SET out_transaction_id=@new_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_transaction_cr_dr_note;
DELIMITER //
CREATE PROCEDURE sp_update_transaction_cr_dr_note
(
	IN in_transaction_id bigint,
	IN in_transaction_date date,
	IN in_store_id int,
	IN in_store2_id int,
	IN in_transactor_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
	IN in_cash_discount double,
	IN in_total_vat double,
	IN in_transaction_comment varchar(255),
	IN in_edit_user_detail_id int,
	IN in_transaction_ref varchar(100),
	IN in_sub_total double,
	IN in_grand_total double,
	IN in_total_trade_discount double,
	IN in_points_awarded double,
	IN in_card_number varchar(10),
	IN in_total_std_vatable_amount double,
	IN in_total_zero_vatable_amount double,
	IN in_total_exempt_vatable_amount double,
	IN in_vat_perc double,
	IN in_amount_tendered double,
	IN in_change_amount double, 
	IN in_is_cash_discount_vat_liable varchar(3),
	IN in_total_profit_margin double,
	IN in_transaction_user_detail_id int,
	IN in_bill_transactor_id bigint,
	IN in_scheme_transactor_id bigint,
	IN in_princ_scheme_member varchar(100),
	IN in_scheme_card_number varchar(100),
	IN in_transaction_number varchar(50),
	IN in_delivery_date date,
	IN in_delivery_address varchar(250),
	IN in_pay_terms varchar(250),
	IN in_terms_conditions varchar(250),
	IN in_authorised_by_user_detail_id int,
	IN in_authorise_date date,
	IN in_pay_due_date date,
	IN in_expiry_date date,
	IN in_from_date date,
	IN in_to_date date,
	IN in_duration_type  varchar(20),
	IN in_site_id bigint,
	IN in_transactor_rep  varchar(50),
	IN in_transactor_vehicle  varchar(20),
	IN in_transactor_driver  varchar(50),
	IN in_duration_value double,
	IN in_location_id bigint,
	IN in_status_code  varchar(20),
	IN in_status_date datetime,
	IN in_delivery_mode varchar(20),
	IN in_is_processed int,
	IN in_is_paid int,
	IN in_is_cancel int,
    IN in_spent_points_amount double,
    IN in_mode_code int
) 
BEGIN 

	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @store2_id=NULL;
	if (in_store2_id!=0) then
		set @store2_id=in_store2_id;
	end if;

	SET @transactor_id=NULL;
	if (in_transactor_id!=0) then
		set @transactor_id=in_transactor_id;
	end if;

	SET @transaction_user_detail_id=NULL;
	if (in_transaction_user_detail_id!=0) then
		set @transaction_user_detail_id=in_transaction_user_detail_id;
	end if;

	SET @bill_transactor_id=NULL;
	if (in_bill_transactor_id!=0) then
		set @bill_transactor_id=in_bill_transactor_id;
	end if;

	SET @scheme_transactor_id=NULL;
	if (in_scheme_transactor_id!=0) then
		set @scheme_transactor_id=in_scheme_transactor_id;
	end if;

	SET @authorised_by_user_detail_id=NULL;
	if (in_authorised_by_user_detail_id!=0) then
		set @authorised_by_user_detail_id=in_authorised_by_user_detail_id;
	end if;

	SET @location_id=NULL;
	if (in_location_id!=0) then
		set @location_id=in_location_id;
	end if;

	UPDATE transaction_cr_dr_note SET 
		transaction_date=in_transaction_date,
		store_id=in_store_id,
		store2_id=@store2_id,
		transactor_id=@transactor_id,
		transaction_type_id=in_transaction_type_id,
		transaction_reason_id=in_transaction_reason_id,
		cash_discount=in_cash_discount,
		total_vat=in_total_vat,
		transaction_comment=in_transaction_comment,
		edit_user_detail_id=in_edit_user_detail_id,
		edit_date=@cur_sys_datetime,
		transaction_ref=in_transaction_ref,
		sub_total=in_sub_total,
		grand_total=in_grand_total,
		total_trade_discount=in_total_trade_discount,
		points_awarded=in_points_awarded,
		card_number=in_card_number,
		total_std_vatable_amount=in_total_std_vatable_amount,
		total_zero_vatable_amount=in_total_zero_vatable_amount,
		total_exempt_vatable_amount=in_total_exempt_vatable_amount,
		in_vat_perc=in_vat_perc,
		amount_tendered=in_amount_tendered,
		change_amount=in_change_amount,
		is_cash_discount_vat_liable=in_is_cash_discount_vat_liable,
		total_profit_margin=in_total_profit_margin,
		transaction_user_detail_id=in_transaction_user_detail_id,
		bill_transactor_id=@bill_transactor_id,
		scheme_transactor_id=@scheme_transactor_id,
		princ_scheme_member=in_princ_scheme_member,
		scheme_card_number=in_scheme_card_number,
		transaction_number=in_transaction_number,
		delivery_date=in_delivery_date,
		delivery_address=in_delivery_address,
		pay_terms=in_pay_terms,
		terms_conditions=in_terms_conditions,
		authorised_by_user_detail_id=@authorised_by_user_detail_id,
		authorise_date=in_authorise_date,
		pay_due_date=in_pay_due_date,
		expiry_date=in_expiry_date,
		from_date=in_from_date,
		to_date=in_to_date,
		duration_type=in_duration_type,
		site_id=in_site_id,
		transactor_rep=in_transactor_rep,
		transactor_vehicle=in_transactor_vehicle,
		transactor_driver=in_transactor_driver,
		duration_value=in_duration_value,
		location_id=@location_id,
		status_code=in_status_code,
		status_date=in_status_date,
		delivery_mode=in_delivery_mode,
		is_processed=in_is_processed,
		is_paid=in_is_paid,
		is_cancel=in_is_cancel,
        spent_points_amount=in_spent_points_amount,
        mode_code=in_mode_code 
	WHERE transaction_id=in_transaction_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_item_cr_dr_note;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_item_cr_dr_note
(
	IN in_transaction_id bigint,
	IN in_item_id bigint,
	IN in_batchno varchar(100),
	IN in_item_qty double,
	IN in_unit_price double,
	IN in_unit_trade_discount double,
	IN in_unit_vat double,
	IN in_amount double,
	IN in_item_expiry_date date,
	IN in_item_mnf_date date,
	IN in_vat_rated varchar(50),
	IN in_vat_perc double,
	IN in_unit_price_inc_vat double,
	IN in_unit_price_exc_vat double,
	IN in_amount_inc_vat double,
	IN in_amount_exc_vat double,
	IN in_stock_effect varchar(1),
	IN in_is_trade_discount_vat_liable varchar(3),
	IN in_unit_cost_price double,
	IN in_unit_profit_margin double,
	IN in_earn_perc double,
	IN in_earn_amount double,
	IN in_code_specific varchar(250),
	IN in_desc_specific varchar(250),
	IN in_desc_more varchar(250),
	IN in_warranty_desc varchar(150),
	IN in_warranty_expiry_date date,
	IN in_account_code varchar(20),
	IN in_purchase_date date,
	IN in_dep_start_date date,
	IN in_dep_method_id int,
	IN in_dep_rate double,
	IN in_average_method_id int,
	IN in_effective_life int,
	IN in_residual_value double,
	IN in_narration varchar(100),
	IN in_qty_balance double,
	IN in_duration_value double,
	IN in_qty_damage double,
	IN in_duration_passed double,
	IN in_specific_size double,
    OUT out_transaction_item_id bigint
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("transaction_item_cr_dr_note","transaction_item_id",@new_id);

	SET @item_expiry_date=NULL;
	if (in_item_expiry_date is not null) then
		set @item_expiry_date=in_item_expiry_date;
	end if;

	SET @item_mnf_date=NULL;
	if (in_item_mnf_date is not null) then
		set @item_mnf_date=in_item_mnf_date;
	end if;

	SET @in_batchno='';
	if (in_batchno is not null) then
		set @in_batchno=in_batchno;
	end if;
	SET @code_specific='';
	if (in_code_specific is not null) then
		set @code_specific=in_code_specific;
	end if;
	SET @desc_specific='';
	if (in_desc_specific is not null) then
		set @desc_specific=in_desc_specific;
	end if;
	SET @desc_more=null;
	if (in_desc_more!='' and in_desc_more is not null) then
		set @desc_more=in_desc_more;
	end if;
	SET @warranty_desc=null;
	if (in_warranty_desc!="" and in_warranty_desc is not null) then
		set @warranty_desc=in_warranty_desc;
	end if;
	SET @warranty_expiry_date=NULL;
	if (in_warranty_expiry_date is not null) then
		set @warranty_expiry_date=in_warranty_expiry_date;
	end if;
	SET @account_code=null;
	if (in_account_code!='' and in_account_code is not null) then
		set @account_code=in_account_code;
	end if;
		SET @in_purchase_date=NULL;
		if (in_purchase_date is not null) then
			set @in_purchase_date=in_purchase_date;
		end if;
		SET @in_dep_start_date=NULL;
		if (in_dep_start_date is not null) then
			set @in_dep_start_date=in_dep_start_date;
		end if;
		SET @in_dep_method_id=NULL;
		if (in_dep_method_id!=0) then
			set @in_dep_method_id=in_dep_method_id;
		end if;
		SET @in_average_method_id=NULL;
		if (in_average_method_id!=0) then
			set @in_average_method_id=in_average_method_id;
		end if;
		SET @in_item_id=NULL;
		if (in_item_id!=0) then
			set @in_item_id=in_item_id;
		end if;
		SET @in_specific_size=1;
		if (in_specific_size!=0) then
			set @in_specific_size=in_specific_size;
		end if;

	INSERT INTO transaction_item_cr_dr_note
	(
		transaction_item_id,
		transaction_id,
		item_id,
		batchno,
		item_qty,
		unit_price,
		unit_trade_discount,
		unit_vat,
		amount,
		item_expiry_date,
		item_mnf_date,
		vat_rated,
		vat_perc,
		unit_price_inc_vat,
		unit_price_exc_vat,
		amount_inc_vat,
		amount_exc_vat,
		stock_effect,
		is_trade_discount_vat_liable,
		unit_cost_price,
		unit_profit_margin,
		earn_perc,
		earn_amount,
		code_specific,
		desc_specific,
		desc_more,
		warranty_desc,
		warranty_expiry_date,
		account_code,
		purchase_date,
		dep_start_date,
		dep_method_id,
		dep_rate,
		average_method_id,
		effective_life,
		residual_value,
		narration,
		qty_balance,
		duration_value,
		qty_damage,
		duration_passed,
		specific_size
	) 
    VALUES
	(
		@new_id,
		in_transaction_id,
		@in_item_id,
		@in_batchno,
		in_item_qty,
		in_unit_price,
		in_unit_trade_discount,
		in_unit_vat,
		in_amount,
		@item_expiry_date,
		@item_mnf_date,
		in_vat_rated,
		in_vat_perc,
		in_unit_price_inc_vat,
		in_unit_price_exc_vat,
		in_amount_inc_vat,
		in_amount_exc_vat,
		in_stock_effect,
		in_is_trade_discount_vat_liable,
		in_unit_cost_price,
		in_unit_profit_margin,
		in_earn_perc,
		in_earn_amount,
		@code_specific,
		@desc_specific,
		@desc_more,
		@warranty_desc,
		@warranty_expiry_date,
		@account_code,
		@in_purchase_date,
		@in_dep_start_date,
		@in_dep_method_id,
		in_dep_rate,
		@in_average_method_id,
		in_effective_life,
		in_residual_value,
		in_narration,
		in_qty_balance,
		in_duration_value,
		in_qty_damage,
		in_duration_passed,
		@in_specific_size
	); 
	SET out_transaction_item_id=@new_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_transaction_item_cr_dr_note;
DELIMITER //
CREATE PROCEDURE sp_update_transaction_item_cr_dr_note
(
	IN in_transaction_item_id bigint,
	IN in_transaction_id bigint,
	IN in_item_id bigint,
	IN in_batchno varchar(100),
	IN in_item_qty double,
	IN in_unit_price double,
	IN in_unit_trade_discount double,
	IN in_unit_vat double,
	IN in_amount double,
	IN in_item_expry_date date,
	IN in_item_mnf_date date,
	IN in_vat_rated varchar(50),
	IN in_vat_perc double,
	IN in_unit_price_inc_vat double,
	IN in_unit_price_exc_vat double,
	IN in_amount_inc_vat double,
	IN in_amount_exc_vat double,
	IN in_stock_effect varchar(1),
	IN in_is_trade_discount_vat_liable varchar(3),
	IN in_unit_cost_price double,
	IN in_unit_profit_margin double,
	IN in_earn_perc double,
	IN in_earn_amount double,
	IN in_code_specific varchar(250),
	IN in_desc_specific varchar(250),
	IN in_desc_more varchar(250),
	IN in_warranty_desc varchar(150),
	IN in_warranty_expiry_date date,
	IN in_account_code varchar(20),
	IN in_purchase_date date,
	IN in_dep_start_date date,
	IN in_dep_method_id int,
	IN in_dep_rate double,
	IN in_average_method_id int,
	IN in_effective_life int,
	IN in_residual_value double,
	IN in_narration varchar(100),
	IN in_qty_balance double,
	IN in_duration_value double,
	IN in_qty_damage double,
	IN in_duration_passed double,
	IN in_specific_size double
) 
BEGIN 
	SET @item_expiry_date=NULL;
	if (in_item_expry_date is not null) then
		set @item_expiry_date=in_item_expry_date;
	end if;

	SET @item_mnf_date=NULL;
	if (in_item_mnf_date is not null) then
		set @item_mnf_date=in_item_mnf_date;
	end if;

	SET @in_batchno='';
	if (in_batchno is not null) then
		set @in_batchno=in_batchno;
	end if;
	SET @code_specific='';
	if (in_code_specific is not null) then
		set @code_specific=in_code_specific;
	end if;
	SET @desc_specific='';
	if (in_desc_specific is not null) then
		set @desc_specific=in_desc_specific;
	end if;
	SET @desc_more=null;
	if (in_desc_more!='' and in_desc_more is not null) then
		set @desc_more=in_desc_more;
	end if;
	SET @warranty_desc=null;
	if (in_warranty_desc!="" and in_warranty_desc is not null) then
		set @warranty_desc=in_warranty_desc;
	end if;
	SET @warranty_expiry_date=NULL;
	if (in_warranty_expiry_date is not null) then
		set @warranty_expiry_date=in_warranty_expiry_date;
	end if;
	SET @account_code=null;
	if (in_account_code!='' and in_account_code is not null) then
		set @account_code=in_account_code;
	end if;
		SET @in_purchase_date=NULL;
		if (in_purchase_date is not null) then
			set @in_purchase_date=in_purchase_date;
		end if;
		SET @in_dep_start_date=NULL;
		if (in_dep_start_date is not null) then
			set @in_dep_start_date=in_dep_start_date;
		end if;
		SET @in_dep_method_id=NULL;
		if (in_dep_method_id!=0) then
			set @in_dep_method_id=in_dep_method_id;
		end if;
		SET @in_average_method_id=NULL;
		if (in_average_method_id!=0) then
			set @in_average_method_id=in_average_method_id;
		end if;
		SET @in_item_id=NULL;
		if (in_item_id!=0) then
			set @in_item_id=in_item_id;
		end if;
		SET @in_specific_size=1;
		if (in_specific_size!=0) then
			set @in_specific_size=in_specific_size;
		end if;

	UPDATE transaction_item_cr_dr_note SET 
		transaction_id=in_transaction_id,
		item_id=@in_item_id,
		batchno=@in_batchno,
		item_qty=in_item_qty,
		unit_price=in_unit_price,
		unit_trade_discount=in_unit_trade_discount,
		unit_vat=in_unit_vat,
		amount=in_amount,
		item_expry_date=@item_expry_date,
		item_mnf_date=@item_mnf_date,
		vat_rated=in_vat_rated,
		vat_perc=in_vat_perc,
		unit_price_inc_vat =in_unit_price_inc_vat,
		unit_price_exc_vat =in_unit_price_exc_vat,
		amount_inc_vat =in_amount_inc_vat,
		amount_exc_vat =in_amount_exc_vat,
		stock_effect=in_stock_effect,
		is_trade_discount_vat_liable=in_is_trade_discount_vat_liable,
		unit_cost_price=in_unit_cost_price,
		unit_profit_margin=in_unit_profit_margin,
		earn_perc=in_earn_perc,
		earn_amount=in_earn_amount,
		code_specific=@code_specific,
		desc_specific=@desc_specific,
		desc_more=@desc_more,
		warranty_desc=@warranty_desc,
		warranty_expiry_date=@warranty_expiry_date,
		account_code=@account_code,
		purchase_date=@in_purchase_date,
		dep_start_date=@in_dep_start_date,
		dep_method_id=@in_dep_method_id,
		dep_rate=in_dep_rate,
		average_method_id=@in_average_method_id,
		effective_life=in_effective_life,
		residual_value=in_residual_value,
		narration=in_narration,
		qty_balance=in_qty_balance,
		duration_value=in_duration_value,
		qty_damage=in_qty_damage,
		duration_passed=in_duration_passed,
		specific_size=@in_specific_size 
	WHERE transaction_item_id=in_transaction_item_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_by_id_cr_dr_note;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_by_id_cr_dr_note
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT * FROM transaction_cr_dr_note t 
		WHERE t.transaction_id=in_transaction_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_item_by_transaction_id_cr_dr_note;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_item_by_transaction_id_cr_dr_note
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT ti.*,tiu.unit_id,tiu.base_unit_qty FROM transaction_item_cr_dr_note ti 
        INNER JOIN transaction_item_cr_dr_note_unit tiu ON ti.transaction_item_id=tiu.transaction_item_id 
		WHERE ti.transaction_id=in_transaction_id ORDER BY ti.transaction_item_id ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_item_cr_dr_note;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_item_cr_dr_note
(
	IN in_transaction_item_id bigint 
) 
BEGIN 
		SELECT ti.*,tiu.unit_id,tiu.base_unit_qty FROM transaction_item_cr_dr_note ti 
        INNER JOIN transaction_item_cr_dr_note_unit tiu ON ti.transaction_item_id=tiu.transaction_item_id 
		WHERE ti.transaction_item_id=in_transaction_item_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_create_stock_ledger_monthly_tables;
DELIMITER //
CREATE PROCEDURE sp_create_stock_ledger_monthly_tables()
BEGIN 
	DECLARE finished INTEGER DEFAULT 0;
	DECLARE TableName varchar(30) DEFAULT "";
	-- declare cursor for distinct year and month
	DEClARE curTableName CURSOR FOR select CONCAT('stock_ledger_',year(add_date),'_',LPAD(month(add_date), 2, 0)) from stock_ledger group by year(add_date),month(add_date);
	-- declare NOT FOUND handler
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;
	OPEN curTableName;
	getTableName: LOOP
		FETCH curTableName INTO TableName;
		IF finished = 1 THEN 
			LEAVE getTableName;
		END IF;
		-- create table
		set @sql_text = CONCAT("CREATE TABLE ",TableName," AS select * from stock_ledger where year(add_date)=SUBSTRING('",TableName,"',14, 4) and LPAD(month(add_date), 2, 0)=SUBSTRING('",TableName,"',19, 2)");
		-- select @sql_text;
        PREPARE stmt FROM @sql_text;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
		-- add primary key
		set @sql_text2 = CONCAT("ALTER TABLE ",TableName," ADD PRIMARY KEY (stock_ledger_id)");
        PREPARE stmt2 FROM @sql_text2;
		EXECUTE stmt2;
		DEALLOCATE PREPARE stmt2;
	END LOOP getTableName;
	CLOSE curTableName;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_today_monthly_stock_ledger_table;
DELIMITER //
CREATE PROCEDURE sp_get_today_monthly_stock_ledger_table()
BEGIN 
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);
	SELECT CONCAT('stock_ledger_',year(@cur_sys_datetime),'_',LPAD(month(@cur_sys_datetime), 2, 0)) as TableName;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_create_today_monthly_stock_ledger_table;
DELIMITER //
CREATE PROCEDURE sp_create_today_monthly_stock_ledger_table(IN in_TableName varchar(30))
BEGIN 
		-- create table
		set @sql_text = CONCAT("CREATE TABLE ",in_TableName," AS select * from stock_ledger where 1=2");
        PREPARE stmt FROM @sql_text;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
		-- add primary key
		set @sql_text2 = CONCAT("ALTER TABLE ",in_TableName," ADD PRIMARY KEY (stock_ledger_id)");
        PREPARE stmt2 FROM @sql_text2;
		EXECUTE stmt2;
		DEALLOCATE PREPARE stmt2;
        -- add auto increment
		set @sql_text3 = CONCAT("ALTER TABLE ",in_TableName," CHANGE COLUMN stock_ledger_id stock_ledger_id BIGINT(20) NOT NULL AUTO_INCREMENT");
        PREPARE stmt3 FROM @sql_text3;
		EXECUTE stmt3;
		DEALLOCATE PREPARE stmt3;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_delete_snapshot_stock_value_below_last_month;
DELIMITER //
CREATE PROCEDURE sp_delete_snapshot_stock_value_below_last_month(IN in_current_date date)
BEGIN 
    SET @cur_month=month(in_current_date);
    SET @start_date='';
    if (@cur_month=1) then
		set @start_date=CONCAT(year(in_current_date)-1,'-','12-01 00:00');
	else
		set @start_date=CONCAT(year(in_current_date),'-',month(in_current_date)-1,'-01 00:00');
	end if;
    DELETE FROM snapshot_stock_value WHERE snapshot_stock_value_id>1 and snapshot_date<@start_date and 
    snapshot_no NOT IN (
		select sn from (select y,m,max(snapshot_no) as sn from snapshot_stock_value_day_sum group by y,m) as t2
	);
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_cr_dr;
DELIMITER //
CREATE PROCEDURE sp_search_cr_dr
(
	IN in_transaction_number varchar(50)
) 
BEGIN 
	SELECT ifnull((select group_concat(concat(t2.transaction_id,',',t2.transaction_comment) separator ':') from transaction_cr_dr_note t2 where t2.transaction_ref=in_transaction_number),'') as cr_dr_flag;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_subscription_category;
DELIMITER //
CREATE PROCEDURE sp_insert_subscription_category
(
	IN in_category_name varchar(50),
	IN in_category_code varchar(50)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("subscription_category","subscription_category_id",@new_id);
	INSERT INTO subscription_category
	(
		subscription_category_id,
		category_name,
		category_code
	) 
    VALUES
	(
		@new_id,
		in_category_name,
		in_category_code
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_subscription_category;
DELIMITER //
CREATE PROCEDURE sp_update_subscription_category
(
	IN in_subscription_category_id int,
	IN in_category_name varchar(50),
	IN in_category_code varchar(50)
) 
BEGIN 
	UPDATE subscription_category SET 
		category_name=in_category_name,
		category_code=in_category_code
	WHERE subscription_category_id=in_subscription_category_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_subscription_category_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_subscription_category_by_id
(
	IN in_subscription_category_id int
) 
BEGIN 
	SELECT * FROM subscription_category 
	WHERE subscription_category_id=in_subscription_category_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_subscription_category_by_name;
DELIMITER //
CREATE PROCEDURE sp_search_subscription_category_by_name
(
	IN in_category_name varchar(50)
) 
BEGIN 
	SELECT * FROM subscription_category 
	WHERE category_name LIKE concat('%',in_category_name,'%') OR category_code LIKE concat('%',in_category_name,'%')  
	ORDER BY category_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_subscription_category_by_exact_name;
DELIMITER //
CREATE PROCEDURE sp_search_subscription_category_by_exact_name
(
	IN in_category_name varchar(50)
) 
BEGIN 
	SELECT * FROM subscription_category 
	WHERE category_name = in_category_name 
	ORDER BY category_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_subscription_category_by_code;
DELIMITER //
CREATE PROCEDURE sp_search_subscription_category_by_code
(
	IN in_category_code varchar(50)
) 
BEGIN 
	SELECT * FROM subscription_category 
	WHERE category_code = in_category_code  
	ORDER BY category_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_subscription_category_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_subscription_category_by_none() 
BEGIN 
	SELECT * FROM subscription_category 
	ORDER BY category_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_business_category;
DELIMITER //
CREATE PROCEDURE sp_insert_business_category
(
	IN in_category_name varchar(50)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("business_category","business_category_id",@new_id);
	INSERT INTO business_category
	(
		business_category_id,
		category_name
	) 
    VALUES
	(
		@new_id,
		in_category_name
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_business_category;
DELIMITER //
CREATE PROCEDURE sp_update_business_category
(
	IN in_business_category_id int,
	IN in_category_name varchar(50)
) 
BEGIN 
	UPDATE business_category SET 
		category_name=in_category_name
	WHERE business_category_id=in_business_category_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_business_category_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_business_category_by_id
(
	IN in_business_category_id int
) 
BEGIN 
	SELECT * FROM business_category 
	WHERE business_category_id=in_business_category_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_business_category_by_name;
DELIMITER //
CREATE PROCEDURE sp_search_business_category_by_name
(
	IN in_category_name varchar(50)
) 
BEGIN 
	SELECT * FROM business_category 
	WHERE category_name LIKE concat('%',in_category_name,'%')  
	ORDER BY category_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_business_category_by_exact_name;
DELIMITER //
CREATE PROCEDURE sp_search_business_category_by_exact_name
(
	IN in_category_name varchar(50)
) 
BEGIN 
	SELECT * FROM business_category 
	WHERE category_name = in_category_name  
	ORDER BY category_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_business_category_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_business_category_by_none() 
BEGIN 
	SELECT * FROM business_category 
	ORDER BY category_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_subscription;
DELIMITER //
CREATE PROCEDURE sp_insert_subscription
(
	IN in_transactor_id bigint,
	IN in_subscription_category_id int,
	IN in_business_category_id int,
	IN in_item_id bigint,
	IN in_description varchar(150),
	IN in_amount double,
	IN in_is_recurring varchar(10),
	IN in_current_status varchar(20),
	IN in_frequency varchar(20),
	IN in_unit_price double,
	IN in_qty double,
	IN in_agent varchar(50),
	IN in_account_manager varchar(50),
	IN in_subscription_date datetime,
	IN in_renewal_date datetime,
	IN in_expiry_date datetime,
	IN in_add_date datetime,
	IN in_added_by varchar(50),
	IN in_last_edit_date datetime,
	IN in_last_edited_by varchar(50),
	IN in_free_at_reg int,
	IN in_commission_amount double,
	IN in_converted_by varchar(50),
	IN in_referred_by varchar(50),
	OUT out_subscription_id int
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("subscription","subscription_id",@new_id);
	set out_subscription_id=@new_id;
	INSERT INTO subscription
	(
		subscription_id,
		transactor_id,
		subscription_category_id,
		business_category_id,
		item_id,
		description,
		amount,
		is_recurring,
		current_status,
		frequency,
		unit_price,
		qty,
		agent,
		account_manager,
		subscription_date,
		renewal_date,
		expiry_date,
		add_date,
		added_by,
		last_edit_date,
		last_edited_by,
		free_at_reg,
		commission_amount,
		converted_by,
		referred_by
	) 
    VALUES
	(
		@new_id,
		in_transactor_id,
		in_subscription_category_id,
		in_business_category_id,
		in_item_id,
		in_description,
		in_amount,
		in_is_recurring,
		in_current_status,
		in_frequency,
		in_unit_price,
		in_qty,
		in_agent,
		in_account_manager,
		in_subscription_date,
		in_renewal_date,
		in_expiry_date,
		in_add_date,
		in_added_by,
		in_last_edit_date,
		in_last_edited_by,
		in_free_at_reg,
		in_commission_amount,
		in_converted_by,
		in_referred_by
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_subscription;
DELIMITER //
CREATE PROCEDURE sp_update_subscription
(
	IN in_subscription_id int,
	IN in_transactor_id bigint,
	IN in_subscription_category_id int,
	IN in_business_category_id int,
	IN in_item_id bigint,
	IN in_description varchar(150),
	IN in_amount double,
	IN in_is_recurring varchar(10),
	IN in_current_status varchar(20),
	IN in_frequency varchar(20),
	IN in_unit_price double,
	IN in_qty double,
	IN in_agent varchar(50),
	IN in_account_manager varchar(50),
	IN in_subscription_date datetime,
	IN in_renewal_date datetime,
	IN in_expiry_date datetime,
	IN in_add_date datetime,
	IN in_added_by varchar(50),
	IN in_last_edit_date datetime,
	IN in_last_edited_by varchar(50),
	IN in_free_at_reg int,
	IN in_commission_amount double,
	IN in_converted_by varchar(50),
	IN in_referred_by varchar(50)
) 
BEGIN 
	UPDATE subscription SET 
		transactor_id=in_transactor_id,
		subscription_category_id=in_subscription_category_id,
		business_category_id=in_business_category_id,
		item_id=in_item_id,
		description=in_description,
		amount=in_amount,
		is_recurring=in_is_recurring,
		current_status=in_current_status,
		frequency=in_frequency,
		unit_price=in_unit_price,
		qty=in_qty,
		agent=in_agent,
		account_manager=in_account_manager,
		subscription_date=in_subscription_date,
		renewal_date=in_renewal_date,
		expiry_date=in_expiry_date,
		add_date=in_add_date,
		added_by=in_added_by,
		last_edit_date=in_last_edit_date,
		last_edited_by=in_last_edited_by,
		free_at_reg=in_free_at_reg,
		commission_amount=in_commission_amount,
		converted_by=in_converted_by,
		referred_by=in_referred_by
	WHERE subscription_id=in_subscription_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_subscription_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_subscription_by_id
(
	IN in_subscription_id int
) 
BEGIN 
	SELECT * FROM subscription 
	WHERE subscription_id=in_subscription_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_subscription_by_description_currenstatus_frequency;
DELIMITER //
CREATE PROCEDURE sp_search_subscription_by_description_currenstatus_frequency
(
	IN in_text varchar(100)
) 
BEGIN 
	SELECT * FROM subscription s 
	WHERE s.description LIKE concat('%',in_text,'%') OR s.current_status LIKE concat('%',in_text,'%') OR s.frequency LIKE concat('%',in_text,'%') 
	ORDER BY s.subscription_id ASC LIMIT 20; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_subscription_by_status_category_expiryrange_recurring;
DELIMITER //
CREATE PROCEDURE sp_search_subscription_by_status_category_expiryrange_recurring
(
	IN in_current_status varchar(20),
	IN in_subscription_category_id int,
	IN in_renewal_date_range int,
	IN in_is_recurring varchar(10)
) 
BEGIN 
	SELECT * FROM subscription s 
	WHERE s.current_status=in_current_status AND s.subscription_category_id=in_subscription_category_id AND timestampdiff(MONTH, NOW(),s.renewal_date)=in_renewal_date_range AND s.is_recurring=in_is_recurring 
	ORDER BY s.subscription_id ASC LIMIT 20; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_subscription_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_subscription_by_none() 
BEGIN 
	SELECT * FROM subscription 
	ORDER BY subscription_id ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_subscription_log;
DELIMITER //
CREATE PROCEDURE sp_insert_subscription_log
(
	IN in_subscription_id int,
	IN in_transactor_id bigint,
	IN in_subscription_category_id int,
	IN in_business_category_id int,
	IN in_item_id bigint,
	IN in_description varchar(150),
	IN in_amount double,
	IN in_is_recurring varchar(10),
	IN in_current_status varchar(20),
	IN in_frequency varchar(20),
	IN in_unit_price double,
	IN in_qty double,
	IN in_agent varchar(50),
	IN in_account_manager varchar(50),
	IN in_subscription_date datetime,
	IN in_renewal_date datetime,
	IN in_expiry_date datetime,
	IN in_free_at_reg int,
	IN in_commission_amount double,
	IN in_action varchar(20),
	IN in_add_date datetime,
	IN in_added_by varchar(50),
	IN in_converted_by varchar(50),
	IN in_referred_by varchar(50)
) 
BEGIN 
	SET @new_id=0;
	CALL sp_get_new_id("subscription_log","subscription_log_id",@new_id);
	INSERT INTO subscription_log
	(
		subscription_log_id,
		subscription_id,
		transactor_id,
		subscription_category_id,
		business_category_id,
		item_id,
		description,
		amount,
		is_recurring,
		current_status,
		frequency,
		unit_price,
		qty,
		agent,
		account_manager,
		subscription_date,
		renewal_date,
		expiry_date,
		free_at_reg,
		commission_amount,
		action,
		add_date,
		added_by,
		converted_by,
		referred_by
	) 
    VALUES
	(
		@new_id,
		in_subscription_id,
		in_transactor_id,
		in_subscription_category_id,
		in_business_category_id,
		in_item_id,
		in_description,
		in_amount,
		in_is_recurring,
		in_current_status,
		in_frequency,
		in_unit_price,
		in_qty,
		in_agent,
		in_account_manager,
		in_subscription_date,
		in_renewal_date,
		in_expiry_date,
		in_free_at_reg,
		in_commission_amount,
		in_action,
		in_add_date,
		in_added_by,
		in_converted_by,
		in_referred_by
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_subscription_log;
DELIMITER //
CREATE PROCEDURE sp_update_subscription_log
(
	IN in_subscription_log_id bigint,
	IN in_subscription_id int,
	IN in_transactor_id bigint,
	IN in_subscription_category_id int,
	IN in_business_category_id int,
	IN in_item_id bigint,
	IN in_description varchar(150),
	IN in_amount double,
	IN in_is_recurring varchar(10),
	IN in_current_status varchar(20),
	IN in_frequency varchar(20),
	IN in_unit_price double,
	IN in_qty double,
	IN in_agent varchar(50),
	IN in_account_manager varchar(50),
	IN in_subscription_date datetime,
	IN in_renewal_date datetime,
	IN in_expiry_date datetime,
	IN in_free_at_reg int,
	IN in_commission_amount double,
	IN in_action varchar(20),
	IN in_add_date datetime,
	IN in_added_by varchar(50),
	IN in_converted_by varchar(50),
	IN in_referred_by varchar(50)
) 
BEGIN 
	UPDATE subscription_log SET 
		subscription_id=in_subscription_id,
		transactor_id=in_transactor_id,
		subscription_category_id=in_subscription_category_id,
		business_category_id=in_business_category_id,
		item_id=in_item_id,
		description=in_description,
		amount=in_amount,
		is_recurring=in_is_recurring,
		current_status=in_current_status,
		frequency=in_frequency,
		unit_price=in_unit_price,
		qty=in_qty,
		agent=in_agent,
		account_manager=in_account_manager,
		subscription_date=in_subscription_date,
		renewal_date=in_renewal_date,
		expiry_date=in_expiry_date,
		free_at_reg=in_free_at_reg,
		commission_amount=in_commission_amount,
		action=in_action,
		add_date=in_add_date,
		added_by=in_added_by,
		converted_by=in_converted_by,
		referred_by=in_referred_by
	WHERE subscription_log_id=in_subscription_log_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_subscription_log_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_subscription_log_by_id
(
	IN in_subscription_log_id bigint
) 
BEGIN 
	SELECT * FROM subscription_log
	WHERE subscription_log_id=in_subscription_log_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_subscription_log_by_subscription_id;
DELIMITER //
CREATE PROCEDURE sp_search_subscription_log_by_subscription_id
(
	IN in_subscription_id int
) 
BEGIN 
	SELECT * FROM subscription_log
	WHERE subscription_id=in_subscription_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_subscription_log_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_subscription_log_by_none() 
BEGIN 
	SELECT * FROM subscription_log 
	ORDER BY subscription_log_id ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_shift;
DELIMITER //
CREATE PROCEDURE sp_insert_shift
(
	IN in_shift_name varchar(50),
	IN in_description varchar(150),
	IN in_start_time time,
	IN in_end_time time
) 
BEGIN
	INSERT INTO shift
	(
		shift_name,
		description,
		start_time,
		end_time
	) 
    VALUES
	(
		in_shift_name,
		in_description,
		in_start_time,
		in_end_time
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_shift;
DELIMITER //
CREATE PROCEDURE sp_update_shift
(
	IN in_shift_id int,
	IN in_shift_name varchar(50),
	IN in_description varchar(150),
	IN in_start_time time,
	IN in_end_time time
) 
BEGIN 
	UPDATE shift SET 
		shift_name=in_shift_name,
		description=in_description,
		start_time=in_start_time,
		end_time=in_end_time
	WHERE shift_id=in_shift_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_shift_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_shift_by_id
(
	IN in_shift_id int
) 
BEGIN 
	SELECT * FROM shift 
	WHERE shift_id=in_shift_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_shift_by_name;
DELIMITER //
CREATE PROCEDURE sp_search_shift_by_name
(
	IN in_shift_name varchar(50)
) 
BEGIN 
	SELECT * FROM shift 
	WHERE shift_name LIKE concat('%',in_shift_name,'%') OR description LIKE concat('%',in_shift_name,'%')  
	ORDER BY shift_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_shift_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_shift_by_none() 
BEGIN 
	SELECT * FROM shift 
	ORDER BY shift_name ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_shift_by_time;
DELIMITER //
CREATE PROCEDURE sp_search_shift_by_time
(
	IN in_time time
) 
BEGIN 
	SELECT * FROM shift WHERE in_time between start_time and end_time; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_shift;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_shift
(
	IN in_transaction_id bigint,
	IN in_shift_id int,
	IN in_transaction_type_id int
) 
BEGIN
	INSERT INTO transaction_shift
	(
		transaction_id,
		shift_id,
		transaction_type_id
	) 
    VALUES
	(
		in_transaction_id,
		in_shift_id,
		in_transaction_type_id
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_transaction_shift;
DELIMITER //
CREATE PROCEDURE sp_update_transaction_shift
(
	IN in_transaction_shift_id bigint,
	IN in_transaction_id bigint,
	IN in_shift_id int,
	IN in_transaction_type_id int
) 
BEGIN 
	UPDATE transaction_shift SET transaction_id=in_transaction_id,shift_id=in_shift_id,transaction_type_id=in_transaction_type_id 
	WHERE transaction_shift_id=in_transaction_shift_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_shift_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_shift_by_id
(
	IN in_transaction_shift_id bigint
) 
BEGIN 
	SELECT * FROM transaction_shift 
	WHERE transaction_shift_id=in_transaction_shift_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_shift_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_shift_by_none() 
BEGIN 
	SELECT * FROM transaction_shift ORDER BY transaction_shift_id ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_pay_shift;
DELIMITER //
CREATE PROCEDURE sp_insert_pay_shift
(
	IN in_pay_id bigint,
	IN in_shift_id int,
	IN in_pay_type_id int
) 
BEGIN
	INSERT INTO pay_shift
	(
		pay_id,
		shift_id,
		pay_type_id
	) 
    VALUES
	(
		in_pay_id,
		in_shift_id,
		in_pay_type_id
	); 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_pay_shift;
DELIMITER //
CREATE PROCEDURE sp_update_pay_shift
(
	IN in_pay_shift_id bigint,
	IN in_pay_id bigint,
	IN in_shift_id int,
	IN in_pay_type_id int
) 
BEGIN 
	UPDATE pay_shift SET pay_id=in_pay_id,shift_id=in_shift_id,pay_type_id=in_pay_type_id 
	WHERE pay_shift_id=in_pay_shift_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_shift_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_pay_shift_by_id
(
	IN in_pay_shift_id bigint
) 
BEGIN 
	SELECT * FROM pay_shift 
	WHERE pay_shift_id=in_pay_shift_id; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_pay_shift_by_none;
DELIMITER //
CREATE PROCEDURE sp_search_pay_shift_by_none() 
BEGIN 
	SELECT * FROM pay_shift ORDER BY pay_shift_id ASC; 
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_alter_stock_ledger_tables_trans_item_id;
DELIMITER //
CREATE PROCEDURE sp_alter_stock_ledger_tables_trans_item_id
(
IN in_db_name varchar(100) 
)
BEGIN 
	DECLARE finished INTEGER DEFAULT 0;
	DECLARE TableName varchar(30) DEFAULT "";
	-- declare cursor for stock legder tables
	DEClARE curTableName CURSOR FOR SELECT TABLE_NAME as tb FROM information_schema.tables WHERE TABLE_SCHEMA=in_db_name AND TABLE_NAME LIKE 'stock_ledger%';
	-- declare NOT FOUND handler
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;
	OPEN curTableName;
	getTableName: LOOP
		FETCH curTableName INTO TableName;
		IF finished = 1 THEN 
			LEAVE getTableName;
		END IF;
		-- alter table
		set @sql_text = CONCAT("ALTER TABLE ",TableName," ADD COLUMN transaction_item_id BIGINT(20) NULL DEFAULT '0'");
		-- select @sql_text;
        PREPARE stmt FROM @sql_text;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
	END LOOP getTableName;
	CLOSE curTableName;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_package;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_package(
    IN in_transaction_number varchar(255),
    IN in_transaction_id bigint(20),
    IN in_transaction_date datetime,
	IN in_store_id int,
	IN in_store2_id int,
	IN in_transactor_id bigint,
	IN in_transaction_type_id int,
	IN in_transaction_reason_id int,
    IN in_sub_total double,
    IN in_total_trade_discount double,
    IN in_total_tax double,
    IN in_grand_total double,
	IN in_transaction_ref varchar(100),
	IN in_transaction_comment varchar(255),
	IN in_cash_discount double,
	IN in_add_user_detail_id int,
	IN in_add_date datetime,
	IN in_edit_user_detail_id int,
	IN in_edit_date datetime,
	OUT out_transaction_package_id bigint,
	IN in_vat_perc double,
	IN in_transaction_user_detail_id int,
    IN in_currency_code varchar(255),
    IN in_location_id bigint(20),
    IN in_status_code varchar(20),
    IN in_status_date datetime
)
BEGIN 
	-- SET @new_id=0;
	-- CALL sp_get_new_id("transaction","transaction_id",@new_id);
	
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	SET @edit_dateTime=null;
	SET @edit_user_detail_id=null;
	
	SET @store2_id=NULL;
	if (in_store2_id!=0) then
		set @store2_id=in_store2_id;
	end if;

	SET @transactor_id=NULL;
	if (in_transactor_id!=0) then
		set @transactor_id=in_transactor_id;
	end if;

	SET @transaction_user_detail_id=NULL;
	if (in_transaction_user_detail_id!=0) then
		set @transaction_user_detail_id=in_transaction_user_detail_id;
	end if;

	SET @transaction_number=@new_id;
	if (in_transaction_number!='') then
		set @transaction_number=in_transaction_number;
	end if;
    
    set @transaction_number=in_transaction_number;

	SET @location_id=NULL;
	if (in_location_id!=0) then
		set @location_id=in_location_id;
	end if;
    
	INSERT INTO transaction_package
	(
    transaction_number,
    transaction_date,
	store_id,
	store2_id,
	transactor_id,
	transaction_type_id,
	transaction_reason_id,
    sub_total,
    total_trade_discount,
    total_tax,
    grand_total,
	transaction_ref,
    transaction_comment,
	cash_discount,
	add_user_detail_id,
	add_date,
    edit_user_detail_id,
	edit_date,
    transaction_user_detail_id,
    currency_code,
    location_id,
    status_code,
    status_date, 
    transaction_id
	) 
    VALUES
	(
		@transaction_number,
		in_transaction_date,
		in_store_id,
		@store2_id,
		@transactor_id,
		in_transaction_type_id,
		in_transaction_reason_id,
		in_sub_total,
		in_total_trade_discount,
		in_total_tax,
		in_grand_total,
		in_transaction_ref,
		in_transaction_comment,
		in_cash_discount,
		in_add_user_detail_id,
        @cur_sys_datetime,
		@edit_user_detail_id,
		@edit_dateTime,
		@transaction_user_detail_id,
		in_currency_code,
		@location_id,
		in_status_code,
		in_status_date,
        in_transaction_id
	); 
SET out_transaction_package_id=LAST_INSERT_ID();
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_insert_transaction_package_item;
DELIMITER //
CREATE PROCEDURE sp_insert_transaction_package_item(
	OUT out_transaction_package_item_id bigint,
	IN in_transaction_package_id bigint,
	IN in_item_id bigint,
	IN in_unit_id  bigint,
	IN in_batchno varchar(255),
	IN in_code_specific varchar(255),
	IN in_desc_specific varchar(255),
	IN in_desc_more varchar(255),
	IN in_item_qty double,
	IN in_base_unit_qty  double,
	IN in_unit_price double,
	IN in_unit_trade_discount double,
	IN in_unit_vat double,
	IN in_excise_rate_name varchar(255),
	IN in_excise_rate_perc varchar(255),
	IN in_excise_rate_value  double,
	IN in_excise_tax double,
	IN in_excise_currency_code_tax varchar(255),
	IN in_excise_unit_code_tax varchar(255),
	IN in_amount double,
	IN in_vat_rated varchar(255),
	IN in_vat_perc double,
	IN in_narration varchar(255))
BEGIN 

INSERT INTO transaction_package_item
(
	transaction_package_id,
	item_id,
	unit_id,
	batchno,
	code_specific,
	desc_specific,
	desc_more,
	item_qty,
	base_unit_qty,
	unit_price,
	unit_trade_discount,
	unit_vat,
	excise_rate_name,
	excise_rate_perc,
	excise_rate_value,
	excise_tax,
	excise_currency_code_tax,
	excise_unit_code_tax,
	amount,
	vat_rated,
	vat_perc,
	narration)
VALUES
(
	in_transaction_package_id,
	in_item_id,
	in_unit_id,
	in_batchno,
	in_code_specific,
	in_desc_specific,
	in_desc_more,
	in_item_qty,
	in_base_unit_qty,
	in_unit_price,
	in_unit_trade_discount,
	in_unit_vat,
	in_excise_rate_name,
	in_excise_rate_perc,
	in_excise_rate_value,
	in_excise_tax,
	in_excise_currency_code_tax,
	in_excise_unit_code_tax,
	in_amount,
	in_vat_rated,
	in_vat_perc,
	in_narration
    );
SET out_transaction_package_item_id=LAST_INSERT_ID();
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_item_by_transaction_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_item_by_transaction_id
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT ti.*,tiu.unit_id,tiu.base_unit_qty FROM transaction_item ti 
        INNER JOIN transaction_item_unit tiu ON ti.transaction_item_id=tiu.transaction_item_id 
		WHERE ti.transaction_id=in_transaction_id ORDER BY ti.transaction_item_id ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_package_item_by_transaction_package_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_package_item_by_transaction_package_id
(
	IN in_transaction_package_id bigint 
) 
BEGIN 
		SELECT tpi.*,tiu.unit_id,tiu.base_unit_qty FROM transaction_package_item tpi 
        INNER JOIN transaction_package_item_unit tiu ON tpi.transaction_package_item_id=tiu.transaction_package_item_unit_id 
		WHERE tpi.transaction_package_id=in_transaction_package_id ORDER BY tpi.transaction_package_item_id ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_package_item_by_transaction_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_package_item_by_transaction_id
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT tpi.*,i.description as item_description,u.unit_symbol,i.item_code,tu.unit_id,tu.base_unit_qty FROM transaction_package_item tpi 
        INNER JOIN item i ON tpi.item_id=i.item_id 
        INNER JOIN transaction_package_item_unit tu ON tpi.transaction_package_item_id=tu.transaction_package_item_unit_id 
        INNER JOIN unit u ON tu.unit_id=u.unit_id 
		INNER JOIN transaction_package tp ON tpi.transaction_package_id = tp.transaction_package_id
		WHERE tp.transaction_id=in_transaction_id 
		ORDER BY tpi.transaction_package_item_id ASC;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_search_transaction_package_transaction_by_id;
DELIMITER //
CREATE PROCEDURE sp_search_transaction_package_transaction_by_id
(
	IN in_transaction_id bigint 
) 
BEGIN 
		SELECT * FROM transaction_package
		WHERE transaction_id=in_transaction_id;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_transaction2;
DELIMITER //
CREATE PROCEDURE sp_update_transaction2
(
	IN in_transaction_id bigint,
	IN in_cash_discount double,
	IN in_total_vat double,
	IN in_edit_user_detail_id int,
	IN in_sub_total double,
	IN in_grand_total double,
	IN in_total_trade_discount double,
	IN in_points_awarded double,
	IN in_card_number varchar(10),
	IN in_total_std_vatable_amount double,
	IN in_total_zero_vatable_amount double,
	IN in_total_exempt_vatable_amount double,
	IN in_amount_tendered double,
	IN in_change_amount double,
	IN in_total_profit_margin double,
    IN in_spent_points_amount double
) 
BEGIN 
	SET @cur_sys_datetime=null;
	CALL sp_get_current_system_datetime(@cur_sys_datetime);

	UPDATE transaction SET 
		cash_discount=in_cash_discount,
		total_vat=in_total_vat,
		edit_user_detail_id=in_edit_user_detail_id,
		edit_date=@cur_sys_datetime,
		sub_total=in_sub_total,
		grand_total=in_grand_total,
		total_trade_discount=in_total_trade_discount,
		points_awarded=in_points_awarded,
		card_number=in_card_number,
		total_std_vatable_amount=in_total_std_vatable_amount,
		total_zero_vatable_amount=in_total_zero_vatable_amount,
		total_exempt_vatable_amount=in_total_exempt_vatable_amount,
		amount_tendered=in_amount_tendered,
		change_amount=in_change_amount,
		total_profit_margin=in_total_profit_margin,
        spent_points_amount=in_spent_points_amount 
	WHERE transaction_id=in_transaction_id; 
END//
DELIMITER ;

