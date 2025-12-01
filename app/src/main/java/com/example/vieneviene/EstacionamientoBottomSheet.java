package com.example.vieneviene;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EstacionamientoBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_NOMBRE = "nombre";
    private static final String ARG_DIRECCION = "direccion";
    private static final String ARG_PRECIO = "precio";
    private static final String ARG_IMAGEN = "imagen_url";
    private static final String ARG_ID = "id_estacionamiento";

    boolean esFavorito = false;
    int idEstacionamiento;
    ImageView iconFavorito;

    public static EstacionamientoBottomSheet newInstance(int id, String nombre, String direccion, String precio, String  imagenUrl) {
        EstacionamientoBottomSheet fragment = new EstacionamientoBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putString(ARG_NOMBRE, nombre);
        args.putString(ARG_DIRECCION, direccion);
        args.putString(ARG_PRECIO, precio);
        args.putString(ARG_IMAGEN, imagenUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_estacionamiento, container, false);

        ImageView img = view.findViewById(R.id.imgEstacionamiento);
        TextView txtNombre = view.findViewById(R.id.txtNombre);
        TextView txtDireccion = view.findViewById(R.id.txtDireccion);
        TextView txtPrecio = view.findViewById(R.id.txtPrecio);
        Button btnReservar = view.findViewById(R.id.btnReservar);
        iconFavorito = view.findViewById(R.id.iconFavorito);

        Bundle args = getArguments();
        if (args != null) {
            idEstacionamiento = args.getInt(ARG_ID);
            txtNombre.setText(args.getString(ARG_NOMBRE));
            txtDireccion.setText(args.getString(ARG_DIRECCION));
            txtPrecio.setText(args.getString(ARG_PRECIO));

            String url = args.getString(ARG_IMAGEN);


            Glide.with(requireContext())
                    .load(url)
                    .placeholder(R.drawable.ic_google_background)
                    .error(R.drawable.ic_google_background)
                    .centerCrop()
                    .into(img);      }

        SharedPreferences prefs = requireContext().getSharedPreferences("favoritos", requireContext().MODE_PRIVATE);
        esFavorito = prefs.getBoolean(String.valueOf(idEstacionamiento), false);

        actualizarIcono();

        iconFavorito.setOnClickListener(v -> {
            esFavorito = !esFavorito;

            SharedPreferences.Editor editor = prefs.edit();

            if (esFavorito) {
                editor.putBoolean(String.valueOf(idEstacionamiento), true);
                Toast.makeText(getContext(), "Agregado a favoritos", Toast.LENGTH_SHORT).show();
            } else {
                editor.remove(String.valueOf(idEstacionamiento));
                Toast.makeText(getContext(), "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
            }

            editor.apply();
            actualizarIcono();
        });

        // Abrir la actividad del detalle
        btnReservar.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EstacionamientoDetalleActivity.class);
            intent.putExtra("id_estacionamiento", idEstacionamiento);
            startActivity(intent);
        });

        return view;
    }

    private void actualizarIcono() {
        if (esFavorito) {
            iconFavorito.setImageResource(R.drawable.ic_favoritos_activo);
        } else {
            iconFavorito.setImageResource(R.drawable.ic_favoritos);
        }
    }
}
