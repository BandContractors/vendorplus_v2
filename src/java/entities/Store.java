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
public class Store implements Serializable {

    private static final long serialVersionUID = 1L;
    private int StoreId;
    private int CompanyId;
    private String StoreName;
    private String store_code;
    private int shift_mode;

    /**
     * @return the StoreId
     */
    public int getStoreId() {
        return StoreId;
    }

    /**
     * @param StoreId the StoreId to set
     */
    public void setStoreId(int StoreId) {
        this.StoreId = StoreId;
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

    /**
     * @return the StoreName
     */
    public String getStoreName() {
        return StoreName;
    }

    /**
     * @param StoreName the StoreName to set
     */
    public void setStoreName(String StoreName) {
        this.StoreName = StoreName;
    }

    /**
     * @return the store_code
     */
    public String getStore_code() {
        return store_code;
    }

    /**
     * @param store_code the store_code to set
     */
    public void setStore_code(String store_code) {
        this.store_code = store_code;
    }

    /**
     * @return the shift_mode
     */
    public int getShift_mode() {
        return shift_mode;
    }

    /**
     * @param shift_mode the shift_mode to set
     */
    public void setShift_mode(int shift_mode) {
        this.shift_mode = shift_mode;
    }
}
