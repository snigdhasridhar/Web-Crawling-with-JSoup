package in.ninestars.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class CrawlerApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of pages to crawl for the Politics section (e.g., 5): ");
        int numberOfPagesPolitics = scanner.nextInt();

        System.out.print("Enter the number of pages to crawl for the Business section (e.g., 1): ");
        int numberOfPagesBusiness = scanner.nextInt();
        scanner.close();

        // Load URLs from config.properties
        Properties properties = new Properties();
        try (InputStream input = CrawlerApp.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Sorry, unable to find config.properties");
                return; // Exit if properties file cannot be loaded
            }
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Error loading properties file: " + e.getMessage());
            return; // Exit if properties file cannot be loaded
        }

        String politicsUrl = properties.getProperty("politics.url");
        String businessUrl = properties.getProperty("business.url");

        // Check if URLs are loaded correctly
        if (politicsUrl == null || businessUrl == null) {
            System.err.println("One or more URLs are not defined in the properties file.");
            return; // Exit if URLs are not found
        }

        PageCounter pageCounter = new PageCounter();

        // Get the last page number in the politics section
        int lastPageNumberPolitics = pageCounter.getLastPageNumber(politicsUrl);
        System.out.println("Last page number in Politics section: " + lastPageNumberPolitics);

        // Specify the output directory for HTML files
        String outputDirectory = "output_asahi_html_files";
        Crawler crawler = new Crawler(3, outputDirectory); // Assuming a default depth level of 3

        System.out.println("Starting Web Crawling for Politics section");
        for (int page = 1; page <= Math.min(numberOfPagesPolitics, lastPageNumberPolitics); page++) {
            System.out.println("Extracting articles from Politics section - Page: " + page);
            String startUrl = politicsUrl + page; // Ensure this line ends with a semicolon
            crawler.startCrawling(startUrl, 1); // Start crawling with initial depth of 1
        }
        System.out.println("Completed Politics Section");

        System.out.println("Starting Web Crawling for Business section");
        for (int page = 1; page <= numberOfPagesBusiness; page++) { // Use user-defined limit
            System.out.println("Extracting articles from Business section - Page: " + page);
            String startUrl = businessUrl + page;
            crawler.startCrawling(startUrl, 1); // Start crawling with initial depth of 1
        }
        System.out.println("Completed Business Section");

        // Clean up and close the crawler
        crawler.quit();
        System.out.println("Crawler has been closed.");
    }
}
