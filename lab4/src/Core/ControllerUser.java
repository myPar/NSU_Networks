package Core;

import Controller.UserCommand;

import java.util.List;

// interface for handling controller messages
public interface ControllerUser {
    void handleCommand(UserCommand command);
}
