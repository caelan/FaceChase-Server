package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class OutputThread extends Thread {
    private Socket socket;
    private IOThread ioThread;
    private BlockingQueue<String> messagesToClient;
    
    public OutputThread(Socket socket, IOThread ioThread)
    {
        this.socket = socket;
        this.ioThread = ioThread;
        messagesToClient = new LinkedBlockingQueue<String>();
    }

    @Override
    public void run() {
        try {
            handleConnection();
            // Connection Terminated by client
            socket.close();
        } 
        catch (IOException e) {
            try {
                socket.close();
            } catch(IOException ex) {}
        } finally {

        }
    }
    
    private void handleConnection() throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        try {
            while(ioThread.connected()) {
                String msgToClient = messagesToClient.poll(15, TimeUnit.SECONDS);
                if (msgToClient != null) {
                    //processing
                    out.println(msgToClient);
                }                
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }        
    }
    
    /**
     * Adds the specified message to the queue for messages
     * to be sent to this client. This method can be called by
     * other threads.
     * @param msg the message to be sent to this client
     */
    public void addMessageToQueue(String msg) {
        messagesToClient.add(msg);
    }
    
    public String[] getMessages() {
        String[] output = new String[messagesToClient.size()];
        messagesToClient.toArray(output);
        return output;
    }

    public String getLastMessage() {
        return getMessages()[messagesToClient.size()-1];
    }
}
