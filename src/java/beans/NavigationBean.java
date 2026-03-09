package beans;

import sessions.GeneralUserSetting;
import entities.CompanySetting;
import entities.GroupRight;
import entities.TransactionType;
import entities.UserDetail;
import java.io.Serializable;
import java.util.List;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import utilities.UtilityBean;

@ManagedBean
@SessionScoped
public class NavigationBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private String NavMsg;
    @ManagedProperty("#{menuItemBean}")
    private MenuItemBean menuItemBean;

    public String redirectToHome() {
        return "Home?faces-redirect=true";
    }

    public String redirectToItem(String aItemPurpose) {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        //httpSession.setAttribute("TRANSACTION_TYPE_ID", 5);
        //httpSession.setAttribute("TRANSACTION_TYPE_NAME", "ITEM");
        httpSession.setAttribute("ITEM_PURPOSE", aItemPurpose);
        return "Item?faces-redirect=true";
    }

    public String redirectToCategory() {
        return "Category?faces-redirect=true";
    }

    public String redirectToCompanySetting() {
        return "CompanySetting?faces-redirect=true";
    }

    public String redirectToTransactorMerge() {
        return "TransactorMerge?faces-redirect=true";
    }

    public String redirectToDiscountPackage() {
        return "DiscountPackage?faces-redirect=true";
    }

    public String redirectToDiscountPackageItem() {
        return "DiscountPackageItem?faces-redirect=true";
    }

    public String redirectToDisposeStock() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 3);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "DISPOSE STOCK");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 3);
        return "DisposeStockTrans?faces-redirect=true";
    }

    public String redirectToAdjustStock() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 71);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "STOCK ADJUSTMENT");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 106);
        return "AdjustStockTrans?faces-redirect=true";
    }

    public String redirectToConsumeStock() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 72);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "STOCK CONSUMPTION");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 108);
        return "ConsumeStockTrans?faces-redirect=true";
    }

    public String redirectToStockTake() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 84);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "STOCK TAKE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 128);
        return "StockTakeTrans?faces-redirect=true";
    }

    public String redirectToGroupDetail() {
        return "GroupDetail?faces-redirect=true";
    }

    public String redirectToUserCategory() {
        return "UserCategory?faces-redirect=true";
    }

    public String redirectToUserItemEarn() {
        return "UserItemEarn?faces-redirect=true";
    }

    public String redirectToGroupRight() {
        return "GroupRight?faces-redirect=true";
    }

    public String redirectToGroupUser() {
        return "GroupUser?faces-redirect=true";
    }

    public String redirectToItemMap() {
        return "ItemMap?faces-redirect=true";
    }

    public String redirectToItemCombination() {
        return "ItemCombination?faces-redirect=true";
    }

    public String redirectToMenu() {
        return "Menu?faces-redirect=true";
    }

    public String redirectToPayPurchase() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "SUPPLIER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 6);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "PAYMENT");
        return "Pay?faces-redirect=true";
    }

    public String redirectToPaySale() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 6);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "PAYMENT");
        return "Pay?faces-redirect=true";
    }

    public String redirectToPayIN() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("PAY_CATEGORY", "IN");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 6);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "PAYMENT");
        return "Pay?faces-redirect=true";
    }

    public String redirectToCashReceiptCS() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("PAY_CATEGORY", "IN");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 14);//was prev 6
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "CASH RECEIPT");// was prev "PAYMENT"
        httpSession.setAttribute("TRANSACTION_REASON_ID", 22);//6
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "CREDIT SALE");
        return "CashReceiptCS?faces-redirect=true";
    }

    public String redirectToCashReceiptRevenue() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("PAY_CATEGORY", "IN");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 14);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "CASH RECEIPT");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 115);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "OTHER REVENUE");
        return "CashReceiptREVENUE?faces-redirect=true";
    }

    public String redirectToCashReceiptPrepaidIncome() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("PAY_CATEGORY", "IN");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 14);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "CASH RECEIPT");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 90);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "PREPAID INCOME");
        return "CashReceiptPrepaidIncome?faces-redirect=true";
    }

    public String redirectToCashReceiptCC() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("PAY_CATEGORY", "IN");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 14);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "CASH RECEIPT");//
        httpSession.setAttribute("TRANSACTION_REASON_ID", 23);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "CAPITAL");
        return "CashReceiptCC?faces-redirect=true";
    }

    public String redirectToCashReceiptLOAN() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("PAY_CATEGORY", "IN");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 14);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "CASH RECEIPT");//
        httpSession.setAttribute("TRANSACTION_REASON_ID", 24);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "LOAN");
        return "CashReceiptLOAN?faces-redirect=true";
    }

    public String redirectToCashPaymentPI() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("PAY_CATEGORY", "OUT");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 15);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "CASH PAYMENT");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 25);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "CREDIT PURCHASE");
        return "CashPaymentPI?faces-redirect=true";
    }

    public String redirectToCashPaymentLOAN() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("PAY_CATEGORY", "OUT");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 15);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "CASH PAYMENT");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 33);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "LOAN INSTALLMENT");
        return "CashPaymentLOAN?faces-redirect=true";
    }

    public String redirectToCashPaymentDRAW() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("PAY_CATEGORY", "OUT");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 15);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "CASH PAYMENT");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 34);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "OWNER CASH DRAWING");
        return "CashPaymentDRAW?faces-redirect=true";
    }

    public String redirectToCashPaymentLIABILITY() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("PAY_CATEGORY", "OUT");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 15);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "CASH PAYMENT");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 105);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "LIABILITY PAYMENT");
        return "CashPaymentLIABILITY?faces-redirect=true";
    }

    public String redirectToCashPaymentPrepaidExpense() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("PAY_CATEGORY", "OUT");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 15);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "CASH PAYMENT");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 91);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "PREPAID EXPENSE");
        return "CashPaymentPrepaidExpense?faces-redirect=true";
    }

    public String redirectToPayOUT() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("PAY_CATEGORY", "OUT");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 6);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "PAYMENT");
        return "Pay?faces-redirect=true";
    }

    public String redirectToPayMethod() {
        return "PayMethod?faces-redirect=true";
    }

    public String redirectToPurchase() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "SUPPLIER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 1);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "PURCHASE");
        return "PurchaseTrans?faces-redirect=true";
    }

    public String redirectToPurchaseOrderGoods() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "SUPPLIER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 8);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "PURCHASE ORDER");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 12);//GOOD OR SERVICE PO
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(8);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "PurchaseOrderGoodsTrans?faces-redirect=true";
    }

    public String redirectToPurchaseOrderExpenses() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "SUPPLIER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 8);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "PURCHASE ORDER");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 30);//EXPENSE PO
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(8);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "PurchaseOrderExpensesTrans?faces-redirect=true";
    }

    public String redirectToPurchaseOrderAssets() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "SUPPLIER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 8);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "PURCHASE ORDER");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 31);//ASSET PO
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(8);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "PurchaseOrderAssetsTrans?faces-redirect=true";
    }

    public String redirectToGoodsDelivery() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 12);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "GOODS DELIVERY");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(12);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "SaleGoodsDeliveryTrans?faces-redirect=true";
    }

    public String redirectToPurchaseInvoiceGoods() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "SUPPLIER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 1);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "PURCHASE INVOICE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 1);//GOOD OR SERVICE
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(1);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "PurchaseInvoiceGoodsTrans?faces-redirect=true";
    }

    public String redirectToPurchaseInvoiceExpenses() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "SUPPLIER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 1);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "PURCHASE INVOICE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 27);//EXPENSE
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(1);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "PurchaseInvoiceExpensesTrans?faces-redirect=true";
    }

    public String redirectToExpenseEntry() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 19);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "EXPENSE ENTRY");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 43);//EXPENSE ENTRY
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(19);
//        if (TempTransType != null) {
//            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
//            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
//            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
//        }
        return "ExpenseEntryTrans?faces-redirect=true";
    }

    public String redirectToPurchaseInvoiceAssets() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "SUPPLIER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 1);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "PURCHASE INVOICE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 29);//ASSET
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(1);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "PurchaseInvoiceAssetsTrans?faces-redirect=true";
    }

    public String redirectToItemReceivedAssets() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "SUPPLIER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 9);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "ITEM RECEIVED");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 28);
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(9);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "ItemReceivedAssetsTrans?faces-redirect=true";
    }

    public String redirectToItemReceivedGoods() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "SUPPLIER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 9);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "ITEM RECEIVED");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 13);
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(9);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "ItemReceivedGoodsTrans?faces-redirect=true";
    }

    public String redirectToItemReceivedExpenses() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "SUPPLIER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 9);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "ITEM RECEIVED");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 32);
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(9);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "ItemReceivedExpensesTrans?faces-redirect=true";
    }

    public String redirectToWholeSaleQuotation() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "WHOLE SALE QUOTATION");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 15);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 10);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE QUOTATION");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(10);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        int quoteV = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "QUOTATION_VERSION").getParameter_value());
        return "SaleQuotationTransV" + quoteV + "?faces-redirect=true";
    }

    public String redirectToSpecialSaleQuotation() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "SPECIAL SALE QUOTATION");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 111);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 10);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE QUOTATION");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(10);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        int quoteV = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "QUOTATION_VERSION").getParameter_value());
        return "SaleQuotationTransV" + quoteV + "?faces-redirect=true";
    }

    public String redirectToRetailSaleQuotation() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "RETAIL SALE QUOTATION");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 14);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 10);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE QUOTATION");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(10);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        int quoteV = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "QUOTATION_VERSION").getParameter_value());
        return "SaleQuotationTransV" + quoteV + "?faces-redirect=true";
    }

    public String redirectToHireQuotation() {
        //update seesion
//        FacesContext context = FacesContext.getCurrentInstance();
//        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
//        HttpSession httpSession = request.getSession(false);
//        httpSession.setAttribute("TRANSACTION_REASON_ID", 92);
//        httpSession.setAttribute("TRANSACTION_TYPE_ID", 63);
//        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "HIRE QUOTATION");
//        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(63);
//        if (TempTransType != null) {
//            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
//            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
//            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
//        }
        return "HireQuotationTrans?faces-redirect=true";
    }

    public String redirectToSaleOrderRetail() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "RETAIL SALE ORDER");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 16);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 11);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE ORDER");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(11);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        int orderV = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "ORDER_VERSION").getParameter_value());
        return "SaleOrderTransV" + orderV + "?faces-redirect=true";
    }

    public String redirectToSaleOrderWhole() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "WHOLE SALE ORDER");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 109);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 11);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE ORDER");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(11);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        int orderV = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "ORDER_VERSION").getParameter_value());
        return "SaleOrderTransV" + orderV + "?faces-redirect=true";
    }

    public String redirectToSaleOrderSpecial() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "SPECIAL SALE ORDER");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 110);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 11);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE ORDER");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(11);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        int orderV = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "ORDER_VERSION").getParameter_value());
        return "SaleOrderTransV" + orderV + "?faces-redirect=true";
    }

    public String redirectToSaleOrderQuick() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "SALE ORDER");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 16);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 11);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE ORDER");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(11);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        httpSession.setAttribute("ORDER_FOR_EDIT", null);
        return "SaleOrderQuickTrans?faces-redirect=true";
    }

    public void setToSaleOrderQuick() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "SALE ORDER");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 16);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 11);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE ORDER");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(11);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
    }

    public String redirectToSaleOrderQuickManage() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "SALE ORDER");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 16);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 11);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE ORDER");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(11);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        httpSession.setAttribute("ORDER_FOR_EDIT", null);
        return "SaleOrderQuickManage?faces-redirect=true";
    }

    public String redirectToWholeSale() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "WHOLE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 10);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 2);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE");
        return "SaleTrans?faces-redirect=true";
    }

    public String redirectToRetailSale() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "RETAIL");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 2);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 2);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE");
        return "SaleTrans?faces-redirect=true";
    }

    public String redirectToCostPriceSale() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "COST-PRICE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 11);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 2);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE");
        return "SaleTrans?faces-redirect=true";
    }

    public String redirectToExemptSale() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "EXEMPT");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 12);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 2);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE");
        return "SaleTrans?faces-redirect=true";
    }

    public String redirectToWholeSaleInvoice() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "WHOLE SALE INVOICE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 10);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 2);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE INVOICE");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(2);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "SaleInvoiceTrans?faces-redirect=true";
    }

    public String redirectToSpecialSaleInvoice() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "SPECIAL SALE INVOICE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 103);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 2);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE INVOICE");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(2);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "SaleInvoiceTrans?faces-redirect=true";
    }

    public String redirectToRetailSaleInvoice() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "RETAIL SALE INVOICE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 2);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 2);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE INVOICE");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(2);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "SaleInvoiceTrans?faces-redirect=true";
    }

    public String redirectToCostPriceSaleInvoice() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "COST-PRICE SALE INVOICE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 11);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 2);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE INVOICE");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(2);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "SaleInvoiceTrans?faces-redirect=true";
    }

    public String redirectToExemptSaleInvoice() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "EXEMPT SALE INVOICE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 17);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 2);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE INVOICE");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(2);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "SaleInvoiceTrans?faces-redirect=true";
    }

    public String redirectToSaleView() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "");
        httpSession.setAttribute("TRANSACTOR_TYPE", "");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 2);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "SALE");
        return "SaleView?faces-redirect=true";
    }

    public String redirectToTransView() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "");
        httpSession.setAttribute("TRANSACTOR_TYPE", "");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 0);//this was 2 bfr edit
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "");//this was SALE bfr edit
        return "TransView?faces-redirect=true";
    }

    public String redirectToPurchaseView() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "");
        httpSession.setAttribute("TRANSACTOR_TYPE", "");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 1);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "PURCHASE");
        return "PurchaseView?faces-redirect=true";
    }

    public String redirectToDisposeView() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "");
        httpSession.setAttribute("TRANSACTOR_TYPE", "");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 3);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "DISPOSE");
        return "DisposeView?faces-redirect=true";
    }

    public String redirectToTransferView() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SALE_TYPE", "");
        httpSession.setAttribute("TRANSACTOR_TYPE", "");
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 4);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "TRANSFER");
        return "TransferView?faces-redirect=true";
    }

    public void defineTransactionTypes(int aTransactionTypeId, String aTransactionTypeName, String aTransactorType, String aSaleType) {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", aTransactionTypeId);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", aTransactionTypeName);
        httpSession.setAttribute("TRANSACTOR_TYPE", aTransactorType);
        httpSession.setAttribute("SALE_TYPE", aSaleType);
    }

    public String redirectToStore() {
        return "Store?faces-redirect=true";
    }

    public String redirectToSubCategory() {
        return "SubCategory?faces-redirect=true";
    }

    public String redirectToLicenseDetail() {
        return "LicenseDetail?faces-redirect=true";
    }

    public String redirectToTransactionReason() {
        return "TransactionReason?faces-redirect=true";
    }

    public String redirectToTransactionType() {
        return "TransactionType?faces-redirect=true";
    }

    public String redirectToMoreSetting() {
        return "MoreCompanySettings?faces-redirect=true";
    }

    public String redirectToContactList() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        return "Contact_list?faces-redirect=true";
    }

    public String redirectToChartOfAccounts() {
        return "ChartOfAccounts?faces-redirect=true";
    }

    public String redirectToChildAccounts() {
        return "ChildAccounts?faces-redirect=true";
    }

    public String redirectToCurrency() {
        return "Currency?faces-redirect=true";
    }

    public String redirectToXrate() {
        return "Xrate?faces-redirect=true";
    }

    public String redirectToTransactorCustomer() {
        //update seesion
        /*
         FacesContext context = FacesContext.getCurrentInstance();
         HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
         HttpSession httpSession = request.getSession(false);
         httpSession.setAttribute("TRANSACTION_TYPE_ID", 17);
         httpSession.setAttribute("TRANSACTION_TYPE_NAME", "TRANSACTOR");
         httpSession.setAttribute("TRANSACTION_REASON_ID", 36);
         httpSession.setAttribute("TRANSACTION_REASON_NAME", "CUSTOMER");
         httpSession.setAttribute("INVOKE_MODE", "PARENT");
         */
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "CUSTOMER");
        return "Transactor?faces-redirect=true";
    }

    public String redirectToTransactorSupplier() {
        //update seesion
//        FacesContext context = FacesContext.getCurrentInstance();
//        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
//        HttpSession httpSession = request.getSession(false);
//        httpSession.setAttribute("TRANSACTION_TYPE_ID", 17);
//        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "TRANSACTOR");
//        httpSession.setAttribute("TRANSACTION_REASON_ID", 37);
//        httpSession.setAttribute("TRANSACTION_REASON_NAME", "SUPPLIER");
//        httpSession.setAttribute("INVOKE_MODE", "PARENT");
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "SUPPLIER");
        return "Transactor?faces-redirect=true";
    }

    public String redirectToTransactorScheme() {
        //update seesion
//        FacesContext context = FacesContext.getCurrentInstance();
//        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
//        HttpSession httpSession = request.getSession(false);
//        httpSession.setAttribute("TRANSACTION_TYPE_ID", 17);
//        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "TRANSACTOR");
//        httpSession.setAttribute("TRANSACTION_REASON_ID", 38);
//        httpSession.setAttribute("TRANSACTION_REASON_NAME", "SCHEME");
//        httpSession.setAttribute("INVOKE_MODE", "PARENT");
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "SCHEME");
        return "Transactor?faces-redirect=true";
    }

    public String redirectToTransactorProvider() {
        //update seesion
//        FacesContext context = FacesContext.getCurrentInstance();
//        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
//        HttpSession httpSession = request.getSession(false);
//        httpSession.setAttribute("TRANSACTION_TYPE_ID", 17);
//        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "TRANSACTOR");
//        httpSession.setAttribute("TRANSACTION_REASON_ID", 39);
//        httpSession.setAttribute("TRANSACTION_REASON_NAME", "PROVIDER");
//        httpSession.setAttribute("INVOKE_MODE", "PARENT");
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTOR_TYPE", "PROVIDER");
        return "Transactor?faces-redirect=true";
    }

    public String redirectToTransactorSegment() {
        return "TransactorSegment?faces-redirect=true";
    }

    public String redirectToEmail() {
        return "Email?faces-redirect=true";
    }

    public String redirectToTransactorEmployee() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 17);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "TRANSACTOR");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 40);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "EMPLOYEE");
        httpSession.setAttribute("INVOKE_MODE", "PARENT");
        return "Transactor?faces-redirect=true";
    }

    public String redirectToCashTransfer() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 18);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "CASH TRANSFER");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 41);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "CASH TRANSFER");
        httpSession.setAttribute("INVOKE_MODE", "PARENT");
        return "CashTransfer?faces-redirect=true";
    }

    public String redirectToCashAdjustment() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 75);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "CASH ADJUSTMENT");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 116);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "CASH ADJUSTMENT");
        httpSession.setAttribute("INVOKE_MODE", "PARENT");
        return "CashAdjustment?faces-redirect=true";
    }

    public String redirectToCashBalancingDaily() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 78);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "CASH BALANCING");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 122);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "CASH BALANCING DAILY");
        httpSession.setAttribute("INVOKE_MODE", "PARENT");
        return "CashBalancingDaily?faces-redirect=true";
    }

    public String redirectToBankTransferCheque() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 18);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "CASH TRANSFER");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 42);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "CHEQUE TRANSFER");
        httpSession.setAttribute("INVOKE_MODE", "PARENT");
        return "CashTransferCheque?faces-redirect=true";
    }

    public String redirectToTransfer() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 4);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "TRANSFER");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 6);
        return "TransferTrans?faces-redirect=true";
    }

    public String redirectToTransferRequest() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 13);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "TRANSFER REQUEST");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 19);
        return "TransferRequestTrans?faces-redirect=true";
    }

    public String redirectToUnit() {
        return "Unit?faces-redirect=true";
    }

    public String redirectToUnpackStock() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 7);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "UNPACK");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 9);
        return "UnpackTrans?faces-redirect=true";
    }

    public String redirectToUserDetail() {
        return "UserDetail?faces-redirect=true";
    }

    public String redirectToUserManagementCRM() {
        return "UserManagementCRM?faces-redirect=true";
    }

    public String redirectToUserRight() {
        return "UserRight?faces-redirect=true";
    }

    public String redirectToIndex() {
        return "Index?faces-redirect=true";
    }

    public String redirectToMyAccount() {
        return "MyAccount?faces-redirect=true";
    }

    public String redirectToReportTransaction() {
        return "ReportTransaction?faces-redirect=true";
    }

    public String redirectToReportSalesInvoiceDetail() {
        return "ReportSalesInvoiceDetail?faces-redirect=true";
    }

    public String redirectToReportSalesPackageDetail() {
        return "ReportSalesPackageDetail?faces-redirect=true";
    }

    public String redirectToReportEFDLogs() {
        return "ReportEFDLogs?faces-redirect=true";
    }

    public String redirectToReportSalesCrDrNoteDetail() {
        return "ReportSalesCrDrNoteDetail?faces-redirect=true";
    }

    public String redirectToReportOverpaidTrans() {
        return "ReportOverpaidTrans?faces-redirect=true";
    }

    public String redirectToReportStockPriceQtyError() {
        return "ReportStockPriceQtyError?faces-redirect=true";
    }

    public String redirectToReportSalesTaxAPI() {
        return "ReportSalesTaxAPI?faces-redirect=true";
    }

    public String redirectToReportStockTaxAPI() {
        return "ReportStockTaxAPI?faces-redirect=true";
    }

    public String redirectToReportSMbiAPI() {
        return "ReportSMbiAPI?faces-redirect=true";
    }

    public String redirectToReportOpenBalance() {
        return "ReportOpenBalance?faces-redirect=true";
    }

    public String redirectToReportHireInvoiceDetail() {
        return "ReportHireInvoiceDetail?faces-redirect=true";
    }

    public String redirectToReportHireQuotationDetail() {
        return "ReportHireQuotationDetail?faces-redirect=true";
    }

    public String redirectToItemTransaction() {
        return "ReportItemTransaction?faces-redirect=true";
    }

    public String redirectToReportSalesQuotationDetail() {
        return "ReportSalesQuotationDetail?faces-redirect=true";
    }

    public String redirectToReportSalesOrderDetail() {
        return "ReportSalesOrderDetail?faces-redirect=true";
    }

    public String redirectToReportSalesDeliveryDetail() {
        return "ReportSalesDeliveryDetail?faces-redirect=true";
    }

    public String redirectToReportSalesUserEarnDetail() {
        return "ReportTransactionUserEarn?faces-redirect=true";
    }

    public String redirectToReportPurchaseInvoiceDetail() {
        return "ReportPurchaseInvoiceDetail?faces-redirect=true";
    }

    public String redirectToReportPurchaseOrderDetail() {
        return "ReportPurchaseOrderDetail?faces-redirect=true";
    }

    public String redirectToReportPurchaseItemReceivedDetail() {
        return "ReportPurchaseItemReceivedDetail?faces-redirect=true";
    }

    public String redirectToReportTransferRequestDetail() {
        return "ReportTransferRequestDetail?faces-redirect=true";
    }

    public String redirectToReportTransferDetail() {
        return "ReportTransferDetail?faces-redirect=true";
    }

    public String redirectToReportDisposeStockDetail() {
        return "ReportDisposeStockDetail?faces-redirect=true";
    }

    public String redirectToReportAdjustStockDetail() {
        return "ReportAdjustStockDetail?faces-redirect=true";
    }

    public String redirectToReportConsumeStockDetail() {
        return "ReportConsumeStockDetail?faces-redirect=true";
    }

    public String redirectToReportStockTake() {
        return "ReportStockTake?faces-redirect=true";
    }

    public String redirectToReportCashReceiptDetail() {
        return "ReportCashReceiptDetail?faces-redirect=true";
    }

    public String redirectToReportCashPaymentDetail() {
        return "ReportCashPaymentDetail?faces-redirect=true";
    }

    public String redirectToReportReportCashBalancingDaily() {
        return "ReportCashBalancingDaily?faces-redirect=true";
    }

    public String redirectToReportPay() {
        return "ReportPay?faces-redirect=true";
    }

    public String redirectToReportTransactionItem() {
        return "ReportTransactionItem?faces-redirect=true";
    }

    public String redirectToReportTransactionUserEarn() {
        return "ReportTransactionUserEarn?faces-redirect=true";
    }

    public String redirectToReportStockIn() {
        return "ReportStockIn?faces-redirect=true";
    }

    public String redirectToReportStockTotal() {
        return "ReportStockTotal?faces-redirect=true";
    }

    public String redirectToReportStockAll() {
        return "ReportStockAll?faces-redirect=true";
    }

    public String redirectToReportItem() {
        return "ReportItem?faces-redirect=true";
    }

    public String redirectToReportItemLocation() {
        return "ReportItemLocation?faces-redirect=true";
    }

    public String redirectToReportTransactor() {
        return "ReportTransactor?faces-redirect=true";
    }

    public String redirectToReportPartnerCustomer() {
        return "ReportPartnerCustomer?faces-redirect=true";
    }

    public String redirectToReportPartnerSupplier() {
        return "ReportPartnerSupplier?faces-redirect=true";
    }

    public String redirectToReportPartnerScheme() {
        return "ReportPartnerScheme?faces-redirect=true";
    }

    public String redirectToReportPartnerProvider() {
        return "ReportPartnerProvider?faces-redirect=true";
    }

    public String redirectToReportEmployee() {
        return "ReportEmployee?faces-redirect=true";
    }

    public String redirectToReportTransactorLedger() {
        return "ReportTransactorLedger?faces-redirect=true";
    }

    public String redirectToReportTransactorLedgerSummary() {
        return "ReportTransactorLedgerSummary?faces-redirect=true";
    }

    public String redirectToReportProviderBill() {
        return "ReportProviderBill?faces-redirect=true";
    }

    public String redirectToReportCustomerCard() {
        return "ReportCustomerCard?faces-redirect=true";
    }

    public String redirectToReportSupplierCard() {
        return "ReportSupplierCard?faces-redirect=true";
    }

    public String redirectToTrialBalance() {
        return "TrialBalance?faces-redirect=true";
    }

    public String redirectToPostCloseTrialBalance() {
        return "PostCloseTrialBalance?faces-redirect=true";
    }

    public String redirectToReportJournal() {
        return "ReportJournal?faces-redirect=true";
    }

    public String redirectToReportCashFlowUser() {
        return "ReportCashFlowUser?faces-redirect=true";
    }

    public String redirectToReportReceivableDetail() {
        return "ReportReceivableDetail?faces-redirect=true";
    }

    public String redirectToReportReceivableSummary() {
        return "ReportReceivableSummary?faces-redirect=true";
    }

    public String redirectToReportSalesInvoiceAge() {
        return "ReportSalesInvoiceAge?faces-redirect=true";
    }

    public String redirectToReportSupplierInvoiceAge() {
        return "ReportSupplierInvoiceAge?faces-redirect=true";
    }

    public String redirectToReportPayableDetail() {
        return "ReportPayableDetail?faces-redirect=true";
    }

    public String redirectToReportPayableSummary() {
        return "ReportPayableSummary?faces-redirect=true";
    }

    public String redirectToReportCashAccBalances() {
        return "ReportCashAccBal?faces-redirect=true";
    }

    public String redirectToBalanceSheet() {
        return "BalanceSheet?faces-redirect=true";
    }

    public String redirectToIncomeStatement() {
        return "IncomeStatement?faces-redirect=true";
    }

    public String redirectToCashFlowStatement() {
        return "CashFlowStatement?faces-redirect=true";
    }

    public String redirectToAccountPeriod() {
        return "AccountPeriod?faces-redirect=true";
    }

    public String redirectToReportInventoryStock() {
        return "ReportInventoryStock?faces-redirect=true";
    }

    public String redirectToReportInventoryLedger() {
        return "ReportStockLedger?faces-redirect=true";
    }

    public String redirectToReportStock() {
        return "ReportStock?faces-redirect=true";
    }

    public String redirectToReportStockStatus() {
        return "ReportStockStatus?faces-redirect=true";
    }

    public String redirectToReportExpiryStatus() {
        return "ReportExpiryStatus?faces-redirect=true";
    }

    public String redirectToReportInventoryExpense() {
        return "ReportInventoryExpense?faces-redirect=true";
    }

    public String redirectToReportInventoryAsset() {
        return "ReportInventoryAsset?faces-redirect=true";
    }

    public String redirectToReportItemDetailStock() {
        return "ReportItemDetailStock?faces-redirect=true";
    }

    public String redirectToReportItemDetail() {
        return "ReportItemDetail?faces-redirect=true";
    }

    public String redirectToReportAlerts() {
        return "ReportAlerts?faces-redirect=true";
    }

    public String redirectToReportItemDetailExpense() {
        return "ReportItemDetailExpense?faces-redirect=true";
    }

    public String redirectToReportItemDetailAsset() {
        return "ReportItemDetailAsset?faces-redirect=true";
    }

    public String redirectToReportItemDetailLocation() {
        return "ReportItemDetailLocation?faces-redirect=true";
    }

    public String redirectToLocation() {
        return "Location?faces-redirect=true";
    }

    public String redirectToItemLocation() {
        return "ItemLocation?faces-redirect=true";
    }

    public String getTransactorReasonStr(String aTransactorType) {
        String ReasIdStr = "";
        switch (aTransactorType) {
            case "CUSTOMER":
                ReasIdStr = "36";
                break;
            case "SUPPLIER":
                ReasIdStr = "37";
                break;
            case "SCHEME":
                ReasIdStr = "38";
                break;
            case "PROVIDER":
                ReasIdStr = "39";
                break;
            case "EMPLOYEE":
                ReasIdStr = "40";
                break;
        }
        return ReasIdStr;
    }

    public void checkAccessDeniedTransactor(String aTransactorType, String aRole) {
        switch (aTransactorType) {
            case "CUSTOMER":
                this.checkAccessDenied(this.getTransactorReasonStr(aTransactorType), aRole);
                break;
            case "SUPPLIER":
                this.checkAccessDenied(this.getTransactorReasonStr(aTransactorType), aRole);
                break;
            case "SCHEME":
                this.checkAccessDenied(this.getTransactorReasonStr(aTransactorType), aRole);
                break;
            case "PROVIDER":
                this.checkAccessDenied(this.getTransactorReasonStr(aTransactorType), aRole);
                break;
            case "EMPLOYEE":
                this.checkAccessDenied(this.getTransactorReasonStr(aTransactorType), aRole);
                break;
        }
    }

    public void checkAccessDenied(String aFunctionName, String aRole) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        String RealFunctionName = "";
        RealFunctionName = aFunctionName;
        UserDetail aCurrentUserDetail = new GeneralUserSetting().getCurrentUser();
        List<GroupRight> aCurrentGroupRights = new GeneralUserSetting().getCurrentGroupRights();
        GroupRightBean grb = new GroupRightBean();

        if (grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, RealFunctionName, aRole) == 0) {
            this.setNavMsg(RealFunctionName + " : " + ub.translateWordsInText(BaseName, "Not Authorized to Access"));
            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(getNavMsg()));
            FacesContext fc = FacesContext.getCurrentInstance();
            ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) fc.getApplication().getNavigationHandler();
            nav.performNavigation("Home?faces-redirect=true");
        } else {
            this.setNavMsg("");
        }
    }

    public int checkAccessDeniedReturn(String aFunctionName, String aRole) {
        int allow_access = 0;
        String RealFunctionName = "";
        RealFunctionName = aFunctionName;
        UserDetail aCurrentUserDetail = new GeneralUserSetting().getCurrentUser();
        List<GroupRight> aCurrentGroupRights = new GeneralUserSetting().getCurrentGroupRights();
        GroupRightBean grb = new GroupRightBean();

        if (grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, RealFunctionName, aRole) == 0) {
            allow_access = 0;
        } else {
            allow_access = 1;
        }
        return allow_access;
    }

