package src.Server;

import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import src.H_U_R.Hotel;
import src.H_U_R.User;

public class HotelManager {

    private Gson gson = new Gson();
    private ConcurrentHashMap<String, ArrayList<Hotel>> hotels;
    private String HOTELS_PATH;
    private final String UDP_ADDRESS = "localhost";
    private final int UDP_PORT = 8081;

    public HotelManager(ConcurrentHashMap<String, ArrayList<Hotel>> hotels, String HOTELS_PATH) {
        this.hotels = hotels;
        this.HOTELS_PATH = HOTELS_PATH;
    }
    
    /**
     * this method is used to search a hotel in a city by name
     * @param hotel 
     * @param city
     * @return a string with the hotel information
     */
    public String searchHotel(String hotel, String city) {
        Hotel hotelHash = findHotelInCity(hotel, city);
        if (hotelHash != null) {
            return hotelHash.toString();
        }
        try (JsonReader reader = new JsonReader(new FileReader(HOTELS_PATH))) {
            reader.beginArray();
            while (reader.hasNext()) {
                Hotel h = gson.fromJson(reader, Hotel.class);
                if (h.getCity().equals(city) && h.getName().equals(hotel)) {
                    checkIfPresentAndAdd(hotels, h);
                    return h.toString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Hotel not found";
    }

    /**
     * this method is used to search all the hotels in a city
     * @param city
     * @return a string with all the hotels in the city
     */
    public String searchAllHotels(String city) {
        try (JsonReader reader = new JsonReader(new FileReader(HOTELS_PATH))) {
            reader.beginArray();
            while (reader.hasNext()) {
                Hotel h = gson.fromJson(reader, Hotel.class);
                if (h.getCity().equals(city)) {
                    checkIfPresentAndAdd(hotels, h);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        ArrayList<Hotel> hotelList = hotels.get(city);
        if (hotelList != null) {
            Collections.sort(hotelList, Comparator.comparing(Hotel::getRankingRate).reversed());
            hotels.put(city, hotelList);
            for(int i = 0; i < hotelList.size(); i++) {
                sb.append(hotelList.get(i).toString());
                if(i != hotelList.size() - 1) {
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    /**
     * this method is used to insert a review for a hotel
     * update the hotel's rate and ratings
     * increase the review count for the user that inserted the review
     * send a notification to the client if the best hotel in the city has changed
     * @param hotelName 
     * @param cityName
     * @param globalScore global score of the review 0-5
     * @param singleScores map of the single scores of the review 0-5
     * @param user the user that inserted the review
     * @return a message to confirm the insertion
     */
    public String insertReview(String hotelName, String cityName, Float globalScore, Map<String, Float> singleScores, User user) {
        Hotel h = findHotelInCity(hotelName, cityName);
        if (h == null) {
            h = findHotelInJson(hotelName, cityName);
            if (h != null) {
                checkIfPresentAndAdd(hotels, h);
            } else {
                return "Hotel non trovato";
            }
        }

        ArrayList<Hotel> cityHotels = new ArrayList<>();
        getCityHotels(cityName, cityHotels);

        h.setRate(globalScore);
        h.setRatings(singleScores);
        h.setTotalRatings(h.getTotalRatings() + 1);
        src.H_U_R.Review review = new src.H_U_R.Review(globalScore, singleScores);
        if(h.getReviews() == null) {
            h.setReviews(new ArrayList<>());
        }
        h.getReviews().add(review);

        Collections.sort(cityHotels, Comparator.comparing(Hotel::getRankingRate).reversed());
        Hotel bestHotel = cityHotels.get(0);
        RankingAlgorithm.calculateRanking(h);

        for (int i = 0; i < cityHotels.size(); i++) {
            if (cityHotels.get(i).getName().equals(h.getName())) {
                cityHotels.set(i, h);
                break;
            }
        }
        Collections.sort(cityHotels, Comparator.comparing(Hotel::getRankingRate).reversed());
        if (bestHotel.getRankingRate() != cityHotels.get(0).getRankingRate()) {
            String message = "Il miglior hotel di " + cityName + " è " + cityHotels.get(0).getName();
            sendUDPNotificationToClient(message);
        }
        UserManager.addReviewCount(user);

        return "Recensione inserita con successo";
    }

    /**
     * this method is used to send a notification to the client using UDP
     * @param message the message to send
     */
    private void sendUDPNotificationToClient(String message) {
        try{
            DatagramSocket socket = new DatagramSocket();
            byte[] buffer = message.getBytes();
            InetAddress address = InetAddress.getByName(UDP_ADDRESS);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, UDP_PORT);
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this method is used to get all the hotels in a city from the json file and add them to the List
     * @param cityName the name of the city
     * @param cityHotels the list of hotels in the city
     */
    private void getCityHotels(String cityName, ArrayList<Hotel> cityHotels) {
        try (JsonReader reader = new JsonReader(new FileReader(HOTELS_PATH))) {
            reader.beginArray();
            while (reader.hasNext()) {
                Hotel hotel = gson.fromJson(reader, Hotel.class);
                if (hotel.getCity().equals(cityName)) {
                    cityHotels.add(hotel);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * this method is used to find a hotel in the hashmap of hotels by name and city
     * @param hotelName
     * @param cityName
     * @return the hotel if found, null otherwise
     */
    private Hotel findHotelInCity(String hotelName, String cityName) {
        List<Hotel> cityHotels = hotels.get(cityName);
        if (cityHotels != null) {
            for (Hotel hotel : cityHotels) {
                if (hotel.getName().equals(hotelName)) {
                    return hotel;
                }
            }
        }
        return null;
    }
    
    /**
     * this method is used to find a hotel in the json file by name and city
     * @param hotelName
     * @param cityName
     * @return the hotel if found, null otherwise
     */
    private Hotel findHotelInJson(String hotelName, String cityName) {
        try (JsonReader reader = new JsonReader(new FileReader(HOTELS_PATH))) {
            reader.beginArray();
            while (reader.hasNext()) {
                Hotel hotel = gson.fromJson(reader, Hotel.class);
                if (hotel.getCity().equals(cityName) && hotel.getName().equals(hotelName)) {
                    return hotel;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * this method is used to check if a hotel is already present in the list of hotels and add it if it is not
     * @param hotels the hashmap of hotels to check
     * @param h the hotel to add
     */
    private void checkIfPresentAndAdd(ConcurrentHashMap<String, ArrayList<Hotel>> hotels, Hotel h) {
        ArrayList<Hotel> hotelList = hotels.get(h.getCity());
        if (hotelList != null) {
            for (Hotel hotel : hotelList) {
                if (hotel.getName().equals(h.getName())) {
                    return;
                }
            }
            hotelList.add(h);
        } else {
            hotelList = new ArrayList<>();
            hotelList.add(h);
            hotels.put(h.getCity(), hotelList);
        }
    }



}
