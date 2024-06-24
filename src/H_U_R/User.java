package src.H_U_R;

public class User {

    private String username;
    private String password;
    private String badge;
    private int numberOfReviews;
    private boolean loggedIn;

    public User(String username, String password, String badge) {
        this.username = username;
        this.password = password;
        this.badge = badge;
        this.numberOfReviews = 0;
        this.loggedIn = false;
    }

    /*
     * Getters
     */

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getBadge() {
        if(!isLoggedIn()){
            return "Must be logged in first";
        }
        return badge;
    }

    public int getNumberOfReviews() {
        return numberOfReviews;
    }

    /*
     * Setters
     */
    
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBadge(String newBadge){
        this.badge = newBadge;
    }

    public void setNumberOfReviews(int numberOfReviews) {
        this.numberOfReviews = numberOfReviews;
    }

    @Override
    public String toString() {
        return "User{" +
            "username='" + username + '\'' +
            ", password='" + password + '\'' +
            '}';
    }

}
