package server;

import java.io.File;
import java.io.IOException;


public class Server {
    private ServerComm comm;
    private PlayerPool playerPool;
    private GamePool gamePool;

    public Server(int port, File faceDirectory) throws IOException {
        System.out.println("FaceChase Server Online at Port: " + port);
        //Load/Save
        comm = new ServerComm(this, port);
        playerPool = new PlayerPool(faceDirectory);
        gamePool = new GamePool();
        initializeOneFriendsGame();
        comm.serve();
    }
    
    public void initializeOneFriendsGame()
    {
        gamePool.addFriendsGame();
    }
    
    public void addPlayerToGame(Player p, Game g)
    {
        Status s = g.createPlayerStatus(p);
        p.addToGame(g, s);
    }
    
    public String addPlayer(String username, String name)
    {
        int id = playerPool.addPlayer(username, name);
        addPlayerToGame(playerPool.getPlayer(id), gamePool.getGame(0));
        return ""+id;
    }
    
    public String processKill(int killerID, String image)
    {
        return "";
    }

    public static void main(String[] args) {   
        int port = 4444;
        try
        {
            Server server = new Server(port, new File(""));
        }
        catch(Exception e){
            System.out.print("Server Crashed " + e.getStackTrace());
        }
    }
}
//Run "telnet localhost 4444" in the Command line
