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
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The common settings shared by all bun commands.
 *
 * @param <Self> The concrete option type.
 */
public abstract class Option<Self extends Option<Self>> {

    /** The working directory, or null to use the current directory. */
    protected Path directory;

    /** The executable override, or null to use the detected bun. */
    protected Path executable;

    /** Additional environment variables. */
    protected final Map<String, String> environment = new LinkedHashMap();

    /** The listener which receives every line of the standard output. */
    protected Consumer<String> stdout = line -> {};

    /** The listener which receives every line of the standard error output. */
    protected Consumer<String> stderr = line -> {};

    /** The maximum duration of the command. */
    protected Duration timeout = Duration.ofMinutes(10);

    /** Extra arguments appended verbatim to the command line. */
    protected final List<String> extra = new ArrayList();

    /**
     * Returns this instance as the concrete option type.
     *
     * @return This instance.
     */
    @SuppressWarnings("unchecked")
    protected final Self self() {
        return (Self) this;
    }

    /**
     * Sets the working directory of the command.
     *
     * @param path A directory path.
     * @return Chainable API.
     */
    public final Self directory(String path) {
        return directory(Path.of(path));
    }

    /**
     * Sets the working directory of the command.
     *
     * @param path A directory path.
     * @return Chainable API.
     */
    public final Self directory(Path path) {
        this.directory = path;
        return self();
    }

    /**
     * Overrides the bun executable to use. Mainly useful for testing.
     *
     * @param path A path to an executable.
     * @return Chainable API.
     */
    public final Self executable(String path) {
        return executable(Path.of(path));
    }

    /**
     * Overrides the bun executable to use. Mainly useful for testing.
     *
     * @param path A path to an executable.
     * @return Chainable API.
     */
    public final Self executable(Path path) {
        this.executable = path;
        return self();
    }

    /**
     * Adds an environment variable.
     *
     * @param key A variable name.
     * @param value A variable value.
     * @return Chainable API.
     */
    public final Self env(String key, String value) {
        this.environment.put(key, value);
        return self();
    }

    /**
     * Sets the listener which is called for every line of the standard output.
     *
     * @param listener A line listener.
     * @return Chainable API.
     */
    public final Self stdout(Consumer<String> listener) {
        this.stdout = listener == null ? line -> {} : listener;
        return self();
    }

    /**
     * Sets the listener which is called for every line of the standard error output.
     *
     * @param listener A line listener.
     * @return Chainable API.
     */
    public final Self stderr(Consumer<String> listener) {
        this.stderr = listener == null ? line -> {} : listener;
        return self();
    }

    /**
     * Prints both output streams to the console as they arrive.
     *
     * @return Chainable API.
     */
    public final Self echo() {
        return stdout(System.out::println).stderr(System.err::println);
    }

    /**
     * Sets the maximum duration of the command.
     *
     * @param timeout A duration.
     * @return Chainable API.
     */
    public final Self timeout(Duration timeout) {
        this.timeout = timeout;
        return self();
    }

    /**
     * Appends arguments verbatim to the command line. Use this for flags which bun4j does not
     * expose yet.
     *
     * @param arguments Additional arguments.
     * @return Chainable API.
     */
    public final Self raw(String... arguments) {
        for (String argument : arguments) {
            if (argument != null && !argument.isEmpty()) {
                this.extra.add(argument);
            }
        }
        return self();
    }

    /**
     * Returns the working directory.
     *
     * @return A directory, or null if the current directory is used.
     */
    public final Path directory() {
        return directory;
    }

    /**
     * Returns the executable override.
     *
     * @return A path, or null if the detected bun is used.
     */
    public final Path executable() {
        return executable;
    }

    /**
     * Returns the additional environment variables.
     *
     * @return A map of variables.
     */
    public final Map<String, String> environment() {
        return environment;
    }

    /**
     * Returns the standard output listener.
     *
     * @return A listener.
     */
    public final Consumer<String> stdout() {
        return stdout;
    }

    /**
     * Returns the standard error listener.
     *
     * @return A listener.
     */
    public final Consumer<String> stderr() {
        return stderr;
    }

    /**
     * Returns the command timeout.
     *
     * @return A duration.
     */
    public final Duration timeout() {
        return timeout;
    }

    /**
     * Returns the bun subcommand to invoke.
     *
     * @return A subcommand name.
     */
    public abstract String command();

    /**
     * Builds the command line arguments, including the subcommand.
     *
     * @return A list of arguments.
     */
    public List<String> arguments() {
        return new ArrayList();
    }

    /**
     * Appends a boolean flag.
     *
     * @param arguments The destination.
     * @param flag The flag.
     * @param enabled Whether the flag is enabled.
     */
    protected final void flag(List<String> arguments, String flag, boolean enabled) {
        if (enabled) {
            arguments.add(flag);
        }
    }

    /**
     * Appends a flag followed by its value.
     *
     * @param arguments The destination.
     * @param flag The flag.
     * @param value The value, which is ignored when null or blank.
     */
    protected final void value(List<String> arguments, String flag, String value) {
        if (value != null && !value.isBlank()) {
            arguments.add(flag);
            arguments.add(value);
        }
    }

    /**
     * Appends every item as a repeated flag.
     *
     * @param arguments The destination.
     * @param flag The flag.
     * @param values The values.
     */
    protected final void values(List<String> arguments, String flag, List<String> values) {
        for (String value : values) {
            value(arguments, flag, value);
        }
    }
}
