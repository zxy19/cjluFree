package cc.xypp.cjluFree;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import de.robv.android.xposed.XSharedPreferences;
import cc.xypp.cjluFree.R;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch auto;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch inj;
    dataUtil data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data=new dataUtil(getContentResolver());
        String quick = getIntent().getAction();
        if(quick!=null){
            Log.i("[AS_LOG][APP]INTENT", quick);
            if(quick.equals("cc.xypp.cjluFree.sig")){
                data.set("quick","sig");
                data.set("once_inj","true");
            }else if(quick.equals("cc.xypp.cjluFree.pass")){
                data.set("quick","pass");
            }else quick=null;

            if(quick!=null){
                startWeWork();
                finish();
            }
        }
        setContentView(R.layout.activity_main);
        inj = findViewById(R.id.b_inj);
        auto = findViewById(R.id.b_auto);

        inj.setChecked(data.get("inj").equals("true"));
        auto.setChecked(data.get("auto").equals("true"));
    }
    public void autoChange(View view){
        data.set("auto",auto.isChecked()?"true":"false");
    }
    public void injChange(View view){
        data.set("inj",inj.isChecked()?"true":"false");
    }

    public void openSig(View view){
        data.set("quick","sig");
        data.set("once_inj","true");
        startWeWork();
    }
    public void openPass(View view){
        data.set("quick","pass");

        startWeWork();
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
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.wework");
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}