import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
    public static void main(String[] args) throws IOException {
        final URL url = new URL("https://graphhopper.com/api/1/geocode?q=berlin&locale=de&debug=true&key=53255f48-32e5-4703-b28b-fd9b6c1c5ee3");
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setConnectTimeout(1000);
        con.setReadTimeout(1000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String inputLine;
        StringBuilder contentBuilder = new StringBuilder();

        while((inputLine=reader.readLine()) != null) {
            contentBuilder.append(inputLine);
        }
        System.out.println(contentBuilder.toString());
    }
}
