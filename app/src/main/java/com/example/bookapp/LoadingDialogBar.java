package com.example.bookapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

public class LoadingDialogBar {
    Context context;
    Dialog dialog;


    public LoadingDialogBar(Context context) {
        this.context = context;
    }

    public void showDialog(String progressText){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.layout_loading_dialog);

        TextView progressTxt = dialog.findViewById(R.id.progressTxt);

        progressTxt.setText(progressText);
        dialog.create();
        dialog.show();
    }

    public void showDialog(){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.layout_loading_dialog);

        TextView progressTxt = dialog.findViewById(R.id.progressTxt);

        progressTxt.setText("Please wait!");
        dialog.create();
        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }
}
