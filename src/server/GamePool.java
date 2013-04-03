package server;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class GamePool {
    private int nextID; //Synchronization
    private HashMap<Integer, Game> gameMap; //Concurrent vs Synchronized or use array
    private HashSet<String> usernames;
    
    public GamePool()
    {        
        int nextID = 0;
        gameMap = new HashMap<Integer, Game>();
    }
    
    public synchronized Game addFriendsGame()
    {
        Game game = new FriendsGame(nextID);
        gameMap.put(nextID, game);
        nextID++;
        return game;
    }
    
    public synchronized Game removeGame(int id) //Synchronized
    {
        if(gameMap.containsKey(id))
            return gameMap.remove(id); //Should remove or create a placeholder copy? Add the id back into the id pool?
        else
            return null;
    }
    
    public synchronized Game getGame(int id) //Synchronized
    {
        if(gameMap.containsKey(id))
            return gameMap.get(id);
        else
            return null;
    }
}
