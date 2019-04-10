package com.fdi.xposed.hooks;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.fdi.xposed.DataModel.EciticTradeDetail;
import com.fdi.xposed.Utils;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findClass;

public class EciticHook {
    private static final String TAG = EciticHook.class.getSimpleName();

    private WebView mHookWebView;

    ArrayList<EciticTradeDetail> mList;
    private int position = 0;
    private int mElementSize = 0; //待點擊列表總數

    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String QUERY_TYPE = "queryType";
    private static final String NAME = "name";
    private static final String ACCOUNT = "account";

    private static final String FLAG_IN = "收入";
    private static final String DEFAULT_FEE = "0";
    private static final String DEFAULT_TRANSFER_TYPE = "網銀轉帳";
    private static final String DEFAULT_PRE_TRANS_ID = "";
    private static final String DEFAULT_IS_UNION_PAY = "0";

    private String mStartDate;
    private String mEndDate;
    private String mQueryType;
    private String mUserAccount;
    private String mUserName;
    private String mNowBalance;

    public class TestBroadcastReceiver extends BroadcastReceiver {

        // 复写onReceive()方法
        // 接收到广播后，则自动调用该方法
        @Override
        public void onReceive(Context context, Intent intent) {
            //写入接收广播后的操作

            mStartDate = intent.getStringExtra(START_DATE);
            mEndDate = intent.getStringExtra(END_DATE);
            mQueryType = intent.getStringExtra(QUERY_TYPE);
            mUserAccount = intent.getStringExtra(ACCOUNT);
            mUserName = intent.getStringExtra(NAME);

            if (mStartDate == null) mStartDate = "";
            if (mEndDate == null) mEndDate = "";
            if (mQueryType == null) mQueryType = "";
            if (mUserAccount == null) mUserAccount = "";
            if (mUserName == null) mUserName = "";

            Log.e(TAG, "[BroadcastReceiver] getReceive = mStartDate:" + mStartDate + ", mEndDate:" + mEndDate + ", mQueryType:" + mQueryType);

            if (mHookWebView == null)
                return;

            XposedHelpers.callMethod(mHookWebView, "evaluateJavascript", "(function(){ return window.document.getElementsByTagName('html')[0].innerHTML})();", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    value = value.replace("\\u003C", "<");
                    value = value.replace("\\&quot;", "");
                    value = value.replace("\\\"", "\"");
                    value = value.replace(" \\n", "");

//                    int maxLogSize = 1000;
//                    for (int i = 0; i <= value.length() / maxLogSize; i++) {
//                        int start = i * maxLogSize;
//                        int end = (i + 1) * maxLogSize;
//                        end = end > value.length() ? value.length() : end;
//
//                        Log.e(TAG, "[onReceiveValue] html:" + value.substring(start, end));
//                    }

                    mList = new ArrayList<>();

                    mNowBalance = Jsoup.parse(value).selectFirst("[data-name=CRTBAL]").text().replace(",","");

                    Elements elements = Jsoup.parse(value).getElementsByClass("common_div_typeIn_sub");
                    Element element;
                    String t = "";
                    mElementSize = elements.size();

                    for (int i = 2; i < mElementSize; i++) {
                        element = elements.get(i);

                        t = element.child(0).text().replace("\\n", "").replace(" ", "");
                        Log.e(TAG, "[Jsoup result] = className:" + element.className() + ", tagName:" + element.tagName() + ", element.text()=" + t + ", mNowBalance:" + mNowBalance);

                        EciticTradeDetail detail = new EciticTradeDetail();
                        detail.setAmount(t.replace("+", "").replace(",", ""));
                        detail.setNowBalance(mNowBalance);
                        mList.add(detail);
                    }

                    getDetail(position);
                    position++;

                }
            });

        }
    }

    private TestBroadcastReceiver mBroadcastReceiver = new TestBroadcastReceiver();

    public void initHooking(ClassLoader classLoader) throws NoSuchMethodException {

        XposedHelpers.findAndHookMethod("com.secneo.apkwrapper.ApplicationWrapper", classLoader, "attachBaseContext", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                final Context context = (Context) param.args[0];
                final ClassLoader classLoader = context.getClassLoader();

                final Class<?> CBWebView = findClass("com.citicbank.cbframework.webview.CBWebView", classLoader);

                Method[] m = CBWebView.getDeclaredMethods();

//                for (int i = 0; i < m.length; i++) {
//                    Log.e(TAG, "[CBWebView] methods : " + m[i].toString());
//                }

                final Class<?> CBBaseWebView = findClass("com.citicbank.cbframework.webview.CBBaseWebView", classLoader);

//                m = CBBaseWebView.getDeclaredMethods();
//
//                for (int i = 0; i < m.length; i++) {
//                    Log.e(TAG, "[CBBaseWebView] methods : " + m[i].toString());
//                }

                final Class<?> CBWebViewLoadListener = findClass("com.citicbank.cbframework.webview.CBWebViewLoadListener", classLoader);

//                m = CBWebViewLoadListener.getDeclaredMethods();
//
//                for (int i = 0; i < m.length; i++) {
//                    Log.e(TAG, "[CBWebViewLoadListener] methods : " + m[i].toString());
//                }

                final Class<?> CBWebViewListener = findClass("com.citicbank.cbframework.webview.CBWebViewListener", classLoader);

//                m = CBWebViewListener.getDeclaredMethods();
//
//                for (int i = 0; i < m.length; i++) {
//                    Log.e(TAG, "[CBWebViewListener] methods : " + m[i].toString());
//                }

                final Class<?> CBJSWebView = findClass("com.citicbank.cbframework.webview.CBJSWebView", classLoader);

//                m = CBJSWebView.getDeclaredMethods();
//
//                for (int i = 0; i < m.length; i++) {
//                    Log.e(TAG, "[CBJSWebView] methods : " + m[i].toString());
//                }

                final Class<?> n = findClass("com.citicbank.cbframework.webview.n", classLoader);

//                m = n.getDeclaredMethods();
//
//                for (int i = 0; i < m.length; i++) {
//                    Log.e(TAG, "[n] methods : " + m[i].toString());
//                }


                //沒有呼叫到的感覺
                XposedBridge.hookAllMethods(CBWebView, "setCBWebviewLoadListener", new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Log.e(TAG, "[CBWebViewLoadListener] Hook onPageFinished classPath:" + param.args[0].getClass().getName());
                        Log.e(TAG, "[CBWebViewLoadListener] Hook onPageFinished parameter:" + param.args[0].toString());
//                        getHtmlString(param.args[0]);
                    }
                });

