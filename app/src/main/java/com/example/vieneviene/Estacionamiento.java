package com.example.vieneviene;

import com.google.android.gms.maps.model.LatLng;

public class Estacionamiento {
    private int id;
    private String nombre;
    private String direccion;
    private String precio;
    private LatLng ubicacion;
    private String imagenUrl;


    public Estacionamiento(int id,String nombre, String direccion, String precio, LatLng ubicacion, String imagenUrl) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.precio = precio;
        this.ubicacion = ubicacion;
        this.imagenUrl = imagenUrl;
    }

    public int getId() {
        return id;
    }

    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getPrecio() { return precio; }
    public LatLng getUbicacion() { return ubicacion; }

    public String getImagenUrl() {
        return imagenUrl;
    }
}
