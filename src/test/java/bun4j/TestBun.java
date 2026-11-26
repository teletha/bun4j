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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Creates a tiny fake bun executable for tests which do not need a real bun runtime.
 */
final class TestBun {

    private TestBun() {
    }

    static Path create(Path directory) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        String name = windows ? "bun.bat" : "bun";
        String[] lines = windows ? new String[] {
                "@echo off",
                "if \"%~1\"==\"--version\" (",
                "  echo 1.2.3",
                "  exit /b 0",
                ")",
                "echo OUT: %*",
                "echo ERR: %* 1>&2",
                "if not \"%FAKE_BUN_EXIT%\"==\"\" exit /b %FAKE_BUN_EXIT%",
                "exit /b 0"
        } : new String[] {
                "#!/bin/sh",
                "if [ \"$1\" = \"--version\" ]; then echo 1.2.3; exit 0; fi",
                "echo \"OUT: $*\"",
                "echo \"ERR: $*\" >&2",
                "exit ${FAKE_BUN_EXIT:-0}"
        };

        Path executable = directory.resolve(name).toAbsolutePath().normalize();
        try {
            Files.writeString(executable, String.join(System.lineSeparator(), lines) + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        executable.toFile().setExecutable(true, false);
        return executable;
    }
}
