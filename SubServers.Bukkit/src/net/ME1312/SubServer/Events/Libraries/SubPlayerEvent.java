package net.ME1312.SubServer.Events.Libraries;

import net.ME1312.SubServer.Executable.SubServer;
import net.ME1312.SubServer.SubPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event.Result;

/**
 * General Event Class for events that can be Triggered by a Player
 *
 * @author ME1312
 */
public class SubPlayerEvent extends SubEvent {
    protected OfflinePlayer Player;
    protected Result Cancelled;

    protected SubPlayerEvent(SubPlugin SubPlugin, EventType Event, SubServer Server, OfflinePlayer Player) {
        super(SubPlugin, Event, Server);
        this.Player = Player;
        this.Cancelled = Result.DEFAULT;
    }

    /**
     * Gets the player that Triggered the Event
     * @return The Player that triggered this Event or Null if Console
     */
    public OfflinePlayer getPlayer() { return Player; }
}