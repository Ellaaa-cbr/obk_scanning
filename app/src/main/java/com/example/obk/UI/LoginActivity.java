package com.example.obk.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.obk.R;
import com.example.obk.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // 获取 ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // 观察登录结果
        viewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                // 登录成功，跳转 MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        // 观察错误信息
        viewModel.getErrorMsg().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        // 登录按钮点击事件
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            viewModel.login(email, password);
        });
    }
}
