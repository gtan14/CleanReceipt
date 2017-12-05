package com.example.cleanreceipt;

/**
 * Created by ericapantoja on 11/19/17.
 */

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.ULocale;
import android.provider.SyncStateContract;
import android.view.View;
import android.os.Bundle;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Locale;

import android.support.v7.app.AppCompatActivity;





public class DBAdapter extends AppCompatActivity {

    Context c;

    SQLiteDatabase db;
    //DBHelper helper;
    SQLiteHelper helper;
    UploadReceipt uR;

    public DBAdapter(Context c) {
        this.c = c;
        helper = new SQLiteHelper(c);
    }

    //OPEN DB
    public void openDB() {
        try {
            db = helper.getWritableDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //CLOSE
    public void closeDB() {
        try {
            helper.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //INSERT DATA
    /*public boolean add(String name)
    {
        try
        {
            ContentValues cv=new ContentValues();
            cv.put(SQLiteHelper.NAME, name);
            db.insert(SQLiteHelper.TB_NAME, SQLiteHelper.ROW_ID, cv);
            return true;
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }*/

    //RETRIEVE DATA AND FILTER
    public Cursor retrieve(String searchTerm) {
        //SQLiteHelper sqLiteHelper = new SQLiteHelper(getActivity());
        //ArrayList<String> tableNames = sqLiteHelper.getTableNames();
        SQLiteHelper sqLiteHelper = new SQLiteHelper(getApplicationContext());
        ArrayList<String> tableNames = sqLiteHelper.getTableNames();
        ArrayList<ReceiptModel> receiptModels = sqLiteHelper.getReceiptRecords("receipt");
        ReceiptModel receiptModel;

        String[] columns = {SQLiteHelper.NAME, SQLiteHelper.DATE, SQLiteHelper.PRICE, SQLiteHelper.CATEGORY, SQLiteHelper.LOCATION};
        Cursor c = null;
        if (searchTerm != null && searchTerm.length() > 0) {
            String sql = "SELECT * FROM " + receiptModels + " WHERE " + receiptModels + " LIKE '%" + searchTerm + "%'";
            c = db.rawQuery(sql, null);
            return c;


        }
        c = db.query(sqLiteHelper.TABLE_NAME, columns, null, null, null, null, null);
        return c;


    }
}

    /*public void loginOnClick(View view) {
        SQLiteHelper sqLiteHelper = new SQLiteHelper(getActivity().getApplicationContext());
        ArrayList<String> tableNames = sqLiteHelper.getTableNames();

        if (tableNames.size() > 1) {
            for (int j = 0; j < tableNames.size(); j++) {
                if (tableNames.get(j).contains("signup")) {
                    ArrayList<LoginModel> loginModels = sqLiteHelper.getLoginRecords(tableNames.get(j));
                    if (loginModels.size() > 0) {
                        LoginModel loginModel;
                        for (int i = 0; i < loginModels.size(); i++) {
                            loginModel = loginModels.get(i);
                            String name = loginModel.getUsername();
                            String pass = loginModel.getPassword();

                            if (name.equals(username.getText().toString()) && pass.equals(password.getText().toString())) {
                                successfulLogin = true;
                            }
                        }
                    }
                }
            }
        }
    }*/


