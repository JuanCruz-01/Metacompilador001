/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package metacompilador;

/**
 *
 * @author LENOVO
 */
public class Error {

    private int linea;
    private String tipo;
    private String descripcion;

    public Error(int linea, String tipo, String descripcion) {
        this.linea = linea;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    public int getLinea() {
        return linea;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Object[] toArray() {
        return new Object[]{linea, tipo, descripcion};
    }
}
