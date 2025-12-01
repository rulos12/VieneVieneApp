package com.example.vieneviene;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.vieneviene.api.ApiEstacionamiento;
import com.example.vieneviene.api.RetrofitCliente;
import com.example.vieneviene.modelos.ReservaCancelar;
import com.example.vieneviene.modelos.ReservaDetalle;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleReservaActivity extends AppCompatActivity {

    TextView tvNombreEst, tvRating, tvPropietario, tvLlegadaFecha, tvLlegadaHora,
            tvSalidaFecha, tvSalidaHora, tvVehiculo, tvPropietario2, tvDireccionEst, tvMonto;

    ImageView imgEst;
    Button btnCancelar;
    LinearLayout lyCodigoQR,lyEstacionamiento;

    int id_reserva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_reserva);

        id_reserva = getIntent().getIntExtra("id_reserva", -1);

        if (id_reserva == -1) {
            Toast.makeText(this, "Error: reserva no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        cargarDetalle();
    }

    private void initViews() {
        imgEst = findViewById(R.id.imgEst);
        tvNombreEst = findViewById(R.id.tvNombreEst);
        tvRating = findViewById(R.id.tvRating);
        tvPropietario = findViewById(R.id.tvPropietario);
        tvLlegadaFecha = findViewById(R.id.tvLlegadaFecha);
        tvLlegadaHora = findViewById(R.id.tvLlegadaHora);
        tvSalidaFecha = findViewById(R.id.tvSalidaFecha);
        tvSalidaHora = findViewById(R.id.tvSalidaHora);
        tvVehiculo = findViewById(R.id.tvVehiculo);
        tvPropietario2 = findViewById(R.id.tvPropietario2);
        tvDireccionEst = findViewById(R.id.tvDireccionEst);
        tvMonto = findViewById(R.id.tvMonto);
        btnCancelar = findViewById(R.id.btnCancelar);
        lyCodigoQR = findViewById(R.id.lyCodigoQR);
        lyEstacionamiento = findViewById(R.id.lyEstacionamiento);

    }

    private void cargarDetalle() {
        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);
        api.getReservaDetalle(id_reserva).enqueue(new Callback<ReservaDetalle>() {
            @Override
            public void onResponse(Call<ReservaDetalle> call, Response<ReservaDetalle> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(DetalleReservaActivity.this, "Error al cargar detalles", Toast.LENGTH_SHORT).show();
                    return;
                }
                ReservaDetalle d = response.body();

                String imagenUrl = null;

                if (d.fotos != null && !d.fotos.isEmpty()) {
                    imagenUrl = d.fotos.get(0); // primera imagen
                }
                Glide.with(DetalleReservaActivity.this)
                        .load(imagenUrl)
                        .placeholder(R.drawable.ic_google_background)
                        .error(R.drawable.ic_google_background)
                        .centerCrop()
                        .into(imgEst);

                // Estacionamiento
                tvNombreEst.setText(d.est_nombre);
                tvDireccionEst.setText(d.est_nombre);
                // Propietario
                tvPropietario.setText("Propietario: " + d.propietario_nombre);
                tvPropietario2.setText(d.propietario_nombre);
                tvLlegadaFecha.setText(d.hora_inicio.substring(0, 10));
                tvLlegadaHora.setText(d.hora_inicio.substring(11));
                tvSalidaFecha.setText(d.hora_fin.substring(0, 10));
                tvSalidaHora.setText(d.hora_fin.substring(11));
                // Vehículo
                tvVehiculo.setText(d.tipo + " " + d.modelo + " " + d.placas);
                double precio = d.precio_hora;
                tvMonto.setText("$" + precio + " MXN");
                btnCancelar.setOnClickListener(view -> mostrarDialogoCancelar());
                lyCodigoQR.setOnClickListener(v -> mostrarBottomSheetQR(String.valueOf(id_reserva)));
                lyEstacionamiento.setOnClickListener(v -> {
                    Intent intent = new Intent(DetalleReservaActivity.this, EstacionamientoDetalleActivity.class);
                    intent.putExtra("id_estacionamiento", d.id_estacionamiento);
                    startActivity(intent);
                });
            }

            @Override
            public void onFailure(Call<ReservaDetalle> call, Throwable t) {
                Toast.makeText(DetalleReservaActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelarReserva() {
        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);
        ReservaCancelar body = new ReservaCancelar("cancelada");
        api.cancelarReserva(id_reserva, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (!response.isSuccessful()) {
                    Toast.makeText(DetalleReservaActivity.this, "Error al cancelar", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(DetalleReservaActivity.this, "Reserva cancelada", Toast.LENGTH_SHORT).show();

                // Cerrar Activity y recargar historial
                finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(DetalleReservaActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
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

        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void mostrarDialogoCancelar() {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar reserva")
                .setMessage("¿Seguro que deseas cancelar esta reserva?")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> cancelarReserva())
                .setNegativeButton("No", null)
                .show();
    }

}