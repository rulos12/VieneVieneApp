package com.example.vieneviene;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.vieneviene.api.ApiEstacionamiento;
import com.example.vieneviene.api.RetrofitCliente;
import com.example.vieneviene.modelos.LoginRequest;
import com.example.vieneviene.modelos.UsuarioDetalle;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {


    private TextInputEditText email;
    private TextInputEditText passwod;
    private Button btnLogin;
    private TextView register;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.etEmail);
        passwod = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        register = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> {
            String correo = email.getText().toString().trim();
            String password = passwod.getText().toString().trim();

            // Validación
            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            iniciarSesion(correo, password);
        });

        register.setOnClickListener(v -> {
             startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    protected void onStart() {
        super.onStart();

        // Recuperar los datos guardados en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("usuario_sesion", MODE_PRIVATE);
        boolean sesionActiva = prefs.getBoolean("sesion_activa", false);

        if (sesionActiva) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Evita volver al login
        }
    }
    private void iniciarSesion(String correo, String password) {
        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);
        LoginRequest request = new LoginRequest(correo, password);

        Call<UsuarioDetalle> call = api.login(request);
        call.enqueue(new Callback<UsuarioDetalle>() {
            @Override
            public void onResponse(Call<UsuarioDetalle> call, Response<UsuarioDetalle> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UsuarioDetalle usuario = response.body();
                    Toast.makeText(LoginActivity.this, "Bienvenido " + usuario.getNombre(), Toast.LENGTH_SHORT).show();

                    SharedPreferences prefs = getSharedPreferences("usuario_sesion", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("sesion_activa", true);
                    editor.putInt("id_usuario", usuario.getId_usuario());
                    editor.putString("nombre", usuario.getNombre());
                    editor.putString("correo", usuario.getCorreo());
                    editor.putString("rol", usuario.getRol());
                    editor.apply();
                    Log.d("LoginActivity", "Nombre guardado: " + usuario.getNombre());

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Cierra LoginActivity
                    // Aquí puedes ir a MainActivity
                } else {
                    Toast.makeText(LoginActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UsuarioDetalle> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}