package com.fdi.xposed.hooks.bank;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.fdi.xposed.DataModel.PABTradeItem;
import com.fdi.xposed.DataModel.PABTradeList;
import com.fdi.xposed.DataModel.PABTradeResult;
import com.fdi.xposed.DataModel.TradeDetail;
import com.fdi.xposed.Helper.ApiSource;
import com.fdi.xposed.Helper.DatabaseOpenHelper;
import com.fdi.xposed.Utils;
import com.fdi.xposed.hooks.BaseHook;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.fdi.xposed.Constant.DEFAULT_FEE;
import static com.fdi.xposed.Constant.DEFAULT_IS_UNION_PAY;
import static com.fdi.xposed.Constant.DEFAULT_TRANSFER_TYPE;
import static com.fdi.xposed.Constant.KEY_ACCOUNT;
import static com.fdi.xposed.Constant.KEY_END_DATE;
import static com.fdi.xposed.Constant.KEY_NOW_BALANCE;
import static com.fdi.xposed.Constant.KEY_PRE_TRAINS_ID;
import static com.fdi.xposed.Constant.KEY_QUERY_TYPE;
import static com.fdi.xposed.Constant.KEY_START_DATE;
import static com.fdi.xposed.Helper.RetrofitHelper.API_DOMAIN_PAB;
import static com.fdi.xposed.Utils.writeToFile;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class PABHook extends BaseHook {

    private static final String TAG = PABHook.class.getSimpleName();

    private WebView mHookWebView;

    private PABBroadcastReceiver mBroadcastReceiver = new PABBroadcastReceiver();

    private static final String BROADCAST_ACTION_KEY = "xposed_pab";

    private String mBeginDate = "";
    private String mEndDate = "";
    private String mQueryType = "";
    private String mNowBalance = "";
    private String mPreTransID = "";
    private String mAccount = "";

    //哈姆建議不要一次查詢太多，免得被發現
    private final int mPageSize = 10;
    private int mPageIndex = 1;

    private final String mEncodeAccountStart = "?bankCardSign=";
    private final String mEncodeAccountEnd = "&ccy=";
    private String mEncodeAccount = "";
    private String mCookie = "";

    private ArrayList<PABTradeItem> mTradeList = null;

    private boolean isBroadcastRegister = false;

    @Override
    public void initHooking(ClassLoader classLoader) {

        final Class<?> Application = findClass("s.h.e.l.l.S", classLoader);

        XposedHelpers.findAndHookMethod(Application, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                Log.i(TAG, "[Application] onCreate 註冊平安銀行Broadcast Action:" + BROADCAST_ACTION_KEY);

                Application thisObject = (Application) param.thisObject;
                IntentFilter intentFilter = new IntentFilter();

                if (!isBroadcastRegister) {
                    isBroadcastRegister = true;
                    intentFilter.addAction(BROADCAST_ACTION_KEY);
                    thisObject.registerReceiver(mBroadcastReceiver, intentFilter);
                }
            }
        });

        XposedHelpers.findAndHookMethod("s.h.e.l.l.S", classLoader, "attachBaseContext", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                final Context context = (Context) param.args[0];
                final ClassLoader classLoader = context.getClassLoader();

                final Class<?> BaseWebViewClient = findClass("com.pingan.aladdin.h5.webview.BaseWebViewClient", classLoader);

                XposedBridge.hookAllMethods(BaseWebViewClient, "onPageFinished", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        Log.e(TAG, "[onPageFinished] call. 當前 webViewClient : " + param.thisObject.getClass().getName() + ", webViewClient url : " + param.args[1]);

                        mHookWebView = (WebView) param.args[0];

                        //因為平安銀行在chrome上開啟怪怪的，所以在這邊補上一次確保chrome開啟成功
                        XposedHelpers.callMethod(mHookWebView, "setWebContentsDebuggingEnabled", true);

                        if (mHookWebView == null) {
                            Log.e(TAG, "[onPageFinished] mHookWebView == null");
                        } else {
                            Log.e(TAG, "[onPageFinished] mHookWebView != null");
                        }

//                        if (param.args[1].toString().contains("bankCardSign")) {
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Intent intent = new Intent();
//                                    //創建一個Inten物件
//
//                                    intent.setAction(BROADCAST_ACTION_KEY);
//                                    intent.putExtra(KEY_START_DATE, "20190201");
//                                    intent.putExtra(KEY_END_DATE, "20190424");
//                                    intent.putExtra(KEY_QUERY_TYPE, "一般查詢");
//                                    intent.putExtra(KEY_PRE_TRAINS_ID, "-");
//                                    intent.putExtra(KEY_ACCOUNT, "sample_account");
//                                    intent.putExtra(KEY_NOW_BALANCE, "1000");
//
//                                    //設定Actio的辨識字串
//
//                                    context.sendBroadcast(intent);
//                                }
//                            }, 5000L);
//                        }

                    }
                });

            }
        });


    }

    public class PABBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            //TODO 取得cookie,encode account,完成上述動作後call API

            Log.e(TAG, "[PABBroadcastReceiver] 收到 BroadcastReceiver 通知");

            if (mHookWebView == null) {
                Log.e(TAG, "[PABBroadcastReceiver] WebView hook失敗，此次查詢無法繼續，故跳出查詢");
                return;
            }

            mBeginDate = intent.getStringExtra(KEY_START_DATE);
            mEndDate = intent.getStringExtra(KEY_END_DATE);
            mQueryType = intent.getStringExtra(KEY_QUERY_TYPE);
            mPreTransID = intent.getStringExtra(KEY_PRE_TRAINS_ID);
            mAccount = intent.getStringExtra(KEY_ACCOUNT);
            mNowBalance = intent.getStringExtra(KEY_NOW_BALANCE);

            if (mBeginDate == null) mBeginDate = "20190301";
            if (mEndDate == null) mEndDate = "20190424";
            if (mQueryType == null) mQueryType = "";
            if (mPreTransID == null) mPreTransID = "-";
            if (mAccount == null) mAccount = "";
            if (mNowBalance == null) mNowBalance = "0";

            Log.e(TAG, "[PABBroadcastReceiver] 此次 BroadcastReceiver 查詢參數 [BeginDate]:" + mBeginDate +
                    ", [EndDate]:" + mEndDate + ", [QueryType]:" + mQueryType + ", [PreTransID]:" + mPreTransID + ", [Account]:" + mAccount + ", [mNowBalance]:" + mNowBalance);

            mPageIndex = 1;
            mTradeList = new ArrayList<>();

            if (getCookie()) {
                getEncodeAccount();
            }

        }
    }

    private boolean getCookie() {
        Log.e(TAG, "[getCookie] 經由database取得Cookie中");

        mCookie = "";

        DatabaseOpenHelper databaseOpenHelper = new DatabaseOpenHelper(mHookWebView.getContext(), "Cookies", "/data/data/com.pingan.paces.ccms/app_webview/");

        boolean isDBExists = databaseOpenHelper.exists();

        Log.e(TAG, "[getCookie] database是否存在 = " + isDBExists);

        if (isDBExists) {
            try {
                databaseOpenHelper.openDatabase(SQLiteDatabase.OPEN_READWRITE);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Cursor cursor = databaseOpenHelper.db.query(
                    "cookies", new String[]{"name,value"},
                    "name like ?", new String[]{"brcpSessionTicket"},
                    null,
                    null,
                    null,
                    null);

            Log.e(TAG, "[getCookie] database 查詢結果數量 = " + cursor.getCount());

            while (cursor.moveToNext()) {
                Log.e(TAG, "[getCookie] database 查詢結果 = " + cursor.getString(0) + " : " + cursor.getString(1));
                mCookie = cursor.getString(1);
            }

            cursor.close();
            databaseOpenHelper.close();
        }

        if (TextUtils.isEmpty(mCookie)) {
            Log.e(TAG, "[getCookie] 完成查詢，查詢失敗");
            return false;
        } else {
            Log.e(TAG, "[getCookie] 完成查詢，查詢成功");
            return true;
        }
    }

    private void getEncodeAccount() {
        Log.e(TAG, "[getEncodeAccount] 取得加密帳號中");

        XposedHelpers.callMethod(mHookWebView,
                "evaluateJavascript",
                "(function(){ return window.WTjson})();",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        mEncodeAccount = value.substring(value.indexOf(mEncodeAccountStart) + mEncodeAccountStart.length(), value.indexOf(mEncodeAccountEnd));
                        Log.e(TAG, "[getEncodeAccount] 取得加密帳號 = " + mEncodeAccount);

                        getTradeList(mCookie, mEncodeAccount, mBeginDate, mEndDate, 1);
                    }
                });
    }

    private void getTradeList(final String cookie, final String encodeAccount, final String startDate, final String endDate, final int pageIndex) {
        Log.e(TAG, "[getTradeList] 取得列表資料中 pageIndex : " + pageIndex);
        Disposable disposable = ApiSource.getInstance(API_DOMAIN_PAB).getPABTradeList(cookie, encodeAccount, startDate, endDate, pageIndex, mPageSize)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PABTradeResult>() {
                    @Override
                    public void accept(PABTradeResult pabTradeResult) throws Exception {
                        Log.e(TAG, "[getTradeList] 收到列表資料 pageIndex : " + pageIndex);
                        if (pabTradeResult == null) {
                            Log.e(TAG, "pabTradeList == null");
                            return;
                        }
                        if (pabTradeResult.data.result_value == null) {
                            Log.e(TAG, "pabTradeList.result_value == null");
                            return;
                        }
                        if (pabTradeResult.data.result_value.size() == 0) {
                            Log.e(TAG, "[getTradeList] pabTradeList.result_value.size() == 0");
                            //TODO 拉完資料了~~~~~~~

                            for (int i = 0; i < mTradeList.size(); i++) {
                                Log.e(TAG, "[getTradeList] = " + mTradeList.get(i).toString());
                            }

                            toTradeDetailList();

                        } else {
                            Log.e(TAG, "[getTradeList] pabTradeList.result_value.size() != 0");
                            //TODO 還沒拉完，準備拉下一頁
                            mTradeList.addAll(pabTradeResult.data.result_value);
                            getTradeList(cookie, encodeAccount, startDate, endDate, pageIndex + 1);
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "[getTradeList] Throwable : " + throwable.toString());
                    }
                });
    }

    private void toTradeDetailList() {
        Log.e(TAG, "[toTradeDetailList] 轉換列表資料為後台所需格式中");

        ArrayList<TradeDetail> tradeDetailList = new ArrayList<>();
        TradeDetail temp;
        PABTradeItem pabTradeItem;

        for (int i = 0; i < mTradeList.size(); i++) {
            pabTradeItem = mTradeList.get(i);

            temp = new TradeDetail();

            //交易ID
            temp.setTransId(pabTradeItem.getTranSysNo());
            //交易金額
            temp.setAmount(((pabTradeItem.getToAcctFlag().equals("02") ? "-" : "") + pabTradeItem.getTranAmt()));
            //交易時間
            temp.setTransferTime(pabTradeItem.getTranTime());
            //該筆交易餘額
            temp.setBalance(pabTradeItem.getAvailBalance().replace(",", ""));

            //如果是二維碼支付會沒有對方銀行帳號，導致寫Transaction出錯，所以放toClientName
            temp.setOutAccount(pabTradeItem.getToAcctFlag().equals("02") ?
                    TextUtils.isEmpty(pabTradeItem.getToAcctNo()) ? pabTradeItem.getToClientName() : pabTradeItem.getToAcctNo() : pabTradeItem.getFromAcctNo());
            temp.setOutName(pabTradeItem.getToAcctFlag().equals("02") ? pabTradeItem.getToClientName() : pabTradeItem.getFromClientName());

            temp.setRemark(Utils.filterNumber(pabTradeItem.getUserRemark()));

            if (mTradeList.size() - 1 >= i + 1) {
                temp.setPreTransID(mTradeList.get(i + 1).getTranSysNo());
            } else {
                temp.setPreTransID(mPreTransID);
            }

            //要給現在帳號餘額
            temp.setNowBalance(mNowBalance);

            temp.setIsUnionPay(((pabTradeItem.getUserRemark().equals("银联扫码转账") ||
                    pabTradeItem.getUserRemark().contains("银联扫码")) ||
                    (pabTradeItem.getToClientName().equals("银联扫码转账") ||
                            pabTradeItem.getToClientName().contains("银联扫码") ||
                            (pabTradeItem.getUserRemark().contains("应退客户款") && pabTradeItem.getRemark().equals("银联调账")))) ? "1" : "0");

            if (pabTradeItem.getRemark().contains("付利息")) {

                temp.setAmount(pabTradeItem.getTranAmt());
                temp.setOutAccount(pabTradeItem.getRemark());
                temp.setOutName(pabTradeItem.getRemark());

            } else if (pabTradeItem.getRemark().contains("短信通费") || pabTradeItem.getRemark().contains("挂失手续") || pabTradeItem.getRemark().contains("卡工本费") || pabTradeItem.getRemark().contains("取款")) {

                temp.setOutAccount(pabTradeItem.getRemark());
                temp.setOutName(pabTradeItem.getRemark());
                //避免toAcctFlag判斷錯誤,這些特殊交易皆為出款
                temp.setAmount("-" + pabTradeItem.getTranAmt());

            } else if (pabTradeItem.getRemark().contains("银联调账")) {

                temp.setOutAccount(pabTradeItem.getRemark());
                temp.setOutName(pabTradeItem.getRemark());
            }

            if(!pabTradeItem.getToAcctFlag().equals("02")){
                temp.setInAccount(mAccount);
                temp.setInName(pabTradeItem.getToClientName());
            }else{
                temp.setInAccount(temp.getOutAccount());
                temp.setInName(temp.getOutName());
                temp.setOutAccount(mAccount);
                temp.setOutName(pabTradeItem.getFromClientName());
            }

            temp.setFee(DEFAULT_FEE);
            temp.setTransferType(pabTradeItem.getRemark());
            temp.setQueryType(mQueryType);

            tradeDetailList.add(temp);
        }

        File detailList = new File(Environment.getExternalStorageDirectory().getPath() + "/recordDetail_PAB.json");
        writeToFile(detailList, new Gson().toJson(tradeDetailList));
    }
}
