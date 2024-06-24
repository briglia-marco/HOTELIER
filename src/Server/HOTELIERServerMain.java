package src.Server;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import src.H_U_R.Hotel;
import src.H_U_R.User;

import java.util.concurrent.ExecutorService;


public class HOTELIERServerMain {

    private volatile boolean running = true;
    private int TCP_PORT ;
    private int UDP_PORT ;
    private String CONFIG_PATH = "./src/Config/ConfigServer.json";
    private String HOTELS_PATH;
    private String USERS_PATH;
    private String UDP_ADDRESS;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private ConcurrentHashMap<String, User> users;
    private ConcurrentHashMap<String, ArrayList<Hotel>> hotels;
    private DataStore dataStore;
    private Thread dataStoreThread;

    public HOTELIERServerMain(){
        try {
            parseConfigFileServer(CONFIG_PATH, this);
            serverSocket = new ServerSocket(TCP_PORT); 
            threadPool = Executors.newCachedThreadPool(); 
            users = loadUsers(USERS_PATH);
            hotels = new ConcurrentHashMap<String, ArrayList<Hotel>>();
            dataStore = new DataStore(users, hotels, USERS_PATH, "./src/Config/newJsonHotel.json", 10000); 
            dataStoreThread = new Thread(dataStore);
            threadPool.execute(dataStore);
            dataStoreThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        HOTELIERServerMain server = new HOTELIERServerMain();
        server.start();
    }

    public void start() {
        System.out.println("Server started on port " + TCP_PORT);
        setupShutdownHook();
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                WorkerThread workerThread = new WorkerThread(clientSocket, users, hotels, HOTELS_PATH, USERS_PATH);
                threadPool.execute(workerThread);
            } catch (IOException e) {
                if (!running) {
                    // Se il server è in fase di chiusura, ignora l'eccezione
                    System.out.println("Server is shutting down, no longer accepting connections.");
                } else {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * this method is used to shutdown the server properly
     * 
     */
    private void shutdownServer() {
        try {
            dataStore.setRunning(false);
            serverSocket.close();
            threadPool.shutdown();
            if (!threadPool.awaitTermination(11, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
            System.out.println("Server shutdown complete.");
        } catch (IOException | InterruptedException e) {
            System.out.println("Error during server shutdown");
            e.printStackTrace();
        }
    }
    
    /**
     * this method is used to setup the shutdown hook
     * 
     */
    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            System.out.println("\nShutting down server...");
            shutdownServer();
        }));
    }

    /**
     * this method is used to load the users from the json file
     * @param USERS_PATH the path of the json file
     * @return a ConcurrentHashMap of the users
     */
    public static ConcurrentHashMap<String, User> loadUsers(String USERS_PATH) {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(USERS_PATH)) {
            java.lang.reflect.Type type = new TypeToken<ConcurrentHashMap<String, User>>(){}.getType();
            ConcurrentHashMap<String, User> usersBackup = gson.fromJson(reader, type);            
            return usersBackup;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * this method is used to parse the config file 
     * @param configPath the path of the config file
     * @param server the server object
     */
    public static void parseConfigFileServer(String configPath, HOTELIERServerMain server) {
        try (Reader reader = new FileReader(configPath)) {
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(reader, JsonObject.class);
            if (config.has("HOTELS_PATH")) {
                server.setHOTELS_PATH(config.get("HOTELS_PATH").getAsString());
            }
            if (config.has("USERS_PATH")) {
                server.setUSERS_PATH(config.get("USERS_PATH").getAsString());
            }
            if (config.has("UDP_PORT")) {
                server.setUDP_PORT(config.get("UDP_PORT").getAsInt());
            }
            if (config.has("UDP_ADDRESS")) {
                server.setUDP_ADDRESS(config.get("UDP_ADDRESS").getAsString());
            }
            if (config.has("TCP_PORT")) {
                server.setTCP_PORT(config.get("TCP_PORT").getAsInt());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter
     */

    public int getTCP_PORT() {
        return TCP_PORT;
    }

    public int getUDP_PORT() {
        return UDP_PORT;
    }

    public String getHOTELS_PATH() {
        return HOTELS_PATH;
    }

    public String getUSERS_PATH() {
        return USERS_PATH;
    }

    public String getUDP_ADDRESS() {
        return UDP_ADDRESS;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public ConcurrentHashMap<String, User> getUsers() {
        return users;
    }

    public ConcurrentHashMap<String, ArrayList<Hotel>> getHotels() {
        return hotels;
    }

    /**
     * Setter
     */

    public void setTCP_PORT(int tCP_PORT) {
        TCP_PORT = tCP_PORT;
    }

    public void setUDP_PORT(int uDP_PORT) {
        UDP_PORT = uDP_PORT;
    }

    public void setHOTELS_PATH(String hOTELS_PATH) {
        HOTELS_PATH = hOTELS_PATH;
    }

    public void setUSERS_PATH(String uSERS_PATH) {
        USERS_PATH = uSERS_PATH;
    }

    public void setUDP_ADDRESS(String uDP_ADDRESS) {
        UDP_ADDRESS = uDP_ADDRESS;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void setThreadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public void setUsers(ConcurrentHashMap<String, User> users) {
        this.users = users;
    }

    public void setHotels(ConcurrentHashMap<String, ArrayList<Hotel>> hotels) {
        this.hotels = hotels;
    }


}