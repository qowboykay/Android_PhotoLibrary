package com.example.android_photos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder> {
    private List<Picture> pictureList;
    private OnItemClickListener onItemClickListener;
    private List<Integer> selectedPositions;

    public PictureAdapter(List<Picture> pictureList) {
        this.pictureList = pictureList;
        this.selectedPositions = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void addPicture(Picture picture) {
        pictureList.add(picture);
        notifyItemInserted(pictureList.size() - 1);
    }

    public List<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picture, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picture picture = pictureList.get(position);
        holder.imageView.setImageURI(picture.getUri());
        holder.captionTextView.setText(picture.getCaption());

        // Toggle selection on item click
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });

        // Highlight selected items
        holder.itemView.setSelected(selectedPositions.contains(position));
    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView captionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            captionTextView = itemView.findViewById(R.id.captionTextView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(Integer.valueOf(position));
        } else {
            selectedPositions.add(position);
        }
        notifyItemChanged(position);
    }

    public void clearSelection() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }
}