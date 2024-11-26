/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package metacompilador;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

public class Lexer {

    private Map<String, Integer> palabrasReservadasIds;
    private Map<String, Integer> operadoresIds;
    public Map<String, Integer> caracteresEspecialesIds;

    private final TokenStorage storage;

    private int idIdentificadores = 1;
    private int idEnteros = 1;
    private int idReales = 1;
    private int idCientificos = 1;
    private int idStrings = 1;
    private int idChars = 1;

    private static final Set<String> palabrasReservadas = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally",
            "float", "for", "if", "implements", "import", "instanceof", "int", "interface", "long",
            "native", "new", "null", "package", "private", "protected", "public", "return", "short",
            "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while",
            "Scanner", "String", "Math", "Integer", "Double", "Boolean", "Character", "ArrayList",
            "HashMap", "LinkedList", "HashSet", "TreeMap", "true", "false",
            "System.out.print", "System.out.println"
    ));

    private static final Pattern patronIdentificador = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]+)*\\b");

    private static final Pattern patronEntero = Pattern.compile("\\b\\d+\\b");
    private static final Pattern patronReal = Pattern.compile("\\b\\d+\\.\\d+[fF]?\\b");
    private static final Pattern patronCientifico = Pattern.compile("\\b\\d+\\.?\\d*[eE][-+]?\\d+\\b");
    private static final Pattern patronCadena = Pattern.compile("\"(.*?)\"");
    private static final Pattern patronCaracter = Pattern.compile("'(.)'");

    private static final Pattern patronOperador = Pattern.compile("[+\\-*/%]|==|!=|>=|<=|&&|\\|\\||!|=|\\+=|-=|\\*=|/=|%=|\\+\\+|--|&|\\||\\^|~|<<|>>|>>>");
    private static final Pattern patronEspecial = Pattern.compile("[!¡°|#\\$&()¿?'@\\\\\\[\\]{};,.:_]");

    public Lexer(TokenStorage storage) {
        this.storage = storage;
        cargarMapas();
    }

    private void cargarMapas() {
        palabrasReservadasIds = new HashMap<>();
        operadoresIds = new HashMap<>();
        caracteresEspecialesIds = new HashMap<>();
        // Palabras reservadas
        palabrasReservadasIds.put("abstract", 1);
        palabrasReservadasIds.put("assert", 2);
        palabrasReservadasIds.put("boolean", 3);
        palabrasReservadasIds.put("break", 4);
        palabrasReservadasIds.put("byte", 5);
        palabrasReservadasIds.put("case", 6);
        palabrasReservadasIds.put("catch", 7);
        palabrasReservadasIds.put("char", 8);
        palabrasReservadasIds.put("class", 9);
        palabrasReservadasIds.put("continue", 10);
        palabrasReservadasIds.put("default", 11);
        palabrasReservadasIds.put("do", 12);
        palabrasReservadasIds.put("double", 13);
        palabrasReservadasIds.put("else", 14);
        palabrasReservadasIds.put("enum", 15);
        palabrasReservadasIds.put("extends", 16);
        palabrasReservadasIds.put("final", 17);
        palabrasReservadasIds.put("finally", 18);
        palabrasReservadasIds.put("float", 19);
        palabrasReservadasIds.put("for", 20);
        palabrasReservadasIds.put("if", 21);
        palabrasReservadasIds.put("implements", 22);
        palabrasReservadasIds.put("import", 23);
        palabrasReservadasIds.put("instanceof", 24);
        palabrasReservadasIds.put("int", 25);
        palabrasReservadasIds.put("interface", 26);
        palabrasReservadasIds.put("long", 27);
        palabrasReservadasIds.put("native", 28);
        palabrasReservadasIds.put("new", 29);
        palabrasReservadasIds.put("null", 30);
        palabrasReservadasIds.put("package", 31);
        palabrasReservadasIds.put("private", 32);
        palabrasReservadasIds.put("protected", 33);
        palabrasReservadasIds.put("public", 34);
        palabrasReservadasIds.put("return", 35);
        palabrasReservadasIds.put("short", 36);
        palabrasReservadasIds.put("static", 37);
        palabrasReservadasIds.put("strictfp", 38);
        palabrasReservadasIds.put("super", 39);
        palabrasReservadasIds.put("switch", 40);
        palabrasReservadasIds.put("synchronized", 41);
        palabrasReservadasIds.put("this", 42);
        palabrasReservadasIds.put("throw", 43);
        palabrasReservadasIds.put("throws", 44);
        palabrasReservadasIds.put("transient", 45);
        palabrasReservadasIds.put("try", 46);
        palabrasReservadasIds.put("void", 47);
        palabrasReservadasIds.put("volatile", 48);
        palabrasReservadasIds.put("while", 49);
        palabrasReservadasIds.put("System.out.print", 50);
        palabrasReservadasIds.put("System.out.println", 51);
        palabrasReservadasIds.put("Scanner", 52);
        palabrasReservadasIds.put("String", 53);
        palabrasReservadasIds.put("Math", 54);
        palabrasReservadasIds.put("Integer", 55);
        palabrasReservadasIds.put("Double", 56);
        palabrasReservadasIds.put("Boolean", 57);
        palabrasReservadasIds.put("Character", 58);
        palabrasReservadasIds.put("ArrayList", 59);
        palabrasReservadasIds.put("HashMap", 60);
        palabrasReservadasIds.put("LinkedList", 61);
        palabrasReservadasIds.put("HashSet", 62);
        palabrasReservadasIds.put("TreeMap", 63);
        palabrasReservadasIds.put("true", 64);
        palabrasReservadasIds.put("false", 65);

        // Operadores
        operadoresIds.put("+", 1);
        operadoresIds.put("-", 2);
        operadoresIds.put("*", 3);
        operadoresIds.put("/", 4);
        operadoresIds.put("%", 5);
        operadoresIds.put("=", 6);
        operadoresIds.put("==", 7);
        operadoresIds.put("!=", 8);
        operadoresIds.put(">=", 9);
        operadoresIds.put("<=", 10);
        operadoresIds.put("&&", 11);
        operadoresIds.put("||", 12);
        operadoresIds.put("!", 13);
        operadoresIds.put("+=", 14);
        operadoresIds.put("-=", 15);
        operadoresIds.put("*=", 16);
        operadoresIds.put("/=", 17);
        operadoresIds.put("%=", 18);
        operadoresIds.put("++", 19);
        operadoresIds.put("--", 20);
        operadoresIds.put("&", 21);
        operadoresIds.put("|", 22);
        operadoresIds.put("^", 23);
        operadoresIds.put("~", 24);
        operadoresIds.put("<<", 25);
        operadoresIds.put(">>", 26);
        operadoresIds.put(">>>", 27);

        // Caracteres especiales
        caracteresEspecialesIds.put("!", 1);
        caracteresEspecialesIds.put("¡", 2);
        caracteresEspecialesIds.put("°", 3);
        caracteresEspecialesIds.put("|", 4);
        caracteresEspecialesIds.put("#", 5);
        caracteresEspecialesIds.put("$", 6);
        caracteresEspecialesIds.put("&", 7);
        caracteresEspecialesIds.put("(", 8);
        caracteresEspecialesIds.put(")", 9);
        caracteresEspecialesIds.put("¿", 10);
        caracteresEspecialesIds.put("?", 11);
        caracteresEspecialesIds.put("'", 12);
        caracteresEspecialesIds.put("@", 13);
        caracteresEspecialesIds.put("\\", 14);
        caracteresEspecialesIds.put("[", 15);
        caracteresEspecialesIds.put("]", 16);
        caracteresEspecialesIds.put("{", 17);
        caracteresEspecialesIds.put("}", 18);
        caracteresEspecialesIds.put(",", 19);
        caracteresEspecialesIds.put(";", 20);
        caracteresEspecialesIds.put(".", 21);
        caracteresEspecialesIds.put(":", 22);
        caracteresEspecialesIds.put("_", 23);
    }

    public Map<String, Integer> getPalabrasReservadasIds() {
        return palabrasReservadasIds;
    }

    public Map<String, Integer> getOperadoresIds() {
        return operadoresIds;
    }

    public Map<String, Integer> getCaracteresEspecialesIds() {
        return caracteresEspecialesIds;
    }

    public void analizarCodigo(String codigo) {
        storage.limpiar();

        codigo = eliminarComentarios(codigo);

        String[] lineas = codigo.split("\n");

        for (int linea = 0; linea < lineas.length; linea++) {
            String codigoLinea = lineas[linea];

            Matcher matcherCadena = patronCadena.matcher(codigoLinea);
            List<int[]> exclusionRanges = new ArrayList<>();

            while (matcherCadena.find()) {
                String token = matcherCadena.group();
                exclusionRanges.add(new int[]{matcherCadena.start(), matcherCadena.end()});

                Token tokenExistente = storage.obtenerToken("strings", token);
                if (tokenExistente != null) {
                    tokenExistente.agregarLinea(linea + 1);
                } else {
                    storage.agregarToken("strings", new Token(idStrings++, token, "9", linea + 1));
                }
            }

            Matcher matcherCaracter = patronCaracter.matcher(codigoLinea);
            while (matcherCaracter.find()) {
                String token = matcherCaracter.group();
                exclusionRanges.add(new int[]{matcherCaracter.start(), matcherCaracter.end()});

                Token tokenExistente = storage.obtenerToken("caracteres", token);
                if (tokenExistente != null) {
                    tokenExistente.agregarLinea(linea + 1);
                } else {
                    storage.agregarToken("caracteres", new Token(idChars++, token, "8", linea + 1));
                }
            }

            Matcher matcherIdentificador = patronIdentificador.matcher(codigoLinea);
            while (matcherIdentificador.find()) {
                String token = matcherIdentificador.group();

                if (!estaEnRango(matcherIdentificador.start(), exclusionRanges)) {

                    if (palabrasReservadas.contains(token)) {
                        Token tokenExistente = storage.obtenerToken("palabras_reservadas", token);
                        if (tokenExistente != null) {
                            tokenExistente.agregarLinea(linea + 1);
                        } else {
                            Integer id = palabrasReservadasIds.get(token); // Usar el ID del mapa
                            storage.agregarToken("palabras_reservadas", new Token(id, token, "1", linea + 1));
                        }
                    } else {

                        Token tokenExistente = storage.obtenerToken("identificadores", token);
                        if (tokenExistente != null) {
                            tokenExistente.agregarLinea(linea + 1);
                        } else {
                            storage.agregarToken("identificadores", new Token(idIdentificadores++, token, "4", linea + 1));
                        }
                    }
                }
            }

            Matcher matcherCaracterEspecial = patronEspecial.matcher(codigoLinea);
            while (matcherCaracterEspecial.find()) {
                String token = matcherCaracterEspecial.group();

                if (!estaEnRango(matcherCaracterEspecial.start(), exclusionRanges)) {
                    Token tokenExistente = storage.obtenerToken("caracteres_especiales", token);
                    if (tokenExistente != null) {
                        tokenExistente.agregarLinea(linea + 1);
                    } else {
                        Integer id = caracteresEspecialesIds.get(token); // Usar el ID del mapa
                        storage.agregarToken("caracteres_especiales", new Token(id, token, "2", linea + 1));
                    }
                }
            }

            Matcher matcherOperador = patronOperador.matcher(codigoLinea);
            while (matcherOperador.find()) {
                String token = matcherOperador.group();

                if (!estaEnRango(matcherOperador.start(), exclusionRanges)) {
                    Token tokenExistente = storage.obtenerToken("operadores", token);
                    if (tokenExistente != null) {
                        tokenExistente.agregarLinea(linea + 1);
                    } else {
                        Integer id = operadoresIds.get(token); // Usar el ID del mapa
                        storage.agregarToken("operadores", new Token(id, token, "3", linea + 1));
                    }
                }
            }

            Matcher matcherCientifico = patronCientifico.matcher(codigoLinea);
            while (matcherCientifico.find()) {
                if (!estaEnRango(matcherCientifico.start(), exclusionRanges)) {
                    String token = matcherCientifico.group();
                    exclusionRanges.add(new int[]{matcherCientifico.start(), matcherCientifico.end()});

                    Token tokenExistente = storage.obtenerToken("cientificos", token);
                    if (tokenExistente != null) {
                        tokenExistente.agregarLinea(linea + 1);
                    } else {
                        storage.agregarToken("cientificos", new Token(idCientificos++, token, "7", linea + 1));
                    }
                }
            }

            Matcher matcherReal = patronReal.matcher(codigoLinea);
            while (matcherReal.find()) {
                if (!estaEnRango(matcherReal.start(), exclusionRanges)) {
                    String token = matcherReal.group();
                    exclusionRanges.add(new int[]{matcherReal.start(), matcherReal.end()});

                    Token tokenExistente = storage.obtenerToken("reales", token);
                    if (tokenExistente != null) {
                        tokenExistente.agregarLinea(linea + 1);
                    } else {
                        storage.agregarToken("reales", new Token(idReales++, token, "6", linea + 1));
                    }
                }
            }

            Matcher matcherEntero = patronEntero.matcher(codigoLinea);
            while (matcherEntero.find()) {
                if (!estaEnRango(matcherEntero.start(), exclusionRanges)) {
                    String token = matcherEntero.group();
                    exclusionRanges.add(new int[]{matcherEntero.start(), matcherEntero.end()});

                    Token tokenExistente = storage.obtenerToken("enteros", token);
                    if (tokenExistente != null) {
                        tokenExistente.agregarLinea(linea + 1);
                    } else {
                        storage.agregarToken("enteros", new Token(idEnteros++, token, "5", linea + 1));
                    }
                }
            }
        }
    }

    private boolean estaEnRango(int posicion, List<int[]> exclusionRanges) {
        for (int[] rango : exclusionRanges) {
            if (posicion >= rango[0] && posicion <= rango[1]) {
                return true;
            }
        }
        return false;
    }

    private String eliminarComentarios(String codigo) {
        String sinComentarios = codigo.replaceAll("//.*", "");
        sinComentarios = sinComentarios.replaceAll("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/", "");
        return sinComentarios;
    }

}
