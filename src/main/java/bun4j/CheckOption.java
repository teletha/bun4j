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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The options of {@code bun check}.
 */
public class CheckOption extends Option<CheckOption> {

    private final List<String> paths = new ArrayList();
    private final Map<String, String> compilerOptions = new LinkedHashMap();

    private String project;
    private boolean build;
    private boolean noPretty;
    private boolean all;
    private boolean noScript = true;
    private int threads = -1;

    public CheckOption path(String... paths) {
        for (String path : paths) {
            if (path != null && !path.isBlank()) {
                this.paths.add(path);
            }
        }
        return this;
    }

    public CheckOption project(String path) {
        this.project = path;
        return this;
    }

    public CheckOption build(boolean enabled) {
        this.build = enabled;
        return this;
    }

    public CheckOption noPretty(boolean enabled) {
        this.noPretty = enabled;
        return this;
    }

    public CheckOption all(boolean enabled) {
        this.all = enabled;
        return this;
    }

    public CheckOption script(boolean enabled) {
        this.noScript = !enabled;
        return this;
    }

    public CheckOption threads(int count) {
        this.threads = count;
        return this;
    }

    /**
     * Adds a TypeScript compiler option such as {@code --strict} or {@code --target es2022}.
     *
     * @param name An option name without the leading dashes.
     * @param value A value, or null for a boolean flag.
     * @return Chainable API.
     */
    public CheckOption compilerOption(String name, String value) {
        this.compilerOptions.put(name, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String command() {
        return "check";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> arguments() {
        List<String> arguments = new ArrayList();
        arguments.add("check");
        arguments.addAll(paths);

        flag(arguments, "--no-script", noScript);
        value(arguments, "--project", project);
        flag(arguments, "--build", build);
        flag(arguments, "--no-pretty", noPretty);
        flag(arguments, "--all", all);
        if (threads >= 0) {
            value(arguments, "--threads", String.valueOf(threads));
        }
        compilerOptions.forEach((name, option) -> {
            if (option == null || option.isBlank()) {
                flag(arguments, "--" + name, true);
            } else {
                value(arguments, "--" + name, option);
            }
        });
        arguments.addAll(extra);
        return arguments;
    }
}
