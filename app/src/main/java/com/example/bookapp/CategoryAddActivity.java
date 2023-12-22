package com.example.bookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityCategoryAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {

    private ActivityCategoryAddBinding activityCategoryAddBinding;

    private FirebaseAuth firebaseAuth;

    private LoadingDialogBar loadingDialogBar;

    private String category = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityCategoryAddBinding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(activityCategoryAddBinding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        loadingDialogBar = new LoadingDialogBar(this);

        activityCategoryAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        activityCategoryAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }

    private void validateData() {
        category = activityCategoryAddBinding.categoryEt.getText().toString().trim();

        if (TextUtils.isEmpty(category)){
            Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show();
        } else {
            addCategoryDatabase();
        }
    }

    private void addCategoryDatabase() {

        loadingDialogBar.showDialog("Adding category ...");

        long timestamp = System.currentTimeMillis();

        //setup info category
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("category",category);
        hashMap.put("timestamp",timestamp);
        hashMap.put("uid",firebaseAuth.getUid());

        //add to db
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://book-app-5a1f1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Categories");
        databaseReference.child(""+timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                loadingDialogBar.hideDialog();
                Toast.makeText(CategoryAddActivity.this, "Category added successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingDialogBar.hideDialog();
                Toast.makeText(CategoryAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}