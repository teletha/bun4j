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
 * The module format of generated bundles.
 */
public enum ModuleFormat {
    ESM("esm"), CJS("cjs"), IIFE("iife");

    private final String value;

    ModuleFormat(String value) {
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
