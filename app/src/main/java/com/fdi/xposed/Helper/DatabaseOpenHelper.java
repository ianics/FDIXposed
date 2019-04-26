package com.fdi.xposed.Helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private Context        mContext;
    private String         mDbPath;
    private String         mDbName;
    private int            mDbVersion;

    public  SQLiteDatabase db;

    private static final String TAG = DatabaseOpenHelper.class.getSimpleName();

    public DatabaseOpenHelper(Context context, String dbName, String dbPath) {
        super(context, dbName, null, 1);
        mContext   = context;
        mDbPath    = dbPath;
        mDbName    = dbName;
    }

    public boolean exists() {
        SQLiteDatabase db = null;

        try {
            db = SQLiteDatabase.openDatabase(mDbPath + mDbName, null, SQLiteDatabase.OPEN_READONLY);
        }
        catch (SQLiteException e) {
            //database does not exist yet.

            Log.e(TAG,"[exists] e = "+ e.toString());
        }

        if (db != null) {
            db.close();
            return true;
        } else {
            return false;
        }
    }

    public void openDatabase(int flag) throws SQLiteException, IOException {
//        if (!exists()) {
//            if (flag == SQLiteDatabase.OPEN_READONLY) {
//                this.getReadableDatabase();
//            } else if (flag == SQLiteDatabase.OPEN_READWRITE) {
//                this.getWritableDatabase();
//            }
//            InputStream iStream = null;
//            OutputStream oStream = null;
//            try {
//                iStream = mContext.getAssets().open(mDbName);
//                oStream = new FileOutputStream(mDbPath + mDbName);
//                byte[]       buffer  = new byte[1024];
//                int          length;
//
//                while ((length = iStream.read(buffer)) > 0) {
//                    oStream.write(buffer, 0, length);
//                }
//            } catch (IOException e) {
//                throw e;
//            } finally {
//                if (iStream != null) {
//                    iStream.close();
//                }
//
//                if (oStream != null) {
//                    oStream.flush();
//                    oStream.close();
//                }
//            }
//        }

        try {
            if (flag == SQLiteDatabase.OPEN_READONLY) {
                db = SQLiteDatabase.openDatabase(mDbPath + mDbName, null,
                        SQLiteDatabase.OPEN_READONLY);
            } else if (flag == SQLiteDatabase.OPEN_READWRITE) {
                db = SQLiteDatabase.openDatabase(mDbPath + mDbName, null,
                        SQLiteDatabase.OPEN_READWRITE);
            }
        } catch (SQLiteException e) {
            throw e;
        }
    }

    @Override
    public synchronized void close() {
        if (db != null) {
            db.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
