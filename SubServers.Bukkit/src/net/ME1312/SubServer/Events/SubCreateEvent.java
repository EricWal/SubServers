package net.ME1312.SubServer.Events;

import net.ME1312.SubServer.Events.Libraries.Event;
import net.ME1312.SubServer.Events.Libraries.EventType;
import net.ME1312.SubServer.Executable.SubCreator;
import net.ME1312.SubServer.SubPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event.Result;

/**
 * The Event for a Player Creating a SubServer
 *
 * @author ME1312
 */
public class SubCreateEvent extends Event {
    private SubCreator.ServerType Type;
    protected Result Cancelled;

    /**
     * SubServer Create Event
     *
     * @param Player Player Creating
     * @param Type Type of Server
     */
    public SubCreateEvent(SubPlugin SubPlugin, OfflinePlayer Player, SubCreator.ServerType Type) {
        super(SubPlugin, EventType.SubCreateEvent);
        this.Type = Type;
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