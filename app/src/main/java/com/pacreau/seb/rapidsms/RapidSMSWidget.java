package com.pacreau.seb.rapidsms;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SettingsActivity}
 */
public class RapidSMSWidget extends AppWidgetProvider {

    private final static String SEND_SMS_ACTION = "com.pacreau.seb.rapidsms.SendSms";

    static void updateAppWidget(Context p_context, AppWidgetManager p_appWidgetManager, int p_appWidgetId) {

        String sPhoneNumber = PreferenceManager
                .getDefaultSharedPreferences(p_context)
                .getString(SettingsActivity.PHONE_NUMBER_PREF_KEY, "");

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(p_context.getPackageName(), R.layout.rapid_smswidget);
        views.setTextViewText(R.id.action_send_sms, sPhoneNumber);

        Intent intent = new Intent(p_context, RapidSMSWidget.class);
        intent.setAction(SEND_SMS_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(p_context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.action_send_sms, pendingIntent);

        // Instruct the widget manager to update the widget
        p_appWidgetManager.updateAppWidget(p_appWidgetId, views);
    }

    public static void updateWidget(Activity p_oActivity) {
        Intent intent = new Intent(p_oActivity, RapidSMSWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        Application oApplication = p_oActivity.getApplication();
        int ids[] = AppWidgetManager.getInstance(oApplication).getAppWidgetIds(
                new ComponentName(oApplication, RapidSMSWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        p_oActivity.sendBroadcast(intent);
    }

    @Override
    public void onUpdate(Context p_context, AppWidgetManager p_appWidgetManager, int[] p_appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : p_appWidgetIds) {
            updateAppWidget(p_context, p_appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context p_context, int[] p_appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        //for (int appWidgetId : p_appWidgetIds) {
        //
        //}
    }

    @Override
    public void onEnabled(Context p_context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context p_context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context p_context, Intent p_intent) {
        super.onReceive(p_context, p_intent);
        if (SEND_SMS_ACTION.equals(p_intent.getAction())) { // clic sur le bouton dans le widget
            SmsService.getInstance().sendSms(p_context);
        } else {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(p_context);
            int[] ids = p_intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (ids != null && ids.length != 0) {
                updateAppWidget(p_context, appWidgetManager, ids[0]);
            }
        }
    }

}

