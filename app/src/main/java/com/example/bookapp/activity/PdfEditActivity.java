package com.example.bookapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.LoadingDialogBar;
import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityPdfEditBinding;
import com.example.bookapp.model.ModelCategory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PrimitiveIterator;

public class PdfEditActivity extends AppCompatActivity {

    private ActivityPdfEditBinding activityPdfEditBinding;

    private LoadingDialogBar loadingDialogBar;

    private ArrayList<String> categoryTitleArrayList,categoryIdArrayList;

    private String bookId = "", selectedCategoryTitle = "", selectedCategoryId = "", bookTitle = "", bookDescription = "";

    private static final String TAG = "BOOK_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPdfEditBinding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(activityPdfEditBinding.getRoot());

        loadingDialogBar = new LoadingDialogBar(this);

        bookId = getIntent().getStringExtra("bookId");

        loadBookCategories();
        loadBookInfo();
        activityPdfEditBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        activityPdfEditBinding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });

        activityPdfEditBinding.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private void validateData() {
        Log.d(TAG, "validateInput: validating data ...");

        bookTitle = activityPdfEditBinding.titleEt.getText().toString().trim();
        bookDescription = activityPdfEditBinding.descriptionEt.getText().toString().trim();

        if(TextUtils.isEmpty(bookTitle)){
            Toast.makeText(this, "Title is empty",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(bookDescription)) {
            Toast.makeText(this, "Description is empty",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryId)) {
            Toast.makeText(this, "Category is not selected",Toast.LENGTH_SHORT).show();
        } else {
            updateBook();
        }
    }

    private void updateBook() {

        loadingDialogBar.showDialog("Updating");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("bookTitle",bookTitle);
        hashMap.put("bookDescription",bookDescription);
        hashMap.put("categoryId",selectedCategoryId);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://book-app-5a1f1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Books");

        databaseReference.child(bookId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: update book successfully");
                        loadingDialogBar.hideDialog();
                        Toast.makeText(PdfEditActivity.this, "Update Successfully",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update due to "+e.getMessage());
                        loadingDialogBar.hideDialog();
                        Toast.makeText(PdfEditActivity.this, "Failed to update",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: loading book info");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://book-app-5a1f1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Books");

        databaseReference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                selectedCategoryId = ""+snapshot.child("categoryId").getValue();
                String bookDescription = ""+snapshot.child("bookDescription").getValue();
                String bookTitle = ""+snapshot.child("bookTitle").getValue();

                activityPdfEditBinding.titleEt.setText(bookTitle);
                activityPdfEditBinding.descriptionEt.setText(bookDescription);

                Log.d(TAG, "onDataChange: loading category book info");
                DatabaseReference categoriesReference = FirebaseDatabase.getInstance("https://book-app-5a1f1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");

                categoriesReference.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String categoryTitle = ""+snapshot.child("category").getValue();

                        activityPdfEditBinding.categoryTv.setText(categoryTitle);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: load category failed due to "+error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: get book info error due to: "+error.getMessage());
            }
        });
    }

    private void loadBookCategories() {
        Log.d(TAG, "loadBookCategories: loading categories");
        
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://book-app-5a1f1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();

                for(DataSnapshot ds: snapshot.getChildren()){
                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();

                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);

                    Log.d(TAG, "onDataChange: "+categoryId);
                    Log.d(TAG, "onDataChange: "+categoryTitle);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PdfEditActivity.this,"Error: "+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing categories dialog");

        String[] categoriesArray = new String[categoryTitleArrayList.size()];

        for(int i = 0; i< categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedCategoryTitle = categoryTitleArrayList.get(i);
                        selectedCategoryId = categoryIdArrayList.get(i);

                        activityPdfEditBinding.categoryTv.setText(selectedCategoryTitle);

                    }
                }).show();
    }
}