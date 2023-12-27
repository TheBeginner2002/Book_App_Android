package com.example.bookapp.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.LoadingDialogBar;
import com.example.bookapp.constants.Constants;
import com.example.bookapp.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding activityPdfAddBinding;

    private FirebaseAuth firebaseAuth;

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    private String bookTitle = "", bookDescription = "";

    private String selectedCategoryId, selectedCategoryTitle;

    private Uri pdfUri = null;
    
    private LoadingDialogBar loadingDialogBar;

    ActivityResultLauncher<Intent> pdfPicker;

    private static final String TAG = "ADD_PDF_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPdfAddBinding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(activityPdfAddBinding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        loadBookCategories();
        
        loadingDialogBar = new LoadingDialogBar(this);

        activityPdfAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        activityPdfAddBinding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });

        activityPdfAddBinding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });

        activityPdfAddBinding.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateInput();
            }
        });

        pdfPicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if(data != null){
                            pdfUri = data.getData();
                            Log.d(TAG,pdfUri.getPath().toString());
                        }
                    }else {
                        Toast.makeText(PdfAddActivity.this, "No PDF selected", Toast.LENGTH_SHORT).show();
                    }
        });
    }

    private void validateInput() {
        Log.d(TAG, "validateInput: validating data ...");
        
        bookTitle = activityPdfAddBinding.titleEt.getText().toString().trim();
        bookDescription = activityPdfAddBinding.descriptionEt.getText().toString().trim();

        if(TextUtils.isEmpty(bookTitle)){
            Toast.makeText(this, "Title is empty",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(bookDescription)) {
            Toast.makeText(this, "Description is empty",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, "Category is not selected",Toast.LENGTH_SHORT).show();
        } else if (pdfUri == null) {
            Toast.makeText(this, "Pdf file is not selected",Toast.LENGTH_SHORT).show();
        } else {
            uploadBook();
        }
    }

    private void uploadBook() {
        Log.d(TAG, "uploadBook: uploading book ... ");

        loadingDialogBar.showDialog("Uploading Book");

        long timestamp = System.currentTimeMillis();

        String filePathAndName = "Book/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance(Constants.FIREBASE_STORAGE_LINK).getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //get pdf url
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadPdfUrl = ""+uriTask.getResult();

                loadingDialogBar.hideDialog();
                //upload book info
                uploadBookInfo(uploadPdfUrl, timestamp);
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialogBar.hideDialog();
                        Toast.makeText(PdfAddActivity.this, "Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadBookInfo(String uploadPdfUrl, long timestamp) {
        Log.d(TAG, "uploadBookInfo: upload book info");

        loadingDialogBar.showDialog("Uploading book info");

        String uid = firebaseAuth.getUid();

        //setup data
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("bookTitle", ""+bookTitle);
        hashMap.put("bookDescription", ""+bookDescription);
        hashMap.put("categoryId", ""+selectedCategoryId);
        hashMap.put("urlPdf", ""+uploadPdfUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", (long)0);
        hashMap.put("downloadsCount", (long)0);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_LINK).getReference("Books");
        databaseReference.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        loadingDialogBar.hideDialog();
                        Toast.makeText(PdfAddActivity.this, "Upload Successfully",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialogBar.hideDialog();
                        Toast.makeText(PdfAddActivity.this, "Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadBookCategories() {
        Log.d(TAG,"loadBookCategories: Loading Book Categories");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_LINK).getReference("Categories");
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

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PdfAddActivity.this,"Error: "+error.getMessage(),Toast.LENGTH_SHORT).show();
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

                        activityPdfAddBinding.categoryTv.setText(selectedCategoryTitle);

                    }
                }).show();
    }

    private void pdfPickIntent() {
        Log.d(TAG,"pdfPickIntent: starting pdf pick");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        pdfPicker.launch(intent);
    }
}