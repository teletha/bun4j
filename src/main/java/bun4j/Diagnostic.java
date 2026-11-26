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

/**
 * A single issue reported by bun's linter, formatter, type checker or bundler.
 *
 * @param file The affected file, or an empty string if unknown.
 * @param line The one based line number, or zero if unknown.
 * @param column The one based column, or zero if unknown.
 * @param severity The severity such as {@code error} or {@code warning}.
 * @param rule The rule identifier, or an empty string if unknown.
 * @param message The human readable message.
 */
public record Diagnostic(String file, int line, int column, String severity, String rule, String message) {

    /**
     * Returns whether this diagnostic is an error.
     *
     * @return true if the severity is error.
     */
    public boolean error() {
        return "error".equalsIgnoreCase(severity);
    }
}
