package server;

import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadPool {
    private ConcurrentHashMap<Integer, IOThread> connectionMap; 
    private ConcurrentHashMap<Integer, LinkedList<String>> messageMap; 

    private int totalConnected;
    private Server server;
    public ThreadPool(Server server)
    {
        this.server = server;
        totalConnected = 0;
        connectionMap = new ConcurrentHashMap<Integer, IOThread>();
        messageMap = new ConcurrentHashMap<Integer, LinkedList<String>>();
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
        
        if(messageMap.containsKey(id))
        {
            while(messageMap.get(id).size() != 0)
            {
                sendMessage(id, messageMap.get(id).poll());
            }
        }
        
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
    
    public boolean sendMessage(int id, String message) //Check to make sure its valid?
    {
        if(!connectionMap.containsKey(id) || connectionMap.get(id) == null)
        {
            if(!messageMap.containsKey(id))
            {
                messageMap.put(id, new LinkedList<String>());
            }
            messageMap.get(id).push(message);     
        }
        else
        {
            connectionMap.get(id).writeMessage(message);
        }
        return true;
    }
}
