/*
 * Copyright (C) 2026 The BUN4J Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bun4j.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import bun4j.BunException;
import bun4j.Installation;
import bun4j.Option;
import bun4j.Result;

/**
 * Downloads, installs and updates the bun runtime.
 */
public final class BunInstaller {

    private static final String LATEST_RELEASE = "https://api.github.com/repos/oven-sh/bun/releases/latest";
    private static final String RELEASE_DOWNLOAD = "https://github.com/oven-sh/bun/releases/download/";
    private static final String VERSION_FILE = "VERSION.txt";
    private static final Pattern TAG_NAME = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");

    private BunInstaller() {
    }

    /**
     * Returns the default installation directory.
     *
     * @return {@code ~/.bun/bin}.
     */
    public static Path defaultDirectory() {
        return Path.of(System.getProperty("user.home"), ".bun", "bin");
    }

    /**
     * Returns the name of the bun executable on the current platform.
     *
     * @return {@code bun.exe} on Windows, otherwise {@code bun}.
     */
    public static String executableName() {
        return isWindows() ? "bun.exe" : "bun";
    }

    /**
     * Finds an installed bun. A managed installation in {@code ~/.bun/bin} takes precedence over
     * the PATH.
     *
     * @return The installation, or null if bun is not available.
     */
    public static Installation resolve() {
        Path local = defaultDirectory().resolve(executableName());
        if (Files.isRegularFile(local)) {
            return new Installation(local, local.getParent(), readVersion(local.getParent(), local));
        }

        String path = System.getenv("PATH");
        if (path != null) {
            for (String element : path.split(Pattern.quote(File.pathSeparator))) {
                if (element.isBlank()) {
                    continue;
                }
                Path candidate = Path.of(element).resolve(executableName());
                if (Files.isRegularFile(candidate)) {
                    return new Implementation(candidate).installation();
                }
            }
        }
        return null;
    }

    /**
     * Finds a bun inside the specified directory.
     *
     * @param directory A directory.
     * @return The installation, or null if no executable exists.
     */
    public static Installation resolve(Path directory) {
        if (directory == null) {
            return resolve();
        }
        Path executable = directory.resolve(executableName());
        if (!Files.isRegularFile(executable)) {
            return null;
        }
        return new Installation(executable, directory.toAbsolutePath().normalize(), readVersion(directory, executable));
    }

    /**
     * Installs bun into the local directory.
     *
     * @param option The installation options.
     * @return The installed runtime.
     */
    public static Installation install(InstallOption option) {
        Path directory = option.directory().toAbsolutePath().normalize();
        Path executable = directory.resolve(executableName());
        String tag = normalizeTag(option.version());
        String version = stripPrefix(tag);

        if (!option.force() && Files.isRegularFile(executable) && version.equals(readVersion(directory, executable))) {
            return new Installation(executable, directory, version);
        }

        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new BunException("Cannot create the installation directory " + directory + ".", e);
        }

