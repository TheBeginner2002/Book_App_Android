package com.example.bookapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.bookapp.authentication.AuthenticationClass;
import com.example.bookapp.databinding.ActivityDashboardUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardUserActivity extends AppCompatActivity {

    private ActivityDashboardUserBinding activityDashboardUserBinding;

    private AuthenticationClass authenticationClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDashboardUserBinding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(activityDashboardUserBinding.getRoot());

        authenticationClass = new AuthenticationClass(this);
        authenticationClass.checkEmailUser(activityDashboardUserBinding.subTitleTv);

        activityDashboardUserBinding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticationClass.logout();
                authenticationClass.checkEmailUser(activityDashboardUserBinding.subTitleTv);
            }
        });
    }
}