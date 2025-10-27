
package asistentepa;

import java.util.Date;


public class Lote {
    public String id;
    public String especie;
    public int capacidad;
    public int cantidadActual;
    public Date fechaCreacion;
    public Date fechaSiembra;
    public Date fechatransplante;
    public Date fechaCosechaEstimada;
    public Date fechaCosechaReal;
    public String estado; // "Preparado", "Sembrado", "En crecimiento", "Listo para venta", "Vacío"
}
