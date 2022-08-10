package com.example.securemessenger.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Message implements Parcelable {

    private String senderUid;
    private String receiverUid;
    private String text;
    private String id;
    private Date timestamp;
    private boolean isReadSender;
    private boolean isReadReceiver;
    private String photoPath;

    public Message(String senderUid, String receiverUid, String text, String id, Date timestamp, boolean isReadSender, boolean isReadReceiver, String photoPath) {
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.text = text;
        this.id = id;
        this.timestamp = timestamp;
        this.isReadSender = isReadSender;
        this.isReadReceiver = isReadReceiver;
        this.photoPath = photoPath;
    }

    public Message() {

    }

    protected Message(Parcel in) {
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isReadSender() {
        return isReadSender;
    }

    public void setReadSender(boolean readSender) {
        isReadSender = readSender;
    }

    public boolean isReadReceiver() {
        return isReadReceiver;
    }

    public void setReadReceiver(boolean readReceiver) {
        isReadReceiver = readReceiver;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
