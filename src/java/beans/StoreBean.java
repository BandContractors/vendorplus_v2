package beans;

import sessions.GeneralUserSetting;
import connections.DBConnection;
import entities.Store;
import entities.GroupRight;
import entities.UserDetail;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import utilities.UtilityBean;
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
public class StoreBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Store> Stores;
    private String ActionMessage = null;
    private Store SelectedStore = null;
    private int SelectedStoreId;
    private String SearchStoreName = "";
    private List<Store> StoresList;
    static Logger LOGGER = Logger.getLogger(StoreBean.class.getName());
    @ManagedProperty("#{menuItemBean}")
    private MenuItemBean menuItemBean;

    public void setStoreFromResultset(Store aStore, ResultSet aResultSet) {
        try {
            try {
                aStore.setStoreId(aResultSet.getInt("store_id"));
            } catch (Exception e) {
                aStore.setStoreId(0);
            }
            try {
                aStore.setCompanyId(aResultSet.getInt("company_id"));
            } catch (Exception e) {
                aStore.setCompanyId(1);  // Default company
            }
            try {
                String store_name = aResultSet.getString("store_name");
                if (null == store_name) {
                    aStore.setStoreName("");
                } else {
                    aStore.setStoreName(store_name);
                }
            } catch (Exception e) {
                aStore.setStoreName("");
            }
            try {
                String store_code = aResultSet.getString("store_code");
                if (null == store_code) {
                    aStore.setStore_code("");
                } else {
                    aStore.setStore_code(store_code);
                }
            } catch (Exception e) {
                aStore.setStore_code("");
            }
            try {
                aStore.setShift_mode(aResultSet.getInt("shift_mode"));
            } catch (Exception e) {
                aStore.setShift_mode(0);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void refreshStoresList() {
        String sql;
        sql = "{call sp_search_store_by_none()}";
        ResultSet rs = null;
        try {
            this.StoresList.clear();
        } catch (Exception e) {
            this.StoresList = new ArrayList<>();
        }
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                Store store = new Store();
                this.setStoreFromResultset(store, rs);
                this.StoresList.add(store);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);//"refreshStoresList:" + e.getMessage());
        }
    }

    public void saveStore(Store store) {
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

        if (store.getStoreId() == 0 && grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, "88", "Add") == 0) {
            msg = "Not Allowed to Access this Function";
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else if (store.getStoreId() > 0 && grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, "88", "Edit") == 0) {
            msg = "Not Allowed to Access this Function";
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else {
            if (store.getStoreId() == 0) {
                sql = "{call sp_insert_store(?,?,?)}";
            } else if (store.getStoreId() > 0) {
                sql = "{call sp_update_store(?,?,?,?)}";
            }

            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    CallableStatement cs = conn.prepareCall(sql);) {
                if (store.getStoreId() == 0) {
                    cs.setString(1, store.getStoreName());
                    cs.setString(2, store.getStore_code());
                    cs.setInt(3, store.getShift_mode());
                    cs.executeUpdate();
                    this.setActionMessage(ub.translateWordsInText(BaseName, "Saved Successfully"));
                    this.clearStore(store);
                } else if (store.getStoreId() > 0) {
                    cs.setInt(1, store.getStoreId());
                    cs.setString(2, store.getStoreName());
                    cs.setString(3, store.getStore_code());
                    cs.setInt(4, store.getShift_mode());
                    cs.executeUpdate();
                    this.setActionMessage(ub.translateWordsInText(BaseName, "Saved Successfully"));
                    this.clearStore(store);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);//se.getMessage());
                this.setActionMessage(ub.translateWordsInText(BaseName, "Store Not Saved"));
            }
        }
    }

    public Store getStore(int aStoreId) {
        String sql = "{call sp_search_store_by_id(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, aStoreId);
            rs = ps.executeQuery();
            if (rs.next()) {
                Store store = new Store();
                this.setStoreFromResultset(store, rs);
                return store;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);//se.getMessage());
            return null;
        }
    }

    public Store getStoreByNameEqual(String aStoreName) {
        String sql = "{call sp_search_store_by_name_equal(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, aStoreName);
            rs = ps.executeQuery();
            if (rs.next()) {
                Store store = new Store();
                this.setStoreFromResultset(store, rs);
                return store;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);//se.getMessage());
            return null;
        }
    }

    public Store getStoreByCode(String aStoreCode) {
        //String sql = "{call sp_search_store_by_store_code(?)}";
        String sql = "SELECT * from store where store_code = ?";
        ResultSet rs;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, aStoreCode);
            rs = ps.executeQuery();
            if (rs.next()) {
                Store store = new Store();
                this.setStoreFromResultset(store, rs);
                return store;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);//se.getMessage());
            return null;
        }
    }

    public void deleteStoreByObject(Store store) {
        this.deleteStore(store);
    }

    public void deleteStore(Store store) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        String sql = "DELETE FROM store WHERE store_id=?";
        UserDetail aCurrentUserDetail = new GeneralUserSetting().getCurrentUser();
        List<GroupRight> aCurrentGroupRights = new GeneralUserSetting().getCurrentGroupRights();
        GroupRightBean grb = new GroupRightBean();

        if (grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, "88", "Delete") == 0) {
            msg = "Not Allowed to Access this Function";
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else if (null == store) {

        } else {
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setInt(1, store.getStoreId());
                ps.executeUpdate();
                this.setActionMessage(ub.translateWordsInText(BaseName, "Deleted Successfully"));
                this.clearStore(SelectedStore);
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);//se.getMessage());
                this.setActionMessage(ub.translateWordsInText(BaseName, "Store Not Deleted"));
            }
        }
    }

    public void displayStore(Store StoreFrom, Store StoreTo) {
        StoreTo.setStoreId(StoreFrom.getStoreId());
        StoreTo.setStoreName(StoreFrom.getStoreName());
        StoreTo.setStore_code(StoreFrom.getStore_code());
        StoreTo.setShift_mode(StoreFrom.getShift_mode());
    }

    public void clearStore(Store store) {
        try {
            store.setStoreId(0);
            store.setStoreName("");
            store.setStore_code("");
            store.setShift_mode(0);
        } catch (NullPointerException npe) {

        }
    }

    /**
     * @return the Stores
     */
    public List<Store> getStores() {
        int companyId = new GeneralUserSetting().getCurrentUser().getCompanyId();
        String sql;
        sql = "SELECT * FROM store WHERE company_id=" + companyId + " ORDER BY store_name";
        ResultSet rs = null;
        Stores = new ArrayList<>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                Store store = new Store();
                this.setStoreFromResultset(store, rs);
                Stores.add(store);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);//se.getMessage());
        }
        return Stores;
    }

    public List<Store> getStoresByName(String aStoreName) {
        int companyId = new GeneralUserSetting().getCurrentUser().getCompanyId();
        String sql;
        sql = "SELECT * FROM store WHERE company_id=" + companyId + " AND store_name LIKE '%" + aStoreName + "%' ORDER BY store_name";
        ResultSet rs = null;
        Stores = new ArrayList<>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                Store store = new Store();
                this.setStoreFromResultset(store, rs);
                Stores.add(store);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);//se.getMessage());
        }
        return Stores;
    }

    /**
     * @param Stores the Stores to set
     */
    public void setStores(List<Store> Stores) {
        this.Stores = Stores;
    }

    public List<Store> getStoresByUser(int aUserDetId) {
        String sql;
        sql = "{call sp_search_store_by_user_detail(?)}";
        ResultSet rs = null;
        Stores = new ArrayList<>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, aUserDetId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Store store = new Store();
                this.setStoreFromResultset(store, rs);
                Stores.add(store);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);//se.getMessage());
        }
        return Stores;
    }

    public List<Store> getStoresAll() {
        int companyId = new GeneralUserSetting().getCurrentUser().getCompanyId();
        String sql;
        sql = "SELECT * FROM store WHERE company_id=" + companyId + " ORDER BY store_name";
        ResultSet rs = null;
        Stores = new ArrayList<>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                Store store = new Store();
                this.setStoreFromResultset(store, rs);
                Stores.add(store);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);//se.getMessage());
        }
        return Stores;
    }

    public List<Store> getWeekDaysList() {
        String sql;
        ResultSet rs = null;
        List<Store> stores = new ArrayList<>();
        try {
            for (int d = 1; d <= 7; d++) {
                Store store = new Store();
                store.setStoreId(d);
                if (d == 1) {
                    store.setStoreName("Mon");
                } else if (d == 2) {
                    store.setStoreName("Tue");
                } else if (d == 3) {
                    store.setStoreName("Wed");
                } else if (d == 4) {
                    store.setStoreName("Thu");
                } else if (d == 5) {
                    store.setStoreName("Fri");
                } else if (d == 6) {
                    store.setStoreName("Sat");
                } else if (d == 7) {
                    store.setStoreName("Sun");
                } else {
                    store.setStoreName("");
                }
                stores.add(store);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);//"getWeekDaysList:" + e.getMessage());
        }
        return stores;
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
     * @return the SelectedStore
     */
    public Store getSelectedStore() {
        return SelectedStore;
    }

    /**
     * @param SelectedStore the SelectedStore to set
     */
    public void setSelectedStore(Store SelectedStore) {
        this.SelectedStore = SelectedStore;
    }

    /**
     * @return the SelectedStoreId
     */
    public int getSelectedStoreId() {
        return SelectedStoreId;
    }

    /**
     * @param SelectedStoreId the SelectedStoreId to set
     */
    public void setSelectedStoreId(int SelectedStoreId) {
        this.SelectedStoreId = SelectedStoreId;
    }

    /**
     * @return the SearchStoreName
     */
    public String getSearchStoreName() {
        return SearchStoreName;
    }

    /**
     * @param SearchStoreName the SearchStoreName to set
     */
    public void setSearchStoreName(String SearchStoreName) {
        this.SearchStoreName = SearchStoreName;
    }

    /**
     * @return the StoresList
     */
    public List<Store> getStoresList() {
        return StoresList;
    }

    /**
     * @param StoresList the StoresList to set
     */
    public void setStoresList(List<Store> StoresList) {
        this.StoresList = StoresList;
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
