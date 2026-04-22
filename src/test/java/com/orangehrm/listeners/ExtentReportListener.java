package com.orangehrm.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.microsoft.playwright.Page;
import com.orangehrm.base.TestBase;
import com.orangehrm.utils.ConfigReader;
import com.orangehrm.utils.ExtentReportManager;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Base64;

/**
 * ExtentReportListener hooks into the TestNG execution lifecycle and writes
 * structured results to both the interactive Spark report and the emailable
 * single-file HTML report.
 *
 * Register this listener in every testng-*.xml file:
 *   <listeners>
 *       <listener class-name="com.orangehrm.listeners.ExtentReportListener"/>
 *   </listeners>
 *
 * Events handled
 * --------------
 * ISuiteListener
 *   onStart(ISuite)                       - initialise ExtentReports
 *   onFinish(ISuite)                      - flush / write reports to disk
 *
 * ITestListener (test context)
 *   onStart(ITestContext)                 - log the <test> block name
 *   onFinish(ITestContext)                - log suite counts
 *
 * ITestListener (individual @Test method)
 *   onTestStart                           - create ExtentTest node for this thread
 *   onTestSuccess                         - mark PASS; optional screenshot
 *   onTestFailure                         - mark FAIL; exception; screenshot
 *   onTestSkipped                         - mark SKIP; skip reason
 *   onTestFailedButWithinSuccessPercentage- mark WARNING
 *   onTestFailedWithTimeout               - mark FAIL with timeout note; screenshot
 */
public class ExtentReportListener implements ITestListener, ISuiteListener {

    // -----------------------------------------------------------------------
    // ISuiteListener
    // -----------------------------------------------------------------------

    @Override
    public void onStart(ISuite suite) {
        ExtentReportManager.getInstance(); // eagerly initialise before any test runs
    }

    @Override
    public void onFinish(ISuite suite) {
        ExtentReportManager.getInstance().flush();
    }

    // -----------------------------------------------------------------------
    // ITestListener - test context (<test> block) level
    // -----------------------------------------------------------------------

    @Override
    public void onStart(ITestContext context) {
        // Nothing to log per-context; individual test events carry all needed info
    }

    @Override
    public void onFinish(ITestContext context) {
        // Nothing additional - flush happens at suite finish
    }

    // -----------------------------------------------------------------------
    // ITestListener - individual @Test method level
    // -----------------------------------------------------------------------

    /** Fired BEFORE a @Test method executes. Creates the ExtentTest node. */
    @Override
    public void onTestStart(ITestResult result) {
        String description = result.getMethod().getDescription();
        String displayName = (description != null && !description.isBlank())
                ? description
                : result.getMethod().getMethodName();

        ExtentTest test = ExtentReportManager.getInstance().createTest(displayName);
        test.info("Class  : " + result.getTestClass().getRealClass().getSimpleName());
        test.info("Method : " + result.getMethod().getMethodName());

        ExtentReportManager.setTest(test);
    }

    /** Fired after a @Test method PASSES. */
    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if ("1".equals(ConfigReader.getProperty("test.success.screenshots"))) {
            attachScreenshot(test, "Pass screenshot");
        }
        test.pass("PASSED: " + result.getMethod().getMethodName());
        ExtentReportManager.removeTest();
    }

    /** Fired after a @Test method FAILS. */
    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        test.fail("FAILED: " + result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            test.fail(result.getThrowable());
        }
        if ("1".equals(ConfigReader.getProperty("test.failure.screenshots"))) {
            attachScreenshot(test, "Failure screenshot");
        }
        ExtentReportManager.removeTest();
    }

    /**
     * Fired when a @Test method is SKIPPED.
     * Note: onTestStart may not have been called for skipped tests caused by a
     * failed dependency, so a null-guard creates the node when needed.
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test == null) {
            test = ExtentReportManager.getInstance()
                    .createTest(result.getMethod().getMethodName());
            ExtentReportManager.setTest(test);
        }
        if (result.getThrowable() != null) {
            test.skip(result.getThrowable());
        } else {
            test.skip("SKIPPED: " + result.getMethod().getMethodName());
        }
        ExtentReportManager.removeTest();
    }

    /**
     * Fired when a @Test method fails but the failure count is within the
     * declared successPercentage threshold.
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        test.log(Status.WARNING,
                "FAILED within success percentage: " + result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            test.warning(result.getThrowable());
        }
        ExtentReportManager.removeTest();
    }

    /**
     * Fired when a @Test method fails because it exceeded its timeout limit.
     * Available since TestNG 7.x.
     */
    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test == null) {
            test = ExtentReportManager.getInstance()
                    .createTest(result.getMethod().getMethodName());
            ExtentReportManager.setTest(test);
        }
        test.fail("TIMED OUT: " + result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            test.fail(result.getThrowable());
        }
        if ("1".equals(ConfigReader.getProperty("test.failure.screenshots"))) {
            attachScreenshot(test, "Timeout screenshot");
        }
        ExtentReportManager.removeTest();
    }

    // -----------------------------------------------------------------------
    // Screenshot helper
    // -----------------------------------------------------------------------

    /**
     * Captures a full-page screenshot from the active Playwright Page and
     * attaches it inline (Base64) to the current ExtentTest node.
     *
     * @param test  the ExtentTest node to attach the screenshot to
     * @param title label shown in the report beneath the image
     */
    private void attachScreenshot(ExtentTest test, String title) {
        try {
            Page page = TestBase.getPage();
            if (page == null) {
                test.warning("Screenshot skipped - no active browser page found");
                return;
            }
            byte[] bytes = page.screenshot(new Page.ScreenshotOptions().setFullPage(false));
            String base64 = Base64.getEncoder().encodeToString(bytes);
            test.addScreenCaptureFromBase64String(base64, title);
        } catch (Exception e) {
            test.warning("Screenshot capture failed: " + e.getMessage());
        }
    }
}
