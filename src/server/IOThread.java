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
    private Server server;

    public IOThread(Server server, ThreadPool pool, Socket socket)
    {
        this.server = server;
        this.pool = pool;
        connected = false;
        input = new InputThread(server, socket, this);
        output = new OutputThread(socket, this);
        id = null;       
    }
    
    public void start()
    {
        connected = true;
        input.start();
        output.start();
    }
    
    public boolean registerThread(int id)
    {
        if(pool.putConnection(id, this))
        {
            this.id = id;
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public boolean deregisterThread()
    {
        if(pool.removeConnection(id))
        {
            this.id = null;
            return true;
        }
        else
        {
            return false;
        }    
    }

    public void writeMessage(String message)
    {
        output.addMessageToQueue(message);
    }
    
    public boolean writeMessageOtherThread(String message, int id)
    {
        return pool.sendMessage(id, message);
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
        else
            pool.decreaseThreadCount();
        //Input socket will have already disconnected
        //Output socket will disconnect shortly
    }
    
    public Integer getID()
    {
        return id;
    }
    
    public void setID(Integer i)
    {
        id = i;
    }
}
