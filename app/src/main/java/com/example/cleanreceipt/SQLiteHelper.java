package com.example.cleanreceipt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import java.sql.Array;
import java.util.ArrayList;


public class SQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SQLiteDatabase.db";
    public SQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public String TABLE_NAME;
    //public static final String ROW_ID="ROW_ID";
    public static final String NAME = "NAME";
    public static final String DATE = "DATE";
    public static final String PRICE = "PRICE";
    public static final String CATEGORY = "CATEGORY";
    public static final String LOCATION = "LOCATION";
    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String FIRST_NAME = "FIRST_NAME";
    public static final String LAST_NAME = "LAST_NAME";
    public static final String IMAGE = "IMAGE";
    //public static final String MONTH = "MONTH";

    SQLiteDatabase database;

    @Override
    public void onCreate(SQLiteDatabase db) {


    }

    //Creates the table corresponding to the workout with the table name as the workout name
    public void createReceiptTable(String tableName) {
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        String CREATE_TABLE_NEW_USER = "CREATE TABLE " + tableName + " ( " + NAME + " STRING," + DATE + " STRING," + PRICE + " STRING," + CATEGORY + " STRING," + LOCATION + " STRING," + IMAGE + " BLOB NOT NULL)";
        database.execSQL(CREATE_TABLE_NEW_USER);
        TABLE_NAME = tableName;
        database.close();
    }

    public void createSignupTable(String tableName){
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        String CREATE_TABLE_NEW_USER = "CREATE TABLE " + tableName + " ( " + USERNAME + " STRING, " + PASSWORD + " STRING, " + FIRST_NAME + " STRING," + LAST_NAME + " STRING)";
        database.execSQL(CREATE_TABLE_NEW_USER);
        TABLE_NAME = tableName;
        database.close();
    }

        //NOT IMPLEMENTED YET
        //might be done now
    public void createBudgetTable(String tableName){
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        String CREATE_TABLE_NEW_USER = "CREATE TABLE " + tableName + " ( " + CATEGORY + " STRING, " + PRICE + " STRING)";
        database.execSQL(CREATE_TABLE_NEW_USER);
        TABLE_NAME = tableName;
        database.close();
    }

    public void createBudgetTotalTable(String tableName){
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        String CREATE_TABLE_NEW_USER = "CREATE TABLE " + tableName + " ( " + PRICE + " STRING)";
        database.execSQL(CREATE_TABLE_NEW_USER);
        TABLE_NAME = tableName;
        database.close();
    }

    public void categoriesTable(String tableName){
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        String categories = "CREATE TABLE" + tableName + "(" + CATEGORY + "STRING)";
        database.execSQL(categories);
        TABLE_NAME = tableName;
        database.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    //Inserts data fields for receipt
    public void insertRecordReceipt(ReceiptModel contact, String tableName, byte[] bytes) {
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME, contact.getName());
        contentValues.put(DATE, contact.getDate());
        contentValues.put(PRICE, contact.getPrice());
        contentValues.put(CATEGORY, contact.getCategory());
        contentValues.put(LOCATION, contact.getLocation());
        contentValues.put(IMAGE, bytes);
        database.insert(tableName, null, contentValues);
        database.close();
    }

    //Inserts data fields for signup
    public void insertRecordSignup(LoginModel contact, String tableName) {
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERNAME, contact.getUsername());
        contentValues.put(PASSWORD, contact.getPassword());
        contentValues.put(FIRST_NAME, contact.getFirstName());
        contentValues.put(LAST_NAME, contact.getLastName());
        database.insert(tableName, null, contentValues);
        database.close();
    }

    public void insertRecordBudgetTotal(BudgetModel contact, String tableName) {
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        //insert content values
        contentValues.put(PRICE, contact.getPrice());
        database.insert(tableName, null, contentValues);
        database.close();
    }

    //Inserts data fields for budget
        //NOT IMPLEMENTED YET
    public void insertRecordBudget(BudgetModel contact, String tableName){
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        //insert content values
        contentValues.put(CATEGORY, contact.getCategory());
        contentValues.put(PRICE, contact.getPrice());
        database.insert(tableName, null, contentValues);
        database.close();
    }

    public void updateColumn(String price, String tableName, String row){
        database = this.getReadableDatabase();
        String dbPrice = null;
        String query = "SELECT * FROM " + tableName + " WHERE " + CATEGORY + "=?";
        Cursor cursor = database.rawQuery(query, new String[]{row + ""});
        while(cursor.moveToNext()){
                dbPrice = cursor.getString(cursor.getColumnIndex(PRICE));
        }
        cursor.close();

        double dbPriceDouble = Double.parseDouble(dbPrice);
        double priceDouble = Double.parseDouble(price);

        ContentValues contentValues = new ContentValues();
        contentValues.put("PRICE", String.format("%s", dbPriceDouble - priceDouble));
        database.update(tableName, contentValues, CATEGORY + "= ?", new String[] {row});
    }

    //Deletes all the rows in the table
    public void deleteRecord(String tableName) {
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        database.delete(tableName, null, null);
        database.close();
    }


    //Deletes tableName from database
    public void deleteTable(String tableName){
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        database.execSQL("DROP TABLE IF EXISTS " + tableName);
        database.close();
    }

    //Gets all the table names and assigns it as an ArrayList<String>
    public ArrayList<String> getTableNames(){
        database = this.getWritableDatabase();
        ArrayList<String> tableNames = new ArrayList<String>();
        Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);


        //While cursor is not in the last position and while it has a position after
        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                tableNames.add( c.getString( c.getColumnIndex("name")) );
                c.moveToNext();
            }
        }
        c.close();
        database.close();
        return tableNames;
    }


    //Gets each column value for tableName
    public ArrayList<ReceiptModel> getReceiptRecords(String tableName) {
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        Cursor cursor = database.query(tableName, null, null, null, null, null, null);

        ArrayList<ReceiptModel> exercise = new ArrayList<ReceiptModel>();
        ReceiptModel receiptModel;
        if (cursor.getCount() > 0) {
            //Gets data from each row and stores it into ArrayList<ExerciseModel>
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                receiptModel = new ReceiptModel();
                receiptModel.setName(cursor.getString(0));
                receiptModel.setDate(cursor.getString(1));
                receiptModel.setPrice(cursor.getString(2));
                receiptModel.setCategory(cursor.getString(3));
                receiptModel.setLocation(cursor.getString(4));
                receiptModel.setImage(cursor.getBlob(5));

                exercise.add(receiptModel);
            }
        }
        cursor.close();
        database.close();
        return exercise;
    }

    //Gets each column value for tableName
    public ArrayList<LoginModel> getLoginRecords(String tableName) {
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        Cursor cursor = database.query(tableName, null, null, null, null, null, null);
        ArrayList<LoginModel> login = new ArrayList<LoginModel>();
        LoginModel loginModel;
        if (cursor.getCount() > 0) {
            //Gets data from each row and stores it into ArrayList<ExerciseModel>
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                loginModel = new LoginModel();
                loginModel.setUsername(cursor.getString(0));
                loginModel.setPassword(cursor.getString(1));
                loginModel.setFirstName(cursor.getString(2));
                loginModel.setLastName(cursor.getString(3));

                login.add(loginModel);
            }
        }
        cursor.close();
        database.close();
        return login;
    }

    //Gets each column value for tableName
        //NOT IMPLEMENTED YET
    public ArrayList<BudgetModel> getBudgetRecords(String tableName){
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        Cursor cursor = database.query(tableName, null, null, null, null, null, null);
        ArrayList<BudgetModel> budget = new ArrayList<BudgetModel>();
        BudgetModel budgetModel;
        if(cursor.getCount() > 0){
            for(int i = 0; i < cursor.getCount(); i++){
                cursor.moveToNext();
                budgetModel = new BudgetModel();
                budgetModel.setCategory(cursor.getString(0));
                budgetModel.setPrice(cursor.getString(1));
                budget.add(budgetModel);
            }
        }
        cursor.close();
        return budget;
    }

    public ArrayList<BudgetModel> getBudgetTotalRecords(String tableName){
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        Cursor cursor = database.query(tableName, null, null, null, null, null, null);
        ArrayList<BudgetModel> budget = new ArrayList<BudgetModel>();
        BudgetModel budgetModel;
        if(cursor.getCount() > 0){
            for(int i = 0; i < cursor.getCount(); i++){
                cursor.moveToNext();
                budgetModel = new BudgetModel();
                budgetModel.setPrice(cursor.getString(0));
                budget.add(budgetModel);
            }
        }
        cursor.close();
        return budget;
    }

    boolean tableExists(String tableName)
    {
        database =this.getReadableDatabase();
        Cursor cursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        database.close();
        return false;
    }

}
