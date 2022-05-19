package cc.xypp.cjluFree;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuApiConstants;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.ShizukuProvider;

public class WifiActivity extends AppCompatActivity {
    public static int ACTION_OPEN=1,ACTION_CLOSE=2;
    boolean selecting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        Intent i = getIntent();
        final int action;
        final Context ctx=this;
        final String afterActivity,afterAction;

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
                selecting=true;
                if(action==ACTION_OPEN){
                    if(wm.getWifiState()!=WifiManager.WIFI_STATE_ENABLED){
                        if(!wm.setWifiEnabled(true) &&Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                            startActivityForResult(panelIntent,32);
                        }
                        for(int i=0;i<20&&wm.getWifiState()!=WifiManager.WIFI_STATE_ENABLED&&selecting;i++) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }else if(action==ACTION_CLOSE){
                    if(wm.getWifiState()!=WifiManager.WIFI_STATE_DISABLED){
                        if(!wm.setWifiEnabled(false) &&Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                            startActivityForResult(panelIntent,32);
                        }
                        for(int i=0;i<20&&wm.getWifiState()!=WifiManager.WIFI_STATE_DISABLED&&selecting;i++) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                actionDone(afterAction);
            }
        }).start();
    }

    private void actionDone(String a) {
        if(a!=null && !a.equals("")){
            Intent i = new Intent(a);
            i.setClassName("cc.xypp.cjluFree","cc.xypp.cjluFree.MainActivity");
            i.putExtra("wifi",false);
            startActivity(i);
        }
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==32)selecting=false;
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
        Shizuku.newProcess(new String[]{"svc wifi " + (open ? "enable" : "disable")},null,null);
        return false;
    }
}