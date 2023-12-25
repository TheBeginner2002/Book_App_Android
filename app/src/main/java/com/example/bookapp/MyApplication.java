package com.example.bookapp;

import android.app.Application;
import android.text.format.DateFormat;


import java.util.Calendar;
import java.util.Locale;

//can access everywhere
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String formatTimestamp(long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);

        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy",calendar).toString();

        return date;
    }
}
