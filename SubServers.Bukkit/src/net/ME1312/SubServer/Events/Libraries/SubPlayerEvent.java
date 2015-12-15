package net.ME1312.SubServer.Events.Libraries;

import net.ME1312.SubServer.Executable.SubServer;
import org.bukkit.OfflinePlayer;

/**
 * General Event Class for events that can be Triggered by a Player
 *
 * @author ME1312
 */
public class SubPlayerEvent extends SubEvent {
    protected OfflinePlayer Player;
    protected boolean Cancelled;

    protected SubPlayerEvent(EventType Event, SubServer Server, OfflinePlayer Player) {
        super(Event, Server);
        this.Player = Player;
        this.Cancelled = false;
    }

    /**
     * Gets the player that Triggered the Event
     * @return The Player that triggered this Event or Null if Console
     */
    public OfflinePlayer getPlayer() { return Player; }

    /**
     * Gets if you have cancelled this event
     * @return if You've cancelled this event
     */
    public boolean isCancelled() { return Cancelled; }

    /**
     * Cancel/Uncancel this event
     */
    public void setCancelled(boolean value) { Cancelled = value; }
}