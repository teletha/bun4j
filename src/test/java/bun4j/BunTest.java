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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import bun4j.internal.BunInstaller;
import bun4j.internal.CommandRunner;

class BunTest {

    @RegisterExtension
    final CleanRoom room = new CleanRoom();

    @Test
    void buildInvokesBunAndStreamsOutput() {
        Path bun = TestBun.create(room.root);
        List<String> out = new ArrayList();
        List<String> err = new ArrayList();

        Result result = Bun.build(new BuildOption()
                .entry("src/index.ts")
                .outdir("dist")
                .executable(bun)
                .directory(room.root)
                .stdout(out::add)
                .stderr(err::add));

        assertTrue(result.succeeded(), result.output());
        assertTrue(result.stdout().contains("OUT: build src/index.ts --outdir dist"), result.output());
        assertTrue(out.contains("OUT: build src/index.ts --outdir dist"), out.toString());
        assertTrue(err.stream().anyMatch(line -> line.trim().equals("ERR: build src/index.ts --outdir dist")), err.toString());
        assertEquals(1, result.lines().size());
    }

    @Test
    void nonzeroExitIsReportedByResult() {
        Path bun = TestBun.create(room.root);

        Result result = Bun.lint(new LintOption()
                .path("src")
                .executable(bun)
                .env("FAKE_BUN_EXIT", "3"));

        assertFalse(result.succeeded());
        assertEquals(3, result.exitCode());
        assertThrows(BunException.class, result::requireSuccess);
    }

    @Test
    void versionIsDetectedFromExecutable() {
        Path bun = TestBun.create(room.root);
        assertEquals("1.2.3", BunInstaller.version(bun));
    }

    @Test
    void bundleGeneratesScriptAndPackageMetadata() throws IOException {
        Path bun = TestBun.create(room.root);

        Result result = Bun.bundle(new BundleOption()
                .entry("src/index.ts")
                .outdir("dist")
                .packageName("example-library")
                .generatePackageJson(true)
                .installPlugins(false)
                .keepScript(true)
                .dts(true)
                .executable(bun)
                .directory(room.root));

        assertTrue(result.succeeded(), result.output());

        Path script;
        try (var files = Files.list(room.root)) {
            script = files.filter(path -> path.getFileName().toString().startsWith(".bun4j-build-"))
                    .findFirst()
                    .orElseThrow();
        }
        String source = Files.readString(script, StandardCharsets.UTF_8);
        assertTrue(source.contains("await Bun.build({"), source);
        assertTrue(source.contains("entrypoints: [\"src/index.ts\"]"), source);

        assertTrue(Files.exists(room.root.resolve("dist/package.json")), result.output() + " files=" + files(room.root));
        String json = Files.readString(room.root.resolve("dist/package.json"), StandardCharsets.UTF_8);
        assertTrue(json.contains("\"name\": \"example-library\""), json);
        assertTrue(json.contains("\"import\": \"./index.js\""), json);
    }

    private static List<String> files(Path root) {
        try (var stream = Files.walk(root)) {
            return stream.map(root::relativize).map(Path::toString).toList();
        } catch (IOException e) {
            return List.of(e.toString());
        }
    }

    @Test
    void realBunCanBuildTypeScript() throws IOException {
        Installation installation = BunInstaller.resolve();
        org.junit.jupiter.api.Assumptions.assumeTrue(installation != null, "A real bun runtime is required.");
        Files.writeString(room.root.resolve("index.ts"), "export const sum: number = 1 + 2;\n");

        Result result = Bun.build(new BuildOption()
                .executable(installation.executable())
                .directory(room.root)
                .entry("index.ts")
                .outdir("out")
                .target(Target.BROWSER));

        assertTrue(result.succeeded(), result.output());
        assertTrue(Files.exists(room.root.resolve("out/index.js")), result.output());
    }

    @Test
    void bundleDetectsFrameworksFromProject() throws IOException {
        Files.writeString(room.root.resolve("package.json"),
                "{\"dependencies\":{\"react\":\"19\",\"vue\":\"3\"},\"devDependencies\":{\"svelte\":\"5\"}}");
        Files.createDirectories(room.root.resolve("src"));
        Files.writeString(room.root.resolve("src/App.tsx"), "export default function App() { return null; }\n");

        BundleOption option = new BundleOption().projectDirectory(room.root);
        assertTrue(option.frameworks().contains(Framework.REACT));
        assertTrue(option.frameworks().contains(Framework.SVELTE));
        assertTrue(option.frameworks().contains(Framework.VUE));

        String script = option.script();
        assertTrue(script.contains("SveltePlugin"), script);
        assertTrue(script.contains("bun4jVueWindows"), script);
    }

