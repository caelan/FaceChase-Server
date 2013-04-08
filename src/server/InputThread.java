package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import util.Constants;

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
                //Proccess
                String output = parseRequest(input);
                if(output == null)
                {
                    ioThread.writeMessage("Invalid Request");
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
            ioThread.disconnect();
            in.close();
        }        
    }

    private String parseRequest(String input)
    {
        String separator = " " + Constants.itemDelim + " ";
        String regex = "(Login" + separator + ".+" + separator + ".+)|" +
                "(Add User" + separator + ".+" +  separator + ".+)|" +
                "(Request Kill" + separator + "\\d+)|" +
                "(Add Picture" + separator + "\\d+)|" +
                "(Add Friend" + separator + ".+)|" +
                "(Quit)|" + 
                "(Logout)";
        if(!input.matches(regex)) {
            return null;
        }
        String[] tokens = input.split(separator);
        if (tokens[0].equals("Login")) 
        {
            if(ioThread.getID() != null)
                return "Already Logged In";
            
            String email = tokens[1];
            String password = tokens[2];
            Integer id = server.login(email, password, ioThread);
            if(id == null)
            {
                return "Could Not Log In";
            }
            else
            {
                return "User " + id;
            }
        }         
        else if (tokens[0].equals("(Add User")) 
        {
            if(ioThread.getID() != null)
                return "Already Logged In";
            
            String email = tokens[1];
            String password = tokens[2];
            Integer id = server.addPlayer(email, password, ioThread);
            if(id == null)
            {
                return "Email Already Registered";
            }
            else
            {
                return "User " + id;
            }
        } 
        else if (tokens[0].equals("Request Kill")) 
        {
            String image = tokens[1];           
            return server.requestKill(ioThread.getID(), image);
        } 
        else if (tokens[0].equals("Add Picture")) 
        {
            String image = tokens[1];           
            return server.addImage(ioThread.getID(), image);
        } 
        else if (tokens[0].equals("Add Friend")) 
        {
            String email = tokens[1];           
            return server.addFriend(ioThread.getID(), email);
        } 
        else if (tokens[0].equals("Quit")) //Exit
        {
            //ioThread.writeMessage("quit");
            return "Quit";
        } 
        else if (tokens[0].equals("logout"))  //Stay connected, just remove user
        {
            if(ioThread.getID() == null)
                return "Not Logged In";
            
            ioThread.deregisterThread();
            
            return "Logged Out";
        }
        else
        {
            return null;
        }
    }
}

