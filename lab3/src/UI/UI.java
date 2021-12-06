package UI;

import Core.APIRequester;
import JSONconvertion.classes.NamedPoints;
import Core.APIRequester.RequesterException;
import java.util.ArrayList;

public interface UI {
    // get API1 (place by name) input
    String getPlaceName();
    // select item from API1 output (returns index in list)
    int getPlaceIdx();
    // get radius of search near selected place
    int getRadius();

    // get API keys methods
    String getGHkey();
    String getOTkey();
    String getOWkey();
    // display places method
    void displayPlaces(ArrayList<NamedPoints.Hit> places);

    // display exception threw while app executing
    void displayException(RequesterException e);

}
