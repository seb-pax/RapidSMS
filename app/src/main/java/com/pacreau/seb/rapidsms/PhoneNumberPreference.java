package com.pacreau.seb.rapidsms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.view.View;

/**
 * RapidSMS
 * com.pacreau.seb.rapidsms
 *
 * @author spacreau
 * @since 30/11/16
 */
public class PhoneNumberPreference
        extends EditTextPreference
        implements EditTextPreference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public PhoneNumberPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnPreferenceClickListener(this);
    }

    @Override
    protected View onCreateDialogView() {
        PreferenceManager
                .getDefaultSharedPreferences(this.getContext())
                .registerOnSharedPreferenceChangeListener(this);
        return super.onCreateDialogView();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        PreferenceManager
                .getDefaultSharedPreferences(this.getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDialogClosed(positiveResult);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
        ((Activity) this.getContext()).startActivityForResult(intent, SettingsActivity.REQUEST_PICK_CONTACT);
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsActivity.PHONE_NUMBER_PREF_KEY)) {
            String newValue = sharedPreferences.getString(key, "");
            this.getEditText().setText(newValue);
        }
    }
}
