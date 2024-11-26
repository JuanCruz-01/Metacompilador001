/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package metacompilador;

import java.util.*;

/**
 *
 * @author LENOVO
 */
public class TokenStorage {

    private Map<String, List<Token>> tablaTokens = new LinkedHashMap<>();

    public void agregarToken(String tipo, Token token) {
        tablaTokens.computeIfAbsent(tipo, k -> new ArrayList<>()).add(token);
    }

    public List<Token> obtenerTokensPorTabla(String tipo) {
        return tablaTokens.getOrDefault(tipo, new ArrayList<>());
    }
    
    public List<Token> obtenerTodosTokens() {
        List<Token> todosTokens = new ArrayList<>();
        for (List<Token> tokens : tablaTokens.values()) {
            todosTokens.addAll(tokens);
        }
        return todosTokens;
    }

    public Token obtenerToken(String tipo, String tokenStr) {
        List<Token> tokens = tablaTokens.get(tipo);
        if (tokens != null) {
            for (Token token : tokens) {
                if (token.getToken().equals(tokenStr)) {
                    return token;
                }
            }
        }
        return null;
    }

    public void limpiar() {
        tablaTokens.clear();
    }
}
