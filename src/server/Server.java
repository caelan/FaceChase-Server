package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final ServerSocket serverSocket;
    private int connected;

    public Server(int port) throws IOException {
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
            out.println("Welcome to Minesweeper. " + connected + " people are playing including you. Type 'help' for help.\n");
            for (String line =in.readLine(); line!=null; line=in.readLine()) {
                String output = parseRequest(line);
                //Do stuff to it
            }
        } finally {        
            out.close();
            in.close();
        }
    }

    private static String parseRequest(String input) {
        /*
        String regex = "(look)|(dig \\d+ \\d+)|(flag \\d+ \\d+)|" +
                "(deflag \\d+ \\d+)|(help)|(bye)";
        if(!input.matches(regex)) {
            //invalid input
            return null;
        }
        String[] tokens = input.split(" ");
        if (tokens[0].equals("look")) {
            // 'look' request
            return board.viewBoard();
        } else if (tokens[0].equals("help")) {
            // 'help' request
            return "Type 'look' to see the board.\nType 'dig <x> <y>' to dig at position (x, y).\nType 'flag <x> <y>' to flag position (x, y).\nType 'deflag <x> <y>' to deflag position (x, y).\nType bye to quit.";
        } else if (tokens[0].equals("bye")) {
            // 'bye' request
            return "kill";
        } else {
            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            if (tokens[0].equals("dig")) {
                // 'dig x y' request
                return board.dig(x, y);
            } else if (tokens[0].equals("flag")) {
                // 'flag x y' request
                return board.flag(x, y);
            } else if (tokens[0].equals("deflag")) {
                // 'deflag x y' request
                return board.deFlag(x, y);
            }
        }*/
        // Should never get here--make sure to return in each of the valid cases above.
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {   
        int port = 4444;
        try
        {
            Server server = new Server(port);
            server.serve();
        }
        catch(Exception e){
            
        }
    }
}
//Run "telnet localhost 4444" in the Command line
