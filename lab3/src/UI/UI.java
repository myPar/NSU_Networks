package UI;

public interface UI {
    // get API1 (place by name) input
    String getPlaceName();
    // select item from API1 output (returns index in list)
    int getPlaceIdx();
    // get API keys methods
    String getGHkey();
    String getOTkey();
    String getOWkey();
}
