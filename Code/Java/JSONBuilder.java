import java.util.*;
import java.lang.Math;


public class JSONBuilder {

    private TreeMap<String, Float> myMap = new TreeMap<>();
    
    public JSONBuilder() {
    }
    
    public void put(String aString, float aFloat) {
        myMap.put(aString, aFloat);
    }
    
    //eg {"Status":"ACTIVE", "Sec":"BOO", "Action":"BUY", "OrderID":5, "Price":10.02, "SizeDone":5, "SizeRemain":6}
    public String toCheckOrderString(String status, String sec, String action, int orderID, float price, int sizeDone, int sizeRemain) { 
        //TODO for now just use .2f for formatting of price. Should really be based on price divisor
        return String.format("{ \"Status\":\"%s\", \"Sec\":\"%s\", \"Action\":\"%s\", \"OrderID\":%d, \"Price\":%.2f, \"SizeDone\":%d, \"SizeRemain\":%d }", status, sec, action, orderID, price, sizeDone, sizeRemain);
    }
    
    //TODO perhaps also send back data like last trades in x minutes
    public String toMarketDepthJSONString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        
        boolean first = true;
        for (Map.Entry<String, Float> stringFloatEntry : myMap.entrySet()) {
            if (!first) {
               sb.append(", ");
            }
            first = false;
            sb.append('"');
            sb.append (stringFloatEntry.getKey());
            sb.append('"');
            sb.append (":");
            if (stringFloatEntry.getKey().strip().endsWith("S")) { //size
                sb.append(String.format("%d",Math.round(stringFloatEntry.getValue())));
            }
            else { //price
                sb.append(stringFloatEntry.getValue());
            }
            
        }
        sb.append(" }");
        
        return sb.toString();
    }

    
}