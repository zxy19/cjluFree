package cc.xypp.cjluFree;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.io.InputStream;

public class AgreementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        AssetManager accMgr = getResources().getAssets();
        try {
            InputStream is = accMgr.open("agm.txt");
            if(is.available()!=0) {
                byte[] t = new byte[is.available()];
                is.read(t);
                String res = new String(t);
                if (!TextUtils.isEmpty(res)) {
                    ((TextView)findViewById(R.id.aggrement)).setText(res);
                }else ((TextView)findViewById(R.id.aggrement)).setText("协议加载没有成功。你可以选择联系开发者，或者相信自己不会犯事并同意");
            }
            is.close();
        } catch (Exception ignored) {}
    }
    public void cli_rf(View view){
        finish();
    }
    public void cli_ac(View view){
        (new dataUtil(getContentResolver())).set("agree","true");
        finish();
        startActivity(new Intent(view.getContext(),MainActivity.class));
    }
}