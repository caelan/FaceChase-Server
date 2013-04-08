package server;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerPool 
{
    private int nextID; //Synchronization
    private HashMap<Integer, Player> playerMap; //Concurrent vs Synchronized or use array
    private HashMap<String, Integer> usernames;
    private String saveDir;

    public PlayerPool(String refDir)
    {        
        nextID = 0;
        playerMap = new HashMap<Integer, Player>();
        usernames = new HashMap<String, Integer>();
        this.saveDir = refDir + "\\Players";
    }
    
    public synchronized Player addPlayer(String email, String password)
    {
        if(usernames.containsKey(email))
            return null;
        Player p = new Player(nextID, email, password);
        playerMap.put(nextID, p);
        usernames.put(email, nextID);
        nextID++;
        return p;
    }
    
    public synchronized Player login(String email, String password)
    {
        if(!usernames.containsKey(email))
            return null;
        
        int id = usernames.get(email);
        if(!playerMap.get(id).getPassword().equals(password))
            return null;
        else
            return playerMap.get(id);
    }
    
    public synchronized Player removePlayer(int id) //Synchronized
    {
        if(playerMap.containsKey(id))
        {
            usernames.remove(id);
            return playerMap.remove(id); //Should remove or create a placeholder copy? Add the id back into the id pool?
        }
        else
        {
            return null;
        }
    }
    
    public synchronized Player getPlayer(int id) //Synchronized
    {
        if(playerMap.containsKey(id))
            return playerMap.get(id);
        else
            return null;
    }
    
    public synchronized Player getPlayer(String email) //Synchronized
    {
        if(usernames.containsKey(email))
        {
            int id = usernames.get(email);
            if(playerMap.containsKey(id))
                return playerMap.get(id);
            else
                return null;
        }
        else
        {
            return null;
        }
    }
    
    public synchronized boolean save() //TODO
    {
        return false;
    }
    
    public synchronized boolean load() //TODO
    {
        return false;
    }
}
