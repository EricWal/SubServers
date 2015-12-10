package net.ME1312.SubServer.Libraries.Events;

import net.ME1312.SubServer.Executable.SubServer;
import net.ME1312.SubServer.SubPlugin;
import org.bukkit.OfflinePlayer;

/**
 * The Event for a Server Running a Command
 *
 * @author ME1312
 */
public class SubRunCommandEvent extends SubPlayerEvent {
    private String Command;

    public SubRunCommandEvent(net.ME1312.SubServer.SubPlugin SubPlugin, SubServer Server, OfflinePlayer Player, String Command) {
        super(SubPlugin, Events.SubRunCommandEvent, Server, Player);
        this.Command = Command;
    }

    /**
     * Gets the inputed Command
     * @return Command in String Form
     */
    public String getCommand() { return Command; }
}