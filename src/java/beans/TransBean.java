package beans;

import api_sm_bi.CheckApiBean;
import api_sm_bi.LoyaltyCard;
import api_sm_bi.SMbiBean;
import api_tax.efris_bean.InvoiceBean;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import sessions.GeneralUserSetting;
import connections.DBConnection;
import entities.AccChildAccount;
import entities.AccCoa;
import entities.AccCurrency;
import entities.AccDepSchedule;
import entities.AccJournal;
import entities.AccPeriod;
import entities.Category;
import entities.CompanySetting;
import entities.Pay;
import entities.TransItem;
import entities.GroupRight;
import entities.Transactor;
import entities.Item;
import entities.Item_unit;
import entities.Location;
import entities.PayMethod;
import entities.PayTrans;
import entities.TransactionType;
import entities.UserDetail;
import entities.Site;
import entities.Stock;
import entities.Stock_out;
import entities.Store;
import entities.Trans;
import entities.TransactionPackage;
import entities.TransactionReason;
import entities.Transaction_approval;
import entities.Transaction_tax;
import entities.Transaction_tax_map;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static java.sql.Types.VARCHAR;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.primefaces.event.SelectEvent;
import utilities.UtilityBean;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author btwesigye
 */
@ManagedBean //(name = "transBean")
@SessionScoped
public class TransBean implements Serializable {

    private static final long serialVersionUID = 1L;
    static Logger LOGGER = Logger.getLogger(TransBean.class.getName());
    private List<Trans> Transs;
    private List<Trans> TranssDraft;
    private List<Transaction_approval> TransListApproval = new ArrayList<>();
    private String ActionMessage = null;
    private Trans SelectedTrans = null;
    private long SelectedTransactionId;
    private long SelectedTransactionId2;
    private String SearchTrans = "";
    private String TypedTransactorName;
    List<Trans> ReportTrans = new ArrayList<>();
    List<TransSummary> ReportTransSummary = new ArrayList<>();
    private boolean AutoPrintAfterSave;
    Map<String, Object> options;
    private String SRCInvoice;
    private double ReportGrandTotal;
    private UserDetail AuthorisedByUserDetail;
    private UserDetail TransUserDetail;
    private List<Trans> TransactorTranss = new ArrayList<>();
    private int OverridePrintVersion;
    private String DateType;
    private Date Date1;
    private Date Date2;
    private String FieldName;
    private List<Trans> TransList;
    private List<Trans> TransListSummary;
    private Trans TransObj;
    private List<TransItem> TransItemList;
    private Pay PayObj;
    private String ActionType;
    private List<Trans> CustomerCardTranss = new ArrayList<>();
    private List<Trans> CustomerCardTotals = new ArrayList<>();
    private double GrandTotalLoc = 0;
    private double GrandTotalPaidLoc = 0;
    private double GrandTotalBalanceLoc = 0;
    private List<Trans> SupplierCardTranss = new ArrayList<>();
    private List<Trans> SupplierCardTotals = new ArrayList<>();
    private List<Trans> SearchTransList;
    private Integer SearchTransId;
    private String SearchTransNo;
    private Trans RefTrans;
    private Trans TransChild = new Trans();
    private List<TransItem> ActiveTransItemsChild = new ArrayList<>();
    private String ActionMessageChild;
    private Pay PayChild;
    private boolean gen_flag;
    private List<Stock_out> Stock_outList;
    private TransactionType TransTypeObj;
    private TransactionReason TransReasonObj;
    private TransactionType TransTypeRefObj;
    private TransactionReason TransReasonRefObj;
    private List<PayMethod> PayMethodList;
    private List<AccChildAccount> AccChildAccountList;
    private List<Location> LocationList;
    private List<Store> StoreList;
    private int todayoryest;
    @ManagedProperty("#{menuItemBean}")
    private MenuItemBean menuItemBean;
    private int OutputNumber;
    private List<UserDetail> UserDetailList;
    private double GrandTotal2;
    private boolean SelectAll;
    private double OrderSummaryTotal;
    private String OrderMode1;
    private String OrderMode2;
    private String OrderMode3;
    private List<Trans> TransListHist = new ArrayList<>();
    private List<TransItem> TransItemSummary;
    private List<Trans> TransListCrDr = new ArrayList<>();
    private List<TransItem> TransItemSummary2;

    private List<Item> orderItemList;
    private List<TransItem> selectedTransItemsList = new ArrayList<>();
    private Trans newTransAtSplit;
    private TransItem selectedTransItem;
    private JsonArray originalTransItemJsonArray;
    private JsonArray originalStaticTransItemJsonArray;

    public String setBgColorIfEqual(String aA, String aB, int aContext) {
        if (aA.equals(aB)) {
            return "#007fff";//#2E74B5
        } else {
            if (aContext == 1) {//order category
                return "#E2E2E2";
            } else if (aContext == 2) {//category
                return "#7F7F7F";
            } else if (aContext == 3) {//sub-category
                return "#E2E2E2";
            } else if (aContext == 4) {//location
                return "#E2E2E2";
            } else if (aContext == 5) {//store
                return "#E2E2E2";
            } else {
                return "#E2E2E2";
            }
        }
    }

    public String setBgColorByStatus(int aStatus) {
        if (aStatus == 1) {
            return "#007fff";
        } else if (aStatus == 0) {
            return "#d2222d";
        } else {
            return "#E2E2E2";
        }
    }

    public int panelColumnsIfListGreater(int aSize, int aValue) {
        if (aSize > aValue) {
            return 2;
        } else {
            return 1;
        }
    }

    public void updateDelivery_mode(Trans aTrans, String aNewDeliveryMode) {
        if (aTrans.getDelivery_mode().equals(aNewDeliveryMode)) {
            aTrans.setDelivery_mode("");
        } else {
            aTrans.setDelivery_mode(aNewDeliveryMode);
        }
    }

    public void updateStatus(Trans aTrans, String aContext) {
        switch (aContext) {
            case "PROCESSED":
                if (aTrans.getIs_processed() == 2) {
                    aTrans.setIs_processed(1);
                } else if (aTrans.getIs_processed() == 1) {
                    aTrans.setIs_processed(0);
                } else if (aTrans.getIs_processed() == 0) {
                    aTrans.setIs_processed(2);
                }
                break;
            case "INVOICED":
                if (aTrans.getIs_invoiced() == 2) {
                    aTrans.setIs_invoiced(1);
                } else if (aTrans.getIs_invoiced() == 1) {
                    aTrans.setIs_invoiced(0);
                } else if (aTrans.getIs_invoiced() == 0) {
                    aTrans.setIs_invoiced(2);
                }
                break;
            case "PAID":
                if (aTrans.getIs_paid() == 2) {
                    aTrans.setIs_paid(1);
                } else if (aTrans.getIs_paid() == 1) {
                    aTrans.setIs_paid(0);
                } else if (aTrans.getIs_paid() == 0) {
                    aTrans.setIs_paid(2);
                }
                break;
            case "CANCELLED":
                if (aTrans.getIs_cancel() == 2) {
                    aTrans.setIs_cancel(1);
                } else if (aTrans.getIs_cancel() == 1) {
                    aTrans.setIs_cancel(0);
                } else if (aTrans.getIs_cancel() == 0) {
                    aTrans.setIs_cancel(2);
                }
                break;
        }
    }

    public void updateStatus_old(Trans aTrans, String aContext) {
        switch (aContext) {
            case "PROCESSED":
                if (aTrans.getIs_processed() == 0) {
                    aTrans.setIs_processed(1);
                } else if (aTrans.getIs_processed() == 1) {
                    aTrans.setIs_processed(0);
                }
                break;
            case "INVOICED":
                if (aTrans.getIs_invoiced() == 0) {
                    aTrans.setIs_invoiced(1);
                } else if (aTrans.getIs_invoiced() == 1) {
                    aTrans.setIs_invoiced(0);
                }
                break;
            case "PAID":
                if (aTrans.getIs_paid() == 0) {
                    aTrans.setIs_paid(1);
                } else if (aTrans.getIs_paid() == 1) {
                    aTrans.setIs_paid(0);
                }
                break;
            case "CANCELLED":
                if (aTrans.getIs_cancel() == 0) {
                    aTrans.setIs_cancel(1);
                } else if (aTrans.getIs_cancel() == 1) {
                    aTrans.setIs_cancel(0);
                }
                break;
        }
    }

    public void updateStatus(Trans aTrans, String aContext, int aStatus) {
        switch (aContext) {
            case "PROCESSED":
                aTrans.setIs_processed(aStatus);
                break;
            case "INVOICED":
                aTrans.setIs_invoiced(aStatus);
                break;
            case "PAID":
                aTrans.setIs_paid(aStatus);
                break;
            case "CANCELLED":
                aTrans.setIs_cancel(aStatus);
                break;
        }
    }

    public void updateTodayYesturday(int aValue) {
        if (this.todayoryest == aValue) {
            this.todayoryest = 0;
        } else {
            this.todayoryest = aValue;
        }
    }

    public void updateStore2Id(Trans aTrans, int aStore2Id) {
        if (aTrans.getStore2Id() == aStore2Id) {
            aTrans.setStore2Id(0);
        } else {
            aTrans.setStore2Id(aStore2Id);
        }
    }

    public void updatePrintFile(int aPrintFile) {
        if (aPrintFile == this.OverridePrintVersion) {
            this.OverridePrintVersion = 0;
        } else if (aPrintFile == 1) {
            this.OverridePrintVersion = 1;
        } else if (aPrintFile == 2) {
            this.OverridePrintVersion = 2;
        } else {
            this.OverridePrintVersion = 0;
        }
    }

    public void updateLocation_id(Trans aTrans, int aLocation_id) {
        if (aTrans.getLocation_id() == aLocation_id) {
            aTrans.setLocation_id(0);
        } else {
            aTrans.setLocation_id(aLocation_id);
        }
    }

    public void updateUserDetail(Trans aTrans, int aUserDetailId) {
        if (aTrans.getTransactionUserDetailId() == aUserDetailId) {
            aTrans.setTransactionUserDetailId(0);
        } else {
            aTrans.setTransactionUserDetailId(aUserDetailId);
        }
    }

    public void updateIs_selected(Trans aTrans) {
        if (aTrans.getIs_selected() == 1) {
            aTrans.setIs_selected(0);
        } else {
            aTrans.setIs_selected(1);
        }
    }

    public void initLocationsByStore(int aStoreId) {
        try {
            try {
                this.LocationList.clear();
            } catch (NullPointerException npe) {
                this.LocationList = new ArrayList<>();
            }
            this.LocationList = new LocationBean().getLocations(aStoreId);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void initStoresByStore(int aStoreId) {
        try {
            try {
                this.StoreList.clear();
            } catch (NullPointerException npe) {
                this.StoreList = new ArrayList<>();
            }
            this.StoreList = new StoreBean().getStores();
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void initUserDetail(int aUserDetailId) {
        try {
            try {
                this.UserDetailList.clear();
            } catch (NullPointerException npe) {
                this.UserDetailList = new ArrayList<>();
            }
            this.UserDetailList = new UserDetailBean().getUserDetailsNotLocked();
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void initResetDefaultQuickOrder(Trans aTrans) {
        aTrans.setStore2Id(0);
        aTrans.setLocation_id(0);
        try {
            aTrans.setDelivery_mode(new Parameter_listBean().getParameter_listByContextNameMemory("ORDER", "DEFAULT_DELIVERY_MODE").getParameter_value());
        } catch (NullPointerException npe) {
            aTrans.setDelivery_mode("");
        }
        try {
            String OrderModes = new Parameter_listBean().getParameter_listByContextNameMemory("ORDER", "DELIVERY_MODES").getParameter_value();
            this.OrderMode1 = "";
            this.OrderMode2 = "";
            this.OrderMode3 = "";
            if (OrderModes.length() == 0) {
                this.OrderMode1 = "";
                this.OrderMode2 = "";
                this.OrderMode3 = "";
            } else {
                String[] items = OrderModes.split(",");
                List<String> container = Arrays.asList(items);
                for (int i = 0; i < container.size(); i++) {
                    if (i == 0 && container.get(i).length() > 0) {
                        this.OrderMode1 = container.get(i);
                    } else if (i == 1 && container.get(i).length() > 0) {
                        this.OrderMode2 = container.get(i);
                    } else if (i == 2 && container.get(i).length() > 0) {
                        this.OrderMode3 = container.get(i);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        try {
            int storeid = 0;
            storeid = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("ORDER", "DEFAULT_ORDER_TO_STORE").getParameter_value());
            if (storeid > 0 && null == new StoreBean().getStore(storeid)) {
                storeid = 0;
            }
            aTrans.setStore2Id(storeid);
        } catch (NullPointerException npe) {
            aTrans.setStore2Id(0);
        }
    }

    public void initQuickOrderManageFilter(Trans aTrans) {
        aTrans.setIs_processed(2);
        aTrans.setIs_cancel(2);
        aTrans.setIs_paid(2);
        aTrans.setIs_invoiced(2);
        aTrans.setUser_code("");
        this.todayoryest = 1;
        aTrans.setStoreId(new GeneralUserSetting().getCurrentStore().getStoreId());
        aTrans.setCategory(null);
        aTrans.setTransactionUserDetailId(0);
    }

    public void initQuickOrderManageFilterDashboard(Trans aTrans) {
        aTrans.setIs_processed(0);
        aTrans.setIs_cancel(2);
        aTrans.setIs_paid(2);
        aTrans.setIs_invoiced(2);
        aTrans.setUser_code("");
        this.todayoryest = 1;
        aTrans.setStoreId(new GeneralUserSetting().getCurrentStore().getStoreId());
        aTrans.setCategory(null);
        aTrans.setTransactionUserDetailId(0);
    }

    public void refreshPayMethodsActiveIn(String aPayMethodIDs) {
        this.PayMethodList = new PayMethodBean().getPayMethodsActiveIn(aPayMethodIDs);
    }

    public void refreshAccChildAccountsForCashReceipt(String aCurrencyCode, int aPayMethodId, int aStoreId, int aUserDetailId) {
        this.AccChildAccountList = new AccChildAccountBean().getAccChildAccountsForCashReceipt(aCurrencyCode, aPayMethodId, aStoreId, aUserDetailId);
    }

    public void RetrieveAndUpdateTransAndItems(int aTransTypeId, int aRetrieveTransTypeId, int aRetrieveTransReasId, Trans aTrans, List<TransItem> aTransItems) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        int CurrStoreId = 0;
        int CurrTransTypeId = 0;
        String CurCode1 = "";
        String CurCode2 = "";
        int TransNeedsCurrency = 1;
        try {
            try {
                aTransItems.clear();
            } catch (Exception e) {
            }
            Trans RetrievedTrans = new Trans();
            RetrievedTrans = this.getTransByTransNumber(aTrans.getTransactionRef());
            if (aTrans.getCurrencyCode() == null) {
            } else {
                CurCode1 = aTrans.getCurrencyCode();
            }
            if (null == RetrievedTrans.getCurrencyCode()) {
            } else {
                CurCode2 = RetrievedTrans.getCurrencyCode();
            }
            TransNeedsCurrency = new TransactionTypeBean().checkTransactionTypeNeedsCurrency(aTransTypeId);
            if (TransNeedsCurrency == 1 && !CurCode1.equals(CurCode2)) {
                //do nothing
            } else if (null != RetrievedTrans && aRetrieveTransTypeId == RetrievedTrans.getTransactionTypeId() && aRetrieveTransReasId == RetrievedTrans.getTransactionReasonId()) {
                CurrStoreId = new GeneralUserSetting().getCurrentStore().getStoreId();
                CurrTransTypeId = new GeneralUserSetting().getCurrentTransactionTypeId();
                if ((CurrTransTypeId != 4 && CurrStoreId == RetrievedTrans.getStoreId()) || (CurrTransTypeId == 4 && CurrStoreId == RetrievedTrans.getStore2Id() && aTrans.getStore2Id() == RetrievedTrans.getStoreId())) {
                    //transactor
                    if (RetrievedTrans.getTransactorId() > 0) {
                        aTrans.setTransactorId(RetrievedTrans.getTransactorId());
                    }
                    //trans items
                    new TransItemBean().assignTransItemsByTransactionId(RetrievedTrans.getTransactionId(), aTransItems);
                    //reset TransItemId=0 for trans such as:
                    //a)When Making Goods Received Trans
                    if (new GeneralUserSetting().getCurrentTransactionTypeId() == 9) {
                        new TransItemBean().resetTransactionItem(3, aTransItems);
                    }
                    //b)When Making Purchase Order Trans
                    if (new GeneralUserSetting().getCurrentTransactionTypeId() == 1) {
                        new TransItemBean().resetTransactionItem(1, aTransItems);
                    }
                    //c)When Making Goods Delivery Trans
                    if (new GeneralUserSetting().getCurrentTransactionTypeId() == 12) {
                        new TransItemBean().resetTransactionItem(2, aTransItems);
                    }
                    //c)When Making Stock Transfers
                    if (new GeneralUserSetting().getCurrentTransactionTypeId() == 4) {
                        if (RetrievedTrans.getStoreId() > 0) {
                            aTrans.setStore2Id(RetrievedTrans.getStoreId());
                        }
                        new TransItemBean().resetTransactionItem(2, aTransItems);
                    }
                }
                //get total transferred/delivered/etc.
                if (aRetrieveTransTypeId == 13) {
                    TransItem ti2;
                    for (int i = 0; i < aTransItems.size(); i++) {
                        //ti2 = getRefTransItemsTotal(4, 6, aTrans.getTransactionRef(), aTransItems.get(i));
                        ti2 = getRefTransItemsTotalNoSpecific(4, 6, aTrans.getTransactionRef(), aTransItems.get(i));
                        aTransItems.get(i).setQty_taken(aTransItems.get(i).getItemQty());//Requested
                        aTransItems.get(i).setQty_total(ti2.getItemQty());//total Issued
                        aTransItems.get(i).setQty_balance(aTransItems.get(i).getQty_taken() - aTransItems.get(i).getQty_total());
                        aTransItems.get(i).setItemQty(aTransItems.get(i).getQty_balance());//Issue
                    }
                }
                //get current stock
                if (aRetrieveTransTypeId == 13) {
                    new TransItemBean().refreshCurrentStock(aTransItems);
                }
            } else {
                aTrans.setTransactorId(0);
                //aTrans.setTransactionRef("");
                aTransItems.clear();
                FacesContext.getCurrentInstance().addMessage("Retrieve PO", new FacesMessage(ub.translateWordsInText(BaseName, "Either Currency or Transaction Type of the Order does Not Match with Selected Currency")));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void RetrieveAndUpdateTransAndItemsGDN(int aTransTypeId, int aRetrieveTransTypeId, int aRetrieveTransTypeId2, Trans aTrans, List<TransItem> aTransItems) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        int CurrStoreId = 0;
        int CurrTransTypeId = 0;
        String CurCode1 = "";
        String CurCode2 = "";
        int TransNeedsCurrency = 1;
        try {
            Trans RetrievedTrans = new Trans();
            RetrievedTrans = this.getTransByTransNumber(aTrans.getTransactionRef());
            if (aTrans.getCurrencyCode() == null) {
            } else {
                CurCode1 = aTrans.getCurrencyCode();
            }
            try {
                CurCode2 = RetrievedTrans.getCurrencyCode();
            } catch (NullPointerException npe) {
            }
            TransNeedsCurrency = new TransactionTypeBean().checkTransactionTypeNeedsCurrency(aTransTypeId);
            if (TransNeedsCurrency == 1 && !CurCode1.equals(CurCode2)) {
                //do nothing
            } else if (RetrievedTrans != null && (aRetrieveTransTypeId == RetrievedTrans.getTransactionTypeId() || aRetrieveTransTypeId2 == RetrievedTrans.getTransactionTypeId())) {
                CurrStoreId = new GeneralUserSetting().getCurrentStore().getStoreId();
                CurrTransTypeId = new GeneralUserSetting().getCurrentTransactionTypeId();
                if ((CurrTransTypeId != 4 && CurrStoreId == RetrievedTrans.getStoreId()) || (CurrTransTypeId == 4 && CurrStoreId == RetrievedTrans.getStore2Id())) {
                    //transactor
                    if (RetrievedTrans.getTransactorId() > 0) {
                        aTrans.setTransactorId(RetrievedTrans.getTransactorId());
                    }
                    //trans items
                    new TransItemBean().assignTransItemsByTransactionId(RetrievedTrans.getTransactionId(), aTransItems);
                    //reset TransItemId=0 for trans such as:
                    //a)When Making Goods Received Trans
                    if (new GeneralUserSetting().getCurrentTransactionTypeId() == 9) {
                        new TransItemBean().resetTransactionItem(3, aTransItems);
                    }
                    //b)When Making Purchase Order Trans
                    if (new GeneralUserSetting().getCurrentTransactionTypeId() == 1) {
                        new TransItemBean().resetTransactionItem(1, aTransItems);
                    }
                    //c)When Making Goods Delivery Trans
                    if (new GeneralUserSetting().getCurrentTransactionTypeId() == 12) {
                        new TransItemBean().resetTransactionItem(2, aTransItems);
                    }
                    //c)When Making Stock Transfers
                    if (new GeneralUserSetting().getCurrentTransactionTypeId() == 4) {
                        if (RetrievedTrans.getStoreId() > 0) {
                            aTrans.setStore2Id(RetrievedTrans.getStoreId());
                        }
                        new TransItemBean().resetTransactionItem(2, aTransItems);
                    }
                }
            } else {
                aTrans.setTransactorId(0);
                //aTrans.setTransactionRef("");
                aTransItems.clear();
                FacesContext.getCurrentInstance().addMessage("Retrieve PO", new FacesMessage(ub.translateWordsInText(BaseName, "Order Currency or Transaction Type Does Not Match with Selected Currency")));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void loadTransForGDN(int aTransTypeIdChoice, Trans aTrans, List<TransItem> aTransItems) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        int CurrStoreId = 0;
        int RetrieveTransTypeId = 0;
        if (aTransTypeIdChoice == 0) {
            //do nothing
        } else {
            RetrieveTransTypeId = aTransTypeIdChoice;
            CurrStoreId = new GeneralUserSetting().getCurrentStore().getStoreId();
            try {
                Trans RetrievedTrans = new Trans();
                this.setTransByTransNumber(RetrievedTrans, aTrans.getTransactionRef(), aTrans.getTransactorId());
                if (CurrStoreId == RetrievedTrans.getStoreId() && RetrieveTransTypeId == RetrievedTrans.getTransactionTypeId() && aTrans.getTransactorId() == RetrievedTrans.getTransactorId()) {
                    //trans items
                    new TransItemBean().assignTransItemsByTransactionId(RetrievedTrans.getTransactionId(), aTransItems);
                    //reset trans item IDs and prices
                    new TransItemBean().resetTransactionItem(2, aTransItems);
                    //update totals delivered
                    TransItem ti = null;
                    for (int i = 0; i < aTransItems.size(); i++) {
                        //ti2 = getRefTransItemsTotal(4, 6, aTrans.getTransactionRef(), aTransItems.get(i));
                        ti = new TransItem();
                        ti = getRefTransItemsTotalNoSpecific(12, 0, aTrans.getTransactionRef(), aTransItems.get(i));
                        aTransItems.get(i).setQty_taken(aTransItems.get(i).getItemQty());//Ordered/Sold Qty
                        aTransItems.get(i).setQty_total(ti.getItemQty());//Delivered Qty
                        aTransItems.get(i).setQty_balance(aTransItems.get(i).getQty_taken() - aTransItems.get(i).getQty_total());//balance
                        if (aTransItems.get(i).getQty_balance() > 0) {
                            aTransItems.get(i).setItemQty(aTransItems.get(i).getQty_balance());//Qty to Deliver=balance
                            new TransItemBean().updateBaseUnityQty(aTransItems.get(i));
                        } else {
                            aTransItems.get(i).setItemQty(0);//Qty to Deliver=0
                            aTransItems.get(i).setBase_unit_qty(0);
                        }
                    }
                } else {
                    aTransItems.clear();
                    FacesContext.getCurrentInstance().addMessage("Retrieved", new FacesMessage(ub.translateWordsInText(BaseName, "Transaction Type or Customer or Transaction Number Does Not Match")));
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    public void RetrieveAndUpdateTransAndItems2(int aRetrieveTransTypeId, int aRetrieveTransReasId1, int aRetrieveTransReasId2, Trans aTrans, List<TransItem> aTransItems) {
        int CurrStoreId = 0;
        int CurrTransTypeId = 0;
        String CurCode1 = "";
        String CurCode2 = "";
        try {
            Trans RetrievedTrans = new Trans();
            RetrievedTrans = this.getTransByTransNumber(aTrans.getTransactionRef());
            if (aTrans.getCurrencyCode() == null) {
            } else {
                CurCode1 = aTrans.getCurrencyCode();
            }
            if (RetrievedTrans.getCurrencyCode() == null) {
            } else {
                CurCode2 = RetrievedTrans.getCurrencyCode();
            }
            if (RetrievedTrans != null && aRetrieveTransTypeId == RetrievedTrans.getTransactionTypeId() && (aRetrieveTransReasId1 == RetrievedTrans.getTransactionReasonId() || aRetrieveTransReasId2 == RetrievedTrans.getTransactionReasonId()) && CurCode1.equals(CurCode2)) {
                CurrStoreId = new GeneralUserSetting().getCurrentStore().getStoreId();
                CurrTransTypeId = new GeneralUserSetting().getCurrentTransactionTypeId();
                if ((CurrTransTypeId != 4 && CurrStoreId == RetrievedTrans.getStoreId()) || (CurrTransTypeId == 4 && CurrStoreId == RetrievedTrans.getStore2Id())) {
                    //transactor
                    if (RetrievedTrans.getTransactorId() > 0) {
                        aTrans.setTransactorId(RetrievedTrans.getTransactorId());
                    }
                    //trans items
                    new TransItemBean().assignTransItemsByTransactionId(RetrievedTrans.getTransactionId(), aTransItems);
                    //reset TransItemId=0 for trans such as:
                    //a)When Making Goods Received Trans
                    if (new GeneralUserSetting().getCurrentTransactionTypeId() == 9) {
                        new TransItemBean().resetTransactionItem(2, aTransItems);
                    }
                    //b)When Making Purchase Order Trans
                    if (new GeneralUserSetting().getCurrentTransactionTypeId() == 1) {
                        new TransItemBean().resetTransactionItem(1, aTransItems);
                    }
                    //c)When Making Goods Delivery Trans
                    if (new GeneralUserSetting().getCurrentTransactionTypeId() == 12) {
                        new TransItemBean().resetTransactionItem(2, aTransItems);
                    }
                    //c)When Making Stock Transfers
                    if (new GeneralUserSetting().getCurrentTransactionTypeId() == 4) {
                        if (RetrievedTrans.getStoreId() > 0) {
                            aTrans.setStore2Id(RetrievedTrans.getStoreId());
                        }
                        new TransItemBean().resetTransactionItem(2, aTransItems);
                    }
                }
            } else {
                aTrans.setTransactorId(0);
                //aTrans.setTransactionRef("");
                aTransItems.clear();
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public String validateTransCEC(int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor) {
        String msg = "";
        Store store2 = new StoreBean().getStore(trans.getStore2Id());
        int Store2Id = 0;
        try {
            Store2Id = store2.getStoreId();
        } catch (Exception e) {
            Store2Id = 0;
        }
        try {
            TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
            TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
            Store store = new StoreBean().getStore(aStoreId);

            String ItemMessage = "";
            try {
                if (aTransTypeId == 2 && Store2Id > 0) {
                    if (trans.getTransactionId() > 0) {//Update
                        ItemMessage = new TransItemBean().getAnyItemTotalQtyGreaterThanCurrentQty(new TransItemBean().getTransItemListCurLessPrevQty(aActiveTransItems, trans), Store2Id, transtype.getTransactionTypeName());
                    } else {//Insert
                        ItemMessage = new TransItemBean().getAnyItemTotalQtyGreaterThanCurrentQty(aActiveTransItems, Store2Id, transtype.getTransactionTypeName());
                    }
                } else {
                    if (trans.getTransactionId() > 0) {//Update
                        ItemMessage = new TransItemBean().getAnyItemTotalQtyGreaterThanCurrentQty(new TransItemBean().getTransItemListCurLessPrevQty(aActiveTransItems, trans), store.getStoreId(), transtype.getTransactionTypeName());
                    } else {//Insert
                        ItemMessage = new TransItemBean().getAnyItemTotalQtyGreaterThanCurrentQty(aActiveTransItems, store.getStoreId(), transtype.getTransactionTypeName());
                    }
                }
            } catch (NullPointerException npe) {
            }
            String ItemMessage2 = "";
            try {
                ItemMessage2 = new TransItemBean().getAnyItemReturnTotalGreaterThanBalance(aActiveTransItems, transtype.getTransactionTypeName());
            } catch (NullPointerException npe) {
            }
            String ItemMessage3 = "";
            try {
                ItemMessage3 = new TransItemBean().getAnyItemDeliveryTotalGreaterThanBalance(aActiveTransItems, transtype.getTransactionTypeName());
            } catch (NullPointerException npe) {
            }
            int IsNewTransNoUsed = 0;
            if (transtype.getTransactionTypeId() == 2 && trans.getTransactionId() == 0 && trans.getTransactionNumber().length() > 0) {
                IsNewTransNoUsed = new Trans_number_controlBean().getIsTrans_number_used(transtype.getTransactionTypeId(), trans.getTransactionNumber());
            }
            String MsgCkeckApproval = "";
            if (trans.getTransaction_approval_id() > 0) {
                Transaction_approval apprtrans = new Transaction_approvalBean().getTransaction_approval(trans.getTransaction_approval_id());
                if (null == apprtrans) {
                    MsgCkeckApproval = "Invalid Approval Transaction Reference";
                } else {
                    if (apprtrans.getGrand_total() != trans.getGrandTotal() || apprtrans.getAmount_tendered() != trans.getAmountTendered() || apprtrans.getTransactor_id() != trans.getTransactorId()) {
                        MsgCkeckApproval = "Approved and Transaction to Save  are Different";
                    }
                    if (apprtrans.getApproval_status() != 1) {
                        MsgCkeckApproval = "Transaction is Not Approved for Processing";
                    }
                }
            }
            String vExcise = new TransItemExtBean().validateExciseDuty(aTransTypeId, aActiveTransItems);
            UserDetail aCurrentUserDetail = new GeneralUserSetting().getCurrentUser();
            List<GroupRight> aCurrentGroupRights = new GeneralUserSetting().getCurrentGroupRights();
            GroupRightBean grb = new GroupRightBean();

            if (null == transtype) {
                msg = "Invalid Transaction";
            } else if (trans.getTransactionId() == 0 && grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, Integer.toString(transreason.getTransactionReasonId()), "Add") == 0) {
                msg = "Access Denied";
            } else if (IsNewTransNoUsed == 1) {
                msg = "Specify New Transaction Number";
            } else if (!ItemMessage.equals("")) {
                msg = "Insufficient Stock for Item ##" + ItemMessage;
            } else if (!ItemMessage2.equals("") && "HIRE RETURN NOTE".equals(transtype.getTransactionTypeName())) {
                msg = "Returned Greater Than Balance for Item  ##" + ItemMessage2;
            } else if (!ItemMessage3.equals("") && "HIRE DELIVERY NOTE".equals(transtype.getTransactionTypeName())) {
                msg = "Delivered Greater Than Balance for Item ##" + ItemMessage3;
            } else if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) && "COST-PRICE SALE INVOICE".equals(aSaleType) && trans.getTransactorId() == 0) {
                msg = "Select " + transtype.getBillTransactorLabel();
            } else if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) && "No".equals(CompanySetting.getIsCashDiscountVatLiable()) && (trans.getCashDiscount() + trans.getSpendPointsAmount()) > (trans.getSubTotal() - trans.getTotalTradeDiscount() + trans.getTotalVat())) {
                msg = "Cash and Loyalty Discount cannot exceed Total";
            } else if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) && "Yes".equals(CompanySetting.getIsCashDiscountVatLiable()) && (trans.getCashDiscount() + trans.getSpendPointsAmount()) > (trans.getSubTotal() - trans.getTotalTradeDiscount())) {
                msg = "Cash and Loyalty Discount cannot extend to VAT or exceed Total";
            } else if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) && trans.getCashDiscount() < 0 || trans.getAmountTendered() < 0 || trans.getSpendPointsAmount() < 0 || trans.getGrandTotal() < 0) {
                msg = "Check Cash Discount or Amount Tendered or Loyalty Discount";
            } else if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) && trans.getCashDiscount() < 0) {
                msg = "Check Cash Discount";
            } else if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) && trans.getCashDiscount() > 0 && new GeneralUserSetting().getIsApproveDiscountNeeded() == 1 && !"APPROVED".equals(new GeneralUserSetting().getCurrentApproveDiscountStatus())) {
                msg = "Access Denied to Cash Discount";
            } else if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) && trans.getSpendPointsAmount() > 0 && new GeneralUserSetting().getIsApprovePointsNeeded() == 1 && !"APPROVED".equals(new GeneralUserSetting().getCurrentApprovePointsStatus())) {
                msg = "Access Denied to Spend Points";
            } else if (trans.getTransactionDate() == null) {
                msg = "Select " + transtype.getTransactionDateLabel();
                if ("UNPACK".equals(transtype.getTransactionTypeName())) {
                    aActiveTransItems.clear();
                }
            } else if ((new GeneralUserSetting().getDaysFromDateToLicenseExpiryDate(trans.getTransactionDate()) <= 0 || new GeneralUserSetting().getDaysFromDateToLicenseExpiryDate(new CompanySetting().getCURRENT_SERVER_DATE()) <= 0) && CompanySetting.getLicenseType() != 9) {
                msg = "Server Date is Wrong or Lincese is Expired";
            } else if (trans.getTransactorId() == 0 && transtype.getIsTransactorMandatory().equals("Yes")) {
                msg = "Select Valid Customer";
            } else if (aActiveTransItems.size() < 1 & !"UNPACK".equals(transtype.getTransactionTypeName()) & trans.getTransactionId() == 0) {
                msg = "Item not Found for " + transtype.getTransactionOutputLabel();
            } else if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) && trans.getPayMethod() == 0 && trans.getTransactionId() == 0) {
                msg = "Select Payment Method";
            } else if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) && "No".equals(CompanySetting.getIsAllowDebt()) && trans.getAmountTendered() < trans.getGrandTotal()) {
                msg = "Amount tendered is Less Than Total";
            } else if (trans.getTransactionId() == 0 && "SALE INVOICE".equals(transtype.getTransactionTypeName()) && trans.getSpendPointsAmount() > trans.getBalancePointsAmount()) {
                msg = "Amount entered for spending points exceeds the available points balance";
            } else if (trans.getTransactionId() == 0 && "SALE INVOICE".equals(transtype.getTransactionTypeName()) && "Yes".equals(CompanySetting.getIsAllowDebt()) && trans.getAmountTendered() < trans.getGrandTotal() && trans.getTransactorId() == 0) {
                msg = "Select Customer for the Credit Sale";
            } else if (trans.getTransactionId() == 0 && "SALE INVOICE".equals(transtype.getTransactionTypeName()) && "Yes".equals(CompanySetting.getIsAllowDebt()) && new TransactorBean().creditLimitExceeded(trans.getTransactorId(), trans.getBillTransactorId(), trans.getGrandTotal() - trans.getAmountTendered(), trans.getCurrencyCode()) == 1) {
                msg = "You Cannot Exceed Customer Credit Limit";
            } else if (("TRANSFER".equals(transtype.getTransactionTypeName()) || "TRANSFER REQUEST".equals(transtype.getTransactionTypeName())) && trans.getStore2Id() == 0) {
                msg = "Select the " + CompanySetting.getStoreEquivName() + " for Item Transfer";
            } else if (("TRANSFER".equals(transtype.getTransactionTypeName()) || "TRANSFER REQUEST".equals(transtype.getTransactionTypeName())) && store.getStoreId() == trans.getStore2Id()) {
                msg = "Select different To and From " + CompanySetting.getStoreEquivName();
            } else if ("Yes".equals(transtype.getIsTransactionUserMandatory()) && trans.getTransactionUserDetailId() == 0) {
                msg = "Select " + transtype.getTransactionUserLabel();
            } else if ("Yes".equals(transtype.getIsTransactionRefMandatory()) && trans.getTransactionRef().equals("")) {
                msg = "Select " + transtype.getTransactionRefLabel();
            } else if ("Yes".equals(transtype.getIsTransactionRefMandatory()) && trans.getTransactionRef().equals("")) {
                msg = "Select " + transtype.getTransactionRefLabel();
            } else if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) && trans.isBillOther() && trans.getBillTransactorId() == 0) {
                msg = "Select Billing Customer";
            } else if (trans.getAuthorisedByUserDetailId() == 0 && transtype.getIsAuthoriseUserMandatory().equals("Yes")) {
                msg = "Select Authorise User";
            } else if (trans.getAuthoriseDate() == null && transtype.getIsAuthoriseDateMandatory().equals("Yes")) {
                msg = "Select Authorise Date";
            } else if (trans.getDeliveryAddress().length() == 0 && transtype.getIsDeliveryAddressMandatory().equals("Yes")) {
                msg = "Specify Delivery Address";
            } else if (trans.getDeliveryDate() == null && transtype.getIsDeliveryDateMandatory().equals("Yes")) {
                msg = "Select Delivery Date";
            } else if (trans.getPayDueDate() == null && transtype.getIsPayDueDateMandatory().equals("Yes")) {
                msg = "Select Pay Due Date";
            } else if (trans.getExpiryDate() == null && transtype.getIsExpiryDateMandatory().equals("Yes")) {
                msg = "Select Expiry Date for " + transtype.getTransactionOutputLabel();
            } else if (aSelectedTransactor != null && aSelectedTransactor.getIsSuspended().equals("Yes")) {
                msg = "Suspended ##" + aSelectedTransactor.getTransactorNames() + " for " + aSelectedTransactor.getSuspendedReason();
            } else if (aSelectedBillTransactor != null && aSelectedBillTransactor.getIsSuspended().equals("Yes")) {
                msg = "Suspended ##" + aSelectedBillTransactor.getTransactorNames() + " for " + aSelectedBillTransactor.getSuspendedReason();
            } else if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) && trans.getGrandTotal() > 0 && trans.getAccChildAccountId() == 0) {
                msg = "Select Payment Receipt Account";
            } else if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) && trans.getAmountTendered() < trans.getGrandTotal() && trans.getTransactorId() == 0) {
                msg = "Select " + transtype.getTransactorLabel();
            } else if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) && trans.getAmountTendered() >= 0 && trans.getAccChildAccountId() == 0) {
                msg = "Select Payment Account";
            } else if ("JOURNAL ENTRY".equals(transtype.getTransactionTypeName()) && trans.getTotalDebit() != trans.getTotalCredit()) {
                msg = "Debit is not Equal to Credit";
            } else if ("EXPENSE ENTRY".equals(transtype.getTransactionTypeName()) && (trans.getAmountTendered() <= 0 && trans.getGrandTotal() <= 0) && trans.getTransactionId() == 0) {
                msg = "Enter Spent or Paid Amount";
            } else if ("EXPENSE ENTRY".equals(transtype.getTransactionTypeName()) && trans.getAccChildAccountId() == 0) {
                msg = "Select Payment Account";
            } else if (null == new AccPeriodBean().getAccPeriod(trans.getTransactionDate())) {
                msg = "Selected Date does not Match Accounting Period";
            } else if (new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getIsClosed() == 1) {
                msg = "Selected Date is for a Closed Accounting Period";
            } else if (trans.getTransactionId() == 0 && (trans.getPayMethod() == 6 || trans.getPayMethod() == 7) && trans.getAmountTendered() <= 0) {
                msg = "Amount Tendered Not Accepted for Payment Method";
            } else if (trans.getTransactionId() == 0 && trans.getPayMethod() == 6 && trans.getAmountTendered() > new AccLedgerBean().getPrepaidIncomeAccBalanceTrade(new TransBean().getBillClientId(trans.getTransactorId(), trans.getBillTransactorId()), trans.getCurrencyCode())) {
                msg = "Insufficient Funds on the Customer Deposit Account";
            } else if (trans.getTransactionId() == 0 && trans.getPayMethod() == 7 && trans.getAmountTendered() > new AccLedgerBean().getPrepaidExpenseAccBalanceTrade(new TransBean().getBillClientId(trans.getTransactorId(), trans.getBillTransactorId()), trans.getCurrencyCode())) {
                msg = "Insufficient Funds on the Supplier Advance Expense Account";
            } else if ("HIRE INVOICE".equals(transtype.getTransactionTypeName()) && (trans.getFrom_date() == null || trans.getTo_date() == null)) {
                msg = "Specify From and To Date";
            } else if ("HIRE INVOICE".equals(transtype.getTransactionTypeName()) && trans.getGrandTotal() > 0 && trans.getAccChildAccountId() == 0) {
                msg = "Select Payment Receipt Account";
            } else if ("HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName()) && trans.getGrandTotal() > 0 && trans.getAccChildAccountId() == 0) {
                msg = "Select Payment Receipt Account";
            } else if ("HIRE INVOICE".equals(transtype.getTransactionTypeName()) && trans.getSite_id() == 0) {
                msg = "Select Site";
            } else if ("HIRE DELIVERY NOTE".equals(transtype.getTransactionTypeName()) && new UtilityBean().countIntegers(trans.getTransactor_driver()) < 9) {
                msg = "Missing Driver Phone Number";
            } else if ("HIRE DELIVERY NOTE".equals(transtype.getTransactionTypeName()) && new UtilityBean().countIntegers(trans.getTransactor_rep()) < 9) {
                msg = "Missing Representative Phone Number";
            } else if ("HIRE RETURN NOTE".equals(transtype.getTransactionTypeName()) && new UtilityBean().countIntegers(trans.getTransactor_rep()) < 9) {
                msg = "Missing Representative Phone Number";
            } else if ("SALE ORDER".equals(transtype.getTransactionTypeName()) && trans.getTransactorId() == 0 && trans.getLocation_id() == 0) {
                msg = "Select Location or " + transtype.getTransactorLabel();
            } else if ("SALE ORDER".equals(transtype.getTransactionTypeName()) && trans.getStore2Id() == 0) {
                msg = "Select " + CompanySetting.getStoreEquivName() + " to send Order to";
            } else if (trans.getTransactionId() == 0 && !new AccLedgerBean().checkerBalancePass(trans.getPayMethod(), trans.getAccChildAccountId(), trans.getCurrencyCode(), trans.getAmountTendered(), transtype.getTransactionTypeId(), transreason.getTransactionReasonId(), 0, 0)) {
                msg = "Paying Account is out of Funds";
            } else if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) && trans.getAmountTendered() > trans.getGrandTotal()) {
                msg = "Paid Amount cannot Exceed Total";
            } else if (trans.getTransactionId() > 0 && trans.getAmountTendered() > trans.getGrandTotal()) {
                msg = "Paid Amount cannot Exceed New Total";
            } else if (trans.getTransactionId() > 0 && new TransItemBean().getAnyItemMixAddSubtractQty(new TransItemBean().getTransItemListCurLessPrevQty(aActiveTransItems, trans), transtype.getTransactionTypeName()) == 1) {
                msg = "You Cannot Add or Debit and Subtract or Credit different items in the same Update";
            } else if (MsgCkeckApproval.length() > 0) {
                msg = MsgCkeckApproval;
            } else if (trans.getTransactionId() == 0 && new Transaction_approvalBean().approvalRequiredTrans(trans, aTransTypeId, aTransReasonId) == 1) {
                msg = "Send this Transaction for Approval";
            } else if (vExcise.length() > 0) {
                msg = vExcise;
            }
            /*else if (trans.getTransactionId() > 0 && new TransItemBean().countItemsWithQtyChanged(new TransItemBean().getTransItemListCurLessPrevQty(aActiveTransItems, trans), transtype.getTransactionTypeName()) == 0) {
             msg = "Cannot Save where Item Qty has Not Changed";
             } */
        } catch (Exception e) {
            msg = "An Error has Occured During the Validation Process";
            //System.err.println("--:validateTransCEC:--" + e.getMessage());
            LOGGER.log(Level.ERROR, e);
        }
        return msg;
    }

    public void insertTransCEC(int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans trans, List<TransItem> aActiveTransItems) {
        long InsertedTransId = 0;
        String sql = "{call sp_insert_transaction(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                CallableStatement cs = conn.prepareCall(sql);) {
            TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
            TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
            Store store = new StoreBean().getStore(aStoreId);
            cs.setDate("in_transaction_date", new java.sql.Date(trans.getTransactionDate().getTime()));
            cs.setInt("in_store_id", store.getStoreId());
            trans.setStoreId(store.getStoreId());
            cs.setInt("in_store2_id", trans.getStore2Id());
            cs.setLong("in_transactor_id", trans.getTransactorId());
            trans.setTransactionTypeId(transtype.getTransactionTypeId());
            cs.setInt("in_transaction_type_id", trans.getTransactionTypeId());
            if (trans.getTransactionTypeId() == 3 || trans.getTransactionTypeId() == 4 || trans.getTransactionTypeId() == 13 || trans.getTransactionTypeId() == 7 || trans.getTransactionTypeId() == 16 || trans.getTransactionTypeId() == 19 || trans.getTransactionTypeId() == 71 || trans.getTransactionTypeId() == 72) {//DISPOSE STOCK, TRANFER & TRANS REQ & UNPACK & JOURNAL ENTRY & EXPENSE ENTRY,STOCK ADJUSTMENT,STOCK CONSUMPTION
                trans.setTransactionReasonId(transreason.getTransactionReasonId());
            }
            cs.setInt("in_transaction_reason_id", trans.getTransactionReasonId());
            cs.setDouble("in_cash_discount", trans.getCashDiscount());
            cs.setDouble("in_total_vat", trans.getTotalVat());
            cs.setString("in_transaction_comment", trans.getTransactionComment());
            cs.setInt("in_add_user_detail_id", new GeneralUserSetting().getCurrentUser().getUserDetailId());
            trans.setAddUserDetailId(new GeneralUserSetting().getCurrentUser().getUserDetailId());
            cs.setTimestamp("in_add_date", new java.sql.Timestamp(new java.util.Date().getTime()));
            cs.setInt("in_edit_user_detail_id", new GeneralUserSetting().getCurrentUser().getUserDetailId());//will be made null by the SP
            cs.setTimestamp("in_edit_date", new java.sql.Timestamp(new java.util.Date().getTime()));//will be made null by the SP
            cs.setString("in_transaction_ref", trans.getTransactionRef());
            cs.registerOutParameter("out_transaction_id", VARCHAR);
            cs.setDouble("in_sub_total", trans.getSubTotal());
            cs.setDouble("in_grand_total", trans.getGrandTotal());
            cs.setDouble("in_total_trade_discount", trans.getTotalTradeDiscount());
            cs.setDouble("in_points_awarded", trans.getPointsAwarded());
            cs.setString("in_card_number", trans.getCardNumber());
            cs.setDouble("in_total_std_vatable_amount", trans.getTotalStdVatableAmount());
            cs.setDouble("in_total_zero_vatable_amount", trans.getTotalZeroVatableAmount());
            cs.setDouble("in_total_exempt_vatable_amount", trans.getTotalExemptVatableAmount());
            cs.setDouble("in_vat_perc", CompanySetting.getVatPerc());
            cs.setDouble("in_amount_tendered", trans.getAmountTendered());
            cs.setDouble("in_change_amount", trans.getChangeAmount());
            cs.setString("in_is_cash_discount_vat_liable", CompanySetting.getIsCashDiscountVatLiable());
            //for profit margin
            cs.setDouble("in_total_profit_margin", trans.getTotalProfitMargin());
            try {
                if (trans.getTransactionUserDetailId() == 0) {
                    trans.setTransactionUserDetailId(new GeneralUserSetting().getCurrentUser().getUserDetailId());
                }
            } catch (NullPointerException npe) {
                trans.setTransactionUserDetailId(new GeneralUserSetting().getCurrentUser().getUserDetailId());
            }
            cs.setInt("in_transaction_user_detail_id", trans.getTransactionUserDetailId());
            try {
                if (trans.getBillTransactorId() == 0) {
                    trans.setBillTransactorId(trans.getTransactorId());
                }
            } catch (NullPointerException npe) {
                trans.setBillTransactorId(trans.getTransactorId());
            }
            cs.setLong("in_bill_transactor_id", trans.getBillTransactorId());
            try {
                cs.setLong("in_scheme_transactor_id", trans.getSchemeTransactorId());
            } catch (NullPointerException npe) {
                cs.setLong("in_scheme_transactor_id", 0);
            }
            try {
                cs.setString("in_princ_scheme_member", trans.getPrincSchemeMember());
            } catch (NullPointerException npe) {
                cs.setString("in_princ_scheme_member", "");
            }
            try {
                cs.setString("in_scheme_card_number", trans.getSchemeCardNumber());
            } catch (NullPointerException npe) {
                cs.setString("in_scheme_card_number", "");
            }
            try {
                if (trans.getTransactionNumber().length() == 0) {
                    String NewTransNo = new Trans_number_controlBean().getNewTransNumber(transtype);
                    int IsNewTransNoUsed = new Trans_number_controlBean().getIsTrans_number_used(transtype.getTransactionTypeId(), NewTransNo);
                    if (IsNewTransNoUsed == 0) {
                        trans.setTransactionNumber(NewTransNo);
                        cs.setString("in_transaction_number", trans.getTransactionNumber());
                        new Trans_number_controlBean().updateTrans_number_control(transtype);
                    } else {
                        trans.setTransactionNumber("");
                        cs.setString("in_transaction_number", trans.getTransactionNumber());
                    }
                } else {
                    cs.setString("in_transaction_number", trans.getTransactionNumber());
                }
            } catch (NullPointerException npe) {
                cs.setString("in_transaction_number", "");
            }
            try {
                cs.setDate("in_delivery_date", new java.sql.Date(trans.getDeliveryDate().getTime()));
            } catch (NullPointerException npe) {
                cs.setDate("in_delivery_date", null);
            }
            try {
                cs.setString("in_delivery_address", trans.getDeliveryAddress());
            } catch (NullPointerException npe) {
                cs.setString("in_delivery_address", "");
            }
            try {
                cs.setString("in_pay_terms", trans.getPayTerms());
            } catch (NullPointerException npe) {
                cs.setString("in_pay_terms", "");
            }
            try {
                cs.setString("in_terms_conditions", trans.getTermsConditions());
            } catch (NullPointerException npe) {
                cs.setString("in_terms_conditions", "");
            }
            try {
                cs.setInt("in_authorised_by_user_detail_id", trans.getAuthorisedByUserDetailId());
            } catch (NullPointerException npe) {
                cs.setInt("in_authorised_by_user_detail_id", 0);
            }
            try {
                cs.setDate("in_authorise_date", new java.sql.Date(trans.getAuthoriseDate().getTime()));
            } catch (NullPointerException npe) {
                cs.setDate("in_authorise_date", null);
            }
            try {
                cs.setDate("in_pay_due_date", new java.sql.Date(trans.getPayDueDate().getTime()));
            } catch (NullPointerException npe) {
                cs.setDate("in_pay_due_date", null);
            }
            try {
                cs.setDate("in_expiry_date", new java.sql.Date(trans.getExpiryDate().getTime()));
            } catch (NullPointerException npe) {
                cs.setDate("in_expiry_date", null);
            }
            try {
                cs.setInt("in_acc_child_account_id", trans.getAccChildAccountId());
            } catch (NullPointerException npe) {
                cs.setInt("in_acc_child_account_id", 0);
            }
            try {
                cs.setString("in_currency_code", trans.getCurrencyCode());
            } catch (NullPointerException npe) {
                cs.setString("in_currency_code", "");
            }
            try {
                AccCurrency LocalCurrency = null;
                LocalCurrency = new AccCurrencyBean().getLocalCurrency();
                trans.setXrate(new AccXrateBean().getXrate(trans.getCurrencyCode(), LocalCurrency.getCurrencyCode()));
            } catch (NullPointerException npe) {
                trans.setXrate(1);
            }
            cs.setDouble("in_xrate", trans.getXrate());
            try {
                cs.setDate("in_from_date", new java.sql.Date(trans.getFrom_date().getTime()));
            } catch (NullPointerException npe) {
                cs.setDate("in_from_date", null);
            }
            try {
                cs.setDate("in_to_date", new java.sql.Date(trans.getTo_date().getTime()));
            } catch (NullPointerException npe) {
                cs.setDate("in_to_date", null);
            }
            try {
                cs.setString("in_duration_type", trans.getDuration_type());
            } catch (NullPointerException npe) {
                cs.setString("in_duration_type", "");
            }
            try {
                cs.setLong("in_site_id", trans.getSite_id());
            } catch (NullPointerException npe) {
                cs.setLong("in_site_id", 0);
            }
            try {
                cs.setString("in_transactor_rep", trans.getTransactor_rep());
            } catch (NullPointerException npe) {
                cs.setString("in_transactor_rep", "");
            }
            try {
                cs.setString("in_transactor_vehicle", trans.getTransactor_vehicle());
            } catch (NullPointerException npe) {
                cs.setString("in_transactor_vehicle", "");
            }
            try {
                cs.setString("in_transactor_driver", trans.getTransactor_driver());
            } catch (NullPointerException npe) {
                cs.setString("in_transactor_driver", "");
            }
            try {
                cs.setDouble("in_duration_value", trans.getDuration_value());
            } catch (NullPointerException npe) {
                cs.setDouble("in_duration_value", 0);
            }
            //bought in after order module
            try {
                cs.setLong("in_location_id", trans.getLocation_id());
            } catch (NullPointerException npe) {
                cs.setLong("in_location_id", 0);
            }
            if (null == trans.getStatus_code()) {
                cs.setString("in_status_code", "");
            } else {
                cs.setString("in_status_code", trans.getStatus_code());
            }
            if (null == trans.getStatus_date()) {
                cs.setTimestamp("in_status_date", null);
            } else {
                cs.setTimestamp("in_status_date", new java.sql.Timestamp(trans.getStatus_date().getTime()));
            }
            if (null == trans.getDelivery_mode()) {
                cs.setString("in_delivery_mode", "");
            } else {
                cs.setString("in_delivery_mode", trans.getDelivery_mode());
            }
            try {
                cs.setInt("in_is_processed", trans.getIs_processed());
            } catch (NullPointerException npe) {
                cs.setInt("in_is_processed", 0);
            }
            try {
                cs.setInt("in_is_paid", trans.getIs_paid());
            } catch (NullPointerException npe) {
                cs.setInt("in_is_paid", 0);
            }
            try {
                cs.setInt("in_is_cancel", trans.getIs_cancel());
            } catch (NullPointerException npe) {
                cs.setInt("in_is_cancel", 0);
            }
            try {
                cs.setDouble("in_spent_points_amount", trans.getSpendPointsAmount());
            } catch (Exception e) {
                cs.setDouble("in_spent_points_amount", 0);
            }
            //save
            cs.executeUpdate();
            InsertedTransId = cs.getLong("out_transaction_id");
            trans.setTransactionId(InsertedTransId);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void saveTest(String aLevel, long aTransID) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        if (aLevel.equals("PARENT")) {
            httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransID);
            httpSession.setAttribute("CURRENT_PAY_ID", 0);
        } else {
            httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", aTransID);
            httpSession.setAttribute("CURRENT_PAY_ID_CHILD", 0);
        }
        new OutputDetailBean().refreshOutput(aLevel, "");
    }

    public void saveTransCallQuickOrder(String aAction, String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa, StatusBean aStatusBean) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        TransactionType transtype = null;
        try {
            transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
        } catch (Exception e) {

        }
        //reset messages
        this.ActionMessage = "";
        aStatusBean.setItemAddedStatus("");
        aStatusBean.setShowItemAddedStatus(0);
        aStatusBean.setItemNotAddedStatus("");
        aStatusBean.setShowItemNotAddedStatus(0);
        long SavedTransId = 0;
        int UserCodeNeeded = 0;
        int LocationNeeded = 0;
        try {
            UserCodeNeeded = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("ORDER", "USER_CODE_NEEDED").getParameter_value());
        } catch (Exception e) {
            //do nothing
        }
        try {
            LocationNeeded = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("ORDER", "LOCATION_NEEDED").getParameter_value());
        } catch (Exception e) {
            //do nothing
        }

        if (LocationNeeded == 1 && trans.getLocation_id() == 0) {
            this.ActionMessage = "Select Location";
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, this.ActionMessage)));
        } else if (UserCodeNeeded == 1 && trans.getUser_code().length() == 0) {
            this.ActionMessage = "Enter User Code";
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, this.ActionMessage)));
        } else {
            if (trans.getUser_code().length() > 0) {
                UserDetail ud = new UserDetailBean().getUserDetailWithTransCode(trans.getUser_code());
                if (null != ud) {
                    trans.setTransactionUserDetailId(ud.getUserDetailId());
                }
            } else {
                if (null != transtype && trans.getTransactionUserDetailId() == 0 && transtype.getIsTransactionUserMandatory().equals("No")) {
                    trans.setTransactionUserDetailId(new GeneralUserSetting().getCurrentUser().getUserDetailId());
                }
            }
            if (trans.getTransactionUserDetailId() == 0) {
                this.ActionMessage = "Select User or Staff";
                FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, this.ActionMessage)));
            } else if (trans.getDelivery_mode().length() == 0) {
                this.ActionMessage = "Specify Delivery Mode";
                FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, this.ActionMessage)));
            } else {
                UserDetail ud = new UserDetailBean().getUserDetail(trans.getTransactionUserDetailId());
                if (aAction.equals("Send")) {
                    trans.setIs_processed(0);
                } else if (aAction.equals("Process")) {
                    trans.setIs_processed(1);
                }
                //can first do something here...
                String CurrentDelivery_mode = trans.getDelivery_mode();
                long CurrentLocation_id = trans.getLocation_id();
                int CurrentStore2Id = trans.getStore2Id();
                //save
                int QualifyForSave = 1;
                if (trans.getTransactionId() > 0) {
                    QualifyForSave = 0;
                    int TransItemsUnitDeleted = new TransItemExtBean().deleteTransItemsUnitByTransId(trans.getTransactionId());
                    int TransItemsDeleted = new TransItemBean().deleteTransItemsCEC(trans.getTransactionId());
                    if (TransItemsDeleted == 1) {
                        new TransItemBean().resetTransactionItem(1, aActiveTransItems);
                        int TransDeleted = this.deleteTransCEC(trans.getTransactionId());
                        if (TransDeleted == 1) {
                            trans.setTransactionId(0);
                            QualifyForSave = 1;
                        }
                    }
                }

                if (QualifyForSave == 1) {
                    SavedTransId = this.saveTransCECE(aLevel, aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems, aSelectedTransactor, aSelectedBillTransactor, aTransUserDetail, aSelectedSchemeTransactor, aAuthorisedByUserDetail, aSelectedAccCoa);
                }
                //ensure previous selection is put back to trans
                trans.setDelivery_mode(CurrentDelivery_mode);
                trans.setLocation_id(CurrentLocation_id);
                trans.setStore2Id(CurrentStore2Id);
                if ((aAction.equals("Send") || aAction.equals("Process")) && QualifyForSave == 1 && this.AutoPrintAfterSave == true) {
                    org.primefaces.PrimeFaces.current().executeScript("doPrintHiddenClickOrder()");
                } else if (aAction.equals("Invoice") && QualifyForSave == 1) {
                    this.OutputNumber = 1;
                    long OrderId = new GeneralUserSetting().getCurrentTransactionId();
                    this.getOrderSalesInvoice(OrderId, ud);
                    this.openOrderParentSalesInvoice();
                } else if (aAction.equals("Pay") && QualifyForSave == 1) {
                    this.OutputNumber = 2;
                    Trans SavedOrderTrans = this.getTrans(SavedTransId);
                    if (null == SavedOrderTrans) {
                        //do nothing
                    } else {
                        if (SavedOrderTrans.getIs_invoiced() == 1) {
                            this.ActionMessage = "INVOICED Order cannot be PAID from here... use Cash Receipt window...";
                            FacesContext.getCurrentInstance().addMessage("Pay", new FacesMessage(this.ActionMessage));
                        } else if (SavedOrderTrans.getIs_cancel() == 1) {
                            this.ActionMessage = "Selected Order is already CANCELLED...";
                            FacesContext.getCurrentInstance().addMessage("Pay", new FacesMessage(this.ActionMessage));
                        } else if (SavedOrderTrans.getIs_paid() == 1) {
                            this.ActionMessage = "Selected Order is already PAID...";
                            FacesContext.getCurrentInstance().addMessage("Pay", new FacesMessage(this.ActionMessage));
                        } else if (SavedOrderTrans.getStoreId() != new GeneralUserSetting().getCurrentStore().getStoreId()) {
                            this.ActionMessage = "ORDER " + CompanySetting.getStoreEquivName() + " AND PAY " + CompanySetting.getStoreEquivName() + " MUST BE THE SAME...";
                            FacesContext.getCurrentInstance().addMessage("Pay", new FacesMessage(this.ActionMessage));
                        } else {
                            this.getOrderSalesInvoice(SavedOrderTrans.getTransactionId(), ud);
                            this.TransChild.setTransactionRef(SavedOrderTrans.getTransactionNumber());
                            this.TransChild.setChangeAmount(0);
                            this.TransChild.setPayMethod(1);
                            int ChildAccountId = 0;
                            try {
                                ChildAccountId = new AccChildAccountBean().getAccChildAccountsForCashReceipt(this.TransChild.getCurrencyCode(), 1, new GeneralUserSetting().getCurrentStore().getStoreId(), ud.getUserDetailId()).get(0).getAccChildAccountId();
                            } catch (Exception e) {
                                ChildAccountId = 0;
                            }
                            this.TransChild.setAccChildAccountId(ChildAccountId);
                            //check - save invoice with full payment
                            if (null == this.TransChild || this.ActiveTransItemsChild.size() <= 0) {
                                this.ActionMessage = "ORDER NOT PAID, please check details...";
                                FacesContext.getCurrentInstance().addMessage("Pay", new FacesMessage(this.ActionMessage));
                            } else if (ChildAccountId == 0) {
                                this.ActionMessage = "You do not have a CASH ACCOUNT to receive cash, please contact administrator...";
                                FacesContext.getCurrentInstance().addMessage("Pay", new FacesMessage(this.ActionMessage));
                            } else {
                                //save
                                this.setAutoPrintAfterSave(false);
                                long SavedInvoiceTransId = this.saveTransCECE("PARENT", new GeneralUserSetting().getCurrentStore().getStoreId(), 2, 2, "", this.TransChild, this.ActiveTransItemsChild, null, null, null, null, null, null);
                                if (SavedInvoiceTransId > 0) {
                                    //update is_invoiced
                                    Trans InvoiceTrans = this.getTrans(SavedInvoiceTransId);
                                    this.updateOrderIsInvoicedPaid(SavedOrderTrans.getTransactionId(), InvoiceTrans.getTransactionNumber(), null);
                                    org.primefaces.PrimeFaces.current().executeScript("doPrintHiddenClick()");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public double getOrderInvoiceQtyBalance(long aTransactorId, String aTransNo, TransItem aTransItem) {
        String sql = "";
        ResultSet rs = null;
        double InvoiceQtyBalance = 0;
        if (aTransactorId == 0 || aTransNo.length() == 0 || null == aTransItem) {
            //do nothing
        } else {
            sql = "SELECT  ti.item_id,tiu.unit_id,ti.item_qty,ti.specific_size,IFNULL(ti.batchno, '') as batchno,IFNULL(ti.code_specific, '') as code_specific,IFNULL(ti.desc_specific, '') as desc_specific, "
                    + "("
                    + "select IFNULL(sum(ti2.item_qty),0) as sum_qty from transaction_item ti2 "
                    + "INNER JOIN transaction t2 ON ti2.transaction_id=t2.transaction_id "
                    + "INNER JOIN transaction_item_unit tiu2 ON ti2.transaction_item_id=tiu2.transaction_item_id "
                    + "WHERE t2.transaction_type_id=2 AND ti2.item_id=ti.item_id AND tiu2.unit_id=tiu.unit_id AND t2.transaction_ref=t.transaction_number AND t2.transactor_id=t.transactor_id "
                    + "AND IFNULL(ti2.batchno, '')=IFNULL(ti.batchno, '') and IFNULL(ti2.code_specific, '')=IFNULL(ti.code_specific, '') and IFNULL(ti2.desc_specific, '')=IFNULL(ti.desc_specific, '') "
                    + ") as qty_invoiced "
                    + "FROM transaction_item ti "
                    + "INNER JOIN transaction t ON ti.transaction_id=t.transaction_id "
                    + "INNER JOIN transaction_item_unit tiu ON ti.transaction_item_id=tiu.transaction_item_id "
                    + "WHERE transaction_type_id=11 AND t.transactor_id=" + aTransactorId + " AND t.transaction_number='" + aTransNo + "' "
                    + "AND ti.item_id=" + aTransItem.getItemId() + " "
                    + "AND tiu.unit_id=" + aTransItem.getUnit_id() + " "
                    + "AND IFNULL(ti.batchno, '')='" + aTransItem.getBatchno() + "' and IFNULL(ti.code_specific, '')='" + aTransItem.getCodeSpecific() + "' and IFNULL(ti.desc_specific, '')='" + aTransItem.getDescSpecific() + "' ";
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                rs = ps.executeQuery();
                if (rs.next()) {
                    double QtyOrdered = 0;
                    double QtyInvoiced = 0;
                    try {
                        QtyOrdered = rs.getDouble("item_qty");
                    } catch (NullPointerException npe) {
                        QtyOrdered = 0;
                    }
                    try {
                        QtyInvoiced = rs.getDouble("qty_invoiced");
                    } catch (NullPointerException npe) {
                        QtyInvoiced = 0;
                    }
                    InvoiceQtyBalance = QtyOrdered - QtyInvoiced;
                }
            } catch (Exception e) {
                System.err.println("getOrderInvoiceQtyBalance:" + e.getMessage());
                LOGGER.log(Level.ERROR, e);
            }
        }
        return InvoiceQtyBalance;
    }

    public int getOrderInvoiceStatus(Trans aOrderTrans) {
        String sql = "";
        ResultSet rs = null;
        int ItemsForInvoice = 0;
        int ItemsFullyInvoiced = 0;
        int ItemsPartiallyInvoiced = 0;
        int ItemsNotInvoiced = 0;
        int InvoiceStatus = 0;
        if (null == aOrderTrans) {
            //do nothing
        } else {
            sql = "SELECT  ti.item_id,tiu.unit_id,ti.item_qty,ti.specific_size,IFNULL(ti.batchno, '') as batchno,IFNULL(ti.code_specific, '') as code_specific,IFNULL(ti.desc_specific, '') as desc_specific, "
                    + "("
                    + "select IFNULL(sum(ti2.item_qty),0) as sum_qty from transaction_item ti2 "
                    + "INNER JOIN transaction t2 ON ti2.transaction_id=t2.transaction_id "
                    + "INNER JOIN transaction_item_unit tiu2 ON ti2.transaction_item_id=tiu2.transaction_item_id "
                    + "WHERE t2.transaction_type_id=2 AND ti2.item_id=ti.item_id AND tiu2.unit_id=tiu.unit_id AND t2.transaction_ref=t.transaction_number AND t2.transactor_id=t.transactor_id "
                    + "AND IFNULL(ti2.batchno, '')=IFNULL(ti.batchno, '') and IFNULL(ti2.code_specific, '')=IFNULL(ti.code_specific, '') and IFNULL(ti2.desc_specific, '')=IFNULL(ti.desc_specific, '') "
                    + ") as qty_invoiced "
                    + "FROM transaction_item ti "
                    + "INNER JOIN transaction t ON ti.transaction_id=t.transaction_id "
                    + "INNER JOIN transaction_item_unit tiu ON ti.transaction_item_id=tiu.transaction_item_id "
                    + "WHERE transaction_type_id=11 AND t.transactor_id=" + aOrderTrans.getTransactorId() + " AND t.transaction_number='" + aOrderTrans.getTransactionNumber() + "'";
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                rs = ps.executeQuery();
                TransItem transitem = null;
                while (rs.next()) {
                    transitem = new TransItem();
                    double QtyOrdered = 0;
                    double QtyInvoiced = 0;
                    try {
                        transitem.setItemId(rs.getLong("item_id"));
                    } catch (NullPointerException npe) {
                        transitem.setItemId(0);
                    }
                    try {
                        QtyOrdered = rs.getDouble("item_qty");
                    } catch (NullPointerException npe) {
                        QtyOrdered = 0;
                    }
                    try {
                        QtyInvoiced = rs.getDouble("qty_invoiced");
                    } catch (NullPointerException npe) {
                        QtyInvoiced = 0;
                    }
                    Item item = new Item();
                    try {
                        item = new ItemBean().getItem(transitem.getItemId());
                    } catch (Exception e) {
                    }
                    ItemsForInvoice = ItemsForInvoice + 1;
                    if (QtyInvoiced >= QtyOrdered) {
                        ItemsFullyInvoiced = ItemsFullyInvoiced + 1;
                    } else if (QtyInvoiced < QtyOrdered && QtyInvoiced > 0) {
                        ItemsPartiallyInvoiced = ItemsPartiallyInvoiced + 1;
                    } else if (QtyInvoiced == 0) {
                        ItemsNotInvoiced = ItemsNotInvoiced + 1;
                    }
                }
                //InvoiceStatus
                if (ItemsPartiallyInvoiced > 0 || (ItemsNotInvoiced > 0 && ItemsFullyInvoiced > 0)) {
                    //status 2
                    InvoiceStatus = 2;
                } else if (ItemsFullyInvoiced == ItemsForInvoice) {
                    //status 1
                    InvoiceStatus = 1;
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
        return InvoiceStatus;
    }

    public int getOrderDeliveryStatus(Trans aOrderTrans) {
        String sql = "";
        ResultSet rs = null;
        int ItemsToBeDelivered = 0;
        int ItemsFullyDelivered = 0;
        int ItemsPartiallyDelivered = 0;
        int ItemsNotDelivered = 0;
        int DeliveryStatus = 0;
        if (null == aOrderTrans) {
            //do nothing
        } else {
            sql = "SELECT  ti.item_id,ti.item_qty,ti.specific_size,IFNULL(ti.batchno, '') as batchno,IFNULL(ti.code_specific, '') as code_specific,IFNULL(ti.desc_specific, '') as desc_specific, "
                    + "("
                    + "select IFNULL(sum(ti2.item_qty),0) as sum_qty from transaction_item ti2 INNER JOIN transaction t2 ON ti2.transaction_id=t2.transaction_id WHERE t2.transaction_type_id=12 AND ti2.item_id=ti.item_id AND t2.transaction_ref=t.transaction_number AND t2.transactor_id=t.transactor_id "
                    + "AND IFNULL(ti2.batchno, '')=IFNULL(ti.batchno, '') and IFNULL(ti2.code_specific, '')=IFNULL(ti.code_specific, '') and IFNULL(ti2.desc_specific, '')=IFNULL(ti.desc_specific, '') "
                    + ") as qty_delivered "
                    + "FROM transaction_item ti "
                    + "INNER JOIN transaction t ON ti.transaction_id=t.transaction_id "
                    + "WHERE transaction_type_id=11 AND t.transactor_id=" + aOrderTrans.getTransactorId() + " AND t.transaction_number='" + aOrderTrans.getTransactionNumber() + "'";
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                rs = ps.executeQuery();
                TransItem transitem = null;
                while (rs.next()) {
                    transitem = new TransItem();
                    double QtyOrdered = 0;
                    double QtyDelivered = 0;
                    try {
                        transitem.setItemId(rs.getLong("item_id"));
                    } catch (NullPointerException npe) {
                        transitem.setItemId(0);
                    }
                    try {
                        QtyOrdered = rs.getDouble("item_qty");
                    } catch (NullPointerException npe) {
                        QtyOrdered = 0;
                    }
                    try {
                        QtyDelivered = rs.getDouble("qty_delivered");
                    } catch (NullPointerException npe) {
                        QtyDelivered = 0;
                    }
                    Item item = new Item();
                    try {
                        item = new ItemBean().getItem(transitem.getItemId());
                    } catch (Exception e) {
                    }
                    if (item.getItemType().equals("PRODUCT")) {
                        ItemsToBeDelivered = ItemsToBeDelivered + 1;
                        if (QtyDelivered >= QtyOrdered) {
                            ItemsFullyDelivered = ItemsFullyDelivered + 1;
                        } else if (QtyDelivered < QtyOrdered && QtyDelivered > 0) {
                            ItemsPartiallyDelivered = ItemsPartiallyDelivered + 1;
                        } else if (QtyDelivered == 0) {
                            ItemsNotDelivered = ItemsNotDelivered + 1;
                        }
                    }
                }
                //DeliveryStatus
                if (ItemsPartiallyDelivered > 0 || (ItemsNotDelivered > 0 && ItemsFullyDelivered > 0)) {
                    //status 2
                    DeliveryStatus = 2;
                } else if (ItemsFullyDelivered == ItemsToBeDelivered) {
                    //status 1
                    DeliveryStatus = 1;
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
        return DeliveryStatus;
    }

    public void saveTransCECcallFromSI(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa) {
        //for safety reasons, double check the balance
        trans.setChangeAmount(this.getChangeAmount(trans));
        //get some details
        String OrderTransNo = trans.getTransactionRef();
        //save
        this.saveTransCEC(aLevel, aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems, aSelectedTransactor, aSelectedBillTransactor, aTransUserDetail, aSelectedSchemeTransactor, aAuthorisedByUserDetail, aSelectedAccCoa);
        //update a few things needed after sales invoice saving
        if (OrderTransNo.length() > 0) {
            Trans OrderTrans = this.getTransByNumberType(OrderTransNo, 11);
            //get order's invoioce status 0,1,2
            int InvoiceStatus = this.getOrderInvoiceStatus(OrderTrans);
            //get order's invoioce pay status 0,1,2
            //save invoice status if not 0
            if (InvoiceStatus > 0) {
                this.updateOrderStatus(OrderTrans.getTransactionId(), "is_invoiced", InvoiceStatus);
            }
            if (trans.getAmountTendered() >= trans.getGrandTotal()) {
                this.updateOrderStatus(OrderTrans.getTransactionId(), "is_paid", 1);
            } else if (trans.getAmountTendered() > 0 && trans.getAmountTendered() < trans.getGrandTotal()) {
                this.updateOrderStatus(OrderTrans.getTransactionId(), "is_paid", 2);
            }
        }
    }

    public void saveTransCECcallFromSINew(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa) {
        //for safety reasons, double check the balance
        trans.setChangeAmount(this.getChangeAmount(trans));
        //get some details
        String OrderTransNo = trans.getTransactionRef();

        String PackaginTransNo = trans.getTransactionNumber();
        trans.setTransactionNumber("");
        //save
        this.saveTransCECNew(aLevel, aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems, aSelectedTransactor, aSelectedBillTransactor, aTransUserDetail, aSelectedSchemeTransactor, aAuthorisedByUserDetail, aSelectedAccCoa);
        //update a few things needed after sales invoice saving
        if (OrderTransNo.length() > 0) {
            Trans OrderTrans = this.getTransByNumberType(OrderTransNo, 11);
            //get order's invoioce status 0,1,2
            int InvoiceStatus = this.getOrderInvoiceStatus(OrderTrans);
            //get order's invoioce pay status 0,1,2
            //save invoice status if not 0
            if (InvoiceStatus > 0) {
                this.updateOrderStatus(OrderTrans.getTransactionId(), "is_invoiced", InvoiceStatus);
            }
            if (trans.getAmountTendered() >= trans.getGrandTotal()) {
                this.updateOrderStatus(OrderTrans.getTransactionId(), "is_paid", 1);
            } else if (trans.getAmountTendered() > 0 && trans.getAmountTendered() < trans.getGrandTotal()) {
                this.updateOrderStatus(OrderTrans.getTransactionId(), "is_paid", 2);
            }
        }
        //added by david to update transaction package table after selling a package
        trans = this.getTransByTransNumber(PackaginTransNo);
        if (PackaginTransNo.startsWith("PCG")) {
            new TransactionPackageBean().updatePackageStatus(trans.getTransactionId(), 2, trans, null);//package sold
        }
        
        //Add success message to confirm sale was saved
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String transNo = (trans != null && trans.getTransactionNumber() != null) ? trans.getTransactionNumber() : "";
        String successMsg = ub.translateWordsInText(BaseName, "Sale Saved Successfully");
        if (transNo.length() > 0) {
            successMsg += " - " + transNo;
        }
        FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(FacesMessage.SEVERITY_INFO, successMsg, ""));
    }

    public void saveTransCECcallFromOpenBalance(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa, TransItem aTransItem) {
        int valid = new TransItemBean().validateOpenBalance(trans, aActiveTransItems, aTransItem, aSelectedAccCoa);
        if (valid == 1) {
            this.saveTransCEC(aLevel, aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems, aSelectedTransactor, aSelectedBillTransactor, aTransUserDetail, aSelectedSchemeTransactor, aAuthorisedByUserDetail, aSelectedAccCoa);
            if (null == aTransItem) {
                //do nothing
            } else {
                aTransItem.setAmount(0);
                aTransItem.setAmountExcVat(0);
                aTransItem.setAmountIncVat(0);
            }
        }
    }

    public void saveTransCECcallFromGDN(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa) {
        long aChoiceId = 99;
        int TransTypeId = 0;
        TransTypeId = (int) trans.getSite_id();
        if (TransTypeId == 0) {
            //do nothing
        } else {
            //get some details
            String XTransNo = trans.getTransactionRef();
            //save
            this.saveTransCEC(aLevel, aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems, aSelectedTransactor, aSelectedBillTransactor, aTransUserDetail, aSelectedSchemeTransactor, aAuthorisedByUserDetail, aSelectedAccCoa);
            //update a few things needed after GDN saving
            if (XTransNo.length() > 0 && TransTypeId > 0 && TransTypeId == 11) {
                Trans XTrans = this.getTransByNumberType(XTransNo, TransTypeId);
                //get order's GDN status 0,1,2
                int DeliveryStatus = this.getOrderDeliveryStatus(XTrans);
                //save GDN status if not 0
                if (DeliveryStatus > 0) {
                    this.updateOrderStatus(XTrans.getTransactionId(), "is_delivered", DeliveryStatus);
                }
            }
        }
    }

    public void saveTransCEC(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        //Date dt1 = null, dt2 = null;
        //long tms = 0;
        //long ms = 0;
        //String TimeStr = "";
        TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
        TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        Store store = new StoreBean().getStore(aStoreId);
        //dt1 = new Date();
        String ValidationMessage = this.validateTransCEC(aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems, aSelectedTransactor, aSelectedBillTransactor);
        //dt2 = new Date();
        //ms = (dt2.getTime() - dt1.getTime());
        //tms = tms + ms;
        //TimeStr = TimeStr + " Val:" + ms;
        long payid = 0;
        //-------
        String sql = null;
        String sql2 = null;

        TransItemBean TransItemBean = new TransItemBean();

        //first clear current session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        switch (aLevel) {
            case "PARENT":
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                httpSession.setAttribute("CURRENT_PAY_ID", 0);
                break;
            case "CHILD":
                httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", 0);
                httpSession.setAttribute("CURRENT_PAY_ID_CHILD", 0);
                break;
        }

        if (ValidationMessage.length() > 0) {
            switch (aLevel) {
                case "PARENT":
                    this.setActionMessage(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                    break;
                case "CHILD":
                    this.setActionMessageChild(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                    break;
            }
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, ValidationMessage)));
        } else {
            try {
                //dt1 = new Date();
                this.insertTransCEC(aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems);
                //dt2 = new Date();
                //ms = (dt2.getTime() - dt1.getTime());
                //tms = tms + ms;
                //TimeStr = TimeStr + " ITrans:" + ms;
                if (trans.getTransactionId() == 0) {
                    switch (aLevel) {
                        case "PARENT":
                            this.setActionMessage(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                            break;
                        case "CHILD":
                            this.setActionMessageChild(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                            break;
                    }
                    FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, "Error" + ", " + "Transaction Not Saved")));
                } else {
                    //set store2 for transfer in session!
                    switch (aLevel) {
                        case "PARENT":
                            httpSession.setAttribute("CURRENT_TRANSACTION_ID", trans.getTransactionId());
                            if ("TRANSFER".equals(transtype.getTransactionTypeName())) {
                                httpSession.setAttribute("CURRENT_STORE2_ID", trans.getStore2Id());
                            } else {
                                httpSession.setAttribute("CURRENT_STORE2_ID", 0);
                            }
                            break;
                        case "CHILD":
                            httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", trans.getTransactionId());
                            if ("TRANSFER".equals(transtype.getTransactionTypeName())) {
                                httpSession.setAttribute("CURRENT_STORE2_ID_CHILD", trans.getStore2Id());
                            } else {
                                httpSession.setAttribute("CURRENT_STORE2_ID_CHILD", 0);
                            }
                            break;
                    }
                    //save trans items
                    //trans.setStoreId(VARCHAR);
                    TransItemBean tib = new TransItemBean();
                    if (trans.getTransactionTypeId() == 16 || trans.getTransactionTypeId() == 76) {//Journal Entry, Opening Balance
                        tib.saveTransItemsJournalEntry(trans, aActiveTransItems, trans.getTransactionId());
                    } else if (trans.getTransactionTypeId() == 18) {//Cash Transfer
                        tib.saveTransItemsCashTransfer(trans, aActiveTransItems, trans.getTransactionId());
                    } else if (trans.getTransactionTypeId() == 75) {//Cash Adjustment
                        tib.saveTransItemsCashAdjustment(trans, aActiveTransItems, trans.getTransactionId());
                    } else {
                        tib.saveTransItemsCEC(aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems, trans.getTransactionId());
                    }

                    //save payment
                    //dt1 = new Date();
                    if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) || "EXPENSE ENTRY".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
                        Pay InnerPay = new Pay();
                        InnerPay.setPayDate(trans.getTransactionDate());
                        double paidamountt = 0;
                        if ((trans.getChangeAmount() > 0)) {
                            paidamountt = trans.getGrandTotal() - trans.getSpendPointsAmount();
                        } else {
                            paidamountt = trans.getAmountTendered();
                        }
                        InnerPay.setPaidAmount(paidamountt);
                        InnerPay.setPayMethodId(trans.getPayMethod());
                        InnerPay.setAddUserDetailId(new GeneralUserSetting().getCurrentUser().getUserDetailId());
                        InnerPay.setEditUserDetailId(new GeneralUserSetting().getCurrentUser().getUserDetailId());
                        InnerPay.setAddDate(new java.util.Date());
                        InnerPay.setEditDate(new java.util.Date());
                        InnerPay.setPointsSpent(trans.getSpendPoints());
                        InnerPay.setPointsSpentAmount(trans.getSpendPointsAmount());
                        InnerPay.setPayRefNo("");
                        InnerPay.setPay_number("");
                        if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
                            InnerPay.setPayCategory("IN");
                            InnerPay.setPayTypeId(14);//CASH RECEIPT
                            if (paidamountt > 0) {//SOME PAYMENT, thus CASH or PREPAID
                                if (trans.getPayMethod() == 6) {//PREPAID INCOME
                                    InnerPay.setPayReasonId(90);
                                } else {//CASH
                                    InnerPay.setPayReasonId(21);
                                }
                            } else {//Otherwise full/part credit sale
                                InnerPay.setPayReasonId(22);
                            }
                        } else if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) || "EXPENSE ENTRY".equals(transtype.getTransactionTypeName())) {
                            InnerPay.setPayCategory("OUT");
                            InnerPay.setPayTypeId(15);//CASH PAYMENT
                            if (paidamountt >= 0) {//SOME PAYMENT, thus CASH or PREPAID
                                if (trans.getPayMethod() == 7) {//PREPAID INCOME
                                    InnerPay.setPayReasonId(91);
                                } else {//CASH
                                    InnerPay.setPayReasonId(26);
                                }
                            } else {//Otherwise full/part credit purchase
                                InnerPay.setPayReasonId(25);
                            }
                        }
                        InnerPay.setBillTransactorId(trans.getBillTransactorId());
                        InnerPay.setStoreId(store.getStoreId());
                        try {
                            InnerPay.setAccChildAccountId(trans.getAccChildAccountId());
                        } catch (NullPointerException npe) {
                            InnerPay.setAccChildAccountId(0);
                        }
                        InnerPay.setCurrencyCode(trans.getCurrencyCode());
                        InnerPay.setXRate(trans.getXrate());
                        InnerPay.setStatus(1);
                        InnerPay.setStatusDesc("");
                        //define output
                        InnerPay.setDeletePayId(0);
                        payid = 0;
                        try {
                            payid = new PayBean().payInsertUpdate(InnerPay);
                        } catch (NullPointerException npe) {
                            payid = 0;
                        }
                        switch (aLevel) {
                            case "PARENT":
                                httpSession.setAttribute("CURRENT_PAY_ID", payid);
                                break;
                            case "CHILD":
                                httpSession.setAttribute("CURRENT_PAY_ID_CHILD", payid);
                                break;
                        }
                        //insert PayTrans
                        PayTrans paytrans = new PayTrans();
                        paytrans.setPayId(payid);
                        paytrans.setTransactionId(trans.getTransactionId());
                        paytrans.setTransPaidAmount(paidamountt);
                        if (trans.getTransactionNumber().length() > 0) {
                            paytrans.setTransactionNumber(trans.getTransactionNumber());
                        } else {
                            paytrans.setTransactionNumber(Long.toString(trans.getTransactionId()));
                        }
                        paytrans.setTransactionTypeId(trans.getTransactionTypeId());
                        paytrans.setTransactionReasonId(trans.getTransactionReasonId());
                        new PayTransBean().savePayTrans(paytrans);
                    }
                    //dt2 = new Date();
                    //ms = (dt2.getTime() - dt1.getTime());
                    //tms = tms + ms;
                    //TimeStr = TimeStr + " Pay:" + ms;
                    //insert approvals
                    if (("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName())) && trans.getCashDiscount() > 0 && new GeneralUserSetting().getIsApproveDiscountNeeded() == 1 && "APPROVED".equals(new GeneralUserSetting().getCurrentApproveDiscountStatus())) {
                        this.insertApproveTrans(new GeneralUserSetting().getCurrentTransactionId(), "DISCOUNT", new GeneralUserSetting().getCurrentApproveUserId());
                    }
                    if (("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName())) && trans.getSpendPointsAmount() > 0 && new GeneralUserSetting().getIsApprovePointsNeeded() == 1 && "APPROVED".equals(new GeneralUserSetting().getCurrentApprovePointsStatus())) {
                        this.insertApproveTrans(new GeneralUserSetting().getCurrentTransactionId(), "SPEND POINT", new GeneralUserSetting().getCurrentApproveUserId());
                    }

                    //Save Sales Journal Entry
                    //dt1 = new Date();
                    if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
                        Pay savedpay = null;
                        long savedpayid = 0;
                        try {
                            //savedpayid = new GeneralUserSetting().getCurrentPayId();
                            savedpayid = payid;
                        } catch (NullPointerException npe) {
                            savedpayid = 0;
                        }
                        if (savedpayid > 0) {
                            savedpay = new PayBean().getPay(savedpayid);
                        }
                        new AccJournalBean().postJournalSaleInvoice(trans, aActiveTransItems, savedpay, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //Save Purchase Invoice Journal Entry
                    long PostJobId = 0;
                    if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName())) {
                        Pay savedpay = null;
                        long savedpayid = 0;
                        try {
                            //savedpayid = new GeneralUserSetting().getCurrentPayId();
                            savedpayid = payid;
                        } catch (NullPointerException npe) {
                            savedpayid = 0;
                        }
                        if (savedpayid > 0) {
                            savedpay = new PayBean().getPay(savedpayid);
                        }
                        PostJobId = new AccJournalBean().postJournalPurchaseInvoice(trans, aActiveTransItems, savedpay, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //Save Asset Depreciation Schedule
                    if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) && transreason.getTransactionReasonId() == 29) {
                        //pay just for reference
                        Pay savedpay = new Pay();
                        long savedpayid = 0;
                        try {
                            //savedpayid = new GeneralUserSetting().getCurrentPayId();
                            savedpayid = payid;
                        } catch (NullPointerException npe) {
                            savedpayid = 0;
                        }
                        if (savedpayid > 0) {
                            savedpay = new PayBean().getPay(savedpayid);
                        }
                        Stock assetstock = null;
                        AccDepScheduleBean depschbean = new AccDepScheduleBean();
                        int assetstoreid = store.getStoreId();
                        for (TransItem assetti : aActiveTransItems) {
                            assetstock = new StockBean().getStock(assetstoreid, assetti.getItemId(), assetti.getBatchno(), assetti.getCodeSpecific(), assetti.getDescSpecific());
                            //Build:1-20-000-020 Land:1-20-000-010 but let us exclude LAND
                            if (null != assetstock && assetstock.getAccountCode().length() > 0 && !assetstock.getAccountCode().equals("1-20-000-010")) {
                                depschbean.insertAccDepSchedules(depschbean.calcAccDepSchedules(assetstock));
                                //post to the ledger the first year's depriciation
                                AccDepSchedule aAccDepSchedule = depschbean.getAccDepScheduleByYear(assetstock.getStockId(), 1);
                                AccPeriod accprd4firstpost = null;
                                AccPeriod accprd4trans = null;
                                try {
                                    accprd4firstpost = new AccPeriodBean().getAccPeriod(assetstock.getDepStartDate());
                                } catch (Exception e) {
                                    accprd4firstpost = null;
                                }
                                try {
                                    accprd4trans = new AccPeriodBean().getAccPeriod(trans.getTransactionDate());
                                } catch (Exception e) {
                                    accprd4trans = null;
                                }
                                if (null == accprd4firstpost || null == accprd4trans) {
                                    //do nothing; means;
                                    //dep start date is for not yet set acc period OR
                                    //current date doesnt have correspondiong acc period 
                                } else {
                                    if (accprd4firstpost.getAccPeriodId() == accprd4trans.getAccPeriodId()) {
                                        new AccJournalBean().postJournalDepreciateAsset(trans, assetstock, aAccDepSchedule, accprd4firstpost.getAccPeriodId(), PostJobId);
                                        aAccDepSchedule.setDepForAccPeriodId(accprd4firstpost.getAccPeriodId());
                                        aAccDepSchedule.setDepFromDate(accprd4firstpost.getStartDate());
                                        aAccDepSchedule.setDepToDate(accprd4firstpost.getEndDate());
                                        aAccDepSchedule.setPost_status(1);
                                        new AccDepScheduleBean().updateAccDepSchedule(aAccDepSchedule);
                                    }
                                }
                            }
                        }
                    }
                    //Save Dispose Stock Journal Entry
                    if ("DISPOSE STOCK".equals(transtype.getTransactionTypeName())) {
                        new AccJournalBean().postJournalDisposeStock(trans, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //Save Journal Entry - Journal Entry
                    if ("JOURNAL ENTRY".equals(transtype.getTransactionTypeName())) {
                        new AccJournalBean().postJournalJournalEntry(trans, aActiveTransItems, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //Save Journal Entry - Cash Transfer
                    if ("CASH TRANSFER".equals(transtype.getTransactionTypeName())) {
                        new AccJournalBean().postJournalCashTransfer(trans, aActiveTransItems, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //Save Journal Entry - Cash Adjustment
                    if ("CASH ADJUSTMENT".equals(transtype.getTransactionTypeName())) {
                        new AccJournalBean().postJournalCashAdjustment(trans, aActiveTransItems, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //Save Expense Journal Entry
                    if ("EXPENSE ENTRY".equals(transtype.getTransactionTypeName())) {
                        Pay savedpay = null;
                        long savedpayid = 0;
                        try {
                            //savedpayid = new GeneralUserSetting().getCurrentPayId();
                            savedpayid = payid;
                        } catch (NullPointerException npe) {
                            savedpayid = 0;
                        }
                        if (savedpayid > 0) {
                            savedpay = new PayBean().getPay(savedpayid);
                        }
                        new AccJournalBean().postJournalExpenseEntry(trans, aActiveTransItems, savedpay, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //Save Opening Balance - Journal Entry
                    if ("OPENING BALANCE".equals(transtype.getTransactionTypeName())) {
                        new AccJournalBean().postJournalOpenBalance(trans, aActiveTransItems, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //Save STOCK CONSUMPTION Journal Entry
                    if ("STOCK CONSUMPTION".equals(transtype.getTransactionTypeName())) {
                        new AccJournalBean().postJournalStockConsumption(trans, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //delete if any draft trans was used
                    if (trans.getTransactionHistId() > 0) {
                        this.deleteTransFromHist(trans.getTransactionHistId());
                    }
                    //dt2 = new Date();
                    //ms = (dt2.getTime() - dt1.getTime());
                    //tms = tms + ms;
                    //TimeStr = TimeStr + " SJournal:" + ms;
                    //TAX API
                    //dt1 = new Date();
                    if (aTransTypeId == 2 && new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value().length() > 0 && new Item_tax_mapBean().countItemsNotMappedSynced(aActiveTransItems) == 0) {//SALES INVOICE
                        int IsThreadOn = 0;
                        try {
                            IsThreadOn = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_TAX_THREAD_ON").getParameter_value());
                        } catch (Exception e) {
                            //
                        }
                        if (IsThreadOn == 0) {
                            new InvoiceBean().submitTaxInvoice(trans.getTransactionId());
                        } else if (IsThreadOn == 1) {
                            new InvoiceBean().submitTaxInvoiceThread(trans.getTransactionId());
                        }
                    }
                    //dt2 = new Date();
                    //ms = (dt2.getTime() - dt1.getTime());
                    //tms = tms + ms;
                    //TimeStr = TimeStr + " TaxAPI:" + ms;
                    //Update Total Paid for Sales/Purchase Invoice
                    //dt1 = new Date();
                    if (aTransTypeId == 2 || aTransTypeId == 1) {
                        new PayTransBean().updateTransTotalPaid(trans.getTransactionId());
                    }
                    //dt2 = new Date();
                    //ms = (dt2.getTime() - dt1.getTime());
                    //tms = tms + ms;
                    //TimeStr = TimeStr + " UPaid:" + ms;
                    //clear
                    //dt1 = new Date();
                    this.clearAll2(trans, aActiveTransItems, null, null, aSelectedTransactor, 2, aSelectedBillTransactor, aTransUserDetail, aSelectedSchemeTransactor, aAuthorisedByUserDetail, aSelectedAccCoa);
                    //dt2 = new Date();
                    //ms = (dt2.getTime() - dt1.getTime());
                    //tms = tms + ms;
                    //TimeStr = TimeStr + " Clear2:" + ms;

                    TransItemBean = null;

                    //clean stock
                    //dt2 = new Date();
                    StockBean.deleteZeroQtyStock();
                    //dt2 = new Date();
                    //ms = (dt2.getTime() - dt1.getTime());
                    //tms = tms + ms;
                    //TimeStr = TimeStr + " deleteZeroStock:" + ms;
                    switch (aLevel) {
                        case "PARENT":
                            this.setActionMessage(ub.translateWordsInText(BaseName, "Saved Successfully Transaction Id " + new GeneralUserSetting().getCurrentTransactionId()));
                            break;
                        case "CHILD":
                            this.setActionMessageChild(ub.translateWordsInText(BaseName, "Saved Successfully Transaction Id  " + new GeneralUserSetting().getCurrentTransactionIdChild()));
                            break;
                    }

                    //Refresh Print output
                    //dt1 = new Date();
                    new OutputDetailBean().refreshOutput(aLevel, "");
                    //dt2 = new Date();
                    //ms = (dt2.getTime() - dt1.getTime());
                    //tms = tms + ms;
                    //TimeStr = TimeStr + " ROutput:" + ms;
                    //refresh draft
                    //dt1 = new Date();
                    if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName())) {
                        this.refreshTranssDraft(aStoreId, new GeneralUserSetting().getCurrentUser().getUserDetailId(), aTransTypeId, aTransReasonId);
                    }
                    //dt2 = new Date();
                    //ms = (dt2.getTime() - dt1.getTime());
                    //tms = tms + ms;
                    //TimeStr = TimeStr + " RDraft:" + ms;
                    //Auto Printing Invoice
                    //dt1 = new Date();
                    if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "TRANSFER".equals(transtype.getTransactionTypeName())) {
                        //1. Update Invoice
                        //---SalesInvoiceBean.initSalesInvoiceBean();
                        //2. Auto Printing Invoice
                        if (this.AutoPrintAfterSave) {
                            org.primefaces.PrimeFaces.current().executeScript("doPrintHiddenClick()");
                        }
                    }
                    //dt2 = new Date();
                    //ms = (dt2.getTime() - dt1.getTime());
                    //tms = tms + ms;
                    //TimeStr = TimeStr + " APrint:" + ms;
                    //check need for child dialogue
                    if ("HIRE RETURN NOTE".equals(transtype.getTransactionTypeName())) {
                        //find out if there is a return invoice candidate
                        if (this.isReturnNoteForInvoice(new GeneralUserSetting().getCurrentTransactionId()) == 1) {
                            this.openChildReturnHireInvoice(new GeneralUserSetting().getCurrentTransactionId());
                        }
                    }
                    //Refresh stock alerts
                    //dt1 = new Date();
                    new UtilityBean().refreshAlertsThread();
                    //dt2 = new Date();
                    //ms = (dt2.getTime() - dt1.getTime());
                    //tms = tms + ms;
                    //TimeStr = TimeStr + " RAlerts:" + ms;
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
                switch (aLevel) {
                    case "PARENT":
                        this.setActionMessage(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                        break;
                    case "CHILD":
                        this.setActionMessageChild(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                        break;
                }
                FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, "Transaction Not Saved") + ", " + ub.translateWordsInText(BaseName, "Ensure Transaction Reference Number is Not Captured Already")));
            }
        }
    }

    public void saveTransCECNew(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        int InsertedTransItems = 0;
        double CheckValueBfr = 0;
        double CheckValueAfr = 0;
        int DeleteInserted = 0;
        CheckValueBfr = this.checkTrans(0, trans, aActiveTransItems);
        TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
        TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        //Store store = new StoreBean().getStore(aStoreId);
        String ValidationMessage = this.validateTransCEC(aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems, aSelectedTransactor, aSelectedBillTransactor);
        long payid = 0;
        //-------
        String sql = null;
        String sql2 = null;

        TransItemBean TransItemBean = new TransItemBean();

        //first clear current session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        switch (aLevel) {
            case "PARENT":
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                httpSession.setAttribute("CURRENT_PAY_ID", 0);
                break;
            case "CHILD":
                httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", 0);
                httpSession.setAttribute("CURRENT_PAY_ID_CHILD", 0);
                break;
        }

        if (ValidationMessage.length() > 0) {
            switch (aLevel) {
                case "PARENT":
                    msg = "Transaction NOT saved";
                    //this.setActionMessage("Transaction NOT saved");
                    this.setActionMessage(ub.translateWordsInText(BaseName, msg));
                    break;
                case "CHILD":
                    //this.setActionMessageChild("Transaction NOT saved");
                    msg = "Transaction Not Saved";
                    this.setActionMessageChild(ub.translateWordsInText(BaseName, msg));
                    break;
            }
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, ValidationMessage)));
        } else {
            try {
                this.insertTransCEC(aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems);
                if (trans.getTransactionId() == 0) {
                    switch (aLevel) {
                        case "PARENT":
                            msg = "Transaction Not Saved";
                            this.setActionMessage(ub.translateWordsInText(BaseName, msg));
                            break;
                        case "CHILD":
                            msg = "Transaction Not Saved";
                            this.setActionMessageChild(ub.translateWordsInText(BaseName, msg));
                            break;
                    }
                    msg = "Transaction Not Saved due to Error";
                    FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
                } else {
                    //set store2 for transfer in session!
                    switch (aLevel) {
                        case "PARENT":
                            httpSession.setAttribute("CURRENT_TRANSACTION_ID", trans.getTransactionId());
                            if ("TRANSFER".equals(transtype.getTransactionTypeName())) {
                                httpSession.setAttribute("CURRENT_STORE2_ID", trans.getStore2Id());
                            } else {
                                httpSession.setAttribute("CURRENT_STORE2_ID", 0);
                            }
                            break;
                        case "CHILD":
                            httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", trans.getTransactionId());
                            if ("TRANSFER".equals(transtype.getTransactionTypeName())) {
                                httpSession.setAttribute("CURRENT_STORE2_ID_CHILD", trans.getStore2Id());
                            } else {
                                httpSession.setAttribute("CURRENT_STORE2_ID_CHILD", 0);
                            }
                            break;
                    }
                    //save trans items
                    //trans.setStoreId(VARCHAR);
                    TransItemBean tib = new TransItemBean();
                    if (trans.getTransactionTypeId() == 16 || trans.getTransactionTypeId() == 76) {//Journal Entry, Opening Balance
                        tib.saveTransItemsJournalEntry(trans, aActiveTransItems, trans.getTransactionId());
                    } else if (trans.getTransactionTypeId() == 18) {//Cash Transfer
                        tib.saveTransItemsCashTransfer(trans, aActiveTransItems, trans.getTransactionId());
                    } else if (trans.getTransactionTypeId() == 75) {//Cash Adjustment
                        tib.saveTransItemsCashAdjustment(trans, aActiveTransItems, trans.getTransactionId());
                    } else {
                        //tib.saveTransItemsCEC(aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems, trans.getTransactionId());
                        InsertedTransItems = tib.insertTransItems(aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems, trans.getTransactionId());
                        CheckValueAfr = this.checkTrans(trans.getTransactionId(), null, null);
                        if (CheckValueBfr == CheckValueAfr) {
                            DeleteInserted = 0;
                        } else {
                            DeleteInserted = 1;
                        }
                        if (DeleteInserted == 0 && InsertedTransItems == aActiveTransItems.size()) {
                            //insert transaction taxes summary
                            if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) && trans.getTotalExciseDutyTaxAmount() > 0) {
                                trans.setVatPerc(CompanySetting.getVatPerc());
                                int ed = new TransExtBean().insertTransTaxes(trans, aActiveTransItems);
                            }
                            //SMbi API insert PointsTransaction for both the awarded and spent points to the stage area
                            String scope = new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_SMBI_SCOPE").getParameter_value();
                            if (scope.isEmpty() || scope.contains("LOYALTY")) {
                                if (("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName())) && (trans.getPointsAwarded() != 0 || trans.getSpendPoints() != 0)) {
                                    if (!trans.getCardNumber().equals("") && !trans.getCardHolder().equals("")) {
                                        int x = new Loyalty_transactionBean().insertLoyalty_transaction(trans);
                                    }
                                }
                            }
                            //pay, stock, journal
                            this.saveTransOthersThread(trans.getTransactionId(), trans.getPayMethod());
                        }
                    }
                    if (DeleteInserted == 1) {
                        //delete inserted
                        int deleted1 = new TransItemExtBean().deleteTransItemExciseByTransId(trans.getTransactionId());
                        int deleted2 = 0;
                        int deleted3 = 0;
                        int deleted4 = 0;
                        if (deleted1 == 1) {
                            deleted2 = new TransItemExtBean().deleteTransItemsUnitByTransId(trans.getTransactionId());
                        }
                        if (deleted2 == 1) {
                            deleted3 = new TransItemBean().deleteTransItemsCEC(trans.getTransactionId());
                        }
                        if (deleted3 == 1) {
                            deleted4 = this.deleteTransCEC(trans.getTransactionId());
                        }
                        //display msg
                        switch (aLevel) {
                            case "PARENT":
                                httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                                httpSession.setAttribute("CURRENT_PAY_ID", 0);
                                this.setActionMessage("Transaction Not Saved");
                                break;
                            case "CHILD":
                                httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", 0);
                                httpSession.setAttribute("CURRENT_PAY_ID_CHILD", 0);
                                this.setActionMessageChild("Transaction Not Saved");
                                break;
                        }
                        msg = "Transaction Not Saved";
                        FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
                    } else {
                        //insert approvals
                        if (("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName())) && trans.getCashDiscount() > 0 && new GeneralUserSetting().getIsApproveDiscountNeeded() == 1 && "APPROVED".equals(new GeneralUserSetting().getCurrentApproveDiscountStatus())) {
                            this.insertApproveTrans(new GeneralUserSetting().getCurrentTransactionId(), "DISCOUNT", new GeneralUserSetting().getCurrentApproveUserId());
                        }
                        if (("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName())) && trans.getSpendPointsAmount() > 0 && new GeneralUserSetting().getIsApprovePointsNeeded() == 1 && "APPROVED".equals(new GeneralUserSetting().getCurrentApprovePointsStatus())) {
                            this.insertApproveTrans(new GeneralUserSetting().getCurrentTransactionId(), "SPEND POINT", new GeneralUserSetting().getCurrentApproveUserId());
                        }
                        //delete if any draft trans was used and refresh
                        if (trans.getTransactionHistId() > 0) {
                            this.deleteTransFromHist(trans.getTransactionHistId());
                            this.refreshTranssDraft(aStoreId, new GeneralUserSetting().getCurrentUser().getUserDetailId(), aTransTypeId, aTransReasonId);
                        }
                        //update status of the approval and refresh
                        if (trans.getTransaction_approval_id() > 0) {
                            //mark processed
                            new Transaction_approvalBean().markProcessed(trans.getTransaction_approval_id(), trans.getTransactionId());
                            //refresh list
                            new Transaction_approvalBean().refreshTransaction_approvalList(this.TransListApproval, new GeneralUserSetting().getCurrentStore().getStoreId(), new GeneralUserSetting().getCurrentUser().getUserDetailId(), new GeneralUserSetting().getCurrentTransactionTypeId(), new GeneralUserSetting().getCurrentTransactionReasonId());
                        }
                        //TAX API
                        if (aTransTypeId == 2 && new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value().length() > 0 && new Item_tax_mapBean().countItemsNotMappedSynced(aActiveTransItems) == 0) {//SALES INVOICE
                            int IsThreadOn = 0;
                            try {
                                IsThreadOn = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_TAX_THREAD_ON").getParameter_value());
                            } catch (Exception e) {
                                //
                            }
                            if (IsThreadOn == 0) {
                                new InvoiceBean().submitTaxInvoice(trans.getTransactionId());
                            } else if (IsThreadOn == 1) {
                                new InvoiceBean().submitTaxInvoiceThread(trans.getTransactionId());
                            }
                        }
                        //SMbi API Transactions
                        if (new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_SMBI_URL").getParameter_value().length() > 0) {
                            new Transaction_smbi_mapBean().insertTransaction_smbi_mapCallThread(trans.getTransactionId(), trans.getTransactionTypeId());
                        }
                        //Insert Work Shift
                        if (new GeneralUserSetting().getCurrentStore().getShift_mode() > 0 && trans.getTransactionTypeId() == 2) {
                            //1. define shift
                            //2. invoke save
                        }
                        //clear
                        this.clearAll2(trans, aActiveTransItems, null, null, aSelectedTransactor, 2, aSelectedBillTransactor, aTransUserDetail, aSelectedSchemeTransactor, aAuthorisedByUserDetail, aSelectedAccCoa);
                        TransItemBean = null;
                        switch (aLevel) {
                            case "PARENT":
                                msg = "Saved Successfully (Transaction Id: " + new GeneralUserSetting().getCurrentTransactionId() + ")";
                                this.setActionMessage(ub.translateWordsInText(BaseName, msg));
                                break;
                            case "CHILD":
                                msg = "Saved Successfully (Transaction Id : " + new GeneralUserSetting().getCurrentTransactionIdChild() + ")";
                                this.setActionMessageChild(ub.translateWordsInText(BaseName, msg));
                                break;
                        }
                        //Refresh Print output
                        new OutputDetailBean().refreshOutput(aLevel, "");
                        //refresh draft
                        /*
                         if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName())) {
                         this.refreshTranssDraft(aStoreId, new GeneralUserSetting().getCurrentUser().getUserDetailId(), aTransTypeId, aTransReasonId);
                         }
                         */
                        //Auto Printing Invoice
                        if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName())) {
                            //1. Update Invoice
                            //2. Auto Printing Invoice
                            if (this.AutoPrintAfterSave) {
                                try {
                                    org.primefaces.PrimeFaces.current().executeScript("doPrintHiddenClick()");
                                } catch (Exception e) {
                                }
                            }
                        }
                        //check need for child dialogue
                        if ("HIRE RETURN NOTE".equals(transtype.getTransactionTypeName())) {
                            //find out if there is a return invoice candidate
                            if (this.isReturnNoteForInvoice(new GeneralUserSetting().getCurrentTransactionId()) == 1) {
                                this.openChildReturnHireInvoice(new GeneralUserSetting().getCurrentTransactionId());
                            }
                        }
                        //Refresh stock alerts
                        new UtilityBean().refreshAlertsThread();
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
                switch (aLevel) {
                    case "PARENT":
                        this.setActionMessage(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                        break;
                    case "CHILD":
                        this.setActionMessageChild(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                        break;
                }
                FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, "Transaction NOT saved! Double check details, ensure transaction ref numbers have not been captured already")));
            }
        }
    }

    public double checkTrans(long aTransactionId, Trans aTrans, List<TransItem> aTransItems) {
        double value = 0;
        int CountItems = 0;
        double CountQty = 0;
        try {
            if (aTransactionId == 0) {
                CountItems = aTransItems.size();
                CountQty = new TransItemBean().getTransItemsTotalQty(aTransItems);
            } else if (aTransactionId > 0) {
                List<TransItem> tis = new TransItemBean().getTransItemsByTransactionId(aTransactionId);
                CountItems = tis.size();
                CountQty = new TransItemBean().getTransItemsTotalQty(tis);
                //value = new UtilityBean().getD("SELECT (count(*)+sum(item_qty)) as d FROM transaction_item ti WHERE ti.transaction_id=" + aTransactionId);
            }
            value = CountQty + CountItems;
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return value;
    }

    public void saveTransOthersThread(long aTransactionId, int aPayMethodId) {
        try {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    long payid = 0;
                    Trans t = new TransBean().getTrans(aTransactionId);
                    t.setPayMethod(aPayMethodId);
                    List<TransItem> tis = new TransItemBean().getTransItemsByTransactionId(t.getTransactionId());
                    TransactionType tt = new TransactionTypeBean().getTransactionType(t.getTransactionTypeId());
                    TransactionReason tr = new TransactionReasonBean().getTransactionReason(t.getTransactionReasonId());
                    try {
                        if (tt.getTransactionTypeId() == 2) {
                            Transaction_tax ttax = new TransExtBean().getTransTaxByCategory(t.getTransactionId(), "Excise Duty");
                            if (null != ttax) {
                                t.setTotalExciseDutyTaxAmount(ttax.getTax_amount());
                                t.setTotalExciseDutableAmount(ttax.getTaxable_amount());
                            }
                        }
                    } catch (Exception e) {
                    }
                    try {
                        if (tt.getTransactionTypeId() == 2) {
                            new TransItemExtBean().setTransaction_item_exciseListByTransItem(tis);
                        }
                    } catch (Exception e) {
                    }
                    try {
                        new TransItemBean().adjustStockForTransItems(t, tis);
                    } catch (Exception e) {
                        //do nothing
                    }
                    payid = savePayForTrans(t, 0);
                    postJournalsForTrans(tt, tr, t, tis, payid);
                    if (tt.getTransactionTypeId() == 2 || tt.getTransactionTypeId() == 1) {
                        new PayTransBean().updateTransTotalPaid(t.getTransactionId());
                    }
                    StockBean.deleteZeroQtyStock();
                }
            };
            Executor e = Executors.newSingleThreadExecutor();
            e.execute(task);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void postJournalsForTrans(TransactionType transtype, TransactionReason transreason, Trans trans, List<TransItem> aActiveTransItems, long payid) {
        //Save Sales Journal Entry
        //dt1 = new Date();
        if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
            Pay savedpay = null;
            long savedpayid = 0;
            try {
                //savedpayid = new GeneralUserSetting().getCurrentPayId();
                savedpayid = payid;
            } catch (NullPointerException npe) {
                savedpayid = 0;
            }
            if (savedpayid > 0) {
                savedpay = new PayBean().getPay(savedpayid);
            }
            new AccJournalBean().postJournalSaleInvoice(trans, aActiveTransItems, savedpay, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
        }
        //Save Purchase Invoice Journal Entry
        long PostJobId = 0;
        if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName())) {
            Pay savedpay = null;
            long savedpayid = 0;
            try {
                //savedpayid = new GeneralUserSetting().getCurrentPayId();
                savedpayid = payid;
            } catch (NullPointerException npe) {
                savedpayid = 0;
            }
            if (savedpayid > 0) {
                savedpay = new PayBean().getPay(savedpayid);
            }
            PostJobId = new AccJournalBean().postJournalPurchaseInvoice(trans, aActiveTransItems, savedpay, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
        }
        //Save Asset Depreciation Schedule
        if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) && transreason.getTransactionReasonId() == 29) {
            //pay just for reference
            Pay savedpay = new Pay();
            long savedpayid = 0;
            try {
                //savedpayid = new GeneralUserSetting().getCurrentPayId();
                savedpayid = payid;
            } catch (NullPointerException npe) {
                savedpayid = 0;
            }
            if (savedpayid > 0) {
                savedpay = new PayBean().getPay(savedpayid);
            }
            Stock assetstock = null;
            AccDepScheduleBean depschbean = new AccDepScheduleBean();
            int assetstoreid = trans.getStoreId();
            for (TransItem assetti : aActiveTransItems) {
                assetstock = new StockBean().getStock(assetstoreid, assetti.getItemId(), assetti.getBatchno(), assetti.getCodeSpecific(), assetti.getDescSpecific());
                //Build:1-20-000-020 Land:1-20-000-010 but let us exclude LAND
                if (null != assetstock && assetstock.getAccountCode().length() > 0 && !assetstock.getAccountCode().equals("1-20-000-010")) {
                    depschbean.insertAccDepSchedules(depschbean.calcAccDepSchedules(assetstock));
                    //post to the ledger the first year's depriciation
                    AccDepSchedule aAccDepSchedule = depschbean.getAccDepScheduleByYear(assetstock.getStockId(), 1);
                    AccPeriod accprd4firstpost = null;
                    AccPeriod accprd4trans = null;
                    try {
                        accprd4firstpost = new AccPeriodBean().getAccPeriod(assetstock.getDepStartDate());
                    } catch (Exception e) {
                        accprd4firstpost = null;
                    }
                    try {
                        accprd4trans = new AccPeriodBean().getAccPeriod(trans.getTransactionDate());
                    } catch (Exception e) {
                        accprd4trans = null;
                    }
                    if (null == accprd4firstpost || null == accprd4trans) {
                        //do nothing; means;
                        //dep start date is for not yet set acc period OR
                        //current date doesnt have correspondiong acc period 
                    } else {
                        if (accprd4firstpost.getAccPeriodId() == accprd4trans.getAccPeriodId()) {
                            new AccJournalBean().postJournalDepreciateAsset(trans, assetstock, aAccDepSchedule, accprd4firstpost.getAccPeriodId(), PostJobId);
                            aAccDepSchedule.setDepForAccPeriodId(accprd4firstpost.getAccPeriodId());
                            aAccDepSchedule.setDepFromDate(accprd4firstpost.getStartDate());
                            aAccDepSchedule.setDepToDate(accprd4firstpost.getEndDate());
                            aAccDepSchedule.setPost_status(1);
                            new AccDepScheduleBean().updateAccDepSchedule(aAccDepSchedule);
                        }
                    }
                }
            }
        }
        //Save Dispose Stock Journal Entry
        if ("DISPOSE STOCK".equals(transtype.getTransactionTypeName())) {
            new AccJournalBean().postJournalDisposeStock(trans, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
        }
        //Save Journal Entry - Journal Entry
        if ("JOURNAL ENTRY".equals(transtype.getTransactionTypeName())) {
            new AccJournalBean().postJournalJournalEntry(trans, aActiveTransItems, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
        }
        //Save Journal Entry - Cash Transfer
        if ("CASH TRANSFER".equals(transtype.getTransactionTypeName())) {
            new AccJournalBean().postJournalCashTransfer(trans, aActiveTransItems, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
        }
        //Save Journal Entry - Cash Adjustment
        if ("CASH ADJUSTMENT".equals(transtype.getTransactionTypeName())) {
            new AccJournalBean().postJournalCashAdjustment(trans, aActiveTransItems, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
        }
        //Save Expense Journal Entry
        if ("EXPENSE ENTRY".equals(transtype.getTransactionTypeName())) {
            Pay savedpay = null;
            long savedpayid = 0;
            try {
                //savedpayid = new GeneralUserSetting().getCurrentPayId();
                savedpayid = payid;
            } catch (NullPointerException npe) {
                savedpayid = 0;
            }
            if (savedpayid > 0) {
                savedpay = new PayBean().getPay(savedpayid);
            }
            new AccJournalBean().postJournalExpenseEntry(trans, aActiveTransItems, savedpay, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
        }
        //Save Opening Balance - Journal Entry
        if ("OPENING BALANCE".equals(transtype.getTransactionTypeName())) {
            new AccJournalBean().postJournalOpenBalance(trans, aActiveTransItems, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
        }
        //Save STOCK CONSUMPTION Journal Entry
        if ("STOCK CONSUMPTION".equals(transtype.getTransactionTypeName())) {
            new AccJournalBean().postJournalStockConsumption(trans, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
        }
    }

    public void savePayForTransThread(Trans aTrans) {
        try {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    long x = savePayForTrans(aTrans, 0);
                }
            };
            Executor e = Executors.newSingleThreadExecutor();
            e.execute(task);
        } catch (Exception e) {
            //System.err.println("subtractStockCallThread:" + e.getMessage());
            LOGGER.log(Level.ERROR, e);
        }
    }

    public long savePayForTrans(Trans aTrans, int aFromFix) {
        TransactionType transtype = new TransactionTypeBean().getTransactionType(aTrans.getTransactionTypeId());
        long payid = 0;
        String sql = null;
        String sql2 = null;
        String msg = "";
        try {
            if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) || "EXPENSE ENTRY".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
                Pay InnerPay = new Pay();
                InnerPay.setPayDate(aTrans.getTransactionDate());
                double paidamountt = 0;
                if ((aTrans.getChangeAmount() > 0)) {
                    //paidamountt = aTrans.getGrandTotal() - aTrans.getSpendPointsAmount();
                    paidamountt = aTrans.getGrandTotal();
                } else {
                    paidamountt = aTrans.getAmountTendered();
                }
                InnerPay.setPaidAmount(paidamountt);
                InnerPay.setPayMethodId(aTrans.getPayMethod());
                InnerPay.setAddUserDetailId(aTrans.getAddUserDetailId());
                InnerPay.setEditUserDetailId(aTrans.getAddUserDetailId());
                InnerPay.setAddDate(aTrans.getAddDate());
                InnerPay.setEditDate(new java.util.Date());
                InnerPay.setPointsSpent(0);//aTrans.getSpendPoints()
                InnerPay.setPointsSpentAmount(0);//aTrans.getSpendPointsAmount()
                InnerPay.setPayRefNo("");
                InnerPay.setPay_number("");
                if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
                    InnerPay.setPayCategory("IN");
                    InnerPay.setPayTypeId(14);//CASH RECEIPT
                    if (paidamountt > 0) {//SOME PAYMENT, thus CASH or PREPAID
                        if (aTrans.getPayMethod() == 6) {//PREPAID INCOME
                            InnerPay.setPayReasonId(90);
                        } else {//CASH
                            InnerPay.setPayReasonId(21);
                        }
                    } else {//Otherwise full/part credit sale
                        InnerPay.setPayReasonId(22);
                    }
                } else if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) || "EXPENSE ENTRY".equals(transtype.getTransactionTypeName())) {
                    InnerPay.setPayCategory("OUT");
                    InnerPay.setPayTypeId(15);//CASH PAYMENT
                    if (paidamountt >= 0) {//SOME PAYMENT, thus CASH or PREPAID
                        if (aTrans.getPayMethod() == 7) {//PREPAID INCOME
                            InnerPay.setPayReasonId(91);
                        } else {//CASH
                            InnerPay.setPayReasonId(26);
                        }
                    } else {//Otherwise full/part credit purchase
                        InnerPay.setPayReasonId(25);
                    }
                }
                InnerPay.setBillTransactorId(aTrans.getBillTransactorId());
                InnerPay.setStoreId(aTrans.getStoreId());
                try {
                    InnerPay.setAccChildAccountId(aTrans.getAccChildAccountId());
                } catch (NullPointerException npe) {
                    InnerPay.setAccChildAccountId(0);
                }
                InnerPay.setCurrencyCode(aTrans.getCurrencyCode());
                InnerPay.setXRate(aTrans.getXrate());
                InnerPay.setStatus(1);
                InnerPay.setStatusDesc("");
                //define output
                InnerPay.setDeletePayId(0);
                payid = 0;
                try {
                    if (aFromFix == 0) {
                        payid = new PayBean().payInsertUpdate(InnerPay);
                    } else if (aFromFix == 1) {
                        payid = new PayBean().payInsertUpdateFix(InnerPay);
                    }
                } catch (NullPointerException npe) {
                    payid = 0;
                }
                //insert PayTrans
                PayTrans paytrans = new PayTrans();
                paytrans.setPayId(payid);
                paytrans.setTransactionId(aTrans.getTransactionId());
                paytrans.setTransPaidAmount(paidamountt);
                if (aTrans.getTransactionNumber().length() > 0) {
                    paytrans.setTransactionNumber(aTrans.getTransactionNumber());
                } else {
                    paytrans.setTransactionNumber(Long.toString(aTrans.getTransactionId()));
                }
                paytrans.setTransactionTypeId(aTrans.getTransactionTypeId());
                paytrans.setTransactionReasonId(aTrans.getTransactionReasonId());
                new PayTransBean().savePayTrans(paytrans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return payid;
    }

    public void saveTransCallOrderInvoice(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa) {
        this.setAutoPrintAfterSave(false);
        long SavedTransId = this.saveTransCECE(aLevel, aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems, aSelectedTransactor, aSelectedBillTransactor, aTransUserDetail, aSelectedSchemeTransactor, aAuthorisedByUserDetail, aSelectedAccCoa);
        if (SavedTransId > 0) {
            //update is_invoiced
            Trans InvoiceTrans = this.getTrans(SavedTransId);
            Trans OrderTrans = this.getTransByTransNumber(InvoiceTrans.getTransactionRef());
            if (InvoiceTrans.getChangeAmount() >= 0) {//all paid
                this.updateOrderIsInvoicedPaid(OrderTrans.getTransactionId(), InvoiceTrans.getTransactionNumber(), null);
            } else {//partially paid
                this.updateOrderIsInvoiced(OrderTrans.getTransactionId(), InvoiceTrans.getTransactionNumber(), null);
            }
            org.primefaces.PrimeFaces.current().executeScript("doPrintHiddenClick()");
        }
    }

    public long saveTransCECE(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        long SavedTransId = 0;
        TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
        TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        Store store = new StoreBean().getStore(aStoreId);
        String ValidationMessage = this.validateTransCEC(aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems, aSelectedTransactor, aSelectedBillTransactor);
        long payid = 0;
        //-------
        String sql = null;
        String sql2 = null;

        TransItemBean TransItemBean = new TransItemBean();

        //first clear current session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        switch (aLevel) {
            case "PARENT":
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                httpSession.setAttribute("CURRENT_PAY_ID", 0);
                break;
            case "CHILD":
                httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", 0);
                httpSession.setAttribute("CURRENT_PAY_ID_CHILD", 0);
                break;
        }

        if (ValidationMessage.length() > 0) {
            switch (aLevel) {
                case "PARENT":
                    this.setActionMessage(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                    break;
                case "CHILD":
                    this.setActionMessageChild(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                    break;
            }
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, ValidationMessage)));
        } else {
            try {
                this.insertTransCEC(aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems);
                if (trans.getTransactionId() == 0) {
                    switch (aLevel) {
                        case "PARENT":
                            this.setActionMessage(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                            break;
                        case "CHILD":
                            this.setActionMessageChild(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                            break;
                    }
                    FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, "Transaction Not Saved Due to Error")));
                } else {
                    //set store2 for transfer in session!
                    switch (aLevel) {
                        case "PARENT":
                            httpSession.setAttribute("CURRENT_TRANSACTION_ID", trans.getTransactionId());
                            if ("TRANSFER".equals(transtype.getTransactionTypeName())) {
                                httpSession.setAttribute("CURRENT_STORE2_ID", trans.getStore2Id());
                            } else {
                                httpSession.setAttribute("CURRENT_STORE2_ID", 0);
                            }
                            break;
                        case "CHILD":
                            httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", trans.getTransactionId());
                            if ("TRANSFER".equals(transtype.getTransactionTypeName())) {
                                httpSession.setAttribute("CURRENT_STORE2_ID_CHILD", trans.getStore2Id());
                            } else {
                                httpSession.setAttribute("CURRENT_STORE2_ID_CHILD", 0);
                            }
                            break;
                    }
                    //save trans items
                    //trans.setStoreId(VARCHAR);
                    TransItemBean tib = new TransItemBean();
                    if (trans.getTransactionTypeId() == 16 || trans.getTransactionTypeId() == 18) {//Journal Entry, Cash Transfer
                        tib.saveTransItemsJournalEntry(trans, aActiveTransItems, trans.getTransactionId());
                    } else {
                        tib.saveTransItemsCEC(aStoreId, aTransTypeId, aTransReasonId, aSaleType, trans, aActiveTransItems, trans.getTransactionId());
                    }

                    //save payment
                    if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) || "EXPENSE ENTRY".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
                        Pay InnerPay = new Pay();
                        InnerPay.setPayDate(trans.getTransactionDate());
                        double paidamountt = 0;
                        if ((trans.getChangeAmount() > 0)) {
                            paidamountt = trans.getGrandTotal() - trans.getSpendPointsAmount();
                        } else {
                            paidamountt = trans.getAmountTendered();
                        }
                        InnerPay.setPaidAmount(paidamountt);
                        InnerPay.setPayMethodId(trans.getPayMethod());
                        InnerPay.setAddUserDetailId(new GeneralUserSetting().getCurrentUser().getUserDetailId());
                        InnerPay.setEditUserDetailId(new GeneralUserSetting().getCurrentUser().getUserDetailId());
                        InnerPay.setAddDate(new java.util.Date());
                        InnerPay.setEditDate(new java.util.Date());
                        InnerPay.setPointsSpent(trans.getSpendPoints());
                        InnerPay.setPointsSpentAmount(trans.getSpendPointsAmount());
                        InnerPay.setPayRefNo("");
                        InnerPay.setPay_number("");
                        if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
                            InnerPay.setPayCategory("IN");
                            InnerPay.setPayTypeId(14);//CASH RECEIPT
                            if (paidamountt > 0) {//SOME PAYMENT, thus CASH or PREPAID
                                if (trans.getPayMethod() == 6) {//PREPAID INCOME
                                    InnerPay.setPayReasonId(90);
                                } else {//CASH
                                    InnerPay.setPayReasonId(21);
                                }
                            } else {//Otherwise full/part credit sale
                                InnerPay.setPayReasonId(22);
                            }
                        } else if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) || "EXPENSE ENTRY".equals(transtype.getTransactionTypeName())) {
                            InnerPay.setPayCategory("OUT");
                            InnerPay.setPayTypeId(15);//CASH PAYMENT
                            if (paidamountt >= 0) {//SOME PAYMENT, thus CASH or PREPAID
                                if (trans.getPayMethod() == 7) {//PREPAID INCOME
                                    InnerPay.setPayReasonId(91);
                                } else {//CASH
                                    InnerPay.setPayReasonId(26);
                                }
                            } else {//Otherwise full/part credit purchase
                                InnerPay.setPayReasonId(25);
                            }
                        }
                        InnerPay.setBillTransactorId(trans.getBillTransactorId());
                        InnerPay.setStoreId(store.getStoreId());
                        try {
                            InnerPay.setAccChildAccountId(trans.getAccChildAccountId());
                        } catch (NullPointerException npe) {
                            InnerPay.setAccChildAccountId(0);
                        }
                        InnerPay.setCurrencyCode(trans.getCurrencyCode());
                        InnerPay.setXRate(trans.getXrate());
                        InnerPay.setStatus(1);
                        InnerPay.setStatusDesc("");
                        //define output
                        InnerPay.setDeletePayId(0);
                        payid = 0;
                        try {
                            payid = new PayBean().payInsertUpdate(InnerPay);
                        } catch (NullPointerException npe) {
                            payid = 0;
                        }
                        switch (aLevel) {
                            case "PARENT":
                                httpSession.setAttribute("CURRENT_PAY_ID", payid);
                                break;
                            case "CHILD":
                                httpSession.setAttribute("CURRENT_PAY_ID_CHILD", payid);
                                break;
                        }
                        //insert PayTrans
                        PayTrans paytrans = new PayTrans();
                        paytrans.setPayId(payid);
                        paytrans.setTransactionId(trans.getTransactionId());
                        paytrans.setTransPaidAmount(paidamountt);
                        if (trans.getTransactionNumber().length() > 0) {
                            paytrans.setTransactionNumber(trans.getTransactionNumber());
                        } else {
                            paytrans.setTransactionNumber(Long.toString(trans.getTransactionId()));
                        }
                        paytrans.setTransactionTypeId(trans.getTransactionTypeId());
                        paytrans.setTransactionReasonId(trans.getTransactionReasonId());
                        new PayTransBean().savePayTrans(paytrans);
                    }

                    //insert approvals
                    if (("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName())) && trans.getCashDiscount() > 0 && new GeneralUserSetting().getIsApproveDiscountNeeded() == 1 && "APPROVED".equals(new GeneralUserSetting().getCurrentApproveDiscountStatus())) {
                        this.insertApproveTrans(new GeneralUserSetting().getCurrentTransactionId(), "DISCOUNT", new GeneralUserSetting().getCurrentApproveUserId());
                    }
                    if (("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName())) && trans.getSpendPointsAmount() > 0 && new GeneralUserSetting().getIsApprovePointsNeeded() == 1 && "APPROVED".equals(new GeneralUserSetting().getCurrentApprovePointsStatus())) {
                        this.insertApproveTrans(new GeneralUserSetting().getCurrentTransactionId(), "SPEND POINT", new GeneralUserSetting().getCurrentApproveUserId());
                    }

                    //Save Sales Journal Entry
                    if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
                        Pay savedpay = null;
                        long savedpayid = 0;
                        try {
                            //savedpayid = new GeneralUserSetting().getCurrentPayId();
                            savedpayid = payid;
                        } catch (NullPointerException npe) {
                            savedpayid = 0;
                        }
                        if (savedpayid > 0) {
                            savedpay = new PayBean().getPay(savedpayid);
                        }
                        new AccJournalBean().postJournalSaleInvoice(trans, aActiveTransItems, savedpay, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //Save Purchase Invoice Journal Entry
                    long PostJobId = 0;
                    if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName())) {
                        Pay savedpay = null;
                        long savedpayid = 0;
                        try {
                            //savedpayid = new GeneralUserSetting().getCurrentPayId();
                            savedpayid = payid;
                        } catch (NullPointerException npe) {
                            savedpayid = 0;
                        }
                        if (savedpayid > 0) {
                            savedpay = new PayBean().getPay(savedpayid);
                        }
                        PostJobId = new AccJournalBean().postJournalPurchaseInvoice(trans, aActiveTransItems, savedpay, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //Save Asset Depreciation Schedule
                    if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) && transreason.getTransactionReasonId() == 29) {
                        //pay just for reference
                        Pay savedpay = new Pay();
                        long savedpayid = 0;
                        try {
                            //savedpayid = new GeneralUserSetting().getCurrentPayId();
                            savedpayid = payid;
                        } catch (NullPointerException npe) {
                            savedpayid = 0;
                        }
                        if (savedpayid > 0) {
                            savedpay = new PayBean().getPay(savedpayid);
                        }
                        Stock assetstock = null;
                        AccDepScheduleBean depschbean = new AccDepScheduleBean();
                        int assetstoreid = store.getStoreId();
                        for (TransItem assetti : aActiveTransItems) {
                            assetstock = new StockBean().getStock(assetstoreid, assetti.getItemId(), assetti.getBatchno(), assetti.getCodeSpecific(), assetti.getDescSpecific());
                            //Build:1-20-000-020 Land:1-20-000-010 but let us exclude LAND
                            if (null != assetstock && assetstock.getAccountCode().length() > 0 && !assetstock.getAccountCode().equals("1-20-000-010")) {
                                depschbean.insertAccDepSchedules(depschbean.calcAccDepSchedules(assetstock));
                                //post to the ledger the first year's depriciation
                                AccDepSchedule aAccDepSchedule = depschbean.getAccDepScheduleByYear(assetstock.getStockId(), 1);
                                new AccJournalBean().postJournalDepreciateAsset(trans, assetstock, aAccDepSchedule, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId(), PostJobId);
                            }
                        }
                    }
                    //Save Dispose Stock Journal Entry
                    if ("DISPOSE STOCK".equals(transtype.getTransactionTypeName())) {
                        new AccJournalBean().postJournalDisposeStock(trans, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //Save Journal Entry - Journal Entry
                    if ("JOURNAL ENTRY".equals(transtype.getTransactionTypeName())) {
                        new AccJournalBean().postJournalJournalEntry(trans, aActiveTransItems, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //Save Journal Entry - Journal Entry
                    if ("CASH TRANSFER".equals(transtype.getTransactionTypeName())) {
                        new AccJournalBean().postJournalCashTransfer(trans, aActiveTransItems, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //Save Expense Journal Entry
                    if ("EXPENSE ENTRY".equals(transtype.getTransactionTypeName())) {
                        Pay savedpay = null;
                        long savedpayid = 0;
                        try {
                            //savedpayid = new GeneralUserSetting().getCurrentPayId();
                            savedpayid = payid;
                        } catch (NullPointerException npe) {
                            savedpayid = 0;
                        }
                        if (savedpayid > 0) {
                            savedpay = new PayBean().getPay(savedpayid);
                        }
                        new AccJournalBean().postJournalExpenseEntry(trans, aActiveTransItems, savedpay, new AccPeriodBean().getAccPeriod(trans.getTransactionDate()).getAccPeriodId());
                    }
                    //delete if any draft trans was used
                    if (trans.getTransactionHistId() > 0) {
                        this.deleteTransFromHist(trans.getTransactionHistId());
                    }
                    //TAX API
                    if (aTransTypeId == 2 && new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value().length() > 0 && new Item_tax_mapBean().countItemsNotMappedSynced(aActiveTransItems) == 0) {//SALES INVOICE
                        int IsThreadOn = 0;
                        try {
                            IsThreadOn = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_TAX_THREAD_ON").getParameter_value());
                        } catch (Exception e) {
                            //
                        }
                        if (IsThreadOn == 0) {
                            new InvoiceBean().submitTaxInvoice(trans.getTransactionId());
                        } else if (IsThreadOn == 1) {
                            new InvoiceBean().submitTaxInvoiceThread(trans.getTransactionId());
                        }
                    }
                    //SMbi API
                    if (new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_SMBI_URL").getParameter_value().length() > 0) {
                        new Transaction_smbi_mapBean().insertTransaction_smbi_mapCallThread(trans.getTransactionId(), trans.getTransactionTypeId());
                    }
                    //Update Total Paid for Sales/Purchase Invoice
                    if (aTransTypeId == 2 || aTransTypeId == 1) {
                        new PayTransBean().updateTransTotalPaid(trans.getTransactionId());
                    }
                    this.clearAll2(trans, aActiveTransItems, null, null, aSelectedTransactor, 2, aSelectedBillTransactor, aTransUserDetail, aSelectedSchemeTransactor, aAuthorisedByUserDetail, aSelectedAccCoa);

                    TransItemBean = null;

                    //clean stock
                    StockBean.deleteZeroQtyStock();
                    switch (aLevel) {
                        case "PARENT":
                            SavedTransId = new GeneralUserSetting().getCurrentTransactionId();
                            this.setActionMessage(ub.translateWordsInText(BaseName, "Saved Successfully Transaction Id " + SavedTransId));
                            break;
                        case "CHILD":
                            SavedTransId = new GeneralUserSetting().getCurrentTransactionIdChild();
                            this.setActionMessageChild(ub.translateWordsInText(BaseName, "Saved Successfully Transaction Id " + SavedTransId));
                            break;
                    }

                    //Refresh Print output
                    new OutputDetailBean().refreshOutput(aLevel, "");
                    //refresh draft
                    if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName())) {
                        this.refreshTranssDraft(aStoreId, new GeneralUserSetting().getCurrentUser().getUserDetailId(), aTransTypeId, aTransReasonId);
                    }
                    //Auto Printing Invoice
                    if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName())) {
                        //1. Update Invoice
                        //---SalesInvoiceBean.initSalesInvoiceBean();
                        //2. Auto Printing Invoice
                        if (this.AutoPrintAfterSave) {
                            org.primefaces.PrimeFaces.current().executeScript("doPrintHiddenClick()");
                        }
                    }
                    //check need for child dialogue
                    if ("HIRE RETURN NOTE".equals(transtype.getTransactionTypeName())) {
                        //find out if there is a return invoice candidate
                        if (this.isReturnNoteForInvoice(new GeneralUserSetting().getCurrentTransactionId()) == 1) {
                            this.openChildReturnHireInvoice(new GeneralUserSetting().getCurrentTransactionId());
                        }
                    }
                    //Refresh stock alerts
                    new UtilityBean().refreshAlertsThread();
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
                switch (aLevel) {
                    case "PARENT":
                        this.setActionMessage(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                        break;
                    case "CHILD":
                        this.setActionMessageChild(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                        break;
                }
                FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, "Transaction Not Saved") + ". " + ub.translateWordsInText(BaseName, "Ensure Transaction Reference Number is not Captured Already")));
            }
        }
        return SavedTransId;
    }

    public int isReturnNoteForInvoice(long aReturnTransId) {
        int OneOrZero = 0;
        try {
            if (aReturnTransId > 0) {
                Trans returned_t = this.getTrans(aReturnTransId);
                List<TransItem> returned_t_items = new TransItemBean().getTransItemsByTransactionId(aReturnTransId);
                //check-1
                int check_1 = 0;
                if (returned_t.getDuration_value() > 0) {
                    check_1 = 1;
                }
                //check-2
                int check_2 = 0;
                for (int i = 0; i < returned_t_items.size(); i++) {
                    if (returned_t_items.get(i).getQty_damage() > 0) {
                        check_2 = 1;
                        break;
                    }
                }
                if (check_1 == 1 || check_2 == 1) {
                    OneOrZero = 1;
                }
            } else {
                OneOrZero = 0;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return OneOrZero;
    }

    public void openChildReturnHireInvoice(long aRuturnNoteId) {
        this.getReturnHireInvoice(aRuturnNoteId);
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
        org.primefaces.PrimeFaces.current().dialog().openDynamic("HireReturnInvoiceTrans", options, null);
    }

    public void closeOrderChildSalesInvoice() {
        org.primefaces.PrimeFaces.current().executeScript("PF('dlgInvoice').close()");
    }

    public void openOrderParentSalesInvoice() {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", false);
        options.put("resizable", false);
        options.put("width", 720);
        options.put("height", 320);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        options.put("scrollable", true);
        options.put("maximizable", true);
        options.put("dynamic", true);
        org.primefaces.PrimeFaces.current().dialog().openDynamic("SaleOrderParentInvoiceTrans", options, null);
    }

    public void openOrderChildSalesInvoice(long aOrderId, UserDetail aUserDetail) {
        this.getOrderSalesInvoice(aOrderId, aUserDetail);
        new OutputDetailBean().refreshOutput("PARENT", "");
        org.primefaces.PrimeFaces.current().executeScript("PF('dlgInvoice').show()");
    }

    public void viewOrderOutput(Trans aOrderTrans) {
        //first set current selection in session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute("CURRENT_TRANSACTION_ID", aOrderTrans.getTransactionId());
        httpSession.setAttribute("CURRENT_PAY_ID", 0);
        //refresh output
        new OutputDetailBean().refreshOutput("PARENT", "");
        org.primefaces.PrimeFaces.current().executeScript("PF('dlgOrder').show()");
    }

    public void openView(String aLevel, Trans aTrans, String aAction) {
        try {
            TransactionType transtype = new TransactionTypeBean().getTransactionType(aTrans.getTransactionTypeId());
            TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTrans.getTransactionReasonId());
            String filename = "";
            this.ActionType = aAction;
            this.TransObj = new TransBean().getTrans(aTrans.getTransactionId());
            this.TransItemList = new TransItemBean().getTransItemsByTransactionId(aTrans.getTransactionId());
            try {
                this.PayObj = new PayBean().getTransactionFirstPayByTransNo(TransObj.getTransactionNumber());//first payment
            } catch (NullPointerException npe) {
                this.PayObj = null;
            }
            try {
                this.RefTrans = new TransBean().getTransByTransNumber(TransObj.getTransactionRef());
            } catch (NullPointerException npe) {
                this.RefTrans = null;
            }
            if (transtype.getTransactionTypeName().equals("HIRE INVOICE")) {
                filename = "ViewHireInvoiceTrans";
            } else if (transtype.getTransactionTypeName().equals("HIRE RETURN INVOICE")) {
                filename = "ViewHireReturnInvoiceTrans";
            } else if (transtype.getTransactionTypeName().equals("HIRE QUOTATION")) {
                filename = "ViewHireQuotationTrans";
            } else if (transtype.getTransactionTypeName().equals("HIRE RETURN NOTE")) {
                filename = "ViewHireReturnNoteTrans";
            } else if (transtype.getTransactionTypeName().equals("HIRE DELIVERY NOTE")) {
                filename = "ViewHireDeliveryNoteTrans";
            }
            //set current session
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
            HttpSession httpSession = request.getSession(true);
            switch (aLevel) {
                case "PARENT":
                    try {
                        httpSession.setAttribute("CURRENT_TRANSACTION_ID", this.TransObj.getTransactionId());
                    } catch (NullPointerException npe) {
                        httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                    }
                    try {
                        httpSession.setAttribute("CURRENT_PAY_ID", this.PayObj.getPayId());
                    } catch (NullPointerException npe) {
                        httpSession.setAttribute("CURRENT_PAY_ID", 0);
                    }
                    break;
                case "CHILD":
                    try {
                        httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", this.TransObj.getTransactionId());
                    } catch (NullPointerException npe) {
                        httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", 0);
                    }
                    try {
                        httpSession.setAttribute("CURRENT_PAY_ID_CHILD", this.PayObj.getPayId());
                    } catch (NullPointerException npe) {
                        httpSession.setAttribute("CURRENT_PAY_ID_CHILD", 0);
                    }
                    break;
            }
            //Refresh Print output
            new OutputDetailBean().refreshOutput(aLevel, "");
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
            org.primefaces.PrimeFaces.current().dialog().openDynamic(filename, options, null);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public long getBillClientId(long aTransactorId, long aBillTransactorId) {
        long id = 0;
        try {
            id = aBillTransactorId;
        } catch (NullPointerException npe) {
            id = 0;
        }

        if (id == 0) {
            try {
                id = aTransactorId;
            } catch (NullPointerException npe) {
                id = 0;
            }
        }

        return id;
    }

    public void saveTransAutoUnpack(TransItem aTransItem) {
        String sql = null;
        String sql2 = null;
        int SystemUserId = 0;
        SystemUserId = UserDetailBean.getSystemUserDetailId();
        if (SystemUserId == 0) {
            SystemUserId = new GeneralUserSetting().getCurrentUser().getUserDetailId();
        }
        sql = "{call sp_insert_transaction(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        if (aTransItem != null) {
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    CallableStatement cs = conn.prepareCall(sql);) {
                cs.setDate("in_transaction_date", new java.sql.Date(new CompanySetting().getCURRENT_SERVER_DATE().getTime()));
                cs.setInt("in_store_id", new GeneralUserSetting().getCurrentStore().getStoreId());
                cs.setInt("in_store2_id", 0);
                cs.setLong("in_transactor_id", 0);
                cs.setInt("in_transaction_type_id", 7);
                cs.setInt("in_transaction_reason_id", 9);
                cs.setDouble("in_cash_discount", 0);
                cs.setDouble("in_total_vat", 0);
                cs.setString("in_transaction_comment", "Auto.Unpack");
                cs.setInt("in_add_user_detail_id", SystemUserId);
                cs.setTimestamp("in_add_date", new java.sql.Timestamp(new java.util.Date().getTime()));
                cs.setInt("in_edit_user_detail_id", SystemUserId);
                cs.setTimestamp("in_edit_date", new java.sql.Timestamp(new java.util.Date().getTime()));
                cs.setString("in_transaction_ref", "Auto.Unpack");
                cs.registerOutParameter("out_transaction_id", VARCHAR);
                cs.setDouble("in_sub_total", 0);
                cs.setDouble("in_grand_total", 0);
                cs.setDouble("in_total_trade_discount", 0);
                cs.setDouble("in_points_awarded", 0);
                cs.setString("in_card_number", "");
                cs.setDouble("in_total_std_vatable_amount", 0);
                cs.setDouble("in_total_zero_vatable_amount", 0);
                cs.setDouble("in_total_exempt_vatable_amount", 0);
                cs.setDouble("in_vat_perc", 0);
                cs.setDouble("in_amount_tendered", 0);
                cs.setDouble("in_change_amount", 0);
                cs.setString("in_is_cash_discount_vat_liable", "");
                cs.setDouble("in_total_profit_margin", 0);
                cs.setInt("in_transaction_user_detail_id", 0);
                cs.setLong("in_bill_transactor_id", 0);
                cs.setLong("in_scheme_transactor_id", 0);
                cs.setString("in_princ_scheme_member", "");
                cs.setString("in_scheme_card_number", "");
                cs.setString("in_transaction_number", "");
                cs.setDate("in_delivery_date", null);
                cs.setString("in_delivery_address", "");
                cs.setString("in_pay_terms", "");
                cs.setString("in_terms_conditions", "");
                cs.setInt("in_authorised_by_user_detail_id", 0);
                cs.setDate("in_authorise_date", null);
                cs.setDate("in_pay_due_date", null);
                cs.setDate("in_expiry_date", null);
                cs.setInt("in_acc_child_account_id", 0);
                cs.setString("in_currency_code", "");
                cs.setDouble("in_xrate", 1);
                cs.setDate("in_from_date", null);
                cs.setDate("in_to_date", null);
                cs.setString("in_duration_type", "");
                cs.setLong("in_site_id", 0);
                cs.setString("in_transactor_rep", "");
                cs.setString("in_transactor_vehicle", "");
                cs.setString("in_transactor_driver", "");
                cs.setDouble("in_duration_value", 0);
                //bought in after order module
                cs.setLong("in_location_id", 0);
                cs.setString("in_status_code", "");
                cs.setTimestamp("in_status_date", null);
                cs.setString("in_delivery_mode", "");
                cs.setInt("in_is_processed", 0);
                cs.setInt("in_is_paid", 0);
                cs.setInt("in_is_cancel", 0);
                cs.setDouble("in_spent_points_amount", 0);
                //save
                cs.executeUpdate();
                //save trans item
                aTransItem.setTransactionId(cs.getLong("out_transaction_id"));
                TransItemBean tib = new TransItemBean();
                tib.saveTransItemAutoUnpack(aTransItem);
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    public void callUpdateTrans(Trans aNewTrans, List<TransItem> aNewTransItems, Pay aPay) {
        try {
            //confirm trans type
            String aTransTypeName = "";
            if (aNewTrans != null) {
                aTransTypeName = new TransactionTypeBean().getTransactionType(aNewTrans.getTransactionTypeId()).getTransactionTypeName();
                new NavigationBean().defineTransactionTypes(aNewTrans.getTransactionTypeId(), aTransTypeName, "", "");
                if (aNewTrans.getTransactionTypeId() == 5 || aNewTrans.getTransactionTypeId() == 6 || aNewTrans.getTransactionTypeId() == 7) {
                    this.setActionMessage("THIS TRANSACTION TYPE CANNOT BE UPDATED!");
                } else {
                    if (aNewTrans.getTransactionTypeId() == 2 && aNewTrans.getBillTransactorId() == 0 && (aNewTrans.getAmountTendered() + aNewTrans.getSpendPointsAmount()) != aNewTrans.getGrandTotal()) {
                        FacesContext.getCurrentInstance().addMessage("Update", new FacesMessage("TenderedAmount plus PiontsSpentAmount should equal the new GrandTotal!"));
                        this.setActionMessage("TenderAmount plus PiontsSpentAmount is should equal the new GrandTotal!");
                    } else if (aNewTrans.getTransactionTypeId() == 2 && aNewTrans.getSpendPointsAmount() > aNewTrans.getBalancePointsAmount()) {
                        FacesContext.getCurrentInstance().addMessage("Update", new FacesMessage("PiontsSpentAmount cannot exceed BalancePointsAmount!"));
                        this.setActionMessage("PiontsSpentAmount cannot exceed BalancePointsAmount!");
                    } else {
                        //this.updateTrans(aNewTrans, aNewTransItems, aPay);
                    }
                }
            } else {
                this.setActionMessage("THIS TRANSACTION IS INVALID!");
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void updateTransV2(Trans aNewTrans, List<TransItem> aNewTransItems, Pay aPay) {
        //1. copy
        //2. reverse stock(for TIs)
        //3. update trans item
        //4. reverse Tra(for ledgers, etc)
        //5. update trans

        String sql = null;
        String msg = "";
        long TransHistId = 0;
        long PayHistId = 0;
        long newPayId = 0;
        boolean isTransCopySuccess = false;
        boolean isTransItemCopySuccess = false;
        boolean isTransUpdateSuccess = false;
        boolean isTransItemReverseSuccess = false;
        boolean isPayCopySuccess = false;
        boolean isPayTransCopySuccess = false;
        boolean isPayUpdateSuccess = false;
        Pay oldPay = new Pay();
        Pay newPay = new Pay();
        Pay savedpay = null;
        Trans savedtrans = null;
        int hasReversed = 0;

        TransactionType tt = new TransactionTypeBean().getTransactionType(aNewTrans.getTransactionTypeId());
        TransItemBean TransItemBean = new TransItemBean();
        Trans OldTrans = new Trans();
        OldTrans = new TransBean().getTrans(aNewTrans.getTransactionId());
        this.setTransTotalsAndUpdateV2(OldTrans, new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId()));

        //first clear current trans and pay ids in session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
        httpSession.setAttribute("CURRENT_PAY_ID", 0);

        UserDetail aCurrentUserDetail = new GeneralUserSetting().getCurrentUser();
        List<GroupRight> aCurrentGroupRights = new GeneralUserSetting().getCurrentGroupRights();
        GroupRightBean grb = new GroupRightBean();

        if (grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, Integer.toString(aNewTrans.getTransactionReasonId()), "Edit") == 0) {
            msg = "YOU ARE NOT ALLOWED TO USE THIS FUNCTION, CONTACT SYSTEM ADMINISTRATOR...";
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(msg));
            this.setActionMessage("Transaction NOT Updated");
        } else {
            //Copy Trans
            sql = "{call sp_copy_transaction(?,?,?)}";
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    CallableStatement cs = conn.prepareCall(sql);) {
                cs.setLong("in_transaction_id", aNewTrans.getTransactionId());
                cs.setString("in_hist_flag", "Edit");
                cs.registerOutParameter("out_transaction_hist_id", VARCHAR);
                cs.executeUpdate();
                TransHistId = cs.getLong("out_transaction_hist_id");
                isTransCopySuccess = true;
            } catch (Exception e) {
                isTransCopySuccess = false;
                LOGGER.log(Level.ERROR, e);
            }
            //Copy TransItem
            if (isTransCopySuccess) {
                List<TransItem> TransItemsToCopy = new ArrayList<TransItem>();
                TransItemsToCopy = new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId());
                int i = 0;
                int n = TransItemsToCopy.size();
                //now copy item by item
                sql = "{call sp_copy_transaction_item(?,?,?)}";
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        CallableStatement cs = conn.prepareCall(sql);) {
                    while (i < n) {
                        cs.setLong("in_transaction_id", aNewTrans.getTransactionId());
                        cs.setLong("in_transaction_hist_id", TransHistId);
                        cs.setLong("in_transaction_item_id", TransItemsToCopy.get(i).getTransactionItemId());
                        cs.executeUpdate();
                        i = i + 1;
                    }
                    isTransItemCopySuccess = true;
                } catch (Exception e) {
                    isTransItemCopySuccess = false;
                    LOGGER.log(Level.ERROR, e);
                }
            }

            //Update by Reversing trans items qty differences
            TransItemBean tib = new TransItemBean();
            isTransItemReverseSuccess = tib.updateTransItemsV2(aNewTrans.getTransactionId(), TransHistId, aNewTransItems);
            //update trans
            if (isTransCopySuccess && isTransItemCopySuccess && isTransItemReverseSuccess) {
                isTransUpdateSuccess = this.updateTransactionTable(aNewTrans);
            }

            //Update the first Payment
            if (tt.getTransactionTypeName().equals("SALE INVOICE") || tt.getTransactionTypeName().equals("PURCHASE INVOICE") || tt.getTransactionTypeName().equals("EXPENSE ENTRY")) {
                oldPay = new PayBean().getTransactionFirstPayByTransNo(aNewTrans.getTransactionNumber());
                newPay = new PayBean().getTransactionFirstPayByTransNo(aNewTrans.getTransactionNumber());
                if (null != newPay) {
                    //copy pay and pay trans
                    //copy pay
                    sql = "{call sp_copy_pay(?,?,?)}";
                    try (
                            Connection conn = DBConnection.getMySQLConnection();
                            CallableStatement cs = conn.prepareCall(sql);) {
                        cs.setLong("in_pay_id", newPay.getPayId());
                        cs.setString("in_hist_flag", "Edit");
                        cs.registerOutParameter("out_pay_hist_id", VARCHAR);
                        cs.executeUpdate();
                        PayHistId = cs.getLong("out_pay_hist_id");
                        isPayCopySuccess = true;
                    } catch (Exception e) {
                        isPayCopySuccess = false;
                        LOGGER.log(Level.ERROR, e);
                    }
                    //copy pay trans
                    if (isPayCopySuccess) {
                        List<PayTrans> PayTranssToCopy = new ArrayList<PayTrans>();
                        PayTranssToCopy = new PayTransBean().getPayTranssByPayId(newPay.getPayId());
                        int i = 0;
                        int n = PayTranssToCopy.size();
                        //now copy item by item
                        sql = "{call sp_copy_pay_trans(?,?,?)}";
                        try (
                                Connection conn = DBConnection.getMySQLConnection();
                                CallableStatement cs = conn.prepareCall(sql);) {
                            while (i < n) {
                                cs.setLong("in_pay_id", newPay.getPayId());
                                cs.setLong("in_pay_hist_id", PayHistId);
                                cs.setLong("in_pay_trans_id", PayTranssToCopy.get(i).getPayTransId());
                                cs.executeUpdate();
                                i = i + 1;
                            }
                            isPayTransCopySuccess = true;
                        } catch (Exception e) {
                            isPayTransCopySuccess = false;
                            LOGGER.log(Level.ERROR, e);
                        }
                    }
                    if (isTransCopySuccess && isTransItemCopySuccess && isTransItemReverseSuccess && isTransUpdateSuccess && isPayCopySuccess && isPayTransCopySuccess) {
                        newPay.setPaidAmount(aNewTrans.getAmountTendered());
                        newPay.setPointsSpentAmount(aNewTrans.getSpendPointsAmount());
                        newPay.setPointsSpent(aNewTrans.getSpendPoints());
                        newPay.setEditUserDetailId(new GeneralUserSetting().getCurrentUser().getUserDetailId());
                        newPayId = new PayBean().payInsertUpdate(newPay);
                        //insert PayTrans
                        PayTrans paytrans = null;
                        try {
                            paytrans = new PayTransBean().getPayTranssByPayId(newPayId).get(0);
                        } catch (NullPointerException | IndexOutOfBoundsException npe) {
                            paytrans = null;
                        }
                        if (null != paytrans) {
                            paytrans.setTransPaidAmount(newPay.getPaidAmount());
                            new PayTransBean().savePayTrans(paytrans);
                        }
                    }
                }
            }
            //Reverse and Insert Journal
            if ("SALE INVOICE".equals(tt.getTransactionTypeName())) {
                savedpay = null;
                savedtrans = null;
                hasReversed = 0;
                List<TransItem> transitems = null;
                if (newPayId > 0) {
                    savedpay = new PayBean().getPay(newPayId);
                }
                savedtrans = new TransBean().getTrans(aNewTrans.getTransactionId());
                transitems = new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId());
                hasReversed = new AccJournalBean().postJournalReverse(OldTrans, oldPay);
                if (hasReversed == 1) {
                    savedtrans.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
                    new AccJournalBean().postJournalSaleInvoice(savedtrans, transitems, savedpay, new AccPeriodBean().getAccPeriod(savedtrans.getTransactionDate()).getAccPeriodId());
                }
            }
            if ("PURCHASE INVOICE".equals(tt.getTransactionTypeName())) {
                savedpay = new Pay();
                savedtrans = new Trans();
                hasReversed = 0;
                List<TransItem> transitems = new ArrayList<>();
                if (newPayId > 0) {
                    savedpay = new PayBean().getPay(newPayId);
                }
                savedtrans = new TransBean().getTrans(aNewTrans.getTransactionId());
                transitems = new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId());
                hasReversed = new AccJournalBean().postJournalReverse(OldTrans, oldPay);
                if (hasReversed == 1) {
                    long PostJobId = 0;
                    savedtrans.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
                    PostJobId = new AccJournalBean().postJournalPurchaseInvoice(savedtrans, transitems, savedpay, new AccPeriodBean().getAccPeriod(savedtrans.getTransactionDate()).getAccPeriodId());
                    //Depriciation for Asset Trans
                    if (savedtrans.getTransactionReasonId() == 29) {
                        Stock assetstock = null;
                        AccDepScheduleBean depschbean = new AccDepScheduleBean();
                        int assetstoreid = savedtrans.getStoreId();
                        for (TransItem assetti : transitems) {
                            assetstock = new StockBean().getStock(assetstoreid, assetti.getItemId(), assetti.getBatchno(), assetti.getCodeSpecific(), assetti.getDescSpecific());
                            //Build:1-20-000-020 Land:1-20-000-010 but let us exclude LAND
                            if (null != assetstock && assetstock.getAccountCode().length() > 0 && !assetstock.getAccountCode().equals("1-20-000-010")) {
                                //first delete previous dep schedules
                                new AccDepScheduleBean().deleteAccDepSchedule(assetstock.getStockId());
                                //then save new schedules
                                depschbean.insertAccDepSchedules(depschbean.calcAccDepSchedules(assetstock));
                                //post to the ledger the first year's depriciation
                                AccDepSchedule aAccDepSchedule = depschbean.getAccDepScheduleByYear(assetstock.getStockId(), 1);
                                new AccJournalBean().postJournalDepreciateAsset(savedtrans, assetstock, aAccDepSchedule, new AccPeriodBean().getAccPeriod(savedtrans.getTransactionDate()).getAccPeriodId(), PostJobId);
                            }
                        }
                    }
                }
            }
            if ("DISPOSE STOCK".equals(tt.getTransactionTypeName())) {
                savedpay = null;
                savedtrans = null;
                hasReversed = 0;
                List<TransItem> transitems = null;
                if (newPayId > 0) {
                    savedpay = new PayBean().getPay(newPayId);
                }
                savedtrans = new TransBean().getTrans(aNewTrans.getTransactionId());
                transitems = new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId());
                hasReversed = new AccJournalBean().postJournalReverse(OldTrans, oldPay);
                if (hasReversed == 1) {
                    savedtrans.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
                    new AccJournalBean().postJournalDisposeStock(savedtrans, new AccPeriodBean().getAccPeriod(savedtrans.getTransactionDate()).getAccPeriodId());
                }
            }
            if ("EXPENSE ENTRY".equals(tt.getTransactionTypeName())) {
                savedpay = new Pay();
                savedtrans = new Trans();
                hasReversed = 0;
                List<TransItem> transitems = new ArrayList<>();
                if (newPayId > 0) {
                    savedpay = new PayBean().getPay(newPayId);
                }
                savedtrans = new TransBean().getTrans(aNewTrans.getTransactionId());
                transitems = new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId());
                hasReversed = new AccJournalBean().postJournalReverse(OldTrans, oldPay);
                if (hasReversed == 1) {
                    savedtrans.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
                    new AccJournalBean().postJournalExpenseEntry(savedtrans, transitems, savedpay, new AccPeriodBean().getAccPeriod(savedtrans.getTransactionDate()).getAccPeriodId());
                }
            }
            TransItemBean = null;
            //clean stock
            StockBean.deleteZeroQtyStock();
            if (isTransCopySuccess && isTransItemCopySuccess && isTransItemReverseSuccess && isTransUpdateSuccess) {
                this.setActionMessage("Transaction Updated Successfully");
            } else {
                this.setActionMessage("Transaction NOT Updated");
            }
        }
    }

    public void updateTransCECcallFromSI(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans aNewTrans, List<TransItem> aNewTransItems, Pay aPay) {
        //get some details
        String OrderTransNo = aNewTrans.getTransactionRef();
        //save
        this.updateTransCEC(aLevel, aStoreId, aTransTypeId, aTransReasonId, aSaleType, aNewTrans, aNewTransItems, aPay);
        //update a few things needed after sales invoice saving
        if (OrderTransNo.length() > 0) {
            Trans OrderTrans = this.getTransByNumberType(OrderTransNo, 11);
            //get order's invoioce status 0,1,2
            int InvoiceStatus = this.getOrderInvoiceStatus(OrderTrans);
            //get order's invoioce pay status 0,1,2
            //save invoice status if not 0
            this.updateOrderStatus(OrderTrans.getTransactionId(), "is_invoiced", InvoiceStatus);
        }
    }

    public void raiseCreditNoteCall(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans aNewTrans, List<TransItem> aNewTransItems, Pay aPay, double aRefundAmount, double aReverseCreditAmt) {
        //get some details
        String OrderTransNo = aNewTrans.getTransactionRef();
        //save
        this.raiseCreditNote(aLevel, aStoreId, aTransTypeId, aTransReasonId, aSaleType, aNewTrans, aNewTransItems, aPay, aRefundAmount, aReverseCreditAmt);
        //update a few things needed after sales invoice saving
        if (OrderTransNo.length() > 0) {
            Trans OrderTrans = this.getTransByNumberType(OrderTransNo, 11);
            //get order's invoioce status 0,1,2
            int InvoiceStatus = this.getOrderInvoiceStatus(OrderTrans);
            //get order's invoioce pay status 0,1,2
            //save invoice status if not 0
            this.updateOrderStatus(OrderTrans.getTransactionId(), "is_invoiced", InvoiceStatus);
        }
    }

    public void raiseDebitNoteCall(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans aNewTrans, List<TransItem> aNewTransItems, Pay aPay) {
        //get some details
        String OrderTransNo = aNewTrans.getTransactionRef();
        //save
        this.raiseDebitNote(aLevel, aStoreId, aTransTypeId, aTransReasonId, aSaleType, aNewTrans, aNewTransItems, aPay);
        //update a few things needed after sales invoice saving
        if (OrderTransNo.length() > 0) {
            Trans OrderTrans = this.getTransByNumberType(OrderTransNo, 11);
            //get order's invoioce status 0,1,2
            int InvoiceStatus = this.getOrderInvoiceStatus(OrderTrans);
            //get order's invoioce pay status 0,1,2
            //save invoice status if not 0
            this.updateOrderStatus(OrderTrans.getTransactionId(), "is_invoiced", InvoiceStatus);
        }
    }

    public void updateTransCECcallFromOpenBalance(long aTransactionId, Trans aTrans, TransBean aTransBean, AccJournal aAccJournal, AccJournalBean aAccJournalBean, TransItemBean aTransItemBean, TransactorBean aTransactorBean) {
        //do before update
        Trans trans = this.getTrans(aTransactionId);
        List<TransItem> transitems = new TransItemBean().getTransItemsByTransactionId(aTransactionId);
        String PayNumbers = new PayBean().getPaysByTransactionStr(aTransactionId);
        if (null == trans) {
            //do nothing
        } else if (trans.getGrandTotal() <= 0) {
            //do nothing
        } else if (PayNumbers.length() > 0) {
            String msg = "First Cancel the following payments recorded against the transaction; Pay Number(s):" + PayNumbers;
            FacesContext.getCurrentInstance().addMessage("Cancel Opening Balance", new FacesMessage(msg));
        } else {
            //update
            int aStoreId = new GeneralUserSetting().getCurrentStore().getStoreId();
            int aTransTypeId = trans.getTransactionTypeId();
            int aTransReasonId = trans.getTransactionReasonId();
            //reset trans
            trans.setGrandTotal(0);
            //reset trans items
            if (null == transitems || transitems.size() == 0) {
                //do nothing
            } else {
                transitems.get(0).setAmountExcVat(0);
                transitems.get(0).setAmountIncVat(0);
                transitems.get(0).setAmount(0);
            }
            this.updateTransCEC("PARENT", aStoreId, aTransTypeId, aTransReasonId, "", trans, transitems, null);
            //do after update
            this.reportOpenBalanceDetail(aTrans, aTransBean, aAccJournal, aAccJournalBean, aTransItemBean, aTransactorBean);
        }
    }

    public void updateTransCECcallFromGDN(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans aNewTrans, List<TransItem> aNewTransItems, Pay aPay) {
        long aChoiceId = 99;
        int TransTypeId = 0;
        aChoiceId = aNewTrans.getSite_id();
        if (aChoiceId == 1) {
            TransTypeId = 2;
        } else if (aChoiceId == 0) {
            TransTypeId = 11;
        } else {
            TransTypeId = 0;
        }
        //get some details
        String XTransNo = aNewTrans.getTransactionRef();
        //save
        this.updateTransCEC(aLevel, aStoreId, aTransTypeId, aTransReasonId, aSaleType, aNewTrans, aNewTransItems, aPay);
        //update a few things needed after GDN saving
        if (XTransNo.length() > 0 && TransTypeId > 0 && TransTypeId == 11) {
            Trans XTrans = this.getTransByNumberType(XTransNo, TransTypeId);
            //get order's GDN status 0,1,2
            int DeliveryStatus = this.getOrderDeliveryStatus(XTrans);
            //save GDN status if not 0
            this.updateOrderStatus(XTrans.getTransactionId(), "is_delivered", DeliveryStatus);
        }
    }

    public void updateTransCEC(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans aNewTrans, List<TransItem> aNewTransItems, Pay aPay) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        //1. copy
        //2. reverse stock(for TIs)
        //3. update trans item
        //4. reverse Tra(for ledgers, etc)
        //5. update trans
        TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
        TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        Store store = new StoreBean().getStore(aStoreId);
        String ValidationMessage = this.validateTransCEC(aStoreId, aTransTypeId, aTransReasonId, aSaleType, aNewTrans, aNewTransItems, null, null);

        String sql = null;
        String msg = "";
        long TransHistId = 0;
        long PayHistId = 0;
        long newPayId = 0;
        boolean isTransCopySuccess = false;
        boolean isTransItemCopySuccess = false;
        boolean isTransUpdateSuccess = false;
        boolean isTransItemReverseSuccess = false;
        boolean isPayCopySuccess = false;
        boolean isPayTransCopySuccess = false;
        boolean isPayUpdateSuccess = false;
        Pay oldPay = new Pay();
        Pay newPay = new Pay();
        Pay savedpay = null;
        Trans savedtrans = null;
        int hasReversed = 0;

        //TransactionType tt = new TransactionTypeBean().getTransactionType(aNewTrans.getTransactionTypeId());
        TransItemBean TransItemBean = new TransItemBean();
        Trans OldTrans = new Trans();
        OldTrans = new TransBean().getTrans(aNewTrans.getTransactionId());
        List<TransItem> OldTransItems = TransItemBean.getTransItemsByTransactionId(aNewTrans.getTransactionId());
        if (aTransTypeId == 76) {//Open Balance
            //do nothing
        } else {
            this.setTransTotalsAndUpdateCEC(aTransTypeId, aTransReasonId, OldTrans, new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId()));
        }
        //first clear current session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        switch (aLevel) {
            case "PARENT":
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                httpSession.setAttribute("CURRENT_PAY_ID", 0);
                break;
            case "CHILD":
                httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", 0);
                httpSession.setAttribute("CURRENT_PAY_ID_CHILD", 0);
                break;
        }

        if (ValidationMessage.length() > 0) {
            switch (aLevel) {
                case "PARENT":
                    this.setActionMessage(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                    break;
                case "CHILD":
                    this.setActionMessageChild(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                    break;
            }
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, ValidationMessage)));
        } else {
            //Copy Trans
            sql = "{call sp_copy_transaction(?,?,?)}";
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    CallableStatement cs = conn.prepareCall(sql);) {
                cs.setLong("in_transaction_id", aNewTrans.getTransactionId());
                cs.setString("in_hist_flag", "Edit");
                cs.registerOutParameter("out_transaction_hist_id", VARCHAR);
                cs.executeUpdate();
                TransHistId = cs.getLong("out_transaction_hist_id");
                isTransCopySuccess = true;
            } catch (Exception e) {
                isTransCopySuccess = false;
                LOGGER.log(Level.ERROR, e);
            }
            //Copy TransItem
            if (isTransCopySuccess) {
                List<TransItem> TransItemsToCopy = new ArrayList<>();
                TransItemsToCopy = new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId());
                int i = 0;
                int n = TransItemsToCopy.size();
                //now copy item by item
                sql = "{call sp_copy_transaction_item(?,?,?)}";
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        CallableStatement cs = conn.prepareCall(sql);) {
                    while (i < n) {
                        cs.setLong("in_transaction_id", aNewTrans.getTransactionId());
                        cs.setLong("in_transaction_hist_id", TransHistId);
                        cs.setLong("in_transaction_item_id", TransItemsToCopy.get(i).getTransactionItemId());
                        cs.executeUpdate();
                        i = i + 1;
                    }
                    isTransItemCopySuccess = true;
                } catch (Exception e) {
                    isTransItemCopySuccess = false;
                    LOGGER.log(Level.ERROR, e);
                }
            }

            //Update by Reversing trans items qty differences
            if (isTransItemCopySuccess) {
                TransItemBean tib = new TransItemBean();
                if (aTransTypeId == 76) {//Open Balance
                    if (tib.updateTransItemCECOpenBalance(aNewTransItems.get(0)) == 1) {
                        isTransItemReverseSuccess = true;
                    } else {
                        isTransItemReverseSuccess = false;
                    }
                } else {
                    isTransItemReverseSuccess = tib.updateTransItemsCEC(aNewTrans.getTransactionId(), TransHistId, aNewTransItems);
                }
            }
            //update trans
            if (isTransCopySuccess && isTransItemCopySuccess && isTransItemReverseSuccess) {
                isTransUpdateSuccess = this.updateTransactionTable(aNewTrans);
                switch (aLevel) {
                    case "PARENT":
                        httpSession.setAttribute("CURRENT_TRANSACTION_ID", aNewTrans.getTransactionId());
                        break;
                    case "CHILD":
                        httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", aNewTrans.getTransactionId());
                        break;
                }
            }
            //Update the first Payment
            if (transtype.getTransactionTypeName().equals("SALE INVOICE") || transtype.getTransactionTypeName().equals("PURCHASE INVOICE") || transtype.getTransactionTypeName().equals("EXPENSE ENTRY") || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
                oldPay = new PayBean().getTransactionFirstPayByTransNo(aNewTrans.getTransactionNumber());
                newPay = new PayBean().getTransactionFirstPayByTransNo(aNewTrans.getTransactionNumber());
                if (null != newPay) {
                    //copy pay and pay trans
                    //copy pay
                    sql = "{call sp_copy_pay(?,?,?)}";
                    try (
                            Connection conn = DBConnection.getMySQLConnection();
                            CallableStatement cs = conn.prepareCall(sql);) {
                        cs.setLong("in_pay_id", newPay.getPayId());
                        cs.setString("in_hist_flag", "Edit");
                        cs.registerOutParameter("out_pay_hist_id", VARCHAR);
                        cs.executeUpdate();
                        PayHistId = cs.getLong("out_pay_hist_id");
                        isPayCopySuccess = true;
                    } catch (Exception e) {
                        isPayCopySuccess = false;
                        LOGGER.log(Level.ERROR, e);
                    }
                    //copy pay trans
                    if (isPayCopySuccess) {
                        List<PayTrans> PayTranssToCopy = new ArrayList<>();
                        PayTranssToCopy = new PayTransBean().getPayTranssByPayId(newPay.getPayId());
                        int i = 0;
                        int n = PayTranssToCopy.size();
                        //now copy item by item
                        sql = "{call sp_copy_pay_trans(?,?,?)}";
                        try (
                                Connection conn = DBConnection.getMySQLConnection();
                                CallableStatement cs = conn.prepareCall(sql);) {
                            while (i < n) {
                                cs.setLong("in_pay_id", newPay.getPayId());
                                cs.setLong("in_pay_hist_id", PayHistId);
                                cs.setLong("in_pay_trans_id", PayTranssToCopy.get(i).getPayTransId());
                                cs.executeUpdate();
                                i = i + 1;
                            }
                            isPayTransCopySuccess = true;
                        } catch (Exception e) {
                            isPayTransCopySuccess = false;
                            LOGGER.log(Level.ERROR, e);
                        }
                    }
                    if (isTransCopySuccess && isTransItemCopySuccess && isTransItemReverseSuccess && isTransUpdateSuccess && isPayCopySuccess && isPayTransCopySuccess) {
                        newPay.setPaidAmount(aNewTrans.getAmountTendered());
                        newPay.setPointsSpentAmount(0);//aNewTrans.getSpendPointsAmount()
                        newPay.setPointsSpent(0);//aNewTrans.getSpendPoints()
                        newPay.setEditUserDetailId(new GeneralUserSetting().getCurrentUser().getUserDetailId());
                        newPayId = new PayBean().payInsertUpdate(newPay);
                        switch (aLevel) {
                            case "PARENT":
                                httpSession.setAttribute("CURRENT_PAY_ID", newPayId);
                                break;
                            case "CHILD":
                                httpSession.setAttribute("CURRENT_PAY_ID_CHILD", newPayId);
                                break;
                        }
                        //insert PayTrans
                        PayTrans paytrans = null;
                        try {
                            paytrans = new PayTransBean().getPayTranssByPayId(newPayId).get(0);
                        } catch (NullPointerException | IndexOutOfBoundsException npe) {
                            paytrans = null;
                        }
                        if (null != paytrans) {
                            paytrans.setTransPaidAmount(newPay.getPaidAmount());
                            new PayTransBean().savePayTrans(paytrans);
                        }
                    }
                }
            }
            //Reverse and Insert Journal
            if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
                savedpay = null;
                savedtrans = null;
                hasReversed = 0;
                List<TransItem> transitems = null;
                if (newPayId > 0) {
                    savedpay = new PayBean().getPay(newPayId);
                }
                savedtrans = new TransBean().getTrans(aNewTrans.getTransactionId());
                transitems = new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId());
                hasReversed = new AccJournalBean().postJournalReverse(OldTrans, oldPay);
                if (hasReversed == 1) {
                    savedtrans.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
                    new AccJournalBean().postJournalSaleInvoice(savedtrans, transitems, savedpay, new AccPeriodBean().getAccPeriod(savedtrans.getTransactionDate()).getAccPeriodId());
                }
            }
            if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName())) {
                savedpay = new Pay();
                savedtrans = new Trans();
                hasReversed = 0;
                List<TransItem> transitems = new ArrayList<>();
                if (newPayId > 0) {
                    savedpay = new PayBean().getPay(newPayId);
                }
                savedtrans = new TransBean().getTrans(aNewTrans.getTransactionId());
                transitems = new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId());
                hasReversed = new AccJournalBean().postJournalReverse(OldTrans, oldPay);
                if (hasReversed == 1) {
                    long PostJobId = 0;
                    savedtrans.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
                    PostJobId = new AccJournalBean().postJournalPurchaseInvoice(savedtrans, transitems, savedpay, new AccPeriodBean().getAccPeriod(savedtrans.getTransactionDate()).getAccPeriodId());
                    //Depriciation for Asset Trans
                    if (savedtrans.getTransactionReasonId() == 29) {
                        Stock assetstock = null;
                        AccDepScheduleBean depschbean = new AccDepScheduleBean();
                        int assetstoreid = savedtrans.getStoreId();
                        for (TransItem assetti : transitems) {
                            assetstock = new StockBean().getStock(assetstoreid, assetti.getItemId(), assetti.getBatchno(), assetti.getCodeSpecific(), assetti.getDescSpecific());
                            //Build:1-20-000-020 Land:1-20-000-010 but let us exclude LAND
                            if (null != assetstock && assetstock.getAccountCode().length() > 0 && !assetstock.getAccountCode().equals("1-20-000-010")) {
                                //1. reverse posted 1st year
                                AccDepSchedule PostedAccDepSchedule = depschbean.getAccDepScheduleByYearPosted(assetstock.getStockId(), 1);
                                AccPeriod accprd4trans = null;
                                try {
                                    accprd4trans = new AccPeriodBean().getAccPeriod(aNewTrans.getTransactionDate());
                                } catch (Exception e) {
                                    accprd4trans = null;
                                }
                                if (null == PostedAccDepSchedule || null == accprd4trans) {
                                    //do nothing
                                } else {
                                    if (PostedAccDepSchedule.getDepForAccPeriodId() == accprd4trans.getAccPeriodId()) {
                                        new AccJournalBean().postJournalDepreciateAssetREVERSE(aNewTrans, assetstock, PostedAccDepSchedule, accprd4trans.getAccPeriodId(), PostJobId);
                                        PostedAccDepSchedule.setPost_status(0);
                                        new AccDepScheduleBean().updateAccDepSchedule(PostedAccDepSchedule);
                                    }
                                }

                                //2. delete all depreciation schedules un posted
                                new AccDepScheduleBean().deleteAccDepScheduleUnposted(assetstock.getStockId());

                                //3. Post new schedules
                                depschbean.insertAccDepSchedules(depschbean.calcAccDepSchedules(assetstock));

                                //4. post to the ledger the first year's depriciation
                                AccDepSchedule aAccDepSchedule = depschbean.getAccDepScheduleByYear(assetstock.getStockId(), 1);
                                AccPeriod accprd4firstpost = null;
                                accprd4trans = null;
                                try {
                                    accprd4firstpost = new AccPeriodBean().getAccPeriod(assetstock.getDepStartDate());
                                } catch (Exception e) {
                                    accprd4firstpost = null;
                                }
                                try {
                                    accprd4trans = new AccPeriodBean().getAccPeriod(aNewTrans.getTransactionDate());
                                } catch (Exception e) {
                                    accprd4trans = null;
                                }
                                if (null == accprd4firstpost || null == accprd4trans) {
                                    //do nothing; means;
                                    //dep start date is for not yet set acc period OR
                                    //current date doesnt have correspondiong acc period 
                                } else {
                                    if (accprd4firstpost.getAccPeriodId() == accprd4trans.getAccPeriodId()) {
                                        new AccJournalBean().postJournalDepreciateAsset(aNewTrans, assetstock, aAccDepSchedule, accprd4firstpost.getAccPeriodId(), PostJobId);
                                        aAccDepSchedule.setDepForAccPeriodId(accprd4firstpost.getAccPeriodId());
                                        aAccDepSchedule.setDepFromDate(accprd4firstpost.getStartDate());
                                        aAccDepSchedule.setDepToDate(accprd4firstpost.getEndDate());
                                        aAccDepSchedule.setPost_status(1);
                                        new AccDepScheduleBean().updateAccDepSchedule(aAccDepSchedule);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if ("DISPOSE STOCK".equals(transtype.getTransactionTypeName())) {
                savedpay = null;
                savedtrans = null;
                hasReversed = 0;
                List<TransItem> transitems = null;
                if (newPayId > 0) {
                    savedpay = new PayBean().getPay(newPayId);
                }
                savedtrans = new TransBean().getTrans(aNewTrans.getTransactionId());
                transitems = new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId());
                hasReversed = new AccJournalBean().postJournalReverse(OldTrans, oldPay);
                if (hasReversed == 1) {
                    savedtrans.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
                    new AccJournalBean().postJournalDisposeStock(savedtrans, new AccPeriodBean().getAccPeriod(savedtrans.getTransactionDate()).getAccPeriodId());
                }
            }
            if ("STOCK CONSUMPTION".equals(transtype.getTransactionTypeName())) {
                savedpay = null;
                savedtrans = null;
                hasReversed = 0;
                List<TransItem> transitems = null;
                if (newPayId > 0) {
                    savedpay = new PayBean().getPay(newPayId);
                }
                savedtrans = new TransBean().getTrans(aNewTrans.getTransactionId());
                transitems = new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId());
                hasReversed = new AccJournalBean().postJournalReverse(OldTrans, oldPay);
                if (hasReversed == 1) {
                    savedtrans.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
                    new AccJournalBean().postJournalStockConsumption(savedtrans, new AccPeriodBean().getAccPeriod(savedtrans.getTransactionDate()).getAccPeriodId());
                }
            }
            if ("EXPENSE ENTRY".equals(transtype.getTransactionTypeName())) {
                savedpay = new Pay();
                savedtrans = new Trans();
                hasReversed = 0;
                List<TransItem> transitems = new ArrayList<>();
                if (newPayId > 0) {
                    savedpay = new PayBean().getPay(newPayId);
                }
                savedtrans = new TransBean().getTrans(aNewTrans.getTransactionId());
                transitems = new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId());
                hasReversed = new AccJournalBean().postJournalReverse(OldTrans, oldPay);
                if (hasReversed == 1) {
                    savedtrans.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
                    new AccJournalBean().postJournalExpenseEntry(savedtrans, transitems, savedpay, new AccPeriodBean().getAccPeriod(savedtrans.getTransactionDate()).getAccPeriodId());
                }
            }
            if ("OPENING BALANCE".equals(transtype.getTransactionTypeName())) {
                new AccJournalBean().postJournalOpenBalanceCANCEL(OldTrans, OldTransItems, new AccPeriodBean().getAccPeriod(OldTrans.getTransactionDate()).getAccPeriodId());
            }
            //start-insert credit/debit note
            long SavedCrDrNoteTransId = 0;
            int ExistCountDrCrNotes = new CreditDebitNoteBean().getCountDebitAndCreditNotes(OldTrans.getTransactionNumber());
            if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
                //insert note
                SavedCrDrNoteTransId = new CreditDebitNoteBean().saveCreditDebitNote(OldTrans, aNewTrans, OldTransItems, aNewTransItems, 0);
                //SMbi API insert loyalty transaction for the note
                String scope = new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_SMBI_SCOPE").getParameter_value();
                if (SavedCrDrNoteTransId > 0 && aNewTrans.getCardNumber().length() > 0 && (scope.isEmpty() || scope.contains("LOYALTY"))) {
                    int x = new Loyalty_transactionBean().insertLoyalty_transaction_cr_dr(SavedCrDrNoteTransId);
                }
            }
            //end-insert credit/debit note
            TransItemBean = null;
            //clean stock
            StockBean.deleteZeroQtyStock();
            if (isTransCopySuccess && isTransItemCopySuccess && isTransItemReverseSuccess && isTransUpdateSuccess) {
                switch (aLevel) {
                    case "PARENT":
                        this.setActionMessage(ub.translateWordsInText(BaseName, "Saved Successfully ( Transaction Id : " + new GeneralUserSetting().getCurrentTransactionId() + " )"));
                        break;
                    case "CHILD":
                        this.setActionMessageChild(ub.translateWordsInText(BaseName, "Saved Successfully ( Transaction Id : " + new GeneralUserSetting().getCurrentTransactionIdChild() + " )"));
                        break;
                }
            }
            //Refresh Print output
            new OutputDetailBean().refreshOutput(aLevel, "");
            //Refresh stock alerts
            new UtilityBean().refreshAlertsThread();
            //TAX API
            if (aTransTypeId == 2 && new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value().length() > 0) {//SALES INVOICE
                int IsThreadOn = 0;
                try {
                    IsThreadOn = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_TAX_THREAD_ON").getParameter_value());
                } catch (Exception e) {
                    //
                }
                Transaction_tax_map PrevSyncedTaxInvoice = new Transaction_tax_mapBean().getTransaction_tax_map(OldTrans.getTransactionId(), aTransTypeId);
                if (null == PrevSyncedTaxInvoice) {
                    //do nothing, original record was not synced/found, that cannot tbe updated
                } else {
                    if (ExistCountDrCrNotes >= 1) {
                        new Transaction_tax_mapBean().markTransaction_tax_mapUpdated_more_than_once(PrevSyncedTaxInvoice);
                    } else {
                        if (IsThreadOn == 0) {
                            if (aNewTrans.getGrandTotal() > OldTrans.getGrandTotal() || aNewTrans.getTotalVat() > OldTrans.getTotalVat()) {//Debit note
                                new InvoiceBean().submitDebitNote(SavedCrDrNoteTransId, 83);
                            } else if (aNewTrans.getGrandTotal() < OldTrans.getGrandTotal() || aNewTrans.getTotalVat() < OldTrans.getTotalVat()) {//Credit note
                                new InvoiceBean().submitCreditNote(SavedCrDrNoteTransId, 82);
                            }
                        } else if (IsThreadOn == 1) {
                            if (aNewTrans.getGrandTotal() > OldTrans.getGrandTotal() || aNewTrans.getTotalVat() > OldTrans.getTotalVat()) {//Debit note
                                new InvoiceBean().submitDebitNoteThread(SavedCrDrNoteTransId, 83);
                            } else if (aNewTrans.getGrandTotal() < OldTrans.getGrandTotal() || aNewTrans.getTotalVat() < OldTrans.getTotalVat()) {//Credit note
                                new InvoiceBean().submitCreditNoteThread(SavedCrDrNoteTransId, 82);
                            }
                        }
                    }
                }
            }
            //SMbi API
            if (aTransTypeId == 2 && new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_SMBI_URL").getParameter_value().length() > 0) {
                int CrDrTransTypeId = 0;
                if (aNewTrans.getGrandTotal() > OldTrans.getGrandTotal() || aNewTrans.getTotalVat() > OldTrans.getTotalVat()) {//Debit note
                    CrDrTransTypeId = 83;
                } else if (aNewTrans.getGrandTotal() < OldTrans.getGrandTotal() || aNewTrans.getTotalVat() < OldTrans.getTotalVat()) {//Credit note
                    CrDrTransTypeId = 82;
                }
                new Transaction_smbi_mapBean().insertTransaction_smbi_mapCallThread(SavedCrDrNoteTransId, CrDrTransTypeId);
            }
            //Update Total Paid for Sales/Purchase Invoice
            if (aTransTypeId == 2 || aTransTypeId == 1) {
                new PayTransBean().updateTransTotalPaid(aNewTrans.getTransactionId());
            }
        }
    }

    public void raiseCreditNote(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans aNewTrans, List<TransItem> aNewTransItems, Pay aPay, double aRefundAmount, double aReverseCreditAmt) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
        TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        Store store = new StoreBean().getStore(aStoreId);
        String ValidationMessage = new CreditDebitNoteBean().validateCreditNote(aStoreId, aNewTrans, aNewTransItems);
        String sql = null;
        String msg = "";
        long TransHistId = 0;
        long PayHistId = 0;
        long newPayId = 0;
        boolean isTransItemReverseSuccess = false;
        //Pay oldPay = new Pay();
        //Pay newPay = new Pay();
        //Pay savedpay = null;
        //Trans SavedCrNote = null;
        int hasReversed = 0;
        long SavedCrDrNoteTransId = 0;

        //TransactionType tt = new TransactionTypeBean().getTransactionType(aNewTrans.getTransactionTypeId());
        TransItemBean TransItemBean = new TransItemBean();
        Trans OldTrans = new Trans();
        OldTrans = new TransBean().getTrans(aNewTrans.getTransactionId());
        List<TransItem> OldTransItems = TransItemBean.getTransItemsByTransactionId(aNewTrans.getTransactionId());
        if (aTransTypeId == 76) {//Open Balance
            //do nothing
        } else {
            this.setTransTotalsAndUpdateCEC(aTransTypeId, aTransReasonId, OldTrans, new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId()));
        }
        //first clear current session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        switch (aLevel) {
            case "PARENT":
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                httpSession.setAttribute("CURRENT_PAY_ID", 0);
                break;
            case "CHILD":
                httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", 0);
                httpSession.setAttribute("CURRENT_PAY_ID_CHILD", 0);
                break;
        }
        if (ValidationMessage.length() > 0) {
            switch (aLevel) {
                case "PARENT":
                    this.setActionMessage(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                    break;
                case "CHILD":
                    this.setActionMessageChild(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                    break;
            }
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, ValidationMessage)));
        } else {
            int ExistCountDrCrNotes = new CreditDebitNoteBean().getCountDebitAndCreditNotes(OldTrans.getTransactionNumber());
            //insert note
            SavedCrDrNoteTransId = new CreditDebitNoteBean().saveCreditDebitNote(OldTrans, aNewTrans, OldTransItems, aNewTransItems, 1);
            //reverse stock
            if (SavedCrDrNoteTransId > 0) {
                TransItemBean tib = new TransItemBean();
                //isTransItemReverseSuccess = tib.reverseTransItemsCEC(OldTrans, aNewTrans, OldTransItems, aNewTransItems);
                isTransItemReverseSuccess = new CreditDebitNoteBean().stockAdjustCrDrNote(SavedCrDrNoteTransId);
            }
            //journal
            if (SavedCrDrNoteTransId > 0 && isTransItemReverseSuccess) {
                new AccJournalBean().postJournalCreditNote(SavedCrDrNoteTransId, new AccPeriodBean().getAccPeriod(new CompanySetting().getCURRENT_SERVER_DATE()).getAccPeriodId(), aRefundAmount, aReverseCreditAmt);
                //session
                switch (aLevel) {
                    case "PARENT":
                        httpSession.setAttribute("CURRENT_TRANSACTION_ID", SavedCrDrNoteTransId);
                        httpSession.setAttribute("CURRENT_PAY_ID", 0);
                        break;
                    case "CHILD":
                        httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", SavedCrDrNoteTransId);
                        httpSession.setAttribute("CURRENT_PAY_ID_CHILD", 0);
                        break;
                }
                //SMbi API insert loyalty transaction for the note
                String scope = new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_SMBI_SCOPE").getParameter_value();
                if (SavedCrDrNoteTransId > 0 && aNewTrans.getCardNumber().length() > 0 && (scope.isEmpty() || scope.contains("LOYALTY"))) {
                    int x = new Loyalty_transactionBean().insertLoyalty_transaction_cr_dr(SavedCrDrNoteTransId);
                }
                TransItemBean = null;
                //clean stock
                StockBean.deleteZeroQtyStock();
                switch (aLevel) {
                    case "PARENT":
                        this.setActionMessage(ub.translateWordsInText(BaseName, "Saved Successfully ( Transaction Id : " + new GeneralUserSetting().getCurrentTransactionId() + " )"));
                        break;
                    case "CHILD":
                        this.setActionMessageChild(ub.translateWordsInText(BaseName, "Saved Successfully ( Transaction Id : " + new GeneralUserSetting().getCurrentTransactionIdChild() + " )"));
                        break;
                }
                //Refresh Print output
                new OutputDetailBean().refreshOutputCrDr(aLevel, "");
                //Refresh stock alerts
                new UtilityBean().refreshAlertsThread();
                //TAX API
                if (new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value().length() > 0) {//SALES INVOICE
                    int IsThreadOn = 0;
                    try {
                        IsThreadOn = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_TAX_THREAD_ON").getParameter_value());
                    } catch (Exception e) {
                        //
                    }
                    Transaction_tax_map PrevSyncedTaxInvoice = new Transaction_tax_mapBean().getTransaction_tax_map(OldTrans.getTransactionId(), aTransTypeId);
                    if (null == PrevSyncedTaxInvoice) {
                        //do nothing, original record was not synced/found, that cannot tbe updated
                    } else {
                        if (ExistCountDrCrNotes >= 1) {
                            new Transaction_tax_mapBean().markTransaction_tax_mapUpdated_more_than_once(PrevSyncedTaxInvoice);
                        } else {
                            if (IsThreadOn == 0) {
                                if (aNewTrans.getGrandTotal() > OldTrans.getGrandTotal() || aNewTrans.getTotalVat() > OldTrans.getTotalVat()) {//Debit note
                                    new InvoiceBean().submitDebitNote(SavedCrDrNoteTransId, 83);
                                } else if (aNewTrans.getGrandTotal() < OldTrans.getGrandTotal() || aNewTrans.getTotalVat() < OldTrans.getTotalVat()) {//Credit note
                                    new InvoiceBean().submitCreditNote(SavedCrDrNoteTransId, 82);
                                }
                            } else if (IsThreadOn == 1) {
                                if (aNewTrans.getGrandTotal() > OldTrans.getGrandTotal() || aNewTrans.getTotalVat() > OldTrans.getTotalVat()) {//Debit note
                                    new InvoiceBean().submitDebitNoteThread(SavedCrDrNoteTransId, 83);
                                } else if (aNewTrans.getGrandTotal() < OldTrans.getGrandTotal() || aNewTrans.getTotalVat() < OldTrans.getTotalVat()) {//Credit note
                                    new InvoiceBean().submitCreditNoteThread(SavedCrDrNoteTransId, 82);
                                }
                            }
                        }
                    }
                }
                //SMbi API
                if (new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_SMBI_URL").getParameter_value().length() > 0) {
                    int CrDrTransTypeId = 0;
                    if (aNewTrans.getGrandTotal() > OldTrans.getGrandTotal() || aNewTrans.getTotalVat() > OldTrans.getTotalVat()) {//Debit note
                        CrDrTransTypeId = 83;
                    } else if (aNewTrans.getGrandTotal() < OldTrans.getGrandTotal() || aNewTrans.getTotalVat() < OldTrans.getTotalVat()) {//Credit note
                        CrDrTransTypeId = 82;
                    }
                    new Transaction_smbi_mapBean().insertTransaction_smbi_mapCallThread(SavedCrDrNoteTransId, CrDrTransTypeId);
                }
                //Update Total Paid for Sales/Purchase Invoice
                if (aTransTypeId == 2 || aTransTypeId == 1) {
                    new PayTransBean().updateTransTotalPaid(aNewTrans.getTransactionId());
                }
            }
        }
    }

    public void raiseDebitNote(String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans aNewTrans, List<TransItem> aNewTransItems, Pay aPay) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
        TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        Store store = new StoreBean().getStore(aStoreId);
        String ValidationMessage = new CreditDebitNoteBean().validateDebitNote(aStoreId, aNewTrans, aNewTransItems);
        String sql = null;
        String msg = "";
        long TransHistId = 0;
        long PayHistId = 0;
        long newPayId = 0;
        boolean isTransItemReverseSuccess = false;
        //Pay oldPay = new Pay();
        //Pay newPay = new Pay();
        //Pay savedpay = null;
        //Trans SavedCrNote = null;
        int hasReversed = 0;
        long SavedCrDrNoteTransId = 0;

        //TransactionType tt = new TransactionTypeBean().getTransactionType(aNewTrans.getTransactionTypeId());
        TransItemBean TransItemBean = new TransItemBean();
        Trans OldTrans = new Trans();
        OldTrans = new TransBean().getTrans(aNewTrans.getTransactionId());
        List<TransItem> OldTransItems = TransItemBean.getTransItemsByTransactionId(aNewTrans.getTransactionId());
        if (aTransTypeId == 76) {//Open Balance
            //do nothing
        } else {
            this.setTransTotalsAndUpdateCEC(aTransTypeId, aTransReasonId, OldTrans, new TransItemBean().getTransItemsByTransactionId(aNewTrans.getTransactionId()));
        }
        //first clear current session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        switch (aLevel) {
            case "PARENT":
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                httpSession.setAttribute("CURRENT_PAY_ID", 0);
                break;
            case "CHILD":
                httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", 0);
                httpSession.setAttribute("CURRENT_PAY_ID_CHILD", 0);
                break;
        }
        if (ValidationMessage.length() > 0) {
            switch (aLevel) {
                case "PARENT":
                    this.setActionMessage(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                    break;
                case "CHILD":
                    this.setActionMessageChild(ub.translateWordsInText(BaseName, "Transaction Not Saved"));
                    break;
            }
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, ValidationMessage)));
        } else {
            int ExistCountDrCrNotes = new CreditDebitNoteBean().getCountDebitAndCreditNotes(OldTrans.getTransactionNumber());
            //insert note
            SavedCrDrNoteTransId = new CreditDebitNoteBean().saveCreditDebitNote(OldTrans, aNewTrans, OldTransItems, aNewTransItems, 1);
            //reverse stock
            if (SavedCrDrNoteTransId > 0) {
                TransItemBean tib = new TransItemBean();
                //isTransItemReverseSuccess = tib.reverseTransItemsCEC(OldTrans, aNewTrans, OldTransItems, aNewTransItems);
                isTransItemReverseSuccess = new CreditDebitNoteBean().stockAdjustCrDrNote(SavedCrDrNoteTransId);
            }
            //journal
            if (SavedCrDrNoteTransId > 0 && isTransItemReverseSuccess) {
                new AccJournalBean().postJournalDebitNote(SavedCrDrNoteTransId, new AccPeriodBean().getAccPeriod(new CompanySetting().getCURRENT_SERVER_DATE()).getAccPeriodId());
                //session
                switch (aLevel) {
                    case "PARENT":
                        httpSession.setAttribute("CURRENT_TRANSACTION_ID", SavedCrDrNoteTransId);
                        httpSession.setAttribute("CURRENT_PAY_ID", 0);
                        break;
                    case "CHILD":
                        httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", SavedCrDrNoteTransId);
                        httpSession.setAttribute("CURRENT_PAY_ID_CHILD", 0);
                        break;
                }
                //SMbi API insert loyalty transaction for the note
                String scope = new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_SMBI_SCOPE").getParameter_value();
                if (SavedCrDrNoteTransId > 0 && aNewTrans.getCardNumber().length() > 0 && (scope.isEmpty() || scope.contains("LOYALTY"))) {
                    int x = new Loyalty_transactionBean().insertLoyalty_transaction_cr_dr(SavedCrDrNoteTransId);
                }
                TransItemBean = null;
                //clean stock
                StockBean.deleteZeroQtyStock();
                switch (aLevel) {
                    case "PARENT":
                        this.setActionMessage(ub.translateWordsInText(BaseName, "Saved Successfully ( Transaction Id : " + new GeneralUserSetting().getCurrentTransactionId() + " )"));
                        break;
                    case "CHILD":
                        this.setActionMessageChild(ub.translateWordsInText(BaseName, "Saved Successfully ( Transaction Id : " + new GeneralUserSetting().getCurrentTransactionIdChild() + " )"));
                        break;
                }
                //Refresh Print output
                new OutputDetailBean().refreshOutputCrDr(aLevel, "");
                //Refresh stock alerts
                new UtilityBean().refreshAlertsThread();
                //TAX API
                if (new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value().length() > 0) {
                    int IsThreadOn = 0;
                    try {
                        IsThreadOn = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_TAX_THREAD_ON").getParameter_value());
                    } catch (Exception e) {
                        //
                    }
                    Transaction_tax_map PrevSyncedTaxInvoice = new Transaction_tax_mapBean().getTransaction_tax_map(OldTrans.getTransactionId(), aTransTypeId);
                    if (null == PrevSyncedTaxInvoice) {
                        //do nothing, original record was not synced/found, that cannot tbe updated
                    } else {
                        if (ExistCountDrCrNotes >= 1) {
                            new Transaction_tax_mapBean().markTransaction_tax_mapUpdated_more_than_once(PrevSyncedTaxInvoice);
                        } else {
                            if (IsThreadOn == 0) {
                                if (aNewTrans.getGrandTotal() > OldTrans.getGrandTotal() || aNewTrans.getTotalVat() > OldTrans.getTotalVat()) {//Debit note
                                    new InvoiceBean().submitDebitNote(SavedCrDrNoteTransId, 83);
                                } else if (aNewTrans.getGrandTotal() < OldTrans.getGrandTotal() || aNewTrans.getTotalVat() < OldTrans.getTotalVat()) {//Credit note
                                    new InvoiceBean().submitCreditNote(SavedCrDrNoteTransId, 82);
                                }
                            } else if (IsThreadOn == 1) {
                                if (aNewTrans.getGrandTotal() > OldTrans.getGrandTotal() || aNewTrans.getTotalVat() > OldTrans.getTotalVat()) {//Debit note
                                    new InvoiceBean().submitDebitNoteThread(SavedCrDrNoteTransId, 83);
                                } else if (aNewTrans.getGrandTotal() < OldTrans.getGrandTotal() || aNewTrans.getTotalVat() < OldTrans.getTotalVat()) {//Credit note
                                    new InvoiceBean().submitCreditNoteThread(SavedCrDrNoteTransId, 82);
                                }
                            }
                        }
                    }
                }
                //SMbi API
                if (new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_SMBI_URL").getParameter_value().length() > 0) {
                    int CrDrTransTypeId = 0;
                    if (aNewTrans.getGrandTotal() > OldTrans.getGrandTotal() || aNewTrans.getTotalVat() > OldTrans.getTotalVat()) {//Debit note
                        CrDrTransTypeId = 83;
                    } else if (aNewTrans.getGrandTotal() < OldTrans.getGrandTotal() || aNewTrans.getTotalVat() < OldTrans.getTotalVat()) {//Credit note
                        CrDrTransTypeId = 82;
                    }
                    new Transaction_smbi_mapBean().insertTransaction_smbi_mapCallThread(SavedCrDrNoteTransId, CrDrTransTypeId);
                }
                //Update Total Paid for Sales/Purchase Invoice
                if (aTransTypeId == 2 || aTransTypeId == 1) {
                    new PayTransBean().updateTransTotalPaid(aNewTrans.getTransactionId());
                }
            }
        }
    }

    public long getCountTransRecords() {
        String sql = "SELECT COUNT(*) as row_count FROM trans_convert WHERE transaction_id>0 AND convert_status=0";
        ResultSet rs = null;
        long rcds = 0;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    rcds = rs.getLong("row_count");
                } catch (NullPointerException npe) {
                    rcds = 0;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return rcds;
    }

    public long getCountPayRecords() {
        String sql = "SELECT COUNT(*) as row_count FROM pay_convert WHERE convert_status=0 AND pay_reason_id IN(22,25)";
        ResultSet rs = null;
        long rcds = 0;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    rcds = rs.getLong("row_count");
                } catch (NullPointerException npe) {
                    rcds = 0;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return rcds;
    }

    public long getCountOverPayRecords(String aMode) {
        String sql = "";
        if (aMode.equals("FULL")) {
            sql = "SELECT COUNT(*) as row_count FROM aa_trans_not_paid WHERE convert_status=0";
        } else if (aMode.equals("PART")) {
            sql = "SELECT COUNT(*) as row_count FROM aa_trans_not_paid WHERE convert_status=2 and (grand_total-trans_paid_amount)>0";
        }
        ResultSet rs = null;
        long rcds = 0;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    rcds = rs.getLong("row_count");
                } catch (NullPointerException npe) {
                    rcds = 0;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return rcds;
    }

    public int updateTransConvertStatus(long aTransId, int aStatus) {
        String sql = null;
        sql = "UPDATE trans_convert SET convert_status=" + aStatus + " WHERE transaction_id=" + aTransId;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareCall(sql);) {
            if (aTransId > 0) {
                ps.executeUpdate();
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return 0;
        }
    }

    public int updatePayConvertStatus(long aPayId, int aStatus) {
        String sql = null;
        sql = "UPDATE pay_convert SET convert_status=" + aStatus + " WHERE pay_id=" + aPayId;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareCall(sql);) {
            if (aPayId > 0) {
                ps.executeUpdate();
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return 0;
        }
    }

    public int updateOverPayTransConvertStatusFULL(long aTransId, int aStatus) {
        String sql = null;
        sql = "UPDATE aa_trans_not_paid SET convert_status=" + aStatus + " WHERE transaction_id=" + aTransId;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareCall(sql);) {
            if (aTransId > 0) {
                ps.executeUpdate();
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return 0;
        }
    }

    public int updateOverPayTransConvertStatusPART(long aTransId, Pay aPay) {
        String sql = null;
        if (aPay.getStatus() == 1) {
            sql = "UPDATE aa_trans_not_paid SET convert_status=1,trans_paid_amount=(trans_paid_amount+" + aPay.getPaidAmount() + ") WHERE transaction_id=" + aTransId;
        } else if (aPay.getStatus() == 2) {
            sql = "UPDATE aa_trans_not_paid SET convert_status=2,trans_paid_amount=(trans_paid_amount+" + aPay.getPaidAmount() + ") WHERE transaction_id=" + aTransId;
        }
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareCall(sql);) {
            if (aTransId > 0) {
                ps.executeUpdate();
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return 0;
        }
    }

    public int updateOverPayTransConvertStatusPARTFail(long aTransId) {
        String sql = null;
        sql = "UPDATE aa_trans_not_paid SET convert_status=22 WHERE transaction_id=" + aTransId;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareCall(sql);) {
            if (aTransId > 0) {
                ps.executeUpdate();
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return 0;
        }
    }

    public String convertTransToJournalAll() {
        int Loops = 1;
        int loop = 1;
        double TotalRecords = this.getCountTransRecords();
        double RecordsBatchFactor = TotalRecords / 50;
        Loops = (int) Math.ceil(RecordsBatchFactor);
        int i = 0;
        int convert_pass = 0;
        int cstatus = 0;
        Trans cTrans = null;
        while (loop <= Loops) {
            String sql_from = "SELECT * FROM trans_convert WHERE convert_status=0";
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql_from);) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    cTrans = new Trans();
                    this.setTransFromResultset(cTrans, rs);
                    cstatus = this.convertTransToJournal(cTrans);
                    convert_pass = convert_pass + 1;
                    this.updateTransConvertStatus(cTrans.getTransactionId(), cstatus);
                    cTrans = null;
                    i = i + 1;
                }
                rs.close();
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
            //System.out.println(convert_pass + "/" + TotalRecords + " Converted" + " Loop:" + loop + "/" + Loops);
            loop = loop + 1;
        }
        return convert_pass + "/" + TotalRecords + " Converted" + " Loops:" + Loops;
    }

    public String convertTransToJournalAll2() {
        int Loops = 1;
        int loop = 1;
        double TotalRecords = this.getCountTransRecords();
        double RecordsBatchFactor = TotalRecords / 50;
        Loops = (int) Math.ceil(RecordsBatchFactor);
        int i = 0;
        int convert_pass = 0;
        int cstatus = 0;
        Trans cTrans = null;
        long LastId = 0;
        while (loop <= Loops) {
            String sql_from = "SELECT * FROM trans_convert WHERE transaction_id>" + LastId + " AND transaction_id<=" + (LastId + 50);
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql_from);) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    cTrans = new Trans();
                    this.setTransFromResultset(cTrans, rs);
                    cstatus = this.convertTransToJournal(cTrans);
                    convert_pass = convert_pass + 1;
                    LastId = cTrans.getTransactionId();
                    //this.updateTransConvertStatus(cTrans.getTransactionId(), cstatus);
                    cTrans = null;
                    i = i + 1;
                }
                rs.close();
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
            //System.out.println(convert_pass + "/" + TotalRecords + " Converted" + " Loop:" + loop + "/" + Loops);
            loop = loop + 1;
        }
        return convert_pass + "/" + TotalRecords + " Converted" + " Loops:" + Loops;
    }

    public String convertPayToJournalAll() {
        int Loops = 1;
        int loop = 1;
        double TotalRecords = this.getCountPayRecords();
        double RecordsBatchFactor = TotalRecords / 50;
        Loops = (int) Math.ceil(RecordsBatchFactor);
        int i = 0;
        int convert_pass = 0;
        int cstatus = 0;
        Pay cPay = null;
        PayBean cPayBean = new PayBean();
        while (loop <= Loops) {
            String sql_from = "SELECT * FROM pay_convert WHERE convert_status=0 AND pay_reason_id IN(22,25)";
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql_from);) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    cPay = new Pay();
                    cPayBean.setPayFromResultset(cPay, rs);
                    cstatus = this.convertPayToJournal(cPay);
                    convert_pass = convert_pass + 1;
                    this.updatePayConvertStatus(cPay.getPayId(), cstatus);
                    cPay = null;
                    i = i + 1;
                }
                rs.close();
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
            //System.out.println(convert_pass + "/" + TotalRecords + " Converted" + " Loop:" + loop + "/" + Loops);
            loop = loop + 1;
        }
        return convert_pass + "/" + TotalRecords + " Converted" + " Loops:" + Loops;
    }

    public String convertOverPayAll(String aMode) {
        int Loops = 1;
        int loop = 1;
        double TotalRecords = 0;
        double RecordsBatchFactor = 0;
        Loops = 0;
        int i = 0;
        int convert_pass = 0;
        int cstatus = 0;
        Trans cTrans = null;
        TransBean cTransBean = new TransBean();
        double TransPaidAmount = 0;
        if (aMode.equals("FULL")) {
            TotalRecords = this.getCountOverPayRecords(aMode);
            RecordsBatchFactor = TotalRecords / 50;
            Loops = (int) Math.ceil(RecordsBatchFactor);
            while (loop <= Loops) {
                String sql_from = "";
                sql_from = "SELECT * FROM aa_trans_not_paid WHERE convert_status=0";
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql_from);) {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        cTrans = new Trans();
                        cTransBean.setTransFromResultset(cTrans, rs);

                        try {
                            TransPaidAmount = rs.getDouble("trans_paid_amount");
                        } catch (NullPointerException npe) {
                            TransPaidAmount = 0;
                        }
                        cTrans.setGrandTotal(cTrans.getGrandTotal() - TransPaidAmount);
                        cstatus = this.convertTransNotPaidToPaidFULL(cTrans);
                        this.updateOverPayTransConvertStatusFULL(cTrans.getTransactionId(), cstatus);
                        convert_pass = convert_pass + 1;
                        cTrans = null;
                        i = i + 1;
                    }
                    rs.close();
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
                loop = loop + 1;
            }
        }
        if (aMode.equals("PART")) {
            TotalRecords = this.getCountOverPayRecords(aMode);
            Loops = (int) TotalRecords;
            loop = 1;
            while (loop <= Loops && Loops > 0) {
                String sql_from = "";
                sql_from = "SELECT * FROM aa_trans_not_paid WHERE convert_status=2 and (grand_total-trans_paid_amount)>0";

                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql_from);) {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        cTrans = new Trans();
                        cTransBean.setTransFromResultset(cTrans, rs);

                        try {
                            TransPaidAmount = rs.getDouble("trans_paid_amount");
                        } catch (NullPointerException npe) {
                            TransPaidAmount = 0;
                        }
                        cTrans.setGrandTotal(cTrans.getGrandTotal() - TransPaidAmount);
                        Pay pay_made = null;
                        pay_made = this.convertTransNotPaidToPaidPART(cTrans);
                        try {
                            if (null != pay_made && pay_made.getPayId() > 0) {
                                this.updateOverPayTransConvertStatusPART(cTrans.getTransactionId(), pay_made);
                            } else if (null == pay_made) {
                                this.updateOverPayTransConvertStatusPARTFail(cTrans.getTransactionId());
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.ERROR, e);
                        }
                        convert_pass = convert_pass + 1;
                        cTrans = null;
                        i = i + 1;
                    }
                    rs.close();
                } catch (Exception e) {
                    //
                }
                if (loop == Loops) {
                    TotalRecords = this.getCountOverPayRecords(aMode);
                    Loops = (int) TotalRecords;
                    loop = 1;
                } else {
                    loop = loop + 1;
                }
            }
        }
        return convert_pass + "/" + TotalRecords + " Converted" + " Loops:" + Loops;
    }

    public String convertPayToJournalAll2() {
        int Loops = 1;
        int loop = 1;
        double TotalRecords = this.getCountPayRecords();
        double RecordsBatchFactor = TotalRecords / 50;
        Loops = (int) Math.ceil(RecordsBatchFactor);
        int i = 0;
        int convert_pass = 0;
        int cstatus = 0;
        Pay cPay = null;
        PayBean cPayBean = new PayBean();
        long LastId = 0;
        while (loop <= Loops) {
            String sql_from = "SELECT * FROM pay_convert WHERE pay_reason_id IN(22,25) AND pay_id>" + LastId + " AND pay_id<=" + (LastId + 50);
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql_from);) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    cPay = new Pay();
                    cPayBean.setPayFromResultset(cPay, rs);
                    cstatus = this.convertPayToJournal(cPay);
                    convert_pass = convert_pass + 1;
                    LastId = cPay.getPayId();
                    //this.updatePayConvertStatus(cPay.getPayId(), cstatus);
                    cPay = null;
                    i = i + 1;
                }
                rs.close();
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
            //System.out.println(convert_pass + "/" + TotalRecords + " Converted" + " Loop:" + loop + "/" + Loops);
            loop = loop + 1;
        }
        return convert_pass + "/" + TotalRecords + " Converted" + " Loops:" + Loops;
    }

    public int convertTransToJournal(Trans aTrans) {
        int passed = 0;
        String sql = null;
        String msg = "";
        List<TransItem> aTransItems = null;
        Pay aPay = new Pay();
        TransactionType tt = new TransactionTypeBean().getTransactionType(aTrans.getTransactionTypeId());
        try {
//            if (tt.getTransactionTypeName().equals("SALE INVOICE") || tt.getTransactionTypeName().equals("PURCHASE INVOICE")) {
//                aPay = new PayBean().getTransactionFirstPayByTransNo(aTrans.getTransactionNumber());
//            }
            aTrans.setAmountTendered(0);//so that Cash Receipt/Payment is not posted
            aTrans.setChangeAmount(0);
            aTransItems = new TransItemBean().getTransItemsByTransactionId(aTrans.getTransactionId());
            if ("SALE INVOICE".equals(tt.getTransactionTypeName())) {
                new AccJournalBean().postJournalSaleInvoice(aTrans, aTransItems, aPay, new AccPeriodBean().getAccPeriod(aTrans.getTransactionDate()).getAccPeriodId());
                passed = 1;
            } else if ("PURCHASE INVOICE".equals(tt.getTransactionTypeName())) {
                long PostJobId = new AccJournalBean().postJournalPurchaseInvoice(aTrans, aTransItems, aPay, new AccPeriodBean().getAccPeriod(aTrans.getTransactionDate()).getAccPeriodId());
                passed = 1;
            } else if ("DISPOSE STOCK".equals(tt.getTransactionTypeName())) {
                new AccJournalBean().postJournalDisposeStock(aTrans, new AccPeriodBean().getAccPeriod(aTrans.getTransactionDate()).getAccPeriodId());
                passed = 1;
            } else {
                passed = 2;
            }
        } catch (Exception e) {
            passed = 3;
        }
        return passed;
    }

    public int convertPayToJournal(Pay aPay) {
        int passed = 0;
        String sql = null;
        String msg = "";
        try {
            if (aPay.getPayTypeId() == 14 && aPay.getPayReasonId() == 22) {
                new AccJournalBean().postJournalCashReceiptReceivable(aPay, new AccPeriodBean().getAccPeriod(aPay.getPayDate()).getAccPeriodId());
                passed = 1;
            } else if (aPay.getPayTypeId() == 15 && aPay.getPayReasonId() == 25) {
                new AccJournalBean().postJournalCashPaymentPurchase(aPay, new AccPeriodBean().getAccPeriod(aPay.getPayDate()).getAccPeriodId());
                passed = 1;
            } else {
                passed = 2;
            }
        } catch (Exception e) {
            passed = 3;
        }
        return passed;
    }

    public int convertTransNotPaidToPaidFULL(Trans aTrans) {
        int passed = 0;
        String sql = null;
        String msg = "";
        try {
            long pay_id = 0;
            if (aTrans.getTransactionTypeId() == 2) {//sales
                pay_id = new PayBean().getOverPayIdReadyForTransFULL(aTrans.getBillTransactorId(), "IN", aTrans.getGrandTotal());
            } else if (aTrans.getTransactionTypeId() == 1) {//purchases
                pay_id = new PayBean().getOverPayIdReadyForTransFULL(aTrans.getBillTransactorId(), "OUT", aTrans.getGrandTotal());
            }
            if (pay_id > 0) {
                //System.out.print("," + pay_id);
                PayTrans paytrans = new PayTrans();
                paytrans.setPayTransId(0);
                paytrans.setPayId(pay_id);
                paytrans.setTransactionId(aTrans.getTransactionId());
                paytrans.setTransactionNumber(aTrans.getTransactionNumber());
                paytrans.setTransPaidAmount(aTrans.getGrandTotal());
                paytrans.setTransactionTypeId(aTrans.getTransactionTypeId());
                paytrans.setTransactionReasonId(aTrans.getTransactionReasonId());
                new PayTransBean().savePayTrans(paytrans);
                passed = 1;
            } else {
                passed = 2;
            }
        } catch (Exception e) {
            passed = 3;
        }
        return passed;
    }

    public Pay convertTransNotPaidToPaidPART(Trans aTrans) {
        int passed = 0;
        String sql = null;
        String msg = "";
        Pay pay = null;
        try {
            //long pay_id = 0;
            //double pay_amount=0;
            if (aTrans.getTransactionTypeId() == 2) {//sales
                pay = new PayBean().getOverPayIdReadyForTransPART(aTrans.getBillTransactorId(), "IN", aTrans.getGrandTotal());
            } else if (aTrans.getTransactionTypeId() == 1) {//purchases
                pay = new PayBean().getOverPayIdReadyForTransPART(aTrans.getBillTransactorId(), "OUT", aTrans.getGrandTotal());
            }
            try {
                if (null != pay && pay.getPayId() > 0) {
                    //System.out.println("," + pay.getPayId() + ":" + pay.getPaidAmount());
                    PayTrans paytrans = new PayTrans();
                    paytrans.setPayTransId(0);
                    paytrans.setPayId(pay.getPayId());
                    paytrans.setTransactionId(aTrans.getTransactionId());
                    paytrans.setTransactionNumber(aTrans.getTransactionNumber());
                    paytrans.setTransPaidAmount(pay.getPaidAmount());
                    paytrans.setTransactionTypeId(aTrans.getTransactionTypeId());
                    paytrans.setTransactionReasonId(aTrans.getTransactionReasonId());
                    new PayTransBean().savePayTrans(paytrans);
                    //passed = 1;
                } else {
                    pay = null;
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        } catch (Exception e) {
            pay = null;
        }
        return pay;
    }

    public void saveDraftTrans(String aHistFlag, Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String sql = null;
        String msg = "";
        long TransHistId = 0;
        boolean isTransCopySuccess = false;
        boolean isTransItemCopySuccess = false;
        try {
            if (aActiveTransItems.isEmpty()) {
                msg = aHistFlag + " Empty Transaction Cannot be Saved";
                FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
                this.setActionMessage(ub.translateWordsInText(BaseName, aHistFlag + " Transaction Not Saved"));
            } else {
                sql = "{call sp_insert_transaction_hist(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        CallableStatement cs = conn.prepareCall(sql);) {
                    cs.setString("in_hist_flag", aHistFlag);//Draft, Approval,Edit,etc.
                    cs.setDate("in_transaction_date", new java.sql.Date(trans.getTransactionDate().getTime()));
                    cs.setInt("in_store_id", new GeneralUserSetting().getCurrentStore().getStoreId());
                    trans.setStoreId(new GeneralUserSetting().getCurrentStore().getStoreId());
                    cs.setInt("in_store2_id", trans.getStore2Id());
                    cs.setLong("in_transactor_id", trans.getTransactorId());
                    trans.setTransactionTypeId(new GeneralUserSetting().getCurrentTransactionTypeId());
                    cs.setInt("in_transaction_type_id", trans.getTransactionTypeId());
                    if (trans.getTransactionTypeId() == 3 || trans.getTransactionTypeId() == 4 || trans.getTransactionTypeId() == 13 || trans.getTransactionTypeId() == 7 || trans.getTransactionTypeId() == 16 || trans.getTransactionTypeId() == 19) {//DISPOSE STOCK, TRANFER & TRANS REQ & UNPACK & JOURNAL ENTRY & EXPENSE ENTRY
                        trans.setTransactionReasonId(new GeneralUserSetting().getCurrentTransactionReasonId());
                    }
                    cs.setInt("in_transaction_reason_id", trans.getTransactionReasonId());
                    cs.setDouble("in_cash_discount", trans.getCashDiscount());
                    cs.setDouble("in_total_vat", trans.getTotalVat());
                    cs.setString("in_transaction_comment", trans.getTransactionComment());
                    cs.setInt("in_add_user_detail_id", new GeneralUserSetting().getCurrentUser().getUserDetailId());
                    trans.setAddUserDetailId(new GeneralUserSetting().getCurrentUser().getUserDetailId());
                    cs.setTimestamp("in_add_date", new java.sql.Timestamp(new java.util.Date().getTime()));
                    cs.setInt("in_edit_user_detail_id", new GeneralUserSetting().getCurrentUser().getUserDetailId());//will be made null by the SP
                    cs.setTimestamp("in_edit_date", new java.sql.Timestamp(new java.util.Date().getTime()));//will be made null by the SP
                    cs.setString("in_transaction_ref", trans.getTransactionRef());
                    cs.registerOutParameter("out_transaction_hist_id", VARCHAR);
                    cs.setDouble("in_sub_total", trans.getSubTotal());
                    cs.setDouble("in_grand_total", trans.getGrandTotal());
                    cs.setDouble("in_total_trade_discount", trans.getTotalTradeDiscount());
                    cs.setDouble("in_points_awarded", trans.getPointsAwarded());
                    cs.setString("in_card_number", trans.getCardNumber());
                    cs.setDouble("in_total_std_vatable_amount", trans.getTotalStdVatableAmount());
                    cs.setDouble("in_total_zero_vatable_amount", trans.getTotalZeroVatableAmount());
                    cs.setDouble("in_total_exempt_vatable_amount", trans.getTotalExemptVatableAmount());
                    cs.setDouble("in_vat_perc", CompanySetting.getVatPerc());
                    cs.setDouble("in_amount_tendered", trans.getAmountTendered());
                    cs.setDouble("in_change_amount", trans.getChangeAmount());
                    cs.setString("in_is_cash_discount_vat_liable", CompanySetting.getIsCashDiscountVatLiable());
                    //for profit margin
                    cs.setDouble("in_total_profit_margin", trans.getTotalProfitMargin());
                    try {
                        if (trans.getTransactionUserDetailId() == 0) {
                            trans.setTransactionUserDetailId(new GeneralUserSetting().getCurrentUser().getUserDetailId());
                        }
                    } catch (NullPointerException npe) {
                        trans.setTransactionUserDetailId(new GeneralUserSetting().getCurrentUser().getUserDetailId());
                    }
                    cs.setInt("in_transaction_user_detail_id", trans.getTransactionUserDetailId());
                    try {
                        if (trans.getBillTransactorId() == 0) {
                            trans.setBillTransactorId(trans.getTransactorId());
                        }
                    } catch (NullPointerException npe) {
                        trans.setBillTransactorId(trans.getTransactorId());
                    }
                    cs.setLong("in_bill_transactor_id", trans.getBillTransactorId());
                    try {
                        cs.setLong("in_scheme_transactor_id", trans.getSchemeTransactorId());
                    } catch (NullPointerException npe) {
                        cs.setLong("in_scheme_transactor_id", 0);
                    }
                    try {
                        cs.setString("in_princ_scheme_member", trans.getPrincSchemeMember());
                    } catch (NullPointerException npe) {
                        cs.setString("in_princ_scheme_member", "");
                    }
                    try {
                        cs.setString("in_scheme_card_number", trans.getSchemeCardNumber());
                    } catch (NullPointerException npe) {
                        cs.setString("in_scheme_card_number", "");
                    }
                    try {
                        cs.setString("in_transaction_number", trans.getTransactionNumber());
                    } catch (NullPointerException npe) {
                        cs.setString("in_transaction_number", "");
                    }
                    try {
                        cs.setDate("in_delivery_date", new java.sql.Date(trans.getDeliveryDate().getTime()));
                    } catch (NullPointerException npe) {
                        cs.setDate("in_delivery_date", null);
                    }
                    try {
                        cs.setString("in_delivery_address", trans.getDeliveryAddress());
                    } catch (NullPointerException npe) {
                        cs.setString("in_delivery_address", "");
                    }
                    try {
                        cs.setString("in_pay_terms", trans.getPayTerms());
                    } catch (NullPointerException npe) {
                        cs.setString("in_pay_terms", "");
                    }
                    try {
                        cs.setString("in_terms_conditions", trans.getTermsConditions());
                    } catch (NullPointerException npe) {
                        cs.setString("in_terms_conditions", "");
                    }
                    try {
                        cs.setInt("in_authorised_by_user_detail_id", trans.getAuthorisedByUserDetailId());
                    } catch (NullPointerException npe) {
                        cs.setInt("in_authorised_by_user_detail_id", 0);
                    }
                    try {
                        cs.setDate("in_authorise_date", new java.sql.Date(trans.getAuthoriseDate().getTime()));
                    } catch (NullPointerException npe) {
                        cs.setDate("in_authorise_date", null);
                    }
                    try {
                        cs.setDate("in_pay_due_date", new java.sql.Date(trans.getPayDueDate().getTime()));
                    } catch (NullPointerException npe) {
                        cs.setDate("in_pay_due_date", null);
                    }
                    try {
                        cs.setDate("in_expiry_date", new java.sql.Date(trans.getExpiryDate().getTime()));
                    } catch (NullPointerException npe) {
                        cs.setDate("in_expiry_date", null);
                    }
                    try {
                        cs.setInt("in_acc_child_account_id", trans.getAccChildAccountId());
                    } catch (NullPointerException npe) {
                        cs.setInt("in_acc_child_account_id", 0);
                    }
                    try {
                        cs.setString("in_currency_code", trans.getCurrencyCode());
                    } catch (NullPointerException npe) {
                        cs.setString("in_currency_code", "");
                    }
                    try {
                        AccCurrency LocalCurrency = null;
                        LocalCurrency = new AccCurrencyBean().getLocalCurrency();
                        trans.setXrate(new AccXrateBean().getXrate(trans.getCurrencyCode(), LocalCurrency.getCurrencyCode()));
                    } catch (NullPointerException npe) {
                        trans.setXrate(1);
                    }
                    cs.setDouble("in_xrate", trans.getXrate());
                    try {
                        cs.setDate("in_from_date", new java.sql.Date(trans.getFrom_date().getTime()));
                    } catch (NullPointerException npe) {
                        cs.setDate("in_from_date", null);
                    }
                    try {
                        cs.setDate("in_to_date", new java.sql.Date(trans.getTo_date().getTime()));
                    } catch (NullPointerException npe) {
                        cs.setDate("in_to_date", null);
                    }
                    try {
                        cs.setString("in_duration_type", trans.getDuration_type());
                    } catch (NullPointerException npe) {
                        cs.setString("in_duration_type", "");
                    }
                    try {
                        cs.setLong("in_site_id", trans.getSite_id());
                    } catch (NullPointerException npe) {
                        cs.setLong("in_site_id", 0);
                    }

                    try {
                        cs.setString("in_transactor_rep", trans.getTransactor_rep());
                    } catch (NullPointerException npe) {
                        cs.setString("in_transactor_rep", "");
                    }
                    try {
                        cs.setString("in_transactor_vehicle", trans.getTransactor_vehicle());
                    } catch (NullPointerException npe) {
                        cs.setString("in_transactor_vehicle", "");
                    }
                    try {
                        cs.setString("in_transactor_driver", trans.getTransactor_driver());
                    } catch (NullPointerException npe) {
                        cs.setString("in_transactor_driver", "");
                    }
                    try {
                        cs.setDouble("in_duration_value", trans.getDuration_value());
                    } catch (NullPointerException npe) {
                        cs.setDouble("in_duration_value", 0);
                    }
                    //save
                    cs.executeUpdate();
                    isTransCopySuccess = true;
                    //save trans items
                    TransHistId = cs.getLong("out_transaction_hist_id");

                    TransItemBean tib = new TransItemBean();
                    tib.saveDraftTransItems(trans, aActiveTransItems, TransHistId);
                    isTransItemCopySuccess = true;
                    if (isTransCopySuccess && isTransItemCopySuccess) {
                        if (aHistFlag.equals("Approval")) {
                            new Transaction_approvalBean().insertTransaction_approvalCall(TransHistId);
                        }
                        this.setActionMessage(ub.translateWordsInText(BaseName, aHistFlag + " Saved Successfully"));
                        this.clearAll2(trans, aActiveTransItems, null, null, aSelectedTransactor, 2, aSelectedBillTransactor, aTransUserDetail, aSelectedSchemeTransactor, aAuthorisedByUserDetail, aSelectedAccCoa);
                        if (aHistFlag.equals("Approval")) {
                            new Transaction_approvalBean().refreshTransaction_approvalList(this.TransListApproval, new GeneralUserSetting().getCurrentStore().getStoreId(), new GeneralUserSetting().getCurrentUser().getUserDetailId(), new GeneralUserSetting().getCurrentTransactionTypeId(), new GeneralUserSetting().getCurrentTransactionReasonId());
                        } else if (aHistFlag.equals("Draft")) {
                            this.refreshTranssDraft(new GeneralUserSetting().getCurrentStore().getStoreId(), new GeneralUserSetting().getCurrentUser().getUserDetailId(), new GeneralUserSetting().getCurrentTransactionTypeId(), new GeneralUserSetting().getCurrentTransactionReasonId());
                        }
                    } else {
                        this.setActionMessage(ub.translateWordsInText(BaseName, aHistFlag + " Not Saved"));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void loadDraftTrans(String aHistFlag, Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        String sql = null;
        long TransHistId = 0;
        if (aHistFlag.equals("Draft")) {
            TransHistId = trans.getTransactionHistId();
        } else if (aHistFlag.equals("Approval")) {
            Transaction_approval transapp = new Transaction_approvalBean().getTransaction_approval(trans.getTransaction_approval_id());
            if (null != transapp) {
                TransHistId = transapp.getTransaction_hist_id();
            }
        }
        try {
            if (TransHistId > 0) {
                this.setTransFromHist(trans, TransHistId);
                new TransItemBean().setTransItemsFromHist(aActiveTransItems, TransHistId);
                try {
                    if (trans.getTransactorId() > 0) {
                        new TransactorBean().setTransactor(aSelectedTransactor, trans.getTransactorId());
                    }
                } catch (NullPointerException npe) {
                }
                try {
                    if (trans.getBillTransactorId() > 0) {
                        new TransactorBean().setTransactor(aSelectedBillTransactor, trans.getBillTransactorId());
                    }
                } catch (NullPointerException npe) {
                }
                try {
                    if (trans.getSchemeTransactorId() > 0) {
                        new TransactorBean().setTransactor(aSelectedSchemeTransactor, trans.getSchemeTransactorId());
                    }
                } catch (NullPointerException npe) {
                }
                try {
                    if (trans.getTransactionUserDetailId() > 0 && null != aTransUserDetail) {
                        new UserDetailBean().setUserDetail(aTransUserDetail, trans.getTransactionUserDetailId());
                    }
                } catch (NullPointerException npe) {
                }
                //Customer Display
                if (new GeneralUserSetting().getCurrentTransactionTypeId() == 2) {
                    String PortName = new Parameter_listBean().getParameter_listByContextNameMemory("CUSTOMER_DISPLAY", "COM_PORT_NAME").getParameter_value();
                    String ClientPcName = new GeneralUserSetting().getClientComputerName();
                    String SizeStr = new Parameter_listBean().getParameter_listByContextNameMemory("CUSTOMER_DISPLAY", "MAX_CHARACTERS_PER_LINE").getParameter_value();
                    int Size = 0;
                    if (SizeStr.length() > 0) {
                        Size = Integer.parseInt(SizeStr);
                    }
                    if (PortName.length() > 0 && ClientPcName.length() > 0 && Size > 0 && (new GeneralUserSetting().getCurrentTransactionTypeId() == 2 || new GeneralUserSetting().getCurrentTransactionTypeId() == 11)) {
                        //UtilityBean ub = new UtilityBean();
                        ub.invokeLocalCustomerDisplay(ClientPcName, PortName, Size, ub.formatDoubleToString(trans.getGrandTotal()), "");
                    }
                }
            } else {
                FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, "Select Valid " + aHistFlag + " Record")));
                this.setActionMessage(ub.translateWordsInText(BaseName, aHistFlag + " Not Loaded"));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void loadDraftTrans(String aHistFlag, Trans aTrans, List<TransItem> aActiveTransItems, TransactorBean aTransactorBean, UserDetailBean aUserDetailBean, TransBean aTransBean) {
        //Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        String sql = null;
        long TransHistId = 0;
        if (aHistFlag.equals("Draft")) {
            TransHistId = aTrans.getTransactionHistId();
        } else if (aHistFlag.equals("Approval")) {
            Transaction_approval transapp = new Transaction_approvalBean().getTransaction_approval(aTrans.getTransaction_approval_id());
            if (null != transapp) {
                TransHistId = transapp.getTransaction_hist_id();
            }
        }
        try {
            if (TransHistId > 0) {
                this.setTransFromHist(aTrans, TransHistId);
                new TransItemBean().setTransItemsFromHist(aActiveTransItems, TransHistId);
                this.setTransTotalsAndUpdateCEC(aTrans.getTransactionTypeId(), aTrans.getTransactionReasonId(), aTrans, aActiveTransItems);
                try {
                    if (aTrans.getTransactorId() > 0) {
                        aTransactorBean.setSelectedTransactor(new TransactorBean().getTransactor(aTrans.getTransactorId()));
                    } else {
                        aTransactorBean.setSelectedTransactor(null);
                    }
                } catch (NullPointerException npe) {
                }
                try {
                    if (aTrans.getBillTransactorId() > 0) {
                        aTransactorBean.setSelectedBillTransactor(new TransactorBean().getTransactor(aTrans.getBillTransactorId()));
                    } else {
                        aTransactorBean.setSelectedBillTransactor(null);
                    }
                } catch (NullPointerException npe) {
                }
                try {
                    if (aTrans.getSchemeTransactorId() > 0) {
                        aTransactorBean.setSelectedSchemeTransactor(new TransactorBean().getTransactor(aTrans.getSchemeTransactorId()));
                    } else {
                        aTransactorBean.setSelectedSchemeTransactor(null);
                    }
                } catch (NullPointerException npe) {
                }
                try {
                    if (aTrans.getTransactionUserDetailId() > 0) {
                        aUserDetailBean.setSelectedUserDetail(new UserDetailBean().getUserDetail(aTrans.getTransactionUserDetailId()));
                    } else {
                        aUserDetailBean.setSelectedUserDetail(null);
                    }
                } catch (NullPointerException npe) {
                }
                //Customer Display
                if (new GeneralUserSetting().getCurrentTransactionTypeId() == 2) {
                    String PortName = new Parameter_listBean().getParameter_listByContextNameMemory("CUSTOMER_DISPLAY", "COM_PORT_NAME").getParameter_value();
                    String ClientPcName = new GeneralUserSetting().getClientComputerName();
                    String SizeStr = new Parameter_listBean().getParameter_listByContextNameMemory("CUSTOMER_DISPLAY", "MAX_CHARACTERS_PER_LINE").getParameter_value();
                    int Size = 0;
                    if (SizeStr.length() > 0) {
                        Size = Integer.parseInt(SizeStr);
                    }
                    if (PortName.length() > 0 && ClientPcName.length() > 0 && Size > 0 && (new GeneralUserSetting().getCurrentTransactionTypeId() == 2 || new GeneralUserSetting().getCurrentTransactionTypeId() == 11)) {
                        //UtilityBean ub = new UtilityBean();
                        ub.invokeLocalCustomerDisplay(ClientPcName, PortName, Size, ub.formatDoubleToString(aTrans.getGrandTotal()), "");
                    }
                }
            } else {
                FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, "Select Valid " + aHistFlag + " Record")));
                this.setActionMessage(ub.translateWordsInText(BaseName, aHistFlag + " Not Loaded"));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void loadOrderTrans(Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa) {
        String sql = null;
        String msg = "";
        long OrderId = 0;
        String OrderNumber = trans.getTransactionRef();
        long TransactorId = 0;
        try {
            TransactorId = aSelectedTransactor.getTransactorId();
        } catch (Exception e) {
        }
        try {
            if (OrderNumber.length() > 0 && TransactorId > 0) {
                this.setTransFromOrder(trans, OrderNumber, TransactorId);
                OrderId = trans.getTransactionId();
                new TransItemBean().setTransItemsByTransactionId(aActiveTransItems, OrderId);
                try {
                    new TransactorBean().setTransactor(aSelectedTransactor, trans.getTransactorId());
                } catch (NullPointerException npe) {
                }
                if (TransactorId == trans.getTransactorId() && trans.getTransactionTypeId() == 11) {
                    //some cleanups and reset
                    //1. for trans
                    trans.setTransactionTypeId(new GeneralUserSetting().getCurrentTransactionTypeId());
                    trans.setTransactionReasonId(new GeneralUserSetting().getCurrentTransactionReasonId());
                    trans.setTransactionId(0);
                    trans.setTransactionNumber("");
                    trans.setTransactionRef(OrderNumber);
                    trans.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
                    trans.setPayMethod(new PayMethodBean().getPayMethodDefault().getPayMethodId());
                    this.refreshCustomerBalances(trans);
                    //2. for trans items
                    TransItemBean tib = new TransItemBean();
                    ItemBean ib = new ItemBean();
                    Item item = new Item();
                    for (int i = 0; i < aActiveTransItems.size(); i++) {
                        aActiveTransItems.get(i).setTransactionItemId(0);
                        aActiveTransItems.get(i).setTransactionId(0);
                        item = ib.getItem(aActiveTransItems.get(i).getItemId());
                        if (item.getItemType().equals("PRODUCT")) {//4-10-000-010 - SALES Products
                            aActiveTransItems.get(i).setAccountCode("4-10-000-010");
                        } else if (item.getItemType().equals("SERVICE")) {//4-10-000-020 - SALES Services	
                            aActiveTransItems.get(i).setAccountCode("4-10-000-020");
                        }
                        tib.updateLookUpsUI(aActiveTransItems.get(i));
                    }
                    this.setTransTotalsAndUpdateCEC(trans.getTransactionTypeId(), trans.getTransactionReasonId(), trans, aActiveTransItems);
                } else {
                    FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage("Select a valid sales order number that matches with client..."));
                    this.setActionMessage("No sale order record loaded");
                }
            } else {
                //FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage("Select a valid sales order number that matches with client..."));
                //this.setActionMessage("No sale order record loaded");
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void loadOrderForInvoiceTrans(Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        String sql = null;
        long OrderId = 0;
        String OrderNumber = trans.getTransactionRef();
        long TransactorId = 0;
        try {
            TransactorId = aSelectedTransactor.getTransactorId();
        } catch (Exception e) {
        }
        try {
            TransactionType transtype = new TransactionTypeBean().getTransactionType(new GeneralUserSetting().getCurrentTransactionTypeId());
            TransactionReason transreason = new TransactionReasonBean().getTransactionReason(new GeneralUserSetting().getCurrentTransactionReasonId());
            Store store = new StoreBean().getStore(new GeneralUserSetting().getCurrentStore().getStoreId());
            if (OrderNumber.length() > 0 && TransactorId > 0) {
                this.setTransFromOrder(trans, OrderNumber, TransactorId);
                OrderId = trans.getTransactionId();
                new TransItemBean().setTransItemsByTransactionId(aActiveTransItems, OrderId);
                try {
                    new TransactorBean().setTransactor(aSelectedTransactor, trans.getTransactorId());
                } catch (NullPointerException npe) {
                }
                if (TransactorId == trans.getTransactorId() && trans.getTransactionTypeId() == 11) {
                    //some cleanups and reset
                    //1. for trans
                    trans.setTransactionTypeId(transtype.getTransactionTypeId());
                    trans.setTransactionReasonId(transreason.getTransactionReasonId());
                    trans.setTransactionId(0);
                    trans.setTransactionNumber("");
                    trans.setTransactionRef(OrderNumber);
                    trans.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
                    trans.setPayMethod(new PayMethodBean().getPayMethodDefault().getPayMethodId());
                    this.refreshCustomerBalances(trans);
                    //2. for trans items
                    TransItemBean tib = new TransItemBean();
                    ItemBean ib = new ItemBean();
                    Item item = new Item();

                    for (int i = 0; i < aActiveTransItems.size(); i++) {
                        aActiveTransItems.get(i).setTransactionItemId(0);
                        aActiveTransItems.get(i).setTransactionId(0);
                        item = ib.getItem(aActiveTransItems.get(i).getItemId());
                        aActiveTransItems.get(i).setAccountCode(tib.getTransItemInventCostAccount(transtype, transreason, item));
                        double OrderInvoiceQtyBalance = this.getOrderInvoiceQtyBalance(TransactorId, OrderNumber, aActiveTransItems.get(i));
                        if (OrderInvoiceQtyBalance > 0) {
                            aActiveTransItems.get(i).setItemQty(OrderInvoiceQtyBalance);
                        } else {
                            aActiveTransItems.get(i).setItemQty(0);
                        }
                        new TransItemBean().editTransItemCEC(transtype.getTransactionTypeId(), transreason.getTransactionReasonId(), "", trans, aActiveTransItems, aActiveTransItems.get(i));
                        //for profit margin
                        if ("SALE INVOICE".equals(transtype.getTransactionTypeName())) {
                            if (item.getIsTrack() == 1) {
                                aActiveTransItems.get(i).setUnitCostPrice(new StockBean().getItemUnitCostPrice(store.getStoreId(), item.getItemId(), aActiveTransItems.get(i).getBatchno(), aActiveTransItems.get(i).getCodeSpecific(), aActiveTransItems.get(i).getDescSpecific()));
                            } else {
                                aActiveTransItems.get(i).setUnitCostPrice(item.getUnitCostPrice());
                            }
                            aActiveTransItems.get(i).setUnitProfitMargin((aActiveTransItems.get(i).getUnitPriceExcVat() - aActiveTransItems.get(i).getUnitTradeDiscount()) - aActiveTransItems.get(i).getUnitCostPrice());
                        } else {
                            aActiveTransItems.get(i).setUnitCostPrice(0);
                            aActiveTransItems.get(i).setUnitProfitMargin(0);
                        }
                        //update lookups
                        tib.updateLookUpsUI(aActiveTransItems.get(i));
                    }
                    //remove those already invoiced
                    Iterator<TransItem> iterator = aActiveTransItems.iterator();
                    while (iterator.hasNext()) {
                        TransItem titem = iterator.next();
                        if (titem.getItemQty() == 0) {
                            iterator.remove();
                        }
                    }
                    //set totals
                    this.setTransTotalsAndUpdateCEC(trans.getTransactionTypeId(), trans.getTransactionReasonId(), trans, aActiveTransItems);
                } else {
                    FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, "Select Valid Sale Order Number that Matches with the Customer")));
                    this.setActionMessage(ub.translateWordsInText(BaseName, "No Sale Order Record Loaded"));
                }
            } else {
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void loadTransferForInvoiceTrans(Trans aTrans, List<TransItem> aActiveTransItems, TransItem aTransItem) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        long TransferId = 0;
        String TransferNumber = aTransItem.getItemCode();
        try {
            TransactionType transtype = new TransactionTypeBean().getTransactionType(new GeneralUserSetting().getCurrentTransactionTypeId());
            TransactionReason transreason = new TransactionReasonBean().getTransactionReason(new GeneralUserSetting().getCurrentTransactionReasonId());
            Store store = new StoreBean().getStore(new GeneralUserSetting().getCurrentStore().getStoreId());
            if (TransferNumber.length() > 0) {
                Trans TransferTrans = new Trans();
                List<TransItem> TransferItems = new ArrayList<>();
                this.setTransFromTransfer(TransferTrans, TransferNumber, store.getStoreId());
                TransferId = TransferTrans.getTransactionId();
                if (TransferId > 0) {
                    new TransItemBean().setTransItemsByTransactionId(TransferItems, TransferId);
                }
                if (TransferId > 0 && TransferItems.size() > 0) {
                    //some cleanups and reset
                    //1. for trans
                    //trans.setTransactionNumber("");
                    //trans.setTransactionRef(TransferNumber);
                    //2. for trans items
                    TransItemBean tib = new TransItemBean();
                    ItemBean ib = new ItemBean();
                    Item item = null;
                    TransItem transitem = null;
                    for (int i = 0; i < TransferItems.size(); i++) {
                        item = new ItemBean().getItem(TransferItems.get(i).getItemId());
                        transitem = new TransItem();
                        transitem.setItemId(item.getItemId());
                        transitem.setItemQty(TransferItems.get(i).getItemQty());
                        try {
                            if (null == TransferItems.get(i).getBatchno()) {
                                transitem.setBatchno("");
                            } else {
                                transitem.setBatchno(TransferItems.get(i).getBatchno());
                            }
                        } catch (NullPointerException npe) {
                            transitem.setBatchno("");
                        }
                        try {
                            if (null == TransferItems.get(i).getCodeSpecific()) {
                                transitem.setCodeSpecific("");
                            } else {
                                transitem.setCodeSpecific(TransferItems.get(i).getCodeSpecific());
                            }
                        } catch (NullPointerException npe) {
                            transitem.setCodeSpecific("");
                        }
                        try {
                            if (null == TransferItems.get(i).getDescSpecific()) {
                                transitem.setDescSpecific("");
                            } else {
                                transitem.setDescSpecific(TransferItems.get(i).getDescSpecific());
                            }
                        } catch (NullPointerException npe) {
                            transitem.setDescSpecific("");
                        }
                        transitem.setAccountCode(tib.getTransItemInventCostAccount(transtype, transreason, item));
                        transitem.setUnit_id(TransferItems.get(i).getUnit_id());
                        transitem.setBase_unit_qty(TransferItems.get(i).getBase_unit_qty());
                        Item_unit iu = new ItemBean().getItemUnitFrmDb(transitem.getItemId(), transitem.getUnit_id());
                        if (null != iu) {
                            item.setUnitRetailsalePrice(iu.getUnit_retailsale_price());
                            item.setUnitWholesalePrice(iu.getUnit_wholesale_price());
                            item.setUnitSymbol(iu.getUnit_symbol());
                        }
                        tib.updateModelTransItemAutoAddFrmTransfer(store, transtype, transreason, new GeneralUserSetting().getCurrentSaleType(), aTrans, new StatusBean(), aActiveTransItems, transitem, item);
                    }
                    tib.clearTransItem(aTransItem);
                } else {
                    msg = "Select Valid Transfer Number";
                    FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
                    this.setActionMessage(ub.translateWordsInText(BaseName, msg));
                }
            } else {
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void deleteDraftTrans(Trans trans) {
        String sql = null;
        String msg = "";
        long TransHistId = 0;
        TransHistId = trans.getTransactionHistId();
        try {
            if (TransHistId > 0) {
                this.setTransFromHist(trans, TransHistId);

            } else {
                FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage("Select a valid draft sale record..."));
                this.setActionMessage("No draft sale record loaded");
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void callUpdateTransV2(Trans aNewTrans, List<TransItem> aNewTransItems, Pay aPay) {
        try {
            //confirm trans type
            String aTransTypeName = "";
            String ItemMessage = "";
            TransactionType aTransType = new TransactionType();
            try {
                aTransType = new TransactionTypeBean().getTransactionType(aNewTrans.getTransactionTypeId());
            } catch (NullPointerException npe) {

            }
            if ("ITEM RECEIVED".equals(aTransType.getTransactionTypeName()) || "SALE INVOICE".equals(aTransType.getTransactionTypeName()) || "DISPOSE STOCK".equals(aTransType.getTransactionTypeName())) {
                ItemMessage = new TransItemBean().getAnyItemTotalQtyGreaterThanCurrentQty(new TransItemBean().getTransItemListCurLessPrevQty(aNewTransItems, aNewTrans), aNewTrans.getStoreId(), new TransactionTypeBean().getTransactionType(aNewTrans.getTransactionTypeId()).getTransactionTypeName());
            }
            if (aNewTrans != null) {
                if (aNewTrans.getTransactionTypeId() == 2 && aNewTrans.getBillTransactorId() == 0 && (aNewTrans.getAmountTendered() + aNewTrans.getSpendPointsAmount()) != aNewTrans.getGrandTotal()) {
                    FacesContext.getCurrentInstance().addMessage("Update", new FacesMessage("TenderedAmount plus PiontsSpentAmount should equal the new GrandTotal!"));
                    this.setActionMessage("TenderAmount plus PiontsSpentAmount should equal the new GrandTotal!");
                } else if (aNewTrans.getTransactionTypeId() == 2 && aNewTrans.getSpendPointsAmount() > aNewTrans.getBalancePointsAmount()) {
                    FacesContext.getCurrentInstance().addMessage("Update", new FacesMessage("PiontsSpentAmount cannot exceed BalancePointsAmount!"));
                    this.setActionMessage("PiontsSpentAmount cannot exceed BalancePointsAmount!");
                } else if (!ItemMessage.equals("")) {
                    FacesContext.getCurrentInstance().addMessage("Update", new FacesMessage("INSUFFICIENT STOCK FOR ITEM(" + ItemMessage + ")..."));
                    this.setActionMessage("INSUFFICIENT STOCK FOR ITEM(" + ItemMessage + ")...");
                } else {
                    this.updateTransV2(aNewTrans, aNewTransItems, aPay);
                }
            } else {
                this.setActionMessage("THIS TRANSACTION IS INVALID!");
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public Trans getTrans(long aTransactionId) {
        String sql = "{call sp_search_transaction_by_id(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, aTransactionId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return this.getTransFromResultset(rs);
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public Trans getTransFromHist(long aTransactionHistId) {
        String sql = "{call sp_search_transaction_hist_by_id(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, aTransactionHistId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return this.getTransHistFromResultset(rs);
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public void setTransFromOrder(Trans aTrans, String aTransOrderNumber, long aTransactorId) {
        String sql = "";
        if (aTransactorId > 0) {
            sql = "SELECT * FROM transaction WHERE transaction_number='" + aTransOrderNumber + "' and transactor_id=" + aTransactorId;
        } else {
            sql = "SELECT * FROM transaction WHERE transaction_number='" + aTransOrderNumber + "'";
        }
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setLong(1, aTransactionHistId);
            rs = ps.executeQuery();
            if (rs.next()) {
                this.setTransFromResultset(aTrans, rs);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void setTransFromTransfer(Trans aTrans, String aTransTransferNumber, int aStore2Id) {
        String sql = "";
        sql = "SELECT * FROM transaction WHERE transaction_type_id=4 AND transaction_number='" + aTransTransferNumber + "' and store2_id=" + aStore2Id;
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setLong(1, aTransactionHistId);
            rs = ps.executeQuery();
            if (rs.next()) {
                this.setTransFromResultset(aTrans, rs);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void setTransByTransNumber(Trans aTrans, String aTransNumber, long aTransactorId) {
        String sql = "";
        if (aTransactorId > 0) {
            sql = "SELECT * FROM transaction WHERE transaction_number='" + aTransNumber + "' and transactor_id=" + aTransactorId;
        } else {
            sql = "SELECT * FROM transaction WHERE transaction_number='" + aTransNumber + "'";
        }
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            //ps.setLong(1, aTransactionHistId);
            rs = ps.executeQuery();
            if (rs.next()) {
                this.setTransFromResultset(aTrans, rs);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void setTransFromHist(Trans aTrans, long aTransactionHistId) {
        String sql = "{call sp_search_transaction_hist_by_id(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, aTransactionHistId);
            rs = ps.executeQuery();
            if (rs.next()) {
                this.setTransHistFromResultset(aTrans, rs);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void deleteTransFromHist(long aTransactionHistId) {
        String sql = "{call sp_delete_transaction_hist_by_id(?)}";
        if (aTransactionHistId > 0) {
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setLong(1, aTransactionHistId);
                ps.executeUpdate();
                this.refreshTranssDraft(new GeneralUserSetting().getCurrentStore().getStoreId(), new GeneralUserSetting().getCurrentUser().getUserDetailId(), new GeneralUserSetting().getCurrentTransactionTypeId(), new GeneralUserSetting().getCurrentTransactionReasonId());
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    public Trans getTransByTransNumber(String aTransactionNumber) {
        String sql = "{call sp_search_transaction_by_number(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, aTransactionNumber);
            rs = ps.executeQuery();
            if (rs.next()) {
                return this.getTransFromResultset(rs);
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public Trans getTransByIdType(long aTransactionId, int aTransactionTypeId) {
        String sql = "{call sp_search_transaction_by_id_type(?,?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, aTransactionId);
            ps.setInt(2, aTransactionTypeId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return this.getTransFromResultset(rs);
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public Trans getTransByNumberType(String aTransactionNumber, int aTransactionTypeId) {
        String sql = "{call sp_search_transaction_by_number_type(?,?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, aTransactionNumber);
            ps.setInt(2, aTransactionTypeId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return this.getTransFromResultset(rs);
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public void setTransByStoreIdType(int aStoreId, long aTransactionId, int aTransactionTypeId, Trans aTrans, List<TransItem> aActiveTransItems) {
        String sql = "{call sp_search_transaction_by_store_id_type(?,?,?)}";
        ResultSet rs = null;

        String msg;
        UserDetail aCurrentUserDetail = new GeneralUserSetting().getCurrentUser();
        List<GroupRight> aCurrentGroupRights = new GeneralUserSetting().getCurrentGroupRights();
        GroupRightBean grb = new GroupRightBean();

        if (aTransactionTypeId == 2 && grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, "RETAIL SALE INVOICE", "View") == 0 && grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, "WHOLE SALE INVOICE", "View") == 0) {
            msg = "YOU ARE NOT ALLOWED TO USE THIS FUNCTION, CONTACT SYSTEM ADMINISTRATOR...";
            FacesContext.getCurrentInstance().addMessage("View", new FacesMessage(msg));
            this.setActionMessage(msg);
        } else if (aTransactionTypeId == 1 && grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, "PURCHASE INVOICE", "View") == 0) {
            msg = "YOU ARE NOT ALLOWED TO USE THIS FUNCTION, CONTACT SYSTEM ADMINISTRATOR...";
            FacesContext.getCurrentInstance().addMessage("View", new FacesMessage(msg));
            this.setActionMessage(msg);
        } else if (aTransactionTypeId == 3 && grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, "DISPOSE STOCK", "View") == 0) {
            msg = "YOU ARE NOT ALLOWED TO USE THIS FUNCTION, CONTACT SYSTEM ADMINISTRATOR...";
            FacesContext.getCurrentInstance().addMessage("View", new FacesMessage(msg));
            this.setActionMessage(msg);
        } else if (aTransactionTypeId == 7 && grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, "UNPACK", "View") == 0) {
            msg = "YOU ARE NOT ALLOWED TO USE THIS FUNCTION, CONTACT SYSTEM ADMINISTRATOR...";
            FacesContext.getCurrentInstance().addMessage("View", new FacesMessage(msg));
            this.setActionMessage(msg);
        } else if (aTransactionTypeId == 4 && grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, "TRANSFER", "View") == 0) {
            msg = "YOU ARE NOT ALLOWED TO USE THIS FUNCTION, CONTACT SYSTEM ADMINISTRATOR...";
            FacesContext.getCurrentInstance().addMessage("View", new FacesMessage(msg));
            this.setActionMessage(msg);
        } else {
            //msg = "";
            //FacesContext.getCurrentInstance().addMessage("View", new FacesMessage(msg));
            //this.setActionMessage(msg);
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setInt(1, aStoreId);
                ps.setLong(2, aTransactionId);
                ps.setInt(3, aTransactionTypeId);
                rs = ps.executeQuery();
                if (rs.next()) {
                    this.setTransFromResultset(aTrans, rs);

                    //update for the first payment
                    Pay firstPay = new Pay();
                    //firstPay = PayBean.getTransactionFirstPay(aTransactionId);
                    try {
                        aTrans.setPayMethod(firstPay.getPayMethodId());
                    } catch (NullPointerException npe) {
                        aTrans.setPayMethod(0);
                    }
                    try {
                        aTrans.setSpendPointsAmount(firstPay.getPointsSpentAmount());
                    } catch (NullPointerException npe) {
                        aTrans.setSpendPointsAmount(0);
                    }
                    try {
                        aTrans.setSpendPoints(firstPay.getPointsSpent());
                    } catch (NullPointerException npe) {
                        aTrans.setSpendPoints(0);
                    }
                    //update for the loyality card point
                    try {
                        aTrans.setCardHolder("");
                    } catch (NullPointerException npe) {
                        aTrans.setCardHolder("");
                    }
                    try {
                        aTrans.setBalancePointsAmount(0);
                    } catch (NullPointerException npe) {
                        aTrans.setBalancePointsAmount(0);
                    }

                    new NavigationBean().defineTransactionTypes(aTransactionTypeId, new TransactionTypeBean().getTransactionType(aTransactionTypeId).getTransactionTypeName(), "", "");
                } else {
                    this.clearTransEdit(aTrans);
                    aTrans = null;
                    new NavigationBean().defineTransactionTypes(0, "", "", "");
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
                aTrans = null;
            }
            //trans items
            if (aTrans != null) {
                new TransItemBean().setTransItemsByTransactionId(aActiveTransItems, aTrans.getTransactionId());
            } else {
                new TransItemBean().setTransItemsByTransactionId(aActiveTransItems, 0);
            }
        }
    }

    public void setTransByStoreNumberType(int aStoreId, String aTransactionNumber, int aTransactionTypeId, Trans aTrans, List<TransItem> aActiveTransItems) {
        String sql = "{call sp_search_transaction_by_store_number_type(?,?,?)}";
        ResultSet rs = null;
        String msg;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, aStoreId);
            ps.setString(2, aTransactionNumber);
            ps.setInt(3, aTransactionTypeId);
            rs = ps.executeQuery();
            if (rs.next()) {
                this.setTransFromResultset(aTrans, rs);
                //update for the first payment
                Pay firstPay = new Pay();
                firstPay = new PayBean().getTransactionFirstPayByTransNo(aTransactionNumber);
                try {
                    aTrans.setPayMethod(firstPay.getPayMethodId());
                } catch (NullPointerException npe) {
                    aTrans.setPayMethod(0);
                }
                try {
                    aTrans.setSpendPointsAmount(firstPay.getPointsSpentAmount());
                } catch (NullPointerException npe) {
                    aTrans.setSpendPointsAmount(0);
                }
                try {
                    aTrans.setSpendPoints(firstPay.getPointsSpent());
                } catch (NullPointerException npe) {
                    aTrans.setSpendPoints(0);
                }
                //update for the loyality card point
                try {
                    aTrans.setCardHolder("");
                } catch (NullPointerException npe) {
                    aTrans.setCardHolder("");
                }
                try {
                    aTrans.setBalancePointsAmount(0);
                } catch (NullPointerException npe) {
                    aTrans.setBalancePointsAmount(0);
                }

                new NavigationBean().defineTransactionTypes(aTransactionTypeId, new TransactionTypeBean().getTransactionType(aTransactionTypeId).getTransactionTypeName(), "", "");
            } else {
                this.clearTransEdit(aTrans);
                aTrans = null;
                new NavigationBean().defineTransactionTypes(0, "", "", "");
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            aTrans = null;
        }
        //trans items
        if (aTrans != null) {
            new TransItemBean().setTransItemsByTransactionNumber(aActiveTransItems, aTrans.getTransactionNumber());
        } else {
            new TransItemBean().setTransItemsByTransactionNumber(aActiveTransItems, "");
        }
    }

    public Trans getTransFromResultset(ResultSet aResultSet) {
        try {
            Trans trans = new Trans();
            trans.setTransactionId(aResultSet.getLong("transaction_id"));
            trans.setTransactionDate(new Date(aResultSet.getDate("transaction_date").getTime()));
            trans.setStoreId(aResultSet.getInt("store_id"));

            try {
                trans.setStore2Id(aResultSet.getInt("store2_id"));
            } catch (NullPointerException npe) {
                trans.setStore2Id(0);
            }
            try {
                trans.setTransactorId(aResultSet.getLong("transactor_id"));
            } catch (NullPointerException npe) {
                trans.setTransactorId(0);
            }
            try {
                trans.setTransactionTypeId(aResultSet.getInt("transaction_type_id"));
            } catch (NullPointerException npe) {
                trans.setTransactionTypeId(0);
            }
            try {
                trans.setTransactionReasonId(aResultSet.getInt("transaction_reason_id"));
            } catch (NullPointerException npe) {
                trans.setTransactionReasonId(0);
            }
            try {
                trans.setSubTotal(aResultSet.getDouble("sub_total"));
            } catch (NullPointerException npe) {
                trans.setSubTotal(0);
            }
            try {
                trans.setTotalTradeDiscount(aResultSet.getDouble("total_trade_discount"));
            } catch (NullPointerException npe) {
                trans.setTotalTradeDiscount(0);
            }
            try {
                trans.setTotalVat(aResultSet.getDouble("total_vat"));
            } catch (NullPointerException npe) {
                trans.setTotalVat(0);
            }
            try {
                trans.setCashDiscount(aResultSet.getDouble("cash_discount"));
            } catch (NullPointerException npe) {
                trans.setCashDiscount(0);
            }
            try {
                trans.setGrandTotal(aResultSet.getDouble("grand_total"));
            } catch (NullPointerException npe) {
                trans.setGrandTotal(0);
            }
            try {
                trans.setTransactionRef(aResultSet.getString("transaction_ref"));
            } catch (NullPointerException npe) {
                trans.setTransactionRef("");
            }
            try {
                trans.setTransactionComment(aResultSet.getString("transaction_comment"));
            } catch (NullPointerException npe) {
                trans.setTransactionComment("");
            }
            try {
                trans.setAddUserDetailId(aResultSet.getInt("add_user_detail_id"));
            } catch (NullPointerException npe) {
                trans.setAddUserDetailId(0);
            }
            try {
                trans.setAddDate(new Date(aResultSet.getTimestamp("add_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setAddDate(null);
            }
            try {
                trans.setEditUserDetailId(aResultSet.getInt("edit_user_detail_id"));
            } catch (NullPointerException npe) {
                trans.setEditUserDetailId(0);
            }
            try {
                trans.setEditDate(new Date(aResultSet.getTimestamp("edit_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setEditDate(null);
            }
            try {
                trans.setPointsAwarded(aResultSet.getDouble("points_awarded"));
            } catch (NullPointerException npe) {
                trans.setPointsAwarded(0);
            }
            try {
                trans.setCardNumber(aResultSet.getString("card_number"));
            } catch (NullPointerException npe) {
                trans.setCardNumber("");
            }
            try {
                trans.setTotalStdVatableAmount(aResultSet.getDouble("total_std_vatable_amount"));
            } catch (NullPointerException npe) {
                trans.setTotalStdVatableAmount(0);
            }
            try {
                trans.setTotalZeroVatableAmount(aResultSet.getDouble("total_zero_vatable_amount"));
            } catch (NullPointerException npe) {
                trans.setTotalZeroVatableAmount(0);
            }
            try {
                trans.setTotalExemptVatableAmount(aResultSet.getDouble("total_exempt_vatable_amount"));
            } catch (NullPointerException npe) {
                trans.setTotalExemptVatableAmount(0);
            }
            try {
                trans.setVatPerc(aResultSet.getDouble("vat_perc"));
            } catch (NullPointerException npe) {
                trans.setVatPerc(0);
            }
            try {
                trans.setAmountTendered(aResultSet.getDouble("amount_tendered"));
            } catch (NullPointerException npe) {
                trans.setAmountTendered(0);
            }
            try {
                trans.setChangeAmount(aResultSet.getDouble("change_amount"));
            } catch (NullPointerException npe) {
                trans.setChangeAmount(0);
            }
            try {
                trans.setIsCashDiscountVatLiable(aResultSet.getString("is_cash_discount_vat_liable"));
            } catch (NullPointerException npe) {
                trans.setIsCashDiscountVatLiable("");
            }

            //for report only
            try {
                trans.setStoreName(aResultSet.getString("store_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setStoreName("");
            }
            try {
                trans.setStore2Name(aResultSet.getString("store_name2"));
            } catch (NullPointerException | SQLException npe) {
                trans.setStore2Name("");
            }
            try {
                trans.setTransactorName(aResultSet.getString("transactor_names"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactorName("");
            }
            try {
                trans.setBillTransactorName(aResultSet.getString("bill_transactor_names"));
            } catch (NullPointerException | SQLException npe) {
                trans.setBillTransactorName("");
            }
            try {
                trans.setTransactionTypeName(aResultSet.getString("transaction_type_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionTypeName("");
            }
            try {
                trans.setTransactionReasonName(aResultSet.getString("transaction_reason_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionReasonName("");
            }
            try {
                trans.setAddUserDetailName(aResultSet.getString("add_user_detail_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setAddUserDetailName("");
            }
            try {
                trans.setEditUserDetailName(aResultSet.getString("edit_user_detail_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setEditUserDetailName("");
            }
            try {
                trans.setTransactionUserDetailName(aResultSet.getString("transaction_user_detail_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionUserDetailName("");
            }

            try {
                trans.setTotalProfitMargin(aResultSet.getDouble("total_profit_margin"));
            } catch (NullPointerException npe) {
                trans.setTotalProfitMargin(0);
            }

            try {
                trans.setTransactionUserDetailId(aResultSet.getInt("transaction_user_detail_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionUserDetailId(0);
            }

            try {
                trans.setBillTransactorId(aResultSet.getLong("bill_transactor_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setBillTransactorId(0);
            }

            try {
                trans.setSchemeTransactorId(aResultSet.getLong("scheme_transactor_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setSchemeTransactorId(0);
            }

            try {
                trans.setSchemeTransactorName(aResultSet.getString("scheme_transactor_names"));
            } catch (NullPointerException | SQLException npe) {
                trans.setSchemeTransactorName("");
            }

            try {
                trans.setPrincSchemeMember(aResultSet.getString("princ_scheme_member"));
            } catch (NullPointerException | SQLException npe) {
                trans.setPrincSchemeMember("");
            }

            try {
                trans.setSchemeCardNumber(aResultSet.getString("scheme_card_number"));
            } catch (NullPointerException | SQLException npe) {
                trans.setSchemeCardNumber("");
            }
            try {
                trans.setTransactionNumber(aResultSet.getString("transaction_number"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionNumber("");
            }
            try {
                trans.setDeliveryDate(new Date(aResultSet.getDate("delivery_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setDeliveryDate(null);
            }
            try {
                trans.setDeliveryAddress(aResultSet.getString("delivery_address"));
            } catch (NullPointerException | SQLException npe) {
                trans.setDeliveryAddress("");
            }
            try {
                trans.setPayTerms(aResultSet.getString("pay_terms"));
            } catch (NullPointerException | SQLException npe) {
                trans.setPayTerms("");
            }
            try {
                trans.setTermsConditions(aResultSet.getString("terms_conditions"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTermsConditions("");
            }
            try {
                trans.setAuthorisedByUserDetailId(aResultSet.getInt("authorised_by_user_detail_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setAuthorisedByUserDetailId(0);
            }
            try {
                trans.setAuthoriseDate(new Date(aResultSet.getDate("authorise_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setAuthoriseDate(null);
            }
            try {
                trans.setPayDueDate(new Date(aResultSet.getDate("pay_due_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setPayDueDate(null);
            }
            try {
                trans.setExpiryDate(new Date(aResultSet.getDate("expiry_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setExpiryDate(null);
            }
            try {
                trans.setAccChildAccountId(aResultSet.getInt("acc_child_account_id"));
            } catch (NullPointerException npe) {
                trans.setAccChildAccountId(0);
            }
            try {
                trans.setCurrencyCode(aResultSet.getString("currency_code"));
            } catch (NullPointerException | SQLException npe) {
                trans.setCurrencyCode("");
            }
            try {
                trans.setXrate(aResultSet.getDouble("xrate"));
            } catch (NullPointerException npe) {
                trans.setXrate(1);
            }
            try {
                trans.setFrom_date(new Date(aResultSet.getDate("from_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setFrom_date(null);
            }
            try {
                trans.setTo_date(new Date(aResultSet.getDate("to_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setTo_date(null);
            }
            try {
                trans.setDuration_type(aResultSet.getString("duration_type"));
            } catch (NullPointerException npe) {
                trans.setDuration_type("");
            }
            try {
                trans.setSite_id(aResultSet.getLong("site_id"));
            } catch (NullPointerException npe) {
                trans.setSite_id(0);
            }
            try {
                trans.setTransactor_rep(aResultSet.getString("transactor_rep"));
            } catch (NullPointerException npe) {
                trans.setTransactor_rep("");
            }
            try {
                trans.setTransactor_vehicle(aResultSet.getString("transactor_vehicle"));
            } catch (NullPointerException npe) {
                trans.setTransactor_vehicle("");
            }
            try {
                trans.setTransactor_driver(aResultSet.getString("transactor_driver"));
            } catch (NullPointerException npe) {
                trans.setTransactor_driver("");
            }
            try {
                trans.setDuration_value(aResultSet.getDouble("duration_value"));
            } catch (NullPointerException npe) {
                trans.setDuration_value(0);
            }
            //mimic for cash discount percent
            try {
                this.setCashDiscountPerc(trans);
            } catch (NullPointerException npe) {
                trans.setCash_dicsount_perc(0);
            }
            try {
                trans.setLocation_id(aResultSet.getLong("location_id"));
            } catch (NullPointerException npe) {
                trans.setLocation_id(0);
            }
            if (null != aResultSet.getString("status_code")) {
                trans.setStatus_code(aResultSet.getString("status_code"));
            } else {
                trans.setStatus_code("");
            }
            try {
                trans.setStatus_date(new Date(aResultSet.getTimestamp("status_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setStatus_date(null);
            }
            if (null != aResultSet.getString("delivery_mode")) {
                trans.setDelivery_mode(aResultSet.getString("delivery_mode"));
            } else {
                trans.setDelivery_mode("");
            }
            try {
                trans.setIs_processed(aResultSet.getInt("is_processed"));
            } catch (NullPointerException npe) {
                trans.setIs_processed(0);
            }
            try {
                trans.setIs_paid(aResultSet.getInt("is_paid"));
            } catch (NullPointerException npe) {
                trans.setIs_paid(0);
            }
            try {
                trans.setIs_cancel(aResultSet.getInt("is_cancel"));
            } catch (NullPointerException npe) {
                trans.setIs_cancel(0);
            }
            try {
                trans.setIs_invoiced(aResultSet.getInt("is_invoiced"));
            } catch (NullPointerException npe) {
                trans.setIs_invoiced(0);
            }
            try {
                trans.setIs_delivered(aResultSet.getInt("is_delivered"));
            } catch (NullPointerException npe) {
                trans.setIs_delivered(0);
            }
            try {
                trans.setSource_code(aResultSet.getString("source_code"));
            } catch (NullPointerException npe) {
                trans.setSource_code("");
            }
            try {
                trans.setTotalPaid(aResultSet.getDouble("total_paid"));
            } catch (Exception e) {
                trans.setTotalPaid(0);
            }
            try {
                trans.setSpendPointsAmount(aResultSet.getDouble("spent_points_amount"));
            } catch (Exception e) {
                trans.setSpendPointsAmount(0);
            }
            try {
                trans.setMode_code(aResultSet.getInt("mode_code"));
            } catch (Exception e) {
                trans.setMode_code(0);
            }
            return trans;

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public Trans getTransHistFromResultset(ResultSet aResultSet) {
        try {
            Trans trans = new Trans();
            trans.setTransactionHistId(aResultSet.getLong("transaction_hist_id"));
            trans.setTransactionId(aResultSet.getLong("transaction_id"));
            trans.setTransactionDate(new Date(aResultSet.getDate("transaction_date").getTime()));
            trans.setStoreId(aResultSet.getInt("store_id"));
            try {
                trans.setHist_add_date(new Date(aResultSet.getTimestamp("hist_add_date").getTime()));
            } catch (Exception npe) {
                trans.setHist_add_date(null);
            }
            try {
                trans.setStore2Id(aResultSet.getInt("store2_id"));
            } catch (NullPointerException npe) {
                trans.setStore2Id(0);
            }
            try {
                trans.setTransactorId(aResultSet.getLong("transactor_id"));
            } catch (NullPointerException npe) {
                trans.setTransactorId(0);
            }
            try {
                trans.setTransactionTypeId(aResultSet.getInt("transaction_type_id"));
            } catch (NullPointerException npe) {
                trans.setTransactionTypeId(0);
            }
            try {
                trans.setTransactionReasonId(aResultSet.getInt("transaction_reason_id"));
            } catch (NullPointerException npe) {
                trans.setTransactionReasonId(0);
            }
            try {
                trans.setSubTotal(aResultSet.getDouble("sub_total"));
            } catch (NullPointerException npe) {
                trans.setSubTotal(0);
            }
            try {
                trans.setTotalTradeDiscount(aResultSet.getDouble("total_trade_discount"));
            } catch (NullPointerException npe) {
                trans.setTotalTradeDiscount(0);
            }
            try {
                trans.setTotalVat(aResultSet.getDouble("total_vat"));
            } catch (NullPointerException npe) {
                trans.setTotalVat(0);
            }
            try {
                trans.setCashDiscount(aResultSet.getDouble("cash_discount"));
            } catch (NullPointerException npe) {
                trans.setCashDiscount(0);
            }
            try {
                trans.setGrandTotal(aResultSet.getDouble("grand_total"));
            } catch (NullPointerException npe) {
                trans.setGrandTotal(0);
            }
            try {
                trans.setTransactionRef(aResultSet.getString("transaction_ref"));
            } catch (NullPointerException npe) {
                trans.setTransactionRef("");
            }
            try {
                trans.setTransactionComment(aResultSet.getString("transaction_comment"));
            } catch (NullPointerException npe) {
                trans.setTransactionComment("");
            }
            try {
                trans.setAddUserDetailId(aResultSet.getInt("add_user_detail_id"));
            } catch (NullPointerException npe) {
                trans.setAddUserDetailId(0);
            }
            try {
                trans.setAddDate(new Date(aResultSet.getTimestamp("add_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setAddDate(null);
            }
            try {
                trans.setEditUserDetailId(aResultSet.getInt("edit_user_detail_id"));
            } catch (NullPointerException npe) {
                trans.setEditUserDetailId(0);
            }
            try {
                trans.setEditDate(new Date(aResultSet.getTimestamp("edit_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setEditDate(null);
            }
            try {
                trans.setPointsAwarded(aResultSet.getDouble("points_awarded"));
            } catch (NullPointerException npe) {
                trans.setPointsAwarded(0);
            }
            try {
                trans.setCardNumber(aResultSet.getString("card_number"));
            } catch (NullPointerException npe) {
                trans.setCardNumber("");
            }
            try {
                trans.setTotalStdVatableAmount(aResultSet.getDouble("total_std_vatable_amount"));
            } catch (NullPointerException npe) {
                trans.setTotalStdVatableAmount(0);
            }
            try {
                trans.setTotalZeroVatableAmount(aResultSet.getDouble("total_zero_vatable_amount"));
            } catch (NullPointerException npe) {
                trans.setTotalZeroVatableAmount(0);
            }
            try {
                trans.setTotalExemptVatableAmount(aResultSet.getDouble("total_exempt_vatable_amount"));
            } catch (NullPointerException npe) {
                trans.setTotalExemptVatableAmount(0);
            }
            try {
                trans.setVatPerc(aResultSet.getDouble("vat_perc"));
            } catch (NullPointerException npe) {
                trans.setVatPerc(0);
            }
            try {
                trans.setAmountTendered(aResultSet.getDouble("amount_tendered"));
            } catch (NullPointerException npe) {
                trans.setAmountTendered(0);
            }
            try {
                trans.setChangeAmount(aResultSet.getDouble("change_amount"));
            } catch (NullPointerException npe) {
                trans.setChangeAmount(0);
            }
            try {
                trans.setIsCashDiscountVatLiable(aResultSet.getString("is_cash_discount_vat_liable"));
            } catch (NullPointerException npe) {
                trans.setIsCashDiscountVatLiable("");
            }

            //for report only
            try {
                trans.setStoreName(aResultSet.getString("store_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setStoreName("");
            }
            try {
                trans.setStore2Name(aResultSet.getString("store_name2"));
            } catch (NullPointerException | SQLException npe) {
                trans.setStore2Name("");
            }
            try {
                trans.setTransactorName(aResultSet.getString("transactor_names"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactorName("");
            }
            try {
                trans.setBillTransactorName(aResultSet.getString("bill_transactor_names"));
            } catch (NullPointerException | SQLException npe) {
                trans.setBillTransactorName("");
            }
            try {
                trans.setTransactionTypeName(aResultSet.getString("transaction_type_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionTypeName("");
            }
            try {
                trans.setTransactionReasonName(aResultSet.getString("transaction_reason_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionReasonName("");
            }
            try {
                trans.setAddUserDetailName(aResultSet.getString("add_user_detail_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setAddUserDetailName("");
            }
            try {
                trans.setEditUserDetailName(aResultSet.getString("edit_user_detail_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setEditUserDetailName("");
            }
            try {
                trans.setTransactionUserDetailName(aResultSet.getString("transaction_user_detail_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionUserDetailName("");
            }

            try {
                trans.setTotalProfitMargin(aResultSet.getDouble("total_profit_margin"));
            } catch (NullPointerException npe) {
                trans.setTotalProfitMargin(0);
            }

            try {
                trans.setTransactionUserDetailId(aResultSet.getInt("transaction_user_detail_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionUserDetailId(0);
            }

            try {
                trans.setBillTransactorId(aResultSet.getLong("bill_transactor_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setBillTransactorId(0);
            }

            try {
                trans.setSchemeTransactorId(aResultSet.getLong("scheme_transactor_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setSchemeTransactorId(0);
            }

            try {
                trans.setSchemeTransactorName(aResultSet.getString("scheme_transactor_names"));
            } catch (NullPointerException | SQLException npe) {
                trans.setSchemeTransactorName("");
            }

            try {
                trans.setPrincSchemeMember(aResultSet.getString("princ_scheme_member"));
            } catch (NullPointerException | SQLException npe) {
                trans.setPrincSchemeMember("");
            }

            try {
                trans.setSchemeCardNumber(aResultSet.getString("scheme_card_number"));
            } catch (NullPointerException | SQLException npe) {
                trans.setSchemeCardNumber("");
            }
            try {
                trans.setTransactionNumber(aResultSet.getString("transaction_number"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionNumber("");
            }
            try {
                trans.setDeliveryDate(new Date(aResultSet.getDate("delivery_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setDeliveryDate(null);
            }
            try {
                trans.setDeliveryAddress(aResultSet.getString("delivery_address"));
            } catch (NullPointerException | SQLException npe) {
                trans.setDeliveryAddress("");
            }
            try {
                trans.setPayTerms(aResultSet.getString("pay_terms"));
            } catch (NullPointerException | SQLException npe) {
                trans.setPayTerms("");
            }
            try {
                trans.setTermsConditions(aResultSet.getString("terms_conditions"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTermsConditions("");
            }
            try {
                trans.setAuthorisedByUserDetailId(aResultSet.getInt("authorised_by_user_detail_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setAuthorisedByUserDetailId(0);
            }
            try {
                trans.setAuthoriseDate(new Date(aResultSet.getDate("authorise_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setAuthoriseDate(null);
            }
            try {
                trans.setPayDueDate(new Date(aResultSet.getDate("pay_due_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setPayDueDate(null);
            }
            try {
                trans.setExpiryDate(new Date(aResultSet.getDate("expiry_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setExpiryDate(null);
            }
            try {
                trans.setAccChildAccountId(aResultSet.getInt("acc_child_account_id"));
            } catch (NullPointerException npe) {
                trans.setAccChildAccountId(0);
            }
            try {
                trans.setCurrencyCode(aResultSet.getString("currency_code"));
            } catch (NullPointerException | SQLException npe) {
                trans.setCurrencyCode("");
            }
            try {
                trans.setXrate(aResultSet.getDouble("xrate"));
            } catch (NullPointerException npe) {
                trans.setXrate(1);
            }
            try {
                trans.setFrom_date(new Date(aResultSet.getDate("from_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setFrom_date(null);
            }
            try {
                trans.setTo_date(new Date(aResultSet.getDate("to_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setTo_date(null);
            }
            try {
                trans.setDuration_type(aResultSet.getString("duration_type"));
            } catch (NullPointerException npe) {
                trans.setDuration_type("");
            }
            try {
                trans.setSite_id(aResultSet.getLong("site_id"));
            } catch (NullPointerException npe) {
                trans.setSite_id(0);
            }
            try {
                trans.setTransactor_rep(aResultSet.getString("transactor_rep"));
            } catch (NullPointerException npe) {
                trans.setTransactor_rep("");
            }
            try {
                trans.setTransactor_vehicle(aResultSet.getString("transactor_vehicle"));
            } catch (NullPointerException npe) {
                trans.setTransactor_vehicle("");
            }
            try {
                trans.setTransactor_driver(aResultSet.getString("transactor_driver"));
            } catch (NullPointerException npe) {
                trans.setTransactor_driver("");
            }
            try {
                trans.setDuration_value(aResultSet.getDouble("duration_value"));
            } catch (NullPointerException npe) {
                trans.setDuration_value(0);
            }
            try {
                trans.setLocation_id(aResultSet.getLong("location_id"));
            } catch (NullPointerException npe) {
                trans.setLocation_id(0);
            }
            if (null != aResultSet.getString("status_code")) {
                trans.setStatus_code(aResultSet.getString("status_code"));
            } else {
                trans.setStatus_code("");
            }
            try {
                trans.setStatus_date(new Date(aResultSet.getTimestamp("status_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setStatus_date(null);
            }
            if (null != aResultSet.getString("delivery_mode")) {
                trans.setDelivery_mode(aResultSet.getString("delivery_mode"));
            } else {
                trans.setDelivery_mode("");
            }
            try {
                trans.setIs_processed(aResultSet.getInt("is_processed"));
            } catch (NullPointerException npe) {
                trans.setIs_processed(0);
            }
            try {
                trans.setIs_paid(aResultSet.getInt("is_paid"));
            } catch (NullPointerException npe) {
                trans.setIs_paid(0);
            }
            try {
                trans.setIs_cancel(aResultSet.getInt("is_cancel"));
            } catch (NullPointerException npe) {
                trans.setIs_cancel(0);
            }
            try {
                trans.setIs_invoiced(aResultSet.getInt("is_invoiced"));
            } catch (NullPointerException npe) {
                trans.setIs_invoiced(0);
            }
            try {
                trans.setIs_delivered(aResultSet.getInt("is_delivered"));
            } catch (NullPointerException npe) {
                trans.setIs_delivered(0);
            }
            try {
                trans.setSource_code(aResultSet.getString("source_code"));
            } catch (NullPointerException npe) {
                trans.setSource_code("");
            }
            return trans;

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }
    }

    public void setTransHistFromResultset(Trans trans, ResultSet aResultSet) {
        try {
            //Trans trans = new Trans();
            trans.setTransactionHistId(aResultSet.getLong("transaction_hist_id"));
            trans.setTransactionId(aResultSet.getLong("transaction_id"));
            trans.setTransactionDate(new Date(aResultSet.getDate("transaction_date").getTime()));
            trans.setStoreId(aResultSet.getInt("store_id"));

            try {
                trans.setStore2Id(aResultSet.getInt("store2_id"));
            } catch (NullPointerException npe) {
                trans.setStore2Id(0);
            }
            try {
                trans.setTransactorId(aResultSet.getLong("transactor_id"));
            } catch (NullPointerException npe) {
                trans.setTransactorId(0);
            }
            try {
                trans.setTransactionTypeId(aResultSet.getInt("transaction_type_id"));
            } catch (NullPointerException npe) {
                trans.setTransactionTypeId(0);
            }
            try {
                trans.setTransactionReasonId(aResultSet.getInt("transaction_reason_id"));
            } catch (NullPointerException npe) {
                trans.setTransactionReasonId(0);
            }
            try {
                trans.setSubTotal(aResultSet.getDouble("sub_total"));
            } catch (NullPointerException npe) {
                trans.setSubTotal(0);
            }
            try {
                trans.setTotalTradeDiscount(aResultSet.getDouble("total_trade_discount"));
            } catch (NullPointerException npe) {
                trans.setTotalTradeDiscount(0);
            }
            try {
                trans.setTotalVat(aResultSet.getDouble("total_vat"));
            } catch (NullPointerException npe) {
                trans.setTotalVat(0);
            }
            try {
                trans.setCashDiscount(aResultSet.getDouble("cash_discount"));
            } catch (NullPointerException npe) {
                trans.setCashDiscount(0);
            }
            try {
                trans.setGrandTotal(aResultSet.getDouble("grand_total"));
            } catch (NullPointerException npe) {
                trans.setGrandTotal(0);
            }
            try {
                trans.setTransactionRef(aResultSet.getString("transaction_ref"));
            } catch (NullPointerException npe) {
                trans.setTransactionRef("");
            }
            try {
                trans.setTransactionComment(aResultSet.getString("transaction_comment"));
            } catch (NullPointerException npe) {
                trans.setTransactionComment("");
            }
            try {
                trans.setAddUserDetailId(aResultSet.getInt("add_user_detail_id"));
            } catch (NullPointerException npe) {
                trans.setAddUserDetailId(0);
            }
            try {
                trans.setAddDate(new Date(aResultSet.getTimestamp("add_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setAddDate(null);
            }
            try {
                trans.setEditUserDetailId(aResultSet.getInt("edit_user_detail_id"));
            } catch (NullPointerException npe) {
                trans.setEditUserDetailId(0);
            }
            try {
                trans.setEditDate(new Date(aResultSet.getTimestamp("edit_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setEditDate(null);
            }
            try {
                trans.setPointsAwarded(aResultSet.getDouble("points_awarded"));
            } catch (NullPointerException npe) {
                trans.setPointsAwarded(0);
            }
            try {
                trans.setCardNumber(aResultSet.getString("card_number"));
            } catch (NullPointerException npe) {
                trans.setCardNumber("");
            }
            try {
                trans.setTotalStdVatableAmount(aResultSet.getDouble("total_std_vatable_amount"));
            } catch (NullPointerException npe) {
                trans.setTotalStdVatableAmount(0);
            }
            try {
                trans.setTotalZeroVatableAmount(aResultSet.getDouble("total_zero_vatable_amount"));
            } catch (NullPointerException npe) {
                trans.setTotalZeroVatableAmount(0);
            }
            try {
                trans.setTotalExemptVatableAmount(aResultSet.getDouble("total_exempt_vatable_amount"));
            } catch (NullPointerException npe) {
                trans.setTotalExemptVatableAmount(0);
            }
            try {
                trans.setVatPerc(aResultSet.getDouble("vat_perc"));
            } catch (NullPointerException npe) {
                trans.setVatPerc(0);
            }
            try {
                trans.setAmountTendered(aResultSet.getDouble("amount_tendered"));
            } catch (NullPointerException npe) {
                trans.setAmountTendered(0);
            }
            try {
                trans.setChangeAmount(aResultSet.getDouble("change_amount"));
            } catch (NullPointerException npe) {
                trans.setChangeAmount(0);
            }
            try {
                trans.setIsCashDiscountVatLiable(aResultSet.getString("is_cash_discount_vat_liable"));
            } catch (NullPointerException npe) {
                trans.setIsCashDiscountVatLiable("");
            }

            //for report only
            try {
                trans.setStoreName(aResultSet.getString("store_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setStoreName("");
            }
            try {
                trans.setStore2Name(aResultSet.getString("store_name2"));
            } catch (NullPointerException | SQLException npe) {
                trans.setStore2Name("");
            }
            try {
                trans.setTransactorName(aResultSet.getString("transactor_names"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactorName("");
            }
            try {
                trans.setBillTransactorName(aResultSet.getString("bill_transactor_names"));
            } catch (NullPointerException | SQLException npe) {
                trans.setBillTransactorName("");
            }
            try {
                trans.setTransactionTypeName(aResultSet.getString("transaction_type_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionTypeName("");
            }
            try {
                trans.setTransactionReasonName(aResultSet.getString("transaction_reason_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionReasonName("");
            }
            try {
                trans.setAddUserDetailName(aResultSet.getString("add_user_detail_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setAddUserDetailName("");
            }
            try {
                trans.setEditUserDetailName(aResultSet.getString("edit_user_detail_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setEditUserDetailName("");
            }
            try {
                trans.setTransactionUserDetailName(aResultSet.getString("transaction_user_detail_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionUserDetailName("");
            }

            try {
                trans.setTotalProfitMargin(aResultSet.getDouble("total_profit_margin"));
            } catch (NullPointerException npe) {
                trans.setTotalProfitMargin(0);
            }

            try {
                trans.setTransactionUserDetailId(aResultSet.getInt("transaction_user_detail_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionUserDetailId(0);
            }

            try {
                trans.setBillTransactorId(aResultSet.getLong("bill_transactor_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setBillTransactorId(0);
            }

            try {
                trans.setSchemeTransactorId(aResultSet.getLong("scheme_transactor_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setSchemeTransactorId(0);
            }

            try {
                trans.setSchemeTransactorName(aResultSet.getString("scheme_transactor_names"));
            } catch (NullPointerException | SQLException npe) {
                trans.setSchemeTransactorName("");
            }

            try {
                trans.setPrincSchemeMember(aResultSet.getString("princ_scheme_member"));
            } catch (NullPointerException | SQLException npe) {
                trans.setPrincSchemeMember("");
            }

            try {
                trans.setSchemeCardNumber(aResultSet.getString("scheme_card_number"));
            } catch (NullPointerException | SQLException npe) {
                trans.setSchemeCardNumber("");
            }
            try {
                trans.setTransactionNumber(aResultSet.getString("transaction_number"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionNumber("");
            }
            try {
                trans.setDeliveryDate(new Date(aResultSet.getDate("delivery_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setDeliveryDate(null);
            }
            try {
                trans.setDeliveryAddress(aResultSet.getString("delivery_address"));
            } catch (NullPointerException | SQLException npe) {
                trans.setDeliveryAddress("");
            }
            try {
                trans.setPayTerms(aResultSet.getString("pay_terms"));
            } catch (NullPointerException | SQLException npe) {
                trans.setPayTerms("");
            }
            try {
                trans.setTermsConditions(aResultSet.getString("terms_conditions"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTermsConditions("");
            }
            try {
                trans.setAuthorisedByUserDetailId(aResultSet.getInt("authorised_by_user_detail_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setAuthorisedByUserDetailId(0);
            }
            try {
                trans.setAuthoriseDate(new Date(aResultSet.getDate("authorise_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setAuthoriseDate(null);
            }
            try {
                trans.setPayDueDate(new Date(aResultSet.getDate("pay_due_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setPayDueDate(null);
            }
            try {
                trans.setExpiryDate(new Date(aResultSet.getDate("expiry_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setExpiryDate(null);
            }
            try {
                trans.setAccChildAccountId(aResultSet.getInt("acc_child_account_id"));
            } catch (NullPointerException npe) {
                trans.setAccChildAccountId(0);
            }
            try {
                trans.setCurrencyCode(aResultSet.getString("currency_code"));
            } catch (NullPointerException | SQLException npe) {
                trans.setCurrencyCode("");
            }
            try {
                trans.setXrate(aResultSet.getDouble("xrate"));
            } catch (NullPointerException npe) {
                trans.setXrate(1);
            }
            try {
                trans.setFrom_date(new Date(aResultSet.getDate("from_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setFrom_date(null);
            }
            try {
                trans.setTo_date(new Date(aResultSet.getDate("to_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setTo_date(null);
            }
            try {
                trans.setDuration_type(aResultSet.getString("duration_type"));
            } catch (NullPointerException npe) {
                trans.setDuration_type("");
            }
            try {
                trans.setSite_id(aResultSet.getLong("site_id"));
            } catch (NullPointerException npe) {
                trans.setSite_id(0);
            }
            try {
                trans.setTransactor_rep(aResultSet.getString("transactor_rep"));
            } catch (NullPointerException npe) {
                trans.setTransactor_rep("");
            }
            try {
                trans.setTransactor_vehicle(aResultSet.getString("transactor_vehicle"));
            } catch (NullPointerException npe) {
                trans.setTransactor_vehicle("");
            }
            try {
                trans.setTransactor_driver(aResultSet.getString("transactor_driver"));
            } catch (NullPointerException npe) {
                trans.setTransactor_driver("");
            }
            try {
                trans.setDuration_value(aResultSet.getDouble("duration_value"));
            } catch (NullPointerException npe) {
                trans.setDuration_value(0);
            }
            try {
                trans.setLocation_id(aResultSet.getLong("location_id"));
            } catch (NullPointerException npe) {
                trans.setLocation_id(0);
            }
            if (null == aResultSet.getString("status_code")) {
                trans.setStatus_code("");
            } else {
                trans.setStatus_code(aResultSet.getString("status_code"));
            }
            try {
                trans.setStatus_date(new Date(aResultSet.getTimestamp("status_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setStatus_date(null);
            }
            if (null == aResultSet.getString("delivery_mode")) {
                trans.setDelivery_mode("");
            } else {
                trans.setDelivery_mode(aResultSet.getString("delivery_mode"));
            }
            try {
                trans.setIs_processed(aResultSet.getInt("is_processed"));
            } catch (NullPointerException npe) {
                trans.setIs_processed(0);
            }
            try {
                trans.setIs_paid(aResultSet.getInt("is_paid"));
            } catch (NullPointerException npe) {
                trans.setIs_paid(0);
            }
            try {
                trans.setIs_cancel(aResultSet.getInt("is_cancel"));
            } catch (NullPointerException npe) {
                trans.setIs_cancel(0);
            }
            try {
                trans.setIs_invoiced(aResultSet.getInt("is_invoiced"));
            } catch (NullPointerException npe) {
                trans.setIs_invoiced(0);
            }
            try {
                trans.setIs_delivered(aResultSet.getInt("is_delivered"));
            } catch (NullPointerException npe) {
                trans.setIs_delivered(0);
            }
            try {
                trans.setSource_code(aResultSet.getString("source_code"));
            } catch (NullPointerException npe) {
                trans.setSource_code("");
            }
            try {
                trans.setTotalPaid(aResultSet.getDouble("total_paid"));
            } catch (Exception e) {
                trans.setTotalPaid(0);
            }
            try {
                trans.setSpendPointsAmount(aResultSet.getDouble("spent_points_amount"));
            } catch (Exception e) {
                trans.setSpendPointsAmount(0);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void setTransFromResultset(Trans trans, ResultSet aResultSet) {
        try {
            //Trans trans = new Trans();
            trans.setTransactionId(aResultSet.getLong("transaction_id"));
            trans.setTransactionDate(new Date(aResultSet.getDate("transaction_date").getTime()));
            trans.setStoreId(aResultSet.getInt("store_id"));

            try {
                trans.setStore2Id(aResultSet.getInt("store2_id"));
            } catch (Exception e) {
                trans.setStore2Id(0);
            }
            try {
                trans.setTransactorId(aResultSet.getLong("transactor_id"));
            } catch (Exception e) {
                trans.setTransactorId(0);
            }
            try {
                trans.setTransactionTypeId(aResultSet.getInt("transaction_type_id"));
            } catch (Exception e) {
                trans.setTransactionTypeId(0);
            }
            try {
                trans.setTransactionReasonId(aResultSet.getInt("transaction_reason_id"));
            } catch (Exception e) {
                trans.setTransactionReasonId(0);
            }
            try {
                trans.setSubTotal(aResultSet.getDouble("sub_total"));
            } catch (Exception e) {
                trans.setSubTotal(0);
            }
            try {
                trans.setTotalTradeDiscount(aResultSet.getDouble("total_trade_discount"));
            } catch (Exception e) {
                trans.setTotalTradeDiscount(0);
            }
            try {
                trans.setTotalVat(aResultSet.getDouble("total_vat"));
            } catch (Exception e) {
                trans.setTotalVat(0);
            }
            try {
                trans.setCashDiscount(aResultSet.getDouble("cash_discount"));
            } catch (Exception e) {
                trans.setCashDiscount(0);
            }
            try {
                trans.setGrandTotal(aResultSet.getDouble("grand_total"));
            } catch (Exception e) {
                trans.setGrandTotal(0);
            }
            try {
                trans.setTransactionRef(aResultSet.getString("transaction_ref"));
            } catch (Exception e) {
                trans.setTransactionRef("");
            }
            try {
                trans.setTransactionComment(aResultSet.getString("transaction_comment"));
            } catch (Exception e) {
                trans.setTransactionComment("");
            }
            try {
                trans.setAddUserDetailId(aResultSet.getInt("add_user_detail_id"));
            } catch (Exception e) {
                trans.setAddUserDetailId(0);
            }
            try {
                trans.setAddDate(new Date(aResultSet.getTimestamp("add_date").getTime()));
            } catch (Exception e) {
                trans.setAddDate(null);
            }
            try {
                trans.setEditUserDetailId(aResultSet.getInt("edit_user_detail_id"));
            } catch (Exception e) {
                trans.setEditUserDetailId(0);
            }
            try {
                trans.setEditDate(new Date(aResultSet.getTimestamp("edit_date").getTime()));
            } catch (Exception e) {
                trans.setEditDate(null);
            }
            try {
                trans.setPointsAwarded(aResultSet.getDouble("points_awarded"));
            } catch (Exception e) {
                trans.setPointsAwarded(0);
            }
            try {
                trans.setCardNumber(aResultSet.getString("card_number"));
            } catch (Exception e) {
                trans.setCardNumber("");
            }
            try {
                trans.setTotalStdVatableAmount(aResultSet.getDouble("total_std_vatable_amount"));
            } catch (Exception e) {
                trans.setTotalStdVatableAmount(0);
            }
            try {
                trans.setTotalZeroVatableAmount(aResultSet.getDouble("total_zero_vatable_amount"));
            } catch (Exception e) {
                trans.setTotalZeroVatableAmount(0);
            }
            try {
                trans.setTotalExemptVatableAmount(aResultSet.getDouble("total_exempt_vatable_amount"));
            } catch (Exception e) {
                trans.setTotalExemptVatableAmount(0);
            }
            try {
                trans.setVatPerc(aResultSet.getDouble("vat_perc"));
            } catch (Exception e) {
                trans.setVatPerc(0);
            }
            try {
                trans.setAmountTendered(aResultSet.getDouble("amount_tendered"));
            } catch (Exception e) {
                trans.setAmountTendered(0);
            }
            try {
                trans.setChangeAmount(aResultSet.getDouble("change_amount"));
            } catch (Exception e) {
                trans.setChangeAmount(0);
            }
            try {
                trans.setIsCashDiscountVatLiable(aResultSet.getString("is_cash_discount_vat_liable"));
            } catch (Exception e) {
                trans.setIsCashDiscountVatLiable("");
            }

            //for report only
            try {
                trans.setStoreName(aResultSet.getString("store_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setStoreName("");
            }
            try {
                trans.setStore2Name(aResultSet.getString("store_name2"));
            } catch (NullPointerException | SQLException npe) {
                trans.setStore2Name("");
            }
            try {
                trans.setTransactorName(aResultSet.getString("transactor_names"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactorName("");
            }
            try {
                trans.setTransactionTypeName(aResultSet.getString("transaction_type_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionTypeName("");
            }
            try {
                trans.setTransactionReasonName(aResultSet.getString("transaction_reason_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionReasonName("");
            }
            try {
                trans.setAddUserDetailName(aResultSet.getString("add_user_detail_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setAddUserDetailName("");
            }
            try {
                trans.setEditUserDetailName(aResultSet.getString("edit_user_detail_name"));
            } catch (NullPointerException | SQLException npe) {
                trans.setEditUserDetailName("");
            }

            try {
                trans.setTotalProfitMargin(aResultSet.getDouble("total_profit_margin"));
            } catch (Exception e) {
                trans.setTotalProfitMargin(0);
            }

            try {
                trans.setTransactionUserDetailId(aResultSet.getInt("transaction_user_detail_id"));
            } catch (Exception e) {
                trans.setTransactionUserDetailId(0);
            }

            try {
                trans.setBillTransactorId(aResultSet.getLong("bill_transactor_id"));
            } catch (Exception e) {
                trans.setBillTransactorId(0);
            }

            try {
                trans.setSchemeTransactorId(aResultSet.getLong("scheme_transactor_id"));
            } catch (Exception e) {
                trans.setSchemeTransactorId(0);
            }

            try {
                trans.setPrincSchemeMember(aResultSet.getString("princ_scheme_member"));
            } catch (Exception e) {
                trans.setPrincSchemeMember("");
            }

            try {
                trans.setSchemeCardNumber(aResultSet.getString("scheme_card_number"));
            } catch (NullPointerException | SQLException npe) {
                trans.setSchemeCardNumber("");
            }

            try {
                trans.setTransactionNumber(aResultSet.getString("transaction_number"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionNumber("");
            }
            try {
                trans.setDeliveryDate(new Date(aResultSet.getDate("delivery_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setDeliveryDate(null);
            }
            try {
                trans.setDeliveryAddress(aResultSet.getString("delivery_address"));
            } catch (NullPointerException | SQLException npe) {
                trans.setDeliveryAddress("");
            }
            try {
                trans.setPayTerms(aResultSet.getString("pay_terms"));
            } catch (NullPointerException | SQLException npe) {
                trans.setPayTerms("");
            }
            try {
                trans.setTermsConditions(aResultSet.getString("terms_conditions"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTermsConditions("");
            }
            try {
                trans.setAuthorisedByUserDetailId(aResultSet.getInt("authorised_by_user_detail_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setAuthorisedByUserDetailId(0);
            }
            try {
                trans.setAuthoriseDate(new Date(aResultSet.getDate("authorise_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setAuthoriseDate(null);
            }
            try {
                trans.setPayDueDate(new Date(aResultSet.getDate("pay_due_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setPayDueDate(null);
            }
            try {
                trans.setExpiryDate(new Date(aResultSet.getDate("expiry_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setExpiryDate(null);
            }
            try {
                trans.setCurrencyCode(aResultSet.getString("currency_code"));
            } catch (NullPointerException | SQLException npe) {
                trans.setCurrencyCode("");
            }
            try {
                trans.setXrate(aResultSet.getDouble("xrate"));
            } catch (NullPointerException | SQLException npe) {
                trans.setXrate(1);
            }
            try {
                trans.setFrom_date(new Date(aResultSet.getDate("from_date").getTime()));
            } catch (Exception e) {
                trans.setFrom_date(null);
            }
            try {
                trans.setTo_date(new Date(aResultSet.getDate("to_date").getTime()));
            } catch (Exception e) {
                trans.setTo_date(null);
            }
            try {
                trans.setDuration_type(aResultSet.getString("duration_type"));
            } catch (Exception e) {
                trans.setDuration_type("");
            }
            try {
                trans.setSite_id(aResultSet.getLong("site_id"));
            } catch (Exception e) {
                trans.setSite_id(0);
            }
            try {
                trans.setTransactor_rep(aResultSet.getString("transactor_rep"));
            } catch (Exception e) {
                trans.setTransactor_rep("");
            }
            try {
                trans.setTransactor_vehicle(aResultSet.getString("transactor_vehicle"));
            } catch (Exception e) {
                trans.setTransactor_vehicle("");
            }
            try {
                trans.setTransactor_driver(aResultSet.getString("transactor_driver"));
            } catch (Exception e) {
                trans.setTransactor_driver("");
            }
            try {
                trans.setDuration_value(aResultSet.getDouble("duration_value"));
            } catch (Exception e) {
                trans.setDuration_value(0);
            }
            try {
                trans.setAccChildAccountId(aResultSet.getInt("acc_child_account_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setAccChildAccountId(0);
            }
            try {
                trans.setLocation_id(aResultSet.getLong("location_id"));
            } catch (Exception e) {
                trans.setLocation_id(0);
            }
            if (null == aResultSet.getString("status_code")) {
                trans.setStatus_code("");
            } else {
                trans.setStatus_code(aResultSet.getString("status_code"));
            }
            try {
                trans.setStatus_date(new Date(aResultSet.getTimestamp("status_date").getTime()));
            } catch (Exception e) {
                trans.setStatus_date(null);
            }
            if (null == aResultSet.getString("delivery_mode")) {
                trans.setDelivery_mode("");
            } else {
                trans.setDelivery_mode(aResultSet.getString("delivery_mode"));
            }
            try {
                trans.setIs_processed(aResultSet.getInt("is_processed"));
            } catch (Exception e) {
                trans.setIs_processed(0);
            }
            try {
                trans.setIs_paid(aResultSet.getInt("is_paid"));
            } catch (Exception e) {
                trans.setIs_paid(0);
            }
            try {
                trans.setIs_cancel(aResultSet.getInt("is_cancel"));
            } catch (Exception e) {
                trans.setIs_cancel(0);
            }
            try {
                trans.setIs_invoiced(aResultSet.getInt("is_invoiced"));
            } catch (Exception e) {
                trans.setIs_invoiced(0);
            }
            try {
                trans.setIs_delivered(aResultSet.getInt("is_delivered"));
            } catch (Exception e) {
                trans.setIs_delivered(0);
            }
            try {
                trans.setSource_code(aResultSet.getString("source_code"));
            } catch (Exception e) {
                trans.setSource_code("");
            }
            try {
                trans.setTotalPaid(aResultSet.getDouble("total_paid"));
            } catch (Exception e) {
                trans.setTotalPaid(0);
            }
            try {
                trans.setSpendPointsAmount(aResultSet.getDouble("spent_points_amount"));
            } catch (Exception e) {
                trans.setSpendPointsAmount(0);
            }
            //return trans;
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void setTransFromResultsetBMS(Trans trans, ResultSet aResultSet) {
        try {
            trans.setTransactionId(aResultSet.getLong("transaction_id"));
            trans.setTransactionDate(new Date(aResultSet.getDate("transaction_date").getTime()));
            trans.setStoreId(aResultSet.getInt("store_id"));

            try {
                trans.setStore2Id(aResultSet.getInt("store2_id"));
            } catch (NullPointerException npe) {
                trans.setStore2Id(0);
            }
            try {
                trans.setTransactorId(aResultSet.getLong("transactor_id"));
            } catch (NullPointerException npe) {
                trans.setTransactorId(0);
            }
            try {
                trans.setTransactionTypeId(aResultSet.getInt("transaction_type_id"));
            } catch (NullPointerException npe) {
                trans.setTransactionTypeId(0);
            }
            try {
                trans.setTransactionReasonId(aResultSet.getInt("transaction_reason_id"));
            } catch (NullPointerException npe) {
                trans.setTransactionReasonId(0);
            }
            try {
                trans.setSubTotal(aResultSet.getDouble("sub_total"));
            } catch (NullPointerException npe) {
                trans.setSubTotal(0);
            }
            try {
                trans.setTotalTradeDiscount(aResultSet.getDouble("total_trade_discount"));
            } catch (NullPointerException npe) {
                trans.setTotalTradeDiscount(0);
            }
            try {
                trans.setTotalVat(aResultSet.getDouble("total_vat"));
            } catch (NullPointerException npe) {
                trans.setTotalVat(0);
            }
            try {
                trans.setCashDiscount(aResultSet.getDouble("cash_discount"));
            } catch (NullPointerException npe) {
                trans.setCashDiscount(0);
            }
            try {
                trans.setGrandTotal(aResultSet.getDouble("grand_total"));
            } catch (NullPointerException npe) {
                trans.setGrandTotal(0);
            }
            try {
                trans.setTransactionRef(aResultSet.getString("transaction_ref"));
            } catch (NullPointerException npe) {
                trans.setTransactionRef("");
            }
            try {
                trans.setTransactionComment(aResultSet.getString("transaction_comment"));
            } catch (NullPointerException npe) {
                trans.setTransactionComment("");
            }
            try {
                trans.setAddUserDetailId(aResultSet.getInt("add_user_detail_id"));
            } catch (NullPointerException npe) {
                trans.setAddUserDetailId(0);
            }
            try {
                trans.setAddDate(new Date(aResultSet.getTimestamp("add_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setAddDate(null);
            }
            try {
                trans.setEditUserDetailId(aResultSet.getInt("edit_user_detail_id"));
            } catch (NullPointerException npe) {
                trans.setEditUserDetailId(0);
            }
            try {
                trans.setEditDate(new Date(aResultSet.getTimestamp("edit_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setEditDate(null);
            }
            try {
                trans.setPointsAwarded(aResultSet.getDouble("points_awarded"));
            } catch (NullPointerException npe) {
                trans.setPointsAwarded(0);
            }
            try {
                trans.setCardNumber(aResultSet.getString("card_number"));
            } catch (NullPointerException npe) {
                trans.setCardNumber("");
            }
            try {
                trans.setTotalStdVatableAmount(aResultSet.getDouble("total_std_vatable_amount"));
            } catch (NullPointerException npe) {
                trans.setTotalStdVatableAmount(0);
            }
            try {
                trans.setTotalZeroVatableAmount(aResultSet.getDouble("total_zero_vatable_amount"));
            } catch (NullPointerException npe) {
                trans.setTotalZeroVatableAmount(0);
            }
            try {
                trans.setTotalExemptVatableAmount(aResultSet.getDouble("total_exempt_vatable_amount"));
            } catch (NullPointerException npe) {
                trans.setTotalExemptVatableAmount(0);
            }
            try {
                trans.setVatPerc(aResultSet.getDouble("vat_perc"));
            } catch (NullPointerException npe) {
                trans.setVatPerc(0);
            }
            try {
                trans.setAmountTendered(aResultSet.getDouble("amount_tendered"));
            } catch (NullPointerException npe) {
                trans.setAmountTendered(0);
            }
            try {
                trans.setChangeAmount(aResultSet.getDouble("change_amount"));
            } catch (NullPointerException npe) {
                trans.setChangeAmount(0);
            }
            try {
                trans.setIsCashDiscountVatLiable(aResultSet.getString("is_cash_discount_vat_liable"));
            } catch (NullPointerException npe) {
                trans.setIsCashDiscountVatLiable("");
            }

            try {
                trans.setTotalProfitMargin(aResultSet.getDouble("total_profit_margin"));
            } catch (NullPointerException npe) {
                trans.setTotalProfitMargin(0);
            }

            try {
                trans.setTransactionUserDetailId(aResultSet.getInt("transaction_user_detail_id"));
            } catch (NullPointerException npe) {
                trans.setTransactionUserDetailId(0);
            }

            try {
                trans.setBillTransactorId(aResultSet.getLong("bill_transactor_id"));
            } catch (NullPointerException npe) {
                trans.setBillTransactorId(0);
            }

            try {
                trans.setSchemeTransactorId(aResultSet.getLong("scheme_transactor_id"));
            } catch (NullPointerException npe) {
                trans.setSchemeTransactorId(0);
            }

            try {
                trans.setPrincSchemeMember(aResultSet.getString("princ_scheme_member"));
            } catch (NullPointerException npe) {
                trans.setPrincSchemeMember("");
            }

            try {
                trans.setSchemeCardNumber(aResultSet.getString("scheme_card_number"));
            } catch (NullPointerException | SQLException npe) {
                trans.setSchemeCardNumber("");
            }

            try {
                trans.setTransactionNumber(aResultSet.getString("transaction_number"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTransactionNumber("");
            }
            try {
                trans.setDeliveryDate(new Date(aResultSet.getDate("delivery_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setDeliveryDate(null);
            }
            try {
                trans.setDeliveryAddress(aResultSet.getString("delivery_address"));
            } catch (NullPointerException | SQLException npe) {
                trans.setDeliveryAddress("");
            }
            try {
                trans.setPayTerms(aResultSet.getString("pay_terms"));
            } catch (NullPointerException | SQLException npe) {
                trans.setPayTerms("");
            }
            try {
                trans.setTermsConditions(aResultSet.getString("terms_conditions"));
            } catch (NullPointerException | SQLException npe) {
                trans.setTermsConditions("");
            }
            try {
                trans.setAuthorisedByUserDetailId(aResultSet.getInt("authorised_by_user_detail_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setAuthorisedByUserDetailId(0);
            }
            try {
                trans.setAuthoriseDate(new Date(aResultSet.getDate("authorise_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setAuthoriseDate(null);
            }
            try {
                trans.setPayDueDate(new Date(aResultSet.getDate("pay_due_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setPayDueDate(null);
            }
            try {
                trans.setExpiryDate(new Date(aResultSet.getDate("expiry_date").getTime()));
            } catch (NullPointerException | SQLException npe) {
                trans.setExpiryDate(null);
            }
            try {
                trans.setCurrencyCode(aResultSet.getString("currency_code"));
            } catch (NullPointerException | SQLException npe) {
                trans.setCurrencyCode("");
            }
            try {
                trans.setXrate(aResultSet.getDouble("xrate"));
            } catch (NullPointerException | SQLException npe) {
                trans.setXrate(1);
            }
            try {
                trans.setFrom_date(new Date(aResultSet.getDate("from_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setFrom_date(null);
            }
            try {
                trans.setTo_date(new Date(aResultSet.getDate("to_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setTo_date(null);
            }
            try {
                trans.setDuration_type(aResultSet.getString("duration_type"));
            } catch (NullPointerException npe) {
                trans.setDuration_type("");
            }
            try {
                trans.setSite_id(aResultSet.getLong("site_id"));
            } catch (NullPointerException npe) {
                trans.setSite_id(0);
            }
            try {
                trans.setTransactor_rep(aResultSet.getString("transactor_rep"));
            } catch (NullPointerException npe) {
                trans.setTransactor_rep("");
            }
            try {
                trans.setTransactor_vehicle(aResultSet.getString("transactor_vehicle"));
            } catch (NullPointerException npe) {
                trans.setTransactor_vehicle("");
            }
            try {
                trans.setTransactor_driver(aResultSet.getString("transactor_driver"));
            } catch (NullPointerException npe) {
                trans.setTransactor_driver("");
            }
            try {
                trans.setDuration_value(aResultSet.getDouble("duration_value"));
            } catch (NullPointerException npe) {
                trans.setDuration_value(0);
            }
            try {
                trans.setAccChildAccountId(aResultSet.getInt("acc_child_account_id"));
            } catch (NullPointerException | SQLException npe) {
                trans.setAccChildAccountId(0);
            }
            try {
                trans.setLocation_id(aResultSet.getLong("location_id"));
            } catch (NullPointerException npe) {
                trans.setLocation_id(0);
            }
            if (null != aResultSet.getString("status_code")) {
                trans.setStatus_code(aResultSet.getString("status_code"));
            } else {
                trans.setStatus_code("");
            }
            try {
                trans.setStatus_date(new Date(aResultSet.getTimestamp("status_date").getTime()));
            } catch (NullPointerException npe) {
                trans.setStatus_date(null);
            }
            if (null != aResultSet.getString("delivery_mode")) {
                trans.setDelivery_mode(aResultSet.getString("delivery_mode"));
            } else {
                trans.setDelivery_mode("");
            }
            try {
                trans.setIs_processed(aResultSet.getInt("is_processed"));
            } catch (NullPointerException npe) {
                trans.setIs_processed(0);
            }
            try {
                trans.setIs_paid(aResultSet.getInt("is_paid"));
            } catch (NullPointerException npe) {
                trans.setIs_paid(0);
            }
            try {
                trans.setIs_cancel(aResultSet.getInt("is_cancel"));
            } catch (NullPointerException npe) {
                trans.setIs_cancel(0);
            }
            //return trans;
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public TransSummary getTransSummaryFromResultset(ResultSet aResultSet) {
        try {
            TransSummary transSummary = new TransSummary();
            try {
                transSummary.setStoreId(aResultSet.getInt("store_id"));
            } catch (Exception e) {
                transSummary.setStoreId(0);
            }
            try {
                transSummary.setTransactionTypeId(aResultSet.getInt("transaction_type_id"));
            } catch (Exception e) {
                transSummary.setTransactionTypeId(0);
            }
            try {
                transSummary.setSumTotalProfitMargin(aResultSet.getDouble("sum_total_profit_margin"));
            } catch (Exception e) {
                transSummary.setSumTotalProfitMargin(0);
            }
            try {
                transSummary.setFieldName(aResultSet.getString("field_name"));
            } catch (Exception e) {
                transSummary.setFieldName("-");
            }
            try {
                transSummary.setSumTotalTradeDiscount(aResultSet.getDouble("sum_total_trade_discount"));
            } catch (NullPointerException npe) {
                transSummary.setSumTotalTradeDiscount(0);
            }
            try {
                transSummary.setSumTotalVat(aResultSet.getLong("sum_total_vat"));
            } catch (NullPointerException npe) {
                transSummary.setSumTotalVat(0);
            }
            try {
                transSummary.setSumCashDiscount(aResultSet.getDouble("sum_cash_discount"));
            } catch (NullPointerException npe) {
                transSummary.setSumCashDiscount(0);
            }
            try {
                transSummary.setSumGrandTotal(aResultSet.getDouble("sum_grand_total"));
            } catch (NullPointerException npe) {
                transSummary.setSumGrandTotal(0);
            }
            try {
                transSummary.setSumTotalStdVatableAmount(aResultSet.getDouble("sum_total_std_vatable_amount"));
            } catch (NullPointerException npe) {
                transSummary.setSumTotalStdVatableAmount(0);
            }
            try {
                transSummary.setSumTotalZeroVatableAmount(aResultSet.getDouble("sum_total_zero_vatable_amount"));
            } catch (NullPointerException npe) {
                transSummary.setSumTotalZeroVatableAmount(0);
            }
            try {
                transSummary.setSumTotalExemptVatableAmount(aResultSet.getDouble("sum_total_exempt_vatable_amount"));
            } catch (NullPointerException npe) {
                transSummary.setSumTotalExemptVatableAmount(0);
            }

            return transSummary;

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return null;
        }

    }

    public int updateTransCECOpenBalance(Trans aTrans) {
        int success = 0;
        String sql = "UPDATE transaction SET grand_total=?,edit_date=?,edit_user_detail_id=? WHERE transaction_id=?";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setDouble(1, aTrans.getGrandTotal());
            ps.setTimestamp(2, new java.sql.Timestamp(aTrans.getEditDate().getTime()));
            ps.setInt(3, aTrans.getEditUserDetailId());
            ps.setLong(4, aTrans.getTransactionId());
            //save
            ps.executeUpdate();
            success = 1;
        } catch (Exception e) {
            success = 0;
            LOGGER.log(Level.ERROR, e);
        }
        return success;
    }

    public void deleteTrans(Trans trans) {
        String sql = "DELETE FROM trans WHERE transaction_id=?";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, trans.getTransactionId());
            ps.executeUpdate();
            this.setActionMessage("Deleted Successfully!");
            this.clearTrans(trans);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            this.setActionMessage("Trans NOT deleted");
        }
    }

    public int deleteTransCEC(long aTransId) {
        int deleted = 0;
        String sql = "DELETE FROM transaction WHERE transaction_id=?";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, aTransId);
            ps.executeUpdate();
            deleted = 1;
        } catch (Exception e) {
            deleted = 0;
            LOGGER.log(Level.ERROR, e);
        }
        return deleted;
    }

    public void clearTrans(Trans trans) {
        if (null != trans) {
            trans.setTransactionId(0);
            trans.setTransactionHistId(0);
            trans.setTransaction_approval_id(0);
            try {
                trans.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
            } catch (NullPointerException npe) {
                trans.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
            }
            trans.setStoreId(0);
            trans.setStore2Id(0);
            trans.setTransactorId(0);
            //trans.setTransactionTypeId(0);//
            //trans.setTransactionReasonId(0);//
            trans.setCashDiscount(0);
            trans.setTotalVat(0);
            trans.setTransactionComment("");
            trans.setAddUserDetailId(0);
            //trans.setAddDate(null);//
            trans.setEditUserDetailId(0);
            //trans.setEditDate(null);//
            trans.setTransactionRef("");
            trans.setAmountTendered(0);
            trans.setChangeAmount(0);
            trans.setChangeAmount(0);
            trans.setTotalTradeDiscount(0);
            trans.setPointsAwarded(0);
            trans.setSpendPoints(0);
            trans.setSpendPointsAmount(0);
            trans.setBalancePoints(0);
            trans.setBalancePointsAmount(0);
            trans.setCardHolder("");
            trans.setCardNumber("");
            trans.setSubTotal(0);

            trans.setSubTotal(0);
            trans.setTotalTradeDiscount(0);
            trans.setTotalVat(0);
            trans.setGrandTotal(0);
            trans.setSpendPoints(0);

            trans.setTotalStdVatableAmount(0);
            trans.setTotalZeroVatableAmount(0);
            trans.setTotalExemptVatableAmount(0);
            trans.setVatPerc(0);
            //trans.setIsCashDiscountVatLiable("");
            trans.setApproveUserName("");
            trans.setApproveUserPassword("");
            //clear current trans and pay ids in session
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
            HttpSession httpSession = request.getSession(true);
            httpSession.setAttribute("APPROVE_USER_ID", 0);
            httpSession.setAttribute("APPROVE_DISCOUNT_STATUS", "");
            httpSession.setAttribute("APPROVE_POINTS_STATUS", "");
            //for profit margin
            trans.setTotalProfitMargin(0);

            trans.setTransactionUserDetailId(0);
            trans.setBillTransactorId(0);
            trans.setSchemeTransactorId(0);
            trans.setPrincSchemeMember("");
            trans.setSchemeCardNumber("");
            trans.setBillOther(false);
            trans.setDisplayLoyalty(false);

            trans.setTransactionNumber("");
            trans.setTransactionNumber2("");
            trans.setTransactionNumber3("");
            trans.setDeliveryDate(null);
            trans.setDeliveryAddress("");
            trans.setPayTerms("");
            trans.setTermsConditions("");
            trans.setAuthorisedByUserDetailId(0);
            trans.setAuthoriseDate(null);
            trans.setPayDueDate(null);
            trans.setExpiryDate(null);
            trans.setAccChildAccountId(0);
            trans.setCurrencyCode("");
            trans.setXrate(1);
            trans.setFrom_date(null);
            trans.setTo_date(null);
            trans.setDuration_type(new GeneralUserSetting().getDEFAULT_DURATION_TYPE());
            trans.setSite_id(0);
            trans.setTransactor_rep("");
            trans.setTransactor_vehicle("");
            trans.setTransactor_driver("");
            trans.setDuration_value(0);
            trans.setTotalDebit(0);
            trans.setTotalCredit(0);
            try {
                //trans.setPayMethod(new PayMethodBean().getPayMethodDefault().getPayMethodId());
                if (new Parameter_listBean().getParameter_listByContextNameMemory("PAY_METHOD", "FORCE_SELECTION").getParameter_value().equals("1")) {
                    trans.setPayMethod(0);
                } else {
                    trans.setPayMethod(new PayMethodBean().getPayMethodDefault().getPayMethodId());
                }
            } catch (NullPointerException npe) {
                trans.setPayMethod(0);
            }
            trans.setTotalPaid(0);
            trans.setCash_dicsount_perc(0);
            trans.setBalance_receivable(0);
            trans.setBalance_payable(0);
            trans.setDeposit_customer(0);
            trans.setDeposit_supplier(0);
            trans.setBalance_receivable2(0);
            trans.setBalance_payable2(0);
            trans.setDeposit_customer2(0);
            trans.setDeposit_supplier2(0);
            trans.setLocation_id(0);
            trans.setStatus_code("");
            trans.setStatus_date(null);
            trans.setDelivery_mode("");
            trans.setIs_processed(0);
            trans.setIs_paid(0);
            trans.setIs_cancel(0);
            trans.setUser_code("");
            trans.setLocation_name("");
            trans.setIs_selected(0);
            trans.setIs_invoiced(0);
            trans.setIs_delivered(0);
            trans.setSource_code("");
            //lookups
            trans.setTransactorName("");
            //init currency
            int TransTypeId = trans.getTransactionTypeId();
            if (TransTypeId == 0) {
                TransTypeId = new GeneralUserSetting().getCurrentTransactionTypeId();
            }
            if (TransTypeId > 0) {
                this.initCurrencyCode(TransTypeId, trans);
            }
            //init other trans type defaults
            if (TransTypeId > 0) {
                try {
                    TransactionType TransType = new TransactionTypeBean().getTransactionType(TransTypeId);
                    trans.setTermsConditions(TransType.getDefault_term_condition());
                } catch (Exception e) {
                    //do nothing
                }
            }
            //Account
            this.AccChildAccountList = new AccChildAccountBean().getAccChildAccountsForCashReceipt(trans.getCurrencyCode(), trans.getPayMethod(), new GeneralUserSetting().getCurrentStore().getStoreId(), new GeneralUserSetting().getCurrentUser().getUserDetailId());
            //Customer Display
            new UtilityBean().clearCustomerDisplay();
            //others
            trans.setShift_id(0);
            trans.setMode_code(0);
            trans.setTotalExciseDutyTaxAmount(0);
            trans.setTotalExciseDutableAmount(0);
        }
    }

    public void clearTransEdit(Trans trans) {
        trans.setTransactionId(0);
        trans.setTransactionHistId(0);
        trans.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
        trans.setStoreId(0);
        trans.setStore2Id(0);
        trans.setTransactorId(0);
        trans.setTransactionTypeId(0);//
        trans.setTransactionReasonId(0);//
        trans.setCashDiscount(0);
        trans.setTotalVat(0);
        trans.setTransactionComment("");
        trans.setAddUserDetailId(0);
        trans.setAddDate(null);//
        trans.setEditUserDetailId(0);
        trans.setEditDate(null);//
        trans.setTransactionRef("");
        trans.setAmountTendered(0);
        trans.setChangeAmount(0);
        trans.setChangeAmount(0);
        trans.setTotalTradeDiscount(0);
        trans.setPointsAwarded(0);
        trans.setSpendPoints(0);
        trans.setSpendPointsAmount(0);
        trans.setBalancePoints(0);
        trans.setBalancePointsAmount(0);
        trans.setCardHolder("");
        trans.setCardNumber("");
        trans.setSubTotal(0);

        trans.setSubTotal(0);
        trans.setTotalTradeDiscount(0);
        trans.setTotalVat(0);
        trans.setGrandTotal(0);
        trans.setSpendPoints(0);

        trans.setTotalStdVatableAmount(0);
        trans.setTotalZeroVatableAmount(0);
        trans.setTotalExemptVatableAmount(0);
        trans.setVatPerc(0);
        //trans.setIsCashDiscountVatLiable("");
        trans.setApproveUserName("");
        trans.setApproveUserPassword("");
        //clear current trans and pay ids in session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute("APPROVE_USER_ID", 0);
        httpSession.setAttribute("APPROVE_DISCOUNT_STATUS", "");
        httpSession.setAttribute("APPROVE_POINTS_STATUS", "");

        //for profit margin
        trans.setTotalProfitMargin(0);

        trans.setTransactionUserDetailId(0);
        trans.setBillTransactorId(0);
        trans.setSchemeTransactorId(0);
        trans.setPrincSchemeMember("");
        trans.setSchemeCardNumber("");
        trans.setTransactionNumber("");
        trans.setDeliveryDate(null);
        trans.setDeliveryAddress("");
        trans.setPayTerms("");
        trans.setTermsConditions("");
        trans.setAuthorisedByUserDetailId(0);
        trans.setAuthoriseDate(null);
        trans.setPayDueDate(null);
        trans.setExpiryDate(null);
        trans.setAccChildAccountId(0);
        trans.setCurrencyCode("");
        trans.setXrate(1);
        trans.setFrom_date(null);
        trans.setTo_date(null);
        trans.setDuration_type("");
        trans.setSite_id(0);
        trans.setTransactor_rep("");
        trans.setTransactor_vehicle("");
        trans.setTransactor_driver("");
        trans.setDuration_value(0);
        trans.setCash_dicsount_perc(0);
        trans.setBalance_receivable(0);
        trans.setBalance_payable(0);
        trans.setDeposit_customer(0);
        trans.setDeposit_supplier(0);
        trans.setBalance_receivable2(0);
        trans.setBalance_payable2(0);
        trans.setDeposit_customer2(0);
        trans.setDeposit_supplier2(0);
        trans.setLocation_id(0);
        trans.setStatus_code("");
        trans.setStatus_date(null);
        trans.setDelivery_mode("");
        trans.setIs_processed(0);
        trans.setIs_paid(0);
        trans.setIs_cancel(0);
        trans.setUser_code("");
        trans.setLocation_name("");
        trans.setIs_selected(0);
        trans.setIs_invoiced(0);
        trans.setIs_delivered(0);
        trans.setSource_code("");
        //lookups
        trans.setTransactorName("");
    }

    public void clearAll(Trans t, List<TransItem> aActiveTransItems, TransItem ti, Item aSelectedItem, Transactor aSelectedTransactor, int ClearNo, AccCoa aSelectedAccCoa) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all  
        TransItemBean tib = new TransItemBean();
        ItemBean itmB = new ItemBean();
        TransactorBean trB = new TransactorBean();
        AccCoaBean acBean = new AccCoaBean();

        if (ClearNo == 1 || ClearNo == 2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all
            //clear autoCompletetd item
            itmB.clearSelectedItem();
            itmB.clearItem(aSelectedItem);
            //clear the selcted trans item
            tib.clearTransItem(ti);
            //clear selected AccCoa
            acBean.clearAccCoa(aSelectedAccCoa);
        }
        if (ClearNo == 2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all
            //put code for clearing customer/supplier/transactor
            trB.clearSelectedTransactor();
            trB.clearTransactor(aSelectedTransactor);
            //clear all the item LIST
            //--//tib.getActiveTransItems().clear();
            aActiveTransItems.clear();

            //clear Trans inc. payments
            this.clearTrans(t);
        }
    }

    public void clearAll2CallQuickOder(Trans t, List<TransItem> aActiveTransItems, TransItem ti, Item aSelectedItem, Transactor aSelectedTransactor, int ClearNo, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa, StatusBean aStatusBean) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all  
        //reset messages
        this.ActionMessage = "";
        aStatusBean.setItemAddedStatus("");
        aStatusBean.setShowItemAddedStatus(0);
        aStatusBean.setItemNotAddedStatus("");
        aStatusBean.setShowItemNotAddedStatus(0);

        //can first do something here...
        String CurrentDelivery_mode = t.getDelivery_mode();
        long CurrentLocation_id = t.getLocation_id();
        int CurrentStore2Id = t.getStore2Id();
        //clear
        this.clearAll2(t, aActiveTransItems, ti, aSelectedItem, aSelectedTransactor, ClearNo, aSelectedBillTransactor, aTransUserDetail, aSelectedSchemeTransactor, aAuthorisedByUserDetail, aSelectedAccCoa);
        //ensure previous selection is put back to trans
        t.setDelivery_mode(CurrentDelivery_mode);
        t.setLocation_id(CurrentLocation_id);
        t.setStore2Id(CurrentStore2Id);
    }

    public void clearAll2(Trans t, List<TransItem> aActiveTransItems, TransItem ti, Item aSelectedItem, Transactor aSelectedTransactor, int ClearNo, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all  
        TransItemBean tib = new TransItemBean();
        ItemBean itmB = new ItemBean();
        TransactorBean trB = new TransactorBean();
        AccCoaBean acBean = new AccCoaBean();

        if (ClearNo == 1 || ClearNo == 2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all
            //clear autoCompletetd item
            itmB.clearSelectedItem();
            itmB.clearItem(aSelectedItem);
            //clear the selcted trans item
            tib.clearTransItem(ti);
            //clear selected AccCoa
            acBean.clearAccCoa(aSelectedAccCoa);
        }
        if (ClearNo == 2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all
            trB.clearTransactor(aSelectedTransactor);
            //code for clearing BILL customer/supplier/transactor
            //trB.clearSelectedBillTransactor();
            trB.clearTransactor(aSelectedBillTransactor);
            trB.clearTransactor(aSelectedSchemeTransactor);
            //clear all the item LIST
            //--//tib.getActiveTransItems().clear();
            aActiveTransItems.clear();

            //clear Trans inc. payments
            this.clearTrans(t);

            //clear TransUser / Service Offered by
            new UserDetailBean().clearUserDetail(aTransUserDetail);

            //clear Authorised By UserDetail
            new UserDetailBean().clearUserDetail(aAuthorisedByUserDetail);
        }
    }

    public void clearAll3(Trans t, List<TransItem> aActiveTransItems, TransItem ti, Item aSelectedItem, Transactor aSelectedTransactor, int ClearNo, AccCoa aSelectedAccCoa, AccCoa aSelectedAccCoa2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all  
        TransItemBean tib = new TransItemBean();
        ItemBean itmB = new ItemBean();
        TransactorBean trB = new TransactorBean();
        AccCoaBean acBean = new AccCoaBean();

        if (ClearNo == 1 || ClearNo == 2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all
            //clear autoCompletetd item
            itmB.clearSelectedItem();
            itmB.clearItem(aSelectedItem);
            //clear the selcted trans item
            tib.clearTransItem(ti);
            //clear selected AccCoa
            acBean.clearAccCoa(aSelectedAccCoa);
            acBean.clearAccCoa(aSelectedAccCoa2);
        }
        if (ClearNo == 2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all
            //put code for clearing customer/supplier/transactor
            trB.clearSelectedTransactor();
            trB.clearTransactor(aSelectedTransactor);
            //clear all the item LIST
            //--//tib.getActiveTransItems().clear();
            aActiveTransItems.clear();

            //clear Trans inc. payments
            this.clearTrans(t);
        }
    }

    public void initClearAll(Trans t, List<TransItem> aActiveTransItems, TransItem ti, Item aSelectedItem, Transactor aSelectedTransactor, int ClearNo, AccCoa aSelectedAccCoa) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all  
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            TransItemBean tib = new TransItemBean();
            ItemBean itmB = new ItemBean();
            TransactorBean trB = new TransactorBean();
            AccCoaBean acBean = new AccCoaBean();

            if (ClearNo == 1 || ClearNo == 2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all
                //clear autoCompletetd item
                itmB.clearSelectedItem();
                itmB.clearItem(aSelectedItem);
                //clear the selcted trans item
                tib.clearTransItem(ti);
                //clear selected AccCoa
                acBean.clearAccCoa(aSelectedAccCoa);
            }
            if (ClearNo == 2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all
                //code for clearing customer/supplier/transactor
                trB.clearSelectedTransactor();
                trB.clearTransactor(aSelectedTransactor);
                //clear all the item LIST
                //--//tib.getActiveTransItems().clear();
                aActiveTransItems.clear();

                //clear Trans
                this.clearTrans(t);

                //clear action message
                this.ActionMessage = "";

                //clear current trans and pay ids in session
                FacesContext context = FacesContext.getCurrentInstance();
                HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
                HttpSession httpSession = request.getSession(true);
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                httpSession.setAttribute("CURRENT_PAY_ID", 0);
            }
        }
    }

    public void initClearAll2(Trans t, List<TransItem> aActiveTransItems, TransItem ti, Item aSelectedItem, Transactor aSelectedTransactor, int ClearNo, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all  
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            TransItemBean tib = new TransItemBean();
            ItemBean itmB = new ItemBean();
            TransactorBean trB = new TransactorBean();

            if (ClearNo == 1 || ClearNo == 2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all
                //clear autoCompletetd item
                itmB.clearSelectedItem();
                itmB.clearItem(aSelectedItem);
                //clear the selcted trans item
                tib.clearTransItem(ti);
            }
            if (ClearNo == 2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all
                //code for clearing customer/supplier/transactor
                trB.clearSelectedTransactor();
                trB.clearTransactor(aSelectedTransactor);
                //code for clearing customer/supplier/transactor
                //trB.clearSelectedBillTransactor();
                trB.clearTransactor(aSelectedBillTransactor);
                trB.clearTransactor(aSelectedSchemeTransactor);
                //clear all the item LIST
                //--//tib.getActiveTransItems().clear();
                aActiveTransItems.clear();

                //clear Trans
                this.clearTrans(t);

                //clear transaction user / service offered by
                new UserDetailBean().clearUserDetail(aTransUserDetail);

                //clear action message
                this.ActionMessage = "";

                //clear current trans and pay ids in session
                FacesContext context = FacesContext.getCurrentInstance();
                HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
                HttpSession httpSession = request.getSession(true);
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                httpSession.setAttribute("CURRENT_PAY_ID", 0);

            }
        }
    }

    public void initClearAllCEC(String aLevel, Trans t, List<TransItem> aActiveTransItems, TransItem ti, Item aSelectedItem, Transactor aSelectedTransactor, int ClearNo, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all  
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            TransItemBean tib = new TransItemBean();
            ItemBean itmB = new ItemBean();
            TransactorBean trB = new TransactorBean();

            if (ClearNo == 1 || ClearNo == 2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all
                //clear autoCompletetd item
                itmB.clearSelectedItem();
                itmB.clearItem(aSelectedItem);
                //clear the selcted trans item
                tib.clearTransItem(ti);
            }
            if (ClearNo == 2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all
                //code for clearing customer/supplier/transactor
                trB.clearSelectedTransactor();
                trB.clearTransactor(aSelectedTransactor);
                //code for clearing customer/supplier/transactor
                //trB.clearSelectedBillTransactor();
                trB.clearTransactor(aSelectedBillTransactor);
                trB.clearTransactor(aSelectedSchemeTransactor);
                //clear all the item LIST
                //--//tib.getActiveTransItems().clear();
                aActiveTransItems.clear();

                //clear Trans
                this.clearTrans(t);

                //clear transaction user / service offered by
                new UserDetailBean().clearUserDetail(aTransUserDetail);

                //clear current trans and pay ids in session
                FacesContext context = FacesContext.getCurrentInstance();
                HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
                HttpSession httpSession = request.getSession(false);
                if (aLevel.equals("PARENT")) {
                    httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                    httpSession.setAttribute("CURRENT_PAY_ID", 0);
                    //clear action message
                    this.ActionMessage = "";
                } else if (aLevel.equals("CHILD")) {
                    httpSession.setAttribute("CURRENT_TRANSACTION_ID_CHILD", 0);
                    httpSession.setAttribute("CURRENT_PAY_ID_CHILD", 0);
                    //clear action message
                    this.ActionMessageChild = "";
                }
            }
            //new OutputDetailBean().refreshOutput(aLevel, "");
            new OutputDetailBean().clearOutput(aLevel, "");
        }
    }

    public void copyTransObject(Trans aTransFrom, Trans aTransTo) {
        aTransTo.setTransactionId(aTransFrom.getTransactionId());
        aTransTo.setTransactionHistId(aTransFrom.getTransactionHistId());
        aTransTo.setTransactionDate(aTransFrom.getTransactionDate());
        aTransTo.setStoreId(aTransFrom.getStoreId());
        aTransTo.setStore2Id(aTransFrom.getStore2Id());
        aTransTo.setTransactorId(aTransFrom.getTransactorId());
        aTransTo.setTransactionTypeId(aTransFrom.getTransactionTypeId());
        aTransTo.setTransactionReasonId(aTransFrom.getTransactionReasonId());
        aTransTo.setCashDiscount(aTransFrom.getCashDiscount());
        aTransTo.setTotalVat(aTransFrom.getTotalVat());
        aTransTo.setTransactionComment(aTransFrom.getTransactionComment());
        aTransTo.setAddUserDetailId(aTransFrom.getAddUserDetailId());
        aTransTo.setAddDate(aTransFrom.getAddDate());
        aTransTo.setEditUserDetailId(aTransFrom.getEditUserDetailId());
        aTransTo.setEditDate(aTransFrom.getEditDate());
        aTransTo.setTransactionRef(aTransFrom.getTransactionRef());
        aTransTo.setAmountTendered(aTransFrom.getAmountTendered());
        aTransTo.setChangeAmount(aTransFrom.getChangeAmount());
        aTransTo.setChangeAmount(aTransFrom.getChangeAmount());
        aTransTo.setTotalTradeDiscount(aTransFrom.getTotalTradeDiscount());
        aTransTo.setPointsAwarded(aTransFrom.getPointsAwarded());
        aTransTo.setSpendPoints(aTransFrom.getSpendPoints());
        aTransTo.setSpendPointsAmount(aTransFrom.getSpendPointsAmount());
        aTransTo.setBalancePoints(aTransFrom.getBalancePoints());
        aTransTo.setBalancePointsAmount(aTransFrom.getBalancePointsAmount());
        aTransTo.setCardHolder(aTransFrom.getCardHolder());
        aTransTo.setCardNumber(aTransFrom.getCardNumber());
        aTransTo.setSubTotal(aTransFrom.getSubTotal());
        aTransTo.setTotalTradeDiscount(aTransFrom.getTotalTradeDiscount());
        aTransTo.setTotalVat(aTransFrom.getTotalVat());
        aTransTo.setGrandTotal(aTransFrom.getGrandTotal());
        aTransTo.setSpendPoints(aTransFrom.getSpendPoints());
        aTransTo.setTotalStdVatableAmount(aTransFrom.getTotalStdVatableAmount());
        aTransTo.setTotalZeroVatableAmount(aTransFrom.getTotalZeroVatableAmount());
        aTransTo.setTotalExemptVatableAmount(aTransFrom.getTotalExemptVatableAmount());
        aTransTo.setVatPerc(aTransFrom.getVatPerc());
        aTransTo.setApproveUserName(aTransFrom.getApproveUserName());
        aTransTo.setApproveUserPassword(aTransFrom.getApproveUserPassword());
        aTransTo.setTotalProfitMargin(aTransFrom.getTotalProfitMargin());
        aTransTo.setTransactionUserDetailId(aTransFrom.getTransactionUserDetailId());
        aTransTo.setBillTransactorId(aTransFrom.getBillTransactorId());
        aTransTo.setSchemeTransactorId(aTransFrom.getSchemeTransactorId());
        aTransTo.setPrincSchemeMember(aTransFrom.getPrincSchemeMember());
        aTransTo.setSchemeCardNumber(aTransFrom.getSchemeCardNumber());
        aTransTo.setTransactionNumber(aTransFrom.getTransactionNumber());
        aTransTo.setDeliveryDate(aTransFrom.getDeliveryDate());
        aTransTo.setDeliveryAddress(aTransFrom.getDeliveryAddress());
        aTransTo.setPayTerms(aTransFrom.getPayTerms());
        aTransTo.setTermsConditions(aTransFrom.getTermsConditions());
        aTransTo.setAuthorisedByUserDetailId(aTransFrom.getAuthorisedByUserDetailId());
        aTransTo.setAuthoriseDate(aTransFrom.getAuthoriseDate());
        aTransTo.setPayDueDate(aTransFrom.getPayDueDate());
        aTransTo.setExpiryDate(aTransFrom.getExpiryDate());
        aTransTo.setAccChildAccountId(aTransFrom.getAccChildAccountId());
        aTransTo.setCurrencyCode(aTransFrom.getCurrencyCode());
        aTransTo.setXrate(aTransFrom.getXrate());
        aTransTo.setFrom_date(aTransFrom.getFrom_date());
        aTransTo.setTo_date(aTransFrom.getTo_date());
        aTransTo.setDuration_type(aTransFrom.getDuration_type());
        aTransTo.setSite_id(aTransFrom.getSite_id());
        aTransTo.setTransactor_rep(aTransFrom.getTransactor_rep());
        aTransTo.setTransactor_vehicle(aTransFrom.getTransactor_vehicle());
        aTransTo.setTransactor_driver(aTransFrom.getTransactor_driver());
        aTransTo.setDuration_value(aTransFrom.getDuration_value());
        aTransTo.setCash_dicsount_perc(aTransFrom.getCash_dicsount_perc());
        aTransTo.setBalance_receivable(aTransFrom.getBalance_receivable());
        aTransTo.setBalance_payable(aTransFrom.getBalance_payable());
        aTransTo.setDeposit_customer(aTransFrom.getDeposit_customer());
        aTransTo.setDeposit_supplier(aTransFrom.getDeposit_supplier());
        aTransTo.setLocation_id(aTransFrom.getLocation_id());
        aTransTo.setStatus_code(aTransFrom.getStatus_code());
        aTransTo.setStatus_date(aTransFrom.getStatus_date());
        aTransTo.setDelivery_mode(aTransFrom.getDelivery_mode());
        aTransTo.setIs_processed(aTransFrom.getIs_processed());
        aTransTo.setIs_paid(aTransFrom.getIs_paid());
        aTransTo.setIs_cancel(aTransFrom.getIs_cancel());
        aTransTo.setUser_code(aTransFrom.getUser_code());
        aTransTo.setLocation_name(aTransFrom.getLocation_name());
        aTransTo.setIs_selected(aTransFrom.getIs_selected());
        aTransTo.setIs_invoiced(aTransFrom.getIs_invoiced());
        aTransTo.setIs_delivered(aTransFrom.getIs_delivered());
        aTransTo.setSource_code(aTransFrom.getSource_code());
    }

    public void initOrderForEdit(Trans aTrans, List<TransItem> aActiveTransItems) {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            Trans TransForEdit = new GeneralUserSetting().getORDER_FOR_EDIT();
            if (null != TransForEdit) {
                if (TransForEdit.getTransactionId() > 0) {
                    this.copyTransObject(TransForEdit, aTrans);
                    this.updateLookup(aTrans);
                    aActiveTransItems.clear();
                    new TransItemBean().getTransItemsByTransactionId(aTrans.getTransactionId()).stream().forEach((ti) -> {
                        aActiveTransItems.add(ti);
                    });
                }
            }
        }
    }

    public void initRefreshDraft(int aStoreId, int aUserDetailId, int aTransTypeId, int aTransreasonId, Trans t, List<TransItem> aActiveTransItems, TransItem ti, Item aSelectedItem, Transactor aSelectedTransactor, int ClearNo, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all  
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            this.refreshTranssDraft(aStoreId, aUserDetailId, aTransTypeId, aTransreasonId);
        }
    }

    public void initClearAllEdit(Trans t, List<TransItem> aActiveTransItems, TransItem ti, Item aSelectedItem, Transactor aSelectedTransactor, int ClearNo) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all  
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            TransItemBean tib = new TransItemBean();
            ItemBean itmB = new ItemBean();
            TransactorBean trB = new TransactorBean();

            if (ClearNo == 1 || ClearNo == 2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all
                //clear autoCompletetd item
                itmB.clearSelectedItem();
                itmB.clearItem(aSelectedItem);
                //clear the selcted trans item
                tib.clearTransItem(ti);
            }
            if (ClearNo == 2) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all
                //put code for clearing customer/supplier/transactor
                trB.clearSelectedTransactor();
                trB.clearTransactor(aSelectedTransactor);
                //clear all the item LIST
                //--//tib.getActiveTransItems().clear();
                aActiveTransItems.clear();

                //clear Trans
                this.clearTransEdit(t);

                //clear action message
                this.ActionMessage = "";

                //clear current trans and pay ids in session
                FacesContext context = FacesContext.getCurrentInstance();
                HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
                HttpSession httpSession = request.getSession(true);
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                httpSession.setAttribute("CURRENT_PAY_ID", 0);

            }
        }
    }

    public void initClearTransReport(Trans trans) {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            //clear Trans
            trans.setTransactionId(0);
            trans.setTransactionDate(null);
            trans.setTransactionDate2(null);
            trans.setStoreId(0);
            trans.setStore2Id(0);
            trans.setTransactorId(0);
            trans.setAddUserDetailId(0);
            trans.setAddDate(null);
            trans.setAddDate2(null);
            trans.setEditUserDetailId(0);
            trans.setEditDate(null);
            trans.setEditDate2(null);
            //clear action message
            this.ActionMessage = "";
            trans.setTransactionUserDetailId(0);
            trans.setBillTransactorId(0);
            trans.setSchemeTransactorId(0);
            trans.setPrincSchemeMember("");
            trans.setSchemeCardNumber("");
        }
    }

    public void initClearCustomerCard() {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            //clear
            try {
                this.CustomerCardTranss.clear();
                this.CustomerCardTotals.clear();
                this.GrandTotalLoc = 0;
                this.GrandTotalPaidLoc = 0;
                this.GrandTotalBalanceLoc = 0;
            } catch (Exception e) {
                System.err.println("initClearCustomerCard:" + e.getMessage());
            }
        }
    }

    public void initClearSupplierCard() {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            //clear
            try {
                this.SupplierCardTranss.clear();
                this.SupplierCardTotals.clear();
                this.GrandTotalLoc = 0;
                this.GrandTotalPaidLoc = 0;
                this.GrandTotalBalanceLoc = 0;
            } catch (Exception e) {
                System.err.println("initClearSupplierCard:" + e.getMessage());
            }
        }
    }

    public void initCurrencyCode(int aTransTypeId, Trans trans) {
        try {
            if (new TransactionTypeBean().checkTransactionTypeNeedsCurrency(aTransTypeId) == 0) {
                trans.setCurrencyCode("");
            } else {
                TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
                String DefaultCurrencyCode = "";
                String TransTypeCurrencyCode = "";
                String LocalCurrencyCode = "";

                try {
                    LocalCurrencyCode = new AccCurrencyBean().getLocalCurrency().getCurrencyCode();
                    if (null == LocalCurrencyCode) {
                        LocalCurrencyCode = "";
                    }
                } catch (NullPointerException npe) {
                    LocalCurrencyCode = "";
                }
                try {
                    DefaultCurrencyCode = new GeneralUserSetting().getDEFAULT_CURRENCY_CODE();
                    if (null == DefaultCurrencyCode) {
                        DefaultCurrencyCode = "";
                    }
                } catch (NullPointerException npe) {
                    DefaultCurrencyCode = "";
                }
                try {
                    TransTypeCurrencyCode = transtype.getDefault_currency_code();
                    if (null == TransTypeCurrencyCode) {
                        TransTypeCurrencyCode = "";
                    }
                } catch (NullPointerException npe) {
                    TransTypeCurrencyCode = "";
                }

                if (TransTypeCurrencyCode.length() > 0) {
                    trans.setCurrencyCode(TransTypeCurrencyCode);
                } else if (DefaultCurrencyCode.length() > 0) {
                    trans.setCurrencyCode(DefaultCurrencyCode);
                } else {
                    trans.setCurrencyCode(LocalCurrencyCode);
                }
            }
        } catch (NullPointerException npe) {
            trans.setCurrencyCode("");
        }
    }

    public void initTransOpenBalance(int aTransTypeId, int aTransReasonId, Trans aTrans, TransItem aTransItem) {
        try {
            //1. currency
            TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
            String DefaultCurrencyCode = "";
            String TransTypeCurrencyCode = "";
            String LocalCurrencyCode = "";

            try {
                LocalCurrencyCode = new AccCurrencyBean().getLocalCurrency().getCurrencyCode();
                if (null == LocalCurrencyCode) {
                    LocalCurrencyCode = "";
                }
            } catch (NullPointerException npe) {
                LocalCurrencyCode = "";
            }
            try {
                DefaultCurrencyCode = new GeneralUserSetting().getDEFAULT_CURRENCY_CODE();
                if (null == DefaultCurrencyCode) {
                    DefaultCurrencyCode = "";
                }
            } catch (NullPointerException npe) {
                DefaultCurrencyCode = "";
            }
            try {
                TransTypeCurrencyCode = transtype.getDefault_currency_code();
                if (null == TransTypeCurrencyCode) {
                    TransTypeCurrencyCode = "";
                }
            } catch (NullPointerException npe) {
                TransTypeCurrencyCode = "";
            }
            if (TransTypeCurrencyCode.length() > 0) {
                aTrans.setCurrencyCode(TransTypeCurrencyCode);
            } else if (DefaultCurrencyCode.length() > 0) {
                aTrans.setCurrencyCode(DefaultCurrencyCode);
            } else {
                aTrans.setCurrencyCode(LocalCurrencyCode);
            }
            //2. account code
            if (aTransReasonId == 117) {//customer
                aTransItem.setAccountCode("1-00-010-010");
                aTransItem.setAccountName("Accounts Receivable Trade");
            } else if (aTransReasonId == 118) {//supplier
                aTransItem.setAccountCode("2-00-000-010");
                aTransItem.setAccountName("Accounts Payable Trade");
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void initTransType(int aTransTypeId, int aTransReasonId) {
        try {
            this.TransTypeObj = new TransactionTypeBean().getTransactionType(aTransTypeId);
            this.TransReasonObj = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void initTransTypeRef(int aTransTypeId, int aTransReasonId) {
        try {
            this.TransTypeRefObj = new TransactionTypeBean().getTransactionType(aTransTypeId);
            this.TransReasonRefObj = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    /**
     * @return the trass
     */
    public List<Trans> getTranss() {
        String sql;
        if (this.SearchTrans.length() > 0) {
            sql = "SELECT * FROM trans WHERE transaction_id=" + this.SearchTrans + " ORDER BY transaction_id DESC LIMIT 5";
        } else {
            sql = "SELECT * FROM trans ORDER BY transaction_id DESC LIMIT 5";
        }
        ResultSet rs = null;
        Transs = new ArrayList<Trans>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                Trans trans = new Trans();
                trans.setTransactionId(rs.getLong("transaction_id"));
                trans.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                trans.setStoreId(rs.getInt("store_id"));
                trans.setStore2Id(rs.getInt("store2_id"));
                trans.setTransactorId(rs.getLong("transactor_id"));
                trans.setTransactionTypeId(rs.getInt("transaction_type_id"));
                trans.setTransactionReasonId(rs.getInt("transaction_reason_id"));
                trans.setSubTotal(rs.getDouble("sub_total"));
                trans.setSubTotal(rs.getDouble("sub_total"));
                trans.setCashDiscount(rs.getDouble("cash_discount"));
                trans.setTotalVat(rs.getDouble("total_vat"));
                trans.setTotalVat(rs.getDouble("total_vat"));
                //trans.setGrandTotal(rs.getDouble("grand_total"));
                trans.setGrandTotal(rs.getDouble("grand_total"));
                trans.setTransactionComment(rs.getString("transaction_comment"));
                trans.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                trans.setAddDate(new Date(rs.getTimestamp("add_date").getTime()));
                trans.setEditUserDetailId(rs.getInt("edit_user_detail_id"));
                trans.setEditDate(new Date(rs.getTimestamp("edit_date").getTime()));
                trans.setTransactionRef(rs.getString("transaction_ref"));
                trans.setTotalTradeDiscount(rs.getDouble("total_trade_discount"));
                trans.setTotalTradeDiscount(rs.getDouble("total_trade_discount"));
                trans.setPointsAwarded(rs.getDouble("points_awarded"));
                trans.setPointsAwarded(rs.getDouble("points_awarded"));
                trans.setCardNumber(rs.getString("card_number"));
                trans.setTotalStdVatableAmount(rs.getDouble("total_std_vatable_amount"));
                trans.setTotalZeroVatableAmount(rs.getDouble("total_zero_vatable_amount"));
                trans.setTotalExemptVatableAmount(rs.getDouble("total_exempt_vatable_amount"));
                trans.setVatPerc(rs.getDouble("vat_perc"));
                trans.setAmountTendered(rs.getDouble("amount_tendered"));
                trans.setChangeAmount(rs.getDouble("change_amount"));
                trans.setIsCashDiscountVatLiable(rs.getString("is_cash_discount_vat_liable"));
                trans.setTotalProfitMargin(rs.getDouble("total_profit_margin"));
                try {
                    trans.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                } catch (NullPointerException npe) {
                    trans.setTransactionUserDetailId(0);
                }
                try {
                    trans.setBillTransactorId(rs.getLong("bill_transactor_id"));
                } catch (NullPointerException npe) {
                    trans.setBillTransactorId(0);
                }
                try {
                    trans.setSchemeTransactorId(rs.getLong("scheme_transactor_id"));
                } catch (NullPointerException npe) {
                    trans.setSchemeTransactorId(0);
                }
                try {
                    trans.setPrincSchemeMember(rs.getString("princ_scheme_member"));
                } catch (NullPointerException npe) {
                    trans.setPrincSchemeMember("");
                }

                try {
                    trans.setSchemeCardNumber(rs.getString("scheme_card_number"));
                } catch (NullPointerException | SQLException npe) {
                    trans.setSchemeCardNumber("");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return Transs;
    }

    public List<Trans> getTranss(long TransactorId) {
        String sql;
        sql = "{call sp_search_transaction_by_transactor_id(?)}";
        ResultSet rs = null;
        Transs = new ArrayList<Trans>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, TransactorId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Transs.add(this.getTransFromResultset(rs));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return Transs;
    }

    public void refreshTranssDraft(int aStoreId, int aAddUserDetailId, int aTransTypeId, int aTransReasonId) {
        try {
            this.TranssDraft.clear();
        } catch (NullPointerException npe) {
            this.TranssDraft = new ArrayList<>();
        }
        String sql;
        sql = "{call sp_search_transaction_hist_draft_by_user_type(?,?,?,?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, aStoreId);
            ps.setInt(2, aAddUserDetailId);
            ps.setInt(3, aTransTypeId);
            ps.setInt(4, aTransReasonId);
            rs = ps.executeQuery();
            while (rs.next()) {
                this.TranssDraft.add(this.getTransHistFromResultset(rs));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public ArrayList<Trans> getTranssDraft(int aStoreId, int aAddUserDetailId, int aTransTypeId, int aTransReasonId) {
        String sql;
        sql = "{call sp_search_transaction_hist_draft_by_user_type(?,?,?,?)}";
        ResultSet rs = null;
        ArrayList<Trans> tds = new ArrayList<>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, aStoreId);
            ps.setInt(2, aAddUserDetailId);
            ps.setInt(3, aTransTypeId);
            ps.setInt(4, aTransReasonId);
            rs = ps.executeQuery();
            while (rs.next()) {
                tds.add(this.getTransHistFromResultset(rs));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        }
        return tds;
    }

    public List<Trans> getTranssByTrTcTt(long aTransactionId, long aTransactorId, String aTransactorType) {
        List<Trans> aTranss = new ArrayList<Trans>();
        if (aTransactorId != 0) {
            aTranss = this.getTranssByTransactorTransType(aTransactorId, aTransactorType);
        } else if (aTransactionId != 0) {
            Trans aTrans = new Trans();
            try {
                if (aTransactorType.equals("CUSTOMER") || aTransactorType.equals("SCHEME")) {
                    aTrans = new TransBean().getTransByIdType(aTransactionId, 2);//SALE
                } else if (aTransactorType.equals("SUPPLIER")) {
                    aTrans = new TransBean().getTransByIdType(aTransactionId, 1);//PURCHASE
                }
            } catch (NullPointerException npe) {
                aTrans = null;
            }
            aTranss.add(aTrans);
        }
        return aTranss;
    }

    public List<Trans> getTranssByBillTrTcTt(long aTransactionId, long aBillTransactorId, String aTransactorType) {
        List<Trans> aTranss = new ArrayList<Trans>();
        if (aBillTransactorId != 0) {
            aTranss = this.getTranssByBillTransactorTransType(aBillTransactorId, aTransactorType);
        } else if (aTransactionId != 0) {
            Trans aTrans = new Trans();
            try {
                if (aTransactorType.equals("CUSTOMER") || aTransactorType.equals("SCHEME")) {
                    aTrans = new TransBean().getTransByIdType(aTransactionId, 2);//SALE
                } else if (aTransactorType.equals("SUPPLIER")) {
                    aTrans = new TransBean().getTransByIdType(aTransactionId, 1);//PURCHASE
                }
            } catch (NullPointerException npe) {
                aTrans = null;
            }
            aTranss.add(aTrans);
        }
        return aTranss;
    }

    public List<Trans> getTranssByBillTrPayCat(long aTransactionId, long aBillTransactorId, String aPayCategory) {
        List<Trans> aTranss = new ArrayList<Trans>();
        if (aBillTransactorId != 0) {
            if (aPayCategory.equals("IN")) {//for IN(CUSTOMER, SCHEME)
                aTranss = this.getTranssByTransactorTransactionType(aBillTransactorId, 2);//SALE INVOICE
            } else if (aPayCategory.equals("OUT")) {
                aTranss = this.getTranssByTransactorTransactionType(aBillTransactorId, 1);//for IN(PURCHASE INVOICE)
            } else {
                aTranss = this.getTranssByTransactorTransactionType(aBillTransactorId, 33);//for INVALID
            }
        } else if (aTransactionId != 0) {
            Trans aTrans = new Trans();
            try {
                if (aPayCategory.equals("IN")) {//for IN(CUSTOMER, SCHEME)
                    aTrans = new TransBean().getTransByIdType(aTransactionId, 2);//SALE INVOICE
                } else if (aPayCategory.equals("OUT")) {
                    aTrans = new TransBean().getTransByIdType(aTransactionId, 1);//for IN(PURCHASE INVOICE)
                }
            } catch (NullPointerException npe) {
                aTrans = null;
            }
            aTranss.add(aTrans);
        }
        return aTranss;
    }

    public List<Trans> getTranssByBillTrPayCatTransNo(String aTransactionNumber, long aBillTransactorId, String aPayCategory) {
        List<Trans> aTranss = new ArrayList<Trans>();
        if (aBillTransactorId != 0) {
            if (aPayCategory.equals("IN")) {//for IN(CUSTOMER, SCHEME)
                aTranss = this.getTranssByTransactorTransactionType(aBillTransactorId, 2);//SALE INVOICE
            } else if (aPayCategory.equals("OUT")) {
                aTranss = this.getTranssByTransactorTransactionType(aBillTransactorId, 1);//for IN(PURCHASE INVOICE)
            } else {
                aTranss = this.getTranssByTransactorTransactionType(aBillTransactorId, 33);//for INVALID
            }
        } else if (aTransactionNumber.length() > 0) {
            Trans aTrans = new Trans();
            try {
                if (aPayCategory.equals("IN")) {//for IN(CUSTOMER, SCHEME)
                    aTrans = new TransBean().getTransByNumberType(aTransactionNumber, 2);//SALE INVOICE
                } else if (aPayCategory.equals("OUT")) {
                    aTrans = new TransBean().getTransByNumberType(aTransactionNumber, 1);//for IN(PURCHASE INVOICE)
                }
            } catch (NullPointerException npe) {
                aTrans = null;
            }
            aTranss.add(aTrans);
        }
        this.setTransactorTranss(aTranss);
        return aTranss;
    }

    public List<Trans> getTranssByTransactorTransType(long aTransactorId, String aTransactorType) {
        String sql;
        sql = "{call sp_search_transaction_by_transactor_transtype(?,?)}";
        ResultSet rs = null;
        Transs = new ArrayList<Trans>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, aTransactorId);
            if (aTransactorType.equals("SUPPLIER")) {
                ps.setInt(2, 1);
            } else if (aTransactorType.equals("CUSTOMER") || aTransactorType.equals("SCHEME")) {
                ps.setInt(2, 2);
            } else {
                ps.setInt(2, 33);//invalid one
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                Transs.add(this.getTransFromResultset(rs));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return Transs;
    }

    public List<Trans> getTranssByTransactorTransactionType(long aTransactorId, int aTransactionTypeId) {
        String sql;
        sql = "{call sp_search_transaction_by_transactor_transtype(?,?)}";
        ResultSet rs = null;
        Transs = new ArrayList<Trans>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, aTransactorId);
            ps.setInt(2, aTransactionTypeId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Transs.add(this.getTransFromResultset(rs));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return Transs;
    }

    public List<Trans> getTranssByBillTransactorTransType(long aBillTransactorId, String aTransactorType) {
        String sql;
        sql = "{call sp_search_transaction_by_bill_transactor_transtype(?,?)}";
        ResultSet rs = null;
        Transs = new ArrayList<Trans>();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, aBillTransactorId);
            if (aTransactorType.equals("SUPPLIER")) {
                ps.setInt(2, 1);
            } else if (aTransactorType.equals("CUSTOMER") || aTransactorType.equals("SCHEME") || aTransactorType.equals("PROVIDER")) {
                ps.setInt(2, 2);
            } else {
                ps.setInt(2, 33);//invalid one
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                Transs.add(this.getTransFromResultset(rs));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        }
        return Transs;
    }

    public List<Trans> getTranssByAddUser(int aAddUserDetailId, int aTransactionTypeId, int aStoreId) {
        String sql;
        sql = "{call sp_search_transaction_by_add_user_detail_id(?,?,?)}";
        ResultSet rs = null;
        if (aAddUserDetailId != 0) {
            Transs = new ArrayList<Trans>();
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.setInt(1, aAddUserDetailId);
                ps.setInt(2, aTransactionTypeId);
                ps.setInt(3, aStoreId);
                rs = ps.executeQuery();
                while (rs.next()) {
                    Transs.add(this.getTransFromResultset(rs));
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        } else {
            Transs.clear();
        }
        return Transs;
    }

    public List<Trans> getReportTrans(Trans aTrans) {
        String sql;
        sql = "{call sp_report_transaction(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        ResultSet rs = null;
        this.ReportTrans.clear();
        if (aTrans != null) {
            if (aTrans.getTransactionDate() == null && aTrans.getTransactionDate2() == null
                    && aTrans.getAddDate() == null && aTrans.getAddDate2() == null
                    && aTrans.getEditDate() == null && aTrans.getEditDate2() == null) {
                this.ActionMessage = (("Atleast one date range(TransactionDate,AddDate,EditDate) is needed..."));
            } else if (aTrans.getTransactionDate() == null && aTrans.getTransactionDate2() != null) {
                this.ActionMessage = (("Trans Date(From) is needed..."));
            } else if (aTrans.getTransactionDate() != null && aTrans.getTransactionDate2() == null) {
                this.ActionMessage = (("Trans Date(T0) is needed..."));
            } else if (aTrans.getAddDate() == null && aTrans.getAddDate2() != null) {
                this.ActionMessage = (("Add Date(From) is needed..."));
            } else if (aTrans.getAddDate() != null && aTrans.getAddDate2() == null) {
                this.ActionMessage = (("Add Date(To) is needed..."));
            } else if (aTrans.getEditDate() == null && aTrans.getEditDate2() != null) {
                this.ActionMessage = (("Edit Date(From) is needed..."));
            } else if (aTrans.getEditDate() != null && aTrans.getEditDate2() == null) {
                this.ActionMessage = (("Edit Date(To) is needed..."));
            } else {
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql);) {
                    try {
                        ps.setDate(1, new java.sql.Date(aTrans.getTransactionDate().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(1, null);
                    }
                    try {
                        ps.setDate(2, new java.sql.Date(aTrans.getTransactionDate2().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(2, null);
                    }
                    try {
                        ps.setInt(3, aTrans.getStoreId());
                    } catch (NullPointerException npe) {
                        ps.setInt(3, 0);
                    }
                    try {
                        ps.setInt(4, aTrans.getStore2Id());
                    } catch (NullPointerException npe) {
                        ps.setInt(4, 0);
                    }
                    try {
                        ps.setLong(5, aTrans.getTransactorId());
                    } catch (NullPointerException npe) {
                        ps.setLong(5, 0);
                    }
                    try {
                        ps.setInt(6, aTrans.getTransactionTypeId());
                    } catch (NullPointerException npe) {
                        ps.setInt(6, 0);
                    }
                    try {
                        ps.setInt(7, aTrans.getTransactionReasonId());
                    } catch (NullPointerException npe) {
                        ps.setInt(7, 0);
                    }
                    try {
                        ps.setInt(8, aTrans.getAddUserDetailId());
                    } catch (NullPointerException npe) {
                        ps.setInt(8, 0);
                    }
                    try {
                        ps.setTimestamp(9, new java.sql.Timestamp(aTrans.getAddDate().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setTimestamp(9, null);
                    }
                    try {
                        ps.setTimestamp(10, new java.sql.Timestamp(aTrans.getAddDate2().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setTimestamp(10, null);
                    }
                    try {
                        ps.setInt(11, aTrans.getEditUserDetailId());
                    } catch (NullPointerException npe) {
                        ps.setInt(11, 0);
                    }
                    try {
                        ps.setTimestamp(12, new java.sql.Timestamp(aTrans.getEditDate().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setTimestamp(12, null);
                    }
                    try {
                        ps.setTimestamp(13, new java.sql.Timestamp(aTrans.getEditDate2().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setTimestamp(13, null);
                    }
                    try {
                        ps.setInt(14, aTrans.getTransactionUserDetailId());
                    } catch (NullPointerException npe) {
                        ps.setInt(14, 0);
                    }
                    try {
                        ps.setLong(15, aTrans.getBillTransactorId());
                    } catch (NullPointerException npe) {
                        ps.setLong(15, 0);
                    }

                    rs = ps.executeQuery();
                    //System.out.println(rs.getStatement());
                    while (rs.next()) {
                        this.ReportTrans.add(this.getTransFromResultset(rs));
                    }
                    this.ActionMessage = ((""));
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        }
        return this.ReportTrans;
    }

    public List<Trans> getReportBill(Trans aTrans) {
        String sql;
        sql = "{call sp_report_bill(?,?,?,?)}";
        ResultSet rs = null;
        this.ReportTrans.clear();
        if (aTrans != null) {
            if (aTrans.getTransactionDate() == null || aTrans.getTransactionDate2() == null) {
                //this.ActionMessage = (("Both From and To Dates are needed..."));
            } else {
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql);) {
                    try {
                        ps.setDate(1, new java.sql.Date(aTrans.getTransactionDate().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(1, null);
                    }
                    try {
                        ps.setDate(2, new java.sql.Date(aTrans.getTransactionDate2().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(2, null);
                    }
                    try {
                        ps.setInt(3, aTrans.getStoreId());
                    } catch (NullPointerException npe) {
                        ps.setInt(3, 0);
                    }
                    try {
                        ps.setLong(4, aTrans.getBillTransactorId());
                    } catch (NullPointerException npe) {
                        ps.setLong(4, 0);
                    }

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        this.ReportTrans.add(this.getTransFromResultset(rs));
                    }
                    this.ActionMessage = ((""));
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        }
        //this.ReportGrandTotal=this.getReportBillGrandTotal(ReportTrans);
        return this.ReportTrans;
    }

    public void calcCustomerCardGrandTotals(List<Trans> aCustomerCardTotals) {
        List<Trans> ati = aCustomerCardTotals;
        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        this.GrandTotalLoc = 0;
        this.GrandTotalPaidLoc = 0;
        this.GrandTotalBalanceLoc = 0;
        String LocCurCode = "";
        try {
            try {
                LocCurCode = new AccCurrencyBean().getLocalCurrency().getCurrencyCode();
            } catch (NullPointerException npe) {
                LocCurCode = "";
            }
            double xrate = 0;
            while (ListItemIndex < ListItemNo) {
                xrate = new AccXrateBean().getXrateMultiply(ati.get(ListItemIndex).getCurrencyCode(), LocCurCode);
                GrandTotalLoc = GrandTotalLoc + (ati.get(ListItemIndex).getGrandTotal() * xrate);
                GrandTotalPaidLoc = GrandTotalPaidLoc + (ati.get(ListItemIndex).getTotalPaid() * xrate);
                GrandTotalBalanceLoc = GrandTotalLoc - GrandTotalPaidLoc;
                ListItemIndex = ListItemIndex + 1;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void calcSupllierCardGrandTotals(List<Trans> aSupllierCardTotals) {
        List<Trans> ati = aSupllierCardTotals;
        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        this.GrandTotalLoc = 0;
        this.GrandTotalPaidLoc = 0;
        this.GrandTotalBalanceLoc = 0;
        String LocCurCode = "";
        try {
            try {
                LocCurCode = new AccCurrencyBean().getLocalCurrency().getCurrencyCode();
            } catch (NullPointerException npe) {
                LocCurCode = "";
            }
            double xrate = 0;
            while (ListItemIndex < ListItemNo) {
                xrate = new AccXrateBean().getXrateMultiply(ati.get(ListItemIndex).getCurrencyCode(), LocCurCode);
                GrandTotalLoc = GrandTotalLoc + (ati.get(ListItemIndex).getGrandTotal() * xrate);
                GrandTotalPaidLoc = GrandTotalPaidLoc + (ati.get(ListItemIndex).getTotalPaid() * xrate);
                GrandTotalBalanceLoc = GrandTotalLoc - GrandTotalPaidLoc;
                ListItemIndex = ListItemIndex + 1;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void refreshReportCustomerCard(Trans aTrans) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String sql;
        sql = "{call sp_report_customer_card(?,?,?,?)}";
        ResultSet rs = null;
        this.CustomerCardTranss = new ArrayList<Trans>();
        if (aTrans != null) {
            if (null == aTrans.getTransactionDate() || null == aTrans.getTransactionDate2() || aTrans.getBillTransactorId() == 0) {
                this.ActionMessage = "Select Date Range and Customer";
                FacesContext.getCurrentInstance().addMessage("Customer Card", new FacesMessage(ub.translateWordsInText(BaseName, this.ActionMessage)));
            } else {
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql);) {
                    try {
                        ps.setDate(1, new java.sql.Date(aTrans.getTransactionDate().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(1, null);
                    }
                    try {
                        ps.setDate(2, new java.sql.Date(aTrans.getTransactionDate2().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(2, null);
                    }
                    try {
                        ps.setInt(3, aTrans.getStoreId());
                    } catch (NullPointerException npe) {
                        ps.setInt(3, 0);
                    }
                    try {
                        ps.setLong(4, aTrans.getBillTransactorId());
                    } catch (NullPointerException npe) {
                        ps.setLong(4, 0);
                    }
                    rs = ps.executeQuery();
                    Trans trans;
                    while (rs.next()) {
                        trans = new Trans();
                        try {
                            trans.setTransactionId(rs.getLong("transaction_id"));
                            trans.setTransactionTypeId(rs.getInt("transaction_type_id"));
                            trans.setTransactionTypeName(new TransactionTypeBean().getTransactionType(trans.getTransactionTypeId()).getTransactionTypeName());
                            trans.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                            trans.setStoreId(rs.getInt("store_id"));
                            try {
                                trans.setTransactorId(rs.getLong("transactor_id"));
                            } catch (NullPointerException npe) {
                                trans.setTransactorId(0);
                            }
                            try {
                                trans.setGrandTotal(rs.getDouble("grand_total"));
                            } catch (NullPointerException npe) {
                                trans.setGrandTotal(0);
                            }
                            try {
                                trans.setTotalPaid(rs.getDouble("total_paid2"));
                            } catch (NullPointerException npe) {
                                trans.setTotalPaid(0);
                            }
                            try {
                                trans.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                            } catch (NullPointerException npe) {
                                trans.setTransactionUserDetailId(0);
                            }
                            try {
                                trans.setBillTransactorId(rs.getLong("bill_transactor_id"));
                            } catch (NullPointerException npe) {
                                trans.setBillTransactorId(0);
                            }
                            try {
                                trans.setTransactionNumber(rs.getString("transaction_number"));
                            } catch (NullPointerException | SQLException npe) {
                                trans.setTransactionNumber("");
                            }
                            try {
                                trans.setCurrencyCode(rs.getString("currency_code"));
                            } catch (NullPointerException | SQLException npe) {
                                trans.setCurrencyCode("");
                            }
                            try {
                                trans.setTransactionUserDetailName(rs.getString("user_name"));
                            } catch (NullPointerException | SQLException npe) {
                                trans.setTransactionUserDetailName("");
                            }
                            try {
                                trans.setTransactorName(rs.getString("transactor_names"));
                            } catch (NullPointerException | SQLException npe) {
                                trans.setTransactorName("");
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.ERROR, e);
                        }
                        this.CustomerCardTranss.add(trans);
                    }
                    this.ActionMessage = ("");
                    //refresh totals
                    this.refreshReportCustomerCardTotals(aTrans);
                    this.calcCustomerCardGrandTotals(this.CustomerCardTotals);
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        }
    }

    public void refreshReportSupplierCard(Trans aTrans) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String sql;
        sql = "{call sp_report_supplier_card(?,?,?,?)}";
        ResultSet rs = null;
        this.setSupplierCardTranss(new ArrayList<>());
        if (aTrans != null) {
            if (null == aTrans.getTransactionDate() || null == aTrans.getTransactionDate2() || aTrans.getBillTransactorId() == 0) {
                this.ActionMessage = "Select Date Range and Supplier";
                FacesContext.getCurrentInstance().addMessage("Supplier Card", new FacesMessage(ub.translateWordsInText(BaseName, this.ActionMessage)));
            } else {
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql);) {
                    try {
                        ps.setDate(1, new java.sql.Date(aTrans.getTransactionDate().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(1, null);
                    }
                    try {
                        ps.setDate(2, new java.sql.Date(aTrans.getTransactionDate2().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(2, null);
                    }
                    try {
                        ps.setInt(3, aTrans.getStoreId());
                    } catch (NullPointerException npe) {
                        ps.setInt(3, 0);
                    }
                    try {
                        ps.setLong(4, aTrans.getBillTransactorId());
                    } catch (NullPointerException npe) {
                        ps.setLong(4, 0);
                    }
                    rs = ps.executeQuery();
                    Trans trans;
                    while (rs.next()) {
                        trans = new Trans();
                        try {
                            trans.setTransactionId(rs.getLong("transaction_id"));
                            trans.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                            trans.setStoreId(rs.getInt("store_id"));
                            try {
                                trans.setTransactorId(rs.getLong("transactor_id"));
                            } catch (NullPointerException npe) {
                                trans.setTransactorId(0);
                            }
                            try {
                                trans.setGrandTotal(rs.getDouble("grand_total"));
                            } catch (NullPointerException npe) {
                                trans.setGrandTotal(0);
                            }
                            try {
                                trans.setTotalPaid(rs.getDouble("total_paid2"));
                            } catch (NullPointerException npe) {
                                trans.setTotalPaid(0);
                            }
                            try {
                                trans.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                            } catch (NullPointerException npe) {
                                trans.setTransactionUserDetailId(0);
                            }
                            try {
                                trans.setBillTransactorId(rs.getLong("bill_transactor_id"));
                            } catch (NullPointerException npe) {
                                trans.setBillTransactorId(0);
                            }
                            try {
                                trans.setTransactionNumber(rs.getString("transaction_number"));
                            } catch (NullPointerException | SQLException npe) {
                                trans.setTransactionNumber("");
                            }
                            try {
                                trans.setCurrencyCode(rs.getString("currency_code"));
                            } catch (NullPointerException | SQLException npe) {
                                trans.setCurrencyCode("");
                            }
                            try {
                                trans.setTransactionUserDetailName(rs.getString("user_name"));
                            } catch (NullPointerException | SQLException npe) {
                                trans.setTransactionUserDetailName("");
                            }
                            try {
                                trans.setTransactorName(rs.getString("transactor_names"));
                            } catch (NullPointerException | SQLException npe) {
                                trans.setTransactorName("");
                            }
                        } catch (Exception e) {
                            System.err.println("getReportSupplierCardInner:" + e.getMessage());
                        }
                        this.getSupplierCardTranss().add(trans);
                    }
                    this.ActionMessage = ("");
                    //refresh totals
                    this.refreshReportSupplierCardTotals(aTrans);
                    //this.calcSupplierCardGrandTotals(this.getSupplierCardTotals());
                    this.calcSupllierCardGrandTotals(this.getSupplierCardTotals());
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        }
    }

    public void refreshReportCustomerCardTotals(Trans aTrans) {
        String sql;
        sql = "{call sp_report_customer_card_totals(?,?,?,?)}";
        ResultSet rs = null;
        this.CustomerCardTotals = new ArrayList<Trans>();
        if (aTrans != null) {
            if (null == aTrans.getTransactionDate() || null == aTrans.getTransactionDate2() || aTrans.getBillTransactorId() == 0) {
                //
            } else {
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql);) {
                    try {
                        ps.setDate(1, new java.sql.Date(aTrans.getTransactionDate().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(1, null);
                    }
                    try {
                        ps.setDate(2, new java.sql.Date(aTrans.getTransactionDate2().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(2, null);
                    }
                    try {
                        ps.setInt(3, aTrans.getStoreId());
                    } catch (NullPointerException npe) {
                        ps.setInt(3, 0);
                    }
                    try {
                        ps.setLong(4, aTrans.getBillTransactorId());
                    } catch (NullPointerException npe) {
                        ps.setLong(4, 0);
                    }
                    rs = ps.executeQuery();
                    Trans trans;
                    while (rs.next()) {
                        trans = new Trans();
                        try {
                            try {
                                trans.setGrandTotal(rs.getDouble("grand_total"));
                            } catch (NullPointerException npe) {
                                trans.setGrandTotal(0);
                            }
                            try {
                                trans.setTotalPaid(rs.getDouble("total_paid2"));
                            } catch (NullPointerException npe) {
                                trans.setTotalPaid(0);
                            }
                            try {
                                trans.setCurrencyCode(rs.getString("currency_code"));
                            } catch (NullPointerException | SQLException npe) {
                                trans.setCurrencyCode("");
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.ERROR, e);
                        }
                        this.CustomerCardTotals.add(trans);
                    }
                    this.ActionMessage = ("");
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        }
    }

    public void refreshReportSupplierCardTotals(Trans aTrans) {
        String sql;
        sql = "{call sp_report_supplier_card_totals(?,?,?,?)}";
        ResultSet rs = null;
        this.setSupplierCardTotals(new ArrayList<Trans>());
        if (aTrans != null) {
            if (null == aTrans.getTransactionDate() || null == aTrans.getTransactionDate2() || aTrans.getBillTransactorId() == 0) {
                //
            } else {
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql);) {
                    try {
                        ps.setDate(1, new java.sql.Date(aTrans.getTransactionDate().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(1, null);
                    }
                    try {
                        ps.setDate(2, new java.sql.Date(aTrans.getTransactionDate2().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(2, null);
                    }
                    try {
                        ps.setInt(3, aTrans.getStoreId());
                    } catch (NullPointerException npe) {
                        ps.setInt(3, 0);
                    }
                    try {
                        ps.setLong(4, aTrans.getBillTransactorId());
                    } catch (NullPointerException npe) {
                        ps.setLong(4, 0);
                    }
                    rs = ps.executeQuery();
                    Trans trans;
                    while (rs.next()) {
                        trans = new Trans();
                        try {
                            try {
                                trans.setGrandTotal(rs.getDouble("grand_total"));
                            } catch (NullPointerException npe) {
                                trans.setGrandTotal(0);
                            }
                            try {
                                trans.setTotalPaid(rs.getDouble("total_paid2"));
                            } catch (NullPointerException npe) {
                                trans.setTotalPaid(0);
                            }
                            try {
                                trans.setCurrencyCode(rs.getString("currency_code"));
                            } catch (NullPointerException | SQLException npe) {
                                trans.setCurrencyCode("");
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.ERROR, e);
                        }
                        this.getSupplierCardTotals().add(trans);
                    }
                    this.ActionMessage = ("");
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        }
    }

    public double getReportBillTotal(Trans aTrans) {
        double gTotal = 0;
        String sql;
        sql = "{call sp_report_bill_summary(?,?,?,?)}";
        ResultSet rs = null;
        if (aTrans != null) {
            if (aTrans.getTransactionDate() == null || aTrans.getTransactionDate2() == null) {
                //this.ActionMessage = (("Both From and To Dates are needed..."));
            } else {
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql);) {
                    try {
                        ps.setDate(1, new java.sql.Date(aTrans.getTransactionDate().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(1, null);
                    }
                    try {
                        ps.setDate(2, new java.sql.Date(aTrans.getTransactionDate2().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(2, null);
                    }
                    try {
                        ps.setInt(3, aTrans.getStoreId());
                    } catch (NullPointerException npe) {
                        ps.setInt(3, 0);
                    }
                    try {
                        ps.setLong(4, aTrans.getBillTransactorId());
                    } catch (NullPointerException npe) {
                        ps.setLong(4, 0);
                    }
                    rs = ps.executeQuery();
                    String LocCurCode = "";
                    try {
                        LocCurCode = new AccCurrencyBean().getLocalCurrency().getCurrencyCode();
                    } catch (NullPointerException npe) {
                        LocCurCode = "";
                    }
                    while (rs.next()) {
                        gTotal = gTotal + (rs.getDouble("sum_grand_total") * new AccXrateBean().getXrateMultiply(rs.getString("currency_code"), LocCurCode));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        }
        return gTotal;
    }

    public long getReportTransCount() {
        return this.ReportTrans.size();
    }

    public List<TransSummary> getReportTransSummary(Trans aTrans, TransSummary aTransSummary) {
        String sql;
        sql = "{call sp_report_transaction_summary(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        ResultSet rs = null;
        this.ReportTransSummary.clear();
        if (aTrans != null && aTransSummary != null) {
            //if(aTransSummary.getFieldName().equals("")){
            //nothing
            //}else 
            if (aTrans.getTransactionDate() == null && aTrans.getTransactionDate2() == null
                    && aTrans.getAddDate() == null && aTrans.getAddDate2() == null
                    && aTrans.getEditDate() == null && aTrans.getEditDate2() == null) {
                //this.ActionMessage=(("Atleast one date range(TransactionDate,AddDate,EditDate) is needed..."));
            } else if (aTrans.getTransactionDate() == null && aTrans.getTransactionDate2() != null) {
                //this.ActionMessage=(("Trans Date(From) is needed..."));
            } else if (aTrans.getTransactionDate() != null && aTrans.getTransactionDate2() == null) {
                //this.ActionMessage=(("Trans Date(T0) is needed..."));
            } else if (aTrans.getAddDate() == null && aTrans.getAddDate2() != null) {
                //this.ActionMessage=(("Add Date(From) is needed..."));
            } else if (aTrans.getAddDate() != null && aTrans.getAddDate2() == null) {
                //this.ActionMessage=(("Add Date(To) is needed..."));
            } else if (aTrans.getEditDate() == null && aTrans.getEditDate2() != null) {
                //this.ActionMessage=(("Edit Date(From) is needed..."));
            } else if (aTrans.getEditDate() != null && aTrans.getEditDate2() == null) {
                //this.ActionMessage=(("Edit Date(To) is needed..."));
            } else {
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql);) {
                    try {
                        ps.setDate(1, new java.sql.Date(aTrans.getTransactionDate().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(1, null);
                    }
                    try {
                        ps.setDate(2, new java.sql.Date(aTrans.getTransactionDate2().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(2, null);
                    }
                    try {
                        ps.setInt(3, aTrans.getStoreId());
                    } catch (NullPointerException npe) {
                        ps.setInt(3, 0);
                    }
                    try {
                        ps.setInt(4, aTrans.getStore2Id());
                    } catch (NullPointerException npe) {
                        ps.setInt(4, 0);
                    }
                    try {
                        ps.setLong(5, aTrans.getTransactorId());
                    } catch (NullPointerException npe) {
                        ps.setLong(5, 0);
                    }
                    try {
                        ps.setInt(6, aTrans.getTransactionTypeId());
                    } catch (NullPointerException npe) {
                        ps.setInt(6, 0);
                    }
                    try {
                        ps.setInt(7, aTrans.getTransactionReasonId());
                    } catch (NullPointerException npe) {
                        ps.setInt(7, 0);
                    }
                    try {
                        ps.setInt(8, aTrans.getAddUserDetailId());
                    } catch (NullPointerException npe) {
                        ps.setInt(8, 0);
                    }
                    try {
                        ps.setTimestamp(9, new java.sql.Timestamp(aTrans.getAddDate().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setTimestamp(9, null);
                    }
                    try {
                        ps.setTimestamp(10, new java.sql.Timestamp(aTrans.getAddDate2().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setTimestamp(10, null);
                    }
                    try {
                        ps.setInt(11, aTrans.getEditUserDetailId());
                    } catch (NullPointerException npe) {
                        ps.setInt(11, 0);
                    }
                    try {
                        ps.setTimestamp(12, new java.sql.Timestamp(aTrans.getEditDate().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setTimestamp(12, null);
                    }
                    try {
                        ps.setTimestamp(13, new java.sql.Timestamp(aTrans.getEditDate2().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setTimestamp(13, null);
                    }
                    try {
                        ps.setString(14, aTransSummary.getFieldName());
                    } catch (NullPointerException npe) {
                        ps.setString(14, "");
                    }
                    try {
                        ps.setInt(15, aTrans.getTransactionUserDetailId());
                    } catch (NullPointerException npe) {
                        ps.setInt(15, 0);
                    }
                    try {
                        ps.setLong(16, aTrans.getBillTransactorId());
                    } catch (NullPointerException npe) {
                        ps.setLong(16, 0);
                    }

                    rs = ps.executeQuery();
                    //System.out.println(rs.getStatement());
                    while (rs.next()) {
                        this.ReportTransSummary.add(this.getTransSummaryFromResultset(rs));
                    }
                    this.ActionMessage = ((""));
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        }
        return this.ReportTransSummary;
    }

    public List<TransSummary> getReportTransEarnUserSummary(Trans aTrans) {
        String sql;
        sql = "{call sp_report_transaction_user_earn_summary(?,?,?,?,?,?)}";
        ResultSet rs = null;
        this.ReportTransSummary.clear();
        if (aTrans != null) {
            if (aTrans.getTransactionDate() == null && aTrans.getTransactionDate2() == null) {
                //this.ActionMessage=(("Atleast one date range(TransactionDate,AddDate,EditDate) is needed..."));
            } else if (aTrans.getTransactionDate() == null && aTrans.getTransactionDate2() != null) {
                //this.ActionMessage=(("Trans Date(From) is needed..."));
            } else if (aTrans.getTransactionDate() != null && aTrans.getTransactionDate2() == null) {
                //this.ActionMessage=(("Trans Date(T0) is needed..."));
            } else {
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql);) {
                    try {
                        ps.setDate(1, new java.sql.Date(aTrans.getTransactionDate().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(1, null);
                    }
                    try {
                        ps.setDate(2, new java.sql.Date(aTrans.getTransactionDate2().getTime()));
                    } catch (NullPointerException npe) {
                        ps.setDate(2, null);
                    }
                    try {
                        ps.setInt(3, aTrans.getStoreId());
                    } catch (NullPointerException npe) {
                        ps.setInt(3, 0);
                    }
                    try {
                        ps.setInt(4, aTrans.getTransactionTypeId());
                    } catch (NullPointerException npe) {
                        ps.setInt(4, 0);
                    }
                    try {
                        ps.setInt(5, aTrans.getTransactionReasonId());
                    } catch (NullPointerException npe) {
                        ps.setInt(5, 0);
                    }
                    try {
                        ps.setInt(6, aTrans.getTransactionUserDetailId());
                    } catch (NullPointerException npe) {
                        ps.setInt(6, 0);
                    }
                    rs = ps.executeQuery();
                    //System.out.println(rs.getStatement());
                    while (rs.next()) {
                        TransSummary ts = new TransSummary();
                        try {
                            ts.setEarnUserId(rs.getInt("EarnUserId"));
                        } catch (Exception ex) {
                            ts.setEarnUserId(0);
                        }
                        try {
                            ts.setTotalEarnAmount(rs.getDouble("TotalEarnAmount"));
                        } catch (Exception ex) {
                            ts.setTotalEarnAmount(0);
                        }
                        this.ReportTransSummary.add(ts);
                    }
                    this.ActionMessage = ((""));
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        }
        return this.ReportTransSummary;
    }

    public String getFieldName(String aFieldId, String aFieldName) {
        UserDetailBean udb = new UserDetailBean();
        TransactorBean tb = new TransactorBean();
        TransactionReasonBean trb = new TransactionReasonBean();
        String ReturnField = "";
        try {
            if ((aFieldName.equals("add_user_detail_id") || aFieldName.equals("edit_user_detail_id") || aFieldName.equals("transaction_user_detail_id")) && !aFieldId.equals("")) {
                ReturnField = udb.getUserDetail(Integer.parseInt(aFieldId)).getFirstName() + " " + udb.getUserDetail(Integer.parseInt(aFieldId)).getSecondName();
            } else if (aFieldName.equals("transactor_id") && !aFieldId.equals("")) {
                ReturnField = tb.getTransactor(Long.parseLong(aFieldId)).getTransactorNames();
            } else if (aFieldName.equals("bill_transactor_id") && !aFieldId.equals("")) {
                ReturnField = tb.getTransactor(Long.parseLong(aFieldId)).getTransactorNames();
            } else if (aFieldName.equals("transaction_reason_id") && !aFieldId.equals("")) {
                ReturnField = trb.getTransactionReason(Integer.parseInt(aFieldId)).getTransactionReasonName();
            } else {
                ReturnField = "Summary";
            }
            return ReturnField;
        } catch (NullPointerException npe) {
            return "";
        }
    }

    public boolean isApproveNeeded(Trans aTrans) {
        if (aTrans != null) {
            if ((aTrans.getCashDiscount() > 0 && new GeneralUserSetting().getIsApproveDiscountNeeded() == 1) || (aTrans.getSpendPointsAmount() > 0 && new GeneralUserSetting().getIsApprovePointsNeeded() == 1)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isDisableOverridePrices(UserDetail aUserDetail, List<GroupRight> aGroupRights) {
        GroupRightBean grb = new GroupRightBean();
        if (grb.IsUserGroupsFunctionAccessAllowed(aUserDetail, aGroupRights, "8", "Edit") == 1 && grb.IsUserGroupsFunctionAccessAllowed(aUserDetail, aGroupRights, "8", "Add") == 1) {
            return false;
        } else {
            return true;
        }
    }

    public void setDateToToday(Trans aTrans) {
        Date CurrentServerDate = new CompanySetting().getCURRENT_SERVER_DATE();

        aTrans.setAddDate(CurrentServerDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(aTrans.getAddDate());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // Put it back in the Date object  
        aTrans.setAddDate(cal.getTime());

        aTrans.setAddDate2(CurrentServerDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(aTrans.getAddDate2());
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        // Put it back in the Date object  
        aTrans.setAddDate2(cal2.getTime());
    }

    public void setTransDateToToday(Trans aTrans) {
        Date CurrentServerDate = new CompanySetting().getCURRENT_SERVER_DATE();

        aTrans.setTransactionDate(CurrentServerDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(aTrans.getTransactionDate());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // Put it back in the Date object  
        aTrans.setTransactionDate(cal.getTime());

        aTrans.setTransactionDate2(CurrentServerDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(aTrans.getTransactionDate2());
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        // Put it back in the Date object  
        aTrans.setTransactionDate2(cal2.getTime());
    }

    public void setDateToYesturday(Trans aTrans) {
        //Date CurrentServerDate=new CompanySetting().getCURRENT_SERVER_DATE();
        Date CurrentServerDate = new CompanySetting().getCURRENT_SERVER_DATE();

        aTrans.setAddDate(CurrentServerDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(aTrans.getAddDate());
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // Put it back in the Date object  
        aTrans.setAddDate(cal.getTime());

        aTrans.setAddDate2(CurrentServerDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(aTrans.getAddDate2());
        cal2.add(Calendar.DATE, -1);
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        // Put it back in the Date object  
        aTrans.setAddDate2(cal2.getTime());
    }

    public void setTransDateToYesturday(Trans aTrans) {
        //Date CurrentServerDate=new CompanySetting().getCURRENT_SERVER_DATE();
        Date CurrentServerDate = new CompanySetting().getCURRENT_SERVER_DATE();

        aTrans.setTransactionDate(CurrentServerDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(aTrans.getTransactionDate());
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // Put it back in the Date object  
        aTrans.setTransactionDate(cal.getTime());

        aTrans.setTransactionDate2(CurrentServerDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(aTrans.getTransactionDate2());
        cal2.add(Calendar.DATE, -1);
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        // Put it back in the Date object  
        aTrans.setTransactionDate2(cal2.getTime());
    }

    /**
     * @param Transs the Transs to set
     */
    public void setTranss(List<Trans> Transs) {
        this.Transs = Transs;
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
     * @return the SelectedTrans
     */
    public Trans getSelectedTrans() {
        return SelectedTrans;
    }

    /**
     * @param SelectedTrans the SelectedTrans to set
     */
    public void setSelectedTrans(Trans SelectedTrans) {
        this.SelectedTrans = SelectedTrans;
    }

    /**
     * @return the SelectedTransactionId
     */
    public long getSelectedTransactionId() {
        return SelectedTransactionId;
    }

    /**
     * @param SelectedTransactionId the SelectedTransactionId to set
     */
    public void setSelectedTransactionId(long SelectedTransactionId) {
        this.SelectedTransactionId = SelectedTransactionId;
    }

    /**
     * @return the SearchTrans
     */
    public String getSearchTrans() {
        return SearchTrans;
    }

    /**
     * @param SearchTrans the SearchTrans to set
     */
    public void setSearchTrans(String SearchTrans) {
        this.SearchTrans = SearchTrans;
    }

    /**
     * @return the TypedTransactorName
     */
    public String getTypedTransactorName() {
        return TypedTransactorName;
    }

    /**
     * @param TypedTransactorName the TypedTransactorName to set
     */
    public void setTypedTransactorName(String TypedTransactorName) {
        this.TypedTransactorName = TypedTransactorName;
    }

    public void insertApproveTrans(long aTransactionId, String aFunctionName, int aUserDetailId) {
        String sql = "{call sp_insert_transaction_approve(?,?,?)}";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                CallableStatement cs = conn.prepareCall(sql);) {
            cs.setLong("in_transaction_id", aTransactionId);
            cs.setString("in_function_name", aFunctionName);
            cs.setInt("in_user_detail_id", aUserDetailId);
            cs.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public long getTransViewAbsoluteTransactionId(long aTransactionId, long aTransactionId2) {
        long AbsoluteTransactionId = 0;
        if (aTransactionId != 0 || aTransactionId2 != 0) {
            if (aTransactionId != 0) {
                AbsoluteTransactionId = aTransactionId;
            } else {
                AbsoluteTransactionId = aTransactionId2;
            }
        }
        return AbsoluteTransactionId;
    }

    public String getTransViewAbsoluteTransactionNo(String aTransactionNo2, String aTransactionNo3) {
        String AbsoluteTransactionNo = "";
        if (aTransactionNo2.length() > 0 || aTransactionNo3.length() > 0) {
            if (aTransactionNo2.length() > 0) {
                AbsoluteTransactionNo = aTransactionNo2;
            } else {
                AbsoluteTransactionNo = aTransactionNo3;
            }
        }
        return AbsoluteTransactionNo;
    }

    public void ViewTransByTransIdType(long aTransactionId, int aTransactionTypeId, int aOverride) {
        //manage session variables
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
        httpSession.setAttribute("CURRENT_PAY_ID", 0);
        try {
            if (aTransactionId > 0 && aTransactionTypeId > 0) {
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransactionId);
                try {
                    //httpSession.setAttribute("CURRENT_PAY_ID", PayBean.getTransactionFirstPay(aTransactionId).getPayId());
                } catch (NullPointerException npe) {
                    httpSession.setAttribute("CURRENT_PAY_ID", 0);
                }
                //open the view in a dialog
                Map<String, Object> options = new HashMap<String, Object>();
                options.put("modal", true);
                options.put("draggable", true);
                options.put("resizable", true);
                options.put("contentWidth", 1000);
                options.put("contentHeight", 600);
                options.put("scrollable", true);
                httpSession.setAttribute("CURRENT_PRINT_OUT_JSF_FILE", this.getPrintoutJsfFile(aTransactionTypeId, aOverride));
                org.primefaces.PrimeFaces.current().dialog().openDynamic(new GeneralUserSetting().getCurrentPrintoutJsfFile(), options, null);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void SaleView(long aTransactionId, long aTransactionId2) {
        Trans aTrans = null;
        //manage session variables
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        try {
            if (aTransactionId != 0 || aTransactionId2 != 0) {
                if (aTransactionId != 0) {
                    httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransactionId);
                    //httpSession.setAttribute("CURRENT_PAY_ID", PayBean.getTransactionFirstPay(aTransactionId).getPayId());
                } else {
                    httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransactionId2);
                    //httpSession.setAttribute("CURRENT_PAY_ID", PayBean.getTransactionFirstPay(aTransactionId2).getPayId());
                }
                aTrans = this.getTrans(new GeneralUserSetting().getCurrentTransactionId());
                if (!"SALE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || aTrans.getTransactionTypeId() != 2 || aTrans.getStoreId() != new GeneralUserSetting().getCurrentStore().getStoreId()) {
                    httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                    httpSession.setAttribute("CURRENT_PAY_ID", 0);
                }
            } else {
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                httpSession.setAttribute("CURRENT_PAY_ID", 0);
            }
            //---SalesInvoiceBean.initSalesInvoiceBean();
        } catch (NullPointerException npe) {
            httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
            httpSession.setAttribute("CURRENT_PAY_ID", 0);
            //---SalesInvoiceBean.initSalesInvoiceBean();
        }
    }

    public void ViewTrans(long aTransactionId) {
        //manage session variables
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
        httpSession.setAttribute("CURRENT_PAY_ID", 0);
        try {
            if (aTransactionId != 0) {
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransactionId);
                try {
                    //httpSession.setAttribute("CURRENT_PAY_ID", PayBean.getTransactionFirstPay(aTransactionId).getPayId());
                } catch (NullPointerException npe) {
                    httpSession.setAttribute("CURRENT_PAY_ID", 0);
                }
                //open the view in a dialog
                Map<String, Object> options = new HashMap<String, Object>();
                options.put("modal", true);
                options.put("draggable", true);
                options.put("resizable", true);
                options.put("contentWidth", 1200);
                options.put("contentHeight", 750);
                options.put("scrollable", true);
                //org.primefaces.PrimeFaces.current().dialog().openDynamic("TransactionView.xhtml", options, null);
                org.primefaces.PrimeFaces.current().dialog().openDynamic("TransViewStatic.xhtml", options, null);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void ViewSalesTrans(long aTransactionId) {
        //manage session variables
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
        httpSession.setAttribute("CURRENT_PAY_ID", 0);
        try {
            if (aTransactionId != 0) {
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransactionId);
                try {
                    //httpSession.setAttribute("CURRENT_PAY_ID", PayBean.getTransactionFirstPay(aTransactionId).getPayId());
                } catch (NullPointerException npe) {
                    httpSession.setAttribute("CURRENT_PAY_ID", 0);
                }
                //open the view in a dialog
                Map<String, Object> options = new HashMap<String, Object>();
                options.put("modal", true);
                options.put("draggable", true);
                options.put("resizable", true);
                options.put("contentWidth", 1000);
                options.put("contentHeight", 500);
                options.put("scrollable", true);
                org.primefaces.PrimeFaces.current().dialog().openDynamic(this.getSRCInvoice(), options, null);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void ViewTransSalesInvoice(long aTransactionId) {
        //manage session variables
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
        //httpSession.setAttribute("CURRENT_PAY_ID", 0);
        try {
            if (aTransactionId != 0) {
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransactionId);
                //open the view in a dialog
                Map<String, Object> options = new HashMap<String, Object>();
                options.put("modal", true);
                options.put("draggable", true);
                options.put("resizable", true);
                options.put("contentWidth", 1000);
                options.put("contentHeight", 500);
                options.put("scrollable", true);
                org.primefaces.PrimeFaces.current().dialog().openDynamic("SaleInvoiceTransView.xhtml", options, null);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void PurchaseView(long aTransactionId, long aTransactionId2) {
        Trans aTrans = null;
        //manage session variables
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        try {
            if (aTransactionId != 0 || aTransactionId2 != 0) {
                if (aTransactionId != 0) {
                    httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransactionId);
                    try {
                        //httpSession.setAttribute("CURRENT_PAY_ID", PayBean.getTransactionFirstPay(aTransactionId).getPayId());
                    } catch (NullPointerException npe) {
                        httpSession.setAttribute("CURRENT_PAY_ID", 0);
                    }
                } else {
                    httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransactionId2);
                    try {
                        //httpSession.setAttribute("CURRENT_PAY_ID", PayBean.getTransactionFirstPay(aTransactionId2).getPayId());
                    } catch (NullPointerException npe) {
                        httpSession.setAttribute("CURRENT_PAY_ID", 0);
                    }
                }
                aTrans = this.getTrans(new GeneralUserSetting().getCurrentTransactionId());
                if (!"PURCHASE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || aTrans.getTransactionTypeId() != 1 || aTrans.getStoreId() != new GeneralUserSetting().getCurrentStore().getStoreId()) {
                    httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                    httpSession.setAttribute("CURRENT_PAY_ID", 0);
                }
            } else {
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                httpSession.setAttribute("CURRENT_PAY_ID", 0);
            }

            //---SalesInvoiceBean.initSalesInvoiceBean();
        } catch (NullPointerException npe) {
            httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
            httpSession.setAttribute("CURRENT_PAY_ID", 0);
            //---SalesInvoiceBean.initSalesInvoiceBean();
        }
    }

    public void DisposeView(long aTransactionId, long aTransactionId2) {
        Trans aTrans = null;
        //manage session variables
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        try {
            if (aTransactionId != 0 || aTransactionId2 != 0) {
                if (aTransactionId != 0) {
                    httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransactionId);
                    httpSession.setAttribute("CURRENT_PAY_ID", 0);
                } else {
                    httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransactionId2);
                    httpSession.setAttribute("CURRENT_PAY_ID", 0);
                }
                aTrans = this.getTrans(new GeneralUserSetting().getCurrentTransactionId());
                if (!"DISPOSE STOCK".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || aTrans.getTransactionTypeId() != 3 || aTrans.getStoreId() != new GeneralUserSetting().getCurrentStore().getStoreId()) {
                    httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                    httpSession.setAttribute("CURRENT_PAY_ID", 0);
                }
            } else {
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                httpSession.setAttribute("CURRENT_PAY_ID", 0);
            }

            //---SalesInvoiceBean.initSalesInvoiceBean();
        } catch (NullPointerException npe) {
            httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
            httpSession.setAttribute("CURRENT_PAY_ID", 0);
            //---SalesInvoiceBean.initSalesInvoiceBean();
        }
    }

    public void TransferView(long aTransactionId, long aTransactionId2) {
        Trans aTrans = null;
        //manage session variables
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        try {
            if (aTransactionId != 0 || aTransactionId2 != 0) {
                if (aTransactionId != 0) {
                    httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransactionId);
                    httpSession.setAttribute("CURRENT_PAY_ID", 0);
                } else {
                    httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransactionId2);
                    httpSession.setAttribute("CURRENT_PAY_ID", 0);
                }
                aTrans = this.getTrans(new GeneralUserSetting().getCurrentTransactionId());
                if (!"TRANSFER".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || aTrans.getTransactionTypeId() != 4 || aTrans.getStoreId() != new GeneralUserSetting().getCurrentStore().getStoreId()) {
                    httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                    httpSession.setAttribute("CURRENT_PAY_ID", 0);
                }
            } else {
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
                httpSession.setAttribute("CURRENT_PAY_ID", 0);
            }

            //---SalesInvoiceBean.initSalesInvoiceBean();
        } catch (NullPointerException npe) {
            httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
            httpSession.setAttribute("CURRENT_PAY_ID", 0);
            //---SalesInvoiceBean.initSalesInvoiceBean();
        }
    }

    public void showSalesInvoice() {
        options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", false);
        options.put("contentHeight", 420);
        org.primefaces.PrimeFaces.current().dialog().openDynamic("SalesInvoice.xhtml", options, null);
    }

    public void printSalesInvoice() {
        options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", false);
        options.put("contentHeight", 420);
        org.primefaces.PrimeFaces.current().dialog().openDynamic("SalesInvoice.xhtml", options, null);
    }

    public void callAnotherButton() {
        //:org.primefaces.PrimeFaces.current().executeScript("doHiddenClick()");
        org.primefaces.PrimeFaces.current().executeScript("doHiddenClick()");
    }

    public void autoPrint() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIViewRoot root = facesContext.getViewRoot();
        //ActionEvent actionEvent = new ActionEvent(root.findComponent("TransFormSales:cmdbPrint"));
        ActionEvent actionEvent = new ActionEvent(root.findComponent("TransFormSales:cmdbPrint"));
        actionEvent.queue();
        //System.out.println("AvtionEvent To String=" + actionEvent.toString());
        //System.out.println("Component Id==" + root.findComponent(":TransFormSales:cmdbPrint").getId());

    }

    public void dummyAction() {
        //does nothing
    }

    public boolean isTransDeleted(long aTransactionId) {
        String sql = "{call sp_search_transaction_deleted_by_id(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setLong(1, aTransactionId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return true;//deleted
            } else {
                return false;//not deleted
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
            return true;//deleted
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        }

    }

    public void initClearActionStatus() {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            this.setActionMessage("");
        }
    }

    /**
     * @return the SelectedTransactionId2
     */
    public long getSelectedTransactionId2() {
        return SelectedTransactionId2;
    }

    /**
     * @param SelectedTransactionId2 the SelectedTransactionId2 to set
     */
    public void setSelectedTransactionId2(long SelectedTransactionId2) {
        this.SelectedTransactionId2 = SelectedTransactionId2;
    }

    /**
     * @return the AutoPrintAfterSave
     */
    public boolean isAutoPrintAfterSave() {
        return AutoPrintAfterSave;
    }

    /**
     * @param AutoPrintAfterSave the AutoPrintAfterSave to set
     */
    public void setAutoPrintAfterSave(boolean AutoPrintAfterSave) {
        this.AutoPrintAfterSave = AutoPrintAfterSave;
    }

    public void setTransTotalsAndUpdate(Trans aTrans, List<TransItem> aActiveTransItems) {
        try {
            aTrans.setTotalTradeDiscount(this.getTotalTradeDiscount(aActiveTransItems));
            aTrans.setTotalVat(this.getTotalVat(aActiveTransItems));
            aTrans.setSubTotal(this.getSubTotal(aActiveTransItems));
            aTrans.setGrandTotal(this.getGrandTotal(aTrans, aActiveTransItems));
            aTrans.setTotalStdVatableAmount(this.getTotalStdVatableAmount(aActiveTransItems));
            aTrans.setTotalZeroVatableAmount(this.getTotalZeroVatableAmount(aActiveTransItems));
            aTrans.setTotalExemptVatableAmount(this.getTotalExemptVatableAmount(aActiveTransItems));
            if ("EXPENSE ENTRY".equals(new GeneralUserSetting().getCurrentTransactionTypeName())) {//EXPENSE ENTRY
                aTrans.setAmountTendered(aTrans.getGrandTotal());
            }
            aTrans.setChangeAmount(this.getChangeAmount(aTrans));
            aTrans.setPointsAwarded(this.getPointsAwarded(aTrans));
            aTrans.setSpendPoints(this.getSpendPoints(aTrans));
            aTrans.setTotalProfitMargin(this.getTotalProfitMargin(aActiveTransItems));
            //Customer Display
            String PortName = new Parameter_listBean().getParameter_listByContextNameMemory("CUSTOMER_DISPLAY", "COM_PORT_NAME").getParameter_value();
            String ClientPcName = new GeneralUserSetting().getClientComputerName();
            String SizeStr = new Parameter_listBean().getParameter_listByContextNameMemory("CUSTOMER_DISPLAY", "MAX_CHARACTERS_PER_LINE").getParameter_value();
            int Size = 0;
            if (SizeStr.length() > 0) {
                Size = Integer.parseInt(SizeStr);
            }
            if (PortName.length() > 0 && ClientPcName.length() > 0 && Size > 0 && (new GeneralUserSetting().getCurrentTransactionTypeId() == 2 || new GeneralUserSetting().getCurrentTransactionTypeId() == 11)) {
                UtilityBean ub = new UtilityBean();
                ub.invokeLocalCustomerDisplay(ClientPcName, PortName, Size, ub.formatDoubleToString(aTrans.getGrandTotal()), "");
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void setPurchaseTransVatAndUpdate(int aTransTypeId, int aTransReasonId, Trans aTrans, List<TransItem> aActiveTransItems) {
        //reset all item VATs to 0
        this.resetPurchaseItemsUnitVAT(aActiveTransItems);
        //re-calculate SubTotal and Trade Discount
        aTrans.setSubTotal(this.getSubTotalCEC(aTrans, aActiveTransItems));
        //set the manual VAT
        //--aTrans.setTotalVat() is already set from the interface
        //re-calculate the others
        aTrans.setTotalTradeDiscount(this.getTotalTradeDiscountCEC(aTrans, aActiveTransItems));
        //Manually calculate Total
        double GTotal = 0;
        GTotal = (aTrans.getSubTotal() + aTrans.getTotalVat()) - (aTrans.getTotalTradeDiscount() + aTrans.getCashDiscount());
        GTotal = (double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), GTotal);
        aTrans.setGrandTotal(GTotal);
        //other totals
        aTrans.setTotalStdVatableAmount(aTrans.getTotalVat());
        aTrans.setTotalZeroVatableAmount(0);
        aTrans.setTotalExemptVatableAmount(0);
        if (aTransTypeId == 19) {//EXPENSE ENTRY
            aTrans.setAmountTendered(aTrans.getGrandTotal());
        }
        aTrans.setPointsAwarded(0);
        aTrans.setSpendPoints(0);
        aTrans.setTotalProfitMargin(0);
        aTrans.setChangeAmount(this.getChangeAmount(aTrans));
        this.setCashDiscountPerc(aTrans);
    }

    public void setPurchaseTransDiscAndUpdate(int aTransTypeId, int aTransReasonId, Trans aTrans, List<TransItem> aActiveTransItems) {
        double GTotal = 0;
        GTotal = (aTrans.getSubTotal() + aTrans.getTotalVat()) - (aTrans.getTotalTradeDiscount() + aTrans.getCashDiscount());
        GTotal = (double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), GTotal);
        aTrans.setGrandTotal(GTotal);
        //other totals
        aTrans.setTotalStdVatableAmount(aTrans.getTotalVat());
        aTrans.setTotalZeroVatableAmount(0);
        aTrans.setTotalExemptVatableAmount(0);
        if (aTransTypeId == 19) {//EXPENSE ENTRY
            aTrans.setAmountTendered(aTrans.getGrandTotal());
        }
        aTrans.setPointsAwarded(0);
        aTrans.setSpendPoints(0);
        aTrans.setTotalProfitMargin(0);
        aTrans.setChangeAmount(this.getChangeAmount(aTrans));
        this.setCashDiscountPerc(aTrans);
    }

    public void setLoyaltyAndUpdate(int aTransTypeId, int aTransReasonId, Trans aTrans, List<TransItem> aActiveTransItems) {
        if (aTrans.getCardHolder().length() == 0) {
            aTrans.setCardNumber("");
        }
        this.setTransTotalsAndUpdateCEC(aTransTypeId, aTransReasonId, aTrans, aActiveTransItems);
    }

    public void setAmountTenderedFromDialog(double aAmountTendered, int aTransTypeId, int aTransReasonId, Trans aTrans, List<TransItem> aActiveTransItems) {
        aTrans.setAmountTendered(aAmountTendered);
        this.setTransTotalsAndUpdateCEC(aTransTypeId, aTransReasonId, aTrans, aActiveTransItems);
    }

    public void setTransTotalsAndUpdateCEC(int aTransTypeId, int aTransReasonId, Trans aTrans, List<TransItem> aActiveTransItems) {
        aTrans.setSubTotal(this.getSubTotalCEC(aTrans, aActiveTransItems));
        aTrans.setTotalTradeDiscount(this.getTotalTradeDiscountCEC(aTrans, aActiveTransItems));
        aTrans.setTotalStdVatableAmount(this.getTotalStdVatableAmountCEC(aTrans, aTransTypeId, aTransReasonId, aActiveTransItems));
        aTrans.setTotalZeroVatableAmount(this.getTotalZeroVatableAmountCEC(aTrans, aTransTypeId, aTransReasonId, aActiveTransItems));
        aTrans.setTotalExemptVatableAmount(this.getTotalExemptVatableAmountCEC(aTrans, aTransTypeId, aTransReasonId, aActiveTransItems));
        aTrans.setTotalVat(this.getTotalVatCEC(aTrans, aActiveTransItems));
        aTrans.setTotalExciseDutableAmount(this.getTotalExciseDutableAmount(aTrans, aTransTypeId, aTransReasonId, aActiveTransItems));
        aTrans.setTotalExciseDutyTaxAmount(this.getTotalExciseDutyTaxAmount(aTrans, aTransTypeId, aTransReasonId, aActiveTransItems));
        aTrans.setGrandTotal(this.getGrandTotalCEC(aTransTypeId, aTransReasonId, aTrans, aActiveTransItems));
        if (aTransTypeId == 19) {//EXPENSE ENTRY
            aTrans.setAmountTendered(aTrans.getGrandTotal());
        }
        aTrans.setChangeAmount(this.getChangeAmount(aTrans));
        aTrans.setPointsAwarded(this.getPointsAwarded(aTrans));
        aTrans.setSpendPoints(this.getSpendPointsCEC(aTransTypeId, aTransReasonId, aTrans));
        aTrans.setTotalProfitMargin(this.getTotalProfitMargin(aActiveTransItems));
        this.setCashDiscountPerc(aTrans);
        //Customer Display
        String PortName = new Parameter_listBean().getParameter_listByContextName("CUSTOMER_DISPLAY", "COM_PORT_NAME").getParameter_value();
        String ClientPcName = new GeneralUserSetting().getClientComputerName();
        String SizeStr = new Parameter_listBean().getParameter_listByContextName("CUSTOMER_DISPLAY", "MAX_CHARACTERS_PER_LINE").getParameter_value();
        int Size = 0;
        if (SizeStr.length() > 0) {
            Size = Integer.parseInt(SizeStr);
        }
        if (PortName.length() > 0 && ClientPcName.length() > 0 && Size > 0 && (aTransTypeId == 2 || aTransTypeId == 11)) {
            UtilityBean ub = new UtilityBean();
            ub.invokeLocalCustomerDisplay(ClientPcName, PortName, Size, ub.formatDoubleToString(aTrans.getGrandTotal()), "");
        }
    }

    public void setRefundAmount(Trans aNewTrans) {
        try {
            aNewTrans.setDeposit_customer(this.getRefundAmount(aNewTrans));
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public double getRefundAmount(Trans aNewTrans) {
        double RefundAmount = 0;
        try {
            if (null != aNewTrans) {
                if (aNewTrans.getTotalPaid() > aNewTrans.getGrandTotal()) {
                    RefundAmount = aNewTrans.getTotalPaid() - aNewTrans.getGrandTotal();
                } else {
                    RefundAmount = 0;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return RefundAmount;
    }

    public void setCashDiscountPercAndUpdateCEC(int aTransTypeId, int aTransReasonId, Trans aTrans, List<TransItem> aActiveTransItems) {
        if (aTrans.getCash_dicsount_perc() > 0) {
            aTrans.setCashDiscount(((aTrans.getSubTotal() - aTrans.getTotalTradeDiscount()) * aTrans.getCash_dicsount_perc()) / 100);
        } else {
            aTrans.setCashDiscount(0);
        }
        aTrans.setCashDiscount((double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), aTrans.getCashDiscount()));
        this.setTransTotalsAndUpdateCEC(aTransTypeId, aTransReasonId, aTrans, aActiveTransItems);
    }

    public void setCashDiscountPercAndUpdateCEC_err(int aTransTypeId, int aTransReasonId, Trans aTrans, List<TransItem> aActiveTransItems) {
        aTrans.setCashDiscount(0);
        this.setTransTotalsAndUpdateCEC(aTransTypeId, aTransReasonId, aTrans, aActiveTransItems);
        //aTrans.setCashDiscount((aTrans.getGrandTotal() * aTrans.getCash_dicsount_perc()) / 100);
        aTrans.setCashDiscount(((aTrans.getSubTotal() - aTrans.getTotalTradeDiscount()) * aTrans.getCash_dicsount_perc()) / 100);
        aTrans.setCashDiscount((double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), aTrans.getCashDiscount()));
        this.setTransTotalsAndUpdateCEC(aTransTypeId, aTransReasonId, aTrans, aActiveTransItems);
    }

    public void setCashDiscountFromGrandTotalCEC(int aTransTypeId, int aTransReasonId, Trans aTrans, List<TransItem> aActiveTransItems, double aGrandTotal2) {
        double VatPerc = CompanySetting.getVatPerc();
        double DiscountAmount = 0;
        //double VatAmount = 0;
        DiscountAmount = (aTrans.getSubTotal() - aTrans.getTotalTradeDiscount()) - (aGrandTotal2 / (1 + (VatPerc / 100)));
        //VatAmount=aTrans.getGrandTotal()-((aTrans.getSubTotal() - aTrans.getTotalTradeDiscount())-DiscountAmount);
        //-aTrans.setCashDiscount(0);
        //-this.setTransTotalsAndUpdateCEC(aTransTypeId, aTransReasonId, aTrans, aActiveTransItems);
        aTrans.setCashDiscount(DiscountAmount);
        //aTrans.setCashDiscount((double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), aTrans.getCashDiscount()));
        this.setTransTotalsAndUpdateCEC(aTransTypeId, aTransReasonId, aTrans, aActiveTransItems);
        //this.setCashDiscountPerc(aTrans);
    }

    public void setCashDiscountAmtAndUpdateCEC(int aTransTypeId, int aTransReasonId, Trans aTrans, List<TransItem> aActiveTransItems) {
        this.setTransTotalsAndUpdateCEC(aTransTypeId, aTransReasonId, aTrans, aActiveTransItems);
        //this.setCashDiscountPerc(aTrans);
    }

    public void setCashDiscountPerc(Trans aTrans) {
        try {
            if (aTrans.getCashDiscount() > 0) {
                double AmountBeforeCashDiscount = aTrans.getSubTotal() - aTrans.getTotalTradeDiscount();
                if (AmountBeforeCashDiscount > 0) {
                    aTrans.setCash_dicsount_perc((100 * aTrans.getCashDiscount()) / AmountBeforeCashDiscount);
                } else {
                    aTrans.setCash_dicsount_perc(0);
                }
            } else {
                aTrans.setCash_dicsount_perc(0);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public double getCashDiscountRatio(Trans aTrans) {
        double cdr = 1.0;
        double AmountBeforeCashDiscount = 0;
        try {
            if (aTrans.getCashDiscount() > 0) {
                AmountBeforeCashDiscount = aTrans.getSubTotal() - aTrans.getTotalTradeDiscount();
                if (AmountBeforeCashDiscount > 0) {
                    cdr = (1.0 * aTrans.getCashDiscount()) / AmountBeforeCashDiscount;
                } else {
                    cdr = 1.0;
                }
            } else {
                cdr = 1.0;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return cdr;
    }

    public void setCashDiscountPerc_old(Trans aTrans) {
        try {
            if (aTrans.getCashDiscount() > 0) {
                double AmountBeforeCashDiscount = aTrans.getGrandTotal() + aTrans.getCashDiscount();
                if (AmountBeforeCashDiscount > 0) {
                    aTrans.setCash_dicsount_perc((100 * aTrans.getCashDiscount()) / AmountBeforeCashDiscount);
                } else {
                    aTrans.setCash_dicsount_perc(0);
                }
            } else {
                aTrans.setCash_dicsount_perc(0);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void setTransTotalsAndUpdateV2(Trans aTrans, List<TransItem> aActiveTransItems) {
        TransactionType tt = new TransactionTypeBean().getTransactionType(aTrans.getTransactionTypeId());
        aTrans.setTotalTradeDiscount(this.getTotalTradeDiscount(aActiveTransItems));
        aTrans.setTotalVat(this.getTotalVat(aActiveTransItems));
        aTrans.setSubTotal(this.getSubTotal(aActiveTransItems));
        aTrans.setGrandTotal(this.getGrandTotalV2(aTrans, aActiveTransItems));
        aTrans.setTotalStdVatableAmount(this.getTotalStdVatableAmountV2(aTrans, aActiveTransItems));
        aTrans.setTotalZeroVatableAmount(this.getTotalZeroVatableAmountV2(aTrans, aActiveTransItems));
        aTrans.setTotalExemptVatableAmount(this.getTotalExemptVatableAmountV2(aTrans, aActiveTransItems));
        if ("EXPENSE ENTRY".equals(tt.getTransactionTypeName())) {//EXPENSE ENTRY
            aTrans.setAmountTendered(aTrans.getGrandTotal());
        }
        aTrans.setChangeAmount(this.getChangeAmount(aTrans));
        aTrans.setPointsAwarded(this.getPointsAwarded(aTrans));
        aTrans.setSpendPoints(this.getSpendPoints(aTrans));
        aTrans.setTotalProfitMargin(this.getTotalProfitMargin(aActiveTransItems));
        //Customer Display
        String PortName = new Parameter_listBean().getParameter_listByContextNameMemory("CUSTOMER_DISPLAY", "COM_PORT_NAME").getParameter_value();
        String ClientPcName = new GeneralUserSetting().getClientComputerName();
        String SizeStr = new Parameter_listBean().getParameter_listByContextNameMemory("CUSTOMER_DISPLAY", "MAX_CHARACTERS_PER_LINE").getParameter_value();
        int Size = 0;
        if (SizeStr.length() > 0) {
            Size = Integer.parseInt(SizeStr);
        }
        if (PortName.length() > 0 && ClientPcName.length() > 0 && Size > 0 && (new GeneralUserSetting().getCurrentTransactionTypeId() == 2 || new GeneralUserSetting().getCurrentTransactionTypeId() == 11)) {
            UtilityBean ub = new UtilityBean();
            ub.invokeLocalCustomerDisplay(ClientPcName, PortName, Size, ub.formatDoubleToString(aTrans.getGrandTotal()), "");
        }
    }

    public void setTransTotalsAndUpdateJournalEntry(Trans aTrans, List<TransItem> aActiveTransItems) {
        aTrans.setTotalDebit(this.getTotalDebit(aActiveTransItems));
        aTrans.setTotalCredit(this.getTotalCredit(aActiveTransItems));
    }

    public double getTotalProfitMargin(List<TransItem> aActiveTransItems) {
        List<TransItem> ati = aActiveTransItems;
        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        double TotProfitMargin = 0;
        while (ListItemIndex < ListItemNo) {
            TotProfitMargin = TotProfitMargin + (ati.get(ListItemIndex).getUnitProfitMargin() * ati.get(ListItemIndex).getItemQty());
            ListItemIndex = ListItemIndex + 1;
        }
        return TotProfitMargin;
    }

    public double getTotalTradeDiscount(List<TransItem> aActiveTransItems) {
        List<TransItem> ati = aActiveTransItems;
        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        double TotTradeDisc = 0;
        while (ListItemIndex < ListItemNo) {
            if (ati.get(ListItemIndex).getDuration_value() > 0) {
                TotTradeDisc = TotTradeDisc + (ati.get(ListItemIndex).getUnitTradeDiscount() * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value());
            } else {
                TotTradeDisc = TotTradeDisc + (ati.get(ListItemIndex).getUnitTradeDiscount() * ati.get(ListItemIndex).getItemQty());
            }
            ListItemIndex = ListItemIndex + 1;
        }
        return TotTradeDisc;
    }

    public double getTotalTradeDiscountCEC(Trans aTrans, List<TransItem> aActiveTransItems) {
        List<TransItem> ati = aActiveTransItems;
        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        double TotTradeDisc = 0;
        while (ListItemIndex < ListItemNo) {
            if (ati.get(ListItemIndex).getDuration_value() > 0) {
                TotTradeDisc = TotTradeDisc + (ati.get(ListItemIndex).getUnitTradeDiscount() * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value());
            } else {
                TotTradeDisc = TotTradeDisc + (ati.get(ListItemIndex).getUnitTradeDiscount() * ati.get(ListItemIndex).getItemQty());
            }
            ListItemIndex = ListItemIndex + 1;
        }
        TotTradeDisc = (double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), TotTradeDisc, "TOTAL_OTHER");
        return TotTradeDisc;
    }

    public double getTotalVat(List<TransItem> aActiveTransItems) {
        List<TransItem> ati = aActiveTransItems;
        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        double TVat = 0;
        while (ListItemIndex < ListItemNo) {
            if (ati.get(ListItemIndex).getDuration_value() > 0) {
                TVat = TVat + (ati.get(ListItemIndex).getUnitVat() * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value());
            } else {
                TVat = TVat + (ati.get(ListItemIndex).getUnitVat() * ati.get(ListItemIndex).getItemQty());
            }
            ListItemIndex = ListItemIndex + 1;
        }
        return TVat;
    }

    public double getTotalVatCEC(Trans aTrans, List<TransItem> aActiveTransItems) {
        List<TransItem> ati = aActiveTransItems;
        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        double TVat = 0;
        while (ListItemIndex < ListItemNo) {
            if (ati.get(ListItemIndex).getDuration_value() > 0) {
                TVat = TVat + (ati.get(ListItemIndex).getUnitVat() * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value());
            } else {
                TVat = TVat + (ati.get(ListItemIndex).getUnitVat() * ati.get(ListItemIndex).getItemQty());
            }
            ListItemIndex = ListItemIndex + 1;
        }
        //case for cash discount offered, overide vat from totals
        double VatPerc = CompanySetting.getVatPerc();
        if ((aTrans.getCashDiscount() + aTrans.getSpendPointsAmount()) > 0 && aTrans.getTotalStdVatableAmount() >= 0 && VatPerc > 0) {
            //TVat = (aTrans.getSubTotal() - aTrans.getTotalTradeDiscount() - aTrans.getCashDiscount()) * VatPerc / 100;
            TVat = aTrans.getTotalStdVatableAmount() * VatPerc / 100;
        }
        TVat = (double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), TVat, "TOTAL_OTHER");
        return TVat;
    }

    public double getTotalExciseDutyTaxAmount(Trans aTrans, int aTransTypeId, int aTransReasonId, List<TransItem> aActiveTransItems) {
        double TED = 0;
        TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
        TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        if ("SALE QUOTATION".equals(transtype.getTransactionTypeName()) || "SALE ORDER".equals(transtype.getTransactionTypeName()) || "SALE INVOICE".equals(transtype.getTransactionTypeName()) || "PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) || "PURCHASE ORDER".equals(transtype.getTransactionTypeName()) || "EXPENSE ENTRY".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
            List<TransItem> ati = aActiveTransItems;
            int ListItemIndex = 0;
            int ListItemNo = ati.size();
            while (ListItemIndex < ListItemNo) {
                TED = TED + ati.get(ListItemIndex).getTransItemExciseObj().getCalc_excise_tax_amount();
                ListItemIndex = ListItemIndex + 1;
            }
            TED = (double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), TED, "TOTAL_OTHER");
        }
        return TED;
    }

    public void resetPurchaseItemsUnitVAT(List<TransItem> aActiveTransItems) {
        List<TransItem> ati = aActiveTransItems;
        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        while (ListItemIndex < ListItemNo) {
            ati.get(ListItemIndex).setUnitVat(0);
            ati.get(ListItemIndex).setUnitPriceExcVat(ati.get(ListItemIndex).getUnitPrice());
            ati.get(ListItemIndex).setUnitPriceIncVat(ati.get(ListItemIndex).getUnitPrice());
            ati.get(ListItemIndex).setAmount(ati.get(ListItemIndex).getItemQty() * (ati.get(ListItemIndex).getUnitPrice() - ati.get(ListItemIndex).getUnitTradeDiscount()));
            ati.get(ListItemIndex).setAmountIncVat(ati.get(ListItemIndex).getAmount());
            ati.get(ListItemIndex).setAmountExcVat(ati.get(ListItemIndex).getItemQty() * (ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()));
            ListItemIndex = ListItemIndex + 1;
        }
    }

    public double getSubTotal(List<TransItem> aActiveTransItems) {
        List<TransItem> ati = aActiveTransItems;
        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        double SubT = 0;
        while (ListItemIndex < ListItemNo) {
            if (ati.get(ListItemIndex).getDuration_value() > 0) {
                SubT = SubT + (ati.get(ListItemIndex).getUnitPriceExcVat() * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value());
            } else {
                SubT = SubT + (ati.get(ListItemIndex).getUnitPriceExcVat() * ati.get(ListItemIndex).getItemQty());
            }
            ListItemIndex = ListItemIndex + 1;
        }
        return SubT;
    }

    public double getSubTotalCEC(Trans aTrans, List<TransItem> aActiveTransItems) {
        List<TransItem> ati = aActiveTransItems;
        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        double SubT = 0;
        while (ListItemIndex < ListItemNo) {
            if (ati.get(ListItemIndex).getDuration_value() > 0) {
                SubT = SubT + (ati.get(ListItemIndex).getUnitPriceExcVat() * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value());
            } else {
                SubT = SubT + (ati.get(ListItemIndex).getUnitPriceExcVat() * ati.get(ListItemIndex).getItemQty());
            }
            ListItemIndex = ListItemIndex + 1;
        }
        SubT = (double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), SubT, "TOTAL_OTHER");
        return SubT;
    }

    public double getGrandTotal(Trans aTrans, List<TransItem> aActiveTransItems) {
        double GTotal = 0;
        if ("SALE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "HIRE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "HIRE QUOTATION".equals(new GeneralUserSetting().getCurrentTransactionTypeName())) {
            GTotal = (aTrans.getSubTotal() + aTrans.getTotalVat()) - (aTrans.getTotalTradeDiscount() + aTrans.getCashDiscount());
        } else if ("SALE QUOTATION".equals(new GeneralUserSetting().getCurrentTransactionTypeName())) {
            GTotal = (aTrans.getSubTotal() + aTrans.getTotalVat()) - (aTrans.getTotalTradeDiscount() + aTrans.getCashDiscount());
        } else if ("SALE ORDER".equals(new GeneralUserSetting().getCurrentTransactionTypeName())) {
            GTotal = (aTrans.getSubTotal() + aTrans.getTotalVat()) - (aTrans.getTotalTradeDiscount() + aTrans.getCashDiscount());
        } else if ("PURCHASE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "EXPENSE ENTRY".equals(new GeneralUserSetting().getCurrentTransactionTypeName())) {
            List<TransItem> ati = aActiveTransItems;
            int ListItemIndex = 0;
            int ListItemNo = ati.size();
            GTotal = 0;
            while (ListItemIndex < ListItemNo) {
                GTotal = GTotal + (ati.get(ListItemIndex).getAmount());
                ListItemIndex = ListItemIndex + 1;
            }
            GTotal = GTotal - aTrans.getCashDiscount();
        } else if ("PURCHASE ORDER".equals(new GeneralUserSetting().getCurrentTransactionTypeName())) {
            List<TransItem> ati = aActiveTransItems;
            int ListItemIndex = 0;
            int ListItemNo = ati.size();
            GTotal = 0;
            while (ListItemIndex < ListItemNo) {
                GTotal = GTotal + (ati.get(ListItemIndex).getAmount());
                ListItemIndex = ListItemIndex + 1;
            }
            GTotal = GTotal - aTrans.getCashDiscount();
        } else if ("DISPOSE STOCK".equals(new GeneralUserSetting().getCurrentTransactionTypeName())) {
            List<TransItem> ati = aActiveTransItems;

            int ListItemIndex = 0;
            int ListItemNo = ati.size();
            GTotal = 0;
            while (ListItemIndex < ListItemNo) {
                GTotal = GTotal + (ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getUnitPriceExcVat());
                ListItemIndex = ListItemIndex + 1;
            }
        }
        return GTotal;
    }

    public double getGrandTotalCEC(int aTransTypeId, int aTransReasonId, Trans aTrans, List<TransItem> aActiveTransItems) {
        TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
        TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        double GTotal = 0;
        if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE QUOTATION".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
            GTotal = (aTrans.getSubTotal() + aTrans.getTotalVat()) - (aTrans.getTotalTradeDiscount() + aTrans.getCashDiscount() + aTrans.getSpendPointsAmount());
        } else if ("SALE QUOTATION".equals(transtype.getTransactionTypeName())) {
            GTotal = (aTrans.getSubTotal() + aTrans.getTotalVat()) - (aTrans.getTotalTradeDiscount() + aTrans.getCashDiscount());
        } else if ("SALE ORDER".equals(transtype.getTransactionTypeName())) {
            GTotal = (aTrans.getSubTotal() + aTrans.getTotalVat()) - (aTrans.getTotalTradeDiscount() + aTrans.getCashDiscount());
        } else if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) || "EXPENSE ENTRY".equals(transtype.getTransactionTypeName())) {
            GTotal = 0;
            GTotal = (aTrans.getSubTotal() + aTrans.getTotalVat()) - (aTrans.getTotalTradeDiscount() + aTrans.getCashDiscount());
        } else if ("PURCHASE ORDER".equals(transtype.getTransactionTypeName())) {
            GTotal = 0;
            GTotal = (aTrans.getSubTotal() + aTrans.getTotalVat()) - (aTrans.getTotalTradeDiscount() + aTrans.getCashDiscount());
        } else if ("DISPOSE STOCK".equals(transtype.getTransactionTypeName()) || "STOCK CONSUMPTION".equals(transtype.getTransactionTypeName())) {
            List<TransItem> ati = aActiveTransItems;
            int ListItemIndex = 0;
            int ListItemNo = ati.size();
            GTotal = 0;
            while (ListItemIndex < ListItemNo) {
                GTotal = GTotal + (ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getUnitPriceExcVat());
                ListItemIndex = ListItemIndex + 1;
            }
        }
        GTotal = (double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), GTotal, "TOTAL");
        return GTotal;
    }

    public double getGrandTotalV2(Trans aTrans, List<TransItem> aActiveTransItems) {
        double GTotal = 0;
        TransactionType tt = new TransactionTypeBean().getTransactionType(aTrans.getTransactionTypeId());
        if ("SALE INVOICE".equals(tt.getTransactionTypeName())) {
            GTotal = (aTrans.getSubTotal() + aTrans.getTotalVat()) - (aTrans.getTotalTradeDiscount() + aTrans.getCashDiscount());
        } else if ("SALE QUOTATION".equals(tt.getTransactionTypeName())) {
            GTotal = (aTrans.getSubTotal() + aTrans.getTotalVat()) - (aTrans.getTotalTradeDiscount() + aTrans.getCashDiscount());
        } else if ("SALE ORDER".equals(tt.getTransactionTypeName())) {
            GTotal = (aTrans.getSubTotal() + aTrans.getTotalVat()) - (aTrans.getTotalTradeDiscount() + aTrans.getCashDiscount());
        } else if ("PURCHASE INVOICE".equals(tt.getTransactionTypeName()) || "EXPENSE ENTRY".equals(tt.getTransactionTypeName())) {
            List<TransItem> ati = aActiveTransItems;
            int ListItemIndex = 0;
            int ListItemNo = ati.size();
            GTotal = 0;
            while (ListItemIndex < ListItemNo) {
                GTotal = GTotal + (ati.get(ListItemIndex).getAmount());
                ListItemIndex = ListItemIndex + 1;
            }
            GTotal = GTotal - aTrans.getCashDiscount();
        } else if ("PURCHASE ORDER".equals(tt.getTransactionTypeName())) {
            List<TransItem> ati = aActiveTransItems;
            int ListItemIndex = 0;
            int ListItemNo = ati.size();
            GTotal = 0;
            while (ListItemIndex < ListItemNo) {
                GTotal = GTotal + (ati.get(ListItemIndex).getAmount());
                ListItemIndex = ListItemIndex + 1;
            }
            GTotal = GTotal - aTrans.getCashDiscount();
        } else if ("DISPOSE STOCK".equals(tt.getTransactionTypeName())) {
            List<TransItem> ati = aActiveTransItems;

            int ListItemIndex = 0;
            int ListItemNo = ati.size();
            GTotal = 0;
            while (ListItemIndex < ListItemNo) {
                GTotal = GTotal + (ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getUnitPriceExcVat());
                ListItemIndex = ListItemIndex + 1;
            }
        }
        return GTotal;
    }

    public double getPointsAwarded(Trans aTrans) {
        double PtsAwarded = 0;
        try {
            if (aTrans.getCardNumber().length() > 0) {
                double XrateMultiply = new AccXrateBean().getXrateMultiply(new AccCurrencyBean().getLocalCurrency().getCurrencyCode(), aTrans.getCurrencyCode());
                PtsAwarded = aTrans.getGrandTotal() / (CompanySetting.getAwardAmountPerPoint() * XrateMultiply);
            }
            return PtsAwarded;
        } catch (Exception e) {
            return 0;
        }
    }

    public double getSpendPoints(Trans aTrans) {
        double SpendPts = 0;
        try {
            if (aTrans.getPointsCardId() > 0 && aTrans.getCardNumber().length() > 0) {
                if ("SALE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "HIRE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "HIRE RETURN INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName())) {
                    SpendPts = aTrans.getSpendPointsAmount() / CompanySetting.getSpendAmountPerPoint();
                } else if ("PURCHASE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName())) {
                    SpendPts = 0;
                }
            } else {
                SpendPts = 0;
            }
            return SpendPts;
        } catch (NullPointerException npe) {
            return 0;
        }
    }

    public double getSpendPointsCEC(int aTransTypeId, int aTransReasonId, Trans aTrans) {
        TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
        TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        double SpendPts = 0;
        try {
            if (aTrans.getCardNumber().length() > 0) {
                if ("SALE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
                    double XrateMultiply = new AccXrateBean().getXrateMultiply(new AccCurrencyBean().getLocalCurrency().getCurrencyCode(), aTrans.getCurrencyCode());
                    SpendPts = aTrans.getSpendPointsAmount() / (CompanySetting.getSpendAmountPerPoint() * XrateMultiply);
                } else if ("PURCHASE INVOICE".equals(transtype.getTransactionTypeName())) {
                    SpendPts = 0;
                }
            } else {
                SpendPts = 0;
            }
            SpendPts = (double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), SpendPts);
            return SpendPts;
        } catch (Exception e) {
            return 0;
        }
    }

    public void updatePointsCard(Trans aTrans) {
        try {
            if (new CheckApiBean().IsSmBiAvailable() && new Parameter_listBean().getParameter_listByContextName("API", "API_SMBI_URL").getParameter_value().length() > 0) {
                double XrateMultiply = new AccXrateBean().getXrateMultiply(new AccCurrencyBean().getLocalCurrency().getCurrencyCode(), aTrans.getCurrencyCode());
                LoyaltyCard loyaltycard = new SMbiBean().getLoyaltyCardDetail(aTrans.getCardNumber());
                if (loyaltycard != null) {
                    String n1 = loyaltycard.getFirst_name();
                    String n2 = loyaltycard.getSecond_name();
                    String n3 = loyaltycard.getThird_name();
                    String an = "";
                    if (n1.length() > 0) {
                        if (an.length() == 0) {
                            an = n1;
                        } else {
                            an = an + " " + n1;
                        }
                    }
                    if (n2.length() > 0) {
                        if (an.length() == 0) {
                            an = n2;
                        } else {
                            an = an + " " + n2;
                        }
                    }
                    if (n3.length() > 0) {
                        if (an.length() == 0) {
                            an = n3;
                        } else {
                            an = an + " " + n3;
                        }
                    }
                    aTrans.setCardHolder(an);
                    aTrans.setBalancePoints(loyaltycard.getPoints_balance());
                    aTrans.setBalancePointsAmount(aTrans.getBalancePoints() * CompanySetting.getSpendAmountPerPoint() * XrateMultiply);
                    aTrans.setSpendPointsAmount(0);
                    aTrans.setSpendPoints(0);
                } else {
                    this.clearTransPointsDetails(aTrans);
                }
            } else {
                this.clearTransPointsDetails(aTrans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

//    public void updatePointsCard(Trans aTrans) {
//        try {
//            if (new DBConnection().isINTER_BRANCH_MySQLConnectionAvailable().equals("ON")) {
//                if (new BranchBean().IsCompanyBranchInvalid()) {
//                    this.clearTransPointsDetails(aTrans);
//                } else if (!new AccCurrencyBean().getLocalCurrency().getCurrencyCode().equals(aTrans.getCurrencyCode())) {
//                    this.clearTransPointsDetails(aTrans);
//                } else {
//                    PointsCard pc = new PointsCard();
//                    pc = new PointsCardBean().getPointsCardByCardNumber(aTrans.getCardNumber());
//                    if (pc != null) {
//                        aTrans.setPointsCardId(pc.getPointsCardId());
//                        aTrans.setCardHolder(pc.getCardHolder());
//                        aTrans.setBalancePoints(pc.getPointsBalance());
//                        aTrans.setBalancePointsAmount(aTrans.getBalancePoints() * CompanySetting.getSpendAmountPerPoint());
//                        aTrans.setSpendPointsAmount(0);
//                        aTrans.setSpendPoints(0);
//                    } else {
//                        this.clearTransPointsDetails(aTrans);
//                    }
//                }
//            } else {
//                this.clearTransPointsDetails(aTrans);
//            }
//        } catch (Exception e) {
//            LOGGER.log(Level.ERROR, e);
//        }
//    }
    public void clearTransPointsDetails(Trans aTrans) {
        aTrans.setPointsCardId(0);
        aTrans.setCardNumber("");
        aTrans.setCardHolder("");
        aTrans.setBalancePoints(0);
        aTrans.setBalancePointsAmount(0);
        aTrans.setSpendPointsAmount(0);
        aTrans.setSpendPoints(0);
    }

    public double getTotalStdVatableAmount(List<TransItem> aActiveTransItems) {
        double GTotalStdVatableAmount = 0;
        List<TransItem> ati = aActiveTransItems;

        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        GTotalStdVatableAmount = 0;
        if ("SALE QUOTATION".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "SALE ORDER".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "SALE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "PURCHASE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "PURCHASE ORDER".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "EXPENSE ENTRY".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "HIRE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName())) {
            while (ListItemIndex < ListItemNo) {
                if ("STANDARD".equals(ati.get(ListItemIndex).getVatRated())) {
                    if ("Yes".equals(CompanySetting.getIsTradeDiscountVatLiable())) {
                        if (ati.get(ListItemIndex).getDuration_value() > 0) {
                            GTotalStdVatableAmount = GTotalStdVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - 0) * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value());
                        } else {
                            GTotalStdVatableAmount = GTotalStdVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - 0) * ati.get(ListItemIndex).getItemQty());
                        }
                    } else {
                        if (ati.get(ListItemIndex).getDuration_value() > 0) {
                            GTotalStdVatableAmount = GTotalStdVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value());
                        } else {
                            GTotalStdVatableAmount = GTotalStdVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty());
                        }
                    }
                }
                ListItemIndex = ListItemIndex + 1;
            }
        }
        return GTotalStdVatableAmount;
    }

    public double getTotalExciseDutableAmount(Trans aTrans, int aTransTypeId, int aTransReasonId, List<TransItem> aActiveTransItems) {
        TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
        TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        double TotalExciseDutableAmount = 0;
        List<TransItem> ati = aActiveTransItems;

        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        TotalExciseDutableAmount = 0;
        if ("SALE QUOTATION".equals(transtype.getTransactionTypeName()) || "SALE ORDER".equals(transtype.getTransactionTypeName()) || "SALE INVOICE".equals(transtype.getTransactionTypeName()) || "PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) || "PURCHASE ORDER".equals(transtype.getTransactionTypeName()) || "EXPENSE ENTRY".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
            while (ListItemIndex < ListItemNo) {
                if (ati.get(ListItemIndex).getTransItemExciseObj().getCalc_excise_tax_amount() > 0) {
                    TotalExciseDutableAmount = TotalExciseDutableAmount + ati.get(ListItemIndex).getAmountExcVat();
                }
                ListItemIndex = ListItemIndex + 1;
            }
        }
        TotalExciseDutableAmount = (double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), TotalExciseDutableAmount, "TOTAL_OTHER");
        return TotalExciseDutableAmount;
    }

    public double getTotalStdVatableAmountCEC(Trans aTrans, int aTransTypeId, int aTransReasonId, List<TransItem> aActiveTransItems) {
        TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
        TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        double GTotalStdVatableAmount = 0;
        List<TransItem> ati = aActiveTransItems;

        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        GTotalStdVatableAmount = 0;
        Double CashLoyaltyDisc = aTrans.getCashDiscount() + aTrans.getSpendPointsAmount();
        Double ItemCashLoyaltyDisc = 0.0;
        if ("SALE QUOTATION".equals(transtype.getTransactionTypeName()) || "SALE ORDER".equals(transtype.getTransactionTypeName()) || "SALE INVOICE".equals(transtype.getTransactionTypeName()) || "PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) || "PURCHASE ORDER".equals(transtype.getTransactionTypeName()) || "EXPENSE ENTRY".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
            while (ListItemIndex < ListItemNo) {
                if (CashLoyaltyDisc > 0) {
                    //ItemCashLoyaltyDisc = CashLoyaltyDisc * (ati.get(ListItemIndex).getAmountExcVat() / aTrans.getSubTotal());
                    ItemCashLoyaltyDisc = CashLoyaltyDisc * (ati.get(ListItemIndex).getAmountExcVat() / (aTrans.getSubTotal() - aTrans.getTotalTradeDiscount()));
                }
                if ("STANDARD".equals(ati.get(ListItemIndex).getVatRated())) {
                    if (ati.get(ListItemIndex).getDuration_value() > 0) {
                        GTotalStdVatableAmount = GTotalStdVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value()) - ItemCashLoyaltyDisc;
                    } else {
                        GTotalStdVatableAmount = GTotalStdVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty()) - ItemCashLoyaltyDisc;
                    }
                }
                ListItemIndex = ListItemIndex + 1;
            }
        }
        GTotalStdVatableAmount = (double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), GTotalStdVatableAmount, "TOTAL_OTHER");
        return GTotalStdVatableAmount;
    }

    public double getTotalStdVatableAmountV2(Trans aTrans, List<TransItem> aActiveTransItems) {
        double GTotalStdVatableAmount = 0;
        List<TransItem> ati = aActiveTransItems;

        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        GTotalStdVatableAmount = 0;
        TransactionType tt = new TransactionTypeBean().getTransactionType(aTrans.getTransactionTypeId());
        if ("SALE QUOTATION".equals(tt.getTransactionTypeName()) || "SALE ORDER".equals(tt.getTransactionTypeName()) || "SALE INVOICE".equals(tt.getTransactionTypeName()) || "PURCHASE INVOICE".equals(tt.getTransactionTypeName()) || "PURCHASE ORDER".equals(tt.getTransactionTypeName()) || "EXPENSE ENTRY".equals(tt.getTransactionTypeName())) {
            while (ListItemIndex < ListItemNo) {
                if ("STANDARD".equals(ati.get(ListItemIndex).getVatRated())) {
                    if ("Yes".equals(CompanySetting.getIsTradeDiscountVatLiable())) {
                        GTotalStdVatableAmount = GTotalStdVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - 0) * ati.get(ListItemIndex).getItemQty());
                    } else {
                        GTotalStdVatableAmount = GTotalStdVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty());
                    }
                }
                ListItemIndex = ListItemIndex + 1;
            }
        }
        return GTotalStdVatableAmount;
    }

    public double getTotalZeroVatableAmount(List<TransItem> aActiveTransItems) {
        double GTotalZeroVatableAmount = 0;
        List<TransItem> ati = aActiveTransItems;

        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        GTotalZeroVatableAmount = 0;
        if ("SALE QUOTATION".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "SALE ORDER".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "SALE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "PURCHASE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "PURCHASE ORDER".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "EXPENSE ENTRY".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "HIRE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName())) {
            while (ListItemIndex < ListItemNo) {
                if ("ZERO".equals(ati.get(ListItemIndex).getVatRated())) {
                    if ("Yes".equals(CompanySetting.getIsTradeDiscountVatLiable())) {
                        if (ati.get(ListItemIndex).getDuration_value() > 0) {
                            GTotalZeroVatableAmount = GTotalZeroVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - 0) * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value());
                        } else {
                            GTotalZeroVatableAmount = GTotalZeroVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - 0) * ati.get(ListItemIndex).getItemQty());
                        }
                    } else {
                        if (ati.get(ListItemIndex).getDuration_value() > 0) {
                            GTotalZeroVatableAmount = GTotalZeroVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value());
                        } else {
                            GTotalZeroVatableAmount = GTotalZeroVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty());
                        }
                    }
                }
                ListItemIndex = ListItemIndex + 1;
            }
        }
        return GTotalZeroVatableAmount;
    }

    public double getTotalZeroVatableAmountCEC(Trans aTrans, int aTransTypeId, int aTransReasonId, List<TransItem> aActiveTransItems) {
        TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
        TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        double GTotalZeroVatableAmount = 0;
        List<TransItem> ati = aActiveTransItems;

        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        GTotalZeroVatableAmount = 0;
        Double CashLoyaltyDisc = aTrans.getCashDiscount() + aTrans.getSpendPointsAmount();
        Double ItemCashLoyaltyDisc = 0.0;
        if ("SALE QUOTATION".equals(transtype.getTransactionTypeName()) || "SALE ORDER".equals(transtype.getTransactionTypeName()) || "SALE INVOICE".equals(transtype.getTransactionTypeName()) || "PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) || "PURCHASE ORDER".equals(transtype.getTransactionTypeName()) || "EXPENSE ENTRY".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName()) || "HIRE RETURN INVOICE".equals(transtype.getTransactionTypeName())) {
            while (ListItemIndex < ListItemNo) {
                if (CashLoyaltyDisc > 0) {
                    ItemCashLoyaltyDisc = CashLoyaltyDisc * (ati.get(ListItemIndex).getAmountExcVat() / aTrans.getSubTotal());
                }
                if ("ZERO".equals(ati.get(ListItemIndex).getVatRated())) {
                    if (ati.get(ListItemIndex).getDuration_value() > 0) {
                        GTotalZeroVatableAmount = GTotalZeroVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value()) - ItemCashLoyaltyDisc;
                    } else {
                        GTotalZeroVatableAmount = GTotalZeroVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty()) - ItemCashLoyaltyDisc;
                    }
                }
                ListItemIndex = ListItemIndex + 1;
            }
        }
        GTotalZeroVatableAmount = (double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), GTotalZeroVatableAmount, "TOTAL_OTHER");
        return GTotalZeroVatableAmount;
    }

    public double getTotalZeroVatableAmountV2(Trans aTrans, List<TransItem> aActiveTransItems) {
        double GTotalZeroVatableAmount = 0;
        List<TransItem> ati = aActiveTransItems;

        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        GTotalZeroVatableAmount = 0;
        TransactionType tt = new TransactionTypeBean().getTransactionType(aTrans.getTransactionTypeId());
        if ("SALE QUOTATION".equals(tt.getTransactionTypeName()) || "SALE ORDER".equals(tt.getTransactionTypeName()) || "SALE INVOICE".equals(tt.getTransactionTypeName()) || "PURCHASE INVOICE".equals(tt.getTransactionTypeName()) || "PURCHASE ORDER".equals(tt.getTransactionTypeName()) || "EXPENSE ENTRY".equals(tt.getTransactionTypeName())) {
            while (ListItemIndex < ListItemNo) {
                if ("ZERO".equals(ati.get(ListItemIndex).getVatRated())) {
                    if ("Yes".equals(CompanySetting.getIsTradeDiscountVatLiable())) {
                        GTotalZeroVatableAmount = GTotalZeroVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - 0) * ati.get(ListItemIndex).getItemQty());
                    } else {
                        GTotalZeroVatableAmount = GTotalZeroVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty());
                    }
                }
                ListItemIndex = ListItemIndex + 1;
            }
        }
        return GTotalZeroVatableAmount;
    }

    public double getTotalExemptVatableAmount(List<TransItem> aActiveTransItems) {
        double GTotalExemptVatableAmount = 0;
        List<TransItem> ati = aActiveTransItems;

        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        GTotalExemptVatableAmount = 0;
        if ("SALE QUOTATION".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "SALE ORDER".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "SALE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "PURCHASE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "PURCHASE ORDER".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "EXPENSE ENTRY".equals(new GeneralUserSetting().getCurrentTransactionTypeName()) || "HIRE INVOICE".equals(new GeneralUserSetting().getCurrentTransactionTypeName())) {
            while (ListItemIndex < ListItemNo) {
                if ("EXEMPT".equals(ati.get(ListItemIndex).getVatRated())) {
                    if ("Yes".equals(CompanySetting.getIsTradeDiscountVatLiable())) {
                        if (ati.get(ListItemIndex).getDuration_value() > 0) {
                            GTotalExemptVatableAmount = GTotalExemptVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - 0) * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value());
                        } else {
                            GTotalExemptVatableAmount = GTotalExemptVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - 0) * ati.get(ListItemIndex).getItemQty());
                        }
                    } else {
                        if (ati.get(ListItemIndex).getDuration_value() > 0) {
                            GTotalExemptVatableAmount = GTotalExemptVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value());
                        } else {
                            GTotalExemptVatableAmount = GTotalExemptVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty());
                        }
                    }
                }
                ListItemIndex = ListItemIndex + 1;
            }
        }
        return GTotalExemptVatableAmount;
    }

    public double getTotalExemptVatableAmountCEC(Trans aTrans, int aTransTypeId, int aTransReasonId, List<TransItem> aActiveTransItems) {
        TransactionType transtype = new TransactionTypeBean().getTransactionType(aTransTypeId);
        TransactionReason transreason = new TransactionReasonBean().getTransactionReason(aTransReasonId);
        double GTotalExemptVatableAmount = 0;
        List<TransItem> ati = aActiveTransItems;

        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        GTotalExemptVatableAmount = 0;
        Double CashLoyaltyDisc = aTrans.getCashDiscount() + aTrans.getSpendPointsAmount();
        Double ItemCashLoyaltyDisc = 0.0;
        if ("SALE QUOTATION".equals(transtype.getTransactionTypeName()) || "SALE ORDER".equals(transtype.getTransactionTypeName()) || "SALE INVOICE".equals(transtype.getTransactionTypeName()) || "PURCHASE INVOICE".equals(transtype.getTransactionTypeName()) || "PURCHASE ORDER".equals(transtype.getTransactionTypeName()) || "EXPENSE ENTRY".equals(transtype.getTransactionTypeName()) || "HIRE INVOICE".equals(transtype.getTransactionTypeName())) {
            while (ListItemIndex < ListItemNo) {
                if (CashLoyaltyDisc > 0) {
                    ItemCashLoyaltyDisc = CashLoyaltyDisc * (ati.get(ListItemIndex).getAmountExcVat() / aTrans.getSubTotal());
                }
                if ("EXEMPT".equals(ati.get(ListItemIndex).getVatRated())) {
                    if (ati.get(ListItemIndex).getDuration_value() > 0) {
                        GTotalExemptVatableAmount = GTotalExemptVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty() * ati.get(ListItemIndex).getDuration_value()) - ItemCashLoyaltyDisc;
                    } else {
                        GTotalExemptVatableAmount = GTotalExemptVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty()) - ItemCashLoyaltyDisc;
                    }
                }
                ListItemIndex = ListItemIndex + 1;
            }
        }
        GTotalExemptVatableAmount = (double) new AccCurrencyBean().roundAmount(aTrans.getCurrencyCode(), GTotalExemptVatableAmount, "TOTAL_OTHER");
        return GTotalExemptVatableAmount;
    }

    public double getTotalExemptVatableAmountV2(Trans aTrans, List<TransItem> aActiveTransItems) {
        double GTotalExemptVatableAmount = 0;
        List<TransItem> ati = aActiveTransItems;

        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        GTotalExemptVatableAmount = 0;
        TransactionType tt = new TransactionTypeBean().getTransactionType(aTrans.getTransactionTypeId());
        if ("SALE QUOTATION".equals(tt.getTransactionTypeName()) || "SALE ORDER".equals(tt.getTransactionTypeName()) || "SALE INVOICE".equals(tt.getTransactionTypeName()) || "PURCHASE INVOICE".equals(tt.getTransactionTypeName()) || "PURCHASE ORDER".equals(tt.getTransactionTypeName()) || "EXPENSE ENTRY".equals(tt.getTransactionTypeName())) {
            while (ListItemIndex < ListItemNo) {
                if ("EXEMPT SALE INVOICE".equals(ati.get(ListItemIndex).getVatRated())) {
                    if ("Yes".equals(CompanySetting.getIsTradeDiscountVatLiable())) {
                        GTotalExemptVatableAmount = GTotalExemptVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - 0) * ati.get(ListItemIndex).getItemQty());
                    } else {
                        GTotalExemptVatableAmount = GTotalExemptVatableAmount + ((ati.get(ListItemIndex).getUnitPriceExcVat() - ati.get(ListItemIndex).getUnitTradeDiscount()) * ati.get(ListItemIndex).getItemQty());
                    }
                }
                ListItemIndex = ListItemIndex + 1;
            }
        }
        return GTotalExemptVatableAmount;
    }

    public double getChangeAmount(Trans aTrans) {
        double ChangeAmt = 0;
        if (aTrans.getPayMethod() == 6) {
            //6 customer deposit used
            if (aTrans.getBillTransactorId() > 0) {
                ChangeAmt = aTrans.getDeposit_customer2() - aTrans.getAmountTendered();
            } else {
                ChangeAmt = aTrans.getDeposit_customer() - aTrans.getAmountTendered();
            }
        } else if (aTrans.getPayMethod() == 7) {
            //7 supplier deposit used
            if (aTrans.getBillTransactorId() > 0) {
                ChangeAmt = aTrans.getDeposit_supplier2() - aTrans.getAmountTendered();
            } else {
                ChangeAmt = aTrans.getDeposit_supplier() - aTrans.getAmountTendered();
            }
        } else {
            //other cash, bank, mm, eft, etc.
            //ChangeAmt = (aTrans.getAmountTendered() + aTrans.getSpendPointsAmount()) - aTrans.getGrandTotal();
            ChangeAmt = aTrans.getAmountTendered() - aTrans.getGrandTotal();
        }
        return ChangeAmt;
    }

    public double getTotalDebit(List<TransItem> aActiveTransItems) {
        List<TransItem> ati = aActiveTransItems;
        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        double Total = 0;
        while (ListItemIndex < ListItemNo) {
            Total = Total + (ati.get(ListItemIndex).getAmountExcVat());
            ListItemIndex = ListItemIndex + 1;
        }
        return Total;
    }

    public double getTotalCredit(List<TransItem> aActiveTransItems) {
        List<TransItem> ati = aActiveTransItems;
        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        double Total = 0;
        while (ListItemIndex < ListItemNo) {
            Total = Total + (ati.get(ListItemIndex).getAmountIncVat());
            ListItemIndex = ListItemIndex + 1;
        }
        return Total;
    }

    public String getPriceConflictMsg(double aCurrentCostPrice, double aNewCostPrice) {
        String PriceConflictMsg;
        if (aNewCostPrice <= 0) {
            PriceConflictMsg = "";
        } else if (aNewCostPrice > aCurrentCostPrice) {
            PriceConflictMsg = "HIGH Cost Price";
        } else if (aNewCostPrice < aCurrentCostPrice) {
            PriceConflictMsg = "LOW Cost Price";
        } else {
            PriceConflictMsg = "";
        }
        return PriceConflictMsg;
    }

    /**
     * @return the SRCInvoice
     */
    public String getSRCInvoice() {
        if (CompanySetting.getSalesReceiptVersion() == 1) {//1-Small Width
            SRCInvoice = "TransactionViewSI1.xhtml";
        } else if (CompanySetting.getSalesReceiptVersion() == 2) {//2-A4 Size
            SRCInvoice = "TransactionViewSI2.xhtml";
        } else if (CompanySetting.getSalesReceiptVersion() == 3) {//3-Very Small Width
            SRCInvoice = "TransactionViewSI3.xhtml";
        }
        return SRCInvoice;
    }

    public String getPrintoutJsfFile(int aTranstypeId, int aOverrideVersion) {
        String the_file = "";
        switch (aTranstypeId) {
            case 1:
                the_file = "TransactionViewPI.xhtml";
                break;
            case 2:
                if (aOverrideVersion == 0) {
                    if (CompanySetting.getSalesReceiptVersion() == 1) {//1-Size Small
                        the_file = "TransactionViewSI1.xhtml";
                    } else if (CompanySetting.getSalesReceiptVersion() == 2) {//2-Size A4
                        the_file = "TransactionViewSI2.xhtml";
                    }
                } else if (aOverrideVersion > 0) {
                    if (aOverrideVersion == 1) {//1-Size Small
                        the_file = "TransactionViewSI1.xhtml";
                    } else if (aOverrideVersion == 2) {//2-Size A4
                        the_file = "TransactionViewSI2.xhtml";
                    }
                }
                break;
            case 3:
                the_file = "TransactionViewDS.xhtml";
                break;
            case 4:
                the_file = "TransactionViewST.xhtml";
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                the_file = "TransactionViewPO.xhtml";
                break;
            case 9:
                the_file = "TransactionViewGRN.xhtml";
                break;
            case 10:
                the_file = "TransactionViewSQ.xhtml";
                break;
            case 11:
                the_file = "TransactionViewSO.xhtml";
                break;
            case 12:
                the_file = "TransactionViewGDN.xhtml";
                break;
            case 13:
                the_file = "TransactionViewSTR.xhtml";
                break;
        }

        return the_file;
    }

    public String getPrintFileName(String aLevel, TransactionType aTransactionType, int aPrintFileNo) {
        String the_file = "Output_0";
        int OutTransTypeId = 0;
        int OutPayTypeId = 0;
        try {
            switch (aLevel) {
                case "PARENT":
                    try {
                        OutTransTypeId = new GeneralUserSetting().getOutputDetailParent().getTrans().getTransactionTypeId();
                    } catch (NullPointerException npe) {
                    }
                    try {
                        OutPayTypeId = new GeneralUserSetting().getOutputDetailParent().getPay().getPayTypeId();
                    } catch (NullPointerException npe) {
                    }
                    if ((OutTransTypeId == aTransactionType.getTransactionTypeId()) || (OutPayTypeId == aTransactionType.getTransactionTypeId())) {
                        if (aPrintFileNo == 1) {
                            the_file = aTransactionType.getPrint_file_name1();
                        } else if (aPrintFileNo == 2) {
                            the_file = aTransactionType.getPrint_file_name2();
                        } else {
                            if (aTransactionType.getDefault_print_file() == 1) {
                                the_file = aTransactionType.getPrint_file_name1();
                            } else if (aTransactionType.getDefault_print_file() == 2) {
                                the_file = aTransactionType.getPrint_file_name2();
                            }
                        }
                    }
                    break;
                case "CHILD":
                    try {
                        OutTransTypeId = new GeneralUserSetting().getOutputDetailChild().getTrans().getTransactionTypeId();
                    } catch (NullPointerException npe) {
                    }
                    try {
                        OutPayTypeId = new GeneralUserSetting().getOutputDetailChild().getPay().getPayTypeId();
                    } catch (NullPointerException npe) {
                    }
                    if ((OutTransTypeId == aTransactionType.getTransactionTypeId()) || (OutPayTypeId == aTransactionType.getTransactionTypeId())) {
                        if (aPrintFileNo == 1) {
                            the_file = aTransactionType.getPrint_file_name1();
                        } else if (aPrintFileNo == 2) {
                            the_file = aTransactionType.getPrint_file_name2();
                        } else {
                            if (aTransactionType.getDefault_print_file() == 1) {
                                the_file = aTransactionType.getPrint_file_name1();
                            } else if (aTransactionType.getDefault_print_file() == 2) {
                                the_file = aTransactionType.getPrint_file_name2();
                            }
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return the_file + ".xhtml";
    }

    public String getPrintFileName2(String aLevel, TransactionType aTransactionType, int aPrintFileNo) {
        String the_file = "Output_0";
        int OutTransTypeId = 0;
        int OutPayTypeId = 0;
        try {
            switch (aLevel) {
                case "PARENT":
                    try {
                        OutTransTypeId = new GeneralUserSetting().getOutputDetailParent().getTrans().getTransactionTypeId();
                    } catch (NullPointerException npe) {
                    }
                    try {
                        OutPayTypeId = new GeneralUserSetting().getOutputDetailParent().getPay().getPayTypeId();
                    } catch (NullPointerException npe) {
                    }
                    if ((OutTransTypeId == aTransactionType.getTransactionTypeId()) || (OutPayTypeId == aTransactionType.getTransactionTypeId())) {
                        if (aPrintFileNo == 1) {
                            the_file = aTransactionType.getPrint_file_name1();
                        } else if (aPrintFileNo == 2) {
                            the_file = aTransactionType.getPrint_file_name2();
                        } else {
                            if (aTransactionType.getDefault_print_file() == 1) {
                                the_file = aTransactionType.getPrint_file_name1();
                            } else if (aTransactionType.getDefault_print_file() == 2) {
                                the_file = aTransactionType.getPrint_file_name2();
                            }
                        }
                    }
                    break;
                case "CHILD":
                    try {
                        OutTransTypeId = new GeneralUserSetting().getOutputDetailChild().getTrans().getTransactionTypeId();
                    } catch (NullPointerException npe) {
                    }
                    try {
                        OutPayTypeId = new GeneralUserSetting().getOutputDetailChild().getPay().getPayTypeId();
                    } catch (NullPointerException npe) {
                    }
                    if ((OutTransTypeId == aTransactionType.getTransactionTypeId()) || (OutPayTypeId == aTransactionType.getTransactionTypeId())) {
                        if (aPrintFileNo == 1) {
                            the_file = aTransactionType.getPrint_file_name1();
                        } else if (aPrintFileNo == 2) {
                            the_file = aTransactionType.getPrint_file_name2();
                        } else {
                            if (aTransactionType.getDefault_print_file() == 1) {
                                the_file = aTransactionType.getPrint_file_name1();
                            } else if (aTransactionType.getDefault_print_file() == 2) {
                                the_file = aTransactionType.getPrint_file_name2();
                            }
                        }
                    }
                    break;
            }

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return the_file + ".xhtml";
    }

    public void setDateToToday() {
        Date CurrentServerDate = new CompanySetting().getCURRENT_SERVER_DATE();
        this.setDate1(CurrentServerDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.getDate1());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // Put it back in the Date object  
        this.setDate1(cal.getTime());

        this.setDate2(CurrentServerDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(this.getDate2());
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        // Put it back in the Date object  
        this.setDate2(cal2.getTime());
    }

    public void setDateToYesturday() {
        Date CurrentServerDate = new CompanySetting().getCURRENT_SERVER_DATE();

        this.setDate1(CurrentServerDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.getDate1());
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // Put it back in the Date object  
        this.setDate1(cal.getTime());

        this.setDate2(CurrentServerDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(this.getDate2());
        cal2.add(Calendar.DATE, -1);
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        // Put it back in the Date object  
        this.setDate2(cal2.getTime());
    }

    public void reportSalesInvoiceDetail(Trans aTrans, TransBean aTransBean) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        aTransBean.setActionMessage("");
        try {
            if ((aTransBean.getDate1() != null && aTransBean.getDate2() != null) || aTrans.getTransactionNumber().length() > 0 || aTrans.getTransactionRef().length() > 0) {
                //okay no problem
            } else {
                msg = "Either Select Date Range or Specify Invoice Number or Specify Reference Number";
            }
        } catch (Exception e) {
            //do nothing
        }
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        if (msg.length() > 0) {
            aTransBean.setActionMessage(ub.translateWordsInText(BaseName, msg));
            FacesContext.getCurrentInstance().addMessage("Report", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else {
            //String sql = "SELECT * FROM transaction WHERE transaction_type_id IN(2,65,68)";
            String sql = "SELECT * FROM view_sales_invoice_detail WHERE transaction_type_id IN(2,65,68)";
            String sqlsum = "";
            if (aTransBean.getFieldName().length() > 0) {
                //sqlsum = "SELECT " + aTransBean.getFieldName() + ",currency_code,sum(grand_total) as grand_total,sum(total_profit_margin) as total_profit_margin,sum(total_vat) as total_vat,sum(cash_discount) as cash_discount,sum(spent_points_amount) as spent_points_amount FROM transaction WHERE transaction_type_id IN(2,65,68)";
                sqlsum = "SELECT " + aTransBean.getFieldName() + ",currency_code,sum(grand_total) as grand_total,sum(total_profit_margin) as total_profit_margin,sum(total_vat) as total_vat,sum(cash_discount) as cash_discount,sum(spent_points_amount) as spent_points_amount,sum(TotalExciseDutyTaxAmount) as TotalExciseDutyTaxAmount FROM view_sales_invoice_detail WHERE transaction_type_id IN(2,65,68)";
            } else {
                //sqlsum = "SELECT currency_code,sum(grand_total) as grand_total,sum(total_profit_margin) as total_profit_margin,sum(total_vat) as total_vat,sum(cash_discount) as cash_discount,sum(spent_points_amount) as spent_points_amount FROM transaction WHERE transaction_type_id IN(2,65,68)";
                sqlsum = "SELECT currency_code,sum(grand_total) as grand_total,sum(total_profit_margin) as total_profit_margin,sum(total_vat) as total_vat,sum(cash_discount) as cash_discount,sum(spent_points_amount) as spent_points_amount,sum(TotalExciseDutyTaxAmount) as TotalExciseDutyTaxAmount FROM view_sales_invoice_detail WHERE transaction_type_id IN(2,65,68)";
            }
            String wheresql = "";
            String ordersql = "";
            String ordersqlsum = "";
            String groupbysql = "";
            if (aTransBean.getFieldName().length() > 0) {
                groupbysql = " GROUP BY " + aTransBean.getFieldName() + ",currency_code";
            } else {
                groupbysql = " GROUP BY currency_code";
            }
            if (aTrans.getStoreId() > 0) {
                wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
            }
            if (aTrans.getTransactionNumber().length() > 0) {
                wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
            }
            if (aTrans.getTransactionRef().length() > 0) {
                wheresql = wheresql + " AND transaction_ref='" + aTrans.getTransactionRef() + "'";
            }
            if (aTrans.getAddUserDetailId() > 0) {
                wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
            }
            if (aTrans.getTransactionUserDetailId() > 0) {
                wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
            }
            if (aTrans.getBillTransactorId() > 0) {
                wheresql = wheresql + " AND bill_transactor_id=" + aTrans.getBillTransactorId();
            }
            if (aTrans.getTransactorId() > 0) {
                wheresql = wheresql + " AND transactor_id=" + aTrans.getTransactorId();
            }
            if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
                switch (aTransBean.getDateType()) {
                    case "Invoice Date":
                        wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                        break;
                    case "Add Date":
                        wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                        break;
                }
            }
            ordersql = " ORDER BY add_date DESC,transaction_id DESC";
            if (aTransBean.getFieldName().length() > 0) {
                ordersqlsum = " ORDER BY " + aTransBean.getFieldName() + ",currency_code";
            } else {
                ordersqlsum = " ORDER BY currency_code";
            }
            sql = sql + wheresql + ordersql;
            sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                rs = ps.executeQuery();
                Trans trans = null;
                TransExtBean teb = new TransExtBean();
                while (rs.next()) {
                    trans = new Trans();
                    this.setTransFromResultset(trans, rs);
                    double TotalPaid = trans.getTotalPaid();
                    trans.setTotalPaid(TotalPaid);
                    if (TotalPaid >= trans.getGrandTotal()) {
                        trans.setIs_paid(1);
                    } else if (TotalPaid > 0 && TotalPaid < trans.getGrandTotal()) {
                        trans.setIs_paid(2);
                    } else {
                        trans.setIs_paid(0);
                    }
                    try {
                        trans.setTotalExciseDutyTaxAmount(rs.getDouble("TotalExciseDutyTaxAmount"));
                    } catch (Exception e) {
                    }
                    this.TransList.add(trans);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }

            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sqlsum);) {
                rs = ps.executeQuery();
                Trans transsum = null;
                while (rs.next()) {
                    transsum = new Trans();
                    if (aTransBean.getFieldName().length() > 0) {
                        switch (aTransBean.getFieldName()) {
                            case "add_user_detail_id":
                                try {
                                    transsum.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                                } catch (Exception e) {
                                    transsum.setAddUserDetailId(0);
                                }
                                break;
                            case "transaction_user_detail_id":
                                try {
                                    transsum.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                                } catch (Exception e) {
                                    transsum.setTransactionUserDetailId(0);
                                }
                                break;
                            case "bill_transactor_id":
                                try {
                                    transsum.setBillTransactorId(rs.getLong("bill_transactor_id"));
                                } catch (Exception e) {
                                    transsum.setBillTransactorId(0);
                                }
                                break;
                            case "transactor_id":
                                try {
                                    transsum.setTransactorId(rs.getLong("transactor_id"));
                                } catch (Exception e) {
                                    transsum.setTransactorId(0);
                                }
                                break;
                            case "transaction_date":
                                try {
                                    transsum.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                                } catch (NullPointerException | SQLException npe) {
                                    transsum.setTransactionDate(null);
                                }
                                break;
                            case "store_id":
                                try {
                                    transsum.setStoreId(rs.getInt("store_id"));
                                    Store st = new StoreBean().getStore(transsum.getStoreId());
                                    transsum.setStoreName(st.getStoreName());
                                } catch (Exception e) {
                                    transsum.setStoreName("");
                                }
                                break;
                        }
                    }
                    try {
                        transsum.setCurrencyCode(rs.getString("currency_code"));
                    } catch (Exception e) {
                        transsum.setCurrencyCode("");
                    }
                    try {
                        transsum.setGrandTotal(rs.getDouble("grand_total"));
                    } catch (Exception e) {
                        transsum.setGrandTotal(0);
                    }
                    try {
                        transsum.setTotalProfitMargin(rs.getDouble("total_profit_margin"));
                    } catch (Exception e) {
                        transsum.setTotalProfitMargin(0);
                    }
                    try {
                        transsum.setTotalVat(rs.getDouble("total_vat"));
                    } catch (Exception e) {
                        transsum.setTotalVat(0);
                    }
                    try {
                        transsum.setCashDiscount(rs.getDouble("cash_discount"));
                    } catch (Exception e) {
                        transsum.setCashDiscount(0);
                    }
                    try {
                        transsum.setSpendPointsAmount(rs.getDouble("spent_points_amount"));
                    } catch (Exception e) {
                        transsum.setSpendPointsAmount(0);
                    }
                    try {
                        transsum.setTotalExciseDutyTaxAmount(rs.getDouble("TotalExciseDutyTaxAmount"));
                    } catch (Exception e) {
                    }
                    this.TransListSummary.add(transsum);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    public String getTransCrDr(String aTransNo) {
        String transcrdr = "";
        String sql = "{call sp_search_cr_dr(?)}";
        ResultSet rs = null;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                CallableStatement cs = conn.prepareCall(sql);) {
            cs.setString("in_transaction_number", aTransNo);
            rs = cs.executeQuery();
            if (rs.next()) {
                transcrdr = rs.getString("cr_dr_flag");
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return transcrdr;
    }

    public void reportSalesInvoiceDetail_old(Trans aTrans, TransBean aTransBean) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        aTransBean.setActionMessage("");
        try {
            if ((aTransBean.getDate1() != null && aTransBean.getDate2() != null) || aTrans.getTransactionNumber().length() > 0 || aTrans.getTransactionRef().length() > 0) {
                //okay no problem
            } else {
                msg = "Either Select Date Range or Specify Invoice Number or Specify Reference Number";
            }
        } catch (Exception e) {
            //do nothing
        }
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        if (msg.length() > 0) {
            aTransBean.setActionMessage(ub.translateWordsInText(BaseName, msg));
            FacesContext.getCurrentInstance().addMessage("Report", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else {
            String sql = "SELECT * FROM view_transaction_cr_dr WHERE 1=1";
            String sqlsum = "";
            if (aTransBean.getFieldName().length() > 0) {
                sqlsum = "SELECT " + aTransBean.getFieldName() + ",currency_code,sum(grand_total) as grand_total,sum(total_profit_margin) as total_profit_margin,sum(total_vat) as total_vat,sum(cash_discount) as cash_discount,sum(spent_points_amount) as spent_points_amount FROM transaction WHERE transaction_type_id IN(2,65,68)";
            } else {
                sqlsum = "SELECT currency_code,sum(grand_total) as grand_total,sum(total_profit_margin) as total_profit_margin,sum(total_vat) as total_vat,sum(cash_discount) as cash_discount,sum(spent_points_amount) as spent_points_amount FROM transaction WHERE transaction_type_id IN(2,65,68)";
            }
            String wheresql = "";
            String ordersql = "";
            String ordersqlsum = "";
            String groupbysql = "";
            if (aTransBean.getFieldName().length() > 0) {
                groupbysql = " GROUP BY " + aTransBean.getFieldName() + ",currency_code";
            } else {
                groupbysql = " GROUP BY currency_code";
            }
            if (aTrans.getStoreId() > 0) {
                wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
            }
            if (aTrans.getTransactionNumber().length() > 0) {
                wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
            }
            if (aTrans.getTransactionRef().length() > 0) {
                wheresql = wheresql + " AND transaction_ref='" + aTrans.getTransactionRef() + "'";
            }
            if (aTrans.getAddUserDetailId() > 0) {
                wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
            }
            if (aTrans.getTransactionUserDetailId() > 0) {
                wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
            }
            if (aTrans.getBillTransactorId() > 0) {
                wheresql = wheresql + " AND bill_transactor_id=" + aTrans.getBillTransactorId();
            }
            if (aTrans.getTransactorId() > 0) {
                wheresql = wheresql + " AND transactor_id=" + aTrans.getTransactorId();
            }
            if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
                switch (aTransBean.getDateType()) {
                    case "Invoice Date":
                        wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                        break;
                    case "Add Date":
                        wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                        break;
                }
            }
            ordersql = " ORDER BY add_date DESC,transaction_id DESC";
            if (aTransBean.getFieldName().length() > 0) {
                ordersqlsum = " ORDER BY " + aTransBean.getFieldName() + ",currency_code";
            } else {
                ordersqlsum = " ORDER BY currency_code";
            }
            sql = sql + wheresql + ordersql;
            sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                rs = ps.executeQuery();
                Trans trans = null;
                while (rs.next()) {
                    trans = new Trans();
                    this.setTransFromResultset(trans, rs);
                    double TotalPaid = trans.getTotalPaid();
                    trans.setTotalPaid(TotalPaid);
                    if (TotalPaid >= trans.getGrandTotal()) {
                        trans.setIs_paid(1);
                    } else if (TotalPaid > 0 && TotalPaid < trans.getGrandTotal()) {
                        trans.setIs_paid(2);
                    } else {
                        trans.setIs_paid(0);
                    }
                    trans.setCr_dr_flag(rs.getString("cr_dr_flag"));
                    this.TransList.add(trans);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }

            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sqlsum);) {
                rs = ps.executeQuery();
                Trans transsum = null;
                while (rs.next()) {
                    transsum = new Trans();
                    if (aTransBean.getFieldName().length() > 0) {
                        switch (aTransBean.getFieldName()) {
                            case "add_user_detail_id":
                                try {
                                    transsum.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                                } catch (NullPointerException npe) {
                                    transsum.setAddUserDetailId(0);
                                }
                                break;
                            case "transaction_user_detail_id":
                                try {
                                    transsum.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                                } catch (NullPointerException npe) {
                                    transsum.setTransactionUserDetailId(0);
                                }
                                break;
                            case "bill_transactor_id":
                                try {
                                    transsum.setBillTransactorId(rs.getLong("bill_transactor_id"));
                                } catch (NullPointerException npe) {
                                    transsum.setBillTransactorId(0);
                                }
                                break;
                            case "transactor_id":
                                try {
                                    transsum.setTransactorId(rs.getLong("transactor_id"));
                                } catch (NullPointerException npe) {
                                    transsum.setTransactorId(0);
                                }
                                break;
                            case "transaction_date":
                                try {
                                    transsum.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                                } catch (NullPointerException | SQLException npe) {
                                    transsum.setTransactionDate(null);
                                }
                                break;
                            case "store_id":
                                try {
                                    transsum.setStoreId(rs.getInt("store_id"));
                                    Store st = new StoreBean().getStore(transsum.getStoreId());
                                    transsum.setStoreName(st.getStoreName());
                                } catch (NullPointerException npe) {
                                    transsum.setStoreName("");
                                }
                                break;
                        }
                    }
                    try {
                        transsum.setCurrencyCode(rs.getString("currency_code"));
                    } catch (NullPointerException npe) {
                        transsum.setCurrencyCode("");
                    }
                    try {
                        transsum.setGrandTotal(rs.getDouble("grand_total"));
                    } catch (NullPointerException npe) {
                        transsum.setGrandTotal(0);
                    }
                    try {
                        transsum.setTotalProfitMargin(rs.getDouble("total_profit_margin"));
                    } catch (NullPointerException npe) {
                        transsum.setTotalProfitMargin(0);
                    }
                    try {
                        transsum.setTotalVat(rs.getDouble("total_vat"));
                    } catch (NullPointerException npe) {
                        transsum.setTotalVat(0);
                    }
                    try {
                        transsum.setCashDiscount(rs.getDouble("cash_discount"));
                    } catch (NullPointerException npe) {
                        transsum.setCashDiscount(0);
                    }
                    try {
                        transsum.setSpendPointsAmount(rs.getDouble("spent_points_amount"));
                    } catch (NullPointerException npe) {
                        transsum.setSpendPointsAmount(0);
                    }
                    this.TransListSummary.add(transsum);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    public void reSubmitDebitOrCreditNoteTaxAPI(long aInnerTransId, int aInnerTransTypeId, Trans aTrans, TransBean aTransBean, String aUpdateType) {
        //TAX API
        try {
            if (aInnerTransTypeId == 2 && new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value().length() > 0) {//SALES INVOICE
                Transaction_tax_map ttm = new Transaction_tax_mapBean().getTransaction_tax_map(aInnerTransId, aInnerTransTypeId);
                if (null == ttm) {
                    //do nothing, original record for update not found
                } else {
                    if (aUpdateType.equals("Debit Note")) {//Debit note
                        new InvoiceBean().submitDebitNote(aInnerTransId, aInnerTransTypeId);
                    } else if (aUpdateType.equals("Credit Note")) {//Credit note
                        new InvoiceBean().submitCreditNote(aInnerTransId, aInnerTransTypeId);
                    }
                }
            }
            this.reportSalesTaxAPI(aTrans, aTransBean);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reSubmitInvoiceTaxAPIAll(List<Trans> aTransList, Trans aTrans, TransBean aTransBean) {
        try {
            int n = 0;
            for (int i = 0; i < aTransList.size() && n <= 100; i++) {
                if (aTransList.get(i).getTax_synced() == 0) {
                    this.reSubmitInvoiceTaxAPI(aTransList.get(i).getTransactionId(), aTransList.get(i).getTransactionTypeId());
                    n = n + 1;
                }
            }
            this.reportSalesTaxAPI(aTrans, aTransBean);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reSubmitInvoiceTaxAPIOne(long aInnerTransId, long aTransTypeId, Trans aTrans, TransBean aTransBean) {
        try {
            this.reSubmitInvoiceTaxAPI(aInnerTransId, aTransTypeId);
            this.reportSalesTaxAPI(aTrans, aTransBean);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reSubmitInvoiceTaxAPI(long aInnerTransId, long aTransTypeId) {
        try {
            if (new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value().length() > 0) {
                if (aTransTypeId == 2) {//Invoice
                    List<TransItem> tis1 = new TransItemBean().getTransItemsByTransactionId(aInnerTransId);
                    if (new Item_tax_mapBean().countItemsNotMappedSynced(tis1) == 0) {
                        new InvoiceBean().submitTaxInvoice(aInnerTransId);
                    }
                } else if (aTransTypeId == 82) {//Credit Note
                    List<TransItem> tis2 = new CreditDebitNoteBean().getTransItemsByTransactionId_cr_dr_note(aInnerTransId);
                    if (new Item_tax_mapBean().countItemsNotMappedSynced(tis2) == 0) {
                        new InvoiceBean().submitCreditNote(aInnerTransId, 82);
                    }
                } else if (aTransTypeId == 83) {//Debit Note
                    List<TransItem> tis3 = new CreditDebitNoteBean().getTransItemsByTransactionId_cr_dr_note(aInnerTransId);
                    if (new Item_tax_mapBean().countItemsNotMappedSynced(tis3) == 0) {
                        new InvoiceBean().submitDebitNote(aInnerTransId, 83);
                    }
                }
                //this.reportSalesTaxAPI(aTrans, aTransBean);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reGetCreditNoteApprovalStatusTaxAPI(String aReferenceNoTax, long aTransTypeId, Trans aTrans, TransBean aTransBean) {
        try {
            if (new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value().length() > 0) {
                if (aTransTypeId == 82) {//Credit Note
                    String SellerTin = CompanySetting.getTaxIdentity();
                    String DeviceNo = new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value();
                    new InvoiceBean().updateCreditNote(aReferenceNoTax, DeviceNo, SellerTin);
                }
                this.reportSalesTaxAPI(aTrans, aTransBean);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void markManyUpdatesReconsiled(long aInnerTransId, int aInnerTransTypeId, Trans aTrans, TransBean aTransBean) {
        try {
            if (aInnerTransTypeId == 2 && new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value().length() > 0) {//SALES INVOICE
                Transaction_tax_map ttm = new Transaction_tax_mapBean().getTransaction_tax_map(aInnerTransId, aInnerTransTypeId);
                new Transaction_tax_mapBean().markTransaction_tax_mapMore_than_once_update_reconsiled(ttm);
            }
            this.reportSalesTaxAPI(aTrans, aTransBean);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportSalesTaxAPI(Trans aTrans, TransBean aTransBean) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        aTransBean.setActionMessage("");
        try {
            if ((aTransBean.getDate1() != null && aTransBean.getDate2() != null) || aTrans.getTransactionNumber().length() > 0 || aTrans.getTransactionRef().length() > 0) {
                //okay no problem
            } else {
                msg = "Either Select Date Range or Specify Transaction Number";
            }
        } catch (Exception e) {
            //do nothing
        }
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        if (msg.length() > 0) {
            aTransBean.setActionMessage(ub.translateWordsInText(BaseName, msg));
            FacesContext.getCurrentInstance().addMessage("Report", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else {
            //1. detail
            String WhereAppend = "";
            String OrderAppend = "";
            if (aTrans.getStoreId() > 0) {
                WhereAppend = WhereAppend + " AND t.store_id=" + aTrans.getStoreId();
            }
            if (aTrans.getTransactionNumber().length() > 0) {
                WhereAppend = WhereAppend + " AND t.transaction_number='" + aTrans.getTransactionNumber() + "'";
            }
            if (aTrans.getTransactionRef().length() > 0) {
                //WhereAppend = WhereAppend + " AND t.transaction_ref='" + aTrans.getTransactionRef() + "'";
                WhereAppend = WhereAppend + " AND t2.transaction_number_tax='" + aTrans.getTransactionRef() + "'";
            }
            if (aTrans.getAddUserDetailId() > 0) {
                WhereAppend = WhereAppend + " AND t.add_user_detail_id=" + aTrans.getAddUserDetailId();
            }
            if (aTrans.getTransactionUserDetailId() > 0) {
                WhereAppend = WhereAppend + " AND t.transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
            }
            if (aTrans.getBillTransactorId() > 0) {
                WhereAppend = WhereAppend + " AND t.bill_transactor_id=" + aTrans.getBillTransactorId();
            }
            if (aTrans.getTransactorId() > 0) {
                WhereAppend = WhereAppend + " AND t.transactor_id=" + aTrans.getTransactorId();
            }
            if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
                switch (aTransBean.getDateType()) {
                    case "Invoice Date":
                        WhereAppend = WhereAppend + " AND t.transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                        break;
                    case "Add Date":
                        WhereAppend = WhereAppend + " AND t.add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                        break;
                }
            }
            String sql = "SELECT  "
                    + "		t.*,0 as mode_code,"
                    + "		ifnull(t2.reference_number_tax,'') as reference_number_tax,ifnull(t2.transaction_number_tax,'') as transaction_number_tax,"
                    + "		ifnull(t2.verification_code_tax,'') as verification_code_tax,ifnull(t2.qr_code_tax,'') as qr_code_tax,"
                    + "		case when ifnull(t2.transaction_tax_map_id,0)>0 then 1 else 0 end as tax_synced,"
                    + "		case when ifnull(t2.transaction_tax_map_id,0)>0 then 'Synced' else 'Not Synced' end as sync_flag,"
                    + "		t2.is_updated_more_than_once,t2.more_than_once_update_reconsiled, "
                    + "		case "
                    + "			when t2.is_updated_more_than_once=1 and t2.more_than_once_update_reconsiled=1 then 'Reconsiled' "
                    + "			when t2.is_updated_more_than_once=1 and t2.more_than_once_update_reconsiled=0 then 'Not Reconsiled' "
                    + "			else '' "
                    + "		end as reconsile_flag "
                    + "		FROM transaction t "
                    + "		left join transaction_tax_map t2 on t.transaction_id=t2.transaction_id and t.transaction_type_id=t2.transaction_type_id "
                    + "		WHERE t.transaction_type_id IN(2,65,68) " + WhereAppend
                    + " UNION "
                    + " SELECT  "
                    + "	t.*,"
                    + "	ifnull(t2.reference_number_tax,'') as reference_number_tax,ifnull(t2.transaction_number_tax,'') as transaction_number_tax,"
                    + "    ifnull(t2.verification_code_tax,'') as verification_code_tax,ifnull(t2.qr_code_tax,'') as qr_code_tax,"
                    + "	case when ifnull(t2.transaction_tax_map_id,0)>0 then 1 else 0 end as tax_synced,"
                    + "    case when ifnull(t2.transaction_tax_map_id,0)>0 then 'Synced' else 'Not Synced' end as sync_flag,"
                    + "	t2.is_updated_more_than_once,t2.more_than_once_update_reconsiled, "
                    + "	case "
                    + "		when t2.is_updated_more_than_once=1 and t2.more_than_once_update_reconsiled=1 then 'Reconsiled' "
                    + "		when t2.is_updated_more_than_once=1 and t2.more_than_once_update_reconsiled=0 then 'Not Reconsiled' "
                    + "		else '' "
                    + "	end as reconsile_flag "
                    + "	FROM transaction_cr_dr_note t "
                    + "	left join transaction_tax_map t2 on t.transaction_id=t2.transaction_id and t.transaction_type_id=t2.transaction_type_id "
                    + "	WHERE t.transaction_type_id IN(82,83) " + WhereAppend;
            OrderAppend = " ORDER BY add_date DESC";
            sql = "SELECT * FROM (" + sql + ") as a " + OrderAppend;
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                rs = ps.executeQuery();
                Trans trans = null;
                while (rs.next()) {
                    trans = new Trans();
                    this.setTransFromResultset(trans, rs);
                    try {
                        trans.setReference_number_tax(rs.getString("reference_number_tax"));
                    } catch (Exception npe) {
                        trans.setReference_number_tax("");
                    }
                    try {
                        trans.setTransaction_number_tax(rs.getString("transaction_number_tax"));
                    } catch (Exception npe) {
                        trans.setTransaction_number_tax("");
                    }
                    try {
                        trans.setTax_synced(rs.getInt("tax_synced"));
                    } catch (Exception npe) {
                        trans.setTax_synced(0);
                    }
                    try {
                        trans.setReconsile_flag(rs.getString("reconsile_flag"));
                    } catch (Exception npe) {
                        trans.setReconsile_flag("");
                    }
                    this.TransList.add(trans);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }

            //2. summary
            String sqlsum = "";
            if (aTransBean.getFieldName().length() > 0) {
                sqlsum = "SELECT " + aTransBean.getFieldName() + ",transaction_type_id,sync_flag,currency_code,sum(grand_total) as grand_total,sum(total_vat) as total_vat,sum(cash_discount) as cash_discount FROM (" + sql + ") as b ";
            } else {
                sqlsum = "SELECT transaction_type_id,sync_flag,currency_code,sum(grand_total) as grand_total,sum(total_vat) as total_vat,sum(cash_discount) as cash_discount FROM (" + sql + ") as b ";
            }

            String OrderAppendSum = "";
            String GroupAppendSum = "";
            if (aTransBean.getFieldName().length() > 0) {
                GroupAppendSum = " GROUP BY " + aTransBean.getFieldName() + ",transaction_type_id,sync_flag,currency_code";
            } else {
                GroupAppendSum = " GROUP BY transaction_type_id,sync_flag,currency_code";
            }
            if (aTransBean.getFieldName().length() > 0) {
                OrderAppendSum = " ORDER BY " + aTransBean.getFieldName() + ",transaction_type_id,sync_flag,currency_code";
            } else {
                OrderAppendSum = " ORDER BY transaction_type_id,sync_flag,currency_code";
            }
            sqlsum = sqlsum + GroupAppendSum + OrderAppendSum;
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sqlsum);) {
                rs = ps.executeQuery();
                Trans transsum = null;
                while (rs.next()) {
                    transsum = new Trans();
                    if (aTransBean.getFieldName().length() > 0) {
                        switch (aTransBean.getFieldName()) {
                            case "add_user_detail_id":
                                try {
                                    transsum.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                                } catch (NullPointerException npe) {
                                    transsum.setAddUserDetailId(0);
                                }
                                break;
                            case "transaction_user_detail_id":
                                try {
                                    transsum.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                                } catch (NullPointerException npe) {
                                    transsum.setTransactionUserDetailId(0);
                                }
                                break;
                            case "bill_transactor_id":
                                try {
                                    transsum.setBillTransactorId(rs.getLong("bill_transactor_id"));
                                } catch (NullPointerException npe) {
                                    transsum.setBillTransactorId(0);
                                }
                                break;
                            case "transactor_id":
                                try {
                                    transsum.setTransactorId(rs.getLong("transactor_id"));
                                } catch (NullPointerException npe) {
                                    transsum.setTransactorId(0);
                                }
                                break;
                            case "transaction_date":
                                try {
                                    transsum.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                                } catch (NullPointerException | SQLException npe) {
                                    transsum.setTransactionDate(null);
                                }
                                break;
                            case "store_id":
                                try {
                                    transsum.setStoreId(rs.getInt("store_id"));
                                    Store st = new StoreBean().getStore(transsum.getStoreId());
                                    transsum.setStoreName(st.getStoreName());
                                } catch (NullPointerException npe) {
                                    transsum.setStoreName("");
                                }
                                break;
                        }
                    }
                    try {
                        transsum.setTransactionTypeName(new TransactionTypeBean().getTransactionType(rs.getInt("transaction_type_id")).getTransactionTypeName());
                    } catch (NullPointerException npe) {
                        transsum.setTransactionTypeName("");
                    }
                    try {
                        transsum.setSync_flag(rs.getString("sync_flag"));
                    } catch (NullPointerException npe) {
                        transsum.setSync_flag("");
                    }
                    try {
                        transsum.setCurrencyCode(rs.getString("currency_code"));
                    } catch (NullPointerException npe) {
                        transsum.setCurrencyCode("");
                    }
                    try {
                        transsum.setGrandTotal(rs.getDouble("grand_total"));
                    } catch (NullPointerException npe) {
                        transsum.setGrandTotal(0);
                    }
                    try {
                        transsum.setTotalVat(rs.getDouble("total_vat"));
                    } catch (NullPointerException npe) {
                        transsum.setTotalVat(0);
                    }
                    try {
                        transsum.setCashDiscount(rs.getDouble("cash_discount"));
                    } catch (NullPointerException npe) {
                        transsum.setCashDiscount(0);
                    }
                    this.TransListSummary.add(transsum);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    public void reportOpenBalanceDetail(Trans aTrans, TransBean aTransBean, AccJournal aAccJournal, AccJournalBean aAccJournalBean, TransItemBean aTransItemBean, TransactorBean aTransactorBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        String sql = "SELECT * FROM view_opening_balance_manual WHERE 1=1";
        String wheresql = "";
        String ordersql = "";
        if (aAccJournal.getAccPeriodId() > 0) {
            wheresql = wheresql + " AND acc_period_id=" + aAccJournal.getAccPeriodId();
        }
        if (aTrans.getTransactionRef().length() > 0) {
            wheresql = wheresql + " AND transaction_ref='" + aTrans.getTransactionRef() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactorId() > 0) {
            wheresql = wheresql + " AND transactor_id=" + aTrans.getTransactorId();
        }
        if (aTrans.getTransactionReasonId() > 0) {
            wheresql = wheresql + " AND transaction_reason_id=" + aTrans.getTransactionReasonId();
        }
        if (null != aAccJournal.getAccountCode() && aAccJournal.getAccountCode().length() > 0) {
            wheresql = wheresql + " AND account_code='" + aAccJournal.getAccountCode() + "'";
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Opening Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        sql = sql + wheresql + ordersql;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                //this.setTransFromResultset(trans, rs);
                trans.setTransactionId(rs.getLong("transaction_id"));
                trans.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                try {
                    trans.setTransactorId(rs.getLong("transactor_id"));
                } catch (NullPointerException npe) {
                    trans.setTransactorId(0);
                }
                try {
                    trans.setTransactionTypeId(rs.getInt("transaction_type_id"));
                } catch (NullPointerException npe) {
                    trans.setTransactionTypeId(0);
                }
                try {
                    trans.setTransactionReasonId(rs.getInt("transaction_reason_id"));
                } catch (NullPointerException npe) {
                    trans.setTransactionReasonId(0);
                }
                try {
                    trans.setGrandTotal(rs.getDouble("grand_total"));
                } catch (NullPointerException npe) {
                    trans.setGrandTotal(0);
                }
                try {
                    trans.setTransactionRef(rs.getString("transaction_ref"));
                } catch (NullPointerException npe) {
                    trans.setTransactionRef("");
                }
                try {
                    trans.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                } catch (NullPointerException npe) {
                    trans.setAddUserDetailId(0);
                }
                try {
                    trans.setAddDate(new Date(rs.getTimestamp("add_date").getTime()));
                } catch (NullPointerException npe) {
                    trans.setAddDate(null);
                }
                try {
                    trans.setTransactorName(new TransactorBean().getTransactor(trans.getTransactorId()).getTransactorNames());
                } catch (Exception e) {
                    trans.setTransactorName("");
                }
                try {
                    trans.setTransactionUserDetailName(new UserDetailBean().getUserDetail(trans.getTransactionUserDetailId()).getUserName());
                } catch (Exception e) {
                    trans.setTransactionUserDetailName("");
                }
                try {
                    trans.setTransactionTypeName(new TransactionTypeBean().getTransactionType(trans.getTransactionTypeId()).getTransactionTypeName());
                } catch (Exception e) {
                    trans.setTransactionTypeName("");
                }
                try {
                    trans.setTransactionReasonName(new TransactionReasonBean().getTransactionReason(trans.getTransactionReasonId()).getTransactionReasonName());
                } catch (Exception e) {
                    trans.setTransactionReasonName("");
                }
                try {
                    trans.setAddUserDetailName(new UserDetailBean().getUserDetail(trans.getAddUserDetailId()).getUserName());
                } catch (Exception e) {
                    trans.setAddUserDetailName("");
                }
                try {
                    String AccCode = rs.getString("account_code");
                    String AccName = "";
                    if (null != AccCode && AccCode.length() > 0) {
                        AccName = new AccCoaBean().getAccCoaByCodeOrId(AccCode, 0).getAccountName();
                    }
                    trans.setTransactor_driver(AccName);
                } catch (NullPointerException npe) {
                    trans.setTransactor_driver("");
                }
                try {
                    String ChildAccCode = rs.getString("child_account_code");
                    String ChildAccName = "";
                    if (null != ChildAccCode && ChildAccCode.length() > 0) {
                        ChildAccName = new AccChildAccountBean().getAccChildAccByCode(ChildAccCode).getChildAccountName();
                    }
                    trans.setTransactor_vehicle(ChildAccName);
                } catch (NullPointerException npe) {
                    trans.setTransactor_vehicle("");
                }
                try {
                    trans.setCurrencyCode(rs.getString("currency_code"));
                } catch (NullPointerException npe) {
                    trans.setCurrencyCode("");
                }
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportHireInvoiceDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id IN(65,68)";
        String sqlsum = "";
        if (aTransBean.getFieldName().length() > 0) {
            sqlsum = "SELECT " + aTransBean.getFieldName() + ",currency_code,sum(grand_total) as grand_total,sum(total_profit_margin) as total_profit_margin,sum(total_vat) as total_vat,sum(cash_discount) as cash_discount FROM transaction WHERE transaction_type_id IN(65,68)";
        } else {
            sqlsum = "SELECT currency_code,sum(grand_total) as grand_total,sum(total_profit_margin) as total_profit_margin,sum(total_vat) as total_vat,sum(cash_discount) as cash_discount FROM transaction WHERE transaction_type_id IN(65,68)";
        }
        String wheresql = "";
        String ordersql = "";
        String ordersqlsum = "";
        String groupbysql = "";
        if (aTransBean.getFieldName().length() > 0) {
            groupbysql = " GROUP BY " + aTransBean.getFieldName() + ",currency_code";
        } else {
            groupbysql = " GROUP BY currency_code";
        }
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
        if (aTrans.getBillTransactorId() > 0) {
            wheresql = wheresql + " AND bill_transactor_id=" + aTrans.getBillTransactorId();
        }
        if (aTrans.getTransactorId() > 0) {
            wheresql = wheresql + " AND transactor_id=" + aTrans.getTransactorId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Invoice Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        if (aTransBean.getFieldName().length() > 0) {
            ordersqlsum = " ORDER BY " + aTransBean.getFieldName() + ",currency_code";
        } else {
            ordersqlsum = " ORDER BY currency_code";
        }
        sql = sql + wheresql + ordersql;
        sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }

        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sqlsum);) {
            rs = ps.executeQuery();
            Trans transsum = null;
            while (rs.next()) {
                transsum = new Trans();
                if (aTransBean.getFieldName().length() > 0) {
                    switch (aTransBean.getFieldName()) {
                        case "add_user_detail_id":
                            try {
                                transsum.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setAddUserDetailId(0);
                            }
                            break;
                        case "transaction_user_detail_id":
                            try {
                                transsum.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactionUserDetailId(0);
                            }
                            break;
                        case "bill_transactor_id":
                            try {
                                transsum.setBillTransactorId(rs.getLong("bill_transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setBillTransactorId(0);
                            }
                            break;
                        case "transactor_id":
                            try {
                                transsum.setTransactorId(rs.getLong("transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactorId(0);
                            }
                            break;
                        case "transaction_date":
                            try {
                                transsum.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                            } catch (NullPointerException | SQLException npe) {
                                transsum.setTransactionDate(null);
                            }
                            break;
                    }
                }
                try {
                    transsum.setCurrencyCode(rs.getString("currency_code"));
                } catch (NullPointerException npe) {
                    transsum.setCurrencyCode("");
                }
                try {
                    transsum.setGrandTotal(rs.getDouble("grand_total"));
                } catch (NullPointerException npe) {
                    transsum.setGrandTotal(0);
                }
                try {
                    transsum.setTotalProfitMargin(rs.getDouble("total_profit_margin"));
                } catch (NullPointerException npe) {
                    transsum.setTotalProfitMargin(0);
                }
                try {
                    transsum.setTotalVat(rs.getDouble("total_vat"));
                } catch (NullPointerException npe) {
                    transsum.setTotalVat(0);
                }
                try {
                    transsum.setCashDiscount(rs.getDouble("cash_discount"));
                } catch (NullPointerException npe) {
                    transsum.setCashDiscount(0);
                }
                this.TransListSummary.add(transsum);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportHireUnReturnedDetail(Trans aTrans, Transactor aTransactor, Item aItem, TransBean aTransBean) {
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.Stock_outList = new ArrayList<>();
        String sql = "";
        String wheresql = "";
        String ordersql = "";
        String groupby = "";
        if (aTransBean.getFieldName().equals("transactor_id")) {
            sql = "SELECT so.transactor_id,so.item_id,sum(qty_out) as qty_out FROM stock_out so INNER JOIN transaction t ON so.transaction_id=t.transaction_id";
            groupby = " GROUP BY so.transactor_id,so.item_id";
            ordersql = " ORDER BY so.transactor_id DESC,so.item_id DESC";
        } else if (aTransBean.getFieldName().equals("item_id")) {
            sql = "SELECT so.item_id,sum(qty_out) as qty_out FROM stock_out so INNER JOIN transaction t ON so.transaction_id=t.transaction_id";
            groupby = " GROUP BY so.item_id";
            ordersql = " ORDER BY so.item_id DESC";
        } else {
            sql = "SELECT so.*,t.to_date FROM stock_out so INNER JOIN transaction t ON so.transaction_id=t.transaction_id";
            ordersql = " ORDER BY so.transactor_id DESC,so.transaction_id DESC";
        }

        try {
            if (aTrans.getStoreId() > 0) {
                wheresql = wheresql + " AND so.store_id=" + aTrans.getStoreId();
            }
        } catch (NullPointerException npe) {
        }
        try {
            if (aTransactor.getTransactorId() > 0) {
                wheresql = wheresql + " AND so.transactor_id=" + aTransactor.getTransactorId();
            }
        } catch (NullPointerException npe) {
        }
        try {
            if (aItem.getItemId() > 0) {
                wheresql = wheresql + " AND so.item_id=" + aItem.getItemId();
            }
        } catch (NullPointerException npe) {
        }
        try {
            if (aTrans.getTransactionNumber().length() > 0) {
                Trans trans = new TransBean().getTransByTransNumber(aTrans.getTransactionNumber());
                wheresql = wheresql + " AND so.transaction_id=" + trans.getTransactionId();
            }
        } catch (NullPointerException npe) {
        }
        try {
            if (aTransBean.isGen_flag()) {//get those due/past return date
                Date CurrentServerDate = new CompanySetting().getCURRENT_SERVER_DATE();
                String DateStr = new SimpleDateFormat("yyyy-MM-dd").format(CurrentServerDate);
                wheresql = wheresql + " AND DATEDIFF('" + DateStr + "',t.to_date)>=0";
            } else {
            }
        } catch (NullPointerException npe) {
        }
        sql = sql + wheresql + groupby + ordersql;
        //System.out.println("SQLSQL:" + sql);
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Stock_out so = null;
            Trans TransTemp = null;
            while (rs.next()) {
                so = new Stock_out();
                if (aTransBean.getFieldName().equals("transactor_id")) {
                    try {
                        so.setTransactor_id(rs.getLong("transactor_id"));
                    } catch (NullPointerException npe) {
                        so.setTransactor_id(0);
                    }
                    try {
                        so.setItem_id(rs.getLong("item_id"));
                    } catch (NullPointerException npe) {
                        so.setItem_id(0);
                    }
                    try {
                        so.setQty_out(rs.getDouble("qty_out"));
                    } catch (NullPointerException npe) {
                        so.setQty_out(0);
                    }
                    try {
                        Transactor TransaTemp = new TransactorBean().getTransactor(so.getTransactor_id());
                        so.setTransactor_names(TransaTemp.getTransactorNames());
                    } catch (NullPointerException npe) {
                    }
                    try {
                        Item ItemTemp = new ItemBean().getItem(so.getItem_id());
                        so.setItem_description(ItemTemp.getDescription());
                    } catch (NullPointerException npe) {
                    }
                } else if (aTransBean.getFieldName().equals("item_id")) {
                    try {
                        so.setItem_id(rs.getLong("item_id"));
                    } catch (NullPointerException npe) {
                        so.setItem_id(0);
                    }
                    try {
                        so.setQty_out(rs.getDouble("qty_out"));
                    } catch (NullPointerException npe) {
                        so.setQty_out(0);
                    }
                    try {
                        Item ItemTemp = new ItemBean().getItem(so.getItem_id());
                        so.setItem_description(ItemTemp.getDescription());
                    } catch (NullPointerException npe) {
                    }
                } else {
                    new Stock_outBean().setStock_outFromResultset(so, rs);
                    try {
                        TransTemp = this.getTrans(so.getTransaction_id());
                        so.setFrom_date(TransTemp.getFrom_date());
                        so.setTo_date(TransTemp.getTo_date());
                        so.setTransaction_date(TransTemp.getTransactionDate());
                        so.setTransaction_number(TransTemp.getTransactionNumber());
                    } catch (NullPointerException npe) {
                    }
                    try {
                        Transactor TransaTemp = new TransactorBean().getTransactor(so.getTransactor_id());
                        so.setTransactor_names(TransaTemp.getTransactorNames());
                    } catch (NullPointerException npe) {
                    }
                    try {
                        Site SiteTemp = new SiteBean().getSiteById(so.getSite_id());
                        so.setSite_name(SiteTemp.getSite_name());
                    } catch (NullPointerException npe) {
                    }
                    try {
                        Item ItemTemp = new ItemBean().getItem(so.getItem_id());
                        so.setItem_description(ItemTemp.getDescription());
                    } catch (NullPointerException npe) {
                    }
                    try {
                        so.setDuration_passed(this.getDurationPassedOnReturn(new CompanySetting().getCURRENT_SERVER_DATE(), TransTemp.getFrom_date(), TransTemp.getDuration_value()));
                    } catch (NullPointerException npe) {
                        so.setDuration_passed(0);
                    }
                }
                this.Stock_outList.add(so);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportHireQuotationDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=63";
        String sqlsum = "";
        if (aTransBean.getFieldName().length() > 0) {
            sqlsum = "SELECT " + aTransBean.getFieldName() + ",currency_code,sum(grand_total) as grand_total,sum(total_profit_margin) as total_profit_margin,sum(total_vat) as total_vat,sum(cash_discount) as cash_discount FROM transaction WHERE transaction_type_id=63";
        } else {
            sqlsum = "SELECT currency_code,sum(grand_total) as grand_total,sum(total_profit_margin) as total_profit_margin,sum(total_vat) as total_vat,sum(cash_discount) as cash_discount FROM transaction WHERE transaction_type_id=63";
        }
        String wheresql = "";
        String ordersql = "";
        String ordersqlsum = "";
        String groupbysql = "";
        if (aTransBean.getFieldName().length() > 0) {
            groupbysql = " GROUP BY " + aTransBean.getFieldName() + ",currency_code";
        } else {
            groupbysql = " GROUP BY currency_code";
        }
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
        if (aTrans.getBillTransactorId() > 0) {
            wheresql = wheresql + " AND bill_transactor_id=" + aTrans.getBillTransactorId();
        }
        if (aTrans.getTransactorId() > 0) {
            wheresql = wheresql + " AND transactor_id=" + aTrans.getTransactorId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Quotation Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        if (aTransBean.getFieldName().length() > 0) {
            ordersqlsum = " ORDER BY " + aTransBean.getFieldName() + ",currency_code";
        } else {
            ordersqlsum = " ORDER BY currency_code";
        }
        sql = sql + wheresql + ordersql;
        sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }

        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sqlsum);) {
            rs = ps.executeQuery();
            Trans transsum = null;
            while (rs.next()) {
                transsum = new Trans();
                if (aTransBean.getFieldName().length() > 0) {
                    switch (aTransBean.getFieldName()) {
                        case "add_user_detail_id":
                            try {
                                transsum.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setAddUserDetailId(0);
                            }
                            break;
                        case "transaction_user_detail_id":
                            try {
                                transsum.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactionUserDetailId(0);
                            }
                            break;
                        case "bill_transactor_id":
                            try {
                                transsum.setBillTransactorId(rs.getLong("bill_transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setBillTransactorId(0);
                            }
                            break;
                        case "transactor_id":
                            try {
                                transsum.setTransactorId(rs.getLong("transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactorId(0);
                            }
                            break;
                        case "transaction_date":
                            try {
                                transsum.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                            } catch (NullPointerException | SQLException npe) {
                                transsum.setTransactionDate(null);
                            }
                            break;
                    }
                }
                try {
                    transsum.setCurrencyCode(rs.getString("currency_code"));
                } catch (NullPointerException npe) {
                    transsum.setCurrencyCode("");
                }
                try {
                    transsum.setGrandTotal(rs.getDouble("grand_total"));
                } catch (NullPointerException npe) {
                    transsum.setGrandTotal(0);
                }
                try {
                    transsum.setTotalProfitMargin(rs.getDouble("total_profit_margin"));
                } catch (NullPointerException npe) {
                    transsum.setTotalProfitMargin(0);
                }
                try {
                    transsum.setTotalVat(rs.getDouble("total_vat"));
                } catch (NullPointerException npe) {
                    transsum.setTotalVat(0);
                }
                try {
                    transsum.setCashDiscount(rs.getDouble("cash_discount"));
                } catch (NullPointerException npe) {
                    transsum.setCashDiscount(0);
                }
                this.TransListSummary.add(transsum);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportHireDeliveryNoteDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=66";
        String wheresql = "";
        String ordersql = "";
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
        if (aTrans.getBillTransactorId() > 0) {
            wheresql = wheresql + " AND bill_transactor_id=" + aTrans.getBillTransactorId();
        }
        if (aTrans.getTransactorId() > 0) {
            wheresql = wheresql + " AND transactor_id=" + aTrans.getTransactorId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Delivery Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        sql = sql + wheresql + ordersql;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportHireReturnNoteDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=67";
        String wheresql = "";
        String ordersql = "";
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
        if (aTrans.getBillTransactorId() > 0) {
            wheresql = wheresql + " AND bill_transactor_id=" + aTrans.getBillTransactorId();
        }
        if (aTrans.getTransactorId() > 0) {
            wheresql = wheresql + " AND transactor_id=" + aTrans.getTransactorId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Delivery Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        sql = sql + wheresql + ordersql;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportDisposeStockDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=3";
        String sqlsum = "";
        if (aTransBean.getFieldName().length() > 0) {
            sqlsum = "SELECT " + aTransBean.getFieldName() + ",currency_code,sum(grand_total) as grand_total FROM transaction WHERE transaction_type_id=3";
        } else {
            sqlsum = "SELECT currency_code,sum(grand_total) as grand_total FROM transaction WHERE transaction_type_id=3";
        }
        String wheresql = "";
        String ordersql = "";
        String ordersqlsum = "";
        String groupbysql = "";
        if (aTransBean.getFieldName().length() > 0) {
            groupbysql = " GROUP BY " + aTransBean.getFieldName() + ",currency_code";
        } else {
            groupbysql = " GROUP BY currency_code";
        }
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Dispose Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        if (aTransBean.getFieldName().length() > 0) {
            ordersqlsum = " ORDER BY " + aTransBean.getFieldName() + ",currency_code";
        } else {
            ordersqlsum = " ORDER BY currency_code";
        }
        sql = sql + wheresql + ordersql;
        sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }

        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sqlsum);) {
            rs = ps.executeQuery();
            Trans transsum = null;
            while (rs.next()) {
                transsum = new Trans();
                if (aTransBean.getFieldName().length() > 0) {
                    switch (aTransBean.getFieldName()) {
                        case "add_user_detail_id":
                            try {
                                transsum.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setAddUserDetailId(0);
                            }
                            break;
                        case "transaction_user_detail_id":
                            try {
                                transsum.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactionUserDetailId(0);
                            }
                            break;
                        case "bill_transactor_id":
                            try {
                                transsum.setBillTransactorId(rs.getLong("bill_transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setBillTransactorId(0);
                            }
                            break;
                        case "transactor_id":
                            try {
                                transsum.setTransactorId(rs.getLong("transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactorId(0);
                            }
                            break;
                        case "transaction_date":
                            try {
                                transsum.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                            } catch (NullPointerException | SQLException npe) {
                                transsum.setTransactionDate(null);
                            }
                            break;
                    }
                }
                try {
                    transsum.setCurrencyCode(rs.getString("currency_code"));
                } catch (NullPointerException npe) {
                    transsum.setCurrencyCode("");
                }
                try {
                    transsum.setGrandTotal(rs.getDouble("grand_total"));
                } catch (NullPointerException npe) {
                    transsum.setGrandTotal(0);
                }
//                try {
//                    transsum.setTotalProfitMargin(rs.getDouble("total_profit_margin"));
//                } catch (NullPointerException npe) {
//                    transsum.setTotalProfitMargin(0);
//                }
                this.TransListSummary.add(transsum);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportAdjustStockDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=71";
        String wheresql = "";
        String ordersql = "";
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Adjust Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        sql = sql + wheresql + ordersql;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportConsumeStockDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=72";
        String wheresql = "";
        String ordersql = "";
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Adjust Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        sql = sql + wheresql + ordersql;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportPurchaseInvoiceDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id IN(1,19)";
        String sqlsum = "";
        if (aTransBean.getFieldName().length() > 0) {
            sqlsum = "SELECT " + aTransBean.getFieldName() + ",currency_code,sum(grand_total) as grand_total,sum(total_vat) as total_vat,sum(cash_discount) as cash_discount FROM transaction WHERE transaction_type_id IN(1,19)";
        } else {
            sqlsum = "SELECT currency_code,sum(grand_total) as grand_total,sum(total_vat) as total_vat,sum(cash_discount) as cash_discount FROM transaction WHERE transaction_type_id IN(1,19)";
        }
        String wheresql = "";
        String ordersql = "";
        String ordersqlsum = "";
        String groupbysql = "";
        if (aTransBean.getFieldName().length() > 0) {
            groupbysql = " GROUP BY " + aTransBean.getFieldName() + ",currency_code";
        } else {
            groupbysql = " GROUP BY currency_code";
        }
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
        if (aTrans.getTransactionReasonId() > 0) {
            wheresql = wheresql + " AND transaction_reason_id=" + aTrans.getTransactionReasonId();
        }
        if (aTrans.getTransactorId() > 0) {
            wheresql = wheresql + " AND transactor_id=" + aTrans.getTransactorId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Invoice Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        if (aTransBean.getFieldName().length() > 0) {
            ordersqlsum = " ORDER BY " + aTransBean.getFieldName() + ",currency_code";
        } else {
            ordersqlsum = " ORDER BY currency_code";
        }
        sql = sql + wheresql + ordersql;
        sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }

        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sqlsum);) {
            rs = ps.executeQuery();
            Trans transsum = null;
            while (rs.next()) {
                transsum = new Trans();
                if (aTransBean.getFieldName().length() > 0) {
                    switch (aTransBean.getFieldName()) {
                        case "add_user_detail_id":
                            try {
                                transsum.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setAddUserDetailId(0);
                            }
                            break;
                        case "transaction_user_detail_id":
                            try {
                                transsum.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactionUserDetailId(0);
                            }
                            break;
                        case "transaction_reason_id":
                            try {
                                transsum.setTransactionReasonId(rs.getInt("transaction_reason_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactionReasonId(0);
                            }
                            break;
                        case "transactor_id":
                            try {
                                transsum.setTransactorId(rs.getLong("transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactorId(0);
                            }
                            break;
                        case "transaction_date":
                            try {
                                transsum.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                            } catch (NullPointerException | SQLException npe) {
                                transsum.setTransactionDate(null);
                            }
                            break;
                        case "store_id":
                            try {
                                transsum.setStoreId(rs.getInt("store_id"));
                                Store st = new StoreBean().getStore(transsum.getStoreId());
                                transsum.setStoreName(st.getStoreName());
                            } catch (NullPointerException npe) {
                                transsum.setStoreName("");
                            }
                            break;
                    }
                }
                try {
                    transsum.setCurrencyCode(rs.getString("currency_code"));
                } catch (NullPointerException npe) {
                    transsum.setCurrencyCode("");
                }
                try {
                    transsum.setTotalVat(rs.getDouble("total_vat"));
                } catch (NullPointerException npe) {
                    transsum.setTotalVat(0);
                }
                try {
                    transsum.setCashDiscount(rs.getDouble("cash_discount"));
                } catch (NullPointerException npe) {
                    transsum.setCashDiscount(0);
                }
                try {
                    transsum.setGrandTotal(rs.getDouble("grand_total"));
                } catch (NullPointerException npe) {
                    transsum.setGrandTotal(0);
                }
                this.TransListSummary.add(transsum);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportPurchaseOrderDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=8";
        String sqlsum = "";
        if (aTransBean.getFieldName().length() > 0) {
            sqlsum = "SELECT " + aTransBean.getFieldName() + ",currency_code,sum(grand_total) as grand_total FROM transaction WHERE transaction_type_id=8";
        } else {
            sqlsum = "SELECT currency_code,sum(grand_total) as grand_total FROM transaction WHERE transaction_type_id=8";
        }
        String wheresql = "";
        String ordersql = "";
        String ordersqlsum = "";
        String groupbysql = "";
        if (aTransBean.getFieldName().length() > 0) {
            groupbysql = " GROUP BY " + aTransBean.getFieldName() + ",currency_code";
        } else {
            groupbysql = " GROUP BY currency_code";
        }
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
//        if (aTrans.getBillTransactorId() > 0) {
//            WhereAppend = WhereAppend + " AND bill_transactor_id=" + aTrans.getBillTransactorId();
//        }
        if (aTrans.getTransactorId() > 0) {
            wheresql = wheresql + " AND transactor_id=" + aTrans.getTransactorId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Order Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        if (aTransBean.getFieldName().length() > 0) {
            ordersqlsum = " ORDER BY " + aTransBean.getFieldName() + ",currency_code";
        } else {
            ordersqlsum = " ORDER BY currency_code";
        }
        sql = sql + wheresql + ordersql;
        sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }

        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sqlsum);) {
            rs = ps.executeQuery();
            Trans transsum = null;
            while (rs.next()) {
                transsum = new Trans();
                if (aTransBean.getFieldName().length() > 0) {
                    switch (aTransBean.getFieldName()) {
                        case "add_user_detail_id":
                            try {
                                transsum.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setAddUserDetailId(0);
                            }
                            break;
                        case "transaction_user_detail_id":
                            try {
                                transsum.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactionUserDetailId(0);
                            }
                            break;
                        case "bill_transactor_id":
                            try {
                                transsum.setBillTransactorId(rs.getLong("bill_transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setBillTransactorId(0);
                            }
                            break;
                        case "transactor_id":
                            try {
                                transsum.setTransactorId(rs.getLong("transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactorId(0);
                            }
                            break;
                        case "transaction_date":
                            try {
                                transsum.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                            } catch (NullPointerException | SQLException npe) {
                                transsum.setTransactionDate(null);
                            }
                            break;
                    }
                }
                try {
                    transsum.setCurrencyCode(rs.getString("currency_code"));
                } catch (NullPointerException npe) {
                    transsum.setCurrencyCode("");
                }
                try {
                    transsum.setGrandTotal(rs.getDouble("grand_total"));
                } catch (NullPointerException npe) {
                    transsum.setGrandTotal(0);
                }
                this.TransListSummary.add(transsum);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportTransferRequestDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=13";
        String sqlsum = "";
        if (aTransBean.getFieldName().length() > 0) {
            sqlsum = "SELECT " + aTransBean.getFieldName() + ",count(transaction_id) as grand_total FROM transaction WHERE transaction_type_id=13";
        } else {
            sqlsum = "SELECT count(transaction_id) as grand_total FROM transaction WHERE transaction_type_id=13";
        }
        String wheresql = "";
        String ordersql = "";
        String ordersqlsum = "";
        String groupbysql = "";
        if (aTransBean.getFieldName().length() > 0) {
            groupbysql = " GROUP BY " + aTransBean.getFieldName();
        } else {
            groupbysql = "";
        }
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getStore2Id() > 0) {
            wheresql = wheresql + " AND store2_id=" + aTrans.getStore2Id();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Request Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        if (aTransBean.getFieldName().length() > 0) {
            ordersqlsum = " ORDER BY " + aTransBean.getFieldName();
        } else {
            ordersqlsum = "";
        }
        sql = sql + wheresql + ordersql;
        sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }

        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sqlsum);) {
            rs = ps.executeQuery();
            Trans transsum = null;
            while (rs.next()) {
                transsum = new Trans();
                if (aTransBean.getFieldName().length() > 0) {
                    switch (aTransBean.getFieldName()) {
                        case "add_user_detail_id":
                            try {
                                transsum.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setAddUserDetailId(0);
                            }
                            break;
                        case "transaction_user_detail_id":
                            try {
                                transsum.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactionUserDetailId(0);
                            }
                            break;
                        case "bill_transactor_id":
                            try {
                                transsum.setBillTransactorId(rs.getLong("bill_transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setBillTransactorId(0);
                            }
                            break;
                        case "transactor_id":
                            try {
                                transsum.setTransactorId(rs.getLong("transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactorId(0);
                            }
                            break;
                        case "transaction_date":
                            try {
                                transsum.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                            } catch (NullPointerException | SQLException npe) {
                                transsum.setTransactionDate(null);
                            }
                            break;
                    }
                }
                try {
                    transsum.setGrandTotal(rs.getDouble("grand_total"));
                } catch (NullPointerException npe) {
                    transsum.setGrandTotal(0);
                }
                this.TransListSummary.add(transsum);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportTransferDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=4";
        String sqlsum = "";
        if (aTransBean.getFieldName().length() > 0) {
            sqlsum = "SELECT " + aTransBean.getFieldName() + ",count(transaction_id) as grand_total FROM transaction WHERE transaction_type_id=4";
        } else {
            sqlsum = "SELECT count(transaction_id) as grand_total FROM transaction WHERE transaction_type_id=4";
        }
        String wheresql = "";
        String ordersql = "";
        String ordersqlsum = "";
        String groupbysql = "";
        if (aTransBean.getFieldName().length() > 0) {
            groupbysql = " GROUP BY " + aTransBean.getFieldName();
        } else {
            groupbysql = "";
        }
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getStore2Id() > 0) {
            wheresql = wheresql + " AND store2_id=" + aTrans.getStore2Id();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Transfer Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        if (aTransBean.getFieldName().length() > 0) {
            ordersqlsum = " ORDER BY " + aTransBean.getFieldName();
        } else {
            ordersqlsum = "";
        }
        sql = sql + wheresql + ordersql;
        sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }

        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sqlsum);) {
            rs = ps.executeQuery();
            Trans transsum = null;
            while (rs.next()) {
                transsum = new Trans();
                if (aTransBean.getFieldName().length() > 0) {
                    switch (aTransBean.getFieldName()) {
                        case "add_user_detail_id":
                            try {
                                transsum.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setAddUserDetailId(0);
                            }
                            break;
                        case "transaction_user_detail_id":
                            try {
                                transsum.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactionUserDetailId(0);
                            }
                            break;
                        case "bill_transactor_id":
                            try {
                                transsum.setBillTransactorId(rs.getLong("bill_transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setBillTransactorId(0);
                            }
                            break;
                        case "transactor_id":
                            try {
                                transsum.setTransactorId(rs.getLong("transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactorId(0);
                            }
                            break;
                        case "transaction_date":
                            try {
                                transsum.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                            } catch (NullPointerException | SQLException npe) {
                                transsum.setTransactionDate(null);
                            }
                            break;
                    }
                }
                try {
                    transsum.setGrandTotal(rs.getDouble("grand_total"));
                } catch (NullPointerException npe) {
                    transsum.setGrandTotal(0);
                }
                this.TransListSummary.add(transsum);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportPurchaseItemReceivedDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=9";
        String sqlsum = "";
        if (aTransBean.getFieldName().length() > 0) {
            sqlsum = "SELECT " + aTransBean.getFieldName() + ",count(transaction_id) as grand_total FROM transaction WHERE transaction_type_id=9";
        } else {
            sqlsum = "SELECT count(transaction_id) as grand_total FROM transaction WHERE transaction_type_id=9";
        }
        String wheresql = "";
        String ordersql = "";
        String ordersqlsum = "";
        String groupbysql = "";
        if (aTransBean.getFieldName().length() > 0) {
            groupbysql = " GROUP BY " + aTransBean.getFieldName();
        } else {
            groupbysql = "";
        }
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
//        if (aTrans.getBillTransactorId() > 0) {
//            WhereAppend = WhereAppend + " AND bill_transactor_id=" + aTrans.getBillTransactorId();
//        }
        if (aTrans.getTransactorId() > 0) {
            wheresql = wheresql + " AND transactor_id=" + aTrans.getTransactorId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Received Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        if (aTransBean.getFieldName().length() > 0) {
            ordersqlsum = " ORDER BY " + aTransBean.getFieldName();
        } else {
            ordersqlsum = "";
        }
        sql = sql + wheresql + ordersql;
        sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }

        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sqlsum);) {
            rs = ps.executeQuery();
            Trans transsum = null;
            while (rs.next()) {
                transsum = new Trans();
                if (aTransBean.getFieldName().length() > 0) {
                    switch (aTransBean.getFieldName()) {
                        case "add_user_detail_id":
                            try {
                                transsum.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setAddUserDetailId(0);
                            }
                            break;
                        case "transaction_user_detail_id":
                            try {
                                transsum.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactionUserDetailId(0);
                            }
                            break;
                        case "bill_transactor_id":
                            try {
                                transsum.setBillTransactorId(rs.getLong("bill_transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setBillTransactorId(0);
                            }
                            break;
                        case "transactor_id":
                            try {
                                transsum.setTransactorId(rs.getLong("transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactorId(0);
                            }
                            break;
                        case "transaction_date":
                            try {
                                transsum.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                            } catch (NullPointerException | SQLException npe) {
                                transsum.setTransactionDate(null);
                            }
                            break;
                    }
                }
//                try {
//                    transsum.setCurrencyCode(rs.getString("currency_code"));
//                } catch (NullPointerException npe) {
//                    transsum.setCurrencyCode("");
//                }
                try {
                    transsum.setGrandTotal(rs.getDouble("grand_total"));
                } catch (NullPointerException npe) {
                    transsum.setGrandTotal(0);
                }
                this.TransListSummary.add(transsum);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportSalesQuotationDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=10";
        String sqlsum = "";
        if (aTransBean.getFieldName().length() > 0) {
            sqlsum = "SELECT " + aTransBean.getFieldName() + ",currency_code,sum(grand_total) as grand_total FROM transaction WHERE transaction_type_id=10";
        } else {
            sqlsum = "SELECT currency_code,sum(grand_total) as grand_total FROM transaction WHERE transaction_type_id=10";
        }
        String wheresql = "";
        String ordersql = "";
        String ordersqlsum = "";
        String groupbysql = "";
        if (aTransBean.getFieldName().length() > 0) {
            groupbysql = " GROUP BY " + aTransBean.getFieldName() + ",currency_code";
        } else {
            groupbysql = " GROUP BY currency_code";
        }
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
        if (aTrans.getBillTransactorId() > 0) {
            wheresql = wheresql + " AND bill_transactor_id=" + aTrans.getBillTransactorId();
        }
        if (aTrans.getTransactorId() > 0) {
            wheresql = wheresql + " AND transactor_id=" + aTrans.getTransactorId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Quote Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        if (aTransBean.getFieldName().length() > 0) {
            ordersqlsum = " ORDER BY " + aTransBean.getFieldName() + ",currency_code";
        } else {
            ordersqlsum = " ORDER BY currency_code";
        }
        sql = sql + wheresql + ordersql;
        sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }

        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sqlsum);) {
            rs = ps.executeQuery();
            Trans transsum = null;
            while (rs.next()) {
                transsum = new Trans();
                if (aTransBean.getFieldName().length() > 0) {
                    switch (aTransBean.getFieldName()) {
                        case "add_user_detail_id":
                            try {
                                transsum.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setAddUserDetailId(0);
                            }
                            break;
                        case "transaction_user_detail_id":
                            try {
                                transsum.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactionUserDetailId(0);
                            }
                            break;
                        case "bill_transactor_id":
                            try {
                                transsum.setBillTransactorId(rs.getLong("bill_transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setBillTransactorId(0);
                            }
                            break;
                        case "transactor_id":
                            try {
                                transsum.setTransactorId(rs.getLong("transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactorId(0);
                            }
                            break;
                        case "transaction_date":
                            try {
                                transsum.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                            } catch (NullPointerException | SQLException npe) {
                                transsum.setTransactionDate(null);
                            }
                            break;
                    }
                }
                try {
                    transsum.setCurrencyCode(rs.getString("currency_code"));
                } catch (NullPointerException npe) {
                    transsum.setCurrencyCode("");
                }
                try {
                    transsum.setGrandTotal(rs.getDouble("grand_total"));
                } catch (NullPointerException npe) {
                    transsum.setGrandTotal(0);
                }
                this.TransListSummary.add(transsum);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportSalesOrderDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        this.TransListSummary = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=11";
        String sqlsum = "";
        if (aTransBean.getFieldName().length() > 0) {
            sqlsum = "SELECT " + aTransBean.getFieldName() + ",currency_code,sum(grand_total) as grand_total FROM transaction WHERE transaction_type_id=11";
        } else {
            sqlsum = "SELECT currency_code,sum(grand_total) as grand_total FROM transaction WHERE transaction_type_id=11";
        }
        String wheresql = "";
        String ordersql = "";
        String ordersqlsum = "";
        String groupbysql = "";
        if (aTransBean.getFieldName().length() > 0) {
            groupbysql = " GROUP BY " + aTransBean.getFieldName() + ",currency_code";
        } else {
            groupbysql = " GROUP BY currency_code";
        }
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
        if (aTrans.getBillTransactorId() > 0) {
            wheresql = wheresql + " AND bill_transactor_id=" + aTrans.getBillTransactorId();
        }
        if (aTrans.getTransactorId() > 0) {
            wheresql = wheresql + " AND transactor_id=" + aTrans.getTransactorId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Order Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        if (aTransBean.getFieldName().length() > 0) {
            ordersqlsum = " ORDER BY " + aTransBean.getFieldName() + ",currency_code";
        } else {
            ordersqlsum = " ORDER BY currency_code";
        }
        sql = sql + wheresql + ordersql;
        sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }

        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sqlsum);) {
            rs = ps.executeQuery();
            Trans transsum = null;
            while (rs.next()) {
                transsum = new Trans();
                if (aTransBean.getFieldName().length() > 0) {
                    switch (aTransBean.getFieldName()) {
                        case "add_user_detail_id":
                            try {
                                transsum.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setAddUserDetailId(0);
                            }
                            break;
                        case "transaction_user_detail_id":
                            try {
                                transsum.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactionUserDetailId(0);
                            }
                            break;
                        case "bill_transactor_id":
                            try {
                                transsum.setBillTransactorId(rs.getLong("bill_transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setBillTransactorId(0);
                            }
                            break;
                        case "transactor_id":
                            try {
                                transsum.setTransactorId(rs.getLong("transactor_id"));
                            } catch (NullPointerException npe) {
                                transsum.setTransactorId(0);
                            }
                            break;
                        case "transaction_date":
                            try {
                                transsum.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                            } catch (NullPointerException | SQLException npe) {
                                transsum.setTransactionDate(null);
                            }
                            break;
                        case "delivery_mode":
                            try {
                                transsum.setDelivery_mode(rs.getString("delivery_mode"));
                            } catch (NullPointerException npe) {
                                transsum.setDelivery_mode("");
                            }
                            break;
                        case "store_id":
                            try {
                                transsum.setStoreId(rs.getInt("store_id"));
                                Store st = new StoreBean().getStore(transsum.getStoreId());
                                transsum.setStoreName(st.getStoreName());
                            } catch (NullPointerException npe) {
                                transsum.setStoreName("");
                            }
                            break;
                    }
                }
                try {
                    transsum.setCurrencyCode(rs.getString("currency_code"));
                } catch (NullPointerException npe) {
                    transsum.setCurrencyCode("");
                }
                try {
                    transsum.setGrandTotal(rs.getDouble("grand_total"));
                } catch (NullPointerException npe) {
                    transsum.setGrandTotal(0);
                }
                this.TransListSummary.add(transsum);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportSalesDeliveryDetail(Trans aTrans, TransBean aTransBean) {
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        aTransBean.setActionMessage("");
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=12";
        String wheresql = "";
        String ordersql = "";
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
        }
        if (aTrans.getTransactionNumber().length() > 0) {
            wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
        }
        if (aTrans.getTransactionRef().length() > 0) {
            wheresql = wheresql + " AND transaction_ref='" + aTrans.getTransactionRef() + "'";
        }
        if (aTrans.getAddUserDetailId() > 0) {
            wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
        }
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
        if (aTrans.getBillTransactorId() > 0) {
            wheresql = wheresql + " AND bill_transactor_id=" + aTrans.getBillTransactorId();
        }
        if (aTrans.getTransactorId() > 0) {
            wheresql = wheresql + " AND transactor_id=" + aTrans.getTransactorId();
        }
        if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
            switch (aTransBean.getDateType()) {
                case "Delivery Date":
                    wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                    break;
                case "Add Date":
                    wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                    break;
            }
        }
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        sql = sql + wheresql + ordersql;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void reportSalesInvoiceDetail_Old(Trans aTrans, String aFieldName, String aDateType, Date aDate1, Date aDate2) {
        if (aDateType.length() == 0) {
            this.TransList = new ArrayList<>();
            this.TransListSummary = new ArrayList<>();
            this.ActionMessage = "Select Date Type...";
        } else {
            ResultSet rs = null;
            this.TransList = new ArrayList<>();
            this.TransListSummary = new ArrayList<>();
            String sql = "SELECT * FROM transaction WHERE transaction_type_id=2";
            String sqlsum = "";
            if (aFieldName.length() > 0) {
                sqlsum = "SELECT " + aFieldName + ",currency_code,sum(grand_total) as grand_total,sum(total_profit_margin) as total_profit_margin FROM transaction WHERE transaction_type_id=2";
            } else {
                sqlsum = "SELECT currency_code,sum(grand_total) as grand_total,sum(total_profit_margin) as total_profit_margin FROM transaction WHERE transaction_type_id=2";
            }
            String wheresql = "";
            String ordersql = "";
            String ordersqlsum = "";
            String groupbysql = "";
            if (aFieldName.length() > 0) {
                groupbysql = " GROUP BY " + aFieldName + ",currency_code";
            } else {
                groupbysql = " GROUP BY currency_code";
            }
            if (aTrans.getStoreId() > 0) {
                wheresql = wheresql + " AND store_id=" + aTrans.getStoreId();
            }
            if (aTrans.getTransactionNumber().length() > 0) {
                wheresql = wheresql + " AND transaction_number='" + aTrans.getTransactionNumber() + "'";
            }
            if (aTrans.getAddUserDetailId() > 0) {
                wheresql = wheresql + " AND add_user_detail_id=" + aTrans.getAddUserDetailId();
            }
            if (aTrans.getTransactionUserDetailId() > 0) {
                wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
            }
            if (aTrans.getBillTransactorId() > 0) {
                wheresql = wheresql + " AND bill_transactor_id=" + aTrans.getBillTransactorId();
            }
            if (aTrans.getTransactorId() > 0) {
                wheresql = wheresql + " AND transactor_id=" + aTrans.getTransactorId();
            }
            if (aDateType.length() > 0 && aDate1 != null && aDate2 != null) {
                switch (aDateType) {
                    case "Invoice Date":
                        wheresql = wheresql + " AND transaction_date BETWEEN '" + new java.sql.Date(aDate1.getTime()) + "' AND '" + new java.sql.Date(aDate2.getTime()) + "'";
                        break;
                    case "Add Date":
                        wheresql = wheresql + " AND add_date BETWEEN '" + new java.sql.Timestamp(aDate1.getTime()) + "' AND '" + new java.sql.Timestamp(aDate2.getTime()) + "'";
                        break;
                }
            }
            ordersql = " ORDER BY add_date DESC,transaction_id DESC";
            if (aFieldName.length() > 0) {
                ordersqlsum = " ORDER BY " + aFieldName + ",currency_code";
            } else {
                ordersqlsum = " ORDER BY currency_code";
            }
            sql = sql + wheresql + ordersql;
            sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                rs = ps.executeQuery();
                Trans trans = null;
                while (rs.next()) {
                    trans = new Trans();
                    this.setTransFromResultset(trans, rs);
                    this.TransList.add(trans);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }

            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sqlsum);) {
                rs = ps.executeQuery();
                Trans transsum = null;
                while (rs.next()) {
                    transsum = new Trans();
                    if (aFieldName.length() > 0) {
                        switch (aFieldName) {
                            case "add_user_detail_id":
                                try {
                                    transsum.setAddUserDetailId(rs.getInt("add_user_detail_id"));
                                } catch (NullPointerException npe) {
                                    transsum.setAddUserDetailId(0);
                                }
                                break;
                            case "transaction_user_detail_id":
                                try {
                                    transsum.setTransactionUserDetailId(rs.getInt("transaction_user_detail_id"));
                                } catch (NullPointerException npe) {
                                    transsum.setTransactionUserDetailId(0);
                                }
                                break;
                            case "bill_transactor_id":
                                try {
                                    transsum.setBillTransactorId(rs.getLong("bill_transactor_id"));
                                } catch (NullPointerException npe) {
                                    transsum.setBillTransactorId(0);
                                }
                                break;
                            case "transactor_id":
                                try {
                                    transsum.setTransactorId(rs.getLong("transactor_id"));
                                } catch (NullPointerException npe) {
                                    transsum.setTransactorId(0);
                                }
                                break;
                            case "transaction_date":
                                try {
                                    transsum.setTransactionDate(new Date(rs.getDate("transaction_date").getTime()));
                                } catch (NullPointerException | SQLException npe) {
                                    transsum.setTransactionDate(null);
                                }
                                break;
                        }
                    }
                    try {
                        transsum.setCurrencyCode(rs.getString("currency_code"));
                    } catch (NullPointerException npe) {
                        transsum.setCurrencyCode("");
                    }
                    try {
                        transsum.setGrandTotal(rs.getDouble("grand_total"));
                    } catch (NullPointerException npe) {
                        transsum.setGrandTotal(0);
                    }
                    try {
                        transsum.setTotalProfitMargin(rs.getDouble("total_profit_margin"));
                    } catch (NullPointerException npe) {
                        transsum.setTotalProfitMargin(0);
                    }
                    this.TransListSummary.add(transsum);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    public void resetSalesInvoiceDetail(Trans aTrans, TransBean aTransBean, Transactor aBillTransactor, Transactor aTransactor) {
        aTransBean.setActionMessage("");
        try {
            this.clearTrans(aTrans);
            aTrans.setTransactionTypeId(0);
            aTrans.setTransactionReasonId(0);
        } catch (NullPointerException npe) {
        }
        try {
            new TransactorBean().clearTransactor(aBillTransactor);
        } catch (NullPointerException npe) {
        }
        try {
            new TransactorBean().clearTransactor(aTransactor);
        } catch (NullPointerException npe) {
        }
        try {
            aTransBean.setDateType("");
            aTransBean.setDate1(null);
            aTransBean.setDate2(null);
            aTransBean.setFieldName("");
            aTransBean.TransList.clear();
            aTransBean.TransListSummary.clear();
        } catch (NullPointerException npe) {
        }
    }

    public void resetReportOpenBalance(Trans aTrans, TransBean aTransBean, AccJournal aAccJournal, AccJournalBean aAccJournalBean, TransItemBean aTransItemBean, TransactorBean aTransactorBean) {
        aTransBean.setActionMessage("");
        try {
            this.clearTrans(aTrans);
            aTrans.setTransactionTypeId(0);
            aTrans.setTransactionReasonId(0);
        } catch (NullPointerException npe) {
        }
        try {
            new TransactorBean().clearTransactor(aTransactorBean.getSelectedTransactor());
        } catch (NullPointerException npe) {
        }
        try {
            new AccCoaBean().clearAccCoa(aTransItemBean.getSelectedAccCoa());
        } catch (NullPointerException npe) {
        }
        try {
            aTransBean.setDateType("");
            aTransBean.setDate1(null);
            aTransBean.setDate2(null);
            aTransBean.setFieldName("Manual");
            aTransBean.TransList.clear();
            aTransBean.TransListSummary.clear();
        } catch (NullPointerException npe) {
        }
    }

    public void resetHireUnReturnedItemsDetail(Trans aTrans, Transactor aTransactor, Item aItem, TransBean aTransBean) {
        aTransBean.setActionMessage("");
        aTransBean.setFieldName("");
        try {
            this.clearTrans(aTrans);
        } catch (NullPointerException npe) {
        }
        try {
            new TransactorBean().clearTransactor(aTransactor);
        } catch (NullPointerException npe) {
        }
        try {
            new ItemBean().clearItem(aItem);
        } catch (NullPointerException npe) {
        }
        try {
            aTransBean.setGen_flag(false);
            aTransBean.Stock_outList.clear();
            //aTransBean.TransListSummary.clear();
        } catch (NullPointerException npe) {
        }
    }

    public void resetTransItemDetail(Trans aTrans, TransBean aTransBean, Item aItem, Transactor aBillTransactor, int aCategoryId) {
        aTransBean.setActionMessage("");
        try {
            this.clearTrans(aTrans);
            aTrans.setTransactionTypeId(0);
        } catch (NullPointerException npe) {
        }
        try {
            new TransactorBean().clearTransactor(aBillTransactor);
        } catch (NullPointerException npe) {
        }
        try {
            new ItemBean().clearItem(aItem);
        } catch (NullPointerException npe) {
        }
        try {
            aTransBean.setDateType("");
            aTransBean.setDate1(null);
            aTransBean.setDate2(null);
            aTransBean.setFieldName("");
            aTransBean.TransItemList.clear();
            aTransBean.TransItemSummary.clear();
            aCategoryId = 0;

        } catch (NullPointerException npe) {
        }
    }

    public void doTransAll(List<TransItem> aTransItems, boolean aTransAll, Trans aTrans) {
        int ListItemIndex = 0;
        int ListItemNo = 0;
        try {
            ListItemNo = aTransItems.size();
        } catch (NullPointerException npe) {
            ListItemNo = 0;
        }
        while (ListItemIndex < ListItemNo) {
            if (aTransAll) {
                aTransItems.get(ListItemIndex).setItemQty(aTransItems.get(ListItemIndex).getQty_balance());
            } else {
                aTransItems.get(ListItemIndex).setItemQty(0);
            }
            ListItemIndex = ListItemIndex + 1;
        }
        //refresh total weight
        //refreshTransTotalWeight
        new TransItemBean().refreshTransTotalWeight(aTrans, aTransItems);
    }

    public void initResetSalesInvoiceDetail(Trans aTrans, TransBean aTransBean, Transactor aBillTransactor, Transactor aTransactor) {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            this.resetSalesInvoiceDetail(aTrans, aTransBean, aBillTransactor, aTransactor);
        }
    }

    public void initResetHireUnReturnedItemsDetail(Trans aTrans, Transactor aTransactor, Item aItem, TransBean aTransBean) {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {

            this.resetHireUnReturnedItemsDetail(aTrans, aTransactor, aItem, aTransBean);
        }
    }

    public void initResetTransItemDetail(Trans aTrans, TransBean aTransBean, Item aItem, Transactor aBillTransactor, int aCategoryId) {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
        } else {
            this.resetTransItemDetail(aTrans, aTransBean, aItem, aBillTransactor, aCategoryId);
        }
    }

    public void initSalesInvoiceSession(long aTransId, String aAction) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        String msg = "";
        this.setActionMessage("");
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        this.TransObj = new TransBean().getTrans(aTransId);
        List<Trans> aList = new ArrayList<>();
        if (this.TransObj.getTransactionTypeId() == 2) {
            aList = new CreditDebitNoteBean().getTrans_cr_dr_notes(this.TransObj.getTransactionNumber(), 0);
        }
        if (aAction.equals("Edit") && !aList.isEmpty()) {
            this.ActionType = "None";
            msg = "Transaction Has Credit or Debit Note";
            this.setActionMessage(ub.translateWordsInText(BaseName, msg));
        } else {
            //first set current selection in session
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
            HttpSession httpSession = request.getSession(true);
            httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransId);
            httpSession.setAttribute("CURRENT_TRANSACTION_ACTION", aAction);
            httpSession.setAttribute("CURRENT_PAY_ID", 0);
            this.ActionType = aAction;
            //this.TransObj = new TransBean().getTrans(aTransId);
            this.updateLookup(this.TransObj);
            this.TransItemList = new TransItemBean().getTransItemsByTransactionId(aTransId);
            try {
                this.PayObj = new PayBean().getTransactionFirstPayByTransNo(TransObj.getTransactionNumber());//first payment
                httpSession.setAttribute("CURRENT_PAY_ID", this.PayObj.getPayId());
            } catch (NullPointerException npe) {
                this.PayObj = null;
            }
            //refresh output
            new OutputDetailBean().refreshOutput("PARENT", "");
            //refresh history
            this.TransListHist = new ReportBean().getTransHistory(aTransId);
        }
    }

    public void initCheckCrDrNoteSession(String aTransNo, long aTransId, String aAction) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        String msg = "";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        List<Trans> aList = new CreditDebitNoteBean().getTrans_cr_dr_notes(aTransNo, 0);
        if (aList.isEmpty()) {
            this.ActionType = aAction;
            httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
            httpSession.setAttribute("CURRENT_TRANSACTION_ACTION", aAction);
            httpSession.setAttribute("CURRENT_PAY_ID", 0);
            this.TransObj = new TransBean().getTrans(aTransId);
            this.updateLookup(this.TransObj);
            this.TransItemList = new TransItemBean().getTransItemsByTransactionId(aTransId);
            try {
                this.PayObj = new PayBean().getTransactionFirstPayByTransNo(TransObj.getTransactionNumber());//first payment
                httpSession.setAttribute("CURRENT_PAY_ID", this.PayObj.getPayId());
            } catch (NullPointerException npe) {
                this.PayObj = null;
            }
            //refresh output
            new OutputDetailBean().refreshOutputCrDr("PARENT", "");
            this.setActionMessage("");
        } else if (aList.size() == 1) {
            this.ActionType = "None";
            Trans CrDrNote = aList.get(0);
            if ((CrDrNote.getTransactionTypeId() == 82 && aAction.equals("Cr")) || (CrDrNote.getTransactionTypeId() == 83 && aAction.equals("Dr"))) {
                httpSession.setAttribute("CURRENT_TRANSACTION_ID", CrDrNote.getTransactionId());
                httpSession.setAttribute("CURRENT_TRANSACTION_ACTION", "None");//aAction
                httpSession.setAttribute("CURRENT_PAY_ID", 0);
                this.clearTrans(this.TransObj);
                try {
                    this.TransItemList.clear();
                } catch (NullPointerException npe) {
                }
                try {
                    new PayBean().clearPay(this.PayObj);
                } catch (NullPointerException npe) {
                }
                //refresh output
                new OutputDetailBean().refreshOutputCrDr("PARENT", "");
                this.setActionMessage("");
            } else if (CrDrNote.getTransactionTypeId() == 82 && aAction.equals("Dr")) {
                msg = "Transaction Has Credit Note //" + CrDrNote.getTransactionNumber();
                //FacesContext.getCurrentInstance().addMessage("Note", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
                this.setActionMessage(ub.translateWordsInText(BaseName, msg));
            } else if (CrDrNote.getTransactionTypeId() == 83 && aAction.equals("Cr")) {
                msg = "Transaction Has Debit Note //" + CrDrNote.getTransactionNumber();
                //FacesContext.getCurrentInstance().addMessage("Note", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
                this.setActionMessage(ub.translateWordsInText(BaseName, msg));
            }
        } else {
            this.ActionType = "None";
            httpSession.setAttribute("CURRENT_TRANSACTION_ID", 0);
            httpSession.setAttribute("CURRENT_TRANSACTION_ACTION", "None");//aAction
            httpSession.setAttribute("CURRENT_PAY_ID", 0);
            this.clearTrans(this.TransObj);
            try {
                this.TransItemList.clear();
            } catch (NullPointerException npe) {
            }
            try {
                new PayBean().clearPay(this.PayObj);
            } catch (NullPointerException npe) {
            }
            //refresh output
            new OutputDetailBean().refreshOutputCrDr("PARENT", "");
            //msg
            msg = "Transaction Has More Than One Credit or Debit Note";
            //FacesContext.getCurrentInstance().addMessage("Note", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
            this.setActionMessage(ub.translateWordsInText(BaseName, msg));
        }
    }

    public void initCreditDebitNoteSession(String aHasTransId, String aAction) {
        try {
            long TransId = Long.parseLong(aHasTransId.split(",")[0]);
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
            HttpSession httpSession = request.getSession(true);
            httpSession.setAttribute("CURRENT_TRANSACTION_ID", TransId);
            httpSession.setAttribute("CURRENT_TRANSACTION_ACTION", aAction);
            httpSession.setAttribute("CURRENT_PAY_ID", 0);
            this.ActionType = aAction;
            this.TransObj = new CreditDebitNoteBean().getTrans_cr_dr_note(TransId);
            this.TransItemList = new CreditDebitNoteBean().getTransItemsByTransactionId_cr_dr_note(TransId);
            this.PayObj = null;
            //refresh output
            new OutputDetailBean().refreshOutputCrDr("PARENT", "");
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void initHireReturnInvoiceSession() {
        System.out.println("INITED-initHireReturnInvoiceSession");
        //first set current selection in session
//        FacesContext context = FacesContext.getCurrentInstance();
//        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
//        HttpSession httpSession = request.getSession(true);
//        httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransId);
//        httpSession.setAttribute("CURRENT_TRANSACTION_ACTION", aAction);
//        httpSession.setAttribute("CURRENT_PAY_ID", 0);
//        this.ActionType = aAction;
//        this.TransObj = new TransBean().getTrans(aTransId);
//        this.TransItemList = new TransItemBean().getTransItemsByTransactionId(aTransId);
//        try {
//            this.PayObj = new PayBean().getTransactionFirstPayByTransNo(TransObj.getTransactionNumber());//first payment
//        } catch (NullPointerException npe) {
//            this.PayObj = null;
//        }
    }

    public void initSalesInvoiceSession(long aTransId) {
        Trans trans = new Trans();
        Pay pay = new Pay();
        trans = new TransBean().getTrans(aTransId);
        try {
            pay = new PayBean().getTransactionFirstPayByTransNo(trans.getTransactionNumber());//first payment
        } catch (NullPointerException npe) {
            pay = null;
        }
        //set session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransId);
        try {
            httpSession.setAttribute("CURRENT_PAY_ID", pay.getPayId());
        } catch (NullPointerException npe) {
            httpSession.setAttribute("CURRENT_PAY_ID", 0);
        }
    }

    public void initSalesInvoiceSession_old(long aTransId, String aAction, Trans aTrans) {
        //first set current selection in session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute("CURRENT_TRANSACTION_ID", aTransId);
        httpSession.setAttribute("CURRENT_TRANSACTION_ACTION", aAction);
        httpSession.setAttribute("CURRENT_PAY_ID", 0);
        //httpSession.setAttribute("IS_INITED", 0);
        System.out.println("..." + aTransId);
        System.out.println("..." + new GeneralUserSetting().getCurrentTransactionId());
        aTrans = new TransBean().getTrans(new GeneralUserSetting().getCurrentTransactionId());
        System.out.println("..." + aTrans.getGrandTotal());
    }

    public void initSalesInvoiceDetail(Trans t, List<TransItem> aActiveTransItems, TransItem ti, Item aSelectedItem, Transactor aSelectedTransactor, int ClearNo, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor) {//Clear No: 0-do not clear, 1 - clear trans item only, 2 - clear all  
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            // Skip ajax requests.
            //System.out.println("---In Faces---");
        } else {
            //System.out.println("---NOT In Faces---");
            //System.out.println("---" + new GeneralUserSetting().getCurrentTransactionId());
            t = new TransBean().getTrans(new GeneralUserSetting().getCurrentTransactionId());
            //System.out.println("---" + t.getTransactionId());
        }
    }

    public boolean updateTransactionTable(Trans aNewTrans) {
        boolean isTransUpdateSuccess = false;
        String newSQL = "{call sp_update_transaction2(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                CallableStatement cs = conn.prepareCall(newSQL);) {
            cs.setLong("in_transaction_id", aNewTrans.getTransactionId());
            cs.setDouble("in_cash_discount", aNewTrans.getCashDiscount());
            cs.setDouble("in_total_vat", aNewTrans.getTotalVat());
            cs.setInt("in_edit_user_detail_id", new GeneralUserSetting().getCurrentUser().getUserDetailId());
            cs.setDouble("in_sub_total", aNewTrans.getSubTotal());
            cs.setDouble("in_grand_total", aNewTrans.getGrandTotal());
            cs.setDouble("in_total_trade_discount", aNewTrans.getTotalTradeDiscount());
            cs.setDouble("in_points_awarded", aNewTrans.getPointsAwarded());
            cs.setString("in_card_number", aNewTrans.getCardNumber());
            cs.setDouble("in_total_std_vatable_amount", aNewTrans.getTotalStdVatableAmount());
            cs.setDouble("in_total_zero_vatable_amount", aNewTrans.getTotalZeroVatableAmount());
            cs.setDouble("in_total_exempt_vatable_amount", aNewTrans.getTotalExemptVatableAmount());
            cs.setDouble("in_amount_tendered", aNewTrans.getAmountTendered());
            cs.setDouble("in_change_amount", aNewTrans.getChangeAmount());
            cs.setDouble("in_total_profit_margin", aNewTrans.getTotalProfitMargin());
            cs.setDouble("in_spent_points_amount", aNewTrans.getSpendPointsAmount());
            cs.executeUpdate();
            isTransUpdateSuccess = true;
        } catch (Exception e) {
            isTransUpdateSuccess = false;
            LOGGER.log(Level.ERROR, e);
        }
        return isTransUpdateSuccess;
    }

    public void reportTransItemDetail_old(Trans aTrans, TransBean aTransBean, Item aItem, Transactor aTransactor, int aCategoryId) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        aTransBean.setActionMessage("");
        try {
            if (aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
                //okay no problem
            } else {
                msg = "Select Date Range";
            }
        } catch (Exception e) {
            //do nothing
        }
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        if (msg.length() > 0) {
            aTransBean.setActionMessage(ub.translateWordsInText(BaseName, msg));
            FacesContext.getCurrentInstance().addMessage("Report", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else {
            ResultSet rs = null;
            ResultSet rssum = null;
            ResultSet rssum2 = null;
            this.TransItemList = new ArrayList<>();
            String sql = "SELECT ti.*,t.transaction_number,t.transaction_type_id,t.store_id,i.description,i.unit_id,i.category_id,t.add_date,t.add_user_detail_id FROM transaction_item ti "
                    + "INNER JOIN transaction t ON ti.transaction_id=t.transaction_id "
                    + "INNER JOIN item i ON ti.item_id=i.item_id WHERE 1=1";
            String wheresql = "";
            String ordersql = "";
            if (aCategoryId > 0) {
                wheresql = wheresql + " AND i.category_id=" + aCategoryId;
            }
            if (aTrans.getStoreId() > 0) {
                wheresql = wheresql + " AND t.store_id=" + aTrans.getStoreId();
            }
            if (aTrans.getTransactionNumber().length() > 0) {
                wheresql = wheresql + " AND t.transaction_number='" + aTrans.getTransactionNumber() + "'";
            }
            if (aTrans.getTransactionTypeId() > 0) {
                wheresql = wheresql + " AND t.transaction_type_id=" + aTrans.getTransactionTypeId();
            }
            if (aTrans.getAddUserDetailId() > 0) {
                wheresql = wheresql + " AND t.add_user_detail_id=" + aTrans.getAddUserDetailId();
            }
            if (aTrans.getTransactionUserDetailId() > 0) {
                wheresql = wheresql + " AND t.transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
            }
            try {
                if (null != aTransactor && aTransactor.getTransactorId() > 0) {
                    wheresql = wheresql + " AND t.transactor_id=" + aTransactor.getTransactorId();
                }
            } catch (NullPointerException npe) {

            }
            try {
                if (null != aItem && aItem.getItemId() > 0) {
                    wheresql = wheresql + " AND ti.item_id=" + aItem.getItemId();
                }
            } catch (NullPointerException npe) {

            }
            if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
                switch (aTransBean.getDateType()) {
                    case "Trans Date":
                        wheresql = wheresql + " AND t.transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                        break;
                    case "Add Date":
                        wheresql = wheresql + " AND t.add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                        break;
                }
            }
            ordersql = " ORDER BY t.add_date DESC,t.transaction_id DESC";
            sql = sql + wheresql + ordersql;
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                rs = ps.executeQuery();
                TransItem transitem = null;
                TransItemBean tib = new TransItemBean();
                while (rs.next()) {
                    transitem = new TransItem();
                    tib.setTransItemFromResultSet(transitem, rs);
                    try {
                        transitem.setStoreName(new StoreBean().getStore(rs.getInt("store_id")).getStoreName());
                    } catch (Exception e) {
                        transitem.setStoreName("");
                    }
                    try {
                        transitem.setTransactionTypeName(new TransactionTypeBean().getTransactionType(rs.getInt("transaction_type_id")).getTransactionTypeName());
                    } catch (Exception e) {
                        transitem.setTransactionTypeName("");
                    }
                    Trans t = new Trans();
                    try {
                        t = this.getTrans(rs.getLong("transaction_id"));
                    } catch (Exception e) {
                        //do nothing
                    }
                    try {
                        transitem.setTransactorNames(new TransactorBean().getTransactor(t.getTransactorId()).getTransactorNames());
                    } catch (Exception e) {
                        transitem.setTransactorNames("");
                    }
                    try {
                        transitem.setDescription(rs.getString("description"));
                    } catch (Exception e) {
                        transitem.setDescription("");
                    }
                    try {
                        transitem.setUnitSymbol(new UnitBean().getUnit(rs.getInt("unit_id")).getUnitSymbol());
                    } catch (Exception e) {
                        transitem.setUnitSymbol("");
                    }
                    try {
                        transitem.setCurrency_code(t.getCurrencyCode());
                    } catch (Exception e) {
                        transitem.setCurrency_code("");
                    }
                    try {
                        transitem.setAddUserDetailName(new UserDetailBean().getUserDetail(t.getAddUserDetailId()).getUserName());
                    } catch (Exception e) {
                        transitem.setStoreName("");
                    }
                    try {
                        transitem.setTransaction_number(t.getTransactionNumber());
                    } catch (Exception e) {
                        transitem.setTransaction_number("");
                    }
                    try {
                        transitem.setAddDate(new Date(rs.getTimestamp("add_date").getTime()));
                    } catch (Exception e) {
                        transitem.setAddDate(null);
                    }
                    this.TransItemList.add(transitem);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }

            //summary-category
            this.TransItemSummary = new ArrayList<>();
            String sqlsum = "";
            sqlsum = "SELECT category_id,t.transaction_type_id,t.currency_code,sum(amount_inc_vat) as amount_inc_vat FROM transaction_item ti "
                    + "INNER JOIN transaction t ON ti.transaction_id=t.transaction_id "
                    + "INNER JOIN item i ON ti.item_id=i.item_id WHERE 1=1";
            String ordersqlsum = "";
            String groupbysql = "";
            groupbysql = " GROUP BY category_id,transaction_type_id,currency_code";
            ordersqlsum = " ORDER BY category_id,transaction_type_id,currency_code";
            sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sqlsum);) {
                rssum = ps.executeQuery();
                TransItem transitemsum = null;
                while (rssum.next()) {
                    transitemsum = new TransItem();
                    try {
                        transitemsum.setCategoryName(new CategoryBean().getCategory(rssum.getInt("category_id")).getCategoryName());

                    } catch (Exception e) {
                        transitemsum.setCategoryName("");
                    }
                    try {
                        transitemsum.setCurrency_code(rssum.getString("currency_code"));
                    } catch (Exception e) {
                        transitemsum.setCurrency_code("");
                    }
                    try {
                        transitemsum.setAmountIncVat(rssum.getDouble("amount_inc_vat"));
                    } catch (NullPointerException npe) {
                        transitemsum.setAmountIncVat(0);
                    }
                    try {
                        transitemsum.setTransactionTypeName(new TransactionTypeBean().getTransactionType(rssum.getInt("transaction_type_id")).getTransactionTypeName());
                    } catch (NullPointerException npe) {
                        transitemsum.setTransactionTypeName("");
                    }
                    this.TransItemSummary.add(transitemsum);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }

            //summary-item
            this.TransItemSummary2 = new ArrayList<>();
            String sqlsum2 = "";
            sqlsum2 = "SELECT description,t.transaction_type_id,t.currency_code,sum(amount_inc_vat) as amount_inc_vat,sum(item_qty) as qty_sum FROM transaction_item ti "
                    + "INNER JOIN transaction t ON ti.transaction_id=t.transaction_id "
                    + "INNER JOIN item i ON ti.item_id=i.item_id WHERE 1=1";
            String ordersqlsum2 = "";
            String groupbysql2 = "";
            groupbysql2 = " GROUP BY description,transaction_type_id,currency_code";
            ordersqlsum2 = " ORDER BY description,transaction_type_id,currency_code";
            sqlsum2 = sqlsum2 + wheresql + groupbysql2 + ordersqlsum2;
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sqlsum2);) {
                rssum2 = ps.executeQuery();
                TransItem transitemsum2 = null;
                while (rssum2.next()) {
                    transitemsum2 = new TransItem();
                    try {
                        transitemsum2.setDescription(rssum2.getString("description"));
                    } catch (Exception e) {
                        transitemsum2.setDescription("");
                    }
                    try {
                        transitemsum2.setCurrency_code(rssum2.getString("currency_code"));
                    } catch (Exception e) {
                        transitemsum2.setCurrency_code("");
                    }
                    try {
                        transitemsum2.setAmountIncVat(rssum2.getDouble("amount_inc_vat"));
                    } catch (NullPointerException npe) {
                        transitemsum2.setAmountIncVat(0);
                    }
                    try {
                        transitemsum2.setItemQty(rssum2.getDouble("qty_sum"));
                    } catch (NullPointerException npe) {
                        transitemsum2.setItemQty(0);
                    }
                    try {
                        transitemsum2.setTransactionTypeName(new TransactionTypeBean().getTransactionType(rssum2.getInt("transaction_type_id")).getTransactionTypeName());
                    } catch (NullPointerException npe) {
                        transitemsum2.setTransactionTypeName("");
                    }
                    this.TransItemSummary2.add(transitemsum2);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    public void reportTransItemDetail(Trans aTrans, TransBean aTransBean, Item aItem, Transactor aTransactor, int aCategoryId) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String msg = "";
        aTransBean.setActionMessage("");
        try {
            if (aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
                //okay no problem
            } else {
                msg = "Select Date Range";
            }
        } catch (Exception e) {
            //do nothing
        }
        if (aTransBean.getDateType().length() == 0) {
            aTransBean.setDateType("Add Date");
        }
        if (msg.length() > 0) {
            aTransBean.setActionMessage(ub.translateWordsInText(BaseName, msg));
            FacesContext.getCurrentInstance().addMessage("Report", new FacesMessage(ub.translateWordsInText(BaseName, msg)));
        } else {
            ResultSet rs = null;
            ResultSet rssum = null;
            ResultSet rssum2 = null;
            this.TransItemList = new ArrayList<>();
            String sql = "SELECT ti.*,"
                    + "ifnull(t.transaction_number,'') as transaction_number,tt.transaction_type_name,s.store_name,"
                    + "i.description,u.unit_symbol,i.category_id,t.add_date,"
                    + "ud1.user_name as add_user,ud2.user_name as trans_user,ifnull(tr.transactor_names,'') as transactor_names,"
                    + "ifnull(t.currency_code,'') as currency_code "
                    + "FROM transaction_item ti "
                    + "INNER JOIN transaction t ON ti.transaction_id=t.transaction_id "
                    + "INNER JOIN item i ON ti.item_id=i.item_id "
                    + "LEFT JOIN transactor tr ON t.transactor_id=tr.transactor_id "
                    + "INNER JOIN store s ON t.store_id=s.store_id "
                    + "INNER JOIN transaction_type tt ON t.transaction_type_id=tt.transaction_type_id "
                    + "INNER JOIN unit u ON i.unit_id=u.unit_id "
                    + "INNER JOIN user_detail ud1 ON t.add_user_detail_id=ud1.user_detail_id "
                    + "INNER JOIN user_detail ud2 ON t.transaction_user_detail_id=ud2.user_detail_id "
                    + "WHERE 1=1";
            String wheresql = "";
            String ordersql = "";
            if (aCategoryId > 0) {
                wheresql = wheresql + " AND i.category_id=" + aCategoryId;
            }
            if (aTrans.getStoreId() > 0) {
                wheresql = wheresql + " AND t.store_id=" + aTrans.getStoreId();
            }
            if (aTrans.getTransactionNumber().length() > 0) {
                wheresql = wheresql + " AND t.transaction_number='" + aTrans.getTransactionNumber() + "'";
            }
            if (aTrans.getTransactionTypeId() > 0) {
                wheresql = wheresql + " AND t.transaction_type_id=" + aTrans.getTransactionTypeId();
            }
            if (aTrans.getAddUserDetailId() > 0) {
                wheresql = wheresql + " AND t.add_user_detail_id=" + aTrans.getAddUserDetailId();
            }
            if (aTrans.getTransactionUserDetailId() > 0) {
                wheresql = wheresql + " AND t.transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
            }
            try {
                if (null != aTransactor && aTransactor.getTransactorId() > 0) {
                    wheresql = wheresql + " AND t.transactor_id=" + aTransactor.getTransactorId();
                }
            } catch (NullPointerException npe) {

            }
            try {
                if (null != aItem && aItem.getItemId() > 0) {
                    wheresql = wheresql + " AND ti.item_id=" + aItem.getItemId();
                }
            } catch (NullPointerException npe) {

            }
            if (aTransBean.getDateType().length() > 0 && aTransBean.getDate1() != null && aTransBean.getDate2() != null) {
                switch (aTransBean.getDateType()) {
                    case "Trans Date":
                        wheresql = wheresql + " AND t.transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
                        break;
                    case "Add Date":
                        wheresql = wheresql + " AND t.add_date BETWEEN '" + new java.sql.Timestamp(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Timestamp(aTransBean.getDate2().getTime()) + "'";
                        break;
                }
            }
            ordersql = " ORDER BY t.add_date DESC,t.transaction_id DESC";
            sql = sql + wheresql + ordersql;
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                rs = ps.executeQuery();
                TransItem transitem = null;
                TransItemBean tib = new TransItemBean();
                while (rs.next()) {
                    transitem = new TransItem();
                    tib.setTransItemFromResultSet(transitem, rs);
                    try {
                        transitem.setStoreName(rs.getString("store_name"));
                    } catch (Exception e) {
                        transitem.setStoreName("");
                    }
                    try {
                        transitem.setTransactionTypeName(rs.getString("transaction_type_name"));
                    } catch (Exception e) {
                        transitem.setTransactionTypeName("");
                    }
                    try {
                        transitem.setTransactorNames(rs.getString("transactor_names"));
                    } catch (Exception e) {
                        transitem.setTransactorNames("");
                    }
                    try {
                        transitem.setDescription(rs.getString("description"));
                    } catch (Exception e) {
                        transitem.setDescription("");
                    }
                    try {
                        transitem.setUnitSymbol(rs.getString("unit_symbol"));
                    } catch (Exception e) {
                        transitem.setUnitSymbol("");
                    }
                    try {
                        transitem.setCurrency_code(rs.getString("currency_code"));
                    } catch (Exception e) {
                        transitem.setCurrency_code("");
                    }
                    try {
                        transitem.setAddUserDetailName(rs.getString("add_user"));
                    } catch (Exception e) {
                        transitem.setStoreName("");
                    }
                    try {
                        transitem.setTransactionUserDetailName(rs.getString("trans_user"));
                    } catch (Exception e) {
                        transitem.setTransactionUserDetailName("");
                    }
                    try {
                        transitem.setAddDate(new Date(rs.getTimestamp("add_date").getTime()));
                    } catch (Exception e) {
                        transitem.setAddDate(null);
                    }
                    this.TransItemList.add(transitem);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }

            //summary-category
            this.TransItemSummary = new ArrayList<>();
            String sqlsum = "";
            sqlsum = "SELECT c.category_name,tt.transaction_type_name,t.currency_code,sum(amount_inc_vat) as amount_inc_vat FROM transaction_item ti "
                    + "INNER JOIN transaction t ON ti.transaction_id=t.transaction_id "
                    + "INNER JOIN transaction_type tt ON t.transaction_type_id=tt.transaction_type_id "
                    + "INNER JOIN item i ON ti.item_id=i.item_id "
                    + "INNER JOIN category c ON i.category_id=c.category_id "
                    + "WHERE 1=1";
            String ordersqlsum = "";
            String groupbysql = "";
            groupbysql = " GROUP BY category_name,transaction_type_name,currency_code";
            ordersqlsum = " ORDER BY category_name,transaction_type_name,currency_code";
            sqlsum = sqlsum + wheresql + groupbysql + ordersqlsum;
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sqlsum);) {
                rssum = ps.executeQuery();
                TransItem transitemsum = null;
                while (rssum.next()) {
                    transitemsum = new TransItem();
                    try {
                        transitemsum.setCategoryName(rssum.getString("category_name"));

                    } catch (Exception e) {
                        transitemsum.setCategoryName("");
                    }
                    try {
                        transitemsum.setCurrency_code(rssum.getString("currency_code"));
                    } catch (Exception e) {
                        transitemsum.setCurrency_code("");
                    }
                    try {
                        transitemsum.setAmountIncVat(rssum.getDouble("amount_inc_vat"));
                    } catch (NullPointerException npe) {
                        transitemsum.setAmountIncVat(0);
                    }
                    try {
                        transitemsum.setTransactionTypeName(rssum.getString("transaction_type_name"));
                    } catch (NullPointerException npe) {
                        transitemsum.setTransactionTypeName("");
                    }
                    this.TransItemSummary.add(transitemsum);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }

            //summary-item
            this.TransItemSummary2 = new ArrayList<>();
            String sqlsum2 = "";
            sqlsum2 = "SELECT i.description,tt.transaction_type_name,t.currency_code,sum(amount_inc_vat) as amount_inc_vat,sum(item_qty) as qty_sum FROM transaction_item ti "
                    + "INNER JOIN transaction t ON ti.transaction_id=t.transaction_id "
                    + "INNER JOIN transaction_type tt ON t.transaction_type_id=tt.transaction_type_id "
                    + "INNER JOIN item i ON ti.item_id=i.item_id WHERE 1=1";
            String ordersqlsum2 = "";
            String groupbysql2 = "";
            groupbysql2 = " GROUP BY description,transaction_type_name,currency_code";
            ordersqlsum2 = " ORDER BY description,transaction_type_name,currency_code";
            sqlsum2 = sqlsum2 + wheresql + groupbysql2 + ordersqlsum2;
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sqlsum2);) {
                rssum2 = ps.executeQuery();
                TransItem transitemsum2 = null;
                while (rssum2.next()) {
                    transitemsum2 = new TransItem();
                    try {
                        transitemsum2.setDescription(rssum2.getString("description"));
                    } catch (Exception e) {
                        transitemsum2.setDescription("");
                    }
                    try {
                        transitemsum2.setCurrency_code(rssum2.getString("currency_code"));
                    } catch (Exception e) {
                        transitemsum2.setCurrency_code("");
                    }
                    try {
                        transitemsum2.setAmountIncVat(rssum2.getDouble("amount_inc_vat"));
                    } catch (NullPointerException npe) {
                        transitemsum2.setAmountIncVat(0);
                    }
                    try {
                        transitemsum2.setItemQty(rssum2.getDouble("qty_sum"));
                    } catch (NullPointerException npe) {
                        transitemsum2.setItemQty(0);
                    }
                    try {
                        transitemsum2.setTransactionTypeName(rssum2.getString("transaction_type_name"));
                    } catch (NullPointerException npe) {
                        transitemsum2.setTransactionTypeName("");
                    }
                    this.TransItemSummary2.add(transitemsum2);
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    public void refreshTransListChoice(int aChoiceId, long aTransactorId, int aLimit) {
        int TransTypeId = 0;
        long TransactorId = 0;
        if (aChoiceId > 1) {
            TransTypeId = aChoiceId;
        }
        if (aTransactorId > 0) {
            TransactorId = aTransactorId;
        }
        if (aChoiceId == 0 || aTransactorId == 0) {
            try {
                this.TransList.clear();
            } catch (NullPointerException npe) {
                this.TransList = new ArrayList<>();
            }
        } else {
            this.refreshTransList(TransTypeId, TransactorId, aLimit);
        }
    }

    public void refreshTransListChoice(int aChoiceId, long aTransactorId, int aLimit, String aStatusColumn, String aStatusValues) {
        int TransTypeId = 0;
        long TransactorId = 0;
        if (aChoiceId > 0) {
            TransTypeId = aChoiceId;
        }
        if (aTransactorId > 0) {
            TransactorId = aTransactorId;
        }
        if (TransTypeId == 11) {
            this.refreshTransList(TransTypeId, TransactorId, aLimit, aStatusColumn, aStatusValues);
        } else if (TransTypeId == 2) {
            this.refreshTransList(TransTypeId, TransactorId, aLimit);
        }
    }

    public void refreshTransList(int aTransTypeId, long aTransactorId, int aLimit) {
        try {
            this.TransList.clear();
        } catch (NullPointerException npe) {
            this.TransList = new ArrayList<>();
        }
        if (aTransactorId > 0) {
            this.TransList = this.getTranss(aTransTypeId, aTransactorId, aLimit);
        }
    }

    public void refreshTransList(int aTransTypeId, long aTransactorId, int aLimit, String aStatusColumn, String aStatusValues) {
        try {
            this.TransList.clear();
        } catch (NullPointerException npe) {
            this.TransList = new ArrayList<>();
        }
        if (aTransactorId > 0) {
            this.TransList = this.getTranss(aTransTypeId, aTransactorId, aLimit, aStatusColumn, aStatusValues);
        }
    }

    public void setRecentHirePuporse(Trans aTrans, long aTransactorId) {
        aTrans.setTransactionComment("");
        long TransId = this.getMostRecentTransId(65, 94, aTransactorId);
        if (TransId > 0) {
            Trans trans = this.getTrans(TransId);
            if (null != trans) {
                if (trans.getTransactionComment().length() > 0) {
                    aTrans.setTransactionComment(trans.getTransactionComment());
                }
            }
        }
    }

    public long getMostRecentTransId(int aTransTypeId, int aTransReasId, long aTransactorId) {
        ResultSet rs = null;
        String sql = "";
        long trans_id = 0;
        sql = "SELECT max(transaction_id) as transaction_id FROM transaction WHERE transaction_type_id=" + aTransTypeId + " AND transaction_reason_id=" + aTransReasId + " AND transactor_id=" + aTransactorId;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            if (rs.next()) {
                trans_id = rs.getLong("transaction_id");
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return trans_id;
    }

    public List<Trans> getTranss(int aTransTypeId, long aTransactorId, int aLimit) {
        String sql = "";
        List<Trans> TempTranss = new ArrayList<>();
        ResultSet rs = null;
        try {
            if (aLimit > 0) {
                sql = "SELECT * FROM transaction WHERE transaction_type_id=" + aTransTypeId + " AND transactor_id=" + aTransactorId + " ORDER BY transaction_id DESC LIMIT " + aLimit;
            } else {
                sql = "SELECT * FROM transaction WHERE transaction_type_id=" + aTransTypeId + " AND transactor_id=" + aTransactorId + " ORDER BY transaction_id DESC";
            }
        } catch (NullPointerException npe) {
        }
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                TempTranss.add(this.getTransFromResultset(rs));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return TempTranss;
    }

    public List<Trans> getTranss(int aTransTypeId, long aTransactorId, int aLimit, String aStatusColumn, String aStatusValues) {
        String sql = "";
        List<Trans> TempTranss = new ArrayList<>();
        ResultSet rs = null;
        try {
            if (aLimit > 0) {
                sql = "SELECT * FROM transaction WHERE transaction_type_id=" + aTransTypeId + " AND transactor_id=" + aTransactorId + " AND " + aStatusColumn + " IN(" + aStatusValues + ") ORDER BY transaction_id DESC LIMIT " + aLimit;
            } else {
                sql = "SELECT * FROM transaction WHERE transaction_type_id=" + aTransTypeId + " AND transactor_id=" + aTransactorId + " AND " + aStatusColumn + " IN(" + aStatusValues + ") ORDER BY transaction_id DESC";
            }
        } catch (NullPointerException npe) {
        }
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                TempTranss.add(this.getTransFromResultset(rs));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return TempTranss;
    }

    public void refreshTransListStoreTransferReq(int aTransTypeId, int aFromStoreId, int aToStoreId, int aLimit) {
        try {
            this.TransList.clear();
        } catch (NullPointerException npe) {
            this.TransList = new ArrayList<>();
        }
        this.TransList = this.getTranssStoreTransferReq(aTransTypeId, aFromStoreId, aToStoreId, aLimit);
    }

    public List<Trans> getTranssStoreTransferReq(int aTransTypeId, int aFromStoreId, int aToStoreId, int aLimit) {
        String sql = "";
        List<Trans> TempTranss = new ArrayList<>();
        ResultSet rs = null;
        try {
            if (aLimit > 0) {
                sql = "SELECT * FROM transaction WHERE transaction_type_id=" + aTransTypeId + " AND store_id=" + aFromStoreId + " AND store2_id=" + aToStoreId + " ORDER BY transaction_id DESC LIMIT " + aLimit;
            } else {
                sql = "SELECT * FROM transaction WHERE transaction_type_id=" + aTransTypeId + " AND store_id=" + aFromStoreId + " AND store2_id=" + aToStoreId + " ORDER BY transaction_id DESC";
            }
        } catch (NullPointerException npe) {
        }
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            while (rs.next()) {
                TempTranss.add(this.getTransFromResultset(rs));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return TempTranss;
    }

    public void getReturnNote(long aHireTransId, Trans aReturnTrans, List<TransItem> aReturnTransItems) {
        aReturnTransItems.clear();
        if (aHireTransId > 0) {
            if (null == this.RefTrans) {
                this.RefTrans = new Trans();
            }
            this.RefTrans = this.getTrans(aHireTransId);
            if (null == aReturnTrans) {
                aReturnTrans = new Trans();
            }
            if (null == this.RefTrans || this.RefTrans.getTransactionId() == 0) {
                String mg = "INVALID HIRE NUMBER...";
                FacesContext.getCurrentInstance().addMessage("Return", new FacesMessage(mg));
            } else if (this.RefTrans.getStoreId() != new GeneralUserSetting().getCurrentStore().getStoreId()) {
                String mg = "RETURN " + CompanySetting.getStoreEquivName() + " AND HIRE " + CompanySetting.getStoreEquivName() + " MUST BE THE SAME...";
                FacesContext.getCurrentInstance().addMessage("Return", new FacesMessage(mg));
            } else {
                aReturnTrans.setTransactionRef(this.RefTrans.getTransactionNumber());
                aReturnTrans.setTransactorId(this.RefTrans.getTransactorId());
                aReturnTrans.setStoreId(this.RefTrans.getStoreId());
                aReturnTrans.setSite_id(this.RefTrans.getSite_id());
                aReturnTrans.setTransactionComment(this.RefTrans.getTransactionComment());
                aReturnTrans.setDuration_value(this.calcDurationReturn(this.RefTrans.getTo_date(), aReturnTrans.getTransactionDate()));
                aReturnTransItems.clear();
                List<TransItem> HireItems = new TransItemBean().getTransItemsByTransactionId(aHireTransId);
                TransItem ti = null;
                TransItem ti2 = null;
                for (int i = 0; i < HireItems.size(); i++) {
                    ti = new TransItem();
                    ti.setItemId(HireItems.get(i).getItemId());
                    ti.setBatchno(HireItems.get(i).getBatchno());
                    ti.setCodeSpecific(HireItems.get(i).getCodeSpecific());
                    ti.setDescSpecific(HireItems.get(i).getDescSpecific());
                    ti.setDescMore(HireItems.get(i).getDescMore());
                    ti.setItemId(HireItems.get(i).getItemId());
                    ti.setQty_taken(HireItems.get(i).getItemQty());
                    ti2 = this.getHireTotalReturned(this.RefTrans.getTransactionNumber(), ti);
                    ti.setQty_total(ti2.getItemQty());//total returned good
                    ti.setQty_damage_total(ti2.getQty_damage());//total returned damage
                    ti.setQty_balance(ti.getQty_taken() - ti.getQty_total() - ti.getQty_damage_total());
                    //ti.setDuration_passed(this.calcDurationReturn(this.RefTrans.getTo_date(), aReturnTrans.getTransactionDate()));//cal weeks passed
                    ti.setDuration_passed(aReturnTrans.getDuration_value());
                    aReturnTransItems.add(ti);
                }
            }
        }
    }

    public void getReturnNoteByNo(String aHireTransNo, Trans aReturnTrans, List<TransItem> aReturnTransItems) {
        aReturnTransItems.clear();
        if (aHireTransNo.length() > 0) {
            if (null == this.RefTrans) {
                this.RefTrans = new Trans();
            }
            this.RefTrans = this.getTransByTransNumber(aHireTransNo);
            if (null == aReturnTrans) {
                aReturnTrans = new Trans();
            }
            if (null == this.RefTrans || this.RefTrans.getTransactionId() == 0) {
                String mg = "INVALID HIRE NUMBER...";
                FacesContext.getCurrentInstance().addMessage("Return", new FacesMessage(mg));
            } else if (this.RefTrans.getStoreId() != new GeneralUserSetting().getCurrentStore().getStoreId()) {
                String mg = "RETURN " + CompanySetting.getStoreEquivName() + " AND HIRE " + CompanySetting.getStoreEquivName() + " MUST BE THE SAME...";
                FacesContext.getCurrentInstance().addMessage("Return", new FacesMessage(mg));
            } else {
                aReturnTrans.setTransactionRef(this.RefTrans.getTransactionNumber());
                aReturnTrans.setTransactorId(this.RefTrans.getTransactorId());
                aReturnTrans.setStoreId(this.RefTrans.getStoreId());
                aReturnTrans.setSite_id(this.RefTrans.getSite_id());
                aReturnTrans.setTransactionComment(this.RefTrans.getTransactionComment());
                double WeeksPassed = this.getDurationPassedOnReturn(aReturnTrans, this.RefTrans);
                aReturnTrans.setDuration_value(WeeksPassed);
                aReturnTransItems.clear();
                List<TransItem> HireItems = new TransItemBean().getTransItemsByTransactionId(this.RefTrans.getTransactionId());
                TransItem ti = null;
                TransItem ti2 = null;
                for (int i = 0; i < HireItems.size(); i++) {
                    ti = new TransItem();
                    ti.setItemId(HireItems.get(i).getItemId());
                    ti.setBatchno(HireItems.get(i).getBatchno());
                    ti.setCodeSpecific(HireItems.get(i).getCodeSpecific());
                    ti.setDescSpecific(HireItems.get(i).getDescSpecific());
                    ti.setDescMore(HireItems.get(i).getDescMore());
                    ti.setItemId(HireItems.get(i).getItemId());
                    ti.setQty_taken(HireItems.get(i).getItemQty());
                    ti2 = this.getHireTotalReturned(this.RefTrans.getTransactionNumber(), ti);
                    ti.setQty_total(ti2.getItemQty());//total returned good
                    ti.setQty_damage_total(ti2.getQty_damage());//total returned damage
                    ti.setQty_balance(ti.getQty_taken() - ti.getQty_total() - ti.getQty_damage_total());
                    //ti.setDuration_passed(this.calcDurationReturn(this.RefTrans.getTo_date(), aReturnTrans.getTransactionDate()));//cal weeks passed
                    ti.setDuration_passed(aReturnTrans.getDuration_value());
                    new TransItemBean().updateLookUpsUI(ti);
                    aReturnTransItems.add(ti);
                }
            }
        }
    }

    public void setHireReturnTotalsAndBalances(String aHireTransNo, List<TransItem> aTransItems) {
        try {
            TransItemBean tib = new TransItemBean();
            for (int i = 0; i < aTransItems.size(); i++) {
                TransItem ti2 = this.getHireTotalReturned(aHireTransNo, aTransItems.get(i));
                aTransItems.get(i).setQty_total(ti2.getItemQty());//total returned good
                aTransItems.get(i).setQty_damage_total(ti2.getQty_damage());//total returned damage
                Trans t = this.getTransByTransNumber(aHireTransNo);
                TransItem hire_ti = tib.getTransItemByTransAndItem(t.getTransactionId(), aTransItems.get(i).getItemId());
                aTransItems.get(i).setQty_taken(hire_ti.getItemQty());
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

//    public void setHireReturnTotalsAndBalances(String aHireTransNo, TransItem aTransItem) {
//        TransItem ti2 = this.getHireTotalReturned(aHireTransNo, aTransItem);
//        aTransItem.setQty_total(ti2.getItemQty());//total returned good
//        aTransItem.setQty_damage_total(ti2.getQty_damage());//total returned damage
//    }
    public void getDeliveryNote(long aHireTransId, Trans aDeliveryTrans, List<TransItem> aReturnTransItems) {
        if (aHireTransId > 0) {
            if (null == this.RefTrans) {
                this.RefTrans = new Trans();
            }
            this.RefTrans = this.getTrans(aHireTransId);
            if (null == aDeliveryTrans) {
                aDeliveryTrans = new Trans();
            }
            if (null == this.RefTrans || this.RefTrans.getTransactionId() == 0) {
                String mg = "INVALID HIRE NUMBER...";
                FacesContext.getCurrentInstance().addMessage("Return", new FacesMessage(mg));
            } else if (this.RefTrans.getStoreId() != new GeneralUserSetting().getCurrentStore().getStoreId()) {
                String mg = "FROM DELIVERY " + CompanySetting.getStoreEquivName() + " AND HIRE " + CompanySetting.getStoreEquivName() + " MUST BE THE SAME...";
                FacesContext.getCurrentInstance().addMessage("Return", new FacesMessage(mg));
            } else {
                aDeliveryTrans.setTransactionRef(this.RefTrans.getTransactionNumber());
                aDeliveryTrans.setTransactorId(this.RefTrans.getTransactorId());
                aDeliveryTrans.setStoreId(this.RefTrans.getStoreId());
                aDeliveryTrans.setSite_id(this.RefTrans.getSite_id());
                aReturnTransItems.clear();
                List<TransItem> HireItems = new TransItemBean().getTransItemsByTransactionId(aHireTransId);
                TransItem ti = null;
                TransItem ti2 = null;
                for (int i = 0; i < HireItems.size(); i++) {
                    ti = new TransItem();
                    ti.setItemId(HireItems.get(i).getItemId());
                    ti.setBatchno(HireItems.get(i).getBatchno());
                    ti.setCodeSpecific(HireItems.get(i).getCodeSpecific());
                    ti.setDescSpecific(HireItems.get(i).getDescSpecific());
                    ti.setDescMore(HireItems.get(i).getDescMore());
                    ti.setItemId(HireItems.get(i).getItemId());
                    ti.setQty_taken(HireItems.get(i).getItemQty());
                    //ti2 = new TransItem();
                    ti2 = this.getHireTotalDelivered(this.RefTrans.getTransactionNumber(), ti);
                    ti.setQty_total(ti2.getItemQty());//cal total delivered
                    ti.setQty_balance(ti.getQty_taken() - ti.getQty_total());//total un-delivered
                    aReturnTransItems.add(ti);
                }
            }
        }
    }

    public void getDeliveryNoteByTransNo(String aHireTransNo, Trans aDeliveryTrans, List<TransItem> aReturnTransItems) {
        aReturnTransItems.clear();
        if (aHireTransNo.length() > 0) {
            if (null == this.RefTrans) {
                this.RefTrans = new Trans();
            }
            this.RefTrans = this.getTransByTransNumber(aHireTransNo);
            if (null == aDeliveryTrans) {
                aDeliveryTrans = new Trans();
            }
            if (null == this.RefTrans || this.RefTrans.getTransactionId() == 0) {
                String mg = "INVALID HIRE NUMBER...";
                FacesContext.getCurrentInstance().addMessage("Return", new FacesMessage(mg));
            } else if (this.RefTrans.getStoreId() != new GeneralUserSetting().getCurrentStore().getStoreId()) {
                String mg = "FROM DELIVERY " + CompanySetting.getStoreEquivName() + " AND HIRE " + CompanySetting.getStoreEquivName() + " MUST BE THE SAME...";
                FacesContext.getCurrentInstance().addMessage("Return", new FacesMessage(mg));
            } else {
                aDeliveryTrans.setTransactionRef(this.RefTrans.getTransactionNumber());
                aDeliveryTrans.setTransactorId(this.RefTrans.getTransactorId());
                aDeliveryTrans.setStoreId(this.RefTrans.getStoreId());
                aDeliveryTrans.setSite_id(this.RefTrans.getSite_id());
                aReturnTransItems.clear();
                List<TransItem> HireItems = new TransItemBean().getTransItemsByTransactionId(this.RefTrans.getTransactionId());
                TransItem ti = null;
                TransItem ti2 = null;
                for (int i = 0; i < HireItems.size(); i++) {
                    ti = new TransItem();
                    ti.setItemId(HireItems.get(i).getItemId());
                    ti.setBatchno(HireItems.get(i).getBatchno());
                    ti.setCodeSpecific(HireItems.get(i).getCodeSpecific());
                    ti.setDescSpecific(HireItems.get(i).getDescSpecific());
                    ti.setDescMore(HireItems.get(i).getDescMore());
                    ti.setItemId(HireItems.get(i).getItemId());
                    ti.setQty_taken(HireItems.get(i).getItemQty());
                    //ti2 = new TransItem();
                    ti2 = this.getHireTotalDelivered(this.RefTrans.getTransactionNumber(), ti);
                    ti.setQty_total(ti2.getItemQty());//cal total delivered
                    ti.setQty_balance(ti.getQty_taken() - ti.getQty_total());//total un-delivered
                    new TransItemBean().updateLookUpsUI(ti);
                    aReturnTransItems.add(ti);
                }
            }
        }
    }

    public String getReturnHireInvoice(long aReturnNoteId) {
        String mg = "";
        TransItemBean tib = new TransItemBean();
        try {
            if (aReturnNoteId > 0) {
                Trans ReturnedTrans = this.getTrans(aReturnNoteId);
                if (null == this.TransChild) {
                    System.out.println("Passed-There-Again");
                    this.TransChild = new Trans();
                }
                this.clearTrans(this.TransChild);
                if (null == ReturnedTrans || ReturnedTrans.getTransactionId() == 0) {
                    mg = "INVALID HIRE RETURN NUMBER...";
                } else if (ReturnedTrans.getStoreId() != new GeneralUserSetting().getCurrentStore().getStoreId()) {
                    mg = "RETURN " + CompanySetting.getStoreEquivName() + " AND HIRE " + CompanySetting.getStoreEquivName() + " MUST BE THE SAME...";
                } else {
                    try {
                        this.TransChild.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
                    } catch (NullPointerException npe) {
                        this.TransChild.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
                    }
                    this.TransChild.setTransactionRef(ReturnedTrans.getTransactionNumber());
                    this.TransChild.setTransactorId(ReturnedTrans.getTransactorId());
                    this.TransChild.setStoreId(ReturnedTrans.getStoreId());
                    this.TransChild.setSite_id(ReturnedTrans.getSite_id());
                    this.TransChild.setTransactionComment(ReturnedTrans.getTransactionComment());
                    Trans RefHireTrans = this.getTransByTransNumber(ReturnedTrans.getTransactionRef());
                    try {
                        this.TransChild.setFrom_date(RefHireTrans.getTo_date());
                    } catch (NullPointerException npe) {
                        this.TransChild.setFrom_date(null);
                    }
                    try {
                        this.TransChild.setCurrencyCode(RefHireTrans.getCurrencyCode());
                    } catch (NullPointerException npe) {
                        this.TransChild.setCurrencyCode("");
                    }
                    this.TransChild.setTo_date(ReturnedTrans.getTransactionDate());
                    this.TransChild.setDuration_value(ReturnedTrans.getDuration_value());
                    this.TransChild.setDuration_type(ReturnedTrans.getDuration_type());
                    this.ActiveTransItemsChild.clear();
                    List<TransItem> ReturnedItems = new TransItemBean().getTransItemsByTransactionId(aReturnNoteId);
                    TransItem ti = null;
                    TransItem ti2 = null;
                    Item i = null;
                    Item i2 = null;
                    for (TransItem ReturnedTransItem : ReturnedItems) {
                        //check for returned GOOD but DELAYED
                        if (ReturnedTransItem.getItemQty() > 0 && ReturnedTransItem.getDuration_passed() > 0) {
                            i = new ItemBean().getItem(ReturnedTransItem.getItemId());
                            ti = new TransItem();
                            tib.updateModelTransItemCEC(this.TransChild.getStoreId(), 68, 97, "", this.TransChild, ti, i, ReturnedTransItem.getItemQty());
                            ti.setItemId(ReturnedTransItem.getItemId());
                            ti.setBatchno(ReturnedTransItem.getBatchno());
                            ti.setCodeSpecific(ReturnedTransItem.getCodeSpecific());
                            ti.setDescSpecific(ReturnedTransItem.getDescSpecific());
                            ti.setDescMore(ReturnedTransItem.getDescMore());
                            ti.setNarration("DELAYED");
                            tib.addTransItemCEC(this.TransChild.getStoreId(), 68, 97, "", this.TransChild, this.ActiveTransItemsChild, ti, i);
                        }
                        //check for returned DAMAGED/LOST
                        if (ReturnedTransItem.getQty_damage() > 0) {
                            i2 = new ItemBean().getItem(ReturnedTransItem.getItemId());
                            ti2 = new TransItem();
                            //only for damage; overrite item hire rate by cost rate
                            double unitcost = new StockBean().getItemUnitCostPrice(this.TransChild.getStoreId(), i2.getItemId(), ReturnedTransItem.getBatchno(), ReturnedTransItem.getCodeSpecific(), ReturnedTransItem.getDescSpecific());
                            i2.setUnit_hire_price(unitcost); //only applies on damaged/lost items
                            tib.updateModelTransItemCEC(this.TransChild.getStoreId(), 68, 97, "", this.TransChild, ti2, i2, ReturnedTransItem.getQty_damage());
                            ti2.setItemId(ReturnedTransItem.getItemId());
                            ti2.setBatchno(ReturnedTransItem.getBatchno());
                            ti2.setCodeSpecific(ReturnedTransItem.getCodeSpecific());
                            ti2.setDescSpecific(ReturnedTransItem.getDescSpecific());
                            ti2.setDescMore(ReturnedTransItem.getDescMore());
                            ti2.setNarration("DAMAGED/LOST");
                            tib.addTransItemCEC(this.TransChild.getStoreId(), 68, 97, "", this.TransChild, this.ActiveTransItemsChild, ti2, i2);
                        }
                    }
                    //update totals
                    this.setTransTotalsAndUpdateCEC(68, 97, this.TransChild, this.ActiveTransItemsChild);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return mg;
    }

    public String getOrderSalesInvoice(long aOrderId, UserDetail aUserDetail) {
        String mg = "";
        TransItemBean tib = new TransItemBean();
        try {
            if (aOrderId > 0) {
                this.TransChild = this.getTrans(aOrderId);
                if (null == this.TransChild || this.TransChild.getTransactionId() == 0) {
                    mg = "INVALID ORDER NUMBER...";
                } else if (this.TransChild.getStoreId() != new GeneralUserSetting().getCurrentStore().getStoreId()) {
                    mg = "ORDER " + CompanySetting.getStoreEquivName() + " AND INVOICE " + CompanySetting.getStoreEquivName() + " MUST BE THE SAME...";
                } else {
                    this.TransChild.setTransactionUserDetailId(aUserDetail.getUserDetailId());
                    this.TransChild.setTransactionId(0);
                    this.TransChild.setTransactionTypeId(2);
                    this.TransChild.setTransactionReasonId(2);
                    try {
                        this.TransChild.setTransactionDate(new CompanySetting().getCURRENT_SERVER_DATE());
                    } catch (NullPointerException npe) {
                        this.TransChild.setTransactionDate(null);
                    }
                    this.TransChild.setTransactionRef(this.TransChild.getTransactionNumber());
                    this.TransChild.setTransactionNumber("");
                    this.TransChild.setAmountTendered(this.TransChild.getGrandTotal());
                    this.updateLookup(this.TransChild);
                    this.ActiveTransItemsChild = new TransItemBean().getTransItemsByTransactionId(aOrderId);
                    for (int i = 0; i < this.ActiveTransItemsChild.size(); i++) {
                        this.ActiveTransItemsChild.get(i).setTransactionItemId(0);
                        this.ActiveTransItemsChild.get(i).setTransactionId(0);
                    }
                    //update totals
                    this.setTransTotalsAndUpdateCEC(2, 2, this.TransChild, this.ActiveTransItemsChild);
                    //update the receivable and payables for the UI
                    this.refreshCustomerBalances(this.TransChild);
                    //pay method and hild acc
                    this.TransChild.setPayMethod(1);
                    int ChildAccountId = 0;
                    try {
                        ChildAccountId = new AccChildAccountBean().getAccChildAccountsForCashReceipt(this.TransChild.getCurrencyCode(), 1, new GeneralUserSetting().getCurrentStore().getStoreId(), aUserDetail.getUserDetailId()).get(0).getAccChildAccountId();
                    } catch (Exception e) {
                        ChildAccountId = 0;
                    }
                    this.TransChild.setAccChildAccountId(ChildAccountId);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return mg;
    }

    public TransItem getHireTotalReturned(String aHireTransNo, TransItem aTransItem) {
        TransItem tempti = new TransItem();
        String sql = "";
        ResultSet rs = null;
        sql = "SELECT SUM(ti.item_qty) AS item_qty,SUM(ti.qty_damage) AS qty_damage FROM transaction_item ti INNER JOIN transaction t ON ti.transaction_id=t.transaction_id "
                + " WHERE t.transaction_type_id=67 AND t.transaction_ref='" + aHireTransNo + "' AND ti.item_id=" + aTransItem.getItemId() + " AND ti.batchno='" + aTransItem.getBatchno() + "' AND ti.code_specific='" + aTransItem.getCodeSpecific() + "' AND ti.desc_specific='" + aTransItem.getDescSpecific() + "'";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            if (rs.next()) {
                tempti = new TransItem();
                try {
                    tempti.setItemQty(rs.getDouble("item_qty"));
                } catch (NullPointerException npe) {
                    tempti.setItemQty(0);
                }
                try {
                    tempti.setQty_damage(rs.getDouble("qty_damage"));
                } catch (NullPointerException npe) {
                    tempti.setQty_damage(0);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return tempti;
    }

    public TransItem getHireTotalDelivered(String aHireTransNo, TransItem aTransItem) {
        TransItem tempti = new TransItem();
        String sql = "";
        ResultSet rs = null;
        sql = "SELECT SUM(ti.item_qty) AS item_qty FROM transaction_item ti INNER JOIN transaction t ON ti.transaction_id=t.transaction_id "
                + " WHERE t.transaction_type_id=66 AND t.transaction_ref='" + aHireTransNo + "' AND ti.item_id=" + aTransItem.getItemId() + " AND ti.batchno='" + aTransItem.getBatchno() + "' AND ti.code_specific='" + aTransItem.getCodeSpecific() + "' AND ti.desc_specific='" + aTransItem.getDescSpecific() + "'";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            if (rs.next()) {
                tempti = new TransItem();
                try {
                    tempti.setItemQty(rs.getDouble("item_qty"));
                } catch (NullPointerException npe) {
                    tempti.setItemQty(0);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return tempti;
    }

    public TransItem getRefTransItemsTotal(int aTransTypeId, int aTransReasonId, String aTransNo, TransItem aTransItem) {
        TransItem tempti = new TransItem();
        String sql = "";
        ResultSet rs = null;
        String TReasWhereSql = "";
        if (aTransReasonId > 0) {
            TReasWhereSql = " AND t.transaction_reason_id=" + aTransReasonId;
        }
        sql = "SELECT SUM(ti.item_qty) AS item_qty,SUM(ti.qty_damage) AS qty_damage FROM transaction_item ti INNER JOIN transaction t ON ti.transaction_id=t.transaction_id "
                + " WHERE t.transaction_type_id=" + aTransTypeId + "" + TReasWhereSql + " AND t.transaction_ref='" + aTransNo + "' AND ti.item_id=" + aTransItem.getItemId() + " AND ti.batchno='" + aTransItem.getBatchno() + "' AND ti.code_specific='" + aTransItem.getCodeSpecific() + "' AND ti.desc_specific='" + aTransItem.getDescSpecific() + "'";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            if (rs.next()) {
                tempti = new TransItem();
                try {
                    tempti.setItemQty(rs.getDouble("item_qty"));
                } catch (NullPointerException npe) {
                    tempti.setItemQty(0);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return tempti;
    }

    public TransItem getRefTransItemsTotalNoSpecific(int aTransTypeId, int aTransReasonId, String aTransNo, TransItem aTransItem) {
        TransItem tempti = new TransItem();
        String sql = "";
        ResultSet rs = null;
        String TReasWhereSql = "";
        if (aTransReasonId > 0) {
            TReasWhereSql = " AND t.transaction_reason_id=" + aTransReasonId;
        }
        /*
         sql = "SELECT SUM(ti.item_qty) AS item_qty,SUM(ti.qty_damage) AS qty_damage FROM transaction_item ti INNER JOIN transaction t ON ti.transaction_id=t.transaction_id "
         + " WHERE t.transaction_type_id=" + aTransTypeId + "" + TReasWhereSql + " AND t.transaction_ref='" + aTransNo + "' AND ti.item_id=" + aTransItem.getItemId();
         */
        sql = "SELECT SUM(ti.item_qty) AS item_qty,SUM(ti.qty_damage) AS qty_damage FROM transaction_item ti "
                + "INNER JOIN transaction t ON ti.transaction_id=t.transaction_id "
                + "INNER JOIN transaction_item_unit tiu ON ti.transaction_item_id=tiu.transaction_item_id "
                + " WHERE t.transaction_type_id=" + aTransTypeId + "" + TReasWhereSql + " AND t.transaction_ref='" + aTransNo + "' AND ti.item_id=" + aTransItem.getItemId() + "' AND tiu.unit_id=" + aTransItem.getUnit_id();
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            if (rs.next()) {
                tempti = new TransItem();
                try {
                    tempti.setItemQty(rs.getDouble("item_qty"));
                } catch (NullPointerException npe) {
                    tempti.setItemQty(0);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return tempti;
    }

    public void calcDuration(Trans aTrans) {
        try {
            if (null != aTrans.getFrom_date() && null != aTrans.getTo_date()) {
                aTrans.setDuration_value(UtilityBean.weeksBetween(aTrans.getFrom_date(), aTrans.getTo_date()));
            } else {
                aTrans.setDuration_value(0);
            }
        } catch (NullPointerException npe) {

        }
    }

    public void calcToWeek(Trans aTrans) {
        try {
            if (null != aTrans.getFrom_date()) {
                int daystoadd = (int) ((aTrans.getDuration_value() * 7) - 1);
                aTrans.setTo_date(new UtilityBean().AddDays(aTrans.getFrom_date(), daystoadd));
            } else {
                aTrans.setTo_date(null);
            }
        } catch (NullPointerException npe) {

        }
    }

    public void calcDurationReturn(Trans aTrans) {
        try {
            if (null != aTrans.getFrom_date() && null != aTrans.getTo_date()) {
                aTrans.setDuration_value(UtilityBean.weeksBetweenReturn(aTrans.getFrom_date(), aTrans.getTo_date()));
            } else {
                aTrans.setDuration_value(0);
            }
        } catch (NullPointerException npe) {

        }
    }

    public void calcDuration(Trans aTrans, Date aFromDate, Date aToDate) {
        try {
            if (null != aFromDate && null != aToDate) {
                double durval = 0;
                durval = UtilityBean.weeksBetween(aFromDate, aToDate);
                if (durval <= 0) {
                    durval = 0;
                }
                aTrans.setDuration_value(durval);
            } else {
                aTrans.setDuration_value(0);
            }
        } catch (NullPointerException npe) {

        }
    }

    public void calcDurationReturn(Trans aTrans, Date aFromDate, Date aToDate) {
        try {
            if (null != aFromDate && null != aToDate) {
                double durval = 0;
                durval = UtilityBean.weeksBetweenReturn(aFromDate, aToDate);
                if (durval <= 0) {
                    durval = 0;
                }
                aTrans.setDuration_value(durval);
            } else {
                aTrans.setDuration_value(0);
            }
        } catch (NullPointerException npe) {

        }
    }

    public double calcDuration(Date aFromDate, Date aToDate) {
        double duration = 0;
        try {
            if (null != aFromDate && null != aToDate) {
                duration = UtilityBean.weeksBetween(aFromDate, aToDate);
                if (duration <= 0) {
                    duration = 0;
                }
            } else {
                duration = 0;
            }
        } catch (NullPointerException npe) {
        }
        return duration;
    }

    public double calcDurationReturn(Date aFromDate, Date aToDate) {
        double duration = 0;
        try {
            if (null != aFromDate && null != aToDate) {
                duration = UtilityBean.weeksBetweenReturn(aFromDate, aToDate);
                if (duration <= 0) {
                    duration = 0;
                }
            } else {
                duration = 0;
            }
        } catch (NullPointerException npe) {
        }
        return duration;
    }

    public void calcDuration(Trans aTrans, Date aFromDate, Date aToDate, List<TransItem> aTransItems) {
        try {
            if (null != aFromDate && null != aToDate) {
                double durval = 0;
                durval = UtilityBean.weeksBetween(aFromDate, aToDate);
                if (durval <= 0) {
                    durval = 0;
                }
                aTrans.setDuration_value(durval);
            } else {
                aTrans.setDuration_value(0);
            }
            //apply to all trans items
            for (int i = 0; i < aTransItems.size(); i++) {
                aTransItems.get(i).setDuration_passed(aTrans.getDuration_value());//cal weeks passed
            }
        } catch (NullPointerException npe) {

        }
    }

    public void calcDurationPassedOnReturn(Trans aTrans, Trans aRefTrans, List<TransItem> aTransItems) {
        try {
            //aReturnTrans.setDuration_value(this.calcDurationReturn(this.RefTrans.getTo_date(), aReturnTrans.getTransactionDate()));
            double weekspassed = 0;
            if (null != aTrans && null != aRefTrans) {
                double WeeksUsed = this.calcDurationReturn(aRefTrans.getFrom_date(), aTrans.getTransactionDate());
                double WeeksHired = aRefTrans.getDuration_value();
                weekspassed = WeeksUsed - WeeksHired;
                if (weekspassed < 0) {
                    weekspassed = 0;
                }
                aTrans.setDuration_value(weekspassed);
                //System.out.println("WeeksUsed:" + WeeksUsed + " WeeksHired:" + WeeksHired);
            }
            //apply to all trans items
            for (int i = 0; i < aTransItems.size(); i++) {
                aTransItems.get(i).setDuration_passed(weekspassed);//cal weeks passed
            }
        } catch (NullPointerException npe) {

        }
    }

    public double getDurationPassedOnReturn(Trans aTrans, Trans aRefTrans) {
        double weekspassed = 0;
        try {
            if (null != aTrans && null != aRefTrans) {
                double WeeksUsed = this.calcDurationReturn(aRefTrans.getFrom_date(), aTrans.getTransactionDate());
                double WeeksHired = aRefTrans.getDuration_value();
                weekspassed = WeeksUsed - WeeksHired;
                if (weekspassed < 0) {
                    weekspassed = 0;
                }
            }
        } catch (NullPointerException npe) {
        }
        return weekspassed;
    }

    public double getDurationPassedOnReturn(Date aReturnDate, Date aHireFromDate, double aWeeksHired) {
        double weekspassed = 0;
        try {
            if (null != aReturnDate && null != aHireFromDate) {
                double WeeksUsed = this.calcDurationReturn(aHireFromDate, aReturnDate);
                double WeeksHired = aWeeksHired;
                weekspassed = WeeksUsed - WeeksHired;
                if (weekspassed < 0) {
                    weekspassed = 0;
                }
            }
        } catch (NullPointerException npe) {
        }
        return weekspassed;
    }

    public void refreshCustomerBalances(Trans aTrans) {
        //receivable balances
        try {
            //aTrans.setBalance_receivable(new AccLedgerBean().getReceivableAccBalance(aTrans.getTransactorId()));
            aTrans.setBalance_receivable(new AccLedgerBean().getReceivableAccBalanceTrade(aTrans.getTransactorId()));
        } catch (Exception e) {
            aTrans.setBalance_receivable(0);
        }
        //prepaid income
        try {
            //aTrans.setDeposit_customer(new AccLedgerBean().getPrepaidIncomeAccBalance(aTrans.getTransactorId(), aTrans.getCurrencyCode()));
            aTrans.setDeposit_customer(new AccLedgerBean().getPrepaidIncomeAccBalanceTrade(aTrans.getTransactorId(), aTrans.getCurrencyCode()));
        } catch (Exception e) {
            aTrans.setDeposit_customer(0);
        }
    }

    public void refreshCustomerBalances2(Trans aTrans) {
        //receivable balances
        try {
            //aTrans.setBalance_receivable2(new AccLedgerBean().getReceivableAccBalance(aTrans.getBillTransactorId()));
            aTrans.setBalance_receivable2(new AccLedgerBean().getReceivableAccBalanceTrade(aTrans.getBillTransactorId()));
        } catch (Exception e) {
            aTrans.setBalance_receivable2(0);
        }
        //prepaid income
        try {
            //aTrans.setDeposit_customer2(new AccLedgerBean().getPrepaidIncomeAccBalance(aTrans.getBillTransactorId(), aTrans.getCurrencyCode()));
            aTrans.setDeposit_customer2(new AccLedgerBean().getPrepaidIncomeAccBalanceTrade(aTrans.getBillTransactorId(), aTrans.getCurrencyCode()));
        } catch (Exception e) {
            aTrans.setDeposit_customer2(0);
        }
    }

    public void refreshCustomerBalances3(Trans aTrans, long aTransactorId, String aCurrencyCode) {
        //receivable balances
        try {
            aTrans.setBalance_receivable(0);
            //aTrans.setBalance_receivable(new AccLedgerBean().getReceivableAccBalanceTrade(aTransId));
        } catch (Exception e) {
            aTrans.setBalance_receivable(0);
        }
        //prepaid income
        try {
            aTrans.setDeposit_customer(0);
            aTrans.setDeposit_customer(new AccLedgerBean().getPrepaidIncomeAccBalanceTrade(aTransactorId, aCurrencyCode));
        } catch (Exception e) {
            aTrans.setDeposit_customer(0);
        }
    }

    public void refreshCustomerBalances4(Pay aPay, long aTransactorId, String aCurrencyCode) {
        //receivable balances
//        try {
//            aTrans.setBalance_receivable(0);
//            //aTrans.setBalance_receivable(new AccLedgerBean().getReceivableAccBalanceTrade(aTransId));
//        } catch (Exception e) {
//            aTrans.setBalance_receivable(0);
//        }
        //prepaid income
        try {
            aPay.setCustomerDeposit(0);
            aPay.setCustomerDeposit(new AccLedgerBean().getPrepaidIncomeAccBalanceTrade(aTransactorId, aCurrencyCode));
        } catch (Exception e) {
            aPay.setCustomerDeposit(0);
        }
    }

    public void refreshSupplierBalances(Trans aTrans) {
        //payable balances
        try {
            //aTrans.setBalance_payable(new AccLedgerBean().getPayableAccBalance(aTrans.getTransactorId()));
            aTrans.setBalance_payable(new AccLedgerBean().getPayableAccBalanceTrade(aTrans.getTransactorId()));
        } catch (Exception e) {
            aTrans.setBalance_payable(0);
        }
        //prepaid expense
        try {
            //aTrans.setDeposit_supplier(new AccLedgerBean().getPrepaidExpenseAccBalance(aTrans.getTransactorId(), aTrans.getCurrencyCode()));
            aTrans.setDeposit_supplier(new AccLedgerBean().getPrepaidExpenseAccBalanceTrade(aTrans.getTransactorId(), aTrans.getCurrencyCode()));
        } catch (Exception e) {
            aTrans.setDeposit_supplier(0);
        }
    }

    public void refreshSupplierBalances2(Trans aTrans) {
        //payable balances
        try {
            //aTrans.setBalance_payable(new AccLedgerBean().getPayableAccBalance(aTrans.getTransactorId()));
            aTrans.setBalance_payable2(new AccLedgerBean().getPayableAccBalanceTrade(aTrans.getBillTransactorId()));
        } catch (Exception e) {
            aTrans.setBalance_payable2(0);
        }
        //prepaid expense
        try {
            //aTrans.setDeposit_supplier(new AccLedgerBean().getPrepaidExpenseAccBalance(aTrans.getTransactorId(), aTrans.getCurrencyCode()));
            aTrans.setDeposit_supplier2(new AccLedgerBean().getPrepaidExpenseAccBalanceTrade(aTrans.getBillTransactorId(), aTrans.getCurrencyCode()));
        } catch (Exception e) {
            aTrans.setDeposit_supplier(0);
        }
    }

    public void openChildCashDiscount() {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", false);
        options.put("resizable", false);
        options.put("width", 500);
        options.put("height", 200);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        options.put("scrollable", true);
        options.put("maximizable", true);
        options.put("dynamic", true);
        org.primefaces.PrimeFaces.current().dialog().openDynamic("CashDiscount", options, null);
    }

    public void refreshTransListQuickOrderManage(Trans aTrans) {
        //re-set navigation details to order
        new NavigationBean().setToSaleOrderQuick();

        this.ActionMessage = "";
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=11 AND transaction_reason_id=16";
        String wheresql = "";
        String ordersql = "";
        if (aTrans.getTransactionUserDetailId() > 0) {
            wheresql = wheresql + " AND transaction_user_detail_id=" + aTrans.getTransactionUserDetailId();
        }
        if (aTrans.getIs_processed() == 1 || aTrans.getIs_processed() == 0) {
            wheresql = wheresql + " AND is_processed=" + aTrans.getIs_processed();
        }
        if (aTrans.getIs_invoiced() == 1 || aTrans.getIs_invoiced() == 0) {
            wheresql = wheresql + " AND is_invoiced=" + aTrans.getIs_invoiced();
        }
        if (aTrans.getIs_paid() == 1 || aTrans.getIs_paid() == 0) {
            wheresql = wheresql + " AND is_paid=" + aTrans.getIs_paid();
        }
        if (aTrans.getIs_cancel() == 1 || aTrans.getIs_cancel() == 0) {
            wheresql = wheresql + " AND is_cancel=" + aTrans.getIs_cancel();
        }
        if (this.todayoryest == 1) {
            wheresql = wheresql + " AND transaction_date='" + new java.sql.Date(new CompanySetting().getCURRENT_SERVER_DATE().getTime()) + "'";
        }
        if (this.todayoryest == 2) {
            wheresql = wheresql + " AND transaction_date='" + new java.sql.Date(new UtilityBean().AddDays(new CompanySetting().getCURRENT_SERVER_DATE(), -1).getTime()) + "'";
        }
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND (store_id=" + aTrans.getStoreId() + " OR store2_id=" + aTrans.getStoreId() + ")";
        }
        try {
            if (null == aTrans.getCategory()) {
                //do nothing
            } else {
                if (aTrans.getCategory().getCategoryId() > 0) {
                    wheresql = wheresql + " AND transaction_id IN(" + "select distinct ti.transaction_id from transaction_item ti inner join item i on ti.item_id=i.item_id where i.category_id=" + aTrans.getCategory().getCategoryId() + ")";
                }
            }
        } catch (Exception e) {

        }
        if (aTrans.getLocation_id() > 0) {
            wheresql = wheresql + " AND location_id=" + aTrans.getLocation_id();
        }
        //wheresql = WhereAppend + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        sql = sql + wheresql + ordersql;
        //System.out.println("SQL:" + sql);
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            TransItemBean tib = new TransItemBean();

            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.updateLookup(trans);
                if (tib.getTransItemsString(trans.getTransactionId(), 0) == null) {
                    trans.setTransItemsString("");
                } else {
                    trans.setTransItemsString(tib.getTransItemsString(trans.getTransactionId(), 0));
                }
                this.TransList.add(trans);
            }
            //refresh summary total
            this.refreshOrderGrandTotal(this.TransList);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void refreshTransListQuickOrderDashboard(Trans aTrans) {
        //re-set navigation details to order
        //new NavigationBean().setToSaleOrderQuick();

        this.ActionMessage = "";
        ResultSet rs = null;
        this.TransList = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE transaction_type_id=11 AND transaction_reason_id=16";
        String wheresql = "";
        String ordersql = "";
        if (aTrans.getIs_processed() == 1 || aTrans.getIs_processed() == 0) {
            wheresql = wheresql + " AND is_processed=" + aTrans.getIs_processed();
        }
        if (aTrans.getIs_invoiced() == 1 || aTrans.getIs_invoiced() == 0) {
            wheresql = wheresql + " AND is_invoiced=" + aTrans.getIs_invoiced();
        }
        if (aTrans.getIs_paid() == 1 || aTrans.getIs_paid() == 0) {
            wheresql = wheresql + " AND is_paid=" + aTrans.getIs_paid();
        }
        if (aTrans.getIs_cancel() == 1 || aTrans.getIs_cancel() == 0) {
            wheresql = wheresql + " AND is_cancel=" + aTrans.getIs_cancel();
        }
        if (this.todayoryest == 1) {
            wheresql = wheresql + " AND transaction_date='" + new java.sql.Date(new CompanySetting().getCURRENT_SERVER_DATE().getTime()) + "'";
        }
        if (this.todayoryest == 2) {
            wheresql = wheresql + " AND transaction_date='" + new java.sql.Date(new UtilityBean().AddDays(new CompanySetting().getCURRENT_SERVER_DATE(), -1).getTime()) + "'";
        }
        if (aTrans.getStoreId() > 0) {
            wheresql = wheresql + " AND (store_id=" + aTrans.getStoreId() + " OR store2_id=" + aTrans.getStoreId() + ")";
        }
        try {
            if (null == aTrans.getCategory()) {
                //do nothing
            } else {
                if (aTrans.getCategory().getCategoryId() > 0) {
                    wheresql = wheresql + " AND transaction_id IN(" + "select distinct ti.transaction_id from transaction_item ti inner join item i on ti.item_id=i.item_id where i.category_id=" + aTrans.getCategory().getCategoryId() + ")";
                }
            }
        } catch (Exception e) {

        }
        //wheresql = WhereAppend + " AND transaction_date BETWEEN '" + new java.sql.Date(aTransBean.getDate1().getTime()) + "' AND '" + new java.sql.Date(aTransBean.getDate2().getTime()) + "'";
        ordersql = " ORDER BY add_date DESC,transaction_id DESC";
        sql = sql + wheresql + ordersql;
        //System.out.println("SQL:" + sql);
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            rs = ps.executeQuery();
            Trans trans = null;
            TransItemBean tib = new TransItemBean();
            while (rs.next()) {
                trans = new Trans();
                this.setTransFromResultset(trans, rs);
                this.updateLookup(trans);
                trans.setTransItemsString(tib.getTransItemsString(trans.getTransactionId(), 0));
                this.TransList.add(trans);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void updateLookup(Trans aTrans) {
        if (null != aTrans) {
            try {
                aTrans.setStoreName(new StoreBean().getStore(aTrans.getStoreId()).getStoreName());
            } catch (Exception e) {
                aTrans.setStoreName("");
            }
            try {
                aTrans.setStore2Name(new StoreBean().getStore(aTrans.getStore2Id()).getStoreName());
            } catch (Exception e) {
                aTrans.setStore2Name("");
            }
            try {
                aTrans.setTransactorName(new TransactorBean().getTransactor(aTrans.getTransactorId()).getTransactorNames());
            } catch (Exception e) {
                aTrans.setTransactorName("");
            }
            try {
                aTrans.setLocation_name(new LocationBean().getLocation(aTrans.getLocation_id()).getLocationName());
            } catch (Exception e) {
                aTrans.setLocation_name("");
            }
            try {
                aTrans.setTransactionUserDetailName(new UserDetailBean().getUserDetail(aTrans.getTransactionUserDetailId()).getUserName());
            } catch (Exception e) {
                aTrans.setTransactionUserDetailName("");
            }
            //get tax invoice number
            String DeviceNo = new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "TAX_BRANCH_NO").getParameter_value();
            if (DeviceNo.length() > 0 && (aTrans.getTransactionTypeId() == 2 || aTrans.getTransactionTypeId() == 82 || aTrans.getTransactionTypeId() == 83)) {
                Transaction_tax_map ttm = new Transaction_tax_mapBean().getTransaction_tax_map(aTrans.getTransactionId(), aTrans.getTransactionTypeId());
                if (null != ttm) {
                    aTrans.setReference_number_tax(ttm.getReference_number_tax());
                    aTrans.setTransaction_number_tax(ttm.getTransaction_number_tax());
                    aTrans.setVerification_code_tax(ttm.getVerification_code_tax());
                    aTrans.setQr_code_tax(ttm.getQr_code_tax());
                    aTrans.setFdn_ref(ttm.getFdn_ref());
                }
            }
        }
    }

    public void closeChildCashDiscount() {
        //org.primefaces.context.RequestContext.getCurrentInstance().closeDialog(this.SelectedTrans);
        org.primefaces.PrimeFaces.current().dialog().closeDynamic(this.SelectedTrans);
    }

    public void closeSalesOrderParentInvoice() {
        try {
            //org.primefaces.context.RequestContext.getCurrentInstance().closeDialog("SaleOrderParentInvoiceTrans");
            org.primefaces.PrimeFaces.current().dialog().closeDynamic("SaleOrderParentInvoiceTrans");
        } catch (Exception e) {
            //do nothing
        }
    }

    public void onChildCashDiscount(SelectEvent selectEvent) {
        this.SelectedTrans = (Trans) selectEvent.getObject();
    }

    public void RefreshCurrentUser(UserDetail aUserDeatail, Store aStore) {
        //create seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(true);
        //set LoggedIn/Current User in session
        httpSession.setAttribute("CURRENT_USER", aUserDeatail);
        //set LoggedIn/Current store in session
        httpSession.setAttribute("CURRENT_STORE", aStore);
        //Set user rights for all the groups the user belongs to in session
        httpSession.setAttribute("CURRENT_GROUP_RIGHTS", new GroupRightBean().getCurrentGroupRights(aStore.getStoreId(), aUserDeatail.getUserDetailId()));
        try {
            menuItemBean.getMenuItemObj().setCURRENT_USER(aUserDeatail);
        } catch (NullPointerException npe) {
        }
    }

    public void quickOrderActions(Trans aTrans, List<Trans> aActiveTranss, String aAction) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        List<Trans> at = aActiveTranss;
        int ListItemIndex = 0;
        int ListItemNo = at.size();
        this.ActionMessage = "";
        int UserCodeNeeded = 0;
        try {
            UserCodeNeeded = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("ORDER", "USER_CODE_NEEDED").getParameter_value());
        } catch (Exception e) {
            //do nothing
        }
        UserDetail ud;
        if (aTrans.getUser_code().length() > 0) {
            ud = new UserDetailBean().getUserDetailWithTransCode(aTrans.getUser_code());
        } else {
            ud = new GeneralUserSetting().getCurrentUser();
        }
        if (UserCodeNeeded == 1 && aTrans.getUser_code().length() == 0) {
            this.ActionMessage = "Enter User Code";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("User", this.ActionMessage));
        } else if (null == ud) {
            this.ActionMessage = "Invalid User";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("User", this.ActionMessage));
        } else {
            //this.RefreshCurrentUser(ud, new GeneralUserSetting().getCurrentStore());
            switch (aAction) {
                case "Process":
                    while (ListItemIndex < ListItemNo) {
                        if (at.get(ListItemIndex).getIs_selected() == 1) {
                            this.ActionMessage = this.processOrder(at.get(ListItemIndex), ud);
                            if (this.ActionMessage.length() > 0) {
                                break;
                            } else {
                                at.get(ListItemIndex).setIs_processed(1);
                                this.clearIs_selected(aActiveTranss);
                            }
                        }
                        ListItemIndex = ListItemIndex + 1;
                    }
                    break;
                case "Invoice":
                    this.OutputNumber = 1;
                    int count = this.countSelected(at);
                    if (count == 1) {
                        int selectedindex = this.getSelectedIndex(at);
                        if (at.get(selectedindex).getIs_invoiced() == 1) {
                            this.ActionMessage = "Selected Order is already Invoiced";
                        } else if (at.get(selectedindex).getIs_cancel() == 1) {
                            this.ActionMessage = "Selected Order is already Cancelled";
                        } else if (at.get(selectedindex).getStoreId() != new GeneralUserSetting().getCurrentStore().getStoreId()) {
                            this.ActionMessage = "Order and Invoice must be in the same " + CompanySetting.getStoreEquivName();
                        } else {
                            this.openOrderChildSalesInvoice(at.get(selectedindex).getTransactionId(), ud);
                        }
                    } else if (count == 0) {
                        this.ActionMessage = "Select Order to Invoice";
                    } else {
                        this.ActionMessage = "Select only one Order to Invoice";
                    }
                    if (this.ActionMessage.length() > 0) {
                        break;
                    } else {
                    }
                    break;
                case "Pay":
                    this.OutputNumber = 2;
                    int countsel = this.countSelected(at);
                    if (countsel == 1) {
                        int selectedindex = this.getSelectedIndex(at);
                        if (at.get(selectedindex).getIs_invoiced() == 1) {
                            this.ActionMessage = "Use Cash Receipt Window to pay Invoiced Order";
                        } else if (at.get(selectedindex).getIs_cancel() == 1) {
                            this.ActionMessage = "Selected Order is already Cancelled";
                        } else if (at.get(selectedindex).getIs_paid() == 1) {
                            this.ActionMessage = "Selected Order is already Paid";
                        } else if (at.get(selectedindex).getStoreId() != new GeneralUserSetting().getCurrentStore().getStoreId()) {
                            this.ActionMessage = "Order and Payment Must be in the same " + CompanySetting.getStoreEquivName();
                        } else {
                            this.getOrderSalesInvoice(at.get(selectedindex).getTransactionId(), ud);
                            this.TransChild.setTransactionRef(at.get(selectedindex).getTransactionNumber());
                            this.TransChild.setChangeAmount(0);
                            this.TransChild.setPayMethod(1);
                            int ChildAccountId = 0;
                            try {
                                ChildAccountId = new AccChildAccountBean().getAccChildAccountsForCashReceipt(this.TransChild.getCurrencyCode(), 1, new GeneralUserSetting().getCurrentStore().getStoreId(), ud.getUserDetailId()).get(0).getAccChildAccountId();
                            } catch (Exception e) {
                                ChildAccountId = 0;
                            }
                            this.TransChild.setAccChildAccountId(ChildAccountId);
                            //check - save invoice with full payment
                            if (null == this.TransChild || this.ActiveTransItemsChild.size() <= 0) {
                                this.ActionMessage = "Order Not Paid";
                            } else if (ChildAccountId == 0) {
                                this.ActionMessage = "Select Cash Account to receive cash";
                            } else {
                                //save
                                this.setAutoPrintAfterSave(false);
                                long SavedTransId = this.saveTransCECE("PARENT", new GeneralUserSetting().getCurrentStore().getStoreId(), 2, 2, "", this.TransChild, this.ActiveTransItemsChild, null, null, null, null, null, null);
                                if (SavedTransId > 0) {
                                    //update is_invoiced
                                    Trans InvoiceTrans = this.getTrans(SavedTransId);
                                    this.updateOrderIsInvoicedPaid(at.get(selectedindex).getTransactionId(), InvoiceTrans.getTransactionNumber(), null);
                                    at.get(selectedindex).setIs_invoiced(1);
                                    at.get(selectedindex).setIs_paid(1);
                                    at.get(selectedindex).setIs_selected(0);
                                    at.get(selectedindex).setTransactionComment(InvoiceTrans.getTransactionNumber());
                                    org.primefaces.PrimeFaces.current().executeScript("doPrintHiddenClick()");
                                }
                            }
                        }
                    } else if (countsel == 0) {
                        this.ActionMessage = "Select Order to Pay";
                    } else {
                        this.ActionMessage = "Select only One order to Pay";
                    }
                    if (this.ActionMessage.length() > 0) {
                        break;
                    } else {
                    }
                    break;
                case "Cancel":
                    while (ListItemIndex < ListItemNo) {
                        if (at.get(ListItemIndex).getIs_selected() == 1) {
                            this.ActionMessage = this.cancelOrder(at.get(ListItemIndex), ud);
                            if (this.ActionMessage.length() > 0) {
                                break;
                            } else {
                                at.get(ListItemIndex).setIs_cancel(1);
                                this.clearIs_selected(aActiveTranss);
                            }
                        }
                        ListItemIndex = ListItemIndex + 1;
                    }
                    break;
                case "Merge":
                    //come back to check for CURRENCY being the same , please!
                    int countselected = this.countSelected(at);
                    int countselectedCancel = this.countSelectedStatus(at, "is_cancel");
                    int countselectedProcessed = this.countSelectedStatus(at, "is_processed");
                    int countselectedInvoiced = this.countSelectedStatus(at, "is_invoiced");
                    int countselectedStoresFrom = this.countSelectedStores(at, "FROM");
                    int countselectedStoresTo = this.countSelectedStores(at, "TO");
                    if (countselected > 1) {
                        if (countselectedCancel > 0) {
                            this.ActionMessage = "Merging with Cancelled Order Failed";
                        } else if (countselectedInvoiced > 0) {
                            this.ActionMessage = "Merging with Invoiced Order Failed";
                        } else if (countselectedStoresFrom > 1) {
                            this.ActionMessage = "Orders to Merge should be From the same " + CompanySetting.getStoreEquivName();
                        } else if (countselectedStoresTo > 1) {
                            this.ActionMessage = "Orders to Merge should be To the same " + CompanySetting.getStoreEquivName();
                        } else {
                            Trans FirstTrans = this.getFirstTransFromSelected(this.getSelectedTranss(at));
                            String TranssIDsExcFirst = this.getSelectedTranssIDsExcFirst(FirstTrans, at);
                            String TransItemsIDsExcFirst = new TransItemBean().getTransItemssIDsByTransIDs(TranssIDsExcFirst);
                            if (new TransItemBean().overrideItemsByTransItemsId(11, 16, FirstTrans.getTransactionId(), TransItemsIDsExcFirst) == 1) {
                                int deleted = this.deleteTranssByIDs(TranssIDsExcFirst);
                                List<TransItem> MergedTransItems = new TransItemBean().getTransItemsByTransactionId(FirstTrans.getTransactionId());
                                this.setTransTotalsAndUpdateCEC(11, 16, FirstTrans, MergedTransItems);
                                this.updateTransCEC("PARENT", FirstTrans.getStoreId(), 11, 16, "", FirstTrans, MergedTransItems, null);
                                this.clickSearchButton();
                            }
                        }
                    } else if (countselected == 1) {
                        this.ActionMessage = "Select more than 1 order to Merge";
                    } else {
                        this.ActionMessage = "Select only orders to Merge";
                    }
                    if (this.ActionMessage.length() > 0) {
                        break;
                    } else {
                    }
                    break;
                case "Edit":
                    int countedit = this.countSelected(at);
                    if (countedit == 1) {
                        int selectedindex = this.getSelectedIndex(at);
                        if (at.get(selectedindex).getIs_invoiced() == 1) {
                            this.ActionMessage = "Selected Order is Invoiced and cannot be edited";
                        } else if (at.get(selectedindex).getIs_cancel() == 1) {
                            this.ActionMessage = "Selected Order is Cancelled and it cannot be edited...";
                        } else {
                            //update session
                            FacesContext context = FacesContext.getCurrentInstance();
                            HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
                            HttpSession httpSession = request.getSession(false);
                            httpSession.setAttribute("ORDER_FOR_EDIT", at.get(selectedindex));
                            //navigate
                            FacesContext fc = FacesContext.getCurrentInstance();
                            ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) fc.getApplication().getNavigationHandler();
                            nav.performNavigation("SaleOrderQuickTrans?faces-redirect=true");
                        }
                    } else if (countedit == 0) {
                        this.ActionMessage = "Select order to Edit";
                    } else {
                        this.ActionMessage = "Select only one order to Edit";
                    }
                    if (this.ActionMessage.length() > 0) {
                        break;
                    } else {
                    }
                    break;
                case "Split":
                    int selectedindex = this.getSelectedIndex(at);
                    Trans t = null;
                    if (at.get(selectedindex).getIs_selected() == 1) {
                        t = at.get(selectedindex);
                        t.setaTransItemsDetailsList(new TransItemBean().getTransItemsList(t.getTransactionId(), 0));
                        t.setTransItemsList(new TransItemBean().getItemsList(t.getTransactionId(), 0));
                        this.ActionMessage = "";
                        reviewViewOrderForSpliting(t);
                    }
                    break;
                case "Print":
                    while (ListItemIndex < ListItemNo) {
                        if (at.get(ListItemIndex).getIs_selected() == 1) {

                        }
                        ListItemIndex = ListItemIndex + 1;
                    }
                    break;
            }
            if (this.ActionMessage.length() > 0) {
                this.ActionMessage = ub.translateWordsInText(BaseName, this.ActionMessage);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Feedback Information", this.ActionMessage));
            }
        }
    }

    public void clickSearchButton() {
        org.primefaces.PrimeFaces.current().executeScript("doSearchClick()");
    }

    public int getSelectedIndex(List<Trans> aActiveTranss) {
        List<Trans> at = aActiveTranss;
        int ListItemIndex = 0;
        int ListItemNo = at.size();
        while (ListItemIndex < ListItemNo) {
            if (at.get(ListItemIndex).getIs_selected() == 1) {
                break;
            }
            ListItemIndex = ListItemIndex + 1;
        }
        return ListItemIndex;
    }

    public Trans getSelectedTransFromAll(List<Trans> aAllTranss) {
        List<Trans> at = aAllTranss;
        int ListItemIndex = 0;
        int ListItemNo = at.size();
        Trans trans = null;
        while (ListItemIndex < ListItemNo) {
            if (at.get(ListItemIndex).getIs_selected() == 1) {
                trans = at.get(ListItemIndex);
                break;
            }
            ListItemIndex = ListItemIndex + 1;
        }
        return trans;
    }

    public Trans getFirstTransFromSelected(List<Trans> aSelectedTranss) {
        List<Trans> at = aSelectedTranss;
        Trans trans = at.get(0);
        return trans;
    }

    public List<Trans> getSelectedTranss(List<Trans> aActiveTranss) {
        List<Trans> at = aActiveTranss;
        List<Trans> SelTranss = new ArrayList<>();
        int ListItemIndex = 0;
        int ListItemNo = at.size();
        while (ListItemIndex < ListItemNo) {
            if (at.get(ListItemIndex).getIs_selected() == 1) {
                SelTranss.add(at.get(ListItemIndex));
            }
            ListItemIndex = ListItemIndex + 1;
        }
        return SelTranss;
    }

    public String getSelectedTranssIDsExcFirst(Trans aFirstTrans, List<Trans> aActiveTranss) {
        List<Trans> at = aActiveTranss;
        String SelTranssIDsExcFirst = "";
        int ListItemIndex = 0;
        int ListItemNo = at.size();
        while (ListItemIndex < ListItemNo) {
            if (at.get(ListItemIndex).getIs_selected() == 1) {
                if (at.get(ListItemIndex).getTransactionId() == aFirstTrans.getTransactionId()) {
                    //skip
                } else {
                    if (SelTranssIDsExcFirst.length() == 0) {
                        SelTranssIDsExcFirst = "" + at.get(ListItemIndex).getTransactionId();
                    } else {
                        SelTranssIDsExcFirst = SelTranssIDsExcFirst + "," + at.get(ListItemIndex).getTransactionId();
                    }
                }
            }
            ListItemIndex = ListItemIndex + 1;
        }
        return SelTranssIDsExcFirst;
    }

    public int countSelected(List<Trans> aActiveTranss) {
        List<Trans> at = aActiveTranss;
        int ListItemIndex = 0;
        int ListItemNo = at.size();
        int counter = 0;
        while (ListItemIndex < ListItemNo) {
            if (at.get(ListItemIndex).getIs_selected() == 1) {
                counter = counter + 1;
            }
            ListItemIndex = ListItemIndex + 1;
        }
        return counter;
    }

    public int countSelectedStatus(List<Trans> aActiveTranss, String aStatus) {
        List<Trans> at = aActiveTranss;
        int ListItemIndex = 0;
        int ListItemNo = at.size();
        int counter = 0;
        while (ListItemIndex < ListItemNo) {
            switch (aStatus) {
                case "is_invoiced":
                    if (at.get(ListItemIndex).getIs_selected() == 1 && at.get(ListItemIndex).getIs_invoiced() == 1) {
                        counter = counter + 1;
                    }
                    break;
                case "is_processed":
                    if (at.get(ListItemIndex).getIs_selected() == 1 && at.get(ListItemIndex).getIs_processed() == 1) {
                        counter = counter + 1;
                    }
                    break;
                case "is_cancel":
                    if (at.get(ListItemIndex).getIs_selected() == 1 && at.get(ListItemIndex).getIs_cancel() == 1) {
                        counter = counter + 1;
                    }
                    break;
            }
            ListItemIndex = ListItemIndex + 1;
        }
        return counter;
    }

    public int countSelectedStores(List<Trans> aActiveTranss, String aFromOrTo) {
        List<Trans> at = aActiveTranss;
        int ListItemIndex = 0;
        int ListItemNo = at.size();
        int counter = 0;
        int prevStore = 0;
        while (ListItemIndex < ListItemNo) {
            switch (aFromOrTo) {
                case "FROM":
                    if (at.get(ListItemIndex).getIs_selected() == 1) {
                        if (at.get(ListItemIndex).getStoreId() != prevStore) {
                            counter = counter + 1;
                        }
                        prevStore = at.get(ListItemIndex).getStoreId();
                    }
                    break;
                case "TO":
                    if (at.get(ListItemIndex).getIs_selected() == 1) {
                        if (at.get(ListItemIndex).getStore2Id() != prevStore) {
                            counter = counter + 1;
                        }
                        prevStore = at.get(ListItemIndex).getStore2Id();
                    }
                    break;
            }
            ListItemIndex = ListItemIndex + 1;
        }
        return counter;
    }

    public void clearIs_selected(List<Trans> aActiveTranss) {
        List<Trans> at = aActiveTranss;
        int ListItemIndex = 0;
        int ListItemNo = at.size();
        while (ListItemIndex < ListItemNo) {
            at.get(ListItemIndex).setIs_selected(0);
            ListItemIndex = ListItemIndex + 1;
        }
    }

    public String processOrder(Trans aTrans, UserDetail aUserDetail) {
        String msg = "";
        String sql = "UPDATE transaction SET is_processed=1 WHERE transaction_id=" + aTrans.getTransactionId();
        if (aTrans.getIs_processed() == 1) {
            msg = "Order already PROCESSED!";
        } else if (aTrans.getIs_cancel() == 1) {
            msg = "Order already CANCELLED cannot be PROCESSED!";
        } else {
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.executeUpdate();
            } catch (Exception e) {
                msg = "ERROR occured, order not PROCESSED...";
                LOGGER.log(Level.ERROR, e);
            }
        }
        return msg;
    }

    public void updateOrderIsInvoiced(long aOrderId, String aInvoiceNumber, UserDetail aUserDetail) {
        String sql = "UPDATE transaction SET is_invoiced=1, transaction_comment='" + aInvoiceNumber + "' WHERE transaction_id=" + aOrderId;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void updateOrderIsInvoicedPaid(long aOrderId, String aInvoiceNumber, UserDetail aUserDetail) {
        String sql = "UPDATE transaction SET is_invoiced=1,is_paid=1,transaction_comment='" + aInvoiceNumber + "' WHERE transaction_id=" + aOrderId;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public String cancelOrder(Trans aTrans, UserDetail aUserDetail) {
        String msg = "";
        String sql = "UPDATE transaction SET is_cancel=1 WHERE transaction_id=" + aTrans.getTransactionId();
        if (aTrans.getIs_cancel() == 1) {
            msg = "Order already CANCELLED!";
        } else if (aTrans.getIs_invoiced() == 1) {
            msg = "Order already INVOICED cannot be cancelled!";
        } else {
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                ps.executeUpdate();
            } catch (Exception e) {
                msg = "ERROR occured, order not CANCELLED...";
                LOGGER.log(Level.ERROR, e);
            }
        }
        return msg;
    }

    public void updateOrderStatus(long aOrderId, String aStatusColumnName, int aStatusValue) {
        //aStatus=is_invoiced,is_cancel,is_processed,is_pad
        String sql = "UPDATE transaction SET " + aStatusColumnName + "=" + aStatusValue + " WHERE transaction_id=" + aOrderId;
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public int deleteTranssByIDs(String aAffectedTranssIDs) {
        int passed = 0;
        String sql = "DELETE FROM transaction WHERE transaction_id>0 AND transaction_id IN(" + aAffectedTranssIDs + ")";
        try (
                Connection conn = DBConnection.getMySQLConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.executeUpdate();
            passed = 1;
        } catch (Exception e) {
            passed = 0;
            LOGGER.log(Level.ERROR, e);
        }
        return passed;
    }

    public String getDisplayCategory(Category aCategory) {
        String outc = "Category";
        try {
            if (null != aCategory) {
                if (aCategory.getCategoryName().length() > 0) {
                    outc = aCategory.getCategoryName();
                }
            }
        } catch (Exception e) {
        }
        return outc;
    }

    public String getDisplayStore(int aStoreId) {
        String outc = CompanySetting.getStoreEquivName();
        try {
            if (aStoreId > 0) {
                Store st = new StoreBean().getStore(aStoreId);
                outc = st.getStoreName();
            }
        } catch (Exception e) {
        }
        return outc;
    }

    public String getDisplayUserDetail(int aUserDetailId) {
        String outc = "User";
        try {
            if (aUserDetailId > 0) {
                UserDetail ud = new UserDetailBean().getUserDetail(aUserDetailId);
                outc = ud.getUserName();
            }
        } catch (Exception e) {
        }
        return outc;
    }

    public String getDisplayLocation(int aLocationId) {
        String outc = "Loc";
        try {
            if (aLocationId > 0) {
                Location loc = new LocationBean().getLocation(aLocationId);
                outc = loc.getLocationName();
            }
        } catch (Exception e) {
        }
        return outc;
    }

    public void refreshOrderGrandTotal(List<Trans> aActiveTranss) {
        List<Trans> ati = aActiveTranss;
        int ListItemIndex = 0;
        int ListItemNo = ati.size();
        double SubT = 0;
        while (ListItemIndex < ListItemNo) {
            SubT = SubT + ati.get(ListItemIndex).getGrandTotal();
            ListItemIndex = ListItemIndex + 1;
        }
        this.OrderSummaryTotal = SubT;
    }

    public void initTransactorMerge(Trans aTrans, Transactor aTransactor, Transactor aBillTransactor) {
        if (null == aTrans) {
            //do nothing
        } else {
            this.clearTrans(aTrans);
            new TransactorBean().clearTransactor(aTransactor);
            new TransactorBean().clearTransactor(aBillTransactor);
            aTrans.setCurrencyCode(new AccCurrencyBean().getLocalCurrency().getCurrencyCode());
        }
    }

    public double getItemSummaryTotalAmount(List<TransItem> transItem) {
        int totalAmount = 0;
        for (TransItem ti : transItem) {
            totalAmount += ti.getAmountIncVat();
        }
        return totalAmount;
    }

    public void setTransItemUnit(TransItem transitem) {
        Item_unit iu = new ItemBean().getItemUnitFrmDb(transitem.getItemId(), transitem.getUnit_id());
        String sql = "SELECT  ti.item_id, u.unit_id, u.unit_symbol, i.description\n"
                + "FROM transaction_item ti \n"
                + "INNER JOIN transaction_item_unit tiu ON ti.transaction_item_id = tiu.transaction_item_id\n"
                + "INNER JOIN unit u ON tiu.unit_id = u.unit_id\n"
                + "INNER JOIN item i ON ti.item_id = i.item_id\n"
                + "Where tiu.transaction_item_id =" + transitem.getTransactionItemId();

        ResultSet rs = null;
        if (transitem.getTransactionItemId() > 0) {
            try (
                    Connection conn = DBConnection.getMySQLConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);) {
                rs = ps.executeQuery();
                while (rs.next()) {
                    transitem.setUnitSymbol(rs.getString("unit_symbol"));
                    transitem.setUnit_id(rs.getInt("unit_id"));
                    transitem.setDescription(rs.getString("description"));
                }
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    //by david for quick order split
    public void reviewViewOrderForSpliting(Trans aOrderTrans) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        List<Item> iL = aOrderTrans.getTransItemsList();
        Gson gson = new Gson();
        setTransObj(aOrderTrans);
        setNewTransAtSplit(aOrderTrans);
        this.setActionMessage("");

        try {
            this.setOrderItemList(aOrderTrans.getTransItemsList());
            this.setTransItemList(aOrderTrans.getaTransItemsDetailsList());
            this.setSelectedTransItem(new TransItem());
            this.selectedTransItemsList = new ArrayList();

            //validations
            if (aOrderTrans.getIs_paid() == 1) {
                FacesContext.getCurrentInstance().addMessage("Retrieve PO", new FacesMessage(ub.translateWordsInText(BaseName, "Order is already paid")));
                return;
            }
            if (aOrderTrans.getIs_cancel() == 1) {
                FacesContext.getCurrentInstance().addMessage("Retrieve PO", new FacesMessage(ub.translateWordsInText(BaseName, "This order is canceled")));
                return;
            }
            if (aOrderTrans.getIs_invoiced() == 1) {
                FacesContext.getCurrentInstance().addMessage("Retrieve PO", new FacesMessage(ub.translateWordsInText(BaseName, "Can\\'t split an invoiced order.")));
                return;
            }
            setOriginalTransItemJsonArray(new JsonArray());
            setOriginalStaticTransItemJsonArray(new JsonArray());
            if ((this.getTransItemList() != null) && (this.getOrderItemList() != null)) {
                if (this.getTransItemList().size() > 0) {
                    for (TransItem titm : this.getTransItemList()) {
                        // for (Item it : iL) {
                        // if (titm.getItemId() == (it.getItemId())) {
                        setTransItemUnit(titm);
                        getOriginalTransItemJsonArray().add(new JsonParser().parse(gson.toJson(titm)).getAsJsonObject());
                        getOriginalStaticTransItemJsonArray().add(new JsonParser().parse(gson.toJson(titm)).getAsJsonObject());
                        //}
                        //}
                    }
                    org.primefaces.PrimeFaces.current().executeScript("PF('sbarReviewOrder').show()");
                }
                this.setSelectedTransItemsList(new ArrayList<TransItem>());
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void onRowSelect(SelectEvent event) {
        Gson gson = new Gson();
        double originalValue = 0.0;
        boolean exists = true;
        try {
            TransItem tit = (TransItem) event.getObject();
            if (this.getSelectedTransItemsList() != null) {
                if (this.getSelectedTransItemsList().size() > 0) {
                    for (TransItem t : this.getSelectedTransItemsList()) {
                        if (t.getTransactionItemId() != tit.getTransactionItemId() && t.getUnit_id() != tit.getUnit_id()) {
                            exists = false;
                        }
                    }
                } else {
                    if (tit.getItemQty() > 0) {
                        tit.setItemQty(0);
                        this.getSelectedTransItemsList().add(tit);
                        this.setTransTotalsAndUpdateCEC(getNewTransAtSplit().getTransactionTypeId(), getNewTransAtSplit().getTransactionReasonId(), getNewTransAtSplit(), this.getSelectedTransItemsList());
                    }
                }
            }

            if (!exists) {
                if (tit.getItemQty() > 0) {
                    tit.setItemQty(0);
                    this.getSelectedTransItemsList().add(tit);
                    this.setTransTotalsAndUpdateCEC(getNewTransAtSplit().getTransactionTypeId(), getNewTransAtSplit().getTransactionReasonId(), getNewTransAtSplit(), this.getSelectedTransItemsList());
                }
            }

            //qty changes
            for (JsonElement transIt : this.getOriginalTransItemJsonArray()) {
                JsonObject trans = transIt.getAsJsonObject();
                if (tit.getTransactionItemId() == trans.get("TransactionItemId").getAsLong() && tit.getUnit_id() == trans.get("unit_id").getAsLong()) {
                    new TransItemBean().addQtyTransItemCEC(getNewTransAtSplit().getTransactionTypeId(), getNewTransAtSplit().getTransactionReasonId(), getNewTransAtSplit(), getSelectedTransItemsList(), tit);
                    originalValue = trans.get("ItemQty").getAsDouble();
                    trans.addProperty("ItemQty", originalValue - 1);//update value in the json arry
                    trans.addProperty("AmountIncVat", trans.get("AmountIncVat").getAsDouble() - tit.getUnitPriceIncVat());
                    trans.addProperty("Amount", trans.get("Amount").getAsDouble() - tit.getUnitPrice());
                    trans.addProperty("AmountExcVat", trans.get("AmountExcVat").getAsDouble() - tit.getUnitPriceExcVat());
                    transIt = gson.fromJson(trans.toString(), JsonElement.class);
                    // this._addQtyTransItemCEC(getNewTransAtSplit().getTransactionTypeId(), getNewTransAtSplit().getTransactionReasonId(), getNewTransAtSplit(), getSelectedTransItemsList(), tit);

                }
            }
            this.getTransItemList().clear();
            for (JsonElement transIt : this.getOriginalTransItemJsonArray()) {
                TransItem itm = gson.fromJson(transIt, TransItem.class);
                if (itm.getItemQty() < 0) {
                    itm.setItemQty(0);
                }
                this.getTransItemList().add(itm);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void _addQtyTransItemCEC(int aTransTypeId, int aTransReasonId, Trans aTrans, List<TransItem> aActiveTransItems, TransItem ti) {

        Gson gson = new Gson();
        double originalValue = 0.0;

        for (JsonElement transIt : this.getOriginalTransItemJsonArray()) {
            JsonObject trans = transIt.getAsJsonObject();
            for (JsonElement statictransIt : this.getOriginalStaticTransItemJsonArray()) {
                JsonObject statictrans = statictransIt.getAsJsonObject();
                if (ti.getTransactionItemId() == trans.get("TransactionItemId").getAsLong() && ti.getUnit_id() == trans.get("unit_id").getAsLong() && trans.get("TransactionItemId").getAsLong() == statictrans.get("TransactionItemId").getAsLong()) {
                    if (ti.getItemQty() + 1 <= statictrans.get("ItemQty").getAsDouble() && -1 <= ti.getItemQty() - 1) {
                        new TransItemBean().addQtyTransItemCEC(aTransTypeId, aTransReasonId, aTrans, aActiveTransItems, ti);
                        originalValue = trans.get("ItemQty").getAsDouble();
                        trans.addProperty("ItemQty", originalValue - 1);//update value in the json arry
                        trans.addProperty("AmountIncVat", trans.get("AmountIncVat").getAsDouble() - ti.getUnitPriceIncVat());
                        trans.addProperty("Amount", trans.get("Amount").getAsDouble() - ti.getUnitPrice());
                        trans.addProperty("AmountExcVat", trans.get("AmountExcVat").getAsDouble() - ti.getUnitPriceExcVat());
                        transIt = gson.fromJson(trans.toString(), JsonElement.class);
                        break;
                    }
                }
            }
        }
        this.getTransItemList().clear();

        for (JsonElement transIt : this.getOriginalTransItemJsonArray()) {
            TransItem itm = gson.fromJson(transIt, TransItem.class);
            this.getTransItemList().add(itm);
        }
    }

    public void _subtractQtyTransItemCEC(int aTransTypeId, int aTransReasonId, Trans aTrans, List<TransItem> aActiveTransItems, TransItem ti) {
        Gson gson = new Gson();
        double originalValue = 0.0;

        for (JsonElement transIt : this.getOriginalTransItemJsonArray()) {
            JsonObject trans = transIt.getAsJsonObject();
            for (JsonElement statictransIt : this.getOriginalStaticTransItemJsonArray()) {
                JsonObject statictrans = statictransIt.getAsJsonObject();
                if (ti.getTransactionItemId() == trans.get("TransactionItemId").getAsLong() && ti.getUnit_id() == trans.get("unit_id").getAsLong() && trans.get("TransactionItemId").getAsLong() == statictrans.get("TransactionItemId").getAsLong()) {
                    if (ti.getItemQty() <= statictrans.get("ItemQty").getAsDouble() && 0 <= ti.getItemQty() - 1) {
                        new TransItemBean().subtractQtyTransItemCEC(aTransTypeId, aTransReasonId, aTrans, aActiveTransItems, ti);
                        originalValue = trans.get("ItemQty").getAsDouble();
                        trans.addProperty("ItemQty", originalValue + 1);//update value in the json arry
                        trans.addProperty("AmountIncVat", trans.get("AmountIncVat").getAsDouble() + ti.getUnitPriceIncVat());
                        trans.addProperty("Amount", trans.get("Amount").getAsDouble() + ti.getUnitPrice());
                        trans.addProperty("AmountExcVat", trans.get("AmountExcVat").getAsDouble() + ti.getUnitPriceExcVat());
                        transIt = gson.fromJson(trans.toString(), JsonElement.class);
                        break;
                    }
                }
            }
        }
        this.getTransItemList().clear();

        for (JsonElement transIt : this.getOriginalTransItemJsonArray()) {
            TransItem itm = gson.fromJson(transIt, TransItem.class);
            this.getTransItemList().add(itm);
        }
    }

    public void _editTransItemCEC(int aTransTypeId, int aTransReasonId, String aSaleType, Trans aTrans, List<TransItem> aActiveTransItems, TransItem ti) {
        Gson gson = new Gson();
        boolean isQtyRight = false;
        new TransItemBean().editTransItemCEC(aTransTypeId, aTransReasonId, "", aTrans, aActiveTransItems, ti);
        for (JsonElement transIt : this.getOriginalTransItemJsonArray()) {
            JsonObject trans = transIt.getAsJsonObject();
            if (ti.getTransactionItemId() == trans.get("TransactionItemId").getAsLong()) {

                //getting the original object values 
                for (JsonElement statictransIt : this.getOriginalStaticTransItemJsonArray()) {
                    JsonObject statictrans = statictransIt.getAsJsonObject();
                    if (ti.getTransactionItemId() == statictrans.get("TransactionItemId").getAsLong() && ti.getUnit_id() == trans.get("unit_id").getAsLong()) {
                        if (statictrans.get("ItemQty").getAsDouble() - ti.getItemQty() >= 0 || statictrans.get("ItemQty").getAsDouble() - ti.getItemQty() > statictrans.get("ItemQty").getAsDouble()) {
                            trans.addProperty("ItemQty", statictrans.get("ItemQty").getAsDouble() - ti.getItemQty());//update value in the json arry
                            trans.addProperty("AmountIncVat", statictrans.get("AmountIncVat").getAsDouble() - ti.getAmountIncVat());
                            trans.addProperty("Amount", statictrans.get("Amount").getAsDouble() - ti.getAmount());
                            trans.addProperty("AmountExcVat", statictrans.get("AmountExcVat").getAsDouble() - ti.getAmountExcVat());
                            transIt = gson.fromJson(trans.toString(), JsonElement.class);
                        } else {
                            ti.setItemQty(statictrans.get("ItemQty").getAsDouble());
                            ti.setAmountIncVat(statictrans.get("AmountIncVat").getAsDouble());
                            ti.setAmount(statictrans.get("Amount").getAsDouble());
                            ti.setAmountExcVat(statictrans.get("AmountExcVat").getAsDouble());
                            trans.addProperty("ItemQty", 0);//update value in the json arry
                            trans.addProperty("AmountIncVat", 0);
                            trans.addProperty("Amount", 0);
                            trans.addProperty("AmountExcVat", 0);
                            transIt = gson.fromJson(trans.toString(), JsonElement.class);
                        }
                    }
                }
            }
        }
        this.getTransItemList().clear();
        for (JsonElement transIt : this.getOriginalTransItemJsonArray()) {
            TransItem itm = gson.fromJson(transIt, TransItem.class);
            if (itm.getItemQty() < 0) {
                itm.setItemQty(0);
            }
            this.getTransItemList().add(itm);
        }
    }

    public void _removeTransItemCEC(int aTransTypeId, int aTransReasonId, Trans aTrans, List<TransItem> aActiveTransItems, TransItem ti) {
        Gson gson = new Gson();
        try {
            new TransItemBean().removeTransItemCEC(aTransTypeId, aTransReasonId, aTrans, aActiveTransItems, ti);

            for (JsonElement transIt : this.getOriginalTransItemJsonArray()) {
                JsonObject trans = transIt.getAsJsonObject();
                if (ti.getItemId() == trans.get("ItemId").getAsLong() && ti.getUnit_id() == trans.get("unit_id").getAsLong()) {
                    for (JsonElement statictransIt : this.getOriginalStaticTransItemJsonArray()) {
                        JsonObject staticTrans = statictransIt.getAsJsonObject();

                        if (ti.getItemId() == staticTrans.get("ItemId").getAsLong()) {
                            trans.addProperty("ItemQty", staticTrans.get("ItemQty").getAsDouble());//update value in the json arry
                            trans.addProperty("AmountIncVat", staticTrans.get("AmountIncVat").getAsDouble());
                            trans.addProperty("Amount", staticTrans.get("Amount").getAsDouble());
                            trans.addProperty("AmountExcVat", staticTrans.get("AmountExcVat").getAsDouble());
                            transIt = gson.fromJson(trans.toString(), JsonElement.class);
                        }
                    }
                }
            }
            this.getTransItemList().clear();
            for (JsonElement transIt : this.getOriginalTransItemJsonArray()) {
                TransItem itm = gson.fromJson(transIt, TransItem.class);
                this.getTransItemList().add(itm);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void saveAdjustedAndSubOrder(String aAction, String aLevel, int aStoreId, int aTransTypeId, int aTransReasonId, String aSaleType, Trans trans, List<TransItem> aActiveTransItems, Transactor aSelectedTransactor, Transactor aSelectedBillTransactor, UserDetail aTransUserDetail, Transactor aSelectedSchemeTransactor, UserDetail aAuthorisedByUserDetail, AccCoa aSelectedAccCoa, StatusBean aStatusBean) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        List<TransItem> o_transItemList = new ArrayList<TransItem>();
        List<TransItem> n_transItemList = new ArrayList<TransItem>();
        Gson gson = new Gson();
        String message = "";

        try {
            long transId = getTransObj().getTransactionId();
            getNewTransAtSplit().setGrandTotal(0);
            getNewTransAtSplit().setUser_code("");

            if (this.getSelectedTransItemsList() != null) {
                if (getSelectedTransItemsList().size() > 0) {
                    trans.setUser_code("");
                } else {
                    this.ActionMessage = "No Order adjustments to save ";
                    FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(ub.translateWordsInText(BaseName, this.ActionMessage)));
                }

                //save original adjusted order
                for (TransItem new_itm : getSelectedTransItemsList()) {
                    new_itm.setTransactionId(0);
                    new_itm.setTransactionItemId(0);
                    if (new_itm.getItemQty() > 0) {
                        n_transItemList.add(new_itm);
                    }
                }
                getNewTransAtSplit().setTransactionId(0);
                this.AutoPrintAfterSave = false;
                this.setTransTotalsAndUpdateCEC(getNewTransAtSplit().getTransactionTypeId(), getNewTransAtSplit().getTransactionReasonId(), getNewTransAtSplit(), n_transItemList);
                saveTransCallQuickOrder(aAction, aLevel, aStoreId, aTransTypeId, aTransReasonId, aSaleType, getNewTransAtSplit(), n_transItemList, aSelectedTransactor, aSelectedBillTransactor, aTransUserDetail, aSelectedSchemeTransactor, aAuthorisedByUserDetail, aSelectedAccCoa, aStatusBean);
                getTransObj().setTransactionId(transId);

                //set original order grand total
                for (Object n_o : getOriginalTransItemJsonArray()) {
                    TransItem transIt = gson.fromJson(n_o.toString(), TransItem.class);
                    if (transIt.getItemQty() > 0) {
                        o_transItemList.add(transIt);
                    }
                }
                this.setTransTotalsAndUpdateCEC(getTransObj().getTransactionTypeId(), getTransObj().getTransactionReasonId(), getTransObj(), o_transItemList);
                saveTransCallQuickOrder(aAction, aLevel, aStoreId, aTransTypeId, aTransReasonId, aSaleType, getTransObj(), o_transItemList, aSelectedTransactor, aSelectedBillTransactor, aTransUserDetail, aSelectedSchemeTransactor, aAuthorisedByUserDetail, aSelectedAccCoa, aStatusBean);
                message = "Order Split Successfully";
            } else {
                message = "No Order adjustments to save";
                // this.setActionMessage(ub.translateWordsInText(BaseName, message));
            }

            this.refreshTransListQuickOrderManage(trans);
            this.setActionMessage(ub.translateWordsInText(BaseName, message));
            org.primefaces.PrimeFaces.current().executeScript("PF('sbarReviewOrder').hide()");
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    /**
     * @param SRCInvoice the SRCInvoice to set
     */
    public void setSRCInvoice(String SRCInvoice) {
        this.SRCInvoice = SRCInvoice;
    }

    /**
     * @return the ReportGrandTotal
     */
    public double getReportGrandTotal() {
        return ReportGrandTotal;
    }

    /**
     * @param ReportGrandTotal the ReportGrandTotal to set
     */
    public void setReportGrandTotal(double ReportGrandTotal) {
        this.ReportGrandTotal = ReportGrandTotal;
    }

    /**
     * @return the AuthorisedByUserDetail
     */
    public UserDetail getAuthorisedByUserDetail() {
        return AuthorisedByUserDetail;
    }

    /**
     * @param AuthorisedByUserDetail the AuthorisedByUserDetail to set
     */
    public void setAuthorisedByUserDetail(UserDetail AuthorisedByUserDetail) {
        this.AuthorisedByUserDetail = AuthorisedByUserDetail;
    }

    /**
     * @return the TransUserDetail
     */
    public UserDetail getTransUserDetail() {
        return TransUserDetail;
    }

    /**
     * @param TransUserDetail the TransUserDetail to set
     */
    public void setTransUserDetail(UserDetail TransUserDetail) {
        this.TransUserDetail = TransUserDetail;
    }

    /**
     * @return the TransactorTranss
     */
    public List<Trans> getTransactorTranss() {
        return TransactorTranss;
    }

    /**
     * @param TransactorTranss the TransactorTranss to set
     */
    public void setTransactorTranss(List<Trans> TransactorTranss) {
        this.TransactorTranss = TransactorTranss;
    }

    /**
     * @return the OverridePrintVersion
     */
    public int getOverridePrintVersion() {
        return OverridePrintVersion;
    }

    /**
     * @param OverridePrintVersion the OverridePrintVersion to set
     */
    public void setOverridePrintVersion(int OverridePrintVersion) {
        this.OverridePrintVersion = OverridePrintVersion;
    }

    /**
     * @return the Date1
     */
    public Date getDate1() {
        return Date1;
    }

    /**
     * @param Date1 the Date1 to set
     */
    public void setDate1(Date Date1) {
        this.Date1 = Date1;
    }

    /**
     * @return the Date2
     */
    public Date getDate2() {
        return Date2;
    }

    /**
     * @param Date2 the Date2 to set
     */
    public void setDate2(Date Date2) {
        this.Date2 = Date2;
    }

    /**
     * @return the DateType
     */
    public String getDateType() {
        return DateType;
    }

    /**
     * @param DateType the DateType to set
     */
    public void setDateType(String DateType) {
        this.DateType = DateType;
    }

    /**
     * @return the FieldName
     */
    public String getFieldName() {
        return FieldName;
    }

    /**
     * @param FieldName the FieldName to set
     */
    public void setFieldName(String FieldName) {
        this.FieldName = FieldName;
    }

    /**
     * @return the TransList
     */
    public List<Trans> getTransList() {
        return TransList;
    }

    /**
     * @param TransList the TransList to set
     */
    public void setTransList(List<Trans> TransList) {
        this.TransList = TransList;
    }

    /**
     * @return the TransListSummary
     */
    public List<Trans> getTransListSummary() {
        return TransListSummary;
    }

    /**
     * @param TransListSummary the TransListSummary to set
     */
    public void setTransListSummary(List<Trans> TransListSummary) {
        this.TransListSummary = TransListSummary;
    }

    /**
     * @return the TransObj
     */
    public Trans getTransObj() {
        return TransObj;
    }

    /**
     * @param TransObj the TransObj to set
     */
    public void setTransObj(Trans TransObj) {
        this.TransObj = TransObj;
    }

    /**
     * @return the TransItemList
     */
    public List<TransItem> getTransItemList() {
        return TransItemList;
    }

    /**
     * @param TransItemList the TransItemList to set
     */
    public void setTransItemList(List<TransItem> TransItemList) {
        this.TransItemList = TransItemList;
    }

    /**
     * @return the PayObj
     */
    public Pay getPayObj() {
        return PayObj;
    }

    /**
     * @param PayObj the PayObj to set
     */
    public void setPayObj(Pay PayObj) {
        this.PayObj = PayObj;
    }

    /**
     * @return the ActionType
     */
    public String getActionType() {
        return ActionType;
    }

    /**
     * @param ActionType the ActionType to set
     */
    public void setActionType(String ActionType) {
        this.ActionType = ActionType;
    }

    /**
     * @return the TranssDraft
     */
    public List<Trans> getTranssDraft() {
        return TranssDraft;
    }

    /**
     * @param TranssDraft the TranssDraft to set
     */
    public void setTranssDraft(List<Trans> TranssDraft) {
        this.TranssDraft = TranssDraft;
    }

    /**
     * @return the CustomerCardTranss
     */
    public List<Trans> getCustomerCardTranss() {
        return CustomerCardTranss;
    }

    /**
     * @param CustomerCardTranss the CustomerCardTranss to set
     */
    public void setCustomerCardTranss(List<Trans> CustomerCardTranss) {
        this.CustomerCardTranss = CustomerCardTranss;
    }

    /**
     * @return the GrandTotalLoc
     */
    public double getGrandTotalLoc() {
        return GrandTotalLoc;
    }

    /**
     * @param GrandTotalLoc the GrandTotalLoc to set
     */
    public void setGrandTotalLoc(double GrandTotalLoc) {
        this.GrandTotalLoc = GrandTotalLoc;
    }

    /**
     * @return the GrandTotalPaidLoc
     */
    public double getGrandTotalPaidLoc() {
        return GrandTotalPaidLoc;
    }

    /**
     * @param GrandTotalPaidLoc the GrandTotalPaidLoc to set
     */
    public void setGrandTotalPaidLoc(double GrandTotalPaidLoc) {
        this.GrandTotalPaidLoc = GrandTotalPaidLoc;
    }

    /**
     * @return the GrandTotalBalanceLoc
     */
    public double getGrandTotalBalanceLoc() {
        return GrandTotalBalanceLoc;
    }

    /**
     * @param GrandTotalBalanceLoc the GrandTotalBalanceLoc to set
     */
    public void setGrandTotalBalanceLoc(double GrandTotalBalanceLoc) {
        this.GrandTotalBalanceLoc = GrandTotalBalanceLoc;
    }

    /**
     * @return the CustomerCardTotals
     */
    public List<Trans> getCustomerCardTotals() {
        return CustomerCardTotals;
    }

    /**
     * @param CustomerCardTotals the CustomerCardTotals to set
     */
    public void setCustomerCardTotals(List<Trans> CustomerCardTotals) {
        this.CustomerCardTotals = CustomerCardTotals;
    }

    /**
     * @return the SupplierCardTranss
     */
    public List<Trans> getSupplierCardTranss() {
        return SupplierCardTranss;
    }

    /**
     * @param SupplierCardTranss the SupplierCardTranss to set
     */
    public void setSupplierCardTranss(List<Trans> SupplierCardTranss) {
        this.SupplierCardTranss = SupplierCardTranss;
    }

    /**
     * @return the SupplierCardTotals
     */
    public List<Trans> getSupplierCardTotals() {
        return SupplierCardTotals;
    }

    /**
     * @param SupplierCardTotals the SupplierCardTotals to set
     */
    public void setSupplierCardTotals(List<Trans> SupplierCardTotals) {
        this.SupplierCardTotals = SupplierCardTotals;
    }

    /**
     * @return the SearchTransList
     */
    public List<Trans> getSearchTransList() {
        return SearchTransList;
    }

    /**
     * @param SearchTransList the SearchTransList to set
     */
    public void setSearchTransList(List<Trans> SearchTransList) {
        this.SearchTransList = SearchTransList;
    }

    /**
     * @return the SearchTransId
     */
    public Integer getSearchTransId() {
        return SearchTransId;
    }

    /**
     * @param SearchTransId the SearchTransId to set
     */
    public void setSearchTransId(Integer SearchTransId) {
        this.SearchTransId = SearchTransId;
    }

    /**
     * @return the RefTrans
     */
    public Trans getRefTrans() {
        return RefTrans;
    }

    /**
     * @param RefTrans the RefTrans to set
     */
    public void setRefTrans(Trans RefTrans) {
        this.RefTrans = RefTrans;
    }

    /**
     * @return the TransChild
     */
    public Trans getTransChild() {
        return TransChild;
    }

    /**
     * @param TransChild the TransChild to set
     */
    public void setTransChild(Trans TransChild) {
        this.TransChild = TransChild;
    }

    /**
     * @return the ActiveTransItemsChild
     */
    public List<TransItem> getActiveTransItemsChild() {
        return ActiveTransItemsChild;
    }

    /**
     * @param ActiveTransItemsChild the ActiveTransItemsChild to set
     */
    public void setActiveTransItemsChild(List<TransItem> ActiveTransItemsChild) {
        this.ActiveTransItemsChild = ActiveTransItemsChild;
    }

    /**
     * @return the ActionMessageChild
     */
    public String getActionMessageChild() {
        return ActionMessageChild;
    }

    /**
     * @param ActionMessageChild the ActionMessageChild to set
     */
    public void setActionMessageChild(String ActionMessageChild) {
        this.ActionMessageChild = ActionMessageChild;
    }

    /**
     * @return the PayChild
     */
    public Pay getPayChild() {
        return PayChild;
    }

    /**
     * @param PayChild the PayChild to set
     */
    public void setPayChild(Pay PayChild) {
        this.PayChild = PayChild;
    }

    /**
     * @return the gen_flag
     */
    public boolean isGen_flag() {
        return gen_flag;
    }

    /**
     * @param gen_flag the gen_flag to set
     */
    public void setGen_flag(boolean gen_flag) {
        this.gen_flag = gen_flag;
    }

    /**
     * @return the Stock_outList
     */
    public List<Stock_out> getStock_outList() {
        return Stock_outList;
    }

    /**
     * @param Stock_outList the Stock_outList to set
     */
    public void setStock_outList(List<Stock_out> Stock_outList) {
        this.Stock_outList = Stock_outList;
    }

    /**
     * @return the TransTypeObj
     */
    public TransactionType getTransTypeObj() {
        return TransTypeObj;
    }

    /**
     * @param TransTypeObj the TransTypeObj to set
     */
    public void setTransTypeObj(TransactionType TransTypeObj) {
        this.TransTypeObj = TransTypeObj;
    }

    /**
     * @return the TransReasonObj
     */
    public TransactionReason getTransReasonObj() {
        return TransReasonObj;
    }

    /**
     * @param TransReasonObj the TransReasonObj to set
     */
    public void setTransReasonObj(TransactionReason TransReasonObj) {
        this.TransReasonObj = TransReasonObj;
    }

    /**
     * @return the PayMethodList
     */
    public List<PayMethod> getPayMethodList() {
        return PayMethodList;
    }

    /**
     * @param PayMethodList the PayMethodList to set
     */
    public void setPayMethodList(List<PayMethod> PayMethodList) {
        this.PayMethodList = PayMethodList;
    }

    /**
     * @return the AccChildAccountList
     */
    public List<AccChildAccount> getAccChildAccountList() {
        return AccChildAccountList;
    }

    /**
     * @param AccChildAccountList the AccChildAccountList to set
     */
    public void setAccChildAccountList(List<AccChildAccount> AccChildAccountList) {
        this.AccChildAccountList = AccChildAccountList;
    }

    /**
     * @return the TransTypeRefObj
     */
    public TransactionType getTransTypeRefObj() {
        return TransTypeRefObj;
    }

    /**
     * @param TransTypeRefObj the TransTypeRefObj to set
     */
    public void setTransTypeRefObj(TransactionType TransTypeRefObj) {
        this.TransTypeRefObj = TransTypeRefObj;
    }

    /**
     * @return the TransReasonRefObj
     */
    public TransactionReason getTransReasonRefObj() {
        return TransReasonRefObj;
    }

    /**
     * @param TransReasonRefObj the TransReasonRefObj to set
     */
    public void setTransReasonRefObj(TransactionReason TransReasonRefObj) {
        this.TransReasonRefObj = TransReasonRefObj;
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
     * @return the StoreList
     */
    public List<Store> getStoreList() {
        return StoreList;
    }

    /**
     * @param StoreList the StoreList to set
     */
    public void setStoreList(List<Store> StoreList) {
        this.StoreList = StoreList;
    }

    /**
     * @return the todayoryest
     */
    public int getTodayoryest() {
        return todayoryest;
    }

    /**
     * @param todayoryest the todayoryest to set
     */
    public void setTodayoryest(int todayoryest) {
        this.todayoryest = todayoryest;
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
     * @return the OutputNumber
     */
    public int getOutputNumber() {
        return OutputNumber;
    }

    /**
     * @param OutputNumber the OutputNumber to set
     */
    public void setOutputNumber(int OutputNumber) {
        this.OutputNumber = OutputNumber;
    }

    /**
     * @return the UserDetailList
     */
    public List<UserDetail> getUserDetailList() {
        return UserDetailList;
    }

    /**
     * @param UserDetailList the UserDetailList to set
     */
    public void setUserDetailList(List<UserDetail> UserDetailList) {
        this.UserDetailList = UserDetailList;
    }

    /**
     * @return the GrandTotal2
     */
    public double getGrandTotal2() {
        return GrandTotal2;
    }

    /**
     * @param GrandTotal2 the GrandTotal2 to set
     */
    public void setGrandTotal2(double GrandTotal2) {
        this.GrandTotal2 = GrandTotal2;
    }

    /**
     * @return the SearchTransNo
     */
    public String getSearchTransNo() {
        return SearchTransNo;
    }

    /**
     * @param SearchTransNo the SearchTransNo to set
     */
    public void setSearchTransNo(String SearchTransNo) {
        this.SearchTransNo = SearchTransNo;
    }

    /**
     * @return the SelectAll
     */
    public boolean isSelectAll() {
        return SelectAll;
    }

    /**
     * @param SelectAll the SelectAll to set
     */
    public void setSelectAll(boolean SelectAll) {
        this.SelectAll = SelectAll;
    }

    /**
     * @return the OrderSummaryTotal
     */
    public double getOrderSummaryTotal() {
        return OrderSummaryTotal;
    }

    /**
     * @param OrderSummaryTotal the OrderSummaryTotal to set
     */
    public void setOrderSummaryTotal(double OrderSummaryTotal) {
        this.OrderSummaryTotal = OrderSummaryTotal;
    }

    /**
     * @return the OrderMode1
     */
    public String getOrderMode1() {
        return OrderMode1;
    }

    /**
     * @param OrderMode1 the OrderMode1 to set
     */
    public void setOrderMode1(String OrderMode1) {
        this.OrderMode1 = OrderMode1;
    }

    /**
     * @return the OrderMode2
     */
    public String getOrderMode2() {
        return OrderMode2;
    }

    /**
     * @param OrderMode2 the OrderMode2 to set
     */
    public void setOrderMode2(String OrderMode2) {
        this.OrderMode2 = OrderMode2;
    }

    /**
     * @return the OrderMode3
     */
    public String getOrderMode3() {
        return OrderMode3;
    }

    /**
     * @param OrderMode3 the OrderMode3 to set
     */
    public void setOrderMode3(String OrderMode3) {
        this.OrderMode3 = OrderMode3;
    }

    /**
     * @return the TransListHist
     */
    public List<Trans> getTransListHist() {
        return TransListHist;
    }

    /**
     * @param TransListHist the TransListHist to set
     */
    public void setTransListHist(List<Trans> TransListHist) {
        this.TransListHist = TransListHist;
    }

    /**
     * @return the TransItemSummary
     */
    public List<TransItem> getTransItemSummary() {
        return TransItemSummary;
    }

    /**
     * @param TransItemSummary the TransItemSummary to set
     */
    public void setTransItemSummary(List<TransItem> TransItemSummary) {
        this.TransItemSummary = TransItemSummary;
    }

    /**
     * @return the TransListCrDr
     */
    public List<Trans> getTransListCrDr() {
        return TransListCrDr;
    }

    /**
     * @param TransListCrDr the TransListCrDr to set
     */
    public void setTransListCrDr(List<Trans> TransListCrDr) {
        this.TransListCrDr = TransListCrDr;
    }

    /**
     * @return the TransItemSummary2
     */
    public List<TransItem> getTransItemSummary2() {
        return TransItemSummary2;
    }

    /**
     * @param TransItemSummary2 the TransItemSummary2 to set
     */
    public void setTransItemSummary2(List<TransItem> TransItemSummary2) {
        this.TransItemSummary2 = TransItemSummary2;
    }

    /**
     * @return the TransListApproval
     */
    public List<Transaction_approval> getTransListApproval() {
        return TransListApproval;
    }

    /**
     * @param TransListApproval the TransListApproval to set
     */
    public void setTransListApproval(List<Transaction_approval> TransListApproval) {
        this.TransListApproval = TransListApproval;
    }

    public List<Item> getOrderItemList() {
        return orderItemList;
    }

    /**
     * @param orderItemList the orderItemList to set
     */
    public void setOrderItemList(List<Item> orderItemList) {
        this.orderItemList = orderItemList;
    }

    /**
     * @return the selectedTransItemsList
     */
    public List<TransItem> getSelectedTransItemsList() {
        return selectedTransItemsList;
    }

    /**
     * @param selectedTransItemsList the selectedTransItemsList to set
     */
    public void setSelectedTransItemsList(List<TransItem> selectedTransItemsList) {
        this.selectedTransItemsList = selectedTransItemsList;
    }

    /**
     * @return the newTransAtSplit
     */
    public Trans getNewTransAtSplit() {
        return newTransAtSplit;
    }

    /**
     * @param newTransAtSplit the newTransAtSplit to set
     */
    public void setNewTransAtSplit(Trans newTransAtSplit) {
        this.newTransAtSplit = newTransAtSplit;
    }

    /**
     * @return the selectedTransItem
     */
    public TransItem getSelectedTransItem() {
        return selectedTransItem;
    }

    /**
     * @param selectedTransItem the selectedTransItem to set
     */
    public void setSelectedTransItem(TransItem selectedTransItem) {
        this.selectedTransItem = selectedTransItem;
    }

    /**
     * @return the originalTransItemJsonArray
     */
    public JsonArray getOriginalTransItemJsonArray() {
        return originalTransItemJsonArray;
    }

    /**
     * @param originalTransItemJsonArray the originalTransItemJsonArray to set
     */
    public void setOriginalTransItemJsonArray(JsonArray originalTransItemJsonArray) {
        this.originalTransItemJsonArray = originalTransItemJsonArray;
    }

    /**
     * @return the originalStaticTransItemJsonArray
     */
    public JsonArray getOriginalStaticTransItemJsonArray() {
        return originalStaticTransItemJsonArray;
    }

    /**
     * @param originalStaticTransItemJsonArray the
     * originalStaticTransItemJsonArray to set
     */
    public void setOriginalStaticTransItemJsonArray(JsonArray originalStaticTransItemJsonArray) {
        this.originalStaticTransItemJsonArray = originalStaticTransItemJsonArray;
    }

}
