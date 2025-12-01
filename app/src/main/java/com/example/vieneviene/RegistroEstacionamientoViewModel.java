package com.example.vieneviene;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.vieneviene.api.ApiEstacionamiento;
import com.example.vieneviene.api.RetrofitCliente;
import com.example.vieneviene.modelos.ApiResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroEstacionamientoViewModel extends ViewModel {

    // Paso 1
    public String nombre = "";

    // Paso 2
    public String descripcion = "";

    // Paso 3
    public String direccion = "";
    public String ciudad = "";
    public String estado = "";
    public String codigo_postal = "";

    public double latitud = 0.0;
    public double longitud = 0.0;

    public List<String> fotos = new ArrayList<>();

    // Datos del estacionamiento
    public int capacidad = 1;
    public double precioHora = 0;
    public String horaApertura = "";
    public String horaCierre = "";

    private MutableLiveData<Boolean> _subiendoFoto = new MutableLiveData<>();
    public LiveData<Boolean> subiendoFoto = _subiendoFoto;

    private MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;


    public void subirFotosEnLote(Context context, int idEstacionamiento, Runnable onFinish) {

        Log.d("SUBIR_FOTO", " Iniciando subida de " + fotos.size() + " fotos");
        ApiEstacionamiento api = RetrofitCliente.obtener().create(ApiEstacionamiento.class);

        if (fotos.isEmpty()) {
            Log.w("SUBIR_FOTO", " No hay fotos para subir");
            onFinish.run();
            return;
        }

        AtomicInteger contador = new AtomicInteger(0);

        for (String uriStr : fotos) {

            Log.d("SUBIR_FOTO", " Procesando foto: " + uriStr);

            Uri uri = Uri.parse(uriStr);
            File file = uriToFile(context, uri);

            if (file == null || !file.exists()) {
                Log.e("SUBIR_FOTO", " ERROR: No se pudo convertir URI a file: " + uriStr);
                int c = contador.incrementAndGet();
                if (c == fotos.size()) onFinish.run();
                continue;
            }

            Log.d("SUBIR_FOTO", " Archivo listo: "
                    + file.getAbsolutePath() + " | size=" + file.length() + " bytes");

            // BODY ID
            RequestBody idRB = RequestBody.create(
                    okhttp3.MediaType.parse("text/plain"),
                    String.valueOf(idEstacionamiento)
            );

            // BODY DE IMAGEN
            RequestBody fileRB = RequestBody.create(
                    okhttp3.MediaType.parse("image/*"),
                    file
            );

            MultipartBody.Part bodyPart = MultipartBody.Part.createFormData(
                    "archivo",
                    file.getName(),
                    fileRB
            );

            Log.d("SUBIR_FOTO", " Enviando imagen → endpoint");

            api.subirFoto(idRB, bodyPart).enqueue(new Callback<ApiResponse>() {

                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    int subidas = contador.incrementAndGet();

                    Log.d("SUBIR_FOTO", " Respuesta HTTP: " + response.code());

                    if (!response.isSuccessful()) {

                        try {
                            String err = response.errorBody() != null ?
                                    response.errorBody().string() : "NO DATA";

                            Log.e("SUBIR_FOTO", " ERROR subida: " + err);
                            _error.postValue("Error subirFoto: " + err);

                        } catch (Exception e) {
                            Log.e("SUBIR_FOTO", " errorBody parse fail: " + e.getMessage());
                        }

                    } else {
                        Log.i("SUBIR_FOTO", " Foto subida correctamente");
                    }

                    if (subidas == fotos.size()) {
                        Log.d("SUBIR_FOTO", " TODAS subidas onFinish()");
                        onFinish.run();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    int subidas = contador.incrementAndGet();

                    Log.e("SUBIR_FOTO", " Retrofit failure: " + t.getMessage());
                    _error.postValue("Error retrofit: " + t.getMessage());

                    if (subidas == fotos.size()) {
                        Log.d("SUBIR_FOTO", " Fin con errores → onFinish()");
                        onFinish.run();
                    }
                }
            });
        }
    }

    private File uriToFile(Context context, Uri uri) {
        Log.d("SUBIR_FOTO", " Convirtiendo URI → File: " + uri);

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e("SUBIR_FOTO", " inputStream es NULL");
                return null;
            }

            File tempFile = File.createTempFile("upload_", ".jpg", context.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int len;

            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.close();
            inputStream.close();

            Log.d("SUBIR_FOTO", " File creado: " + tempFile.getAbsolutePath());
            return tempFile;

        } catch (Exception e) {
            Log.e("SUBIR_FOTO", " Error uriToFile: " + e.getMessage());
            return null;
        }
    }


}
