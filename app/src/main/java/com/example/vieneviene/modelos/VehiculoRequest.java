package com.example.vieneviene.modelos;

public class VehiculoRequest {
    public int id_usuario;
    public String tipo;
    public String modelo;
    public String placas;

    public VehiculoRequest(int id_usuario, String tipo, String modelo, String placas) {
        this.id_usuario = id_usuario;
        this.tipo = tipo;
        this.modelo = modelo;
        this.placas = placas;
    }
}


