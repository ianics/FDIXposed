package com.fdi.xposed.hooks;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class AllClassHook {
    private static final String TAG = AllClassHook.class.getSimpleName();
    static String strClassName = "";

    public static void initHooking(ClassLoader classLoader) throws NoSuchMethodException {
        XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass", String.class, new XC_MethodHook() {

            // 在类方法loadClass执行之后执行的代码
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                // 参数的检查
                if (param.hasThrowable()) {
                    return;
                }

                // 获取指定名称的类加载之后的Class<?>
                Class<?> clazz = (Class<?>) param.getResult();
                // 获取加载的指定类的名称
                String strClazz = clazz.getName();
                Log.v(TAG, "LoadClass : " + strClazz);

//                  // 被Hook操作的目标类名称
//                  String strClazzName = "";
//                  // 被Hook操作的类方法的名称
//                  String strMethodName = "";

                // 所有的类都是通过loadClass方法加载的
                // 过滤掉Android系统的类以及一些常见的java类库
                if (!strClazz.startsWith("android.")
                        && !strClazz.startsWith(".system")
                        && !strClazz.startsWith("java.")
                        && !strClazz.startsWith("org.")
                        && !strClazz.contains("umeng.")
                        && !strClazz.contains("com.google")
                        && !strClazz.contains(".alipay")
                        && !strClazz.contains(".netease")
                        && !strClazz.contains(".alibaba")
                        && !strClazz.contains(".pgyersdk")
                        && !strClazz.contains(".daohen")
                        && !strClazz.contains(".bugly")
                        && !strClazz.contains("mini")
                        && !strClazz.contains("xposed")) {
                    // 或者只Hook加密算法类、网络数据传输类、按钮事件类等协议分析的重要类

                    // 同步处理一下
                    synchronized (this.getClass()) {

                        // 获取被Hook的目标类的名称
                        strClassName = strClazz;
                        //XposedBridge.log("HookedClass : "+strClazz);
                        // 获取到指定名称类声明的所有方法的信息
                        Method[] m = clazz.getDeclaredMethods();
                        // 打印获取到的所有的类方法的信息
                        for (int i = 0; i < m.length; i++) {

                            Log.v(TAG, "CLASS: " + strClassName + ", METHOD: " + m[i]);

                            //XposedBridge.log("HOOKED CLASS-METHOD: "+strClazz+"-"+m[i].toString());
//                            if (!Modifier.isAbstract(m[i].getModifiers())           // 过滤掉指定名称类中声明的抽象方法
//                                    && !Modifier.isNative(m[i].getModifiers())     // 过滤掉指定名称类中声明的Native方法
//                                    && !Modifier.isInterface(m[i].getModifiers())  // 过滤掉指定名称类中声明的接口方法
//                            ) {

                            // 对指定名称类中声明的非抽象方法进行java Hook处理

                            if (strClassName.contains("CBWeb") || strClassName.contains("CBWebviewActivity")) {
                                XposedBridge.hookMethod(m[i], new XC_MethodHook() {

                                    // 被java Hook的类方法执行完毕之后，打印log日志
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                                        // 打印被java Hook的类方法的名称和参数类型等信息
                                        Log.e(TAG, "HOOKED METHOD: " + strClassName + "-" + param.method.toString());

                                        if (param.args == null || param.args.length == 0) {
                                            return;
                                        }

                                        for (int i = 0; i < param.args.length; i++) {

                                            String content = "";
                                            if (param.args[i] != null) {
                                                content = param.args[i].toString();
                                            }

                                            Log.e(TAG, "Parameter[" + i + "] = " + content);
                                        }
                                    }
                                });
                            } else {
                                Log.v(TAG, "CLASS: " + strClassName + ", METHOD: " + m[i]);
                            }
//                            }
                        }

                        Field[] f = clazz.getDeclaredFields();
                        // 打印获取到的所有变量的信息
                        for (int j = 0; j < f.length; j++) {

                            XposedBridge.log(f[j].toString());
                            Log.e(TAG, "FIELD : " + f[j].toString());
                        }
                    }


                }
            }
        });
    }
}
