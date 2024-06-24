package src.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import src.H_U_R.Hotel;
import src.H_U_R.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkerThread implements Runnable {

    private Socket clientSocket;
    private ConcurrentHashMap<String, User> users;
    private ConcurrentHashMap<String, ArrayList<Hotel>> hotels;
    private String HOTELS_PATH;

    public WorkerThread(Socket clientSocket, ConcurrentHashMap<String, User> users, ConcurrentHashMap<String, ArrayList<Hotel>> hotels, String HOTELS_PATH, String USERS_PATH) {
        this.clientSocket = clientSocket;
        this.users = users;
        this.hotels = hotels;
        this.HOTELS_PATH = HOTELS_PATH;
    }

    /**
     * this method is used to run the worker thread
     * 
     */
    @Override
    public void run() {
        while (true) {
            if(clientSocket == null || clientSocket.isClosed()){
                System.out.println("waiting for client connection...");
                return;
            }
            try {
                handleRequest(clientSocket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * this method is used to handle the request from the client 
     * @param clientSocket the socket of the client that made the request
     */
    public void handleRequest(Socket clientSocket) {
        try {
            System.out.println("Handling request from " + clientSocket.getInetAddress());
            if (clientSocket == null || clientSocket.isClosed()) {
                System.out.println("Socket is null or closed");
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String request = in.readLine();
            if (request == null) {
                clientSocket.close();
                System.out.println("Client closed connection");
                return;
            }

            String[] requestParts = request.split(" ");
            String requestType = requestParts[0];

            UserManager userManager = new UserManager(users);
            HotelManager hotelManager = new HotelManager(hotels, HOTELS_PATH);

            // handle the request based on the request type
            switch (requestType) {
                case "REGISTER":
                    String registerUsername = requestParts[1];
                    String registerPassword = requestParts[2];
                    String registerResponde = userManager.register(registerUsername, registerPassword);
                    out.println(registerResponde + "\n");
                    break;
                case "LOGIN":
                    String loginUsername = requestParts[1];
                    String loginPassword = requestParts[2];
                    String loginResponde = userManager.login(loginUsername, loginPassword);
                    out.println(loginResponde + "\n");
                    break;
                case "LOGOUT":
                    String logoutUsername = requestParts[1];
                    String logoutResponde= userManager.logout(logoutUsername);
                    out.println(logoutResponde + "\n");
                    break;
                case "SEARCH_HOTEL":
                    String searchHotelName = "";
                    for (int i = 1; i < requestParts.length - 1; i++) {
                        searchHotelName += requestParts[i] + " ";
                    }
                    searchHotelName = searchHotelName.trim();
                    String searchCityName = requestParts[requestParts.length - 1];
                    String searchHotelResponde = hotelManager.searchHotel(searchHotelName, searchCityName);
                    out.println(searchHotelResponde + "\n");
                    break;
                case "SEARCH_ALL_HOTELS":
                    String searchAllCityName = requestParts[1];
                    String searchAllHotelsResponde = hotelManager.searchAllHotels(searchAllCityName);
                    out.println(searchAllHotelsResponde + "\n");
                    break;
                case "INSERT_REVIEW":
                    String insertHotelName = requestParts[1] + " " + requestParts[2] + " " + requestParts[3];
                    String insertCityName = requestParts[4];
                    Float globalScore = Float.parseFloat(requestParts[5]);
                    String[] keys = new String[]{"cleaning", "position", "services", "quality"};
                    Map<String, Float> ratings = new HashMap<>(); 
                    for (int i = 0; i < 4; i++) {
                        ratings.put(keys[i], Float.parseFloat(requestParts[6 + i]));
                    }
                    String insertUsername = requestParts[10];
                    User user = users.get(insertUsername);
                    String insertReviewResponde = hotelManager.insertReview(insertHotelName, insertCityName, globalScore, ratings, user);
                    out.println(insertReviewResponde + "\n");
                    break;
                case "SHOW_MY_BADGES":
                    String showBadgesUsername = requestParts[1];
                    String showMyBadges = userManager.showMyBadges(showBadgesUsername);
                    out.println(showMyBadges + "\n");
                    break;
                default:
                    out.println("Invalid request" + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
