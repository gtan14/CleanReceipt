package com.example.cleanreceipt;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.SearchView;
import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Arrays;
import java.util.Calendar;
import java.util.Calendar;


import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import com.example.cleanreceipt.DBAdapter;
import com.rackspira.kristiawan.rackmonthpicker.RackMonthPicker;
import com.rackspira.kristiawan.rackmonthpicker.listener.DateMonthDialogListener;
import com.rackspira.kristiawan.rackmonthpicker.listener.OnCancelMonthDialogListener;

import android.widget.ArrayAdapter;

import android.widget.ListView;
import android.widget.Spinner;

import android.widget.LinearLayout;

import static android.content.Context.INPUT_METHOD_SERVICE;


/**
 * Created by ericapantoja on 10/17/17.
 */

public class ReceiptLookUp extends Fragment{
    Button SearchBtn;
    RecycleAdapter recycler, recycler2;
    ListView listView;
    SearchView SearchView;
    EditText DateSearch;
    RecyclerView recyclerView;

    Spinner spinerCat;
    //LinearLayout categoryContainer;
    Context ctx;
    private List<String> defaultCategorySuggestions = Arrays.asList("Groceries", "Rent", "Phone", "Car insurance", "Gas", "Utilities", "Books"
            , "Free spending", "Emergency", "Category");


    boolean successfulSearch;

    //ArrayList<ReceiptModel> rm=new ArrayList<>();


    //Assigns activity to UploadReceipt context once Home is attached
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context != null) {
            ctx = context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ctx = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returns layout file
        View v = inflater.inflate(R.layout.receipt_look_up, container, false);



        return v;
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        //view initialization

        SearchBtn = (Button) v.findViewById(R.id.findBtn);
        //SearchView = (SearchView) v.findViewById(R.id.SearchView);
        spinerCat = (Spinner) v.findViewById(R.id.spinnerCategories);
        //listView = (ListView) v.findViewById(R.id.listView);
        DateSearch = (EditText) v.findViewById(R.id.eTxt_Date);
        recyclerView = (RecyclerView) v.findViewById(R.id.recycle);
        successfulSearch = false;

    }


    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        setAdapter(spinerCat);

        //datePicker

        //creates the month/year picker and sets it to the budgetMonth edittext
        final RackMonthPicker rackMonthPicker = new RackMonthPicker(ctx)
                .setPositiveButton(new DateMonthDialogListener() {
                    @Override
                    public void onDateMonth(int month, int startDate, int endDate, int year, String monthLabel) {

                        //creates a list of months so that int month can be converted to a month string
                        List<String> monthList = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

                        String m = String.format("%s", monthList.get(month - 1));
                        String y = String.format("%s", year);
                        String result = m + "/" + y;
                        DateSearch.setText(result);
                    }
                })
                .setNegativeButton(new OnCancelMonthDialogListener() {
                    @Override
                    public void onCancel(AlertDialog alertDialog) {
                        alertDialog.dismiss();
                    }
                });

        final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                String year = String.format("%s", i);
                String month = String.format("%s", i1);
                String day = String.format("%s", i2);

                String formattedDate = month + "/" + day + "/" + year;
                DateSearch.setText(formattedDate);
            }
        };

        DateSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view){

                try {
                    InputMethodManager imm = (InputMethodManager)ctx.getSystemService(INPUT_METHOD_SERVICE);
                    if(imm != null && getActivity().getCurrentFocus() != null) {
                        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
                rackMonthPicker.show();
                /*Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                DatePickerDialog dialog = new DatePickerDialog(ctx, onDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();*/
            }

        });




        //onclick method for login button
        //takes you to home page

        SearchBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view){
                SQLiteHelper sqLiteHelper = new SQLiteHelper(getActivity().getApplicationContext());
                ArrayList<String> tableNames = sqLiteHelper.getTableNames();

                RecyclerView.LayoutManager reLayoutManager =new LinearLayoutManager(getActivity().getApplicationContext());
                recyclerView.setLayoutManager(reLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());

                //this is the list for the rows in the database that cointains the key word
                ArrayList<ReceiptModel> data=new ArrayList<ReceiptModel>();
                if (tableNames.size() > 1) {
                    for (int j = 0; j < tableNames.size(); j++) {
                        if (tableNames.get(j).contains("receipt")) {
                            ArrayList<ReceiptModel> budgetModels = sqLiteHelper.getReceiptRecords(tableNames.get(j));
                            if (budgetModels.size() > 0) {
                                ReceiptModel budgetModel;
                                String dateSearched = DateSearch.getText().toString();
                                String text = spinerCat.getSelectedItem().toString();
                                String[] monthAndYear = dateSearched.split("/");
                                String monthNum = monthToString(monthAndYear[0]);
                                //String formattedDateToSearch = monthAndYear[0] + "/" + monthAndYear[1];
                                for (int i = 0; i < budgetModels.size(); i++) {
                                    budgetModel = budgetModels.get(i);
                                    String category1 = budgetModel.getCategory();
                                    String date = budgetModel.getDate();
                                    String[] match = date.split("/");

                                    if(!text.isEmpty() && dateSearched.isEmpty()) {
                                        if(category1.equals(text)) {
                                            data.add(budgetModel);
                                        }
                                    }

                                    else if(text.equals("Category") && !dateSearched.isEmpty()){
                                        if(match[0].equals(monthNum) && match[2].equals(monthAndYear[1])){
                                            data.add(budgetModel);
                                        }
                                    }

                                    else if(!text.isEmpty() && !dateSearched.isEmpty()){
                                        if(match[0].equals(monthNum) && match[2].equals(monthAndYear[1]) && category1.equals(text)){
                                            data.add(budgetModel);
                                        }
                                    }
                                }

                            }
                        }
                    }
                    if(data.size() > 0){
                        recycler =new RecycleAdapter(data, ReceiptLookUp.this);
                        recyclerView.setAdapter(recycler);
                    }

                    else {
                        Toast.makeText(getActivity(), "Receipt not found!",
                                Toast.LENGTH_LONG).show();
                    }
                }


            }





        });
    }

    private void setAdapter(Spinner categorySpinner){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_dropdown_item, defaultCategorySuggestions){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount()-1; // you dont display last item. It is used as hint.
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categorySpinner.setAdapter(adapter);

        categorySpinner.setSelection(adapter.getCount());
    }

    private String monthToString(String month){
        if(month.equals("January")){
            return "1";
        }
        else if(month.equals("February")){
            return "2";
        }
        else if(month.equals("March")){
            return "3";
        }
        else if(month.equals("April")){
            return "4";
        }
        else if(month.equals("May")){
            return "5";
        }
        else if(month.equals("June")){
            return "6";
        }
        else if(month.equals("July")){
            return "7";
        }
        else if(month.equals("August")){
            return "8";
        }
        else if(month.equals("September")){
            return "9";
        }
        else if(month.equals("October")){
            return "10";
        }
        else if(month.equals("November")){
            return "11";
        }
        else{
            return "12";
        }
    }
}













