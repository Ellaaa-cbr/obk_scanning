package com.example.obk.UI;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.obk.data.SyncWorker;
import com.example.obk.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SyncWorker.enqueue(this);

        // Courier Checkout，
        binding.btnCourier.setOnClickListener(v ->
                startActivity(new Intent(this, SelectCharityActivity.class)));
    }
}
