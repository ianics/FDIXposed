package com.fdi.xposed.DataModel;

import org.json.JSONException;
import org.json.JSONObject;

public class SPDBTradeItem {
    private String AuthNo;
    //當前餘額
    private String AcctBal;
    //交易對象名稱
    private String TranCnterNm;
    private String DebitFlag;
    //交易對象帳號
    private String TranCnterAcctNo;
    //交易金額
    private String TranAmt;
    private String TranCd;
    //交易ID
    private String BusinessId;
    //交易時間(時分秒)
    private String TranTime2;
    //交易為收款或付款判斷
    private String Sign;
    private String AbstractCode;
    //交易對象銀行
    private String CnterBankName;
    private String RemitFlag;
    private String Postscript;
    //交易時間(年月日)
    private String TranDate1;
    private String DetailRequestBody;
    private String Memo;

    public JSONObject getSPDBTradeItem() {
        JSONObject json = new JSONObject();
        try {
            json.put("交易ID", BusinessId);
            json.put("交易對象名稱", TranCnterNm);
            json.put("交易對象帳號", TranCnterAcctNo);
            json.put("交易對象銀行", CnterBankName);
            json.put("交易金額", Sign + TranAmt);
            json.put("該筆交易餘額", AcctBal);
            json.put("交易時間", TranDate1 + " " + TranTime2);
            json.put("附言", Memo);
            json.put("詳細頁RequestBody", DetailRequestBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public String getAuthNo() {
        return AuthNo;
    }

    public void setAuthNo(String authNo) {
        AuthNo = authNo;
    }

    public String getAcctBal() {
        return AcctBal;
    }

    public void setAcctBal(String acctBal) {
        AcctBal = acctBal;
    }

    public String getTranCnterNm() {
        return TranCnterNm;
    }

    public void setTranCnterNm(String tranCnterNm) {
        TranCnterNm = tranCnterNm;
    }

    public String getDebitFlag() {
        return DebitFlag;
    }

    public void setDebitFlag(String debitFlag) {
        DebitFlag = debitFlag;
    }

    public String getTranCnterAcctNo() {
        return TranCnterAcctNo;
    }

    public void setTranCnterAcctNo(String tranCnterAcctNo) {
        TranCnterAcctNo = tranCnterAcctNo;
    }

    public String getTranAmt() {
        return TranAmt;
    }

    public void setTranAmt(String tranAmt) {
        TranAmt = tranAmt;
    }

    public String getTranCd() {
        return TranCd;
    }

    public void setTranCd(String tranCd) {
        TranCd = tranCd;
    }

    public String getBusinessId() {
        return BusinessId;
    }

    public void setBusinessId(String businessId) {
        BusinessId = businessId;
    }

    public String getTranTime2() {
        return TranTime2;
    }

    public void setTranTime2(String tranTime2) {
        TranTime2 = tranTime2;
    }

    public String getSign() {
        return Sign;
    }

    public void setSign(String sign) {
        Sign = sign;
    }

    public String getAbstractCode() {
        return AbstractCode;
    }

    public void setAbstractCode(String abstractCode) {
        AbstractCode = abstractCode;
    }

    public String getCnterBankName() {
        return CnterBankName;
    }

    public void setCnterBankName(String cnterBankName) {
        CnterBankName = cnterBankName;
    }

    public String getRemitFlag() {
        return RemitFlag;
    }

    public void setRemitFlag(String remitFlag) {
        RemitFlag = remitFlag;
    }

    public String getPostscript() {
        return Postscript;
    }

    public void setPostscript(String postscript) {
        Postscript = postscript;
    }

    public String getTranDate1() {
        return TranDate1;
    }

    public void setTranDate1(String tranDate1) {
        TranDate1 = tranDate1;
    }

    public String getDetailRequestBody() {
        return DetailRequestBody;
    }

    public void setDetailRequestBody(String detailRequestBody) {
        DetailRequestBody = detailRequestBody;
    }

    public String getMemo() {
        return Memo;
    }

    public void setMemo(String memo) {
        Memo = memo;
    }
}
