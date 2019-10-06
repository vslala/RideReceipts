package com.automation.uber;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

public class UberDownloadReceipt {

    public static final String SAVE_INVOICE_BTN_XPATH = "/html/body/div/div/div[4]/div/div[2]/div/div[1]/div/div/div[2]/div[${trip_container}]/div[2]/div[2]/div[2]/div/div"
            .replace("${trip_container}", "1");

    private final WebDriver browser;
    WebDriverWait wait;
    private Scanner scan = new Scanner(System.in);
    private int offset = 0;

    public UberDownloadReceipt(WebDriver browser) {
        this.browser = browser;
        this.browser.manage().window().maximize();
        wait = new WebDriverWait(browser, 10000);
    }

    public String login() {
        String loginUrl = "https://m.uber.com";
        browser.get(loginUrl);

        String mobileNumberInputId = "useridInput";

        String nextBtnXPath = "/html/body/div[1]/div/div/div/div[1]/form/button";
        String passwordInputId = "password";
        String passwordBtnNextXPath = "/html/body/div[1]/div/div/div/div/form/button";

        String mobileNumber = "9960543885";
        browser.findElement(By.id(mobileNumberInputId)).sendKeys(mobileNumber);
        browser.findElement(By.xpath(nextBtnXPath)).click();

        String afterLoginTitle = "Book an Uber trip using the web browser on your desktop or phone";

        wait.until(ExpectedConditions.titleIs(afterLoginTitle));

        return browser.getTitle();
    }

    public void close() {
        browser.close();
    }

    public String navigateToTripsPage(int offset) {
        this.offset = offset;
        String myTripsUrl = "https://riders.uber.com/trips?offset="+offset;
        browser.get(myTripsUrl);
        return browser.getTitle();
    }

    public int selectLastMonthTrips() throws InterruptedException {
        int offset = LocalDate.now().getDayOfMonth();
        int totalInvoices = 0;
        login();
        navigateToTripsPage(offset);

        while (true) {
            totalInvoices += downloadAllInvoices();
            try {
                navigateToNextPage();
            } catch(NoSuchElementException e) {
                System.out.println("No more invoices left.");
                break;
            }
        }
        return totalInvoices;
    }

    public int downloadAllInvoices() throws InterruptedException {
        WebElement saveInvoiceBtn;
        int tripContainerIndex = 0;
        List<WebElement> tripContainers = browser.findElements(By.cssSelector("div[data-identity='trip-container']"));
        for (WebElement el : tripContainers) {
            tripContainerIndex++;

            System.out.println("\n\nInvoice: " + el.getText());
            if (el.getText().contains("Cancelled")) {
                continue;
            }

            Actions actions = new Actions(browser);
            actions.moveToElement(el);
            actions.click().perform();

            Thread.sleep(2000);//

            Optional<WebElement> saveBtn = el.findElements(By.tagName("div")).stream().filter(div -> div.getText().equals("Save Invoice")).findFirst();
            if (saveBtn.isEmpty())
                break;
            WebElement saveInvoiceButton = saveBtn.get();
            System.out.println("Btn Text: " + saveInvoiceButton.getText());
//            saveInvoiceBtn =  wait.until(ExpectedConditions.visibilityOfElementLocated(
//                    By.xpath(SAVE_INVOICE_BTN_XPATH.replace("${trip_container}", String.valueOf(tripContainerIndex)))));

            JavascriptExecutor jse2 = (JavascriptExecutor)browser;
            jse2.executeScript("arguments[0].scrollIntoView()", saveInvoiceButton);

            actions = new Actions(browser);
            actions.moveToElement(saveInvoiceButton);
            actions.click().perform();

            Thread.sleep(2000);
        }
        return tripContainerIndex-1;
    }

    public int navigateToNextPage() throws InterruptedException {
        navigateToTripsPage(offset + 10);
        int size = browser.findElements(By.cssSelector("div[data-identity='trip-container']")).size();
        if (size == 0)
            throw new NoSuchElementException();
        return size;
    }
}
