package server;

import java.awt.GridLayout;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import util.Pair;

import facialRecognition.ImageFormat;

public class Server {
    private ServerComm comm;
    private PlayerPool playerPool;
    private GamePool gamePool;
    
    /*
     * TODO check to see if an existing server is running...
     * Services running
     * Network checking
     * 
     * Have to be connected to the same WIFI network now...
     * 
     * Have multiple saves/loads
     * 
     * Save images 
     * 
     */

    public Server(int port, String workDir, boolean load) throws IOException {
        if(load)
            System.out.println("Loading");
        else
            System.out.println("Initializing");
        
        //Load/Save
        comm = new ServerComm(this, port);
        playerPool = new PlayerPool(workDir, load);
        gamePool = new GamePool(playerPool, workDir, load);
        
        if(!load)
            initializeOneFriendsGame();
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               JPanel inputPanel = new JPanel();
               inputPanel.setLayout(new GridLayout(1, 1));
               
               //IP Address
               inputPanel.add(new JLabel("Terminate the Server?"));       
                                         
               String[] options = {"Save and Quit", "Quit"};
               int i = JOptionPane.showOptionDialog(
                       null,
                       inputPanel,
                       "Terminate the Server?",
                       JOptionPane.YES_NO_OPTION,
                       JOptionPane.PLAIN_MESSAGE,
                       null,     //do not use a custom Icon
                       options,  //the titles of buttons
                       options[0]); //default button title
               
                //i = 0 indicates Save and Quit selected. i = -1 indicates closed window. i = 1 indicates Quit selected.
                if(i != 0) //TODO make the exiting more peaceful 
                {
                    //User canceled/closed Dialog
                    try{
                    //comm.stop(); //TODO java.net.SocketException: socket closed error
                    } catch (Exception e)
                    {
                        System.err.println("Could not close connections");
                    }
                    System.out.println("Server Terminated Without Saving");
                    System.exit(0);
                }
                else
                {
                    System.out.println("Saving");
                    save();
                    try{
                    //comm.stop();
                    } catch (Exception e)
                    {
                        System.err.println("Could not close connections");
                    }
                    System.out.println("Server Terminated After Saving");
                    System.exit(0);
                }
            }
        });        
        
        System.out.println("FaceChase Server Online at IP Address " + getLocalIpAddress() + " and Port " + port);
        comm.serve();

    }
    
    public boolean save()
    {
        boolean b = playerPool.save();
        b = gamePool.save() && b;
        return b;
    }
    
    public boolean load()
    {
        boolean b = playerPool.load();
        b = gamePool.load() && b;
        return b;
    }
    
    public void initializeOneFriendsGame()
    {
        gamePool.addFriendsGame();
    }
    
    public Player getPlayer(int id)
    {
        return playerPool.getPlayer(id);
    }
    
    public void addPlayerToGame(Player p, Game g)
    {
        Status s = g.addPlayer(p);
        p.addToGame(g, s);
    }
        
    public Integer addPlayer(String name, String email, String password, IOThread thread)
    {
        Player p = playerPool.addPlayer(email, password, name);
        if(p == null)
        {
            return null;
        }
        
        addPlayerToGame(p, gamePool.getGame(0));
        
        Integer id = p.getID();
        if(thread.registerThread(id))
        {
            return id;
        }
        else
        {
            return null;
        }        
    }
    
    public Player login(String email, String password, IOThread thread)
    {
        Player p = playerPool.login(email, password);
        if(p == null)
        {
            return null;
        }

        Integer id = p.getID();
        if(thread.registerThread(id))
        {
            return p;
        }
        else
        {
            return null;
        }
    }
    
    public Pair<Player, Player> requestKill(int killerID, String image) //What if different facial recs in different games don't agree
    {
        Player killer = playerPool.getPlayer(killerID);
 
        IplImage iplimage = ImageFormat.convertToImage(image);
        
        for(Game g: killer.getGames().keySet()) //Stop at first game for now
        {
            Player dead = g.killRequest(killer, iplimage);
            if(dead == null)
                continue;
            
            //Something about location from killer
            
            return new Pair<Player, Player>(killer, dead);          
        }      
        return null;
    }
    
    public boolean addImage(int id, String image)
    {
        Player p = playerPool.getPlayer(id);
        IplImage iplimage = ImageFormat.convertToImage(image);
        cvSaveImage("testing.jpg", iplimage);        
        return p.addFace(iplimage);       
    }
    
    public boolean addFriend(int id, String name) //TODO 
    {
        Player p1 = playerPool.getPlayer(id);
        Player p2 = playerPool.getPlayer(name);
        if(p2 == null || p1.equals(p2))
            return false;
        
        p1.addFriend(p2);
        p2.addFriend(p1);
        
        return true;
    }
    
    private static String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "Unable to Obtain IP";
        }
    }
    

    public static void main(String[] args) {   
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, just use the default
            UIManager.put("swing.boldMetal", Boolean.FALSE);
        }
            
        //JOptionPane for prompting which port to connect to 
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 1));
        
        //Prompts port number for server
        inputPanel.add(new JLabel("Initializing Server"));    
        inputPanel.add(new JLabel("Server IP: "+ getLocalIpAddress()));               
        inputPanel.add(new JLabel("Enter Port Number:"));
        JTextField port = new JTextField(10);
        port.setText("" + 4444);
        inputPanel.add(port);
                      
        String[] options = {"New", "Load"};
        int i = JOptionPane.showOptionDialog(
                null,
                inputPanel,
                "Initiate Server",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,     //do not use a custom Icon
                options,  //the titles of buttons
                options[0]); //default button title
        
         boolean load = false;
         //i = 0 indicates New selected. i = -1 indicates closed window. i = 1 indicates Load selected.
         if(i != 0 && i != 1)
         {
             System.out.println("User aborted server creation");
             System.exit(0);
         }
         else if(i == 1)
         {
             load = true;
         }
                  
         //Ensures that the port number is an integer. If not, sets to the default number of 4444
         int portNumber = 4444;
         try{
             portNumber = Integer.parseInt(port.getText());
         }catch(NumberFormatException e){
             System.out.println("Invalid port number. Using default port " + portNumber);
         }

        try
        {
            String directory = System.getProperty("user.dir");
            Server server = new Server(portNumber, directory, load);
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.print("Server Crashed " + e.getStackTrace());
        }
    }
}
//Run "telnet localhost 4444" in the Command line
