package com.plymouth.assessment.cw2.showcase;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProjectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    ImageView mImageView;
    TextView mTitle;
    TextView mDesc;
    TextView mYear;

    RowClickListener rowClickListener;

    public ProjectViewHolder(@NonNull View itemView) {
        super(itemView);

        this.mImageView = itemView.findViewById(R.id.row_image);
        this.mTitle = itemView.findViewById(R.id.row_title);
        this.mDesc = itemView.findViewById(R.id.row_desc);
        this.mYear = itemView.findViewById(R.id.row_year);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        this.rowClickListener.onRowClickListener(view, getLayoutPosition());

    }

    public void setRowClickListener (RowClickListener rowClickListener) {

        this.rowClickListener = rowClickListener;

    }
}
