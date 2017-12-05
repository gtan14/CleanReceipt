package com.example.cleanreceipt;

/**
 * Created by Ryan on 11/13/2017.
 */

public class BudgetModel {

    private String category;

    private String price;
    private String date;


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDate(){
        return date;
    }

    public void setDate(String date){
        this.date = date;
    }
}
