package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerComm {
    private final ServerSocket serverSocket;
    private int connected;
    private Server server;

    public ServerComm(Server server, int port) throws IOException {
        this.server = server;
        serverSocket = new ServerSocket(port);
        this.connected = 0;
    }

    public void serve() throws IOException {
        while (true) {
            // block until a client connects
            final Socket socket = serverSocket.accept();
            Runnable thread = new Runnable(){
                public void run(){                
                    try {
                        connected++;
                        handleConnection(socket);
                        connected--;
                    } catch (IOException e) {
                        e.printStackTrace(); // but don't terminate serve()
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            new Thread(thread).start();
        }
    }

    private void handleConnection(Socket socket) throws IOException {        
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        try {
            out.println("You have connected to FaceChase\n");
            for (String line =in.readLine(); line!=null; line=in.readLine()) {
                String output = parseRequest(line);
                if(output == null)
                {
                    out.println("Invalid Request");
                }
                else if(output.equals("quit"))
                {
                    out.println("User Disconnected");
                    break;
                }
            }
        } finally {        
            out.close();
            in.close();
        }
    }

    private String parseRequest(String input) 
    {
        String regex = "(login .+)|" +
        		"(adduser .+ [a-zA-Z ]+)|" +
        		"(kill \\d+ \\d+)|" +
        		"(quit)"; 
        if(!input.matches(regex)) {
            return null;
        }
        String[] tokens = input.split(" ");
        if (tokens[0].equals("login")) 
        {
            String username = tokens[1];
            return "";
        }         
        else if (tokens[0].equals("adduser")) 
        {
            String username = tokens[1];
            String name = tokens[2]; //Can have spaces...
            String message = server.addPlayer(username, name);
            return message;
        } 
        else if (tokens[0].equals("kill")) 
        {
            int killerID;
            try{
                killerID = Integer.parseInt(tokens[1]);
            } catch(NumberFormatException e){
                return null;
            }
            String image = tokens[2];
            
            //Kill request
            return server.processKill(killerID, image);
        } 
        else if (tokens[0].equals("quit")) 
        {
            return "quit";
        } 
        else
        {
            return null;
        }
    }
    public void stop() throws IOException {
        serverSocket.close();
    }
}
//Run "telnet localhost 4444" in the Command line
