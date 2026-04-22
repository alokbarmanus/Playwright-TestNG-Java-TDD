package com.orangehrm.tests;

import com.aventstack.extentreports.ExtentTest;
import com.orangehrm.base.TestBase;
import com.orangehrm.pages.LoginPageObjects;
import com.orangehrm.utils.ExtentReportManager;
import com.orangehrm.utils.JsonReader;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * LoginPageTests contains all test scenarios for the OrangeHRM login page.
 *
 * Extends TestBase to inherit the browser lifecycle (setUp / tearDown).
 * Page objects are initialised in initPages(), which runs after setUp() completes
 * and guarantees the browser Page is ready before any test method executes.
 *
 * Lifecycle per test method:
 *   TestBase#setUp()  →  initPages()  →  @Test method  →  TestBase#tearDown()
 */
public class LoginPageTests extends TestBase {

    private LoginPageObjects loginPage;

    // -----------------------------------------------------------------------
    // Page Object initialisation – runs after TestBase#setUp() completes
    // -----------------------------------------------------------------------

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "setUp")
    public void initPages() {
        loginPage = new LoginPageObjects(getPage());
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /**
     * Performs a login using credentials supplied via a Map.
     * Expected keys: "username", "password".
     *
     * Using Map<String, String> keeps the method data-source agnostic —
     * callers can supply credentials from JSON, a DataProvider, or any other source.
     */
    private void performLogin(Map<String, String> credentials) {
        loginPage.login(credentials);
    }

    // -----------------------------------------------------------------------
    // Test methods
    // -----------------------------------------------------------------------

    @Test(description = "Verify successful login with valid credentials")
    public void testSuccessfulLogin() {
        ExtentTest test = ExtentReportManager.getTest();

        Map<String, String> credentials = JsonReader.getAsMap("loginData.json", "validUser");

        test.info("Navigating to login page");
        test.info("Attempting login with username: " + credentials.get("username"));
        performLogin(credentials);

        test.info("Verifying dashboard is displayed after login");
        boolean dashboardDisplayed = loginPage.isDashboardDisplayed();
        test.info("Dashboard displayed: " + dashboardDisplayed);

        Assert.assertTrue(dashboardDisplayed,
                "Dashboard should be visible after a successful login");
        test.pass("Dashboard is visible - successful login confirmed");
    }

    @Test(description = "Verify login fails with invalid credentials")
    public void testLoginWithInvalidCredentials() {
        ExtentTest test = ExtentReportManager.getTest();

        Map<String, String> credentials = JsonReader.getAsMap("loginData.json", "invalidUser");
        test.info("Attempting login with invalid credentials - username: " + credentials.get("username"));
        loginPage.login(credentials);

        test.info("Retrieving error message from login page");
        String errorMsg = loginPage.getErrorMessage();
        test.info("Error message received: " + errorMsg);

        Assert.assertNotNull(errorMsg, "An error message should appear for invalid credentials");
        Assert.assertFalse(errorMsg.isBlank(), "Error message should not be blank");
        test.pass("Error message displayed as expected: \"" + errorMsg + "\"");
    }

    @Test(description = "Verify login page title")
    public void testLoginPageTitle() {
        ExtentTest test = ExtentReportManager.getTest();

        test.info("Retrieving page title");
        String title = loginPage.getPageTitle();
        test.info("Page title: " + title);

        Assert.assertNotNull(title, "Page title should not be null");
        test.pass("Page title is present: \"" + title + "\"");
    }

    @Test(description = "Print all registration nested data values from JSON using Map")
    public void testPrintRegistrationDataAsMap() {
        ExtentTest test = ExtentReportManager.getTest();

        test.info("Loading top-level validUser fields from registrationNestedData.json");
        Map<String, String> userMap = JsonReader.getAsMap("registrationNestedData.json", "validUser");
        System.out.println("=== validUser (top-level fields) ===");
        userMap.forEach((key, value) -> {
            System.out.println("  " + key + " : " + value);
            test.info("validUser." + key + " = " + value);
        });

        test.info("Loading nested address fields from registrationNestedData.json");
        Map<String, String> addressMap = JsonReader.getAsMap("registrationNestedData.json", "validUser", "address");
        System.out.println("=== validUser -> address ===");
        addressMap.forEach((key, value) -> {
            System.out.println("  " + key + " : " + value);
            test.info("validUser.address." + key + " = " + value);
        });

        Assert.assertFalse(userMap.isEmpty(), "User map should not be empty");
        Assert.assertFalse(addressMap.isEmpty(), "Address map should not be empty");
        test.pass("All nested JSON data read and logged successfully");
    }
}
