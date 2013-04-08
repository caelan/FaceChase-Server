package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerComm {
    private final ServerSocket serverSocket;
    private Server server;
    private ThreadPool pool;
    private boolean online;

    /*
     * Could move the pool to be here...
     * Add server references to everything
     */
    
    public ServerComm(Server server, int port) throws IOException {
        online = true;
        this.server = server;
        serverSocket = new ServerSocket(port);
        pool = new ThreadPool(server);
    }

    public void serve() throws IOException {
        while (online) {
            // block until a client connects
            //if(!serverSocket.isClosed())
            {
                final Socket socket = serverSocket.accept();
                System.out.println("Connection Established");
                pool.addThread(socket);
            }
        }
    }
    
    /*
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
    }*/

    public void stop() throws IOException {
        serverSocket.close();
        online = false;
    }
}
//Run "telnet localhost 4444" in the Command line
