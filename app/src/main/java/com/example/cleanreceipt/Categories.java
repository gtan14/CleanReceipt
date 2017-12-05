package com.example.cleanreceipt;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Gerald on 11/28/2017.
 */

public class Categories implements Parcelable{

    private String category;
    private String price;

    public Categories(String category, String price){
        this.category = category;
        this.price = price;
    }

    protected Categories(Parcel in){
        category = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getCategory(){
        return category;
    }

    public String getPrice(){
        return price;
    }


    public static final Creator<Categories> CREATOR = new Creator<Categories>() {
        @Override
        public Categories createFromParcel(Parcel in) {
            return new Categories(in);
        }

        @Override
        public Categories[] newArray(int size) {
            return new Categories[size];
        }
    };


}
