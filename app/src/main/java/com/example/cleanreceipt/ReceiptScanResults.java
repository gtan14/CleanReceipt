package com.example.cleanreceipt;

import android.graphics.Bitmap;

/**
 * Created by Gerald on 11/21/2017.
 */

public class ReceiptScanResults {
    String price, location, date;
    Bitmap bitmap;

    public ReceiptScanResults(){

    }

    public String getPrice(){
        return price;
    }

    public void setPrice(String price){
        this.price = price;
    }

    public String getLocation(){
        return location;
    }

    public void setLocation(String location){
        this.location =location;
    }

    public String getDate(){
        return date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
    }
}
