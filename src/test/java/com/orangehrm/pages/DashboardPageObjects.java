package com.orangehrm.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;

/**
 * DashboardPageObjects encapsulates all interactions and verifications for
 * the OrangeHRM Dashboard screen.
 *
 * Plain class — no base class needed. The Page instance is passed in via the
 * constructor. Locators are initialised using the Playwright-native Locator
 * API for full auto-wait support.
 *
 * Pre-condition: the user must already be logged in before any method is called.
 */
public class DashboardPageObjects {

    private final Page page;

    // -----------------------------------------------------------------------
    // Locators — Top Navigation Bar
    // -----------------------------------------------------------------------
    private final Locator topbar;
    private final Locator userDropdownTab;
    private final Locator userDropdownName;
    private final Locator logoutLink;

    // -----------------------------------------------------------------------
    // Locators — Left Navigation Menu
    // -----------------------------------------------------------------------
    private final Locator sideNavMenu;
    private final Locator sideNavMenuItems;

    // -----------------------------------------------------------------------
    // Locators — Dashboard Body
    // -----------------------------------------------------------------------
    private final Locator dashboardHeading;
    private final Locator dashboardWidgetNames;
    private final Locator dashboardGridItems;
    private final Locator quickLaunchItems;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * @param page the Playwright Page instance; user must already be on the dashboard.
     */
    public DashboardPageObjects(Page page) {
        this.page = page;

        // Top nav bar
        topbar            = page.locator(".oxd-topbar");
        userDropdownTab   = page.locator(".oxd-userdropdown-tab");
        userDropdownName  = page.locator(".oxd-userdropdown-name");
        logoutLink        = page.locator("a:has-text('Logout')");

        // Side navigation
        sideNavMenu       = page.locator(".oxd-sidepanel");
        sideNavMenuItems  = page.locator(".oxd-main-menu-item");

        // Dashboard body
        dashboardHeading     = page.locator("h6.oxd-text").filter(new Locator.FilterOptions().setHasText("Dashboard"));
        dashboardWidgetNames = page.locator(".orangehrm-dashboard-widget-name");
        dashboardGridItems   = page.locator(".orangehrm-dashboard-widget");
        quickLaunchItems     = page.locator(".orangehrm-quick-launch-card");
    }

    // -----------------------------------------------------------------------
    // Top Navigation Bar — Actions & Verifications
    // -----------------------------------------------------------------------

    /** Returns true if the top navigation bar is visible on the page. */
    public boolean isTopbarVisible() {
        topbar.waitFor();
        return topbar.isVisible();
    }

    /** Returns the logged-in user's display name from the top-right dropdown. */
    public String getLoggedInUserName() {
        return userDropdownName.textContent().trim();
    }

    /** Opens the user dropdown menu (top-right corner). */
    public void openUserDropdown() {
        userDropdownTab.click();
    }

    /** Logs out by opening the user dropdown and clicking Logout. */
    public void logout() {
        openUserDropdown();
        logoutLink.click();
    }

    // -----------------------------------------------------------------------
    // Left Navigation Menu — Actions & Verifications
    // -----------------------------------------------------------------------

    /** Returns true if the left side navigation panel is visible. */
    public boolean isSideNavMenuVisible() {
        sideNavMenu.waitFor();
        return sideNavMenu.isVisible();
    }

    /** Returns the count of items in the left side navigation menu. */
    public int getSideNavMenuItemCount() {
        return sideNavMenuItems.count();
    }

    /**
     * Clicks a navigation menu item by its visible label text.
     *
     * @param menuItemText visible label e.g. "Admin", "PIM", "Leave"
     */
    public void clickNavMenuItem(String menuItemText) {
        sideNavMenuItems.filter(new Locator.FilterOptions().setHasText(menuItemText))
                .first()
                .click();
    }

    /**
     * Returns the text labels of all visible navigation menu items.
     *
     * @return list of menu item label strings
     */
    public List<String> getAllNavMenuItemTexts() {
        return sideNavMenuItems.allTextContents()
                .stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    // -----------------------------------------------------------------------
    // Dashboard Body — Verifications
    // -----------------------------------------------------------------------

    /** Returns the text of the main "Dashboard" page heading. */
    public String getDashboardHeadingText() {
        return dashboardHeading.first().textContent().trim();
    }

    /** Returns the count of dashboard widget grid items. */
    public int getDashboardWidgetCount() {
        dashboardGridItems.first().waitFor();
        return dashboardGridItems.count();
    }

    /** Returns the visible names of all dashboard widgets. */
    public List<String> getDashboardWidgetNames() {
        return dashboardWidgetNames.allTextContents()
                .stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    /** Returns the count of Quick Launch shortcut icons. */
    public int getQuickLaunchItemCount() {
        quickLaunchItems.first().waitFor();
        return quickLaunchItems.count();
    }

    // -----------------------------------------------------------------------
    // URL / Page state
    // -----------------------------------------------------------------------

    /** Returns true if the current URL contains "dashboard". */
    public boolean isDashboardPage() {
        try {
            // Wait up to 15 s for the URL to contain "dashboard".
            // Firefox and WebKit redirect slower than Chromium; without this
            // wait the URL check races the navigation and returns false.
            page.waitForURL(url -> url.contains("dashboard"),
                    new Page.WaitForURLOptions().setTimeout(15_000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns the current page URL. */
    public String getCurrentUrl() {
        return page.url();
    }

    /** Returns the page title. */
    public String getPageTitle() {
        return page.title();
    }
}
