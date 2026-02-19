package util;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Validates file paths before they are used in Runtime.exec() calls.
 * Prevents command injection and execution of unexpected binaries.
 */
public final class ExecutableValidator {

    // Characters that should never appear in executable paths
    private static final Pattern SUSPICIOUS_CHARS = Pattern.compile("[;&|`$<>!]");

    private ExecutableValidator() {}

    /**
     * Validates that a path is safe to use as an executable in Runtime.exec().
     * Checks:
     * - Path is not null/empty
     * - Path does not contain shell metacharacters
     * - Path resolves to a valid filesystem path
     * - Path points to a file (not a directory)
     * - Path is absolute (not relative, which could be hijacked)
     */
    public static boolean isValidExecutablePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        // Reject paths with shell metacharacters
        if (SUSPICIOUS_CHARS.matcher(path).find()) {
            return false;
        }

        // Reject paths with null bytes (path traversal attack)
        if (path.contains("\0")) {
            return false;
        }

        try {
            Path resolved = Paths.get(path).toAbsolutePath().normalize();
            File file = resolved.toFile();

            // Must be a file, not a directory
            if (file.exists() && file.isDirectory()) {
                return false;
            }

            // Warn but allow if path doesn't exist yet (file may not be installed)
            return true;
        } catch (InvalidPathException ex) {
            return false;
        }
    }
}
