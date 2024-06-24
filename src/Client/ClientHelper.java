package src.Client;

import java.io.*;

public class ClientHelper {
    private BufferedReader in;
    private PrintWriter out;

    public ClientHelper(BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
    }

    /**
     * Send a request to the server
     * @param request the request to send 
     * @return the response from the server as a string
     */
    public String sendRequest(String request) {
        out.println(request);
        try {
            System.out.println("Request sent: " + request);
            StringBuilder response = new StringBuilder();
            String line;
            while (!(line = in.readLine()).isEmpty()) {
                response.append(line).append("\n");
            }
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Close the input and output streams
     * 
     */
    public void close() {
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    

}

