// File: com/example/obk/viewmodel/CourierCheckoutViewModel.java
package com.example.obk.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.obk.data.AppRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds scanned tote IDs and photo paths for the checkout screen.
 */
public class CourierCheckoutViewModel extends AndroidViewModel {

    private final AppRepository repo;
    private final MutableLiveData<List<String>> scannedTotes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> photoPaths   = new MutableLiveData<>(new ArrayList<>());

    public CourierCheckoutViewModel(@NonNull Application app) {
        super(app);
        repo = AppRepository.getInstance(app);
    }

    /* getters */
    public LiveData<List<String>> getScannedTotes() { return scannedTotes; }
    public LiveData<List<String>> getPhotoPaths()   { return photoPaths; }

    /* mutators */
    public void addScannedTote(String code) {
        List<String> list = new ArrayList<>(scannedTotes.getValue());
        if (!list.contains(code)) { list.add(code); scannedTotes.setValue(list); }
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
        repo.submitCheckout(charityId, scannedTotes.getValue(), photoFile);
        scannedTotes.setValue(new ArrayList<>());
        photoPaths.setValue(new ArrayList<>());
    }
}
