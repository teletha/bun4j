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
 * The sourcemap style of generated bundles.
 */
public enum SourceMap {
    NONE("none"), LINKED("linked"), EXTERNAL("external"), INLINE("inline");

    private final String value;

    SourceMap(String value) {
        this.value = value;
    }

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
