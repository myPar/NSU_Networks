package Controller;

public class UserCommand {
    public static class CHOOSE_GAME extends UserCommand {
        public final String selected_item;

        public CHOOSE_GAME(String selected_game) {
            this.type = Type.CHOOSE_GAME;
            this.selected_item = selected_game;
        }
    }
    public UserCommand(Type type) {
        this.type = type;
    }
    private UserCommand() {}

    public enum Type {KEY_UP, KEY_DOWN, KEY_RIGHT, KEY_LEFT, EXIT, CHOOSE_GAME, JOIN_GAME, START_NEW_GAME}
    protected Type type;

    public Type getType() {return type;}
}
