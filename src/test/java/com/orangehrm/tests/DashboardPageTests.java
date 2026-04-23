package com.orangehrm.tests;

import com.aventstack.extentreports.ExtentTest;
import com.orangehrm.base.TestBase;
import com.orangehrm.pages.DashboardPageObjects;
import com.orangehrm.pages.LoginPageObjects;
import com.orangehrm.utils.ExtentReportManager;
import com.orangehrm.utils.JsonReader;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * DashboardPageTests contains all test scenarios for the OrangeHRM Dashboard screen.
 *
 * Each test method logs in using LoginPageObjects, then exercises the dashboard
 * through DashboardPageObjects.  Extent logs are written at every step so the
 * generated HTML reports fully document what each test did.
 *
 * Lifecycle per test method:
 *   TestBase#setUp() -> initPages() -> @Test method -> TestBase#tearDown()
 */
public class DashboardPageTests extends TestBase {

    private LoginPageObjects    loginPage;
    private DashboardPageObjects dashboardPage;

    // -----------------------------------------------------------------------
    // Credentials loaded from the active env data file
    // -----------------------------------------------------------------------
    private Map<String, String> validCredentials;

    // -----------------------------------------------------------------------
    // Page object initialisation — runs after TestBase#setUp() completes
    // -----------------------------------------------------------------------

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "setUp")
    public void initPages() {
        loginPage     = new LoginPageObjects(getPage());
        dashboardPage = new DashboardPageObjects(getPage());
        validCredentials = JsonReader.getAsMap("loginData.json", "validUser");
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /** Performs a full login and verifies the dashboard is reached. */
    private void loginAndGoToDashboard(ExtentTest test) {
        test.info("Logging in with username: " + validCredentials.get("username"));
        loginPage.login(validCredentials);
        test.info("Login submitted — verifying redirect to dashboard");
        Assert.assertTrue(dashboardPage.isDashboardPage(),
                "URL should contain 'dashboard' after successful login");
        test.info("Dashboard URL confirmed: " + dashboardPage.getCurrentUrl());
    }

    // -----------------------------------------------------------------------
    // Test methods
    // -----------------------------------------------------------------------

    @Test(description = "DashboardPage: Verify dashboard URL and page title after login")
    public void testDashboardUrlAndTitle() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToDashboard(test);

        String title = dashboardPage.getPageTitle();
        test.info("Page title: " + title);
        Assert.assertNotNull(title, "Page title should not be null");
        Assert.assertFalse(title.isBlank(), "Page title should not be blank");

        test.pass("Dashboard URL and page title verified — title: \"" + title + "\"");
    }

    @Test(description = "DashboardPage: Verify top navigation bar is visible on dashboard")
    public void testTopbarIsVisible() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToDashboard(test);

        test.info("Checking visibility of the top navigation bar");
        boolean visible = dashboardPage.isTopbarVisible();
        test.info("Top navigation bar visible: " + visible);

        Assert.assertTrue(visible, "Top navigation bar should be visible on the dashboard");
        test.pass("Top navigation bar is visible");
    }

    @Test(description = "DashboardPage: Verify logged-in user's display name is shown in the top bar")
    public void testLoggedInUserNameIsDisplayed() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToDashboard(test);

        test.info("Retrieving logged-in user display name from top navigation bar");
        String userName = dashboardPage.getLoggedInUserName();
        test.info("Logged-in user display name: " + userName);

        Assert.assertNotNull(userName, "User display name should not be null");
        Assert.assertFalse(userName.isBlank(), "User display name should not be blank");
        test.pass("Logged-in user display name is shown: \"" + userName + "\"");
    }

    @Test(description = "DashboardPage: Verify left side navigation menu is visible with expected items")
    public void testSideNavigationMenuIsVisible() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToDashboard(test);

        test.info("Checking visibility of the left side navigation menu");
        Assert.assertTrue(dashboardPage.isSideNavMenuVisible(),
                "Side navigation menu should be visible");

        int itemCount = dashboardPage.getSideNavMenuItemCount();
        test.info("Number of navigation menu items found: " + itemCount);
        Assert.assertTrue(itemCount > 0, "Navigation menu should have at least one item");

        List<String> menuItems = dashboardPage.getAllNavMenuItemTexts();
        test.info("Navigation menu items: " + menuItems);

        test.pass("Side navigation menu is visible with " + itemCount + " items");
    }

    @Test(description = "DashboardPage: Verify dashboard widgets are displayed")
    public void testDashboardWidgetsAreDisplayed() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToDashboard(test);

        test.info("Counting dashboard widget grid items");
        int widgetCount = dashboardPage.getDashboardWidgetCount();
        test.info("Dashboard widget grid item count: " + widgetCount);
        Assert.assertTrue(widgetCount > 0, "At least one dashboard widget should be displayed");

        List<String> widgetNames = dashboardPage.getDashboardWidgetNames();
        test.info("Dashboard widget names: " + widgetNames);

        test.pass("Dashboard is displaying " + widgetCount + " widgets: " + widgetNames);
    }

    @Test(description = "DashboardPage: Verify Quick Launch shortcuts are displayed on the dashboard")
    public void testQuickLaunchItemsAreDisplayed() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToDashboard(test);

        test.info("Counting Quick Launch shortcut items");
        int quickLaunchCount = dashboardPage.getQuickLaunchItemCount();
        test.info("Quick Launch item count: " + quickLaunchCount);

        Assert.assertTrue(quickLaunchCount > 0,
                "At least one Quick Launch shortcut should be visible on the dashboard");
        test.pass("Quick Launch section is visible with " + quickLaunchCount + " shortcuts");
    }

    @Test(description = "DashboardPage: Verify user can navigate to Admin page from the dashboard menu")
    public void testNavigateToAdminPage() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToDashboard(test);

        test.info("Clicking 'Admin' item in the left navigation menu");
        dashboardPage.clickNavMenuItem("Admin");

        String currentUrl = dashboardPage.getCurrentUrl();
        test.info("URL after clicking Admin: " + currentUrl);

        Assert.assertTrue(currentUrl.contains("admin") || currentUrl.contains("viewSystemUsers"),
                "URL should indicate the Admin page after clicking the Admin menu item");
        test.pass("Successfully navigated to Admin page — URL: " + currentUrl);
    }

    @Test(description = "DashboardPage: Verify user can logout from the dashboard")
    public void testLogout() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToDashboard(test);

        test.info("Opening user dropdown menu");
        test.info("Clicking Logout");
        dashboardPage.logout();

        String currentUrl = dashboardPage.getCurrentUrl();
        test.info("URL after logout: " + currentUrl);

        Assert.assertTrue(currentUrl.contains("login") || currentUrl.contains("auth"),
                "URL should redirect to the login page after logout");
        test.pass("Logout successful — redirected to: " + currentUrl);
    }

}
