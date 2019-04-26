package com.fdi.xposed.Helper;

import android.util.Log;

import com.fdi.xposed.DataModel.PABTradeList;
import com.fdi.xposed.DataModel.PABTradeResult;
import com.fdi.xposed.DataModel.SPDBTradeItemDetail;
import com.fdi.xposed.DataModel.SPDBTradeList;

import io.reactivex.Flowable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class ApiSource {


    public interface ApiService {

        @POST("pmclient/svt/transservlet.shtml")
        Flowable<SPDBTradeList> getSPDBTradeList(
                @Header("Cookie") String cookies,
                @Body RequestBody requestBody);

        @POST("pmclient/svt/transservlet.shtml")
        Flowable<SPDBTradeItemDetail> getSPDBTradeDetail(
                @Header("Cookie") String cookies,
                @Body RequestBody requestBody);

        @Headers({"Content-Type: application/x-www-form-urlencoded; charset=utf-8"})
        @POST("brop/acct/cust/qry/qryTranList.do")
        Flowable<PABTradeResult> getPABTradeList(
                @Header("Cookie") String cookie,
                @Body RequestBody requestBody);


    }

    private ApiService apiService;
    private static ApiSource INSTANCE;

    public static ApiSource getInstance(String domainUrl) {
//        if (INSTANCE == null) {
//            INSTANCE = new ApiSource(domainUrl);
//        }
//        return INSTANCE;
        return new ApiSource(domainUrl);
    }

    public ApiSource(String domainUrl) {
        apiService = RetrofitHelper.getRetrofit(domainUrl).create(ApiService.class);
    }

    public Flowable<SPDBTradeList> getSPDBTradeList(String cookies, String requestBody) {
        requestBody = requestBody.replace("\"", "");
        Log.e("hook", "cookies:" + cookies + ", requestBody:" + requestBody);
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), requestBody);
        return apiService.getSPDBTradeList(cookies, body);
    }

    public Flowable<SPDBTradeItemDetail> getSPDBTradeDetail(String cookies, String requestBody) {
        requestBody = requestBody.replace("\"", "");
        Log.e("hook", "cookies:" + cookies + ", requestBody:" + requestBody);
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), requestBody);
        return apiService.getSPDBTradeDetail(cookies, body);
    }

    public Flowable<PABTradeResult> getPABTradeList(String cookie, String encodeAccount, String startDate, String endDate, int pageindex, int pageSize) {

        String cookies = "brcpSessionTicket=" + cookie;
        String requestBody = "accNum=" + encodeAccount + "&bankType=0&currType=RMB&endDate=" + endDate + "&startDate=" + startDate + "&pageIndex=" + pageindex + "&pageSize=" + pageSize;
        Log.e("hook", "cookies:" + cookies + ", requestBody:" + requestBody);
        RequestBody body = RequestBody.create(MediaType.parse("Content-Type: application/x-www-form-urlencoded; charset=utf-8"), requestBody);
        return apiService.getPABTradeList(cookies, body);
    }


}
