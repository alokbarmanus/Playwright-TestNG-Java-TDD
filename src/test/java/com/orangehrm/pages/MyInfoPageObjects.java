package com.orangehrm.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.orangehrm.utils.ConfigReader;

/**
 * MyInfoPageObjects encapsulates all interactions and verifications for the
 * OrangeHRM PIM > Personal Details screen.
 *
 * Plain class — no base class needed. The Page instance is passed via the
 * constructor. Locators are initialised using the Playwright-native Locator
 * API for full auto-wait support.
 *
 * Pre-condition: the user must already be logged in before any method is called.
 * Call navigateTo(empNumber) to land on the Personal Details page for a given employee.
 *
 * URL pattern: /web/index.php/pim/viewPersonalDetails/empNumber/{empNumber}
 */
public class MyInfoPageObjects {

    private final Page page;

    // -----------------------------------------------------------------------
    // Locators — Personal Details form fields
    // -----------------------------------------------------------------------
    private final Locator firstNameInput;
    private final Locator middleNameInput;
    private final Locator lastNameInput;
    private final Locator employeeIdInput;

    // -----------------------------------------------------------------------
    // Locators — Other Details fields
    // -----------------------------------------------------------------------
    private final Locator otherIdInput;
    private final Locator driverLicenseInput;
    private final Locator dobInput;

    // -----------------------------------------------------------------------
    // Locators — Dropdowns
    // -----------------------------------------------------------------------
    private final Locator nationalityDropdown;
    private final Locator maritalStatusDropdown;

    // -----------------------------------------------------------------------
    // Locators — Section headings
    // -----------------------------------------------------------------------
    private final Locator personalDetailsSectionHeading;
    private final Locator customFieldsSectionHeading;

    // -----------------------------------------------------------------------
    // Locators — Save buttons & form
    // -----------------------------------------------------------------------
    private final Locator saveButton;
    private final Locator personalDetailsForm;

    // -----------------------------------------------------------------------
    // Locators — Employee name display (top of page)
    // -----------------------------------------------------------------------
    private final Locator employeeNameDisplay;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * @param page the Playwright Page instance; user must already be logged in.
     */
    public MyInfoPageObjects(Page page) {
        this.page = page;

        // Name row — OrangeHRM uses name attributes on these inputs
        firstNameInput  = page.locator("input[name='firstName']");
        middleNameInput = page.locator("input[name='middleName']");
        lastNameInput   = page.locator("input[name='lastName']");

        // Employee ID — second input in the form after the name row
        employeeIdInput = page.locator(".oxd-form-row .oxd-input-group .oxd-input")
                .filter(new Locator.FilterOptions()
                        .setHasNot(page.locator("[name='firstName'],[name='middleName'],[name='lastName']")))
                .first();

        // Other Details fields (rows 3 and 4 of the form)
        otherIdInput      = page.locator(".oxd-input-group .oxd-input").nth(3);
        driverLicenseInput = page.locator(".oxd-input-group .oxd-input").nth(4);

        // Date of birth — inside the date-picker component
        dobInput = page.locator(".oxd-date-wrapper .oxd-date-input input");

        // Dropdowns — nationality is first, marital status second on the page
        nationalityDropdown    = page.locator(".oxd-select-text").first();
        maritalStatusDropdown  = page.locator(".oxd-select-text").nth(1);

        // Section headings
        personalDetailsSectionHeading = page.locator("h6.oxd-text, h5.oxd-text")
                .filter(new Locator.FilterOptions().setHasText("Personal Details")).first();
        customFieldsSectionHeading    = page.locator("h6.oxd-text, h5.oxd-text")
                .filter(new Locator.FilterOptions().setHasText("Custom Fields")).first();

        // Save button (first one on the page — belongs to Personal Details section)
        saveButton = page.locator("button[type='submit']").first();

        // Personal Details form container
        personalDetailsForm = page.locator(".oxd-form").first();

        // Employee name shown at the top of the edit-employee view
        employeeNameDisplay = page.locator(".employee-name, .orangehrm-edit-employee-name").first();
    }

    // -----------------------------------------------------------------------
    // Navigation
    // -----------------------------------------------------------------------

