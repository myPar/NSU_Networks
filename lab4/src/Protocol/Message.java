package Protocol;

import java.util.List;

public class Message {
    public enum Type {PingMsg, SteerMsg, AcceptMsg, StateMsg, InitMsg, JoinMsg, ErrorMsg, ChangeRoleMsg}

    // not SERVER player asks to turn snake's head
    public static class SteerMessage extends Message {
        public final Data.Directions direction;   // rotation direction on the net step

        public SteerMessage (Data.Directions d) {direction = d; this.type = Type.SteerMsg;}
    }

    public static class PingMessage extends Message {
        public PingMessage() {this.type = Type.PingMsg;}
    }

    // accept message with the same seq
    public static class AcceptMessage extends Message {
        public AcceptMessage() {this.type = Type.AcceptMsg;}
    }

    // game state message from MASTER
    public static class StateMessage extends Message {
        public final Data.GameState state;    // current game state

        public StateMessage(Data.GameState s) {state = s; this.type = Type.StateMsg;}
    }

    // Init new game message
    public static class InitMessage extends Message {
        public final List<Data.GamePlayer> players; // players in game
        public final Data.GameConfig config;  // game parameters
        public final boolean can_join;        // can new player connect to the game (does free place consist)

        public InitMessage(List<Data.GamePlayer> p, Data.GameConfig c, boolean b) {players = p; config = c; can_join = b; this.type = Type.InitMsg;}
    }

    // ask to existing game message
    public static class JoinMessage extends Message {
        public final Data.PlayerType player_type;     // type of joining player
        public final boolean view_mode;       // if true - player in the default mode; false - view
        public final String name;                            // player's name

        public JoinMessage(Data.PlayerType t, boolean mode, String n) {player_type = t; view_mode = mode; name = n; this.type = Type.JoinMsg;}

        // default constructor
        public JoinMessage(String p_name) { player_type= Data.PlayerType.HUMAN; view_mode = false; name = p_name; this.type = Type.JoinMsg;}
    }

    // error message
    public static class ErrorMessage extends Message {
        public final String message;

        public ErrorMessage(String m) {message = m; this.type = Type.ErrorMsg;}
    }

    // role change message (further handling depends from two factors: sender role and receiver role)
    public static class ChangeRoleMessage extends Message {
        public final Data.NodeRole sender_role;
        public final Data.NodeRole receiver_role;

        public ChangeRoleMessage(Data.NodeRole s_r, Data.NodeRole r_r) {sender_role = s_r; receiver_role = r_r; this.type = Type.ChangeRoleMsg;}
    }
    // set common fields method
    public void set_common(long seq_number, int sender_id, int receiver_id) {
        this.seq_number = seq_number;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.set = true;
    }
    private boolean set = false;            // common fields set flag; false at the start
    public boolean is_set() {return set;}   // check is message common fields set

    // message sequence number, unique for sender
    public long seq_number;
    // sender id - sender's id
    public int sender_id;
    // receiver id
    public int receiver_id;
    // message type
    public Type type;
}
