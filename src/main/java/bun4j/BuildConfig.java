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
 * The options shared by {@link BuildOption} and {@link BundleOption}.
 *
 * @param <Self> The concrete option type.
 */
public abstract class BuildConfig<Self extends BuildConfig<Self>> extends Option<Self> {

    protected final List<String> entries = new ArrayList();
    protected final List<String> external = new ArrayList();
    protected final Map<String, String> define = new LinkedHashMap();

    protected String outdir;
    protected Target target;
    protected ModuleFormat format;
    protected SourceMap sourcemap;
    protected boolean minify;
    protected boolean splitting;
    protected boolean check;
    protected boolean dts;

    /**
     * Adds an entrypoint.
     *
     * @param paths Entrypoint paths.
     * @return Chainable API.
     */
    public Self entry(String... paths) {
        for (String path : paths) {
            if (path != null && !path.isBlank()) {
                entries.add(path);
            }
        }
        return self();
    }

    public Self outdir(String path) {
        this.outdir = path;
        return self();
    }

    public Self target(Target target) {
        this.target = target;
        return self();
    }

    public Self format(ModuleFormat format) {
        this.format = format;
        return self();
    }

    public Self sourcemap(SourceMap sourcemap) {
        this.sourcemap = sourcemap;
        return self();
    }

    public Self minify(boolean enabled) {
        this.minify = enabled;
        return self();
    }

    public Self splitting(boolean enabled) {
        this.splitting = enabled;
        return self();
    }

    public Self check(boolean enabled) {
        this.check = enabled;
        return self();
    }

    public Self dts(boolean enabled) {
        this.dts = enabled;
        return self();
    }

    public Self external(String... modules) {
        for (String module : modules) {
            if (module != null && !module.isBlank()) {
                external.add(module);
            }
        }
        return self();
    }

    /**
     * Defines a compile time constant. The replacement is written verbatim as a JavaScript
     * expression.
     *
     * @param identifier A global identifier.
     * @param javascriptExpression A JavaScript expression such as {@code "\"production\""}.
     * @return Chainable API.
     */
    public Self define(String identifier, String javascriptExpression) {
        this.define.put(identifier, javascriptExpression);
        return self();
    }

    protected final void headerArguments(List<String> arguments) {
        value(arguments, "--outdir", outdir);
        if (target != null) {
            value(arguments, "--target", target.value());
        }
        if (format != null) {
            value(arguments, "--format", format.value());
        }
        if (sourcemap != null) {
            value(arguments, "--sourcemap", sourcemap.value());
        }
    }

    protected final void flagArguments(List<String> arguments) {
        flag(arguments, "--minify", minify);
        flag(arguments, "--splitting", splitting);
        flag(arguments, "--check", check);
        flag(arguments, "--dts", dts);
    }

    protected final void definedArguments(List<String> arguments) {
        values(arguments, "--external", external);
        define.forEach((key, replacement) -> value(arguments, "--define", key + "=" + replacement));
    }
}
