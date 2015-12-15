package net.ME1312.SubServer.Events;

import net.ME1312.SubServer.Events.Libraries.EventType;
import net.ME1312.SubServer.Events.Libraries.SubPlayerEvent;
import net.ME1312.SubServer.Executable.SubServer;
import org.bukkit.OfflinePlayer;

/**
 * Stop Command Event
 */
public class SubStopEvent extends SubPlayerEvent {

    public SubStopEvent(SubServer Server, OfflinePlayer Player) {
        super(EventType.SubStopEvent, Server, Player);
    }
}
