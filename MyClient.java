import java.io.*;
import java.net.*;
import java.util.*;
/**
 *
 * @author Ronbin Johnson
 */
public class MyClient {
    private Socket myCS = null;
    private String name;
    private BufferedReader inPut;
    private BufferedWriter outPut;
    private static String gameMsg = "";
    private static ArrayList<MyClient> aClient = new ArrayList<>();
    
    public MyClient(Socket sock, String str){
        try{
            this.myCS = sock;
            // for outputing characters in msgs
            this.outPut = new BufferedWriter(new OutputStreamWriter(myCS.getOutputStream()));
            // for reading in characters from msgs
            this.inPut = new BufferedReader(new InputStreamReader(myCS.getInputStream()));
            this.name = str;
            this.gameMsg = "";
            aClient.add(this);
            
        }
        catch(Exception i){
           System.out.println("Error in MyC const: " + i.getMessage());
        }//end of try/catch
    }//end of constructor
    
    //method for client to send msg to ClientHandler
    public void sendMsg(){
        try{
          //print username to chat
          outPut.write(this.name);
          outPut.newLine();
          outPut.flush();
          //set up/sending of acutal client chat message
          Scanner scn = new Scanner(System.in);
          // only want this to execute while connected
          while(myCS.isConnected()){
                String msg = scn.nextLine();
                setGameMsg(msg);
                //System.out.println(msg);
                outPut.write(this.name + "~ " + msg);
                outPut.newLine();
                outPut.flush();
                //Closes connect(logOff) with keyword
                if(msg.equalsIgnoreCase("bye")){                 
                    clientLogOff(this.myCS, this.outPut, this.inPut);
                }    
            }  
        }
        catch(Exception e){
            System.out.println("Error in sendMsg: " + e.getMessage());
        }//end of try/catch        
    }//end of sendMsg const
    
    //method for client to listen for messages. needs to be on its own thread, blocking operation
    public void monitorChat(){
        //abstract calling of a thread object
        new Thread(new Runnable(){
            @Override
            public void run(){
                String chatMsg;
                //want to do this while connected
                while(myCS.isConnected()){
                    try{
                    //getting msg from inPut(from chat)
                    chatMsg = inPut.readLine();
                    if (chatMsg.equals("you need to get out of here")){
                        System.exit(0);
                    }
                    System.out.println(chatMsg);
                    }
                    catch(Exception e){
                        System.out.println("Error in sendMsg: " + e.getMessage());
                    }//end of try/catch
                }    
            }//end of run
        //because this is abstract and need to run thread
        }).start(); // end of thread    
    }// end of monitorchat
    
    //method for closing all that is needed for disconnecting
    public void clientLogOff(Socket s, BufferedWriter bw, BufferedReader br){
        try{
            if(br != null){
                br.close();
            }
            if(bw != null){
                bw.close();
            }
            if(s != null){
                s.close();
            }
        }
        catch(Exception io){
                System.out.println("Error in sendOutMsg: " + io.getMessage());
        }//end of try/catch        
    }//end of clientlogoff
    
    //Method for get input from client for assigning name
    public static String userName(){
        Scanner getName = new Scanner(System.in);
        System.out.println("Enter display name for chat");
        String name = getName.nextLine();
        return name;        
    }//end of username
    public void setGameMsg(String string){
        this.gameMsg = string; 
    }
    public String getGameMsg(){
        return this.gameMsg;
    }
    public String getClientName(){
        return this.name;
    }
    public static MyClient getClient(String name){
        MyClient tempClient = null;
        for (MyClient clt : aClient){
            if(name.equals(clt.getClientName())){
               tempClient =clt;
               break;
            }
        }
        return tempClient;
    }
    public Socket getClientSocket(){
        return this.myCS;
    }
       
    public static void main(String[] args) throws IOException{
        String disName;
        //get the clients user name
        disName = userName();
        //check for empty
        if(disName == null || disName.length() == 0){
            System.out.println("Name cannot be empty. Goodbye!");
            System.exit(0);
        }
        //create connection
        Socket newSocket = new Socket("localhost", 5010);
        //create a client with socket and client name
        MyClient newClient = new MyClient(newSocket, disName);
        //these both employ blocking operations, seperate threads, running at same time
        newClient.monitorChat();
        newClient.sendMsg();    
    }
}//end of class
