package com.pacreau.seb.rapidsms;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RapidSMS
 * com.pacreau.seb.rapidsms
 *
 * @author spacreau
 * @since 30/11/16
 */
public class SmsService {

    public static final String SENT = "sent";
    public static final String DELIVERED = "delivered";
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final String NOTIF_ID_PREF_KEY = "NOTIF_ID_PREF_KEY";
    private static SmsService instance;

    private SmsService() {

    }

    public static SmsService getInstance() {
        if (instance == null) {
            instance = new SmsService();
        }
        return instance;
    }

    public static void sendLocalNotification(Context p_oContext, String p_sPhoneNumber, String p_sMessage, String p_sDate) {

        String title = p_oContext.getResources().getString(R.string.titleMessageSentNotif) + p_sPhoneNumber;
        String content = p_oContext.getResources().getString(R.string.contentMessageSentNotif) + p_sDate;
        // récup du son dans les prefs
        String stringPrefValue = PreferenceManager
                .getDefaultSharedPreferences(p_oContext).getString(SettingsActivity.RINGTONE_KEY_PREF, "");
        Uri uriNotificationSound;
        if (stringPrefValue.isEmpty()) {
            uriNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            uriNotificationSound = Uri.parse(stringPrefValue);
        }

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(p_oContext);
        notifBuilder
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(content);
        notifBuilder.setSound(uriNotificationSound);

        // Mise en forme multiligne
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(title);
        inboxStyle.addLine(content).addLine(p_sMessage);
        notifBuilder.setStyle(inboxStyle);

        NotificationManager notificationManager = (NotificationManager) p_oContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(computeNotificationId(p_oContext), notifBuilder.build());
    }

    private static int computeNotificationId(Context p_oContext) {
        int iNewId = 1 + PreferenceManager.getDefaultSharedPreferences(p_oContext).getInt(NOTIF_ID_PREF_KEY, 0);
        PreferenceManager.getDefaultSharedPreferences(p_oContext).edit().putInt(NOTIF_ID_PREF_KEY, iNewId).commit();
        return iNewId;
    }

    public static void saveData(Context p_oContext, String sDate, String sPhoneNumber, String sMessage) {

        PreferenceManager
                .getDefaultSharedPreferences(p_oContext.getApplicationContext()).edit()
                .putString(MainActivity.LAST_SENDING_DATE, sDate)
                .putString(MainActivity.LAST_SENDING_TO, sPhoneNumber)
                .putString(MainActivity.LAST_SENDING_CONTENT, sMessage).commit();
    }

    public static void doOnSmsDelivered(int resultCode, Context context, Intent intent) {
        String sPhoneNumber = intent.getStringExtra(MainActivity.LAST_SENDING_TO);
        switch (resultCode) {
            case Activity.RESULT_OK:
                SimpleDateFormat oFormatDate = new SimpleDateFormat(context.getResources().getString(R.string.lastMessageFormatDate),
                        Locale.getDefault());
                String sDate = oFormatDate.format(new Date());
                String sMessage = intent.getStringExtra(MainActivity.LAST_SENDING_CONTENT);
                intent.putExtra(MainActivity.LAST_SENDING_DATE, sDate);
                Toast.makeText(context.getApplicationContext(), "Délivré à " + sPhoneNumber, Toast.LENGTH_LONG).show();
                SmsService.saveData(context, sDate, sPhoneNumber, sMessage);
                SmsService.sendLocalNotification(context, sPhoneNumber, sMessage, sDate);
                break;
            case Activity.RESULT_CANCELED:
                Toast.makeText(context.getApplicationContext(), "Pas Délivré à " + sPhoneNumber, Toast.LENGTH_LONG).show();
                break;
            case Activity.RESULT_FIRST_USER:
                Toast.makeText(context.getApplicationContext(), "First User à " + sPhoneNumber, Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(context.getApplicationContext(), "'" + resultCode + "' Default à " + sPhoneNumber, Toast.LENGTH_LONG).show();
                break;
        }
    }

    public static void doOnSmsSend(int resultCode, Context context, Intent intent) {
        String result = "";
        String sPhoneNumber = intent.getStringExtra(MainActivity.LAST_SENDING_TO);

        switch (resultCode) {

            case Activity.RESULT_OK:
                result = "Envoyé à " + sPhoneNumber;
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                result = "Transmission failed" + sPhoneNumber;
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                result = "Radio off";
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                result = "No PDU defined " + sPhoneNumber;
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                result = "No service";
                break;
        }

        Toast.makeText(context.getApplicationContext(), result, Toast.LENGTH_LONG).show();
    }

    public void sendSms(final Context p_oContext) {

        String sPhoneNumber = PreferenceManager
                .getDefaultSharedPreferences(p_oContext)
                .getString(SettingsActivity.PHONE_NUMBER_PREF_KEY, "");

        if (p_oContext instanceof Activity) {
            View drawerView = ((Activity) p_oContext).findViewById(R.id.drawer_layout);
            if (ContextCompat.checkSelfPermission(p_oContext, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) p_oContext, Manifest.permission.SEND_SMS)) {
                    ActivityCompat.requestPermissions((Activity) p_oContext,
                            new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                }
            }
            if (sPhoneNumber.isEmpty()) {
                Snackbar.make(drawerView, p_oContext.getResources().getString(R.string.errorMessageNotSentPhoneNumberNotDefined)
                        + sPhoneNumber, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Modifier", new View.OnClickListener() {
                            @Override
                            public void onClick(View p_view) {
                                p_view.getContext().startActivity(new Intent(p_oContext, SettingsActivity.class));
                            }
                        }).show();
                return;
            }
        }
        String sMessage = PreferenceManager
                .getDefaultSharedPreferences(p_oContext)
                .getString(SettingsActivity.MESSAGE_KEY_PREF, "");

        try {
            Intent sentIntent = new Intent(SENT);
            sentIntent.putExtra(MainActivity.LAST_SENDING_TO, sPhoneNumber);
            sentIntent.putExtra(MainActivity.LAST_SENDING_CONTENT, sMessage);

            /*Create Pending Intents*/
            PendingIntent sentPI = PendingIntent.getBroadcast(
                    p_oContext.getApplicationContext(), 0, sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Intent deliveryIntent = new Intent(DELIVERED);
            deliveryIntent.putExtra(MainActivity.LAST_SENDING_TO, sPhoneNumber);
            deliveryIntent.putExtra(MainActivity.LAST_SENDING_CONTENT, sMessage);

            PendingIntent deliverPI = PendingIntent.getBroadcast(
                    p_oContext.getApplicationContext(), 0, deliveryIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);


            SmsManager.getDefault().sendTextMessage(sPhoneNumber, null, sMessage, sentPI, deliverPI);

        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "sendSms", e);
        }
    }
}
