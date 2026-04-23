package com.orangehrm.tests;

import com.aventstack.extentreports.ExtentTest;
import com.orangehrm.base.TestBase;
import com.orangehrm.pages.AdminPageObjects;
import com.orangehrm.pages.LoginPageObjects;
import com.orangehrm.utils.ExtentReportManager;
import com.orangehrm.utils.JsonReader;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * AdminPageTests contains all test scenarios for the OrangeHRM
 * Admin > System Users screen (/web/index.php/admin/viewSystemUsers).
 *
 * Each test logs in via LoginPageObjects, navigates directly to the System
 * Users page, then exercises AdminPageObjects.  Extent logs are written at
 * every step so the HTML report fully documents what each test performed.
 *
 * Lifecycle per test method:
 *   TestBase#setUp() -> initPages() -> @Test method -> TestBase#tearDown()
 */
public class AdminPageTests extends TestBase {

    private LoginPageObjects loginPage;
    private AdminPageObjects adminPage;

    // -----------------------------------------------------------------------
    // Credentials loaded from the active env data file
    // -----------------------------------------------------------------------
    private Map<String, String> validCredentials;

    // -----------------------------------------------------------------------
    // Page object initialisation — runs after TestBase#setUp() completes
    // -----------------------------------------------------------------------

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "setUp")
    public void initPages() {
        loginPage        = new LoginPageObjects(getPage());
        adminPage        = new AdminPageObjects(getPage());
        validCredentials = JsonReader.getAsMap("loginData.json", "validUser");
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /**
     * Logs in and navigates directly to Admin > System Users, then verifies
     * the URL contains "viewSystemUsers" before returning.
     */
    private void loginAndGoToAdminPage(ExtentTest test) {
        test.info("Logging in with username: " + validCredentials.get("username"));
        loginPage.login(validCredentials);
        test.info("Login submitted — navigating to Admin > System Users");
        adminPage.navigateTo();
        Assert.assertTrue(adminPage.isAdminPageDisplayed(),
                "URL should contain 'viewSystemUsers' after navigating to the Admin page");
        test.info("Admin page URL confirmed: " + adminPage.getCurrentUrl());
    }

    // -----------------------------------------------------------------------
    // Test methods
    // -----------------------------------------------------------------------

    @Test(description = "Verify Admin page is accessible and 'System Users' heading is displayed")
    public void testAdminPageIsAccessible() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToAdminPage(test);

        test.info("Verifying 'System Users' heading is visible");
        Assert.assertTrue(adminPage.isSystemUsersHeadingVisible(),
                "'System Users' heading should be visible on the Admin page");

        String title = adminPage.getPageTitle();
        test.info("Browser page title: " + title);
        Assert.assertNotNull(title, "Page title should not be null");
        Assert.assertFalse(title.isBlank(), "Page title should not be blank");

        test.pass("Admin page is accessible — 'System Users' heading visible, title: \"" + title + "\"");
    }

    @Test(description = "Verify System Users table is displayed on the Admin page")
    public void testSystemUsersTableIsDisplayed() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToAdminPage(test);

        test.info("Checking visibility of the System Users table");
        Assert.assertTrue(adminPage.isTableDisplayed(),
                "System Users table should be visible on the Admin page");

        test.pass("System Users table is displayed");
    }

    // @Test(description = "Verify System Users table contains at least one record")
    // public void testSystemUsersTableHasRecords() {
    //     ExtentTest test = ExtentReportManager.getTest();

    //     loginAndGoToAdminPage(test);

    //     int rowCount = adminPage.getTableRowCount();
    //     test.info("Table row count: " + rowCount);
    //     Assert.assertTrue(rowCount > 0,
    //             "System Users table should contain at least one record");

    //     String recordCountText = adminPage.getRecordCountText();
    //     test.info("Record count text: " + recordCountText);

    //     test.pass("System Users table has " + rowCount + " row(s) — " + recordCountText);
    // }

    @Test(description = "Verify System Users table displays expected column headers")
    public void testTableHeaderColumnsAreDisplayed() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToAdminPage(test);

        List<String> headers = adminPage.getTableHeaderTexts();
        test.info("Column headers found: " + headers);

        Assert.assertFalse(headers.isEmpty(),
                "System Users table should have column headers");
        Assert.assertTrue(headers.stream().anyMatch(h -> h.contains("Username")),
                "Table should have a 'Username' column");
        Assert.assertTrue(headers.stream().anyMatch(h -> h.contains("User Role")),
                "Table should have a 'User Role' column");
        Assert.assertTrue(headers.stream().anyMatch(h -> h.contains("Employee Name")),
                "Table should have an 'Employee Name' column");
        Assert.assertTrue(headers.stream().anyMatch(h -> h.contains("Status")),
                "Table should have a 'Status' column");

        test.pass("All expected column headers are present: " + headers);
    }

    @Test(description = "Verify search filter form elements are visible on the Admin page")
    public void testSearchFormIsDisplayed() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToAdminPage(test);

        test.info("Checking visibility of the search filter form (Username input, Search, Reset)");
        Assert.assertTrue(adminPage.isSearchFormDisplayed(),
                "Search form elements (Username input, Search button, Reset button) should all be visible");

        test.pass("Search filter form is visible with all required elements");
    }

    // @Test(description = "Verify searching by username 'Admin' returns matching results")
    // public void testSearchByUsernameReturnsResults() {
    //     ExtentTest test = ExtentReportManager.getTest();

    //     loginAndGoToAdminPage(test);

    //     test.info("Entering 'Admin' in the username search field and clicking Search");
    //     adminPage.searchByUsername("Admin");

    //     int rowCount = adminPage.getTableRowCount();
    //     test.info("Rows returned after search: " + rowCount);
    //     Assert.assertTrue(rowCount > 0,
    //             "Searching for 'Admin' should return at least one result");

    //     Assert.assertTrue(adminPage.isUsernameInTable("Admin"),
    //             "At least one table row should contain the username 'Admin'");

    //     test.pass("Search by username 'Admin' returned " + rowCount + " result(s) including an 'Admin' row");
    // }

    @Test(description = "Verify Reset button clears the username search filter")
    public void testResetClearsSearchFilter() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToAdminPage(test);

        test.info("Entering 'Admin' in the username filter and performing a search");
        adminPage.searchByUsername("Admin");

        test.info("Clicking Reset to clear the search filters");
        adminPage.resetSearch();

        String usernameValue = adminPage.getSearchUsernameValue();
        test.info("Username field value after Reset: '" + usernameValue + "'");
        Assert.assertTrue(usernameValue.isBlank(),
                "Username filter field should be empty after clicking Reset");

        test.pass("Reset button successfully cleared the username filter field");
    }

    @Test(description = "Verify the Add button is visible and enabled on the Admin page")
    public void testAddButtonIsVisibleAndEnabled() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToAdminPage(test);

        test.info("Checking visibility of the 'Add' button");
        Assert.assertTrue(adminPage.isAddButtonVisible(),
                "'Add' button should be visible on the Admin page");

        test.info("Checking the 'Add' button is enabled");
        Assert.assertTrue(adminPage.isAddButtonEnabled(),
                "'Add' button should be enabled on the Admin page");

        test.pass("'Add' button is visible and enabled on the Admin page");
    }
}
