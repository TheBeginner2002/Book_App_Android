package com.example.bookapp.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.MyApplication;
import com.example.bookapp.constants.Constants;
import com.example.bookapp.databinding.ActivityPdfDetailBinding;
import com.example.bookapp.handle_data.HandleData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding activityPdfDetailBinding;

    private HandleData handleData;

    private String bookId = "",bookTitle = "", urlPdf = "";

    private String TAG = "PDF_DETAIL_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPdfDetailBinding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(activityPdfDetailBinding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        handleData = new HandleData(this);

        handleData.incrementBookViewCount(bookId);

        activityPdfDetailBinding.downloadBtn.setVisibility(View.GONE);

        loadBookDetails();
        activityPdfDetailBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        activityPdfDetailBinding.readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent.putExtra("bookId",bookId);
                startActivity(intent);
            }
        });

        activityPdfDetailBinding.downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Check permission");

                if (ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "onClick: Permission already granted");
                    handleData.downloadBook(""+bookId,""+urlPdf,""+bookTitle);
                } else {
                    Log.d(TAG, "onClick: Permission was not granted, request permission");

                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
                if(result){
                    Log.d(TAG, "Permission granted");
                    handleData.downloadBook(""+bookId,""+urlPdf,""+bookTitle);
                } else {
                    Log.d(TAG, "Permission was denied ...");
                    Toast.makeText(this,"Permission was denied ...",Toast.LENGTH_SHORT).show();
                }
            });

    private void loadBookDetails() {

        DatabaseReference reference = FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_LINK).getReference("Books");

        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        bookTitle = ""+snapshot.child("bookTitle").getValue();
                        String bookDescription = ""+snapshot.child("bookDescription").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        urlPdf = ""+snapshot.child("urlPdf").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();

                        activityPdfDetailBinding.downloadBtn.setVisibility(View.VISIBLE);

                        Log.d(TAG, "onDataChange: "+bookTitle);

                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        handleData.loadCategory(categoryId,activityPdfDetailBinding.categoryTv);
                        handleData.loadPdfFromUrl(urlPdf,bookTitle, activityPdfDetailBinding.pdfView, activityPdfDetailBinding.progressBar);
                        handleData.loadPdfSize(urlPdf,activityPdfDetailBinding.sizeTv);

                        activityPdfDetailBinding.titleTv.setText(bookTitle);
                        activityPdfDetailBinding.descriptionTv.setText(bookDescription);
                        activityPdfDetailBinding.dateTv.setText(date);
                        activityPdfDetailBinding.downloadTv.setText(downloadsCount.replace("null","N/A"));
                        activityPdfDetailBinding.viewsTv.setText(viewsCount.replace("null","N/A"));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}