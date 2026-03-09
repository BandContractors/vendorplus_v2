package beans;

import connections.DBConnection;
import entities.Company;
import entities.UserDetail;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import utilities.Security;
import utilities.CustomValidator;

/**
 * User Registration Bean for Self-Service Registration
 * 
 * @author VendorPlus Team
 */
@ManagedBean
@SessionScoped
public class UserRegistrationBean implements Serializable {

    private static final long serialVersionUID = 1L;
    static Logger LOGGER = Logger.getLogger(UserRegistrationBean.class.getName());
    
    // User fields
    private String UserName;
    private String Password;
    private String PasswordConfirm;
    private String FirstName;
    private String SecondName;
    private String ThirdName;
    private String EmailAddress;
    private String PhoneNo;
    private String TransCode;
    
    // Company fields
    private String CompanyName;
    private String TaxId;
    private String Address;
    private String City;
    private String Country;
    
    // Status messages
    private String ActionMessageSuccess;
    private String ActionMessageFailure;
    private boolean RegistrationSuccessful = false;

    /**
     * Register new user with company
     */
    public void registerUser() {
        this.ActionMessageSuccess = null;
        this.ActionMessageFailure = null;
        this.RegistrationSuccessful = false;
        
        try {
            // Validate input
            if (!validateRegistrationData()) {
                return;
            }
            
            // Check if username already exists
            if (isUsernameExists(this.UserName)) {
                this.ActionMessageFailure = "Username already exists. Please choose a different username.";
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessageFailure));
                return;
            }
            
