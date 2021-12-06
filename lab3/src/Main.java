import JSONconvertion.classes.NamedPoints.Place;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
    public static void main(String[] args) throws IOException {
        String key1 = "53255f48-32e5-4703-b28b-fd9b6c1c5ee3";
        String key2 = "5ae2e3f221c38a28845f05b654d480bb5edc7e51e89936794e87256a";
        String key3 = "4adcfc154c66951391692079f68e9b55";
        String URL2 = "https://api.opentripmap.com/0.1/ru/places/radius?radius=10000&lon=13.38888&lat=52.51701&limit=10&apikey=5ae2e3f221c38a28845f05b654d480bb5edc7e51e89936794e87256a";
        String URL3 = "https://api.opentripmap.com/0.1/en/places/xid/R4682064?apikey=5ae2e3f221c38a28845f05b654d480bb5edc7e51e89936794e87256a";
        String URL4 = "https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}";

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
        String jsonData = contentBuilder.toString();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Class placeClass = Place.class;
        Place place = gson.fromJson(jsonData, Place.class); //throws JsonIOException
        System.out.println(gson.toJson(place));
    }
}
