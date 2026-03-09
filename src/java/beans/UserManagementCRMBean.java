package beans;

import connections.DBConnection;
import entities.UserDetail;
import entities.Company;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User Management CRM Bean
 * For admin dashboard to monitor and manage users
 * 
 * @author VendorPlus Team
 */
@ManagedBean
@SessionScoped
public class UserManagementCRMBean implements Serializable {

    private static final long serialVersionUID = 1L;
    static Logger LOGGER = Logger.getLogger(UserManagementCRMBean.class.getName());
    
    // User list and filters
    private List<CRMUserDTO> UserList;
    private String SearchText = "";
    private String StatusFilter = "ALL";  // ALL, ACTIVE, LOCKED
    private CRMUserDTO SelectedUser;
    
    // Login activity
    private List<LoginActivityDTO> LoginActivityList;
    private int ActivityLimit = 50;
    
    // Analytics
    private int TotalUsers = 0;
    private int ActiveUsers = 0;
    private int LockedUsers = 0;
    private List<RegistrationStatDTO> RegistrationStats;
    private int StatsDaysBack = 30;
    
    // Messages
    private String ActionMessage;
    private String ActionMessageType;  // SUCCESS, ERROR

    /**
     * Get current user's company ID from session
     */
    private int getCurrentCompanyId() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            UserDetail currentUser = (UserDetail) session.getAttribute("CURRENT_USER");
            if (currentUser != null) {
                return currentUser.getCompanyId();
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return 1;  // Default company
    }

    /**
     * Load user list with filters (Priority #1)
     */
    public void loadUserList() {
        this.UserList = new ArrayList<>();
        int companyId = getCurrentCompanyId();
        
        String sql = "{call sp_search_users_for_crm(?, ?, ?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, companyId);
            ps.setString(2, this.SearchText);
            ps.setString(3, this.StatusFilter);
            
            rs = ps.executeQuery();
            while (rs.next()) {
                CRMUserDTO user = new CRMUserDTO();
                user.setUserId(rs.getInt("user_detail_id"));
                user.setUserName(rs.getString("user_name"));
                user.setFirstName(rs.getString("first_name"));
                user.setSecondName(rs.getString("second_name"));
                user.setThirdName(rs.getString("third_name"));
                user.setEmailAddress(rs.getString("email_address"));
                user.setPhoneNo(rs.getString("phone_no"));
                user.setIsLocked(rs.getString("is_user_locked"));
                user.setIsAdmin(rs.getString("is_user_gen_admin"));
                user.setRegistrationDate(rs.getTimestamp("add_date"));
                user.setCompanyName(rs.getString("company_name"));
                user.setLastLogin(rs.getTimestamp("last_login"));
                this.UserList.add(user);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            this.ActionMessage = "Error loading user list: " + e.getMessage();
            this.ActionMessageType = "ERROR";
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    /**
     * Load login activity (Priority #3)
     */
    public void loadLoginActivity() {
        this.LoginActivityList = new ArrayList<>();
        int companyId = getCurrentCompanyId();
        
        String sql = "{call sp_get_recent_login_activity(?, ?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, companyId);
            ps.setInt(2, this.ActivityLimit);
            
            rs = ps.executeQuery();
            while (rs.next()) {
                LoginActivityDTO activity = new LoginActivityDTO();
                activity.setLoginSessionId(rs.getInt("login_session_id"));
                activity.setUserId(rs.getInt("user_detail_id"));
                activity.setUserName(rs.getString("user_name"));
                activity.setFullName(rs.getString("full_name"));
                activity.setLoginTime(rs.getTimestamp("login_timestamp"));
                activity.setLogoutTime(rs.getTimestamp("logout_timestamp"));
                activity.setIpAddress(rs.getString("remote_ip"));
                activity.setHostName(rs.getString("remote_host"));
                activity.setStoreName(rs.getString("store_name"));
                this.LoginActivityList.add(activity);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            this.ActionMessage = "Error loading login activity: " + e.getMessage();
            this.ActionMessageType = "ERROR";
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    /**
     * Load analytics/statistics (Priority #4)
     */
    public void loadAnalytics() {
        int companyId = getCurrentCompanyId();
        
        // Load user counts
        String sql1 = "{call sp_count_users_by_company(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql1);) {
            ps.setInt(1, companyId);
            rs = ps.executeQuery();
            if (rs.next()) {
                this.TotalUsers = rs.getInt("total_users");
                this.ActiveUsers = rs.getInt("active_users");
                this.LockedUsers = rs.getInt("locked_users");
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
        
        // Load registration statistics
        this.RegistrationStats = new ArrayList<>();
        String sql2 = "{call sp_get_registration_stats(?, ?)}";
        rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql2);) {
            ps.setInt(1, companyId);
            ps.setInt(2, this.StatsDaysBack);
            
            rs = ps.executeQuery();
            while (rs.next()) {
                RegistrationStatDTO stat = new RegistrationStatDTO();
                stat.setRegistrationDate(rs.getDate("registration_date"));
                stat.setRegistrationsCount(rs.getInt("registrations_count"));
                this.RegistrationStats.add(stat);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    /**
     * Initialize dashboard - load all data
     */
    public void initializeDashboard() {
        loadAnalytics();
        loadUserList();
        loadLoginActivity();
    }

    /**
     * Lock user (Priority #5 - User Management Actions)
     */
    public void lockUser(CRMUserDTO user) {
        String sql = "UPDATE user_detail SET is_user_locked = 'Yes' WHERE user_detail_id = ?";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, user.getUserId());
            int updated = ps.executeUpdate();
            if (updated > 0) {
                this.ActionMessage = "User '" + user.getUserName() + "' has been locked successfully.";
                this.ActionMessageType = "SUCCESS";
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", this.ActionMessage));
                loadUserList();
                loadAnalytics();
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            this.ActionMessage = "Error locking user: " + e.getMessage();
            this.ActionMessageType = "ERROR";
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessage));
        }
    }

    /**
     * Unlock user (Priority #5 - User Management Actions)
     */
    public void unlockUser(CRMUserDTO user) {
        String sql = "UPDATE user_detail SET is_user_locked = 'No' WHERE user_detail_id = ?";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, user.getUserId());
            int updated = ps.executeUpdate();
            if (updated > 0) {
                this.ActionMessage = "User '" + user.getUserName() + "' has been unlocked successfully.";
                this.ActionMessageType = "SUCCESS";
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", this.ActionMessage));
                loadUserList();
                loadAnalytics();
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            this.ActionMessage = "Error unlocking user: " + e.getMessage();
            this.ActionMessageType = "ERROR";
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessage));
        }
    }

