// File: com/example/obk/viewmodel/CharitySelectViewModel.java
package com.example.obk.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.obk.data.AppRepository;
import com.example.obk.data.local.entity.Charity;

/**
 * ViewModel used only on SelectCharityActivity.
 */
public class CharitySelectViewModel extends AndroidViewModel {

    private final LiveData<java.util.List<Charity>> charities;

    public CharitySelectViewModel(@NonNull Application app) {
        super(app);
        charities = AppRepository.getInstance(app).getCharityList();
    }

    public LiveData<java.util.List<Charity>> getCharities() {
        return charities;
    }
}
