package com.example.obk.UI;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.obk.R;
import com.example.obk.viewmodel.SubmitState;
import com.example.obk.viewmodel.SubmitViewModel;

import java.util.ArrayList;

public class SubmittingActivity extends ComponentActivity {

    public static final String EXTRA_CHARITY_ID   = "CHARITY_ID";
    public static final String EXTRA_TOTE_IDS     = "TOTE_IDS";
    public static final String EXTRA_PHOTO_PATH   = "PHOTO_PATH";

    private SubmitViewModel vm;
    private TextView tvStatus;
    private Button   btnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submitting);

        tvStatus = findViewById(R.id.tvStatus);
        btnRetry = findViewById(R.id.btnRetry);

        vm = new ViewModelProvider(this).get(SubmitViewModel.class);

        /* — 观察状态 — */
        vm.getSubmitState().observe(this, s -> {
            switch (s) {
                case SENDING:
                    tvStatus.setText("Sending…");
                    btnRetry.setEnabled(false);
                    break;
                case SUCCESS:
                    Toast.makeText(this, "Checkout sent!", Toast.LENGTH_SHORT).show();
                    finish();            // 关闭界面
                    break;
                case ERROR:
                    tvStatus.setText("Failed. Check network and retry.");
                    btnRetry.setEnabled(true);
                    break;
            }
        });

        btnRetry.setOnClickListener(v -> triggerSubmit());

        /* 首次触发 */
        if (savedInstanceState == null) triggerSubmit();
    }

    private void triggerSubmit() {
        String charityId   = getIntent().getStringExtra(EXTRA_CHARITY_ID);
        ArrayList<String> ids = getIntent().getStringArrayListExtra(EXTRA_TOTE_IDS);
        String photoPath   = getIntent().getStringExtra(EXTRA_PHOTO_PATH);
        vm.submit(charityId, ids, photoPath);
    }
}
