# VendorPlus Self-Service Registration + CRM System
## Deployment Guide

### Overview
This implementation adds:
1. **Self-Service User Registration** - Users can register their own accounts with company information
2. **Multi-Tenant Data Segregation** - Each company sees only their own data
3. **CRM Dashboard** - Admins can monitor, manage, and analyze users

### Features Implemented

#### 1. User Registration (Priority Based on Your Requirements)
- **Required Fields**: Username, Password, First Name, Email
- **Optional Fields**: Second Name, Third Name, Phone, Company Name, Tax ID, Address, City, Country
- **Auto-Activation**: Users can login immediately after registration
- **View-Only Default Access**: New users assigned to "Registered Users" group
- **Company Auto-Creation**: Each registration creates a new company record

#### 2. CRM Dashboard (Priority: 1, 3, 4, 5)
- **Priority #1**: User list with filters (search by username/name/email, filter by status)
- **Priority #3**: Login activity tracking (recent sessions, IP addresses, timestamps)
- **Priority #4**: Analytics dashboard (total users, active/locked counts, registration trends)
- **Priority #5**: User management actions (lock/unlock, delete users)

---

## DEPLOYMENT STEPS

### STEP 1: Apply Database Schema Changes
Run the SQL script on your vendorplus database:

```bash
# On local development server:
mysql -u root -p vendorplus < vp/registration_crm_schema.sql

# On production server (Linode):
mysql -u vendorplus -p vendorplus < vp/registration_crm_schema.sql
```

**What this does:**
- Creates `company` table for multi-tenant support
- Adds `company_id` column to `user_detail` table
- Updates all existing users to belong to default company (company_id = 1)
- Enhances `login_session` table with login timestamp tracking
- Creates `user_activity_log` table for audit trail
- Creates stored procedures for company and user management
- Creates default "Registered Users" group (ID: 999)

### STEP 2: Build & Deploy Application
The following files have been created/modified:

**New Files Created:**
- `src/java/entities/Company.java` - Company entity
- `src/java/beans/CompanyBean.java` - Company CRUD operations
- `src/java/beans/UserRegistrationBean.java` - Registration logic
- `src/java/beans/UserManagementCRMBean.java` - CRM dashboard logic
- `web/Registration.xhtml` - Public registration page
- `web/UserManagementCRM.xhtml` - CRM dashboard page
- `vp/registration_crm_schema.sql` - Database schema changes

**Modified Files:**
- `src/java/entities/UserDetail.java` - Added `CompanyId` field
- `src/java/beans/UserDetailBean.java` - Load `company_id` from database
- `src/java/beans/NavigationBean.java` - Added `redirectToUserManagementCRM()`
- `web/Login.xhtml` - Added "Register Now" button
- `web/Menu.xhtml` - Added "User Management CRM" menu item

**Build Commands:**
```bash
# If using Ant (from workspace folder):
cd c:\Users\LENOVO\Desktop\vendorplus
ant clean
ant dist

# Output WAR will be in: dist/vendorplus.war
```

### STEP 3: Deploy to Production Server

**Option A: Deploy WAR file (Recommended)**
```bash
# Stop Tomcat
ssh vendorplus@vendorplusuganda.com
sudo systemctl stop tomcat

# Backup current deployment
sudo mv /opt/tomcat/webapps/vendorplus /opt/tomcat/webapps/vendorplus.backup_$(date +%Y%m%d)
sudo mv /opt/tomcat/webapps/vendorplus.war /opt/tomcat/webapps/vendorplus.war.backup_$(date +%Y%m%d)

# Upload new WAR from local (run from local PowerShell):
scp c:\Users\LENOVO\Desktop\vendorplus\dist\vendorplus.war vendorplus@vendorplusuganda.com:/tmp/

# Deploy (back on server):
sudo mv /tmp/vendorplus.war /opt/tomcat/webapps/
sudo chown tomcat:tomcat /opt/tomcat/webapps/vendorplus.war

# Start Tomcat
sudo systemctl start tomcat
```

**Option B: Deploy Source Files (Alternative)**
If you prefer updating files individually:
```bash
# Upload new/modified files
scp src/java/entities/Company.java vendorplus@vendorplusuganda.com:/opt/tomcat/webapps/vendorplus/WEB-INF/classes/entities/
scp src/java/beans/CompanyBean.java vendorplus@vendorplusuganda.com:/opt/tomcat/webapps/vendorplus/WEB-INF/classes/beans/
scp src/java/beans/UserRegistrationBean.java vendorplus@vendorplusuganda.com:/opt/tomcat/webapps/vendorplus/WEB-INF/classes/beans/
scp src/java/beans/UserManagementCRMBean.java vendorplus@vendorplusuganda.com:/opt/tomcat/webapps/vendorplus/WEB-INF/classes/beans/
scp web/Registration.xhtml vendorplus@vendorplusuganda.com:/opt/tomcat/webapps/vendorplus/
scp web/UserManagementCRM.xhtml vendorplus@vendorplusuganda.com:/opt/tomcat/webapps/vendorplus/

# Restart Tomcat
ssh vendorplus@vendorplusuganda.com
sudo systemctl restart tomcat
```

