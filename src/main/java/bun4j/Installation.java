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

import java.nio.file.Path;

/**
 * Describes the bun runtime which bun4j uses.
 *
 * @param executable The path to the bun executable.
 * @param directory The directory which contains the executable.
 * @param version The detected bun version, or an empty string if it cannot be detected.
 */
public record Installation(Path executable, Path directory, String version) {

    /**
     * Returns whether this installation is managed by bun4j (located in {@code ~/.bun/bin} or a
     * user specified directory).
     *
     * @return true if the executable is a managed installation.
     */
    public boolean managed() {
        return directory != null && executable.startsWith(directory);
    }
}
