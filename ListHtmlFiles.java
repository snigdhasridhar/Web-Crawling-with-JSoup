package in.ninestars.crawler;

import java.io.File;

public class ListHtmlFiles {
    public static void main(String[] args) {
        // Specify the directory path
        String directoryPath = "/home/snigdha/IdeaProjects/ninestars-crawler/output_asahi_html_files/text.html";


        // Create a File object for the directory
        File directory = new File(directoryPath);

        // Check if the path is a directory
        if (directory.isDirectory()) {
            // List all HTML files in the directory
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".html"));
            if (files != null && files.length > 0) {
                System.out.println("HTML files in the directory:");
                for (File file : files) {
                    System.out.println(file.getAbsolutePath());
                }
            } else {
                System.out.println("No HTML files found in the directory.");
            }
        } else {
            System.out.println("The specified path is not a directory: " + directoryPath);
        }
    }
}
