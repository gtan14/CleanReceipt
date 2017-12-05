package com.example.cleanreceipt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.stetho.Stetho;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NavDrawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Stetho.initializeWithDefaults(this);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        SQLiteHelper sqLiteHelper = new SQLiteHelper(this.getApplicationContext());
        ArrayList<String> tableNames = sqLiteHelper.getTableNames();
        double remainingBudget = 0.0;
        Calendar cal = Calendar.getInstance();
        String month = new SimpleDateFormat("MMMM").format(cal.getTime());
        String year = String.format("%s", cal.get(Calendar.YEAR));
        String possibleTable = month + year + "Month";
        String name = "";

        //this is the list for the rows in the database that cointains the key word
        ArrayList<ReceiptModel> data=new ArrayList<ReceiptModel>();
        if (tableNames.size() > 1) {
            for (int j = 0; j < tableNames.size(); j++) {
                if (tableNames.get(j).equals(possibleTable)) {
                    ArrayList<BudgetModel> budgetModels = sqLiteHelper.getBudgetRecords(tableNames.get(j));
                    if (budgetModels.size() > 0) {
                        BudgetModel budgetModel;
                        for (int i = 0; i < budgetModels.size(); i++) {
                            budgetModel = budgetModels.get(i);
                            remainingBudget += Double.parseDouble(budgetModel.getPrice());
                        }
                        }
                    }
                    else if(tableNames.get(j).contains("signup")){
                        ArrayList<LoginModel> loginModels = sqLiteHelper.getLoginRecords(tableNames.get(j));
                        if(loginModels.size() > 0){
                            LoginModel loginModel;
                            for(int i = 0; i< loginModels.size(); i++){
                                loginModel = loginModels.get(i);
                                name = loginModel.getFirstName();
                            }
                        }
                    }
                }
            }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);

        TextView nameTextView = (TextView) header.findViewById(R.id.profileName);
        TextView remainingBudgetTextView = (TextView) header.findViewById(R.id.remainingBudget);

        nameTextView.setText(name);

        remainingBudgetTextView.setText("Remaining budget: " + remainingBudget);
        navigationView.setNavigationItemSelectedListener(this);


        Fragment home = new HomeActivity();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, home, "FragmentHome");
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        Fragment home = null;
        Fragment budgetSetup = null;
        Fragment budgetReview = null;
        Fragment receiptSetup = null;
        Fragment receiptReview = null;
        boolean loginActivity = false;

        int id = item.getItemId();

        switch(id){
            case R.id.home:
                home = new HomeActivity();
                break;
            case R.id.budgetSetup:
                budgetSetup = new BudgetEntry();
                break;
            case R.id.budgetReview:
                budgetReview = new BudgetHistory();
                break;
            case R.id.receiptSetup:
                receiptSetup = new UploadReceipt();
                break;
            case R.id.receiptReview:
                receiptReview = new ReceiptLookUp();
                break;
            case R.id.logout:
                loginActivity = true;
                break;
        }

        if(home != null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, home, "FragmentHome");
            ft.addToBackStack(null);
            ft.commit();
        }

        if(budgetSetup != null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, budgetSetup, "FragmentBudgetSetup");
            ft.addToBackStack(null);
            ft.commit();
        }

        if(budgetReview != null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, budgetReview, "FragmentBudgetReview");
            ft.addToBackStack(null);
            ft.commit();
        }

        if(receiptSetup != null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, receiptSetup, "FragmentReceiptSetup");
            ft.addToBackStack(null);
            ft.commit();
        }

        if(receiptReview != null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, receiptReview, "FragmentReceiptReview");
            ft.addToBackStack(null);
            ft.commit();
        }

        if(loginActivity){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Logout?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent a = new Intent(NavDrawer.this, LoginActivity.class);
                    startActivity(a);
                    (NavDrawer.this).overridePendingTransition(0,0);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
