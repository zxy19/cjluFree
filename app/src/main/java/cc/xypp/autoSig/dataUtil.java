package cc.xypp.autoSig;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class dataUtil {
    Uri uri;
    ContentResolver resolver;
    dataUtil(ContentResolver _resolver){
        uri = Uri.parse("content://cc.xypp.autoSig.dataProvider/data");
        resolver=_resolver;
    }

    public String get(String name){
        Cursor cursor2 = resolver.query(uri, new String[]{"value"}, "name=?", new String[]{name}, null);
        if (cursor2.moveToFirst()){
            return cursor2.getString(0);
        }
        return "";
    }
    public void set(String name,String val){
        ContentValues values2 = new ContentValues();
        values2.put("value", val);
        resolver.update(uri,values2,"name=?",new String[]{name});
    }
}
