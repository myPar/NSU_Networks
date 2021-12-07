package JSONconvertion.classes;

import java.util.ArrayList;

// describes list of geography points with specified names and types
public class NamedPoints {

    public class Hit{
        public int osm_id;
        public String country;
        public String osm_key;
        public String osm_value;
        public String name;
        public Point point;
    }

    public class Place {
        public ArrayList<Hit> hits;
    }
}
