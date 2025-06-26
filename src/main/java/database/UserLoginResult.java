package database;

public class UserLoginResult {
    private final int userId;
    private final String username;

    public UserLoginResult(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
