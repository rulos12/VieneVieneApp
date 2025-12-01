package com.example.vieneviene;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class RegistroPaso2Fragment extends Fragment {

    TextInputLayout layoutDescripcion;
    TextInputEditText editDescripcion;
    Button btnSiguiente;
    TextView btnAtras;

    RegistroEstacionamientoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_registro_paso2, container, false);

        layoutDescripcion = view.findViewById(R.id.layoutDescripcion);
        editDescripcion = view.findViewById(R.id.editDescripcion);
        btnSiguiente = view.findViewById(R.id.btnSiguiente);
        btnAtras = view.findViewById(R.id.btnAtras);

        viewModel = new ViewModelProvider(requireActivity()).get(RegistroEstacionamientoViewModel.class);

        if (viewModel.descripcion != null) {
            editDescripcion.setText(viewModel.descripcion);
        }

        btnSiguiente.setOnClickListener(v -> {

            String desc = editDescripcion.getText().toString().trim();

            if (desc.isEmpty()) {
                layoutDescripcion.setError("Ingresa una descripción");
                return;
            }

            layoutDescripcion.setError(null);

            viewModel.descripcion = desc;

            ((RegistroEstacionamientoActivity) requireActivity())
                    .mostrarFragment(new RegistroPaso3Fragment());
        });

        btnAtras.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }
}
