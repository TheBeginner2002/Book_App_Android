package com.example.bookapp.authentication;

import com.example.bookapp.LoadingDialogBar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AuthenticationClass {

    private FirebaseAuth firebaseAuth;

    private LoadingDialogBar loadingDialogBar;

    public AuthenticationClass(LoadingDialogBar loadingDialogBar) {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.loadingDialogBar = loadingDialogBar;
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
//                        loadingDialogBar.HideDialog();
//                        Toast.makeText(RegisterActivity.this, "Error: "+ e.getMessage(),Toast.LENGTH_SHORT).show();
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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://book-app-5a1f1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        databaseReference
                .child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        loadingDialogBar.hideDialog();
//                        Toast.makeText(RegisterActivity.this, "Account Created!",Toast.LENGTH_SHORT).show();

//                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
//                        finish();//destroy this activity
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        loadingDialogBar.hideDialog();
//                        Toast.makeText(RegisterActivity.this, "Error: "+ e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
