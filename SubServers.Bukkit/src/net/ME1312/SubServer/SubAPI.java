package net.ME1312.SubServer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.ME1312.SubServer.Libraries.Config.ConfigFile;
import net.ME1312.SubServer.Libraries.Events.SubEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.ME1312.SubServer.Executable.Executable;
import net.ME1312.SubServer.Executable.SubServer;
import net.ME1312.SubServer.Libraries.Events.SubListener;
import net.ME1312.SubServer.Libraries.Version.Version;

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
     * Creates a SubServer
     *
     * @param Name Name of SubServer
     * @param Port Port of SubServer
     * @param Log Toggle Output to console
     * @param SharedChat Toggle Shared Chat
     * @param Dir Shell Directory
     * @param Exec Executable String or File
     * @param AutoRestart AutoRestarts Server
     * @param Temporary Toggles Temporary Server actions
     */
    public static void addServer(final String Name, int Port, boolean Log, boolean SharedChat, File Dir, Executable Exec, boolean AutoRestart, boolean Temporary) {
        final int PID = (SubPlugin.SubServers.size() + 1);
        if (Temporary) {
            SubPlugin.Servers.put(PID, new SubServer(true, Name, PID, Port, Log, SharedChat, Dir, Exec, false, true, SubPlugin));
        } else {
            SubPlugin.Servers.put(PID, new SubServer(true, Name, PID, Port, Log, SharedChat, Dir, Exec, AutoRestart, false, SubPlugin));
        }
        SubPlugin.PIDs.put(Name, PID);
        SubPlugin.SubServers.add(Name);
        Bukkit.getLogger().info("Servers: " + SubPlugin.Servers.toString());
        Bukkit.getLogger().info("PIDs: " + SubPlugin.PIDs.toString());
        Bukkit.getLogger().info("SubServers: " + SubPlugin.SubServers.toString());

        if (SubPlugin.sql != null) {
            try {
                Statement update = SubPlugin.sql.getConnection().createStatement();
                update.executeUpdate("INSERT INTO `SubServers` (`Name`, `IP`, `PID`, `Enabled`, `Shared_Chat`, `Temp`, `Running`) VALUES " +
                        "('"+ Name +"', '"+ SubPlugin.config.getString("Settings.Server-IP")+":"+Integer.toString(Port) +"', '"+ PID +"', '1', '"+ ((SharedChat)?"1":"0") +"', '"+ ((Temporary)?"1":"0") +"', '0')");
                update.close();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Problem Syncing Database!");
                e.printStackTrace();
            }
        } else if (getSubServer(0).isRunning()) getSubServer(0).sendCommandSilently("subconf@proxy addserver " + Name + " " + SubPlugin.config.getString("Settings.Server-IP") + " " + Port + " " + SharedChat);

        if (Temporary) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    SubPlugin.Servers.get(PID).start();
                    try {
                        Thread.sleep(1500);
                        SubPlugin.Servers.get(SubPlugin.PIDs.get(Name)).waitFor();
                        Thread.sleep(1000);
                        SubPlugin.Servers.get(SubPlugin.PIDs.get(Name)).destroy();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (SubPlugin.sql != null) {
                        try {
                            Statement update = SubPlugin.sql.getConnection().createStatement();
                            update.executeUpdate("DELETE FROM `SubServers` WHERE PID='"+ PID + "'");
                            update.close();
                        } catch (SQLException e) {
                            Bukkit.getLogger().severe("Problem Syncing Database!");
                            e.printStackTrace();
                        }
                    } else if (getSubServer(0).isRunning()) getSubServer(0).sendCommandSilently("subconf@proxy removeserver " + Name);

                    SubPlugin.Servers.remove(PID);
                    SubPlugin.PIDs.remove(Name);
                    SubPlugin.SubServers.remove(Name);
                }
            }.runTaskAsynchronously(SubPlugin.Plugin);
        }
    }

    /**
     * Creates a SubServer
     *
     * @param Sender The player who sent this Command
     * @param Name Name of SubServer
     * @param Port Port of SubServer
     * @param Log Toggle Output to console
     * @param SharedChat Toggle Shared Chat
     * @param Dir Shell Directory
     * @param Exec Executable String or File
     * @param AutoRestart AutoRestarts Server
     * @param Temporary Toggles Temporary Server actions
     */
    public static void addServer(OfflinePlayer Sender, final String Name, int Port, boolean Log, boolean SharedChat, File Dir, Executable Exec, boolean AutoRestart, boolean Temporary) {
        final int PID = (SubPlugin.SubServers.size() + 1);
        if (Temporary) {
            SubPlugin.Servers.put(PID, new SubServer(true, Name, PID, Port, Log, SharedChat, Dir, Exec, false, true, SubPlugin));
        } else {
            SubPlugin.Servers.put(PID, new SubServer(true, Name, PID, Port, Log, SharedChat, Dir, Exec, AutoRestart, false, SubPlugin));
        }
        SubPlugin.PIDs.put(Name, PID);
        SubPlugin.SubServers.add(Name);

        if (SubPlugin.sql != null) {
            try {
                Statement update = SubPlugin.sql.getConnection().createStatement();
                update.executeUpdate("INSERT INTO `SubServers` (`Name`, `IP`, `PID`, `Enabled`, `Shared_Chat`, `Temp`, `Running`) VALUES " +
                        "('"+ Name +"', '"+ SubPlugin.config.getString("Settings.Server-IP")+":"+Integer.toString(Port) +"', '"+ PID +"', '1', '"+ ((SharedChat)?"1":"0") +"', '"+ ((Temporary)?"1":"0") +"', '0')");
                update.close();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Problem Syncing Database!");
                e.printStackTrace();
            }
        } else if (getSubServer(0).isRunning()) getSubServer(0).sendCommandSilently("subconf@proxy addserver " + Name + " " + SubPlugin.config.getString("Settings.Server-IP") + " " + Port + " " + SharedChat);

        if (Temporary) {
            SubPlugin.Servers.get(PID).start(Sender);
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1500);
                        SubPlugin.Servers.get(SubPlugin.PIDs.get(Name)).waitFor();
                        Thread.sleep(1000);
                        SubPlugin.Servers.get(SubPlugin.PIDs.get(Name)).destroy();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (SubPlugin.sql != null) {
                        try {
                            Statement update = SubPlugin.sql.getConnection().createStatement();
                            update.executeUpdate("DELETE FROM `SubServers` WHERE PID='"+ PID + "'");
                            update.close();
                        } catch (SQLException e) {
                            Bukkit.getLogger().severe("Problem Syncing Database!");
                            e.printStackTrace();
                        }
                    } else if (getSubServer(0).isRunning()) getSubServer(0).sendCommandSilently("subconf@proxy removeserver " + Name);

                    SubPlugin.Servers.remove(PID);
                    SubPlugin.PIDs.remove(Name);
                    SubPlugin.SubServers.remove(Name);
                }
            }.runTaskAsynchronously(SubPlugin.Plugin);
        }
    }

    /**
     * Adds your Listener to Bukkit and/or Subservers
     *
     * @param Listener the Object that implements SubListener
     * @param Plugin The Plugin calling this SubListener
     * @param RegisterBukkit Register listener with Bukkit? (true/false)
     */
    public static void registerListener(SubListener Listener, JavaPlugin Plugin, boolean RegisterBukkit) {
        if (RegisterBukkit) Bukkit.getServer().getPluginManager().registerEvents(Listener, Plugin);

        List<SubListener> listeners = new ArrayList<SubListener>();
        if (SubPlugin.EventHandlers.keySet().contains(Plugin)) listeners.addAll(SubPlugin.EventHandlers.get(Plugin));
        listeners.add(Listener);
        SubPlugin.EventHandlers.put(Plugin, listeners);

    }

    /**
     * Adds your Listener to Bukkit and Subservers
     *
     * @param Listener the Object that implements SubListener
     * @param Plugin The Plugin calling this SubListener
     */
    public static void registerListener(SubListener Listener, JavaPlugin Plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(Listener, Plugin);

        List<SubListener> listeners = new ArrayList<SubListener>();
        if (SubPlugin.EventHandlers.keySet().contains(Plugin)) listeners.addAll(SubPlugin.EventHandlers.get(Plugin));
        listeners.add(Listener);
        SubPlugin.EventHandlers.put(Plugin, listeners);

    }
    /**
     * Gets the Lang File
     *
     * @return The lang file's nodes
     */
    public static ConfigFile getLang() { return SubPlugin.lang; }


    /**
     *
     * @param Event The Event to Execute
     * @param Args The Args required to execute this event
     * @return If the event was cancelled
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static boolean executeEvent(SubEvent.Events Event, Object... Args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        return SubEvent.RunEvent(SubPlugin, Event, Args);
    }

    /**
     * Gets the SubServers Version
     *
     * @return The SubServers Version
     */
    public static Version getPluginVersion() { return SubPlugin.PluginVersion; }

    public static Version getMinecraftVersion() { return SubPlugin.MCVersion; }
}
