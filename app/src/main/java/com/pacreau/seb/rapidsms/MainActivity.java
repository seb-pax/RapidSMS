package com.pacreau.seb.rapidsms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String SENDING_SMS = "SENDING_SMS";
    public static final String LAST_SENDING_DATE = "LAST_SENDING_DATE";
    public static final String LAST_SENDING_TO = "LAST_SENDING_TO";
    public static final String LAST_SENDING_CONTENT = "LAST_SENDING_CONTENT";
    private static final int DURATION_WAIT_IN_MS = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final MainActivity thisFinal = this;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setEnabled(false);
                SmsService.getInstance().sendSms(thisFinal);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setEnabled(true);
                    }
                }, DURATION_WAIT_IN_MS);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final String sPhoneNumber = PreferenceManager.getDefaultSharedPreferences(this).getString(LAST_SENDING_TO, "");
        String sDate = PreferenceManager.getDefaultSharedPreferences(this).getString(LAST_SENDING_DATE, "");
        final String sContent = PreferenceManager.getDefaultSharedPreferences(this).getString(LAST_SENDING_CONTENT, "");
        this.changeUIDesign(sPhoneNumber, sDate, sContent);

        /* Register for SMS send action */
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SmsService.doOnSmsSend(getResultCode(), context, intent);
            }
        }, new IntentFilter(SmsService.SENT));

        final Activity oMyContext = this;
        /* Register for Delivery event */
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SmsService.doOnSmsDelivered(getResultCode(), context, intent);
                String sMessage = intent.getStringExtra(MainActivity.LAST_SENDING_CONTENT);
                String sDate = intent.getStringExtra(MainActivity.LAST_SENDING_DATE);
                String sPhoneNumber = intent.getStringExtra(MainActivity.LAST_SENDING_TO);
                changeUIDesign(sPhoneNumber, sDate, sMessage);
            }
        }, new IntentFilter(SmsService.DELIVERED));
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

    public void changeUIDesign(String p_sPhoneNumber, String p_sDate, String p_sContent) {

        TextView oTextView = (TextView) this.findViewById(R.id.lastMessageDate);
        if (oTextView != null) {
            oTextView.setText(p_sDate);
        }
        oTextView = (TextView) this.findViewById(R.id.lastMessageTo);
        if (oTextView != null) {
            oTextView.setText(p_sPhoneNumber);
        }
        oTextView = (TextView) this.findViewById(R.id.lastMessageContent);
        if (oTextView != null) {
            oTextView.setText(p_sContent);
        }
    }

}
