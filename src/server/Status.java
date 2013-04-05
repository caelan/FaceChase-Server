package server;

import java.util.HashSet;

public abstract class Status {
    protected int score;
    protected HashSet<Integer> targets;
    protected Player player;
    protected Game game;
    
    public Status(Game g, Player p)
    {
        this.player = p;
        this.game = g;
    }
}
