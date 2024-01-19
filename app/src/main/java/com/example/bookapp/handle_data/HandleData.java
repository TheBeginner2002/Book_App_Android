package com.example.bookapp.handle_data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.bookapp.LoadingDialogBar;
import com.example.bookapp.constants.Constants;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class HandleData {

    private Context context;

    private FirebaseDatabase firebaseDatabase;

    private FirebaseStorage firebaseStorage;

    private LoadingDialogBar loadingDialogBar;

    private ArrayList dataArrayList;


    public HandleData(Context context){
        this.context = context;
        this.firebaseDatabase = FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_LINK);
        this.firebaseStorage = FirebaseStorage.getInstance(Constants.FIREBASE_STORAGE_LINK);
        this.loadingDialogBar = new LoadingDialogBar(context);
//        this.dataArrayList = dataArrayList;
    }

    public void deleteBook(String bookId, String bookUrl, String bookTitle) {
        String TAG = "DELETE_BOOK_TAG";

        Log.d(TAG, "deleteBook: Deleting ...");

        loadingDialogBar.showDialog("Deleting "+bookTitle);

        Log.d(TAG, "deleteBook: deleting book from storage");

        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        DatabaseReference reference = firebaseDatabase.getReference("Books");

                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: deleted book");
                                        loadingDialogBar.hideDialog();
                                        Toast.makeText(context,"Delete book successfully",Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadingDialogBar.hideDialog();
                                        Log.d(TAG, "onFailure: "+e.getMessage());
                                        Toast.makeText(context,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                        loadingDialogBar.hideDialog();
                        Toast.makeText(context,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void loadPdfSize(String pdfUrl, TextView bookSize) {

        String TAG = "LOAD_BOOK_TAG";

        StorageReference reference = firebaseStorage.getReferenceFromUrl(pdfUrl);
        reference.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes =storageMetadata.getSizeBytes();

                        double kb = bytes/1024;
                        double mb = bytes/(1024*1024);

                        if (mb >= 1){
                            bookSize.setText(String.format("%.2f",mb)+" MB");
                        } else if (kb >= 1){
                            bookSize.setText(String.format("%.2f",kb)+" KB");
                        } else {
                            bookSize.setText(String.format("%.2f",bytes)+" bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                        Toast.makeText(context,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void loadCategory(String categoryId,TextView bookCategory) {
        String TAG = "LOAD_PDF_TAG";

        DatabaseReference reference = firebaseDatabase.getReference("Categories");
        reference.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();

                        bookCategory.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: "+error.getMessage());
                    }
                });
    }

    public void loadPdfFromUrl(String pdfUrl,String bookTitle,PDFView pdfView,ProgressBar progressBar) {
        String TAG = "LOAD_CATEGORY_TAG";

        Log.d(TAG, "loadCategory: "+pdfUrl);
        StorageReference reference = firebaseStorage.getReferenceFromUrl(pdfUrl);

        reference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+bookTitle+" successfully got the file");

                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }).load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: "+e.getMessage());
                        Toast.makeText(context,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void incrementBookViewCount(String bookId){
        DatabaseReference reference = firebaseDatabase.getReference("Books");

        reference.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String viewsCount = ""+snapshot.child("viewsCount").getValue();

                if (viewsCount.equals("") || viewsCount.equals("null")){
                    viewsCount="0";
                }

                long newViewsCount = Long.parseLong(viewsCount) + 1;
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("viewsCount",newViewsCount);

                reference.child(bookId)
                        .updateChildren(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void downloadBook(String bookId, String urlPdf, String bookTitle){
        String TAG = "DOWNLOAD_TAG";
        Log.d(TAG, "downloadBook: downloading book...");

        String nameWithExtension = bookTitle + ".pdf";
        Log.d(TAG, "downloadBook: "+nameWithExtension);

        loadingDialogBar.showDialog("Downloading "+nameWithExtension+"...");
        Log.d(TAG, "downloadBook: "+urlPdf);
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(urlPdf);

        storageReference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: Book downloaded");

                        saveDownloadedBook(bytes, nameWithExtension, bookId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Fail to download to "+e.getMessage());
                        loadingDialogBar.hideDialog();
                        Toast.makeText(context, "Download fail due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void saveDownloadedBook(byte[] bytes, String nameWithExtension, String bookId) {
        String TAG = "DOWNLOAD_TAG";
        Log.d(TAG, "saveDownloadedBook: Saving download book");
        try{
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadFolder.mkdirs();

            String filePath = downloadFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context,"Save to Download folder",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "saveDownloadedBook: Save to download folder");
            loadingDialogBar.hideDialog();
            
            incrementBookDownloadCount(bookId);
        } catch (Exception e){
            Log.d(TAG, "saveDownloadedBook: fail to saving due to "+e.getMessage());
            loadingDialogBar.hideDialog();
            Toast.makeText(context,"saveDownloadedBook: fail to saving due to "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void incrementBookDownloadCount(String bookId) {
        String TAG = "DOWNLOAD_TAG";
        Log.d(TAG, "incrementBookDownloadCount: increment count");

        DatabaseReference reference = firebaseDatabase.getReference("Books");

        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downloadCount = ""+snapshot.child("downloadsCount").getValue();

                        if (downloadCount.equals("") || downloadCount.equals("null")){
                            downloadCount = "0";
                        }

                        long newDownloadsCount = Long.parseLong(downloadCount) + 1;

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("downloadsCount", newDownloadsCount);

                        reference.child(bookId)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: success");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: fail due to"+e.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: fail due to"+error.getMessage());
                    }
                });
    }
}
