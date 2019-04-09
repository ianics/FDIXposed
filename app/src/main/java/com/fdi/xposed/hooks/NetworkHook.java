package com.fdi.xposed.hooks;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class NetworkHook {
    private static final String TAG = NetworkHook.class.getSimpleName();

    public static void initHooking(ClassLoader classLoader) throws NoSuchMethodException {

        findAndHookConstructor(InetSocketAddress.class, String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                Log.e(TAG, "[网络地址] = "+param.args[0]+":"+param.args[1]);
                super.beforeHookedMethod(param);
            }
        });
        findAndHookMethod("android.webkit.WebView", classLoader, "loadUrl", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                String d = (String)param.args[0];
                Log.e(TAG,"[webview] loadUrl = "+d);
                super.beforeHookedMethod(param);
            }
        });
        findAndHookMethod("android.webkit.WebView", classLoader, "loadUrl", String.class, Map.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                String d = (String)param.args[0];
                Log.e(TAG,"[webview] loadUrl ="+d);


                super.beforeHookedMethod(param);
            }
        });
        findAndHookMethod("android.webkit.WebView", classLoader, "postUrl", String.class, byte[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                String d = (String)param.args[0];
                Log.e(TAG,"[webview] postUrl ="+d);

                super.beforeHookedMethod(param);
            }
        });

    }
}
