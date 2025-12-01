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
import com.example.vieneviene.modelos.RegisterRequest;
import com.example.vieneviene.modelos.UsuarioDetalle;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {


    private Button btnRegister;
    private TextInputEditText passwodText,passwod2Text,emailText, nameText, phoneText;

    private TextView btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        emailText = findViewById(R.id.emailText);
        passwodText = findViewById(R.id.password1Text);
        passwod2Text = findViewById(R.id.password2Text);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        nameText = findViewById(R.id.nameText);
        phoneText = findViewById(R.id.PhoneText);

        btnRegister.setOnClickListener(v -> {
            String correo = emailText.getText().toString().trim();
            String password = passwodText.getText().toString().trim();
            String password2 = passwod2Text.getText().toString().trim();
            String name = nameText.getText().toString().trim();
            String phone = phoneText.getText().toString().trim();


            // Validación
            if (name.isEmpty()||phone.isEmpty()||correo.isEmpty() || password.isEmpty()||password2.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.equals(password2)){
                registrarUsuario(name,correo, password,phone);
            }
        });

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

    }

    protected void onStart() {
        super.onStart();
        // Recuperar los datos guardados en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("usuario_sesion", MODE_PRIVATE);
        boolean sesionActiva = prefs.getBoolean("sesion_activa", false);

        // validacicón para verificar si existe un inicio de sesión
        if (sesionActiva) {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Evita volver al login
        }
    }

    public void registrarUsuario (String name,String email, String password,String telefono){
        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);


        RegisterRequest request = new RegisterRequest (name, email,telefono, password,"cliente");
        Call<UsuarioDetalle> call = api.register(request);

        call.enqueue(new Callback<UsuarioDetalle>() {
            @Override
            public void onResponse(Call<UsuarioDetalle> call, Response<UsuarioDetalle> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UsuarioDetalle usuario = response.body();
                    Toast.makeText(RegisterActivity.this, "Bienvenido " + usuario.getNombre(), Toast.LENGTH_SHORT).show();

                    SharedPreferences prefs = getSharedPreferences("usuario_sesion", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("sesion_activa", true);
                    editor.putInt("id_usuario", usuario.getId_usuario());
                    editor.putString("nombre", usuario.getNombre());
                    editor.putString("correo", usuario.getCorreo());
                    editor.apply();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Cierra LoginActivity
                } else {
                    Toast.makeText(RegisterActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UsuarioDetalle> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }
}