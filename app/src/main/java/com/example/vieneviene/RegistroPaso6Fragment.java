package com.example.vieneviene;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class RegistroPaso6Fragment extends Fragment implements OnMapReadyCallback {

        private GoogleMap mMap;
        private RegistroEstacionamientoViewModel viewModel;
        private LatLng marcadorActual;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View v = inflater.inflate(R.layout.fragment_registro_paso6, container, false);

            viewModel = new ViewModelProvider(requireActivity())
                    .get(RegistroEstacionamientoViewModel.class);

            SupportMapFragment map = (SupportMapFragment)
                    getChildFragmentManager().findFragmentById(R.id.mapContainer);

            map.getMapAsync(this);

            v.findViewById(R.id.btnSiguiente).setOnClickListener(btn -> {
                if (marcadorActual == null) {
                    Toast.makeText(getContext(), "Selecciona la ubicación", Toast.LENGTH_SHORT).show();
                    return;
                }

                viewModel.latitud = marcadorActual.latitude;
                viewModel.longitud = marcadorActual.longitude;

                ((RegistroEstacionamientoActivity) requireActivity())
                        .mostrarFragment(new RegistroPaso7Fragment());
            });

            return v;
        }

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;

            LatLng mx = new LatLng(19.81501, -97.36049);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mx, 14));

            mMap.setOnMapClickListener(latLng -> {
                mMap.clear();
                marcadorActual = latLng;
                mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación seleccionada"));
            });
        }
    }
