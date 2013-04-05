package server;

import java.util.HashMap;

import util.Constants;

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
    
    public Game(int id, String refDir)
    {
        this.id = id;
        this.saveDir = refDir + "\\" + id;
        terminated = false;
        playerStatus = new HashMap<Player, Status>();
        facialRec = new FacialRecognition(saveDir, Constants.classifierType, Constants.faceSize);
    }
    
    public abstract Status createPlayerStatus(Player p);
    public abstract boolean save();
    
    public String toString()
    {
        return "Game " + id;
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