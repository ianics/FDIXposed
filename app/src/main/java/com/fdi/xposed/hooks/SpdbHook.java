package com.fdi.xposed.hooks;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.ValueCallback;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findClass;

public class SpdbHook {
    private static final String TAG = SpdbHook.class.getSimpleName();

    public static void initHooking(ClassLoader classLoader) throws NoSuchMethodException {
        XposedHelpers.findAndHookMethod("com.secneo.apkwrapper.ApplicationWrapper", classLoader, "attachBaseContext", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                Context context = (Context) param.args[0];
                final ClassLoader classLoader = context.getClassLoader();

                final Class<?> WebViewClient = findClass("android.webkit.WebViewClient", classLoader);

                XposedBridge.hookAllMethods(WebViewClient,"onPageFinished",new XC_MethodHook(){
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Log.i(TAG,"当前 n : " + param.thisObject.getClass().getName());
                        Log.i(TAG, "n url:" + param.args[1]);
                        getHtmlString(param.args[0]);

                        Intent intent = new Intent();

                    }
                });
            }
        });
    }

    private static void getHtmlString(final Object webView) {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
        XposedHelpers.callMethod(webView, "evaluateJavascript", "(function(){ return window.document.getElementsByTagName('html')[0].innerHTML})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(java.lang.String value) {
//                            Log.e(TAG, "[onReceiveValue] html:" + value);

//                if (value.contains("加载中")) {
//                    Log.e(TAG, "[onReceiveValue] html: 加载中");
//                    getHtmlString(webView);
//                } else {

                    value = value.replace("\\u003C", "<");
                    value = value.replace("\\&quot;","");

                    int maxLogSize = 1000;
                    for (int i = 0; i <= value.length() / maxLogSize; i++) {
                        int start = i * maxLogSize;
                        int end = (i + 1) * maxLogSize;
                        end = end > value.length() ? value.length() : end;

                        Log.e(TAG, "[onReceiveValue] html:" + value.substring(start, end));
                    }

//                    AndroidAppHelper.currentApplication().getSharedPreferences("Demo", Context.MODE_PRIVATE).edit().putString("Remark",value).apply();
//                }


            }
        });
//            }
//        },5000);
    }
}
