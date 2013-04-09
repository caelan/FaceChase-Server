package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Scanner;

import util.General;

public class FriendsStatus extends Status
{
    private Long lastKilled;
    private final double deathTime = General.minToMilli(3); //In milliseconds
    
    public FriendsStatus(Game g, Player p)
    {
        super(g, p);
        getScore();
        lastKilled = null;
    }
    
    public boolean kill()
    {
        if(alive())
        {
            kills++;
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public boolean die()
    {
        if(alive())
        {
            lastKilled = System.nanoTime();
            deaths++;
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public double getScore()
    {
        score = kills-deaths;
        return score;
    }
    
    public boolean alive()
    {
        if(lastKilled == null || General.nanoToMilli(System.nanoTime() - lastKilled) >= deathTime)
        {
            lastKilled = null;
            return true;
        }
        else
        {
            return false;
        }
    }    
    
    public boolean typeSpecificSave(BufferedWriter out)
    {
        try {
            out.write("lastKilled " + lastKilled + "\n");
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    
    public boolean typeSpecificLoad(Scanner scanner)
    {
        try {
            lastKilled = General.parseLong(scanner.nextLine().split(" ")[1]);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
