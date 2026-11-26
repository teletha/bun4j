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

import java.util.List;

/**
 * Frontend frameworks supported by the library bundle presets.
 */
public enum Framework {
    /** React and Preact are handled natively by bun through JSX/TSX. */
    REACT(List.of()),
    /** Requires {@code bun-plugin-svelte}. */
    SVELTE(List.of("bun-plugin-svelte@0.0.6")),
    /** Requires {@code @eckidevs/bun-plugin-vue}. */
    VUE(List.of("@eckidevs/bun-plugin-vue@0.3.0"));

    private final List<String> dependencies;

    Framework(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Returns the npm packages required to bundle this framework.
     *
     * @return A list of package specifiers.
     */
    public List<String> dependencies() {
        return dependencies;
    }
}
