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
import java.util.List;

/**
 * The options of {@code bun fmt}.
 */
public class FormatOption extends Option<FormatOption> {

    private final List<String> paths = new ArrayList();
    private final List<String> ignorePatterns = new ArrayList();

    private boolean check;
    private boolean listDifferent;
    private boolean useTabs;
    private boolean noSemi;
    private boolean singleQuote;
    private boolean sortImports;
    private boolean sortTailwindcss;
    private boolean sortPackageJson;
    private boolean jsdoc;
    private boolean noEditorconfig;
    private boolean staged;
    private boolean noScript = true;
    private int printWidth = -1;
    private int tabWidth = -1;
    private String trailingComma;
    private String changed;

    /**
     * Adds files, directories or globs to format.
     *
     * @param paths Target paths.
     * @return Chainable API.
     */
    public FormatOption path(String... paths) {
        for (String path : paths) {
            if (path != null && !path.isBlank()) {
                this.paths.add(path);
            }
        }
        return this;
    }

    public FormatOption check(boolean enabled) {
        this.check = enabled;
        return this;
    }

    public FormatOption listDifferent(boolean enabled) {
        this.listDifferent = enabled;
        return this;
    }

    public FormatOption printWidth(int width) {
        this.printWidth = width;
        return this;
    }

    public FormatOption tabWidth(int width) {
        this.tabWidth = width;
        return this;
    }

    public FormatOption useTabs(boolean enabled) {
        this.useTabs = enabled;
        return this;
    }

    public FormatOption semi(boolean enabled) {
        this.noSemi = !enabled;
        return this;
    }

    public FormatOption singleQuote(boolean enabled) {
        this.singleQuote = enabled;
        return this;
    }

    public FormatOption trailingComma(String value) {
        this.trailingComma = value;
        return this;
    }

    public FormatOption sortImports(boolean enabled) {
        this.sortImports = enabled;
        return this;
    }

    public FormatOption sortTailwindcss(boolean enabled) {
        this.sortTailwindcss = enabled;
        return this;
    }

    public FormatOption sortPackageJson(boolean enabled) {
        this.sortPackageJson = enabled;
        return this;
    }

    public FormatOption jsdoc(boolean enabled) {
        this.jsdoc = enabled;
        return this;
    }

    public FormatOption noEditorconfig(boolean enabled) {
        this.noEditorconfig = enabled;
        return this;
    }

    public FormatOption staged(boolean enabled) {
        this.staged = enabled;
        return this;
    }

    /**
     * Controls whether the package.json fmt script should be used instead of the built-in
     * formatter. The default is false, which always runs the built-in formatter.
     *
     * @param enabled Whether to allow a script.
     * @return Chainable API.
     */
    public FormatOption script(boolean enabled) {
        this.noScript = !enabled;
        return this;
    }

    public FormatOption ignorePattern(String pattern) {
        this.ignorePatterns.add(pattern);
        return this;
    }

    public FormatOption changed(String ref) {
        this.changed = ref == null ? "" : ref;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String command() {
        return "fmt";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> arguments() {
        List<String> arguments = new ArrayList();
        arguments.add("fmt");
        arguments.addAll(paths);

        flag(arguments, "--no-script", noScript);
        flag(arguments, "--check", check);
        flag(arguments, "--list-different", listDifferent);
        flag(arguments, "--staged", staged);
        if (changed != null) {
            arguments.add(changed.isEmpty() ? "--changed" : "--changed=" + changed);
        }
        values(arguments, "--ignore-pattern", ignorePatterns);
        if (printWidth >= 0) {
            value(arguments, "--print-width", String.valueOf(printWidth));
        }
        if (tabWidth >= 0) {
            value(arguments, "--tab-width", String.valueOf(tabWidth));
        }
        flag(arguments, "--use-tabs", useTabs);
        flag(arguments, "--no-semi", noSemi);
        flag(arguments, "--single-quote", singleQuote);
        value(arguments, "--trailing-comma", trailingComma);
        flag(arguments, "--sort-imports", sortImports);
        flag(arguments, "--sort-tailwindcss", sortTailwindcss);
        flag(arguments, "--sort-package-json", sortPackageJson);
        flag(arguments, "--jsdoc", jsdoc);
        flag(arguments, "--no-editorconfig", noEditorconfig);
        arguments.addAll(extra);
        return arguments;
    }
}
