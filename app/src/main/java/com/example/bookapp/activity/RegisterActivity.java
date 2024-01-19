package com.example.bookapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.LoadingDialogBar;
import com.example.bookapp.authentication.AuthenticationClass;
import com.example.bookapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //binding
    private ActivityRegisterBinding activityRegisterBinding;

    private String TAG = "REGISTER_TAG";

    private String name = "",email = "", password = "",selectedAccType = "";

    private AuthenticationClass authenticationClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityRegisterBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(activityRegisterBinding.getRoot());

        //init authentication class
        authenticationClass = new AuthenticationClass(this);

        //handle back
        activityRegisterBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        //handle signup
        activityRegisterBinding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        activityRegisterBinding.typeAccTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                typeAccDialog();
            }
        });
    }

    private void typeAccDialog() {
        Log.d(TAG, "categoryPickDialog: showing categories dialog");

        String[] typeAccArray = new String[2];
        typeAccArray[0] = "user";
        typeAccArray[1] = "admin";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Pick Category")
                .setItems(typeAccArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedAccType = typeAccArray[i];

                        activityRegisterBinding.typeAccTv.setText(selectedAccType);

                    }
                }).show();
    }

    private void validateData() {
        //check info validate

        //get data
        name = activityRegisterBinding.nameEt.getText().toString().trim();
        email = activityRegisterBinding.emailEt.getText().toString().trim();
        password = activityRegisterBinding.passwordEt.getText().toString().trim();
        String confirmPass = activityRegisterBinding.confirmPasswordEt.getText().toString().trim();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Name is required",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is required",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(this, "You need to type confirm password",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedAccType)) {
            Toast.makeText(this, "You need select type of account",Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email pattern ...!",Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmPass)){
            Toast.makeText(this, "Password doesn't match",Toast.LENGTH_SHORT).show();
        } else {
            authenticationClass.createUserAcc(email,password,name,selectedAccType);
        }
    }



}