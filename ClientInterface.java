import java.util.ArrayList;

/**
 * this interface defines what methods a client needs to have
 * @author Victoria Parashchak
 * @version April 15, 2025
 */

public interface ClientInterface {

    //tries to connect to the server
    //true if it worked, false if it didn't
    boolean connect();

    //closes the connection to the server
    void disconnect();

    //tries to log in a user
    //using username and password to try
    //return true if login worked, false if it didn't
    boolean login(String username, String password);

    //tries to make a new user account
    //using username and password for the new account
    //return true if it worked, false if it didn't
    boolean createUser(String username, String password);

    //tries to post a new item for sale
    //title - what the item is called
    //description - what the item is like
    //price - how much it costs
    //sellerUsername - who's selling it
    //returns true if it worked, false if it didn't
    boolean postItem(String title, String description, double price, String sellerUsername);

    //tries to buy an item
    //buyerUsername holds who's buying the item
    //temId holds which item they want to buy
    //return true if it worked, false if it didn't
    boolean buyItem(String buyerUsername, String itemId);

    //tries to send a message to another user
    //senderUsername -  who's sending the message
    //ceiverUsername - who's getting the message
    //temId - which item the message is about
    //body - what the message says
    //return true if it worked, false if it didn't
    boolean sendMessage(String senderUsername, String receiverUsername, String itemId, String body);

    //searches for items that match a keyword
    //keyword - what to search for
    //return list of items that match
    ArrayList<ItemListing> searchItems(String keyword);

    //checks if we're connected to the server
    //return true if we're connected, false if we're not
    boolean isConnected();

    //gets the response from the server
    String getResponse();

}
