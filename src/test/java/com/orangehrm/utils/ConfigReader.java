package com.orangehrm.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigReader provides a single access point for all configuration values.
 *
 * Two property files are loaded at class-initialisation time:
 *
 *  1. application.properties      - common, environment-agnostic settings
 *     (browser type, headless flag, timeouts, ...)
 *
 *  2. <env>/<env>.properties      - environment-specific settings
 *     (application URL, database connection strings, ...)
 *     Located under src/test/resources/<env>/<env>.properties.
 *     The environment is determined by the JVM system property "env"
 *     (e.g. -Denv=sit).  Falls back to "default.env" in
 *     application.properties, which itself defaults to "dev".
 *
 * Usage:
 *   ConfigReader.getProperty("browser.name")       // from application.properties
 *   ConfigReader.getEnvProperty("env.baseurl")     // from <env>/<env>.properties
 *   ConfigReader.getActiveEnv()                    // returns current env name
 *   ConfigReader.getDataFilePath("logindata.json") // path to env data file
 */
public final class ConfigReader {

    private static final Properties APP_PROPERTIES = new Properties();
    private static final Properties ENV_PROPERTIES = new Properties();
    private static final String     ACTIVE_ENV;

    static {
        loadApplicationProperties();
        ACTIVE_ENV = System.getProperty("env",
                APP_PROPERTIES.getProperty("default.env", "dev"));
        loadEnvironmentProperties();
    }

    private ConfigReader() {
        // utility class - no instances
    }

    // -----------------------------------------------------------------------
    // Loaders
    // -----------------------------------------------------------------------

    private static void loadApplicationProperties() {
        try (InputStream in = ConfigReader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new RuntimeException("application.properties not found on the classpath");
            }
            APP_PROPERTIES.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    private static void loadEnvironmentProperties() {
        // Path pattern: <env>/<env>.properties  e.g. dev/dev.properties
        String envFile = ACTIVE_ENV + "/" + ACTIVE_ENV + ".properties";

        try (InputStream in = ConfigReader.class.getClassLoader()
                .getResourceAsStream(envFile)) {
            if (in == null) {
                throw new RuntimeException(envFile + " not found on the classpath");
            }
            ENV_PROPERTIES.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + envFile, e);
        }
    }

    // -----------------------------------------------------------------------
    // Public accessors
    // -----------------------------------------------------------------------

    /** Returns a value from application.properties.
     * JVM system properties (e.g. -Dheadless=true) take precedence,
     * allowing CI pipelines to override file-based defaults at runtime. */
    public static String getProperty(String key) {
        String sysProp = System.getProperty(key);
        return (sysProp != null) ? sysProp : APP_PROPERTIES.getProperty(key);
    }

    /** Returns a value from the active environment's properties file. */
    public static String getEnvProperty(String key) {
        return ENV_PROPERTIES.getProperty(key);
    }

    /** Returns the active environment name (e.g. "dev", "sit", "uat"). */
    public static String getActiveEnv() {
        return ACTIVE_ENV;
    }

    /**
     * Returns the classpath-relative path to a data file in the active
     * environment's data folder.
     *
     * Example: getDataFilePath("logindata.json") -> "dev/data/logindata.json"
     */
    public static String getDataFilePath(String fileName) {
        return ACTIVE_ENV + "/data/" + fileName;
    }
}
