package com.fdi.xposed.DataModel;

public class PABTradeResult {

    public PABTradeList data;
    public String responseCode;
    public String responseMsg;

    public PABTradeList getData() {
        return data;
    }

    public void setData(PABTradeList data) {
        this.data = data;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }
}