### STEP 4: Verify Deployment

**1. Test Registration Page:**
- Visit: https://vendorplusuganda.com/Registration.xhtml
- Fill out registration form
- Submit and verify success message
- Check database: `SELECT * FROM company ORDER BY company_id DESC LIMIT 1;`
- Check database: `SELECT * FROM user_detail ORDER BY user_detail_id DESC LIMIT 1;`

**2. Test Login with New User:**
- Logout if logged in
- Login with newly registered username/password
- Should see store selection and access menu

**3. Test CRM Dashboard (Admin Only):**
- Login as admin user
- Go to: Setup → Users and Groups → User Management CRM
- Verify you see:
  - Analytics cards (Total Users, Active Users, Locked Users, Registrations)
  - User Management tab with user list
  - Login Activity tab with recent logins
  - Analytics & Reports tab with registration statistics

**4. Test User Management Actions:**
- In CRM dashboard, try locking a test user
- Verify user cannot login when locked
- Unlock the user
- Verify user can login again

---

## CONFIGURATION & CUSTOMIZATION

### Default "Registered Users" Group Permissions
New registrations are auto-assigned to group_detail_id = 999. To configure default permissions:

1. Login as admin
2. Go to: Setup → Users and Groups → Group Rights
3. Select "Registered Users (Default)" group
4. Select your store
5. Configure permissions for each function (View, Add, Edit, Delete)
6. Save changes

**Recommended Initial Permissions:**
- Items: View Only
- Transactions: No Access
- Reports: View Only
- Account Setup: No Access

### Email Notifications (Future Enhancement)
The registration system is ready for email notifications. To enable:
1. Configure SMTP settings in `parameter_list` table
2. Uncomment email sending code in `UserRegistrationBean.java`
3. Create email templates for:
   - Welcome email after registration
   - Account activation confirmation
   - Password reset

### Multi-Tenant Data Filtering (Important!)
Currently, the system creates separate companies but **does not yet filter all data queries by company_id**. To enable full data segregation:

**What's Already Implemented:**
- User list in CRM dashboard filtered by company
- Login activity filtered by company
- Analytics filtered by company

**What Needs Additional Work:**
- Transaction queries need `WHERE user_detail.company_id = ?` filters
- Report queries need company filtering
- Item/inventory queries may need company filtering (depends on your requirements)

**Example of adding company filter to a query:**
```java
// Before:
String sql = "SELECT * FROM transaction WHERE store_id = ?";

// After (if you want company segregation for transactions):
String sql = "SELECT t.* FROM transaction t " +
             "INNER JOIN user_detail u ON t.user_detail_id = u.user_detail_id " +
             "WHERE t.store_id = ? AND u.company_id = ?";
```

---

## SECURITY CONSIDERATIONS

### 1. Public Access to Registration Page
The Registration.xhtml page is publicly accessible (no login required). To secure it:

**Option A: Add CAPTCHA** (Recommended)
- Integrate Google reCAPTCHA to prevent bot registrations
- Add reCAPTCHA library to project
- Update Registration.xhtml with CAPTCHA widget

**Option B: Email Verification**
- Modify to require email verification before activation
- Set `is_user_locked = 'Yes'` during registration
- Send verification email with unique token
- Create verification endpoint to unlock account

**Option C: Admin Approval**
- Modify to create users as locked
- Admins review in CRM dashboard
- Admins manually unlock after verification

### 2. Username/Email Uniqueness
Already implemented - registration checks for:
- Duplicate usernames
- Duplicate email addresses
- Duplicate tax IDs (if provided)

### 3. Password Security
- Minimum length: 6 characters (configurable in `UserRegistrationBean.java`)
- Passwords encrypted using `Security.Encrypt()` method
- To enforce stronger passwords, update validation in `validateRegistrationData()`

### 4. CRM Dashboard Access Control
Currently accessible to all logged-in users. To restrict to admins only:

**Add to UserManagementCRM.xhtml (at top after <f:metadata>):**
```xml
<f:event type="preRenderView" listener="#{navigationBean.checkAdminAccess()}"/>
```

**Add to NavigationBean.java:**
```java
public void checkAdminAccess() {
    FacesContext context = FacesContext.getCurrentInstance();
    HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
    UserDetail currentUser = (UserDetail) session.getAttribute("CURRENT_USER");
    
    if (currentUser == null || !"Yes".equals(currentUser.getIsUserGenAdmin())) {
        context.getApplication().getNavigationHandler().handleNavigation(context, null, "Home?faces-redirect=true");
    }
}
```

---

## TESTING CHECKLIST

