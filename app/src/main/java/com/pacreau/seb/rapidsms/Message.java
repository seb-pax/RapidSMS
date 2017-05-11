package com.pacreau.seb.rapidsms;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * RapidSMS
 * com.pacreau.seb.rapidsms
 *
 * @author spacreau
 * @since 11/05/2017
 */

public class Message implements Parcelable {
	public static final Parcelable.Creator<Message> CREATOR
			= new Parcelable.Creator<Message>() {

		public Message createFromParcel(Parcel in) {
			return new Message(in);
		}

		public Message[] newArray(int size) {
			return new Message[size];
		}
	};
	public String date;
	public String recipient;
	public String content;

	public Message() {
	}


	public Message(Parcel in) {
		this.date = in.readString();
		this.recipient = in.readString();
		this.content = in.readString();
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Describe the kinds of special objects contained in this Parcelable's
	 * marshalled representation.
	 *
	 * @return a bitmask indicating the set of special object types marshalled
	 * by the Parcelable.
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Flatten this object in to a Parcel.
	 *
	 * @param dest  The Parcel in which the object should be written.
	 * @param flags Additional flags about how the object should be written.
	 *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.date);
		dest.writeString(this.recipient);
		dest.writeString(this.content);
	}

}