    @Test
    void bundleCombinesReactSvelteAndVue() throws IOException {
        Installation bun = BunInstaller.resolve();
        org.junit.jupiter.api.Assumptions.assumeTrue(bun != null, "A real bun runtime is required.");
        Files.createDirectories(room.root.resolve("src"));
        Files.writeString(room.root.resolve("package.json"), "{\"name\":\"fixture\",\"private\":true,\"type\":\"module\"}\n");
        Files.writeString(room.root.resolve("src/index.ts"), """
                export { default as react } from './react';
                export { default as svelte } from './Component.svelte';
                export { default as vue } from './Component.vue';
                """);
        Files.writeString(room.root.resolve("src/react.tsx"), "export default function Hello() { return <div>hello</div>; }\n");
        Files.writeString(room.root.resolve("src/Component.svelte"), "<script lang=\"ts\">let name = 'svelte';</script>\n<p>{name}</p>\n<style>p{color:red}</style>\n");
        Files.writeString(room.root.resolve("src/Component.vue"), "<template><p>{{ msg }}</p></template>\n<script setup lang=\"ts\">const msg = 'vue';</script>\n<style scoped>p{color:blue}</style>\n");

        Result dependencies = CommandRunner.run(bun.executable(), List.of("add", "react", "react-dom", "svelte", "vue"),
                new BuildOption().directory(room.root));
        assertTrue(dependencies.succeeded(), dependencies.output());

        Result result = Bun.bundle(new BundleOption()
                .executable(bun.executable())
                .directory(room.root)
                .entry("src/index.ts")
                .outdir("dist"));

        assertTrue(result.succeeded(), result.output());
        assertTrue(Files.exists(room.root.resolve("dist/index.js")), result.output());
        assertTrue(Files.exists(room.root.resolve("dist/index.css")), result.output());
    }

    @Test
    void updateCheckIsLimitedToWeeklyInterval() throws IOException {
        Path originalDirectory = Bun.stateDirectory;
        Duration originalInterval = Bun.updateInterval();
        try {
            Bun.stateDirectory = room.root;
            assertTrue(Bun.needsUpdateCheck());
            assertNull(Bun.lastUpdateCheck());

            Bun.recordUpdateCheck();
            assertFalse(Bun.needsUpdateCheck());
            assertNotNull(Bun.lastUpdateCheck());

            Files.writeString(room.root.resolve("last-update.txt"),
                    String.valueOf(System.currentTimeMillis() - Duration.ofDays(8).toMillis()));
            assertTrue(Bun.needsUpdateCheck());

            Bun.updateInterval(Duration.ofDays(30));
            assertFalse(Bun.needsUpdateCheck());
        } finally {
            Bun.stateDirectory = originalDirectory;
            Bun.updateInterval(originalInterval);
        }
    }

    @Test
    void scopedPackageSpecifiersAreParsed() {
        assertEquals("@eckidevs/bun-plugin-vue", Bun.packageName("@eckidevs/bun-plugin-vue@0.3.0"));
        assertEquals("@eckidevs/bun-plugin-vue", Bun.packageName("@eckidevs/bun-plugin-vue"));
        assertEquals("bun-plugin-svelte", Bun.packageName("bun-plugin-svelte@0.0.6"));
    }

    @Test
    void lintFallsBackToOxlintWhenNativeLintIsMissing() {
        Path bun = TestBun.create(room.root);

        Result fallback = Bun.lint(new LintOption().path("src").fix(true).executable(bun));

        assertTrue(fallback.stdout().contains("OUT: x oxlint src --fix"), fallback.output());
    }

    @Test
    void lintCanForceNativeCommand() {
        Path bun = TestBun.create(room.root);

        Result nativeLint = Bun.lint(new LintOption().path("src").fix(true).fallback(false).executable(bun));

        assertTrue(nativeLint.stdout().contains("OUT: lint src --no-script --fix"), nativeLint.output());
    }

    @Test
    void formatUsesFmtSubcommand() {
        Path bun = TestBun.create(room.root);
        Result result = Bun.format(new FormatOption().check(true).path("src").executable(bun));
        assertTrue(result.stdout().contains("OUT: fmt src --no-script --check"), result.output());
    }
}
