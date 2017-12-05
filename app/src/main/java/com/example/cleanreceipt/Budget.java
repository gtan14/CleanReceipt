package com.example.cleanreceipt;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by Gerald on 11/28/2017.
 */

public class Budget extends ExpandableGroup<Categories> {

    private String title;

    public Budget(String title, List<Categories> items){
        super(title, items);
        this.title = title;
    }

    public String getTitle(){
        return title;
    }
}
