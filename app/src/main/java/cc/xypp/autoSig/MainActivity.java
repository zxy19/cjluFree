package cc.xypp.autoSig;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import de.robv.android.xposed.XSharedPreferences;
import cc.xypp.autoSig.R;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch auto;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch inj;
    dataUtil data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inj = findViewById(R.id.b_inj);
        auto = findViewById(R.id.b_auto);
        data=new dataUtil(getContentResolver());
        inj.setChecked(data.get("inj").equals("true"));
        auto.setChecked(data.get("auto").equals("true"));
    }
    public void autoChange(View view){
        data.set("auto",auto.isChecked()?"true":"false");
    }
    public void injChange(View view){
        data.set("inj",inj.isChecked()?"true":"false");
    }
}