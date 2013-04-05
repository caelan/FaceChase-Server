package server;

import java.util.HashSet;

public class FriendsStatus extends Status
{
    public FriendsStatus(Game g, Player p)
    {
        super(g, p);
        score = 0;
        targets = new HashSet<Integer>();
        for(int id: p.getFriends())
        {
            //If in the game
            targets.add(id);
        }
    }
}
