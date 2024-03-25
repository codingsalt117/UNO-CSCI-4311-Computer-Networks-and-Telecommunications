import java.io.*;
import java.net.*;
import java.util.*;
/**
 *
 * @author Robin Johnson
 */

public class MyServer {
    
    private Socket socket = null;
    private ServerSocket server = null;
    private ArrayList<String> gamePop;
    
    //server constructor
    public MyServer(int port){
        try{
            //create serversocket at passed port number
            this.server = new ServerSocket(port);
        }
        catch(Exception io){
           System.out.println("Error in server const: " + io.getMessage());
        }//end of try/catch
    }//end of constructor
    
    //method to start the server
    public void serverStart(){
        try{
                System.out.println("Server started");
                System.out.println("To play a game of Hangman.Format message in the following manner.");
                System.out.println("\tLets play Hangman");
                System.out.println("Waiting on clients");            
            // keep server running until told to shut down
            //System.out.println(server);
            while(!server.isClosed()){
                //wait for client to connect

                Socket socket = server.accept();
                System.out.println("A client has connected to chat\nWelcome to the chat!");
                // creating instance of a client to give to a thread
                ClientHandler clhand = new ClientHandler(socket);
                // creating thread and passing client handler object
                Thread chThread = new Thread(clhand);
                chThread.start();
            }//end of while
        }
        catch(Exception io){
            System.out.println("Error in serverStart: " + io.getMessage());
        }//end of try/catch
    }//end of serverStart
    
    public void shutdown(){
        try{
            //checking if serversocket is null to avoid nullpointer
            if(server != null){
                server.close();
            }
        }
        catch(Exception io){
            System.out.println("Error in shutdown: " + io.getMessage());
        }//end of try/catch
    }//end of shutdown
    
    public static void main(String[] args){
        try{
            MyServer newServer = new MyServer(5010);
            newServer.serverStart();
        }
        catch(Exception e){
            System.out.println("Error in server main: " + e.getMessage());
        }//end of try/catch
    }//end of main
}//end of class
