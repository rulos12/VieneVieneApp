package com.example.vieneviene.modelos;

public class ReservaRequest {
    public int id_usuario;
    public int id_estacionamiento;
    public int id_vehiculo;
    public String fecha_reserva;
    public String hora_inicio;
    public String hora_fin;
    public double monto_total;
    public String estado;

    public ReservaRequest(int id_usuario, int id_estacionamiento, int id_vehiculo,
                          String fecha_reserva, String hora_inicio, String hora_fin,
                          double monto_total, String estado) {

        this.id_usuario = id_usuario;
        this.id_estacionamiento = id_estacionamiento;
        this.id_vehiculo = id_vehiculo;
        this.fecha_reserva = fecha_reserva;
        this.hora_inicio = hora_inicio;
        this.hora_fin = hora_fin;
        this.monto_total = monto_total;
        this.estado = estado;
    }
}

