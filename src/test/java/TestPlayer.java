public class TestPlayer {
    String username;
    String password;
    Boolean observer;

    public TestPlayer(String username, String password) {
        this(username, password, false);
    }
    public TestPlayer(String username, String password, Boolean observer) {
        this.username = username;
        this.password = password;
        this.observer = observer;
    }
}
