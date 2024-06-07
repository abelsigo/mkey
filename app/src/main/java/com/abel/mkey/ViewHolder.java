package com.abel.mkey;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ViewHolder extends RecyclerView.ViewHolder {

    public TextView textViewItem;
    public FloatingActionButton bEditarKey;

    public ViewHolder(View itemView) {
        super(itemView);
        textViewItem = itemView.findViewById(R.id.textPL);
        bEditarKey = itemView.findViewById(R.id.bKeys);
    }
}
