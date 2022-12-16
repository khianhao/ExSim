import java.util.*;
import java.lang.Math;

public class Book {
    
    //TODO onto GitHub
    
    private String mySec;
    private HashMap<Integer, Order> myOrdersMap; //maps from OrderID to Order. Includes both ACTIVE and INACTIVE orders.
    private int myDivisor = 100; //for 100, this means 2 dp price supported, so a price of 10.00 will use internal representation of 1000 here
    private HashMap<Integer, LinkedList<Order>> myBidsMap; //at each price level, there is a linked list of ACTIVE orders. INACTIVE orders should be deleted.
    private HashMap<Integer, LinkedList<Order>> myOffersMap; //at each price level, there is a linked list of ACTIVE orders.  INACTIVE orders should be deleted.
    private String myCachedMarketDepthJSONString = "";
    
    public Book(String sec) {
        mySec = sec;
        myOrdersMap = new HashMap<Integer,Order>();
        myBidsMap = new HashMap<Integer, LinkedList<Order>>();
        myOffersMap = new HashMap<Integer, LinkedList<Order>>();
    }
    
    private int getBestBid() throws Exception{
        if (myBidsMap.size() == 0) {
            return 0; //no bids around, return some small number
        } 
        Integer bestBid = Collections.max(myBidsMap.keySet());
        LinkedList<Order> aLinkedList = myBidsMap.get(bestBid);
        if (aLinkedList.size() == 0) {
            throw new Exception ("getBestBid aLinkedList.size() == 0. Expecting at least some orders here.");
        }
        Order aOrder = aLinkedList.peek();
        if (aOrder.getStatus() == Order.OrderStatus.INACTIVE || aOrder.getSizeRemain() == 0) {
            throw new Exception ("getBestBid aOrder.getStatus == Order.OrderStatus.INACTIVE || aOrder.getSizeRemain == 0.");
        }
        return bestBid.intValue();
    }
    
    private int getBestOffer() throws Exception {
        if (myOffersMap.size() == 0) {
            return 999999999; //no offers around, return some big number
        }
        Integer bestOffer = Collections.min(myOffersMap.keySet());
        LinkedList<Order> aLinkedList = myOffersMap.get(bestOffer);
        if (aLinkedList.size() == 0) {
            throw new Exception ("getBestOffer aLinkedList.size() == 0. Expecting at least some orders here.");
        }
        Order aOrder = aLinkedList.peek();
        if (aOrder.getStatus() == Order.OrderStatus.INACTIVE || aOrder.getSizeRemain() == 0) {
            throw new Exception ("getBestOffer aOrder.getStatus() == Order.OrderStatus.INACTIVE || aOrder.getSizeRemain() == 0.");
        }
        return bestOffer.intValue();
    }
    
    private int getQtyAtPrice(String action, int Price) throws Exception {
                
        LinkedList<Order> linkedList; 
        
        if (action == "BUY") {
            linkedList = myBidsMap.get(Price);
        } else if (action == "SELL") {
            linkedList = myOffersMap.get(Price);
        } else {
            throw new Exception ("getQtyAtPrice action " + action + " invalid. Should be BUY|SELL.");
        }
        
        if (linkedList == null) { //no orders at that level
            return 0;
        }

        int qty = 0;
        for (Order aOrder: linkedList) {
            if (aOrder.getStatus() == Order.OrderStatus.INACTIVE) {
                throw new Exception ("getQtyAtPrice aOrder.getStatus() == Order.OrderStatus.INACTIVE.");
            }
            qty += aOrder.getSizeRemain();
        }
        
        return qty;
    }
    
    public String handleDataRequest() throws Exception {

        if (!myCachedMarketDepthJSONString.equals("")) { //cache not invalided yet, still valid, just return same;
            return myCachedMarketDepthJSONString;
        }

        int bestBid = getBestBid();
        int bestOffer = getBestOffer();
        
        if (bestBid == 0 || bestOffer == 999999999) {
            return "{}"; //no market yet
        }
        
        JSONBuilder builder = new JSONBuilder();
        
        float B1P = ((float)bestBid) / myDivisor;
        builder.put("B1P", B1P);
        builder.put("B1S", getQtyAtPrice("BUY", bestBid));
        
        float B2P = ((float)bestBid - 1)/ myDivisor;
        builder.put("B2P", B2P);
        builder.put("B2S", getQtyAtPrice("BUY", bestBid-1));
        
        float B3P = ((float)bestBid - 2)/ myDivisor;
        builder.put("B3P", B3P);
        builder.put("B3S", getQtyAtPrice("BUY", bestBid-2));
        
        float O1P = ((float)bestOffer) / myDivisor;
        builder.put("O1P", O1P);
        builder.put("O1S", getQtyAtPrice("SELL", bestOffer));
        
        float O2P = ((float)bestOffer+1) / myDivisor;
        builder.put("O2P", O2P);
        builder.put("O2S", getQtyAtPrice("SELL", bestOffer+1));
        
        float O3P = ((float)bestOffer+2) / myDivisor;
        builder.put("O3P", O3P);
        builder.put("O3S", getQtyAtPrice("SELL", bestOffer+2));
        
        myCachedMarketDepthJSONString = builder.toMarketDepthJSONString(); //also store away as cache
        return myCachedMarketDepthJSONString;
        
    }
    
