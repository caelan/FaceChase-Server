package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import util.FileSystem;

public class GamePool {
    private int nextID; //Synchronization
    private HashMap<Integer, Game> gameMap; //Concurrent vs Synchronized or use array
    private String saveDir;
    private final String saveData = "\\saveData.dat";
    private PlayerPool playerPool;
    
    public GamePool(PlayerPool playerPool, String refDir, boolean load)
    {        
        this.playerPool = playerPool;
        this.saveDir = refDir + "\\Games";
        if(load)
        {
            load();
        }
        else
        {
            nextID = 0;
            gameMap = new HashMap<Integer, Game>();
        }
    }
    
    public synchronized Game addFriendsGame()
    {
        Game game = new FriendsGame(playerPool, nextID, saveDir, false);
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
        
    public synchronized boolean save()
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
        
        saveDirFile.mkdirs();
        try{            
            FileWriter fstream = new FileWriter(saveDir + saveData);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("GamePool\n");
            out.write("nextID " + nextID + "\n");
            out.write("Games:\n");
            for(int id: gameMap.keySet())
            {
                out.write(""+gameMap.get(id) + "\n");
            }

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
                System.err.println("Could Not Save GamePool: Error Saving Games");
                return false; 
            }
        }
        return true;
    }
        
    public synchronized boolean load()
    {
        try 
        {
            gameMap = new HashMap<Integer, Game>();
            
            Scanner scanner =  new Scanner(new File(saveDir + saveData));
            scanner.nextLine(); //GamePool
            
            nextID = Integer.parseInt(scanner.nextLine().split(" ")[1]); //nextID
            scanner.nextLine(); //Games:
           
            while (scanner.hasNextLine())
            {
                String[] split = scanner.nextLine().split(" ");
                String name = split[0];
                int id = Integer.parseInt(split[1]);
                
                if(name.equals("FriendsGame"))
                {
                    Game g = new FriendsGame(playerPool, id, saveDir, true);
                    gameMap.put(id, g);
                }
                else
                {
                    System.err.println("Could Not Load GamePool: Error Loading Games");
                    scanner.close();
                    return false;
                }
            }
            scanner.close();
        }
        catch (Exception e)
        {
            System.err.println("Could Not Load GamePool: " + e.getMessage());
            return false;
        }        

        return true;
    }   
}
