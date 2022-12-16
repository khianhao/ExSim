import javaxt.http.servlet.*;

public class Main {

    public static void main(String[] args) {

        //Start the server
        try {
            int port = 9080;
            int numThreads = 50;
            javaxt.http.Server server = new javaxt.http.Server(port, numThreads, new ExSimServlet());
            
            Exchange.initialize();
            server.start();
        }
        catch (Exception e) {
            System.out.println("Server could not start because of an " + e.getClass());
            System.exit(1);
        }
    }

    //Useful info
	//https://happycoding.io/tutorials/java-server/rest-api
    //https://www.javaxt.com/javaxt-server/
    //https://www.javaxt.com/documentation?jar=javaxt-server&package=javaxt.http.servlet&class=HttpServlet
    //https://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletResponse.html
    //https://www.tabnine.com/code/java/classes/javax.servlet.http.HttpServletResponse
    
    private static class ExSimServlet extends HttpServlet {

        public void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, java.io.IOException {
            String responseText;
            try {
                String path = request.getURL().getPath();
                responseText = RequestHandler.handleRequest(path);
            } catch (Exception e) {
                response.setStatus(404);
                responseText = e.toString();
                e.printStackTrace(System.err);
                
            }
            response.write(responseText);
        }
 
    }
    
	
}

//TODO Bugs to fix
//When enough buys to wipe out the bestoffers, null pointer exception when fetching market data