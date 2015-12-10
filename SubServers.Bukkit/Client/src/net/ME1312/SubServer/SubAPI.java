package net.ME1312.SubServer;

import net.ME1312.SubServer.Executable.SubServer;
import net.ME1312.SubServer.Libraries.Config.ConfigFile;
import net.ME1312.SubServer.Libraries.Version.Version;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This is the API File for Subservers<br>
 * <br>
 * NOTES:<br>
 *   Methods ending in "All" don't effect the Proxy<br>
 *   Methods can be Requested<br>
 *
 * @author ME1312
 * @version 1.8.8s+
 *
 */
@SuppressWarnings("static-access")
public class SubAPI {
    private static SubPlugin SubPlugin;

    protected SubAPI(SubPlugin SubPlugin) {
        this.SubPlugin = SubPlugin;
    }

    /**
     * Execute Command on All Remote Servers
     *
     * @param Command The Command to Execute
     */
    public static void sendCommandToAll(String Command) {
        for(Iterator<String> str = SubPlugin.SubServers.iterator(); str.hasNext(); ) {
            String item = str.next();
            if (!item.equalsIgnoreCase("~Proxy") && SubPlugin.Servers.keySet().contains(SubPlugin.PIDs.get(item)) && getSubServer(item).isRunning()) {
                getSubServer(item).sendCommand(Command);
            }
        }
    }

    /**
     * Execute Command on All Remote Servers
     *
     * @param Command The Command to Execute
     * @param Sender The player who sent this Command
     */
    public static void sendCommandToAll(OfflinePlayer Sender, String Command) {
        for(Iterator<String> str = SubPlugin.SubServers.iterator(); str.hasNext(); ) {
            String item = str.next();
            if (!item.equalsIgnoreCase("~Proxy") && SubPlugin.Servers.keySet().contains(SubPlugin.PIDs.get(item)) && getSubServer(item).isRunning()) {
                getSubServer(item).sendCommand(Sender, Command);
            }
        }
    }

    /**
     * Stop All Remote Servers
     */
    public static void stopAll() {
        for(Iterator<String> str = SubPlugin.SubServers.iterator(); str.hasNext(); ) {
            String item = str.next();
            if (!item.equalsIgnoreCase("~Proxy") && SubPlugin.Servers.keySet().contains(SubPlugin.PIDs.get(item)) && getSubServer(item).isRunning()) {
                getSubServer(item).stop();
            }
        }
    }

    /**
     * Stop All Remote Servers
     *
     * @param Sender The player who sent this Command
     */
    public static void stopAll(OfflinePlayer Sender) {
        for(Iterator<String> str = SubPlugin.SubServers.iterator(); str.hasNext(); ) {
            String item = str.next();
            if (!item.equalsIgnoreCase("~Proxy") && SubPlugin.Servers.keySet().contains(SubPlugin.PIDs.get(item)) && getSubServer(item).isRunning()) {
                getSubServer(item).stop(Sender);
            }
        }
    }

    /**
     * Get SubServers from the Configuration
     *
     * @return List<SubServer> Of all Servers Defined in the Configuration
     */
    public static List<SubServer> getSubServers() {
        List<SubServer> Server = new ArrayList<SubServer>();
        Server.addAll(SubPlugin.Servers.values());
        return Server;
    }

    public static SubServer getSubServer(int PID) {
        return SubPlugin.Servers.get(PID);

    }

    public static SubServer getSubServer(String Name) {
        return SubPlugin.Servers.get(SubPlugin.PIDs.get(Name));

    }

    /**
     * Gets the Lang File
     *
     * @return The lang file's nodes
     */
    public static HashMap<String, String> getLang() { return SubPlugin.lang; }

    /**
     * Gets the SubServers Version
     *
     * @return The SubServers Version
     */
    public static Version getPluginVersion() { return SubPlugin.PluginVersion; }

    public static Version getMinecraftVersion() { return SubPlugin.MCVersion; }
}
