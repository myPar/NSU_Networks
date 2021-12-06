package Core;

import JSONconvertion.classes.NamedPoints.Hit;
import UI.UI;

import java.io.IOException;
import java.util.ArrayList;

public class APIRequester {
    public static class RequesterException extends Exception {
        enum ExceptionType {
            API_INPUT("API INPUT"),
            API_RESPONSE("API RESPONSE"),
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

    public APIRequester(UI ui) {
        this.ui = ui;
    }
    // init API keys
    private void config() {
        graphHooperAPIkey = ui.getGHkey();
        openTripMapAPIkey = ui.getOTkey();
        openWeatherMapAPIkey = ui.getOWkey();
    }
    public void execute() {
        config();

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
            // get nearest places

            } catch (RequesterException e) {
                ui.displayException(e);
            }
        }
    }
}
