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
 * The execution environment for a bundle.
 */
public enum Target {
    BROWSER("browser"), BUN("bun"), NODE("node");

    private final String value;

    Target(String value) {
        this.value = value;
    }

    /**
     * Returns the CLI value.
     *
     * @return A value understandable by bun.
     */
    public String value() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return value;
    }
}
