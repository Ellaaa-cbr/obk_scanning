package com.example.obk.UI;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.obk.databinding.ActivityCourierCheckoutBinding;
import com.example.obk.viewmodel.CourierCheckoutViewModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Second screen – scan barcodes, take/upload photos, submit checkout.
 */
public class CourierCheckoutActivity extends AppCompatActivity {

    private ActivityCourierCheckoutBinding binding;
    private CourierCheckoutViewModel viewModel;
    private TotesAdapter adapter;
    private Uri photoUri;
    private int fakeCounter = 1;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCourierCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        final String charityId = getIntent().getStringExtra("CHARITY_ID");
        String charityName     = getIntent().getStringExtra("CHARITY_NAME");
        binding.tvCharityName.setText(charityName == null ? "Unknown" : charityName);
        viewModel = new ViewModelProvider(this).get(CourierCheckoutViewModel.class);
        adapter = new TotesAdapter((code, qty) -> viewModel.updateQty(code, qty));
        binding.rvTotes.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTotes.setAdapter(adapter);
        viewModel.getScannedTotes().observe(this, adapter::submitList);


        PhotoAdapter photoAdapter = new PhotoAdapter();
        binding.rvPhotos.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvPhotos.setAdapter(photoAdapter);
        viewModel.getPhotoPaths().observe(this, photoAdapter::submitList);


        // Photo counter
        viewModel.getPhotoPaths().observe(this,
                list -> binding.tvPhotoCount.setText(list.size() + " photo(s)"));

        // Scan button
        binding.btnScan.setOnClickListener(v -> launchBarcodeScanner());

        // Photo button
        binding.btnPhoto.setOnClickListener(v -> checkCameraPermissionThenLaunch());

        // Submit button
        binding.btnSubmit.setOnClickListener(v -> {
            if (viewModel.getScannedTotes().getValue().isEmpty()) {
                Snackbar.make(v, "No totes scanned", Snackbar.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, SubmittingActivity.class)
                    .putExtra(SubmittingActivity.EXTRA_CHARITY_ID, charityId)
                    .putStringArrayListExtra(
                            SubmittingActivity.EXTRA_TOTE_IDS,
                            new ArrayList<>(viewModel.collectToteIds()))   // ➜ 自己写个 helper
                    .putExtra(SubmittingActivity.EXTRA_PHOTO_PATH,
                            viewModel.getFirstPhotoPathOrNull());

            startActivity(intent);
            finish();
        });

        binding.btnMockScan.setVisibility(View.VISIBLE);
        binding.btnMockScan.setOnClickListener(v -> simulateScan());
    }
    private void simulateScan() {
        String fakeCode = "TOTE-" + String.format(Locale.US, "%04d", fakeCounter++);
        viewModel.addScannedTote(fakeCode);
        Snackbar.make(binding.getRoot(), "已添加模拟条码：" + fakeCode, Snackbar.LENGTH_SHORT).show();
    }

    /* ------------ ZXing ------------- */
    private void launchBarcodeScanner() {
        new IntentIntegrator(this)
                .setOrientationLocked(true)
                .setPrompt("Scan tote barcode")
                .initiateScan();
    }

    @Override
    protected void onActivityResult(int rc, int res, Intent data) {
        super.onActivityResult(rc, res, data);
        IntentResult result = IntentIntegrator.parseActivityResult(rc, res, data);
        if (result != null && result.getContents() != null) {
            viewModel.addScannedTote(result.getContents());
        }
    }

    /* ------------ Photo (camera or gallery) ------------- */
    private void checkCameraPermissionThenLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            cameraPermLauncher.launch(Manifest.permission.CAMERA);
        } else {
            launchChooser();
        }
    }

    private final ActivityResultLauncher<String> cameraPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> { if (granted) launchChooser(); });

    private void launchChooser() {
        try {
            // camera intent
            File photoFile = createTempImageFile(this);
            photoUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", photoFile);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    .putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

            // gallery intent
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            Intent chooser = Intent.createChooser(cameraIntent, "Select or capture photo");
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{galleryIntent});
            chooserLauncher.launch(chooser);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private final ActivityResultLauncher<Intent> chooserLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() != RESULT_OK) return;
                Uri finalUri = (r.getData() != null && r.getData().getData() != null)
                        ? r.getData().getData()
                        : photoUri;
                if (finalUri != null) viewModel.addPhotoPath(finalUri.getPath());
            });

    private static File createTempImageFile(Context ctx) throws Exception {
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File dir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + stamp + "_", ".jpg", dir);
    }
}
