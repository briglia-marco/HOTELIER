package src.Server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import src.H_U_R.Hotel;
import src.H_U_R.User;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Map;

public class DataStore implements Runnable {
    
    private volatile boolean running = true;
    private ConcurrentHashMap<String, User> users;
    private ConcurrentHashMap<String, ArrayList<Hotel>> hotels;
    private String usersPath;
    private String hotelsPath;
    private long interval;

    public DataStore(ConcurrentHashMap<String, User> users, ConcurrentHashMap<String, ArrayList<Hotel>> hotels, String usersPath, String hotelsPath, long interval) {
        this.users = users;
        this.hotels = hotels;
        this.usersPath = usersPath;
        this.hotelsPath = hotelsPath;
        this.interval = interval;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(interval);
                saveData();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * this method is used to save the updated users and hotels in the json file
     * 
     */
    private void saveData() {
        saveUsers();
        saveHotels();
    }

    /**
     * this method is used to save the updated users in the json file
     * 
     */
    private void saveUsers() {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(usersPath)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this method is used to save the updated hotels in the json file
     */
    private void saveHotels() {
        Gson gson = new Gson();
        ConcurrentHashMap<String, ArrayList<Hotel>> tempHotels = new ConcurrentHashMap<String, ArrayList<Hotel>>();
        ArrayList<Hotel> temp = new ArrayList<Hotel>();
        Hotel[] newJson;

        fillTempHashMap(tempHotels, temp);
        mergeHotels(tempHotels);
        newJson = getHotelsArray(tempHotels);

        try (FileWriter writer = new FileWriter(hotelsPath)) {
            gson.toJson(newJson, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * this method is used to fill the temp hashmap with the hotels from the json file
     * @param tempHotels the temp hashmap to fill
     * @param temp the temp arraylist of hotels
     */
    private void fillTempHashMap(ConcurrentHashMap<String, ArrayList<Hotel>> tempHotels, ArrayList<Hotel> temp) {
        Gson gson = new Gson();
        try (JsonReader reader = new JsonReader(new FileReader(hotelsPath))) {
            reader.beginArray();
            while (reader.hasNext()) {
                try {
                    Hotel h = gson.fromJson(reader, Hotel.class);
                    temp.add(h);
                } catch (JsonSyntaxException e) {
                    continue;
                }
            }
        } catch (IOException e) {
        }

        for (Hotel h : temp) {
            ArrayList<Hotel> hotelList = tempHotels.get(h.getCity());
            if (hotelList != null) {
                hotelList.add(h);
            } else {
                hotelList = new ArrayList<Hotel>();
                hotelList.add(h);
                tempHotels.put(h.getCity(), hotelList);
            }
        }
    }

    /**
     * this method is used to merge the hotels from the temp hashmap with the hotels hashmap
     * @param tempHotels the temp hashmap to merge
     */
    private void mergeHotels(ConcurrentHashMap<String, ArrayList<Hotel>> tempHotels) {
        for (Map.Entry<String, ArrayList<Hotel>> entry : hotels.entrySet()) {
            String city = entry.getKey();
            ArrayList<Hotel> hotelList = entry.getValue();
            for (Hotel h : hotelList) {
                ArrayList<Hotel> tempHotelList = tempHotels.get(city);
                if (tempHotelList != null) {
                    for (Hotel hotel : tempHotelList) {
                        if (hotel.getName().equals(h.getName())) {
                            tempHotelList.remove(hotel);
                            break;
                        }
                    }
                    tempHotelList.add(h);
                } else {
                    tempHotelList = new ArrayList<Hotel>();
                    tempHotelList.add(h);
                    tempHotels.put(city, tempHotelList);
                }
            }
        }
    }


    /**
     * this method is used to get the hotels array from the temp hashmap
     * @param tempHotels the temp hashmap to get the hotels array from
     * @return the hotels array
     */
    private Hotel[] getHotelsArray(ConcurrentHashMap<String, ArrayList<Hotel>> tempHotels) {
        ArrayList<Hotel> newJson = new ArrayList<Hotel>();
        for (Map.Entry<String, ArrayList<Hotel>> entry : tempHotels.entrySet()) {
            ArrayList<Hotel> hotelList = entry.getValue();
            for (Hotel h : hotelList) {
                newJson.add(h);
            }
        }
        return newJson.toArray(new Hotel[0]);
    }

}