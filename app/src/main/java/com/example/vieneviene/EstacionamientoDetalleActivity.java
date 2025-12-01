package com.example.vieneviene;

import static java.security.AccessController.getContext;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.vieneviene.api.ApiEstacionamiento;
import com.example.vieneviene.api.RetrofitCliente;
import com.example.vieneviene.modelos.EstacionamientoApi;
import com.example.vieneviene.modelos.ReservaRequest;
import com.example.vieneviene.modelos.ReservaResponse;
import com.example.vieneviene.modelos.VehiculoRequest;
import com.example.vieneviene.modelos.VehiculoResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstacionamientoDetalleActivity extends AppCompatActivity {

    TextView txtNombre, txtDireccion, txtPrecio, txtDescripcion,txtCapacidad;
    TextInputEditText editFecha, editHora,editPlacasVehiculo,editModeloVehiculo;
    AutoCompleteTextView editTiempo, editTipoVehiculo;
    ImageView imgEstacionamiento;
    Button btnReservar;
    ImageView iconFavorito;
    boolean esFavorito = false;
    int idEstacionamiento;
    double precioPorHora = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estacionamiento_detalle);

        txtNombre = findViewById(R.id.txtNombreDetalle);
        txtDireccion = findViewById(R.id.txtDireccionDetalle);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        txtCapacidad = findViewById(R.id.txtCapacidad);
        txtPrecio = findViewById(R.id.txtPrecioDetalle);
        imgEstacionamiento = findViewById(R.id.imgDetalleEstacionamiento);
        btnReservar = findViewById(R.id.btnConfirmarReserva);
        editFecha = findViewById(R.id.editFecha);
        editHora  = findViewById(R.id.editHora);
        editTiempo = findViewById(R.id.editTiempo);
        editPlacasVehiculo = findViewById(R.id.editPlacasVehiculo);
        editModeloVehiculo = findViewById(R.id.editModeloVehiculo);
        editTipoVehiculo = findViewById(R.id.editTipoVehiculo);

        iconFavorito = findViewById(R.id.iconFavorito);
        idEstacionamiento = getIntent().getIntExtra("id_estacionamiento", -1);
        SharedPreferences prefs = getSharedPreferences("favoritos", MODE_PRIVATE);
        esFavorito = prefs.getBoolean(String.valueOf(idEstacionamiento), false);
        actualizarIcono();

        iconFavorito.setOnClickListener(v -> {
            esFavorito = !esFavorito;

            SharedPreferences.Editor editor = prefs.edit();

            if (esFavorito) {
                editor.putBoolean(String.valueOf(idEstacionamiento), true);
                Toast.makeText(this, "Agregado a favoritos", Toast.LENGTH_SHORT).show();
            } else {
                editor.remove(String.valueOf(idEstacionamiento));
                Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
            }

            editor.apply();
            actualizarIcono();
        });


        btnReservar.setOnClickListener(v -> {
            String fecha = editFecha.getText().toString();
            String hora = editHora.getText().toString();
            String tiempo = editTiempo.getText().toString();
            String tipoVehiculo = editTipoVehiculo.getText().toString();
            String modeloVehiculo = editModeloVehiculo.getText().toString();
            String placaVehiculo = editPlacasVehiculo.getText().toString();

            if (fecha.isEmpty() || hora.isEmpty() || tiempo.isEmpty()
                    || tipoVehiculo.isEmpty() || modeloVehiculo.isEmpty() || placaVehiculo.isEmpty()) {

                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validarPlaca(placaVehiculo)) {
                editPlacasVehiculo.setError("Placa inválida. Ej: ABC123A");
                Toast.makeText(this, "Formato de placa incorrecto", Toast.LENGTH_SHORT).show();
                return;
            }

            // Mostrar BottomSheet con el resumen
            mostrarBottomSheetResumen(fecha, hora, tiempo, tipoVehiculo, modeloVehiculo, placaVehiculo);
        });


        //calendario
        editFecha.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePicker = new DatePickerDialog(
                    EstacionamientoDetalleActivity.this,
                    (view1, year, month, dayOfMonth) -> {

                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                        editFecha.setText(sdf.format(selectedDate.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePicker.show();
        });

        //Reloj
        editHora.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Selecciona la hora")
                    .build();

            picker.addOnPositiveButtonClickListener(view -> {
                String hora = String.format("%02d:%02d", picker.getHour(), picker.getMinute());
                editHora.setText(hora);
            });

            picker.show(getSupportFragmentManager(), "TIME_PICKER");
        });
        //lista de horas
        String[] tiempos = {"30 minutos", "1 hora", "2 horas", "3 horas", "4 horas"};
        String[] tipoAuto = {"Auto", "Moto", "Camioneta"};


        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                tiempos
        );
        editTiempo.setAdapter(adapter);
        editTiempo.setOnItemClickListener((parent, view, position, id) -> {
            actualizarPrecio();
        });

        android.widget.ArrayAdapter<String> adapterTipoVehiculo = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                tipoAuto
        );
        editTipoVehiculo.setAdapter(adapterTipoVehiculo);

        int id = getIntent().getIntExtra("id_estacionamiento", -1);

        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);

        api.getEstacionamiento(id).enqueue(new Callback<EstacionamientoApi>() {
            @Override
            public void onResponse(Call<EstacionamientoApi> call, Response<EstacionamientoApi> response) {
                if (response.isSuccessful()) {
                    EstacionamientoApi e = response.body();

                    txtNombre.setText(e.nombre);
                    txtDireccion.setText(e.direccion + ", " + e.ciudad+ ", " + e.estado);

                    txtDescripcion.setText(e.descripcion);
                    txtCapacidad.setText(String.valueOf(e.capacidad));
                    precioPorHora = e.precio_hora;
                    txtPrecio.setText("$" + (int) precioPorHora + " MXN");
                    Glide.with(EstacionamientoDetalleActivity.this)
                            .load(e.foto_principal) // ya normalizada
                            .placeholder(R.drawable.ic_google_background)
                            .error(R.drawable.ic_google_background)
                            .centerCrop()
                            .into(imgEstacionamiento);

                }
            }

            @Override
            public void onFailure(Call<EstacionamientoApi> call, Throwable t) {
                Toast.makeText(EstacionamientoDetalleActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });


    }
    private boolean validarPlaca(String placa) {
        placa = placa.toUpperCase().replace("-", "").replace(" ", "");

        return placa.matches("^[A-Z0-9]{5,8}$");
    }
    private void mostrarBottomSheetResumen(String fecha, String hora, String tiempo,
                                           String tipoVehiculo, String modeloVehiculo, String placaVehiculo) {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.resumen_reserva_bottom_sheet, null);
      //  dialog.setContentView(bottomSheetView);
        // Configurar el BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this,R.style.BottomSheetFullScreen);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextView txtNombre = bottomSheetView.findViewById(R.id.txtNombre);
        TextView txtDireccion = bottomSheetView.findViewById(R.id.txtDireccion);
        TextView txtFechaInicio = bottomSheetView.findViewById(R.id.txtFechaInicio);
        TextView txtHoraInicio = bottomSheetView.findViewById(R.id.txtHoraInicio);
        TextView txtFechaFin = bottomSheetView.findViewById(R.id.txtFechaFin);
        TextView txtHoraFin = bottomSheetView.findViewById(R.id.txtHoraFin);
        TextView txtVehiculo = bottomSheetView.findViewById(R.id.txtVehiculo);
        TextView txtPrecioTotal = bottomSheetView.findViewById(R.id.txtPrecioTotal);

        Button btnCancelar = bottomSheetView.findViewById(R.id.btnCancelar);
        MaterialButton btnConfirmar = bottomSheetView.findViewById(R.id.btnConfirmar);

        txtNombre.setText(this.txtNombre.getText().toString());  // Usar "this." para referirse al TextView de la actividad
        txtDireccion.setText(this.txtDireccion.getText().toString());  // Usar "this." para referirse al TextView de la actividad
        txtFechaInicio.setText(fecha);
        txtHoraInicio.setText(hora);

        String[] fechaHoraFin = calcularFechaHoraFin(fecha, hora, tiempo);
        txtFechaFin.setText(fechaHoraFin[0]);
        txtHoraFin.setText(fechaHoraFin[1]);

        String vehiculoInfo = tipoVehiculo + " - " + modeloVehiculo + " - " + placaVehiculo;
        txtVehiculo.setText(vehiculoInfo);

        double precioTotal = calcularPrecioTotal();
        txtPrecioTotal.setText(String.format("$%.2f MXN", precioTotal, "impuestos incluidos"));

        btnCancelar.setOnClickListener(v -> bottomSheetDialog.dismiss());
        btnConfirmar.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();

            registrarVehiculoYCrearReserva(
                    tipoVehiculo,
                    modeloVehiculo,
                    placaVehiculo,
                    fecha,
                    hora,
                    tiempo,
                    precioTotal
            );
        });
        bottomSheetDialog.show();
    }

    private String[] calcularFechaHoraFin(String fecha, String hora, String tiempo) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
            String fechaHoraInicio = fecha + " " + hora;
            Date inicio = dateFormat.parse(fechaHoraInicio);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(inicio);

            switch (tiempo) {
                case "30 minutos":
                    calendar.add(Calendar.MINUTE, 30);
                    break;
                case "1 hora":
                    calendar.add(Calendar.HOUR, 1);
                    break;
                case "2 horas":
                    calendar.add(Calendar.HOUR, 2);
                    break;
                case "3 horas":
                    calendar.add(Calendar.HOUR, 3);
                    break;
                case "4 horas":
                    calendar.add(Calendar.HOUR, 4);
                    break;
            }

            Date fin = calendar.getTime();

            SimpleDateFormat fechaFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            SimpleDateFormat horaFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            return new String[]{
                    fechaFormat.format(fin),
                    horaFormat.format(fin)
            };

        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{fecha, hora};
        }
    }
    private void actualizarIcono() {
        if (esFavorito) {
            iconFavorito.setImageResource(R.drawable.ic_favoritos_activo);
        } else {
            iconFavorito.setImageResource(R.drawable.ic_favoritos);
        }
    }
    private void crearReservaEnAPI(String fecha, String hora, String tiempo,
                                   String tipoVehiculo, String modeloVehiculo,
                                   String placaVehiculo, double precioTotal,
                                   int idVehiculo, int idUsuario) {

        int idEst = idEstacionamiento;

        // Calcular final
        String[] fechaHoraFin = calcularFechaHoraFin(fecha, hora, tiempo);
        String horaFin = fechaHoraFin[1];
        String fechaForm = "";
        try {
            SimpleDateFormat entrada = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            SimpleDateFormat salida = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date fechaConvertida = entrada.parse(fecha);
            fechaForm = salida.format(fechaConvertida);
        } catch (Exception e) {
            e.printStackTrace();
            fechaForm = fecha;
        }

        ReservaRequest reserva = new ReservaRequest(
                idUsuario,
                idEst,
                idVehiculo,
                fechaForm,
                hora,
                horaFin,
                precioTotal,
                "pendiente"
        );


        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);

        api.crearReserva(reserva).enqueue(new Callback<ReservaResponse>() {
            @Override
            public void onResponse(Call<ReservaResponse> call, Response<ReservaResponse> response) {
                if (response.isSuccessful()) {

                    int id = response.body().id_reserva;

                    Toast.makeText(EstacionamientoDetalleActivity.this,
                            "Reserva creada correctamente", Toast.LENGTH_SHORT).show();

                    mostrarBottomSheetQR(String.valueOf(id));
                } else {
                    Toast.makeText(EstacionamientoDetalleActivity.this,
                            "Error: hora reservada", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReservaResponse> call, Throwable t) {
                Toast.makeText(EstacionamientoDetalleActivity.this,
                        "Error en la conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registrarVehiculoYCrearReserva(String tipo, String modelo, String placas,
                                                String fecha, String hora, String tiempo, double precioTotal) {

        SharedPreferences prefs = getSharedPreferences("usuario_sesion", MODE_PRIVATE);
        int idUsuario = prefs.getInt("id_usuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Error: No se encontró la sesión del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        VehiculoRequest vehiculo = new VehiculoRequest(idUsuario, tipo, modelo, placas);

        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);

        api.registrarVehiculo(vehiculo).enqueue(new Callback<VehiculoResponse>() {
            @Override
            public void onResponse(Call<VehiculoResponse> call, Response<VehiculoResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(EstacionamientoDetalleActivity.this,
                            "Error al registrar vehículo", Toast.LENGTH_SHORT).show();
                    return;
                }

                int idVehiculo = response.body().id_vehiculo;

                crearReservaEnAPI(
                        fecha, hora, tiempo,
                        tipo, modelo, placas,
                        precioTotal,
                        idVehiculo,
                        idUsuario
                );
            }

            @Override
            public void onFailure(Call<VehiculoResponse> call, Throwable t) {
                Toast.makeText(EstacionamientoDetalleActivity.this,
                        "Error de conexión al registrar vehículo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarBottomSheetQR(String codigoQR) {

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetFullScreen);
        View view = getLayoutInflater().inflate(R.layout.bottomsheet_qr, null);
        dialog.setContentView(view);

        ImageView imgQR = view.findViewById(R.id.imgCodigoQR);
        Button btnCerrar = view.findViewById(R.id.btnCerrarQR);

        // Generar QR
        try {
            com.google.zxing.qrcode.QRCodeWriter writer = new com.google.zxing.qrcode.QRCodeWriter();
            int size = 500;
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(codigoQR,
                    com.google.zxing.BarcodeFormat.QR_CODE, size, size);

            android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
                }
            }
            imgQR.setImageBitmap(bmp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnCerrar.setOnClickListener(
                v -> {
                    Intent intent = new Intent(this, DetalleReservaActivity.class);
                    intent.putExtra("id_reserva", Integer.parseInt(codigoQR));
                    startActivity(intent);
                });
        dialog.show();
    }

    private void actualizarPrecio() {
        String tiempo = editTiempo.getText().toString();
        if (tiempo.isEmpty() || precioPorHora == 0) return;
        double horas = 1; // valor por defecto
        switch (tiempo) {
            case "30 minutos":
                horas = 0.5;
                break;
            case "1 hora":
                horas = 1;
                break;
            case "2 horas":
                horas = 2;
                break;
            case "3 horas":
                horas = 3;
                break;
            case "4 horas":
                horas = 4;
                break;
        }
        double total = horas * precioPorHora;
        txtPrecio.setText("$" + String.format("%.2f",  total) + " MXN");
    }

    private double calcularPrecioTotal() {
        String tiempo = editTiempo.getText().toString();
        if (tiempo.isEmpty() || precioPorHora == 0) return 0;

        double horas = 1; // valor por defecto
        switch (tiempo) {
            case "30 minutos":
                horas = 0.5;
                break;
            case "1 hora":
                horas = 1;
                break;
            case "2 horas":
                horas = 2;
                break;
            case "3 horas":
                horas = 3;
                break;
            case "4 horas":
                horas = 4;
                break;
        }
        return horas * precioPorHora;
    }




}
