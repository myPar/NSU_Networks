package UI;
import ThreadPool.Data.DataTransferDescription;

public interface GUI {
    void displayTraverseStatus(DataTransferDescription description, int clientId);
    void displayClientConnecting(int clientId);
    void displayClientDisconnecting(int clientId);
    void displayDataTraverseSpeed(double speed, int clientId);
    void displayServerMessage(String message);
}
