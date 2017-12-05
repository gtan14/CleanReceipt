package com.example.cleanreceipt;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class BudgetHistory extends Fragment{

    private String month;
    private RecyclerView recyclerView;
    private ArrayList<String> categories;
    private ArrayList<Double> prices;
    private ArrayList<BudgetModel> budgets;
    private SQLiteHelper sqLiteHelper;
    private List<Budget> budgetList;
    private BudgetHistoryAdapter budgetHistoryAdapter;

    Context ctx;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_budget_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.budget_history_recyclerview);
    }


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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("Budget History");

        //get all the table names in db and initialize the list to be displayed in recycler view
        SQLiteHelper sqLiteHelper = new SQLiteHelper(getActivity());
        ArrayList<String> tableNames = sqLiteHelper.getTableNames();
        budgetList = new ArrayList<>();

        //if table in db contains word month, this means that it is a budget
        //get the categories and prices for each budget, and add to list
        if(tableNames.size() > 1){
            for(int j = 0; j < tableNames.size(); j++){
                if(tableNames.get(j).contains("Month")){
                    List<Categories> categoriesList = new ArrayList<>();
                    ArrayList<BudgetModel> budgetModel = sqLiteHelper.getBudgetRecords(tableNames.get(j));
                    if(budgetModel.size() > 0){
                        BudgetModel budgetModel1;
                        for(int i = 0; i < budgetModel.size(); i++){
                            budgetModel1 = budgetModel.get(i);
                            String category = budgetModel1.getCategory();
                            String price = "$" + budgetModel1.getPrice();
                            Categories categories = new Categories(category, price);
                            categoriesList.add(categories);
                        }
                    }

                    //formats the table name so that it will display as Month yyyy for the title of each expandable item
                    String formattedTableName = tableNames.get(j).replace("Month", "");
                    int indexOfYear = formattedTableName.indexOf("2");
                    formattedTableName = formattedTableName.substring(0, indexOfYear) + " " + formattedTableName.substring(indexOfYear, formattedTableName.length());

                    //add the budget with the table name as the title and the categories and prices as the items when expanded to the list which will display in recycler view
                    Budget budget = new Budget(formattedTableName, categoriesList);
                    budgetList.add(budget);
                }
            }
        }


        //sets up recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        Collections.sort(budgetList, new StringDateComparator());
        budgetHistoryAdapter = new BudgetHistoryAdapter(budgetList, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(budgetHistoryAdapter);

    }

    class StringDateComparator implements Comparator<Budget>
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
        public int compare(Budget budget, Budget budget1)
        {
            try {
                return dateFormat.parse(budget1.getTitle()).compareTo(dateFormat.parse(budget.getTitle()));
            }
            catch (ParseException e){

            }
            return 0;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.budget_history_long_click, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        SharedPreferences prefs = ctx.getSharedPreferences("recyclerItemPos", MODE_PRIVATE);
        SharedPreferences prefs1 = ctx.getSharedPreferences("recyclerItemName", MODE_PRIVATE);
        String tableName = prefs1.getString("name", null);
        int position = prefs.getInt("pos", -1);

        switch (item.getItemId()) {
            case R.id.edit:
                Fragment fragment = new BudgetEntry();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment, "FragmentBudgetEntry");
                ft.addToBackStack(getClass().getName());
                ft.commit();
                return true;
            case R.id.delete:
                if(position != -1 && tableName != null) {
                    String formattedName = tableName.replace(" ", "");
                    budgetHistoryAdapter.removeChild(position, formattedName);
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

}
