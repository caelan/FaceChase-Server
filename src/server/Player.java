package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

import util.FileSystem;
import util.General;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_core.*;

public class Player 
{
    private int id;
    private String username;
    private String profilePicture;
    
    private String email;
    private String name;
    private String password;
    
    private HashSet<Integer> friends;   
    private HashMap<Game, Status> gameStatus;   
    private LinkedList<IplImage> facesToBeSaved;    
    
    private final String saveData = "\\saveData.dat";
    private String saveDir;
    
    private int faceCount;

    public Player(int id, String refDir, boolean load)
    {
        this.id = id;
        this.saveDir = refDir + "\\p" + id;
        
        friends = new HashSet<Integer>();
        gameStatus = new HashMap<Game, Status>();
        facesToBeSaved = new LinkedList<IplImage>();
        
        if(load)
            load();
    }
    
    public Player(int id, String refDir, String email, String password, String name)
    {
        this.id = id;
        //this.username = username;
        //this.name = name;
        this.email = email;
        this.password = General.hashPassword(password);
        this.name = name;
        this.saveDir = refDir + "\\p" + id;
        this.faceCount = 0;
        
        friends = new HashSet<Integer>();
        gameStatus = new HashMap<Game, Status>();
        facesToBeSaved = new LinkedList<IplImage>();
    }
    
    public void addToGame(Game g, Status s)
    {
        //Check if already in the game?
        gameStatus.put(g, s);
        //TODO load images for the game
    }
    
    public void addFace(IplImage i) //TODO save faces that were matches for better data later
    {
        for(Game g: gameStatus.keySet())
            g.updateRecognition(i, id);
        
        facesToBeSaved.add(i);
    }
    
    public synchronized void addFriend(Player p)
    {
        friends.add(p.getID());
        //Go through game - don't store targets in gamestatus for this one
    }
    
    public boolean save()
    {
        for(IplImage face: facesToBeSaved)
        {
            faceCount++;
            cvSaveImage(saveDir + "\\f" + faceCount + ".jpg", face);
            cvReleaseImage(face);
        }
        
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
            out.write("id " + id + "\n");
            out.write("email " + email + "\n");
            out.write("password " + password + "\n"); //TODO hash this
            out.write("name " + name + "\n");
            out.write("faceCount " + faceCount + "\n");
            
            /*
            out.write("Games:\n");
            for(Game g: gameStatus.keySet())
                out.write("" + g.getID());
            */
            
            out.write("Friends:\n");
            for(int i: friends)
                out.write("" + i);

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
            Scanner scanner =  new Scanner(new File(saveDir + saveData));
            scanner.nextLine(); //Player Name
            scanner.nextLine(); //Player ID
            email = scanner.nextLine().split(" ")[1];
            password = scanner.nextLine().split(" ")[1];
            name = scanner.nextLine().split(" ")[1];
            faceCount = Integer.parseInt(scanner.nextLine().split(" ")[1]);

            /*
            scanner.nextLine(); //Games:
            String line = "";
            while (scanner.hasNextLine()) //TODO could just not save games on the player side and instead load
            {
                line = scanner.nextLine();
                if(line.equals("Friends:"))
                    break;
                int gameID = Integer.parseInt(line);
                gameStatus.put(gameID, null);
            }*/
            
            while (scanner.hasNextLine())
            {
                friends.add(Integer.parseInt(scanner.nextLine()));
            }
            
            scanner.close();

        }
        catch (Exception e)
        {
            return false;
        }     
        return true;
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
    
    public HashMap<Game, Status> getGames()
    {
        return gameStatus;
    }

    public String getEmail() {
        return email;
    }
}
