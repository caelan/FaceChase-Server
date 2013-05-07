package server;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import facialRecognition.ImageFormat;

import util.Constants;
import util.General;
import util.Pair;

public class InputThread extends Thread {

    private Socket socket;
    private IOThread ioThread;
    private Server server;
    public InputThread(Server server, Socket socket, IOThread ioThread)
    {
        this.server = server;
        this.socket = socket;
        this.ioThread = ioThread;
    }
    @Override
    public void run() {
        // handle the client
        try {
            handleConnection();
            // Connection Terminated by client
            socket.close();
            
        } catch (IOException e) {
            // IOExceptions from Individual clients don't terminate serve()
            // Just the current client
            // Close the connection if it still open
            try {
                socket.close();
            } catch(IOException ex) {}
        } finally {
            // This socket is no longer active so decrease our count
            // TODO Client is no longer connected
        }
    }

    private void handleConnection() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        try {
            String input = in.readLine();
            while(ioThread.connected() && input != null) {
                String output = parseRequest(input);
                if(output == null)
                {
                    ioThread.writeMessage("InvalidRequest");
                }
                else if(output.equals("Quit"))
                {
                    //ioThread.writeMessage(output); //TODO this won't write
                    break;
                }
                else
                {
                    ioThread.writeMessage(output);
                }
                input = in.readLine();
            }
        } finally 
        {
            if(ioThread.getID() != null) //TODO keep logged in even after failure
                ioThread.deregisterThread();
            ioThread.disconnect();
            in.close();
        }        
    }

    private String parseRequest(String input)
    {
        String separator = " " + "\\" + Constants.itemDelim + " ";
        String regex = "(Login" + separator + ".+" + separator + ".+)|" +
                "(Reconnect" + separator + ".+" + separator + ".+)|" +
                "(AddUser" + separator + ".+" + separator + ".+" +  separator + ".+)|" +
                "(RequestKill" + separator + "(\\d+ )*(\\d+))|" +
                "(AddPicture" + separator + "(\\d+ )*(\\d+))|" +
                "(AddFriend" + separator + ".+)|" +
                "(Quit)|" + 
                "(Logout)";
        
        if(input.length() > 11 && input.substring(0, 11).equals("RequestKill"))
        {
            System.out.println(">>RequestKill | <image>");

            /*String[] tokens = input.split(separator);
            IplImage image = ImageFormat.convertToImage(tokens[1]);
            cvSaveImage("testing.jpg", image);            
            return "Success";*/
            
            if(ioThread.getID() == null)
            {
                System.out.println("Not Logged In");
                return "NotLoggedIn";
            }
            
            String image = input.split(separator)[1];           
            Pair<Player, Player> pair = server.requestKill(ioThread.getID(), image);
            if(pair == null)
            {
                System.out.println("Invalid Kill");              
                return "InvalidKill";
            }
            else
            {
                ioThread.writeMessageOtherThread("Dead " + pair.getFirst().getEmail(), pair.getSecond().getID());
                System.out.println("Killed " + pair.getSecond());
                return "Killed" + Constants.itemDelim + pair.getSecond().getName();
            }
        }
        else if(input.length() > 10 && input.substring(0, 10).equals("AddPicture"))
        {
            System.out.println(">>AddPicture | <image>");

            /*String[] tokens = input.split(separator);           
            IplImage image = ImageFormat.convertToImage(tokens[1]);
            cvSaveImage("testing.jpg", image);
            return "Success";*/
            
            if(ioThread.getID() == null)
            {
                System.out.println("Not Logged In");
                return "NotLoggedIn";
            }
            
            String image = input.split(separator)[1]; 
            if(server.addImage(ioThread.getID(), image))
            {
                System.out.println("Added Image");
                return "Success";
            }
            else
            {
                System.out.println("Bad Picture");
                return "BadPicture";
            }
        }

        if(input.length() > 100)
        {
            System.out.println(">><invalid input length>");
            System.out.println("Invalid Command");
            return null;
        }
        
        System.out.println(">>" + input);
        
        if(!input.matches(regex)) {
            System.out.println("Invalid Command");
            return null;
        }
        String[] tokens = input.split(separator);
        if (tokens[0].equals("Login")) 
        {
            if(ioThread.getID() != null)
            {
                System.out.println("Invalid Login");
                return "InvalidLogin";
            }
            
            String email = General.formatEmail(tokens[1]);
            if(email == null)
            {
                System.out.println("Invalid Login");
                return "InvalidLogin";
            }
            
            String password = tokens[2];
            if(!General.checkPassword(password))
            {
                System.out.println("Invalid Login");
                return "InvalidLogin";
            }
            
            Player p = server.login(email, password, ioThread);
            if(p == null)
            {
                System.out.println("Invalid Login");
                return "InvalidLogin";
            }
            else
            {
                Integer id = p.getID();
                String output = "User" + Constants.itemDelim + id + Constants.itemDelim + "Friends";
                for(Integer i: p.getFriends())
                {
                    Player friend = server.getPlayer(i);
                    if(friend != null)
                        output += Constants.itemDelim + friend.getName();
                }
                
                System.out.println("Login: " + p);
                return output;
            }
        }        
        else if (tokens[0].equals("Reconnect")) 
        {
            if(ioThread.getID() != null)
            {
                System.out.println("Already Connected");
                return "InvalidReconnect";
            }
            
            String email = General.formatEmail(tokens[1]);
            if(email == null)
            {
                System.out.println("Invalid Email");
                return "InvalidReconnect";
            }
            
            String password = tokens[2];
            if(!General.checkPassword(password))
            {
                System.out.println("Invalid Password");
                return "InvalidReconnect";
            }
            
            Player p = server.login(email, password, ioThread);
            if(p == null)
            {
                System.out.println("Invalid Reconnect");
                return "InvalidReconnect";
            }
            else
            {
                System.out.println("Reconnect: " + p);
                return "User" + Constants.itemDelim + p.getID();
            }
        }     
        else if (tokens[0].equals("AddUser")) 
        {
            if(ioThread.getID() != null)
            {
                System.out.println("Already Logged In");
                return "InvalidAddUser";
            }            
            
            String name = tokens[1];
            if(!General.checkName(name))
            {
                System.out.println("Invalid Name");
                return "InvalidAddUser";
            }            
            
            String email = General.formatEmail(tokens[2]);
            if(email == null)
            {
                System.out.println("Invalid Email");
                return "InvalidAddUser";
            }                     
            
            String password = tokens[3];
            if(!General.checkPassword(password))
            {
                System.out.println("Invalid Password");
                return "InvalidAddUser";
            }            
            
            Integer id = server.addPlayer(name, email, password, ioThread);
            if(id == null)
            {
                System.out.println("Email Taken");
                return "EmailAlreadyRegistered";
            }
            else
            {
                System.out.println("Added User: " + id);
                return "User " + id;
            }
        } 
        /*else if (tokens[0].equals("RequestKill")) 
        {                        
            if(ioThread.getID() == null)
                return "NotLoggedIn";
            
            String image = tokens[1];           
            Pair<Player, Player> pair = server.requestKill(ioThread.getID(), image);
            if(pair == null)
            {
                return "InvalidKill";
            }
            else
            {
                ioThread.writeMessageOtherThread("Dead " + pair.getFirst().getEmail(), pair.getSecond().getID());
                return "Killed " + pair.getSecond().getEmail();
            }
        }*/
        /*else if (tokens[0].equals("AddPicture")) 
        {
            if(ioThread.getID() == null)
                return "NotLoggedIn";
            
            String image = tokens[1]; 
            if(server.addImage(ioThread.getID(), image))
                return "Success";
            else
                return "UnableToAddImage";
                        
        }*/
        else if (tokens[0].equals("AddFriend")) 
        {
            if(ioThread.getID() == null)
            {
                System.out.println("Not Logged In");
                return "UnableToAddFriend";
            }            
            
            String email = General.formatEmail(tokens[1]);
            if(email == null)
            {
                System.out.println("Invalid Email");
                return "UnableToAddFriend";
            }            
            if(server.addFriend(ioThread.getID(), email))
            {
                System.out.println("Added Friend: " + email);
                return "Success";
            }
            else
            {
                System.out.println("Unable to Add Friend");
                return "UnableToAddFriend";
            }
        } 
        else if (tokens[0].equals("Quit")) //Exit
        {
            if(ioThread.getID() != null)
                ioThread.deregisterThread();
            
            System.out.println("Quit");
            return "Quit";
        } 
        else if (tokens[0].equals("Logout"))  //Stay connected, just remove user
        {
            if(ioThread.getID() == null)
            {
                System.out.println("Not Logged In");
                return "NotLoggedIn";
            }            
            ioThread.deregisterThread();
            
            System.out.println("Logged Out");
            return "LoggedOut";
        }
        else
        {
            return null;
        }
    }
}

