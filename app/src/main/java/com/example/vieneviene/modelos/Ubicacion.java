package com.example.vieneviene.modelos;

public class Ubicacion {
    public String direccion;
    public String ciudad;
    public String estado;
    public String codigo_postal;
    public double latitud;
    public double longitud;

    public Ubicacion(String direccion, String ciudad, String estado,
                     String codigo_postal, double latitud, double longitud) {

        this.direccion = direccion;
        this.ciudad = ciudad;
        this.estado = estado;
        this.codigo_postal = codigo_postal;
        this.latitud = latitud;
        this.longitud = longitud;
    }
}

