package beans;

import sessions.GeneralUserSetting;
import connections.DBConnection;
import entities.GroupRight;
import entities.UserDetail;
import entities.Category;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import utilities.UtilityBean;

@ManagedBean
@SessionScoped
public class CategoryBean implements Serializable {

    private static final long serialVersionUID = 1L;
    static Logger LOGGER = Logger.getLogger(CategoryBean.class.getName());
    private List<Category> Categories;
    private String ActionMessage;
    private Category SelectedCategory = null;
    private int SelectedCategoryId;
    private String SearchCategoryName = "";
    private int TempId1;
    private String TempString1;
    private int TempId2;
    private String TempString2;
    @ManagedProperty("#{menuItemBean}")
    private MenuItemBean menuItemBean;

    public void saveCategory(Category cat) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        String sql = null;

        UserDetail aCurrentUserDetail = new GeneralUserSetting().getCurrentUser();
        List<GroupRight> aCurrentGroupRights = new GeneralUserSetting().getCurrentGroupRights();
        GroupRightBean grb = new GroupRightBean();

        if (cat.getCategoryId() == 0 && grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, "8", "Add") == 0) {
            msg = "Not Allowed to Access this Function";
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else if (cat.getCategoryId() > 0 && grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, "8", "Edit") == 0) {
            msg = "Not Allowed to Access this Function";
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else if (cat.getCategoryName().length() <= 0) {
            msg = "Category Name Needed...";
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else {
            if (cat.getCategoryId() == 0) {
                sql = "{call sp_insert_category(?,?,?,?)}";
            } else if (cat.getCategoryId() > 0) {
                sql = "{call sp_update_category(?,?,?,?,?)}";
            }

            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    CallableStatement cs = conn.prepareCall(sql);) {
                if (cat.getCategoryId() == 0) {
                    cs.setString(1, cat.getCategoryName());
                    cs.setInt(2, cat.getDisplay_quick_order());
                    cs.setInt(3, cat.getList_rank());
                    cs.setInt(4, cat.getStore_quick_order());
                    cs.executeUpdate();
                    this.setActionMessage(ub.translateWordsInText(BaseName, "Saved Successfully"));
                    this.clearCategory(cat);
                } else if (cat.getCategoryId() > 0) {
                    cs.setInt(1, cat.getCategoryId());
                    cs.setString(2, cat.getCategoryName());
                    cs.setInt(3, cat.getDisplay_quick_order());
                    cs.setInt(4, cat.getList_rank());
                    cs.setInt(5, cat.getStore_quick_order());
                    cs.executeUpdate();
                    this.setActionMessage(ub.translateWordsInText(BaseName, "Saved Successfully"));
                    this.clearCategory(cat);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
                this.setActionMessage("Category NOT saved");
            }
        }
    }

    public void setCategoryFromResultset(Category aCategory, ResultSet aResultSet) {
        try {
            try {
                aCategory.setCategoryId(aResultSet.getInt("category_id"));
            } catch (NullPointerException npe) {
                aCategory.setCategoryId(0);
            }
            try {
                aCategory.setCompanyId(aResultSet.getInt("company_id"));
            } catch (Exception e) {
                aCategory.setCompanyId(1);  // Default company
            }
            try {
                aCategory.setCategoryName(aResultSet.getString("category_name"));
            } catch (NullPointerException npe) {
                aCategory.setCategoryName("");
            }
            try {
                aCategory.setDisplay_quick_order(aResultSet.getInt("display_quick_order"));
            } catch (NullPointerException npe) {
                aCategory.setDisplay_quick_order(0);
            }
            try {
                aCategory.setList_rank(aResultSet.getInt("list_rank"));
            } catch (NullPointerException npe) {
                aCategory.setList_rank(0);
            }
            try {
                aCategory.setStore_quick_order(aResultSet.getInt("store_quick_order"));
            } catch (NullPointerException npe) {
                aCategory.setStore_quick_order(0);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public Category getCategory(int CatId) {
        String sql = "{call sp_search_category_by_id(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, CatId);
            rs = ps.executeQuery();
            if (rs.next()) {
                Category cat = new Category();
                this.setCategoryFromResultset(cat, rs);
                return cat;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public void deleteCategory() {
        this.deleteCategoryById(this.SelectedCategoryId);
    }

    public void deleteCategoryByObject(Category Cat) {
        this.deleteCategoryById(Cat.getCategoryId());
    }

    public void deleteCategoryById(int CatId) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        UserDetail aCurrentUserDetail = new GeneralUserSetting().getCurrentUser();
        List<GroupRight> aCurrentGroupRights = new GeneralUserSetting().getCurrentGroupRights();
        GroupRightBean grb = new GroupRightBean();

        if (grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, "8", "Delete") == 0) {
            msg = "Not Allowed to Access this Function";
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else if (null != new ItemBean().getItemObjectListByCategory(CatId)){
            msg = "Category is in use by an Item";
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else if (null != new SubCategoryBean().getSubCategoriesByCategoryId(CatId)){
            msg = "Category is in use by a Subcategory";
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else {
            String sql = "DELETE FROM category WHERE category_id=?";
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setInt(1, CatId);
                ps.executeUpdate();
                this.setActionMessage(ub.translateWordsInText(BaseName, "Deleted Successfully"));
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
                this.setActionMessage(ub.translateWordsInText(BaseName, "Category Not Deleted"));
            }
        }
    }

    public void displayCategory(Category CatFrom, Category CatTo) {
        CatTo.setCategoryId(CatFrom.getCategoryId());
        CatTo.setCategoryName(CatFrom.getCategoryName());
        CatTo.setDisplay_quick_order(CatFrom.getDisplay_quick_order());
        CatTo.setList_rank(CatFrom.getList_rank());
        CatTo.setStore_quick_order(CatFrom.getStore_quick_order());
    }

    public void clearCategory(Category Cat) {
        Cat.setCategoryId(0);
        Cat.setCategoryName("");
        Cat.setDisplay_quick_order(0);
        Cat.setList_rank(0);
        Cat.setStore_quick_order(0);
    }

    public List<Category> getCategories() {
        int companyId = new GeneralUserSetting().getCurrentUser().getCompanyId();
        String sql;
        sql = "SELECT * FROM category WHERE company_id=" + companyId + " ORDER BY category_name";
        ResultSet rs = null;
        Categories = new ArrayList<Category>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                Category cat = new Category();
                this.setCategoryFromResultset(cat, rs);
                Categories.add(cat);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return Categories;
    }
    
    public List<Category> getCategoriesStockTake() {
        int companyId = new GeneralUserSetting().getCurrentUser().getCompanyId();
        String sql;
        sql = "SELECT c.* FROM category c WHERE c.company_id=" + companyId + " AND c.category_id IN(SELECT distinct i.category_id from item i WHERE i.company_id=" + companyId + " AND i.is_track=1 AND i.is_asset=0) ORDER BY c.category_name ASC";
        ResultSet rs = null;
        Categories = new ArrayList<Category>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                Category cat = new Category();
                this.setCategoryFromResultset(cat, rs);
                Categories.add(cat);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return Categories;
    }

    public List<Category> getCategoriesQuickOrder() {
        int companyId = new GeneralUserSetting().getCurrentUser().getCompanyId();
        String sql;
        int CurStoreId = new GeneralUserSetting().getCurrentStore().getStoreId();
        sql = "SELECT * FROM category WHERE company_id=" + companyId + " AND display_quick_order=1 and (store_quick_order=0 or store_quick_order=" + CurStoreId + ") ORDER BY list_rank DESC,category_name ASC";
        ResultSet rs = null;
        Categories = new ArrayList<Category>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                Category cat = new Category();
                this.setCategoryFromResultset(cat, rs);
                Categories.add(cat);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return Categories;
    }

    /**
     * @param aCategoryName
     * @return the Categories
     */
    public List<Category> getCategoriesByCategoryName(String aCategoryName) {
        int companyId = new GeneralUserSetting().getCurrentUser().getCompanyId();
        String sql;
        sql = "SELECT * FROM category WHERE company_id=" + companyId + " AND category_name LIKE '%" + aCategoryName + "%' ORDER BY category_name";
        ResultSet rs = null;
        Categories = new ArrayList<Category>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                Category cat = new Category();
                this.setCategoryFromResultset(cat, rs);
                Categories.add(cat);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return Categories;
    }

    public List<Category> getDeliveryFor() {
        List<Category> cats = new ArrayList<>();
        try {
            String TransRef = new Parameter_listBean().getParameter_listByContextNameMemory("GOODS_DELIVERY", "TRANSACTION_REF").getParameter_value();
            if (TransRef.equals("0") || TransRef.equals("2")) {
                Category cat = new Category();
                cat.setCategoryId(2);
                cat.setCategoryName("Sale");
                cats.add(cat);
            }
            if (TransRef.equals("0") || TransRef.equals("11")) {
                Category cat = new Category();
                cat.setCategoryId(11);
                cat.setCategoryName("Order");
                cats.add(cat);
            }
        } catch (Exception e) {
            //do nothing
        }
        return cats;
    }

    /**
     * @param Categories the Categories to set
     */
    public void setCategories(List<Category> Categories) {
        this.Categories = Categories;
    }

    /**
     * @return the SelectedCategory
     */
    public Category getSelectedCategory() {
        return SelectedCategory;
    }

    /**
     * @param SelectedCategory the SelectedCategory to set
     */
    public void setSelectedCategory(Category SelectedCategory) {
        this.SelectedCategory = SelectedCategory;
    }

    /**
     * @return the SelectedCategoryId
     */
    public int getSelectedCategoryId() {
        return SelectedCategoryId;
    }

    /**
     * @param SelectedCategoryId the SelectedCategoryId to set
     */
    public void setSelectedCategoryId(int SelectedCategoryId) {
        this.SelectedCategoryId = SelectedCategoryId;
    }

    /**
     * @return the SearchCategoryName
     */
    public String getSearchCategoryName() {
        return SearchCategoryName;
    }

    /**
     * @param SearchCategoryName the SearchCategoryName to set
     */
    public void setSearchCategoryName(String SearchCategoryName) {
        this.SearchCategoryName = SearchCategoryName;
    }

    /**
     * @return the ActionMessage
     */
    public String getActionMessage() {
        return ActionMessage;
    }

    /**
     * @param ActionMessage the ActionMessage to set
     */
    public void setActionMessage(String ActionMessage) {
        this.ActionMessage = ActionMessage;
    }

    /**
     * @return the TempId1
     */
    public int getTempId1() {
        return TempId1;
    }

    /**
     * @param TempId1 the TempId1 to set
     */
    public void setTempId1(int TempId1) {
        this.TempId1 = TempId1;
    }

    /**
     * @return the TempString1
     */
    public String getTempString1() {
        return TempString1;
    }

    /**
     * @param TempString1 the TempString1 to set
     */
    public void setTempString1(String TempString1) {
        this.TempString1 = TempString1;
    }

    /**
     * @return the TempId2
     */
    public int getTempId2() {
        return TempId2;
    }

    /**
     * @param TempId2 the TempId2 to set
     */
    public void setTempId2(int TempId2) {
        this.TempId2 = TempId2;
    }

    /**
     * @return the TempString2
     */
    public String getTempString2() {
        return TempString2;
    }

    /**
     * @param TempString2 the TempString2 to set
     */
    public void setTempString2(String TempString2) {
        this.TempString2 = TempString2;
    }

    /**
     * @return the menuItemBean
     */
    public MenuItemBean getMenuItemBean() {
        return menuItemBean;
    }

    /**
     * @param menuItemBean the menuItemBean to set
     */
    public void setMenuItemBean(MenuItemBean menuItemBean) {
        this.menuItemBean = menuItemBean;
    }

}