//                HttpHook.initHooking(classLoader);

                XposedBridge.hookAllMethods(CBWebView, "setWebViewClient", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Log.i(TAG, "[setWebViewClient] Hook ");
                    }
                });

                XposedBridge.hookAllMethods(n, "onPageFinished", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Log.i(TAG, "当前 n : " + param.thisObject.getClass().getName());
                        Log.i(TAG, "n url:" + param.args[1]);

                        mHookWebView = (WebView) param.args[0];

//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                Intent intent = new Intent();
//                                //創建一個Inten物件
//
//                                intent.setAction("xposed");
//                                intent.putExtra("date", "20190304");
//                                //設定Actio的辨識字串
//
//                                context.sendBroadcast(intent);
//                            }
//                        }, 5000L);

                    }
                });

                final Class<?> ApplicationExtension = findClass("com.ecitic.bank.mobile.common.ApplicationExtension", classLoader);

                XposedHelpers.findAndHookMethod(ApplicationExtension, "onCreate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        Log.i(TAG, "[ApplicationExtension] onCreate~~~~~~~~~~~~~~~~~~~~~~~~~");

                        Application thisObject = (Application) param.thisObject;
                        IntentFilter intentFilter = new IntentFilter();

                        // 2. 设置接收广播的类型
                        intentFilter.addAction("xposed");
                        thisObject.registerReceiver(mBroadcastReceiver, intentFilter);
                    }
                });

//                XposedHelpers.findAndHookMethod(ApplicationExtension, "onTrimMemory",int.class, new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//                        Log.i(TAG,"[ApplicationExtension] onTrimMemory~~~~~~~~~~~~~~~~~~~~~~~~~");
//                        if(mBroadcastReceiver!=null){
//                            Application thisObject = (Application) param.thisObject;
//                            thisObject.unregisterReceiver(mBroadcastReceiver);
//                        }
//
//                    }
//                });

            }
        });
    }

    private void getHtmlString(final Object webView) {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
        XposedHelpers.callMethod(webView, "evaluateJavascript", "(function(){ return window.document.getElementsByTagName('html')[0].innerHTML})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(java.lang.String value) {
//                            Log.e(TAG, "[onReceiveValue] html:" + value);

                if (value.contains("加载中")) {
                    Log.e(TAG, "[onReceiveValue] html: 加载中");
                    getHtmlString(webView);
                } else {

                    value = value.replace("\\u003C", "<");
                    value = value.replace("\\&quot;", "");

                    int maxLogSize = 1000;
                    for (int i = 0; i <= value.length() / maxLogSize; i++) {
                        int start = i * maxLogSize;
                        int end = (i + 1) * maxLogSize;
                        end = end > value.length() ? value.length() : end;

                        Log.e(TAG, "[onReceiveValue] html:" + value.substring(start, end));
                    }


//                    AndroidAppHelper.currentApplication().getSharedPreferences("Demo", Context.MODE_PRIVATE).edit().putString("Remark",value).apply();
                }


            }
        });
