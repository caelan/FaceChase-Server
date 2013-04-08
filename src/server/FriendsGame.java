package server;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import facialRecognition.FacialRecognition;

import util.Constants;
import util.Pair;

public class FriendsGame extends Game {
    public FriendsGame(PlayerPool playerPool, int id, String refDir, boolean load)
    {
        super(playerPool, id, refDir, load);
        if(load)
        {
        }
        else
        {
            facesPerPerson = 1;
            joinAfterCreation = true;
            killThreshold = null;
            testFaces = 10;
            if(testFaces > 0)
                facialRec = new FacialRecognition(testFaces, saveDir, Constants.classifierType, Constants.faceSize, facesPerPerson);
            else
                facialRec = new FacialRecognition(saveDir, Constants.classifierType, Constants.faceSize, facesPerPerson);
        }
    }
    
    public Status createPlayerStatus(Player p)
    {
        Status s = new FriendsStatus(this, p);
        playerStatus.put(p, s);
        return s;
    }
    
    public Player killRequest(Player killer, IplImage image)
    {
        Pair<Integer, Double> results = facialRec.predictConfidence(image);
        if(killThreshold == null || results.getSecond() >= killThreshold)
        {
            int id = results.getFirst();
            if(id < 0) //one of the test faces
                return null;
            
            Player p = playerPool.getPlayer(id);
            
            //Location stuff...
            //Edit the status
            
            if(killer.getFriends().contains(id))
                return p;
            else
                return null;
        }
        else
        {
            return null;
        }
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
        return true;
    }
}
