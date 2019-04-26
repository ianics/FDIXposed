package com.fdi.xposed.Helper;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitHelper {

    public static String API_DOMAIN_SPDB = "https://wap.spdb.com.cn/";
    public static String API_DOMAIN_PAB = "https://rmb.pingan.com.cn/";

    private static OkHttpClient mOkHttpClient;

    public static Retrofit getRetrofit(String domainUrl) {
        return new Retrofit.Builder()
                .baseUrl(domainUrl) //domain 路徑
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getOkHttpClient())
                .build();
    }

    public static OkHttpClient getOkHttpClient() {
        if (null == mOkHttpClient) {
            mOkHttpClient = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new NetWorkInterceptor())
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
        return mOkHttpClient;
    }
}
