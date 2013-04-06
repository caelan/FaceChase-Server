package server;

import java.net.Socket;

/*
 * TODO - connection losses? - right now I just quit the socket
 */

public class IOThread {
    private InputThread input;
    private OutputThread output;
    private Integer id;
    private boolean connected;
    private ThreadPool pool;

    public IOThread(ThreadPool pool, Socket socket)
    {
        this.pool = pool;
        connected = false;
        input = new InputThread(socket, this);
        output = new OutputThread(socket, this);
        id = null;       
    }
    
    public void start()
    {
        connected = true;
        input.start();
        output.start();
    }
    
    public void writeMessage(String message)
    {
        output.addMessageToQueue(message);
    }
    
    public boolean connected()
    {
        return connected;
    }
    
    public void disconnect()
    {
        connected = false;
        if(id != null)
            pool.removeConnection(id);
        //Input socket will have already disconnected
        //Output socket will disconnect shortly
    }
    
    public Integer getID()
    {
        return id;
    }
    
    public void setID(int i)
    {
        id = i;
    }
}
