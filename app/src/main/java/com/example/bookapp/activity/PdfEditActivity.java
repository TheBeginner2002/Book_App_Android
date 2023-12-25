package com.example.bookapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.bookapp.LoadingDialogBar;
import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityPdfEditBinding;

public class PdfEditActivity extends AppCompatActivity {

    private ActivityPdfEditBinding activityPdfEditBinding;

    private LoadingDialogBar loadingDialogBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPdfEditBinding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(activityPdfEditBinding.getRoot());

        loadingDialogBar = new LoadingDialogBar(this);
    }
}