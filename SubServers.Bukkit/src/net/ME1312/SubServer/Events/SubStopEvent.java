package net.ME1312.SubServer.Events;

import net.ME1312.SubServer.Events.Libraries.EventType;
import net.ME1312.SubServer.Events.Libraries.SubPlayerEvent;
import net.ME1312.SubServer.Executable.SubServer;
import net.ME1312.SubServer.SubPlugin;
import org.bukkit.OfflinePlayer;

/**
 * Stop Server Event Class
 */
public class SubStopEvent extends SubPlayerEvent {

    /**
     * Server Stop Event
     *
     * @param Server Server to be Stopped
     * @param Player Player Stopping Server
     */
    public SubStopEvent(SubPlugin SubPlugin, SubServer Server, OfflinePlayer Player) {
        super(SubPlugin, EventType.SubStopEvent, Server, Player);
    }
}
