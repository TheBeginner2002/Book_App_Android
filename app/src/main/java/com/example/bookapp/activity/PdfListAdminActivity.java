package com.example.bookapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.bookapp.R;
import com.example.bookapp.adapter.AdapterBookAdmin;
import com.example.bookapp.databinding.ActivityPdfListAdminBinding;
import com.example.bookapp.model.ModelBook;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListAdminActivity extends AppCompatActivity {

    private ArrayList<ModelBook> books;

    private AdapterBookAdmin adapterBookAdmin;

    private ActivityPdfListAdminBinding activityPdfListAdminBinding;

    private static final String TAG = "BOOK_LIST_TAG";

    private String categoryId, categoryTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPdfListAdminBinding = ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(activityPdfListAdminBinding.getRoot());

        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");
        Log.d(TAG, "onCreate: "+categoryTitle+" "+categoryId);

        activityPdfListAdminBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        activityPdfListAdminBinding.subtitleTv.setText(categoryTitle);

        activityPdfListAdminBinding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterBookAdmin.getFilter().filter(charSequence);

                } catch (Exception e){
                    Log.d(TAG, "beforeTextChanged: "+e.getMessage());
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        loadPdfList();
    }

    private void loadPdfList() {
        books = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance("https://book-app-5a1f1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Books");
        Log.d(TAG, "loadPdfList: "+reference.toString());
        reference.orderByChild("categoryId")
                .equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        books.clear();

                        for(DataSnapshot ds: snapshot.getChildren()){

                            ModelBook book = ds.getValue(ModelBook.class);

                            books.add(book);
                        }

                        adapterBookAdmin = new AdapterBookAdmin(PdfListAdminActivity.this,books);
                        activityPdfListAdminBinding.bookRv.setAdapter(adapterBookAdmin);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: "+error.getMessage());
                    }
                });
    }
}