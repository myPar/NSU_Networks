package UI;

import Core.APIRequester;
import JSONconvertion.classes.NamedPoints;
import JSONconvertion.classes.PlacesDescription;
import JSONconvertion.classes.PlacesWithId;
import JSONconvertion.classes.WeatherDescription;

import java.util.ArrayList;
import java.util.Scanner;

public class TextUI implements UI {
    private Scanner sc;

    public TextUI() {
        sc = new Scanner(System.in);
    }

    @Override
    public String getPlaceName() {
        System.out.println("Enter the place name:");

        return sc.nextLine();
    }

    @Override
    public int getPlaceIdx() {
        System.out.println("Enter the idx of place you want to select:");
        return sc.nextInt();
    }

    @Override
    public int getRadius() {
        System.out.println("Enter the radius of search in meters:");
        return sc.nextInt();
    }

    @Override
    public String getGHkey() {
        System.out.println("Enter the graphhoper API key:");
        return sc.nextLine();
    }

    @Override
    public String getOTkey() {
        System.out.println("Enter the opentripmap API key:");
        return sc.nextLine();
    }

    @Override
    public String getOWkey() {
        System.out.println("Enter the openweather API key:");
        return sc.nextLine();
    }

    @Override
    public void displayPlaces(ArrayList<NamedPoints.Hit> places) {
        System.out.println("found places:");
        if (places == null) {
            System.out.println("    null");
            return;
        }
        if (places.size() == 0) {
            System.out.print("  no places found");
            return;
        }
        int idx = 0;

        for (NamedPoints.Hit place: places) {
            System.out.println("idx=" + idx);
            System.out.println("    name: " + place.name);
            System.out.println("    country: " + place.country);
            System.out.println("    point: " + "lat=" + place.point.lat + " lon=" + place.point.lon);
            idx++;
        }
        System.out.println();
    }

    @Override
    public synchronized void displayException(APIRequester.RequesterException e) {
        System.err.println(e.getExceptionMessage());
    }

    @Override
    public int getLimit() {
        System.out.println("Enter the object's count limit you want to display:");
        return sc.nextInt();
    }

    @Override
    public void displayPlacesWithinRadius(ArrayList<PlacesWithId.Feature> nearPlaces) {
        System.out.println("found places near the selected one: ");

        if (nearPlaces == null) {
            System.out.println("null");
            return;
        }
        if (nearPlaces.size() == 0) {
            System.out.print("empty");
            return;
        }

        for (PlacesWithId.Feature place: nearPlaces) {
            System.out.println("    name: " + place.name);
            System.out.println("    point: " + "lat=" + place.point.lat + " lon=" + place.point.lon);
            System.out.println("    xid: " + place.xid);
        }
        System.out.println("-------------------------------");
    }

    @Override
    public void displayWeather(PlacesWithId.Feature feature, WeatherDescription.Root weather) {
        if (feature == null) {
            System.out.println("null place");
            return;
        }
        System.out.println("Weather in place " + feature.name + "(" + "lat=" + feature.point.lat + " lon=" + feature.point.lon + "):");
        if (weather == null) {
            System.out.println("    null");
            return;
        }
        System.out.println("    weather: " + weather.name);
        System.out.println("    wind speed: " + weather.wind.speed);
        System.out.println("    temperature description: ");
        System.out.println("        temperature: " + weather.main.temp);
        System.out.println("        fills like: " + weather.main.feels_like);
        System.out.println("        humidity: " + weather.main.humidity);
    }

    @Override
    public void displayDescription(PlacesWithId.Feature feature, PlacesDescription.PlaceDescription description) {
        if (feature == null) {
            System.out.println("null place");
            return;
        }
        System.out.println("Description of place " + feature.name + "(" + "lat=" + feature.point.lat + " lon=" + feature.point.lon + "):");
        if (description == null) {
            System.out.println("    null");
            return;
        }
        System.out.println(description.wikipedia_extracts.text);
        System.out.println();
    }

    @Override
    public boolean getStatus() {
        String status = sc.nextLine();
        return status.equals("exit");
    }
    public void close() {
        sc.close();
    }
}
