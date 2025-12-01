package com.example.vieneviene.api;

import com.example.vieneviene.EstacionamientoPropietario;
import com.example.vieneviene.Reserva;
import com.example.vieneviene.modelos.ApiResponse;
import com.example.vieneviene.modelos.EstacionamientoApi;
import com.example.vieneviene.modelos.EstacionamientoCompleto;
import com.example.vieneviene.modelos.EstadisticasResponse;
import com.example.vieneviene.modelos.LoginRequest;
import com.example.vieneviene.modelos.RegisterRequest;
import com.example.vieneviene.modelos.ReservaCancelar;
import com.example.vieneviene.modelos.ReservaDetalle;
import com.example.vieneviene.modelos.ReservaHistorial;
import com.example.vieneviene.modelos.ReservaRequest;
import com.example.vieneviene.modelos.ReservaResponse;
import com.example.vieneviene.modelos.RespuestaRol;
import com.example.vieneviene.modelos.RolUpdate;
import com.example.vieneviene.modelos.UsuarioDetalle;
import com.example.vieneviene.modelos.VehiculoRequest;
import com.example.vieneviene.modelos.VehiculoResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;


public interface ApiEstacionamiento {
    @POST("loginUsuario")
    Call<UsuarioDetalle> login(@Body LoginRequest datos);

    @POST("registerUsuario")
    Call<UsuarioDetalle> register(@Body RegisterRequest datos2);

    @GET("getEstacionamientosUbicacion")
    Call<List<EstacionamientoApi>> getEstacionamientos();

    @GET("getEstacionamiento/{id}")
    Call<EstacionamientoApi> getEstacionamiento(@Path("id") int id);

    @POST("/createReserva")
    Call<ReservaResponse> crearReserva(@Body ReservaRequest reserva);

    @POST("vehiculo/registrar")
    Call<VehiculoResponse> registrarVehiculo(@Body VehiculoRequest register);

    @GET("getReservasHistorial/{id_usuario}")
    Call<List<ReservaHistorial>> getReservasHistorial(@Path("id_usuario") int idUsuario);

    @GET("getReservaDetalle/{id}")
    Call<ReservaDetalle> getReservaDetalle(@Path("id") int id);

    @PUT("cancelarReserva/{id}")
    Call<ResponseBody> cancelarReserva(@Path("id") int id, @Body ReservaCancelar body);

    @GET("estadisticas/{cliente_id}")
    Call<EstadisticasResponse> getEstadisticas(@Path("cliente_id") int clienteId);

    @POST("createEstacionamientoCompleto")
    Call<ApiResponse> crearEstacionamientoCompleto(@Body EstacionamientoCompleto datos);

    @PUT("actualizarRol")
    Call<RespuestaRol> actualizarRol(@Body RolUpdate body);

    @Multipart
    @POST("uploadFotoEstacionamiento")
    Call<ApiResponse> subirFoto(
            @Part("id_estacionamiento") RequestBody id,
            @Part MultipartBody.Part archivo
    );

    @GET("getReservasPropietario/{idPropietario}")
    Call<List<Reserva>> obtenerReservas(
            @Path("idPropietario") int idPropietario
    );

    @PUT("actualizarReserva/{id}")
    Call<ResponseBody> actualizarReserva(
            @Path("id") int idReserva,
            @Body Map<String, String> estado
    );


    @GET("getEstacionamientosPropietario/{id_usuario}")
    Call<List<EstacionamientoPropietario>> getEstacionamientosPropietario(
            @Path("id_usuario") int idUsuario
    );


}
