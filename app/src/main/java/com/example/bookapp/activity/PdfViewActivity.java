package com.example.bookapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.constants.Constants;
import com.example.bookapp.databinding.ActivityPdfViewBinding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PdfViewActivity extends AppCompatActivity {

    private ActivityPdfViewBinding activityPdfViewBinding;

    private String bookId = "";

    private static final String TAG = "PDF_VIEW_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPdfViewBinding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(activityPdfViewBinding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        activityPdfViewBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        
        loadBookDetail();

    }

    private void loadBookDetail() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_LINK).getReference("Books");

        databaseReference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pdfUrl = ""+snapshot.child("urlPdf").getValue();

                loadBookFromUrl(pdfUrl);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadBookFromUrl(String pdfUrl) {
        StorageReference storageReference = FirebaseStorage.getInstance(Constants.FIREBASE_STORAGE_LINK).getReferenceFromUrl(pdfUrl);

        storageReference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        activityPdfViewBinding.pdfView
                                .fromBytes(bytes)
                                .swipeHorizontal(false)
                                .onPageChange(new OnPageChangeListener() {
                                    @Override
                                    public void onPageChanged(int page, int pageCount) {
                                        int currentPage = page+1;
                                        activityPdfViewBinding.subTitleTv.setText(currentPage+"/"+pageCount);
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        Toast.makeText(PdfViewActivity.this,"Error on page "+page+" "+t.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Toast.makeText(PdfViewActivity.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                }).load();
                        activityPdfViewBinding.progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        activityPdfViewBinding.progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }
}