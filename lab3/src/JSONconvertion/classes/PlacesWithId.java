package JSONconvertion.classes;

import java.util.ArrayList;

public class PlacesWithId {
    public class Point {
        public double lon;
        public double lat;
    }

    public class Feature {
        public String name;
        public String xid;
        public Point point;
    }

    public class Root {
        public ArrayList<Feature> features;
    }

}
