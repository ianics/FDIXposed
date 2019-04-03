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

import com.fdi.xposed.DataModel.EciticTradeDetail;

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

    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String QUERY_TYPE = "queryType";
    private static final String NAME = "name";
    private static final String ACCOUNT = "account";

    private static final String FLAG_IN = "收入";

    private String mStartDate;
    private String mEndDate;
    private String mQueryType;
    private String mUserAccount;
    private String mUserName;

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

            Log.e(TAG, "[BroadcastReceiver] getReceive = mStartDate:" + mStartDate +", mEndDate:"+mEndDate+", mQueryType:"+mQueryType);

            if (mHookWebView == null)
                return;

//            mHookWebView.evaluateJavascript("(function(){ return var result =\"\";\n" +
//                    "var tempobj = document.getElementsByClassName('common_div_typeIn_sub');\n" +
//                    "for(int i=2; i<tempobj.length;i++){\n" +
//                    "    result += i+\"/\"+tempobj[i].innerText+\",\"\n" +
//                    "};\n" +
//                    "return result;})();", new ValueCallback<String>() {

            XposedHelpers.callMethod(mHookWebView, "evaluateJavascript", "(function(){ return window.document.getElementsByTagName('html')[0].innerHTML})();", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    value = value.replace("\\u003C", "<");
                    value = value.replace("\\&quot;", "");
                    value = value.replace("\\n", "");

//                    int maxLogSize = 1000;
//                    for (int i = 0; i <= value.length() / maxLogSize; i++) {
//                        int start = i * maxLogSize;
//                        int end = (i + 1) * maxLogSize;
//                        end = end > value.length() ? value.length() : end;
//
//                        Log.e(TAG, "[onReceiveValue] html:" + value.substring(start, end));
//                    }

                    mList = new ArrayList<>();

                    File htmlFile = new File(Environment.getExternalStorageDirectory().getPath() + "/hookhtml");
                    writeToFile(htmlFile, value);

                    for (Element element : Jsoup.parse(value).getElementsByClass("\\\"common_div_typeIn_sub\\\"")) {
                        Log.e(TAG, "[Jsoup result] = className:" + element.className() + ", tagName:" + element.tagName() + ", element.text()=" + element.text());

                        String result[] = element.text().split(" ");

                        EciticTradeDetail detail = new EciticTradeDetail();
                        detail.setAmount(result[0]);
                        detail.setTransferTime(result[1]);
                        mList.add(detail);
                    }

                    getDetail(position);
                    position++;

                }
            });


//            XposedHelpers.callMethod(mHookWebView, "evaluateJavascript","(function(){ return document.getElementsByClassName('common_div_typeIn_sub')[2].click();})();", new ValueCallback<String>() {
//                @Override
//                public void onReceiveValue(String value) {
//                    value = value.replace("\\u003C", "<");
//                    value = value.replace("\\&quot;", "");
//
//                    int maxLogSize = 1000;
//                    for (int i = 0; i <= value.length() / maxLogSize; i++) {
//                        int start = i * maxLogSize;
//                        int end = (i + 1) * maxLogSize;
//                        end = end > value.length() ? value.length() : end;
//
//                        Log.e(TAG, "[onReceiveValue] html:" + value.substring(start, end));
//                    }
//
//                    //TODO 擷取list
//
//                    //TODO 擷取detail
//
//
//                }
//            });
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

//                        getHtmlString(param.args[0]);


                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent();
                                //創建一個Inten物件

                                intent.setAction("Ian");
                                intent.putExtra("date", "20190304");
                                //設定Actio的辨識字串

                                context.sendBroadcast(intent);
                            }
                        }, 5000L);

//                        try {
//                            new File(Environment.getExternalStorageDirectory().getPath()+"/test.json").createNewFile();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        } finally {
//                            Log.e(TAG,"[file] isExist:"+new File(Environment.getExternalStorageDirectory().getPath()+"/test.json").exists());
//                            Log.e(TAG,"[file] isExist:"+new File(Environment.getExternalStorageDirectory().getPath()+"/test.json").getAbsolutePath());
//                        }

//                        XposedHelpers.callMethod(XposedHelpers.callMethod(param.thisObject,"getContext"),"sendBroadcast",intent);
                    }
                });

