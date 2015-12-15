package net.ME1312.SubServer.Events;

import net.ME1312.SubServer.Executable.SubCreator;
import org.bukkit.OfflinePlayer;

/**
 * The Event for a Player Creating a SubServer
 *
 * @author ME1312
 */
public class SubCreateEvent {
    private SubCreator.ServerType Type;
    private boolean cancelled;

    public SubCreateEvent(OfflinePlayer Player, SubCreator.ServerType Type) {
        super();
        this.Type = Type;
    }

    /**
     * Gets the Type of server being Created
     * @return The Type
     */
    public SubCreator.ServerType getType() { return Type; }

    /**
     * Gets the player that Triggered the Event
     * @return The Player that triggered this Event or Null if Console
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Cancel/Uncancel this event
     */
    public void setCancelled(boolean value) {
        cancelled = value;
    }
}