- [ ] Database schema applied successfully
- [ ] Application builds without errors
- [ ] Registration page loads (https://vendorplusuganda.com/Registration.xhtml)
- [ ] Can register new user with company
- [ ] New user appears in database with company_id
- [ ] Can login with newly registered user
- [ ] CRM dashboard accessible from menu
- [ ] CRM shows correct user count
- [ ] CRM user list displays all users
- [ ] Can filter users by status
- [ ] Can search users by name/email
- [ ] Login activity tab shows recent sessions
- [ ] Analytics tab shows registration statistics
- [ ] Can lock a user from CRM
- [ ] Locked user cannot login
- [ ] Can unlock user from CRM
- [ ] Unlocked user can login
- [ ] Can delete user with no history
- [ ] Cannot delete user with login history

---

## TROUBLESHOOTING

### Issue: Registration page shows 404
**Solution:** Verify Registration.xhtml was deployed to `/opt/tomcat/webapps/vendorplus/`

### Issue: "Table 'company' doesn't exist"
**Solution:** Run the schema SQL script: `mysql -u vendorplus -p vendorplus < vp/registration_crm_schema.sql`

### Issue: "sp_insert_user_with_company does not exist"
**Solution:** Stored procedure not created. Re-run schema SQL script.

### Issue: CRM menu doesn't appear
**Solution:** 
1. Check Menu.xhtml was updated
2. Check NavigationBean.java has `redirectToUserManagementCRM()` method
3. Clear browser cache
4. Restart Tomcat

### Issue: "company_id" column doesn't exist
**Solution:** Run: `ALTER TABLE user_detail ADD COLUMN company_id INT(11) DEFAULT 1;`

### Issue: New users can see all data
**Solution:** This is expected - full multi-tenant filtering requires updating ALL queries in the application. Start with critical areas (transactions, reports) and add `WHERE company_id = ?` filters.

### Issue: Registration fails with "Tax ID already exists"
**Solution:** Either use different tax ID or leave it blank (it's optional).

---

## NEXT STEPS & FUTURE ENHANCEMENTS

### Phase 2 Enhancements (Optional):
1. **Email Verification**
   - Send verification email after registration
   - Require email confirmation before activation
   
2. **Admin Approval Workflow**
   - Create approval queue in CRM dashboard
   - Admins review and approve/reject registrations
   
3. **Role-Based Permissions**
   - Create different registration types (Customer, Vendor, Agent)
   - Auto-assign different permissions based on role
   
4. **Company Management**
   - Add company settings page for company admins
   - Allow company logo upload
   - Company-specific configurations
   
5. **Advanced Analytics**
   - User activity reports (most active users, inactive users)
   - Login heatmap (time of day, day of week)
   - Registration source tracking
   - Export reports to Excel/PDF
   
6. **Complete Data Segregation**
   - Update ALL queries to filter by company_id
   - Add company filter to reports
   - Add company filter to transactions
   
7. **API for Registration**
   - RESTful API for mobile app registration
   - API authentication tokens
   
8. **User Self-Service**
   - Password reset page
   - Profile update page
   - Account settings

---

## SUPPORT & MAINTENANCE

### Log Files to Monitor:
- `/opt/tomcat/logs/catalina.out` - Tomcat application logs
- `/var/log/mysql/error.log` - MySQL errors

### Regular Maintenance:
1. **Weekly:** Review login activity for suspicious patterns
2. **Monthly:** Review and cleanup locked/inactive users
3. **Quarterly:** Analyze registration trends and adjust permissions

### Database Backups:
```bash
# Backup before major changes
mysqldump -u vendorplus -p vendorplus > vendorplus_backup_$(date +%Y%m%d).sql

# Backup company and user tables specifically
mysqldump -u vendorplus -p vendorplus company user_detail login_session user_activity_log > user_data_backup_$(date +%Y%m%d).sql
```

---

## FILES SUMMARY

### Database Schema:
- `vp/registration_crm_schema.sql` - Complete schema changes and stored procedures

### Java Entities:
- `src/java/entities/Company.java` - Company entity
- `src/java/entities/UserDetail.java` - Updated with CompanyId field

### Java Beans:
- `src/java/beans/CompanyBean.java` - Company CRUD operations
- `src/java/beans/UserRegistrationBean.java` - Registration logic
- `src/java/beans/UserManagementCRMBean.java` - CRM dashboard logic
- `src/java/beans/UserDetailBean.java` - Updated to load company_id
- `src/java/beans/NavigationBean.java` - Added CRM navigation

### XHTML Pages:
- `web/Registration.xhtml` - Public registration form
- `web/UserManagementCRM.xhtml` - CRM dashboard
- `web/Login.xhtml` - Updated with registration link
- `web/Menu.xhtml` - Updated with CRM menu item

---

## CONTACT & QUESTIONS

For issues or questions about this implementation:
1. Check the troubleshooting section above
2. Review application logs in `/opt/tomcat/logs/catalina.out`
3. Check MySQL logs for database errors
4. Verify all files were deployed correctly

**Created:** March 9, 2026  
**Version:** 1.0  
**System:** VendorPlus Self-Service Registration + CRM
