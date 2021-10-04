package Structures;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

// View host table data class
public class View {
    // initial text
    private String header;
    // key - host key; value - host text description
    private HashMap<String, String> displayedHostsMap;
// GUI components
    // text area for displaying text field
    private JTextArea area;
    // main frame
    private JFrame frame;

    // constructor
    public View(String initialText) {
        header = initialText;
        displayedHostsMap = new HashMap<>();

        // init GUI  components
        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        frame.setBackground(Color.white);
        frame.setLayout(null);

        area = new JTextArea();
        frame.add(area);

        area.setEditable(false);
        area.setLineWrap(true);
        area.setBounds(50, 50, 400, 400);
        area.setWrapStyleWord(true);
        area.setText(header);
    }
// get text representation method
    private String getText() {
        String text = header + "\n";

        Iterator<Entry<String, String>> iterator = displayedHostsMap.entrySet().iterator();
        // iterate over map
        while (iterator.hasNext()) {
            Entry<String, String> item = iterator.next();
            text += (item.getValue() + "\n");
        }
        return text;
    }

// public methods for text updating and displaying:
    // new host was connected
    public void displayHostAdd(String ipAddress, long id) {
        String key = ipAddress + id;
        String hostData = "Host ip: " + ipAddress + " app id: " + id;
        // put new item in map (key - host data; value - host description)
        displayedHostsMap.put(key, hostData);

        // update text in the text area
        area.setText(getText());
        // repaint
        area.repaint();
    }
    // host was removed
    public void displayHostRemove(String hostKey) {
        assert displayedHostsMap.containsKey(hostKey);
        // remove host from displayedMap
        displayedHostsMap.remove(hostKey);

        // update text in the text area
        area.setText(getText());
        // repaint
        area.repaint();
    }
// view GUI
    public void viewGUI() {
        frame.setVisible(true);
    }
}
