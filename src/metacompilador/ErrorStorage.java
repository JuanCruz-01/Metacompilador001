/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package metacompilador;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author LENOVO
 */
public class ErrorStorage {

    private List<Error> errores = new ArrayList<>();

    public void agregarError(int linea, String tipo, String descripcion) {
        errores.add(new Error(linea, tipo, descripcion));
    }

    public List<Error> obtenerErrores() {
        return errores;
    }

    public List<Error> obtenerErroresSintacticos() {
        List<Error> erroresSintacticos = new ArrayList<>();
        for (Error error : errores) {
            if ("Sintáctico".equals(error.getTipo())) {
                erroresSintacticos.add(error);
            }
        }
        return erroresSintacticos;
    }

    public List<Error> obtenerErroresSemanticos() {
        List<Error> erroresSemanticos = new ArrayList<>();
        for (Error error : errores) {
            if ("Semántico".equals(error.getTipo())) {
                erroresSemanticos.add(error);
            }
        }
        return erroresSemanticos;
    }

    public void limpiarErrores() {
        errores.clear();
    }

    public void limpiarErroresSintacticos() {
        errores.removeIf(error -> error.getTipo().equalsIgnoreCase("Sintáctico"));
    }

    public void limpiarErroresSemanticos() {
        errores.removeIf(error -> error.getTipo().equalsIgnoreCase("Semántico"));
    }
}
