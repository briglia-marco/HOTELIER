package src.H_U_R;

import java.util.*;
import com.google.gson.Gson;

public class Hotel {

    private int id;
    private String name; 
    private String description;
    private String city; 
    private String phone;
    private List<String> services;
    private double rankingRate;
    private double rate; 
    private Map<String, Float> ratings;
    private int totalRatings;
    private List<Review> reviews;


    public Hotel(int id, String name, String description, String city, String phone, List<String> services, double rate, Map<String, Float> ratings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.services = services;
        this.rankingRate = 0;
        this.rate = rate;
        this.ratings = ratings;
        this.totalRatings = 0;
        this.reviews = new ArrayList<Review>();
    }

    /*
     * Getters
     */

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCity() {
        return city;
    }

    public String getPhone() {
        return phone;
    }

    public List<String> getServices() {
        return services;
    }

    public double getRankingRate() {
        return rankingRate;
    }

    public double getRate() {
        return rate;
    }

    public Map<String, Float> getRatings() {
        return ratings;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public List<Review> getReviews() {
        return reviews;
    }


    /*
     * Setters
     */

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }
    
    public void setRate(double rate) {
        this.rate = rate;
    }

    public void setRatings(Map<String, Float> ratings) {
        this.ratings = ratings;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public void setRankingRate(double rankingRate) {
        this.rankingRate = rankingRate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    @Override
    public String toString() {
        String reviews = "";
        if(this.reviews != null) {
            for(int i=0; i<this.reviews.size(); i++) {
                reviews += this.reviews.get(i).toString();
                if(i != this.reviews.size() - 1) {
                    reviews += "\n";
                }
            }
        } else {
            reviews = "Nessuna recensione presente";
        }
        return "__________________________________________ \n" + 
                "Hotel: \n" +
                "--- ID: " + id + "\n" +
                "--- Nome: " + name + "\n" +
                "--- Descrizione: " + description + "\n" +
                "--- Città: " + city + "\n" +
                "--- Telefono: " + phone + "\n" +
                "--- Servizi: " + services + "\n" +
                "--- Valutazione Globale: " + rate + "\n" +
                "--- Valutazioni: \n" + reviews + "\n" +
                "__________________________________________";
    }

    public String toStringJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
