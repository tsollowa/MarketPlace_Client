import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 *class that connects to the server and sends commands to it
 * @author Thomas Solloway
 * @version 4/14/2025
 */

public class SLClient implements ClientInterface {

    private Socket socket; //represents the connection to the server
    private BufferedReader input; //reads the input from the server
    private PrintWriter output; //writes the output to the server

    private final String serverAddress;//the address of the server
    private final int serverPort; //the port of the server
    private static final int CONNECTION_TIMEOUT = 5000; //5 seconds

    private String currentUser; //the current user
    private boolean connected; //determines if the client is connected to the server

    public SLClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public SLClient() {
        this("localhost", 12345); //corresponding port number
    }

    @Override
    public boolean connect() { //tries to connect to the server with socket
        if (connected) return true;

        try {
            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(serverAddress, serverPort), CONNECTION_TIMEOUT);

            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            String response = input.readLine();
            if (response != null && response.startsWith("SUCCESS")) {
                connected = true;
                return true;
            }

        } catch (SocketTimeoutException e) {
            System.err.println("Connection timed out after " + CONNECTION_TIMEOUT + "ms");
        } catch (IOException e) { //if the connection fails, the client is disconnected
            System.err.println("Connection failed: " + e.getMessage());
        }

        disconnect();
        return false;
    }

    @Override //disconnects the client from the server
    //also closes all I/O streams and the socket plus resets the connected and currentUser
    public void disconnect() {
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error while closing connection: " + e.getMessage());
        } finally {
            connected = false;
            currentUser = null;
        }
    }

    @Override
    public boolean login(String username, String password) {
        //checks if the client is connected to the server

        if (!isConnected()) return false;

        try { //sends the login command to the server
            output.println("LOGIN " + username + " " + password);
            String response = input.readLine();

            if (response != null && response.startsWith("SUCCESS")) {
                currentUser = username;
                return true;
            }

        } catch (IOException e) { //if the login fails, the client is disconnected
            System.err.println("Login failed: " + e.getMessage());
            disconnect();
        }

        return false;
    }

    @Override //creates a new user
    public boolean createUser(String username, String password) {
        if (!isConnected()) return false;

        try {
            output.println("CREATE_USER " + username + " " + password);
            String response = input.readLine();
            return response != null && response.startsWith("SUCCESS");

        } catch (IOException e) { //if the user creation fails, the client is disconnected
            System.err.println("User creation failed: " + e.getMessage());
        }

        return false;
    }

    @Override //posts an item for sale
    public boolean postItem(String title, String description, double price, String sellerUsername) {
        if (!isConnected() || !sellerUsername.equals(currentUser)) return false;

        try {
            output.println("POST_ITEM " + title + "|" + description + "|" + String.format("%.2f", price) + "|" + sellerUsername);
            String response = input.readLine();
            return response != null && response.startsWith("SUCCESS");

        } catch (IOException e) { //if the item posting fails, the client is disconnected
            System.err.println("Failed to post item: " + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean buyItem(String buyerUsername, String itemId) {
        if (!isConnected() || !buyerUsername.equals(currentUser)) return false;

        try {
            output.println("BUY_ITEM " + itemId + " " + buyerUsername);
            String response = input.readLine();
            return response != null && response.startsWith("SUCCESS");

        } catch (IOException e) { //if the purchase fails, the client is disconnected
            System.err.println("Purchase failed: " + e.getMessage());
        }

        return false;
    }

    @Override //sends a message to a user
    public boolean sendMessage(String senderUsername, String receiverUsername,
                               String itemId, String body) {
        if (!isConnected() || !senderUsername.equals(currentUser)) return false;

        try {
            output.printf("SEND_MSG %s|%s|%s|%s%n", senderUsername, receiverUsername, itemId, body);
            String response = input.readLine();
            return response != null && response.startsWith("SUCCESS");

        } catch (IOException e) { //if the message sending fails, the client is disconnected
            System.err.println("Message send failed: " + e.getMessage());
        }

        return false;
    }

    @Override
    public ArrayList<ItemListing> searchItems(String keyword) {
        ArrayList<ItemListing> results = new ArrayList<>();
        if (!isConnected()) return results;

        try {
            output.println("SEARCH " + keyword);

            String line;
            while ((line = input.readLine()) != null && !line.equals("END")) {
                ItemListing item = parseItemListing(line);
                if (item != null) results.add(item);
            }

        } catch (IOException e) { //if the search fails, the client is disconnected
            System.err.println("Search failed: " + e.getMessage());
        }

        return results;
    }

    private ItemListing parseItemListing(String data) { //parses the item listing
        String[] parts = data.split("\\|");
        if (parts.length != 6) return null;

        try {
            return new ItemListing(parts[0], parts[1], parts[2],
                    Double.parseDouble(parts[3]), parts[4], Boolean.parseBoolean(parts[5]));
        } catch (NumberFormatException e) { //if the item listing is malformed, the client is disconnected
            return null;
        }
    }

    @Override
    public boolean isConnected() {
        return connected && socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public String getResponse() {
        String response = null;
        
        try {
            if (input != null) { //ensures that the input has a value
                response = input.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error reading response: " + e.getMessage());
        }
        
        return response;
    }
}
