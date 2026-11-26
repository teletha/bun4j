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
 * The options of {@code bun lint}.
 */
public class LintOption extends Option<LintOption> {

    private final List<String> paths = new ArrayList();
    private final List<String> rules = new ArrayList();
    private final List<String> ignorePatterns = new ArrayList();

    private boolean fix;
    private boolean fixSuggestions;
    private boolean fixDryRun;
    private boolean typeAware;
    private boolean typeCheck;
    private boolean staged;
    private boolean quiet;
    private boolean all;
    private boolean noIgnore;
    private boolean noScript = true;
    private boolean fallback = true;
    private int maxWarnings = -1;
    private String changed;
    private String format;
    private String outputFile;
    private String tsconfig;

    /**
     * Adds files, directories or globs to lint.
     *
     * @param paths Target paths.
     * @return Chainable API.
     */
    public LintOption path(String... paths) {
        for (String path : paths) {
            if (path != null && !path.isBlank()) {
                this.paths.add(path);
            }
        }
        return this;
    }

    public LintOption fix(boolean enabled) {
        this.fix = enabled;
        return this;
    }

    public LintOption fixSuggestions(boolean enabled) {
        this.fixSuggestions = enabled;
        return this;
    }

    public LintOption fixDryRun(boolean enabled) {
        this.fixDryRun = enabled;
        return this;
    }

    public LintOption typeAware(boolean enabled) {
        this.typeAware = enabled;
        return this;
    }

    public LintOption typeCheck(boolean enabled) {
        this.typeCheck = enabled;
        return this;
    }

    public LintOption staged(boolean enabled) {
        this.staged = enabled;
        return this;
    }

    public LintOption quiet(boolean enabled) {
        this.quiet = enabled;
        return this;
    }

    public LintOption all(boolean enabled) {
        this.all = enabled;
        return this;
    }

    public LintOption noIgnore(boolean enabled) {
        this.noIgnore = enabled;
        return this;
    }

    /**
     * Controls whether the package.json lint script should be used instead of the built-in linter.
     * The default is false, which always runs the built-in linter.
     *
     * @param enabled Whether to allow a script.
     * @return Chainable API.
     */
    public LintOption script(boolean enabled) {
        this.noScript = !enabled;
        return this;
    }

    /**
     * Controls whether oxlint is used when the installed bun has no built-in {@code bun lint}. The
     * default is true.
     *
     * @param enabled Whether to allow the fallback.
     * @return Chainable API.
     */
    public LintOption fallback(boolean enabled) {
        this.fallback = enabled;
        return this;
    }

    public boolean fallback() {
        return fallback;
    }

    public LintOption maxWarnings(int count) {
        this.maxWarnings = count;
        return this;
    }

    /**
     * Lints only files differing from a git ref.
     *
     * @param ref A git ref, or null to use the default branch.
     * @return Chainable API.
     */
    public LintOption changed(String ref) {
        this.changed = ref == null ? "" : ref;
        return this;
    }

    public LintOption format(String format) {
        this.format = format;
        return this;
    }

    public LintOption outputFile(String path) {
        this.outputFile = path;
        return this;
    }

    public LintOption tsconfig(String path) {
        this.tsconfig = path;
        return this;
    }

    public LintOption rule(String rule) {
        this.rules.add(rule);
        return this;
    }

    public LintOption ignorePattern(String pattern) {
        this.ignorePatterns.add(pattern);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String command() {
        return "lint";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> arguments() {
        List<String> arguments = new ArrayList();
        arguments.add("lint");
        arguments.addAll(paths);

        flag(arguments, "--no-script", noScript);
        flag(arguments, "--fix", fix);
        flag(arguments, "--fix-suggestions", fixSuggestions);
        flag(arguments, "--fix-dry-run", fixDryRun);
        flag(arguments, "--type-aware", typeAware);
        flag(arguments, "--type-check", typeCheck);
        value(arguments, "--tsconfig", tsconfig);
        flag(arguments, "--staged", staged);
        if (changed != null) {
            arguments.add(changed.isEmpty() ? "--changed" : "--changed=" + changed);
        }
        values(arguments, "--rule", rules);
        values(arguments, "--ignore-pattern", ignorePatterns);
        flag(arguments, "--no-ignore", noIgnore);
        flag(arguments, "--quiet", quiet);
        flag(arguments, "--all", all);
        if (maxWarnings >= 0) {
            value(arguments, "--max-warnings", String.valueOf(maxWarnings));
        }
        value(arguments, "--format", format);
        value(arguments, "--output-file", outputFile);
        arguments.addAll(extra);
        return arguments;
    }

    /**
     * Builds the command line for the oxlint fallback.
     *
     * @return A list of arguments for {@code bun x oxlint}.
     */
    public List<String> fallbackArguments() {
        List<String> arguments = new ArrayList();
        arguments.add("x");
        arguments.add("oxlint");
        arguments.addAll(paths);

        flag(arguments, "--fix", fix);
        flag(arguments, "--fix-suggestions", fixSuggestions);
        flag(arguments, "--type-aware", typeAware);
        flag(arguments, "--type-check", typeCheck);
        value(arguments, "--tsconfig", tsconfig);
        values(arguments, "--ignore-pattern", ignorePatterns);
        flag(arguments, "--no-ignore", noIgnore);
        flag(arguments, "--quiet", quiet);
        if (maxWarnings >= 0) {
            value(arguments, "--max-warnings", String.valueOf(maxWarnings));
        }
        value(arguments, "--format", format);

        for (String rule : rules) {
            int separator = rule.indexOf(':');
            if (separator == -1) {
                value(arguments, "--deny", rule);
                continue;
            }
            String name = rule.substring(0, separator).trim();
            String severity = rule.substring(separator + 1).trim();
            switch (severity) {
                case "warn", "warning" -> value(arguments, "--warn", name);
                case "off" -> value(arguments, "--allow", name);
                default -> value(arguments, "--deny", name);
            }
        }
        arguments.addAll(extra);
        return arguments;
    }
}
