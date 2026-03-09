package beans;

import api_tax.efris.EFRIS_excise_duty_list;
import api_tax.efris.innerclasses.ItemTax;
import api_tax.efris_bean.EFRIS_excise_duty_listBean;
import api_tax.efris_bean.StockManage;
import com.google.gson.Gson;
import sessions.GeneralUserSetting;
import connections.DBConnection;
import entities.Category;
import entities.CompanySetting;
import entities.DiscountPackageItem;
import entities.GroupRight;
import entities.Item;
import entities.Item_code_other;
import entities.Item_excise_duty_map;
import entities.Item_store_reorder;
import entities.Item_tax_map;
import entities.Item_unit;
import entities.Item_unit_other;
import entities.Item_unspsc;
import entities.Location;
import entities.Stock;
import entities.Trans;
import entities.TransItem;
import entities.TransactionPackageItem;
import entities.TransactionType;
import entities.Unit;
import entities.UserDetail;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.sql.CallableStatement;
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
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import utilities.CustomValidator;
import utilities.UtilityBean;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.primefaces.event.TabChangeEvent;
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
public class ItemBean implements Serializable {

    private static final long serialVersionUID = 1L;
    static Logger LOGGER = Logger.getLogger(ItemBean.class.getName());

    private List<Item> Items;
    private String ActionMessage = null;
    private Item SelectedItem = null;
    private Item SelectedItemX = null;
    private long SelectedItemId;
    private String SearchItemDesc = "";
    private List<Item> ItemObjectList;
    private String TypedItemCode;
    List<Item> ReportItems = new ArrayList<>();
    List<Item> ReportItemsSummary = new ArrayList<>();
    private List<Item> ItemsList;
    private List<Item> ItemsSummary;
    private Item ItemObj;
    private List<Location> LocationList;
    private List<Stock> StockList;
    private Item ParentItem;
    private Part file;
    private List<Item> producedItemList;
    private List<Category> InventoryTypeList;
    private List<Category> InventoryAccountList;
    private Item_unspsc Item_unspscObj;
    private List<Item_unspsc> Item_unspscList;
    private List<EFRIS_excise_duty_list> ExciseList;
    private String SearchUNSPSC = "";
    private String SearchExcise = "";
    private Item_tax_map Item_tax_mapObj;
    private ItemTax ItemTaxObj;
    @ManagedProperty("#{menuItemBean}")
    private MenuItemBean menuItemBean;
    private List<Item_code_other> Item_code_otherList;
    private Item_code_other Item_code_otherObj;
    private Item_store_reorder Item_store_reorderObj;
    private List<Item_store_reorder> Item_store_reorderList;
    private List<Item_unit_other> Item_unit_otherList;
    private Item_unit_other Item_unit_otherObj;
    private int ReorderLevelEdited;
    private int ItemOtherUnitsEdited;
    private List<Item_unit> Item_unitList = new ArrayList<>();

    public void refreshInventoryType(Item aItem, String aItemPurpose) {
        try {
            this.InventoryTypeList = new ArrayList<>();
            if (aItemPurpose.equals("Asset")) {
                //list
                this.InventoryTypeList.clear();
            } else if (aItemPurpose.equals("Expense")) {
                //list
                this.InventoryTypeList.clear();
                Category cat = null;
                cat = new Category();
                cat.setCategoryName("Raw Material");
                this.InventoryTypeList.add(cat);
                cat = new Category();
                cat.setCategoryName("Consumption");
                this.InventoryTypeList.add(cat);
                cat = new Category();
                cat.setCategoryName("Services");
                this.InventoryTypeList.add(cat);
            } else {//Stock
                //list
                this.InventoryTypeList.clear();
                Category cat = null;
                cat = new Category();
                cat.setCategoryName("Merchandise");
                this.InventoryTypeList.add(cat);
                cat = new Category();
                cat.setCategoryName("Finished Goods");
                this.InventoryTypeList.add(cat);
                cat = new Category();
                cat.setCategoryName("Services");
                this.InventoryTypeList.add(cat);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void updateItemObjName(Part aFile, Item aItem) {
        try {
            if (aFile.getName().length() > 0) {
                aItem.setItemImgUrl(aItem.getItemId() + ".png");
            }
        } catch (Exception e) {
        }
    }

    public void handleFileUpload(Item aItem) {
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, new File(new GeneralUserSetting().getITEM_IMAGE_LOCAL_LOCATION(), Long.toString(aItem.getItemId()) + ".png").toPath());
        } catch (IOException e) {
            // Show faces message?
        }
    }

    public void ItemBarCodeListener(AjaxBehaviorEvent event) {
        //System.out.println("OkaY");
    }

    public void checkRemoteTaxRateAndUpdateLocal(long aItemId, String aDescription, String aVatRateOrder) {
        try {
            Item item = null;
            if (aItemId > 0) {
                item = this.getItem(aItemId);
            } else if (aItemId == 0 && aDescription.length() > 0) {
                item = this.getItemByDesc(aDescription);
            }
            if (null != item) {
                if (aVatRateOrder.isEmpty()) {//apply general order
                    item.setVat_rate_order(this.getVatRateOrder(new Parameter_listBean().getParameter_listByContextName("GENERAL", "TAX_VAT_RATE_ORDER").getParameter_value()));
                } else {
                    item.setVat_rate_order(this.getVatRateOrder(item.getVatRated()));
                }
                Item_tax_map im = new Item_tax_mapBean().getItem_tax_mapSynced(item.getItemId());
                if (null != im) {
                    String taxratelocal = item.getVatRated();
                    String taxrateremote = "";
                    String taxrateadd = "";
                    String APIMode = new Parameter_listBean().getParameter_listByContextName("API", "API_TAX_MODE").getParameter_value();
                    ItemTax it = null;
                    if (APIMode.equals("OFFLINE")) {
                        it = new StockManage().getItemFromTaxOffline(im.getItem_id_tax());
                    } else {
                        it = new StockManage().getItemFromTaxOnline(im.getItem_id_tax());
                    }
                    if (it != null) {
                        taxrateremote = this.getVatTaxRatesRemote(it);
                    }
                    if (taxrateremote.contains("ZERO") && !taxratelocal.contains("ZERO")) {
                        if (taxrateadd.length() == 0) {
                            taxrateadd = "ZERO";
                        } else {
                            taxrateadd = taxrateadd + ",ZERO";
                        }
                    }
                    if (taxrateremote.contains("EXEMPT") && !taxratelocal.contains("EXEMPT")) {
                        if (taxrateadd.length() == 0) {
                            taxrateadd = "EXEMPT";
                        } else {
                            taxrateadd = taxrateadd + ",EXEMPT";
                        }
                    }
                    if (taxrateremote.contains("STANDARD") && !taxratelocal.contains("STANDARD")) {
                        if (taxrateadd.length() == 0) {
                            taxrateadd = "STANDARD";
                        } else {
                            taxrateadd = taxrateadd + ",STANDARD";
                        }
                    }
                    if (taxrateadd.length() > 0) {
                        taxratelocal = taxratelocal + "," + taxrateadd;
                        //re-arrange
                        String taxratelocalNew = "";

                        String TAX_VAT_RATE_ORDER = item.getVat_rate_order();
                        taxratelocalNew = this.reArrangeVatRate(taxratelocal, TAX_VAT_RATE_ORDER);
                        // now update new tax rate
                        if (taxratelocalNew.length() > 0 && taxratelocal.length() > 0) {
                            item.setVatRated(taxratelocalNew);
                            this.saveValidatedItem(item);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reArrangeVatRateCall(long aItemId, String aDescription) {
        try {
            Item item = null;
            if (aItemId > 0) {
                item = this.getItem(aItemId);
            } else if (aItemId == 0 && aDescription.length() > 0) {
                item = this.getItemByDesc(aDescription);
            }
            if (null != item) {
                String taxratelocal = item.getVatRated();
                if (taxratelocal.length() > 0) {
                    String taxratelocalNew = "";
                    String TAX_VAT_RATE_ORDER = new Parameter_listBean().getParameter_listByContextName("GENERAL", "TAX_VAT_RATE_ORDER").getParameter_value();
                    taxratelocalNew = this.reArrangeVatRate(taxratelocal, TAX_VAT_RATE_ORDER);
                    // now update new tax rate
                    if (taxratelocalNew.length() > 0 && taxratelocal.length() > 0) {
                        item.setVatRated(taxratelocalNew);
                        this.saveValidatedItem(item);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public String reArrangeVatRate(String aNotArrangedVatRated, String aVatRatedOrder) {
        String ArrangedVatRated = "";
        if (aVatRatedOrder.isEmpty() || (!aVatRatedOrder.contains("ZERO") && !aVatRatedOrder.contains("EXEMPT") && !aVatRatedOrder.contains("STANDARD"))) {
            aVatRatedOrder = "ZERO,EXEMPT,STANDARD";
        }
        String[] aVatRatedOrderArray = new UtilityBean().getStringArrayFromCommaSeperatedStr(aVatRatedOrder);
        for (int i = 0; i < aVatRatedOrderArray.length; i++) {
            if (aNotArrangedVatRated.contains(aVatRatedOrderArray[i])) {
                if (ArrangedVatRated.length() == 0) {
                    ArrangedVatRated = aVatRatedOrderArray[i];
                } else {
                    ArrangedVatRated = ArrangedVatRated + "," + aVatRatedOrderArray[i];
                }
            }
        }
        return ArrangedVatRated;
    }

    public String getVatRateOrder(String aVatRated) {
        String VatRateOrder = "STANDARD,EXEMPT,ZERO";
        if (aVatRated.length() > 0) {
            if (aVatRated.startsWith("STANDARD")) {
                VatRateOrder = "STANDARD,EXEMPT,ZERO";
            } else if (aVatRated.startsWith("ZERO") || aVatRated.startsWith("EXEMPT")) {
                VatRateOrder = "ZERO,EXEMPT,STANDARD";
            }
        }
        return VatRateOrder;
    }

    public void saveItem() {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        try {
            this.ItemObj.setUnit_symbol_tax(new UnitBean().getUnit(this.ItemObj.getUnitId()).getUnit_symbol_tax());
        } catch (Exception e) {
        }
        String msg = "";
        String msgExciseDuty = "";
        //first convert vat rated from array to string
        this.ItemObj.setVatRated(new UtilityBean().getVATRateStrFromArray(this.ItemObj.getSelectedVatRateds()));
        //re-arrange vat rate basing on order
        this.ItemObj.setVatRated(this.reArrangeVatRate(this.ItemObj.getVatRated(), this.ItemObj.getVat_rate_order()));
        //validate
        msg = this.validateItem(this.ItemObj);
        msgExciseDuty = this.validateExciseDuty(ItemObj, this.Item_unit_otherList);
        if (msg.length() > 0) {
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else if (msgExciseDuty.length() > 0) {
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msgExciseDuty)));
        } else {
            try {
                if (this.saveValidatedItem(this.ItemObj) == 1) {
                    //REORDER LEVELS
                    if (this.ReorderLevelEdited == 1) {
                        if (this.ItemObj.getItemId() > 0) {
                            this.saveItem_store_reorderCall(this.ItemObj.getItemId());
                        } else if (this.ItemObj.getItemId() == 0 && this.ItemObj.getDescription().length() > 0) {
                            Item item = this.getItemByDesc(this.ItemObj.getDescription());
                            if (null != item) {
                                this.saveItem_store_reorderCall(item.getItemId());
                            }
                        }
                    }
                    //ITEM OTHER UNITS
                    if (this.ItemOtherUnitsEdited == 1) {
                        int x = this.insertOrUpdateItem_unit_otherList();
                    }
                    //EXCISE DUTY CODE
                    if (this.ItemObj.getItemId() > 0 || (this.ItemObj.getItemId() == 0 && this.ItemObj.getExcise_duty_code().length() > 0)) {
                        long ItemId = 0;
                        if (this.ItemObj.getItemId() == 0 && this.ItemObj.getDescription().length() > 0) {
                            Item item = this.getItemByDesc(this.ItemObj.getDescription());
                            ItemId = item.getItemId();
                        } else {
                            ItemId = this.ItemObj.getItemId();
                        }
                        if (ItemId > 0) {
                            Item_excise_duty_map excobj = this.getItem_excise_duty_mapByItem(ItemId);
                            if (null == excobj) {
                                //insert
                                excobj = new Item_excise_duty_map();
                                excobj.setItem_excise_duty_map_id(0);
                                excobj.setItem_id(ItemId);
                                excobj.setExcise_duty_code(this.getItemObj().getExcise_duty_code());
                                int x = this.insertOrUpdateItem_excise_duty_map(excobj);
                            } else {
                                if (excobj.getExcise_duty_code() == null ? this.getItemObj().getExcise_duty_code() != null : !excobj.getExcise_duty_code().equals(this.getItemObj().getExcise_duty_code())) {
                                    //update
                                    excobj.setExcise_duty_code(this.getItemObj().getExcise_duty_code());
                                    int x = this.insertOrUpdateItem_excise_duty_map(excobj);
                                }
                            }
                        }
                    }
                    //ITEM TAX MAPPING
                    new Item_tax_mapBean().saveItem_tax_mapCall(this.ItemObj.getDescription(), this.ItemObj.getItem_code_tax(), "");
                    //check sync status
                    String SyncStatus = "";
                    if (new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value().length() > 0) {
                        if (null == new Item_tax_mapBean().getItem_tax_mapSyncedByName(this.ItemObj.getDescription())) {
                            SyncStatus = "No";
                        } else {
                            SyncStatus = "Yes";
                        }
                    }
                    //display Message
                    if (SyncStatus.length() == 0) {
                        this.setActionMessage(ub.translateWordsInText(BaseName, "Saved Successfully"));
                    } else {
                        this.setActionMessage(ub.translateWordsInText(BaseName, "Saved Successfully") + ", Synced : " + ub.translateWordsInText(BaseName, SyncStatus));
                    }
                    //update local tax rate with remote tax rate
                    if (new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value().length() > 0) {
                        this.checkRemoteTaxRateAndUpdateLocal(this.ItemObj.getItemId(), this.ItemObj.getDescription(), this.ItemObj.getVat_rate_order());
                    }
                    this.clearItem();
                    this.refreshStockLocation(0);
                } else {
                    this.setActionMessage(ub.translateWordsInText(BaseName, "Item Not Saved"));
                    FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, "Item Not Saved")));
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
                this.setActionMessage(ub.translateWordsInText(BaseName, "Item Not Saved"));
                FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, "Item Not Saved")));
            }
        }
    }

    public void saveItem(Item aItem) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        //first convert vat rated from array to string
        aItem.setVatRated(new UtilityBean().getCommaSeperatedStrFromStringArray(aItem.getSelectedVatRateds()));
        msg = this.validateItem(aItem);
        if (msg.length() > 0) {
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else {
            try {
                if (this.saveValidatedItem(aItem) == 1) {
                    this.setActionMessage(ub.translateWordsInText(BaseName, "Saved Successfully"));
                    this.clearItem(aItem);
                    this.refreshStockLocation(0);
                } else {
                    this.setActionMessage(ub.translateWordsInText(BaseName, "Item Not Saved"));
                    FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, "Item Not Saved")));
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
                this.setActionMessage(ub.translateWordsInText(BaseName, "Item Not Saved"));
                FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, "Item Not Saved")));
            }
        }
    }

