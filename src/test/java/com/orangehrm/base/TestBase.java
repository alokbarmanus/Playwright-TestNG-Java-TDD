package com.orangehrm.base;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.orangehrm.utils.ConfigReader;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.util.ArrayList;
import java.util.List;

/**
 * TestBase is the root class of the framework.
 * All Test classes must extend TestBase.
 *
 * Responsibilities:
 *  - Initialise Playwright / Browser / Page before each test method.
 *  - Tear down all resources after each test method.
 *  - Store all Playwright resources in ThreadLocal so that parallel
 *    test execution is safe - each thread gets its own isolated instance.
 *
 * Usage in subclasses:
 *   getPage()           - the primary Playwright Page for the current thread
 *   getBrowser()        - the active Browser for the current thread
 *   getBrowserContext() - the primary BrowserContext for the current thread
 *   createNewPage()     - open a second (or nth) isolated browser session
 */
public class TestBase {

    private static final ThreadLocal<Playwright>           TL_PLAYWRIGHT      = new ThreadLocal<>();
    private static final ThreadLocal<Browser>              TL_BROWSER         = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext>       TL_BROWSER_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page>                 TL_PAGE            = new ThreadLocal<>();
    private static final ThreadLocal<List<BrowserContext>> TL_EXTRA_CONTEXTS  =
            ThreadLocal.withInitial(ArrayList::new);

    public static Page           getPage()           { return TL_PAGE.get(); }
    public static Browser        getBrowser()        { return TL_BROWSER.get(); }
    public static BrowserContext getBrowserContext() { return TL_BROWSER_CONTEXT.get(); }
    public static Playwright     getPlaywright()     { return TL_PLAYWRIGHT.get(); }

    /**
     * Creates a new isolated browser session (separate BrowserContext).
     * Use when a test needs to act as more than one user simultaneously,
     * e.g. User A sends a message while User B receives it.
     *
     * The returned Page and its BrowserContext are closed automatically
     * by tearDown() - no manual cleanup needed in the test itself.
     *
     * Example:
     *   Page userBPage = createNewPage();
     *   userBPage.navigate(ConfigReader.getEnvProperty("url"));
     *   LoginPageObjects userBLogin = new LoginPageObjects(userBPage);
     *   userBLogin.login("userB", "password");
     *
     * @return a fresh Page belonging to a new BrowserContext
     */
    public static Page createNewPage() {
        BrowserContext extraContext = getBrowser().newContext();
        TL_EXTRA_CONTEXTS.get().add(extraContext);
        return extraContext.newPage();
    }

    @Parameters({"browser"})
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional String browserParam) {
        Playwright playwright = Playwright.create();
        TL_PLAYWRIGHT.set(playwright);

        // XML <parameter name="browser" value="firefox"/> takes precedence;
        // falls back to application.properties browser.name when not set.
        String browserName = (browserParam != null && !browserParam.isBlank())
                ? browserParam
                : ConfigReader.getProperty("browser.name");
        boolean headless         = Boolean.parseBoolean(ConfigReader.getProperty("headless"));
        boolean remoteExecution  = Boolean.parseBoolean(ConfigReader.getProperty("remote.execution"));

        Browser browser;
        if (remoteExecution) {
            // ── Remote mode: connect to a running Playwright Browser Server ──
            // Start the server on the remote machine with:
            //   mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI \
            //     -Dexec.args="run-server --port 8080" -Dexec.classpathScope=test
            String wsUrl = ConfigReader.getProperty("remote.server.url");
            browser = switch (browserName.toLowerCase()) {
                case "firefox" -> playwright.firefox().connect(wsUrl);
                case "webkit"  -> playwright.webkit().connect(wsUrl);
                default        -> playwright.chromium().connect(wsUrl);
            };
        } else {
            // ── Local mode: launch a browser process on this machine ──────────
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(headless);
            browser = switch (browserName.toLowerCase()) {
                case "firefox" -> playwright.firefox().launch(launchOptions);
                case "webkit"  -> playwright.webkit().launch(launchOptions);
                default        -> playwright.chromium().launch(launchOptions);
            };
        }
        TL_BROWSER.set(browser);

        BrowserContext browserContext = browser.newContext();
        TL_BROWSER_CONTEXT.set(browserContext);

        Page page = browserContext.newPage();
        TL_PAGE.set(page);

        // Apply configurable default timeout (overridable via -Dplaywright.wait.timeout=60000
        // at the CLI so CI pipelines can increase it without touching source).
        String timeoutProp = ConfigReader.getProperty("playwright.wait.timeout");
        if (timeoutProp != null && !timeoutProp.isBlank()) {
            page.setDefaultTimeout(Double.parseDouble(timeoutProp));
        }

        page.navigate(ConfigReader.getEnvProperty("env.baseurl"));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        TL_EXTRA_CONTEXTS.get().forEach(BrowserContext::close);
        TL_EXTRA_CONTEXTS.get().clear();

        if (getPage()           != null) getPage().close();
        if (getBrowserContext() != null) getBrowserContext().close();
        if (getBrowser()        != null) getBrowser().close();
        if (getPlaywright()     != null) getPlaywright().close();

        TL_EXTRA_CONTEXTS.remove();
        TL_PAGE.remove();
        TL_BROWSER_CONTEXT.remove();
        TL_BROWSER.remove();
        TL_PLAYWRIGHT.remove();
    }
}
