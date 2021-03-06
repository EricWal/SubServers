package net.ME1312.SubServer.Events;

import net.ME1312.SubServer.Events.Libraries.Event;
import net.ME1312.SubServer.Events.Libraries.EventType;
import net.ME1312.SubServer.Executable.SubCreator;
import net.ME1312.SubServer.SubPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;

/**
 * The Event for a Player Creating a SubServer
 *
 * @author ME1312
 */
public class SubCreateEvent extends Event {
    private SubCreator.ServerType Type;
    private Player Player;
    protected Result Cancelled;

    /**
     * SubServer Create Event
     *
     * @param Player Player Creating
     * @param Type Type of Server
     */
    public SubCreateEvent(SubPlugin SubPlugin, Player Player, SubCreator.ServerType Type) {
        super(SubPlugin, EventType.SubCreateEvent);
        this.Player = Player;
        this.Type = Type;
    }

    /**
     * Gets the Player creating this Server
     *
     * @return The Player
     */
    public Player getPlayer() {
        return Player;
    }

    /**
     * Gets the Type of server being Created
     * @return The Type
     */
    public SubCreator.ServerType getType() { return Type; }

    /**
     * Gets if you have cancelled this event
     * @return if You've cancelled this event
     */
    public Result getStatus() { return Cancelled; }

    /**
     * Cancel/Uncancel this event
     */
    public void setStatus(boolean value) {
        if (value) {
            Cancelled = org.bukkit.event.Event.Result.ALLOW;
        } else {
            Cancelled = org.bukkit.event.Event.Result.DENY;
        }
    }
}