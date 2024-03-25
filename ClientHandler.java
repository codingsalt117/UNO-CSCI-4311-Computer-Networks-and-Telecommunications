import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
/**
 *
 * @author Robin Johnson
 */
public class ClientHandler implements Runnable {
    public boolean inGame;
    private String cName;
    public static ArrayList<ClientHandler> serverPop = new ArrayList<>();
    public static ArrayList<String> serverNameList = new ArrayList<>();
    private Socket chSocket;   
    private BufferedReader inPut;
    private BufferedWriter outPut;
    int wrongGuess = 0;
    List<Character> allGuesses = new ArrayList<>();
    boolean plTwoWin = false;
       
    //constructor takes a socket obj
    public ClientHandler(Socket socket){
        try{
            this.inGame = false;
            //getting socket from param sent from server
            this.chSocket = socket;
            // for outputing characters in msgs
            this.outPut = new BufferedWriter(new OutputStreamWriter(chSocket.getOutputStream()));
            // for reading in characters from msgs
            this.inPut = new BufferedReader(new InputStreamReader(chSocket.getInputStream()));
            //client self assigned name
            this.cName = inPut.readLine();
            if(uniqueName(cName) == true){
                serverNameList.add(cName);
            serverPop.add(this);
            sendOutMsg("Server Says: A wild " + cName + " has logged on!");               
            }
            else{
               System.out.println("Server says: Name needs to be unique, Goodbye!");
               sendToClient("Server says: Name needs to be unique, Goodbye!");
               sendToClient("you need to get out of here");
            }  
            //add this client to the arraylist tracking the server population
        }
        catch(Exception io){
           System.out.println("Error in CL const: " + io.getMessage());
        }//end of try/catch
    }//end of constructor
    
    // this method is for handling msgs while connected to server
    @Override
    public void run(){
        String msgOut;
        String challenged;
        String challenger;
        // keep going while the connection is active
        while(chSocket.isConnected()){
            if(!this.inGame){
                try{
                    msgOut = inPut.readLine();
                    //msg needs to be split from "'name'~ " to accuratly detect keyword
                   if(msgOut.split("~ ")[1].equals("AllUsers")){
                        //iterate through all client objects get getnames and print only to requester
                        for(ClientHandler name : serverPop){
                            String str = name.getName();
                            //System.out.println(str);
                            sendToClient(str);
                        }
                    }
                   //msg needs to be split from "'name'~ " to accuratly detect keyword
                   else if (msgOut.split("~ ")[1].equalsIgnoreCase("bye")){
                       //formatting the goodbye msg
                       String byeMsg = String.format("Server says: %s has left chat!", this.cName);
                       //printout for server
                       System.out.println(byeMsg);
                       //printout for client
                       sendOutMsg(byeMsg);
                   }
                   else if (msgOut.split("~ ")[1].equals("Lets play Hangman")){

                       challenger = this.cName; 
                       String againstWho = "Type the user name of player 2";
                       sendToClient(againstWho);
                       String usersName = inPut.readLine();
                       String[] getName = usersName.split("~ ");
                       challenged = getName[1];
                       sendToClient(String.format("Enter an English word for %s to guess.\n"
                               + "Be reasonable in your choice!", challenged));
                       String tempStr = inPut.readLine();
                       String[] tmpArr = tempStr.split("~ ");
                       String choosenWord = tmpArr[1];
                       String issued = String.format("%s has challenged you to a game of Hangman,\n"
                               + "get ready to guess letters one at a time.", challenger);
                       sendToOne(challenged,issued);
                       //System.out.println(choosenWord);
                       sendOutMsgTwo(challenged,String.format("%s has challenge %s to a game of Hangman\n"
                               + "Stay tuned for the result when completed", challenger, challenged));
                       this.setInGame(true);
                       hangmanGame(challenged,challenger,choosenWord);
                       this.setInGame(false);
                       getClientHandler(challenged).setInGame(false);
                       
                       if(plTwoWin){
                           sendOutMsgTwo(challenged, String.format("%s guessed %s's word!\n"
                                   + "The word was %s",challenged,challenger,choosenWord));
                       }
                       else{
                           sendOutMsgTwo(challenged, String.format("%s did not guess %s's word!\n"
                                   + "The word was %s",challenged,challenger,choosenWord));
                       }
                    }
                   else{
                       //for all other msg
                        System.out.println(msgOut);
                        sendOutMsg(msgOut);                   
                   }   
                }
                catch(Exception io){
                    System.out.println("Error in CL run: " + io.getMessage());
                    break;
                }//end of try/catch
            }
        }
    }// end of run
    public void hangmanGame(String challenged,String challenger,String choosenWord){
        wrongGuess = 0;
        BufferedReader playerTwoIn = null;
        ClientHandler clientPlTwo = getClientHandler(challenged);
        clientPlTwo.setInGame(true);
        try{
            playerTwoIn =  clientPlTwo.getInput();//new BufferedReader(new InputStreamReader(plTwoSoc.getInputStream()));
        }
        catch(Exception i){
            System.out.println("Error in CL run: " + i.getMessage());
        }
        while(true){
            printHangman(wrongGuess,challenger,challenged);                       
            if (wrongGuess >=6){
                sendToOne(challenged,String.format("You lost!\n"
                          + "The word was %s",choosenWord));
                sendOutMsgTwo(challenged, String.format("%s has lost the Hangman challenge\n!"
                              + "The word was %s",challenged, choosenWord));
                plTwoWin = false;
                    clientPlTwo.setInGame(false);
                break;
            }
            stateOfWord(choosenWord,allGuesses,challenged);
            if(!getGuess(playerTwoIn,choosenWord,allGuesses,challenged)){
                wrongGuess++; 
            }
            if(stateOfWord(choosenWord,allGuesses,challenged)){                
                sendToOne(challenged,"You won!!!");
                sendOutMsgTwo(challenged, String.format("%s has won the Hangman challenge\n!"
                               + "The word was %s",challenged, choosenWord));
                plTwoWin = true;
                clientPlTwo.setInGame(false);
                break;
            }
            else{
                sendToOne(challenged, "That letter is not present\n"
                            + "Try Again!");
            }
        }        
    }//end of hangman
    
