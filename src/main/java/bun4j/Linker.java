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
 * The layout strategy Bun uses for {@code node_modules}.
 */
public enum Linker {
    /** A single shared {@code node_modules} directory at the project root. */
    HOISTED("hoisted"),
    /** A virtual store below {@code node_modules/.bun} with links into the project tree. */
    ISOLATED("isolated");

    private final String value;

    Linker(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
