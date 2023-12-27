package com.example.bookapp.handle_data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.LoadingDialogBar;
import com.example.bookapp.constants.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class HandleData {

    private Context context;

    private FirebaseDatabase firebaseDatabase;

    private FirebaseStorage firebaseStorage;

    private LoadingDialogBar loadingDialogBar;

    private ArrayList dataArrayList;

    private RecyclerView.Adapter adapter;

    public HandleData(Context context, ArrayList dataArrayList, RecyclerView.Adapter adapter){
        this.context = context;
        this.firebaseDatabase = FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_LINK);
        this.firebaseStorage = FirebaseStorage.getInstance(Constants.FIREBASE_STORAGE_LINK);
        this.loadingDialogBar = new LoadingDialogBar(context);
        this.dataArrayList = dataArrayList;
        this.adapter = adapter;
    }

    public void fetchData(String refPath){


        DatabaseReference reference = firebaseDatabase.getReference(refPath);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataArrayList.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
