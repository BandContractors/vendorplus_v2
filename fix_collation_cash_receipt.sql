-- Fix collation issue in sp_search_trans_for_cash_receipt_credit_sale
USE vendorplus;

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
		set @CUST=CONCAT(" AND t.bill_transactor_id=",CAST(in_transactor_id AS CHAR)," ");
	end if;
    SET @STORE="";
	if (in_store_id>0) then 
		set @STORE=CONCAT(' AND t.store_id=',CAST(in_store_id AS CHAR)," ");
	end if;
    SET @CUR="";
	if (length(in_currency_code)>0) then 
		set @CUR=CONCAT(" AND t.currency_code='",CAST(in_currency_code AS CHAR),"' ");
	end if;
    SET @TRANSNO="";
	if (length(in_transaction_number)>0) then 
		set @TRANSNO=CONCAT(" AND t.transaction_number='",CAST(in_transaction_number AS CHAR),"' ");
	end if;
    SET @DAT="";
	if (in_date1 is not null and in_date2 is not null) then 
		set @DAT=CONCAT(" AND t.transaction_date BETWEEN '",CAST(in_date1 AS CHAR),"' AND '",CAST(in_date2 AS CHAR),"' ");
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
