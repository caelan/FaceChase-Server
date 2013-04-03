package server;

public class FriendsGame extends Game {
    public FriendsGame(int id)
    {
        super(id);
        joinAfterCreation = true;
    }
    
    public Status createPlayerStatus(Player p)
    {
        Status s = new FriendsStatus(p);
        playerStatus.put(p, s);
        return s;
    }
}
