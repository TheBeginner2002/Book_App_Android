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
import com.example.bookapp.activity.PdfDetailActivity;
import com.example.bookapp.activity.PdfEditActivity;
import com.example.bookapp.constants.Constants;
import com.example.bookapp.databinding.RowBookAdminBinding;
import com.example.bookapp.databinding.RowCategoryBinding;
import com.example.bookapp.filter.FilterBookAdmin;
import com.example.bookapp.handle_data.HandleData;
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

    private HandleData handleData;

    private static final String TAG = "BOOK_ADAPTER_TAG";

    public AdapterBookAdmin(Context context, ArrayList<ModelBook> bookArrayList) {
        this.context = context;
        this.bookArrayList = bookArrayList;
        this.filterList = bookArrayList;

        loadingDialogBar = new LoadingDialogBar(context);
        handleData = new HandleData(context);
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
        String bookId = book.getId();
        String formattedDate = MyApplication.formatTimestamp(timestamp);
        String pdfUrl = book.getUrlPdf();

        holder.bookTitle.setText(title);
        holder.bookDescription.setText(description);
        holder.date.setText(formattedDate);

        handleData.loadPdfFromUrl(pdfUrl,title,holder.pdfView,holder.progressBar);
        handleData.loadCategory(book.getCategoryId(),holder.bookCategory);
        handleData.loadPdfSize(pdfUrl,holder.bookSize);

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(book,holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", bookId);
                context.startActivity(intent);
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
                            context.startActivity(intent);
                        } else if (which == 1) { //delete
                            handleData.deleteBook(
                                    ""+bookId,
                                    ""+bookUrl,
                                    ""+bookTitle);
                        }
                    }
                }).show();
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
