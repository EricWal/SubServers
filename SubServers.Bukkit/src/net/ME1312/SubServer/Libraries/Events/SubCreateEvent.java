package net.ME1312.SubServer.Libraries.Events;

import net.ME1312.SubServer.Executable.SubCreator;
import net.ME1312.SubServer.Executable.SubServer;
import org.bukkit.OfflinePlayer;

/**
 * The Event for a Player Creating a SubServer
 *
 * @author ME1312
 *
 */
public class SubCreateEvent extends SubPlayerEvent {
    private SubCreator.ServerTypes Type;

    public SubCreateEvent(net.ME1312.SubServer.SubPlugin SubPlugin, SubServer Server, OfflinePlayer Player, SubCreator.ServerTypes Type) {
        super(SubPlugin, Events.SubCreateEvent, Server, Player);
        this.Type = Type;
    }

    public SubCreator.ServerTypes getType() { return Type; }
}