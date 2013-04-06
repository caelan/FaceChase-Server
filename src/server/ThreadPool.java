package server;

import java.util.HashMap;
import java.util.HashSet;

public class ThreadPool {
    private HashMap<Integer, IOThread> connectionMap; //TODO concurrent...
    public ThreadPool()
    {
        connectionMap = new HashMap<Integer, IOThread>();
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
        return true;
    }
    
    public boolean sendMessage(int id, String message)
    {
        if(!connectionMap.containsKey(id))
            return false;
        connectionMap.get(id).writeMessage(message);
        return true;
    }
}
