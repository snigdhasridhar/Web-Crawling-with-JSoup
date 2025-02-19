package in.ninestars.crawler;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Crawler {
    private WebDriver driver;
    private String outputDirectory;

    public Crawler(int depth, String outputDirectory) {
        this.outputDirectory = outputDirectory;
        // Set up ChromeDriver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode
        this.driver = new ChromeDriver(options);
    }

    public void startCrawling(String url, int depth) {
        try {
            driver.get(url);
            // Take a screenshot
            takeScreenshot(url);
            // Add your existing crawling logic here
        } catch (Exception e) {
            System.err.println("Error while crawling URL: " + url + " - " + e.getMessage());
        }
    }

    private void takeScreenshot(String url) {
        // Create a unique filename based on the URL
        String fileName = url.replaceAll("[^a-zA-Z0-9]", "_") + ".png"; // Replace invalid characters
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            Files.copy(screenshot.toPath(), Paths.get(outputDirectory, fileName));
            System.out.println("Screenshot saved: " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving screenshot: " + e.getMessage());
        }
    }

    public void quit() {
        if (driver != null) {
            driver.quit();
        }
    }
}