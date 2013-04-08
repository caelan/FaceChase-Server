package server;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class Player 
{
    private int id;
    private String username;
    private String name;
    
    private File faceFolder;
    private String profilePicture;
    
    private String password;
    private String email;
    
    private HashSet<Integer> friends;   
    private HashMap<Game, Status> gameStatus;
    
    public Player(int id, String email, String password)
    {
        this.id = id;
        //this.username = username;
        //this.name = name;
        this.email = email;
        this.password = password;
        friends = new HashSet<Integer>();
        gameStatus = new HashMap<Game, Status>();
    }
    
    public void addToGame(Game g, Status s)
    {
        //Check if already in the game?
        gameStatus.put(g, s);
    }
    
    public void addFace()
    {
        
    }
    
    public String toString()
    {
        return "Player " + id + " | Email: " + email;
    }
    
    public int hashCode()
    {
        return id;
    }
    
    public boolean equals(Player p)
    {
        return id == p.getID();
    }  

    public int getID()
    {
        return id;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public HashSet<Integer> getFriends()
    {
        return friends;
    }
}
