import java.util.*;

public class Order {
    
    public enum OrderStatus {
        ACTIVE,
        INACTIVE,
    }
    
    private int myOrderID;
    private String myAction;
    private String mySec;
    
    private int myPrice;
    private int myDivisor;
    
    private int mySizeFull;
    protected int mySizeRemain;
    
    private OrderStatus myStatus; 
        
    public Order(int orderID, String action, String sec, int price, int divisor, int sizeFull) {
        
        myOrderID = orderID;
        myAction = action;
        mySec = sec;
        myPrice = price;
        myDivisor = divisor;
        
        mySizeFull = sizeFull;
        mySizeRemain = sizeFull;
        myStatus = OrderStatus.ACTIVE;
    }
    
    public int getSizeRemain() {
        return mySizeRemain;
    }
    
    public String getAction() {
        return myAction;
    }
    
    public int getPrice() {
        return myPrice;
    }
    
    public OrderStatus getStatus() {
        return myStatus;
    }
    
    /*
    //returns the actual quantity filled
    public int fill(int fillQty) throws Exception {
        if (fillQty <= 0) {
            throw new Exception ("fill fillQty " + Integer.toString(fillQty) + " invalid. Should be non-zero positive integer.");
        }
        
        int actualFilledQty = 0;
        if (fillQty > mySizeRemain) {
            actualFilledQty = mySizeRemain;
        } else {
            actualFilledQty = fillQty;
        }
        mySizeRemain = mySizeRemain - actualFilledQty;
        
        if (mySizeRemain == 0) {
            myStatus = OrderStatus.INACTIVE;
        }
        
        return actualFilledQty;
    }
    */
    
    public void fill(Order otherOrder) throws Exception {
        
        int otherOrderSizeRemain = otherOrder.mySizeRemain;
        
        if (mySizeRemain == 0) {
            throw new Exception ("fill mySizeRemain() == 0");
        }
        if (otherOrderSizeRemain == 0) {
            throw new Exception ("fill otherOrderSizeRemain == 0");
        }
        if (myStatus == OrderStatus.INACTIVE) {
            throw new Exception ("fill myStatus == OrderStatus.INACTIVE");
        }
        if (otherOrder.getStatus() == OrderStatus.INACTIVE) {
            throw new Exception ("fill otherOrder.getStatus == OrderStatus.INACTIVE");
        }
        
        int actualFilledQty = Math.min(mySizeRemain, otherOrderSizeRemain);
        mySizeRemain = mySizeRemain - actualFilledQty;
        otherOrder.mySizeRemain = otherOrder.mySizeRemain - actualFilledQty;
        
        if (mySizeRemain == 0) {
            myStatus = OrderStatus.INACTIVE;
        }
        
        if (otherOrder.mySizeRemain == 0) {
            otherOrder.myStatus = OrderStatus.INACTIVE;
        }

    }
    
    //eg {"Status":"ACTIVE", "Sec":"BOO", "Action":"BUY", "OrderID":5, "Price":10.02, "SizeDone":5, "SizeRemain":6}
    public String handleCheckOrderRequest() { 
    
        int sizeDone = mySizeFull - mySizeRemain;
        
        JSONBuilder builder = new JSONBuilder();
        float price = ((float)myPrice)/myDivisor;
        return builder.toCheckOrderString(myStatus.name(), mySec, myAction, myOrderID, price, sizeDone, mySizeRemain);
        
    }
    
    public void handleDeleteOrderRequest() {
        myStatus = OrderStatus.INACTIVE;
    }
}