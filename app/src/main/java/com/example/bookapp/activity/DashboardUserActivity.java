package com.example.bookapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.bookapp.databinding.ActivityDashboardUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardUserActivity extends AppCompatActivity {

    private ActivityDashboardUserBinding activityDashboardUserBinding;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDashboardUserBinding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(activityDashboardUserBinding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        activityDashboardUserBinding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                checkUser();
            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null){
            Intent intent = new Intent(DashboardUserActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            String email = firebaseUser.getEmail();

            activityDashboardUserBinding.subTitleTv.setText(email);


        }
    }
}