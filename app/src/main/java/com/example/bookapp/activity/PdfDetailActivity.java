package com.example.bookapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityPdfDetailBinding;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding activityPdfDetailBinding;

    private String bookId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPdfDetailBinding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(activityPdfDetailBinding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        loadBookDetails();
    }

    private void loadBookDetails() {
    }
}