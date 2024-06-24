package src.Server;

import java.util.concurrent.ConcurrentHashMap;

import src.Client.InputValidator;
import src.H_U_R.User;

public class UserManager {

    private ConcurrentHashMap<String, User> users;

    public UserManager(ConcurrentHashMap<String, User> users) {
        this.users = users;
    }

    /**
     * this method is used to register a new user
     * @param username 
     * @param password
     * @return a message to confirm the registration
     */
    public String register(String username, String password) {
        if (users.containsKey(username) || !InputValidator.isValidUsername(username)) {
            return "Username already exists or invalid";
        }
        if (!InputValidator.isValidPassword(password)) {
            return "Password not valid";
        }
        User newUser = new User(username, password, "Utente normale");
        users.put(username, newUser);
        System.out.println(users.get(username).toString());
        return "Registration successful";
    }

    /**
     * this method is used to login a user
     * @param username 
     * @param password
     * @return a message to confirm the login
     */
    public String login(String username, String password) {
        User user = users.get(username);
        if (user == null) {
            return "User not found";
        }
        if (!user.getPassword().equals(password)) {
            return "Password not valid";
        }
        user.setLoggedIn(true);
        return "Login successful";
    }

    /**
     * this method is used to logout a user
     * @param username the username of the user to logout
     * @return a message to confirm the logout
     */
    public String logout(String username) {
        User user = users.get(username);
        if (user == null || !user.isLoggedIn()) {
            return "User not found or not logged in";
        }
        user.setLoggedIn(false);
        return "Logout successful";
    }

    /**
     * this method is used to show the badges of a user
     * @param username the username of the user to show badges of
     * @return the badges of the user
     */
    public String showMyBadges(String username) {
        User user = users.get(username);
        if (user == null) {
            return "Utente non trovato";
        }
        return user.getBadge();
    }

    /**
     * this method is used to add a review count to a user
     * @param user the user to add the review count to
     */
    public static void addReviewCount(User user) {
        if (user == null) {
            return ;
        }
        user.setNumberOfReviews(user.getNumberOfReviews() + 1);
        addBadge(user);
        return ;
    }

    /**
     * this method is used to check if a user is logged in
     * @param username the username of the user to check
     * @return true if the user is logged in, false otherwise
     */
    public boolean isLoggedIn(String username) {
        User user = users.get(username);
        return user != null && user.isLoggedIn();
    }

    /**
     * this method is used to add a badge to a user if he has reached a certain number of reviews
     * @param user the user to add the badge to
     */
    public static void addBadge(User user){
        if (user == null) {
            return;
        }
        if(user.getNumberOfReviews() == 5){
            user.setBadge("Recensore");
        }
        if(user.getNumberOfReviews() == 10){
            user.setBadge("Recensore esperto");
        }
        if(user.getNumberOfReviews() == 20){
            user.setBadge("Contributore");
        }
        if(user.getNumberOfReviews() == 30){
            user.setBadge("Contributore esperto");
        }
        if(user.getNumberOfReviews() == 50){
            user.setBadge("Contributore Super");
        }
        return;
    }

}
