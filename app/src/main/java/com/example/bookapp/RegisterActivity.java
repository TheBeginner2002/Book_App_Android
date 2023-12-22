package com.example.bookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    //binding
    private ActivityRegisterBinding activityRegisterBinding;

    private String name = "",email = "", password = "";

    private FirebaseAuth firebaseAuth;

    private LoadingDialogBar loadingDialogBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityRegisterBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(activityRegisterBinding.getRoot());

        //progress bar
        loadingDialogBar = new LoadingDialogBar(this);

        //init Firebase
        firebaseAuth = FirebaseAuth.getInstance();

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
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email pattern ...!",Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmPass)){
            Toast.makeText(this, "Password doesn't match",Toast.LENGTH_SHORT).show();
        } else {
            createUserAcc();
        }
    }

    private void createUserAcc() {
//        alertDialog.show();
        loadingDialogBar.showDialog("Create User");
        //create acc
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                updateUserInfo();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
//                alertDialog.dismiss();
                loadingDialogBar.hideDialog();
                Toast.makeText(RegisterActivity.this, "Error: "+ e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserInfo() {
        loadingDialogBar.showDialog("Add user to db");
        long timestamp = System.currentTimeMillis();

        //get id of user
        String uid = firebaseAuth.getUid();

        //setup data user to add in db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("name", name);
        hashMap.put("email", email);
        hashMap.put("profileImage", "");
        hashMap.put("userType", "user"); //user & admin
        hashMap.put("timestamp",timestamp);

        //set data to db
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://book-app-5a1f1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        databaseReference
                .child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
//                alertDialog.dismiss();
                loadingDialogBar.hideDialog();
                Toast.makeText(RegisterActivity.this, "Account Created!",Toast.LENGTH_SHORT).show();

                startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                finish();//destroy this activity
            }
        })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
//                alertDialog.dismiss();
                loadingDialogBar.hideDialog();
                Toast.makeText(RegisterActivity.this, "Error: "+ e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });



    }

}