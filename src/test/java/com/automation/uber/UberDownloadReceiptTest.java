package com.automation.uber;

import org.junit.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class UberDownloadReceiptTest {

    UberDownloadReceipt uber;
    public static final int OFFSET = LocalDate.now().getDayOfMonth();

    @Before
    public void setup() {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        WebDriver driver = new ChromeDriver();
        uber = new UberDownloadReceipt(driver);
        assertNotNull(uber);
    }

    @Test
    public void itShouldLoginIntoUber() {
        // loginUrl = m.uber.com
        String title = uber.login();

        String expectedTitle = "Book an Uber trip using the web browser on your desktop or phone";
        assertEquals(expectedTitle, title);
    }

    @Test
    public void itShouldNavigateToTripsPageAfterLoginWithGivenOffset() {
        uber.login();
        String expectedTitle = uber.navigateToTripsPage(OFFSET);
        assertEquals(expectedTitle, "My trips - Uber riders");
    }

    @Test
    public void itShouldDownloadAllTheInvoicesOnThePage() throws InterruptedException {
        uber.login();
        uber.navigateToTripsPage(OFFSET);
        int invoicesCount = uber.downloadAllInvoices();
        Assert.assertEquals(10, invoicesCount);
    }

    @Test
    public void itShouldNavigateToNextPageIfAvailable() throws InterruptedException {
        uber.login();
        uber.navigateToTripsPage(OFFSET);
        int invoiceCount = uber.navigateToNextPage();
        Assert.assertEquals(10, invoiceCount);
    }

    @Test
    public void itShouldDownloadAllTheLastMonthsReceipts() throws InterruptedException {
        int totalInvoices = uber.selectLastMonthTrips();
        Assert.assertEquals(28, totalInvoices);
    }

    @After
    public void close() throws InterruptedException {
        Thread.sleep(10000);
        uber.close();
    }
}
