package com.example.vieneviene.modelos;

public class RegisterRequest {
    private String correo;
    private String nombre,telefono,rol;
    private String contraseña;

    public RegisterRequest(String nombre, String correo,String telefono, String password, String rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.contraseña = password;
        this.rol = rol;
    }

    public String getCorreo() {
        return correo;
    }

    public String getPassword() {
        return contraseña;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getName() {
        return nombre;
    }

    public void setName(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public void setPassword(String password) {
        this.contraseña = password;
    }
}