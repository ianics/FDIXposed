package com.fdi.xposed.DataModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SPDBTradeList {
    private String JSESSIONID;
    private Boolean JUMP_BASEPARAM;
    private String ResponseCode;
    private String STATUS;
    private List<SPDBTradeItem> List;
    private String TransSeqNo;
    private String ResponseMsg;
    private String TransName;

    public SPDBTradeList(){

    }

    public JSONObject getSPDBTradeList() {
        JSONObject json = new JSONObject();
        try {
            json.put("JSESSIONID", JSESSIONID);
            json.put("JUMP_BASEPARAM", JUMP_BASEPARAM);
            json.put("ResponseCode", ResponseCode);
            json.put("STATUS", STATUS);
            json.put("List", List);
            json.put("TransSeqNo", TransSeqNo);
            json.put("ResponseMsg", ResponseMsg);
            json.put("TransName", TransName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public String getJSESSIONID() {
        return JSESSIONID;
    }

    public void setJSESSIONID(String JSESSIONID) {
        this.JSESSIONID = JSESSIONID;
    }

    public Boolean getJUMP_BASEPARAM() {
        return JUMP_BASEPARAM;
    }

    public void setJUMP_BASEPARAM(Boolean JUMP_BASEPARAM) {
        this.JUMP_BASEPARAM = JUMP_BASEPARAM;
    }

    public String getResponseCode() {
        return ResponseCode;
    }

    public void setResponseCode(String responseCode) {
        ResponseCode = responseCode;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }

    public java.util.List<SPDBTradeItem> getList() {
        return List;
    }

    public void setList(java.util.List<SPDBTradeItem> list) {
        List = list;
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
}
