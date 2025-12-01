package com.example.vieneviene;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class indexActivity extends AppCompatActivity {


    private Button btnLogin, btnRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_index);

         btnLogin = findViewById(R.id.btnLogin);
         btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(indexActivity.this, LoginActivity.class));
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(indexActivity.this, RegisterActivity.class));
        });
    }

    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("usuario_sesion", MODE_PRIVATE);
        boolean sesionActiva = prefs.getBoolean("sesion_activa", false);

        if (sesionActiva) {
            Intent intent = new Intent(indexActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}