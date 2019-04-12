package com.fdi.xposed.DataModel;

import org.json.JSONException;
import org.json.JSONObject;

public class SPDBTradeItemDetail {
    private String PayeeAcct;
    private String Amount;
    private String PayeeBankName;
    private String PostScript;
    private String PayerBankName;
    private String AcctNo;
    private String ResponseCode;
    private String Charge;
    private String STATUS;
    private String NetBusinessStatus;
    private String TransSeqNo;
    private String ResponseMsg;
    private String TransName;
    private String PayerName;
    private String BusinessId;
    private String PayeeName;
    private String TransDate;

    public JSONObject getSPDBTradeItemDetail() {
        JSONObject json = new JSONObject();
        try {
            json.put("交易ID", BusinessId);
            json.put("附言", PostScript);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public String getPayeeAcct() {
        return PayeeAcct;
    }

    public void setPayeeAcct(String payeeAcct) {
        PayeeAcct = payeeAcct;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }

    public String getPayeeBankName() {
        return PayeeBankName;
    }

    public void setPayeeBankName(String payeeBankName) {
        PayeeBankName = payeeBankName;
    }

    public String getPostScript() {
        return PostScript;
    }

    public void setPostScript(String postScript) {
        PostScript = postScript;
    }

    public String getPayerBankName() {
        return PayerBankName;
    }

    public void setPayerBankName(String payerBankName) {
        PayerBankName = payerBankName;
    }

    public String getAcctNo() {
        return AcctNo;
    }

    public void setAcctNo(String acctNo) {
        AcctNo = acctNo;
    }

    public String getResponseCode() {
        return ResponseCode;
    }

    public void setResponseCode(String responseCode) {
        ResponseCode = responseCode;
    }

    public String getCharge() {
        return Charge;
    }

    public void setCharge(String charge) {
        Charge = charge;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }

    public String getNetBusinessStatus() {
        return NetBusinessStatus;
    }

    public void setNetBusinessStatus(String netBusinessStatus) {
        NetBusinessStatus = netBusinessStatus;
    }

    public String getTransSeqNo() {
        return TransSeqNo;
    }

    public void setTransSeqNo(String transSeqNo) {
        TransSeqNo = transSeqNo;
    }

    public String getResponseMsg() {
        return ResponseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        ResponseMsg = responseMsg;
    }

    public String getTransName() {
        return TransName;
    }

    public void setTransName(String transName) {
        TransName = transName;
    }

    public String getPayerName() {
        return PayerName;
    }

    public void setPayerName(String payerName) {
        PayerName = payerName;
    }

    public String getBusinessId() {
        return BusinessId;
    }

    public void setBusinessId(String businessId) {
        BusinessId = businessId;
    }

    public String getPayeeName() {
        return PayeeName;
    }

    public void setPayeeName(String payeeName) {
        PayeeName = payeeName;
    }

    public String getTransDate() {
        return TransDate;
    }

    public void setTransDate(String transDate) {
        TransDate = transDate;
    }
}
