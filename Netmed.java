package in.ninestars.ss;

import com.assertthat.selenium_shutterbug.core.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

public class Netmed {
    private static Properties config;
    private static Set<String> crawledUrls = new HashSet<>();
    private static final String URLS_FILE = "url_netmeds.txt"; // File to store all crawled URLs

    static {
        config = new Properties();
        try {
            System.out.println("Current working directory: " + System.getProperty("user.dir"));
            config.load(new FileInputStream("config.properties")); // Adjust the path if necessary
            loadCrawledUrls(); // Load already crawled URLs from urls.txt
        } catch (IOException e) {
            System.out.println("Error loading config.properties: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Create a single folder for this run
        String runFolder = "crawl_run_" + System.currentTimeMillis();
        new File(runFolder).mkdirs(); // Create the directory

        // Define the folders for HTML, screenshots, and text files within the run folder
        String htmlFolder = runFolder + File.separator + "html";
        String ssFolder = runFolder + File.separator + "screenshots";
        String textFolder = runFolder + File.separator + "text"; // New folder for text files

        // Create directories for HTML, screenshots, and text files
        new File(htmlFolder).mkdirs();
        new File(ssFolder).mkdirs();
        new File(textFolder).mkdirs(); // Create text folder

        String baseUrl = "https://www.netmeds.com/prescriptions/hair-loss"; // Base URL to start crawling

        // Set up Selenium WebDriver with Chrome
        System.setProperty("webdriver.chrome.driver", config.getProperty("chrome.driver.path"));
        ChromeOptions chrome = new ChromeOptions();
        chrome.addArguments("--disable-dev-shm-usage");
        chrome.addArguments("--disable-gpu");
        chrome.addArguments("--no-sandbox");
        chrome.addArguments("--window-size=1920,1080");
        chrome.addArguments("--disable-blink-features=AutomationControlled");

        WebDriver webD = new ChromeDriver(chrome);

        // Counters for HTML files, screenshots, and text files
        int htmlFileCount = 0;
        int screenshotCount = 0;
        int textFileCount = 0;

        try {
            // Navigate to the base URL
            webD.get(baseUrl);

            // Accept cookies using XPath
            acceptCookies(webD);

            // Fetch and parse the HTML document using Jsoup
            Document webDoc = Jsoup.connect(baseUrl).get();
            Elements drugLinks = webDoc.select("a[href]"); // Select all anchor tags with href attribute

            // Regex pattern to match URLs under /prescriptions/
            Pattern acceptedUrlPattern = Pattern.compile("^https://www\\.netmeds\\.com/prescriptions/.*$");

            // Extract drug links from the current page
            for (Element link : drugLinks) {
                String absLink = link.absUrl("href");

                // Debugging output to see the URLs being processed
                System.out.println("Found link: " + absLink);

                // Check if the link matches the accepted pattern
                if (acceptedUrlPattern.matcher(absLink).matches()) {
                    // Proceed with crawling the URL
                    System.out.println("Trying to Crawl   =  " + absLink);
                    if (crawledUrls.contains(absLink)) {
                        System.out.println("Rejected (already crawled): " + absLink);
                        continue; // Skip this URL if it has already been crawled
                    }

                    // Capture both screenshot and HTML content
                    String uniqueNumber = UUID.randomUUID().toString();
                    screenShot(webD, absLink, uniqueNumber, ssFolder);
                    saveHtml(absLink, uniqueNumber, htmlFolder);
                    saveTextData(webD, absLink, uniqueNumber, textFolder);

                    // Add the URL to the set of crawled URLs
                    crawledUrls.add(absLink);
                    writeUrlToFile(absLink); // Write the URL to urls.txt
                    System.out.println("Crawled and added URL: " + absLink); // Debugging output
                    // Increment counters
                    htmlFileCount++;
                    screenshotCount++;
                    textFileCount++;
                } else {
                    System.out.println("URL does not match pattern: " + absLink);
                }
            }

            // Print the total counts of HTML files, screenshots, and text files taken
            System.out.println("Total HTML files saved: " + htmlFileCount);
            System.out.println("Total screenshots taken: " + screenshotCount);
            System.out.println("Total text files saved: " + textFileCount);

        } catch (IOException e) {
            System.out.println(e);
        } finally {
            webD.quit(); // Close WebDriver
        }
    }

    private static void loadCrawledUrls() {
        File urlsFile = new File(URLS_FILE);
        if (!urlsFile.exists()) {
            try {
                boolean created = urlsFile.createNewFile(); // Create the file if it does not exist
                if (created) {
                    System.out.println("Created new URL file: " + URLS_FILE);
                } else {
                    System.out.println("URL file already exists: " + URLS_FILE);
                }
            } catch (IOException e) {
                System.out.println("Error creating URL file: " + e.getMessage());
                return; // Exit the method if the file cannot be created
            }
        } else {
            System.out.println("Loading crawled URLs from existing URL file: " + URLS_FILE);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(urlsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                crawledUrls.add(line.trim()); // Add each URL to the set
            }
        } catch (IOException e) {
            System.out.println("Error loading crawled URLs from URL file: " + e.getMessage());
        }
    }

    private static void writeUrlToFile(String url) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(URLS_FILE, true))) { // Append to the file
            writer.println(url); // Write the URL to urls.txt
            System.out.println("Added to urls.txt: " + url); // Debugging output
        } catch (IOException e) {
            System.out.println("Error writing URL to urls.txt: " + e.getMessage());
        }
    }

    private static void acceptCookies(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30)); // Increased wait time

            // Wait for the cookie consent modal to be visible using the new XPath
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='webklipper-publisher-widget-container-notification-container']/div[2]/a/img")));

            // Locate the accept button using the new XPath
            WebElement acceptButton = driver.findElement(By.xpath("//*[@id='webklipper-publisher-widget-container-notification-container']/div[2]/a/img"));

            // Click the button to accept cookies
            acceptButton.click();
            System.out.println("Accepted cookies.");
        } catch (NoSuchElementException e) {
            System.out.println("Cookie consent button not found: " + e.getMessage());
        } catch (TimeoutException e) {
            System.out.println("Timed out waiting for cookie consent button: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Failed to accept cookies: " + e.getMessage());
        }
    }

    private static void saveTextData(WebDriver driver, String url, String uniqueStr, String textFolder) {
        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete';"));

            // Wait for the title to be visible
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[@class='black-txt']")));

            // Extract the title using the provided XPath
            String title = driver.findElement(By.xpath("//h1[@class='black-txt']")).getText();
            String safeTitle = title.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_"); // Make the title safe for file names

            // Extract specific sections using the provided XPaths
            String manufacturer = driver.findElement(By.xpath("//span[@class='drug-manu']/a")).getText();
            String composition = driver.findElement(By.xpath("//div[@class='drug-content']/div/div[@class='manufacturer_details '][3]//div[@class='manufacturer__name_value']")).getText();
            String productContent = driver.findElement(By.xpath("//div[@id='np_tab1']//div[@class='col-md-12']/div/p")).getText();

            // Create a new file for the article
            File outputFile = new File(textFolder, safeTitle + ".txt");

            // Write the results to the file
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
                writer.println("TITLE: " + title);
                writer.println("MANUFACTURER: " + manufacturer);
                writer.println("COMPOSITION: " + composition);
                writer.println("PRODUCT CONTENT: ");
                writer.println(" CONTENT: " + productContent); // Include product content
                writer.println("--------------------------------------------------------------------------------------------------------------------");
            }

            System.out.println("Saved text data to: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            System.out.println("Error fetching the article from " + url + ": " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing the article from " + url + ": " + e.getMessage());
            // Print the page source for debugging
            System.out.println(driver.getPageSource());
        }
    }

    private static void screenShot(WebDriver driver, String url, String uniqueStr, String ssFolder) {
        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(120));
            wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete';"));

            // Capture full-page screenshot
            Shutterbug.shootPage(driver, Capture.FULL)
                    .withName(uniqueStr)
                    .save(ssFolder); // Save to the specified screenshots folder

            System.out.println("Saved screenshot to: " + ssFolder + File.separator + uniqueStr + ".png");

        } catch (TimeoutException e) {
            System.out.println("Page load timed out for URL: " + url);
        } catch (Exception e) {
            System.out.println("Failed to take screenshot for URL: " + url);
        }
    }

    private static void saveHtml(String url, String uniqueStr, String htmlFolder) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                // Set a User-Agent to mimic a browser request
                Document webDocs = Jsoup.connect(url)
                        .timeout(120000) // Set timeout to 120 seconds
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") // Example User-Agent
                        .get();

                String htmlContent = webDocs.html();
                String fileName = htmlFolder + File.separator + uniqueStr + ".html"; // Save to the specified HTML folder
                File file = new File(fileName);
                try (OutputStream outputStream = new FileOutputStream(file)) {
                    outputStream.write(htmlContent.getBytes());
                }
                return; // Exit the method after successfully saving the HTML
            } catch (IOException e) {
                attempts++;
                System.out.println("Failed to download or save HTML content for URL: " + url + ". Attempt " + attempts + ". Error: " + e.getMessage());
                if (attempts >= 3) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }
}