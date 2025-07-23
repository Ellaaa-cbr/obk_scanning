package com.example.obk.UI;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.obk.R;
import com.example.obk.data.local.entity.ScannedTote;

/** 列表：每行显示条码 + 可编辑数量 */
public class TotesAdapter extends ListAdapter<ScannedTote, TotesAdapter.ToteVH> {

    /* 回调，把数量变动通知给 ViewModel */
    public interface QtyChangeListener { void onQtyChanged(String code, int qty); }
    private final QtyChangeListener listener;

    public TotesAdapter(QtyChangeListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    /* ---------- DiffUtil ---------- */
    private static final DiffUtil.ItemCallback<ScannedTote> DIFF =
            new DiffUtil.ItemCallback<ScannedTote>() {
                @Override public boolean areItemsTheSame(@NonNull ScannedTote o, @NonNull ScannedTote n) {
                    return o.code.equals(n.code);
                }
                @Override public boolean areContentsTheSame(@NonNull ScannedTote o, @NonNull ScannedTote n) {
                    return o.equals(n);
                }
            };

    /* ---------- ViewHolder ---------- */
    static class ToteVH extends RecyclerView.ViewHolder {
        TextView tvCode;
        EditText etQty;
        ToteVH(@NonNull View v) {
            super(v);
            tvCode = v.findViewById(R.id.tvCode);
            etQty  = v.findViewById(R.id.etQty);
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
    public void onBindViewHolder(@NonNull ToteVH h, int pos) {
        ScannedTote item = getItem(pos);
        h.tvCode.setText(item.code);

        // 先移除旧监听，再设值，避免复用时回调错乱
        if (h.etQty.getTag() instanceof TextWatcher) {
            h.etQty.removeTextChangedListener((TextWatcher) h.etQty.getTag());
        }
        h.etQty.setText(String.valueOf(item.qty));

        TextWatcher tw = new SimpleWatcher(q -> listener.onQtyChanged(item.code, q));
        h.etQty.addTextChangedListener(tw);
        h.etQty.setTag(tw);
    }

    /* 只关心 afterTextChanged 的轻量 TextWatcher */
    private static class SimpleWatcher implements TextWatcher {
        private final java.util.function.IntConsumer cb;
        SimpleWatcher(java.util.function.IntConsumer cb) { this.cb = cb; }
        @Override public void beforeTextChanged(CharSequence s,int i,int c,int a){}
        @Override public void onTextChanged(CharSequence s,int i,int b,int c){}
        @Override public void afterTextChanged(Editable e){
            try {
                int q = e.length() == 0 ? 0 : Integer.parseInt(e.toString());
                cb.accept(q);
            } catch (NumberFormatException ignore) {}
        }
    }
}
