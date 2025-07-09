package com.example.obk.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.obk.network.dto.UserDto;


/**
 * 仅保存当前登录用户信息。
 */
public class LoginViewModel extends ViewModel {

    private final MutableLiveData<UserDto> currentUser = new MutableLiveData<>();

    /** 在登录成功后调用 */
    public void setUser(UserDto user) {
        currentUser.postValue(user);
    }

    /** 供界面层或其他组件观察用户信息 */
    public LiveData<UserDto> getUser() {
        return currentUser;
    }
}