    /**
     * Delete user (Priority #5 - User Management Actions)
     */
    public void deleteUser(CRMUserDTO user) {
        // First check if user has transactions
        String checkSql = "SELECT COUNT(*) as count FROM login_session WHERE user_detail_id = ?";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(checkSql);) {
            ps.setInt(1, user.getUserId());
            rs = ps.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {
                this.ActionMessage = "Cannot delete user '" + user.getUserName() + "' - user has login history. Consider locking instead.";
                this.ActionMessageType = "ERROR";
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", this.ActionMessage));
                return;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
        
        // Delete user
        String sql = "DELETE FROM user_detail WHERE user_detail_id = ?";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, user.getUserId());
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                this.ActionMessage = "User '" + user.getUserName() + "' has been deleted successfully.";
                this.ActionMessageType = "SUCCESS";
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", this.ActionMessage));
                loadUserList();
                loadAnalytics();
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            this.ActionMessage = "Error deleting user: " + e.getMessage();
            this.ActionMessageType = "ERROR";
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessage));
        }
    }

    /**
     * Apply filters to user list
     */
    public void applyFilters() {
        loadUserList();
    }

    /**
     * Clear filters
     */
    public void clearFilters() {
        this.SearchText = "";
        this.StatusFilter = "ALL";
        loadUserList();
    }

    // DTO Classes for CRM data
    public static class CRMUserDTO implements Serializable {
        private int userId;
        private String userName;
        private String firstName;
        private String secondName;
        private String thirdName;
        private String emailAddress;
        private String phoneNo;
        private String isLocked;
        private String isAdmin;
        private Date registrationDate;
        private String companyName;
        private Date lastLogin;

        // Getters and Setters
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getSecondName() { return secondName; }
        public void setSecondName(String secondName) { this.secondName = secondName; }
        public String getThirdName() { return thirdName; }
        public void setThirdName(String thirdName) { this.thirdName = thirdName; }
        public String getEmailAddress() { return emailAddress; }
        public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
        public String getPhoneNo() { return phoneNo; }
        public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }
        public String getIsLocked() { return isLocked; }
        public void setIsLocked(String isLocked) { this.isLocked = isLocked; }
        public String getIsAdmin() { return isAdmin; }
        public void setIsAdmin(String isAdmin) { this.isAdmin = isAdmin; }
        public Date getRegistrationDate() { return registrationDate; }
        public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public Date getLastLogin() { return lastLogin; }
        public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }
        
        public String getFullName() {
            return firstName + " " + (secondName != null ? secondName : "") + " " + (thirdName != null ? thirdName : "");
        }
        
        public String getStatusBadge() {
            return "Yes".equals(isLocked) ? "Locked" : "Active";
        }
    }

    public static class LoginActivityDTO implements Serializable {
        private int loginSessionId;
        private int userId;
        private String userName;
        private String fullName;
        private Date loginTime;
        private Date logoutTime;
        private String ipAddress;
        private String hostName;
        private String storeName;

        // Getters and Setters
        public int getLoginSessionId() { return loginSessionId; }
        public void setLoginSessionId(int loginSessionId) { this.loginSessionId = loginSessionId; }
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public Date getLoginTime() { return loginTime; }
        public void setLoginTime(Date loginTime) { this.loginTime = loginTime; }
        public Date getLogoutTime() { return logoutTime; }
        public void setLogoutTime(Date logoutTime) { this.logoutTime = logoutTime; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public String getHostName() { return hostName; }
        public void setHostName(String hostName) { this.hostName = hostName; }
        public String getStoreName() { return storeName; }
        public void setStoreName(String storeName) { this.storeName = storeName; }
        
        public String getStatus() {
            return logoutTime != null ? "Logged Out" : "Active";
        }
    }

    public static class RegistrationStatDTO implements Serializable {
        private Date registrationDate;
        private int registrationsCount;

        // Getters and Setters
        public Date getRegistrationDate() { return registrationDate; }
        public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }
        public int getRegistrationsCount() { return registrationsCount; }
        public void setRegistrationsCount(int registrationsCount) { this.registrationsCount = registrationsCount; }
        
        // Computed property for chart bar width
        public String getBarWidthStyle() {
            return "width: " + (registrationsCount * 20) + "px";
        }
    }

    // Getters and Setters
    public List<CRMUserDTO> getUserList() {
        if (UserList == null) {
            loadUserList();
        }
        return UserList;
    }

    public void setUserList(List<CRMUserDTO> UserList) {
        this.UserList = UserList;
    }

    public String getSearchText() {
        return SearchText;
    }

    public void setSearchText(String SearchText) {
        this.SearchText = SearchText;
    }

    public String getStatusFilter() {
        return StatusFilter;
    }

    public void setStatusFilter(String StatusFilter) {
        this.StatusFilter = StatusFilter;
    }

    public CRMUserDTO getSelectedUser() {
        return SelectedUser;
    }

    public void setSelectedUser(CRMUserDTO SelectedUser) {
        this.SelectedUser = SelectedUser;
    }

    public List<LoginActivityDTO> getLoginActivityList() {
        if (LoginActivityList == null) {
            loadLoginActivity();
        }
        return LoginActivityList;
    }

    public void setLoginActivityList(List<LoginActivityDTO> LoginActivityList) {
        this.LoginActivityList = LoginActivityList;
    }

    public int getActivityLimit() {
        return ActivityLimit;
    }

    public void setActivityLimit(int ActivityLimit) {
        this.ActivityLimit = ActivityLimit;
    }

    public int getTotalUsers() {
        return TotalUsers;
    }

    public void setTotalUsers(int TotalUsers) {
        this.TotalUsers = TotalUsers;
    }

    public int getActiveUsers() {
        return ActiveUsers;
    }

    public void setActiveUsers(int ActiveUsers) {
        this.ActiveUsers = ActiveUsers;
    }

    public int getLockedUsers() {
        return LockedUsers;
    }

    public void setLockedUsers(int LockedUsers) {
        this.LockedUsers = LockedUsers;
    }

    public List<RegistrationStatDTO> getRegistrationStats() {
        return RegistrationStats;
    }

    public void setRegistrationStats(List<RegistrationStatDTO> RegistrationStats) {
        this.RegistrationStats = RegistrationStats;
    }

    public int getStatsDaysBack() {
        return StatsDaysBack;
    }

    public void setStatsDaysBack(int StatsDaysBack) {
        this.StatsDaysBack = StatsDaysBack;
    }

    public String getActionMessage() {
        return ActionMessage;
    }

    public void setActionMessage(String ActionMessage) {
        this.ActionMessage = ActionMessage;
    }

    public String getActionMessageType() {
        return ActionMessageType;
    }

    public void setActionMessageType(String ActionMessageType) {
        this.ActionMessageType = ActionMessageType;
    }
}
