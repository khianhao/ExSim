import java.util.*;

public class Exchange {
    
    private HashMap<String, Book> myBooks; //map SEC to Book
    private int myNextOrderID = 0;
    private HashMap<Integer, Book> myOrderIDMap; //map orderid to Book
    public static Exchange singletonExchange = new Exchange();
    
    public static void initialize() throws Exception {
        //TODO remove this later?
        singletonExchange.insertTestOrders();
    }
    
    public Exchange() {
        myBooks = new HashMap<String, Book>();
        myOrderIDMap = new HashMap<Integer, Book>();
        Book aBook = new Book("BOO"); //first default sec
        myBooks.put("BOO", aBook);
    }
    
    public synchronized void insertTestOrders() throws Exception {
        handleTradeRequest(0, "BUY", "BOO", 9.99f, 5); //UserID is exchange
        handleTradeRequest(0, "BUY", "BOO", 9.98f, 6);
        handleTradeRequest(0, "BUY", "BOO", 9.97f, 3);
        handleTradeRequest(0, "SELL", "BOO", 10.00f, 10);
        handleTradeRequest(0, "SELL", "BOO", 10.01f, 6);
        handleTradeRequest(0, "SELL", "BOO", 10.02f, 2);
    }
    
    public synchronized String handleDataRequest(int userID, String sec) throws Exception{
        if (!myBooks.containsKey(sec)) {
            throw new Exception ("SEC " + sec + " Unknown");
        }
        return myBooks.get(sec).handleDataRequest();
    }
    
    public synchronized String handleTradeRequest(int userID, String action, String sec, float price, int size) throws Exception {
        
        if (!action.matches("BUY|SELL")) {
            throw new Exception ("Action " + action + " invalid. Should be BUY|SELL.");
        }
        if (!myBooks.containsKey(sec)) {
            throw new Exception ("SEC " + sec + " Unknown");
        }
        int orderID = myNextOrderID;
        Book aBook = myBooks.get(sec);
        myOrderIDMap.put(orderID, aBook);
        myNextOrderID++;
        
        return aBook.handleTradeRequest(orderID, action, sec, price, size);
    }
    
    public synchronized String handleCheckOrderRequest(int userID, int orderid)  throws Exception{
        //TODO should only allow this action if orderid was created by this userID
        if (!myOrderIDMap.containsKey(Integer.valueOf(orderid))) {
            throw new Exception("OrderID " + Integer.toString(orderid) + " unknown.");
        }
        Book aBook = myOrderIDMap.get(Integer.valueOf(orderid));
        return aBook.handleCheckOrderRequest(orderid);
    }
    
    public synchronized String handleDeleteOrderRequest(int userID, int orderid)  throws Exception{
        //TODO should only allow this action if orderid was created by this userID
        if (!myOrderIDMap.containsKey(Integer.valueOf(orderid))) {
            throw new Exception("OrderID " + Integer.toString(orderid) + " unknown.");
        }
        Book aBook = myOrderIDMap.get(Integer.valueOf(orderid));
        return aBook.handleDeleteOrderRequest(orderid);
    }
    
}