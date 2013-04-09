package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import util.FileSystem;

public class PlayerPool 
{
    private int nextID; //Synchronization
    private HashMap<Integer, Player> playerMap; //Concurrent vs Synchronized or use array
    private HashMap<String, Integer> usernames;
    private String saveDir;
    private final String saveData = "\\saveData.dat";

    public PlayerPool(String refDir, boolean load)
    {        
        this.saveDir = refDir + "\\Players";
        if(load)
        {
            load();
        }
        else
        {
            nextID = 0;
            playerMap = new HashMap<Integer, Player>();
            usernames = new HashMap<String, Integer>();
        }
    }
    
    public synchronized Player addPlayer(String email, String password)
    {
        if(usernames.containsKey(email))
            return null;
        Player p = new Player(nextID, saveDir, email, password);
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
    
    public synchronized boolean save()
    {
        File saveDirFile = new File(saveDir);
        if(saveDirFile.exists())
        {
            try {
                FileSystem.delete(saveDirFile);
            } catch (IOException e) {
                System.err.println("Could Not Save PlayerPool: " + e.getMessage());
                return false;
            }
        }
        
        saveDirFile.mkdirs();
        try{            
            FileWriter fstream = new FileWriter(saveDir + saveData);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("PlayerPool\n");
            out.write("nextID " + nextID + "\n");
            out.write("Players:\n");
            for(int id: playerMap.keySet())
            {
                out.write("Player "+ id + " "+ playerMap.get(id).getEmail() + "\n");
            }

            out.close();
        }
        catch (Exception e)
        {
            System.err.println("Could Not Save PlayerPool: " + e.getMessage());
            return false;
        }
                
        for(int id: playerMap.keySet())
        {
            Player p = playerMap.get(id);
            if(!p.save())
            {
                System.err.println("Could Not Save PlayerPool: Error Saving Players");
                return false; 
            }
        }
        return true;
    }
        
    public synchronized boolean load()
    {
        try 
        {
            playerMap = new HashMap<Integer, Player>();
            usernames = new HashMap<String, Integer>();
            
            Scanner scanner =  new Scanner(new File(saveDir + saveData));
            scanner.nextLine(); //PlayerPool
            
            nextID = Integer.parseInt(scanner.nextLine().split(" ")[1]); //nextID
            scanner.nextLine(); //Players:
           
            while (scanner.hasNextLine())
            {
                String[] split = scanner.nextLine().split(" ");
                int id = Integer.parseInt(split[1]);
                String email = split[2];
                
                //Player p = new Player(playerPool, id, saveDir, true);
                //playerMap.put(id, p);
                usernames.put(email, id);
            }
            scanner.close();
        }
        catch (Exception e)
        {
            System.err.println("Could Not Load PlayerPool: " + e.getMessage());
            return false;
        }        

        return true;
    }   
}
