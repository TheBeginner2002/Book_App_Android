package com.example.bookapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.activity.PdfListAdminActivity;
import com.example.bookapp.databinding.RowCategoryBinding;
import com.example.bookapp.filter.FilterCategory;
import com.example.bookapp.model.ModelCategory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> implements Filterable {

    private Context context;

    private ArrayList<ModelCategory> categoryArrayList, filterList;

    private RowCategoryBinding rowCategoryBinding;

    private FilterCategory filterCategory;

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
    }

    public ArrayList<ModelCategory> getCategoryArrayList() {
        return categoryArrayList;
    }

    public void setCategoryArrayList(ArrayList<ModelCategory> categoryArrayList) {
        this.categoryArrayList = categoryArrayList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding row category
        rowCategoryBinding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderCategory(rowCategoryBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        //get data
        ModelCategory categoryData = categoryArrayList.get(position);
        String id = categoryData.getId();
        String category = categoryData.getCategory();
        String uid = categoryData.getUid();
        long timestamp = categoryData.getTimestamp();

        //set data
        holder.categoryTv.setText(category);

        //delete Category
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this category ?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(context,"Deleting ...",Toast.LENGTH_SHORT).show();
                                deleteCategory(categoryData,holder);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("categoryId", id);
                intent.putExtra("categoryTitle",category);
                context.startActivity(intent);
            }
        });
    }

    private void deleteCategory(ModelCategory categoryData, HolderCategory holder) {
        String id = categoryData.getId();

        DatabaseReference reference = FirebaseDatabase.getInstance("https://book-app-5a1f1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Categories");

        reference.child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context,"Delete successfully",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filterCategory == null){
            filterCategory = new FilterCategory(filterList,this);
        }
        return filterCategory;
    }

    class HolderCategory extends RecyclerView.ViewHolder {

        TextView categoryTv;

        ImageButton deleteBtn;

        public HolderCategory(@NonNull View itemView) {
            super(itemView);
            categoryTv = rowCategoryBinding.categoryTv;
            deleteBtn = rowCategoryBinding.deleteBtn;
        }
    }
}
