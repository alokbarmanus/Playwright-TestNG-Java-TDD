package com.orangehrm.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Map;

/**
 * LoginPageObjects encapsulates all interactions with the OrangeHRM login screen.
 *
 * Plain class — no base class needed. The Page instance is passed in via the
 * constructor and stored locally. Locators are initialised here using the
 * Playwright-native Locator API for full auto-wait and type-safe element actions.
 */
public class LoginPageObjects {

    private final Page page;

    // -----------------------------------------------------------------------
    // Locators
    // -----------------------------------------------------------------------
    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator errorMessage;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * @param page the Playwright Page instance initialised by TestBase#setUp()
     */
    public LoginPageObjects(Page page) {
        this.page     = page;
        usernameInput = page.locator("input[name='username']");
        passwordInput = page.locator("input[name='password']");
        loginButton   = page.locator("button[type='submit']");
        errorMessage  = page.locator(".oxd-alert-content-text");
    }

    // -----------------------------------------------------------------------
    // Actions
    // -----------------------------------------------------------------------

    public void enterUsername(String username) {
        usernameInput.fill(username);
    }

    public void enterPassword(String password) {
        passwordInput.fill(password);
    }

    public void clickLoginButton() {
        loginButton.click();
    }

    /**
     * Convenience method: fills credentials and clicks the login button.
     * Accepts a Map<String, String> with keys "username" and "password",
     * keeping the method data-source agnostic (JSON, DataProvider, etc.).
     */
    public void login(Map<String, String> credentials) {
        enterUsername(credentials.get("username"));
        enterPassword(credentials.get("password"));
        clickLoginButton();
    }

    // -----------------------------------------------------------------------
    // Verifications / Getters
    // -----------------------------------------------------------------------

    public String getErrorMessage() {
        return errorMessage.textContent();
    }

    public boolean isDashboardDisplayed() {
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

    public String getPageTitle() {
        return page.title();
    }
}
