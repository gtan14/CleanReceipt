//Created by Ryan Drumm 10/7/2017

package com.example.cleanreceipt;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class HomeActivity extends Fragment {

    private Button login, receiptEntry, receiptHistory, budget;
    private TableLayout tableLayout;
    GraphView graph;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returns layout file
        View v = inflater.inflate(R.layout.activity_home, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        graph = (GraphView) v.findViewById(R.id.graph);
        tableLayout = (TableLayout) v.findViewById(R.id.tableLayout);
    }


    @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("Home");


        //Make dates

        ArrayList<String> recentMonthsToDisplay = lastYearFromOldestToRecent();
        ArrayList<DataPoint> points = new ArrayList<DataPoint>();
        ArrayList<Date> dateArrayList = new ArrayList<>();

        SQLiteHelper sqLiteHelper = new SQLiteHelper(getActivity());
        ArrayList<String> tableNames = sqLiteHelper.getTableNames();
        ArrayList<String> remainingBudgetList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        String m = new SimpleDateFormat("MMMM").format(cal.getTime());
        String year = String.format("%s", cal.get(Calendar.YEAR));
        String possibleTable = m + year + "Month";


        if (tableNames.size() > 1) {
            for (int j = 0; j < tableNames.size(); j++) {
                if (tableNames.get(j).equals(possibleTable)) {
                    ArrayList<BudgetModel> budgetModels = sqLiteHelper.getBudgetRecords(tableNames.get(j));
                    if (budgetModels.size() > 0) {
                        BudgetModel budgetModel;
                        for (int i = 0; i < budgetModels.size(); i++) {
                            budgetModel = budgetModels.get(i);
                            remainingBudgetList.add(budgetModel.getCategory() + ": " + budgetModel.getPrice());
                        }
                    }
                }
            }
        }

        //if table in db contains word month, this means that it is a budget
        //get the categories and prices for each budget, and add to list
        for(int i = 0; i < recentMonthsToDisplay.size(); i++) {
            if (tableNames.size() > 1) {
                double budgetSpent = 0.0;
                double budgetTotal = 0.0;
                String totalBudgetTable = recentMonthsToDisplay.get(i).replace("Month", "BudgetTotal");
                for (int j = 0; j < tableNames.size(); j++) {
                    if (tableNames.get(j).equals(recentMonthsToDisplay.get(i))) {
                        ArrayList<BudgetModel> budgetModel = sqLiteHelper.getBudgetRecords(tableNames.get(j));
                        if (budgetModel.size() > 0) {
                            BudgetModel budgetModel1;
                            for (int a = 0; a < budgetModel.size(); a++) {
                                budgetModel1 = budgetModel.get(a);
                                String price = budgetModel1.getPrice();
                                budgetSpent += Double.parseDouble(price);
                            }
                        }
                    }

                    else if(tableNames.get(j).equals(totalBudgetTable)){
                        ArrayList<BudgetModel> budgetModel = sqLiteHelper.getBudgetTotalRecords(tableNames.get(j));
                        if (budgetModel.size() > 0) {
                            BudgetModel budgetModel1;
                            for (int a = 0; a < budgetModel.size(); a++) {
                                budgetModel1 = budgetModel.get(a);
                                String price = budgetModel1.getPrice();
                                budgetTotal += Double.parseDouble(price);
                            }

                            int monthAsInt = monthToInt(totalBudgetTable.substring(0, totalBudgetTable.indexOf("2")));
                            Calendar calendar = Calendar.getInstance();
                            calendar.clear();
                            calendar.set(Calendar.MONTH, monthAsInt);
                            calendar.set(Calendar.YEAR, Integer.parseInt(totalBudgetTable.substring(totalBudgetTable.indexOf("2"), totalBudgetTable.indexOf("B"))));
                            Date date = calendar.getTime();
                            dateArrayList.add(date);

                            double percent = ((budgetTotal - budgetSpent) / budgetTotal) * 100;
                            points.add(new DataPoint(date, percent));

                            budgetTotal = 0.0;
                            budgetSpent = 0.0;
                        }
                    }
                }
            }
        }


        DataPoint[] dataPointArray = points.toArray(new DataPoint[points.size()]);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPointArray);
        graph.addSeries(series);

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

// set manual x bounds to have nice steps
        if(dateArrayList.size() > 0) {
            double month = Math.pow(2.628, 9);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(dateArrayList.get(0).getTime());


            graph.getViewport().setMinX(dateArrayList.get(0).getTime() - month);
            graph.getViewport().setMaxX(dateArrayList.get(dateArrayList.size() - 1).getTime() - Math.pow(2.628, 9));
            graph.getViewport().setXAxisBoundsManual(true);
        }

        //y bounds as well
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);

// as we use dates as labels, the human rounding to nice readable numbers
// is not necessary
        graph.getGridLabelRenderer().setHumanRounding(false);

        for(int i = 0; i < remainingBudgetList.size(); i++){
            TableRow tableRow = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.table_row, null);
            ((TextView)tableRow.findViewById(R.id.catAndPriceTableRow)).setText(remainingBudgetList.get(i));
            tableLayout.addView(tableRow);
        }
    }

    private ArrayList<String> lastYearFromOldestToRecent() {
        SQLiteHelper sqLiteHelper = new SQLiteHelper(getActivity());
        ArrayList<String> tableNames = sqLiteHelper.getTableNames();
        ArrayList<String> dates = new ArrayList<>();

        //if table in db contains word month, this means that it is a budget
        //get the categories and prices for each budget, and add to list
        if(tableNames.size() > 1){
            for(int j = 0; j < tableNames.size(); j++){
                if(tableNames.get(j).contains("Month")){

                    //formats the table name so that it will display as Month yyyy for the title of each expandable item
                    String formattedTableName = tableNames.get(j).replace("Month", "");
                    int indexOfYear = formattedTableName.indexOf("2");
                    formattedTableName = formattedTableName.substring(0, indexOfYear) + " " + formattedTableName.substring(indexOfYear, formattedTableName.length());
                    dates.add(formattedTableName);
                }
            }
        }

        Collections.sort(dates, new StringDateComparator());
        for(int i = 0; i < dates.size(); i++){
            String newString = dates.get(i).replace(" ", "") + "Month";
            dates.set(i, newString);
        }
        return dates;
    }

    class StringDateComparator implements Comparator<String>
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
        public int compare(String string, String string1)
        {
            try {
                return dateFormat.parse(string).compareTo(dateFormat.parse(string1));
            }
            catch (ParseException e){

            }
            return 0;
        }
    }

    private int monthToInt(String month){
        if(month.equals("January")){
            return 1;
        }
        else if(month.equals("February")){
            return 2;
        }
        else if(month.equals("March")){
            return 3;
        }
        else if(month.equals("April")){
            return 4;
        }
        else if(month.equals("May")){
            return 5;
        }
        else if(month.equals("June")){
            return 6;
        }
        else if(month.equals("July")){
            return 7;
        }
        else if(month.equals("August")){
            return 8;
        }
        else if(month.equals("September")){
            return 9;
        }
        else if(month.equals("October")){
            return 10;
        }
        else if(month.equals("November")){
            return 11;
        }
        else{
            return 12;
        }
    }

}
