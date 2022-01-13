package Core;

import Controller.UserCommand;
import Model.Model;
import Net.ChannelProvider;
import Net.Socket;
import Protocol.Data;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

import static Protocol.Data.PlayerType.HUMAN;

// game process affects on the game model using Model interface
public class GameProcess extends Thread implements ControllerUser {
    enum Mode {SERVER_MODE, DEPUTY_MODE, SIMPLE_PLAYER_MODE, VIEWER_MODE}   // mode of the game process
    private enum State {INIT_GAME, WAIT_GAME_JOIN, GAME_RUNNING}   // game process state

    private Model gameModelInterface;
    private ChannelProvider channel;

    private Mode mode;                          // current mode
    State curState;                     // current state
    AvailableServersData serversData;           // available server's data
    private UserCommandHandler commandHandler;  // command handler
    private final String name = "player";

    // multicast messages handler

    // handles user commands which was set by Controller
    private class UserCommandHandler extends Thread {
        private UserCommand curCommand = null; // curent user command (null - if previous command was handled and no new commands came)
        private boolean running;

        // called by controller thread
        private synchronized void setCommand(UserCommand command) {
            if (curCommand != null) {
                try {
                    // wait till previous command will be processed
                    wait();
                } catch (InterruptedException e) {
                    System.err.println("Fatal: Interrupted exception in setCommand() method");
                    System.exit(1);
                }
            }
            curCommand = command;
            notify();
        }
        private synchronized void handleCommand() {
            if (curCommand == null) {
                try {
                    // wait till command will be produced by controller
                    wait();
                } catch (InterruptedException e) {
                    System.err.println("Fatal: Interrupted exception in setCommand() method");
                    System.exit(1);
                }
            }
            if (curState == GameProcess.State.INIT_GAME) {
                switch (curCommand.getType()) {
                    case JOIN_GAME:
                        String selectedServer = serversData.selectedServer;
                        if (selectedServer != null) {
                            // Join to the selected server
                            InetAddress ipAddress = null;
                            try {
                                ipAddress = InetAddress.getByName(serversData.getIp(selectedServer));
                            } catch (UnknownHostException e) {
                                System.err.println("Unknown Host exception while getting available server ip address");
                                System.exit(1);
                            }
                            int port = serversData.getPort(selectedServer);

                            try {
                                channel.sendJoinMessage(ipAddress, port, name,false, HUMAN);
                            }
                            catch (Exception e) {
                                assert e instanceof Socket.SocketException;
                                System.err.println(((Socket.SocketException) e).message);
                            }
                        }
                        break;
                    case START_NEW_GAME:
                        // init default game config
                        gameModelInterface.initNewGame(new Data.GameConfig());
                        break;
                    case CHOOSE_GAME:
                        UserCommand.CHOOSE_GAME command = (UserCommand.CHOOSE_GAME) curCommand;
                        serversData.setSelectedServer(command.selected_item);
                        break;
                    default:
                }
            }

            // command has handled; null current command and notify controller thread:
            curCommand = null;
            notify();
        }
        private void stopHandler() {
            running = false;
        }
        @Override
        public void run() {
            running = true;
            while (running) {
                handleCommand();
            }
        }
    }

    // list of available servers, to join
    private static class AvailableServersData {
        // servers item format: ip + " " + port
        private HashSet<String> servers;
        private String selectedServer;

        // constructor:
        private AvailableServersData() {
            servers = new HashSet<>();
            selectedServer = null;  // no selected server at the start
        }
        // add new server
        private void addServer(String server) { servers.add(server);}

        // remove existing server
        private void removeServer(String server) {
            assert servers.contains(server);
            servers.remove(server);

            // check is we remove selected server
            if (selectedServer != null && selectedServer.equals(server)) {
                selectedServer = null;
            }
        }
        // get selected server (returns null if it doesn't exist)
        private String getSelectedServer() {
            return selectedServer;
        }
        // set existing server as selected
        private void setSelectedServer(String selectServer) {
            assert servers.contains(selectServer);
            selectedServer = selectServer;
        }
        private String getIp(String server) {
            assert servers.contains(server);
            return server.split(" ")[0];
        }
        private int getPort(String server) {
            assert servers.contains(server);
            int result = 0;
            try {
                result = Integer.parseInt(server.split(" ")[1]);
            }
            catch (NumberFormatException e) {
                assert false;
            }
            return result;
        }
    }

    GameProcess (Model model, String groupAddress, int groupPort) {
        this.gameModelInterface = model;
        try {
            channel = new Socket(groupAddress, groupPort);
        }
        catch (IOException e) {
            System.err.println("Fatal: I/O exception while channel creation");
            System.exit(1);
        }
        this.curState = State.INIT_GAME;
        this.serversData = new AvailableServersData();
        this.commandHandler= new UserCommandHandler();
    }

    @Override
    public void handleCommand(UserCommand command) {
        commandHandler.setCommand(command);
    }


    @Override
    public void run() {
        // decide start new game or join to an existing one

        while(true) {

        }
    }
}
