package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import util.FileSystem;

public class GamePool {
    private int nextID; //Synchronization
    private HashMap<Integer, Game> gameMap; //Concurrent vs Synchronized or use array
    private HashSet<String> usernames;
    private String saveDir;
    private String saveData = "\\gamesData.dat";
    
    public GamePool(String refDir)
    {        
        //Load
        nextID = 0;
        gameMap = new HashMap<Integer, Game>();
        this.saveDir = refDir + "\\Games";
    }
    
    public synchronized Game addFriendsGame()
    {
        Game game = new FriendsGame(nextID, saveDir);
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
    
    public boolean save()
    {
        File saveDirFile = new File(saveDir);
        if(saveDirFile.exists())
        {
            try {
                FileSystem.delete(saveDirFile);
            } catch (IOException e) {
                System.err.println("Could Not Save GamePool: " + e.getMessage());
                return false;
            }
        }
        System.out.println("lolz");
        
        saveDirFile.mkdirs();
        try{            
            FileWriter fstream = new FileWriter(saveDir + saveData);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("There is no data!\n");

            out.close();
        }
        catch (Exception e)
        {
            System.err.println("Could Not Save GamePool: " + e.getMessage());
            return false;
        }
                
        for(int id: gameMap.keySet())
        {
            Game g = gameMap.get(id);
            if(!g.save())
            {
                System.err.println("Could Not Save GamePool");
                return false; 
            }
        }
        return true;
    }
}
