package src.Client;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class HOTELIERClientMain {

    private volatile boolean running = true;
    private DatagramSocket udpSocket;
    private volatile boolean udpSocketClosedIntentionally = false;
    private String SERVER_ADDRESS;
    private int TCP_PORT ;
    private int UDP_PORT ;
    private String UDP_ADDRESS;
    private String CONFIG_PATH = "./src/Config/ConfigClient.json";
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ClientHelper clientHelper;
    private boolean loggedIn = false;
    private String currentUsername = "";


    public HOTELIERClientMain() {
        try{
            parseConfigFileClient(CONFIG_PATH, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        HOTELIERClientMain client = new HOTELIERClientMain();
        client.start();
    }

    /**
     * Starts the client.
     * Establishes a connection to the server and initializes the input and output streams.
     * Displays the menu to the user.
     */
    public void start() {
        try {
            socket = new Socket(SERVER_ADDRESS, TCP_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            openReceiverUDPConnection();
            clientHelper = new ClientHelper(in, out);
            showMenu();
            clientHelper.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * this method is used to parse the config file
     * @param configPath 
     * @param client
     */
    public static void parseConfigFileClient(String configPath, HOTELIERClientMain client) {
        try (Reader reader = new FileReader(configPath)) {
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(reader, JsonObject.class);
            if (config.has("UDP_PORT")) {
                client.setUDP_PORT(config.get("UDP_PORT").getAsInt());
            }
            if (config.has("UDP_ADDRESS")) {
                client.setUDP_ADDRESS(config.get("UDP_ADDRESS").getAsString());
            }
            if (config.has("TCP_PORT")) {
                client.setTCP_PORT(config.get("TCP_PORT").getAsInt());
            }
            if (config.has("SERVER_ADDRESS")) {
                client.setSERVER_ADDRESS(config.get("SERVER_ADDRESS").getAsString());   
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getters
     */

    public String getSERVER_ADDRESS() {
        return SERVER_ADDRESS;
    }

    public int getTCP_PORT() {
        return TCP_PORT;
    }

    public int getUDP_PORT() {
        return UDP_PORT;
    }

    public String getUDP_ADDRESS() {
        return UDP_ADDRESS;
    }

    /**
     * Setters
     * 
     */

    public void setSERVER_ADDRESS(String SERVER_ADDRESS) {
        this.SERVER_ADDRESS = SERVER_ADDRESS;
    }

    public void setTCP_PORT(int TCP_PORT) {
        this.TCP_PORT = TCP_PORT;
    }

    public void setUDP_PORT(int UDP_PORT) {
        this.UDP_PORT = UDP_PORT;
    }

    public void setUDP_ADDRESS(String UDP_ADDRESS) {
        this.UDP_ADDRESS = UDP_ADDRESS;
    }

    /**
     * Closes the UDP connection to the server.
     * 
     */
    private void closeUDPConnection() {
        udpSocketClosedIntentionally = true; // Imposta il flag su true prima di chiudere il socket
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
    }

    /**
     * Opens a UDP connection to the server.
     * 
     */
    private void openReceiverUDPConnection() {
        new Thread(() -> {
            try {
                udpSocket = new DatagramSocket(UDP_PORT);
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                while (running) {
                    udpSocket.receive(packet);
                    String response = new String(packet.getData(), 0, packet.getLength());
                    synchronized(System.out){
                    System.out.println(response);
                    }
                }
            } catch (IOException e) {
                if (!udpSocketClosedIntentionally) { // Controlla il flag prima di stampare l'errore
                    e.printStackTrace();
                }
            } finally {
                if (udpSocket != null && !udpSocket.isClosed()) {
                    udpSocket.close();
                }
            }
        }).start();
    }

    /**
     * Displays the menu to the user.
     * If the user is not logged in, the menu displays options to register, login, and exit.
     * If the user is logged in, the menu displays options to search for a hotel, search for all hotels in a city,
     * insert a review, show the user's badges, logout, and exit.
     */
    private void showMenu() {
        Scanner scanner = new Scanner(System.in);
        while (running) {
            if (!loggedIn) {
                synchronized(System.out){
                    System.out.println("1. Register\n2. Login\n3. Search Hotel\n4. Search All Hotels in City\n5. Exit\nSelect a number: ");
                }
                if (!scanner.hasNextInt()) {
                    synchronized(System.out){
                        System.out.println("ONLY NUMBER ALLOWED");
                    }
                    scanner.nextLine(); // consume the invalid input
                    continue;
                }
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1:
                        register(scanner);
                        break;
                    case 2:
                        login(scanner);
                        break;
                    case 3:
                        searchHotel(scanner);
                        break;
                    case 4:
                        searchAllHotels(scanner);
                        break;
                    case 5:
                        exit();
                        return;
                    default:
                        synchronized(System.out){
                            System.out.println("Invalid choice. Try again.");
                        }
                }
            } else {
                synchronized(System.out){
                    System.out.println("1. Search Hotel\n2. Search All Hotels in City\n3. Insert Review\n4. Show My Badges\n5. Logout\n6. Exit\nSelect a number: ");
                }
                if (!scanner.hasNextInt()) {
                    synchronized(System.out){
                        System.out.println("ONLY NUMBER ALLOWED");
                    }
                    scanner.nextLine(); // consume the invalid input
                    continue;
                }
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1:
                        searchHotel(scanner);
                        break;
                    case 2:
                        searchAllHotels(scanner);
                        break;
                    case 3:
                        insertReview(scanner);
                        break;
                    case 4:
                        showMyBadges();
                        break;
                    case 5:
                        logout();
                        break;
                    case 6:
                        exit();
                        return;
                    default:
                        synchronized(System.out){
                            System.out.println("Invalid choice. Try again.");
                        }
                }
            }
        }
    }

    /**
     * Sends a register request to the server. 
     * @param scanner the scanner object to read user input from the console
     * 
     */
    private void register(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if(!InputValidator.isValidUsername(username)){
            System.out.println("Invalid username.");
            return;
        }
        if(!InputValidator.isValidPassword(password)){
            System.out.println("Invalid password.");
            return;
        }

        String request = "REGISTER " + username + " " + password;
        String response = clientHelper.sendRequest(request);
        synchronized(System.out){
            System.out.println(response);
        }
    }

    /**
     * Sends a login request to the server.
     * @param scanner the scanner object to read user input from the console
     * 
     */
    private void login(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if(!InputValidator.isValidUsername(username)){
            System.out.println("Invalid username.");
            return;
        }
        if(!InputValidator.isValidPassword(password)){
            System.out.println("Invalid password.");
            return;
        }

        String request = "LOGIN " + username + " " + password;
        String response = clientHelper.sendRequest(request);
        synchronized(System.out){
            System.out.println(response);
        }

        if (response.equals("Login successful" + "\n")) {
            loggedIn = true;
            currentUsername = username;
        }

    }

    /**
     * Sends a logout request to the server.
     * 
     */
    private void logout() {
        String request = "LOGOUT " + currentUsername;
        String response = clientHelper.sendRequest(request);
        if (response.equals("Logout successful" + "\n")) {
            loggedIn = false;
            currentUsername = "";
        }
        synchronized(System.out){
            System.out.println(response);
        }
    }

    /**
     * Sends a search hotel request to the server.
     * @param scanner the scanner object to read user input from the console
     * 
     */
    private void searchHotel(Scanner scanner) {
        System.out.print("Enter hotel name: ");
        String nomeHotel = scanner.nextLine();
        System.out.print("Enter city: ");
        String nomeCittà = scanner.nextLine();

        if(!InputValidator.isValidHotelName(nomeHotel)){
            System.out.println("Invalid hotel name.");
            return;
        }
        if(!InputValidator.isValidCity(nomeCittà)){
            System.out.println("Invalid city.");
            return;
        }

        String request = "SEARCH_HOTEL " + nomeHotel + " " + nomeCittà;
        String response = clientHelper.sendRequest(request);
        synchronized(System.out){
            System.out.println(response);
        }
    }

    /**
     * Sends a search all hotels in city request to the server.
     * @param scanner the scanner object to read user input from the console
     * 
     */
    private void searchAllHotels(Scanner scanner) {

        System.out.print("Enter city: ");
        String nomeCittà = scanner.nextLine();

        if(!InputValidator.isValidCity(nomeCittà)){
            System.out.println("Invalid city.");
            return;
        }

        String request = "SEARCH_ALL_HOTELS " + nomeCittà;
        String response = clientHelper.sendRequest(request);
        synchronized(System.out){
            System.out.println(response);
        }
    }

    /**
     * Sends an insert review request to the server.
     * @param scanner the scanner object to read user input from the console
     * 
     */
    private void insertReview(Scanner scanner) {

        System.out.print("Enter hotel name: ");
        String nomeHotel = scanner.nextLine();
        System.out.print("Enter city: ");
        String nomeCittà = scanner.nextLine();

        if(!InputValidator.isValidHotelName(nomeHotel)){
            System.out.println("Invalid hotel name.");
            return;
        }
        if(!InputValidator.isValidCity(nomeCittà)){
            System.out.println("Invalid city.");
            return;
        }

        System.out.print("Enter global score (0-5): ");
        while (!scanner.hasNextInt()) {
            scanner.next(); // Consume invalid input
            System.out.println("Error: please enter an integer.");
            System.out.print("Enter global score (0-5): ");
        }
        int global = scanner.nextInt();
        
        System.out.print("Enter cleaning score (0-5): ");
        while (!scanner.hasNextInt()) {
            scanner.next(); // Consume invalid input
            System.out.println("Error: please enter an integer.");
            System.out.print("Enter cleaning score (0-5): ");
        }
        int cleaning = scanner.nextInt();
        
        System.out.print("Enter position score (0-5): ");
        while (!scanner.hasNextInt()) {
            scanner.next(); // Consume invalid input
            System.out.println("Error: please enter an integer.");
            System.out.print("Enter position score (0-5): ");
        }
        int position = scanner.nextInt();
        
        System.out.print("Enter services score (0-5): ");
        while (!scanner.hasNextInt()) {
            scanner.next(); // Consume invalid input
            System.out.println("Error: please enter an integer.");
            System.out.print("Enter services score (0-5): ");
        }
        int services = scanner.nextInt();
        
        System.out.print("Enter quality score (0-5): ");
        while (!scanner.hasNextInt()) {
            scanner.next(); // Consume invalid input
            System.out.println("Error: please enter an integer.");
            System.out.print("Enter quality score (0-5): ");
        }
        int quality = scanner.nextInt();
        scanner.nextLine(); // Consume the newline
        
        if (!InputValidator.isValidScore(global) ||
            !InputValidator.isValidScore(position) || 
            !InputValidator.isValidScore(cleaning) || 
            !InputValidator.isValidScore(services) || 
            !InputValidator.isValidScore(quality)) {
            System.out.println("Invalid scores. All scores must be between 0 and 5.");
            return;
        }

        String request = "INSERT_REVIEW " + nomeHotel + " " + nomeCittà + " " + global + " " + position + " " + cleaning + " " + services + " " + quality + " " + currentUsername;
        String response = clientHelper.sendRequest(request);
        synchronized(System.out){
            System.out.println(response);
        }
    }

    /**
     * Sends a show my badges request to the server.
     * 
     */
    private void showMyBadges() {
        String request = "SHOW_MY_BADGES " + currentUsername;
        String response = clientHelper.sendRequest(request);
        synchronized(System.out){
            System.out.println(response);
        }
    }

    /**
     * Closes the connection to the server.
     * 
     */
    private void exit() {
        try {
            running = false;
            loggedIn = false;
            currentUsername = "";
            closeUDPConnection(); // Chiama questo metodo per chiudere la connessione UDP
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Connection closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 

}
