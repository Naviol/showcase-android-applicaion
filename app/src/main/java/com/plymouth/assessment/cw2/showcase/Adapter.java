package com.plymouth.assessment.cw2.showcase;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;
import java.util.Locale;

public class Adapter extends RecyclerView.Adapter<ProjectViewHolder> {

    private Context mContext;
    private List<Project> projects;

    public Adapter(Context mContext, List<Project> projects) {
        this.mContext = mContext;
        this.projects = projects;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item,null);

        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.mTitle.setText(projects.get(position).getTitle());
        holder.mDesc.setText(projects.get(position).getDescription());
        holder.mYear.setText(Integer.toString(projects.get(position).getYear()));

        String thumbnailUrl = projects.get(position).getThumbnailURL();

        if (thumbnailUrl == null || !URLUtil.isValidUrl(thumbnailUrl)) {
            holder.mImageView.setImageResource(R.drawable.null_image);
        } else {
            Glide.with(mContext)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.null_image)
                    .error(R.drawable.null_image)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(holder.mImageView);
            Log.d("Glide", "Loaded thumbnail for Project #" + projects.get(position).getProjectID());
        }

        holder.setRowClickListener(new RowClickListener() {
            @Override
            public void onRowClickListener(View view, int position) {

                int projectId = projects.get(position).getProjectID();
                String title = projects.get(position).getTitle();

                Intent intent = new Intent(mContext, ProjectDetailActivity.class);
                try {
                    intent.putExtra("ProjectId", projectId);
                    intent.putExtra("iTitle", title);
                } catch (Exception e) {
                    throw e;
                }
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }
}