            // Check if email already exists
            if (isEmailExists(this.EmailAddress)) {
                this.ActionMessageFailure = "Email address already registered. Please use a different email or login with existing account.";
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessageFailure));
                return;
            }
            
            // Generate unique company code from company name
            String companyCode = generateCompanyCode(this.CompanyName);
            
            // Check if tax ID exists (if provided)
            CompanyBean companyBean = new CompanyBean();
            if (this.TaxId != null && !this.TaxId.trim().isEmpty()) {
                if (companyBean.isTaxIdExists(this.TaxId)) {
                    this.ActionMessageFailure = "Tax ID already registered. Please contact support@vendorplus.com if your company is already registered.";
                    FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessageFailure));
                    return;
                }
            }
            
            // Create company first
            Company company = new Company();
            company.setCompanyCode(companyCode);
            company.setCompanyName(this.CompanyName != null ? this.CompanyName : "");
            company.setTaxId(this.TaxId != null ? this.TaxId : "");
            company.setAddress(this.Address != null ? this.Address : "");
            company.setCity(this.City != null ? this.City : "");
            company.setCountry(this.Country != null ? this.Country : "");
            company.setContactPerson(this.FirstName + " " + (this.SecondName != null ? this.SecondName : ""));
            company.setContactEmail(this.EmailAddress);
            company.setContactPhone(this.PhoneNo != null ? this.PhoneNo : "");
            company.setIsActive("Yes");
            
            int companyId = companyBean.insertCompany(company);
            
            if (companyId == 0) {
                this.ActionMessageFailure = "Failed to create company record. Please try again or contact support.";
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessageFailure));
                return;
            }
            
            // Create user
            String encryptedPassword = Security.Encrypt(this.Password);
            String encryptedTransCode = Security.Encrypt(this.TransCode != null ? this.TransCode : "0000");
            
            int userId = insertUserWithCompany(
                companyId,
                this.UserName,
                encryptedPassword,
                this.FirstName,
                this.SecondName != null ? this.SecondName : "",
                this.ThirdName != null ? this.ThirdName : "",
                this.EmailAddress,
                this.PhoneNo != null ? this.PhoneNo : "",
                encryptedTransCode,
                "en",  // Default language
                "en"   // Default output language
            );
            
            if (userId > 0) {
                this.ActionMessageSuccess = "Registration successful! You can now login with your username and password.";
                this.RegistrationSuccessful = true;
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", this.ActionMessageSuccess));
                
                // Clear form
                clearForm();
                
            } else {
                this.ActionMessageFailure = "Failed to create user account. Please try again or contact support.";
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessageFailure));
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            this.ActionMessageFailure = "An error occurred during registration. Please try again later.";
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessageFailure));
        }
    }

    /**
     * Validate registration data
     */
    private boolean validateRegistrationData() {
        CustomValidator validator = new CustomValidator();
        
        // Required fields
        if (this.UserName == null || this.UserName.trim().isEmpty()) {
            this.ActionMessageFailure = "Username is required.";
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessageFailure));
            return false;
        }
        
        if (this.Password == null || this.Password.trim().isEmpty()) {
            this.ActionMessageFailure = "Password is required.";
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessageFailure));
            return false;
        }
        
        if (this.PasswordConfirm == null || !this.Password.equals(this.PasswordConfirm)) {
            this.ActionMessageFailure = "Passwords do not match.";
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessageFailure));
            return false;
        }
        
        if (this.Password.length() < 6) {
            this.ActionMessageFailure = "Password must be at least 6 characters long.";
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessageFailure));
            return false;
        }
        
        if (this.FirstName == null || this.FirstName.trim().isEmpty()) {
            this.ActionMessageFailure = "First Name is required.";
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessageFailure));
            return false;
        }
        
        if (this.EmailAddress == null || this.EmailAddress.trim().isEmpty()) {
            this.ActionMessageFailure = "Email Address is required.";
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessageFailure));
            return false;
        }
        
        // Validate email format
        if (!validator.isValidEmail(this.EmailAddress)) {
            this.ActionMessageFailure = "Invalid email address format.";
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessageFailure));
            return false;
        }
        
        if (this.CompanyName == null || this.CompanyName.trim().isEmpty()) {
            this.ActionMessageFailure = "Company/Organization Name is required.";
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", this.ActionMessageFailure));
            return false;
        }
        
        return true;
    }

    /**
     * Check if username exists
     */
    private boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) as count FROM user_detail WHERE user_name = ?";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
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
        return false;
    }

    /**
     * Check if email exists
     */
    private boolean isEmailExists(String email) {
        String sql = "SELECT COUNT(*) as count FROM user_detail WHERE email_address = ?";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, email);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
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
        return false;
    }

    /**
     * Generate unique company code from company name
     */
    private String generateCompanyCode(String companyName) {
        // Remove special characters and spaces, take first 10 chars, add timestamp
        String baseCode = companyName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (baseCode.length() > 10) {
            baseCode = baseCode.substring(0, 10);
        }
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
        return baseCode + timestamp;
    }

    /**
     * Insert user with company using stored procedure
     */
    private int insertUserWithCompany(int companyId, String userName, String password, 
            String firstName, String secondName, String thirdName, String email, 
            String phone, String transCode, String langSystem, String langOutput) {
        
        String sql = "{call sp_insert_user_with_company(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, companyId);
            ps.setString(2, userName);
            ps.setString(3, password);
            ps.setString(4, firstName);
            ps.setString(5, secondName);
            ps.setString(6, thirdName);
            ps.setString(7, email);
            ps.setString(8, phone);
            ps.setString(9, transCode);
            ps.setString(10, langSystem);
            ps.setString(11, langOutput);
            
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_detail_id");
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
        return 0;
    }

    /**
     * Clear form after successful registration
     */
    private void clearForm() {
        this.UserName = null;
        this.Password = null;
        this.PasswordConfirm = null;
        this.FirstName = null;
        this.SecondName = null;
        this.ThirdName = null;
        this.EmailAddress = null;
        this.PhoneNo = null;
        this.TransCode = null;
        this.CompanyName = null;
        this.TaxId = null;
        this.Address = null;
        this.City = null;
        this.Country = null;
    }

    // Getters and Setters
    public String getUserName() {
        return UserName;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String Password) {
        this.Password = Password;
    }

    public String getPasswordConfirm() {
        return PasswordConfirm;
    }

    public void setPasswordConfirm(String PasswordConfirm) {
        this.PasswordConfirm = PasswordConfirm;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String FirstName) {
        this.FirstName = FirstName;
    }

    public String getSecondName() {
        return SecondName;
    }

    public void setSecondName(String SecondName) {
        this.SecondName = SecondName;
    }

    public String getThirdName() {
        return ThirdName;
    }

    public void setThirdName(String ThirdName) {
        this.ThirdName = ThirdName;
    }

    public String getEmailAddress() {
        return EmailAddress;
    }

    public void setEmailAddress(String EmailAddress) {
        this.EmailAddress = EmailAddress;
    }

    public String getPhoneNo() {
        return PhoneNo;
    }

    public void setPhoneNo(String PhoneNo) {
        this.PhoneNo = PhoneNo;
    }

    public String getTransCode() {
        return TransCode;
    }

    public void setTransCode(String TransCode) {
        this.TransCode = TransCode;
    }

    public String getCompanyName() {
        return CompanyName;
    }

    public void setCompanyName(String CompanyName) {
        this.CompanyName = CompanyName;
    }

    public String getTaxId() {
        return TaxId;
    }

    public void setTaxId(String TaxId) {
        this.TaxId = TaxId;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String Address) {
        this.Address = Address;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String City) {
        this.City = City;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String Country) {
        this.Country = Country;
    }

    public String getActionMessageSuccess() {
        return ActionMessageSuccess;
    }

    public void setActionMessageSuccess(String ActionMessageSuccess) {
        this.ActionMessageSuccess = ActionMessageSuccess;
    }

    public String getActionMessageFailure() {
        return ActionMessageFailure;
    }

    public void setActionMessageFailure(String ActionMessageFailure) {
        this.ActionMessageFailure = ActionMessageFailure;
    }

    public boolean isRegistrationSuccessful() {
        return RegistrationSuccessful;
    }

    public void setRegistrationSuccessful(boolean RegistrationSuccessful) {
        this.RegistrationSuccessful = RegistrationSuccessful;
    }
}
