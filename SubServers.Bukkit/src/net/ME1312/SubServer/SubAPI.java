package net.ME1312.SubServer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.List;

import net.ME1312.SubServer.Events.*;
import net.ME1312.SubServer.Events.Libraries.EventType;
import net.ME1312.SubServer.Events.Libraries.SubEventHandler;
import net.ME1312.SubServer.Executable.SubCreator;
import net.ME1312.SubServer.Libraries.Config.ConfigFile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.ME1312.SubServer.Executable.Executable;
import net.ME1312.SubServer.Executable.SubServer;
import net.ME1312.SubServer.Libraries.Version.Version;

/**
 * This is the API File for Subservers<br>
 * <br>
 * NOTES:<br>
 *   Methods ending in "All" don't effect the Proxy<br>
 *   Methods can be Requested<br>
 *
 * @author ME1312
 * @version 1.8.9f+
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
     * Adds your Listener Class to Subservers
     *
     * @param Listener The Class that contains SubEvents
     * @param Plugin The Plugin calling this SubListener
     */
    public static void registerListener(Listener Listener, JavaPlugin Plugin) {
        List<Listener> listeners = new ArrayList<Listener>();
        if (SubPlugin.Listeners.keySet().contains(Plugin)) listeners.addAll(SubPlugin.Listeners.get(Plugin));
        listeners.add(Listener);
        SubPlugin.Listeners.put(Plugin, listeners);
    }

    /**
     * Removes a Listener Class from SubServers
     *
     * @param Listener The Class that contains SubEvents (Must be the same object)
     * @param Plugin The Plugin to remove the SubListener from
     */
    public static void unRegisterListener(Listener Listener, JavaPlugin Plugin) {
        List<Listener> listeners = new ArrayList<Listener>();
        if (SubPlugin.Listeners.keySet().contains(Plugin)) listeners.addAll(SubPlugin.Listeners.get(Plugin));
        listeners.remove(Listener);
        SubPlugin.Listeners.put(Plugin, listeners);
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
     * @param args The Args required to execute this event (Usually they Mirror their Constructor)
     * @return If the event was overridden
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static boolean executeEvent(EventType Event, Object... args) throws IllegalAccessException, IllegalArgumentException, NoSuchMethodException, SecurityException {
        boolean Continue = true;

        for (Iterator<EventPriority> Priorities = Arrays.<EventPriority>asList(EventPriority.values()).iterator(); Priorities.hasNext(); ) {
            EventPriority priority = Priorities.next();
            for (Iterator<JavaPlugin> Plugins = SubPlugin.Listeners.keySet().iterator(); Plugins.hasNext(); ) {
                JavaPlugin plugin = Plugins.next();
                for (Iterator<Listener> Listeners = SubPlugin.Listeners.get(plugin).iterator(); Listeners.hasNext(); ) {
                    Listener listener = Listeners.next();
                    for (Iterator<Method> Methods = Arrays.<Method>asList(listener.getClass().getMethods()).iterator(); Methods.hasNext(); ) {
                        Method method = Methods.next();
                        if (method.isAnnotationPresent(SubEventHandler.class)) {
                            if (method.getAnnotation(SubEventHandler.class).priority() == priority) {
                                if (method.getParameterTypes().length == 1) {
                                    switch (method.getParameterTypes()[0].getSimpleName()) {
                                        case "SubCreateEvent":
                                            if (Event.toString().equals("SubCreateEvent")) {
                                                SubCreateEvent event = new SubCreateEvent((OfflinePlayer) args[0], (SubCreator.ServerType) args[1]);
                                                try {
                                                    method.invoke(listener, event);
                                                    if (event.isCancelled()) Continue = false;
                                                } catch (InvocationTargetException e) {
                                                    Bukkit.getLogger().severe(SubPlugin.lprefix + "Method \"" + method.getName() + "\" in Class \"" + listener.getClass().getCanonicalName() + "\" for Plugin \"" + plugin.getName() + "\"");
                                                    Bukkit.getLogger().severe("had the following Unhandled Exception while running SubEvents:");
                                                    e.getTargetException().printStackTrace();
                                                    Bukkit.getLogger().severe("");
                                                }
                                            }
                                            break;
                                        case "SubStartEvent":
                                            if (Event.toString().equals("SubStartEvent")) {
                                                SubStartEvent event = new SubStartEvent((SubServer) args[0], (OfflinePlayer) args[1]);
                                                try {
                                                    method.invoke(listener, event);
                                                    if (event.isCancelled()) Continue = false;
                                                } catch (InvocationTargetException e) {
                                                    Bukkit.getLogger().severe(SubPlugin.lprefix + "Method \"" + method.getName() + "\" in Class \"" + listener.getClass().getCanonicalName() + "\" for Plugin \"" + plugin.getName() + "\"");
                                                    Bukkit.getLogger().severe("had the following Unhandled Exception while running SubEvents:");
                                                    e.getTargetException().printStackTrace();
                                                    Bukkit.getLogger().severe("");
                                                }
                                            }
                                            break;
                                        case "SubStopEvent":
                                            if (Event.toString().equals("SubStopEvent")) {
                                                SubStopEvent event = new SubStopEvent((SubServer) args[0], (OfflinePlayer) args[1]);
                                                try {
                                                    method.invoke(listener, event);
                                                    if (event.isCancelled()) Continue = false;
                                                } catch (InvocationTargetException e) {
                                                    Bukkit.getLogger().severe(SubPlugin.lprefix + "Method \"" + method.getName() + "\" in Class \"" + listener.getClass().getCanonicalName() + "\" for Plugin \"" + plugin.getName() + "\"");
                                                    Bukkit.getLogger().severe("had the following Unhandled Exception while running SubEvents:");
                                                    e.getTargetException().printStackTrace();
                                                    Bukkit.getLogger().severe("");
                                                }
                                            }
                                            break;
                                        case "SubShellExitEvent":
                                            if (Event.toString().equals("SubShellExitEvent")) {
                                                SubShellExitEvent event = new SubShellExitEvent((SubServer) args[0]);
                                                try {
                                                    method.invoke(listener, event);
                                                } catch (InvocationTargetException e) {
                                                    Bukkit.getLogger().severe(SubPlugin.lprefix + "Method \"" + method.getName() + "\" in Class \"" + listener.getClass().getCanonicalName() + "\" for Plugin \"" + plugin.getName() + "\"");
                                                    Bukkit.getLogger().severe("had the following Unhandled Exception while running SubEvents:");
                                                    e.getTargetException().printStackTrace();
                                                    Bukkit.getLogger().severe("");
                                                }
                                            }
                                            break;
                                        case "SubRunCommandEvent":
                                            if (Event.toString().equals("SubRunCommandEvent")) {
                                                SubRunCommandEvent event = new SubRunCommandEvent((SubServer) args[0], (OfflinePlayer) args[1], (String) args[2]);
                                                try {
                                                    method.invoke(listener, event);
                                                    if (event.isCancelled()) Continue = false;
                                                } catch (InvocationTargetException e) {
                                                    Bukkit.getLogger().severe(SubPlugin.lprefix + "Method \"" + method.getName() + "\" in Class \"" + listener.getClass().getCanonicalName() + "\" for Plugin \"" + plugin.getName() + "\"");
                                                    Bukkit.getLogger().severe("had the following Unhandled Exception while running SubEvents:");
                                                    e.getTargetException().printStackTrace();
                                                    Bukkit.getLogger().severe("");
                                                }
                                            }
                                            break;
                                        default:
                                            Bukkit.getLogger().severe(SubPlugin.lprefix + "Method \"" + method.getName() + "\" in Class \"" + listener.getClass().getCanonicalName() + "\" for Plugin \"" + plugin.getName() + "\"");
                                            Bukkit.getLogger().severe("is tagged as a SubEvent, but isn't! Please Notify the developer(s)");
                                            Bukkit.getLogger().severe("Caused by: Invalid Event Class \"" + method.getParameterTypes()[0].getCanonicalName() + "\"");
                                            Bukkit.getLogger().severe("");
                                            break;
                                    }
                                } else {
                                    Bukkit.getLogger().severe(SubPlugin.lprefix + "Method \"" + method.getName() + "\" in Class \"" + listener.getClass().getCanonicalName() + "\" for Plugin \"" + plugin.getName() + "\"");
                                    Bukkit.getLogger().severe("is tagged as a SubEvent, but isn't! Please Notify the developer(s)");
                                    Bukkit.getLogger().severe("Caused by: Too many Parameters for SubEvent to be Executed");
                                    Bukkit.getLogger().severe("");
                                }
                            }
                        }
                    }
                }
            }
        }

        return Continue;
    }

    /**
     * Gets the SubServers Version
     *
     * @return The SubServers Version
     */
    public static Version getPluginVersion() { return SubPlugin.PluginVersion; }

    public static Version getMinecraftVersion() { return SubPlugin.MCVersion; }
}
