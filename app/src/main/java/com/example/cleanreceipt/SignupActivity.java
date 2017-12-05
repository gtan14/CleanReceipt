package com.example.cleanreceipt;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import static com.example.cleanreceipt.R.id.passwordTextInputLayout;

/**
 * Created by Gerald on 10/10/2017.
 */

public class SignupActivity extends AppCompatActivity {

    Button cancelSignup;
    Button confirmSignup;
    TextInputLayout reenterPassTextInputLayout;
    TextInputLayout passwordTextInputlayout;
    EditText username;
    EditText firstName;
    EditText lastName;
    EditText password;
    EditText reenterPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setTitle("Signup");
        cancelSignup = (Button) findViewById(R.id.cancelSignup);
        confirmSignup = (Button) findViewById(R.id.signupConfirm);
        reenterPassTextInputLayout = (TextInputLayout) findViewById(R.id.reenterPassTextInputLayout);
        passwordTextInputlayout = (TextInputLayout) findViewById(passwordTextInputLayout);
        password = (EditText) findViewById(R.id.passwordSignup);
        reenterPassword = (EditText) findViewById(R.id.reenterPasswordSignup);
        username = (EditText) findViewById(R.id.usernameSignup);
        firstName = (EditText) findViewById(R.id.firstNameSignup);
        lastName = (EditText) findViewById(R.id.lastNameSignup);
    }

    @Override
    public void onResume(){
        super.onResume();

        reenterPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                reenterPassTextInputLayout.setError(null);
                reenterPassTextInputLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordTextInputlayout.setError(null);
                passwordTextInputlayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        cancelSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        confirmSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int tableNum = 1;
                String tableName = "signup" + String.format("%s", tableNum);
                boolean successfulSignup = true;
                if(password.getText().toString().length() == 0){
                    passwordTextInputlayout.setErrorEnabled(true);
                    passwordTextInputlayout.setError("Enter a password");
                    successfulSignup = false;
                }
                if(!password.getText().toString().equals(reenterPassword.getText().toString()) && password.getText().toString().length() > 0){
                    reenterPassTextInputLayout.setErrorEnabled(true);
                    reenterPassTextInputLayout.setError("Password does not match");
                    successfulSignup = false;
                }
                if(successfulSignup){
                    SQLiteHelper sqLiteHelper = new SQLiteHelper(getApplicationContext());
                    while (sqLiteHelper.tableExists(tableName)) {
                        ++tableNum;
                        tableName = "signup" + String.format("%s", tableNum);
                    }
                    sqLiteHelper.createSignupTable(tableName);

                    LoginModel loginModel = new LoginModel();
                    loginModel.setUsername(username.getText().toString());
                    loginModel.setPassword(password.getText().toString());
                    loginModel.setFirstName(firstName.getText().toString());
                    loginModel.setLastName(lastName.getText().toString());

                    sqLiteHelper.insertRecordSignup(loginModel, tableName);

                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
