package com.fdi.xposed.hooks.common;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class CommonHook {
    private static final String TAG = CommonHook.class.getSimpleName();



    public static void initHooking(ClassLoader classLoader) throws NoSuchMethodException {

        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Application thisObject = (Application) param.thisObject;
                Log.e(TAG, "---------------------------------当前 Application : (onCreate)" + thisObject.getClass().getName());
                super.afterHookedMethod(param);


            }
        });

        XposedHelpers.findAndHookMethod(Application.class, "onTrimMemory",int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Application thisObject = (Application) param.thisObject;
                Log.e(TAG, "---------------------------------当前 Application : (onTrimMemory)" + thisObject.getClass().getName());
                super.afterHookedMethod(param);


            }
        });

        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity thisObject = (Activity) param.thisObject;
                Log.e(TAG, "---------------------------------当前 Activity : " + thisObject.getClass().getName());
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod(Fragment.class, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Fragment thisObject = (Fragment) param.thisObject;
                Log.e(TAG, "---------------------------------当前 Fragment : " + thisObject.getClass().getName());
                super.afterHookedMethod(param);
            }
        });

        XposedBridge.hookAllConstructors(WebView.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                WebView thisObject = (WebView) param.thisObject;
                XposedHelpers.callMethod(thisObject,"setWebContentsDebuggingEnabled",true);
                Log.e(TAG, "---------------------------------当前 WebView : " + thisObject.getClass().getName());
                super.afterHookedMethod(param);
            }
        });

        XposedBridge.hookAllConstructors(WebViewClient.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                WebViewClient thisObject = (WebViewClient) param.thisObject;
                Log.e(TAG, "---------------------------------当前 WebViewClient : " + thisObject.getClass().getName());
                super.afterHookedMethod(param);
            }
        });
    }
}
