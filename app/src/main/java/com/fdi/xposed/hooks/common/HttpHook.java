package com.fdi.xposed.hooks.common;

import android.os.Build;
import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


import static com.fdi.xposed.Hook.TAG;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class HttpHook {
    public static void initHooking(ClassLoader classLoader) throws NoSuchMethodException {


        final Class<?> httpUrlConnection = findClass("java.net.HttpURLConnection", classLoader);


        hookAllConstructors(httpUrlConnection, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args.length != 1 || param.args[0].getClass() != URL.class)
                    return;

                Log.i(TAG, "HttpURLConnection: " + param.args[0] + "");
            }
        });

        XC_MethodHook ResponseHook = new XC_MethodHook() {

            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                HttpURLConnection urlConn = (HttpURLConnection) param.thisObject;

                if (urlConn != null) {
                    StringBuilder sb = new StringBuilder();
                    int code = urlConn.getResponseCode();
                    if (code == 200) {

                        Map<String, List<String>> properties = urlConn.getHeaderFields();
                        if (properties != null && properties.size() > 0) {

                            for (Map.Entry<String, List<String>> entry : properties.entrySet()) {
                                sb.append(entry.getKey() + ": " + entry.getValue() + ", ");
                            }
                        }
                    }

                    Log.i(TAG, "RESPONSE: method=" + urlConn.getRequestMethod() + " " +
                            "URL=" + urlConn.getURL().toString() + " " +
                            "Params=" + sb.toString());
                }

            }
        };


        findAndHookMethod("java.io.OutputStream", classLoader, "write", byte[].class, int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                OutputStream os = (OutputStream) param.thisObject;
                if (!os.toString().contains("internal.http"))
                    return;
                String print = new String((byte[]) param.args[0]);
                Log.i(TAG, "DATA" + print.toString());
                Pattern pt = Pattern.compile("(\\w+=.*)");
                Matcher match = pt.matcher(print);
                if (match.matches()) {
                    Log.i(TAG, "POST DATA: " + print.toString());
                }
            }
        });


        findAndHookMethod("java.io.OutputStream", classLoader, "write", byte[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                OutputStream os = (OutputStream) param.thisObject;
                if (!os.toString().contains("internal.http"))
                    return;
                String print = new String((byte[]) param.args[0]);
                Log.i(TAG, "DATA: " + print.toString());
                Pattern pt = Pattern.compile("(\\w+=.*)");
                Matcher match = pt.matcher(print);
                if (match.matches()) {
                    Log.i(TAG, "POST DATA: " + print.toString());
                }
            }
        });

        try {
            final Class<?> okHttpClient = findClass("com.android.okhttp.OkHttpClient", classLoader);
            if (okHttpClient != null) {
                findAndHookMethod(okHttpClient, "open", URI.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        URI uri = null;
                        if (param.args[0] != null)
                            uri = (URI) param.args[0];
                        Log.i(TAG, "OkHttpClient: " + uri.toString() + "");
                    }
                });
            }
        } catch (Error e) {

        }


        try {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {

                findAndHookMethod("libcore.net.http.HttpURLConnectionImpl", classLoader, "getOutputStream", ResponseHook);
            } else {
                findAndHookMethod("com.android.okhttp.internal.http.HttpURLConnectionImpl", classLoader, "getOutputStream", ResponseHook);
                findAndHookMethod("com.android.okhttp.internal.http.HttpURLConnectionImpl", classLoader, "getInputStream", ResponseHook);
            }
        } catch (Error e) {
        }

    }
}
