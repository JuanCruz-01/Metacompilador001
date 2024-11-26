/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package metacompilador;

import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author LENOVO
 */

public class Token {

    private int id;
    private String token;
    private String alias;
    private List<Integer> lineas;

    public Token(int id, String token, String alias, int linea) {
        this.id = id;
        this.token = token;
        this.alias = alias;
        this.lineas = new ArrayList<>();
        this.lineas.add(linea);
    }

    public void agregarLinea(int linea) {
        if (!lineas.contains(linea)) {
            this.lineas.add(linea);
        }
    }

    public Object[] toArray() {
        String lineasStr = lineas.stream().map(String::valueOf).collect(Collectors.joining(","));
        return new Object[]{id, token, alias, lineasStr};
    }

    public String getToken() {
        return token;
    }

    public int getId() {
        return id;
    }

    public List<Integer> getLineas() {
        return lineas;
    }

    public String getAlias() {
        return alias;
    }
}
