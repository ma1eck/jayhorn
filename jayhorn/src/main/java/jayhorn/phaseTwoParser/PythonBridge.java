package jayhorn.phaseTwoParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PythonBridge {

    private static final String PYTHON_EXECUTABLE = detectPythonExecutable();
    private static final int TIMEOUT_SECONDS = 30;

    // Base directory where all python scripts live
    private static final Path SCRIPTS_DIR = resolveScriptsDir();

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Runs a Python script by name (with or without .py extension) and returns
     * its stdout lines, filtered of WARNING lines.
     *
     * @param scriptName  e.g. "addGBitVector" or "addGBitVector.py"
     * @param args        arguments passed directly to the script
     * @return            non-warning stdout lines
     */
    public static List<String> run(String scriptName, String... args)
            {

        Path script = resolveScript(scriptName);
        validateScriptExists(script);
        validateArgs(args);

        List<String> command = new ArrayList<>();
        command.add(PYTHON_EXECUTABLE);
        command.add(script.toString());
        Collections.addAll(command, args);

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false); // keep stderr separate for better errors
            Process process = pb.start();
            process.destroyForcibly();

            return readOutput(process, scriptName);
        } catch (Exception e) {
            System.out.println(e);
            return null; // todo clean
        }
    }

    /**
     * Convenience: runs a script and returns the first output line split by comma.
     * Matches the existing "value,mask" contract used in PythonBitVectorBridge.
     */
    public static String[] runForPair(String scriptName, String... args)
            throws IOException, InterruptedException {

        List<String> lines = run(scriptName, args);
        if (lines.isEmpty()) {
            throw new RuntimeException("Script '" + scriptName + "' produced no output");
        }

        String result = lines.get(0).trim();
        String[] parts = result.split(",", -1);
        if (parts.length != 2) {
            throw new RuntimeException(
                    "Expected 'value,mask' from '" + scriptName + "', got: " + result);
        }
        return parts;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static List<String> readOutput(Process process, String scriptName)
            throws IOException, InterruptedException {

        // Read stdout and stderr concurrently to avoid blocking
        List<String> stdoutLines = new ArrayList<>();
        StringBuilder stderr = new StringBuilder();

        Thread stderrReader = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    stderr.append(line).append("\n");
                }
            } catch (IOException ignored) {}
        });
        stderrReader.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stdoutLines.add(line);
            }
        }

        stderrReader.join(5_000);

        boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Script '" + scriptName + "' timed out after " + TIMEOUT_SECONDS + "s");
        }

        int exitCode = process.exitValue();

        List<String> filtered = stdoutLines.stream()
                .filter(l -> !l.startsWith("WARNING"))
                .collect(Collectors.toList());

        if (exitCode != 0) {
            throw new RuntimeException(
                    "Script '" + scriptName + "' failed (exit " + exitCode + "):\n" + stderr);
        }

        if (!filtered.isEmpty() && filtered.get(0).startsWith("Error")) {
            throw new RuntimeException("Script '" + scriptName + "' error: " + filtered.get(0));
        }

        return filtered;
    }

    private static Path resolveScript(String scriptName) {
        String name = scriptName.endsWith(".py") ? scriptName : scriptName + ".py";
        return SCRIPTS_DIR.resolve(name);
    }

    private static void validateScriptExists(Path script) {
        if (!Files.exists(script)) {
            throw new IllegalArgumentException("Script not found: " + script.toAbsolutePath());
        }
    }

    private static void validateArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                throw new IllegalArgumentException("Argument at index " + i + " is null");
            }
        }
    }

    private static Path resolveScriptsDir() {
        String custom = System.getProperty("jayhorn.scripts.dir");
        if (custom != null && !custom.isEmpty()) {
            return Paths.get(custom);
        }

        Path relative = Paths.get(
                "jayhorn/src/main/java/jayhorn/pythonAPIs");
        if (Files.exists(relative)) {
            return relative;
        }

        try {
            URL resource = PythonBridge.class.getClassLoader()
                    .getResource("jayhorn/pythonAPIs");
            if (resource != null) {
                return Paths.get(resource.toURI());
            }
        } catch (URISyntaxException e) {
            // fall through
        }

        return relative; // will fail later with a clear message
    }

    private static String detectPythonExecutable() {
        String custom = System.getProperty("jayhorn.python.path");
        if (custom != null && !custom.isEmpty()) return custom;

        for (String candidate : new String[]{"python3", "python", "py"}) {
            if (isPythonAvailable(candidate)) return candidate;
        }
        return "python3";
    }

    private static boolean isPythonAvailable(String executable) {
        try {
            Process p = new ProcessBuilder(executable, "--version")
                    .redirectErrorStream(true)
                    .start();
            boolean done = p.waitFor(5, TimeUnit.SECONDS);
            if (!done) { p.destroyForcibly(); return false; }
            return p.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}
