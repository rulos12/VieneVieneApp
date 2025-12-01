package com.example.vieneviene;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.vieneviene.api.ApiEstacionamiento;
import com.example.vieneviene.api.RetrofitCliente;
import com.example.vieneviene.modelos.EstacionamientoApi;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritosFragment extends Fragment {

    RecyclerView recycler;
    FavoritosAdapter adapter;
    ArrayList<EstacionamientoApi> listaFavs;

    ApiEstacionamiento api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favoritos, container, false);

        recycler = view.findViewById(R.id.recyclerFavoritos);

        listaFavs = new ArrayList<>();
        api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);

        recycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new FavoritosAdapter(getContext(), listaFavs);
        recycler.setAdapter(adapter);

        cargarFavoritos();

        return view;
    }

    private void cargarFavoritos() {
        SharedPreferences prefs = requireContext().getSharedPreferences("favoritos", Context.MODE_PRIVATE);

        Map<String, ?> favoritosGuardados = prefs.getAll();

        listaFavs.clear();

        for (String keyId : favoritosGuardados.keySet()) {
            int id = Integer.parseInt(keyId);

            api.getEstacionamiento(id).enqueue(new Callback<EstacionamientoApi>() {
                @Override
                public void onResponse(Call<EstacionamientoApi> call, Response<EstacionamientoApi> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        listaFavs.add(response.body());
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<EstacionamientoApi> call, Throwable t) {
                }
            });
        }
    }


}
