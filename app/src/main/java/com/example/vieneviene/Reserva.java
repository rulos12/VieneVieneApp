package com.example.vieneviene;

public class Reserva {
    private int id_reserva;
    private String foto_principal;
    private String nombre_cliente;
    private String placas;
    private String modelo_vehiculo;
    private String fecha_reserva;
    public String hora_inicio;
    public String hora_fin;
    private String estado;
    private String tipo_vehiculo;
    private float monto_total;


    public Reserva(String foto_principal, String nombre_cliente, String placas, String modelo_vehiculo, String horario_apertura,
                   String horario_cierre, String estado, String fecha_reserva,String tipo_vehiculo, float monto_total
    ,int id_reserva) {
        this.foto_principal = foto_principal;
        this.nombre_cliente = nombre_cliente;
        this.placas = placas;
        this.modelo_vehiculo = modelo_vehiculo;
        this.hora_inicio = horario_apertura;
        this.hora_fin = horario_cierre;
        this.estado = estado;
        this.fecha_reserva = fecha_reserva;
        this.tipo_vehiculo = tipo_vehiculo;
        this.monto_total = monto_total;
        this.id_reserva = id_reserva;
    }


    public int getId_reserva() {
        return id_reserva;
    }

    public String getFecha_reserva() {
        return fecha_reserva;
    }

    public float getMonto_total() {
        return monto_total;
    }

    public String getTipo_vehiculo() {
        return tipo_vehiculo;
    }

    public String getHora_inicio() {
        return hora_inicio;
    }

    public String getHora_fin() {
        return hora_fin;
    }

    public String getFoto_principal() {
        return foto_principal;
    }

    public String getNombre_cliente() {
        return nombre_cliente;
    }

    public String getPlacas() {
        return placas;
    }

    public String getModelo_vehiculo() {
        return modelo_vehiculo;
    }

    public String getEstado() {
        return estado;
    }
}
