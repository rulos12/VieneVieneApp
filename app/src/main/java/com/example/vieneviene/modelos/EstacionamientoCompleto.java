package com.example.vieneviene.modelos;

public class EstacionamientoCompleto {
    public int id_usuario;
    public String nombre;
    public String descripcion;
    public double precio_hora;
    public int capacidad;
    public String horario_apertura;
    public String horario_cierre;
    public String estado = "activo";
    public int lugares_ocupados = 0;
    public Ubicacion ubicacion;

    public EstacionamientoCompleto(int idUsuario,
                                   String nombre,
                                   String descripcion,
                                   double precioHora,
                                   int capacidad,
                                   String apertura,
                                   String cierre,
                                   Ubicacion ubicacion) {
        this.id_usuario = idUsuario;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio_hora = precioHora;
        this.capacidad = capacidad;
        this.horario_apertura = apertura;
        this.horario_cierre = cierre;
        this.estado = "activo";
        this.lugares_ocupados = 0;
        this.ubicacion = ubicacion;
    }
}

