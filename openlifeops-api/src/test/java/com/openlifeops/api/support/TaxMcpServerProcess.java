package com.openlifeops.api.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class TaxMcpServerProcess implements AutoCloseable {

    private static final int DEFAULT_PORT = 18090;

    private final Process process;
    private final String baseUrl;

    private TaxMcpServerProcess(Process process, String baseUrl) {
        this.process = process;
        this.baseUrl = baseUrl;
    }

    public static TaxMcpServerProcess start() throws IOException, InterruptedException {
        Path jar = resolveExecJar();
        if (!Files.exists(jar)) {
            throw new IllegalStateException("Tax MCP server jar not found. Build with: mvnw -pl openlifeops-tax-mcp-server package");
        }

        Process process = new ProcessBuilder(
                        "java",
                        "-jar",
                        jar.toString(),
                        "--server.port=" + DEFAULT_PORT)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start();

        String baseUrl = "http://localhost:" + DEFAULT_PORT;
        awaitReady(baseUrl);
        return new TaxMcpServerProcess(process, baseUrl);
    }

    public String baseUrl() {
        return baseUrl;
    }

    @Override
    public void close() {
        process.destroy();
        try {
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }

    private static Path resolveExecJar() throws IOException {
        Path workingDirectory = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        Path[] candidateDirectories = {
                workingDirectory.resolve("../openlifeops-tax-mcp-server/target").normalize(),
                workingDirectory.resolve("openlifeops-tax-mcp-server/target").normalize()
        };
        for (Path directory : candidateDirectories) {
            if (!Files.isDirectory(directory)) {
                continue;
            }
            try (Stream<Path> files = Files.list(directory)) {
                Path jar = files
                        .filter(path -> path.getFileName().toString()
                                .matches("openlifeops-tax-mcp-server-.+-exec\\.jar"))
                        .min(Comparator.comparing(Path::toString))
                        .orElse(null);
                if (jar != null) {
                    return jar;
                }
            }
        }
        return candidateDirectories[0].resolve("openlifeops-tax-mcp-server-<version>-exec.jar");
    }

    private static void awaitReady(String baseUrl) throws InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/mcp"))
                .timeout(Duration.ofSeconds(2))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .header("Content-Type", "application/json")
                .build();

        long deadline = System.nanoTime() + Duration.ofSeconds(30).toNanos();
        while (System.nanoTime() < deadline) {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 500) {
                    return;
                }
            } catch (Exception ignored) {
                Thread.sleep(250);
            }
        }
        throw new IllegalStateException("Tax MCP server did not become ready at " + baseUrl);
    }
}
