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
 * The options of the low level {@code bun build} CLI command.
 */
public class BuildOption extends BuildConfig<BuildOption> {

    private final List<String> drop = new ArrayList();
    private final Map<String, String> loader = new LinkedHashMap();

    private String root;
    private String publicPath;
    private String entryNaming;
    private String chunkNaming;
    private String assetNaming;
    private String banner;
    private String footer;
    private boolean minifyWhitespace;
    private boolean minifyIdentifiers;
    private boolean minifySyntax;
    private boolean modulePreload = true;
    private boolean watch;
    private boolean noBundle;

    public BuildOption root(String path) {
        this.root = path;
        return this;
    }

    public BuildOption publicPath(String path) {
        this.publicPath = path;
        return this;
    }

    public BuildOption entryNaming(String pattern) {
        this.entryNaming = pattern;
        return this;
    }

    public BuildOption chunkNaming(String pattern) {
        this.chunkNaming = pattern;
        return this;
    }

    public BuildOption assetNaming(String pattern) {
        this.assetNaming = pattern;
        return this;
    }

    public BuildOption banner(String text) {
        this.banner = text;
        return this;
    }

    public BuildOption footer(String text) {
        this.footer = text;
        return this;
    }

    public BuildOption minifyWhitespace(boolean enabled) {
        this.minifyWhitespace = enabled;
        return this;
    }

    public BuildOption minifyIdentifiers(boolean enabled) {
        this.minifyIdentifiers = enabled;
        return this;
    }

    public BuildOption minifySyntax(boolean enabled) {
        this.minifySyntax = enabled;
        return this;
    }

    public BuildOption modulePreload(boolean enabled) {
        this.modulePreload = enabled;
        return this;
    }

    public BuildOption watch(boolean enabled) {
        this.watch = enabled;
        return this;
    }

    public BuildOption noBundle(boolean enabled) {
        this.noBundle = enabled;
        return this;
    }

    public BuildOption loader(String extension, String loader) {
        this.loader.put(extension, loader);
        return this;
    }

    public BuildOption drop(String... identifiers) {
        for (String identifier : identifiers) {
            if (identifier != null && !identifier.isBlank()) {
                drop.add(identifier);
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String command() {
        return "build";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> arguments() {
        List<String> arguments = new ArrayList();
        arguments.add("build");
        arguments.addAll(entries);

        headerArguments(arguments);

        flag(arguments, "--minify", minify);
        flag(arguments, "--minify-whitespace", minifyWhitespace);
        flag(arguments, "--minify-identifiers", minifyIdentifiers);
        flag(arguments, "--minify-syntax", minifySyntax);
        flag(arguments, "--splitting", splitting);
        flag(arguments, "--no-module-preload", !modulePreload);
        flag(arguments, "--check", check);
        flag(arguments, "--dts", dts);
        flag(arguments, "--watch", watch);
        flag(arguments, "--no-bundle", noBundle);

        value(arguments, "--root", root);
        value(arguments, "--public-path", publicPath);
        value(arguments, "--entry-naming", entryNaming);
        value(arguments, "--chunk-naming", chunkNaming);
        value(arguments, "--asset-naming", assetNaming);
        value(arguments, "--banner", banner);
        value(arguments, "--footer", footer);

        definedArguments(arguments);
        values(arguments, "--loader", loader.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).toList());
        values(arguments, "--drop", drop);
        arguments.addAll(extra);
        return arguments;
    }
}
