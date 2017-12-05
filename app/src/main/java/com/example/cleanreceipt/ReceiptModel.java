package com.example.cleanreceipt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Gerald on 10/17/2017.
 */

public class ReceiptModel {

    private String name, date, price, category, location;
    private byte[] image;

    public byte[] getImageByte(){
        return image;
    }

    public void setImage(byte[] image){
        this.image = image;
    }

    public Bitmap getImage(){
        return getImage(image);
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getPrice(){
        return price;
    }

    public void setPrice(String price){
        this.price = price;
    }

    public String getDate(){
        return date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getCategory(){
        return category;
    }

    public void setCategory(String category){
        this.category = category;
    }

    public String getLocation(){
        return location;
    }

    public void setLocation(String location){
        this.location = location;
    }

    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}


