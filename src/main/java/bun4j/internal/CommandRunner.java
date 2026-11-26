/*
 * Copyright (C) 2026 The BUN4J Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bun4j.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import bun4j.BunException;
import bun4j.Option;
import bun4j.Result;

/**
 * Starts a bun process and pumps its output to the registered listeners.
 */
public final class CommandRunner {

    private CommandRunner() {
    }

    /**
     * Runs a command synchronously.
     *
     * @param executable The bun executable.
     * @param arguments The command line arguments.
     * @param option The common settings.
     * @return The result of the command.
     */
    public static Result run(Path executable, List<String> arguments, Option<?> option) {
        if (executable == null) {
            throw new BunException("bun is not installed and could not be installed automatically.");
        }

        List<String> command = new ArrayList();
        command.add(executable.toString());
        command.addAll(arguments);

        ProcessBuilder builder = new ProcessBuilder(command);
        if (option.directory() != null) {
            builder.directory(option.directory().toFile());
        }
        builder.environment().putAll(option.environment());

        Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new BunException("Failed to start " + executable + ".", e);
        }

        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();
        Thread outPump = pump(process.getInputStream(), out, option.stdout());
        Thread errPump = pump(process.getErrorStream(), err, option.stderr());

        try {
            if (!process.waitFor(option.timeout().toMillis(), TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                throw new BunException("bun command timed out after " + option.timeout() + ".");
            }
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new BunException("bun command was interrupted.", e);
        }

        try {
            outPump.join(1000);
            errPump.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new Result(process.exitValue(), out.toString(), err.toString());
    }

    /**
     * Starts a daemon thread which reads a stream line by line.
     */
    private static Thread pump(InputStream stream, StringBuilder buffer, Consumer<String> listener) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append('\n');
                    try {
                        listener.accept(line);
                    } catch (RuntimeException ignored) {
                        // A broken listener must not stop the process from being consumed.
                    }
                }
            } catch (IOException ignored) {
                // The stream can be closed when the process is forcibly destroyed.
            }
        }, "bun4j-output-pump");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
}
