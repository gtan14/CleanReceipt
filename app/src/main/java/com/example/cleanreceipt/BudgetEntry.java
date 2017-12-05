package com.example.cleanreceipt;

import android.content.Context;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.rackspira.kristiawan.rackmonthpicker.RackMonthPicker;
import com.rackspira.kristiawan.rackmonthpicker.listener.DateMonthDialogListener;
import com.rackspira.kristiawan.rackmonthpicker.listener.OnCancelMonthDialogListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Gerald on 9/26/2017.
 */

public class BudgetEntry extends Fragment {

    TextView budgetTotal;
    EditText budgetMonth;
    Button addCategory, cancel, save;
    ScrollView scrollbar;

    LinearLayout categoryContainer;
    Context ctx;

    double total;


    private SQLiteHelper sqlHelper;
    private String recyclerTableName;
    private List<String> defaultCategorySuggestions = Arrays.asList("Groceries", "Rent", "Phone", "Car insurance", "Gas", "Utilities", "Books"
            , "Free spending", "Emergency", "Category");


    public BudgetEntry(){

    }
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
        View v = inflater.inflate(R.layout.budget_setup, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {

        //view initialization
        categoryContainer = (LinearLayout) v.findViewById(R.id.categoryContainer);
        budgetMonth = (EditText) v.findViewById(R.id.budgetMonth);
        budgetTotal = (TextView) v.findViewById(R.id.totalBudget);
        addCategory = (Button) v.findViewById(R.id.addCategory);
        cancel = (Button) v.findViewById(R.id.cancelBudgetSetup);
        save = (Button) v.findViewById(R.id.saveBudgetSetup);
        scrollbar = (ScrollView) v.findViewById(R.id.scrollbar);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        SharedPreferences prefs = ctx.getSharedPreferences("recyclerItemName", MODE_PRIVATE);
        recyclerTableName = prefs.getString("name", null);
        prefs.edit().clear().apply();

        sqlHelper = new SQLiteHelper(ctx);

        if(recyclerTableName == null) {
            getActivity().setTitle("Budget Setup");
        }

        else{
            getActivity().setTitle(recyclerTableName);

            budgetMonth.setText(recyclerTableName.replace(" ", " / "));

            String formattedTableName = recyclerTableName.replace(" ", "") + "Month";

            ArrayList<String> tableNames = sqlHelper.getTableNames();

            if(tableNames.size() > 1) {
                LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                for (int j = 0; j < tableNames.size(); j++) {
                    if (tableNames.get(j).equals(formattedTableName)) {
                        ArrayList<BudgetModel> budgetModelList = sqlHelper.getBudgetRecords(tableNames.get(j));
                        if(budgetModelList.size() > 0) {
                            BudgetModel budgetModel;
                            for (int i = 0; i < budgetModelList.size(); i++) {
                                budgetModel = budgetModelList.get(i);
                                String categoryString = budgetModel.getCategory();
                                String priceString = budgetModel.getPrice();
                                final View categoryView = layoutInflater.inflate(R.layout.category, null);
                                ImageButton deleteCategory = (ImageButton) categoryView.findViewById(R.id.deleteCategoryRow);
                                Spinner category = (Spinner) categoryView.findViewById(R.id.categorySpinner);
                                EditText price = (EditText) categoryView.findViewById(R.id.budgetEditText);
                                price.setText(priceString);
                                setAdapter(defaultCategorySuggestions, category, categoryString);
                                deleteCategory(deleteCategory, price, categoryView);
                                categoryContainer.addView(categoryView);
                            }
                        }
                        Update();
                    }
                }

            }
        }


        //creates the month/year picker and sets it to the budgetMonth edittext
        final RackMonthPicker rackMonthPicker = new RackMonthPicker(ctx)
                .setPositiveButton(new DateMonthDialogListener() {
                    @Override
                    public void onDateMonth(int month, int startDate, int endDate, int year, String monthLabel) {

                        //creates a list of months so that int month can be converted to a month string
                        List<String> monthList = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

                        String m = String.format("%s", monthList.get(month - 1));
                        String y = String.format("%s", year);
                        String result = m + " / " + y;
                        budgetMonth.setText(result);
                    }
                })
                .setNegativeButton(new OnCancelMonthDialogListener() {
                    @Override
                    public void onCancel(AlertDialog alertDialog) {
                        alertDialog.dismiss();
                    }
                });

        budgetMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(scrollbar.getWindowToken(), 0);
                rackMonthPicker.show();
            }
        });

        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View categoryInput = layoutInflater.inflate(R.layout.category, null);
                ImageButton deleteCategory = (ImageButton) categoryInput.findViewById(R.id.deleteCategoryRow);
                Spinner categorySpinner = (Spinner) categoryInput.findViewById(R.id.categorySpinner);
                final EditText et = (EditText) categoryInput.findViewById(R.id.budgetEditText);
                deleteCategory(deleteCategory, et, categoryInput);


                setAdapter(defaultCategorySuggestions, categorySpinner, null);


                    categoryContainer.addView(categoryInput);
                    final TextWatcher textwatcher = new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            Update();
                        }
                    };
                    et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            et.addTextChangedListener(textwatcher);
                        }
                    });


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(scrollbar.getWindowToken(), 0);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(scrollbar.getWindowToken(), 0);

                if(recyclerTableName != null){
                    String tableName = recyclerTableName.replace(" ", "") + "Month";
                    String budgetTotalTable = tableName.replace("Month", "BudgetTotal");
                    sqlHelper.deleteTable(tableName);
                    sqlHelper.deleteTable(budgetTotalTable);
                    sqlHelper.createBudgetTable(tableName);
                    sqlHelper.createBudgetTotalTable(budgetTotalTable);
                    insertToBudgetTotalTable(budgetTotalTable);
                    insertData(tableName);
                    goToBudgetReviewPage();
                }

                else {
                    if (!budgetMonth.getText().toString().isEmpty()) {

                        //Add info to DB
                        final String tableName = budgetMonth.getText().toString().replace(" ", "").replace("/", "") + "Month";
                        final String budgetTotalTable = tableName.replace("Month", "BudgetTotal");
                        boolean tableExists = sqlHelper.tableExists(tableName);

                        //if there already exists a budget under that month and year, display alert dialog asking if user wants to override
                        if (tableExists) {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
                            dialog.setMessage("Override existing budget?");
                            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    sqlHelper.deleteTable(tableName);
                                    sqlHelper.deleteTable(budgetTotalTable);
                                    sqlHelper.createBudgetTable(tableName);
                                    sqlHelper.createBudgetTotalTable(budgetTotalTable);
                                    insertData(tableName);
                                    insertToBudgetTotalTable(budgetTotalTable);
                                    goToBudgetReviewPage();
                                }
                            });
                            dialog.show();
                        } else {
                            sqlHelper.createBudgetTable(tableName);
                            sqlHelper.createBudgetTotalTable(budgetTotalTable);
                            insertData(tableName);
                            insertToBudgetTotalTable(budgetTotalTable);
                            goToBudgetReviewPage();
                        }

                    }
                }
            }
        });
    }

    //method to change to the budget review fragment
    private void goToBudgetReviewPage(){
        Fragment fragment = new BudgetHistory();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment, "FragmentBudgetHistory");
        ft.addToBackStack(getClass().getName());
        ft.commit();
    }

    //method to insert data into sqlite db
    //checks to see if the category container has any children
    //if it does, it loops through the children and gets the category and budget for each child and inserts it into db
    private void insertData(String tableName){
        int categoryContainerChildrenCount = categoryContainer.getChildCount();

        if(categoryContainerChildrenCount > 0) {
            for (int i = 0; i < categoryContainerChildrenCount; i++) {
                View v = categoryContainer.getChildAt(i);
                Spinner categories = (Spinner) v.findViewById(R.id.categorySpinner);
                EditText budget = (EditText) v.findViewById(R.id.budgetEditText);
                BudgetModel budgetModel = new BudgetModel();
                budgetModel.setCategory(categories.getSelectedItem().toString());
                budgetModel.setPrice(budget.getText().toString());
                sqlHelper.insertRecordBudget(budgetModel, tableName);
            }
        }
    }

    private void insertToBudgetTotalTable(String tableName){
        String total = budgetTotal.getText().toString().substring(7, budgetTotal.getText().length() - 1);
        BudgetModel budgetModel = new BudgetModel();
        budgetModel.setPrice(total);
        sqlHelper.insertRecordBudgetTotal(budgetModel, tableName);
    }

    void Update() {
        int count = categoryContainer.getChildCount();

        //Reset total to 0 to recalculate
        total = 0;

        try {
            for (int i = 0; i < count; i++) {
                //Get child view
                View v = categoryContainer.getChildAt(i);

                //Get elements of child view
                EditText budgetTemp = (EditText) v.findViewById(R.id.budgetEditText);
                EditText catTemp = (EditText) v.findViewById(R.id.categoryEditText);
                //String cat = catTemp.getText() + "";

                String budgetString = budgetTemp.getText().toString();

                //if there is a budget entered, add it to total
                if (!budgetString.equals("")){
                    Double budget = Double.parseDouble(budgetTemp.getText().toString());
                    total += budget;
                }

            }
        } catch (Exception ex) {
            Log.e("upTotal", ex.toString());
        }

        String t = "Total: " + total;
        budgetTotal.setText(t);
    }

    private void deleteCategory(ImageButton delete, final EditText editText, final View categoryInput) {
        View.OnClickListener deleteCategoryListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText.getText().toString().length() > 0){
                    String budgetString = budgetTotal.getText().toString().substring(7, budgetTotal.getText().length() - 1);
                    Double budgetTot = Double.parseDouble(budgetString);
                    Double budg = Double.parseDouble(editText.getText().toString());
                    Double newBudgetTot = budgetTot - budg;
                    String newBudgetTotalText = "Total: " + String.format("%s", newBudgetTot);
                    budgetTotal.setText(newBudgetTotalText);
                }
                categoryContainer.removeView(categoryInput);
            }
        };
        delete.setOnClickListener(deleteCategoryListener);
    }

    private void setAdapter(List<String> list, Spinner categorySpinner, String categoryString){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_dropdown_item, list){

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

        if(categoryString != null){
            for(int i = 0; i < list.size(); i++){
                if(categoryString.equals(list.get(i))){
                    categorySpinner.setSelection(i);
                }
            }
        }

        else {
            categorySpinner.setSelection(adapter.getCount());
        }
    }


}