    /**
     * Navigates directly to the Personal Details page for the given employee number.
     * Constructs the URL from the active environment's baseurl property.
     *
     * @param empNumber e.g. 7
     */
    public void navigateTo(int empNumber) {
        String url = ConfigReader.getEnvProperty("env.baseurl")
                .replace("auth/login", "pim/viewPersonalDetails/empNumber/" + empNumber);
        page.navigate(url);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Waits up to 15 s for the URL to contain "viewPersonalDetails".
     *
     * @return true if the Personal Details page is displayed, false on timeout
     */
    public boolean isPersonalDetailsPageDisplayed() {
        try {
            page.waitForURL(url -> url.contains("viewPersonalDetails"),
                    new Page.WaitForURLOptions().setTimeout(15_000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns the current URL. */
    public String getCurrentUrl() {
        return page.url();
    }

    /** Returns the browser page title. */
    public String getPageTitle() {
        return page.title();
    }

    // -----------------------------------------------------------------------
    // Section headings
    // -----------------------------------------------------------------------

    /** Returns true if the "Personal Details" section heading is visible. */
    public boolean isPersonalDetailsSectionHeadingVisible() {
        personalDetailsSectionHeading.waitFor();
        return personalDetailsSectionHeading.isVisible();
    }

    /** Returns true if the "Custom Fields" section heading is visible. */
    public boolean isCustomFieldsSectionVisible() {
        try {
            customFieldsSectionHeading.waitFor(
                    new Locator.WaitForOptions().setTimeout(5_000));
            return customFieldsSectionHeading.isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Name fields
    // -----------------------------------------------------------------------

    /** Returns true if the First Name, Middle Name and Last Name inputs are all visible. */
    public boolean areNameFieldsVisible() {
        firstNameInput.waitFor();
        return firstNameInput.isVisible()
                && middleNameInput.isVisible()
                && lastNameInput.isVisible();
    }

    /** Returns the current value of the First Name input. */
    public String getFirstNameValue() {
        return firstNameInput.inputValue();
    }

    /** Returns the current value of the Middle Name input. */
    public String getMiddleNameValue() {
        return middleNameInput.inputValue();
    }

    /** Returns the current value of the Last Name input. */
    public String getLastNameValue() {
        return lastNameInput.inputValue();
    }

    /**
     * Clears and fills the First Name input.
     *
     * @param firstName new first name value
     */
    public void setFirstName(String firstName) {
        firstNameInput.clear();
        firstNameInput.fill(firstName);
    }

    /**
     * Clears and fills the Last Name input.
     *
     * @param lastName new last name value
     */
    public void setLastName(String lastName) {
        lastNameInput.clear();
        lastNameInput.fill(lastName);
    }

    // -----------------------------------------------------------------------
    // Employee ID field
    // -----------------------------------------------------------------------

    /** Returns true if the Employee ID input field is visible. */
    public boolean isEmployeeIdFieldVisible() {
        return employeeIdInput.isVisible();
    }

    /** Returns the current value of the Employee ID field. */
    public String getEmployeeIdValue() {
        return employeeIdInput.inputValue();
    }

    // -----------------------------------------------------------------------
    // Date of Birth field
    // -----------------------------------------------------------------------

    /** Returns true if the Date of Birth date-picker input is visible. */
    public boolean isDobFieldVisible() {
        return dobInput.isVisible();
    }

    /** Returns the current value of the Date of Birth input. */
    public String getDobValue() {
        return dobInput.inputValue();
    }

    // -----------------------------------------------------------------------
    // Nationality & Marital Status dropdowns
    // -----------------------------------------------------------------------

    /** Returns true if the Nationality dropdown is visible. */
    public boolean isNationalityDropdownVisible() {
        return nationalityDropdown.isVisible();
    }

    /** Returns true if the Marital Status dropdown is visible. */
    public boolean isMaritalStatusDropdownVisible() {
        return maritalStatusDropdown.isVisible();
    }

    // -----------------------------------------------------------------------
    // Form & Save button
    // -----------------------------------------------------------------------

    /** Returns true if the Personal Details form container is visible. */
    public boolean isPersonalDetailsFormVisible() {
        personalDetailsForm.waitFor();
        return personalDetailsForm.isVisible();
    }

    /** Returns true if the Save button is visible on the page. */
    public boolean isSaveButtonVisible() {
        return saveButton.isVisible();
    }

    /** Returns true if the Save button is enabled (not disabled). */
    public boolean isSaveButtonEnabled() {
        return saveButton.isEnabled();
    }

    // -----------------------------------------------------------------------
    // Employee name at top of page
    // -----------------------------------------------------------------------

    /**
     * Returns the employee's full name as displayed at the top of the
     * edit-employee form, or an empty string if the element is not found.
     */
    public String getEmployeeNameDisplayed() {
        try {
            employeeNameDisplay.waitFor(
                    new Locator.WaitForOptions().setTimeout(5_000));
            return employeeNameDisplay.textContent().trim();
        } catch (Exception e) {
            return "";
        }
    }
}
