/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package metacompilador;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SemanticaChecker {

    private final ErrorStorage errorStorage;
    private final Map<String, String> variables = new HashMap<>(); 
    private final Set<String> variablesInicializadas = new HashSet<>(); 

    public SemanticaChecker(ErrorStorage errorStorage) {
        this.errorStorage = errorStorage;
    }

 
    public void verificarSemantica(String codigo) {
        errorStorage.limpiarErroresSemanticos();

        String[] lineas = codigo.split("\n");

        for (int linea = 0; linea < lineas.length; linea++) {
            String codigoLinea = lineas[linea].trim();

           
            if (codigoLinea.isEmpty() || codigoLinea.startsWith("//") || codigoLinea.startsWith("/*") || codigoLinea.startsWith("*")) {
                continue;
            }

            boolean lineaProcesada = false;

          
            if (esDeclaracionDeVariable(codigoLinea)) {
                String tipo = obtenerTipoVariable(codigoLinea);
                String variable = obtenerNombreVariable(codigoLinea);
                String valor = obtenerValorVariable(codigoLinea);

                if (tipo != null && variable != null) {
                    if (variables.containsKey(variable)) {
                      
                        errorStorage.agregarError(linea + 1, "Semántico", "Variable ya declarada: " + variable);
                        System.out.println("Error agregado: Variable ya declarada en línea " + (linea + 1));
                    } else {
                        variables.put(variable, tipo);
                        if (valor != null && esValorCompatible(tipo, valor, linea + 1)) {
                            variablesInicializadas.add(variable);
                        } else if (valor != null) {
                           
                            errorStorage.agregarError(linea + 1, "Semántico", "Asignación incompatible: la variable '" + variable + "' es de tipo " + tipo + " y no es compatible con el valor '" + valor + "'.");
                            System.out.println("Error agregado: Variable ya declarada en línea " + (linea + 1));
                        }
                       
                    }
                }
                lineaProcesada = true;
            }
            else if (esAsignacionDeVariable(codigoLinea)) {
                String variable = obtenerVariableAsignada(codigoLinea);
                String valor = obtenerValorAsignado(codigoLinea);

                if (variables.containsKey(variable)) {
                    String tipo = variables.get(variable);
                    if (esValorCompatible(tipo, valor, linea + 1)) {
                       
                        variablesInicializadas.add(variable);
                    } else {
                        errorStorage.agregarError(linea + 1, "Semántico", "Asignación incompatible: la variable '" + variable + "' es de tipo " + tipo + " y no es compatible con el valor '" + valor + "'.");
                        System.out.println("Error agregado: Variable ya declarada en línea " + (linea + 1));
                    }
                } else {
                 
                    errorStorage.agregarError(linea + 1, "Semántico", "Variable no definida: " + variable);
                    System.out.println("Error agregado: Variable ya declarada en línea " + (linea + 1));
                }
                lineaProcesada = true;
            } 
            else if (esEstructuraFor(codigoLinea)) {
                procesarEstructuraFor(codigoLinea, linea + 1);
                lineaProcesada = true;
            }

           
            if (!lineaProcesada) {
              
            }
        }
    }

 
    private boolean esDeclaracionDeVariable(String codigoLinea) {
        return codigoLinea.matches("(int|double|float|boolean|String|char)\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*(=\\s*.+)?;");
    }

  
    private boolean esAsignacionDeVariable(String codigoLinea) {
        return codigoLinea.matches("[a-zA-Z_][a-zA-Z0-9_]*\\s*=\\s*.+;");
    }

  
    private String obtenerTipoVariable(String codigoLinea) {
        String[] palabras = codigoLinea.split("\\s+");
        return palabras[0];  
    }

   
    private String obtenerNombreVariable(String codigoLinea) {
        String[] palabras = codigoLinea.split("\\s+|=");
        if (palabras.length > 1) {
            return palabras[1].replace(";", "").trim(); 
        }
        return null;
    }

  
    private String obtenerValorVariable(String codigoLinea) {
        if (codigoLinea.contains("=")) {
            String[] partes = codigoLinea.split("=");
            if (partes.length > 1) {
                return partes[1].trim().replace(";", ""); 
            }
        }
        return null;
    }

  
    private String obtenerVariableAsignada(String codigoLinea) {
        String[] partes = codigoLinea.split("=");
        return partes[0].trim();
    }


    private String obtenerValorAsignado(String codigoLinea) {
        String[] partes = codigoLinea.split("=");
        if (partes.length > 1) {
            return partes[1].trim().replace(";", "");
        }
        return null;
    }

   
    private boolean esValorCompatible(String tipo, String valor, int linea) {
  
        if (esLiteralCompatible(tipo, valor)) {
            return true;
        } else {
      
            return verificarVariablesEnExpresion(tipo, valor, linea);
        }
    }

    private boolean esLiteralCompatible(String tipo, String valor) {
        switch (tipo) {
            case "int":
                return valor.matches("-?\\d+"); 
            case "double":
                return valor.matches("-?\\d+(\\.\\d+)?"); 
            case "float":
                return valor.matches("-?\\d+(\\.\\d+)?[fF]"); 
            case "boolean":
                return valor.equals("true") || valor.equals("false");
            case "String":
                return valor.startsWith("\"") && valor.endsWith("\"");
            case "char":
                return valor.startsWith("'") && valor.endsWith("'") && valor.length() == 3;
            default:
                return false;
        }
    }

   
    private boolean verificarVariablesEnExpresion(String tipoEsperado, String expresion, int linea) {
        Set<String> variablesEnExpresion = obtenerVariablesDeExpresion(expresion);
        boolean compatible = true;

        for (String var : variablesEnExpresion) {
            if (!variables.containsKey(var)) {
             
                errorStorage.agregarError(linea, "Semántico", "Variable no definida: " + var);
                compatible = false;
            } else if (!variablesInicializadas.contains(var)) {
           
                errorStorage.agregarError(linea, "Semántico", "Variable no inicializada: " + var);
                compatible = false;
            } else {
                String tipoVar = variables.get(var);
                if (!sonTiposCompatibles(tipoEsperado, tipoVar)) {
                 
                    errorStorage.agregarError(linea, "Semántico", "Variable '" + var + "' de tipo incompatible: " + tipoVar + ". Se esperaba: " + tipoEsperado);
                    compatible = false;
                }
            }
        }

      
        if (variablesEnExpresion.isEmpty()) {
            if (!esLiteralCompatible(tipoEsperado, expresion)) {
                compatible = false;
            }
        }

        return compatible;
    }

 
    private Set<String> obtenerVariablesDeExpresion(String expresion) {
        Set<String> variablesEnExpresion = new HashSet<>();
     
        String sinLiterales = expresion.replaceAll("-?\\d+(\\.\\d+)?([eE][-+]?\\d+)?[fFdD]?", "");
        sinLiterales = sinLiterales.replaceAll("\"[^\"]*\"", "");
        sinLiterales = sinLiterales.replaceAll("'[^']*'", "");
        sinLiterales = sinLiterales.replaceAll("[()+\\-*/%&|!<>^=]", " "); 

        String[] tokens = sinLiterales.split("\\s+");
        for (String token : tokens) {
            if (!token.isEmpty() && !esPalabraReservada(token)) {
                variablesEnExpresion.add(token);
            }
        }
        return variablesEnExpresion;
    }

  
    private boolean esPalabraReservada(String palabra) {
     
        Set<String> palabrasReservadas = new HashSet<>(Arrays.asList(
                "int", "double", "float", "boolean", "char", "String",
                "if", "else", "for", "while", "do", "switch", "case",
                "default", "break", "continue", "return", "public", "private",
                "protected", "static", "final", "void", "class", "new",
                "true", "false"
        ));
        return palabrasReservadas.contains(palabra);
    }

  
    private boolean sonTiposCompatibles(String tipoEsperado, String tipoVar) {
        if (tipoEsperado.equals(tipoVar)) {
            return true;
        }
     
        if (esTipoNumerico(tipoEsperado) && esTipoNumerico(tipoVar)) {
            return true;
        }
    
        if (tipoEsperado.equals("String")) {
            return true;
        }
        return false;
    }

   
    private boolean esTipoNumerico(String tipo) {
        return tipo.equals("int") || tipo.equals("double") || tipo.equals("float");
    }

  
    private boolean esEstructuraFor(String codigoLinea) {
        return codigoLinea.startsWith("for(") || codigoLinea.startsWith("for (");
    }

 
    private void procesarEstructuraFor(String codigoLinea, int linea) {
        int inicio = codigoLinea.indexOf('(');
        int fin = codigoLinea.lastIndexOf(')');
        if (inicio != -1 && fin != -1 && fin > inicio) {
            String contenidoFor = codigoLinea.substring(inicio + 1, fin).trim();
            String[] partes = contenidoFor.split(";");
            if (partes.length == 3) {
             
                String inicializacion = partes[0].trim();
                if (esDeclaracionDeVariable(inicializacion + ";")) {
                 
                    String tipo = obtenerTipoVariable(inicializacion);
                    String variable = obtenerNombreVariable(inicializacion);
                    String valor = obtenerValorVariable(inicializacion);

                    if (tipo != null && variable != null) {
                        if (variables.containsKey(variable)) {
                          
                            errorStorage.agregarError(linea, "Semántico", "Variable ya declarada: " + variable);
                            System.out.println("Error agregado: Variable ya declarada en línea " + (linea + 1));
                        } else {
                            variables.put(variable, tipo);
                            if (valor != null && esValorCompatible(tipo, valor, linea)) {
                                variablesInicializadas.add(variable);
                            } else if (valor != null) {
                               
                                errorStorage.agregarError(linea, "Semántico", "Asignación incompatible en 'for': la variable '" + variable + "' es de tipo " + tipo + " y no es compatible con el valor '" + valor + "'.");
                                System.out.println("Error agregado: Variable ya declarada en línea " + (linea + 1));
                            }
                        }
                    }
                } else if (esAsignacionDeVariable(inicializacion + ";")) {
                  
                    String variable = obtenerVariableAsignada(inicializacion + ";");
                    String valor = obtenerValorAsignado(inicializacion + ";");

                    if (variables.containsKey(variable)) {
                        String tipo = variables.get(variable);
                        if (esValorCompatible(tipo, valor, linea)) {
                            variablesInicializadas.add(variable);
                        } else {
                            errorStorage.agregarError(linea, "Semántico", "Asignación incompatible en 'for': la variable '" + variable + "' es de tipo " + tipo + " y no es compatible con el valor '" + valor + "'.");
                            System.out.println("Error agregado: Variable ya declarada en línea " + (linea + 1));
                        }
                    } else {
                        errorStorage.agregarError(linea, "Semántico", "Variable no definida en 'for': " + variable);
                        System.out.println("Error agregado: Variable ya declarada en línea " + (linea + 1));
                    }
                }
               
            }
        }
    }
}
