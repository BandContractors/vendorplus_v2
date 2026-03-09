package entities;


import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author btwesigye
 */
@ManagedBean
@SessionScoped
public class UserDetail implements Serializable {

    private static final long serialVersionUID = 1L;
    private int UserDetailId;
    private int CompanyId;
    private String UserName;
    private String UserPassword;
    private String UserPasswordConfirm;
    private String FirstName;
    private String SecondName;
    private String ThirdName;
    private String UserImgUrl;
    private String IsUserLocked;
    private String IsUserGenAdmin;
    private String OldUserPassword;
    private String NewUserPassword;
    private String NewUserPasswordConfirm;
    private int UserCategoryId;
    private String email_address;
    private String phone_no;
    private String trans_code;
    private String new_trans_code;
    private String language_system;
    private String language_output;

    /**
     * @return the UserDetailId
     */
    public int getUserDetailId() {
        return UserDetailId;
    }

    /**
     * @param UserDetailId the UserDetailId to set
     */
    public void setUserDetailId(int UserDetailId) {
        this.UserDetailId = UserDetailId;
    }

    /**
     * @return the UserName
     */
    public String getUserName() {
        return UserName;
    }

    /**
     * @param UserName the UserName to set
     */
    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    /**
     * @return the UserPassword
     */
    public String getUserPassword() {
        return UserPassword;
    }

    /**
     * @param UserPassword the UserPassword to set
     */
    public void setUserPassword(String UserPassword) {
        this.UserPassword = UserPassword;
    }

    /**
     * @return the UserImgUrl
     */
    public String getUserImgUrl() {
        return UserImgUrl;
    }

    /**
     * @param UserImgUrl the UserImgUrl to set
     */
    public void setUserImgUrl(String UserImgUrl) {
        this.UserImgUrl = UserImgUrl;
    }

    /**
     * @return the IsUserLocked
     */
    public String getIsUserLocked() {
        return IsUserLocked;
    }

    /**
     * @param IsUserLocked the IsUserLocked to set
     */
    public void setIsUserLocked(String IsUserLocked) {
        this.IsUserLocked = IsUserLocked;
    }

    /**
     * @return the IsUserGenAdmin
     */
    public String getIsUserGenAdmin() {
        return IsUserGenAdmin;
    }

    /**
     * @param IsUserGenAdmin the IsUserGenAdmin to set
     */
    public void setIsUserGenAdmin(String IsUserGenAdmin) {
        this.IsUserGenAdmin = IsUserGenAdmin;
    }

    /**
     * @return the FirstName
     */
    public String getFirstName() {
        return FirstName;
    }

    /**
     * @param FirstName the FirstName to set
     */
    public void setFirstName(String FirstName) {
        this.FirstName = FirstName;
    }

    /**
     * @return the SecondName
     */
    public String getSecondName() {
        return SecondName;
    }

    /**
     * @param SecondName the SecondName to set
     */
    public void setSecondName(String SecondName) {
        this.SecondName = SecondName;
    }

    /**
     * @return the ThirdName
     */
    public String getThirdName() {
        return ThirdName;
    }

    /**
     * @param ThirdName the ThirdName to set
     */
    public void setThirdName(String ThirdName) {
        this.ThirdName = ThirdName;
    }

    /**
     * @return the UserPasswordConfirm
     */
    public String getUserPasswordConfirm() {
        return UserPasswordConfirm;
    }

    /**
     * @param UserPasswordConfirm the UserPasswordConfirm to set
     */
    public void setUserPasswordConfirm(String UserPasswordConfirm) {
        this.UserPasswordConfirm = UserPasswordConfirm;
    }

    /**
     * @return the OldUserPassword
     */
    public String getOldUserPassword() {
        return OldUserPassword;
    }

    /**
     * @param OldUserPassword the OldUserPassword to set
     */
    public void setOldUserPassword(String OldUserPassword) {
        this.OldUserPassword = OldUserPassword;
    }

    /**
     * @return the NewUserPassword
     */
    public String getNewUserPassword() {
        return NewUserPassword;
    }

    /**
     * @param NewUserPassword the NewUserPassword to set
     */
    public void setNewUserPassword(String NewUserPassword) {
        this.NewUserPassword = NewUserPassword;
    }

    /**
     * @return the NewUserPasswordConfirm
     */
    public String getNewUserPasswordConfirm() {
        return NewUserPasswordConfirm;
    }

    /**
     * @param NewUserPasswordConfirm the NewUserPasswordConfirm to set
     */
    public void setNewUserPasswordConfirm(String NewUserPasswordConfirm) {
        this.NewUserPasswordConfirm = NewUserPasswordConfirm;
    }

    /**
     * @return the UserCategoryId
     */
    public int getUserCategoryId() {
        return UserCategoryId;
    }

    /**
     * @param UserCategoryId the UserCategoryId to set
     */
    public void setUserCategoryId(int UserCategoryId) {
        this.UserCategoryId = UserCategoryId;
    }

    /**
     * @return the email_address
     */
    public String getEmail_address() {
        return email_address;
    }

    /**
     * @param email_address the email_address to set
     */
    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    /**
     * @return the phone_no
     */
    public String getPhone_no() {
        return phone_no;
    }

    /**
     * @param phone_no the phone_no to set
     */
    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }

    /**
     * @return the trans_code
     */
    public String getTrans_code() {
        return trans_code;
    }

    /**
     * @param trans_code the trans_code to set
     */
    public void setTrans_code(String trans_code) {
        this.trans_code = trans_code;
    }

    /**
     * @return the new_trans_code
     */
    public String getNew_trans_code() {
        return new_trans_code;
    }

    /**
     * @param new_trans_code the new_trans_code to set
     */
    public void setNew_trans_code(String new_trans_code) {
        this.new_trans_code = new_trans_code;
    }

    /**
     * @return the language_system
     */
    public String getLanguage_system() {
        return language_system;
    }

    /**
     * @param language_system the language_system to set
     */
    public void setLanguage_system(String language_system) {
        this.language_system = language_system;
    }

    /**
     * @return the language_output
     */
    public String getLanguage_output() {
        return language_output;
    }

    /**
     * @param language_output the language_output to set
     */
    public void setLanguage_output(String language_output) {
        this.language_output = language_output;
    }

    /**
     * @return the CompanyId
     */
    public int getCompanyId() {
        return CompanyId;
    }

    /**
     * @param CompanyId the CompanyId to set
     */
    public void setCompanyId(int CompanyId) {
        this.CompanyId = CompanyId;
    }
    
}
