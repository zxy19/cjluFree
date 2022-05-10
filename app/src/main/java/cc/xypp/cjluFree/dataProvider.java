package cc.xypp.cjluFree;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class dataProvider extends ContentProvider {

    private Context mContext;
    DBHelper mDbHelper = null;
    SQLiteDatabase db = null;

    public static final String AUTOHORITY = "cc.xypp.cjluFree.dataProvider";
    // 设置ContentProvider的唯一标识

    public static final int User_Code = 1;
    public static final int Job_Code = 2;

    // UriMatcher类使用:在ContentProvider 中注册URI
    private static final UriMatcher mMatcher;
    static{
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 初始化
        mMatcher.addURI(AUTOHORITY,"data", User_Code);
    }

    // 以下是ContentProvider的6个方法

    /**
     * 初始化ContentProvider
     */
    @Override
    public boolean onCreate() {

        mContext = getContext();
        // 在ContentProvider创建时对数据库进行初始化
        // 运行在主线程，故不能做耗时操作,此处仅作展示
        mDbHelper = new DBHelper(getContext());
        db = mDbHelper.getWritableDatabase();
        initVal("auto","false");
        initVal("inj","false");
        initVal("once_inj","false");
        initVal("quick","");
        return true;
    }
    public void initVal(String name,String val) {
        try {
            ContentValues ctVal = new ContentValues();
            ctVal.put("name", name);
            ctVal.put("value", val);
            db.insertWithOnConflict("datas", null, ctVal, SQLiteDatabase.CONFLICT_ABORT);
        }catch (Exception ignore){};
    }
    public void updateVal(String name,String val) {
        ContentValues ctVal = new ContentValues();
        ctVal.put("name", name);
        ctVal.put("value", val);

        db.insertWithOnConflict("datas", "name,val", ctVal, SQLiteDatabase.CONFLICT_REPLACE);
    }
    public String getVal(String name) {
        Cursor ret = db.query("datas", new String[]{"value"}, "name", new String[]{name}, "", "", "");
        ret.moveToFirst();
        return ret.getString(0);
    }
    /**
     * 添加数据
     */
    private int notifyee(Uri uri) {
        mContext.getContentResolver().notifyChange(uri, null);
        return 0x7fffffff;
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db.insert("datas", null, values);
        notifyee(uri);
        return uri;
    }

    /**
     * 查询数据
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return db.query("datas",projection,selection,selectionArgs,null,null,sortOrder,null);
    }

    /**
     * 更新数据
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return db.update("datas",values,selection,selectionArgs)&notifyee(uri);
    }

    /**
     * 删除数据
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return db.delete("datas",selection,selectionArgs)&notifyee(uri);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    private class DBHelper extends SQLiteOpenHelper {

        // 数据库名
        private static final String DATABASE_NAME = "xypp_xposed.db";


        private static final int DATABASE_VERSION = 1;
        //数据库版本号

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // 创建两个表格:用户表 和职业表
            db.execSQL("CREATE TABLE IF NOT EXISTS datas (name TEXT PRIMARY KEY,value TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)   {

        }
    }
}
