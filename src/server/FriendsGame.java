package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import util.FileSystem;

public class FriendsGame extends Game {
    public FriendsGame(int id, String refDir)
    {
        super(id, refDir);
        joinAfterCreation = true;
    }
    
    public Status createPlayerStatus(Player p)
    {
        Status s = new FriendsStatus(this, p);
        playerStatus.put(p, s);
        return s;
    }
    
    public boolean save() //Could inherit instead of implement
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
            out.write("FriendsGame\n");
            
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
}
