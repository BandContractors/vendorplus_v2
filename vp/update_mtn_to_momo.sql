-- Update MTN account from MOOM to MOMO
-- Safe to run - only updates one specific record

-- Step 1: Check current value
SELECT 
    acc_child_account_id,
    child_account_code,
    child_account_name,
    acc_coa_account_code
FROM acc_child_account 
WHERE acc_child_account_id = 10;

-- Step 2: Update to MTN MOMO
UPDATE acc_child_account 
SET 
    child_account_code = 'MTN MOMO',
    last_edit_date = NOW(),
    last_edit_by = 1
WHERE acc_child_account_id = 10;

-- Step 3: Verify the change
SELECT 
    acc_child_account_id,
    child_account_code,
    child_account_name,
    acc_coa_account_code,
    last_edit_date
FROM acc_child_account 
WHERE acc_child_account_id = 10;

-- You should see: child_account_code = 'MTN MOMO'
