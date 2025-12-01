package com.example.vieneviene;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.vieneviene.api.ApiEstacionamiento;
import com.example.vieneviene.api.RetrofitCliente;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstacionamientoFragment extends Fragment {

    RecyclerView recycler;
    PropietarioEstacionamientoAdapter adapter;
    List<EstacionamientoPropietario> lista = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_estacionamiento, container, false);

        recycler = view.findViewById(R.id.recyclerEstacionamientos);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new PropietarioEstacionamientoAdapter(
                lista,
                requireContext(),
                estacionamiento -> {
                    Toast.makeText(
                            requireContext(),
                            "Seleccionaste: " + estacionamiento.getNombre(),
                            Toast.LENGTH_SHORT
                    ).show();

                }
        );

        recycler.setAdapter(adapter);

        cargarEstacionamientos();

        return view;
    }

    private void cargarEstacionamientos() {

        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_sesion", requireActivity().MODE_PRIVATE);

        int idUsuario = prefs.getInt("id_usuario", -1);

        Call<List<EstacionamientoPropietario>> call = api.getEstacionamientosPropietario(idUsuario);

        call.enqueue(new Callback<List<EstacionamientoPropietario>>() {
            @Override
            public void onResponse(Call<List<EstacionamientoPropietario>> call,
                                   Response<List<EstacionamientoPropietario>> response) {

                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Error " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                List<EstacionamientoPropietario> data = response.body();
                if (data != null) {
                    lista.clear();
                    lista.addAll(data);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<EstacionamientoPropietario>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