//    public void checkAccessDenied(String aFunctionName, String aRole) {
//        String RealFunctionName = "";
//
//        if ("SALE INVOICE".equals(aFunctionName)) {
//
//            if ("WHOLE SALE INVOICE".equals(new GeneralUserSetting().getCurrentSaleType())) {
//                RealFunctionName = "WHOLE SALE INVOICE";
//            } else if ("RETAIL SALE INVOICE".equals(new GeneralUserSetting().getCurrentSaleType())) {
//                RealFunctionName = "RETAIL SALE INVOICE";
//            } else if ("COST-PRICE SALE INVOICE".equals(new GeneralUserSetting().getCurrentSaleType())) {
//                RealFunctionName = "COST-PRICE SALE INVOICE";
//            } else if ("EXEMPT SALE INVOICE".equals(new GeneralUserSetting().getCurrentSaleType())) {
//                RealFunctionName = "EXEMPT SALE INVOICE";
//            } else {
//                RealFunctionName = "RETAIL SALE INVOICE";
//            }
//        } else if ("TRANSACTOR".equals(aFunctionName)) {
//            RealFunctionName = "TRANSACTOR";
//        } else {
//            RealFunctionName = aFunctionName;
//        }
//
//        UserDetail aCurrentUserDetail = new GeneralUserSetting().getCurrentUser();
//        List<GroupRight> aCurrentGroupRights = new GeneralUserSetting().getCurrentGroupRights();
//        GroupRightBean grb = new GroupRightBean();
//
//        if (grb.IsUserGroupsFunctionAccessAllowed(aCurrentUserDetail, aCurrentGroupRights, RealFunctionName, aRole) == 0) {
//            this.setNavMsg(RealFunctionName + ": Unauthorized access, contact system admin...");
//            FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(getNavMsg()));
//            FacesContext fc = FacesContext.getCurrentInstance();
//            ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) fc.getApplication().getNavigationHandler();
//            nav.performNavigation("Home?faces-redirect=true");
//        } else {
//            this.setNavMsg("");
//        }
//    }
    public void checkAccessDeniedHome() {

        try {
            UserDetail aCurrentUserDetail = new GeneralUserSetting().getCurrentUser();
            if (aCurrentUserDetail.getUserDetailId() == 0) {
                this.setNavMsg("Unauthorized access, contact system admin...");
                FacesContext.getCurrentInstance().addMessage("Save", new FacesMessage(getNavMsg()));
                FacesContext fc = FacesContext.getCurrentInstance();
                ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) fc.getApplication().getNavigationHandler();
                nav.performNavigation("Login?faces-redirect=true");
            }
        } catch (NullPointerException npe) {
            FacesContext fc = FacesContext.getCurrentInstance();
            ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) fc.getApplication().getNavigationHandler();
            nav.performNavigation("Login?faces-redirect=true");
        }
    }

    public void checkLicenseExpired() {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        if (CompanySetting.getLicenseDaysLeft() <= 0 && CompanySetting.getLicenseType() != 9) {
            this.setNavMsg(ub.translateWordsInText(BaseName, "License Expired") + ", " + ub.translateWordsInText(BaseName, "Contact System Vendor"));
            FacesContext.getCurrentInstance().addMessage("License", new FacesMessage(getNavMsg()));
            FacesContext fc = FacesContext.getCurrentInstance();
            ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) fc.getApplication().getNavigationHandler();
            nav.performNavigation("Home?faces-redirect=true");
        } else {
            this.setNavMsg("");
        }
    }

    public void checkCurrentPage(String aTransactionType, String aTransactorType, String aSaleType) {
        UtilityBean ub = new UtilityBean();
        String BaseName = "language_en";
        try {
            BaseName = menuItemBean.getMenuItemObj().getLANG_BASE_NAME_SYS();
        } catch (Exception e) {
        }
        int LogOut = 0;
        if (!aTransactionType.equals(new GeneralUserSetting().getCurrentTransactionTypeName())) {
            //in the wrong place
            LogOut = 1;
        } else {
            LogOut = 0;
        }

        if (LogOut == 1) {
            //log-out
            this.setNavMsg(ub.translateWordsInText(BaseName, "Stop Opening Multiple Transaction Pages"));
            FacesContext.getCurrentInstance().addMessage("Security", new FacesMessage(getNavMsg()));
            Login aLogin = new Login();
            aLogin.userLogout();
        }
    }

    /**
     * @return the NavMsg
     */
    public String getNavMsg() {
        return NavMsg;
    }

    public String redirectToJournalEntryTrans() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 16);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "JOURNAL ENTRY");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 35);
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(16);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "JournalEntryTrans?faces-redirect=true";
    }

    public String redirectToOpenBalanceCustomer() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 76);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "OPENING BALANCE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 117);
        httpSession.setAttribute("TRANSACTION_REASON_NAME", "CUSTOMER OPENING BALANCE");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(76);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "OpenBalance?faces-redirect=true";
    }

    public String redirectToOpenBalanceSupplier() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 76);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "OPENING BALANCE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 118);
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(76);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "OpenBalance?faces-redirect=true";
    }

    public String redirectToOpenBalanceCash() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 76);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "OPENING BALANCE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 119);
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(76);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "OpenBalance?faces-redirect=true";
    }

    public String redirectToOpenBalanceOtherAcc() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 76);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "OPENING BALANCE");
        httpSession.setAttribute("TRANSACTION_REASON_ID", 120);
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(76);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "OpenBalance?faces-redirect=true";
    }

    /**
     * @param NavMsg the NavMsg to set
     */
    public void setNavMsg(String NavMsg) {
        this.NavMsg = NavMsg;
    }

    public String redirectToHireInvoice() {
        return "HireInvoiceTrans?faces-redirect=true";
    }

    public String redirectToHireReturnNote() {
        return "HireReturnNoteTrans?faces-redirect=true";
    }

    public String redirectToHireDeliveryNote() {
        return "HireDeliveryNoteTrans?faces-redirect=true";
    }

    public String redirectToReportHireDeliveryNoteDetail() {
        return "ReportHireDeliveryNoteDetail?faces-redirect=true";
    }

    public String redirectToReportHireReturnNoteDetail() {
        return "ReportHireReturnNoteDetail?faces-redirect=true";
    }

    public String redirectToReportHireUnReturnedDetail() {
        return "ReportHireUnReturnedDetail?faces-redirect=true";
    }

    public String redirectToProductionMap() {
        return "ProductionItemMap?faces-redirect=true";
    }

    public String redirectToProduction() {
        int prodV = Integer.parseInt(new Parameter_listBean().getParameter_listByContextNameMemory("COMPANY_SETTING", "PRODUCTION_VERSION").getParameter_value());
        return "ProductionTransV" + prodV + "?faces-redirect=true";
    }

    public String redirectToReportProductionDetail() {
        return "ReportProductionDetail?faces-redirect=true";
    }

    public String redirectToSalesPackaging() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_REASON_ID", 135);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 88);
        httpSession.setAttribute("TRANSACTION_TYPE_NAME", "PACKAGING");
        TransactionType TempTransType = new TransactionTypeBean().getTransactionType(88);
        if (TempTransType != null) {
            httpSession.setAttribute("TRANSACTOR_LABEL", TempTransType.getTransactorLabel());
            httpSession.setAttribute("TRANSACTION_NUMBER_LABEL", TempTransType.getTransactionNumberLabel());
            httpSession.setAttribute("TRANSACTION_OUTPUT_LABEL", TempTransType.getTransactionOutputLabel());
        }
        return "SalePackage?faces-redirect=true";
    }

    public String redirectToReportProdInputOutputDetail() {
        return "ReportProdInputOutput?faces-redirect=true";
    }

    public String redirectToSubscription() {
        //update seesion
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("TRANSACTION_REASON_ID", 129);
        httpSession.setAttribute("TRANSACTION_TYPE_ID", 85);
        return "Subscription?faces-redirect=true";
    }

    public String redirectToTimesheet() {
        //update seesion
//        FacesContext context = FacesContext.getCurrentInstance();
//        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
//        HttpSession httpSession = request.getSession(false);
//        httpSession.setAttribute("TRANSACTION_REASON_ID", 129);
//        httpSession.setAttribute("TRANSACTION_TYPE_ID", 85);
        return "Timesheet?faces-redirect=true";
    }

    public String redirectToSourceXhtmlFile() {
        String SourceXhtmlFile = "";
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
            HttpSession httpSession = request.getSession(false);
            SourceXhtmlFile = (String) httpSession.getAttribute("SOURCE_XHTML");
            if (null == SourceXhtmlFile || SourceXhtmlFile.isEmpty()) {
                SourceXhtmlFile = "Home";
            }
        } catch (NullPointerException | ClassCastException npe) {
            SourceXhtmlFile = "Home";
        }
        return SourceXhtmlFile + "?faces-redirect=true";
    }

    public String redirectToSubActivityCategory(String aXhtmlFile) {
        //set session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SOURCE_XHTML", aXhtmlFile);
        //redirect
        return "SubCategoryActivity?faces-redirect=true";
    }

    public String redirectToStaff() {
        return "Staff?faces-redirect=true";
    }

    public String redirectToActivityStatus() {
        return "ActivityStatus?faces-redirect=true";
    }

    public String redirectToMode_activity() {
        return "Mode_activity?faces-redirect=true";
    }

    public String redirectToProject() {
        return "Project?faces-redirect=true";
    }

    public String redirectToSubscriptionCategory() {
        return "SubscriptionCategory?faces-redirect=true";
    }

    public String redirectToCategoryActivity(String aXhtmlFile) {
        //set session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SOURCE_XHTML", aXhtmlFile);
        //redirect
        return "CategoryActivity?faces-redirect=true";
    }

    public String redirectToBusinessCategory() {
        return "BusinessCategory?faces-redirect=true";
    }

    public String redirectToTransactionApproval() {
        return "TransactionApproval?faces-redirect=true";
    }

    public String redirectToShift() {
        return "Shift?faces-redirect=true";
    }

    /**
     * @return the menuItemBean
     */
    public MenuItemBean getMenuItemBean() {
        return menuItemBean;
    }

    public String redirectToStaff(String aXhtmlFile) {
        //set session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SOURCE_XHTML", aXhtmlFile);
        //redirect
        return "Staff?faces-redirect=true";
    }

    public String redirectToActivityStatus(String aXhtmlFile) {
        //set session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SOURCE_XHTML", aXhtmlFile);
        //redirect
        return "ActivityStatus?faces-redirect=true";
    }

    public String redirectToMode_activity(String aXhtmlFile) {
        //set session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SOURCE_XHTML", aXhtmlFile);
        //redirect
        return "Mode_activity?faces-redirect=true";
    }

    public String redirectToProject(String aXhtmlFile) {
        //set session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute("SOURCE_XHTML", aXhtmlFile);
        //redirect
        return "Project?faces-redirect=true";
    }

    /**
     * @param menuItemBean the menuItemBean to set
     */
    public void setMenuItemBean(MenuItemBean menuItemBean) {
        this.menuItemBean = menuItemBean;
    }
}
