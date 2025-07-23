package com.example.obk.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.obk.data.AppRepository;
import com.example.obk.data.local.entity.ScannedTote;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds scanned tote IDs and photo paths for the checkout screen.
 */
public class CourierCheckoutViewModel extends AndroidViewModel {

    private final AppRepository repo;
    private final MutableLiveData<List<ScannedTote>> scannedTotes =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> photoPaths =
            new MutableLiveData<>(new ArrayList<>());

    public CourierCheckoutViewModel(@NonNull Application app) {
        super(app);
        repo = AppRepository.getInstance(app);
    }

    /* getters */
    public LiveData<List<ScannedTote>> getScannedTotes() {     // ★ 修正类型
        return scannedTotes;
    }
    public LiveData<List<String>> getPhotoPaths() {
        return photoPaths;
    }

    /* mutators */
    public void addScannedTote(String code) {
        List<ScannedTote> list = new ArrayList<>(scannedTotes.getValue());
        for (ScannedTote t : list) {
            if (t.code.equals(code)) return;   // 已存在
        }
        list.add(new ScannedTote(code));
        scannedTotes.setValue(list);
    }

    public void updateQty(String code, int newQty) {
        List<ScannedTote> list = new ArrayList<>(scannedTotes.getValue());
        for (ScannedTote t : list) {
            if (t.code.equals(code)) { t.qty = newQty; break; }
        }
        scannedTotes.setValue(list);
    }

    public void addPhotoPath(String path) {
        List<String> list = new ArrayList<>(photoPaths.getValue());
        list.add(path);
        photoPaths.setValue(list);
    }

    /* submit */
    public void submitCheckout(String charityId) {
        File photoFile = photoPaths.getValue().isEmpty()
                ? null
                : new File(photoPaths.getValue().get(0));

        /* ❶ 仍然沿用旧版接口：把 ScannedTote ⇒ 条码字符串 */
        List<String> ids = new ArrayList<>();
        for (ScannedTote t : scannedTotes.getValue()) ids.add(t.code);

        repo.submitCheckout(charityId, ids, photoFile);   // ← 不再报 List<> 类型不匹配

        scannedTotes.setValue(new ArrayList<>());
        photoPaths.setValue(new ArrayList<>());
    }

}
