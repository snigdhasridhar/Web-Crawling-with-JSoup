package in.ninestars.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Htmlcontentsaver {
    private final String outputDirectory;

    // Constructor to set the output directory
    public Htmlcontentsaver(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        // Create the directory if it doesn't exist
        new File(outputDirectory).mkdirs();
    }

    public void saveHtmlContent(String url, String htmlContent) {
        if (htmlContent == null) {
            return; // Skip if HTML content is null
        }

        // Create a valid file name
        String fileName = url.replaceAll("https?://", "").replaceAll("[^a-zA-Z0-9]", "_") + ".html";
        File file = new File(outputDirectory, fileName); // Save in the specified directory

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(htmlContent); // Write the HTML content to the file
            System.out.println("HTML content saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error saving HTML content to file: " + e.getMessage());
        }
    }
}