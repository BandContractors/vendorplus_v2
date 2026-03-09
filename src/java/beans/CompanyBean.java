package beans;

import connections.DBConnection;
import entities.Company;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Company Bean for Multi-Tenant Support
 * 
 * @author VendorPlus Team
 */
@ManagedBean
@SessionScoped
public class CompanyBean implements Serializable {

    private static final long serialVersionUID = 1L;
    static Logger LOGGER = Logger.getLogger(CompanyBean.class.getName());
    
    private List<Company> CompanyList;
    private Company SelectedCompany;

    /**
     * Set company data from ResultSet
     */
    public void setCompanyFromResultset(Company aCompany, ResultSet aResultSet) {
        try {
            aCompany.setCompanyId(aResultSet.getInt("company_id"));
            aCompany.setCompanyCode(aResultSet.getString("company_code") != null ? aResultSet.getString("company_code") : "");
            aCompany.setCompanyName(aResultSet.getString("company_name") != null ? aResultSet.getString("company_name") : "");
            aCompany.setTaxId(aResultSet.getString("tax_id") != null ? aResultSet.getString("tax_id") : "");
            aCompany.setAddress(aResultSet.getString("address") != null ? aResultSet.getString("address") : "");
            aCompany.setCity(aResultSet.getString("city") != null ? aResultSet.getString("city") : "");
            aCompany.setCountry(aResultSet.getString("country") != null ? aResultSet.getString("country") : "");
            aCompany.setRegistrationDate(aResultSet.getTimestamp("registration_date"));
            aCompany.setIsActive(aResultSet.getString("is_active") != null ? aResultSet.getString("is_active") : "Yes");
            aCompany.setContactPerson(aResultSet.getString("contact_person") != null ? aResultSet.getString("contact_person") : "");
            aCompany.setContactEmail(aResultSet.getString("contact_email") != null ? aResultSet.getString("contact_email") : "");
            aCompany.setContactPhone(aResultSet.getString("contact_phone") != null ? aResultSet.getString("contact_phone") : "");
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    /**
     * Get company by ID
     */
    public Company getCompanyById(int companyId) {
        String sql = "{call sp_search_company_by_id(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, companyId);
            rs = ps.executeQuery();
            if (rs.next()) {
                Company company = new Company();
                this.setCompanyFromResultset(company, rs);
                return company;
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
        return null;
    }

    /**
     * Get company by code
     */
    public Company getCompanyByCode(String companyCode) {
        String sql = "{call sp_search_company_by_code(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, companyCode);
            rs = ps.executeQuery();
            if (rs.next()) {
                Company company = new Company();
                this.setCompanyFromResultset(company, rs);
                return company;
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
        return null;
    }

    /**
     * Insert new company and return the generated company_id
     */
    public int insertCompany(Company company) {
        String sql = "{call sp_insert_company(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, company.getCompanyCode());
            ps.setString(2, company.getCompanyName());
            ps.setString(3, company.getTaxId());
            ps.setString(4, company.getAddress());
            ps.setString(5, company.getCity());
            ps.setString(6, company.getCountry());
            ps.setString(7, company.getContactPerson());
            ps.setString(8, company.getContactEmail());
            ps.setString(9, company.getContactPhone());
            
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("company_id");
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
     * Check if tax ID already exists
     */
    public boolean isTaxIdExists(String taxId) {
        if (taxId == null || taxId.trim().isEmpty()) {
            return false;
        }
        
        String sql = "{call sp_check_tax_id_exists(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, taxId);
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
     * Get all active companies
     */
    public List<Company> getAllActiveCompanies() {
        this.CompanyList = new ArrayList<>();
        String sql = "SELECT * FROM company WHERE is_active = 'Yes' ORDER BY company_name";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                Company company = new Company();
                this.setCompanyFromResultset(company, rs);
                this.CompanyList.add(company);
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
        return this.CompanyList;
    }

    // Getters and Setters
    public List<Company> getCompanyList() {
        return CompanyList;
    }

    public void setCompanyList(List<Company> CompanyList) {
        this.CompanyList = CompanyList;
    }

    public Company getSelectedCompany() {
        return SelectedCompany;
    }

    public void setSelectedCompany(Company SelectedCompany) {
        this.SelectedCompany = SelectedCompany;
    }
}
