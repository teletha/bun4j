/*
 * Copyright (C) 2026 The BUN4J Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bun4j.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A small JSON reader which is sufficient for bun's machine readable reports.
 */
public final class Json {

    private Json() {
    }

    /**
     * Parses a JSON text into Maps, Lists, Strings, BigDecimal, Boolean or null.
     *
     * @param source A JSON text.
     * @return The parsed value.
     */
    public static Object parse(String source) {
        if (source == null) {
            return null;
        }
        Reader reader = new Reader(source);
        Object value = reader.value();
        reader.space();
        return reader.done() ? value : null;
    }

    private static final class Reader {

        private final String source;
        private int index;

        private Reader(String source) {
            this.source = source;
        }

        private boolean done() {
            return index >= source.length();
        }

        private void space() {
            while (!done()) {
                char c = source.charAt(index);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                    index++;
                } else {
                    break;
                }
            }
        }

        private char peek() {
            space();
            return done() ? '\0' : source.charAt(index);
        }

        private Object value() {
            switch (peek()) {
                case '{' -> {
                    return object();
                }
                case '[' -> {
                    return array();
                }
                case '"' -> {
                    return string();
                }
                case 't' -> {
                    literal("true");
                    return Boolean.TRUE;
                }
                case 'f' -> {
                    literal("false");
                    return Boolean.FALSE;
                }
                case 'n' -> {
                    literal("null");
                    return null;
                }
                default -> {
                    return number();
                }
            }
        }

        private Map<String, Object> object() {
            Map<String, Object> map = new LinkedHashMap();
            index++;
            while (true) {
                char c = peek();
                if (c == '}') {
                    index++;
                    return map;
                }
                String key = string();
                if (peek() != ':') {
                    throw new IllegalArgumentException("Expected ':' at " + index);
                }
                index++;
                map.put(key, value());
                c = peek();
                if (c == ',') {
                    index++;
                } else if (c != '}') {
                    throw new IllegalArgumentException("Expected ',' or '}' at " + index);
                }
            }
        }

        private List<Object> array() {
            List<Object> list = new ArrayList();
            index++;
            while (true) {
                char c = peek();
                if (c == ']') {
                    index++;
                    return list;
                }
                list.add(value());
                c = peek();
                if (c == ',') {
                    index++;
                } else if (c != ']') {
                    throw new IllegalArgumentException("Expected ',' or ']' at " + index);
                }
            }
        }

        private String string() {
            if (peek() != '"') {
                throw new IllegalArgumentException("Expected string at " + index);
            }
            index++;
            StringBuilder text = new StringBuilder();
            while (!done()) {
                char c = source.charAt(index++);
                if (c == '"') {
                    return text.toString();
                }
                if (c != '\\') {
                    text.append(c);
                    continue;
                }
                if (done()) {
                    break;
                }
                char escape = source.charAt(index++);
                switch (escape) {
                    case '"', '\\', '/' -> text.append(escape);
                    case 'b' -> text.append('\b');
                    case 'f' -> text.append('\f');
                    case 'n' -> text.append('\n');
                    case 'r' -> text.append('\r');
                    case 't' -> text.append('\t');
                    case 'u' -> {
                        if (index + 4 > source.length()) {
                            throw new IllegalArgumentException("Invalid unicode escape at " + index);
                        }
                        text.append((char) Integer.parseInt(source.substring(index, index + 4), 16));
                        index += 4;
                    }
                    default -> throw new IllegalArgumentException("Invalid escape at " + index);
                }
            }
            throw new IllegalArgumentException("Unterminated string at " + index);
        }

        private BigDecimal number() {
            space();
            int begin = index;
            while (!done()) {
                char c = source.charAt(index);
                if (c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E' || (c >= '0' && c <= '9')) {
                    index++;
                } else {
                    break;
                }
            }
            if (begin == index) {
                throw new IllegalArgumentException("Expected value at " + index);
            }
            return new BigDecimal(source.substring(begin, index));
        }

        private void literal(String word) {
            if (!source.startsWith(word, index)) {
                throw new IllegalArgumentException("Expected " + word + " at " + index);
            }
            index += word.length();
        }
    }
}