//                XposedBridge.hookAllMethods(CBJSWebView, "loadData", new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//                        String num = "";
//                        for (int i = 0; i < param.args.length; i++) {
//                            if (param.args[i] == null) {
//                                num = "";
//                            } else {
//                                num = param.args[i].toString();
//                            }
//                            Log.i(TAG, "CBJSWebView loadData paraMeter[" + i + "] : " + num);
//                        }
//                    }
//                });

                final Class<?> ApplicationExtension = findClass("com.ecitic.bank.mobile.common.ApplicationExtension", classLoader);

                XposedHelpers.findAndHookMethod(ApplicationExtension, "onCreate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        Log.i(TAG, "[ApplicationExtension] onCreate~~~~~~~~~~~~~~~~~~~~~~~~~");

                        Application thisObject = (Application) param.thisObject;
                        IntentFilter intentFilter = new IntentFilter();

                        // 2. 设置接收广播的类型
                        intentFilter.addAction("Ian");
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

//                final Class<?> CBWebviewActivity = findClass("com.ecitic.bank.mobile.ui.CBWebviewActivity", classLoader);
//
//                XposedHelpers.findAndHookMethod(CBWebviewActivity, "onCreate", Bundle.class, new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//
//                        Log.i(TAG, "[CBWebviewActivity] onCreate Hook ");
//
////                        final Class<?> adapter = findClass("com.alipay.mobile.accountdetail.ui.DealListActivity$a", lpparam.classLoader);
////
////                        Log.e(TAG, "test:" + XposedHelpers.findField(adapter, "d").get("mListDatas").toString());
//
//                        //获取到当前hook的类,这里是CBWebviewActivity
//                        Class clazz = param.thisObject.getClass();
//                        Log.i(TAG, "class name:" + clazz.getName());
//
//                        // 通过反射获取控件，无论parivate或者public
//                        Field field = clazz.getDeclaredField("mWebView");
//                        // 设置访问权限
//                        field.setAccessible(true);
//
//                        Log.i(TAG,"[HookTest] = "+field.toString());
//
//                        Log.i(TAG,"[Hook find best match]"+XposedHelpers.findMethodBestMatch(CBWebView,"setWebViewClient").toString());
//
////                        WebViewClient webViewClient = new WebViewClient(){
////                            @Override
////                            public void onPageFinished(WebView view, String url) {
////                                super.onPageFinished(view, url);
////
////                                Log.i(TAG,"---------------------onPageFinished--------------------");
////                            }
////                        };
////
////                        Class<?> classs[] = {WebViewClient.class};
////
////                        XposedHelpers.callMethod(field,"setWebViewClient",classs,webViewClient);
////
////                        Object t = XposedHelpers.callMethod(field,"getWebViewClient");
////
////                        Log.i(TAG,"[Hook getWebViewClient] = "+t.toString());
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

    public void getDetail(int position) {
        position = position+2;
        XposedHelpers.callMethod(mHookWebView, "evaluateJavascript",
                "(function(){ " +
                        "document.getElementsByClassName('common_div_typeIn_sub')["+position+"].click();" +
                        "return document.getElementsByClassName('fw-page-current fw-page-content')[0].innerHTML;"+
                        "}()); ", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                value = value.replace("\\u003C", "<");
                value = value.replace("\\&quot;", "");
                value = value.replace("\\\"", "\"");
                value = value.replace(" \\n", "");

                int maxLogSize = 1000;
                for (int i = 0; i <= value.length() / maxLogSize; i++) {
                    int start = i * maxLogSize;
                    int end = (i + 1) * maxLogSize;
                    end = end > value.length() ? value.length() : end;

                    Log.e(TAG, "[onReceiveValue] html:" + value.substring(start, end));
                }

                Document document = Jsoup.parse(value);

//                String inOrOut = document.getElementsByAttribute("LOANFLAG").toString();
//                String transId = document.getElementsByAttribute("TRANNO").toString();
//
//                String amount,inAccount,inName,outAccount,outName;
//
//                if(inOrOut.equals(FLAG_IN)){
//                    outAccount = document.getElementsByAttribute("OTHERACCNO_Show").toString();
//                    outName = document.getElementsByAttribute("OTHERACCNAME").toString();
//                    inAccount = mUserAccount;
//                    inName = mUserName;
//                    amount = document.getElementsByAttribute("TRANAMT").toString();
//                }else{
//                    outAccount = mUserAccount;
//                    outName = mUserName;
//                    inAccount = document.getElementsByAttribute("OTHERACCNO_Show").toString();
//                    inName = document.getElementsByAttribute("OTHERACCNAME").toString();
//                    amount = "-"+document.getElementsByAttribute("TRANAMT").toString();
//                }
//
//                String remark = document.getElementsByAttribute("MEMO").toString();
//                String transferTime = document.getElementsByAttribute("TRANTIME").toString();
//                String balance = document.getElementsByAttribute("BALAMT").toString();
//
//                Log.e(TAG,"[inOrOut]:"+inOrOut+", [transId]:"+transId+", [amount]:"+amount+", [inAccount]:"+inAccount+", [inName]:"+inName+", [outAccount]:"+outAccount+", [outName]:"+outName+
//                        ", [remark]:"+remark+", [transferTime]:"+transferTime+", [balance]:"+balance);

                for(Element element:document.getAllElements()){
                    Log.e(TAG,"[getDetail] element.text :"+element.text()+", element.toString :"+element.toString());
                }





                //TODO 擷取list

                //TODO 擷取detail


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
