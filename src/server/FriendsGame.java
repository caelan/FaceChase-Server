package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import util.FileSystem;

public class FriendsGame extends Game {
    public FriendsGame(int id, String refDir, boolean load)
    {
        super(id, refDir, load);
        if(load)
        {
        }
        else
        {
            joinAfterCreation = true;
        }
    }
    
    public Status createPlayerStatus(Player p)
    {
        Status s = new FriendsStatus(this, p);
        playerStatus.put(p, s);
        return s;
    }
    
    public String gameType()
    {
        return "FriendsGame";
    }
    
    public String toString()
    {
        return "FriendsGame " + id;
    }
    
    public boolean typeSpecificSave() //TODO
    {
        return true;
    }
    
    public boolean typeSpecificLoad() //TODO
    {
        return false;
    }
}
