package com.fdi.xposed.DataModel;

import com.google.gson.annotations.SerializedName;

public class EciticTradeDetail {
    @SerializedName("transID")//唯一識別交易ID
    private String transId;
    @SerializedName("inAccount")//轉入帳號
    private String inAccount;
    @SerializedName("inName")//轉入帳號戶名
    private String inName;
    @SerializedName("outAccount")//轉出帳號
    private String outAccount;
    @SerializedName("outName")//轉出帳號戶名
    private String outName;
    @SerializedName("amount")//金額
    private String amount;
    @SerializedName("fee")//手續費
    private String fee;
    @SerializedName("remark")//附言
    private String remark;
    @SerializedName("transferType")//銀行紀錄的轉帳類型
    private String transferType;
    @SerializedName("transferTime")//轉帳時間
    private String transferTime;
    @SerializedName("preTransID")//前一筆的交易紀錄唯一識別ID
    private String preTransID;
    @SerializedName("queryType")//１：查詢 2:補查詢
    private String queryType;
    @SerializedName("balance")//該筆交易餘額
    private String balance;
    @SerializedName("nowBalance")//現在帳號餘額
    private String nowBalance;
    @SerializedName("isUnionPay")//是否為銀聯轉帳 1 :是　０：否
    private String isUnionPay;

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getInAccount() {
        return inAccount;
    }

    public void setInAccount(String inAccount) {
        this.inAccount = inAccount;
    }

    public String getInName() {
        return inName;
    }

    public void setInName(String inName) {
        this.inName = inName;
    }

    public String getOutAccount() {
        return outAccount;
    }

    public void setOutAccount(String outAccount) {
        this.outAccount = outAccount;
    }

    public String getOutName() {
        return outName;
    }

    public void setOutName(String outName) {
        this.outName = outName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTransferType() {
        return transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public String getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(String transferTime) {
        this.transferTime = transferTime;
    }

    public String getPreTransID() {
        return preTransID;
    }

    public void setPreTransID(String preTransID) {
        this.preTransID = preTransID;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getNowBalance() {
        return nowBalance;
    }

    public void setNowBalance(String nowBalance) {
        this.nowBalance = nowBalance;
    }

    public String getIsUnionPay() {
        return isUnionPay;
    }

    public void setIsUnionPay(String isUnionPay) {
        this.isUnionPay = isUnionPay;
    }
}
