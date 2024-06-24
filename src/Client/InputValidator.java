package src.Client;

import java.util.Set;
import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{6,20}$");
    private static final Set<String> CITIES = Set.of("Aosta", "L'Aquila", "Potenza", "Catanzaro", "Napoli", "Bologna", "Trieste", "Roma", "Genova", "Milano", "Ancona", "Campobasso", "Torino", "Bari", "Cagliari", "Palermo", "Firenze", "Trento", "Perugia", "Venezia");


    /**
     * Validates the given username according to the Pattern USERNAME_PATTERN.
     * @param username the username to validate
     * @return true if the username is valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty() && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validates the given password according to the Pattern PASSWORD_PATTERN.
     * @param password the password to validate
     * @return true if the password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return password != null && !password.trim().isEmpty() && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validates the given city according to the CITIES set.
     * @param city the city to validate
     * @return true if the city is valid, false otherwise
     */
    public static boolean isValidCity(String city) {
        return city != null && !city.trim().isEmpty() && CITIES.contains(city);
    }


    /**
     * Validates the given score.
     * @param score the score to validate
     * @return true if the score is valid, false otherwise
     */
    public static boolean isValidScore(int score) {
        return score >= 0 && score <= 5;
    }

    /**
     * Validates the given hotel name.
     * @param hotelName the hotel name to validate
     * @return true if the hotel name is valid, false otherwise
     */
    public static boolean isValidHotelName(String hotelName) {
        return hotelName != null && !hotelName.trim().isEmpty();
    }

}