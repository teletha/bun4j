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
 * The options of {@code bun test}.
 */
public class TestOption extends Option<TestOption> {

    private final List<String> paths = new ArrayList();
    private final List<String> ignorePatterns = new ArrayList();

    private int timeout = -1;
    private boolean coverage;
    private String coverageReporter;
    private String coverageDir;
    private String bail;
    private String namePattern;
    private String reporter;
    private String reporterOutfile;
    private int rerunEach = -1;
    private int retry = -1;
    private boolean only;
    private boolean todo;
    private boolean passWithNoTests;
    private boolean randomize;
    private boolean onlyFailures;
    private boolean updateSnapshots;
    private int maxConcurrency = -1;
    private String changed;
    private String shard;

    public TestOption path(String... paths) {
        for (String path : paths) {
            if (path != null && !path.isBlank()) {
                this.paths.add(path);
            }
        }
        return this;
    }

    public TestOption ignorePattern(String pattern) {
        this.ignorePatterns.add(pattern);
        return this;
    }

    public TestOption timeout(int milliseconds) {
        this.timeout = milliseconds;
        return this;
    }

    public TestOption coverage(boolean enabled) {
        this.coverage = enabled;
        return this;
    }

    public TestOption coverageReporter(String reporter) {
        this.coverageReporter = reporter;
        return this;
    }

    public TestOption coverageDir(String directory) {
        this.coverageDir = directory;
        return this;
    }

    public TestOption bail(int failures) {
        this.bail = String.valueOf(failures);
        return this;
    }

    public TestOption namePattern(String pattern) {
        this.namePattern = pattern;
        return this;
    }

    public TestOption reporter(String reporter) {
        this.reporter = reporter;
        return this;
    }

    public TestOption reporterOutfile(String path) {
        this.reporterOutfile = path;
        return this;
    }

    public TestOption rerunEach(int count) {
        this.rerunEach = count;
        return this;
    }

    public TestOption retry(int count) {
        this.retry = count;
        return this;
    }

    public TestOption only(boolean enabled) {
        this.only = enabled;
        return this;
    }

    public TestOption todo(boolean enabled) {
        this.todo = enabled;
        return this;
    }

    public TestOption passWithNoTests(boolean enabled) {
        this.passWithNoTests = enabled;
        return this;
    }

    public TestOption randomize(boolean enabled) {
        this.randomize = enabled;
        return this;
    }

    public TestOption onlyFailures(boolean enabled) {
        this.onlyFailures = enabled;
        return this;
    }

    public TestOption updateSnapshots(boolean enabled) {
        this.updateSnapshots = enabled;
        return this;
    }

    public TestOption maxConcurrency(int count) {
        this.maxConcurrency = count;
        return this;
    }

    public TestOption changed(String ref) {
        this.changed = ref == null ? "" : ref;
        return this;
    }

    public TestOption shard(String shard) {
        this.shard = shard;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String command() {
        return "test";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> arguments() {
        List<String> arguments = new ArrayList();
        arguments.add("test");
        arguments.addAll(paths);

        if (timeout >= 0) {
            value(arguments, "--timeout", String.valueOf(timeout));
        }
        flag(arguments, "--coverage", coverage);
        value(arguments, "--coverage-reporter", coverageReporter);
        value(arguments, "--coverage-dir", coverageDir);
        value(arguments, "--bail", bail);
        value(arguments, "--test-name-pattern", namePattern);
        value(arguments, "--reporter", reporter);
        value(arguments, "--reporter-outfile", reporterOutfile);
        if (rerunEach >= 0) {
            value(arguments, "--rerun-each", String.valueOf(rerunEach));
        }
        if (retry >= 0) {
            value(arguments, "--retry", String.valueOf(retry));
        }
        flag(arguments, "--only", only);
        flag(arguments, "--todo", todo);
        flag(arguments, "--pass-with-no-tests", passWithNoTests);
        flag(arguments, "--randomize", randomize);
        flag(arguments, "--only-failures", onlyFailures);
        flag(arguments, "--update-snapshots", updateSnapshots);
        if (maxConcurrency >= 0) {
            value(arguments, "--max-concurrency", String.valueOf(maxConcurrency));
        }
        values(arguments, "--path-ignore-patterns", ignorePatterns);
        if (changed != null) {
            arguments.add(changed.isEmpty() ? "--changed" : "--changed=" + changed);
        }
        value(arguments, "--shard", shard);
        arguments.addAll(extra);
        return arguments;
    }
}
