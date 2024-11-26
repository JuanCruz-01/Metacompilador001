package metacompilador;

import java.util.Arrays;
import java.util.Stack;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.List;

public class SintaxisChecker {

    private final ErrorStorage errorStorage;

    private static final Set<String> palabrasReservadas = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally",
            "float", "for", "if", "implements", "import", "instanceof", "int", "interface", "long",
            "native", "new", "null", "package", "private", "protected", "public", "return", "short",
            "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while", "true", "false", "main", "String",
            "System", "out", "println", "args"
    ));

    private static final Set<String> palabrasClaveNoRequierenPuntoYComa = new HashSet<>(Arrays.asList(
            "if", "else", "for", "while", "do", "switch", "case", "default",
            "try", "catch", "finally", "class", "interface", "enum"
    ));

    
    private Set<String> variablesDeclaradas = new HashSet<>();

    public SintaxisChecker(ErrorStorage errorStorage) {
        this.errorStorage = errorStorage;
    }

    public void verificarSintaxis(String codigo) {
        errorStorage.limpiarErroresSintacticos();
        variablesDeclaradas.clear(); 
        Stack<Character> pilaSimbolos = new Stack<>();
        Stack<Integer> pilaLineas = new Stack<>();
        String[] lineas = codigo.split("\n");
        boolean dentroComentarioMultilinea = false;

        for (int linea = 0; linea < lineas.length; linea++) {
            String codigoLinea = lineas[linea];
            String trimmedLine = codigoLinea.trim();

            
            if (dentroComentarioMultilinea) {
                if (trimmedLine.contains("*/")) {
                    dentroComentarioMultilinea = false;
                    
                    trimmedLine = trimmedLine.substring(trimmedLine.indexOf("*/") + 2).trim();
                    if (trimmedLine.isEmpty()) {
                        continue;
                    }
                } else {
                    continue; 
                }
            }

            
            if (trimmedLine.startsWith("/*")) {
                dentroComentarioMultilinea = true;
                
                if (trimmedLine.endsWith("*/") && trimmedLine.length() > 4) {
                    dentroComentarioMultilinea = false;
                    trimmedLine = trimmedLine.substring(trimmedLine.indexOf("*/") + 2).trim();
                    if (trimmedLine.isEmpty()) {
                        continue;
                    }
                } else {
                    continue; 
                }
            }

            
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("//")) {
                continue;
            }

            
            verificarNombresDeVariables(trimmedLine, linea + 1);

            
            verificarSimbolosYSentencias(trimmedLine, linea + 1, pilaSimbolos, pilaLineas);

           
            verificarCondicionesIf(trimmedLine, linea + 1);

            
            verificarAsignacionesInvalidas(trimmedLine, linea + 1);

            
            verificarUsoDeVariables(trimmedLine, linea + 1);

            
            verificarPalabrasReservadas(trimmedLine, linea + 1);

            
            verificarOperadoresIncorrectos(trimmedLine, linea + 1);

            
            verificarBloquesVacios(trimmedLine, linea + 1);

           
            verificarUsoIncorrectoElse(trimmedLine, linea + 1);

            
            verificarOperadoresIncrementoDecremento(trimmedLine, linea + 1);
        }

        
        while (!pilaSimbolos.isEmpty()) {
            char simboloAbierto = pilaSimbolos.pop();
            int lineaAbierta = pilaLineas.pop();
            errorStorage.agregarError(lineaAbierta, "Sintáctico", "Falta cierre para '" + simboloAbierto + "' en línea " + lineaAbierta);
        }
    }

    
    private void verificarOperadoresIncorrectos(String linea, int numeroLinea) {
        
        Pattern patronAsignacion = Pattern.compile("\\b(int|float|double|boolean|char|String|long)\\s+\\w+\\s+==\\s+.+;");
        Matcher matcherAsignacion = patronAsignacion.matcher(linea);
        if (matcherAsignacion.find()) {
            errorStorage.agregarError(numeroLinea, "Sintáctico", "Uso incorrecto del operador '=='. Se esperaba '=' para asignación.");
        }

        
        Pattern patronAsignacionEnCondicion = Pattern.compile("\\b(if|while|for)\\s*\\(\\s*\\w+\\s*=\\s*[^=].*\\)");
        Matcher matcherCondicion = patronAsignacionEnCondicion.matcher(linea);
        if (matcherCondicion.find()) {
            errorStorage.agregarError(numeroLinea, "Sintáctico", "Asignación dentro de la condición. Se esperaba un operador relacional '==' en su lugar.");
        }
    }

    
    private void verificarBloquesVacios(String linea, int numeroLinea) {
        
        if (linea.matches(".*\\{\\s*\\}.*")) {
            errorStorage.agregarError(numeroLinea, "Sintáctico", "Bloque de código vacío.");
        }
    }

    
    private void verificarUsoIncorrectoElse(String linea, int numeroLinea) {
        
        if (linea.startsWith("else")) {
            
            if (!linea.matches("^else\\s*\\{.*")) {
                errorStorage.agregarError(numeroLinea, "Sintáctico", "'else' mal formado. Se esperaba '{' después de 'else'.");
            }
        }
    }

    
    private void verificarOperadoresIncrementoDecremento(String linea, int numeroLinea) {
        
        Pattern patron = Pattern.compile("^(int|float|double|boolean|char|String|long)\\s+(\\+\\+|--)?\\w+;");
        Matcher matcher = patron.matcher(linea);
        if (matcher.find()) {
            String operador = matcher.group(2);
            if (operador != null && (operador.equals("++") || operador.equals("--"))) {
                errorStorage.agregarError(numeroLinea, "Sintáctico", "Uso incorrecto de operador '" + operador + "' en la declaración de variable.");
            }
        }

        
        Pattern patronSoloOperador = Pattern.compile("^(\\+\\+|--);");
        Matcher matcherSoloOperador = patronSoloOperador.matcher(linea);
        if (matcherSoloOperador.find()) {
            errorStorage.agregarError(numeroLinea, "Sintáctico", "Uso incorrecto de operadores de incremento/decremento sin una variable.");
        }
    }

    private void verificarPalabrasReservadas(String linea, int numeroLinea) {
        List<String> tokens = tokenizeLine(linea);

        for (String token : tokens) {
            String tokenMinusculas = token.toLowerCase();

            // Verificar si el token es una palabra reservada mal escrita
            if (palabrasReservadas.stream().anyMatch(reservada -> reservada.equalsIgnoreCase(token))
                    && !palabrasReservadas.contains(token)) {
                String formaCorrecta = encontrarFormaCorrecta(tokenMinusculas);
                errorStorage.agregarError(numeroLinea, "Sintáctico",
                        "Palabra reservada mal escrita: '" + token + "'. ¿Quiso decir '" + formaCorrecta + "'?");
            }
        }
    }

    private void verificarSimbolosYSentencias(String trimmedLine, int linea, Stack<Character> pilaSimbolos, Stack<Integer> pilaLineas) {
        boolean insideDoubleQuote = false;
        boolean insideSingleQuote = false;

        for (int i = 0; i < trimmedLine.length(); i++) {
            char c = trimmedLine.charAt(i);

            if (c == '"' && !insideSingleQuote) {
                if (!isEscaped(trimmedLine, i)) {
                    insideDoubleQuote = !insideDoubleQuote;
                }
            } else if (c == '\'' && !insideDoubleQuote) {
                if (!isEscaped(trimmedLine, i)) {
                    insideSingleQuote = !insideSingleQuote;
                }
            } else if (!insideDoubleQuote && !insideSingleQuote) {
                if (c == '(' || c == '{' || c == '[') {
                    pilaSimbolos.push(c);
                    pilaLineas.push(linea);
                } else if (c == ')' || c == '}' || c == ']') {
                    if (pilaSimbolos.isEmpty()) {
                        errorStorage.agregarError(linea, "Sintáctico", "Símbolo de cierre inesperado: '" + c + "'");
                    } else {
                        char simboloApertura = pilaSimbolos.pop();
                        int lineaApertura = pilaLineas.pop();

                        if (!esParCorrecto(simboloApertura, c)) {
                            errorStorage.agregarError(lineaApertura, "Sintáctico", "Falta cierre para '" + simboloApertura + "' en línea " + lineaApertura);
                        }
                    }
                }
            }
        }

        if (insideDoubleQuote) {
            errorStorage.agregarError(linea, "Sintáctico", "Falta cierre para '\"' en línea " + linea);
        }

        if (insideSingleQuote) {
            errorStorage.agregarError(linea, "Sintáctico", "Falta cierre para '\'' en línea " + linea);
        }

        if (trimmedLine.equals(";")) {
            errorStorage.agregarError(linea, "Sintáctico", "Sentencia incompleta, solo ';'.");
        }

        if (!trimmedLine.endsWith(";") && !trimmedLine.endsWith("{") && !trimmedLine.endsWith("}")) {
            List<String> tokens = tokenizeLine(trimmedLine);
            if (tokens.size() > 0) {
                String firstToken = tokens.get(0);
                if (!palabrasClaveNoRequierenPuntoYComa.contains(firstToken)) {
                    if (!esDeclaracionDeMetodoOClase(trimmedLine)) {
                        errorStorage.agregarError(linea, "Sintáctico", "Falta ';' al final de la sentencia.");
                    }
                }
            }
        }

        List<String> tokens = tokenizeLine(trimmedLine);
        verificarLlamadasAMetodos(tokens, linea);
    }

    private boolean isEscaped(String line, int pos) {
        int count = 0;
        pos--;
        while (pos >= 0 && line.charAt(pos) == '\\') {
            count++;
            pos--;
        }
        return count % 2 == 1;
    }

    private void verificarNombresDeVariables(String codigoLinea, int numeroLinea) {
        List<String> palabras = tokenizeLine(codigoLinea);

        if (palabras.isEmpty()) {
            return;
        }

        String primerPalabra = palabras.get(0);

        if (esTipoDeDato(primerPalabra)) {
            if (palabras.size() < 2) {
                errorStorage.agregarError(numeroLinea, "Sintáctico",
                        "Declaración de tipo incompleta. Se esperaba un identificador después del tipo: '" + primerPalabra + "'");
                return;
            }

            String siguientePalabra = palabras.get(1);

            if (esIdentificadorInvalido(siguientePalabra) || palabrasReservadas.contains(siguientePalabra)) {
                errorStorage.agregarError(numeroLinea, "Sintáctico",
                        "Nombre de variable inválido o palabra reservada usada como identificador: '" + siguientePalabra + "'");
                return;
            }

           
            variablesDeclaradas.add(siguientePalabra);

            
            if (palabras.size() == 2) {
               
                errorStorage.agregarError(numeroLinea, "Sintáctico",
                        "Falta ';' al final de la declaración de variable.");
                return;
            }

            String tercerToken = palabras.get(2);

            if (tercerToken.equals("=")) {
                
                if (palabras.size() < 4) {
                    errorStorage.agregarError(numeroLinea, "Sintáctico",
                            "Expresión inválida o faltante después de '='.");
                    return;
                }

                
                String cuartoToken = palabras.get(3);
                if (!esValorValido(cuartoToken)) {
                    errorStorage.agregarError(numeroLinea, "Sintáctico",
                            "Valor inválido en la asignación: '" + cuartoToken + "'.");
                }

               
                if (palabras.size() > 4 && !palabras.get(4).equals(";")) {
                    errorStorage.agregarError(numeroLinea, "Sintáctico",
                            "Falta ';' al final de la declaración de variable.");
                } else if (palabras.size() < 4) {
                    errorStorage.agregarError(numeroLinea, "Sintáctico",
                            "Falta ';' al final de la declaración de variable.");
                }
            } else if (tercerToken.equals("==")) {
                
                errorStorage.agregarError(numeroLinea, "Sintáctico",
                        "Uso incorrecto del operador '=='. Se esperaba '=' para asignación.");
            } else if (!tercerToken.equals(";")) {
                
                errorStorage.agregarError(numeroLinea, "Sintáctico",
                        "Sintaxis inválida después del identificador: '" + siguientePalabra + "'.");
            }
        }
        
    }

    private boolean esValorValido(String token) {
        return token.matches("^\\d+(\\.\\d+)?$") || 
               token.matches("^\".*\"$") ||          
               token.matches("^'.*'$") ||           
               esIdentificadorValido(token);          
    }

    private boolean esIdentificadorValido(String palabra) {
        if (palabra.isEmpty()) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(palabra.charAt(0))) {
            return false;
        }
        for (int i = 1; i < palabra.length(); i++) {
            if (!Character.isJavaIdentifierPart(palabra.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean esTipoDeDato(String palabra) {
        return palabra.equals("int") || palabra.equals("float") || palabra.equals("double")
                || palabra.equals("boolean") || palabra.equals("char") || palabra.equals("String") || palabra.equals("long");
    }

    private String encontrarFormaCorrecta(String palabraMinusculas) {
        for (String reservada : palabrasReservadas) {
            if (reservada.toLowerCase().equals(palabraMinusculas)) {
                return reservada;
            }
        }
        return palabraMinusculas;
    }

    private boolean esIdentificadorInvalido(String palabra) {
        if (palabra.isEmpty()) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(palabra.charAt(0))) {
            return true;
        }
        for (int i = 1; i < palabra.length(); i++) {
            if (!Character.isJavaIdentifierPart(palabra.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean esParCorrecto(char apertura, char cierre) {
        return (apertura == '(' && cierre == ')')
                || (apertura == '{' && cierre == '}')
                || (apertura == '[' && cierre == ']');
    }

    private boolean esDeclaracionDeMetodoOClase(String linea) {
        String lineaSinComentarios = linea.replaceAll("//.*", "").replaceAll("/\\*.*\\*/", "");

        if (lineaSinComentarios.matches("\\s*(public|private|protected)?\\s*(class|interface|enum)\\s+\\w+.*")) {
            return true;
        }

        if (lineaSinComentarios.matches(".*\\)\\s*(\\{)?\\s*")) {
            return true;
        }

        return false;
    }

    private void verificarLlamadasAMetodos(List<String> tokens, int linea) {

        if (tokens.size() < 5 || !tokens.contains(".")) {
            return;
        }

        if (!tokens.get(0).equals("System")) {
            errorStorage.agregarError(linea, "Sintáctico", "Llamada incorrecta: 'System.out.println'");
            return;
        }

        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equals("System")) {

                if (i + 4 < tokens.size() &&
                        tokens.get(i + 1).equals(".") &&
                        tokens.get(i + 2).equals("out") &&
                        tokens.get(i + 3).equals(".") &&
                        (tokens.get(i + 4).equals("println") || tokens.get(i + 4).equals("print"))) {

                    
                    if (i + 5 >= tokens.size() || !tokens.get(i + 5).equals("(")) {
                        errorStorage.agregarError(linea, "Sintáctico", "Falta '(' después de 'System.out." + tokens.get(i + 4) + "'");
                        break;
                    }

                    
                    int indiceCierre = -1;
                    for (int j = i + 6; j < tokens.size(); j++) {
                        if (tokens.get(j).equals(")")) {
                            indiceCierre = j;
                            
                            if (j + 1 >= tokens.size() || !tokens.get(j + 1).equals(";")) {
                                errorStorage.agregarError(linea, "Sintáctico", "Falta ';' después de la llamada a 'System.out." + tokens.get(i + 4) + "'");
                            }
                            break;
                        }
                    }

                    if (indiceCierre == -1) {
                        errorStorage.agregarError(linea, "Sintáctico", "Falta ')' en la llamada a 'System.out." + tokens.get(i + 4) + "'");
                    } else {
                        
                        List<String> argumentos = tokens.subList(i + 6, indiceCierre);
                        if (!esContenidoValido(argumentos)) {
                            errorStorage.agregarError(linea, "Sintáctico", "Argumento inválido en 'System.out." + tokens.get(i + 4) + "'. Se esperaba un string o una variable.");
                        }
                    }
                } else {
                    errorStorage.agregarError(linea, "Sintáctico", "Llamada incorrecta: 'System.out.println'");
                    break;
                }
            }
        }
    }

    private boolean esContenidoValido(List<String> argumentos) {
        if (argumentos.isEmpty()) {
            return true; // 
        }

        for (String token : argumentos) {
            if (token.matches("\"(\\\\.|[^\"\\\\])*\"")) {
                continue; // 
            }

            if (Character.isJavaIdentifierStart(token.charAt(0)) && token.matches("[a-zA-Z0-9_]*")) {
                continue; // 
            }

            if (token.equals("+")) {
                continue; 
            }

            return false; 
        }

        return true;
    }

    private List<String> tokenizeLine(String line) {
        List<String> tokens = new ArrayList<>();

        Matcher m = Pattern.compile(
                "\"(\\\\.|[^\"\\\\])*\"|'(\\\\.|[^'\\\\])*'|==|!=|<=|>=|&&|\\|\\||[{}()\\[\\];.,+-/*%<>=]|\\w+"
        ).matcher(line);

        while (m.find()) {
            String token = m.group().trim();

            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }

        return tokens;
    }

    
    private void verificarCondicionesIf(String linea, int numeroLinea) {
        String trimmedLine = linea.trim();

        if (trimmedLine.startsWith("if")) {
            int indiceInicio = trimmedLine.indexOf('(');
            int indiceFin = trimmedLine.lastIndexOf(')');

            if (indiceInicio != -1 && indiceFin != -1 && indiceFin > indiceInicio) {
                String condicion = trimmedLine.substring(indiceInicio + 1, indiceFin).trim();
                
                if (!condicion.matches(".*(==|!=|<=|>=|<|>).+")) {
                    errorStorage.agregarError(numeroLinea, "Sintáctico", "Falta operador relacional en la condición del 'if'.");
                }
            } else {
                errorStorage.agregarError(numeroLinea, "Sintáctico", "Condición del 'if' mal formada. Faltan paréntesis.");
            }
        }
    }

    
    private void verificarAsignacionesInvalidas(String linea, int numeroLinea) {
        String trimmedLine = linea.trim();

        
        Pattern patronAsignacion = Pattern.compile("^(int|float|double|boolean|char|String|long)\\s+\\w+\\s*=\\s*.+;");
        Matcher matcher = patronAsignacion.matcher(trimmedLine);

        if (matcher.matches()) {
            
            int indiceIgual = trimmedLine.indexOf('=');
            int indicePuntoComa = trimmedLine.lastIndexOf(';');
            if (indiceIgual != -1 && indicePuntoComa != -1 && indicePuntoComa > indiceIgual) {
                String valorAsignado = trimmedLine.substring(indiceIgual + 1, indicePuntoComa).trim();

                if (!esValorValido(valorAsignado)) {
                    errorStorage.agregarError(numeroLinea, "Sintáctico", "Asignación inválida en declaración de variable.");
                }
            }
        }
    }

    
    private void verificarUsoDeVariables(String linea, int numeroLinea) {
        List<String> tokens = tokenizeLine(linea);

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            
            if (!palabrasReservadas.contains(token) && esIdentificadorValido(token)) {
                
                if (i + 1 < tokens.size() && tokens.get(i + 1).equals("=")) {
                    if (!variablesDeclaradas.contains(token)) {
                        errorStorage.agregarError(numeroLinea, "Semántico", "La variable '" + token + "' no ha sido declarada.");
                    }
                }

                
                if (i + 2 < tokens.size() && tokens.get(i + 1).matches("[+\\-*/%]")) {
                    String siguienteToken = tokens.get(i + 2);
                    if (esIdentificadorValido(siguienteToken) && !variablesDeclaradas.contains(siguienteToken)) {
                        errorStorage.agregarError(numeroLinea, "Semántico", "La variable '" + siguienteToken + "' no ha sido declarada.");
                    }
                }
            }
        }
    }
}
