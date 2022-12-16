import java.util.*;
import java.text.SimpleDateFormat;

public class RequestHandler {
    
    //eg
    //<URL>/USERID=1;INSTR=DATA;SEC=BOO=> Returns top 3 bid price and size, top 3 ask price and size or error {"B1P":9.99, "B1S":5, ... "O1P":10.00, "O1S":6... }
    //<URL>/USERID=1;INSTR=BUY;SEC=BOO;PRICE=10.5;SIZE=2 => Returns OrderID or error
    //<URL>/USERID=1;INSTR=SELL;SEC=BOO;PRICE=10.5;SIZE=2 => Returns OrderID or error
    //<URL>/UserID=1;INSTR=CHECKORDER;ORDERID=5 => Returns order status or error
    //<URL>/UserID=1;INSTR=DELETEORDER;ORDERID=5 => Returns status or error
    //
    public static final String[] validTagsArray = new String[] {"USERID", "INSTR", "ORDERID", "SEC","PRICE", "SIZE"};
    public static final HashSet<String> validTagsSet = new HashSet<String> (Arrays.asList(validTagsArray));
    
    public static String handleRequest(String request) throws Exception{
        HashMap<String, String> requestMap = null;
        requestMap = parseRequest(request);
        
        if (!requestMap.containsKey("USERID")) {
            throw new Exception ("USERID missing. Example valid request should be something like <URL>/USERID=1;INSTR=DATA;SEC=BOO");
        }
        
        if (!requestMap.containsKey("INSTR")) {
            throw new Exception ("INSTR missing. Example valid request should be something like <URL>/USERID=1;INSTR=DATA;SEC=BOO");
        }
        
        String instr = requestMap.get("INSTR");
        if (!instr.matches("(DATA|BUY|SELL|CHECKORDER|DELETEORDER)")) {
            throw new Exception ("INSTR should be one of DATA|BUY|SELL|CHECKORDER|DELETEORDER");
        }
        
        if (instr.matches("BUY|SELL")) {
            if (!requestMap.containsKey("SEC")) {
                throw new Exception ("For BUY|SELL INSTR, need to provide SEC eg <URL>/USERID=1;INSTR=BUY;SEC=BOO;PRICE=10.5;SIZE=2");
            }
            if (!requestMap.containsKey("PRICE")) {
                throw new Exception ("For BUY|SELL INSTR, need to provide PRICE eg <URL>/USERID=1;INSTR=BUY;SEC=BOO;PRICE=10.5;SIZE=2");
            }
            if (!requestMap.containsKey("SIZE")) {
                throw new Exception ("For BUY|SELL INSTR, need to provide SIZE eg <URL>/USERID=1;INSTR=BUY;SEC=BOO;PRICE=10.5;SIZE=2");
            }
        }
        
        if (instr.matches("CHECKORDER|DELETEORDER")) {
            if (!requestMap.containsKey("ORDERID")) {
                throw new Exception ("For CHECKORDER|DELETEORDER INSTR, need to provide ORDERID eg <URL>/USERID=1;INSTR=CHECKORDER;ORDERID=5");
            }
        }
        
        if (instr.equals("DATA")) {
            return handleDataRequest(requestMap);
        }
        if (instr.matches("BUY|SELL")) {
            return handleTradeRequest(requestMap);
        }
        if (instr.equals("CHECKORDER")) {
            return handleCheckOrderRequest(requestMap);
        }
        if (instr.equals("DELETEORDER")) {
            return handleDeleteOrderRequest(requestMap);
        }
        
        throw new Exception ("Unknown INSTR type " + instr + ". INSTR should be one of DATA|BUY|SELL|CHECKORDER|DELETEORDER");
        
    }
    
    public static String handleDataRequest(HashMap<String, String> requestMap) throws Exception {
        int userID = Integer.parseInt(requestMap.get("USERID"));
        String sec = requestMap.get("SEC");
                
        return Exchange.singletonExchange.handleDataRequest(userID, sec);
        //return dummyMarketData();
    }
    
    public static String handleTradeRequest(HashMap<String, String> requestMap) throws Exception {
        
        int userID = Integer.parseInt(requestMap.get("USERID"));
        String action = requestMap.get("INSTR");
        String sec = requestMap.get("SEC");
        float price = Float.parseFloat(requestMap.get("PRICE"));
        int size = Integer.parseInt(requestMap.get("SIZE"));
        
        return Exchange.singletonExchange.handleTradeRequest(userID, action, sec, price, size);
        
    }
    
    public static String handleCheckOrderRequest(HashMap<String, String> requestMap)  throws Exception {
        
        int userID = Integer.parseInt(requestMap.get("USERID"));
        int orderID = Integer.parseInt(requestMap.get("ORDERID"));
    
        return Exchange.singletonExchange.handleCheckOrderRequest(userID, orderID);

    }
    
    public static String handleDeleteOrderRequest(HashMap<String, String> requestMap) throws Exception {
        
        int userID = Integer.parseInt(requestMap.get("USERID"));
        int orderID = Integer.parseInt(requestMap.get("ORDERID"));
        
        return Exchange.singletonExchange.handleDeleteOrderRequest(userID, orderID);
        
    }
    
    /*
    public static String dummyMarketData() {
        
        JSONBuilder builder = new JSONBuilder();
        builder.put("B1P", 9.99f);
        builder.put("B1S", 5);
        builder.put("B2P", 9.98f);
        builder.put("B2S", 6);
        builder.put("B3P", 9.97f);
        builder.put("B3S", 3);
        builder.put("O1P", 10.00f);
        builder.put("O1S", 10);
        builder.put("O2P", 10.01f);
        builder.put("O2S", 6);
        builder.put("O3P", 10.02f);
        builder.put("O3S", 2);
        
        return builder.toMarketDepthJSONString();
        
    }
    */
    
    public static HashMap<String, String> parseRequest(String request) throws Exception{
        
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()) + " " + request);
        
        request = request.strip();
        if (request.charAt(0) == '/') {
            request = request.substring(1);
        }
        
        HashMap<String, String> requestMap = new HashMap<String, String>();
        
        String[] requestStrings = request.split(";");
        for (int i = 0; i < requestStrings.length; i++) {
            String tagValueStr = requestStrings[i];
            String[] tagValueStrSplit = tagValueStr.split("=");
            if (tagValueStrSplit.length != 2) {
                throw new Exception ("Tag Value Pair "+ tagValueStr + " malformed. Example valid request should be something like <URL>/USERID=1;INSTR=DATA;SEC=BOO");
            }
            String tag = tagValueStrSplit[0].toUpperCase().strip();
            if (!validTagsSet.contains(tag)) {
                throw new Exception ("Tag value " + tag + " invalid");
            }
            
            String value = tagValueStrSplit[1].toUpperCase().strip();
            requestMap.put(tag, value);
        }
        return requestMap;
    }
}