    public String validateItem(Item aItem) {
        String msg = "";
        String sql2 = null;
        String sql3 = null;
        sql2 = "SELECT * FROM item WHERE (item_code!='' AND item_code='" + aItem.getItemCode() + "') or description='" + aItem.getDescription() + "'";
        sql3 = "SELECT * FROM item_code_other WHERE item_code='" + aItem.getItemCode() + "'";
        UserDetail aCurrentUserDetail = new GeneralUserSetting().getCurrentUser();
        List<GroupRight> aCurrentGroupRights = new GeneralUserSetting().getCurrentGroupRights();
        GroupRightBean grb = new GroupRightBean();
        double qtybal = 0;
        try {
            if (aItem.getItemId() > 0) {
                qtybal = new StockBean().getStockAtHand(aItem.getItemId());
            }
        } catch (Exception e) {
            qtybal = 0;
        }
        if (aItem.getItemId() == 0 && grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, "8", "Add") == 0) {
            msg = "Not Allowed to Access this Function";
        } else if (aItem.getItemId() > 0 && grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, "8", "Edit") == 0) {
            msg = "Not Allowed to Access this Function";
        } else if (aItem.getCategoryId() == 0) {
            msg = "Select Valid Category";
        } else if (aItem.getItemType().length() <= 0) {
            msg = "Select Valid Item Type";
        } else if (aItem.getUnitId() == 0) {
            msg = "Select Valid Unit";
        } else if (new CustomValidator().TextSize(aItem.getDescription(), 1, 100).equals("FAIL")) {
            msg = "Enter Item Description";
        } else if (new CustomValidator().TextSize(aItem.getIsSuspended(), 2, 3).equals("FAIL")) {
            msg = "Select Value for Is Suspended";
        } else if (new CustomValidator().TextSize(aItem.getVatRated(), 2, 50).equals("FAIL")) {
            msg = "Select Value for VAT Rated";
        } else if (aItem.getUnitCostPrice() > aItem.getUnitRetailsalePrice()) {
            msg = "Cost Price Cannot be Greater Than Retail Sale Price";
        } else if (aItem.getUnitCostPrice() > aItem.getUnitWholesalePrice()) {
            msg = "Cost Price Cannot be Greater Than Wholesale Price";
        } else if ((new CustomValidator().CheckRecords(sql2) > 0 && aItem.getItemId() == 0) || (new CustomValidator().CheckRecords(sql2) > 0 && new CustomValidator().CheckRecords(sql2) != 1 && aItem.getItemId() > 0)) {
            msg = "Item Code or Description Exists";
        } else if (new CustomValidator().CheckRecords(sql3) > 0) {
            msg = "Item Code Exists";
        } else if (aItem.getDisplay_alias_name() == 1 && aItem.getAlias_name().length() == 0) {
            msg = "Specify Item Alias Name";
        } else if (aItem.getIsAsset() == 1 && (aItem.getAssetType().length() == 0 || aItem.getAssetAccountCode().length() == 0)) {
            msg = "Specify Asset Type and Account";
        } else if (aItem.getIsAsset() == 0 && (aItem.getExpense_type().length() == 0 || aItem.getExpenseAccountCode().length() == 0)) {
            msg = "Specify Account Type and Name";
        } else if (aItem.getIsSale() == 1 && aItem.getItem_code_tax().length() == 0 && new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value().length() > 0) {
            msg = "Specify Item Tax Code";
        } else if (aItem.getItemId() > 0 && aItem.getItemType().equals("SERVICE") && qtybal > 0) {
            msg = "Adjust Current Stock before Changing Item Type";
        } else {
            msg = "";
        }
        return msg;
    }

    public String validateExciseDuty(Item aItem, List<Item_unit_other> aItem_unit_otherList) {
        String msg = "";
        try {
            if (null == aItem_unit_otherList) {
                aItem_unit_otherList = new ArrayList<>();
            }
            if (aItem.getExcise_duty_code().length() > 0) {
                EFRIS_excise_duty_list ExciseDutyDtl = null;
                String ExciseUnitCodeTax = "";
                ExciseDutyDtl = new EFRIS_excise_duty_listBean().getEFRIS_invoice_detailByExciseDutyCode(aItem.getExcise_duty_code());
                if (null != ExciseDutyDtl) {
                    ExciseUnitCodeTax = ExciseDutyDtl.getUnit();
                    if (null == ExciseUnitCodeTax) {
                        ExciseUnitCodeTax = "";
                    }
                }
                if (ExciseDutyDtl == null) {
                    msg = "Excise Duty Code is Not Valid";
                } else if (aItem_unit_otherList.isEmpty() && !ExciseUnitCodeTax.isEmpty() && !aItem.getUnit_symbol_tax().equals(ExciseUnitCodeTax)) {
                    msg = "Unit of Excise Duty is different from Item Unit Code. Change or Add Unit";
                } else if (!aItem_unit_otherList.isEmpty() && !ExciseUnitCodeTax.isEmpty()) {
                    //check if found and on what position
                    int found = 0;
                    int foundPsn = -1;
                    for (int i = 0; i < aItem_unit_otherList.size(); i++) {
                        if (ExciseUnitCodeTax.equals(new UnitBean().getUnit(aItem_unit_otherList.get(i).getOther_unit_id()).getUnit_symbol_tax())) {
                            found = 1;
                            foundPsn = i;
                            break;
                        }
                    }
                    if (found == 0 && !aItem.getUnit_symbol_tax().equals(ExciseUnitCodeTax)) {
                        msg = "Unit of Excise Duty is not found among Item Units. Change or Add Unit";
                    } else if (found == 1 && !aItem.getUnit_symbol_tax().equals(ExciseUnitCodeTax) && foundPsn != 0) {
                        msg = "Other Unit that Matches Excise Duty Unit should be the first on the list of other Units";
                    }
                }
            }
        } catch (Exception e) {
        }
        return msg;
    }

    public int saveValidatedItem(Item aItem) {
        int save_status = 0;
        String sql = null;
        if (aItem.getItemId() == 0) {
            sql = "{call sp_insert_item(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        } else if (aItem.getItemId() > 0) {
            sql = "{call sp_update_item(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        }
        try (
                Connection conn = DBConnection.getMySQLConnection();
                CallableStatement cs = conn.prepareCall(sql);) {
            if (aItem.getItemId() > 0) {
                cs.setLong("in_item_id", aItem.getItemId());
            }
            cs.setString("in_item_code", aItem.getItemCode());
            cs.setString("in_description", aItem.getDescription());
            cs.setInt("in_category_id", aItem.getCategoryId());
            cs.setInt("in_sub_category_id", aItem.getSubCategoryId());
            cs.setInt("in_unit_id", aItem.getUnitId());
            cs.setDouble("in_reorder_level", aItem.getReorderLevel());
            cs.setDouble("in_unit_cost_price", aItem.getUnitCostPrice());
            cs.setDouble("in_unit_retailsale_price", aItem.getUnitRetailsalePrice());
            cs.setDouble("in_unit_wholesale_price", aItem.getUnitWholesalePrice());
            cs.setString("in_is_suspended", aItem.getIsSuspended());
            cs.setString("in_vat_rated", aItem.getVatRated());
            cs.setString("in_item_img_url", aItem.getItemImgUrl());
            cs.setString("in_item_type", aItem.getItemType());
            try {
                cs.setString("in_currency_code", aItem.getCurrencyCode());
            } catch (NullPointerException npe) {
                cs.setString("in_currency_code", "");
            }
            try {
                cs.setInt("in_is_general", aItem.getIsGeneral());
            } catch (NullPointerException npe) {
                cs.setInt("in_is_general", 0);
            }
            try {
                cs.setString("in_asset_type", aItem.getAssetType());
            } catch (NullPointerException npe) {
                cs.setString("in_asset_type", "");
            }
            try {
                cs.setInt("in_is_buy", aItem.getIsBuy());
            } catch (NullPointerException npe) {
                cs.setInt("in_is_buy", 0);
            }
            try {
                cs.setInt("in_is_sale", aItem.getIsSale());
            } catch (NullPointerException npe) {
                cs.setInt("in_is_sale", 0);
            }
            try {
                cs.setInt("in_is_track", aItem.getIsTrack());
            } catch (NullPointerException npe) {
                cs.setInt("in_is_track", 0);
            }
            try {
                cs.setInt("in_is_asset", aItem.getIsAsset());
            } catch (NullPointerException npe) {
                cs.setInt("in_is_asset", 0);
            }
            try {
                cs.setString("in_asset_account_code", aItem.getAssetAccountCode());
            } catch (NullPointerException npe) {
                cs.setString("in_asset_account_code", "");
            }
            try {
                cs.setString("in_expense_account_code", aItem.getExpenseAccountCode());
            } catch (NullPointerException npe) {
                cs.setString("in_expense_account_code", "");
            }
            try {
                cs.setInt("in_is_hire", aItem.getIs_hire());
            } catch (NullPointerException npe) {
                cs.setInt("in_is_hire", 0);
            }
            try {
                cs.setString("in_duration_type", aItem.getDuration_type());
            } catch (NullPointerException npe) {
                cs.setString("in_duration_type", "");
            }
            try {
                cs.setDouble("in_unit_hire_price", aItem.getUnit_hire_price());
            } catch (NullPointerException npe) {
                cs.setDouble("in_unit_hire_price", 0);
            }
            try {
                cs.setDouble("in_unit_special_price", aItem.getUnit_special_price());
            } catch (NullPointerException npe) {
                cs.setDouble("in_unit_special_price", 0);
            }
            try {
                cs.setDouble("in_unit_weight", aItem.getUnit_weight());
            } catch (NullPointerException npe) {
                cs.setDouble("in_unit_weight", 0);
            }
            if (null == aItem.getExpense_type()) {
                cs.setString("in_expense_type", "");
            } else {
                cs.setString("in_expense_type", aItem.getExpense_type());
            }
            if (null == aItem.getAlias_name()) {
                cs.setString("in_alias_name", "");
            } else {
                cs.setString("in_alias_name", aItem.getAlias_name());
            }
            try {
                cs.setInt("in_display_alias_name", aItem.getDisplay_alias_name());
            } catch (NullPointerException npe) {
                cs.setInt("in_display_alias_name", 0);
            }
            try {
                cs.setInt("in_is_free", aItem.getIs_free());
            } catch (NullPointerException npe) {
                cs.setInt("in_is_free", 0);
            }
            try {
                cs.setInt("in_specify_size", aItem.getSpecify_size());
            } catch (NullPointerException npe) {
                cs.setInt("in_specify_size", 0);
            }
            try {
                cs.setInt("in_size_to_specific_name", aItem.getSize_to_specific_name());
            } catch (NullPointerException npe) {
                cs.setInt("in_size_to_specific_name", 0);
            }
            try {
                cs.setString("in_expiry_band", aItem.getExpiry_band());
            } catch (NullPointerException npe) {
                cs.setString("in_expiry_band", "");
            }
            try {
                cs.setInt("in_override_gen_name", aItem.getOverride_gen_name());
            } catch (NullPointerException npe) {
                cs.setInt("in_override_gen_name", 0);
            }
            try {
                cs.setInt("in_hide_unit_price_invoice", aItem.getHide_unit_price_invoice());
            } catch (NullPointerException npe) {
                cs.setInt("in_display_alias_name", 0);
            }
            cs.executeUpdate();
            save_status = 1;
        } catch (Exception e) {
            save_status = 0;
            LOGGER.log(Level.ERROR, e);
        }
        return save_status;
    }

    public void setItemFromResultset(Item aItem, ResultSet aResultSet) {
        try {
            try {
                aItem.setItemId(aResultSet.getInt("item_id"));
            } catch (NullPointerException npe) {
                aItem.setItemId(0);
            }
            try {
                aItem.setCompanyId(aResultSet.getInt("company_id"));
            } catch (NullPointerException | java.sql.SQLException npe) {
                aItem.setCompanyId(1);  // Default company
            }
            try {
                aItem.setItemCode(aResultSet.getString("item_code"));
            } catch (NullPointerException npe) {
                aItem.setItemCode("");
            }
            try {
                aItem.setDescription(aResultSet.getString("description"));
            } catch (NullPointerException npe) {
                aItem.setDescription("");
            }
            try {
                aItem.setCategoryId(aResultSet.getInt("category_id"));
            } catch (NullPointerException npe) {
                aItem.setCategoryId(0);
            }
            try {
                aItem.setSubCategoryId(aResultSet.getInt("sub_category_id"));
            } catch (NullPointerException npe) {
                aItem.setSubCategoryId(0);
            }
            try {
                aItem.setUnitId(aResultSet.getInt("unit_id"));
            } catch (NullPointerException npe) {
                aItem.setUnitId(0);
            }
            try {
                aItem.setReorderLevel(aResultSet.getDouble("reorder_level"));
            } catch (NullPointerException npe) {
                aItem.setReorderLevel(0);
            }
            try {
                aItem.setUnitCostPrice(aResultSet.getDouble("unit_cost_price"));
            } catch (NullPointerException npe) {
                aItem.setUnitCostPrice(0);
            }
            try {
                aItem.setUnitRetailsalePrice(aResultSet.getDouble("unit_retailsale_price"));
            } catch (NullPointerException npe) {
                aItem.setUnitRetailsalePrice(0);
            }
            try {
                aItem.setUnitWholesalePrice(aResultSet.getDouble("unit_wholesale_price"));
            } catch (NullPointerException npe) {
                aItem.setUnitWholesalePrice(0);
            }
            try {
                aItem.setIsSuspended(aResultSet.getString("is_suspended"));
            } catch (NullPointerException npe) {
                aItem.setIsSuspended("");
            }
            try {
                aItem.setVatRated(aResultSet.getString("vat_rated"));
            } catch (NullPointerException npe) {
                aItem.setVatRated("");
            }
            try {
                aItem.setItemImgUrl(aResultSet.getString("item_img_url"));
            } catch (NullPointerException npe) {
                aItem.setItemImgUrl("");
            }
            try {
                aItem.setItemType(aResultSet.getString("item_type"));
            } catch (NullPointerException npe) {
                aItem.setItemType("");
            }
            try {
                aItem.setCurrencyCode(aResultSet.getString("currency_code"));
            } catch (NullPointerException npe) {
                aItem.setCurrencyCode("");
            }
            try {
                aItem.setIsGeneral(aResultSet.getInt("is_general"));
            } catch (NullPointerException npe) {
                aItem.setIsGeneral(0);
            }
            try {
                aItem.setAssetType(aResultSet.getString("asset_type"));
            } catch (NullPointerException npe) {
                aItem.setAssetType("");
            }
            try {
                aItem.setIsBuy(aResultSet.getInt("is_buy"));
            } catch (NullPointerException npe) {
                aItem.setIsBuy(0);
            }
            try {
                aItem.setIsSale(aResultSet.getInt("is_sale"));
            } catch (NullPointerException npe) {
                aItem.setIsSale(0);
            }
            try {
                aItem.setIsTrack(aResultSet.getInt("is_track"));
            } catch (NullPointerException npe) {
                aItem.setIsTrack(0);
            }
            try {
                aItem.setIsAsset(aResultSet.getInt("is_asset"));
            } catch (NullPointerException npe) {
                aItem.setIsAsset(0);
            }
            try {
                if (null == aResultSet.getString("asset_account_code")) {
                    aItem.setAssetAccountCode("");
                } else {
                    aItem.setAssetAccountCode(aResultSet.getString("asset_account_code"));
                }
            } catch (NullPointerException npe) {
                aItem.setAssetAccountCode("");
            }
            try {
                aItem.setExpenseAccountCode(aResultSet.getString("expense_account_code"));
            } catch (NullPointerException npe) {
                aItem.setExpenseAccountCode("");
            }
            try {
                aItem.setIs_hire(aResultSet.getInt("is_hire"));
            } catch (NullPointerException npe) {
                aItem.setIs_hire(0);
            }
            try {
                aItem.setDuration_type(aResultSet.getString("duration_type"));
            } catch (NullPointerException npe) {
                aItem.setDuration_type("");
            }
            try {
                aItem.setUnit_hire_price(aResultSet.getDouble("unit_hire_price"));
            } catch (NullPointerException npe) {
                aItem.setUnit_hire_price(0);
            }
            try {
                aItem.setUnit_special_price(aResultSet.getDouble("unit_special_price"));
            } catch (NullPointerException npe) {
                aItem.setUnit_special_price(0);
            }
            try {
                aItem.setUnit_weight(aResultSet.getDouble("unit_weight"));
            } catch (NullPointerException npe) {
                aItem.setUnit_weight(0);
            }
            try {
                aItem.setExpense_type(aResultSet.getString("expense_type"));
            } catch (NullPointerException npe) {
                aItem.setExpense_type("");
            }
            try {
                aItem.setAlias_name(aResultSet.getString("alias_name"));
            } catch (NullPointerException npe) {
                aItem.setAlias_name("");
            }
            try {
                aItem.setDisplay_alias_name(aResultSet.getInt("display_alias_name"));
            } catch (NullPointerException npe) {
                aItem.setDisplay_alias_name(0);
            }
            try {
                aItem.setIs_free(aResultSet.getInt("is_free"));
            } catch (NullPointerException npe) {
                aItem.setIs_free(0);
            }
            try {
                aItem.setSpecify_size(aResultSet.getInt("specify_size"));
            } catch (NullPointerException npe) {
                aItem.setSpecify_size(0);
            }
            try {
                aItem.setSize_to_specific_name(aResultSet.getInt("size_to_specific_name"));
            } catch (NullPointerException npe) {
                aItem.setSize_to_specific_name(0);
            }
            try {
                aItem.setExpiry_band(aResultSet.getString("expiry_band"));
            } catch (NullPointerException npe) {
                aItem.setExpiry_band("");
            }
            try {
                aItem.setOverride_gen_name(aResultSet.getInt("override_gen_name"));
            } catch (NullPointerException npe) {
                aItem.setOverride_gen_name(0);
            }
            try {
                aItem.setHide_unit_price_invoice(aResultSet.getInt("hide_unit_price_invoice"));
            } catch (NullPointerException npe) {
                aItem.setHide_unit_price_invoice(0);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void setItemFromResultsetReport(Item aItem, ResultSet aResultSet) {
        try {
            try {
                aItem.setItemId(aResultSet.getInt("item_id"));
            } catch (NullPointerException npe) {
                aItem.setItemId(0);
            }
            try {
                aItem.setItemCode(aResultSet.getString("item_code"));
            } catch (NullPointerException npe) {
                aItem.setItemCode("");
            }
            try {
                aItem.setDescription(aResultSet.getString("description"));
            } catch (NullPointerException npe) {
                aItem.setDescription("");
            }
            try {
                aItem.setCategoryId(aResultSet.getInt("category_id"));
            } catch (NullPointerException npe) {
                aItem.setCategoryId(0);
            }
            try {
                aItem.setCategoryName(aResultSet.getString("category_name"));
            } catch (NullPointerException npe) {
                aItem.setCategoryName("");
            }
            try {
                aItem.setSubCategoryId(aResultSet.getInt("sub_category_id"));
            } catch (NullPointerException npe) {
                aItem.setSubCategoryId(0);
            }
            try {
                aItem.setSubCategoryName(aResultSet.getString("sub_category_name"));
            } catch (NullPointerException npe) {
                aItem.setSubCategoryName("");
            }
            try {
                aItem.setUnitId(aResultSet.getInt("unit_id"));
            } catch (NullPointerException npe) {
                aItem.setUnitId(0);
            }
            try {
                aItem.setUnitSymbol(aResultSet.getString("unit_symbol"));
            } catch (NullPointerException npe) {
                aItem.setUnitSymbol("");
            }
            try {
                aItem.setReorderLevel(aResultSet.getDouble("reorder_level"));
            } catch (NullPointerException npe) {
                aItem.setReorderLevel(0);
            }
            try {
                aItem.setUnitCostPrice(aResultSet.getDouble("unit_cost_price"));
            } catch (NullPointerException npe) {
                aItem.setUnitCostPrice(0);
            }
            try {
                aItem.setUnitRetailsalePrice(aResultSet.getDouble("unit_retailsale_price"));
            } catch (NullPointerException npe) {
                aItem.setUnitRetailsalePrice(0);
            }
            try {
                aItem.setUnitWholesalePrice(aResultSet.getDouble("unit_wholesale_price"));
            } catch (NullPointerException npe) {
                aItem.setUnitWholesalePrice(0);
            }
            try {
                aItem.setIsSuspended(aResultSet.getString("is_suspended"));
            } catch (NullPointerException npe) {
                aItem.setIsSuspended("");
            }
            try {
                aItem.setVatRated(aResultSet.getString("vat_rated"));
            } catch (NullPointerException npe) {
                aItem.setVatRated("");
            }
            try {
                aItem.setItemImgUrl(aResultSet.getString("item_img_url"));
            } catch (NullPointerException npe) {
                aItem.setItemImgUrl("");
            }
            try {
                aItem.setItemType(aResultSet.getString("item_type"));
            } catch (NullPointerException npe) {
                aItem.setItemType("");
            }
            try {
                aItem.setCurrencyCode(aResultSet.getString("currency_code"));
            } catch (NullPointerException npe) {
                aItem.setCurrencyCode("");
            }
            try {
                aItem.setIsGeneral(aResultSet.getInt("is_general"));
            } catch (NullPointerException npe) {
                aItem.setIsGeneral(0);
            }
            try {
                aItem.setAssetType(aResultSet.getString("asset_type"));
            } catch (NullPointerException npe) {
                aItem.setAssetType("");
            }
            try {
                aItem.setIsBuy(aResultSet.getInt("is_buy"));
            } catch (NullPointerException npe) {
                aItem.setIsBuy(0);
            }
            try {
                aItem.setIsSale(aResultSet.getInt("is_sale"));
            } catch (NullPointerException npe) {
                aItem.setIsSale(0);
            }
            try {
                aItem.setIsTrack(aResultSet.getInt("is_track"));
            } catch (NullPointerException npe) {
                aItem.setIsTrack(0);
            }
            try {
                aItem.setIsAsset(aResultSet.getInt("is_asset"));
            } catch (NullPointerException npe) {
                aItem.setIsAsset(0);
            }
            try {
                aItem.setAssetAccountCode(aResultSet.getString("asset_account_code"));
            } catch (NullPointerException npe) {
                aItem.setAssetAccountCode("");
            }
            try {
                aItem.setExpenseAccountCode(aResultSet.getString("expense_account_code"));
            } catch (NullPointerException npe) {
                aItem.setExpenseAccountCode("");
            }
            try {
                aItem.setIs_hire(aResultSet.getInt("is_hire"));
            } catch (NullPointerException npe) {
                aItem.setIs_hire(0);
            }
            try {
                aItem.setDuration_type(aResultSet.getString("duration_type"));
            } catch (NullPointerException npe) {
                aItem.setDuration_type("");
            }
            try {
                aItem.setUnit_hire_price(aResultSet.getDouble("unit_hire_price"));
            } catch (NullPointerException npe) {
                aItem.setUnit_hire_price(0);
            }
            try {
                aItem.setUnit_special_price(aResultSet.getDouble("unit_special_price"));
            } catch (NullPointerException npe) {
                aItem.setUnit_special_price(0);
            }
            try {
                aItem.setUnit_weight(aResultSet.getDouble("unit_weight"));
            } catch (NullPointerException npe) {
                aItem.setUnit_weight(0);
            }
            try {
                aItem.setExpense_type(aResultSet.getString("expense_type"));
            } catch (NullPointerException npe) {
                aItem.setExpense_type("");
            }
            try {
                aItem.setAlias_name(aResultSet.getString("alias_name"));
            } catch (NullPointerException npe) {
                aItem.setAlias_name("");
            }
            try {
                aItem.setDisplay_alias_name(aResultSet.getInt("display_alias_name"));
            } catch (NullPointerException npe) {
                aItem.setDisplay_alias_name(0);
            }
            try {
                aItem.setIs_free(aResultSet.getInt("is_free"));
            } catch (NullPointerException npe) {
                aItem.setIs_free(0);
            }
            try {
                aItem.setSpecify_size(aResultSet.getInt("specify_size"));
            } catch (NullPointerException npe) {
                aItem.setSpecify_size(0);
            }
            try {
                aItem.setSize_to_specific_name(aResultSet.getInt("size_to_specific_name"));
            } catch (NullPointerException npe) {
                aItem.setSize_to_specific_name(0);
            }
            try {
                aItem.setStock_type(aResultSet.getString("stock_type"));
            } catch (Exception e) {
                aItem.setStock_type("");
            }
            try {
                aItem.setQty_total(aResultSet.getDouble("qty_total"));
            } catch (Exception e) {
                aItem.setQty_total(0);
            }
            try {
                aItem.setStock_status(aResultSet.getString("stock_status"));
            } catch (Exception e) {
                aItem.setStock_status("");
            }
            try {
                aItem.setExpiry_band(aResultSet.getString("expiry_band"));
            } catch (Exception e) {
                aItem.setExpiry_band("");
            }
            try {
                aItem.setOverride_gen_name(aResultSet.getInt("override_gen_name"));
            } catch (NullPointerException npe) {
                aItem.setOverride_gen_name(0);
            }
            try {
                aItem.setHide_unit_price_invoice(aResultSet.getInt("hide_unit_price_invoice"));
            } catch (NullPointerException npe) {
                aItem.setHide_unit_price_invoice(0);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void setItem_code_otherFromResultset(Item_code_other aItem_code_other, ResultSet aResultSet) {
        try {
            try {
                aItem_code_other.setItem_code_other_id(aResultSet.getLong("item_code_other_id"));
            } catch (Exception e) {
                aItem_code_other.setItem_code_other_id(0);
            }
            try {
                aItem_code_other.setItem_id(aResultSet.getLong("item_id"));
            } catch (Exception e) {
                aItem_code_other.setItem_id(0);
            }
            try {
                aItem_code_other.setItem_code(aResultSet.getString("item_code"));
            } catch (Exception e) {
                aItem_code_other.setItem_code("");
            }
            try {
                aItem_code_other.setDescription(aResultSet.getString("description"));
            } catch (Exception e) {
                aItem_code_other.setDescription("");
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void setItem_unit_otherFromResultset(Item_unit_other aItem_unit_other, ResultSet aResultSet) {
        try {
            try {
                aItem_unit_other.setItem_unit_other_id(aResultSet.getLong("item_unit_other_id"));
            } catch (Exception e) {
                aItem_unit_other.setItem_unit_other_id(0);
            }
            try {
                aItem_unit_other.setItem_id(aResultSet.getLong("item_id"));
            } catch (Exception e) {
                aItem_unit_other.setItem_id(0);
            }
            try {
                aItem_unit_other.setOther_unit_id(aResultSet.getInt("other_unit_id"));
            } catch (Exception e) {
                aItem_unit_other.setOther_unit_id(0);
            }
            try {
                aItem_unit_other.setBase_qty(aResultSet.getDouble("base_qty"));
            } catch (Exception e) {
                aItem_unit_other.setBase_qty(0);
            }
            try {
                aItem_unit_other.setOther_qty(aResultSet.getDouble("other_qty"));
            } catch (Exception e) {
                aItem_unit_other.setOther_qty(0);
            }
            try {
                aItem_unit_other.setOther_unit_retailsale_price(aResultSet.getDouble("other_unit_retailsale_price"));
            } catch (Exception e) {
                aItem_unit_other.setOther_unit_retailsale_price(0);
            }
            try {
                aItem_unit_other.setOther_unit_wholesale_price(aResultSet.getDouble("other_unit_wholesale_price"));
            } catch (Exception e) {
                aItem_unit_other.setOther_unit_wholesale_price(0);
            }
            try {
                aItem_unit_other.setOther_default_purchase(aResultSet.getInt("other_default_purchase"));
            } catch (Exception e) {
                aItem_unit_other.setOther_default_purchase(0);
            }
            try {
                aItem_unit_other.setOther_default_sale(aResultSet.getInt("other_default_sale"));
            } catch (Exception e) {
                aItem_unit_other.setOther_default_sale(0);
            }
            try {
                aItem_unit_other.setIs_active(aResultSet.getInt("is_active"));
            } catch (Exception e) {
                aItem_unit_other.setIs_active(0);
            }
            try {
                aItem_unit_other.setLast_edit_by(aResultSet.getString("last_edit_by"));
            } catch (Exception e) {
                aItem_unit_other.setLast_edit_by("");
            }
            try {
                aItem_unit_other.setLast_edit_date(new Date(aResultSet.getTimestamp("last_edit_date").getTime()));
            } catch (Exception e) {
                aItem_unit_other.setLast_edit_date(null);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void setItem_excise_duty_mapFromResultset(Item_excise_duty_map aItem_excise_duty_map, ResultSet aResultSet) {
        try {
            try {
                aItem_excise_duty_map.setItem_excise_duty_map_id(aResultSet.getLong("item_excise_duty_map_id"));
            } catch (Exception e) {
                aItem_excise_duty_map.setItem_excise_duty_map_id(0);
            }
            try {
                aItem_excise_duty_map.setItem_id(aResultSet.getLong("item_id"));
            } catch (Exception e) {
                aItem_excise_duty_map.setItem_id(0);
            }
            try {
                aItem_excise_duty_map.setExcise_duty_code(aResultSet.getString("excise_duty_code"));
            } catch (Exception e) {
                aItem_excise_duty_map.setExcise_duty_code("");
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void setItem_unitFromResultset(Item_unit aItem_unit, ResultSet aResultSet) {
        try {
            try {
                aItem_unit.setUnit_id(aResultSet.getInt("unit_id"));
            } catch (Exception e) {
                aItem_unit.setUnit_id(0);
            }
            try {
                aItem_unit.setUnit_symbol(aResultSet.getString("unit_symbol"));
            } catch (Exception e) {
                aItem_unit.setUnit_symbol("");
            }
            try {
                aItem_unit.setUnit_name(aResultSet.getString("unit_name"));
            } catch (Exception e) {
                aItem_unit.setUnit_name("");
            }
            try {
                aItem_unit.setIs_base(aResultSet.getInt("is_base"));
            } catch (Exception e) {
                aItem_unit.setIs_base(0);
            }
            try {
                aItem_unit.setDefault_purchase(aResultSet.getInt("default_purchase"));
            } catch (Exception e) {
                aItem_unit.setDefault_purchase(0);
            }
            try {
                aItem_unit.setDefault_sale(aResultSet.getInt("default_sale"));
            } catch (Exception e) {
                aItem_unit.setDefault_sale(0);
            }
            try {
                aItem_unit.setBase_qty(aResultSet.getDouble("base_qty"));
            } catch (Exception e) {
                aItem_unit.setBase_qty(0);
            }
            try {
                aItem_unit.setOther_qty(aResultSet.getDouble("other_qty"));
            } catch (Exception e) {
                aItem_unit.setOther_qty(0);
            }
            try {
                aItem_unit.setUnit_retailsale_price(aResultSet.getDouble("unit_retailsale_price"));
            } catch (Exception e) {
                aItem_unit.setUnit_retailsale_price(0);
            }
            try {
                aItem_unit.setUnit_wholesale_price(aResultSet.getDouble("unit_wholesale_price"));
            } catch (Exception e) {
                aItem_unit.setUnit_wholesale_price(0);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void setItem_store_reorderFromResultset(Item_store_reorder aItem_store_reorder, ResultSet aResultSet) {
        try {
            try {
                aItem_store_reorder.setItem_store_reorder_id(aResultSet.getLong("item_store_reorder_id"));
            } catch (Exception e) {
                aItem_store_reorder.setItem_store_reorder_id(0);
            }
            try {
                aItem_store_reorder.setItem_id(aResultSet.getLong("item_id"));
            } catch (Exception e) {
                aItem_store_reorder.setItem_id(0);
            }
            try {
                aItem_store_reorder.setStore_id(aResultSet.getInt("store_id"));
                if (aItem_store_reorder.getStore_id() > 0) {
                    aItem_store_reorder.setStore_name(new StoreBean().getStore(aItem_store_reorder.getStore_id()).getStoreName());
                } else {
                    aItem_store_reorder.setStore_name("");
                }
            } catch (Exception e) {
                aItem_store_reorder.setStore_id(0);
            }
            try {
                aItem_store_reorder.setReorder_level(aResultSet.getDouble("reorder_level"));
            } catch (Exception e) {
                aItem_store_reorder.setReorder_level(0);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public Item getItem(long ItemId) {
        String sql = "{call sp_search_item_by_id(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, ItemId);
            rs = ps.executeQuery();
            if (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                return item;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public Item getItemByDesc(String aItemDesc) {
        String sql = "SELECT * FROM item WHERE description='" + aItemDesc + "'";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            if (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                return item;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public void setItem(long aItemId, Item aItem) {
        String sql = "{call sp_search_item_by_id(?)}";
        ResultSet rs = null;
        if (null == aItem) {
            aItem = new Item();
        }
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, aItemId);
            rs = ps.executeQuery();
            if (rs.next()) {
                this.setItemFromResultset(aItem, rs);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public Item findItem(long ItemId) {
        String sql = "{call sp_search_item_by_id(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, ItemId);
            rs = ps.executeQuery();
            if (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                return item;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public void setItem_unspscFromResultset(Item_unspsc aItem_unspsc, ResultSet aResultSet) {
        try {
            try {
                aItem_unspsc.setItem_unspsc_id(aResultSet.getLong("item_unspsc_id"));
            } catch (NullPointerException npe) {
                aItem_unspsc.setItem_unspsc_id(0);
            }
            try {
                aItem_unspsc.setSegment_code(aResultSet.getString("segment_code"));
            } catch (NullPointerException npe) {
                aItem_unspsc.setSegment_code("");
            }
            try {
                aItem_unspsc.setSegment_name(aResultSet.getString("segment_name"));
            } catch (NullPointerException npe) {
                aItem_unspsc.setSegment_name("");
            }
            try {
                aItem_unspsc.setFamily_code(aResultSet.getString("family_code"));
            } catch (NullPointerException npe) {
                aItem_unspsc.setFamily_code("");
            }
            try {
                aItem_unspsc.setFamily_name(aResultSet.getString("family_name"));
            } catch (NullPointerException npe) {
                aItem_unspsc.setFamily_name("");
            }
            try {
                aItem_unspsc.setClass_code(aResultSet.getString("class_code"));
            } catch (NullPointerException npe) {
                aItem_unspsc.setClass_code("");
            }
            try {
                aItem_unspsc.setClass_name(aResultSet.getString("class_name"));
            } catch (NullPointerException npe) {
                aItem_unspsc.setClass_name("");
            }
            try {
                aItem_unspsc.setCommodity_code(aResultSet.getString("commodity_code"));
            } catch (NullPointerException npe) {
                aItem_unspsc.setCommodity_code("");
            }
            try {
                aItem_unspsc.setCommodity_name(aResultSet.getString("commodity_name"));
            } catch (NullPointerException npe) {
                aItem_unspsc.setCommodity_name("");
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public int saveItem_unspsc(Item_unspsc aItem_unspscs) {
        int saved = 0;
        try {
            //save Item_unspsc
            saved = this.insertItem_unspsc(aItem_unspscs);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return saved;
    }

    public int saveItem_unspsc(List<Item_unspsc> aItem_unspscs) {
        int saved = 0;
        try {
            int Item_UNSPSCListSaved = 0;
            //save Item_unspsc
            for (int i = 0, size = aItem_unspscs.size(); i < size; i++) {
                Item_UNSPSCListSaved = Item_UNSPSCListSaved + this.insertItem_unspsc(aItem_unspscs.get(i));
            }

            if (Item_UNSPSCListSaved == aItem_unspscs.size()) {
                saved = 1;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return saved;
    }

    public int insertItem_unspsc(Item_unspsc aItem_unspsc) {
        int saved = 0;
        String sql = "INSERT INTO item_unspsc"
                + "(segment_code, segment_name, family_code, family_name, class_code, class_name, commodity_code, commodity_name,"
                + "excise_duty_product_type, vat_rate, service_mark, zero_rate, exempt_rate, add_date)"
                + "VALUES"
                + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //segment_code, segment_name, family_code, family_name, class_code, class_name, commodity_code, commodity_name
            if (aItem_unspsc.getSegment_code() != null) {
                ps.setString(1, aItem_unspsc.getSegment_code());
            } else {
                ps.setString(1, "");
            }
            if (aItem_unspsc.getSegment_name() != null) {
                ps.setString(2, aItem_unspsc.getSegment_name());
            } else {
                ps.setString(2, "");
            }
            if (aItem_unspsc.getFamily_code() != null) {
                ps.setString(3, aItem_unspsc.getFamily_code());
            } else {
                ps.setString(3, "");
            }
            if (aItem_unspsc.getFamily_name() != null) {
                ps.setString(4, aItem_unspsc.getFamily_name());
            } else {
                ps.setString(4, "");
            }
            if (aItem_unspsc.getClass_code() != null) {
                ps.setString(5, aItem_unspsc.getClass_code());
            } else {
                ps.setString(5, "");
            }
            if (aItem_unspsc.getClass_name() != null) {
                ps.setString(6, aItem_unspsc.getClass_name());
            } else {
                ps.setString(6, "");
            }
            if (aItem_unspsc.getCommodity_code() != null) {
                ps.setString(7, aItem_unspsc.getCommodity_code());
            } else {
                ps.setString(7, "");
            }
            if (aItem_unspsc.getCommodity_name() != null) {
                ps.setString(8, aItem_unspsc.getCommodity_name());
            } else {
                ps.setString(8, "");
            }
            //excise_duty_product_type, vat_rate, service_mark, zero_rate, exempt_rate, add_date
            if (aItem_unspsc.getExcise_duty_product_type() != null) {
                ps.setString(9, aItem_unspsc.getExcise_duty_product_type());
            } else {
                ps.setString(9, "");
            }
            if (aItem_unspsc.getVat_rate() != null) {
                ps.setString(10, aItem_unspsc.getVat_rate());
            } else {
                ps.setString(10, "");
            }
            if (aItem_unspsc.getService_mark() != null) {
                ps.setString(11, aItem_unspsc.getService_mark());
            } else {
                ps.setString(11, "");
            }
            if (aItem_unspsc.getZero_rate() != null) {
                ps.setString(12, aItem_unspsc.getZero_rate());
            } else {
                ps.setString(12, "");
            }
            if (aItem_unspsc.getExempt_rate() != null) {
                ps.setString(13, aItem_unspsc.getExempt_rate());
            } else {
                ps.setString(13, "");
            }
            ps.setTimestamp(14, new java.sql.Timestamp(new CompanySetting().getCURRENT_SERVER_DATE().getTime()));
            ps.executeUpdate();
            saved = 1;
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return saved;
    }

    public Item_unspsc findItem_unspsc(long aItem_unspsc_id) {
        String sql = "SELECT * FROM item_unspsc WHERE item_unspsc_id=" + aItem_unspsc_id;
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            if (rs.next()) {
                Item_unspsc iu = new Item_unspsc();
                this.setItem_unspscFromResultset(iu, rs);
                return iu;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public int deleteItem_unspsc_All() {
        int IsDeleted = 0;
        String sql = "DELETE FROM item_unspsc WHERE item_unspsc_id > ?";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, 0);
            ps.executeUpdate();
            IsDeleted = 1;
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return IsDeleted;
    }

    public Item findItemByCode(String ItemCode) {
        String sql = "{call sp_search_item_by_code(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, ItemCode);
            rs = ps.executeQuery();
            if (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                return item;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public Item findItemByCodeActive(String ItemCode) {
        if (ItemCode.trim().isEmpty()) {
            return null;
        }
        String sql = "{call sp_search_item_active_by_code(?,?)}";
        ResultSet rs = null;
        int ItemCodeErrorOn = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("GENERAL", "ITEM_CODE_ERROR_ON").getParameter_value());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, ItemCode);
            ps.setInt(2, ItemCodeErrorOn);//menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON()
            rs = ps.executeQuery();
            if (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                return item;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public Item findItemByIdActive(long aItemId) {
        if (aItemId == 0) {
            return null;
        }
        String sql = "{call sp_search_item_active_by_id(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, aItemId);
            rs = ps.executeQuery();
            if (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                return item;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public void deleteItem(Item aItem) {
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
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(msg));
        } else {
            String sql = "DELETE FROM item WHERE item_id=?";
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setLong(1, aItem.getItemId());
                ps.executeUpdate();
                this.setActionMessage(ub.translateWordsInText(BaseName, "Deleted Successfully"));
                this.clearItem(aItem);
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
                this.setActionMessage(ub.translateWordsInText(BaseName, "Item Not Deleted"));
            }
        }
    }

    public void displayItem(Item ItemFrom) {
        try {
            this.ItemObj = this.getItem(ItemFrom.getItemId());
            this.refreshStockLocation(ItemFrom.getItemId());
            this.ItemObj.setSelectedVatRateds(new UtilityBean().getStringArrayFromCommaSeperatedStr(this.ItemObj.getVatRated()));
            this.ItemObj.setVat_rate_order(this.getVatRateOrder(this.ItemObj.getVatRated()));
            Item_tax_map itmap = new Item_tax_mapBean().getItem_tax_map(this.ItemObj.getItemId());
            if (itmap != null) {
                this.ItemObj.setItem_code_tax(itmap.getItem_code_tax());
                this.ItemObj.setIs_synced_tax(itmap.getIs_synced());
            }
            this.setReorderLevelEdited(0);
            Item_excise_duty_map excobj = this.getItem_excise_duty_mapByItem(ItemFrom.getItemId());
            if (null != excobj) {
                this.ItemObj.setExcise_duty_code(excobj.getExcise_duty_code());
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void clearItemSearch() {
        try {
            this.clearItem(this.ItemObj);
            this.setSearchItemDesc("");
            this.ItemsList.clear();
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void onUNSPSCTabChange(TabChangeEvent event) {
        if (event.getTab().getTitle().equals("Manage")) {
            this.displayUNSPSC();
        }
    }

    public void displayUNSPSC() {
        try {
            //clear TaxMap
            this.Item_tax_mapObj.setItem_tax_map_id(0);
            this.Item_tax_mapObj.setDescription("");
            this.Item_tax_mapObj.setItem_id(0);
            this.Item_tax_mapObj.setItem_id_tax("");
            this.Item_tax_mapObj.setItem_code_tax("");
            this.Item_tax_mapObj.setIs_synced(0);
            this.Item_tax_mapObj.setUnit_code_tax("");
            this.Item_tax_mapObj.setTaxRateLocal("");
            this.Item_tax_mapObj.setTaxRateRemote("");
            //clear ItemTax
            this.ItemTaxObj.setGoodsCode("");
            this.ItemTaxObj.setGoodsName("");
            this.ItemTaxObj.setCommodityCategoryCode("");
            this.ItemTaxObj.setMeasureUnit("");
            if (this.ItemObj.getItemId() > 0 && new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value().length() > 0) {
                //set TaxMap
                this.Item_tax_mapObj.setDescription(this.ItemObj.getDescription());
                Item_tax_map im = new Item_tax_mapBean().getItem_tax_map(this.ItemObj.getItemId());
                if (im != null) {
                    this.Item_tax_mapObj.setItem_tax_map_id(im.getItem_tax_map_id());
                    this.Item_tax_mapObj.setItem_id(im.getItem_id());
                    this.Item_tax_mapObj.setItem_id_tax(im.getItem_id_tax());
                    this.Item_tax_mapObj.setItem_code_tax(im.getItem_code_tax());
                    this.Item_tax_mapObj.setIs_synced(im.getIs_synced());
                    this.Item_tax_mapObj.setTaxRateLocal(this.ItemObj.getVatRated());
                }
                Unit un = new UnitBean().getUnit(this.ItemObj.getUnitId());
                if (un != null) {
                    this.Item_tax_mapObj.setUnit_code_tax(un.getUnit_symbol_tax());
                }
                //set ItemTax
                if (this.Item_tax_mapObj.getItem_id_tax().length() > 0) {
                    String APIMode = new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_TAX_MODE").getParameter_value();
                    ItemTax it = null;
                    if (APIMode.equals("OFFLINE")) {
                        it = new StockManage().getItemFromTaxOffline(this.Item_tax_mapObj.getItem_id_tax());
                    } else {
                        it = new StockManage().getItemFromTaxOnline(this.Item_tax_mapObj.getItem_id_tax());
                    }
                    if (it != null) {
                        this.ItemTaxObj.setGoodsCode(it.getGoodsCode());
                        this.ItemTaxObj.setGoodsName(it.getGoodsName());
                        this.ItemTaxObj.setCommodityCategoryCode(it.getCommodityCategoryCode());
                        this.ItemTaxObj.setMeasureUnit(it.getMeasureUnit());
                        this.Item_tax_mapObj.setTaxRateRemote(this.getVatTaxRatesRemote(it));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void displayItemCodes() {
        try {
            //clear
            this.Item_code_otherObj.setItem_code_other_id(0);
            this.Item_code_otherObj.setDescription("");
            this.Item_code_otherObj.setItem_id(0);
            this.Item_code_otherObj.setItem_code("");
            try {
                this.Item_code_otherList.clear();
            } catch (Exception e) {
                this.Item_code_otherList = new ArrayList<>();
            }
            //set detail
            if (this.ItemObj.getItemId() > 0) {
                //set Item_code_other
                this.Item_code_otherObj.setDescription(this.ItemObj.getDescription());
                this.Item_code_otherObj.setItem_id(this.ItemObj.getItemId());
                this.Item_code_otherObj.setItem_code_other_id(0);
                //refresh list
                this.refreshItem_code_otherList(this.ItemObj);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void clearItem_unit_other(int aInitWithItemObj) {
        try {
            if (null != this.Item_unit_otherObj) {
                this.Item_unit_otherObj.setItem_unit_other_id(0);
                this.Item_unit_otherObj.setItem_id(0);
                this.Item_unit_otherObj.setDescription("");
                this.Item_unit_otherObj.setBase_unit_symbol("");
                this.Item_unit_otherObj.setBase_qty(0);
                this.Item_unit_otherObj.setOther_unit_id(0);
                this.Item_unit_otherObj.setOther_unit_symbol("");
                this.Item_unit_otherObj.setOther_qty(0);
                this.Item_unit_otherObj.setOther_unit_retailsale_price(0);
                this.Item_unit_otherObj.setOther_unit_wholesale_price(0);
                this.Item_unit_otherObj.setOther_default_purchase(0);
                this.Item_unit_otherObj.setOther_default_sale(0);
                this.Item_unit_otherObj.setIs_active(0);
                this.Item_unit_otherObj.setLast_edit_by("");
                this.Item_unit_otherObj.setLast_edit_date(null);
                if (aInitWithItemObj == 1 && null != this.ItemObj) {
                    this.Item_unit_otherObj.setItem_unit_other_id(0);
                    this.Item_unit_otherObj.setDescription(this.ItemObj.getDescription());
                    if (this.ItemObj.getItemId() > 0) {
                        this.Item_unit_otherObj.setItem_id(this.ItemObj.getItemId());
                    } else {
                        this.Item_unit_otherObj.setItem_id(0);
                    }
                    if (this.ItemObj.getUnitId() > 0) {
                        this.Item_unit_otherObj.setBase_unit_symbol(new UnitBean().getUnit(this.ItemObj.getUnitId()).getUnitSymbol());
                    } else {
                        this.Item_unit_otherObj.setBase_unit_symbol("");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void displayItemOtherUnits() {
        try {
            if (this.ItemOtherUnitsEdited == 1) {
                //do nothing
            } else {
                //clear and init with item obj
                this.clearItem_unit_other(1);
                try {
                    this.Item_unit_otherList.clear();
                } catch (Exception e) {
                    this.Item_unit_otherList = new ArrayList<>();
                }
                //refresh list
                if (null != this.ItemObj && this.ItemObj.getItemId() > 0) {
                    this.refreshItem_unit_otherList(this.ItemObj, 1);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void displayItemReorderLevels() {
        try {
            if (this.ReorderLevelEdited == 0) {
                //clear
                /*
                 this.Item_store_reorderObj.setItem_store_reorder_id(0);
                 this.Item_store_reorderObj.setDescription("");
                 this.Item_store_reorderObj.setItem_id(0);
                 this.Item_store_reorderObj.setReorder_level(0);
                 this.Item_store_reorderObj.setStore_id(0);
                 */
                try {
                    this.Item_store_reorderList.clear();
                } catch (Exception e) {
                    this.Item_store_reorderList = new ArrayList<>();
                }
                //set object
                this.Item_store_reorderObj.setDescription(this.ItemObj.getDescription());
                //refresh list
                this.refreshItem_store_reorderList(this.ItemObj);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void copyItemReorderLevels() {
        try {
            try {
                this.Item_store_reorderList.clear();
            } catch (Exception e) {
                this.Item_store_reorderList = new ArrayList<>();
            }
            this.refreshItem_store_reorderList(this.ItemObj);
            int records = 0;
            try {
                records = this.Item_store_reorderList.size();
            } catch (Exception e) {
            }
            if (records > 0) {
                this.ReorderLevelEdited = 1;
                for (int i = 0; i < this.Item_store_reorderList.size(); i++) {
                    this.Item_store_reorderList.get(i).setItem_store_reorder_id(0);
                    this.Item_store_reorderList.get(i).setItem_id(0);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public String getVatTaxRatesRemote(ItemTax aItemTax) {
        String str = "";
        if (Double.parseDouble(aItemTax.getTaxRate()) > 0) {
            if (str == "") {
                str = "STANDARD";
            } else {
                str = str + "," + "STANDARD";
            }
        }
        if (aItemTax.getIsExempt().equals("101") || aItemTax.getExclusion().equals("1") || aItemTax.getExclusion().equals("3")) {
            if (str == "") {
                str = "EXEMPT";
            } else {
                str = str + "," + "EXEMPT";
            }
        }
        if (aItemTax.getIsZeroRate().equals("101") || aItemTax.getExclusion().equals("0") || aItemTax.getExclusion().equals("3")) {
            if (str == "") {
                str = "ZERO";
            } else {
                str = str + "," + "ZERO";
            }
        }
        return str;
    }

    public void clearItem() {
        try {
            if (null == this.ItemObj) {
                //do nothing
            } else {
                this.ItemObj.setItemId(0);
                this.ItemObj.setItemCode("");
                this.ItemObj.setDescription("");
                this.ItemObj.setCategoryId(0);
                this.ItemObj.setSubCategoryId(0);
                this.ItemObj.setUnitId(0);
                this.ItemObj.setReorderLevel(0);
                this.ItemObj.setUnitRetailsalePrice(0);
                this.ItemObj.setUnitWholesalePrice(0);
                this.ItemObj.setIsSuspended("");
                this.ItemObj.setVatRated("");
                this.ItemObj.setItemImgUrl("");
                this.ItemObj.setItemType("");
                this.ItemObj.setCurrencyCode("");
                this.ItemObj.setIsGeneral(0);
                this.ItemObj.setAssetType("");
                this.ItemObj.setIsBuy(0);
                this.ItemObj.setIsSale(0);
                this.ItemObj.setIsTrack(0);
                this.ItemObj.setIsAsset(0);
                this.ItemObj.setAssetAccountCode("");
                this.ItemObj.setExpenseAccountCode("");
                this.ItemObj.setIs_hire(0);
                this.ItemObj.setDuration_type("");
                this.ItemObj.setUnit_hire_price(0);
                this.ItemObj.setUnit_special_price(0);
                this.ItemObj.setUnit_weight(0);
                this.ItemObj.setExpense_type("");
                this.ItemObj.setAlias_name("");
                this.ItemObj.setDisplay_alias_name(0);
                this.ItemObj.setIs_free(0);
                this.ItemObj.setSpecify_size(0);
                this.ItemObj.setSize_to_specific_name(0);
                this.ItemObj.setUnitCostPrice(0);
                this.ItemObj.setExpiry_band("");
                this.ItemObj.setOverride_gen_name(0);
                this.ItemObj.setHide_unit_price_invoice(0);
                this.ItemObj.setItem_code_tax("");
                this.ItemObj.setExcise_duty_code("");
                this.ItemObj.setIs_synced_tax(0);
                this.ItemObj.setSelectedVatRateds(null);
                this.ItemObj.setVat_rate_order(this.getVatRateOrder(new Parameter_listBean().getParameter_listByContextName("GENERAL", "TAX_VAT_RATE_ORDER").getParameter_value()));
                this.setSearchItemDesc("");
                this.refreshStockLocation(0);
                this.refreshItemsList(this.getSearchItemDesc(), 1);
                this.setReorderLevelEdited(0);
                this.setItemOtherUnitsEdited(0);
                //Default for (Yes, Track, Buy, and Account Type)
                String ItemPurpose = "";
                ItemPurpose = new GeneralUserSetting().getCurrentItemPurpose();
                if (ItemPurpose.equals("Stock")) {
                    this.ItemObj.setItemType("PRODUCT");
                    this.ItemObj.setIsBuy(1);
                    this.ItemObj.setIsTrack(1);
                    this.ItemObj.setExpense_type("Merchandise");
                    this.ItemObj.setExpenseAccountCode("1-00-020-010");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void copyToNew() {
        try {
            if (null == this.ItemObj) {
                //do nothing
            } else {
                this.copyItemReorderLevels();
                this.ItemObj.setItemId(0);
                this.ItemObj.setDescription(this.ItemObj.getDescription() + " Copy");
                this.ItemObj.setItemCode("");
                this.ItemObj.setIs_synced_tax(0);
                this.refreshStockLocation(0);
                try {
                    this.Item_code_otherList.clear();
                } catch (Exception e) {

                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void clearItem(Item aItem) {
        try {
            if (null != aItem) {
                aItem.setItemId(0);
                aItem.setItemCode("");
                aItem.setDescription("");
                aItem.setUnitSymbol("");
                aItem.setCategoryId(0);
                aItem.setSubCategoryId(0);
                aItem.setUnitId(0);
                aItem.setReorderLevel(0);
                aItem.setUnitCostPrice(0);
                aItem.setUnitRetailsalePrice(0);
                aItem.setUnitWholesalePrice(0);
                aItem.setIsSuspended("");
                aItem.setVatRated("");
                aItem.setItemImgUrl("");
                aItem.setItemType("");
                aItem.setCurrencyCode("");
                aItem.setIsGeneral(0);
                aItem.setAssetType("");
                aItem.setIsBuy(0);
                aItem.setIsSale(0);
                aItem.setIsTrack(0);
                aItem.setIsAsset(0);
                aItem.setAssetAccountCode("");
                aItem.setExpenseAccountCode("");
                aItem.setIs_hire(0);
                aItem.setDuration_type("");
                aItem.setUnit_hire_price(0);
                aItem.setUnit_special_price(0);
                aItem.setUnit_weight(0);
                aItem.setExpense_type("");
                aItem.setAlias_name("");
                aItem.setDisplay_alias_name(0);
                aItem.setIs_free(0);
                aItem.setSpecify_size(0);
                aItem.setSize_to_specific_name(0);
                aItem.setExpiry_band("");
                aItem.setOverride_gen_name(0);
                aItem.setHide_unit_price_invoice(0);
                aItem.setPurpose("");
                aItem.setItem_code_tax("");
                aItem.setExcise_duty_code("");
                aItem.setIs_synced_tax(0);
                aItem.setSelectedVatRateds(null);
                aItem.setVat_rate_order(this.getVatRateOrder(new Parameter_listBean().getParameter_listByContextName("GENERAL", "TAX_VAT_RATE_ORDER").getParameter_value()));
                this.setSearchItemDesc("");
                this.refreshStockLocation(0);
                this.setReorderLevelEdited(0);
                this.setItemOtherUnitsEdited(0);
                aItem.setStore_id(0);
                //Default for (Yes, Track, Buy, and Account Type)
                String ItemPurpose = "";
                ItemPurpose = new GeneralUserSetting().getCurrentItemPurpose();
                if (null != ItemPurpose) {
                    if (ItemPurpose.equals("Stock")) {
                        aItem.setItemType("PRODUCT");
                        aItem.setIsBuy(1);
                        aItem.setIsTrack(1);
                        aItem.setExpense_type("Merchandise");
                        aItem.setExpenseAccountCode("1-00-020-010");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void initClearItem(Item aItem, List<Item> aItemList) {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            try {
                if (aItem != null) {
                    this.clearItem(aItem);
                }
            } catch (NullPointerException npe) {
            }
            try {
                if (aItemList != null) {
                    aItemList.clear();
                }
            } catch (NullPointerException npe) {
            }
        }
    }

    public void initClearItemParent() {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            try {
                if (null == this.ParentItem) {
                } else {
                    this.ParentItem = new Item();
                }
            } catch (NullPointerException npe) {
            }
        }
    }

    public void clearSelectedItem() {
        this.clearItem(this.getSelectedItem());
    }

    public List<Item> getItemObjectList_old(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_by_code_desc(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectList(String Query) {
        this.setTypedItemCode(Query);
        int companyId = new GeneralUserSetting().getCurrentUser().getCompanyId();
        String sql, sqlDesc = "", sqlCode = "", sqlCodeOther = "";
        Item_code_other ico = this.getItem_code_otherByCode(Query);
        //desc
        String[] ArrayDesc = new UtilityBean().getStringArrayFromXSeperatedStr(Query, " ");
        if (menuItemBean.getMenuItemObj().getITEM_FULL_SEARCH_ON() == 1 && ArrayDesc.length > 1) {
            for (String ArrayDesc1 : ArrayDesc) {
                if (sqlDesc.length() == 0) {
                    sqlDesc = " i.description LIKE '%" + ArrayDesc1 + "%' ";
                } else {
                    sqlDesc = sqlDesc + " AND i.description LIKE '%" + ArrayDesc1 + "%' ";
                }
            }
        } else {
            sqlDesc = " i.description LIKE '%" + Query + "%' ";
        }
        //code
        if (menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON() == 1) {
            //sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            if (Query.length() > 1) {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            } else {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' ";
            }
        } else {
            sqlCode = " i.item_code='" + Query + "' ";
        }
        //code other
        if (null != ico) {
            sqlCodeOther = " OR (i.item_id=" + ico.getItem_id() + ") ";
        } else {
            sqlCodeOther = "";
        }
        sql = "SELECT * FROM view_item i WHERE i.company_id=" + companyId + " AND ("
                + "(" + sqlDesc + ") "
                + "OR "
                + "(" + sqlCode + ") "
                + "OR "
                + "(i.alias_name LIKE '%" + Query + "%') "
                + sqlCodeOther
                + ") ORDER BY i.description ASC LIMIT " + menuItemBean.getMenuItemObj().getSEARCH_ITEMS_LIST_LIMIT();
        //System.out.println("SQL:" + sql);
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                //this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListActive_old(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_active_by_code_desc(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<Item>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListActive(String Query) {
        this.setTypedItemCode(Query);
        int companyId = new GeneralUserSetting().getCurrentUser().getCompanyId();
        String sql, sqlDesc = "", sqlCode = "", sqlCodeOther = "";
        Item_code_other ico = this.getItem_code_otherByCode(Query);
        //desc
        String[] ArrayDesc = new UtilityBean().getStringArrayFromXSeperatedStr(Query, " ");
        if (menuItemBean.getMenuItemObj().getITEM_FULL_SEARCH_ON() == 1 && ArrayDesc.length > 1) {
            for (String ArrayDesc1 : ArrayDesc) {
                if (sqlDesc.length() == 0) {
                    sqlDesc = " i.description LIKE '%" + ArrayDesc1 + "%' ";
                } else {
                    sqlDesc = sqlDesc + " AND i.description LIKE '%" + ArrayDesc1 + "%' ";
                }
            }
        } else {
            sqlDesc = " i.description LIKE '%" + Query + "%' ";
        }
        //code
        if (menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON() == 1) {
            //sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            if (Query.length() > 1) {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            } else {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' ";
            }
        } else {
            sqlCode = " i.item_code='" + Query + "' ";
        }
        //code other
        if (null != ico) {
            sqlCodeOther = " OR (i.item_id=" + ico.getItem_id() + ") ";
        } else {
            sqlCodeOther = "";
        }
        sql = "SELECT * FROM item i WHERE i.company_id=" + companyId + " AND i.is_suspended='No' AND ("
                + "(" + sqlDesc + ") "
                + "OR "
                + "(" + sqlCode + ") "
                + "OR "
                + "(i.alias_name LIKE '%" + Query + "%') "
                + sqlCodeOther
                + ") ORDER BY i.description ASC LIMIT " + menuItemBean.getMenuItemObj().getSEARCH_ITEMS_LIST_LIMIT();
        //System.out.println("SQL:" + sql);
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                //this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListByUnit(int Unit) {
        String sql;
        sql = "{call sp_search_item_by_unit(?)}";
        ResultSet rs;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, Unit);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListByCategory(int Category) {
        String sql;
        sql = "{call sp_search_item_by_category(?)}";
        ResultSet rs;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, Category);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public void updateLookUpsUI(Item aItem) {
        try {
            //Item item = null;
            if (null == aItem) {
                //do nothing
            } else {
                //unit symbol
                try {
                    aItem.setUnitSymbol(new UnitBean().getUnit(aItem.getUnitId()).getUnitSymbol());
                } catch (NullPointerException npe) {
                    aItem.setUnitSymbol("");
                }
                //category
                try {
                    aItem.setCategoryName(new CategoryBean().getCategory(aItem.getCategoryId()).getCategoryName());
                } catch (NullPointerException npe) {
                    aItem.setCategoryName("");
                }
                //location
                try {
                    if (aItem.getIsTrack() == 1) {
                        LocationBean lb = new LocationBean();
                        aItem.setLocationName(lb.getLocationsString(lb.getLocationsByItem(aItem.getItemId())));
                    }
                } catch (NullPointerException npe) {
                    aItem.setLocationName("");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public List<Item> getItemObjectListForSale_old(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_sale_old(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForSale(String Query) {
        this.setTypedItemCode(Query);
        int companyId = new GeneralUserSetting().getCurrentUser().getCompanyId();
        String sql, sqlDesc = "", sqlCode = "", sqlCodeOther = "";
        Item_code_other ico = this.getItem_code_otherByCode(Query);
        //desc
        String[] ArrayDesc = new UtilityBean().getStringArrayFromXSeperatedStr(Query, " ");
        if (menuItemBean.getMenuItemObj().getITEM_FULL_SEARCH_ON() == 1 && ArrayDesc.length > 1) {
            for (String ArrayDesc1 : ArrayDesc) {
                if (sqlDesc.length() == 0) {
                    sqlDesc = " i.description LIKE '%" + ArrayDesc1 + "%' ";
                } else {
                    sqlDesc = sqlDesc + " AND i.description LIKE '%" + ArrayDesc1 + "%' ";
                }
            }
        } else {
            sqlDesc = " i.description LIKE '%" + Query + "%' ";
        }
        //code
        if (menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON() == 1) {
            //sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            if (Query.length() > 1) {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            } else {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' ";
            }
        } else {
            sqlCode = " i.item_code='" + Query + "' ";
        }
        //code other
        if (null != ico) {
            sqlCodeOther = " OR (i.item_id=" + ico.getItem_id() + ") ";
        } else {
            sqlCodeOther = "";
        }
        sql = "SELECT * FROM item i WHERE i.company_id=" + companyId + " AND i.is_suspended='No' AND i.is_sale=1 AND i.is_asset=0 AND ("
                + "(" + sqlDesc + ") "
                + "OR "
                + "(" + sqlCode + ") "
                + "OR "
                + "(i.alias_name LIKE '%" + Query + "%') "
                + sqlCodeOther
                + ") ORDER BY i.description ASC LIMIT " + menuItemBean.getMenuItemObj().getSEARCH_ITEMS_LIST_LIMIT();
        //System.out.println("SQL:" + sql);
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                //this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item_unspsc> getItem_unspscObjectList(String Query) {
        String sql;
        sql = "SELECT * FROM item_unspsc WHERE segment_name LIKE '%" + Query + "%' OR family_name LIKE '%" + Query + "%' OR class_name LIKE '%" + Query + "%' OR commodity_name LIKE '%" + Query + "%' ";
        ResultSet rs = null;
        List<Item_unspsc> ius = new ArrayList<>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                Item_unspsc iu = new Item_unspsc();
                this.setItem_unspscFromResultset(iu, rs);
                ius.add(iu);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return ius;
    }

    public List<Item> getItemObjectListForProduction_old(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_production(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<Item>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForProduction(String Query) {
        this.setTypedItemCode(Query);
        String sql, sqlDesc = "", sqlCode = "", sqlCodeOther = "";
        Item_code_other ico = this.getItem_code_otherByCode(Query);
        //desc
        String[] ArrayDesc = new UtilityBean().getStringArrayFromXSeperatedStr(Query, " ");
        if (menuItemBean.getMenuItemObj().getITEM_FULL_SEARCH_ON() == 1 && ArrayDesc.length > 1) {
            for (String ArrayDesc1 : ArrayDesc) {
                if (sqlDesc.length() == 0) {
                    sqlDesc = " i.description LIKE '%" + ArrayDesc1 + "%' ";
                } else {
                    sqlDesc = sqlDesc + " AND i.description LIKE '%" + ArrayDesc1 + "%' ";
                }
            }
        } else {
            sqlDesc = " i.description LIKE '%" + Query + "%' ";
        }
        //code
        if (menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON() == 1) {
            //sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            if (Query.length() > 1) {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            } else {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' ";
            }
        } else {
            sqlCode = " i.item_code='" + Query + "' ";
        }
        //code other
        if (null != ico) {
            sqlCodeOther = " OR (i.item_id=" + ico.getItem_id() + ") ";
        } else {
            sqlCodeOther = "";
        }
        sql = "SELECT * FROM item i WHERE i.is_suspended='No' AND i.is_track=1 AND i.is_asset=0 AND ("
                + "(" + sqlDesc + ") "
                + "OR "
                + "(" + sqlCode + ") "
                + "OR "
                + "(i.alias_name LIKE '%" + Query + "%') "
                + sqlCodeOther
                + ") ORDER BY i.description ASC LIMIT " + menuItemBean.getMenuItemObj().getSEARCH_ITEMS_LIST_LIMIT();
        //System.out.println("SQL:" + sql);
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                //this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForHire(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_hire(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<Item>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForStockDispose_old(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_stock_dispose(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<Item>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForStockDispose(String Query) {
        this.setTypedItemCode(Query);
        String sql, sqlDesc = "", sqlCode = "", sqlCodeOther = "";
        Item_code_other ico = this.getItem_code_otherByCode(Query);
        //desc
        String[] ArrayDesc = new UtilityBean().getStringArrayFromXSeperatedStr(Query, " ");
        if (menuItemBean.getMenuItemObj().getITEM_FULL_SEARCH_ON() == 1 && ArrayDesc.length > 1) {
            for (String ArrayDesc1 : ArrayDesc) {
                if (sqlDesc.length() == 0) {
                    sqlDesc = " i.description LIKE '%" + ArrayDesc1 + "%' ";
                } else {
                    sqlDesc = sqlDesc + " AND i.description LIKE '%" + ArrayDesc1 + "%' ";
                }
            }
        } else {
            sqlDesc = " i.description LIKE '%" + Query + "%' ";
        }
        //code
        if (menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON() == 1) {
            //sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            if (Query.length() > 1) {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            } else {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' ";
            }
        } else {
            sqlCode = " i.item_code='" + Query + "' ";
        }
        //code other
        if (null != ico) {
            sqlCodeOther = " OR (i.item_id=" + ico.getItem_id() + ") ";
        } else {
            sqlCodeOther = "";
        }
        sql = "SELECT * FROM item i WHERE i.is_suspended='No' AND i.is_asset=0 AND i.is_track=1 AND ("
                + "(" + sqlDesc + ") "
                + "OR "
                + "(" + sqlCode + ") "
                + "OR "
                + "(i.alias_name LIKE '%" + Query + "%') "
                + sqlCodeOther
                + ") ORDER BY i.description ASC LIMIT " + menuItemBean.getMenuItemObj().getSEARCH_ITEMS_LIST_LIMIT();
        //System.out.println("SQL:" + sql);
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                //this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForConsumption(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_consumption(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<Item>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForTransfer(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_transfer(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<Item>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForUnpack(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_unpack(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<Item>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForPurchase_old(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_purchase(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<Item>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForPurchase(String Query) {
        this.setTypedItemCode(Query);
        int companyId = new GeneralUserSetting().getCurrentUser().getCompanyId();
        String sql, sqlDesc = "", sqlCode = "", sqlCodeOther = "";
        Item_code_other ico = this.getItem_code_otherByCode(Query);
        //desc
        String[] ArrayDesc = new UtilityBean().getStringArrayFromXSeperatedStr(Query, " ");
        if (menuItemBean.getMenuItemObj().getITEM_FULL_SEARCH_ON() == 1 && ArrayDesc.length > 1) {
            for (String ArrayDesc1 : ArrayDesc) {
                if (sqlDesc.length() == 0) {
                    sqlDesc = " i.description LIKE '%" + ArrayDesc1 + "%' ";
                } else {
                    sqlDesc = sqlDesc + " AND i.description LIKE '%" + ArrayDesc1 + "%' ";
                }
            }
        } else {
            sqlDesc = " i.description LIKE '%" + Query + "%' ";
        }
        //code
        if (menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON() == 1) {
            //sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            if (Query.length() > 1) {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            } else {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' ";
            }
        } else {
            sqlCode = " i.item_code='" + Query + "' ";
        }
        //code other
        if (null != ico) {
            sqlCodeOther = " OR (i.item_id=" + ico.getItem_id() + ") ";
        } else {
            sqlCodeOther = "";
        }
        sql = "SELECT * FROM item i WHERE i.company_id=" + companyId + " AND i.is_suspended='No' AND i.is_buy=1 AND ("
                + "(" + sqlDesc + ") "
                + "OR "
                + "(" + sqlCode + ") "
                + "OR "
                + "(i.alias_name LIKE '%" + Query + "%') "
                + sqlCodeOther
                + ") ORDER BY i.description ASC LIMIT " + menuItemBean.getMenuItemObj().getSEARCH_ITEMS_LIST_LIMIT();
        //System.out.println("SQL:" + sql);
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                //this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForReceiveGoods_old(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_receive_goods(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForReceiveGoods(String Query) {
        this.setTypedItemCode(Query);
        String sql, sqlDesc = "", sqlCode = "", sqlCodeOther = "";
        Item_code_other ico = this.getItem_code_otherByCode(Query);
        //desc
        String[] ArrayDesc = new UtilityBean().getStringArrayFromXSeperatedStr(Query, " ");
        if (menuItemBean.getMenuItemObj().getITEM_FULL_SEARCH_ON() == 1 && ArrayDesc.length > 1) {
            for (String ArrayDesc1 : ArrayDesc) {
                if (sqlDesc.length() == 0) {
                    sqlDesc = " i.description LIKE '%" + ArrayDesc1 + "%' ";
                } else {
                    sqlDesc = sqlDesc + " AND i.description LIKE '%" + ArrayDesc1 + "%' ";
                }
            }
        } else {
            sqlDesc = " i.description LIKE '%" + Query + "%' ";
        }
        //code
        if (menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON() == 1) {
            //sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            if (Query.length() > 1) {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            } else {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' ";
            }
        } else {
            sqlCode = " i.item_code='" + Query + "' ";
        }
        //code other
        if (null != ico) {
            sqlCodeOther = " OR (i.item_id=" + ico.getItem_id() + ") ";
        } else {
            sqlCodeOther = "";
        }
        sql = "SELECT * FROM item i WHERE i.is_suspended='No' AND i.is_buy=1 AND i.is_sale=1 AND i.is_asset=0 AND i.is_track=1 AND ("
                + "(" + sqlDesc + ") "
                + "OR "
                + "(" + sqlCode + ") "
                + "OR "
                + "(i.alias_name LIKE '%" + Query + "%') "
                + sqlCodeOther
                + ") ORDER BY i.description ASC LIMIT " + menuItemBean.getMenuItemObj().getSEARCH_ITEMS_LIST_LIMIT();
        //System.out.println("SQL:" + sql);
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                //this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForReceiveExpenses(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_receive_expenses(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForReceiveAssets(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_receive_assets(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForPurchaseExpense_old(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_purchase_expense(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<Item>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForPurchaseExpense(String Query) {
        this.setTypedItemCode(Query);
        String sql, sqlDesc = "", sqlCode = "", sqlCodeOther = "";
        Item_code_other ico = this.getItem_code_otherByCode(Query);
        //desc
        String[] ArrayDesc = new UtilityBean().getStringArrayFromXSeperatedStr(Query, " ");
        if (menuItemBean.getMenuItemObj().getITEM_FULL_SEARCH_ON() == 1 && ArrayDesc.length > 1) {
            for (String ArrayDesc1 : ArrayDesc) {
                if (sqlDesc.length() == 0) {
                    sqlDesc = " i.description LIKE '%" + ArrayDesc1 + "%' ";
                } else {
                    sqlDesc = sqlDesc + " AND i.description LIKE '%" + ArrayDesc1 + "%' ";
                }
            }
        } else {
            sqlDesc = " i.description LIKE '%" + Query + "%' ";
        }
        //code
        if (menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON() == 1) {
            //sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            if (Query.length() > 1) {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            } else {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' ";
            }
        } else {
            sqlCode = " i.item_code='" + Query + "' ";
        }
        //code other
        if (null != ico) {
            sqlCodeOther = " OR (i.item_id=" + ico.getItem_id() + ") ";
        } else {
            sqlCodeOther = "";
        }
        sql = "SELECT * FROM item i WHERE is_suspended='No' AND is_buy=1 AND is_sale=0 AND is_asset=0 AND ("
                + "(" + sqlDesc + ") "
                + "OR "
                + "(" + sqlCode + ") "
                + "OR "
                + "(i.alias_name LIKE '%" + Query + "%') "
                + sqlCodeOther
                + ") ORDER BY i.description ASC LIMIT " + menuItemBean.getMenuItemObj().getSEARCH_ITEMS_LIST_LIMIT();
        //System.out.println("SQL:" + sql);
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                //this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForPurchaseGoods_old(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_purchase_goods(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<Item>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForPurchaseGoods(String Query) {
        this.setTypedItemCode(Query);
        String sql, sqlDesc = "", sqlCode = "", sqlCodeOther = "";
        Item_code_other ico = this.getItem_code_otherByCode(Query);
        //desc
        String[] ArrayDesc = new UtilityBean().getStringArrayFromXSeperatedStr(Query, " ");
        if (menuItemBean.getMenuItemObj().getITEM_FULL_SEARCH_ON() == 1 && ArrayDesc.length > 1) {
            for (String ArrayDesc1 : ArrayDesc) {
                if (sqlDesc.length() == 0) {
                    sqlDesc = " i.description LIKE '%" + ArrayDesc1 + "%' ";
                } else {
                    sqlDesc = sqlDesc + " AND i.description LIKE '%" + ArrayDesc1 + "%' ";
                }
            }
        } else {
            sqlDesc = " i.description LIKE '%" + Query + "%' ";
        }
        //code
        if (menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON() == 1) {
            //sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            if (Query.length() > 1) {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            } else {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' ";
            }
        } else {
            sqlCode = " i.item_code='" + Query + "' ";
        }
        //code other
        if (null != ico) {
            sqlCodeOther = " OR (i.item_id=" + ico.getItem_id() + ") ";
        } else {
            sqlCodeOther = "";
        }
        sql = "SELECT * FROM item i WHERE i.is_suspended='No' AND i.is_buy=1 AND i.is_sale=1 AND i.is_asset=0 AND ("
                + "(" + sqlDesc + ") "
                + "OR "
                + "(" + sqlCode + ") "
                + "OR "
                + "(i.alias_name LIKE '%" + Query + "%') "
                + sqlCodeOther
                + ") ORDER BY i.description ASC LIMIT " + menuItemBean.getMenuItemObj().getSEARCH_ITEMS_LIST_LIMIT();
        //System.out.println("SQL:" + sql);
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                //this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForAssetFixed(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_asset_fixed(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<Item>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    /**
     * @param ItemObjectList the ItemObjectList to set
     */
    public void setItemObjectList(List<Item> ItemObjectList) {
        this.ItemObjectList = ItemObjectList;
    }

    /**
     * @param aNameOrCode
     * @return the Items
     */
    public List<Item> getItems_old(String aNameOrCode) {
        String sql = "{call sp_search_item_by_code_desc(?)}";
        ResultSet rs = null;
        Items = new ArrayList<Item>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, aNameOrCode);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                Items.add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return Items;
    }

    public List<Item> getItems(String Query) {
        this.setTypedItemCode(Query);
        String sql, sqlDesc = "", sqlCode = "", sqlCodeOther = "";
        Item_code_other ico = this.getItem_code_otherByCode(Query);
        //desc
        String[] ArrayDesc = new UtilityBean().getStringArrayFromXSeperatedStr(Query, " ");
        if (menuItemBean.getMenuItemObj().getITEM_FULL_SEARCH_ON() == 1 && ArrayDesc.length > 1) {
            for (String ArrayDesc1 : ArrayDesc) {
                if (sqlDesc.length() == 0) {
                    sqlDesc = " i.description LIKE '%" + ArrayDesc1 + "%' ";
                } else {
                    sqlDesc = sqlDesc + " AND i.description LIKE '%" + ArrayDesc1 + "%' ";
                }
            }
        } else {
            sqlDesc = " i.description LIKE '%" + Query + "%' ";
        }
        //code
        if (menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON() == 1) {
            //sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            if (Query.length() > 1) {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            } else {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' ";
            }
        } else {
            sqlCode = " i.item_code='" + Query + "' ";
        }
        //code other
        if (null != ico) {
            sqlCodeOther = " OR (i.item_id=" + ico.getItem_id() + ") ";
        } else {
            sqlCodeOther = "";
        }
        sql = "SELECT * FROM view_item i WHERE 1=1 AND ("
                + "(" + sqlDesc + ") "
                + "OR "
                + "(" + sqlCode + ") "
                + "OR "
                + "(i.alias_name LIKE '%" + Query + "%') "
                + sqlCodeOther
                + ") ORDER BY i.description ASC LIMIT " + menuItemBean.getMenuItemObj().getSEARCH_ITEMS_LIST_LIMIT();
        //System.out.println("SQL:" + sql);
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                //this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForQuickOrder(int aCategoryId, int aSubCategoryId) {
        String sql = "";
        String aWhereSql = "";
        if (aCategoryId > 0) {
            aWhereSql = aWhereSql + " AND category_id=" + aCategoryId;
        }
        if (aSubCategoryId > 0) {
            aWhereSql = aWhereSql + " AND sub_category_id=" + aSubCategoryId;
        }
        if (aSubCategoryId < 0) {
            aWhereSql = aWhereSql + " AND sub_category_id IS NULL";
        }
        sql = "SELECT * FROM item WHERE is_suspended='No' AND is_sale=1 AND is_asset=0 " + aWhereSql
                + " ORDER BY description ASC";
        //System.out.println("SQL:" + sql);
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setInt(1, aCategoryId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public void refreshItemsList_old(String aNameOrCode) {
        this.ItemsList = new ArrayList<>();
        if (aNameOrCode.replace(" ", "").length() <= 0) {
            this.ItemsList.clear();
        } else {
            String ItemPurpose = "";
            int IsAsset = 0;
            int IsSale = 0;
            ItemPurpose = new GeneralUserSetting().getCurrentItemPurpose();
            if (ItemPurpose.equals("Asset")) {
                IsAsset = 1;
                IsSale = 0;
            }
            if (ItemPurpose.equals("Stock")) {
                IsAsset = 0;
                IsSale = 1;
            }
            if (ItemPurpose.equals("Expense")) {
                IsAsset = 0;
                IsSale = 0;
            }
            String sql = "{call sp_search_item_by_code_desc_purpose(?,?,?)}";
            ResultSet rs = null;
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setString(1, aNameOrCode);
                ps.setInt(2, IsAsset);
                ps.setInt(3, IsSale);
                rs = ps.executeQuery();
                Item item = null;
                while (rs.next()) {
                    item = new Item();
                    this.setItemFromResultset(item, rs);
                    this.ItemsList.add(item);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    public void setRetailToWhole() {
        try {
            this.ItemObj.setUnitWholesalePrice(this.ItemObj.getUnitRetailsalePrice());
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void refreshItemsList(String aNameOrCode, int aLimitFlag) {//aLimitFlag: 0 No Limit, 1 Use Set Limit
        this.ItemsList = new ArrayList<>();
        if (aNameOrCode.replace(" ", "").length() <= 0) {
            this.ItemsList.clear();
        } else {
            String ItemPurpose = "";
            int IsAsset = 0;
            int IsSale = 0;
            ItemPurpose = new GeneralUserSetting().getCurrentItemPurpose();
            if (ItemPurpose.equals("Asset")) {
                IsAsset = 1;
                IsSale = 0;
            }
            if (ItemPurpose.equals("Stock")) {
                IsAsset = 0;
                IsSale = 1;
            }
            if (ItemPurpose.equals("Expense")) {
                IsAsset = 0;
                IsSale = 0;
            }
            String sql, sqlDesc = "", sqlCode = "", sqlCodeOther = "";
            Item_code_other ico = this.getItem_code_otherByCode(aNameOrCode);
            //desc
            String[] ArrayDesc = new UtilityBean().getStringArrayFromXSeperatedStr(aNameOrCode, " ");
            if (menuItemBean.getMenuItemObj().getITEM_FULL_SEARCH_ON() == 1 && ArrayDesc.length > 1) {
                for (String ArrayDesc1 : ArrayDesc) {
                    if (sqlDesc.length() == 0) {
                        sqlDesc = " i.description LIKE '%" + ArrayDesc1 + "%' ";
                    } else {
                        sqlDesc = sqlDesc + " AND i.description LIKE '%" + ArrayDesc1 + "%' ";
                    }
                }
            } else {
                sqlDesc = " i.description LIKE '%" + aNameOrCode + "%' ";
            }
            //code
            if (menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON() == 1) {
                //sqlCode = " i.item_code LIKE '%" + aNameOrCode + "%' OR i.item_code LIKE '%" + aNameOrCode.substring(1) + "%' OR i.item_code LIKE '%" + aNameOrCode.substring(2) + "%' ";
                if (aNameOrCode.length() > 1) {
                    sqlCode = " i.item_code LIKE '%" + aNameOrCode + "%' OR i.item_code LIKE '%" + aNameOrCode.substring(1) + "%' OR i.item_code LIKE '%" + aNameOrCode.substring(2) + "%' ";
                } else {
                    sqlCode = " i.item_code LIKE '%" + aNameOrCode + "%' OR i.item_code LIKE '%" + aNameOrCode.substring(1) + "%' ";
                }
            } else {
                sqlCode = " i.item_code='" + aNameOrCode + "' ";
            }
            //code other
            if (null != ico) {
                sqlCodeOther = " OR (i.item_id=" + ico.getItem_id() + ") ";
            } else {
                sqlCodeOther = "";
            }
            String LimitStr = "";
            if (aLimitFlag == 1) {
                LimitStr = " LIMIT " + menuItemBean.getMenuItemObj().getSEARCH_ITEMS_LIST_LIMIT();
            }
            sql = "SELECT * FROM item i WHERE i.is_asset=" + IsAsset + " AND i.is_sale=" + IsSale + " AND ("
                    + "(" + sqlDesc + ") "
                    + "OR "
                    + "(" + sqlCode + ") "
                    + "OR "
                    + "(i.alias_name LIKE '%" + aNameOrCode + "%') "
                    + sqlCodeOther
                    + ") ORDER BY i.description ASC " + LimitStr;
            ResultSet rs = null;
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                rs = ps.executeQuery();
                Item item = null;
                while (rs.next()) {
                    item = new Item();
                    this.setItemFromResultset(item, rs);
                    this.ItemsList.add(item);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    public List<Item> getReportItems(Item aItem, boolean RETRIEVE_REPORT) {
        String sql = "{call sp_report_item(?,?,?)}";
        ResultSet rs = null;
        this.ReportItems.clear();
        if (aItem != null && RETRIEVE_REPORT == true) {
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setInt(1, aItem.getCategoryId());
                try {
                    ps.setInt(2, aItem.getSubCategoryId());
                } catch (NullPointerException npe) {
                    ps.setInt(2, 0);
                }
                ps.setString(3, aItem.getIsSuspended());
                rs = ps.executeQuery();
                while (rs.next()) {
                    Item item = new Item();
                    aItem.setItemId(rs.getLong("item_id"));
                    aItem.setItemCode(rs.getString("item_code"));
                    aItem.setDescription(rs.getString("description"));
                    aItem.setCategoryId(rs.getInt("category_id"));
                    aItem.setCategoryName(rs.getString("category_name"));
                    try {
                        aItem.setSubCategoryId(rs.getInt("sub_category_id"));
                        aItem.setSubCategoryName(rs.getString("sub_category_name"));
                    } catch (NullPointerException npe) {
                        aItem.setSubCategoryId(0);
                        aItem.setSubCategoryName("");
                    }
                    aItem.setUnitId(rs.getInt("unit_id"));
                    aItem.setUnitSymbol(rs.getString("unit_symbol"));
                    aItem.setReorderLevel(rs.getInt("reorder_level"));
                    aItem.setUnitCostPrice(rs.getDouble("unit_cost_price"));
                    aItem.setUnitRetailsalePrice(rs.getDouble("unit_retailsale_price"));
                    aItem.setUnitWholesalePrice(rs.getDouble("unit_wholesale_price"));
                    aItem.setIsSuspended(rs.getString("is_suspended"));
                    aItem.setVatRated(rs.getString("vat_rated"));
                    try {
                        aItem.setItemImgUrl(rs.getString("item_img_url"));
                    } catch (NullPointerException npe) {
                        aItem.setItemImgUrl("");
                    }
                    aItem.setItemType(rs.getString("item_type"));
                    this.ReportItems.add(item);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        } else {
            this.ReportItems.clear();
        }
        return this.ReportItems;
    }

    public long getReportItemsCount() {
        return this.ReportItems.size();
    }

    public List<Item> getReportItemsSummary(Item aItem, boolean RETRIEVE_REPORT) {
        String sql = "{call sp_report_item_summary(?,?,?)}";
        ResultSet rs = null;
        this.ReportItemsSummary.clear();
        if (aItem != null && RETRIEVE_REPORT == true) {
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setInt(1, aItem.getCategoryId());
                try {
                    ps.setInt(2, aItem.getSubCategoryId());
                } catch (NullPointerException npe) {
                    ps.setInt(2, 0);
                }
                ps.setString(3, aItem.getIsSuspended());
                rs = ps.executeQuery();
                while (rs.next()) {
                    Item item = new Item();
                    aItem.setCategoryId(rs.getInt("category_id"));
                    aItem.setCategoryName(rs.getString("category_name"));
                    try {
                        aItem.setSubCategoryId(rs.getInt("sub_category_id"));
                        aItem.setSubCategoryName(rs.getString("sub_category_name"));
                    } catch (NullPointerException npe) {
                        aItem.setSubCategoryId(0);
                        aItem.setSubCategoryName("");
                    }
                    try {
                        aItem.setCountItems(rs.getInt("count_items"));
                    } catch (NullPointerException npe) {
                        aItem.setCountItems(0);
                    }

                    this.ReportItemsSummary.add(item);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        } else {
            this.ReportItemsSummary.clear();
        }
        return this.ReportItemsSummary;
    }

    public void reportItemDetail(String aPurpose, String aItemType, int aCategoryId, int aSubCategoryId, String aCurrency, String aIsSuspended, int aIsGeneral, int aIsTaxMapped) {
        String sql = "SELECT * FROM view_item_detail WHERE 1=1";
        String wheresql = "";
        String ordersql = "";
        ResultSet rs = null;
        this.setItemsList(new ArrayList<>());
        this.setItemsSummary(new ArrayList<>());
        AccCoaBean acb = new AccCoaBean();
        if (aPurpose.length() > 0) {
            wheresql = wheresql + " AND purpose='" + aPurpose + "'";
        }
        if (aItemType.length() > 0) {
            wheresql = wheresql + " AND item_type='" + aItemType + "'";
        }
        if (aCategoryId > 0) {
            wheresql = wheresql + " AND category_id=" + aCategoryId;
        }
        if (aSubCategoryId > 0) {
            wheresql = wheresql + " AND sub_category_id=" + aSubCategoryId;
        }
        if (aCurrency.length() > 0) {
            wheresql = wheresql + " AND currency_code='" + aCurrency + "'";
        }
        if (aIsSuspended.length() > 0) {
            wheresql = wheresql + " AND is_suspended='" + aIsSuspended + "'";
        }
        if (aIsGeneral == 10) {
            wheresql = wheresql + " AND is_general=0";
        }
        if (aIsGeneral == 11) {
            wheresql = wheresql + " AND is_general=1";
        }
        if (aIsTaxMapped == 10 || aIsTaxMapped == 11) {
            if (aIsTaxMapped == 10) {
                wheresql = wheresql + " AND is_synced=0";
            }
            if (aIsTaxMapped == 11) {
                wheresql = wheresql + " AND is_synced=1";
            }
        }
        ordersql = " ORDER BY description ASC";
        sql = sql + wheresql + ordersql;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Item item = null;
            while (rs.next()) {
                item = new Item();
                this.setItemFromResultsetReport(item, rs);
                try {
                    item.setPurpose(rs.getString("purpose"));
                } catch (Exception e) {
                    item.setPurpose("");
                }
                try {
                    item.setItem_code_tax(rs.getString("item_code_tax"));
                } catch (Exception e) {
                    item.setItem_code_tax("");
                }
                try {
                    item.setIs_synced_tax(rs.getInt("is_synced"));
                } catch (Exception e) {
                    item.setIs_synced_tax(0);
                }
                //merge asset details with non asset
                if (item.getIsAsset() == 1) {
                    item.setExpense_type(item.getAssetType());
                    item.setExpenseAccountCode(item.getAssetAccountCode());
                }
                //retrieve account details
                String accname = "";
                try {
                    accname = acb.getAccCoaByCodeOrId(item.getExpenseAccountCode(), 0).getAccountName();
                } catch (Exception e) {
                    //do nothing
                }
                item.setAccount_name(accname);
                this.getItemsList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportItemDetailStock(String aItemType, int aCategoryId, int aSubCategoryId, String aCurrency, String aIsSuspended, int aIsGeneral) {
        String sql = "SELECT * FROM view_item_detail_stock WHERE 1=1";
        String wheresql = "";
        String ordersql = "";
        ResultSet rs = null;
        this.setItemsList(new ArrayList<>());
        this.setItemsSummary(new ArrayList<>());
        if (aItemType.length() > 0) {
            wheresql = wheresql + " AND item_type='" + aItemType + "'";
        }
        if (aCategoryId > 0) {
            wheresql = wheresql + " AND category_id=" + aCategoryId;
        }
        if (aSubCategoryId > 0) {
            wheresql = wheresql + " AND sub_category_id=" + aSubCategoryId;
        }
        if (aCurrency.length() > 0) {
            wheresql = wheresql + " AND currency_code='" + aCurrency + "'";
        }
        if (aIsSuspended.length() > 0) {
            wheresql = wheresql + " AND is_suspended='" + aIsSuspended + "'";
        }
        if (aIsGeneral == 10) {
            wheresql = wheresql + " AND is_general=0";
        }
        if (aIsGeneral == 11) {
            wheresql = wheresql + " AND is_general=1";
        }
        ordersql = " ORDER BY description ASC";
        sql = sql + wheresql + ordersql;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Item item = null;
            while (rs.next()) {
                item = new Item();
                this.setItemFromResultsetReport(item, rs);
                this.getItemsList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportItemDetailAsset(String aItemType, int aCategoryId, int aSubCategoryId, String aCurrency, String aIsSuspended, int aIsGeneral) {
        String sql = "SELECT * FROM view_item_detail_asset WHERE 1=1";
        String wheresql = "";
        String ordersql = "";
        ResultSet rs = null;
        this.setItemsList(new ArrayList<>());
        this.setItemsSummary(new ArrayList<>());
        if (aItemType.length() > 0) {
            wheresql = wheresql + " AND item_type='" + aItemType + "'";
        }
        if (aCategoryId > 0) {
            wheresql = wheresql + " AND category_id=" + aCategoryId;
        }
        if (aSubCategoryId > 0) {
            wheresql = wheresql + " AND sub_category_id=" + aSubCategoryId;
        }
        if (aCurrency.length() > 0) {
            wheresql = wheresql + " AND currency_code='" + aCurrency + "'";
        }
        if (aIsSuspended.length() > 0) {
            wheresql = wheresql + " AND is_suspended='" + aIsSuspended + "'";
        }
        if (aIsGeneral == 10) {
            wheresql = wheresql + " AND is_general=0";
        }
        if (aIsGeneral == 11) {
            wheresql = wheresql + " AND is_general=1";
        }
        ordersql = " ORDER BY description ASC";
        sql = sql + wheresql + ordersql;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Item item = null;
            while (rs.next()) {
                item = new Item();
                this.setItemFromResultsetReport(item, rs);
                this.getItemsList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportItemDetailExpense(String aItemType, int aCategoryId, int aSubCategoryId, String aCurrency, String aIsSuspended, int aIsGeneral) {
        String sql = "SELECT * FROM view_item_detail_expense WHERE 1=1";
        String wheresql = "";
        String ordersql = "";
        ResultSet rs = null;
        this.setItemsList(new ArrayList<>());
        this.setItemsSummary(new ArrayList<>());
        if (aItemType.length() > 0) {
            wheresql = wheresql + " AND item_type='" + aItemType + "'";
        }
        if (aCategoryId > 0) {
            wheresql = wheresql + " AND category_id=" + aCategoryId;
        }
        if (aSubCategoryId > 0) {
            wheresql = wheresql + " AND sub_category_id=" + aSubCategoryId;
        }
        if (aCurrency.length() > 0) {
            wheresql = wheresql + " AND currency_code='" + aCurrency + "'";
        }
        if (aIsSuspended.length() > 0) {
            wheresql = wheresql + " AND is_suspended='" + aIsSuspended + "'";
        }
        if (aIsGeneral == 10) {
            wheresql = wheresql + " AND is_general=0";
        }
        if (aIsGeneral == 11) {
            wheresql = wheresql + " AND is_general=1";
        }
        ordersql = " ORDER BY description ASC";
        sql = sql + wheresql + ordersql;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Item item = null;
            while (rs.next()) {
                item = new Item();
                this.setItemFromResultsetReport(item, rs);
                this.getItemsList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportItemStockLowOut(String aItemType, int aCategoryId, int aSubCategoryId, String aCurrency, int aIsGeneral, String aStockType, String aStockStatus, int aStoreId) {
        String ViewName = "";
        if (aStoreId > 0) {
            ViewName = "view_inventory_low_out_per_store_vw";
        } else {
            ViewName = "view_inventory_low_out_vw";
        }
        String sql = "SELECT * FROM " + ViewName + " WHERE is_suspended='No'";
        String sqlsum = "SELECT stock_status,count(*) as qty_total FROM " + ViewName + " WHERE is_suspended='No'";
        String wheresql = "";
        String ordersql = " ORDER BY stock_type_order,stock_type,description ASC";
        String ordersqlsum = " ORDER BY qty_total DESC";
        String groupbysum = " GROUP BY stock_status";
        ResultSet rs = null;
        ResultSet rs2 = null;
        this.setItemsList(new ArrayList<>());
        this.setItemsSummary(new ArrayList<>());
        if (aStockType.length() > 0) {
            wheresql = wheresql + " AND stock_type='" + aStockType + "'";
        }
        if (aItemType.length() > 0) {
            wheresql = wheresql + " AND item_type='" + aItemType + "'";
        }
        if (aCategoryId > 0) {
            wheresql = wheresql + " AND category_id=" + aCategoryId;
        }
        if (aSubCategoryId > 0) {
            wheresql = wheresql + " AND sub_category_id=" + aSubCategoryId;
        }
        if (aCurrency.length() > 0) {
            wheresql = wheresql + " AND currency_code='" + aCurrency + "'";
        }
        if (aIsGeneral == 10) {
            wheresql = wheresql + " AND is_general=0";
        }
        if (aIsGeneral == 11) {
            wheresql = wheresql + " AND is_general=1";
        }
        if (aStockStatus.length() > 0) {
            wheresql = wheresql + " AND stock_status='" + aStockStatus + "'";
        }
        if (aStoreId > 0) {
            wheresql = wheresql + " AND store_id_ro=" + aStoreId;
        }
        sql = sql + wheresql + ordersql;
        sqlsum = sqlsum + wheresql + groupbysum + ordersqlsum;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Item item = null;
            while (rs.next()) {
                item = new Item();
                this.setItemFromResultsetReport(item, rs);
                if (aStoreId > 0) {
                    try {
                        item.setReorderLevel(rs.getDouble("reorder_level_ro"));
                    } catch (Exception e) {
                        item.setReorderLevel(0);
                    }
                }
                this.getItemsList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }

        //summary
        double totalitems = this.getItemsList().size();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps2 = conn.prepareStatement(sqlsum);) {
            rs2 = ps2.executeQuery();
            Item item2 = null;
            while (rs2.next()) {
                item2 = new Item();
                try {
                    item2.setStock_status(rs2.getString("stock_status"));
                } catch (NullPointerException npe) {
                    item2.setStock_status("");
                }
                try {
                    item2.setQty_total(rs2.getDouble("qty_total"));
                } catch (NullPointerException npe) {
                    item2.setQty_total(0);
                }
                if (totalitems > 0) {
                    item2.setStock_status_perc(100.0 * item2.getQty_total() / totalitems);
                }
                this.getItemsSummary().add(item2);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportItemStockLowOut_old(String aItemType, int aCategoryId, int aSubCategoryId, String aCurrency, int aIsGeneral, String aStockType, String aStockStatus) {
        String sql = "SELECT * FROM view_inventory_low_out_vw WHERE is_suspended='No'";
        String sqlsum = "SELECT stock_status,count(*) as qty_total FROM view_inventory_low_out_vw WHERE is_suspended='No'";
        String wheresql = "";
        String ordersql = " ORDER BY stock_type_order,stock_type,description ASC";
        String ordersqlsum = " ORDER BY qty_total DESC";
        String groupbysum = " GROUP BY stock_status";
        ResultSet rs = null;
        ResultSet rs2 = null;
        this.setItemsList(new ArrayList<>());
        this.setItemsSummary(new ArrayList<>());
        if (aStockType.length() > 0) {
            wheresql = wheresql + " AND stock_type='" + aStockType + "'";
        }
        if (aItemType.length() > 0) {
            wheresql = wheresql + " AND item_type='" + aItemType + "'";
        }
        if (aCategoryId > 0) {
            wheresql = wheresql + " AND category_id=" + aCategoryId;
        }
        if (aSubCategoryId > 0) {
            wheresql = wheresql + " AND sub_category_id=" + aSubCategoryId;
        }
        if (aCurrency.length() > 0) {
            wheresql = wheresql + " AND currency_code='" + aCurrency + "'";
        }
        if (aIsGeneral == 10) {
            wheresql = wheresql + " AND is_general=0";
        }
        if (aIsGeneral == 11) {
            wheresql = wheresql + " AND is_general=1";
        }
        if (aStockStatus.length() > 0) {
            wheresql = wheresql + " AND stock_status='" + aStockStatus + "'";
        }
        sql = sql + wheresql + ordersql;
        sqlsum = sqlsum + wheresql + groupbysum + ordersqlsum;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Item item = null;
            while (rs.next()) {
                item = new Item();
                this.setItemFromResultsetReport(item, rs);
                this.getItemsList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }

        //summary
        double totalitems = this.getItemsList().size();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps2 = conn.prepareStatement(sqlsum);) {
            rs2 = ps2.executeQuery();
            Item item2 = null;
            while (rs2.next()) {
                item2 = new Item();
                try {
                    item2.setStock_status(rs2.getString("stock_status"));
                } catch (NullPointerException npe) {
                    item2.setStock_status("");
                }
                try {
                    item2.setQty_total(rs2.getDouble("qty_total"));
                } catch (NullPointerException npe) {
                    item2.setQty_total(0);
                }
                if (totalitems > 0) {
                    item2.setStock_status_perc(100.0 * item2.getQty_total() / totalitems);
                }
                this.getItemsSummary().add(item2);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public Item getItemCurrentStockStatus(long aItem_id) {
        String sql = "SELECT * FROM view_inventory_low_out_vw WHERE item_id=" + aItem_id;
        ResultSet rs = null;
        Item item = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            if (rs.next()) {
                item = new Item();
                this.setItemFromResultsetReport(item, rs);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return item;
    }

    public void initItemObj() {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            if (null == this.ItemObj) {
                this.ItemObj = new Item();
            }
            if (null == this.ParentItem) {
            } else {
                this.displayItem(this.ParentItem);
            }
        }
    }

    public void initItem_tax_mapObj() {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            if (null == this.Item_tax_mapObj) {
                this.Item_tax_mapObj = new Item_tax_map();
            }
        }
    }

    public void initItemTaxObj() {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            if (null == this.ItemTaxObj) {
                this.ItemTaxObj = new ItemTax();
            }
        }
    }

    public void initItem_code_otherObj() {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            if (null == this.Item_code_otherObj) {
                this.Item_code_otherObj = new Item_code_other();
            }
        }
    }

    public void initItem_unit_otherObj() {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            if (null == this.Item_unit_otherObj) {
                this.Item_unit_otherObj = new Item_unit_other();
            }
        }
    }

    public void initItem_store_reorderObj() {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            if (null == this.Item_store_reorderObj) {
                this.Item_store_reorderObj = new Item_store_reorder();
            }
        }
    }

    public void initStockLocation() {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            long ItemId = 0;
            try {
                ItemId = this.ItemObj.getItemId();
            } catch (NullPointerException npe) {
            }
            if (ItemId > 0) {
                this.refreshStockLocation(ItemId);
            }
        }
    }

    public void refreshStockLocation(long aItemId) {
        try {
            this.LocationList.clear();
        } catch (NullPointerException npe) {
            this.LocationList = new ArrayList<>();
        }

        try {
            this.StockList.clear();
        } catch (NullPointerException npe) {
            this.StockList = new ArrayList<>();
        }

        if (aItemId > 0) {
            try {
                this.LocationList = new LocationBean().getLocationsByItem(aItemId);
            } catch (NullPointerException npe) {
            }
            try {
                this.StockList = new StockBean().getStocksByItem(aItemId);
            } catch (NullPointerException npe) {
            }
        }
    }

    public long getReportItemsSummaryCount() {
        return this.ReportItemsSummary.size();
    }

    public void openChildItem(String aItemPurpose) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("ITEM_PURPOSE", aItemPurpose);

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", false);
        options.put("resizable", false);
        options.put("width", 600);
        options.put("height", 300);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        options.put("scrollable", true);
        options.put("maximizable", true);
        options.put("dynamic", true);
        org.primefaces.PrimeFaces.current().dialog().openDynamic("ItemChild", options, null);
    }

    public void openChildItem(String aItemPurpose, long aItemId) {
        if (new NavigationBean().checkAccessDeniedReturn("8", "View") == 1) {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
            HttpSession httpSession = request.getSession(false);
            httpSession.setAttribute("ITEM_PURPOSE", aItemPurpose);
            try {
                if (aItemId > 0) {
                    this.ParentItem = this.getItem(aItemId);
                }// else {
                //    this.ParentItem = new Item();
                //}
            } catch (NullPointerException npe) {
            }
            Map<String, Object> options = new HashMap<String, Object>();
            options.put("modal", true);
            options.put("draggable", false);
            options.put("resizable", false);
            options.put("width", 600);
            options.put("height", 300);
            options.put("contentWidth", "100%");
            options.put("contentHeight", "100%");
            options.put("scrollable", true);
            options.put("maximizable", true);
            options.put("dynamic", true);
            org.primefaces.PrimeFaces.current().dialog().openDynamic("ItemChild", options, null);
        }
    }

    public List<Item> getProductionItemObjectList_old(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_production(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<Item>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getProductionItemObjectList(String Query) {
        this.setTypedItemCode(Query);
        String sql, sqlDesc = "", sqlCode = "", sqlCodeOther = "";
        Item_code_other ico = this.getItem_code_otherByCode(Query);
        //desc
        String[] ArrayDesc = new UtilityBean().getStringArrayFromXSeperatedStr(Query, " ");
        if (menuItemBean.getMenuItemObj().getITEM_FULL_SEARCH_ON() == 1 && ArrayDesc.length > 1) {
            for (String ArrayDesc1 : ArrayDesc) {
                if (sqlDesc.length() == 0) {
                    sqlDesc = " i.description LIKE '%" + ArrayDesc1 + "%' ";
                } else {
                    sqlDesc = sqlDesc + " AND i.description LIKE '%" + ArrayDesc1 + "%' ";
                }
            }
        } else {
            sqlDesc = " i.description LIKE '%" + Query + "%' ";
        }
        //code
        if (menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON() == 1) {
            //sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            if (Query.length() > 1) {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            } else {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' ";
            }
        } else {
            sqlCode = " i.item_code='" + Query + "' ";
        }
        //code other
        if (null != ico) {
            sqlCodeOther = " OR (i.item_id=" + ico.getItem_id() + ") ";
        } else {
            sqlCodeOther = "";
        }
        sql = "SELECT * FROM item i WHERE i.is_suspended='No' AND i.is_track=1 AND i.is_asset=0 AND ("
                + "(" + sqlDesc + ") "
                + "OR "
                + "(" + sqlCode + ") "
                + "OR "
                + "(i.alias_name LIKE '%" + Query + "%') "
                + sqlCodeOther
                + ") ORDER BY i.description ASC LIMIT " + menuItemBean.getMenuItemObj().getSEARCH_ITEMS_LIST_LIMIT();
        //System.out.println("SQL:" + sql);
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                //this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForRawMaterial_old(String Query) {
        this.setTypedItemCode(Query);
        String sql;
        sql = "{call sp_search_item_for_raw_material(?)}";
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<Item>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public List<Item> getItemObjectListForRawMaterial(String Query) {
        this.setTypedItemCode(Query);
        String sql, sqlDesc = "", sqlCode = "", sqlCodeOther = "";
        Item_code_other ico = this.getItem_code_otherByCode(Query);
        //desc
        String[] ArrayDesc = new UtilityBean().getStringArrayFromXSeperatedStr(Query, " ");
        if (menuItemBean.getMenuItemObj().getITEM_FULL_SEARCH_ON() == 1 && ArrayDesc.length > 1) {
            for (String ArrayDesc1 : ArrayDesc) {
                if (sqlDesc.length() == 0) {
                    sqlDesc = " i.description LIKE '%" + ArrayDesc1 + "%' ";
                } else {
                    sqlDesc = sqlDesc + " AND i.description LIKE '%" + ArrayDesc1 + "%' ";
                }
            }
        } else {
            sqlDesc = " i.description LIKE '%" + Query + "%' ";
        }
        //code
        if (menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON() == 1) {
            //sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            if (Query.length() > 1) {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            } else {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' ";
            }
        } else {
            sqlCode = " i.item_code='" + Query + "' ";
        }
        //code other
        if (null != ico) {
            sqlCodeOther = " OR (i.item_id=" + ico.getItem_id() + ") ";
        } else {
            sqlCodeOther = "";
        }
        sql = "SELECT * FROM item i WHERE i.is_suspended='No' AND i.is_track=1 AND i.is_asset=0 AND ("
                + "(" + sqlDesc + ") "
                + "OR "
                + "(" + sqlCode + ") "
                + "OR "
                + "(i.alias_name LIKE '%" + Query + "%') "
                + sqlCodeOther
                + ") ORDER BY i.description ASC LIMIT " + menuItemBean.getMenuItemObj().getSEARCH_ITEMS_LIST_LIMIT();
        //System.out.println("SQL:" + sql);
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                //this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return getItemObjectList();
    }

    public void refreshItem_unspscList(String Query) {
        String sql;
        if (Query.length() == 0) {
            sql = "SELECT * FROM item_unspsc ORDER BY class_name,commodity_name";
        } else {
            sql = "SELECT * FROM item_unspsc WHERE commodity_code='" + Query + "' OR commodity_name LIKE '%" + Query + "%' OR class_name LIKE '%" + Query + "%' ORDER BY class_name,commodity_name";
        }
        ResultSet rs = null;
        this.Item_unspscList = new ArrayList<>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                Item_unspsc itemun = new Item_unspsc();
                itemun.setSegment_name(rs.getString("segment_name"));
                itemun.setFamily_name(rs.getString("family_name"));
                itemun.setClass_name(rs.getString("class_name"));
                itemun.setCommodity_name(rs.getString("commodity_name"));
                itemun.setCommodity_code(rs.getString("commodity_code"));
                itemun.setExcise_duty_product_type(rs.getString("excise_duty_product_type"));
                itemun.setVat_rate(rs.getString("vat_rate"));
                itemun.setZero_rate(rs.getString("zero_rate"));
                itemun.setExempt_rate(rs.getString("exempt_rate"));
                itemun.setService_mark(rs.getString("service_mark"));
                this.Item_unspscList.add(itemun);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void refreshExciseList(String Query) {
        String sql;
        if (Query.length() == 0) {
            sql = "select lv1.*,lv2.goodService as parentName from efris_excise_duty_list lv1 inner join efris_excise_duty_list lv2 on lv1.parentCode=lv2.exciseDutyCode "
                    + "where lv1.isLeafNode=1 "
                    + "order by parentName,goodService";
        } else {
            sql = "select lv1.*,lv2.goodService as parentName from efris_excise_duty_list lv1 inner join efris_excise_duty_list lv2 on lv1.parentCode=lv2.exciseDutyCode "
                    + "where lv1.isLeafNode=1 and (lv1.exciseDutyCode='" + Query + "' or lv1.goodService LIKE '%" + Query + "%' or lv2.goodService LIKE '%" + Query + "%') "
                    + "order by parentName,goodService";
        }
        ResultSet rs = null;
        this.ExciseList = new ArrayList<>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                EFRIS_excise_duty_list obj = new EFRIS_excise_duty_list();
                new EFRIS_excise_duty_listBean().setEFRIS_excise_duty_listFromResultset(obj, rs);
                try {
                    obj.setParentName(rs.getString("parentName"));
                } catch (Exception e) {
                    obj.setParentName("");
                }
                this.ExciseList.add(obj);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void refreshItemSearchList(String Query) {
        this.setTypedItemCode(Query);
        String sql, sqlDesc = "", sqlCode = "", sqlCodeOther = "";
        Item_code_other ico = this.getItem_code_otherByCode(Query);
        //desc
        String[] ArrayDesc = new UtilityBean().getStringArrayFromXSeperatedStr(Query, " ");
        if (menuItemBean.getMenuItemObj().getITEM_FULL_SEARCH_ON() == 1 && ArrayDesc.length > 1) {
            for (String ArrayDesc1 : ArrayDesc) {
                if (sqlDesc.length() == 0) {
                    sqlDesc = " i.description LIKE '%" + ArrayDesc1 + "%' ";
                } else {
                    sqlDesc = sqlDesc + " AND i.description LIKE '%" + ArrayDesc1 + "%' ";
                }
            }
        } else {
            sqlDesc = " i.description LIKE '%" + Query + "%' ";
        }
        //code
        if (menuItemBean.getMenuItemObj().getITEM_CODE_ERROR_ON() == 1) {
            //sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            if (Query.length() > 1) {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' OR i.item_code LIKE '%" + Query.substring(2) + "%' ";
            } else {
                sqlCode = " i.item_code LIKE '%" + Query + "%' OR i.item_code LIKE '%" + Query.substring(1) + "%' ";
            }
        } else {
            sqlCode = " i.item_code='" + Query + "' ";
        }
        //code other
        if (null != ico) {
            sqlCodeOther = " OR (i.item_id=" + ico.getItem_id() + ") ";
        } else {
            sqlCodeOther = "";
        }
        sql = "SELECT * FROM item i WHERE i.is_suspended='No' AND i.is_sale=1 AND i.is_asset=0 AND ("
                + "(" + sqlDesc + ") "
                + "OR "
                + "(" + sqlCode + ") "
                + "OR "
                + "(i.alias_name LIKE '%" + Query + "%') "
                + sqlCodeOther
                + ") ORDER BY i.description ASC LIMIT 500";// + menuItemBean.getMenuItemObj().getSEARCH_ITEMS_LIST_LIMIT()
        //System.out.println("SQL:" + sql);
        ResultSet rs = null;
        this.setItemObjectList(new ArrayList<>());
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setString(1, Query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                this.setItemFromResultset(item, rs);
                //this.updateLookUpsUI(item);
                this.getItemObjectList().add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void resetAtItemSearchList() {
        try {
            this.SearchItemDesc = "";
        } catch (Exception e) {
            //LOGGER.log(Level.ERROR, e);
        }
        try {
            this.SelectedItem = null;
        } catch (Exception e) {
            //LOGGER.log(Level.ERROR, e);
        }
        try {
            this.ItemObjectList.clear();
        } catch (Exception e) {
            //LOGGER.log(Level.ERROR, e);
        }
    }

    public void updateItemFromUNSPC(Item aItem, Item_unspsc aItem_unspsc) {
        String[] StringArray = null;
        String CommaSeperatedStr = "";
        String S = "", E = "", Z = "";
        int n = 0;
        try {
            if (null == aItem || null == aItem_unspsc) {
                //
            } else {
                aItem.setItem_code_tax(aItem_unspsc.getCommodity_code());
                //PRODUCT or SERVICE
                if (aItem_unspsc.getService_mark().equals("Y")) {
                    aItem.setItemType("SERVICE");
                } else {
                    aItem.setItemType("PRODUCT");
                }
                //VAT RATE
                aItem.setSelectedVatRateds(null);
                if (aItem_unspsc.getVat_rate().length() > 0 && Double.parseDouble(aItem_unspsc.getVat_rate()) > 0) {
                    S = "STANDARD";
                    if (CommaSeperatedStr.length() == 0) {
                        CommaSeperatedStr = S;
                    } else {
                        CommaSeperatedStr = CommaSeperatedStr + "," + S;
                    }
                }
                if (aItem_unspsc.getZero_rate().equals("Y")) {
                    Z = "ZERO";
                    if (CommaSeperatedStr.length() == 0) {
                        CommaSeperatedStr = Z;
                    } else {
                        CommaSeperatedStr = CommaSeperatedStr + "," + Z;
                    }
                }
                if (aItem_unspsc.getExempt_rate().equals("Y")) {
                    E = "EXEMPT";
                    if (CommaSeperatedStr.length() == 0) {
                        CommaSeperatedStr = E;
                    } else {
                        CommaSeperatedStr = CommaSeperatedStr + "," + E;
                    }
                }
                if (CommaSeperatedStr.length() > 0) {
                    StringArray = CommaSeperatedStr.split(",");
                    aItem.setSelectedVatRateds(StringArray);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void updateItemFromExcise(Item aItem, EFRIS_excise_duty_list aExciseObj) {
        try {
            if (null == aItem || null == aExciseObj) {
                //
            } else {
                aItem.setExcise_duty_code(aExciseObj.getExciseDutyCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void refreshItem_code_otherList(Item aItem) {
        String sql;
        sql = "SELECT * FROM item_code_other WHERE item_id=" + aItem.getItemId();
        ResultSet rs = null;
        Item_code_other ic = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            //make the parent item code come first but with an ID of -1
            if (aItem.getItemCode().length() > 0) {
                ic = new Item_code_other();
                ic.setItem_code_other_id(-1);
                ic.setItem_code(aItem.getItemCode());
                ic.setDescription(aItem.getDescription());
                this.Item_code_otherList.add(ic);
            }
            //load the others
            while (rs.next()) {
                ic = new Item_code_other();
                this.setItem_code_otherFromResultset(ic, rs);
                this.Item_code_otherList.add(ic);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void refreshItem_unit_otherList(Item aItem, int aIs_active) {
        String sql;
        sql = "SELECT * FROM item_unit_other WHERE item_id=" + aItem.getItemId() + " AND is_active=" + aIs_active;
        ResultSet rs = null;
        Item_unit_other iu = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                iu = new Item_unit_other();
                this.setItem_unit_otherFromResultset(iu, rs);
                this.updateLookupItem_unit_other(iu);
                this.Item_unit_otherList.add(iu);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public List<Item_unit_other> getItem_unit_otherList(long aItemId, int aIs_active) {
        String sql;
        sql = "SELECT * FROM item_unit_other WHERE item_id=" + aItemId + " AND is_active=" + aIs_active;
        ResultSet rs = null;
        List<Item_unit_other> iuList = new ArrayList<>();
        Item_unit_other iu = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                iu = new Item_unit_other();
                this.setItem_unit_otherFromResultset(iu, rs);
                iuList.add(iu);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return iuList;
    }

    public void updateLookupItem_unit_other(Item_unit_other aItem_unit_other) {
        try {
            if (null != aItem_unit_other) {
                try {
                    aItem_unit_other.setBase_unit_symbol(new UnitBean().getUnit(this.getItemObj().getUnitId()).getUnitSymbol());
                } catch (Exception e) {
                    aItem_unit_other.setBase_unit_symbol("");
                }
                try {
                    aItem_unit_other.setOther_unit_symbol(new UnitBean().getUnit(aItem_unit_other.getOther_unit_id()).getUnitSymbol());
                } catch (Exception e) {
                    aItem_unit_other.setOther_unit_symbol("");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void refreshItem_store_reorderList(Item aItem) {
        String sql;
        sql = "select "
                + "ifnull(r.item_store_reorder_id,0) as item_store_reorder_id,"
                + "ifnull(r.store_id,s.store_id) as store_id,ifnull(r.item_id," + aItem.getItemId() + ") as item_id,ifnull(r.reorder_level,0) as reorder_level "
                + "from store s left join item_store_reorder r on s.store_id=r.store_id and r.item_id=" + aItem.getItemId();
        ResultSet rs = null;
        Item_store_reorder ro = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                ro = new Item_store_reorder();
                this.setItem_store_reorderFromResultset(ro, rs);
                this.Item_store_reorderList.add(ro);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public int saveItem_code_other(Item_code_other aItem_code_other) {
        int saved = 0;
        String sql = "INSERT INTO item_code_other(item_id,item_code) VALUES(?,?)";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            if (null != aItem_code_other) {
                try {
                    ps.setLong(1, aItem_code_other.getItem_id());
                } catch (NullPointerException npe) {
                    ps.setLong(1, 0);
                }
                try {
                    ps.setString(2, aItem_code_other.getItem_code());
                } catch (NullPointerException npe) {
                    ps.setString(2, "");
                }
                ps.executeUpdate();
                saved = 1;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return saved;
    }

    public int saveItem_store_reorder(Item_store_reorder aItem_store_reorder) {
        int saved = 0;
        String sql = "";
        try {
            if (null != aItem_store_reorder) {
                if (aItem_store_reorder.getItem_store_reorder_id() == 0) {
                    sql = "INSERT INTO item_store_reorder(item_id,store_id,reorder_level) VALUES(?,?,?)";
                } else if (aItem_store_reorder.getItem_store_reorder_id() > 0) {
                    sql = "UPDATE item_store_reorder SET item_id=?,store_id=?,reorder_level=? WHERE item_store_reorder_id=" + aItem_store_reorder.getItem_store_reorder_id();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            if (null != aItem_store_reorder) {
                try {
                    ps.setLong(1, aItem_store_reorder.getItem_id());
                } catch (NullPointerException npe) {
                    ps.setLong(1, 0);
                }
                try {
                    ps.setInt(2, aItem_store_reorder.getStore_id());
                } catch (NullPointerException npe) {
                    ps.setInt(2, 0);
                }
                try {
                    ps.setDouble(3, aItem_store_reorder.getReorder_level());
                } catch (NullPointerException npe) {
                    ps.setDouble(3, 0);
                }
                ps.executeUpdate();
                saved = 1;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return saved;
    }

    public void saveItem_code_otherCall(Item_code_other aItem_code_other) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        try {
            String sql1 = "select count(*) as n from item where item_code='" + aItem_code_other.getItem_code() + "'";
            String sql2 = "select count(*) as n from item_code_other where item_code='" + aItem_code_other.getItem_code() + "'";
            long count1 = new UtilityBean().getN(sql1);
            long count2 = new UtilityBean().getN(sql2);
            long n = count1 + count2;
            if (n >= 1) {
                msg = "Code Exists";
            } else {
                int i = this.saveItem_code_other(aItem_code_other);
                if (i == 1) {
                    msg = "Added Successfully";
                    try {
                        aItem_code_other.setItem_code("");
                        this.Item_code_otherList.clear();
                    } catch (Exception e) {
                    }
                    this.refreshItem_code_otherList(this.getItem(aItem_code_other.getItem_id()));
                } else {
                    msg = "Saving Has Failed";
                }
            }
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void addItem_unit_other(Item_unit_other aItem_unit_other) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        Item_unit_other iuo = new Item_unit_other();
        Gson g = new Gson();
        String json = "";
        try {
            if (null != aItem_unit_other) {
                // from object to json
                json = g.toJson(aItem_unit_other);
                // from json to object
                iuo = g.fromJson(json, Item_unit_other.class);
            }
        } catch (Exception e) {

        }
        //check for other unit exists
        int OtherUnitExists = 0;
        try {
            String UnitCodeTax1 = new UnitBean().getUnit(iuo.getOther_unit_id()).getUnit_symbol_tax();
            if (null == UnitCodeTax1) {
                UnitCodeTax1 = "x";
            }
            for (int i = 0; i < this.Item_unit_otherList.size(); i++) {
                if (this.Item_unit_otherList.get(i).getIs_active() == 1) {
                    String UnitCodeTax2 = new UnitBean().getUnit(this.Item_unit_otherList.get(i).getOther_unit_id()).getUnit_symbol_tax();
                    if (null == UnitCodeTax2) {
                        UnitCodeTax2 = "xx";
                    }
                    if ((iuo.getOther_unit_id() == this.Item_unit_otherList.get(i).getOther_unit_id()) || (UnitCodeTax1.equals(UnitCodeTax2))) {
                        OtherUnitExists = 1;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            //do nothing
        }
        //check for other unit and base unit are same
        int BaseOtherSame = 0;
        try {
            String BaseUnitCodeTax = new UnitBean().getUnit(this.ItemObj.getUnitId()).getUnit_symbol_tax();
            if (null == BaseUnitCodeTax) {
                BaseUnitCodeTax = "x";
            }
            String OtherUnitCodeTax = new UnitBean().getUnit(iuo.getOther_unit_id()).getUnit_symbol_tax();
            if (null == OtherUnitCodeTax) {
                OtherUnitCodeTax = "xx";
            }
            if ((iuo.getOther_unit_id() == this.ItemObj.getUnitId()) || (BaseUnitCodeTax == OtherUnitCodeTax)) {
                BaseOtherSame = 1;
            }
        } catch (Exception e) {
            //do nothing
        }

        try {
            if (iuo.getOther_unit_id() == 0) {
                msg = "Select Other Unit";
            } else if (iuo.getOther_unit_retailsale_price() <= 0) {
                msg = "Specify Other Retail Price";
            } else if (iuo.getOther_qty() <= 0) {
                msg = "Specify Other Unit Quantity";
            } else if (iuo.getBase_qty() <= 0) {
                msg = "Specify Base Unit Quantity";
            } else if (OtherUnitExists == 1) {
                msg = "Other Unit Exists";
            } else if (BaseOtherSame == 1) {
                msg = "Base and Other Unit Cannot be the Same";
            } else {
                try {
                    iuo.setIs_active(1);
                    iuo.setOther_unit_symbol(new UnitBean().getUnit(iuo.getOther_unit_id()).getUnitSymbol());
                    this.Item_unit_otherList.add(iuo);
                    this.setItemOtherUnitsEdited(1);
                    msg = "Added to the List";
                } catch (Exception e) {
                }
            }
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public int insertOrUpdateItem_unit_other(Item_unit_other aItem_unit_other) {
        int saved = 0;
        String sql = "";
        try {
            if (null != aItem_unit_other) {
                aItem_unit_other.setLast_edit_date(new CompanySetting().getCURRENT_SERVER_DATE());
                aItem_unit_other.setLast_edit_by(new GeneralUserSetting().getCurrentUser().getUserName());
                if (aItem_unit_other.getItem_unit_other_id() == 0) {
                    sql = "INSERT INTO item_unit_other(item_id,base_qty,other_unit_id,other_qty,other_unit_retailsale_price,"
                            + "other_unit_wholesale_price,other_default_purchase, other_default_sale,is_active,last_edit_by,last_edit_date) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
                } else if (aItem_unit_other.getItem_unit_other_id() > 0) {
                    sql = "UPDATE item_unit_other SET item_id=?,base_qty=?,other_unit_id=?,other_qty=?,other_unit_retailsale_price=?,"
                            + "other_unit_wholesale_price=?,other_default_purchase=?,other_default_sale=?,is_active=?,last_edit_by=?,last_edit_date=? "
                            + "WHERE item_unit_other_id=?";
                }
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql);) {
                    ps.setLong(1, aItem_unit_other.getItem_id());
                    ps.setDouble(2, aItem_unit_other.getBase_qty());
                    ps.setInt(3, aItem_unit_other.getOther_unit_id());
                    ps.setDouble(4, aItem_unit_other.getOther_qty());
                    ps.setDouble(5, aItem_unit_other.getOther_unit_retailsale_price());
                    ps.setDouble(6, aItem_unit_other.getOther_unit_wholesale_price());
                    ps.setInt(7, aItem_unit_other.getOther_default_purchase());
                    ps.setInt(8, aItem_unit_other.getOther_default_sale());
                    ps.setInt(9, aItem_unit_other.getIs_active());
                    ps.setString(10, aItem_unit_other.getLast_edit_by());
                    ps.setTimestamp(11, new java.sql.Timestamp(aItem_unit_other.getLast_edit_date().getTime()));
                    if (aItem_unit_other.getItem_unit_other_id() > 0) {
                        ps.setLong(12, aItem_unit_other.getItem_unit_other_id());
                    }
                    ps.executeUpdate();
                    saved = 1;
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return saved;
    }

    public int insertOrUpdateItem_excise_duty_map(Item_excise_duty_map aItem_excise_duty_map) {
        int saved = 0;
        String sql = "";
        try {
            if (null != aItem_excise_duty_map) {
                if (aItem_excise_duty_map.getItem_excise_duty_map_id() == 0) {
                    sql = "INSERT INTO item_excise_duty_map(item_id,excise_duty_code) VALUES (?,?)";
                } else if (aItem_excise_duty_map.getItem_excise_duty_map_id() > 0) {
                    sql = "UPDATE item_excise_duty_map SET item_id=?,excise_duty_code=? WHERE item_excise_duty_map_id=?";
                }
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql);) {
                    ps.setLong(1, aItem_excise_duty_map.getItem_id());
                    ps.setString(2, aItem_excise_duty_map.getExcise_duty_code());
                    if (aItem_excise_duty_map.getItem_excise_duty_map_id() > 0) {
                        ps.setLong(3, aItem_excise_duty_map.getItem_excise_duty_map_id());
                    }
                    ps.executeUpdate();
                    saved = 1;
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return saved;
    }

    public Item_excise_duty_map getItem_excise_duty_mapByItem(long aItemId) {
        String sql;
        sql = "SELECT * FROM item_excise_duty_map WHERE item_id=" + aItemId;
        ResultSet rs = null;
        Item_excise_duty_map obj = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            if (rs.next()) {
                obj = new Item_excise_duty_map();
                this.setItem_excise_duty_mapFromResultset(obj, rs);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return obj;
    }

    public int insertOrUpdateItem_unit_otherList() {
        int saved = 0;
        int n = 0;
        int x = 0;
        try {
            try {
                n = this.Item_unit_otherList.size();
            } catch (Exception e) {

            }
            if (n >= 1) {
                long ItemId = 0;
                if (this.ItemObj.getItemId() == 0 && this.ItemObj.getDescription().length() > 0) {
                    Item item = this.getItemByDesc(this.ItemObj.getDescription());
                    ItemId = item.getItemId();
                } else {
                    ItemId = this.ItemObj.getItemId();
                }
                for (int i = 0; i < n; i++) {
                    if (this.Item_unit_otherList.get(i).getItem_id() == 0) {
                        this.Item_unit_otherList.get(i).setItem_id(ItemId);
                    }
                    int s = this.insertOrUpdateItem_unit_other(this.Item_unit_otherList.get(i));
                    x = x + s;
                }
            }
            if (n == x) {
                saved = 1;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return saved;
    }

    public int saveItem_store_reorderCall(long aItem_id) {
        int saved = 0;
        try {
            //this.Item_store_reorderObj;
            int savedN = 0;
            for (int i = 0; i < this.Item_store_reorderList.size(); i++) {
                if (this.Item_store_reorderList.get(i).getItem_store_reorder_id() == 0) {
                    //Insert
                    this.Item_store_reorderList.get(i).setItem_id(aItem_id);
                    savedN = savedN + this.saveItem_store_reorder(this.Item_store_reorderList.get(i));
                } else if (this.Item_store_reorderList.get(i).getItem_store_reorder_id() > 0) {
                    //Update
                    savedN = savedN + this.saveItem_store_reorder(this.Item_store_reorderList.get(i));
                }
            }
            if (savedN == this.Item_store_reorderList.size()) {
                saved = 1;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return saved;
    }

    public void deleteItem_code_otherCall(Item_code_other aItem_code_other) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        String sql = "delete from item_code_other where item_code_other_id=" + aItem_code_other.getItem_code_other_id();

        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.executeUpdate();
            try {
                this.Item_code_otherList.clear();
            } catch (Exception e) {
            }
            this.refreshItem_code_otherList(this.getItem(aItem_code_other.getItem_id()));
            msg = "Deleted Successfully";
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void deleteItem_unit_otherCall(Item_unit_other aItem_unit_other) {
        try {
            if (null != aItem_unit_other) {
                if (aItem_unit_other.getItem_unit_other_id() == 0) {
                    this.Item_unit_otherList.remove(aItem_unit_other);
                } else {
                    aItem_unit_other.setIs_active(0);
                }
                this.setItemOtherUnitsEdited(1);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public Item_code_other getItem_code_otherByCode(String aItem_code) {
        String sql;
        int ItemCodeErrorOn = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("GENERAL", "ITEM_CODE_ERROR_ON").getParameter_value());
        if (ItemCodeErrorOn == 0) {
            sql = "SELECT * FROM item_code_other WHERE item_code='" + aItem_code + "'";
        } else {
            sql = "SELECT * FROM item_code_other WHERE (item_code='" + aItem_code + "' OR item_code=SUBSTRING('" + aItem_code + "',2) OR item_code=SUBSTRING('" + aItem_code + "',3))";
        }
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            if (rs.next()) {
                Item_code_other ic = new Item_code_other();
                this.setItem_code_otherFromResultset(ic, rs);
                return ic;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public void changeItemUnitSales(Item aItem, TransItem aTransItem, int aTransTypeId, int aTransReasId, int aStoreId, Trans aTrans) {
        try {
            Item_unit iu = this.getItemUnitFrmList(aTransItem.getUnit_id());
            aItem.setUnitRetailsalePrice(iu.getUnit_retailsale_price());
            aItem.setUnitWholesalePrice(iu.getUnit_wholesale_price());
            aTransItem.setUnit_id(iu.getUnit_id());
            aTransItem.setUnitSymbol(iu.getUnit_symbol());
            if (aTransTypeId == 2 && aTransReasId == 10) {
                aTransItem.setUnitPrice(aItem.getUnitWholesalePrice());
            } else {
                aTransItem.setUnitPrice(aItem.getUnitRetailsalePrice());
            }
            DiscountPackageItem dpi = new DiscountPackageItemBean().getActiveDiscountPackageItem(aStoreId, aItem.getItemId(), 1, aTrans.getTransactorId(), aItem.getCategoryId(), aItem.getSubCategoryId());
            if (dpi != null) {
                if (aTransReasId == 10 || aTransReasId == 15 || aTransReasId == 109) {
                    aTransItem.setUnitTradeDiscount(aItem.getUnitWholesalePrice() * dpi.getWholesaleDiscountAmt() / 100);
                } else {
                    aTransItem.setUnitTradeDiscount(aItem.getUnitRetailsalePrice() * dpi.getRetailsaleDiscountAmt() / 100);
                }
            }
            new TransItemBean().editTransItemUponUnitChange(aTransTypeId, aTransReasId, aTransItem);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void changeItemUnitSales(Item aItem, TransactionPackageItem transactionPackageItem, int aTransTypeId, int aTransReasId, int aStoreId, Trans aTrans) {
        try {
            Item_unit iu = this.getItemUnitFrmList(transactionPackageItem.getUnitId());
            aItem.setUnitRetailsalePrice(iu.getUnit_retailsale_price());
            aItem.setUnitWholesalePrice(iu.getUnit_wholesale_price());
            transactionPackageItem.setUnitId(iu.getUnit_id());
            transactionPackageItem.setUnitSymbol(iu.getUnit_symbol());
            if (aTransTypeId == 2 && aTransReasId == 10) {
                transactionPackageItem.setUnitPrice(aItem.getUnitWholesalePrice());
            } else {
                transactionPackageItem.setUnitPrice(aItem.getUnitRetailsalePrice());
            }
            DiscountPackageItem dpi = new DiscountPackageItemBean().getActiveDiscountPackageItem(aStoreId, aItem.getItemId(), 1, aTrans.getTransactorId(), aItem.getCategoryId(), aItem.getSubCategoryId());
            if (dpi != null) {
                if (aTransReasId == 10 || aTransReasId == 15 || aTransReasId == 109) {
                    transactionPackageItem.setUnitTradeDiscount(aItem.getUnitWholesalePrice() * dpi.getWholesaleDiscountAmt() / 100);
                } else {
                    transactionPackageItem.setUnitTradeDiscount(aItem.getUnitRetailsalePrice() * dpi.getRetailsaleDiscountAmt() / 100);
                }
            }
            new TransactionPackageItemBean().editTransItemUponUnitChange(aTransTypeId, aTransReasId, transactionPackageItem);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void changeItemUnitQuickOrder(TransItem aTransItem, int aTransTypeId, int aTransReasId, int aStoreId, Trans aTrans) {
        try {
            //Item_unit iu = this.getItemUnitFrmList(aTransItem.getUnit_id());
            Item_unit iu = this.getItemUnitFrmDb(aTransItem.getItemId(), aTransItem.getUnit_id());
            aTransItem.setUnit_id(iu.getUnit_id());
            aTransItem.setUnitSymbol(iu.getUnit_symbol());
            if (aTransTypeId == 2 && aTransReasId == 10) {
                aTransItem.setUnitPrice(iu.getUnit_wholesale_price());
            } else {
                aTransItem.setUnitPrice(iu.getUnit_retailsale_price());
            }
            Item itm = null;
            try {
                itm = new ItemBean().getItem(aTransItem.getItemId());
            } catch (Exception e) {
            }
            DiscountPackageItem dpi = null;
            if (null != itm) {
                dpi = new DiscountPackageItemBean().getActiveDiscountPackageItem(aStoreId, itm.getItemId(), 1, aTrans.getTransactorId(), itm.getCategoryId(), itm.getSubCategoryId());
            }
            if (dpi != null) {
                if (aTransReasId == 10 || aTransReasId == 15 || aTransReasId == 109) {
                    aTransItem.setUnitTradeDiscount(iu.getUnit_wholesale_price() * dpi.getWholesaleDiscountAmt() / 100);
                } else {
                    aTransItem.setUnitTradeDiscount(iu.getUnit_retailsale_price() * dpi.getRetailsaleDiscountAmt() / 100);
                }
            }
            double BaseQty = this.getBaseUnitQty(aTransItem.getItemId(), aTransItem.getUnit_id(), aTransItem.getItemQty());
            if (BaseQty > 0) {
                aTransItem.setBase_unit_qty(BaseQty);
            } else {
                if (null != itm) {
                    aTransItem.setUnit_id(itm.getUnitId());
                    aTransItem.setBase_unit_qty(aTransItem.getItemQty());
                }
            }
            new TransItemBean().editTransItemUponUnitChange(aTransTypeId, aTransReasId, aTransItem);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void changeItemUnit(Item aItem, TransItem aTransItem, int aTransTypeId, int aTransReasId, int aStoreId, Trans aTrans) {
        try {
            TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
            Item_unit iu = this.getItemUnitFrmList(aTransItem.getUnit_id());
            aItem.setUnitRetailsalePrice(iu.getUnit_retailsale_price());
            aItem.setUnitWholesalePrice(iu.getUnit_wholesale_price());
            aTransItem.setUnit_id(iu.getUnit_id());
            aTransItem.setUnitSymbol(iu.getUnit_symbol());
            //apply recent unit cost
            if (transtype.getTransactionTypeName().equals("ITEM RECEIVED") || transtype.getTransactionTypeName().equals("STOCK ADJUSTMENT") || transtype.getTransactionTypeName().equals("DISPOSE STOCK") || transtype.getTransactionTypeName().equals("PURCHASE INVOICE") || transtype.getTransactionTypeName().equals("STOCK CONSUMPTION")) {
                String CurCode = aTrans.getCurrencyCode();
                if (null == CurCode || CurCode.isEmpty()) {
                    CurCode = aItem.getCurrencyCode();
                }
                aTransItem.setUnitCostPrice(new TransItemBean().getItemLatestUnitCostPrice(aItem.getItemId(), "", "", "", aTransItem.getUnit_id(), CurCode, 1));
                if (transtype.getTransactionTypeName().equals("PURCHASE INVOICE")) {
                    aTransItem.setUnitPrice(aTransItem.getUnitCostPrice());
                    aTransItem.setAmount(aTransItem.getItemQty() * (aTransItem.getUnitPrice() + aTransItem.getUnitVat() - aTransItem.getUnitTradeDiscount()));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public List<Item_unit> getItemUnitList(long aItemId, TransItem aTransItem, int aOrderByFlag) {
        //aOrderByFlag 1 Sales, 2 Purchase, 0 None
        List<Item_unit> iuList = new ArrayList<>();
        try {
            if (aItemId > 0) {
                Item itm = this.getItem(aItemId);
                this.setItemUnitList(iuList, itm, aOrderByFlag);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return iuList;
    }

    public void refreshItemUnitList(Item aItem, TransItem aTransItem, int aOrderByFlag) {
        //aOrderByFlag 1 Sales, 2 Purchase, 0 None
        try {
            this.setItemUnitList(this.Item_unitList, aItem, aOrderByFlag);
            if (null != this.Item_unitList && this.Item_unitList.size() > 0) {
                Item_unit iu = this.Item_unitList.get(0);
                aItem.setUnitRetailsalePrice(iu.getUnit_retailsale_price());
                aItem.setUnitWholesalePrice(iu.getUnit_wholesale_price());
                aTransItem.setUnit_id(iu.getUnit_id());
                aTransItem.setUnitSymbol(iu.getUnit_symbol());
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void refreshItemtransactionPackageUnitList(Item aItem, TransactionPackageItem transactionPackageItem, int aOrderByFlag) {
        //aOrderByFlag 1 Sales, 2 Purchase, 0 None
        try {
            this.setItemUnitList(this.Item_unitList, aItem, aOrderByFlag);
            if (null != this.Item_unitList && this.Item_unitList.size() > 0) {
                Item_unit iu = this.Item_unitList.get(0);
                aItem.setUnitRetailsalePrice(iu.getUnit_retailsale_price());
                aItem.setUnitWholesalePrice(iu.getUnit_wholesale_price());
                transactionPackageItem.setUnitId(iu.getUnit_id());
                transactionPackageItem.setUnitSymbol(iu.getUnit_symbol());
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void setItemUnitList(List<Item_unit> aItem_unitList, Item aItem, int aOrderByFlag) {
        //aOrderByFlag 1 Sales, 2 Purchase, 0 None
        String OrderBy;
        if (aOrderByFlag == 1) {
            OrderBy = "default_sale desc,is_base desc";
        } else if (aOrderByFlag == 2) {
            OrderBy = "default_purchase desc,is_base desc";
        } else {
            OrderBy = "is_base desc";
        }
        String sql;
        sql = "select un.* from "
                + "("
                + "select i.unit_id,u.unit_symbol,u.unit_name,1 as is_base,0 as default_sale,0 as default_purchase,"
                + "1 as base_qty,1 as other_qty,i.unit_retailsale_price,i.unit_wholesale_price "
                + "from item i inner join unit u on i.unit_id=u.unit_id where i.item_id=? "
                + "UNION "
                + "select u.unit_id,u.unit_symbol,u.unit_name,0 as is_base,iu.other_default_sale as default_sale,iu.other_default_purchase as default_purchase,"
                + "iu.base_qty,iu.other_qty,iu.other_unit_retailsale_price as unit_retailsale_price,iu.other_unit_wholesale_price as unit_wholesale_price "
                + "from item_unit_other iu inner join unit u on iu.other_unit_id=u.unit_id where iu.is_active=1 and iu.item_id=? "
                + ") as un order by " + OrderBy;
        ResultSet rs = null;
        try {
            aItem_unitList.clear();
        } catch (Exception e) {
            aItem_unitList = new ArrayList<>();
        }
        if (null == aItem) {
            //do nothing
        } else {
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setLong(1, aItem.getItemId());
                ps.setLong(2, aItem.getItemId());
                rs = ps.executeQuery();
                while (rs.next()) {
                    Item_unit iu = new Item_unit();
                    this.setItem_unitFromResultset(iu, rs);
                    aItem_unitList.add(iu);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    public List<Item_unit> getItemUnitListByCodeTax(Item aItem, String aUnitCodeTax) {
        List<Item_unit> aItem_unitList = new ArrayList<>();
        String sql;
        sql = "select un.* from "
                + "("
                + "select i.unit_id,u.unit_symbol,u.unit_name,1 as is_base,0 as default_sale,0 as default_purchase,"
                + "1 as base_qty,1 as other_qty,i.unit_retailsale_price,i.unit_wholesale_price "
                + "from item i inner join unit u on i.unit_id=u.unit_id where i.item_id=? and u.unit_symbol_tax=? "
                + "UNION "
                + "select u.unit_id,u.unit_symbol,u.unit_name,0 as is_base,iu.other_default_sale as default_sale,iu.other_default_purchase as default_purchase,"
                + "iu.base_qty,iu.other_qty,iu.other_unit_retailsale_price as unit_retailsale_price,iu.other_unit_wholesale_price as unit_wholesale_price "
                + "from item_unit_other iu inner join unit u on iu.other_unit_id=u.unit_id where iu.is_active=1 and iu.item_id=? and u.unit_symbol_tax=? "
                + ") as un ";
        ResultSet rs = null;
        try {
            aItem_unitList.clear();
        } catch (Exception e) {
            aItem_unitList = new ArrayList<>();
        }
        if (null == aItem || null == aUnitCodeTax) {
            //do nothing
        } else {
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setLong(1, aItem.getItemId());
                ps.setString(2, aUnitCodeTax);
                ps.setLong(3, aItem.getItemId());
                ps.setString(4, aUnitCodeTax);
                rs = ps.executeQuery();
                while (rs.next()) {
                    Item_unit iu = new Item_unit();
                    this.setItem_unitFromResultset(iu, rs);
                    aItem_unitList.add(iu);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
        return aItem_unitList;
    }

    public Item_unit getItemUnitFrmDb(long aItem_id, int aUnit_id) {
        Item_unit iu = null;
        String sql;
        sql = "select un.* from "
                + "("
                + "select i.unit_id,u.unit_symbol,u.unit_name,1 as is_base,0 as default_sale,0 as default_purchase,"
                + "1 as base_qty,1 as other_qty,i.unit_retailsale_price,i.unit_wholesale_price "
                + "from item i inner join unit u on i.unit_id=u.unit_id where i.item_id=? and u.unit_id=? "
                + "UNION "
                + "select u.unit_id,u.unit_symbol,u.unit_name,0 as is_base,iu.other_default_sale as default_sale,iu.other_default_purchase as default_purchase,"
                + "iu.base_qty,iu.other_qty,iu.other_unit_retailsale_price as unit_retailsale_price,iu.other_unit_wholesale_price as unit_wholesale_price "
                + "from item_unit_other iu inner join unit u on iu.other_unit_id=u.unit_id where iu.is_active=1 and iu.item_id=? and u.unit_id=? "
                + ") as un order by default_sale desc,is_base desc";
        ResultSet rs = null;
        if (aItem_id <= 0 && aUnit_id <= 0) {
            //do nothing
        } else {
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setLong(1, aItem_id);
                ps.setInt(2, aUnit_id);
                ps.setLong(3, aItem_id);
                ps.setInt(4, aUnit_id);
                rs = ps.executeQuery();
                if (rs.next()) {
                    iu = new Item_unit();
                    this.setItem_unitFromResultset(iu, rs);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
        return iu;
    }

    public Item_unit getItemUnitFrmDb(long aItem_id, String aUnit_code_tax) {
        Item_unit iu = null;
        if (null == aUnit_code_tax) {
            aUnit_code_tax = "";
        }
        String sql;
        sql = "select un.* from "
                + "("
                + "select i.unit_id,u.unit_symbol,u.unit_name,1 as is_base,0 as default_sale,0 as default_purchase,"
                + "1 as base_qty,1 as other_qty,i.unit_retailsale_price,i.unit_wholesale_price "
                + "from item i inner join unit u on i.unit_id=u.unit_id where i.item_id=? and u.unit_symbol_tax=? "
                + "UNION "
                + "select u.unit_id,u.unit_symbol,u.unit_name,0 as is_base,iu.other_default_sale as default_sale,iu.other_default_purchase as default_purchase,"
                + "iu.base_qty,iu.other_qty,iu.other_unit_retailsale_price as unit_retailsale_price,iu.other_unit_wholesale_price as unit_wholesale_price "
                + "from item_unit_other iu inner join unit u on iu.other_unit_id=u.unit_id where iu.is_active=1 and iu.item_id=? and u.unit_symbol_tax=? "
                + ") as un order by default_sale desc,is_base desc";
        ResultSet rs = null;
        if (aItem_id <= 0 && aUnit_code_tax.isEmpty()) {
            //do nothing
        } else {
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setLong(1, aItem_id);
                ps.setString(2, aUnit_code_tax);
                ps.setLong(3, aItem_id);
                ps.setString(4, aUnit_code_tax);
                rs = ps.executeQuery();
                if (rs.next()) {
                    iu = new Item_unit();
                    this.setItem_unitFromResultset(iu, rs);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
        return iu;
    }

    public Item_unit getItemUnitFrmList(int aUnit_id) {
        Item_unit iu = null;

        if (aUnit_id <= 0) {
            //do nothing
        } else {
            try {
                for (int i = 0; i < this.Item_unitList.size(); i++) {
                    if (aUnit_id == this.Item_unitList.get(i).getUnit_id()) {
                        //iu = new Item_unit();
                        iu = this.Item_unitList.get(i);
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
        return iu;
    }

    public double getBaseUnitQty(long aItemId, int aOtherUnitId, double aOtherUnitQty) {
        double BaseUnitQty = 0;
        try {
            Item_unit iu = this.getItemUnitFrmDb(aItemId, aOtherUnitId);
            if (null != iu) {
                BaseUnitQty = aOtherUnitQty * iu.getBase_qty() / iu.getOther_qty();
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return BaseUnitQty;
    }

    public double getOtherUnitQty(long aItemId, int aBaseUnitId, double aBaseUnitQty) {
        double OtherUnitQty = 0;
        try {
            Item_unit iu = this.getItemUnitFrmDb(aItemId, aBaseUnitId);
            if (null != iu) {
                OtherUnitQty = aBaseUnitQty * iu.getOther_qty() / iu.getBase_qty();
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return OtherUnitQty;
    }

    public double getUnitConversionRate(long aItemId, int aFromUnitId, int aToUnitId) {
        double ConversionRate = 1;
        try {
            if (aFromUnitId == aToUnitId) {
                ConversionRate = 1;
            } else {
                Item_unit FromIu = this.getItemUnitFrmDb(aItemId, aFromUnitId);
                Item_unit ToIu = this.getItemUnitFrmDb(aItemId, aToUnitId);
                if (null != FromIu && null != ToIu) {
                    //other to base
                    if (FromIu.getIs_base() == 0 && ToIu.getIs_base() == 1) {
                        //ConversionRate = ToIu.getOther_qty() / ToIu.getBase_qty();
                        ConversionRate = FromIu.getOther_qty() / FromIu.getBase_qty();
                    }
                    //base to other
                    if (FromIu.getIs_base() == 1 && ToIu.getIs_base() == 0) {
                        ConversionRate = ToIu.getBase_qty() / ToIu.getOther_qty();
                    }
                    //other to other
                    if (FromIu.getIs_base() == 0 && ToIu.getIs_base() == 0) {
                        ConversionRate = (FromIu.getOther_qty() / FromIu.getBase_qty()) * (ToIu.getBase_qty() / ToIu.getOther_qty());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return ConversionRate;
    }

    /**
     * @param Items the Items to set
     */
    public void setItems(List<Item> Items) {
        this.Items = Items;
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
     * @return the SelectedItemId
     */
    public long getSelectedItemId() {
        return SelectedItemId;
    }

    /**
     * @param SelectedItemId the SelectedItemId to set
     */
    public void setSelectedItemId(long SelectedItemId) {
        this.SelectedItemId = SelectedItemId;
    }

    /**
     * @return the SearchItemDesc
     */
    public String getSearchItemDesc() {
        return SearchItemDesc;
    }

    /**
     * @param SearchItemDesc the SearchItemDesc to set
     */
    public void setSearchItemDesc(String SearchItemDesc) {
        this.SearchItemDesc = SearchItemDesc;
    }

    /**
     * @return the SelectedItem
     */
    public Item getSelectedItem() {
        return SelectedItem;
    }

    /**
     * @param SelectedItem the SelectedItem to set
     */
    public void setSelectedItem(Item SelectedItem) {
        this.SelectedItem = SelectedItem;
    }

    /**
     * @return the SelectedItemX
     */
    public Item getSelectedItemX() {
        return SelectedItemX;
    }

    /**
     * @param SelectedItemX the SelectedItemX to set
     */
    public void setSelectedItemX(Item SelectedItemX) {
        this.SelectedItemX = SelectedItemX;
    }

    /**
     * @return the TypedItemCode
     */
    public String getTypedItemCode() {
        return TypedItemCode;
    }

    /**
     * @param TypedItemCode the TypedItemCode to set
     */
    public void setTypedItemCode(String TypedItemCode) {
        this.TypedItemCode = TypedItemCode;
    }

    /**
     * @return the ItemsList
     */
    public List<Item> getItemsList() {
        return ItemsList;
    }

    /**
     * @param ItemsList the ItemsList to set
     */
    public void setItemsList(List<Item> ItemsList) {
        this.ItemsList = ItemsList;
    }

    /**
     * @return the ItemsSummary
     */
    public List<Item> getItemsSummary() {
        return ItemsSummary;
    }

    /**
     * @param ItemsSummary the ItemsSummary to set
     */
    public void setItemsSummary(List<Item> ItemsSummary) {
        this.ItemsSummary = ItemsSummary;
    }

    /**
     * @return the ItemObj
     */
    public Item getItemObj() {
        return ItemObj;
    }

    /**
     * @param ItemObj the ItemObj to set
     */
    public void setItemObj(Item ItemObj) {
        this.ItemObj = ItemObj;
    }

    /**
     * @return the LocationList
     */
    public List<Location> getLocationList() {
        return LocationList;
    }

    /**
     * @param LocationList the LocationList to set
     */
    public void setLocationList(List<Location> LocationList) {
        this.LocationList = LocationList;
    }

    /**
     * @return the StockList
     */
    public List<Stock> getStockList() {
        return StockList;
    }

    /**
     * @param StockList the StockList to set
     */
    public void setStockList(List<Stock> StockList) {
        this.StockList = StockList;
    }

    /**
     * @return the ParentItem
     */
    public Item getParentItem() {
        return ParentItem;
    }

    /**
     * @param ParentItem the ParentItem to set
     */
    public void setParentItem(Item ParentItem) {
        this.ParentItem = ParentItem;
    }

    /**
     * @return the file
     */
    public Part getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(Part file) {
        this.file = file;
    }

    /**
     * @return the producedItemList
     */
    public List<Item> getProducedItemList() {
        return producedItemList;
    }

    /**
     * @param producedItemList the producedItemList to set
     */
    public void setProducedItemList(List<Item> producedItemList) {
        this.producedItemList = producedItemList;
    }

    /**
     * @return the InventoryTypeList
     */
    public List<Category> getInventoryTypeList() {
        return InventoryTypeList;
    }

    /**
     * @param InventoryTypeList the InventoryTypeList to set
     */
    public void setInventoryTypeList(List<Category> InventoryTypeList) {
        this.InventoryTypeList = InventoryTypeList;
    }

    /**
     * @return the InventoryAccountList
     */
    public List<Category> getInventoryAccountList() {
        return InventoryAccountList;
    }

    /**
     * @param InventoryAccountList the InventoryAccountList to set
     */
    public void setInventoryAccountList(List<Category> InventoryAccountList) {
        this.InventoryAccountList = InventoryAccountList;
    }

    /**
     * @return the Item_unspscObj
     */
    public Item_unspsc getItem_unspscObj() {
        return Item_unspscObj;
    }

    /**
     * @param Item_unspscObj the Item_unspscObj to set
     */
    public void setItem_unspscObj(Item_unspsc Item_unspscObj) {
        this.Item_unspscObj = Item_unspscObj;
    }

    /**
     * @return the Item_unspscList
     */
    public List<Item_unspsc> getItem_unspscList() {
        return Item_unspscList;
    }

    /**
     * @param Item_unspscList the Item_unspscList to set
     */
    public void setItem_unspscList(List<Item_unspsc> Item_unspscList) {
        this.Item_unspscList = Item_unspscList;
    }

    /**
     * @return the SearchUNSPSC
     */
    public String getSearchUNSPSC() {
        return SearchUNSPSC;
    }

    /**
     * @param SearchUNSPSC the SearchUNSPSC to set
     */
    public void setSearchUNSPSC(String SearchUNSPSC) {
        this.SearchUNSPSC = SearchUNSPSC;
    }

    /**
     * @return the Item_tax_mapObj
     */
    public Item_tax_map getItem_tax_mapObj() {
        return Item_tax_mapObj;
    }

    /**
     * @param Item_tax_mapObj the Item_tax_mapObj to set
     */
    public void setItem_tax_mapObj(Item_tax_map Item_tax_mapObj) {
        this.Item_tax_mapObj = Item_tax_mapObj;
    }

    /**
     * @return the ItemTaxObj
     */
    public ItemTax getItemTaxObj() {
        return ItemTaxObj;
    }

    /**
     * @param ItemTaxObj the ItemTaxObj to set
     */
    public void setItemTaxObj(ItemTax ItemTaxObj) {
        this.ItemTaxObj = ItemTaxObj;
    }

    /**
     * @return the ItemObjectList
     */
    public List<Item> getItemObjectList() {
        return ItemObjectList;
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

    /**
     * @return the Item_code_otherList
     */
    public List<Item_code_other> getItem_code_otherList() {
        return Item_code_otherList;
    }

    /**
     * @param Item_code_otherList the Item_code_otherList to set
     */
    public void setItem_code_otherList(List<Item_code_other> Item_code_otherList) {
        this.Item_code_otherList = Item_code_otherList;
    }

    /**
     * @return the Item_code_otherObj
     */
    public Item_code_other getItem_code_otherObj() {
        return Item_code_otherObj;
    }

    /**
     * @param Item_code_otherObj the Item_code_otherObj to set
     */
    public void setItem_code_otherObj(Item_code_other Item_code_otherObj) {
        this.Item_code_otherObj = Item_code_otherObj;
    }

    /**
     * @return the Item_store_reorderObj
     */
    public Item_store_reorder getItem_store_reorderObj() {
        return Item_store_reorderObj;
    }

    /**
     * @param Item_store_reorderObj the Item_store_reorderObj to set
     */
    public void setItem_store_reorderObj(Item_store_reorder Item_store_reorderObj) {
        this.Item_store_reorderObj = Item_store_reorderObj;
    }

    /**
     * @return the Item_store_reorderList
     */
    public List<Item_store_reorder> getItem_store_reorderList() {
        return Item_store_reorderList;
    }

    /**
     * @param Item_store_reorderList the Item_store_reorderList to set
     */
    public void setItem_store_reorderList(List<Item_store_reorder> Item_store_reorderList) {
        this.Item_store_reorderList = Item_store_reorderList;
    }

    /**
     * @return the ReorderLevelEdited
     */
    public int getReorderLevelEdited() {
        return ReorderLevelEdited;
    }

    /**
     * @param ReorderLevelEdited the ReorderLevelEdited to set
     */
    public void setReorderLevelEdited(int ReorderLevelEdited) {
        this.ReorderLevelEdited = ReorderLevelEdited;
    }

    /**
     * @return the Item_unit_otherList
     */
    public List<Item_unit_other> getItem_unit_otherList() {
        return Item_unit_otherList;
    }

    /**
     * @param Item_unit_otherList the Item_unit_otherList to set
     */
    public void setItem_unit_otherList(List<Item_unit_other> Item_unit_otherList) {
        this.Item_unit_otherList = Item_unit_otherList;
    }

    /**
     * @return the Item_unit_otherObj
     */
    public Item_unit_other getItem_unit_otherObj() {
        return Item_unit_otherObj;
    }

    /**
     * @param Item_unit_otherObj the Item_unit_otherObj to set
     */
    public void setItem_unit_otherObj(Item_unit_other Item_unit_otherObj) {
        this.Item_unit_otherObj = Item_unit_otherObj;
    }

    /**
     * @return the ItemOtherUnitsEdited
     */
    public int getItemOtherUnitsEdited() {
        return ItemOtherUnitsEdited;
    }

    /**
     * @param ItemOtherUnitsEdited the ItemOtherUnitsEdited to set
     */
    public void setItemOtherUnitsEdited(int ItemOtherUnitsEdited) {
        this.ItemOtherUnitsEdited = ItemOtherUnitsEdited;
    }

    /**
     * @return the Item_unitList
     */
    public List<Item_unit> getItem_unitList() {
        return Item_unitList;
    }

    /**
     * @param Item_unitList the Item_unitList to set
     */
    public void setItem_unitList(List<Item_unit> Item_unitList) {
        this.Item_unitList = Item_unitList;
    }

    /**
     * @return the SearchExcise
     */
    public String getSearchExcise() {
        return SearchExcise;
    }

    /**
     * @param SearchExcise the SearchExcise to set
     */
    public void setSearchExcise(String SearchExcise) {
        this.SearchExcise = SearchExcise;
    }

    /**
     * @return the ExciseList
     */
    public List<EFRIS_excise_duty_list> getExciseList() {
        return ExciseList;
    }

    /**
     * @param ExciseList the ExciseList to set
     */
    public void setExciseList(List<EFRIS_excise_duty_list> ExciseList) {
        this.ExciseList = ExciseList;
    }
}
