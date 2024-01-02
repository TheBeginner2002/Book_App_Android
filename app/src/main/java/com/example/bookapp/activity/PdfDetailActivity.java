package com.example.bookapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
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

    private String bookId = "";

    private String TAG = "PDF_DETAIL_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPdfDetailBinding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(activityPdfDetailBinding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        handleData = new HandleData(this);

        handleData.incrementBookViewCount(bookId);

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

    }

    private void loadBookDetails() {

        DatabaseReference reference = FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_LINK).getReference("Books");

        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String bookTitle = ""+snapshot.child("bookTitle").getValue();
                        String bookDescription = ""+snapshot.child("bookDescription").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        String urlPdf = ""+snapshot.child("urlPdf").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();

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