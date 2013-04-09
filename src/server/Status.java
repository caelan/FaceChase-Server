package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import util.General;

public abstract class Status implements Comparable<Status> {
    protected double score;
    protected Player player;
    protected Game game;
    protected int kills;
    protected int deaths;
    
    public Status(Game g, Player p)
    {
        this.player = p;
        this.game = g;
        kills = 0;
        deaths = 0;
    }
    
    public abstract boolean kill();
    public abstract boolean die();
    public abstract boolean alive();
    public abstract double getScore();
    public abstract boolean typeSpecificSave(BufferedWriter out);
    public abstract boolean typeSpecificLoad(Scanner scanner);
    
    public boolean save()
    {
        try{            
            FileWriter fstream = new FileWriter(game.saveDir + "\\s" + player.getID() + ".dat");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(toString() + "\n");
            out.write("kills " + kills + "\n");
            out.write("deaths " + deaths + "\n");
            out.write("score " + score + "\n");
            
            if(!typeSpecificSave(out))
            {
                out.close();
                return false;
            }
            out.close();
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }
    
    public boolean load()
    {
        try 
        {           
            Scanner scanner =  new Scanner(new File(game.saveDir + "\\s" + player.getID() + ".dat"));
            scanner.nextLine(); //Game, Player
            
            kills = General.parseInt(scanner.nextLine().split(" ")[1]);
            deaths = General.parseInt(scanner.nextLine().split(" ")[1]);
            score = General.parseDouble(scanner.nextLine().split(" ")[1]);
      
            if(!typeSpecificLoad(scanner))
            {
                scanner.close();
                return false;
            }
            scanner.close();
        }
        catch (Exception e)
        {
            return false;
        }     
        return true;
    }
    
    public int compareTo(Status s)
    {
        if(getScore() < s.getScore())
            return -1;
        else if(getScore() < s.getScore())
            return 1;
        else
            return 0;
    }
    
    public String toString()
    {
        return game + ", " + player;
    }
}
