package chat.octet.accordion.action.shell;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Shell executor configuration with secure absolute paths.
 * <p>
 * This class provides secure shell executor paths to prevent PATH environment variable
 * manipulation attacks. It uses absolute paths for shell executables and supports
 * custom configuration through system properties.
 * </p>
 *
 * <p>System Properties:</p>
 * <ul>
 *   <li>{@code accordion.shell.bash.path} - Custom bash executable path</li>
 *   <li>{@code accordion.shell.cmd.path} - Custom cmd executable path</li>
 *   <li>{@code accordion.shell.powershell.path} - Custom PowerShell executable path</li>
 * </ul>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Slf4j
public final class ShellExecutorConfig {

    private static final String BASH_PATH_PROPERTY = "accordion.shell.bash.path";
    private static final String CMD_PATH_PROPERTY = "accordion.shell.cmd.path";
    private static final String POWERSHELL_PATH_PROPERTY = "accordion.shell.powershell.path";

    // Common bash locations on Unix-like systems
    private static final List<String> BASH_PATHS = Arrays.asList(
            "/bin/bash",
            "/usr/bin/bash",
            "/usr/local/bin/bash"
    );

    // Common cmd locations on Windows
    private static final List<String> CMD_PATHS = Arrays.asList(
            "C:\\Windows\\System32\\cmd.exe",
            System.getenv("COMSPEC") // Fallback to COMSPEC environment variable
    );

    // Common PowerShell locations on Windows
    private static final List<String> POWERSHELL_PATHS = Arrays.asList(
            "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe",
            "C:\\Program Files\\PowerShell\\7\\pwsh.exe"
    );

    private ShellExecutorConfig() {
        // Utility class, prevent instantiation
    }

    /**
     * Gets the absolute path for bash executable.
     *
     * @return absolute path to bash
     * @throws IllegalStateException if bash executable is not found
     */
    public static String getBashPath() {
        // Check system property first
        String customPath = System.getProperty(BASH_PATH_PROPERTY);
        if (customPath != null && isExecutableFile(customPath)) {
            log.debug("Using custom bash path from system property: {}", customPath);
            return customPath;
        }

        // Find bash in common locations
        for (String path : BASH_PATHS) {
            if (isExecutableFile(path)) {
                log.debug("Found bash at: {}", path);
                return path;
            }
        }

        throw new IllegalStateException(
                "Bash executable not found. Please install bash or set system property: " + BASH_PATH_PROPERTY
        );
    }

    /**
     * Gets the absolute path for cmd executable.
     *
     * @return absolute path to cmd
     * @throws IllegalStateException if cmd executable is not found
     */
    public static String getCmdPath() {
        // Check system property first
        String customPath = System.getProperty(CMD_PATH_PROPERTY);
        if (customPath != null && isExecutableFile(customPath)) {
            log.debug("Using custom cmd path from system property: {}", customPath);
            return customPath;
        }

        // Find cmd in common locations
        for (String path : CMD_PATHS) {
            if (path != null && isExecutableFile(path)) {
                log.debug("Found cmd at: {}", path);
                return path;
            }
        }

        throw new IllegalStateException(
                "CMD executable not found. Please ensure Windows is properly installed or set system property: "
                        + CMD_PATH_PROPERTY
        );
    }

    /**
     * Gets the absolute path for PowerShell executable.
     *
     * @return absolute path to PowerShell
     * @throws IllegalStateException if PowerShell executable is not found
     */
    public static String getPowerShellPath() {
        // Check system property first
        String customPath = System.getProperty(POWERSHELL_PATH_PROPERTY);
        if (customPath != null && isExecutableFile(customPath)) {
            log.debug("Using custom PowerShell path from system property: {}", customPath);
            return customPath;
        }

        // Find PowerShell in common locations
        for (String path : POWERSHELL_PATHS) {
            if (isExecutableFile(path)) {
                log.debug("Found PowerShell at: {}", path);
                return path;
            }
        }

        throw new IllegalStateException(
                "PowerShell executable not found. Please install PowerShell or set system property: "
                        + POWERSHELL_PATH_PROPERTY
        );
    }

    /**
     * Gets the absolute path for shell executable based on type.
     *
     * @param type shell type
     * @return absolute path to shell executable
     * @throws IllegalStateException if shell executable is not found
     */
    public static String getShellPath(final ShellParameter.ShellType type) {
        return switch (type) {
            case BASH -> getBashPath();
            case CMD -> getCmdPath();
            case POWERSHELL -> getPowerShellPath();
        };
    }

    /**
     * Checks if the given path is an executable file.
     *
     * @param path file path to check
     * @return true if the file exists and is executable
     */
    private static boolean isExecutableFile(final String path) {
        if (path == null) {
            return false;
        }
        File file = new File(path);
        // On Windows, canExecute() may not work reliably, so we check existence for .exe files
        if (SystemUtils.IS_OS_WINDOWS && path.toLowerCase(java.util.Locale.ROOT).endsWith(".exe")) {
            return file.exists() && file.isFile();
        }
        return file.exists() && file.isFile() && file.canExecute();
    }
}