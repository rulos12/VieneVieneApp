package com.example.vieneviene;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.vieneviene.api.ApiEstacionamiento;
import com.example.vieneviene.api.RetrofitCliente;
import com.example.vieneviene.modelos.EstacionamientoApi;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuscarFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView txtNombre, txtDireccion, txtPrecio;
    private ImageView imgEstacionamiento;
    private Button btnReservar;

    private HashMap<Marker, Estacionamiento> estacionamientoMap = new HashMap<>();

    public BuscarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buscar, container, false);


        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }



        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        cargarEstacionamientosDesdeAPI();

        // Evento al hacer clic en marcador
        mMap.setOnMarkerClickListener(marker -> {
            Estacionamiento e = estacionamientoMap.get(marker);
            if (e != null) {
                mostrarBottomSheet(e);
            }
            return true;
        });
    }


    private void agregarMarcador(Estacionamiento e) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(e.getUbicacion())
                .title(e.getNombre()));
        estacionamientoMap.put(marker, e);
    }
    private void cargarEstacionamientosDesdeAPI() {

        ApiEstacionamiento apiService = RetrofitCliente.obtener().create(ApiEstacionamiento.class);
        apiService.getEstacionamientos().enqueue(new Callback<List<EstacionamientoApi>>() {

            @Override
            public void onResponse(Call<List<EstacionamientoApi>> call, Response<List<EstacionamientoApi>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<EstacionamientoApi> lista = response.body();

                    for (EstacionamientoApi apiE : lista) {

                        Estacionamiento estacionamiento = new Estacionamiento(
                                apiE.id_estacionamiento,
                                apiE.nombre,
                                apiE.direccion,
                                "$" + apiE.precio_hora + " por hora",
                                new LatLng(apiE.latitud, apiE.longitud),
                                apiE.foto_principal

                        );

                        Log.d("API_IMAGE", "Estacionamiento " + apiE.id_estacionamiento +
                                " => " + apiE.foto_principal);

                        agregarMarcador(estacionamiento);
                    }

                    // Centrar la cámara en el primer estacionamiento
                    if (!lista.isEmpty()) {
                        EstacionamientoApi first = lista.get(0);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(first.latitud, first.longitud),
                                15
                        ));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<EstacionamientoApi>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }



    private void mostrarBottomSheet(Estacionamiento e) {
        EstacionamientoBottomSheet bottomSheet = EstacionamientoBottomSheet.newInstance(
                        e.getId(),
                        e.getNombre(),
                        e.getDireccion(),
                        e.getPrecio(),
                        e.getImagenUrl()
                );

        bottomSheet.show(getParentFragmentManager(), "EstacionamientoBottomSheet");

    }

}
