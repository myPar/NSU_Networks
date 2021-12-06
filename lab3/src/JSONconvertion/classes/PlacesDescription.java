package JSONconvertion.classes;

public class PlacesDescription {
    public class WikipediaExtracts {
        public String title;
        public String text;
        public String html;
    }

    public class Point {
        public double lon;
        public double lat;
    }

    public class PlaceDescription {
        public String xid;
        public String name;

        public WikipediaExtracts wikipedia_extracts;
        public Point point;
    }
}