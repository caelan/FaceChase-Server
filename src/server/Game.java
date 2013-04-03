package server;

import java.util.HashMap;

public abstract class Game {
    protected int id;
    protected boolean active;
    protected boolean joinAfterCreation;  
    protected HashMap<Player, Status> playerStatus;
    
    public Game(int id)
    {
        this.id = id;
        active = true;
        playerStatus = new HashMap<Player, Status>();
    }
    
    public abstract Status createPlayerStatus(Player p);
    
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