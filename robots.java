package in.ninestars.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class robots {
    private Set<String> disallowedUrls = new HashSet<>();

    public void fetchRobotsTxt(String baseUrl) throws IOException {
        String robotsUrl = baseUrl + "/robots.txt";
        HttpURLConnection connection = (HttpURLConnection) new URL(robotsUrl).openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Disallow:")) {
                    String disallowedPath = line.substring(10).trim();
                    disallowedUrls.add(baseUrl + disallowedPath);
                }
            }
        }
    }

    public boolean isAllowed(String url) {
        for (String disallowedUrl : disallowedUrls) {
            if (url.startsWith(disallowedUrl)) {
                return false; // URL is disallowed
            }
        }
        return true; // URL is allowed
    }
}
