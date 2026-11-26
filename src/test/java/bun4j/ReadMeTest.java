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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Code snippets used by {@code ci:readme}. They are disabled because they require a real bun
 * installation and a frontend project.
 */
@Disabled("README snippets")
class ReadMeTest {

    /**
     * Bun is installed into ~/.bun/bin automatically before the first command in a JVM, and checked for updates at most once a week. The current state can be inspected at any time.
     */
    @Test
    void automaticSetup() {
        boolean installed = Bun.installed();
        String version = Bun.version();
        java.time.Duration interval = Bun.updateInterval();
    }

    /**
     * Run build, lint and format. Every command returns a Result which reports the exit code and both output streams. When bun has no built-in linter, lint automatically falls back to oxlint through bunx.
     */
    @Test
    void buildLintFormat() {
        Result built = Bun.build(o -> o.entry("src/index.ts")
                .outdir("dist")
                .target(Target.BROWSER)
                .format(ModuleFormat.ESM)
                .minify(true));

        Bun.lint(o -> o.path("src").fix(true).maxWarnings(0));
        Bun.format(o -> o.path("src").check(true));

        if (!built.succeeded()) {
            System.err.println(built.output());
        }
    }

    /**
     * Bundle a library into JavaScript and CSS with type declarations and a package.json exposing the exports. React, Svelte and Vue are detected automatically from package.json and source files; they can also be selected explicitly with Framework.
     */
    @Test
    void bundleLibrary() {
        Bun.bundle(o -> o.entry("src/index.tsx")
                .outdir("dist")
                .format(ModuleFormat.ESM)
                .dts(true)
                .minify(true)
                .generatePackageJson(true)
                .packageName("my-library")
                .echo());
    }
}
