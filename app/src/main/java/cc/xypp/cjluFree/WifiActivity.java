package cc.xypp.cjluFree;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;

import rikka.shizuku.Shizuku;

public class WifiActivity extends AppCompatActivity {
    public static int ACTION_OPEN=1,ACTION_CLOSE=2;
    static boolean selecting=false;
    String afterAction;
    boolean autoStartWifi=false,step2=false;
    WifiManager wm;
    ConnectivityManager connectivityManager;
    final Runnable startWifi=new Runnable() {
        @Override
        public void run() {
            if(wm.getWifiState()!=WifiManager.WIFI_STATE_ENABLED){
                setWifi(wm,true);
                for(int i=0;i<20&&(wm.getWifiState()!=WifiManager.WIFI_STATE_ENABLED||selecting);i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            finish();
        }
    };
    final Runnable stopWifi=new Runnable() {
        @Override
        public void run() {
            NetworkInfo wifiNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if(wm.getWifiState()!=WifiManager.WIFI_STATE_DISABLED && wifiNetworkInfo.getType()==ConnectivityManager.TYPE_WIFI){
                autoStartWifi =true;
                setWifi(wm,false);
                for(int i=0;i<20&&(wm.getWifiState()!=WifiManager.WIFI_STATE_DISABLED||selecting);i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            step2=true;
            actionDone(afterAction);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        Intent i = getIntent();
        final int action;
        final Context ctx=this;
        wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        afterAction=i.getStringExtra("afterAction");
        step2=false;
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(step2){
            if(autoStartWifi)new Thread(startWifi).start();
            else finish();
        }else new Thread(stopWifi).start();
    }
    private void setWifi(WifiManager wm,boolean b) {
        if (wm.setWifiEnabled(b)) return;
        if (rootWay(b)) return;
        if (shizukuWay(b)) return;
        selecting = true;
        Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
        startActivityForResult(panelIntent, 32);
    }
    private void actionDone(String a) {
        selecting=false;
        if(a!=null && !a.equals("")){
            Intent i = new Intent(a);
            i.setClassName("cc.xypp.cjluFree","cc.xypp.cjluFree.MainActivity");
            i.putExtra("wifi",false);
            startActivity(i);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==32) {
            selecting = false;
        }
    }

    protected boolean rootWay(boolean open){
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.write("svc wifi ".getBytes());
            os.write((open?"enable":"disable").getBytes());
            os.writeBytes("\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            os.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    private boolean shizukuWay(boolean open){
        //if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q)return false;
        if(Shizuku.pingBinder())
            Shizuku.newProcess(new String[]{"svc","wifi", (open ? "enable" : "disable")},null,null);
        else return false;
        return true;
    }
}