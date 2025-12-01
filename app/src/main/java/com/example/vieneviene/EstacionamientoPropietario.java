package com.example.vieneviene;

import com.google.android.gms.maps.model.LatLng;

public class EstacionamientoPropietario {

    private int id_estacionamiento;
    private int id_usuario;
    private int id_ubicacion;
    private String nombre;
    private String descripcion;
    private double precio_hora;
    private int capacidad;
    private String horario_apertura;
    private String horario_cierre;
    private String estado;
    private String created_at;
    private String updated_at;
    private String deleted_at;
    private int lugares_ocupados;

    private String direccion;
    private String ciudad;
    private String estado_region;
    private String codigo_postal;
    private double latitud;
    private double longitud;

    private String foto_principal;

    public int getId_estacionamiento() { return id_estacionamiento; }
    public int getId_usuario() { return id_usuario; }
    public int getId_ubicacion() { return id_ubicacion; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio_hora() { return precio_hora; }
    public int getCapacidad() { return capacidad; }
    public String getHorario_apertura() { return horario_apertura; }
    public String getHorario_cierre() { return horario_cierre; }
    public String getEstado() { return estado; }
    public String getCreated_at() { return created_at; }
    public String getUpdated_at() { return updated_at; }
    public String getDeleted_at() { return deleted_at; }
    public int getLugares_ocupados() { return lugares_ocupados; }

    public String getDireccion() { return direccion; }
    public String getCiudad() { return ciudad; }
    public String getEstado_region() { return estado_region; }
    public String getCodigo_postal() { return codigo_postal; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }

    public String getFoto_principal() { return foto_principal; }
}
