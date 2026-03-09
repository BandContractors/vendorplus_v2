package api_sm_bi;

import beans.CreditDebitNoteBean;
import beans.ItemBean;
import beans.Loyalty_transactionBean;
import beans.Parameter_listBean;
import beans.StoreBean;
import beans.TransBean;
import beans.TransItemBean;
import beans.Transaction_smbi_mapBean;
import beans.TransactorBean;
import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import connections.DBConnection;
import entities.CompanySetting;
import entities.Loyalty_transaction;
import entities.Trans;
import entities.TransItem;
import entities.Transaction_smbi_map;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.faces.bean.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import sessions.GeneralUserSetting;
import utilities.UtilityBean;

@ManagedBean
@SessionScoped
public class SMbiBean implements Serializable {

    private static final long serialVersionUID = 1L;
    static Logger LOGGER = Logger.getLogger(SMbiBean.class.getName());
    private Bi_stg_sale_invoice saleInvoice;
    private List<Bi_stg_sale_invoice_item> saleInvoiceItems;
    private Bi_stg_sale_cr_dr_note saleCreditDebitNote;
    private List<Bi_stg_sale_cr_dr_note_item> saleCreditDebitNoteItems;
    private LoyaltyTransaction loyaltyTransaction;

    public void syncSMbiCallThread() {
        try {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    syncSMbiCall();
                }
            };
            Executor e = Executors.newSingleThreadExecutor();
            e.execute(task);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void syncSMbiCall() {
        syncSMbiCall1();
        syncSMbiCall2();
    }

