package com.example.obk.UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.obk.R;

public class TotesAdapter extends ListAdapter<String, TotesAdapter.ToteVH> {

    public TotesAdapter() {
        super(DIFF);
    }

    private static final DiffUtil.ItemCallback<String> DIFF = new DiffUtil.ItemCallback<String>() {
        @Override public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }
        @Override public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }
    };

    static class ToteVH extends RecyclerView.ViewHolder {
        TextView tvCode;
        ToteVH(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvCode);
        }
    }

    @NonNull
    @Override
    public ToteVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tote, parent, false);
        return new ToteVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ToteVH holder, int position) {
        holder.tvCode.setText(getItem(position));
    }
}
