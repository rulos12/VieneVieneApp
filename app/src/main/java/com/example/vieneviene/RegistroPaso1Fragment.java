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


public class RegistroPaso1Fragment extends Fragment {

    TextInputLayout layoutNombre;
    TextInputEditText editNombre;
    Button btnSiguiente;
    TextView btnAtras;

    RegistroEstacionamientoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_registro_paso1, container, false);

        layoutNombre = view.findViewById(R.id.layoutNombre);
        editNombre = view.findViewById(R.id.editNombre);
        btnSiguiente = view.findViewById(R.id.btnSiguiente);
        btnAtras = view.findViewById(R.id.btnAtras);

        viewModel = new ViewModelProvider(requireActivity()).get(RegistroEstacionamientoViewModel.class);

        if (viewModel.nombre != null) {
            editNombre.setText(viewModel.nombre);
        }

        btnSiguiente.setOnClickListener(v -> {

            String nombre = editNombre.getText().toString().trim();

            if (nombre.isEmpty()) {
                layoutNombre.setError("Ingresa un nombre válido");
                return;
            }

            layoutNombre.setError(null);

            viewModel.nombre = nombre;

            ((RegistroEstacionamientoActivity) requireActivity())
                    .mostrarFragment(new RegistroPaso2Fragment());
        });

        btnAtras.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }
}
