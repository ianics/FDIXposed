package com.fdi.xposed;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fdi.xposed.hooks.AllClassHook;
import com.fdi.xposed.hooks.CommonHook;
import com.fdi.xposed.hooks.EciticHook;
import com.fdi.xposed.hooks.HttpHook;
import com.fdi.xposed.hooks.SpdbHook;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dalvik.system.DexFile;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class Hook implements IXposedHookLoadPackage {

    public static final String TAG = Hook.class.getSimpleName();
    private static final String PACKAGENAME_ALIPAY = "com.eg.android.AlipayGphone";
    private static final String PACKAGENAME_SPDB = "cn.com.spdb.mobilebank.per";
    private static final String PACKAGENAME_ECITIC = "com.ecitic.bank.mobile";

    private EciticHook mEciticHook = new EciticHook();


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        CommonHook.initHooking(lpparam.classLoader);
//        AllClassHook.initHooking(lpparam.classLoader);

        switch (lpparam.packageName) {
            case PACKAGENAME_ECITIC:
                mEciticHook.initHooking(lpparam.classLoader);
                break;
            default:
                break;
        }

    }

    private void HookAlipay(final XC_LoadPackage.LoadPackageParam lpparam) {

        final Class<?> loginParam = findClass("com.ali.user.mobile.login.LoginParam", lpparam.classLoader);
        final Class<?> dealListActivity = findClass("com.alipay.mobile.accountdetail.ui.DealListActivity", lpparam.classLoader);
        final Class<?> arrayList = findClass("java.util.ArrayList", lpparam.classLoader);
        final Class<?> transListVO = findClass("com.alipay.mobilebill.biz.shared.acctrans.model.TransListVO", lpparam.classLoader);
        final Class<?> BillMainListActivity = findClass("com.alipay.mobile.bill.list.ui.BillMainListActivity", lpparam.classLoader);
        final Class<?> FragmentManager = findClass("android.support.v4.app.FragmentManager", lpparam.classLoader);
        final Class<?> List = findClass("java.util.List", lpparam.classLoader);
        final Class<?> BillCacheManager = findClass("com.alipay.mobile.bill.list.cache.BillCacheManager", lpparam.classLoader);
        final Class<?> Context = findClass("android.content.Context", lpparam.classLoader);
        final Class<?> Map = findClass("java.util.Map", lpparam.classLoader);
        final Class<?> LayoutInflater = findClass("android.view.LayoutInflater", lpparam.classLoader);
        final Class<?> ViewGroup = findClass("android.view.ViewGroup", lpparam.classLoader);
        final Class<?> Bundle = findClass("android.os.Bundle", lpparam.classLoader);
        final Class<?> H5WebView = findClass("com.alipay.mobile.nebulacore.web.H5WebView", lpparam.classLoader);
        final Class<?> APWebView = findClass("com.alipay.mobile.nebula.webview.APWebView", lpparam.classLoader);
        final Class<?> WebView = findClass("android.webkit.WebView", lpparam.classLoader);
        final Class<?> String = findClass("java.lang.String", lpparam.classLoader);
        final Class<?> APWebViewClient = findClass("com.alipay.mobile.nebula.webview.APWebViewClient", lpparam.classLoader);
        final Class<?> H5WebViewClient = findClass("com.alipay.mobile.nebulacore.web.H5WebViewClient", lpparam.classLoader);
        final Class<?> H5Fragment = findClass("com.alipay.mobile.nebulacore.ui.H5Fragment", lpparam.classLoader);
        final Class<?> H5WebChromeClient = findClass("com.alipay.mobile.nebulacore.web.H5WebChromeClient", lpparam.classLoader);

        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity thisObject = (Activity) param.thisObject;
                Log.e(TAG, "当前 Activity : " + thisObject.getClass().getName());
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod(Fragment.class, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity thisObject = (Activity) param.thisObject;
                Log.e(TAG, "当前 Fragment : " + thisObject.getClass().getName());
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookMethod("com.alipay.mobile.accountdetail.ui.DealListActivity$3", lpparam.classLoader, "getListData", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                ArrayList result = (ArrayList) param.getResult();
                if (result == null) return;

                for (Object object : result) {

                    String memo;

                    try {
                        memo = XposedHelpers.getObjectField(object, "transMemo").toString();
                    } catch (Exception e) {
                        memo = "null";
                    }

                    String num = "balance : " + XposedHelpers.getObjectField(object, "balance").toString() +
                            ", date : " + XposedHelpers.getObjectField(object, "date").toString() +
                            ", money : " + XposedHelpers.getObjectField(object, "money").toString() +
                            ", simpleDate : " + XposedHelpers.getObjectField(object, "simpleDate").toString() +
                            ", transInstitution : " + XposedHelpers.getObjectField(object, "transInstitution").toString() +
                            ", transLogId : " + XposedHelpers.getObjectField(object, "transLogId").toString() +
                            ", transMemo : " + memo +
                            ", transType : " + XposedHelpers.getObjectField(object, "transType").toString();

                    Log.e(TAG, "DealListActivity getListData() result:" + num);
                }

                Gson gson = new Gson();
                String jsonArray = gson.toJson((result));

                final Class<?> adapter = findClass("com.alipay.mobile.accountdetail.ui.DealListActivity$a", lpparam.classLoader);

                Log.e(TAG, "test:" + XposedHelpers.findField(adapter, "d").get("mListDatas").toString());


            }
        });

        XposedBridge.hookAllMethods(H5WebViewClient, "onPageFinished", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.e(TAG, "H5WebViewClient all onPageFinished() before call. url:" + param.args[1]);
            }

            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.e(TAG, "H5WebViewClient all onPageFinished() after call.");
                getHtmlString(param.args[0]);
            }
        });

    }

    private void getHtmlString(final Object webView) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
            }
        }, 5000);
    }

    // 获取指定名称的类声明的类成员变量、类方法、内部类的信息
    public void dumpClass(Class<?> actions) {

        XposedBridge.log("Dump class " + actions.getName());
        XposedBridge.log("Methods");

        // 获取到指定名称类声明的所有方法的信息
        Method[] m = actions.getDeclaredMethods();
        // 打印获取到的所有的类方法的信息
        for (int i = 0; i < m.length; i++) {

            XposedBridge.log(m[i].toString());
        }

        XposedBridge.log("Fields");
        // 获取到指定名称类声明的所有变量的信息
        Field[] f = actions.getDeclaredFields();
        // 打印获取到的所有变量的信息
        for (int j = 0; j < f.length; j++) {

            XposedBridge.log(f[j].toString());
        }

        XposedBridge.log("Classes");
        // 获取到指定名称类中声明的所有内部类的信息
        Class<?>[] c = actions.getDeclaredClasses();
        // 打印获取到的所有内部类的信息
        for (int k = 0; k < c.length; k++) {

            XposedBridge.log(c[k].toString());
        }
    }
}
