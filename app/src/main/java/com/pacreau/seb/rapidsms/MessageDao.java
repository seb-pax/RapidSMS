package com.pacreau.seb.rapidsms;

import android.content.Context;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RapidSMS
 * com.pacreau.seb.rapidsms
 *
 * @author spacreau
 * @since 11/05/2017
 */

public class MessageDao {
	public static final String LAST_SENDING_DATE = "LAST_SENDING_DATE";
	public static final String LAST_SENDING_TO = "LAST_SENDING_TO";
	public static final String LAST_SENDING_CONTENT = "LAST_SENDING_CONTENT";
	private static MessageDao instance;

	private MessageDao() {

	}

	public static MessageDao getInstance() {
		if (instance == null) {
			instance = new MessageDao();
		}
		return instance;
	}

	public static void saveMessage(Context p_oContext, Message oMessage, boolean p_last) {
		SimpleDateFormat oFormatDate = new SimpleDateFormat(p_oContext.getResources().getString(R.string.lastMessageFormatDate),
				Locale.getDefault());

		String sDate = oFormatDate.format(new Date());
		if (p_last) {
			PreferenceManager
					.getDefaultSharedPreferences(p_oContext.getApplicationContext()).edit()
					.putString(LAST_SENDING_DATE, sDate)
					.putString(LAST_SENDING_TO, oMessage.getRecipient())
					.putString(LAST_SENDING_CONTENT, oMessage.getContent()).apply();
		} else {
			PreferenceManager.getDefaultSharedPreferences(p_oContext.getApplicationContext()).edit()
					.putString(SettingsActivity.PHONE_NUMBER_PREF_KEY, oMessage.getRecipient())
					.putString(SettingsActivity.MESSAGE_KEY_PREF, oMessage.getContent()).apply();
		}
	}


	public Message findMessage(Context p_oContext, boolean p_last) {
		Message r_oMessage = new Message();
		if (p_last) {
			final String sPhoneNumber = PreferenceManager.getDefaultSharedPreferences(p_oContext).getString(LAST_SENDING_TO, "");
			String sDate = PreferenceManager.getDefaultSharedPreferences(p_oContext).getString(LAST_SENDING_DATE, "");
			final String sContent = PreferenceManager.getDefaultSharedPreferences(p_oContext).getString(LAST_SENDING_CONTENT, "");

			r_oMessage.setContent(sContent);
			r_oMessage.setDate(sDate);
			r_oMessage.setRecipient(sPhoneNumber);
		} else {
			String sMessage = PreferenceManager
					.getDefaultSharedPreferences(p_oContext)
					.getString(SettingsActivity.MESSAGE_KEY_PREF, "");
			String sPhoneNumber = PreferenceManager
					.getDefaultSharedPreferences(p_oContext)
					.getString(SettingsActivity.PHONE_NUMBER_PREF_KEY, "");
			r_oMessage.setRecipient(sPhoneNumber);
			r_oMessage.setContent(sMessage);
			r_oMessage.setDate("");
		}
		return r_oMessage;
	}

}
