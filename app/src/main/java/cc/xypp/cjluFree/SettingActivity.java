package cc.xypp.cjluFree;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

public class SettingActivity extends AppCompatActivity {
    dataUtil data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        data=new dataUtil(getContentResolver());
        ((Switch)findViewById(R.id.b_inj)).setChecked(data.get("inj").equals("true"));
        ((Switch)findViewById(R.id.b_auto)).setChecked(data.get("auto").equals("true"));
        ((Switch)findViewById(R.id.b_inj_pass)).setChecked(data.get("inj_pass").equals("true"));
        ((Switch)findViewById(R.id.b_wifi)).setChecked(data.get("auto_wifi").equals("true"));
    }
    public void optChange(View view){
        data.set((String) view.getTag(),((Switch)view).isChecked()?"true":"false");
    }
    public void editBtn(View view){
        String tag= (String) view.getTag();
        int o=tag.indexOf("/");
        Intent i = new Intent();
        i.putExtra("varName",tag.substring(o+1));
        i.putExtra("varType",tag.substring(0,o));
        i.setClass(this,EditorActivity.class);
        startActivity(i);
    }
}