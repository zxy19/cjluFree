package cc.xypp.cjluFree;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

public class EditorActivity extends AppCompatActivity {
    private dataUtil data;
    String varName;
    String varType;
    boolean isImage;
    String varContent;
    String imType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor2);
        String desc;
        data=new dataUtil(getContentResolver());

        Intent i = getIntent();
        desc=varName=i.getStringExtra("varName");
        varType=i.getStringExtra("varType");
        try {
            varContent = data.get(varName);
        }catch (Exception e){
            varContent="";
        }
        if(varType.equals("image")){
            isImage=true;
        }else isImage=false;

        if(isImage){
            try {
                if (varContent.startsWith("data:image/")) {
                    imType = varContent.substring(11, varContent.indexOf(";"));
                    varContent = varContent.substring(varContent.indexOf("base64,") + 7);
                    byte[] imgDat = Base64.decode(varContent, Base64.DEFAULT);
                    Bitmap bm = BitmapFactory.decodeByteArray(imgDat, 0, imgDat.length);
                    ((ImageView) findViewById(R.id.image_view)).setImageBitmap(bm);
                    desc += "[图片-" + imType + "]";
                } else desc += "[图片-无]";
            }catch (Exception e){
                desc += "[图片-异常]";
                varContent="";
            }
            ((ImageView)findViewById(R.id.image_view)).setVisibility(View.VISIBLE);
            ((Button)findViewById(R.id.image_clear)).setVisibility(View.VISIBLE);
            ((Button)findViewById(R.id.image_load)).setVisibility(View.VISIBLE);
        }else{
            ((EditText)findViewById(R.id.text_content)).setText(varContent);
            ((EditText)findViewById(R.id.text_content)).setVisibility(View.VISIBLE);
        }
        ((TextView)findViewById(R.id.editor_title)).setText(desc);
    }
    public void openPic(View view){
        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(gallery, 2);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("[AS_LOG]32434","euthrsu");
        if (requestCode == 2) {
            // 从相册返回的数据
            if (data != null) {
                Uri uri = data.getData();
                try {
                    InputStream is = getContentResolver().openInputStream(Uri.parse(uri.toString()));
                    if(is.available()!=0) {
                        byte[] t = new byte[is.available()];
                        is.read(t);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        Bitmap bm = BitmapFactory.decodeByteArray(t,0,t.length,options);

                        ((ImageView)findViewById(R.id.image_view)).setImageBitmap(bm);
                        String res = Base64.encodeToString(t, Base64.DEFAULT);


                        if (!TextUtils.isEmpty(res)) {
                            varContent=res;
                            imType=options.outMimeType.substring(options.outMimeType.indexOf("/")+1);
                            Log.i("[AS_LOG]"+imType, Integer.toString(t.length));
                            ((TextView)findViewById(R.id.editor_title)).setText(varName+"[图片-"+imType+"]");
                        }
                    }
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void clearPic(View view){
        ((ImageView)findViewById(R.id.image_view)).setImageResource(R.drawable.ic_icon_back);
        varContent="";
        imType="";
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(isImage){
            if(!varContent.equals(""))
                varContent="data:image/"+imType+";base64,"+varContent;
        }
        else varContent=((EditText)findViewById(R.id.text_content)).getText().toString();
        data.set(varName,varContent);
    }
}