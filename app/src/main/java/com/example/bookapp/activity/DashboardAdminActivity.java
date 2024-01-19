package com.example.bookapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.bookapp.adapter.AdapterCategory;
import com.example.bookapp.authentication.AuthenticationClass;
import com.example.bookapp.constants.Constants;
import com.example.bookapp.databinding.ActivityDashboardAdminBinding;
import com.example.bookapp.model.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {

    private ActivityDashboardAdminBinding activityDashboardAdminBinding;

    private ArrayList<ModelCategory> categoryArrayList;

    private AdapterCategory adapterCategory;

    private AuthenticationClass authenticationClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDashboardAdminBinding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(activityDashboardAdminBinding.getRoot());

        authenticationClass = new AuthenticationClass(this);

        authenticationClass.checkEmailUser(activityDashboardAdminBinding.subTitleTv);
        loadCategories();

        activityDashboardAdminBinding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticationClass.logout();
                authenticationClass.checkEmailUser(activityDashboardAdminBinding.subTitleTv);
            }
        });

        //start Category Add page
        activityDashboardAdminBinding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, CategoryAddActivity.class));
            }
        });

        activityDashboardAdminBinding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterCategory.getFilter().filter(charSequence);
                }catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        activityDashboardAdminBinding.addPdfFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this, PdfAddActivity.class));
            }
        });
    }

    private void loadCategories() {
        //init array list
        categoryArrayList = new ArrayList<>();
        //get category from database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_LINK).getReference("Categories");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear data
                categoryArrayList.clear();

                //add data
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelCategory model = ds.getValue(ModelCategory.class);

                    categoryArrayList.add(model);


                }

                //setup adapter
                adapterCategory = new AdapterCategory(DashboardAdminActivity.this,categoryArrayList);

                //add to recycle view
                activityDashboardAdminBinding.categoriesRv.setAdapter(adapterCategory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}