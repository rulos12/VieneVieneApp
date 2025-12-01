package com.example.vieneviene;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;
import java.util.Locale;


public class RegistroPaso5Fragment extends Fragment {

    private RegistroEstacionamientoViewModel viewModel;
    private TextInputEditText editHoraApertura, editHoraCierre, editPrecio;
    private TextView txtCapacidad;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_registro_paso5, container, false);

        viewModel = new ViewModelProvider(requireActivity())
                .get(RegistroEstacionamientoViewModel.class);

        txtCapacidad = v.findViewById(R.id.txtCapacidad);
        editPrecio = v.findViewById(R.id.editPrecio);

        editHoraApertura = v.findViewById(R.id.editHoraApertura);
        editHoraCierre   = v.findViewById(R.id.editHoraCierre);

        // Evitar teclado
        editHoraApertura.setInputType(InputType.TYPE_NULL);
        editHoraCierre.setInputType(InputType.TYPE_NULL);
        editHoraApertura.setFocusable(false);
        editHoraCierre.setFocusable(false);

        editHoraApertura.setOnClickListener(view -> mostrarTimePicker(editHoraApertura, true));
        editHoraCierre.setOnClickListener(view -> mostrarTimePicker(editHoraCierre, false));

        txtCapacidad.setText(String.valueOf(viewModel.capacidad));
        editPrecio.setText(viewModel.precioHora == 0 ? "" : String.valueOf(viewModel.precioHora));
        editHoraApertura.setText(viewModel.horaApertura);
        editHoraCierre.setText(viewModel.horaCierre);

        v.findViewById(R.id.btnSumar).setOnClickListener(btn -> {
            viewModel.capacidad++;
            txtCapacidad.setText(String.valueOf(viewModel.capacidad));
        });

        v.findViewById(R.id.btnRestar).setOnClickListener(btn -> {
            if (viewModel.capacidad > 1) {
                viewModel.capacidad--;
                txtCapacidad.setText(String.valueOf(viewModel.capacidad));
            }
        });

        v.findViewById(R.id.btnSiguiente).setOnClickListener(btn -> {

            // Validar precio
            String precioTxt = editPrecio.getText().toString().trim();
            if (precioTxt.isEmpty()) {
                editPrecio.setError("Ingresa un precio");
                return;
            }

            viewModel.precioHora = Double.parseDouble(precioTxt);

            // Validar horas
            if (viewModel.horaApertura == null || viewModel.horaApertura.isEmpty()) {
                Toast.makeText(getContext(), "Selecciona hora de apertura", Toast.LENGTH_SHORT).show();
                return;
            }
            if (viewModel.horaCierre == null || viewModel.horaCierre.isEmpty()) {
                Toast.makeText(getContext(), "Selecciona hora de cierre", Toast.LENGTH_SHORT).show();
                return;
            }

            ((RegistroEstacionamientoActivity) requireActivity())
                    .mostrarFragment(new RegistroPaso6Fragment());
        });


        v.findViewById(R.id.btnAtras).setOnClickListener(x -> requireActivity().onBackPressed());

        return v;
    }

    private void mostrarTimePicker(TextInputEditText editText, boolean apertura) {

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)  // Puedes usar CLOCK_24H si deseas
                .setHour(12)
                .setMinute(0)
                .setTitleText(apertura ? "Hora de apertura" : "Hora de cierre")
                .build();

        picker.addOnPositiveButtonClickListener(view -> {

            String hora = String.format("%02d:%02d %s",
                    picker.getHour() % 12 == 0 ? 12 : picker.getHour() % 12,
                    picker.getMinute(),
                    picker.getHour() >= 12 ? "PM" : "AM"
            );

            editText.setText(hora);

            if (apertura) viewModel.horaApertura = hora;
            else viewModel.horaCierre = hora;
        });

        picker.show(getParentFragmentManager(), "TIME_PICKER");
    }

}
