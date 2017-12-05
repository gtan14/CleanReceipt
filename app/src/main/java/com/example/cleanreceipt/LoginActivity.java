//Create by Ryan Drumm 10/7/2017
package com.example.cleanreceipt;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity{

    private Button loginBtn;
    private Button signup;
    EditText username;
    EditText password;
    boolean successfulLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //view initialization

        signup = (Button) findViewById(R.id.signUpBtn);
        username = (EditText) findViewById(R.id.usernameEditText);
        password = (EditText) findViewById(R.id.passwordEditText);
        successfulLogin = false;

        setTitle("Login");

        //deleteDatabase("SQLiteDatabase.db");
    }

    //onclick method for login button
    //takes you to home page
    public void loginOnClick(View view) {
        SQLiteHelper sqLiteHelper = new SQLiteHelper(getApplicationContext());
        ArrayList<String> tableNames = sqLiteHelper.getTableNames();

        if(tableNames.size() > 1){
            for(int j = 0; j < tableNames.size(); j++){
                if(tableNames.get(j).contains("signup")){
                    ArrayList<LoginModel> loginModels = sqLiteHelper.getLoginRecords(tableNames.get(j));
                    if(loginModels.size() > 0){
                        LoginModel loginModel;
                        for(int i = 0; i < loginModels.size(); i++){
                            loginModel = loginModels.get(i);
                            String name = loginModel.getUsername();
                            String pass = loginModel.getPassword();

                            if(name.equals(username.getText().toString()) && pass.equals(password.getText().toString())){
                                successfulLogin = true;
                            }
                        }
                    }
                }
            }
        }

        if(successfulLogin){
            Intent intent = new Intent(this, NavDrawer.class);
            startActivity(intent);
        }
        else{
            Toast toast = Toast.makeText(this, "Invalid login", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //onclick method for signup button
    //takes you to signup page
    public void signupOnClick(View view){
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }


}
