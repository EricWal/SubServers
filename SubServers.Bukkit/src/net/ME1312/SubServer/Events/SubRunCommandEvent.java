package net.ME1312.SubServer.Events;

import net.ME1312.SubServer.Events.Libraries.EventType;
import net.ME1312.SubServer.Executable.SubServer;
import net.ME1312.SubServer.Events.Libraries.SubEvent;
import net.ME1312.SubServer.Events.Libraries.SubPlayerEvent;
import net.ME1312.SubServer.SubPlugin;
import org.bukkit.OfflinePlayer;

/**
 * The Event for a Server Running a Command
 *
 * @author ME1312
 */
public class SubRunCommandEvent extends SubPlayerEvent {
    private String Command;

    /**
     * Run Command Event
     *
     * @param Server Server to be run on
     * @param Player Player Running command
     * @param Command Command to be run
     */
    public SubRunCommandEvent(SubPlugin SubPlugin, SubServer Server, OfflinePlayer Player, String Command) {
        super(SubPlugin, EventType.SubRunCommandEvent, Server, Player);
        this.Command = Command;
    }

    /**
     * Gets the inputed Command
     * @return Command in String Form
     */
    public String getCommand() { return Command; }
}