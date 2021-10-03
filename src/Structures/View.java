package Structures;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

// View host table data class
public class View {
    // updating text represents group members
    private String text;
    // text header size
    private int headerSize;
    // array of strings lengths in the text
    private ArrayList<Integer> lengthArray;
    // displaying hosts map (key - host data, value - idx of string in length array)
    private HashMap<String, Integer> displayedHostsMap;
// GUI components
    // text area for displaying text field
    private JTextArea area;
    // main frame
    private JFrame frame;

    // constructor
    public View(String initialText) {
        text = initialText;
        headerSize = initialText.length();
        lengthArray = new ArrayList<>(100);
        displayedHostsMap = new HashMap<>();

        // init GUI components
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
        area.setText(text);
    }
    // adding new string at the end of the text
    private void addText(String newString) {
        int strCount = lengthArray.size();
        int maxHostsCount = 1000;
        assert strCount < maxHostsCount;
        // write new string length in length array
        int len = newString.length();
        lengthArray.add(len);
        // add new string in the end of the text
        this.text += (newString + "\n");
    }
    // removing string from the text by index in length array
    private void removeText(int idx) {
        assert idx > 0 && idx < lengthArray.size();

        // get start character index of string in idx position
        int startIdx = headerSize;
        // get end character index of string in idx position
        int endIdx;
        for (int i = 0; i < idx; i++) {
            startIdx += lengthArray.get(i) + 1; // skip current string and '\n' character
        }
        endIdx = startIdx + lengthArray.get(idx);
        // cut string by idx in lengthArray from the text
        text = text.substring(0, startIdx) + text.substring(endIdx + 1);
        // remove len of removed string
        lengthArray.remove(idx);
    }
// public methods for text updating and displaying:
    // new host was connected
    public void displayHostAdd(String ipAddress, long id) {
        String key = ipAddress + id;
        String hostData = "Host ip: " + ipAddress + " app id: " + id;
        // add host data in the text
        addText(hostData);
        // put new item in map (key - host data; value - index in length array)
        displayedHostsMap.put(key, lengthArray.size() - 1);

        // update text in the text area
        area.setText(text);
        // repaint
        area.repaint();
    }
    // host was removed
    public void displayHostRemove(String hostKey) {
        assert displayedHostsMap.containsKey(hostKey);
        // remove host data from text by index
        removeText(displayedHostsMap.get(hostKey));
        // remove host from displayedMap
        displayedHostsMap.remove(hostKey);

        // update text in the text area
        area.setText(text);
        // repaint
        area.repaint();
    }
// view GUI
    public void viewGUI() {
        frame.setVisible(true);
    }
}
