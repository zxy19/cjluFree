package cc.xypp.cjluFree;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener{
    dataUtil data;
    private void updateRes(String assName,String stoName,AssetManager accMgr){
        if(data.get(stoName).equals("") || data.get("auto_update").equals("true"))
            try {
                InputStream is = accMgr.open(assName);
                if(is.available()!=0) {
                    byte[] t = new byte[is.available()];
                    is.read(t);
                    String res = new String(t);
                    if (!TextUtils.isEmpty(res)) {
                        data.set(stoName, res);
                    }
                }
                is.close();
            } catch (Exception e) {
                XVdLog("merr", e.toString());
            }
    }
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
        String quick = intent.getAction(),code="";
        if(quick!=null){
            Log.i("[AS_LOG][APP]INTENT", quick);
            switch (quick) {
                case "cc.xypp.cjluFree.sig":
                    if (intent.getBooleanExtra("wifi", true) && data.get("auto_wifi").equals("true")) {
                        startAfterWifi("cc.xypp.cjluFree.sig");
                        finish();
                        return;
                    } else {
                        data.set("quick", code = "sig");
                    }
                    break;
                case "cc.xypp.cjluFree.siga":
                    if (intent.getBooleanExtra("wifi", true) && data.get("auto_wifi").equals("true")) {
                        startAfterWifi("cc.xypp.cjluFree.siga");
                        finish();
                        return;
                    } else {
                        data.set("quick", code = "sig");
                        data.set("once_inj", "true");
                    }
                    break;
                case "cc.xypp.cjluFree.pass":
                    if (intent.getBooleanExtra("wifi", true) && data.get("auto_wifi").equals("true")) {
                        startAfterWifi("cc.xypp.cjluFree.pass");
                        finish();
                        return;
                    } else {
                        data.set("quick", code = "pass");
                    }
                    break;
                default:
                    quick = null;
                    break;
            }

            if(quick!=null){
                startWeWork(code);
                finish();
                return;
            }
        }
        setContentView(R.layout.activity_main);

        AssetManager accMgr = getResources().getAssets();
        try {
            updateRes("pass.js","pass_src",accMgr);
            updateRes("sig.js","sig_src",accMgr);
            updateRes("usr.html","user_page",accMgr);
            updateRes("jq_qinjIf.js","user_jq",accMgr);
            updateRes("jquery-1.11.1.min.js","user_jqo",accMgr);
        }catch (Exception e){
            XVdLog("merrg", e.toString());
        }


    }
    protected void onStart() {
        super.onStart();
        String lstSigTs = data.get("lastSignTs");
        System.out.println("[AS_LOG]MAIN-rec:"+lstSigTs);
        if(lstSigTs.equals(""))lstSigTs="0";
        long ts = Long.parseLong(lstSigTs);
        Date ltdat = new Date(ts);
        ((TextView)findViewById(R.id.text_sign_rec)).setText(
                new SimpleDateFormat("上次打卡：yyyy年MM月dd日 HH:mm:ss", Locale.CHINA).format(ltdat)
        );
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
        startWeWork("x5");
    }
    public void about(View view){
        Uri uri = Uri.parse("https://freecjlu.xypp.cc/");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    private String getUrl(String code){
        switch (code){
            case "x5":return Constants.X5;
            case "sig":return Constants.URL_SIG;
            case "pass":return Constants.URL_PASS;
            default:return "";
        }
    }
    private void startWeWork(String setCode) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.write(("am start -n com.tencent.wework/com.tencent.wework.common.web.JsWebActivity -e extra_web_title 自动化页面中间页 -e extra_web_url '"+getUrl(setCode)+"'").getBytes());
            os.writeBytes("\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            os.close();
            return;
        } catch (IOException ignore) { }
        try {
            Intent intent = new Intent();
            intent.setClassName("com.tencent.wework", "com.tencent.wework.common.web.JsWebActivity");
            intent.putExtra("extra_web_title", "自动化页面中间页");
            intent.putExtra("extra_web_url",getUrl(setCode));
            startActivity(intent);
            return;
        } catch (Exception ignore) { }
        try {
            data.set("quick",setCode);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName("com.tencent.wework", "com.tencent.wework.launch.LaunchSplashEduActivity");
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(intent);
            return;
        }catch (Exception ignore){}
        runOnUiThread(()-> Toast.makeText(this,"未安装企业微信，请先安装。",Toast.LENGTH_LONG).show());
    }
    private void XVdLog(String flg, String content) {
        Log.i("[AS_LOG]" + flg, content);
    }

    private void startAfterWifi(String action){
        Intent i = new Intent(this,WifiActivity.class);
        //i.putExtra("act",WifiActivity.ACTION_CLOSE);
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