    //method prints current game state to the participating players
    public void printHangman(int wrongGuess, String playerOne, String playerTwo){
        sendToOne(playerTwo,"Current state of game!");
        sendToOne(playerOne,"Current state of game!");
        sendToOne(playerTwo,"-------\n|     |");
        sendToOne(playerOne,"-------\n|     |");
        if(wrongGuess == 1){
            sendToOne(playerTwo, "   O");
            sendToOne(playerOne, "   O");
        }
        if(wrongGuess == 2){
            sendToOne(playerTwo, "   O\n    \\");
            sendToOne(playerOne, "   O\n    \\");
        }    
        if(wrongGuess == 3){
            sendToOne(playerTwo, "   O\n /  \\");
            sendToOne(playerOne, "   O\n /  \\");
        }
        if(wrongGuess == 4){
            sendToOne(playerTwo, "   O\n / | \\");
            sendToOne(playerOne, "   O\n / | \\");
        }
        if(wrongGuess == 5){
            sendToOne(playerTwo, "   O\n / | \\\n    \\");
            sendToOne(playerOne, "   O\n / | \\\n    \\");
        }    
        if(wrongGuess == 6){
            sendToOne(playerTwo, "   O\n / | \\\n /  \\");
            sendToOne(playerOne, "   O\n / | \\\n /  \\");
        }
        sendToOne(playerTwo, "");
        sendToOne(playerOne, "");   
    }
    
    //get a letter guess from player 2
    public boolean getGuess(BufferedReader playerTwoIn,String word,List<Character> allGuesses,String playerTwo){
        Character guessInput;
        String tempstr = "";
        sendToOne(playerTwo,"Time to guess a letter");
        try{
            String tempInput = playerTwoIn.readLine();
            //System.out.println(tempInput);
            String[] temparr = tempInput.split("~ ");
            tempstr = temparr[1];
            //System.out.println(tempstr);
            guessInput = tempstr.charAt(0);
            tempstr = String.valueOf(guessInput);
            allGuesses.add(guessInput);
            
        }
        catch(Exception o){
            System.out.println("Error in CL run: " + o.getMessage());
        }
        
        return word.contains(tempstr);
    }
    
