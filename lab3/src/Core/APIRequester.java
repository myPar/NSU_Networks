package Core;

import UI.UI;

public class APIRequester {
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

        }
    }
}
