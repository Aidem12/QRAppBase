package com.example.qrasist.models;

public class TareaAlumno {
    private int id;
    private int tareaId;
    private int alumnoId;
    private String estado;
    private String fechaEntrega;
    private String comentario;

    private String evidencia;

    private String nombreAlumno;


    private String titulo;
    private String descripcion;

    public TareaAlumno() {}

    public TareaAlumno(int id, int tareaId, int alumnoId, String estado, String fechaEntrega, String comentario) {
        this.id = id;
        this.tareaId = tareaId;
        this.alumnoId = alumnoId;
        this.estado = estado;
        this.fechaEntrega = fechaEntrega;
        this.comentario = comentario;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTareaId() { return tareaId; }
    public void setTareaId(int tareaId) { this.tareaId = tareaId; }
    public int getAlumnoId() { return alumnoId; }
    public void setAlumnoId(int alumnoId) { this.alumnoId = alumnoId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(String fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public String getComentario() { return comentario; }


    public String getEvidencia() {
        return evidencia;
    }

    public void setEvidencia(String evidencia) {
        this.evidencia = evidencia;
    }

    public String getNombreAlumno() {
        return nombreAlumno;
    }

    public void setNombreAlumno(String nombreAlumno) {
        this.nombreAlumno = nombreAlumno;
    }


    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}