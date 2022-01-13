package Controller;

import Core.ControllerUser;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

// Game controller - handler of game commands
public class Controller implements ControllerInterface, Runnable {
    private final int queueCapacity = 20;
    private ControllerUser controllerUser;
    private boolean running = true;
    private final int sleepDeltaTime = 1;

    // Thread-safe queue of User commands
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
    private List<UserCommand> extractAllCommands() {
        LinkedList<UserCommand> dstCommandList = new LinkedList<UserCommand>();
        commandQueue.drainTo(dstCommandList);

        return dstCommandList;
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
                Thread.sleep(sleepDeltaTime);
                // controller user handles
                controllerUser.handleCommands(extractAllCommands());
            } catch (InterruptedException e) {
                System.out.println("Fatal - interrupted exception while thread sleeps in Controller's run() method");
                System.exit(1);
            }
        }
    }
}
