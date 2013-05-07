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
            facesPerPerson = 3;
            joinAfterCreation = true;
            killThreshold = null;
            testFaces = 3;
            if(testFaces > 0)
                facialRec = new FacialRecognition(testFaces, saveDir, Constants.classifierType, Constants.faceSize, facesPerPerson);
            else
                facialRec = new FacialRecognition(saveDir, Constants.classifierType, Constants.faceSize, facesPerPerson);
        }
    }
    
    public Status addPlayer(Player p)
    {
        Status s = new FriendsStatus(this, p);
        playerStatus.put(p, s);
        //p.addToGame(this, s); //Somewhere else
        return s;
    }
    
    public Player killRequest(Player killer, IplImage image)
    {
        Pair<Integer, Double> results = facialRec.predictConfidenceFail(image);
        if(results == null)
            return null;
        System.out.println(results);
        if(killThreshold == null || results.getSecond() >= killThreshold)
        {
            int id = results.getFirst();
            if(id < 0) //one of the test faces
                return null;
            
            Player dead = playerPool.getPlayer(id);
            
            if(!killer.getFriends().contains(id))
                return null;

            
            if(!playerStatus.containsKey(killer))//Don't actually need
                return null;            
            Status s1 = playerStatus.get(killer);
            
            if(!playerStatus.containsKey(dead)) //Don't actually
                return null;
            
            Status s2 = playerStatus.get(dead);
                        
            if(!s1.alive() || !s2.alive())
                return null;

            s1.kill();
            s2.die();
            
            //Location stuff...
            
            return dead;
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
