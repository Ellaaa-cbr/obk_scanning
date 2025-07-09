package com.example.obk.UI;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.obk.ObkApp;
import com.example.obk.R;
import com.example.obk.auth.AuthRepository;

import com.example.obk.network.dto.UserDto;
import com.example.obk.viewmodel.LoginViewModel;

/**
 * 负责登录，只需获取 UserDto 即跳转主界面。
 */
public class LoginActivity extends ComponentActivity {

    private EditText etUsername;
    private EditText etPassword;
    private ProgressBar progress;
    private Button btnLogin;

    private AuthRepository authRepository;
    private LoginViewModel viewModel;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // --- Bind UI
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        progress   = findViewById(R.id.progress);
        btnLogin   = findViewById(R.id.btnLogin);

        // --- DI / ViewModel
        authRepository = ((ObkApp) getApplication()).getAuthRepository();
        viewModel      = new ViewModelProvider(this).get(LoginViewModel.class);

        // --- Prefill from Settings (用户名 / 密码)
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        etUsername.setText(sp.getString("pref_username", ""));
        etPassword.setText(sp.getString("pref_password", ""));

        // --- 若已有有效 Token，直接进主界面
        if (authRepository.hasValidToken()) {
            goToMain();
            return;
        }

        btnLogin.setOnClickListener(v -> onLoginClicked());
    }

    /** 点击登录按钮后调用 */
    private void onLoginClicked() {
        btnLogin.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        authRepository.login(username, password)
                .thenAccept(user ->
                        runOnUiThread(() -> onLoginSuccess(user))
                )
                .exceptionally(err -> {
                    runOnUiThread(() -> onLoginError(err));
                    return null;
                });
    }

    /** 登录成功：保存用户信息并跳转主界面 */
    private void onLoginSuccess(UserDto user) {
        progress.setVisibility(View.GONE);
        btnLogin.setEnabled(true);

        viewModel.setUser(user);
        goToMain();
    }

    /** 登录失败：提示错误信息 */
    private void onLoginError(Throwable err) {
        progress.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
        // TODO: 用 Snackbar / Toast 显示 err.getMessage()
    }

    /** 进入主界面并关闭登录页 */
    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
