package net.ME1312.SubServer.Events;

import net.ME1312.SubServer.Events.Libraries.EventType;
import net.ME1312.SubServer.Events.Libraries.SubPlayerEvent;
import net.ME1312.SubServer.Executable.SubServer;
import org.bukkit.OfflinePlayer;

/**
 * Start Server Event
 */
public class SubStartEvent extends SubPlayerEvent {
    public SubStartEvent(SubServer Server, OfflinePlayer Player) {
        super(EventType.SubStartEvent, Server, Player);
    }
}
