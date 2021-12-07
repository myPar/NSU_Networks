package Core;

import JSONconvertion.classes.NamedPoints.Hit;
import JSONconvertion.classes.PlacesDescription.PlaceDescription;
import JSONconvertion.classes.PlacesWithId;
import JSONconvertion.classes.WeatherDescription;
import UI.UI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import Core.APIRequester.RequesterException.ExceptionType;
import javafx.concurrent.Task;

public class APIRequester {
    public static class RequesterException extends Exception {
        enum ExceptionType {
            API_INPUT("API INPUT"),
            API_RESPONSE("API RESPONSE"),
            SOFTWARE("SOFTWARE"),
            HTTP_REQUEST("HTTP REQUEST");
            private String value;
            ExceptionType(String value) {this.value = value;}

            String getValue() {return value;}
        }
        private ExceptionType type;
        private String message;

        RequesterException(ExceptionType t, String m) {
            type = t;
            message = m;
        }
        String getExceptionMessage() {
            return "Exception of type " + type.getValue() + ". " + message;
        }
    }

    private UI ui;
    private String graphHooperAPIkey;
    private String openTripMapAPIkey;
    private String openWeatherMapAPIkey;
    private int readTimeout;
    private int connectTimeout;

    public APIRequester(UI ui) {
        this.ui = ui;
    }
    // init API keys
    private void config() {
        graphHooperAPIkey = ui.getGHkey();
        openTripMapAPIkey = ui.getOTkey();
        openWeatherMapAPIkey = ui.getOWkey();
        readTimeout = 1000;
        connectTimeout = 1000;
    }
    public void execute() {
        config();
        Tasks.config(graphHooperAPIkey, openTripMapAPIkey, openWeatherMapAPIkey, connectTimeout, readTimeout);

        while(true) {
            try {
                String placeName = ui.getPlaceName();
                ArrayList<Hit> places;

                // get possible places
                places = Tasks.getPlacesByName(placeName);
                // display places
                ui.displayPlaces(places);
                // get place selected by user
                int idx = ui.getPlaceIdx();
                Hit hit = places.get(idx);
                // get nearest places:
                int radius = ui.getRadius();
                if (radius < 0) {   // check radius value
                    throw new RequesterException(ExceptionType.API_INPUT, "negative radius-" + radius);
                }
                int limit = ui.getLimit();
                if (limit < 0) {    // check limit
                    throw new RequesterException(ExceptionType.API_INPUT, "negative limit-" + limit);
                }
                // get places within a radius from the selected place
                ArrayList<PlacesWithId.Feature> nearPlaces = Tasks.getNearestPlaces(hit.point, radius, limit);
                ui.displayPlacesWithinRadius(nearPlaces);   // display places

                // supply asynch tasks for each place:
                int tasksCount = nearPlaces.size();
                ArrayList<CompletableFuture<PlaceDescription>> placesDescriptionTasks = new ArrayList<>(tasksCount);
                ArrayList<CompletableFuture<WeatherDescription.Root>> placesWeatherTasks = new ArrayList<>(tasksCount);

                for (PlacesWithId.Feature place: nearPlaces) {
                    // supply async - getting places description, and add CompletableFuture to list
                    placesDescriptionTasks.add(CompletableFuture.supplyAsync(()-> {
                        PlaceDescription result = null;
                        try {
                            result = Tasks.getPlaceDescription(place.xid, Tasks.Language.RU);
                        } catch (RequesterException e) {
                            ui.displayException(e);
                        }
                        return result;
                    }));
                    // supply async - getting places weather, and add CompletableFuture to list
                    placesWeatherTasks.add(CompletableFuture.supplyAsync(() -> {
                        WeatherDescription.Root result = null;
                        try {
                            result = Tasks.getPlaceWeather(place.point);
                        }
                        catch(RequesterException e) {
                            ui.displayException(e);
                        }
                        // if exception was thrown result is equal to null
                        return result;
                    }));
                }
                // collect Task's results:
                for (int i = 0; i < tasksCount; i++) {
                    PlaceDescription description;
                    WeatherDescription.Root weather;
                    try {
                        description = placesDescriptionTasks.get(i).get();
                        weather = placesWeatherTasks.get(i).get();
                    }
                    catch(Exception e) {
                        throw new RequesterException(ExceptionType.SOFTWARE, "Task for place " + i + " was interrupted");
                    }
                    ui.displayWeather(nearPlaces.get(i), weather);
                    ui.displayDescription(nearPlaces.get(i), description);
                }
            } catch (RequesterException e) {
                ui.displayException(e);
            }
        }
    }
}
