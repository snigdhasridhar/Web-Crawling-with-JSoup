package in.ninestars.crawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ParsedHtmlContent {

    private static Set<String> visitedUrls = new HashSet<>(); // Set to track visited URLs
    private static final String BASE_DOMAIN = "ndtv.com"; // Set the base domain to restrict URLs
    private static final Pattern ACCEPTED_URL_PATTERN = Pattern.compile("https://www\\.ndtv\\.com/world/.*");
    private static final Pattern REJECTED_URL_PATTERN = Pattern.compile("https://www\\.ndtv\\.com/video/.*");
    private static Set<String> rejectedUrls = new HashSet<>(); // Set to track rejected URLs

    public static void main(String[] args) {
        String urlstr = "https://www.ndtv.com/";
        int maxDepth1Links = 5; // Set a limit for the number of depth 1 URLs to fetch

        try {
            // Fetch the HTML content using Jsoup depth 0
            Document document = Jsoup.connect(urlstr)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .get();

            // Print the title of the webpage
            String title = document.title();
            System.out.println("Title: " + title);

            // Extract and print all links (anchor tags) from the parent URL
            Elements links = document.select("a[href]"); // Select all anchor tags with href attribute
            System.out.println("URLs from parent URL:");
            for (org.jsoup.nodes.Element link : links) {
                String linkUrl = link.attr("href"); // Get the URL of each link

                // Check if the URL matches the accepted or rejected patterns
                if (REJECTED_URL_PATTERN.matcher(linkUrl).matches()) {
                    rejectedUrls.add(linkUrl); // Add to rejected URLs
                    continue; // Skip rejected URLs
                }

                if (ACCEPTED_URL_PATTERN.matcher(linkUrl).matches()) {
                    // Fetch and parse accepted URLs
                    fetchAndSaveHtmlContent(linkUrl);
                }
            }

            // Print rejected URLs
            System.out.println("Rejected URLs:");
            for (String rejectedUrl : rejectedUrls) {
                System.out.println(rejectedUrl);
            }

        } catch (IOException e) {
            System.out.println("Error fetching the parent URL: " + e.getMessage());
            e.printStackTrace(); // Print the stack trace if an error occurs
        }
    }

    private static void fetchAndSaveHtmlContent(String url) {
        // Validate the URL
        if (url.isEmpty() || url.equals("javascript:void(0);")) {
            System.out.println("Invalid URL: " + url);
            return; // Skip invalid URLs
        }

        // Convert relative URLs to absolute URLs
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://www.ndtv.com" + url; // Append the base URL to the relative URL
        }

        // Check if the URL has already been visited
        if (visitedUrls.contains(url)) {
            System.out.println("Already visited: " + url);
            return; // Skip already visited URLs
        }

        // Mark the URL as visited
        visitedUrls.add(url);

        try {
            // Fetch the HTML content of the accepted URL
            Document acceptedDocument = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .timeout(10000) // Set a timeout of 10 seconds
                    .get();

            // Save the HTML content to a file
            saveHtmlToFile(url, acceptedDocument.html());

            // Print the accepted URL
            System.out.println("Accepted URL: " + url);

        } catch (IOException e) {
            System.out.println("Failed to fetch content from: " + url + " - " + e.getMessage());
            e.printStackTrace(); // Print the stack trace if an error occurs
        }
    }

    private static void saveHtmlToFile(String url, String htmlContent) {
        String fileName = url.replaceAll("https?://", "").replaceAll("[^a-zA-Z0-9]", "_") + ".html"; // Create a valid file name
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(htmlContent); // Write the HTML content to the file
            System.out.println("HTML content saved to: " + fileName);
        } catch (IOException e) {
            System.out.println("Error saving HTML content to file: " + e.getMessage());
        }
    }
}



