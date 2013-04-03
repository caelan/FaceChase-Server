package server;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerPool 
{
    private int nextID; //Synchronization
    private HashMap<Integer, Player> playerMap; //Concurrent vs Synchronized or use array
    private File faceDirectory;
    private HashSet<String> usernames;
    
    public PlayerPool(File faceDirectory)
    {        
        int nextID = 0;
        playerMap = new HashMap<Integer, Player>();
        usernames = new HashSet<String>();
        this.faceDirectory = faceDirectory;
    }
    
    public synchronized Integer addPlayer(String username, String name)
    {
        if(usernames.contains(username))
            return null;
        playerMap.put(nextID, new Player(nextID, username, name));
        usernames.add(username);
        return nextID++;
    }
    
    public synchronized Player removePlayer(int id) //Synchronized
    {
        if(playerMap.containsKey(id))
            return playerMap.remove(id); //Should remove or create a placeholder copy? Add the id back into the id pool?
        else
            return null;
    }
    
    public synchronized Player getPlayer(int id) //Synchronized
    {
        if(playerMap.containsKey(id))
            return playerMap.get(id);
        else
            return null;
    }
}
