package com.example.cleanreceipt;

import android.content.SharedPreferences;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static android.view.animation.Animation.RELATIVE_TO_SELF;

/**
 * Created by Gerald on 11/28/2017.
 */

public class BudgetHistoryAdapter extends ExpandableRecyclerViewAdapter <BudgetHistoryAdapter.DateViewHolder, BudgetHistoryAdapter.IndividualCategoryViewHolder> {

    private BudgetHistory activity;
    private List<? extends ExpandableGroup> list;
    private int position;

    public BudgetHistoryAdapter(List<? extends ExpandableGroup> groups, BudgetHistory activity){
        super(groups);
        list = groups;
        this.activity = activity;
    }

    public class DateViewHolder extends GroupViewHolder{

        private TextView date;
        private ImageView expandableArrow;
        private View view;

        public DateViewHolder(View view){
            super(view);
            date = (TextView) view.findViewById(R.id.dateExpandableGroup);
            expandableArrow = (ImageView) view.findViewById(R.id.expandableArrow);
            this.view = view;
        }


        public void setLongClick(final int position){
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    activity.registerForContextMenu(view);
                    SharedPreferences.Editor editor = activity.ctx.getSharedPreferences("recyclerItemPos", MODE_PRIVATE).edit();
                    SharedPreferences.Editor editor1 = activity.ctx.getSharedPreferences("recyclerItemName", MODE_PRIVATE).edit();
                    editor.putInt("pos", position);
                    editor.apply();
                    editor1.putString("name", date.getText().toString());
                    editor1.apply();
                    return false;
                }
            });
        }
        public void setDateTitle(ExpandableGroup group){
            date.setText(group.getTitle());
        }

        @Override
        public void expand() {
            animateExpand();
        }

        @Override
        public void collapse() {
            animateCollapse();
        }

        private void animateExpand() {
            RotateAnimation rotate =
                    new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            expandableArrow.setAnimation(rotate);
        }

        private void animateCollapse() {
            RotateAnimation rotate =
                    new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            expandableArrow.setAnimation(rotate);
        }
    }

    public class IndividualCategoryViewHolder extends ChildViewHolder{

        private TextView category;
        private TextView price;

        public IndividualCategoryViewHolder(View view){
            super(view);
            category = (TextView) view.findViewById(R.id.categoryExpandableChildTextView);
            price = (TextView) view.findViewById(R.id.priceExpandableChildTextView);
        }

        public void setCategoryPrice(String category, String price){
            this.category.setText(category);
            this.price.setText(price);
        }
    }


    @Override
    public DateViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expandable_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public IndividualCategoryViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_expandable_child, parent, false);
        return new IndividualCategoryViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(IndividualCategoryViewHolder holder, int flatPosition, ExpandableGroup group,
                                      int childIndex) {
        final Categories categories = ((Budget) group).getItems().get(childIndex);
        holder.setCategoryPrice(categories.getCategory(), categories.getPrice());
    }

    @Override
    public void onBindGroupViewHolder(DateViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {
        holder.setDateTitle(group);
        holder.setLongClick(flatPosition);
    }

    public void removeChild(int index, String tableName) {
        SQLiteHelper sqLiteHelper = new SQLiteHelper(activity.ctx);
        sqLiteHelper.deleteTable(tableName + "BudgetTable");
        sqLiteHelper.deleteTable(tableName + "Month");
        ArrayList<String> tableNames = sqLiteHelper.getTableNames();
        String title = list.get(index).getTitle();
        String[] titleArray = title.split(" ");
        String monthNum = monthToString(titleArray[0]);

        //this is the list for the rows in the database that cointains the key word
        ArrayList<ReceiptModel> data = new ArrayList<ReceiptModel>();
        if (tableNames.size() > 1) {
            for (int j = 0; j < tableNames.size(); j++) {
                if (tableNames.get(j).contains("receipt")) {
                    ArrayList<ReceiptModel> budgetModels = sqLiteHelper.getReceiptRecords(tableNames.get(j));
                    if (budgetModels.size() > 0) {
                        ReceiptModel budgetModel;

                        //String formattedDateToSearch = monthAndYear[0] + "/" + monthAndYear[1];
                        for (int i = 0; i < budgetModels.size(); i++) {
                            budgetModel = budgetModels.get(i);
                            String category1 = budgetModel.getCategory();
                            String date = budgetModel.getDate();
                            String[] match = date.split("/");

                            if (match[0].equals(monthNum) && match[2].equals(titleArray[1])) {
                                sqLiteHelper.deleteTable(tableNames.get(j));
                            }

                        }
                    }
                }
            }
        }

        list.remove(index);
        this.notifyDataSetChanged();
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
