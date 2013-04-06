package server;

import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;


public class Server {
    private ServerComm comm;
    private PlayerPool playerPool;
    private GamePool gamePool;

    public Server(int port, String workDir, boolean load) throws IOException {
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
                    comm.stop();
                    } catch (Exception e)
                    {
                        System.err.println("Could not close connections");
                    }
                    System.out.println("Server Terminated Without Saving");
                    System.exit(0);
                }
                else
                {
                    save();
                    try{
                    comm.stop();
                    } catch (Exception e)
                    {
                        System.err.println("Could not close connections");
                    }
                    System.out.println("Server Terminated After Saving");
                    System.exit(0);
                }
            }
        });
        
        
        System.out.println("FaceChase Server Online at Port: " + port);
        //Load/Save
        comm = new ServerComm(this, port);
        playerPool = new PlayerPool(workDir);
        gamePool = new GamePool(workDir, load);
        
        if(!load)
            initializeOneFriendsGame();
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
    
    public void addPlayerToGame(Player p, Game g)
    {
        Status s = g.createPlayerStatus(p);
        p.addToGame(g, s);
    }
    
    public String addPlayer(String username, String name)
    {
        Player p = playerPool.addPlayer(username, name);
        if(p == null)
            return "Username Taken";
        addPlayerToGame(p, gamePool.getGame(0));
        System.out.println("Added " + p);
        
        return "User " + p.getID() + "Added";
    }
    
    public String processKill(int killerID, String image)
    {
        Player killer = playerPool.getPlayer(killerID);
        if(killer == null)
            return "Invalid ID";
        
        //Something about location from killer
        
        //parse image
        
        
        
        return "";
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
        inputPanel.setLayout(new GridLayout(3, 1));
        
        //Prompts port number for server
        inputPanel.add(new JLabel("Initializing Server"));               
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
            System.out.print("Server Crashed " + e.getStackTrace());
        }
    }
}
//Run "telnet localhost 4444" in the Command line
