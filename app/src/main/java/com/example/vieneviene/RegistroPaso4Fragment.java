package com.example.vieneviene;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class RegistroPaso4Fragment extends Fragment {

    private RegistroEstacionamientoViewModel viewModel;
    private ActivityResultLauncher<Intent> galeriaLauncher;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_registro_paso4, container, false);

        viewModel = new ViewModelProvider(requireActivity())
                .get(RegistroEstacionamientoViewModel.class);

        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {

                            Uri imgUri = data.getData();

                            if (imgUri != null) {

                                viewModel.fotos.add(imgUri.toString());

                                Toast.makeText(getContext(), "Foto agregada", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        v.findViewById(R.id.btnAgregarFotos).setOnClickListener(btn -> abrirGaleria());

        // Siguiente paso
        v.findViewById(R.id.btnSiguiente).setOnClickListener(btn -> {
            ((RegistroEstacionamientoActivity) requireActivity())
                    .mostrarFragment(new RegistroPaso5Fragment());
        });

        // Atrás
        v.findViewById(R.id.btnAtras).setOnClickListener(x -> requireActivity().onBackPressed());

        return v;
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galeriaLauncher.launch(intent);
    }
}



