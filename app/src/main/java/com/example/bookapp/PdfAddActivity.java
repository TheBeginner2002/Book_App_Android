package com.example.bookapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.bookapp.databinding.ActivityPdfAddBinding;
import com.google.firebase.auth.FirebaseAuth;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding activityPdfAddBinding;

    private FirebaseAuth firebaseAuth;

    ActivityResultLauncher<String> mTakePdf;

    private static final String TAG = "ADD_PDF_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPdfAddBinding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(activityPdfAddBinding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        mTakePdf = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {

            }
        });

        activityPdfAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        activityPdfAddBinding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mTakePdf.launch("file/*");
            }
        });
    }

//    private void pdfPickIntent() {
//        Log.d(TAG,"pdfPickIntent: starting pdf pick");
//
//        Intent intent = new Intent();
//        intent.setType("application/pdf");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent,"Select Pdf"));
//    }
}