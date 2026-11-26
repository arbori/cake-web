package cake.web.configuration;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Central registry for Cake Web configuration.
 *
 * <p>The configuration is written once at startup (from
 * {@link cake.web.CakeWebApplication#run}) and read from anywhere in the
 * framework afterward. It is intentionally a static registry because the
 * framework operates without dependency injection by design.</p>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>Application startup: {@link #initialize(CakeWebConfig)}</li>
 *   <li>Framework runtime: {@link #basePackage()} etc.</li>
 *   <li>Tests: {@link #reset()} to restore defaults</li>
 * </ol>
 *
 * <h2>Thread Safety</h2>
 * <p>The registry is written once at startup and read concurrently. Writes
 * are synchronized; reads are lock-free once initialized.</p>
 */
public final class CakeWebRegistry {

    private static final Logger LOGGER = Logger.getLogger(CakeWebRegistry.class.getName());

    private static volatile CakeWebConfig config;

    private CakeWebRegistry() {
        // static class
    }

    /**
     * Writes the configuration into the registry. Called once by
     * {@link cake.web.CakeWebApplication} before the server starts.
     *
     * @param cfg the configuration to register (must not be null)
     * @throws IllegalStateException if the configuration was already initialized
     */
    public static synchronized void initialize(CakeWebConfig cfg) {
        Objects.requireNonNull(cfg, "CakeWebConfig cannot be null");

        if (config != null) {
            throw new IllegalStateException("CakeWebConfig already initialized");
        }

        config = cfg;
        LOGGER.info(() -> "CakeWeb configuration initialized: " + describe(cfg));
    }

    /**
     * Resets the registry to its default configuration. Intended for tests.
     */
    public static synchronized void reset() {
        config = null;
    }

    /**
     * Returns the base package prefix, or an empty string if not set.
     * Used by the resource resolver to restrict class loading.
     *
     * @return the configured base package or an empty string
     */
    public static String basePackage() {
        CakeWebConfig cfg = config;
        return cfg != null && cfg.getBasePackage() != null ? cfg.getBasePackage() : "";
    }

    /**
     * Returns the configured context path.
     *
     * @return the context path or an empty string
     */
    public static String contextPath() {
        CakeWebConfig cfg = config;
        return cfg != null && cfg.getContextPath() != null ? cfg.getContextPath() : "";
    }

    /**
     * Returns the configured server port.
     *
     * @return the port number (defaults to 8080 if not set)
     */
    public static int port() {
        CakeWebConfig cfg = config;
        return cfg != null ? cfg.getPort() : 8080;
    }

    /**
     * Returns the configured base directory.
     *
     * @return the base directory or the system temporary directory
     */
    public static String baseDir() {
        CakeWebConfig cfg = config;
        return cfg != null && cfg.getBaseDir() != null
                ? cfg.getBaseDir()
                : System.getProperty("java.io.tmpdir");
    }

    /**
     * Returns true if the given FQCN is allowed to be loaded.
     * If no base package is configured, everything is allowed.
     *
     * @param fqcn the fully-qualified class name to validate
     * @return {@code true} if allowed, {@code false} otherwise
     */
    public static boolean isClassLoadAllowed(String fqcn) {
        String base = basePackage();
        return base.isEmpty() || fqcn.equals(base) || fqcn.startsWith(base + ".");
    }

    private static String describe(CakeWebConfig cfg) {
        return "port=" + cfg.getPort()
                + ", contextPath='" + cfg.getContextPath() + "'"
                + ", baseDir='" + cfg.getBaseDir() + "'"
                + ", basePackage='" + cfg.getBasePackage() + "'";
    }
}