package server;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadPool {
    private ConcurrentHashMap<Integer, IOThread> connectionMap; 
    private int totalConnected;
    private Server server;
    public ThreadPool(Server server)
    {
        this.server = server;
        totalConnected = 0;
        connectionMap = new ConcurrentHashMap<Integer, IOThread>();
    }
    
    public synchronized void addThread(Socket socket)
    {
        totalConnected+=1;
        new IOThread(server, this, socket).start();
    }
    
    public synchronized void decreaseThreadCount()
    {
        totalConnected-=1;
    }
    
    public boolean putConnection(int id, IOThread thread)
    {
        if(connectionMap.containsKey(id))
            return false;        
        connectionMap.put(id, thread);
        return true;
    }
    
    public boolean removeConnection(int id)
    {
        if(!connectionMap.containsKey(id))
            return false;        
        connectionMap.remove(id);
        decreaseThreadCount();
        return true;
    }
    
    public boolean sendMessage(int id, String message) //Ideally each user will have a connection queue, but for now nope
    {
        if(!connectionMap.containsKey(id))
            return false;
        connectionMap.get(id).writeMessage(message);
        return true;
    }
}
