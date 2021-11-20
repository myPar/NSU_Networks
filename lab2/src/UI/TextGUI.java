package UI;

import ThreadPool.Data.DataTransferDescription;

// text implementation of GUI
public class TextGUI implements GUI {
// All display methods are synchronized because GUI is used in several tasks
    // display result message of traverse data from client
    @Override
    public synchronized void displayTraverseStatus(DataTransferDescription description, int clientId) {
        System.out.print("client " + clientId + ": traverse status: ");
        System.out.println(description.getMessage());
    }
    // new client connected
    @Override
    public synchronized void displayClientConnecting(int clientId) {
        System.out.println("client " + clientId + ": paired");
    }
    // client disconnected
    @Override
    public synchronized void displayClientDisconnecting(int clientId) {
        System.out.println("client " + clientId + ": disconnected");
    }
    // display data receive speed from client
    @Override
    public synchronized void displayDataTraverseSpeed(double speed, int clientId) {
        System.out.println("client " + clientId + ": traverse speed - " + speed);
    }
    // display serve message method
    @Override
    public synchronized void displayServerMessage(String message) {
        System.out.println("SERVER: " + message);
    }
}
