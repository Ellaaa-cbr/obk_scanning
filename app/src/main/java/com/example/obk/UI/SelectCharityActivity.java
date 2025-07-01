// File: com/example/obk/ui/SelectCharityActivity.java
package com.example.obk.UI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.obk.data.local.entity.Charity;
import com.example.obk.data.remote.FakeApiService;
import com.example.obk.databinding.ActivitySelectCharityBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * First screen – let user choose a charity and tap GO.
 * Uses FakeApiService.getCharities() to simulate network fetch.
 */
public class SelectCharityActivity extends AppCompatActivity {

    private ActivitySelectCharityBinding binding;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private List<Charity> charities;
    private String selectedId, selectedName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectCharityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1) 后台获取慈善机构列表
        ioExecutor.execute(() -> {
            List<Charity> list = FakeApiService.getCharities();
            // 返回主线程更新 UI
            mainHandler.post(() -> populateDropdown(list));
        });

        // 2) 点击 GO 按钮，跳转并带上选中的 charityId/name
        binding.btnGo.setOnClickListener(v -> {
            if (selectedId == null) {
                Snackbar.make(v, "Please choose a charity", Snackbar.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CourierCheckoutActivity.class)
                    .putExtra("CHARITY_ID", selectedId)
                    .putExtra("CHARITY_NAME", selectedName);
            startActivity(intent);
        });
    }

    private void populateDropdown(List<Charity> list) {
        this.charities = list;
        List<String> names = new ArrayList<>();
        for (Charity c : list) names.add(c.name);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                names
        );
        binding.spCharity.setAdapter(adapter);

        binding.spCharity.setOnItemClickListener((parent, view, position, id) -> {
            Charity c = charities.get(position);
            selectedId   = c.id;
            selectedName = c.name;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdownNow();
    }
}
