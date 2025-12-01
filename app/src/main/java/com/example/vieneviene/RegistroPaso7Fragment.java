package com.example.vieneviene;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vieneviene.api.ApiEstacionamiento;
import com.example.vieneviene.api.RetrofitCliente;
import com.example.vieneviene.modelos.ApiResponse;
import com.example.vieneviene.modelos.EstacionamientoCompleto;
import com.example.vieneviene.modelos.RespuestaRol;
import com.example.vieneviene.modelos.RolUpdate;
import com.example.vieneviene.modelos.Ubicacion;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RegistroPaso7Fragment extends Fragment {

    private RegistroEstacionamientoViewModel viewModel;
    private Button btnPublicar;
    private TextView txtNombre, txtDescripcion, txtDireccion;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_registro_paso7, container, false);

        viewModel = new ViewModelProvider(requireActivity())
                .get(RegistroEstacionamientoViewModel.class);

        txtNombre = v.findViewById(R.id.txtNombre);
        txtDescripcion = v.findViewById(R.id.txtDescripcion);
        txtDireccion = v.findViewById(R.id.txtDireccion);
        btnPublicar = v.findViewById(R.id.btnPublicar);

        // Mostrar datos
        txtNombre.setText(viewModel.nombre);
        txtDescripcion.setText(viewModel.descripcion);
        txtDireccion.setText(viewModel.direccion);

        v.findViewById(R.id.btnAtras).setOnClickListener(x ->
                requireActivity().onBackPressed()
        );

        btnPublicar.setOnClickListener(x -> enviarAPI());

        return v;
    }

    private void enviarAPI() {

        SharedPreferences prefs = requireActivity().getSharedPreferences(
                "usuario_sesion", requireActivity().MODE_PRIVATE);

        int idUsuario = prefs.getInt("id_usuario", -1);
        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error: usuario no encontrado", Toast.LENGTH_LONG).show();
            return;
        }
        Ubicacion u = new Ubicacion(
                viewModel.direccion,
                viewModel.ciudad,
                viewModel.estado,
                viewModel.codigo_postal,
                viewModel.latitud,
                viewModel.longitud
        );

        EstacionamientoCompleto body = new EstacionamientoCompleto(
                idUsuario,
                viewModel.nombre,
                viewModel.descripcion,
                viewModel.precioHora,
                viewModel.capacidad,
                viewModel.horaApertura,
                viewModel.horaCierre,
                u
        );

        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);

        api.crearEstacionamientoCompleto(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    int idEstacionamiento = response.body().id_estacionamiento;

                    Toast.makeText(getContext(),
                            "Estacionamiento creado, subiendo fotos...",
                            Toast.LENGTH_LONG).show();
                    Log.d("Estacionamiento debug","Estacionamiento creado, subiendo fotos");

                    viewModel.subirFotosEnLote(
                            requireContext(),
                            idEstacionamiento,
                            () -> cambiarRol(idUsuario)
                    );

                }else {
                    String err = "unknown";
                    try {
                        err = response.errorBody() != null ? response.errorBody().string() : "empty";
                    } catch (Exception e) { err = e.getMessage(); }
                    Toast.makeText(getContext(),
                            "Estacionamiento creado, pero no se pudo actualizar el rol. code=" + response.code() + " err=" + err,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }




    private void cambiarRol(int idUsuario) {

        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);
        RolUpdate body = new RolUpdate(idUsuario, "propietario");

        api.actualizarRol(body).enqueue(new Callback<RespuestaRol>() {
            @Override
            public void onResponse(Call<RespuestaRol> call, Response<RespuestaRol> response) {

                if (response.isSuccessful() && response.body() != null) {

                    SharedPreferences prefs = requireActivity().getSharedPreferences(
                            "usuario_sesion", requireActivity().MODE_PRIVATE);

                    prefs.edit().putString("rol", "propietario").apply();

                    Toast.makeText(getContext(),
                            "Tu estacionamiento ya está activo",
                            Toast.LENGTH_SHORT).show();

                    requireActivity().finish();

                } else {
                    Toast.makeText(getContext(),
                            "Estacionamiento creado, pero no se pudo actualizar el rol.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaRol> call, Throwable t) {
                Toast.makeText(getContext(),
                        "El estacionamiento se creó, pero fallo al actualizar rol: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

}

