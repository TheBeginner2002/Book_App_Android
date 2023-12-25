package com.example.bookapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.LoadingDialogBar;
import com.example.bookapp.MyApplication;
import com.example.bookapp.activity.PdfEditActivity;
import com.example.bookapp.constants.Constants;
import com.example.bookapp.databinding.RowBookAdminBinding;
import com.example.bookapp.databinding.RowCategoryBinding;
import com.example.bookapp.filter.FilterBookAdmin;
import com.example.bookapp.model.ModelBook;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.auth.api.signin.internal.Storage;
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

import java.util.ArrayList;

public class AdapterBookAdmin extends RecyclerView.Adapter<AdapterBookAdmin.HolderBookAdmin> implements Filterable {
    private Context context;

    private ArrayList<ModelBook> bookArrayList, filterList;

    private RowBookAdminBinding rowBookAdminBinding;

    private FilterBookAdmin filterBookAdmin;

    private LoadingDialogBar loadingDialogBar;

    private static final String TAG = "BOOK_ADAPTER_TAG";

    public AdapterBookAdmin(Context context, ArrayList<ModelBook> bookArrayList) {
        this.context = context;
        this.bookArrayList = bookArrayList;
        this.filterList = bookArrayList;

        loadingDialogBar = new LoadingDialogBar(context);
    }

    public void setFilterList(ArrayList<ModelBook> filterList) {
        this.filterList = filterList;
    }

    public ArrayList<ModelBook> getFilterList() {
        return filterList;
    }

    public ArrayList<ModelBook> getBookArrayList() {
        return bookArrayList;
    }

    public void setBookArrayList(ArrayList<ModelBook> bookArrayList) {
        this.bookArrayList = bookArrayList;
    }

    @NonNull
    @Override
    public HolderBookAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        rowBookAdminBinding = RowBookAdminBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderBookAdmin(rowBookAdminBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBookAdmin holder, int position) {
        ModelBook book = bookArrayList.get(position);

        String title = book.getBookTitle();
        String description = book.getBookDescription();
        long timestamp = book.getTimestamp();

        String formattedDate = MyApplication.formatTimestamp(timestamp);

        holder.bookTitle.setText(title);
        holder.bookDescription.setText(description);
        holder.date.setText(formattedDate);

        loadCategory(book, holder);
        loadPdfFromUrl(book, holder);
        loadPdfSize(book, holder);

        rowBookAdminBinding.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(book,holder);
            }
        });
    }

    private void moreOptionsDialog(ModelBook book, HolderBookAdmin holder) {
        String bookId = book.getId();
        String bookUrl = book.getUrlPdf();
        String bookTitle = book.getBookTitle();
        String bookDescription = book.getBookDescription();
        String bookCategoryId = book.getCategoryId();

        String[] options = {"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Choose Options")
                .setItems(options,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){ //edit
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId",bookId);
                            intent.putExtra("bookUrl",bookUrl);
                            intent.putExtra("bookTitle",bookTitle);
                            intent.putExtra("bookTitle",bookTitle);
                        } else if (which == 1) { //delete
                            deleteBook(book,holder);
                        }
                    }
                }).show();
    }

    private void deleteBook(ModelBook book, HolderBookAdmin holder) {
        String bookId = book.getId();
        String bookUrl = book.getUrlPdf();
        String bookTitle = book.getBookTitle();

        Log.d(TAG, "deleteBook: Deleting ...");

        loadingDialogBar.showDialog("Deleting "+bookTitle);

        Log.d(TAG, "deleteBook: deleting book from storage");

        StorageReference storageReference = FirebaseStorage.getInstance("gs://book-app-5a1f1.appspot.com").getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        DatabaseReference reference = FirebaseDatabase.getInstance("https://book-app-5a1f1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Books");

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

    private void loadPdfSize(ModelBook book, HolderBookAdmin holder) {

        String pdfUrl = book.getUrlPdf();

        StorageReference reference = FirebaseStorage.getInstance("gs://book-app-5a1f1.appspot.com").getReferenceFromUrl(pdfUrl);
        reference.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes =storageMetadata.getSizeBytes();

                        Log.d(TAG, "onSuccess: "+book.getBookTitle()+ " " +bytes);

                        double kb = bytes/1024;
                        double mb = bytes/(1024*1024);

                        if (mb >= 1){
                            holder.bookSize.setText(String.format("%.2f",mb)+" MB");
                        } else if (kb >= 1){
                            holder.bookSize.setText(String.format("%.2f",kb)+" KB");
                        } else {
                            holder.bookSize.setText(String.format("%.2f",bytes)+" bytes");
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

    private void loadCategory(ModelBook book, HolderBookAdmin holder) {
        String pdfUrl = book.getUrlPdf();
        Log.d(TAG, "loadCategory: "+pdfUrl);
        StorageReference reference = FirebaseStorage.getInstance("gs://book-app-5a1f1.appspot.com").getReferenceFromUrl(pdfUrl);

        reference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+book.getBookTitle()+" successfully got the file");

                        holder.pdfView.fromBytes(bytes)
                                .pages(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }).load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: "+e.getMessage());
                        Toast.makeText(context,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfFromUrl(ModelBook book, HolderBookAdmin holder) {
        String categoryId = book.getCategoryId();

        DatabaseReference reference = FirebaseDatabase.getInstance("https://book-app-5a1f1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();

                        holder.bookCategory.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: "+error.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return bookArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filterBookAdmin == null){
            filterBookAdmin = new FilterBookAdmin(filterList, this);
        }
        return filterBookAdmin;
    }


    class HolderBookAdmin extends RecyclerView.ViewHolder {
        //UI views
        PDFView pdfView;
        ProgressBar progressBar;
        TextView bookTitle;
        TextView bookDescription;
        TextView bookCategory;
        TextView bookSize;
        TextView date;
        ImageButton moreBtn;


        public HolderBookAdmin(@NonNull View itemView) {
            super(itemView);

            pdfView = rowBookAdminBinding.pdfView;
            progressBar = rowBookAdminBinding.progressBar;
            bookTitle = rowBookAdminBinding.titleTv;
            bookDescription = rowBookAdminBinding.descriptionTv;
            bookCategory = rowBookAdminBinding.categoryTv;
            bookSize = rowBookAdminBinding.sizeTv;
            date = rowBookAdminBinding.dateTv;
            moreBtn = rowBookAdminBinding.moreBtn;
        }
    }
}
