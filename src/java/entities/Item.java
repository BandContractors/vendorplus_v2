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
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;
    private long ItemId;
    private int CompanyId;
    private String ItemCode;
    private String Description;
    private int CategoryId;
    private String CategoryName;
    private Integer SubCategoryId = null;
    private String SubCategoryName;
    private int UnitId;
    private String UnitSymbol;
    private String Unit_symbol_tax;
    private double ReorderLevel;
    private double UnitCostPrice;
    private double UnitRetailsalePrice;
    private double UnitWholesalePrice;
    private String IsSuspended;
    private String VatRated;
    private String[] SelectedVatRateds;
    private String ItemImgUrl;
    private int CountItems;//for report only
    private String ItemType;
    private String CurrencyCode;
    private String currency_code_tax;
    private int IsGeneral;
    private String AssetType;
    private int IsBuy;
    private int IsSale;
    private int IsTrack;
    private int IsAsset;
    private String AssetAccountCode;
    private String ExpenseAccountCode;
    private int is_hire;
    private String duration_type;
    private double unit_hire_price;
    private double unit_special_price;
    private double unit_weight;
    private String LocationName;
    private String expense_type;
    private String alias_name;
    private int display_alias_name;
    private int is_free;
    private int specify_size;
    private int size_to_specific_name;
    private String stock_type;
    private double qty_total;
    private String stock_status;
    private double stock_status_perc;
    private String expiry_band;
    private int override_gen_name;
    private int hide_unit_price_invoice;
    private String account_name;
    private String purpose;
    private String item_code_tax;
    private int is_synced_tax;
    private String vat_rate_order;
    private int store_id;
    private String excise_duty_code;

    /**
     * @return the ItemId
     */
    public long getItemId() {
        return ItemId;
    }

    /**
     * @param ItemId the ItemId to set
     */
    public void setItemId(long ItemId) {
        this.ItemId = ItemId;
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
     * @return the ItemCode
     */
    public String getItemCode() {
        return ItemCode;
    }

    /**
     * @param ItemCode the ItemCode to set
     */
    public void setItemCode(String ItemCode) {
        this.ItemCode = ItemCode;
    }

    /**
     * @return the Description
     */
    public String getDescription() {
        return Description;
    }

    /**
     * @param Description the Description to set
     */
    public void setDescription(String Description) {
        this.Description = Description;
    }

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
     * @return the SubCategoryName
     */
    public String getSubCategoryName() {
        return SubCategoryName;
    }

    /**
     * @param SubCategoryName the SubCategoryName to set
     */
    public void setSubCategoryName(String SubCategoryName) {
        this.SubCategoryName = SubCategoryName;
    }

    /**
     * @return the UnitId
     */
    public int getUnitId() {
        return UnitId;
    }

    /**
     * @param UnitId the UnitId to set
     */
    public void setUnitId(int UnitId) {
        this.UnitId = UnitId;
    }

    /**
     * @return the UnitSymbol
     */
    public String getUnitSymbol() {
        return UnitSymbol;
    }

    /**
     * @param UnitSymbol the UnitSymbol to set
     */
    public void setUnitSymbol(String UnitSymbol) {
        this.UnitSymbol = UnitSymbol;
    }

    /**
     * @return the ReorderLevel
     */
    public double getReorderLevel() {
        return ReorderLevel;
    }

    /**
     * @param ReorderLevel the ReorderLevel to set
     */
    public void setReorderLevel(double ReorderLevel) {
        this.ReorderLevel = ReorderLevel;
    }

//    /**
//     * @return the UnitCostPrice
//     */
//    public double getUnitCostPrice() {
//        return UnitCostPrice;
//    }
//
//    /**
//     * @param UnitCostPrice the UnitCostPrice to set
//     */
//    public void setUnitCostPrice(double UnitCostPrice) {
//        this.UnitCostPrice = UnitCostPrice;
//    }
    /**
     * @return the UnitRetailsalePrice
     */
    public double getUnitRetailsalePrice() {
        return UnitRetailsalePrice;
    }

    /**
     * @param UnitRetailsalePrice the UnitRetailsalePrice to set
     */
    public void setUnitRetailsalePrice(double UnitRetailsalePrice) {
        this.UnitRetailsalePrice = UnitRetailsalePrice;
    }

    /**
     * @return the UnitWholesalePrice
     */
    public double getUnitWholesalePrice() {
        return UnitWholesalePrice;
    }

    /**
     * @param UnitWholesalePrice the UnitWholesalePrice to set
     */
    public void setUnitWholesalePrice(double UnitWholesalePrice) {
        this.UnitWholesalePrice = UnitWholesalePrice;
    }

    /**
     * @return the IsSuspended
     */
    public String getIsSuspended() {
        return IsSuspended;
    }

    /**
     * @param IsSuspended the IsSuspended to set
     */
    public void setIsSuspended(String IsSuspended) {
        this.IsSuspended = IsSuspended;
    }

    /**
     * @return the VatRated
     */
    public String getVatRated() {
        return VatRated;
    }

    /**
     * @param VatRated the VatRated to set
     */
    public void setVatRated(String VatRated) {
        this.VatRated = VatRated;
    }

    /**
     * @return the ItemImgUrl
     */
    public String getItemImgUrl() {
        return ItemImgUrl;
    }

    /**
     * @param ItemImgUrl the ItemImgUrl to set
     */
    public void setItemImgUrl(String ItemImgUrl) {
        this.ItemImgUrl = ItemImgUrl;
    }

    /**
     * @return the SubCategoryId
     */
    public Integer getSubCategoryId() {
        return SubCategoryId;
    }

    /**
     * @param SubCategoryId the SubCategoryId to set
     */
    public void setSubCategoryId(Integer SubCategoryId) {
        this.SubCategoryId = SubCategoryId;
    }

    public void updateModelItem(Item i) {
        this.ItemId = i.getItemId();
        this.UnitId = i.getUnitId();
        this.UnitSymbol = i.getUnitSymbol();
        this.UnitRetailsalePrice = i.getUnitRetailsalePrice();
    }

    /**
     * @return the CountItems
     */
    public int getCountItems() {
        return CountItems;
    }

    /**
     * @param CountItems the CountItems to set
     */
    public void setCountItems(int CountItems) {
        this.CountItems = CountItems;
    }

    /**
     * @return the ItemType
     */
    public String getItemType() {
        return ItemType;
    }

    /**
     * @param ItemType the ItemType to set
     */
    public void setItemType(String ItemType) {
        this.ItemType = ItemType;
    }

    /**
     * @return the CurrencyCode
     */
    public String getCurrencyCode() {
        return CurrencyCode;
    }

    /**
     * @param CurrencyCode the CurrencyCode to set
     */
    public void setCurrencyCode(String CurrencyCode) {
        this.CurrencyCode = CurrencyCode;
    }

    /**
     * @return the IsGeneral
     */
    public int getIsGeneral() {
        return IsGeneral;
    }

    /**
     * @param IsGeneral the IsGeneral to set
     */
    public void setIsGeneral(int IsGeneral) {
        this.IsGeneral = IsGeneral;
    }

    /**
     * @return the AssetType
     */
    public String getAssetType() {
        return AssetType;
    }

    /**
     * @param AssetType the AssetType to set
     */
    public void setAssetType(String AssetType) {
        this.AssetType = AssetType;
    }

    /**
     * @return the IsBuy
     */
    public int getIsBuy() {
        return IsBuy;
    }

    /**
     * @param IsBuy the IsBuy to set
     */
    public void setIsBuy(int IsBuy) {
        this.IsBuy = IsBuy;
    }

    /**
     * @return the IsSale
     */
    public int getIsSale() {
        return IsSale;
    }

    /**
     * @param IsSale the IsSale to set
     */
    public void setIsSale(int IsSale) {
        this.IsSale = IsSale;
    }

    /**
     * @return the IsTrack
     */
    public int getIsTrack() {
        return IsTrack;
    }

    /**
     * @param IsTrack the IsTrack to set
     */
    public void setIsTrack(int IsTrack) {
        this.IsTrack = IsTrack;
    }

    /**
     * @return the IsAsset
     */
    public int getIsAsset() {
        return IsAsset;
    }

    /**
     * @param IsAsset the IsAsset to set
     */
    public void setIsAsset(int IsAsset) {
        this.IsAsset = IsAsset;
    }

    /**
     * @return the AssetAccountCode
     */
    public String getAssetAccountCode() {
        return AssetAccountCode;
    }

    /**
     * @param AssetAccountCode the AssetAccountCode to set
     */
    public void setAssetAccountCode(String AssetAccountCode) {
        this.AssetAccountCode = AssetAccountCode;
    }

    /**
     * @return the ExpenseAccountCode
     */
    public String getExpenseAccountCode() {
        return ExpenseAccountCode;
    }

    /**
     * @param ExpenseAccountCode the ExpenseAccountCode to set
     */
    public void setExpenseAccountCode(String ExpenseAccountCode) {
        this.ExpenseAccountCode = ExpenseAccountCode;
    }

    /**
     * @return the is_hire
     */
    public int getIs_hire() {
        return is_hire;
    }

    /**
     * @param is_hire the is_hire to set
     */
    public void setIs_hire(int is_hire) {
        this.is_hire = is_hire;
    }

    /**
     * @return the duration_type
     */
    public String getDuration_type() {
        return duration_type;
    }

    /**
     * @param duration_type the duration_type to set
     */
    public void setDuration_type(String duration_type) {
        this.duration_type = duration_type;
    }

    /**
     * @return the unit_hire_price
     */
    public double getUnit_hire_price() {
        return unit_hire_price;
    }

    /**
     * @param unit_hire_price the unit_hire_price to set
     */
    public void setUnit_hire_price(double unit_hire_price) {
        this.unit_hire_price = unit_hire_price;
    }

    /**
     * @return the unit_special_price
     */
    public double getUnit_special_price() {
        return unit_special_price;
    }

    /**
     * @param unit_special_price the unit_special_price to set
     */
    public void setUnit_special_price(double unit_special_price) {
        this.unit_special_price = unit_special_price;
    }

    /**
     * @return the unit_weight
     */
    public double getUnit_weight() {
        return unit_weight;
    }

    /**
     * @param unit_weight the unit_weight to set
     */
    public void setUnit_weight(double unit_weight) {
        this.unit_weight = unit_weight;
    }

    /**
     * @return the LocationName
     */
    public String getLocationName() {
        return LocationName;
    }

    /**
     * @param LocationName the LocationName to set
     */
    public void setLocationName(String LocationName) {
        this.LocationName = LocationName;
    }

    /**
     * @return the expense_type
     */
    public String getExpense_type() {
        return expense_type;
    }

    /**
     * @param expense_type the expense_type to set
     */
    public void setExpense_type(String expense_type) {
        this.expense_type = expense_type;
    }

    /**
     * @return the alias_name
     */
    public String getAlias_name() {
        return alias_name;
    }

    /**
     * @param alias_name the alias_name to set
     */
    public void setAlias_name(String alias_name) {
        this.alias_name = alias_name;
    }

    /**
     * @return the UnitCostPrice
     */
    public double getUnitCostPrice() {
        return UnitCostPrice;
    }

    /**
     * @param UnitCostPrice the UnitCostPrice to set
     */
    public void setUnitCostPrice(double UnitCostPrice) {
        this.UnitCostPrice = UnitCostPrice;
    }

    /**
     * @return the display_alias_name
     */
    public int getDisplay_alias_name() {
        return display_alias_name;
    }

    /**
     * @param display_alias_name the display_alias_name to set
     */
    public void setDisplay_alias_name(int display_alias_name) {
        this.display_alias_name = display_alias_name;
    }

    /**
     * @return the is_free
     */
    public int getIs_free() {
        return is_free;
    }

    /**
     * @param is_free the is_free to set
     */
    public void setIs_free(int is_free) {
        this.is_free = is_free;
    }

    /**
     * @return the specify_size
     */
    public int getSpecify_size() {
        return specify_size;
    }

    /**
     * @param specify_size the specify_size to set
     */
    public void setSpecify_size(int specify_size) {
        this.specify_size = specify_size;
    }

    /**
     * @return the size_to_specific_name
     */
    public int getSize_to_specific_name() {
        return size_to_specific_name;
    }

    /**
     * @param size_to_specific_name the size_to_specific_name to set
     */
    public void setSize_to_specific_name(int size_to_specific_name) {
        this.size_to_specific_name = size_to_specific_name;
    }

    /**
     * @return the stock_type
     */
    public String getStock_type() {
        return stock_type;
    }

    /**
     * @param stock_type the stock_type to set
     */
    public void setStock_type(String stock_type) {
        this.stock_type = stock_type;
    }

    /**
     * @return the qty_total
     */
    public double getQty_total() {
        return qty_total;
    }

    /**
     * @param qty_total the qty_total to set
     */
    public void setQty_total(double qty_total) {
        this.qty_total = qty_total;
    }

    /**
     * @return the stock_status
     */
    public String getStock_status() {
        return stock_status;
    }

    /**
     * @param stock_status the stock_status to set
     */
    public void setStock_status(String stock_status) {
        this.stock_status = stock_status;
    }

    /**
     * @return the stock_status_perc
     */
    public double getStock_status_perc() {
        return stock_status_perc;
    }

    /**
     * @param stock_status_perc the stock_status_perc to set
     */
    public void setStock_status_perc(double stock_status_perc) {
        this.stock_status_perc = stock_status_perc;
    }

    /**
     * @return the expiry_band
     */
    public String getExpiry_band() {
        return expiry_band;
    }

    /**
     * @param expiry_band the expiry_band to set
     */
    public void setExpiry_band(String expiry_band) {
        this.expiry_band = expiry_band;
    }

    /**
     * @return the override_gen_name
     */
    public int getOverride_gen_name() {
        return override_gen_name;
    }

    /**
     * @param override_gen_name the override_gen_name to set
     */
    public void setOverride_gen_name(int override_gen_name) {
        this.override_gen_name = override_gen_name;
    }

    /**
     * @return the hide_unit_price_invoice
     */
    public int getHide_unit_price_invoice() {
        return hide_unit_price_invoice;
    }

    /**
     * @param hide_unit_price_invoice the hide_unit_price_invoice to set
     */
    public void setHide_unit_price_invoice(int hide_unit_price_invoice) {
        this.hide_unit_price_invoice = hide_unit_price_invoice;
    }

    /**
     * @return the account_name
     */
    public String getAccount_name() {
        return account_name;
    }

    /**
     * @param account_name the account_name to set
     */
    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }

    /**
     * @return the purpose
     */
    public String getPurpose() {
        return purpose;
    }

    /**
     * @param purpose the purpose to set
     */
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    /**
     * @return the item_code_tax
     */
    public String getItem_code_tax() {
        return item_code_tax;
    }

    /**
     * @param item_code_tax the item_code_tax to set
     */
    public void setItem_code_tax(String item_code_tax) {
        this.item_code_tax = item_code_tax;
    }

    /**
     * @return the Unit_symbol_tax
     */
    public String getUnit_symbol_tax() {
        return Unit_symbol_tax;
    }

    /**
     * @param Unit_symbol_tax the Unit_symbol_tax to set
     */
    public void setUnit_symbol_tax(String Unit_symbol_tax) {
        this.Unit_symbol_tax = Unit_symbol_tax;
    }

    /**
     * @return the currency_code_tax
     */
    public String getCurrency_code_tax() {
        return currency_code_tax;
    }

    /**
     * @param currency_code_tax the currency_code_tax to set
     */
    public void setCurrency_code_tax(String currency_code_tax) {
        this.currency_code_tax = currency_code_tax;
    }

    /**
     * @return the is_synced_tax
     */
    public int getIs_synced_tax() {
        return is_synced_tax;
    }

    /**
     * @param is_synced_tax the is_synced_tax to set
     */
    public void setIs_synced_tax(int is_synced_tax) {
        this.is_synced_tax = is_synced_tax;
    }

    /**
     * @return the SelectedVatRateds
     */
    public String[] getSelectedVatRateds() {
        return SelectedVatRateds;
    }

    /**
     * @param SelectedVatRateds the SelectedVatRateds to set
     */
    public void setSelectedVatRateds(String[] SelectedVatRateds) {
        this.SelectedVatRateds = SelectedVatRateds;
    }

    /**
     * @return the vat_rate_order
     */
    public String getVat_rate_order() {
        return vat_rate_order;
    }

    /**
     * @param vat_rate_order the vat_rate_order to set
     */
    public void setVat_rate_order(String vat_rate_order) {
        this.vat_rate_order = vat_rate_order;
    }

    /**
     * @return the store_id
     */
    public int getStore_id() {
        return store_id;
    }

    /**
     * @param store_id the store_id to set
     */
    public void setStore_id(int store_id) {
        this.store_id = store_id;
    }

    /**
     * @return the excise_duty_code
     */
    public String getExcise_duty_code() {
        return excise_duty_code;
    }

    /**
     * @param excise_duty_code the excise_duty_code to set
     */
    public void setExcise_duty_code(String excise_duty_code) {
        this.excise_duty_code = excise_duty_code;
    }

}
