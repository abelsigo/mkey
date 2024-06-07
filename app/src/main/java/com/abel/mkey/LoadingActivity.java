package com.abel.mkey;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

public class LoadingActivity extends AppCompatActivity {
    Context context;
    Dialog dialog;


    public LoadingActivity(Context context){
        this.context = context;
    }
    public void ShowDialog(String title){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.activity_loading);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView titleTextView = dialog.findViewById(R.id.loadingText);
        titleTextView.setText(title);
        dialog.create();
        dialog.show();
    }

    public void HideDialog(){
        dialog.dismiss();
    }

}
