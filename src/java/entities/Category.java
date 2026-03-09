package entities;


import java.io.Serializable;
import javax.faces.bean.*;

@ManagedBean
@SessionScoped
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;
    private int CategoryId;
    private int CompanyId;
    private String CategoryName;
    private int display_quick_order;
    private int list_rank;
    private int store_quick_order;

    /**
     * @return the CategoryId
     */
    public int getCategoryId() {
        return CategoryId;
    }

    /**
     * @param CategoryId the CategoryId to set
     */
    public void setCategoryId(int CategoryId) {
        this.CategoryId = CategoryId;
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
     * @return the CategoryName
     */
    public String getCategoryName() {
        return CategoryName;
    }

    /**
     * @param CategoryName the CategoryName to set
     */
    public void setCategoryName(String CategoryName) {
        this.CategoryName = CategoryName;
    }

    /**
     * @return the display_quick_order
     */
    public int getDisplay_quick_order() {
        return display_quick_order;
    }

    /**
     * @param display_quick_order the display_quick_order to set
     */
    public void setDisplay_quick_order(int display_quick_order) {
        this.display_quick_order = display_quick_order;
    }

    /**
     * @return the list_rank
     */
    public int getList_rank() {
        return list_rank;
    }

    /**
     * @param list_rank the list_rank to set
     */
    public void setList_rank(int list_rank) {
        this.list_rank = list_rank;
    }

    /**
     * @return the store_quick_order
     */
    public int getStore_quick_order() {
        return store_quick_order;
    }

    /**
     * @param store_quick_order the store_quick_order to set
     */
    public void setStore_quick_order(int store_quick_order) {
        this.store_quick_order = store_quick_order;
    }
}
