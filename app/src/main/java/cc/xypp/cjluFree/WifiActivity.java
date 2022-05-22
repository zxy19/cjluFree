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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        Intent i = getIntent();
        final int action;
        final Context ctx=this;
        final String afterActivity,afterAction;
        final boolean[] autoStartWifi = {false};
        action=i.getIntExtra("act",-1);
        afterAction=i.getStringExtra("afterAction");
        System.out.println("ACT:"+action);

        switch (action){
            case 2:((TextView)findViewById(R.id.dtip1)).setText("正在关闭WIFI");break;
            case 1:((TextView)findViewById(R.id.dtip1)).setText("正在开启WIFI");break;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
                ConnectivityManager connectivityManager = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifiNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if(action==ACTION_OPEN){
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
                }else if(action==ACTION_CLOSE){
                    if(wm.getWifiState()!=WifiManager.WIFI_STATE_DISABLED && wifiNetworkInfo.getType()==ConnectivityManager.TYPE_WIFI){
                        autoStartWifi[0] =true;
                        setWifi(wm,false);
                        for(int i=0;i<20&&(wm.getWifiState()!=WifiManager.WIFI_STATE_DISABLED||selecting);i++) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                actionDone(afterAction, autoStartWifi[0]);
            }
        }).start();
    }

    private void setWifi(WifiManager wm,boolean b) {
        if(wm.setWifiEnabled(b))return;
        if(rootWay(b))return;
        if(shizukuWay(b))return;
        selecting=true;
        Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
        startActivityForResult(panelIntent,32);
    }

    private void actionDone(String a,boolean autoStartWifi) {
        selecting=false;
        if(a!=null && !a.equals("")){
            Intent i = new Intent(a);
            i.setClassName("cc.xypp.cjluFree","cc.xypp.cjluFree.MainActivity");
            i.putExtra("wifi",false);
            i.putExtra("autoStartWifi",autoStartWifi);
            startActivity(i);
        }
        finish();
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
        return true;
    }
}