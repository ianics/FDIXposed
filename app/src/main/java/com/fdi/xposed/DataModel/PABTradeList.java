package com.fdi.xposed.DataModel;

import java.util.List;

public class PABTradeList {

    public int count;
    public String result_key;
    public List<PABTradeItem> result_value;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getResult_key() {
        return result_key;
    }

    public void setResult_key(String result_key) {
        this.result_key = result_key;
    }

    public List<PABTradeItem> getResult_value() {
        return result_value;
    }

    public void setResult_value(List<PABTradeItem> result_value) {
        this.result_value = result_value;
    }
}
