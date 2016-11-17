package com.pacreau.seb.rapidsms;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int DURATION_WAIT_IN_MS = 5000;
    public static final String SENDING_SMS = "SENDING_SMS";
    public static final String LAST_SENDING_DATE = "LAST_SENDING_DATE";
    public static final String LAST_SENDING_TO = "LAST_SENDING_TO";
    public static final String LAST_SENDING_CONTENT = "LAST_SENDING_CONTENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setEnabled(false);
                sendSms(view);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (view != null) {
                            view.setEnabled(true);
                        }
                    }
                }, DURATION_WAIT_IN_MS);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (this.getIntent().getBooleanExtra(SENDING_SMS,true)) {
            sendSms(drawer);
        } else {
            String sPhoneNumber = PreferenceManager.getDefaultSharedPreferences(this).getString(LAST_SENDING_TO, "");
            String sDate = PreferenceManager.getDefaultSharedPreferences(this).getString(LAST_SENDING_DATE, "");
            String sContent = PreferenceManager.getDefaultSharedPreferences(this).getString(LAST_SENDING_CONTENT, "");
            changeUIDesign(drawer, sPhoneNumber,sDate,sContent);
        }
    }

    private void sendSms(final View p_view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
        String sPhoneNumber = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(SettingsActivity.PHONE_NUMBER_PREF_KEY, "");
        try {
            if (sPhoneNumber.isEmpty()) {
                final MainActivity moi = this;
                Snackbar.make(p_view, this.getResources().getString(R.string.errorMessageNotSentPhoneNumberNotDefined)
                        + sPhoneNumber, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Modifier", new View.OnClickListener() {
                            @Override
                            public void onClick(View p_view) {
                                startActivity(new Intent(moi, SettingsActivity.class));
                            }
                        }).show();
                return;
            }
            String sMessage = PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .getString(SettingsActivity.MESSAGE_KEY_PREF, "");

            SmsManager.getDefault().sendTextMessage(sPhoneNumber, null, sMessage, null, null);

            SimpleDateFormat oFormatDate = new SimpleDateFormat(this.getResources().getString(R.string.lastMessageFormatDate));
            String sDate = oFormatDate.format(new Date());

            PreferenceManager
                    .getDefaultSharedPreferences(this).edit()
                    .putString(LAST_SENDING_DATE,sDate)
                    .putString(LAST_SENDING_TO, sPhoneNumber)
                    .putString(LAST_SENDING_CONTENT, sMessage).commit();

            Snackbar.make(p_view, this.getResources().getString(R.string.okMessageSent) + sPhoneNumber, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            changeUIDesign(p_view, sPhoneNumber, sDate, sMessage);
            sendLocalNotification(sPhoneNumber, sMessage , sDate);

        } catch (Exception e) {
            Snackbar.make(p_view, this.getResources().getString(R.string.errorMessageNotSent)
                    + sPhoneNumber, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void changeUIDesign(View p_view, String p_sPhoneNumber , String p_sDate, String p_sContent) {

        TextView oTextView = (TextView) findViewById(R.id.lastMessageDate);
        oTextView.setText(p_sDate);

        oTextView = (TextView) findViewById(R.id.lastMessageTo);
        oTextView.setText(p_sPhoneNumber);

        oTextView = (TextView) findViewById(R.id.lastMessageContent);
        oTextView.setText(p_sContent);
    }

    private void sendLocalNotification(String p_sPhoneNumber, String p_sMessage, String p_sDate) {
        Context ctx = getApplicationContext();

        String title = getResources().getString(R.string.titleMessageSentNotif) + p_sPhoneNumber ;
        String content = getResources().getString(R.string.contentMessageSentNotif) + p_sDate ;
        // récup du son dans les prefs
        String stringPrefValue = PreferenceManager
                .getDefaultSharedPreferences(this).getString(SettingsActivity.RINGTONE_KEY_PREF, "");
        Uri uriNotificationSound;
        if (stringPrefValue.isEmpty()) {
            uriNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            uriNotificationSound = Uri.parse(stringPrefValue);
        }

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(ctx);
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
        inboxStyle.addLine(content).addLine( p_sMessage);
        notifBuilder.setStyle(inboxStyle);

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notifBuilder.build());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            this.startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {
            // Handle the camera action
            Intent oIntent = new Intent(this, SettingsActivity.class);
            this.startActivity(oIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
