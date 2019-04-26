package com.fdi.xposed;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XposedBridge;

public class Utils {

    private static final String TAG = "Hook_Utils";

    /**
     * 將String做正規表示式
     *
     * @param args 待正規化的字串
     * @return
     */
    public static String filterNumber(String args) {
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(args);
        return m.replaceAll("").trim();
    }

    public static void printClassMethods(Class<?> className){
        Method[] methods = className.getDeclaredMethods();

        for (int i = 0; i < methods.length; i++) {
            Log.e(TAG, "["+className.getName()+"] methods : " + methods[i].toString());
        }
    }

    public static void printClassFields(Class<?> className){
        Field[] fields = className.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Log.e(TAG, "["+className.getName()+"] FIELD : " + fields[i].toString());
        }
    }

    public static void printClassAllrelate(Class<?> className){
        Log.e(TAG,"Dump class " + className.getName());
        Log.e(TAG,"Methods");

        // 获取到指定名称类声明的所有方法的信息
        Method[] m = className.getDeclaredMethods();
        // 打印获取到的所有的类方法的信息
        for (int i = 0; i < m.length; i++) {

            Log.e(TAG,m[i].toString());
        }

        Log.e(TAG,"Fields");
        // 获取到指定名称类声明的所有变量的信息
        Field[] f = className.getDeclaredFields();
        // 打印获取到的所有变量的信息
        for (int j = 0; j < f.length; j++) {

            Log.e(TAG,f[j].toString());
        }

        Log.e(TAG,"Classes");
        // 获取到指定名称类中声明的所有内部类的信息
        Class<?>[] c = className.getDeclaredClasses();
        // 打印获取到的所有内部类的信息
        for (int k = 0; k < c.length; k++) {

            Log.e(TAG,c[k].toString());
        }
    }

    public static void writeToFile(File fout, String data) {
        Log.e(TAG, "[writeToFile] call.");
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
