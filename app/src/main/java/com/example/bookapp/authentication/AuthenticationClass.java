package com.example.bookapp.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.bookapp.LoadingDialogBar;
import com.example.bookapp.activity.DashboardAdminActivity;
import com.example.bookapp.activity.DashboardUserActivity;
import com.example.bookapp.activity.LoginActivity;
import com.example.bookapp.activity.MainActivity;
import com.example.bookapp.activity.SplashActivity;
import com.example.bookapp.constants.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AuthenticationClass {

    private FirebaseAuth firebaseAuth;

    private Context context;

    private LoadingDialogBar loadingDialogBar;

    public AuthenticationClass(Context context) {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.context = context;
        this.loadingDialogBar = new LoadingDialogBar(context);
    }

    public void createUserAcc(String email, String password, String name) {
        loadingDialogBar.showDialog("Create User");
        //create acc
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        updateUserInfo(name, email);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        loadingDialogBar.hideDialog();
                        Toast.makeText(context, "Error: "+ e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo(String name, String email) {
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
        hashMap.put("timestamp", timestamp);

        //set data to db
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_LINK).getReference("Users");
        databaseReference
                .child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        loadingDialogBar.hideDialog();
                        Toast.makeText(context, "Account Created!",Toast.LENGTH_SHORT).show();

                        context.startActivity(new Intent(context, DashboardUserActivity.class));
                        ((Activity)context).finish();//destroy this activity
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        loadingDialogBar.hideDialog();
                        Toast.makeText(context, "Error: "+ e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void loginUser(String email,String password) {
        loadingDialogBar.showDialog("Login");

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                checkUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingDialogBar.hideDialog();
                Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_LINK).getReference("Users");

        databaseReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userType = ""+snapshot.child("userType").getValue();
                loadingDialogBar.hideDialog();
                if (userType.equals("user")){
                    context.startActivity(new Intent(context, DashboardUserActivity.class));
                    ((Activity)context).finish();
                } else if (userType.equals("admin")) {
                    context.startActivity(new Intent(context, DashboardAdminActivity.class));
                    ((Activity)context).finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialogBar.hideDialog();
                Toast.makeText(context, "Error: "+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkUserType() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null){
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
            ((Activity)context).finish();
        } else {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_LINK).getReference("Users");

            databaseReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userType = ""+snapshot.child("userType").getValue();

                    if (userType.equals("user")){
                        context.startActivity(new Intent(context, DashboardUserActivity.class));
                        ((Activity)context).finish();
                    } else if (userType.equals("admin")) {
                        context.startActivity(new Intent(context, DashboardAdminActivity.class));
                        ((Activity)context).finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                    Toast.makeText(context, "Error: "+error.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public void checkEmailUser(TextView textView) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null){
            Intent intent = new Intent(context,MainActivity.class);
            context.startActivity(intent);
            ((Activity)context).finish();
        } else {
            String email = firebaseUser.getEmail();

            textView.setText(email);
        }
    }

    public void logout(){
        firebaseAuth.signOut();
    }

}
