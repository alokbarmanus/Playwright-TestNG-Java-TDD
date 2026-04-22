package com.orangehrm.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;

/**
 * ExtentReportManager manages the lifecycle of the ExtentReports instance
 * and provides thread-safe access to per-test ExtentTest nodes.
 *
 * Reports are written to:
 *   target/extent-reports/extentReport.html      - interactive Spark report (dark theme)
 *   target/extent-reports/emailable-report.html  - self-contained emailable report (light theme)
 *
 * Usage in listeners / test code:
 *   ExtentReportManager.getInstance()         - shared ExtentReports (init on first call)
 *   ExtentReportManager.setTest(extentTest)   - store current thread's ExtentTest
 *   ExtentReportManager.getTest()             - retrieve current thread's ExtentTest
 *   ExtentReportManager.removeTest()          - clean up ThreadLocal after test ends
 */
public final class ExtentReportManager {

    private static final String REPORT_DIR = "target/extent-reports/";

    private static ExtentReports extent;

    // One ExtentTest per thread - parallel-execution safe
    private static final ThreadLocal<ExtentTest> TL_EXTENT_TEST = new ThreadLocal<>();

    private ExtentReportManager() {}

    // -----------------------------------------------------------------------
    // ExtentReports instance (lazy, thread-safe singleton)
    // -----------------------------------------------------------------------

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            new File(REPORT_DIR).mkdirs();

            // --- Spark reporter (interactive HTML, dark theme) ---
            ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_DIR + "extentReport.html");
            spark.config().setDocumentTitle("OrangeHRM Automation Report");
            spark.config().setReportName("Test Execution Results");
            spark.config().setTheme(Theme.DARK);
            spark.config().setEncoding("UTF-8");

            // --- Emailable reporter (self-contained HTML, light theme) ---
            ExtentSparkReporter emailable = new ExtentSparkReporter(REPORT_DIR + "emailable-report.html");
            emailable.config().setDocumentTitle("OrangeHRM Automation Report");
            emailable.config().setReportName("Test Execution Results");
            emailable.config().setTheme(Theme.STANDARD);
            emailable.config().setEncoding("UTF-8");

            extent = new ExtentReports();
            extent.attachReporter(spark, emailable);

            // System / environment info shown in the report header
            extent.setSystemInfo("OS",          System.getProperty("os.name"));
            extent.setSystemInfo("Java",         System.getProperty("java.version"));
            extent.setSystemInfo("Environment",  ConfigReader.getActiveEnv());
            extent.setSystemInfo("Browser",      ConfigReader.getProperty("browser.name"));
        }
        return extent;
    }

    // -----------------------------------------------------------------------
    // Per-thread ExtentTest accessors
    // -----------------------------------------------------------------------

    public static void       setTest(ExtentTest test) { TL_EXTENT_TEST.set(test); }
    public static ExtentTest getTest()                { return TL_EXTENT_TEST.get(); }
    public static void       removeTest()             { TL_EXTENT_TEST.remove(); }
}
