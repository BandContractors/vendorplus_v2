package entities;

import java.io.Serializable;
import java.util.Date;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * Company Entity for Multi-Tenant Support
 * 
 * @author VendorPlus Team
 */
@ManagedBean
@SessionScoped
public class Company implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private int CompanyId;
    private String CompanyCode;
    private String CompanyName;
    private String TaxId;
    private String Address;
    private String City;
    private String Country;
    private Date RegistrationDate;
    private String IsActive;
    private String ContactPerson;
    private String ContactEmail;
    private String ContactPhone;

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

    /**
     * @return the CompanyCode
     */
    public String getCompanyCode() {
        return CompanyCode;
    }

    /**
     * @param CompanyCode the CompanyCode to set
     */
    public void setCompanyCode(String CompanyCode) {
        this.CompanyCode = CompanyCode;
    }

    /**
     * @return the CompanyName
     */
    public String getCompanyName() {
        return CompanyName;
    }

    /**
     * @param CompanyName the CompanyName to set
     */
    public void setCompanyName(String CompanyName) {
        this.CompanyName = CompanyName;
    }

    /**
     * @return the TaxId
     */
    public String getTaxId() {
        return TaxId;
    }

    /**
     * @param TaxId the TaxId to set
     */
    public void setTaxId(String TaxId) {
        this.TaxId = TaxId;
    }

    /**
     * @return the Address
     */
    public String getAddress() {
        return Address;
    }

    /**
     * @param Address the Address to set
     */
    public void setAddress(String Address) {
        this.Address = Address;
    }

    /**
     * @return the City
     */
    public String getCity() {
        return City;
    }

    /**
     * @param City the City to set
     */
    public void setCity(String City) {
        this.City = City;
    }

    /**
     * @return the Country
     */
    public String getCountry() {
        return Country;
    }

    /**
     * @param Country the Country to set
     */
    public void setCountry(String Country) {
        this.Country = Country;
    }

    /**
     * @return the RegistrationDate
     */
    public Date getRegistrationDate() {
        return RegistrationDate;
    }

    /**
     * @param RegistrationDate the RegistrationDate to set
     */
    public void setRegistrationDate(Date RegistrationDate) {
        this.RegistrationDate = RegistrationDate;
    }

    /**
     * @return the IsActive
     */
    public String getIsActive() {
        return IsActive;
    }

    /**
     * @param IsActive the IsActive to set
     */
    public void setIsActive(String IsActive) {
        this.IsActive = IsActive;
    }

    /**
     * @return the ContactPerson
     */
    public String getContactPerson() {
        return ContactPerson;
    }

    /**
     * @param ContactPerson the ContactPerson to set
     */
    public void setContactPerson(String ContactPerson) {
        this.ContactPerson = ContactPerson;
    }

    /**
     * @return the ContactEmail
     */
    public String getContactEmail() {
        return ContactEmail;
    }

    /**
     * @param ContactEmail the ContactEmail to set
     */
    public void setContactEmail(String ContactEmail) {
        this.ContactEmail = ContactEmail;
    }

    /**
     * @return the ContactPhone
     */
    public String getContactPhone() {
        return ContactPhone;
    }

    /**
     * @param ContactPhone the ContactPhone to set
     */
    public void setContactPhone(String ContactPhone) {
        this.ContactPhone = ContactPhone;
    }
}
