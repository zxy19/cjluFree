package cc.xypp.cjluFree;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import rikka.shizuku.Shizuku;

public class killAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kill_app);
        if(rootWay() || shizukuWay()) {
            Toast.makeText(this,"已结束进程",Toast.LENGTH_LONG).show();
        }else{
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", "com.tencent.wework", null));
            startActivity(intent);
            Toast.makeText(this,"无相关权限，请手动结束进程",Toast.LENGTH_LONG).show();
        }
        finish();
    }
    protected boolean rootWay(){
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.write("am force-stop com.tencent.wework".getBytes());
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
    private boolean shizukuWay(){
        //if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q)return false;
        if(Shizuku.pingBinder())
            Shizuku.newProcess(new String[]{"am","force-stop","com.tencent.wework"},null,null);
        else return false;
        return true;
    }
}