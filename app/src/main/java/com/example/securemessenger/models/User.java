package com.example.securemessenger.models;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String name;
    private String password;
    private String email;
    private String uid;
    private String photoPath;

    public User(String name, String password, String email, String uid, String photoPath) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.uid = uid;
        this.photoPath = photoPath;
    }

    public User() {

    }

    protected User(Parcel in) {
        name = in.readString();
        password = in.readString();
        email = in.readString();
        uid = in.readString();
        photoPath = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(password);
        dest.writeString(email);
        dest.writeString(uid);
        dest.writeString(photoPath);
    }
}
