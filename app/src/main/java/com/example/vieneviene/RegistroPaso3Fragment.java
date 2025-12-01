package com.example.vieneviene;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputEditText;


public class RegistroPaso3Fragment extends Fragment {

    private TextInputEditText editDireccion, editCiudad, editEstado, editCP;
    private RegistroEstacionamientoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_registro_paso3, container, false);

        editDireccion = v.findViewById(R.id.editDireccion);
        editCiudad = v.findViewById(R.id.editCiudad);
        editEstado = v.findViewById(R.id.editEstado);
        editCP = v.findViewById(R.id.editCP);

        viewModel = new ViewModelProvider(requireActivity())
                .get(RegistroEstacionamientoViewModel.class);

        // Restaurar datos
        editDireccion.setText(viewModel.direccion);
        editCiudad.setText(viewModel.ciudad);
        editEstado.setText(viewModel.estado);
        editCP.setText(viewModel.codigo_postal);

        v.findViewById(R.id.btnSiguiente).setOnClickListener(btn -> {

            viewModel.direccion = editDireccion.getText().toString();
            viewModel.ciudad = editCiudad.getText().toString();
            viewModel.estado = editEstado.getText().toString();
            viewModel.codigo_postal = editCP.getText().toString();

            ((RegistroEstacionamientoActivity) requireActivity())
                    .mostrarFragment(new RegistroPaso4Fragment());
        });

        return v;
    }
}


