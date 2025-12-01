package com.example.vieneviene;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.vieneviene.api.ApiEstacionamiento;
import com.example.vieneviene.api.RetrofitCliente;
import com.example.vieneviene.modelos.ReservaHistorial;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HistorialFragment extends Fragment {

    RecyclerView recycler;
    HistorialAdapter adapter;
    List<ReservaHistorial> lista = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_historial, container, false);

        recycler = v.findViewById(R.id.rvHistorial);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HistorialAdapter(lista, this::abrirDetalle);
        recycler.setAdapter(adapter);

        cargarReservas();

        return v;
    }
    private void cargarReservas() {

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_sesion", getContext().MODE_PRIVATE);

        int idUsuario = prefs.getInt("id_usuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error: usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);

        Call<List<ReservaHistorial>> call = api.getReservasHistorial(idUsuario);

        call.enqueue(new Callback<List<ReservaHistorial>>() {
            @Override
            public void onResponse(Call<List<ReservaHistorial>> call,
                                   Response<List<ReservaHistorial>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    lista.clear();

                    // Filtrar todas las reservas cuyo estado NO sea "cancelada"
                    for (ReservaHistorial r : response.body()) {
                        if (!r.estado.equalsIgnoreCase("cancelada")) {
                            lista.add(r);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ReservaHistorial>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirDetalle(ReservaHistorial r) {

        Intent intent = new Intent(getContext(), DetalleReservaActivity.class);
        intent.putExtra("id_reserva", r.id_reserva);
        startActivity(intent);
    }

}
