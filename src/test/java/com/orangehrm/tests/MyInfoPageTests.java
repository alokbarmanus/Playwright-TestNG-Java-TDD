package com.orangehrm.tests;

import com.aventstack.extentreports.ExtentTest;
import com.orangehrm.base.TestBase;
import com.orangehrm.pages.LoginPageObjects;
import com.orangehrm.pages.MyInfoPageObjects;
import com.orangehrm.utils.ExtentReportManager;
import com.orangehrm.utils.JsonReader;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * MyInfoPageTests contains all test scenarios for the OrangeHRM
 * PIM > Personal Details screen.
 *
 * Test URL: /web/index.php/pim/viewPersonalDetails/empNumber/7
 *
 * Each test logs in via LoginPageObjects, navigates directly to the Personal
 * Details page for employee #7, then exercises MyInfoPageObjects.
 * Extent logs are written at every step so the HTML report fully documents
 * what each test performed.
 *
 * Lifecycle per test method:
 *   TestBase#setUp() -> initPages() -> @Test method -> TestBase#tearDown()
 */
public class MyInfoPageTests extends TestBase {

    /** Employee number used across all tests — Admin user in the demo instance. */
    private static final int EMP_NUMBER = 7;

    private LoginPageObjects loginPage;
    private MyInfoPageObjects myInfoPage;

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
        myInfoPage       = new MyInfoPageObjects(getPage());
        validCredentials = JsonReader.getAsMap("loginData.json", "validUser");
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /** Logs in and navigates directly to Personal Details for EMP_NUMBER. */
    private void loginAndGoToPersonalDetails(ExtentTest test) {
        test.info("Logging in with username: " + validCredentials.get("username"));
        loginPage.login(validCredentials);
        test.info("Login submitted — navigating to Personal Details for empNumber=" + EMP_NUMBER);
        myInfoPage.navigateTo(EMP_NUMBER);
        Assert.assertTrue(myInfoPage.isPersonalDetailsPageDisplayed(),
                "URL should contain 'viewPersonalDetails' after navigation");
        test.info("Personal Details URL confirmed: " + myInfoPage.getCurrentUrl());
    }

    // -----------------------------------------------------------------------
    // Test methods
    // -----------------------------------------------------------------------

    @Test(description = "MyInfoPage: Verify Personal Details page is accessible for employee #7")
    public void testPersonalDetailsPageIsAccessible() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToPersonalDetails(test);

        String url = myInfoPage.getCurrentUrl();
        test.info("Current URL: " + url);
        Assert.assertTrue(url.contains("empNumber/" + EMP_NUMBER),
                "URL should contain empNumber/" + EMP_NUMBER);

        String title = myInfoPage.getPageTitle();
        test.info("Browser page title: " + title);
        Assert.assertNotNull(title, "Page title should not be null");
        Assert.assertFalse(title.isBlank(), "Page title should not be blank");

