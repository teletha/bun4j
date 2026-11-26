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

import static antibug.Tester.same;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class OptionArgumentsTest {

    @Test
    void buildArguments() {
        BuildOption option = new BuildOption()
                .entry("src/index.ts", "src/admin.ts")
                .outdir("dist")
                .target(Target.BROWSER)
                .format(ModuleFormat.ESM)
                .sourcemap(SourceMap.LINKED)
                .minify(true)
                .splitting(true)
                .check(true)
                .dts(true)
                .external("react")
                .define("process.env.NODE_ENV", "\"production\"")
                .loader(".png", "file")
                .drop("console")
                .raw("--custom");

        assertTrue(same(List.of(
                "build",
                "src/index.ts", "src/admin.ts",
                "--outdir", "dist",
                "--target", "browser",
                "--format", "esm",
                "--sourcemap", "linked",
                "--minify",
                "--splitting",
                "--check",
                "--dts",
                "--external", "react",
                "--define", "process.env.NODE_ENV=\"production\"",
                "--loader", ".png:file",
                "--drop", "console",
                "--custom"
        ), option.arguments()));
        assertEquals("build", option.command());
    }

    @Test
    void lintArguments() {
        LintOption option = new LintOption()
                .path("src")
                .fix(true)
                .typeAware(true)
                .maxWarnings(0)
                .format("json")
                .rule("eqeqeq: error")
                .ignorePattern("**/*.gen.ts")
                .changed("origin/main");

        assertTrue(same(List.of(
                "lint",
                "src",
                "--no-script",
                "--fix",
                "--type-aware",
                "--changed=origin/main",
                "--rule", "eqeqeq: error",
                "--ignore-pattern", "**/*.gen.ts",
                "--max-warnings", "0",
                "--format", "json"
        ), option.arguments()));
    }

    @Test
    void oxlintFallbackArguments() {
        LintOption option = new LintOption()
                .path("src")
                .fix(true)
                .maxWarnings(0)
                .format("json")
                .rule("prefer-const: warn")
                .rule("eqeqeq: off")
                .ignorePattern("**/*.gen.ts");

        assertTrue(same(List.of(
                "x", "oxlint",
                "src",
                "--fix",
                "--ignore-pattern", "**/*.gen.ts",
                "--max-warnings", "0",
                "--format", "json",
                "--warn", "prefer-const",
                "--allow", "eqeqeq"
        ), option.fallbackArguments()));
    }

    @Test
    void dependencyArguments() {
        assertTrue(same(List.of("install", "--frozen-lockfile"),
                new DependencyOption().operation(DependencyOperation.INSTALL).frozenLockfile(true).arguments()));
        assertTrue(same(List.of("add", "--dev", "--exact", "react@19", "svelte"),
                new DependencyOption().operation(DependencyOperation.ADD).dev(true).exact(true)
                        .packageNames("react@19", "svelte").arguments()));
        assertTrue(same(List.of("remove", "react"),
                new DependencyOption().operation(DependencyOperation.REMOVE).packageNames("react").arguments()));
        assertTrue(same(List.of("update", "react"),
                new DependencyOption().operation(DependencyOperation.UPDATE).packageNames("react").arguments()));
    }

    @Test
    void dependencyGlobalLocations() {
        DependencyOption option = new DependencyOption()
                .operation(DependencyOperation.INSTALL)
                .linker(Linker.ISOLATED)
                .globalStore(true)
                .cacheDir("D:/bun-cache")
                .globalDir("D:/bun/global")
                .globalBinDir("D:/bun/bin")
                .config("bunfig.toml");

        assertTrue(same(List.of(
                "install",
                "--config", "bunfig.toml",
                "--cache-dir", "D:/bun-cache",
                "--linker=isolated"
        ), option.arguments()));
        assertEquals("1", option.environment().get("BUN_INSTALL_GLOBAL_STORE"));
        assertEquals("D:/bun/global", option.environment().get("BUN_INSTALL_GLOBAL_DIR"));
        assertEquals("D:/bun/bin", option.environment().get("BUN_INSTALL_BIN"));
    }

    @Test
    void checkArguments() {
        CheckOption option = new CheckOption()
                .path("src/index.ts")
                .project("tsconfig.json")
                .build(true)
                .noPretty(true)
                .compilerOption("strict", null)
                .compilerOption("target", "es2022");

        assertTrue(same(List.of(
                "check", "src/index.ts",
                "--no-script",
                "--project", "tsconfig.json",
                "--build",
                "--no-pretty",
                "--strict",
                "--target", "es2022"
        ), option.arguments()));
    }

    @Test
    void testArguments() {
        TestOption option = new TestOption()
                .path("test/**/*.test.ts")
                .timeout(1000)
                .coverage(true)
                .coverageReporter("text")
                .bail(2)
                .namePattern("add")
                .reporter("junit")
                .reporterOutfile("out.xml");

        assertTrue(same(List.of(
                "test", "test/**/*.test.ts",
                "--timeout", "1000",
                "--coverage",
                "--coverage-reporter", "text",
                "--bail", "2",
                "--test-name-pattern", "add",
                "--reporter", "junit",
                "--reporter-outfile", "out.xml"
        ), option.arguments()));
    }

    @Test
    void formatArguments() {
        FormatOption option = new FormatOption()
                .path("src", "test/**/*.ts")
                .check(true)
                .printWidth(100)
                .semi(false)
                .singleQuote(true)
                .sortImports(true);

        assertTrue(same(List.of(
                "fmt",
                "src", "test/**/*.ts",
                "--no-script",
                "--check",
                "--print-width", "100",
                "--no-semi",
                "--single-quote",
                "--sort-imports"
        ), option.arguments()));
        assertFalse(new FormatOption().arguments().contains("--check"));
    }

    @Test
    void bundleScriptContainsFrameworkPlugins() {
        BundleOption option = new BundleOption()
                .entry("src/index.tsx")
                .outdir("dist")
                .framework(Framework.SVELTE, Framework.VUE)
                .plugin("my-plugin")
                .dts(true)
                .external("react")
                .define("process.env.NODE_ENV", "\"production\"");

        String script = option.script();
        assertTrue(script.contains("import { SveltePlugin } from \"bun-plugin-svelte\";"));
        assertTrue(script.contains("import vuePlugin from \"@eckidevs/bun-plugin-vue\";"));
        assertTrue(script.contains("import plugin0 from \"my-plugin\";"));
        assertTrue(script.contains("SveltePlugin({ development: false })"));
        assertTrue(script.contains("const bun4jVue = vuePlugin();"));
        assertTrue(script.contains("bun4j-vue-path-fix"));
        assertTrue(script.contains("plugin0()"));
        assertTrue(script.contains("entrypoints: [\"src/index.tsx\"]"));
        assertTrue(script.contains("dts: true"));
        assertTrue(script.contains("external: [\"react\"]"));
        assertTrue(script.contains("\"process.env.NODE_ENV\": \"production\""));
        assertTrue(script.contains("if (!result.success)"));
    }

    @Test
    void bundleScriptEscapesStrings() {
        BundleOption option = new BundleOption().entry("src/a\"b.ts").outdir("out\\dist");
        String script = option.script();
        assertTrue(script.contains("src/a\\\"b.ts"));
        assertTrue(script.contains("out\\\\dist"));
    }

    @Test
    void bundlePackageJsonExposesTypes() {
        BundleOption option = new BundleOption()
                .entry("src/index.ts")
                .packageName("my-library")
                .dts(true);

        String json = option.packageJson();
        assertTrue(json.contains("\"name\": \"my-library\""));
        assertTrue(json.contains("\"types\": \"./index.d.ts\""));
        assertTrue(json.contains("\"import\": \"./index.js\""));
        assertEquals("index.js", option.mainFileName());
    }

    @Test
    void bundleRequiresFrameworkPackages() {
        assertTrue(same(List.of(), new BundleOption().framework(Framework.REACT).requiredPackages()));
        assertTrue(same(List.of("bun-plugin-svelte@0.0.6"), new BundleOption().framework(Framework.SVELTE).requiredPackages()));
        assertTrue(same(List.of("@eckidevs/bun-plugin-vue@0.3.0"), new BundleOption().framework(Framework.VUE).requiredPackages()));
    }
}
