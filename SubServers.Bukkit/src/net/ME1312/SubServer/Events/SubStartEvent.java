package net.ME1312.SubServer.Events;

import net.ME1312.SubServer.Events.Libraries.EventType;
import net.ME1312.SubServer.Events.Libraries.SubPlayerEvent;
import net.ME1312.SubServer.Executable.SubServer;
import net.ME1312.SubServer.SubPlugin;
import org.bukkit.OfflinePlayer;

/**
 * Start Server Event Class
 */
public class SubStartEvent extends SubPlayerEvent {

    /**
     * Server Start Event
     *
     * @param Server Server Starting
     * @param Player Player Starting Server
     */
    public SubStartEvent(SubPlugin SubPlugin, SubServer Server, OfflinePlayer Player) {
        super(SubPlugin, EventType.SubStartEvent, Server, Player);
    }
}
