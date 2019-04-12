package com.fdi.xposed.hooks;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.fdi.xposed.DataModel.SPDBTradeItem;
import com.fdi.xposed.DataModel.SPDBTradeItemDetail;
import com.fdi.xposed.DataModel.SPDBTradeList;
import com.fdi.xposed.DataModel.TradeDetail;
import com.fdi.xposed.Helper.ApiSource;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.fdi.xposed.Helper.RetrofitHelper.API_DOMAIN_SPDB;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class SpdbHook {
    private static final String TAG = SpdbHook.class.getSimpleName();

    private SPDBBroadcastReceiver mBroadcastReceiver = new SPDBBroadcastReceiver();

    private static final String BROADCAST_ACTION_KEY = "xposed_spdb";
    private static final String DEFAULT_FEE = "0";
    private static final String DEFAULT_TRANSFER_TYPE = "網銀轉帳";
    private static final String DEFAULT_IS_UNION_PAY = "0";

    private WebView mHookWebView;
    private String mCookies = "";

    private String mCardNumber = "";
    private int mQueryNumber = 0;
    private int mBeginNumber = 0;
    private String mBeginDate = "";
    private String mEndDate = "";
    private String mQueryType = "";
    private String mNowBalance = "";

    private ArrayList<SPDBTradeItem> mTradeList = null;

    public void initHooking(ClassLoader classLoader) throws NoSuchMethodException {
        XposedHelpers.findAndHookMethod("com.secneo.apkwrapper.ApplicationWrapper", classLoader, "attachBaseContext", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                final Context context = (Context) param.args[0];
                final ClassLoader classLoader = context.getClassLoader();

                /* 列出所有的webViewClient 找出自己需要的 */
//                final Class<?> WebViewClient = findClass("android.webkit.WebViewClient", classLoader);
//
//                XposedBridge.hookAllMethods(WebViewClient,"onPageFinished",new XC_MethodHook(){
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//                        Log.i(TAG,"當前 webViewClient : " + param.thisObject.getClass().getName());
//                        Log.i(TAG, "n url:" + param.args[1]);
//
//
//                    }
//                });

                //---------------------------------------WebViewClient part-----------------------------------------------

                final Class<?> a = findClass("cn.com.spdb.mobilebank.per.m.a", classLoader);

                XposedBridge.hookAllMethods(a, "onPageFinished", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Log.i(TAG, "當前 webViewClient : " + param.thisObject.getClass().getName());
                        Log.i(TAG, "webViewClient url : " + param.args[1]);

                        mHookWebView = (WebView) param.args[0];

                        //TODO 抓當前餘額

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent();
                                //創建一個Inten物件

                                intent.setAction(BROADCAST_ACTION_KEY);
                                intent.putExtra("date", "20190304");
                                //設定Actio的辨識字串

                                context.sendBroadcast(intent);
                            }
                        }, 5000L);

                    }
                });

                //---------------------------------------Application part-----------------------------------------------

                final Class<?> MBApplication = findClass("cn.com.spdb.mobilebank.per.app.MBApplication", classLoader);

                XposedHelpers.findAndHookMethod(MBApplication, "onCreate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        Log.i(TAG, "[ApplicationExtension] onCreate~~~~~~~~~~~~~~~~~~~~~~~~~");

                        Application thisObject = (Application) param.thisObject;
                        IntentFilter intentFilter = new IntentFilter();

                        // 2. 设置接收广播的类型
                        intentFilter.addAction(BROADCAST_ACTION_KEY);
                        thisObject.registerReceiver(mBroadcastReceiver, intentFilter);
                    }
                });
            }
        });
    }

    public class SPDBBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            //TODO 取得cookie,requestBody,完成上述動作後call API

            Log.e(TAG, "[BroadcastReceiver] call.");

            if (mHookWebView == null)
                return;

            mCardNumber = "6217930975054622";
            mQueryNumber = 100;
            mBeginNumber = 0;
            mBeginDate = "20190403";
            mEndDate = "20190411";
            mTradeList = new ArrayList<>();

            getNowBalance(mHookWebView,mCardNumber);

        }
    }

    private void getCookie() {
        XposedHelpers.callMethod(mHookWebView,
                "evaluateJavascript",
                "(function(){ return document.cookie})();",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        mCookies = value;
                        mCookies = mCookies.replace("\"", "");
                        Log.e(TAG, "[return document.cookie] = " + mCookies);

                        getListRequestBody(mCardNumber, mQueryNumber, mBeginNumber, mBeginDate, mEndDate);
                    }
                });
    }

    private void getListRequestBody(String cardNumber, int queryNumber, int beginNumber, String beginDate, String endDate) {
        XposedHelpers.callMethod(mHookWebView,
                "evaluateJavascript",
                "(function(){ " +
                        "var t = '{\"data\" : {\"AcctNo\":\"" + cardNumber + "\",\"AcctType\":\"1\",\"CurrencyNo\":\"01\",\"CurrencyType\":\"0\",\"QueryNumber\":\"" + queryNumber + "\"," +
                        "\"BeginNumber\":\"" + beginNumber + "\",\"BeginDate\":\"" + beginDate + "\",\"EndDate\":\"" + endDate + "\",\"CrDtIndicator\":\"\",\"AcctKind\":\"0001\",\"FundSortOrder\":\"1\"," +
                        "\"TransId\":\"PdIdvDmdDepHstryDtlQry\"} ,\"callback\":\"\"}';" +
                        "var result = window.SysClientJs.encryptDataNew(t);" +
                        "return result;" +
                        "})();",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.e(TAG, "[window.SysClientJs.encryptDataNew] = " + value);
                        getTradeList(mCookies, value);
                    }
                });

    }

    private void getTradeList(String cookie, String requestBody) {
        Disposable disposable = ApiSource.getInstance(API_DOMAIN_SPDB).getSPDBTradeList(cookie, requestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<SPDBTradeList>() {
                    @Override
                    public void accept(SPDBTradeList spdbTradeList) throws Exception {
                        if (spdbTradeList == null) {
                            Log.e(TAG, "[getTradeList] null");
                        } else {
                            Log.e(TAG, "[getTradeList] not null result = " + spdbTradeList.getSPDBTradeList().toString());
                            List<SPDBTradeItem> list = spdbTradeList.getList();
                            mTradeList.addAll(list);

                            //列表還沒全部拉下來，需要繼續拉
                            if (list.size() == mQueryNumber) {
                                Log.e(TAG, "[list.size() == queryNumber]");
                                getListRequestBody(mCardNumber, mQueryNumber, mTradeList.size(), mBeginDate, mEndDate);
                            } else {
                                //TODO 如果有preTradeID 需要對列表做篩選後再要detail
                                Log.e(TAG, "[list.size() != queryNumber] 列表全拉下來了，開始去要detailRequestBody");
                                getDetailRequestBody(0);


                            }
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "[getTradeList] throwable" + throwable.getMessage());
                    }
                });
    }

    private void getDetailRequestBody(final int position) {
        XposedHelpers.callMethod(mHookWebView,
                "evaluateJavascript",
                "(function(){ " +
                        "var t = '{\"data\" : {\"BusinessId\":\"" + mTradeList.get(position).getBusinessId() + "\",\"TransId\":\"QueryPaymentHistoryDetail\"} ,\"callback\":\"\"}';" +
                        "var result = window.SysClientJs.encryptDataNew(t);" +
                        "return result;" +
                        "})();",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
//                        Log.e(TAG, "[window.SysClientJs.encryptDataNew] = " + value);
                        mTradeList.get(position).setDetailRequestBody(value);
                        if (position + 1 < mTradeList.size()) {
                            getDetailRequestBody(position + 1);
                        } else {
                            //DetailRequestBody都要完了，開始call detail api
                            getTradeDetail(0);
                        }
                    }
                });
    }

    private void getTradeDetail(final int position) {
        Disposable disposable = ApiSource.getInstance(API_DOMAIN_SPDB).getSPDBTradeDetail(mCookies, mTradeList.get(position).getDetailRequestBody())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<SPDBTradeItemDetail>() {
                    @Override
                    public void accept(SPDBTradeItemDetail spdbTradeItemDetail) throws Exception {
//                        Log.e(TAG,"[getTradeDetail]:"+spdbTradeItemDetail.getSPDBTradeItemDetail());
                        mTradeList.get(position).setMemo(spdbTradeItemDetail.getPostScript());
                        if (position + 1 < mTradeList.size()) {
                            getTradeDetail(position + 1);
                        } else {
                            //TODO Detail都要完了，開始準備收尾
                            printTradeList();
                            toTradeDetailList();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "[getTradeDetail] throwable" + throwable.getMessage());
                    }
                });
    }

    private void printTradeList() {
        for (int i = 0; i < mTradeList.size(); i++) {
            Log.e(TAG, "[printTradeList] tradeItem[" + i + "] : " + mTradeList.get(i).getSPDBTradeItem().toString());
        }
    }

    private void getNowBalance(final Object webView,String cardNumber) {
        XposedHelpers.callMethod(webView, "evaluateJavascript",
                "(function(){ return document.getElementById('Balance"+cardNumber+"').innerText})();",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(java.lang.String value) {
                        mNowBalance = value.replace("\\\"","");
                        Log.e(TAG,"[getNowBalance] = "+mNowBalance);
                        getCookie();
                    }
                });
    }

    private void toTradeDetailList() {
        ArrayList<TradeDetail> tradeDetailList = new ArrayList<>();
        TradeDetail temp;
        SPDBTradeItem spdbTradeItem;
        for (int i = 0; i < mTradeList.size(); i++) {
            spdbTradeItem = mTradeList.get(i);

            temp = new TradeDetail();
            temp.setTransId(spdbTradeItem.getBusinessId());

            if (spdbTradeItem.getSign().equals("+")) {
                temp.setInAccount(spdbTradeItem.getTranCnterAcctNo());
                temp.setInName(spdbTradeItem.getTranCnterNm());
//                temp.setOutAccount();
//                temp.setOutName();

                temp.setAmount(spdbTradeItem.getTranAmt());
            } else {
//                temp.setInAccount();
//                temp.setInName();
                temp.setOutAccount(spdbTradeItem.getTranCnterAcctNo());
                temp.setOutName(spdbTradeItem.getTranCnterNm());
                temp.setAmount("-" + spdbTradeItem.getTranAmt());
            }

            temp.setFee(DEFAULT_FEE);
            temp.setRemark(spdbTradeItem.getMemo());
            temp.setTransferType(DEFAULT_TRANSFER_TYPE);
            temp.setTransferTime(spdbTradeItem.getTranDate1() + spdbTradeItem.getTranTime2());


            if (mTradeList.size() - 1 >= i + 1) {
                temp.setPreTransID(mTradeList.get(i + 1).getBusinessId());
            } else {
                //TODO 有點疑惑
                temp.setPreTransID("-");
            }

            temp.setQueryType(mQueryType);
            temp.setBalance(spdbTradeItem.getAcctBal());
            //要給現在帳號餘額
            temp.setNowBalance(mNowBalance);

            temp.setIsUnionPay(DEFAULT_IS_UNION_PAY);

            tradeDetailList.add(temp);
        }

        File detailList = new File(Environment.getExternalStorageDirectory().getPath() + "/recordDetail_SPDB.json");
        writeToFile(detailList, new Gson().toJson(tradeDetailList));
    }

    private void writeToFile(File fout, String data) {
        Log.e(TAG,"[writeToFile] call.");
        FileOutputStream osw = null;
        try {
            osw = new FileOutputStream(fout);
            osw.write("".getBytes());
            osw.flush();
            osw.write(data.getBytes());
            osw.flush();
        } catch (Exception e) {
        } finally {
            try {
                osw.close();
            } catch (Exception e) {
            }

        }
    }
}
