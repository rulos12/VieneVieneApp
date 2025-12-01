package com.example.vieneviene;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vieneviene.api.ApiEstacionamiento;
import com.example.vieneviene.api.RetrofitCliente;

import com.example.vieneviene.modelos.EstadisticasResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {


    private TextView tvNombre, tvReservas, tvHoras,tvRol;

    private Button btnConvertirPropietario,btnCambiarModo;
    public PerfilFragment() {
    }
    public static PerfilFragment newInstance(String param1, String param2) {
        PerfilFragment fragment = new PerfilFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        View linearCerrarSesion = view.findViewById(R.id.logout);
        tvNombre = view.findViewById(R.id.tvNombre);
        tvReservas = view.findViewById(R.id.tvReservas);
        tvHoras = view.findViewById(R.id.tvHoras);
        tvRol = view.findViewById(R.id.tvRol);


        linearCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });


        btnConvertirPropietario = view.findViewById(R.id.btnConvertirPropietario);
        btnCambiarModo = view.findViewById(R.id.btnCambiarModo);

        configurarBotones();
        mostrarNombreUsuario();
        cargarEstadisticas();

        btnConvertirPropietario.setOnClickListener(v -> verificarRolUsuario());


        return view;
    }
    public void onResume() {
        super.onResume();
        cargarEstadisticas();
    }
    private void cerrarSesion() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_sesion", requireActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
    private void mostrarNombreUsuario() {
        try {
            SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_sesion", requireActivity().MODE_PRIVATE);

            // Verificar todos los valores
            boolean sesionActiva = prefs.getBoolean("sesion_activa", false);
            int idUsuario = prefs.getInt("id_usuario", -1);
            String nombre = prefs.getString("nombre", "No encontrado");
            String correo = prefs.getString("correo", "No encontrado");
            String rol = prefs.getString("rol", "cliente");


            Log.d(TAG, "=== DEBUG SharedPreferences ===");
            Log.d(TAG, "sesion_activa: " + sesionActiva);
            Log.d(TAG, "id_usuario: " + idUsuario);
            Log.d(TAG, "nombre: " + nombre);
            Log.d(TAG, "correo: " + correo);
            Log.d(TAG, "===============================");

            if (tvNombre != null) {
                tvNombre.setText(nombre);
                tvRol.setText(rol);
                Log.d(TAG, "Texto asignado al TextView: " + nombre);
            } else {
                Log.e(TAG, "tvNombre es null");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error en mostrarNombreUsuario: " + e.getMessage());
            if (tvNombre != null) {
                tvNombre.setText("Error cargando nombre");
            }
        }
    }
    private void cargarEstadisticas() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_sesion", requireActivity().MODE_PRIVATE);
        int idUsuario = prefs.getInt("id_usuario", -1);

        if (idUsuario == -1) {
            Log.e("PerfilFragment", "ID de usuario no encontrado");
            return;
        }
        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);
        Call<EstadisticasResponse> call = api.getEstadisticas(idUsuario);
        call.enqueue(new Callback<EstadisticasResponse>() {
            @Override
            public void onResponse(Call<EstadisticasResponse> call, Response<EstadisticasResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    EstadisticasResponse data = response.body();

                    tvReservas.setText(String.valueOf(data.total_reservas));
                    tvHoras.setText(String.valueOf(data.horas_totales));

                } else {
                    Log.e("PerfilFragment", "Error en la API: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EstadisticasResponse> call, Throwable t) {
                Log.e("PerfilFragment", "Fallo al conectar: " + t.getMessage());
            }
        });
    }
    private void verificarRolUsuario() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_sesion", Context.MODE_PRIVATE);

        String rol = prefs.getString("rol", "cliente");

        if (rol.equals("cliente")) {
            startActivity(new Intent(getActivity(), RegistroEstacionamientoActivity.class));

        } else if (rol.equals("propietario")) {
            Toast.makeText(getContext(), "Ya eres propietario", Toast.LENGTH_SHORT).show();
        }
    }

    private void configurarBotones() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_sesion", Context.MODE_PRIVATE);

        String rol = prefs.getString("rol", "cliente");
        String modo = prefs.getString("modo_actual", "cliente");

        btnConvertirPropietario.setVisibility(rol.equals("cliente") ? View.VISIBLE : View.GONE);

        if (rol.equals("propietario")) {

            btnCambiarModo.setVisibility(View.VISIBLE);

            if (modo.equals("cliente")) {
                btnCambiarModo.setText("Cambiar a modo Propietario");
            } else {
                btnCambiarModo.setText("Cambiar a modo Cliente");
            }

            btnCambiarModo.setOnClickListener(v -> cambiarModo());

        } else {
            btnCambiarModo.setVisibility(View.GONE);
        }
    }
    private void cambiarModo() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_sesion", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String modoActual = prefs.getString("modo_actual", "cliente");

        if (modoActual.equals("cliente")) {
            editor.putString("modo_actual", "propietario");
            editor.apply();
            startActivity(new Intent(requireActivity(), MainActivity2.class));

        } else {
            editor.putString("modo_actual", "cliente");
            editor.apply();
            startActivity(new Intent(requireActivity(), MainActivity.class));
        }

        requireActivity().finish();
    }
}