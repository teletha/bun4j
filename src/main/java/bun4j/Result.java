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