        downloadAndExtract(tag, executable, option);
        String installed = version(executable);
        if (installed.isEmpty()) {
            installed = version;
        }
        writeVersion(directory, installed);
        return new Installation(executable, directory, installed);
    }

    /**
     * Updates the local bun. The official {@code bun upgrade} command is tried first, then a fresh
     * download of the requested release.
     *
     * @param option The update options.
     * @return The updated runtime.
     */
    public static Installation update(UpdateOption option) {
        Path directory = option.directory().toAbsolutePath().normalize();
        Path executable = option.executable() == null ? directory.resolve(executableName()) : option.executable();
        boolean pinned = !"latest".equalsIgnoreCase(option.version());
        String before = Files.isRegularFile(executable) ? version(executable) : "";

        if (!pinned && option.upgradeCommand() && !before.isEmpty()) {
            Result result = CommandRunner.run(executable, List.of("upgrade"), option);
            String after = version(executable);
            if (result.succeeded() && !after.isEmpty() && (option.force() || !after.equals(before))) {
                writeVersion(directory, after);
                return new Installation(executable, directory, after);
            }
        }

        InstallOption install = new InstallOption().to(directory).version(option.version()).force(option.force() || pinned);
        install.stdout(option.stdout()).stderr(option.stderr()).timeout(option.timeout());
        option.environment().forEach(install::env);
        return install(install);
    }

    /**
     * Returns the version of an executable by running {@code bun --version}.
     *
     * @param executable A bun executable.
     * @return A version string, or an empty string if it cannot be detected.
     */
    public static String version(Path executable) {
        if (executable == null || !Files.isRegularFile(executable)) {
            return "";
        }
        try {
            Result result = CommandRunner.run(executable, List.of("--version"), new VersionOption());
            if (!result.succeeded()) {
                return "";
            }
            return result.stdout().strip().replaceFirst("^bun\\s*", "");
        } catch (BunException e) {
            return "";
        }
    }

    /**
     * Normalizes a user supplied version into a release tag.
     */
    private static String normalizeTag(String version) {
        if (version == null || version.isBlank() || "latest".equalsIgnoreCase(version)) {
            return latestTag();
        }
        return version.startsWith("bun-") ? version : "bun-v" + version;
    }

    /**
     * Queries the latest release tag from GitHub.
     */
    private static String latestTag() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(LATEST_RELEASE))
                .header("User-Agent", "bun4j")
                .header("Accept", "application/vnd.github+json")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        try {
            HttpResponse<String> response = http().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() / 100 != 2) {
                throw new BunException("Cannot resolve the latest bun release (HTTP " + response.statusCode() + ").");
            }
            Matcher matcher = TAG_NAME.matcher(response.body());
            if (!matcher.find()) {
                throw new BunException("The GitHub release response does not contain a tag name.");
            }
            return matcher.group(1);
        } catch (IOException e) {
            throw new BunException("Cannot resolve the latest bun release.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BunException("Interrupted while resolving the latest bun release.", e);
        }
    }

    /**
     * Downloads a release archive and extracts the bun executable.
     */
    private static void downloadAndExtract(String tag, Path executable, InstallOption option) {
        String archiveName = "bun-" + platform() + "-" + architecture() + ".zip";
        String location = RELEASE_DOWNLOAD + tag + "/" + archiveName;

        HttpRequest request = HttpRequest.newBuilder(URI.create(location))
                .header("User-Agent", "bun4j")
                .timeout(Duration.ofMinutes(5))
                .GET()
                .build();

        Path archive;
        try {
            archive = Files.createTempFile("bun4j-", ".zip");
            HttpResponse<Path> response = http().send(request, HttpResponse.BodyHandlers.ofFile(archive));
            if (response.statusCode() / 100 != 2) {
                throw new BunException("Cannot download " + location + " (HTTP " + response.statusCode() + ").");
            }
        } catch (IOException e) {
            throw new BunException("Cannot download " + location + ".", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BunException("Interrupted while downloading " + location + ".", e);
        }

        Path temporary = null;
        try {
            temporary = Files.createTempFile(executable.getParent(), "bun-download-", ".tmp");
            try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive), StandardCharsets.UTF_8)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (entry.isDirectory() || !entry.getName().endsWith(executableName())) {
                        continue;
                    }
                    Files.copy(zip, temporary, StandardCopyOption.REPLACE_EXISTING);
                    break;
                }
            }
            if (Files.size(temporary) == 0) {
                throw new BunException("The archive " + archiveName + " does not contain " + executableName() + ".");
            }
            makeExecutable(temporary);
            Files.move(temporary, executable, StandardCopyOption.REPLACE_EXISTING);
            temporary = null;
        } catch (IOException e) {
            throw new BunException("Cannot extract " + archiveName + ".", e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                }
            }
            try {
                Files.deleteIfExists(archive);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Makes a file executable on platforms which support POSIX permissions.
     */
    private static void makeExecutable(Path path) {
        if (!path.toFile().setExecutable(true, false)) {
            return;
        }
        try {
            Set<PosixFilePermission> permissions = new HashSet(Files.getPosixFilePermissions(path));
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(path, permissions);
        } catch (IOException | UnsupportedOperationException ignored) {
            // Windows does not support POSIX permissions, but setExecutable takes care of it.
        }
    }

    private static String readVersion(Path directory, Path executable) {
        Path marker = directory.resolve(VERSION_FILE);
        try {
            if (Files.isRegularFile(marker)) {
                String value = Files.readString(marker, StandardCharsets.UTF_8).strip();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        } catch (IOException ignored) {
        }
        return version(executable);
    }

    private static void writeVersion(Path directory, String version) {
        try {
            Files.writeString(directory.resolve(VERSION_FILE), version, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BunException("Cannot record the bun version in " + directory + ".", e);
        }
    }

    private static String stripPrefix(String tag) {
        return tag.replaceFirst("^bun-", "").replaceFirst("^v", "");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private static String platform() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return "windows";
        } else if (os.contains("mac") || os.contains("darwin")) {
            return "darwin";
        } else if (os.contains("linux")) {
            return "linux";
        } else {
            throw new BunException("Unsupported operating system: " + os + ".");
        }
    }

    private static String architecture() {
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        if (arch.equals("amd64") || arch.equals("x86_64")) {
            return "x64";
        } else if (arch.equals("aarch64") || arch.equals("arm64")) {
            return "arm64";
        } else {
            throw new BunException("Unsupported architecture: " + arch + ".");
        }
    }

    private static HttpClient http() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * The option used when probing the version of an executable.
     */
    private static final class VersionOption extends Option<VersionOption> {

        @Override
        public String command() {
            return "--version";
        }
    }

    /**
     * Reads the version of a PATH installation lazily.
     */
    private static final class Implementation {

        private final Path executable;

        private Implementation(Path executable) {
            this.executable = executable;
        }

        private Installation installation() {
            return new Installation(executable, executable.getParent(), version(executable));
        }
    }
}
