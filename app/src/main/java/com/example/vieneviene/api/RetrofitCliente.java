package com.example.vieneviene.api;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class RetrofitCliente {
    private static Retrofit instancia;
    public static Retrofit obtener(){
        if (instancia == null){
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(log).build();
            instancia = new Retrofit.Builder()
                    //.baseUrl("http://192.168.0.237:8000/")
                    .baseUrl("http://192.168.1.79:8000/")
                  //  .baseUrl("http://172.16.23.107:8000/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instancia;
    }
}
