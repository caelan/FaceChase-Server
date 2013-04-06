package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import util.Constants;
import util.FileSystem;

import facialRecognition.FacialRecognition;

public abstract class Game {
    protected int id;
    protected boolean active;
    protected boolean terminated;
    protected boolean joinAfterCreation;  
    protected HashMap<Player, Status> playerStatus;
    protected FacialRecognition facialRec;
    protected String saveDir;
    protected final String saveData = "\\saveData.dat";
    
    public Game(int id, String refDir, boolean load)
    {
        this.id = id;
        this.saveDir = refDir + "\\g" + id;
        if(load)
        {
            load();      
        }
        else
        {
            active = false;
            terminated = false;
            //joinAfterCreation specified in type
            playerStatus = new HashMap<Player, Status>();
            facialRec = new FacialRecognition(saveDir, Constants.classifierType, Constants.faceSize);
        }
    }
    
    public abstract Status createPlayerStatus(Player p);
    public abstract String toString();
    public abstract String gameType();
    public abstract boolean typeSpecificSave();
    public abstract boolean typeSpecificLoad();

    
    public boolean save()
    {
        File saveDirFile = new File(saveDir);
        if(saveDirFile.exists())
        {
            try {
                FileSystem.delete(saveDirFile);
            } catch (IOException e) {
                return false;
            }
        }

        saveDirFile.mkdirs();

        try{            
            FileWriter fstream = new FileWriter(saveDir + saveData);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(toString() + "\n");
            out.write("active " + active + "\n");
            out.write("terminated " + terminated + "\n");
            out.write("joinAfterCreation " + joinAfterCreation + "\n");

            if(!typeSpecificSave())
            {
                out.close();
                return false;
            }
                
            out.write("Players:\n");
            for(Player p: playerStatus.keySet())
                out.write("" + p.getID());

            out.close();
        }
        catch (Exception e)
        {
            return false;
        }

        facialRec.save();

        //Save the statuses here

        return true;
    }
    public boolean load()
    {
        try 
        {
            playerStatus = new HashMap<Player, Status>();
            facialRec = new FacialRecognition(saveDir, Constants.classifierType, Constants.faceSize);
            
            Scanner scanner =  new Scanner(new File(saveDir));
            scanner.nextLine(); //Game id
            
            active = Boolean.parseBoolean(scanner.nextLine().split(" ")[1]);
            terminated = Boolean.parseBoolean(scanner.nextLine().split(" ")[1]);
            joinAfterCreation = Boolean.parseBoolean(scanner.nextLine().split(" ")[1]);

            if(!typeSpecificLoad())
            {
                scanner.close();
                return false;
            }
            
            scanner.nextLine(); //Players:

            while (scanner.hasNextLine())
            {
                int id = Integer.parseInt(scanner.nextLine());
                //TODO append player/status
            }
            scanner.close();
        }
        catch (Exception e)
        {
            return false;
        }     
        return true;
    }
    
    public int hashCode()
    {
        return id;
    }
    
    public boolean equals(Game g)
    {
        return id == g.getID();
    }  

    public int getID()
    {
        return id;
    }
}