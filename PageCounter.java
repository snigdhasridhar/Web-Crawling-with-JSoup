package in.ninestars.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class PageCounter {
    public int getLastPageNumber(String startUrl) {
        int lastPageNumber = 1; // Default to 1 if no pagination is found

        try {
            // Fetch the main page
            Document document = Jsoup.connect(startUrl)
                    .userAgent("Mozilla/5.0")
                    .get();

            // Select pagination links
            Elements paginationLinks = document.select("a[href]"); // Adjust the selector as needed

            // Loop through pagination links to find the last page number
            for (Element link : paginationLinks) {
                String linkUrl = link.attr("href");
                // Check if the link is a pagination link
                if (linkUrl.contains("page=")) { // Adjust this condition based on the actual URL structure
                    String[] parts = linkUrl.split("page=");
                    if (parts.length > 1) {
                        try {
                            int pageNumber = Integer.parseInt(parts[1]);
                            if (pageNumber > lastPageNumber) {
                                lastPageNumber = pageNumber; // Update last page number
                            }
                        } catch (NumberFormatException e) {
                            // Handle the case where the page number is not a valid integer
                        }
                    }
                } else if (link.text().matches("\\d+")) { // Check if the link text is a number
                    int pageNumber = Integer.parseInt(link.text());
                    if (pageNumber > lastPageNumber) {
                        lastPageNumber = pageNumber; // Update last page number
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lastPageNumber; // Return the last page number found
    }
}