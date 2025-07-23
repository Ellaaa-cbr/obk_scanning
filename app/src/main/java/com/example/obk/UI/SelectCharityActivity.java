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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
        ioExecutor.execute(() -> {
            List<Charity> list = FakeApiService.getCharities(); // fake
            mainHandler.post(() -> charities = list);
        });


        binding.btnScan.setOnClickListener(v -> launchBarcodeScanner());

        // Go button
        binding.btnGo.setOnClickListener(v -> {
            String input = binding.etCharityId.getText().toString().trim();
            if (input.isEmpty()) {
                Snackbar.make(v, "Please enter or scan a charity ID", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // get name
            Charity matched = null;
            if (charities != null) {
                for (Charity c : charities) {
                    if (input.equalsIgnoreCase(c.id) || input.equalsIgnoreCase(c.name)) {
                        matched = c;
                        break;
                    }
                }
            }

            if (matched == null) {
                Snackbar.make(v, "Charity not found", Snackbar.LENGTH_SHORT).show();
                return;
            }

            selectedId = matched.id;
            selectedName = matched.name;

            Intent intent = new Intent(this, CourierCheckoutActivity.class)
                    .putExtra("CHARITY_ID", selectedId)
                    .putExtra("CHARITY_NAME", selectedName);
            startActivity(intent);
        });
    }

    private void launchBarcodeScanner() {
        new IntentIntegrator(this)
                .setOrientationLocked(true)
                .setPrompt("Scan charity barcode")
                .initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            binding.etCharityId.setText(result.getContents());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdownNow();
    }
}