//            }
//        },5000);
    }

    public void getDetail(final int position) {
        Log.e(TAG, "[getDetail] position:" + position);
        int realPosition = position + 2;
        XposedHelpers.callMethod(mHookWebView, "evaluateJavascript",
                "(function(){ " +
                        "document.getElementsByClassName('common_div_typeIn_sub')[" + realPosition + "].click();" +
                        "return document.getElementsByClassName('fw-page-current fw-page-content')[0].innerHTML;" +
                        "}()); ", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {

                        if (TextUtils.isEmpty(value)) return;

                        value = value.replace("\\u003C", "<");
                        value = value.replace("\\&quot;", "");
                        value = value.replace("\\\"", "\"");
                        value = value.replace(" \\n", "");

//                        int maxLogSize = 1000;
//                        for (int i = 0; i <= value.length() / maxLogSize; i++) {
//                            int start = i * maxLogSize;
//                            int end = (i + 1) * maxLogSize;
//                            end = end > value.length() ? value.length() : end;
//
//                            Log.e(TAG, "[onReceiveValue] html:" + value.substring(start, end));
//                        }

                        Document document = Jsoup.parse(value);

                        if (document == null) {
                            return;
                        }

                        String inOrOut = "", transId = "", amount = "", inAccount = "", inName = "", outAccount = "", outName = "", remark = "", transferTime = "", balance = "";

                        Element element = document.selectFirst("[data-name=LOANFLAG]");
                        if (element != null) {
                            inOrOut = element.text();
                        }
                        element = document.selectFirst("[data-name=TRANNO]");
                        if (element != null) {
                            transId = element.text();
                        }

                        if (inOrOut.equals(FLAG_IN)) {
                            element = document.selectFirst("[data-name=OTHERACCNO_Show]");
                            if (element != null) {
                                outAccount = element.text().trim().replace(" ","");
                            }
                            element = document.selectFirst("[data-name=OTHERACCNAME]");
                            if (element != null) {
                                outName = element.text();
                            }
                            inAccount = mUserAccount;
                            inName = mUserName;
                            element = document.selectFirst("[data-name=TRANAMT]");
                            if (element != null) {
                                amount = element.text();
                            }
                        } else {
                            outAccount = mUserAccount;
                            outName = mUserName;
                            element = document.selectFirst("[data-name=OTHERACCNO_Show]");
                            if (element != null) {
                                inAccount = element.text().trim().replace(" ","");
                            }
                            element = document.selectFirst("[data-name=OTHERACCNAME]");
                            if (element != null) {
                                inName = element.text();
                            }
                            element = document.selectFirst("[data-name=TRANAMT]");
                            if (element != null) {
                                amount = "-" + element.text();
                            }
                        }

                        element = document.selectFirst("[data-name=MEMO]");
                        if (element != null) {
                            remark = Utils.filterNumber(element.text());
                        }
                        element = document.selectFirst("[data-name=TRANTIME]");
                        if (element != null) {
                            transferTime = element.text();
                        }
                        element = document.selectFirst("[data-name=BALAMT]");
                        if (element != null) {
                            balance = element.text().replace(",","");
                        }

                        Log.e(TAG, "[inOrOut]:" + inOrOut + ", [transId]:" + transId + ", [amount]:" + amount + ", [inAccount]:" + inAccount + ", [inName]:" + inName + ", [outAccount]:" + outAccount + ", [outName]:" + outName +
                                ", [remark]:" + remark + ", [transferTime]:" + transferTime + ", [balance]:" + balance);

                        EciticTradeDetail detail = mList.get(position);
                        detail.setTransId(transId);
                        detail.setInAccount(inAccount);
                        detail.setInName(inName);
                        detail.setOutAccount(outAccount);
                        detail.setOutName(outName);
                        detail.setFee(DEFAULT_FEE);
                        detail.setRemark(remark);
                        detail.setTransferType(DEFAULT_TRANSFER_TYPE);
                        detail.setTransferTime(transferTime);
                        detail.setPreTransID(DEFAULT_PRE_TRANS_ID);
                        if(mList.size()-1 >= position && position-1 >= 0){
                            mList.get(position-1).setPreTransID(transId);
                        }

//                        if (mList.size() > 0) {
//                            detail.setPreTransID(mList.get(mList.size() - 1).getTransId());
//                        } else if (mList.size() == mElementSize - 1) {
//                            detail.setPreTransID(DEFAULT_PRE_TRANS_ID);
//                        }
                        detail.setQueryType(mQueryType);
                        detail.setBalance(balance);
                        detail.setIsUnionPay(DEFAULT_IS_UNION_PAY);

//                        mList.add(position, detail);

                        if (position + 1 < mList.size()) {
                            final int nextPosition = position + 1;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getDetail(nextPosition);
                                }
                            }, 1000);
                        } else {
                            File detailList = new File(Environment.getExternalStorageDirectory().getPath() + "/recordDetail.json");
                            writeToFile(detailList, new Gson().toJson(mList));
                        }

                    }
                });
    }

    private void writeToFile(File fout, String data) {
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
