package com.fdi.xposed.DataModel;

public class RecordDetail {

    //商戶號
    private String PaymentMerchantID;

    //交易流水號-
    private String TransactionNo;

    //支付寶賬號ID
    private String PaymentAlipayID;

    //請回傳支付寶-
    private String PayType;

    //金額-
    private String Amount;

    //手續費
    private String Fee;

    //狀態的中文解說(類型)-
    private String StatusRemark;

    //附言（沒有就傳空字串）
    private String Remark;

    //交易時間-
    private String TransactionTime;

    //交易餘額-
    private String Balance;

    //帳號目前餘額
    private String NowBalance;

    public RecordDetail(String TransactionNo,String PayType,String Amount,String StatusRemark,String TransactionTime,String Balance){
        this.PaymentMerchantID = "";
        this.TransactionNo = TransactionNo;
        this.PaymentAlipayID = "";
        this.PayType = PayType;
        this.Amount = Amount;
        this.Fee = "";
        this.StatusRemark = StatusRemark;
        this.Remark = "";
        this.TransactionTime = TransactionTime;
        this.Balance = Balance;
        this.NowBalance = "";

    }

}