    public void syncSMbiCall1() {//transaction_smbi_map
        try {
            String sqlN = "SELECT COUNT(*) as n FROM transaction_smbi_map WHERE status_sync=0";
            long n = new UtilityBean().getN(sqlN);
            double iterations = Math.ceil(1.0 * n / 50);
            String sql = "";
            ResultSet rs = null;
            Transaction_smbi_map tsm = null;
            for (int itrn = 1; itrn <= iterations; itrn++) {
                sql = "SELECT * FROM transaction_smbi_map WHERE status_sync=0 LIMIT 50";
                rs = null;
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql);) {
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        long TransId = rs.getLong("transaction_id");
                        int TransTypeId = rs.getInt("transaction_type_id");
                        if (TransTypeId == 2) {//Invoice
                            sendInvoice(TransId);
                        }
                        if (TransTypeId == 82 || TransTypeId == 83) {//82-126-CREDIT NOTE, 83-127-DEBIT NOTE
                            sendCreditDebitNote(TransId);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void syncSMbiCall2() {//loyalty_transaction
        try {
            String sqlN = "SELECT COUNT(*) as n FROM loyalty_transaction WHERE status_sync=0";
            long n = new UtilityBean().getN(sqlN);
            double iterations = Math.ceil(1.0 * n / 50);
            String sql = "";
            ResultSet rs = null;
            Transaction_smbi_map tsm = null;
            for (int itrn = 1; itrn <= iterations; itrn++) {
                sql = "SELECT * FROM loyalty_transaction WHERE status_sync=0 LIMIT 50";
                rs = null;
                try (
                        Connection conn = DBConnection.getMySQLConnection();
                        PreparedStatement ps = conn.prepareStatement(sql);) {
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        long LoyTransId = rs.getLong("loyalty_transaction_id");
                        sendLoyaltyTransaction(LoyTransId);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.ERROR, e);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void sendInvoice(long aTransactionId) {
        try {
            Trans t = new TransBean().getTrans(aTransactionId);
            List<TransItem> tis = new TransItemBean().getTransItemsByTransactionId(aTransactionId);
            if (null == t || null == tis) {
                //do nothing
            } else {
                Gson gson = new Gson();
                String json = "";
                //init objects
                saleInvoice = new Bi_stg_sale_invoice();
                saleInvoiceItems = new ArrayList<>();
                //prepare
                this.prepareInvoice(t, tis);
                //creating JSON STRING from Object - Branch
                Bi_stg_sale_invoiceBean invBean = new Bi_stg_sale_invoiceBean();
                invBean.setSaleInvoice(saleInvoice);
                invBean.setSaleInvoiceItems(saleInvoiceItems);
                json = gson.toJson(invBean);
                //System.out.println("invBean:" + json);
                com.sun.jersey.api.client.Client client = com.sun.jersey.api.client.Client.create();
                WebResource webResource = client.resource(new Parameter_listBean().getParameter_listByContextName("API", "API_SMBI_URL").getParameter_value());
                ClientResponse response = webResource.type("application/json").post(ClientResponse.class, json);
                String output = response.getEntity(String.class);
                Status s = gson.fromJson(output, Status.class);
                //System.out.println("Success:" + s.getSuccess() + ",Description" + s.getDescription());
                //update the database table:transaction_smbi_map
                if (s.getSuccess() == 1) {
                    new Transaction_smbi_mapBean().updateTransaction_smbi_map(1, new CompanySetting().getCURRENT_SERVER_DATE(), "success", t.getTransactionId(), t.getTransactionTypeId());
                } else {
                    new Transaction_smbi_mapBean().updateTransaction_smbi_map(2, new CompanySetting().getCURRENT_SERVER_DATE(), s.getDescription(), t.getTransactionId(), t.getTransactionTypeId());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void prepareInvoice(Trans aTrans, List<TransItem> aTransItems) {
        try {
            //invoice
            try {
                saleInvoice.setSection_code(new StoreBean().getStore(aTrans.getStoreId()).getStore_code());
            } catch (Exception e) {
                saleInvoice.setSection_code("");
            }
            saleInvoice.setBranch_code(Integer.toString(CompanySetting.getBranchId()));
            saleInvoice.setBusiness_code(CompanySetting.getCompanyName());
            saleInvoice.setGroup_code(new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_SMBI_GROUP_CODE").getParameter_value());
            saleInvoice.setInvoice_number(aTrans.getTransactionNumber());
            saleInvoice.setSales_date(aTrans.getTransactionDate());
            try {
                if (aTrans.getBillTransactorId() > 0) {
                    saleInvoice.setCustomer_name(new TransactorBean().getTransactor(aTrans.getBillTransactorId()).getTransactorNames());
                } else {
                    saleInvoice.setCustomer_name("Walk-In Customer");
                }
            } catch (Exception e) {
                //
            }
            saleInvoice.setGross_amount(aTrans.getGrandTotal());
            saleInvoice.setTrade_discount(aTrans.getTotalTradeDiscount());
            saleInvoice.setCash_discount(aTrans.getCashDiscount() + aTrans.getSpendPointsAmount());
            saleInvoice.setTax_amount(aTrans.getTotalVat());
            saleInvoice.setProfit_margin(aTrans.getTotalProfitMargin());
            saleInvoice.setAmount_tendered(aTrans.getAmountTendered());
            saleInvoice.setStaff_code("");
            saleInvoice.setCurrency_code(aTrans.getCurrencyCode());
            saleInvoice.setCountry_code("");
            saleInvoice.setLoc_level2_code("");
            saleInvoice.setLoc_level3_code("");
            //invoice items
            Bi_stg_sale_invoice_item item = null;
            for (int i = 0; i < aTransItems.size(); i++) {
                item = new Bi_stg_sale_invoice_item();
                item.setBi_item_code(Long.toString(aTransItems.get(i).getItemId()));
                item.setSrc_item_description(new ItemBean().getItem(aTransItems.get(i).getItemId()).getDescription());
                item.setQty(aTransItems.get(i).getItemQty());
                item.setUnit_price(aTransItems.get(i).getUnitPriceExcVat());
                item.setUnit_trade_discount(aTransItems.get(i).getUnitTradeDiscount());
                item.setUnit_vat(aTransItems.get(i).getUnitVat());
                item.setAmount(aTransItems.get(i).getAmountIncVat());
                item.setVat_rated(aTransItems.get(i).getVatRated());
                item.setUnit_cost_price(aTransItems.get(i).getUnitCostPrice());
                item.setUnit_profit_margin(aTransItems.get(i).getUnitProfitMargin());
                saleInvoiceItems.add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void sendCreditDebitNote(long aTransactionId) {
        try {
            Trans t = new CreditDebitNoteBean().getTrans_cr_dr_note(aTransactionId);
            List<TransItem> tis = new CreditDebitNoteBean().getTransItemsByTransactionId_cr_dr_note(aTransactionId);
            if (null == t || null == tis) {
                //do nothing
            } else {
                Gson gson = new Gson();
                String json = "";
                //init objects
                saleCreditDebitNote = new Bi_stg_sale_cr_dr_note();
                saleCreditDebitNoteItems = new ArrayList<>();
                //prepare
                this.prepareCreditDebitNote(t, tis);
                //creating JSON STRING from Object - Branch
                Bi_stg_sale_cr_dr_noteBean noteBean = new Bi_stg_sale_cr_dr_noteBean();
                if (saleCreditDebitNote.getNote_type().equals("CREDIT NOTE")) {
                    noteBean.setTransactionType("CREDIT NOTE");
                } else if (saleCreditDebitNote.getNote_type().equals("DEBIT NOTE")) {
                    noteBean.setTransactionType("DEBIT NOTE");
                } else {
                    noteBean.setTransactionType("");
                }
                noteBean.setSaleCreditDebitNote(saleCreditDebitNote);
                noteBean.setSaleCreditDebitNoteItems(saleCreditDebitNoteItems);
                json = gson.toJson(noteBean);
                //System.out.println("noteBean:" + json);
                com.sun.jersey.api.client.Client client = com.sun.jersey.api.client.Client.create();
                WebResource webResource = client.resource(new Parameter_listBean().getParameter_listByContextName("API", "API_SMBI_URL").getParameter_value());
                ClientResponse response = webResource.type("application/json").post(ClientResponse.class, json);
                String output = response.getEntity(String.class);
                Status s = gson.fromJson(output, Status.class);
                //System.out.println("Success:" + s.getSuccess() + ",Description" + s.getDescription());
                //update the database table:transaction_smbi_map
                if (s.getSuccess() == 1) {
                    new Transaction_smbi_mapBean().updateTransaction_smbi_map(1, new CompanySetting().getCURRENT_SERVER_DATE(), "success", t.getTransactionId(), t.getTransactionTypeId());
                } else {
                    new Transaction_smbi_mapBean().updateTransaction_smbi_map(2, new CompanySetting().getCURRENT_SERVER_DATE(), s.getDescription(), t.getTransactionId(), t.getTransactionTypeId());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void prepareCreditDebitNote(Trans aTrans, List<TransItem> aTransItems) {
        try {
            //Cr/Dr Note
            try {
                Trans Invoice = new TransBean().getTransByTransNumber(aTrans.getTransactionRef());
                saleCreditDebitNote.setSales_date(Invoice.getTransactionDate());
            } catch (Exception e) {
                saleCreditDebitNote.setSales_date(aTrans.getTransactionDate());
            }
            try {
                saleCreditDebitNote.setSection_code(new StoreBean().getStore(aTrans.getStoreId()).getStore_code());
            } catch (Exception e) {
                saleCreditDebitNote.setSection_code("");
            }
            saleCreditDebitNote.setBranch_code(Integer.toString(CompanySetting.getBranchId()));
            saleCreditDebitNote.setBusiness_code(CompanySetting.getCompanyName());
            saleCreditDebitNote.setGroup_code(new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_SMBI_GROUP_CODE").getParameter_value());
            //82->126->CREDIT NOTE, 83->127->DEBIT NOTE
            if (aTrans.getTransactionTypeId() == 82) {
                saleCreditDebitNote.setNote_type("CREDIT NOTE");
            } else if (aTrans.getTransactionTypeId() == 83) {
                saleCreditDebitNote.setNote_type("DEBIT NOTE");
            } else {
                saleCreditDebitNote.setNote_type("");
            }
            saleCreditDebitNote.setNote_number(aTrans.getTransactionNumber());
            saleCreditDebitNote.setInvoice_number_ref(aTrans.getTransactionRef());
            saleCreditDebitNote.setNote_date(aTrans.getTransactionDate());
            try {
                if (aTrans.getBillTransactorId() > 0) {
                    saleCreditDebitNote.setCustomer_name(new TransactorBean().getTransactor(aTrans.getBillTransactorId()).getTransactorNames());
                } else {
                    saleCreditDebitNote.setCustomer_name("Walk-In Customer");
                }
            } catch (Exception e) {
                //
            }
            saleCreditDebitNote.setGross_amount(aTrans.getGrandTotal());
            saleCreditDebitNote.setTrade_discount(aTrans.getTotalTradeDiscount());
            saleCreditDebitNote.setCash_discount(aTrans.getCashDiscount() + aTrans.getSpendPointsAmount());
            saleCreditDebitNote.setTax_amount(aTrans.getTotalVat());
            saleCreditDebitNote.setProfit_margin(aTrans.getTotalProfitMargin());
            saleCreditDebitNote.setAmount_tendered(aTrans.getAmountTendered());
            saleCreditDebitNote.setStaff_code("");
            saleCreditDebitNote.setCurrency_code(aTrans.getCurrencyCode());
            saleCreditDebitNote.setCountry_code("");
            saleCreditDebitNote.setLoc_level2_code("");
            saleCreditDebitNote.setLoc_level3_code("");
            //Cr/Dr Note Items
            Bi_stg_sale_cr_dr_note_item item = null;
            for (int i = 0; i < aTransItems.size(); i++) {
                item = new Bi_stg_sale_cr_dr_note_item();
                item.setBi_item_code(Long.toString(aTransItems.get(i).getItemId()));
                item.setSrc_item_description(new ItemBean().getItem(aTransItems.get(i).getItemId()).getDescription());
                item.setQty(aTransItems.get(i).getItemQty());
                item.setUnit_price(aTransItems.get(i).getUnitPriceExcVat());
                item.setUnit_trade_discount(aTransItems.get(i).getUnitTradeDiscount());
                item.setUnit_vat(aTransItems.get(i).getUnitVat());
                item.setAmount(aTransItems.get(i).getAmountIncVat());
                item.setVat_rated(aTransItems.get(i).getVatRated());
                item.setUnit_cost_price(aTransItems.get(i).getUnitCostPrice());
                item.setUnit_profit_margin(aTransItems.get(i).getUnitProfitMargin());
                saleCreditDebitNoteItems.add(item);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public LoyaltyCard getLoyaltyCardDetail(String aCardNumber) {
        LoyaltyCard loyaltycard = null;
        try {
            if (aCardNumber.length() > 0) {
                String sectioncode = new GeneralUserSetting().getCurrentStore().getStore_code();
                String branchcode = Integer.toString(CompanySetting.getBranchId());
                String businesscode = CompanySetting.getCompanyName();
                String groupcode = new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_SMBI_GROUP_CODE").getParameter_value();
                Gson gson = new Gson();
                String json = "";
                //creating JSON STRING from Object
                GetLoyaltyCardBean cardBean = new GetLoyaltyCardBean();
                cardBean.setTransactionType("LOYALTY CARD");
                cardBean.setSectionCode(sectioncode);
                cardBean.setBranchCode(branchcode);
                cardBean.setBusinessCode(businesscode);
                cardBean.setGroupCode(groupcode);
                cardBean.setCardNumber(aCardNumber);
                json = gson.toJson(cardBean);
                //System.out.println("json:" + json);
                com.sun.jersey.api.client.Client client = com.sun.jersey.api.client.Client.create();
                WebResource webResource = client.resource(new Parameter_listBean().getParameter_listByContextName("API", "API_SMBI_URL").getParameter_value());
                ClientResponse response = webResource.type("application/json").post(ClientResponse.class, json);
                String output = response.getEntity(String.class);
                //System.out.println("output:" + output);
                JSONObject jobj = new JSONObject(output);
                JSONObject jobjS = jobj.getJSONObject("status");
                Status s = gson.fromJson(jobjS.toString(), Status.class);
                if (s.getSuccess() == 1) {
                    JSONObject jobjL = jobj.getJSONObject("loyaltyCard");
                    //loyaltycard = gson.fromJson(jobjL.toString(), LoyaltyCard.class);
                    loyaltycard = new LoyaltyCard();
                    loyaltycard.setCard_number(jobjL.get("card_number").toString());
                    loyaltycard.setFirst_name(jobjL.get("first_name").toString());
                    loyaltycard.setSecond_name(jobjL.get("second_name").toString());
                    loyaltycard.setThird_name(jobjL.get("third_name").toString());
                    loyaltycard.setEmail(jobjL.get("email").toString());
                    loyaltycard.setPhone(jobjL.get("phone").toString());
                    loyaltycard.setDob(null);
                    loyaltycard.setCurrency_code(jobjL.get("currency_code").toString());
                    loyaltycard.setPoints_balance(Double.parseDouble(jobjL.get("points_balance").toString()));
                } else {
                    loyaltycard = null;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
        return loyaltycard;
    }

    public void sendLoyaltyTransaction(long aLoyaltyTransactionId) {
        try {
            Loyalty_transaction t = new Loyalty_transactionBean().getLoyalty_transaction(aLoyaltyTransactionId);
            if (null != t) {
                Gson gson = new Gson();
                String json = "";
                //init objects
                loyaltyTransaction = new LoyaltyTransaction();
                //prepare
                this.prepareLoyaltyTransaction(t);
                //creating JSON STRING from Object
                LoyaltyTransactionBean loyTrans = new LoyaltyTransactionBean();
                loyTrans.setTransactionType("LOYALTY");
                loyTrans.setLoyaltyTransaction(loyaltyTransaction);
                json = gson.toJson(loyTrans);
                com.sun.jersey.api.client.Client client = com.sun.jersey.api.client.Client.create();
                WebResource webResource = client.resource(new Parameter_listBean().getParameter_listByContextName("API", "API_SMBI_URL").getParameter_value());
                ClientResponse response = webResource.type("application/json").post(ClientResponse.class, json);
                String output = response.getEntity(String.class);
                Status s = gson.fromJson(output, Status.class);
                //update the database table
                if (s.getSuccess() == 1) {
                    new Loyalty_transactionBean().updateLoyalty_transaction(1, new CompanySetting().getCURRENT_SERVER_DATE(), "success", t.getLoyalty_transaction_id());
                } else {
                    new Loyalty_transactionBean().updateLoyalty_transaction(2, new CompanySetting().getCURRENT_SERVER_DATE(), s.getDescription(), t.getLoyalty_transaction_id());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    public void prepareLoyaltyTransaction(Loyalty_transaction aLoyalty_transaction) {
        try {
            try {
                loyaltyTransaction.setSection_code(new StoreBean().getStore(aLoyalty_transaction.getStore_id()).getStore_code());
            } catch (Exception e) {
                loyaltyTransaction.setSection_code("");
            }
            loyaltyTransaction.setBranch_code(Integer.toString(CompanySetting.getBranchId()));
            loyaltyTransaction.setBusiness_code(CompanySetting.getCompanyName());
            loyaltyTransaction.setGroup_code(new Parameter_listBean().getParameter_listByContextNameMemory("API", "API_SMBI_GROUP_CODE").getParameter_value());
            loyaltyTransaction.setCard_number(aLoyalty_transaction.getCard_number());
            if (aLoyalty_transaction.getCredit_note_number().length() > 0) {
                loyaltyTransaction.setInvoice_number(aLoyalty_transaction.getCredit_note_number());
            } else if (aLoyalty_transaction.getDebit_note_number().length() > 0) {
                loyaltyTransaction.setInvoice_number(aLoyalty_transaction.getDebit_note_number());
            } else {
                loyaltyTransaction.setInvoice_number(aLoyalty_transaction.getInvoice_number());
            }
            loyaltyTransaction.setTransaction_date(aLoyalty_transaction.getTransaction_date());
            loyaltyTransaction.setPoints_awarded(aLoyalty_transaction.getPoints_awarded());
            loyaltyTransaction.setAmount_awarded(aLoyalty_transaction.getAmount_awarded());
            loyaltyTransaction.setPoints_spent(aLoyalty_transaction.getPoints_spent());
            loyaltyTransaction.setAmount_spent(aLoyalty_transaction.getAmount_spent());
            loyaltyTransaction.setCurrency_code(aLoyalty_transaction.getCurrency_code());
            loyaltyTransaction.setStaff_code(aLoyalty_transaction.getStaff_code());
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, e);
        }
    }

    /**
     * @return the saleInvoice
     */
    public Bi_stg_sale_invoice getSaleInvoice() {
        return saleInvoice;
    }

    /**
     * @param saleInvoice the saleInvoice to set
     */
    public void setSaleInvoice(Bi_stg_sale_invoice saleInvoice) {
        this.saleInvoice = saleInvoice;
    }

    /**
     * @return the saleInvoiceItems
     */
    public List<Bi_stg_sale_invoice_item> getSaleInvoiceItems() {
        return saleInvoiceItems;
    }

    /**
     * @param saleInvoiceItems the saleInvoiceItems to set
     */
    public void setSaleInvoiceItems(List<Bi_stg_sale_invoice_item> saleInvoiceItems) {
        this.saleInvoiceItems = saleInvoiceItems;
    }

    /**
     * @return the saleCreditDebitNote
     */
    public Bi_stg_sale_cr_dr_note getSaleCreditDebitNote() {
        return saleCreditDebitNote;
    }

    /**
     * @param saleCreditDebitNote the saleCreditDebitNote to set
     */
    public void setSaleCreditDebitNote(Bi_stg_sale_cr_dr_note saleCreditDebitNote) {
        this.saleCreditDebitNote = saleCreditDebitNote;
    }

    /**
     * @return the saleCreditDebitNoteItems
     */
    public List<Bi_stg_sale_cr_dr_note_item> getSaleCreditDebitNoteItems() {
        return saleCreditDebitNoteItems;
    }

    /**
     * @param saleCreditDebitNoteItems the saleCreditDebitNoteItems to set
     */
    public void setSaleCreditDebitNoteItems(List<Bi_stg_sale_cr_dr_note_item> saleCreditDebitNoteItems) {
        this.saleCreditDebitNoteItems = saleCreditDebitNoteItems;
    }

    /**
     * @return the loyaltyTransaction
     */
    public LoyaltyTransaction getLoyaltyTransaction() {
        return loyaltyTransaction;
    }

    /**
     * @param loyaltyTransaction the loyaltyTransaction to set
     */
    public void setLoyaltyTransaction(LoyaltyTransaction loyaltyTransaction) {
        this.loyaltyTransaction = loyaltyTransaction;
    }
}
