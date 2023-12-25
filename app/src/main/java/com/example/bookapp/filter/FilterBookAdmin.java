package com.example.bookapp.filter;

import android.widget.Filter;

import com.example.bookapp.adapter.AdapterBookAdmin;
import com.example.bookapp.adapter.AdapterCategory;
import com.example.bookapp.model.ModelBook;
import com.example.bookapp.model.ModelCategory;

import java.util.ArrayList;


public class FilterBookAdmin extends Filter {

    ArrayList<ModelBook> filterList;

    AdapterBookAdmin adapterBookAdmin;

    public FilterBookAdmin(ArrayList<ModelBook> filterList, AdapterBookAdmin adapterBookAdmin) {
        this.filterList = filterList;
        this.adapterBookAdmin = adapterBookAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();

        if(charSequence != null && charSequence.length() > 0){
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelBook> filteredModels =new ArrayList<>();

            for(int i=0; i<filterList.size(); i++){
                if(filterList.get(i).getBookTitle().toUpperCase().contains(charSequence)){
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapterBookAdmin.setBookArrayList((ArrayList<ModelBook>) filterResults.values);

        adapterBookAdmin.notifyDataSetChanged();
    }
}
