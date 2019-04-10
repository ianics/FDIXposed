package com.fdi.xposed;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
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
}
