package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.MatVector;

import util.FileSystem;
import util.General;

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
    protected Integer facesPerPerson;
    protected Double killThreshold;
    protected Integer testFaces;
    protected PlayerPool playerPool;
    
    public Game(PlayerPool playerPool, int id, String refDir, boolean load)
    {
        this.playerPool = playerPool;
        this.id = id;
        this.saveDir = refDir + "\\g" + id;
        playerStatus = new HashMap<Player, Status>();
        if(load)
        {
            load();      
        }
        else
        {
            active = false;
            terminated = false;
            //joinAfterCreation specified in type
        }
    }
    
    public abstract Status addPlayer(Player p);
    public abstract Player killRequest(Player killer, IplImage image);
    public abstract String toString();
    public abstract String gameType();
    public abstract boolean typeSpecificSave();
    public abstract boolean typeSpecificLoad();

    public boolean updateRecognition(IplImage image, int label)
    {
        final MatVector faces = new MatVector(1);
        final int[] labels = new int[1];

        labels[0] = label;
        IplImage processed = facialRec.extractFaceFail(image);
        if(processed == null)
            return false;
        faces.put(0, processed);
        
        Runnable thread = new Runnable(){ //TODO likely better to spawn a thread for this one...
            public void run(){
                facialRec.update(faces, labels);
                }
        };
        new Thread(thread).start();
        
        return true;
    }   
    
    public boolean save()
    {
        File saveDirFile = new File(saveDir);
        /*if(saveDirFile.exists())
        {
            try {
                FileSystem.delete(saveDirFile);
            } catch (IOException e) {
                return false;
            }
        }*/

        saveDirFile.mkdirs();

        try{            
            FileWriter fstream = new FileWriter(saveDir + saveData);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(toString() + "\n");
            out.write("active " + active + "\n");
            out.write("terminated " + terminated + "\n");
            out.write("joinAfterCreation " + joinAfterCreation + "\n");
            out.write("facesPerPerson " + facesPerPerson + "\n");
            out.write("killThreshold " + killThreshold + "\n");
            out.write("testFaces " + testFaces + "\n");
            
            if(!typeSpecificSave())
            {
                out.close();
                return false;
            }
                
            out.write("Players:\n");
            for(Player p: playerStatus.keySet())
                out.write("" + p.getID() + "\n");

            out.close();
        }
        catch (Exception e)
        {
            return false;
        }

        facialRec.save();

        for(Player p: playerStatus.keySet())
            playerStatus.get(p).save();
        
        return true;
    }
    public boolean load()
    {
        try 
        {           
            Scanner scanner =  new Scanner(new File(saveDir + saveData));
            scanner.nextLine(); //Game id

            active = Boolean.parseBoolean(scanner.nextLine().split(" ")[1]);
            terminated = Boolean.parseBoolean(scanner.nextLine().split(" ")[1]);
            joinAfterCreation = Boolean.parseBoolean(scanner.nextLine().split(" ")[1]);
            
            facesPerPerson = General.parseInt(scanner.nextLine().split(" ")[1]);
            killThreshold = General.parseDouble(scanner.nextLine().split(" ")[1]);
            testFaces = General.parseInt(scanner.nextLine().split(" ")[1]);
      
            if(!typeSpecificLoad())
            {
                scanner.close();
                return false;
            }
            
            scanner.nextLine(); //Players:

            while (scanner.hasNextLine())
            {
                int id = Integer.parseInt(scanner.nextLine());
                Status s = addPlayer(playerPool.getPlayer(id));
                s.load();
                playerPool.getPlayer(id).addToGame(this, s);
            }
            scanner.close();
            
            facialRec = new FacialRecognition(saveDir);
        }
        catch (Exception e)
        {
            return false;
        }     
        return true;
    }
    
    public String getDir()
    {
        return saveDir;
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