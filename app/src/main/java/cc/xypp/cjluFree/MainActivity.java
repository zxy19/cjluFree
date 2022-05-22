package cc.xypp.cjluFree;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import de.robv.android.xposed.XSharedPreferences;
import cc.xypp.cjluFree.R;
import rikka.shizuku.*;

public class MainActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener{
    dataUtil data;
    @SuppressLint("BlockedPrivateApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data=new dataUtil(getContentResolver());
        if(!data.get("agree").equals("true")){
            startActivity(new Intent(this,AgreementActivity.class));
            finish();
            return;
        }
        if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(0);
            }
        }
        Intent intent = getIntent();
        String quick = intent.getAction();
        if(quick!=null){
            Log.i("[AS_LOG][APP]INTENT", quick);
            if(quick.equals("cc.xypp.cjluFree.sig")){
                data.set("quick","sig");
                if(intent.getBooleanExtra("wifi",true) && data.get("auto_wifi").equals("true")){
                    startAfterWifi("cc.xypp.cjluFree.sig");
                    finish();
                    return;
                }
            }else if(quick.equals("cc.xypp.cjluFree.siga")){
                data.set("quick","sig");
                data.set("once_inj","true");
                if(intent.getBooleanExtra("wifi",true) && data.get("auto_wifi").equals("true")){
                    startAfterWifi("cc.xypp.cjluFree.siga");
                    finish();
                    return;
                }
            }else if(quick.equals("cc.xypp.cjluFree.pass")){
                data.set("quick","pass");
                if(intent.getBooleanExtra("wifi",true) && data.get("auto_wifi").equals("true")){
                    startAfterWifi("cc.xypp.cjluFree.pass");
                    finish();
                    return;
                }
            }else quick=null;

            if(quick!=null){
                if(intent.getBooleanExtra("autoStartWifi",false)){
                    data.set("is_auto_wifi","true");
                }
                startWeWork();
                finish();
            }
        }
        setContentView(R.layout.activity_main);

        AssetManager accMgr = getResources().getAssets();
        try {
            if(data.get("pass_src").equals("") || data.get("auto_update").equals("true"))
            try {
                InputStream is = accMgr.open("pass.js");
                if(is.available()!=0) {
                    byte[] t = new byte[is.available()];
                    is.read(t);
                    String res = new String(t);
                    if (!TextUtils.isEmpty(res)) {
                        data.set("pass_src", res);
                    }
                }
                is.close();
            } catch (Exception e) {
                XVdLog("merr", e.toString());
            }
            if(data.get("sig_src").equals("") || data.get("auto_update").equals("true"))
            try {
                InputStream is = accMgr.open("sig.js");
                if(is.available()!=0) {
                    byte[] t = new byte[is.available()];
                    is.read(t);
                    String res = new String(t);
                    if (!TextUtils.isEmpty(res)) {
                        data.set("sig_src", res);
                    }
                }
                is.close();
            } catch (Exception e) {
                XVdLog("merr", e.toString());
            }
        }catch (Exception e){
            XVdLog("merrg", e.toString());
        }
        //accMgr.close();

        //startWeWork();
    }
    public void setting(View view){
        startActivity(new Intent(view.getContext(), SettingActivity.class));
    }
    public void openSig(View view){
        startSelf("cc.xypp.cjluFree.sig");
    }
    public void openSigA(View view){
        startSelf("cc.xypp.cjluFree.siga");
    }
    public void openPass(View view){
        startSelf("cc.xypp.cjluFree.pass");
    }
    public void openX5D(View view){
        data.set("quick","x5");

        startWeWork();
    }
    public void about(View view){
        Uri uri = Uri.parse("https://freecjlu.xypp.cc/");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    private void startWeWork() {
        //Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.wework");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.tencent.wework", "com.tencent.wework.launch.LaunchSplashEduActivity");
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(intent);
    }
    private void XVdLog(String flg, String content) {
        Log.i("[AS_LOG]" + flg, content);
    }

    private void startAfterWifi(String action){
        Intent i = new Intent(this,WifiActivity.class);
        i.putExtra("act",WifiActivity.ACTION_CLOSE);
        i.putExtra("afterAction",action);
        startActivity(i);
    }
    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
    }
    public void killApp(View view){
        Intent i = new Intent(this,killAppActivity.class);
        startActivity(i);
    }
    private void startSelf(String action){
        Intent intent = new Intent(this,MainActivity.class);
        intent.setAction(action);
        startActivity(intent);
    }
}