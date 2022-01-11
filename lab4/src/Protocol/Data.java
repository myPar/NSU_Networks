package Protocol;

import java.util.List;

public class Data {
    // role of each node (NORMAL - simple player, MASTER - works server mode, DEPUTY - server's deputy, VIEWER - just view the game)
    public enum NodeRole {NORMAL, MASTER, DEPUTY, VIEWER}
    // default player type - HUMAN
    public enum PlayerType {HUMAN, BOT}
    // snake direction
    public enum Directions {UP, DOWN, LEFT, RIGHT}

    // game player's data class
    public static class GamePlayer {
        public String name;    // player name
        public int id;         // unique player id
        public String ip;      // sender doesn't know it's ip? so the string is empty
        public int port;       // port of player's udp socket
        public NodeRole role;  // role of the node in net topology
        public PlayerType type = PlayerType.HUMAN; // default player type is human
        public int score;      // player's score
    }
    // unchangeable game parameters class
    public static class GameConfig {
        // map w and h:
        public int width;
        public int height;
        // in each moment there are const_food_count + food_per_player * (alive snakes count) food cells
        public int const_food_count;
        public int food_per_player_count;
        public int prob_food_spawn;    // probability of spawn food on free cell
        // timeouts:
        public int ping_delay_msg;     // delay of sending ping messages
        public int node_timeout_msg;   // disconnect timeout
    }
    // game state class
    public static class GameState {
        // Coordinates or offset of snake's body cells relative to the it's head
        public static class Coord {
            public int x;
            public int y;
        }
        // snake data
        public static class Snake {
            public enum SnakeState {ALIVE, ZOMBIE}

            public int player_id;               // id of snake owner (player.id)
            public List<Coord> points;          // 0 item - coordinates of the snake's head, other - offsets relative to the snake's head
            public Directions head_direction;   // direction of snake's head
            public SnakeState state;            // current snake state
        }
        public int state_order;         // sequence number of state; unique in game session; monotonously increments
        public List<Snake> snakes;      // list of snakes in the game
        public List<Coord> food_points; // point with food coordinates
        public List<GamePlayer> players;// players list
        public GameConfig config;       // Game parameters
    }
}
