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
    
    public synchronized Player addPlayer(String username, String name)
    {
        if(usernames.containsKey(username))
            return null;
        Player p = new Player(nextID, username, name);
        playerMap.put(nextID, p);
        usernames.put(username, nextID);
        nextID++;
        return p;
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
    
    public synchronized boolean save() //TODO
    {
        return false;
    }
    
    public synchronized boolean load() //TODO
    {
        return false;
    }
}