        test.pass("Personal Details page is accessible — URL and title verified");
    }

    @Test(description = "MyInfoPage: Verify 'Personal Details' section heading is visible on the page")
    public void testPersonalDetailsSectionHeadingIsVisible() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToPersonalDetails(test);

        test.info("Verifying 'Personal Details' section heading is visible");
        Assert.assertTrue(myInfoPage.isPersonalDetailsSectionHeadingVisible(),
                "'Personal Details' section heading should be visible");

        test.pass("'Personal Details' section heading is visible");
    }

    @Test(description = "MyInfoPage: Verify Personal Details form is visible on the page")
    public void testPersonalDetailsFormIsVisible() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToPersonalDetails(test);

        test.info("Checking visibility of the Personal Details form");
        Assert.assertTrue(myInfoPage.isPersonalDetailsFormVisible(),
                "Personal Details form should be visible on the page");

        test.pass("Personal Details form is visible");
    }

    @Test(description = "MyInfoPage: Verify First Name, Middle Name and Last Name input fields are visible")
    public void testNameFieldsAreVisible() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToPersonalDetails(test);

        test.info("Checking visibility of First Name, Middle Name and Last Name inputs");
        Assert.assertTrue(myInfoPage.areNameFieldsVisible(),
                "First Name, Middle Name and Last Name fields should all be visible");

        test.pass("All three name input fields (First, Middle, Last) are visible");
    }

    @Test(description = "MyInfoPage: Verify First Name field is pre-populated for employee #7")
    public void testFirstNameFieldIsPrePopulated() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToPersonalDetails(test);

        String firstName = myInfoPage.getFirstNameValue();
        test.info("First Name field value: '" + firstName + "'");

        Assert.assertNotNull(firstName, "First Name value should not be null");
        Assert.assertFalse(firstName.isBlank(),
                "First Name field should be pre-populated for employee #" + EMP_NUMBER);

        test.pass("First Name is pre-populated: '" + firstName + "'");
    }

    @Test(description = "MyInfoPage: Verify Last Name field is pre-populated for employee #7")
    public void testLastNameFieldIsPrePopulated() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToPersonalDetails(test);

        String lastName = myInfoPage.getLastNameValue();
        test.info("Last Name field value: '" + lastName + "'");

        Assert.assertNotNull(lastName, "Last Name value should not be null");
        Assert.assertFalse(lastName.isBlank(),
                "Last Name field should be pre-populated for employee #" + EMP_NUMBER);

        test.pass("Last Name is pre-populated: '" + lastName + "'");
    }

    @Test(description = "MyInfoPage: Verify Employee ID field is visible on the Personal Details page")
    public void testEmployeeIdFieldIsVisible() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToPersonalDetails(test);

        test.info("Checking visibility of the Employee ID field");
        Assert.assertTrue(myInfoPage.isEmployeeIdFieldVisible(),
                "Employee ID field should be visible on the Personal Details page");

        String empId = myInfoPage.getEmployeeIdValue();
        test.info("Employee ID field value: '" + empId + "'");

        test.pass("Employee ID field is visible with value: '" + empId + "'");
    }

    // @Test(description = "MyInfoPage: Verify Date of Birth field is visible on the Personal Details page")
    // public void testDateOfBirthFieldIsVisible() {
    //     ExtentTest test = ExtentReportManager.getTest();

    //     loginAndGoToPersonalDetails(test);

    //     test.info("Checking visibility of the Date of Birth field");
    //     Assert.assertTrue(myInfoPage.isDobFieldVisible(),
    //             "Date of Birth field should be visible on the Personal Details page");

    //     String dob = myInfoPage.getDobValue();
    //     test.info("Date of Birth value: '" + dob + "'");

    //     test.pass("Date of Birth field is visible");
    // }

    @Test(description = "MyInfoPage: Verify Nationality and Marital Status dropdowns are visible")
    public void testDropdownFieldsAreVisible() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToPersonalDetails(test);

        test.info("Checking visibility of the Nationality dropdown");
        Assert.assertTrue(myInfoPage.isNationalityDropdownVisible(),
                "Nationality dropdown should be visible");

        test.info("Checking visibility of the Marital Status dropdown");
        Assert.assertTrue(myInfoPage.isMaritalStatusDropdownVisible(),
                "Marital Status dropdown should be visible");

        test.pass("Both Nationality and Marital Status dropdowns are visible");
    }

    @Test(description = "MyInfoPage: Verify Save button is visible and enabled on the Personal Details page")
    public void testSaveButtonIsVisibleAndEnabled() {
        ExtentTest test = ExtentReportManager.getTest();

        loginAndGoToPersonalDetails(test);

        test.info("Checking visibility and state of the Save button");
        Assert.assertTrue(myInfoPage.isSaveButtonVisible(),
                "Save button should be visible on the Personal Details page");
        Assert.assertTrue(myInfoPage.isSaveButtonEnabled(),
                "Save button should be enabled on the Personal Details page");

        test.pass("Save button is visible and enabled");
    }
}
