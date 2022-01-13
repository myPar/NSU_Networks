package Controller;

import Core.ControllerUser;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

// Game controller - handler of game commands
public class Controller implements ControllerInterface, Runnable {
    private final int queueCapacity = 10;
    private ControllerUser controllerUser;
    private boolean running = true;

    // Thread-safe queue of User commands
    // GUI thread - add command to queue, Controller thread remove commands and sends it to Model
    private LinkedBlockingQueue<UserCommand> commandQueue;

    @Override
    public void putUserCommand(UserCommand command) {
        try {
            commandQueue.put(command);
        } catch (InterruptedException e) {
            System.err.println("Fatal - interrupted exception while putting command in command queue");
            System.exit(1);
        }
    }

    public Controller(ControllerUser user) {
        controllerUser = user;
        commandQueue = new LinkedBlockingQueue<UserCommand>(queueCapacity);
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while(running) {
            try {
                controllerUser.handleCommand(commandQueue.take());
            } catch (InterruptedException e) {
                System.out.println("Fatal: Interupted exception while taking an item from command queue");
                System.exit(1);
            }
        }
    }
}
