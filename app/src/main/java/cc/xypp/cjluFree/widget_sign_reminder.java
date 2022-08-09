package cc.xypp.cjluFree;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class widget_sign_reminder extends AppWidgetProvider {
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
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
        }else{
            views.setTextViewText(R.id.widget_signStatus,"未打卡");
            views.setInt(R.id.widget_layout,"setBackgroundResource",R.drawable.ic_widget_bg_default);
            views.setInt(R.id.widget_btn,"setBackgroundResource",R.drawable.ic_widget_btn_normal);
        }

        String lastSignDate;
        if(ts == 0)lastSignDate = "无记录";
        else lastSignDate = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(new Date(ts));

        views.setTextViewText(R.id.widget_sign_rec,"上次打卡："+lastSignDate);
        Intent clickIntent = new Intent(context,MainActivity.class);
        clickIntent.setAction("cc.xypp.cjluFree.sig");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
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
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND,10);

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, widget_sign_reminder.class);
        intent.setAction("cc.xypp.cjlufree.widgetUpdate");

        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        alarmMgr.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        if (alarmMgr!= null && alarmIntent!=null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}