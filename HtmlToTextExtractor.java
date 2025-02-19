package in.ninestars.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HtmlToTextExtractor {
    public static void main(String[] args) {
        // Specify the directory containing HTML files
        String inputDirectoryPath = "/home/snigdha/IdeaProjects/ninestars-crawler/output_asahi_html_files"; // Update this path
        String outputDirectoryPath = "/home/snigdha/IdeaProjects/ninestars-crawler/output_asahi_text_files"; // Update this path

        // Create output directory if it doesn't exist
        new File(outputDirectoryPath).mkdirs();

        // List all HTML files in the input directory
        File inputDirectory = new File(inputDirectoryPath);
        File[] htmlFiles = inputDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".html"));

        if (htmlFiles != null) {
            for (File htmlFile : htmlFiles) {
                try {
                    // Load the HTML file
                    Document document = Jsoup.parse(htmlFile, "UTF-8");

                    // Extract text from the HTML document
                    String text = document.body().text();

                    // Create a corresponding text file
                    String textFileName = htmlFile.getName().replace(".html", ".txt");
                    File textFile = new File(outputDirectoryPath, textFileName);

                    // Write the extracted text to the text file
                    try (FileWriter writer = new FileWriter(textFile)) {
                        writer.write(text);
                    }

                    System.out.println("Extracted text from: " + htmlFile.getName() + " to " + textFile.getName());
                } catch (IOException e) {
                    System.err.println("Error processing file: " + htmlFile.getName());
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("No HTML files found in the specified directory.");
        }
    }
}