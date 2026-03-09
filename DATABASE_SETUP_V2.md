# VendorPlus V2 - Local Database Setup Instructions
## Company-Based Multi-Tenant System

## STEP 1: Create vendorplus_v2 Database

Open MySQL command line or MySQL Workbench and run:

```sql
-- Create the new database
CREATE DATABASE vendorplus_v2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verify it was created
SHOW DATABASES LIKE 'vendorplus%';
```

## STEP 2: Import Base Schema

Import the base vendorplus schema into the new database:

### Option A: Using MySQL Command Line
```bash
# Navigate to your project folder
cd c:\Users\LENOVO\Desktop\vendorplus

# Import the base schema (you'll be prompted for password)
mysql -u root -p vendorplus_v2 < vp/vendorplus.sql
```

### Option B: Using MySQL Workbench
1. Open MySQL Workbench
2. Connect to your local MySQL server
3. Click: Server → Data Import
4. Select "Import from Self-Contained File"
5. Browse to: `c:\Users\LENOVO\Desktop\vendorplus\vp\vendorplus.sql`
6. Select "Default Target Schema": vendorplus_v2
7. Click "Start Import"

## STEP 3: Apply Company-Based Migration

Run the migration script to add company features:

### Option A: Using MySQL Command Line
```bash
mysql -u root -p vendorplus_v2 < vp/migrate_to_company_based.sql
```

### Option B: Using MySQL Workbench
1. Open MySQL Workbench
2. Connect to your local MySQL server
3. Open SQL file: `vp/migrate_to_company_based.sql`
4. Make sure "vendorplus_v2" is selected in the schema dropdown
5. Click Execute (lightning bolt icon)

### Option C: Copy and Paste
1. Open `vp/migrate_to_company_based.sql` in a text editor
2. Copy all content
3. Open MySQL Workbench or command line
4. Run: `USE vendorplus_v2;`
5. Paste and execute the migration script

## STEP 4: Verify Migration Success

Run these verification queries:

```sql
USE vendorplus_v2;

-- Check company table exists
SELECT * FROM company;

-- Verify user_detail has company_id
DESCRIBE user_detail;

-- Check stored procedures were created
SHOW PROCEDURE STATUS WHERE Db = 'vendorplus_v2';

-- Verify company_id added to key tables
SELECT 
  'user_detail' as table_name, COUNT(*) as records, COUNT(DISTINCT company_id) as companies 
FROM user_detail
UNION ALL
SELECT 'store', COUNT(*), COUNT(DISTINCT company_id) FROM store
UNION ALL
SELECT 'item', COUNT(*), COUNT(DISTINCT company_id) FROM item
UNION ALL
SELECT 'transactor', COUNT(*), COUNT(DISTINCT company_id) FROM transactor;
```

Expected results:
- ✅ Company table has 1 record (System Default Company)
- ✅ All tables show company_id column
- ✅ All existing data has company_id = 1
- ✅ Stored procedures for company management exist

## STEP 5: Application Configuration (Already Done)

✅ ConfigFile.properties updated to use vendorplus_v2:
```properties
branch_database=vendorplus_v2
```

## STEP 6: Build and Test Application

### Build the Application
```bash
cd c:\Users\LENOVO\Desktop\vendorplus
ant clean
ant dist
```

### Deploy to Tomcat (if using local Tomcat)
```bash
# Copy WAR to Tomcat webapps
copy dist\vendorplus.war C:\path\to\tomcat\webapps\

# Restart Tomcat
```

### Test Registration
1. Start your application
2. Visit: http://localhost:8080/vendorplus/Registration.xhtml
3. Register a test user with company information
4. Check database:
   ```sql
   SELECT * FROM company ORDER BY company_id DESC LIMIT 1;
   SELECT * FROM user_detail ORDER BY user_detail_id DESC LIMIT 1;
   ```

### Test CRM Dashboard
1. Login as admin user
2. Go to: Setup → Users and Groups → User Management CRM
3. Verify dashboard shows users, login activity, and analytics

## TROUBLESHOOTING

### Issue: "Table 'vendorplus_v2.company' doesn't exist"
**Solution:** Re-run the migration script: `vp/migrate_to_company_based.sql`

### Issue: "Unknown column 'company_id' in 'field list'"
**Solution:** The ALTER TABLE statements didn't run. Check for MySQL errors and re-run migration.

### Issue: "Procedure 'sp_insert_company' does not exist"
**Solution:** Stored procedures weren't created. Re-run the DELIMITER section of the migration script.

### Issue: Application still connects to old vendorplus database
**Solution:** 
1. Check `src/java/configurations/ConfigFile.properties` shows `vendorplus_v2`
2. Rebuild application: `ant clean && ant dist`
3. Redeploy WAR file
4. Restart Tomcat/server

## WHAT'S BEEN ADDED

### New Tables:
- `company` - Stores company/organization information
- `user_activity_log` - Tracks all user actions for audit

### Modified Tables (company_id added):
- `user_detail` - Users belong to companies
- `store` - Each company has their own stores
- `item` - Each company has their own inventory
- `transactor` - Each company has their own customers/suppliers
- `category` - Product categories per company
- `subcategory` - Product subcategories per company
- `transaction` - All transactions linked to company
- `project` - Projects belong to companies
- `login_session` - Enhanced with timestamps for tracking

### New Stored Procedures:
- `sp_insert_company` - Create new company
- `sp_search_company_by_id` - Find company by ID
- `sp_search_company_by_code` - Find company by code
- `sp_check_tax_id_exists` - Validate tax ID uniqueness
- `sp_insert_user_with_company` - Register user with company
- `sp_count_users_by_company` - Get user statistics
- `sp_get_recent_login_activity` - Track login history
- `sp_get_registration_stats` - Registration analytics
- `sp_search_users_for_crm` - CRM dashboard queries
- `sp_log_user_activity` - Log user actions

## NEXT STEPS

After successful migration:

1. **Test Registration Flow:**
   - Register multiple users with different companies
   - Verify each creates a separate company

2. **Test Data Segregation:**
   - Login as User A (Company 1)
   - Create items, transactions
   - Login as User B (Company 2)
   - Verify they don't see Company 1's data

3. **Configure Default Permissions:**
   - Login as admin
   - Go to: Setup → Users and Groups → Group Rights
   - Configure "Registered Users" group permissions

4. **Deploy to Production:**
   - Follow same steps on production server
   - Backup existing database first!
   - Test thoroughly before switching

## BACKUP COMMAND

Before migration, always backup:
```bash
mysqldump -u root -p vendorplus > vendorplus_backup_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql
```

## FILES CREATED/MODIFIED

- ✅ `vp/migrate_to_company_based.sql` - Migration script
- ✅ `src/java/configurations/ConfigFile.properties` - Updated to vendorplus_v2
- ✅ All Java entities and beans already updated with company support

---

**Ready to proceed!** Run Steps 1-3 above to create and migrate your database.
