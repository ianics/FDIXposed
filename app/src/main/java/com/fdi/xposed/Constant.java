package com.fdi.xposed;

public class Constant {

    //-------------------------------------------------- 帶入參數預設值 ----------------------------------------------------------
    public static final String DEFAULT_FEE = "0";
    public static final String DEFAULT_TRANSFER_TYPE = "网银转账";
    public static final String DEFAULT_IS_UNION_PAY = "0";

    //-------------------------------------------------- broadcast receiver key ----------------------------------------------------------
    public static final String KEY_START_DATE = "startDate";
    public static final String KEY_END_DATE = "endDate";
    public static final String KEY_QUERY_TYPE = "queryType";
    public static final String KEY_PRE_TRAINS_ID = "preTransID";
    //不一定需要 視銀行需求添加
    public static final String KEY_NAME = "name";
    public static final String KEY_ACCOUNT = "account";
    public static final String KEY_NOW_BALANCE = "now_balance";
}