    private void fill(Order aOrder, int price) throws Exception {
        
        String action = aOrder.getAction();
        
        HashMap<Integer, LinkedList<Order>> map;
        if (action.equals("BUY")) { //if this order is buying, then will match against Offer orders
            map = myOffersMap;
        } else if (action.equals("SELL")) {
            map = myBidsMap;
        } else {
            throw new Exception ("fill unknown action " + action + " . Should be BUY|SELL.");
        }
        
        LinkedList<Order> aLinkedList = map.get(Integer.valueOf(price));
        
        if (aLinkedList == null) { //no existing orders left to match against.
            return;
        }
        
        if (aLinkedList.size() == 0) { // should not be this case. If size was 0, should have removed the price level LinkedList already
            throw new Exception ("fill aLinkedList.size() == 0");
        }
        
        Order existingOrder = aLinkedList.peek();
        
        if (existingOrder.getStatus() == Order.OrderStatus.INACTIVE) {
            throw new Exception ("fill existingOrder.getStatus() == Order.OrderStatus.INACTIVE");
        }
        
        if (existingOrder.getSizeRemain() == 0) {
            throw new Exception ("fillexistingOrder.getSizeRemain() == 0");
        }
        
        aOrder.fill(existingOrder);
        
        if (existingOrder.getSizeRemain() == 0) {
            aLinkedList.remove(existingOrder); //remove existing order
            if (aLinkedList.size() == 0) { //if there are no more orders at this price level, remove the price level from the map too
                map.remove(price);            }
        }
        
        if (aOrder.getSizeRemain() == 0) {
            return; //this order is completely filled, can return

        } else { //continue recursing
            fill(aOrder, price);
        }
        
    }
    
    public String handleTradeRequest(int orderID, String action, String sec, float price, int size) throws Exception{ //returns "OK" if trade working
    
        myCachedMarketDepthJSONString = ""; //invalidate the cache
        
        if (size <= 0) {
            throw new Exception ("Size " + Integer.toString(size) + " invalid. Should be positive non-zero integer.");
        }
        
        //TODO should throw exception if there is remainder when rounding, meaning price param was not in correct step size
        int integerPrice = Math.round(price * myDivisor);
        
        int bestBid = getBestBid();
        int bestOffer = getBestOffer();
        
        //This is for simplicity in implementation and avoids the situation where an order keeps wiping out many levels.
        if (action.equals("BUY")) {
            if (integerPrice > bestOffer) {
                integerPrice = bestOffer; //If buy order price >= BestOffer, price set to BestOffer
            }
        } else if (action.equals ("SELL")) {
            if (integerPrice < bestBid) { //If sell order price <= BestBid, price set to BestBid
                integerPrice = bestBid;
            }
        } else {
            throw new Exception ("action " + action + " is not valid. Should be BUY|SELL.");
        }
        
        Order aOrder = new Order(orderID, action, sec, integerPrice, myDivisor, size);
        myOrdersMap.put(Integer.valueOf(orderID), aOrder);
        
        //some fills will happen in this case if someone willing to cross
        if ( (action.equals("BUY") && integerPrice == bestOffer) || ((action.equals ("SELL")) && integerPrice == bestBid) ) {
            
            fill(aOrder, integerPrice);
            if (aOrder.getSizeRemain() == 0) { 
                //if this new order is fully filled, then can return and no need to continue inserting order. Otherwise, need to continue inserting remaining order into book
                return Integer.toString(orderID);
            }
        }
        
        //insert Order into relevant linkedlist
        if (action.equals("BUY")) {
            if (!myBidsMap.containsKey(integerPrice)) {
                LinkedList<Order> linkedList = new LinkedList<Order>();
                myBidsMap.put(integerPrice, linkedList);
            }
            LinkedList<Order> linkedList = myBidsMap.get(integerPrice);
            linkedList.add(aOrder);
            
        } else if (action.equals ("SELL")) {
            if (!myOffersMap.containsKey(integerPrice)) {
                LinkedList<Order> linkedList = new LinkedList<Order>();
                myOffersMap.put(integerPrice, linkedList);
            }
            LinkedList<Order>linkedList = myOffersMap.get(integerPrice);
            linkedList.add(aOrder);
            
        } else {
            throw new Exception ("action " + action + " is not valid. Should be BUY|SELL.");
        }
        
        return Integer.toString(orderID);
    }
    
    public String handleCheckOrderRequest(int orderID) throws Exception{  //eg {"Status":"ACTIVE", "Price":10.02, "SizeDone":5, "SizeRemain":6}
        if (!myOrdersMap.containsKey(Integer.valueOf(orderID))) {
            throw new Exception ("Unknown orderID " + Integer.toString(orderID));
        }
        Order aOrder = myOrdersMap.get(Integer.valueOf(orderID));
        return aOrder.handleCheckOrderRequest();
    }
    
    public String handleDeleteOrderRequest(int orderID) throws Exception{ //returns "OK" if deleted
    
        if (!myOrdersMap.containsKey(Integer.valueOf(orderID))) {
            throw new Exception ("Unknown orderID " + Integer.toString(orderID));
        }
        
        Order aOrder = myOrdersMap.get(Integer.valueOf(orderID));
        
        //Order might already be INACTIVE already. Just return
        if (aOrder.getStatus() == Order.OrderStatus.INACTIVE) {
            return "OK";
        }
        
        myCachedMarketDepthJSONString = ""; //invalidate the cache
        
        aOrder.handleDeleteOrderRequest();
        String action = aOrder.getAction();
        int price = aOrder.getPrice();
        
        HashMap<Integer, LinkedList<Order>> map;
        if (action.equals("BUY")) {
            map = myBidsMap;
        } else if (action.equals("SELL")) {
            map = myOffersMap;
        } else {
            throw new Exception ("handleDeleteOrderRequest unknown action " + action + " . Should be BUY|SELL.");
        }
        
        LinkedList<Order> aLinkedList = map.get(Integer.valueOf(price));
        aLinkedList.remove(aOrder); //remove order
         
        if (aLinkedList.size() == 0) { //if there are no more orders at this price level, remove the price level from the map too
            map.remove(price);
        }
        
        return "OK";
    }
    
}