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
        System.out.println(">>" + input);
        String separator = " " + Constants.itemDelim + " ";
        String regex = "(Login" + separator + ".+" + separator + ".+)|" +
                "(AddUser" + separator + ".+" + separator + ".+" +  separator + ".+)|" +
                "(RequestKill" + separator + "(\\d+ )*(\\d+))|" +
                "(AddPicture" + separator + "(\\d+ )*(\\d+))|" +
                "(AddFriend" + separator + ".+)|" +
                "(Quit)|" + 
                "(Logout)";
        
        if(input.substring(0, 11).equals("RequestKill"))
        {
            String[] tokens = input.split(separator);
            
            IplImage image = ImageFormat.convertToImage(tokens[1]);
            cvSaveImage("testing.jpg", image);
            
            return "Weeeeeeeeeeeeee";
        }
        
        if(!input.matches(regex)) {
            return null;
        }
        String[] tokens = input.split(separator);
        if (tokens[0].equals("Login")) 
        {
            if(ioThread.getID() != null)
                return "AlreadyLoggedIn";
            
            String email = General.formatEmail(tokens[1]);
            if(email == null)
                return "InvalidEmail";
            
            String password = tokens[2];
            if(!General.checkPassword(password))
                return "InvalidPassword";
            
            Integer id = server.login(email, password, ioThread);
            if(id == null)
            {
                return "InvalidLogin";
            }
            else
            {
                return "User " + id;
            }
        }         
        else if (tokens[0].equals("AddUser")) 
        {
            if(ioThread.getID() != null)
                return "AlreadyLoggedIn";
            
            String name = tokens[1];
            if(!General.checkName(name))
                return "InvalidName";
            
            String email = General.formatEmail(tokens[2]);
            if(email == null)
                return "InvalidEmail";
            
            String password = tokens[3];
            if(!General.checkPassword(password))
                return "InvalidPassword";
            
            Integer id = server.addPlayer(name, email, password, ioThread);
            if(id == null)
            {
                return "EmailAlreadyRegistered";
            }
            else
            {
                return "User " + id;
            }
        } 
        else if (tokens[0].equals("RequestKill")) 
        {
            System.out.println("Yay!");
                        
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
        } 
        else if (tokens[0].equals("AddPicture")) 
        {
            if(ioThread.getID() == null)
                return "NotLoggedIn";
            
            String image = tokens[1]; 
            if(server.addImage(ioThread.getID(), image))
                return "Success";
            else
                return "UnableToAddImage";
                        
        } 
        else if (tokens[0].equals("AddFriend")) 
        {
            if(ioThread.getID() == null)
                return "NotLoggedIn";
            
            String email = General.formatEmail(tokens[1]);
            if(email == null)
                return "UnableToAddFriend";
            
            if(server.addFriend(ioThread.getID(), email))
                return "Success";
            else
                return "UnableToAddFriend";
        } 
        else if (tokens[0].equals("Quit")) //Exit
        {
            if(ioThread.getID() != null)
                ioThread.deregisterThread();
            
            return "Quit";
        } 
        else if (tokens[0].equals("Logout"))  //Stay connected, just remove user
        {
            if(ioThread.getID() == null)
                return "NotLoggedIn";
            
            ioThread.deregisterThread();
            
            return "LoggedOut";
        }
        else
        {
            return null;
        }
    }
}

