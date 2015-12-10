package net.ME1312.SubServer.Libraries.Events;

import net.ME1312.SubServer.Executable.SubCreator;
import net.ME1312.SubServer.Executable.SubServer;
import net.ME1312.SubServer.SubPlugin;
import org.bukkit.OfflinePlayer;

/**
 * General Event Class for events that can be Triggered by a Player
 *
 * @author ME1312
 *
 */
public class SubPlayerEvent extends SubEvent {
    protected OfflinePlayer Player;

    protected SubPlayerEvent(net.ME1312.SubServer.SubPlugin SubPlugin, Events Event, SubServer Server, OfflinePlayer Player) {
        super(SubPlugin, Event, Server);
        this.Player = Player;
    }

    /**
     * Gets the player that Triggered the Event
     * @return The Player that triggered this Event or Null if Console
     */
    public OfflinePlayer getPlayer() { return Player; }
}