    public boolean stateOfWord(String word, List<Character> allGuesses, String name){
        int correct = 0;
        for (int i=0; i<word.length(); i++){
            if(allGuesses.contains(word.charAt(i))){
                sendToOne(name,String.valueOf(word.charAt(i)));
                correct++;
            }
            else{
                 sendToOne(name, "-");   
            }
        }
        sendToOne(name, "");
        return (word.length() == correct);
    }
    
    //method to send msg from client to those who are in server via serverPop
    public void sendOutMsg(String string){
        // interating over arraylist of clienthandler objects
        for(ClientHandler clienthandlers : serverPop){
            try{
                //send message to all those that is not the sender
                if(!clienthandlers.cName.equals(this.cName)){
                    clienthandlers.outPut.write(string);
                    //procedure to say no more to send
                    clienthandlers.outPut.newLine();
                    clienthandlers.outPut.flush();
                }
            }
            catch(Exception io){
                System.out.println("Error in sendOutMsg: " + io.getMessage());
            }//end of try/catch
        }
    }//end of sendOutMsg
    
    // sends out message excluding the Hangman players
    public void sendOutMsgTwo(String name, String string){
        // interating over arraylist of clienthandler objects
        for(ClientHandler clienthandlers : serverPop){
            try{
                //send message to all those that is not the sender
                if(!clienthandlers.cName.equals(this.cName) && !clienthandlers.cName.equals(name)){
                    clienthandlers.outPut.write(string);
                    //procedure to say no more to send
                    clienthandlers.outPut.newLine();
                    clienthandlers.outPut.flush();
                }
            }
            catch(Exception io){
                System.out.println("Error in sendOutMsg: " + io.getMessage());
            }//end of try/catch
        }
    }//end of sendOutMsg    
    
    //send to only to a specific person
    public void sendToOne(String name,String string){
        // interating over arraylist of clienthandler objects
        for(ClientHandler clienthandlers : serverPop){
            try{
                //send message to all those that is not the sender
                if(clienthandlers.cName.equals(name)){
                    clienthandlers.outPut.write(string);
                    //procedure to say no more to send
                    clienthandlers.outPut.newLine();
                    clienthandlers.outPut.flush();
                }
            }
            catch(Exception io){
                System.out.println("Error in sendOutMsg: " + io.getMessage());
            }//end of try/catch
        }
    }//end of sendOutMsg
    
    //for self sending not to chat
    public void sendToClient(String string){
            try{
                //send message to current sender
                    outPut.write(string);
                    //procedure to say no more to send
                    outPut.newLine();
                    outPut.flush();
            }
            catch(Exception io){
                System.out.println("Error in sendOutMsg: " + io.getMessage());
            }//end of try/catch
    }//end of sendtoclient
    
    // removes the calling ClientHandler object from arraylists
    public void removeClient(){
        serverPop.remove(this);
        serverNameList.remove(this);    
    }//end of renove client
    
    // method for when client logs out call for closing connection and avoid null pointers
    public void disconnect(Socket s, BufferedReader br, BufferedWriter bw){
        removeClient();
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
    }//end of disconnect
    
    //getter methods for the arraylists and a client name
    public ArrayList<ClientHandler> getServerPop(){
        return serverPop;
    }
    
    //get a specific client
    public ClientHandler getClientHandler(String name){
        String temp = name; 
        ClientHandler i = null;
        for (ClientHandler client : serverPop){
            if(temp.equals(client.getName())){
                i = client;
                //System.out.println(temp.getName());
                break;
            }
        }
        return i;
    }
    
    public void setInGame(boolean playing){
        this.inGame = playing;
        //System.out.println("inGame turned true");
    }
    public static ArrayList<String> getServerNames(){
        return serverNameList;
    }
    public String getName(){
        return this.cName;
    }
    public BufferedWriter getOutPut(){
        return this.outPut;
    }
    public BufferedReader getInput(){
        return this.inPut;
    }//end of getters
    
    //method used to enforce uniqueness
    public static boolean uniqueName(String name){
        String n2Check = name;
        //check if its the condition of first client to log on
        if(serverPop.isEmpty()){
          return true;      
        }
        //enhanced for loop to iterate, check for any matches that violate unique
        for(ClientHandler i : serverPop){
            String temp = i.getName();
            if(temp.equalsIgnoreCase(n2Check)){
                return false;
            }
        }
        return true;
    }//end of uniquename
}//end of class