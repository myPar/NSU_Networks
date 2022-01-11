package Protocol;

import java.util.List;

public class Data {
    // game config default values
    public static final int default_width = 40;
    public static final int default_height = 30;
    // in each moment there are const_food_count + food_per_player * (alive snakes count) food cells
    public static final int default_const_food_count = 1;
    public static final int default_food_per_player_count = 1;
    public static final float default_prob_food_spawn = (float) 0.1;    // probability of spawn food on free cell
    // timeouts:
    public static final int default_ping_msg_delay = 100;     // delay of sending ping messages
    public static final int default_node_disconnect_timeout = 800;   // disconnect timeout
    public static final int default_state_delay = 1000;

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
        public PlayerType type; // default player type is human
        public int score;      // player's score

        public GamePlayer(String name, int id, String ip, int port, NodeRole role, PlayerType type, int score) {
            this.name = name;
            this.id = id;
            this.ip = ip;
            this.port = port;
            this.role = role;
            this.type = type;
            this.score = score;
        }
    }
    // unchangeable game parameters class
    public static class GameConfig {
        // map w and h:
        public int width;
        public int height;
        // in each moment there are const_food_count + food_per_player * (alive snakes count) food cells
        public int const_food_count;
        public int food_per_player_count;
        public float prob_food_spawn;    // probability of spawn food on free cell
        // timeouts:
        public int ping_msg_delay;     // delay of sending ping messages
        public int node_disconnect_timeout;   // disconnect timeout
        public int state_delay;

        public GameConfig(int width, int height, int const_food_count, int food_per_player_count, float prob_food_spawn, int ping_msg_delay, int node_disconnect_timeout, int state_delay) {
            this.width = width;
            this.height = height;
            this.const_food_count = const_food_count;
            this.food_per_player_count = food_per_player_count;
            this.prob_food_spawn = prob_food_spawn;
            this.ping_msg_delay = ping_msg_delay;
            this.node_disconnect_timeout = node_disconnect_timeout;
            this.state_delay = state_delay;
        }
        // default constructor
        public GameConfig() {
            this.width = default_width;
            this.height = default_height;
            this.const_food_count = default_const_food_count;
            this.food_per_player_count = default_food_per_player_count;
            this.prob_food_spawn = default_prob_food_spawn;
            this.ping_msg_delay = default_ping_msg_delay;
            this.node_disconnect_timeout = default_node_disconnect_timeout;
            this.state_delay = default_state_delay;
        }
    }
    // game state class
    public static class GameState {
        // Coordinates or offset of snake's body cells relative to the it's head
        public static class Coord {
            public Coord(int x, int y) {
                this.x = x;
                this.y = y;
            }
            // default constructor
            public Coord() {x = 0; y = 0;}

            public int x;
            public int y;
        }
        // snake data
        public static class Snake {
            public enum SnakeState {ALIVE, ZOMBIE}

            public Snake(int player_id, List<Coord> points, Directions head_direction, SnakeState state) {
                this.player_id = player_id;
                this.points = points;
                this.head_direction = head_direction;
                this.state = state;
            }

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

        public GameState(int state_order, List<Snake> snakes, List<Coord> food_points, List<GamePlayer> players, GameConfig config) {
            this.state_order = state_order;
            this.snakes = snakes;
            this.food_points = food_points;
            this.players = players;
            this.config = config;
        }
    }
}
