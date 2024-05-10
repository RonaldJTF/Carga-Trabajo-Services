package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

public class ActividadOutDTO {
    private String nombre;
    private String nivel;
    private String extrae;
    private Double frecuencia;
    private Double tiempoMaximo;
    private Double tiempoMinimo;
    private Double tiempoUsual;
    private Double tiempoEstandar;
    private Double tiempoTotalTarea;

    public ActividadOutDTO() {
    }

    public ActividadOutDTO(String nombre, String nivel, String extrae, Double frecuencia, Double tiempoMaximo, Double tiempoMinimo, Double tiempoUsual, Double tiempoEstandar, Double tiempoTotalTarea) {
        this.nombre = nombre;
        this.nivel = nivel;
        this.extrae = extrae;
        this.frecuencia = frecuencia;
        this.tiempoMaximo = tiempoMaximo;
        this.tiempoMinimo = tiempoMinimo;
        this.tiempoUsual = tiempoUsual;
        this.tiempoEstandar = tiempoEstandar;
        this.tiempoTotalTarea = tiempoTotalTarea;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getExtrae() {
        return extrae;
    }

    public void setExtrae(String extrae) {
        this.extrae = extrae;
    }

    public Double getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(Double frecuencia) {
        this.frecuencia = frecuencia;
    }

    public Double getTiempoMaximo() {
        return tiempoMaximo;
    }

    public void setTiempoMaximo(Double tiempoMaximo) {
        this.tiempoMaximo = tiempoMaximo;
    }

    public Double getTiempoMinimo() {
        return tiempoMinimo;
    }

    public void setTiempoMinimo(Double tiempoMinimo) {
        this.tiempoMinimo = tiempoMinimo;
    }

    public Double getTiempoUsual() {
        return tiempoUsual;
    }

    public void setTiempoUsual(Double tiempoUsual) {
        this.tiempoUsual = tiempoUsual;
    }

    public Double getTiempoEstandar() {
        return tiempoEstandar;
    }

    public void setTiempoEstandar(Double tiempoEstandar) {
        this.tiempoEstandar = tiempoEstandar;
    }

    public Double getTiempoTotalTarea() {
        return tiempoTotalTarea;
    }

    public void setTiempoTotalTarea(Double tiempoTotalTarea) {
        this.tiempoTotalTarea = tiempoTotalTarea;
    }

    @Override
    public String toString() {
        return "ActividadOutDTO{" +
                "nombre='" + nombre + '\'' +
                ", nivel='" + nivel + '\'' +
                ", extrae='" + extrae + '\'' +
                ", frecuencia=" + frecuencia +
                ", tiempoMaximo=" + tiempoMaximo +
                ", tiempoMinimo=" + tiempoMinimo +
                ", tiempoUsual=" + tiempoUsual +
                ", tiempoEstandar=" + tiempoEstandar +
                ", tiempoTotalTarea=" + tiempoTotalTarea +
                '}';
    }
}
