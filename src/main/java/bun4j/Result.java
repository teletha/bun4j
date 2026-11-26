/*
 * Copyright (C) 2026 The BUN4J Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bun4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bun4j.internal.Json;

/**
 * The result of a bun command invocation.
 *
 * @param exitCode the process exit code
 * @param stdout the text written to the standard output
 * @param stderr the text written to the standard error output
 */
public record Result(int exitCode, String stdout, String stderr) {

    /**
     * Returns whether the command finished successfully.
     *
     * @return true if the exit code is zero.
     */
    public boolean succeeded() {
        return exitCode == 0;
    }

    /**
     * Returns the combined output of the standard output and the standard error output.
     *
     * @return A combined text.
     */
    public String output() {
        if (stderr.isEmpty()) {
            return stdout;
        } else if (stdout.isEmpty()) {
            return stderr;
        } else {
            return stdout + stderr;
        }
    }

    /**
     * Returns every non blank line of the standard output.
     *
     * @return A list of lines.
     */
    public List<String> lines() {
        return stdout.lines().filter(line -> !line.isBlank()).toList();
    }

    private static final Pattern PLAIN = Pattern.compile("^(.+?):(\\d+):(\\d+):\\s*(.*)$");

    /**
     * Extracts structured diagnostics from the command output. JSON reports from {@code bun lint
     * -f json} or oxlint are understood, as well as plain {@code file:line:column: message} lines.
     *
     * @return A list of diagnostics.
     */
    public List<Diagnostic> diagnostics() {
        List<Diagnostic> found = new ArrayList();
        Set<String> keys = new HashSet();
        String text = output().strip();

        if (text.startsWith("{") || text.startsWith("[")) {
            try {
                collect(Json.parse(text), found, keys);
            } catch (RuntimeException ignored) {
                // Fall through to the plain text parser.
            }
        }

        if (found.isEmpty()) {
            for (String line : output().lines().toList()) {
                Matcher matcher = PLAIN.matcher(line.strip());
                if (matcher.matches()) {
                    String detail = matcher.group(4).strip();
                    String severity = "error";
                    if (detail.toLowerCase().startsWith("warning") || detail.toLowerCase().startsWith("warn")) {
                        severity = "warning";
                    } else if (detail.toLowerCase().startsWith("note") || detail.toLowerCase().startsWith("info")) {
                        severity = "info";
                    }
                    add(new Diagnostic(matcher.group(1), integer(matcher.group(2)), integer(matcher.group(3)),
                            severity, "", detail), found, keys);
                }
            }
        }
        return List.copyOf(found);
    }

    private static void collect(Object node, List<Diagnostic> found, Set<String> keys) {
        if (node instanceof List<?> list) {
            for (Object item : list) {
                collect(item, found, keys);
            }
        } else if (node instanceof Map<?, ?> map) {
            String message = text(map, "message", "text", "description");
            if (message != null) {
                Map<?, ?> span = span(map);
                add(new Diagnostic(
                        text(map, "file", "filename", "path", "filePath", "filepath") == null
                                ? "" : text(map, "file", "filename", "path", "filePath", "filepath"),
                        number(map, "line", "lineNumber") == 0 ? number(span, "line") : number(map, "line", "lineNumber"),
                        number(map, "column", "col", "columnNumber") == 0 ? number(span, "column") : number(map, "column", "col", "columnNumber"),
                        normalizeSeverity(text(map, "severity", "level", "type")),
                        rule(map),
                        message), found, keys);
            }
            for (Object value : map.values()) {
                collect(value, found, keys);
            }
        }
    }

    private static void add(Diagnostic diagnostic, List<Diagnostic> found, Set<String> keys) {
        String key = diagnostic.file() + ":" + diagnostic.line() + ":" + diagnostic.column() + ":" + diagnostic.message();
        if (keys.add(key)) {
            found.add(diagnostic);
        }
    }

    private static Map<?, ?> span(Map<?, ?> map) {
        Object labels = map.get("labels");
        if (labels instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> label) {
            if (label.get("span") instanceof Map<?, ?> value) {
                return value;
            }
        }
        return Map.of();
    }

    private static String rule(Map<?, ?> map) {
        for (String key : new String[] {"rule", "ruleId", "rule_id", "code", "linter"}) {
            Object value = map.get(key);
            if (value instanceof String text) {
                return text;
            }
            if (value instanceof Map<?, ?> nested) {
                String text = text(nested, "code", "name", "id");
                if (text != null) {
                    return text;
                }
            }
        }
        return "";
    }

    private static String normalizeSeverity(String value) {
        if (value == null) {
            return "error";
        }
        String lower = value.toLowerCase();
        if (lower.contains("warn")) {
            return "warning";
        }
        if (lower.contains("info") || lower.contains("note")) {
            return "info";
        }
        return lower.contains("error") ? "error" : lower;
    }

    private static String text(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof String text && !text.isBlank()) {
                return text;
            }
        }
        return null;
    }

    private static int number(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof Number number) {
                return number.intValue();
            }
            if (value instanceof String text) {
                try {
                    return Integer.parseInt(text.strip());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0;
    }

    private static int integer(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Ensures that the command finished successfully.
     *
     * @return This result itself.
     * @throws BunException If the exit code is not zero.
     */
    public Result requireSuccess() {
        if (!succeeded()) {
            throw new BunException("bun command failed with exit code " + exitCode + ".\n" + output());
        }
        return this;
    }
}
