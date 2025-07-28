package com.example.obk.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.obk.data.AppRepository;

import java.io.File;
import java.util.List;

public class SubmitViewModel extends AndroidViewModel {

    private final AppRepository repo;
    private final MutableLiveData<SubmitState> submitState = new MutableLiveData<>(SubmitState.IDLE);

    public SubmitViewModel(@NonNull Application app) {
        super(app);
        repo = AppRepository.getInstance(app);
    }

    public LiveData<SubmitState> getSubmitState() { return submitState; }

    public void submit(String charityId,
                       List<String> toteIds,
                       String photoPath) {

        if (submitState.getValue() == SubmitState.SENDING) return; // 避免重复

        submitState.setValue(SubmitState.SENDING);

        repo.submitCheckout(charityId,
                toteIds,
                photoPath == null ? null : new File(photoPath),
                (success, err) -> {
                    if (success) {
                        submitState.postValue(SubmitState.SUCCESS);
                    } else {
                        submitState.postValue(SubmitState.ERROR);
                    }
                });
    }
}
