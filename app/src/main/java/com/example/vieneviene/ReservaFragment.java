package com.example.vieneviene;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vieneviene.api.ApiEstacionamiento;
import com.example.vieneviene.api.RetrofitCliente;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ReservaFragment extends Fragment {

    private RecyclerView rvReservas;
    private ProgressBar progress;
    private TextView txtSinReservas;

    private ReservasAdapter adapter;
    private List<Reserva> lista = new ArrayList<>();

    private int idPropietario = 1;

    public ReservaFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reserva, container, false);

        rvReservas = view.findViewById(R.id.rvReservaciones);
        progress = view.findViewById(R.id.progressCargando);
        txtSinReservas = view.findViewById(R.id.txtSinReservas);

        rvReservas.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ReservasAdapter(lista, getContext(), reserva -> {
            mostrarBottomSheetPropietario(reserva);
        });
        rvReservas.setAdapter(adapter);

        cargarReservaciones();

        return view;
    }


    private void cargarReservaciones() {

        progress.setVisibility(View.VISIBLE);
        txtSinReservas.setVisibility(View.GONE);
        rvReservas.setVisibility(View.GONE);


        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_sesion", requireActivity().MODE_PRIVATE);
        idPropietario = prefs.getInt("id_usuario", -1);

        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);

        api.obtenerReservas(idPropietario).enqueue(new Callback<List<Reserva>>() {
            @Override
            public void onResponse(Call<List<Reserva>> call, Response<List<Reserva>> response) {

                progress.setVisibility(View.GONE);

                if (!response.isSuccessful()) {
                    txtSinReservas.setText("Error del servidor: " + response.code());
                    txtSinReservas.setVisibility(View.VISIBLE);
                    return;
                }

                List<Reserva> datos = response.body();

                if (datos == null || datos.isEmpty()) {
                    txtSinReservas.setVisibility(View.VISIBLE);
                    return;
                }

                lista.clear();
                lista.addAll(datos);
                adapter.notifyDataSetChanged();

                rvReservas.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<List<Reserva>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                txtSinReservas.setText("No se pudo conectar al servidor");
                txtSinReservas.setVisibility(View.VISIBLE);
            }
        });
    }

    private void mostrarBottomSheetPropietario(Reserva reserva) {

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(),
                com.google.android.material.R.style.Theme_Design_Light_BottomSheetDialog);

        View view = getLayoutInflater().inflate(R.layout.resumen_reserva_bottom_shee_propietariot, null);
        dialog.setContentView(view);

        TextView txtNombreCliente = view.findViewById(R.id.txtNombre);
        TextView txtNombreReal = view.findViewById(R.id.txtNombreCliente);
        TextView txtFechaInicio = view.findViewById(R.id.txtFechaInicio);
        TextView txtHoraInicio = view.findViewById(R.id.txtHoraInicio);
        TextView txtFechaFin = view.findViewById(R.id.txtFechaFin);
        TextView txtHoraFin = view.findViewById(R.id.txtHoraFin);
        TextView txtVehiculo = view.findViewById(R.id.txtVehiculo);
        TextView txtPrecioTotal = view.findViewById(R.id.txtPrecioTotal);

        Button btnCancelar = view.findViewById(R.id.btnCancelar);
        MaterialButton btnConfirmar = view.findViewById(R.id.btnConfirmar);

        txtNombreCliente.setText("Cliente");
        txtNombreReal.setText(reserva.getNombre_cliente());

        txtFechaInicio.setText(reserva.getFecha_reserva());
        txtHoraInicio.setText(reserva.getHora_inicio());

        txtFechaFin.setText(reserva.getFecha_reserva());
        txtHoraFin.setText(reserva.getHora_fin());

        txtVehiculo.setText(
                reserva.getTipo_vehiculo() + " - " +
                        reserva.getModelo_vehiculo() + " - " +
                        reserva.getPlacas()
        );

        txtPrecioTotal.setText("$" + reserva.getMonto_total()+ " MXN");

        btnCancelar.setOnClickListener(v -> {
            cancelarReserva(reserva);
            dialog.dismiss();
        });

        btnConfirmar.setOnClickListener(v -> {
            confirmarReserva(reserva);
            dialog.dismiss();
        });

        dialog.show();
    }
    private void confirmarReserva(Reserva reserva) {

        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);

        Map<String, String> body = new HashMap<>();
        body.put("estado", "confirmada");

        api.actualizarReserva(reserva.getId_reserva(), body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (!response.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "Error al confirmar: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Toast.makeText(requireContext(),
                                "Reserva confirmada correctamente",
                                Toast.LENGTH_SHORT).show();

                        cargarReservaciones();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(requireContext(),
                                "Error de conexión al confirmar",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cancelarReserva(Reserva reserva) {

        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);

        Map<String, String> body = new HashMap<>();
        body.put("estado", "cancelada");

        api.actualizarReserva(reserva.getId_reserva(), body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (!response.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "Error al cancelar: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Toast.makeText(requireContext(),
                                "Reserva cancelada correctamente",
                                Toast.LENGTH_SHORT).show();

                        cargarReservaciones();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(requireContext(),
                                "Error de conexión al cancelar",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
