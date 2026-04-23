package com.orangehrm.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.orangehrm.utils.ConfigReader;

import java.util.List;

/**
 * AdminPageObjects encapsulates all interactions and verifications for the
 * OrangeHRM Admin > System Users screen.
 *
 * Plain class — no base class needed. The Page instance is passed via the
 * constructor. Locators are initialised using the Playwright-native Locator
 * API for full auto-wait support.
 *
 * Pre-condition: the user must already be logged in before any method is called.
 * Call navigateTo() to land on the System Users page.
 *
 * URL: /web/index.php/admin/viewSystemUsers
 */
public class AdminPageObjects {

    private final Page page;

    // -----------------------------------------------------------------------
    // Locators — Page heading & Add button
    // -----------------------------------------------------------------------
    private final Locator systemUsersHeading;
    private final Locator addButton;

    // -----------------------------------------------------------------------
    // Locators — Search filter form
    // -----------------------------------------------------------------------
    private final Locator searchUsernameInput;
    private final Locator userRoleDropdown;
    private final Locator statusDropdown;
    private final Locator searchButton;
    private final Locator resetButton;

    // -----------------------------------------------------------------------
    // Locators — System Users table
    // -----------------------------------------------------------------------
    private final Locator tableContainer;
    private final Locator tableHeaderCells;
    private final Locator tableRows;
    private final Locator recordCount;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * @param page the Playwright Page instance; user must already be logged in.
     */
    public AdminPageObjects(Page page) {
        this.page = page;

        // Page heading — covers both h5 and h6 variants across OrangeHRM versions
        systemUsersHeading = page.locator("h5.oxd-text, h6.oxd-text")
                .filter(new Locator.FilterOptions().setHasText("System Users")).first();

        // Add button — scoped to the header container to avoid false matches
        addButton = page.locator("button.oxd-button:has-text('Add')");

        // Search filter form — Username is the first plain text input in the form;
        // Employee Name uses a separate autocomplete component (oxd-autocomplete)
        searchUsernameInput = page.locator("form.oxd-form .oxd-input-group .oxd-input").first();
        userRoleDropdown    = page.locator(".oxd-select-text").first();
        statusDropdown      = page.locator(".oxd-select-text").nth(1);
        searchButton        = page.locator("button[type='submit']");
        resetButton         = page.locator("button:has-text('Reset')");

        // System Users table
        tableContainer   = page.locator(".oxd-table");
        tableHeaderCells = page.locator(".oxd-table-header .oxd-table-header-cell");
        tableRows        = page.locator(".oxd-table-body .oxd-table-row");
        recordCount      = page.locator(".orangehrm-bottom-container .oxd-text").first();
    }

    // -----------------------------------------------------------------------
    // Navigation
    // -----------------------------------------------------------------------

    /**
     * Navigates directly to the Admin > System Users page.
     * Constructs the URL from the active environment's baseurl property by
     * replacing the login path with the admin viewSystemUsers path.
     */
    public void navigateTo() {
        String adminUrl = ConfigReader.getEnvProperty("env.baseurl")
                .replace("auth/login", "admin/viewSystemUsers");
        page.navigate(adminUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Waits up to 15 s for the URL to contain "viewSystemUsers".
     *
     * @return true if the admin page is displayed, false on timeout
     */
    public boolean isAdminPageDisplayed() {
        try {
            page.waitForURL(url -> url.contains("viewSystemUsers"),
                    new Page.WaitForURLOptions().setTimeout(15_000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns the current URL of the page. */
    public String getCurrentUrl() {
        return page.url();
    }

    /** Returns the browser page title. */
    public String getPageTitle() {
        return page.title();
    }

    // -----------------------------------------------------------------------
    // Page heading & Add button
    // -----------------------------------------------------------------------

    /** Returns true if the "System Users" heading is visible. */
    public boolean isSystemUsersHeadingVisible() {
        systemUsersHeading.waitFor();
        return systemUsersHeading.isVisible();
    }

    /** Returns true if the Add button is visible on the page. */
    public boolean isAddButtonVisible() {
        return addButton.isVisible();
    }

    /** Returns true if the Add button is enabled (not disabled). */
    public boolean isAddButtonEnabled() {
        return addButton.isEnabled();
    }

    // -----------------------------------------------------------------------
    // Search filter form
    // -----------------------------------------------------------------------

    /**
     * Returns true if the core search form elements (Username input, Search
     * button, Reset button) are all visible.
     */
    public boolean isSearchFormDisplayed() {
        searchUsernameInput.waitFor();
        return searchUsernameInput.isVisible()
                && searchButton.isVisible()
                && resetButton.isVisible();
    }

    /**
     * Fills the Username filter field and clicks the Search button.
     * Waits for the network to become idle before returning so that the
     * table is fully refreshed with filtered results.
     *
     * @param username the username to search for, e.g. "Admin"
     */
    public void searchByUsername(String username) {
        searchUsernameInput.fill(username);
        searchButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Clicks the Reset button to clear all filter fields.
     * Waits for the network to become idle so the table reloads fully.
     */
    public void resetSearch() {
        resetButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /** Returns the current value of the Username filter input field. */
    public String getSearchUsernameValue() {
        return searchUsernameInput.inputValue();
    }

    // -----------------------------------------------------------------------
    // System Users table
    // -----------------------------------------------------------------------

    /** Returns true if the System Users table container is visible. */
    public boolean isTableDisplayed() {
        tableContainer.waitFor();
        return tableContainer.isVisible();
    }

    /** Returns the number of data rows currently visible in the table body. */
    public int getTableRowCount() {
        tableContainer.waitFor();
        return tableRows.count();
    }

    /**
     * Returns the trimmed text of each visible column header cell,
     * excluding any blank entries (e.g. the Actions column checkbox cell).
     */
    public List<String> getTableHeaderTexts() {
        tableHeaderCells.first().waitFor();
        return tableHeaderCells.allTextContents()
                .stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    /**
     * Returns true if any visible table row contains the given username text.
     *
     * @param username text to search for within table rows
     */
    public boolean isUsernameInTable(String username) {
        return tableRows.filter(new Locator.FilterOptions().setHasText(username)).count() > 0;
    }

    /**
     * Returns the record count text displayed below the table,
     * e.g. "(5) Records Found".
     */
    public String getRecordCountText() {
        recordCount.waitFor();
        return recordCount.textContent().trim();
    }
}
