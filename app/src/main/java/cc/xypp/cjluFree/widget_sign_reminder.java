package cc.xypp.cjluFree;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class widget_sign_reminder extends AppWidgetProvider {
    public static final String CLICK_ACTION = "cc.xypp.cjlufree.widgetClick";
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        dataUtil data = new dataUtil(context.getContentResolver());
        AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_sign_reminder);
        String lstSigTs = data.get("lastSignTs");
        if(lstSigTs.equals(""))lstSigTs="0";
        long ts = Long.parseLong(lstSigTs);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long today = calendar.getTimeInMillis();


        if(ts >= today){
            System.out.println("[AS_LOG]"+lstSigTs+","+String.valueOf(today));
            views.setTextViewText(R.id.widget_signStatus,"已打卡");
            views.setInt(R.id.widget_layout,"setBackgroundResource",R.drawable.ic_widget_bg_ok);
            views.setInt(R.id.widget_btn,"setBackgroundResource",R.drawable.ic_widget_btn_ok);
        }

        String lastSignDate;
        if(ts == 0)lastSignDate = "无记录";
        else lastSignDate = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(new Date(ts));

        views.setTextViewText(R.id.widget_sign_rec,"上次打卡："+lastSignDate);
        Intent clickIntent = new Intent(context.getApplicationContext(), widget_sign_reminder.class);
        clickIntent.setAction(CLICK_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0,
                clickIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        views.setOnClickPendingIntent(R.id.widget_btn,pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);
        System.out.println("[AS_LOG]UPDATED:ID#"+String.valueOf(appWidgetId));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        System.out.println("[AS_LOG]UPDATING:");
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        System.out.println("[AS_LOG]AAA:"+intent.getAction());
        if (intent.getAction().equals("cc.xypp.cjlufree.widgetUpdate")) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            for (int appWidgetId : appWidgetManager.getAppWidgetIds(new ComponentName(context, widget_sign_reminder.class))) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }else if(intent.getAction().equals(CLICK_ACTION)){
            Intent i = new Intent(context,MainActivity.class);
            i.setAction("cc.xypp.cjluFree.sig");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(i);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}