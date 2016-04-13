package net.ME1312.SubServer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.List;

import net.ME1312.SubServer.Events.*;
import net.ME1312.SubServer.Events.Libraries.Event;
import net.ME1312.SubServer.Events.Libraries.EventType;
import net.ME1312.SubServer.Events.Libraries.SubEventHandler;
import net.ME1312.SubServer.Executable.SubCreator;
import net.ME1312.SubServer.Executable.SubProxy;
import net.ME1312.SubServer.Libraries.Config.ConfigFile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
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
 * @version 1.9.2a+
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

    public static SubProxy getProxy() {
        return (SubProxy) getSubServer(0);
    }

    /**
     * Get SubServers from the Configuration
     *
     * @return List Of all Servers Defined in the Configuration
     */
    public static List<SubServer> getSubServers() {
        List<SubServer> Server = new ArrayList<SubServer>();
        Server.addAll(SubPlugin.Servers.values());
        return Server;
    }

    /**
     * Gets SubServer by ID
     *
     * @param PID ID of SubServer
     * @return The Requested SubServer (or null if there is no such server)
     */
    public static SubServer getSubServer(int PID) {
        return SubPlugin.Servers.get(PID);

    }

    /**
     * Gets SubServer by Name
     *
     * @param Name Name of SubServer (Case Sensitive)
     * @return The Requested SubServer (or null if there is no such server)
     */
    public static SubServer getSubServer(String Name) {
        return SubPlugin.Servers.get(SubPlugin.PIDs.get(Name));

    }

    /**
     * Defines a SubServer
     *
     * @param Name Name of SubServer
     * @param Port Port of SubServer
     * @param Log Toggle Output to console
     * @param SharedChat Toggle Shared Chat
     * @param Dir Shell Directory
     * @param Exec Executable String or File
     * @param AutoRestart AutoRestarts Server
     * @param Temporary Toggles Temporary Server actions
     * @return The SubServer
     */
    public static SubServer addServer(final String Name, int Port, boolean Log, boolean SharedChat, File Dir, Executable Exec, boolean AutoRestart, boolean Temporary) {
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

                    SubPlugin.Servers.get(PID).destroy();
                    SubPlugin.Servers.remove(PID);
                    SubPlugin.PIDs.remove(Name);
                    SubPlugin.SubServers.remove(Name);
                }
            }.runTaskAsynchronously(SubPlugin.Plugin);
        }
        return SubPlugin.Servers.get(PID);
    }

    /**
     * Defines a SubServer
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
     * @return The SubServer
     */
    public static SubServer addServer(OfflinePlayer Sender, final String Name, int Port, boolean Log, boolean SharedChat, File Dir, Executable Exec, boolean AutoRestart, boolean Temporary) {
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

                    SubPlugin.Servers.get(PID).destroy();
                    SubPlugin.Servers.remove(PID);
                    SubPlugin.PIDs.remove(Name);
                    SubPlugin.SubServers.remove(Name);
                }
            }.runTaskAsynchronously(SubPlugin.Plugin);
        }

        return SubPlugin.Servers.get(PID);
    }

    /**
     * Creates a SubServer
     *
     * @param Name Name of SubServer
     * @param Port Port of SubServer
     * @param Dir Directory for SubServer
     * @param Type Type of SubServer
     * @param Version Version of SubServer
     * @param Memory Memory for SubServer
     * @return The SubCreator for this SubServer
     * @throws IllegalStateException when SubCreator is already running
     */
    public static SubCreator createServer(String Name, int Port, File Dir, SubCreator.ServerType Type, Version Version, int Memory) throws IllegalStateException {
        if (SubPlugin.ServerCreator == null) {
            return SubPlugin.ServerCreator = new SubCreator(Name, Port, Dir, Type, Version, Memory, null, SubPlugin);
        } else {
            throw new IllegalStateException("SubCreator Already Running!");
        }
    }

    /**
     * Creates a SubServer
     *
     * @param Player Player Creating this SubServer
     * @param Name Name of SubServer
     * @param Port Port of SubServer
     * @param Dir Directory for SubServer
     * @param Type Type of SubServer
     * @param Version Version of SubServer
     * @param Memory Memory for SubServer
     * @return The SubCreator for this SubServer
     * @throws IllegalStateException when SubCreator is already running
     */
    public static SubCreator createServer(Player Player, String Name, int Port, File Dir, SubCreator.ServerType Type, Version Version, int Memory) throws IllegalStateException {
        if (SubPlugin.ServerCreator == null) {
            return SubPlugin.ServerCreator = new SubCreator(Name, Port, Dir, Type, Version, Memory, Player, SubPlugin);
        } else {
            throw new IllegalStateException("SubCreator Already Running!");
        }
    }

    /**
     * Adds your Listener Class to Subservers
     *
     * @param Listener The Class that contains SubEvents
     * @param Plugin The Plugin calling this SubListener
     */
    public static void registerListener(Listener Listener, JavaPlugin Plugin) {
        HashMap<Listener, HashMap<EventType, HashMap<EventPriority, List<Method>>>> listenerMap = new HashMap<Listener, HashMap<EventType, HashMap<EventPriority, List<Method>>>>();
        if (SubPlugin.Listeners.keySet().contains(Plugin)) listenerMap.putAll(SubPlugin.Listeners.get(Plugin));
        HashMap<EventType, HashMap<EventPriority, List<Method>>> eventMap = new HashMap<EventType, HashMap<EventPriority, List<Method>>>();
        if (listenerMap.keySet().contains(Listener)) eventMap.putAll(listenerMap.get(Listener));

        for (Iterator<Method> Methods = Arrays.<Method>asList(Listener.getClass().getMethods()).iterator(); Methods.hasNext(); ) {
            Method method = Methods.next();
            if (method.isAnnotationPresent(SubEventHandler.class)) {
                if (method.getParameterTypes().length == 1) {
                    try {
                        HashMap<EventPriority, List<Method>> priorityMap = new HashMap<EventPriority, List<Method>>();
                        if (eventMap.keySet().contains(EventType.valueOf(method.getParameterTypes()[0].getSimpleName())))
                            priorityMap.putAll(eventMap.get(EventType.valueOf(method.getParameterTypes()[0].getSimpleName())));
                        List<Method> methods = new ArrayList<Method>();
                        if (priorityMap.keySet().contains(method.getAnnotation(SubEventHandler.class).priority()))
                            methods.addAll(priorityMap.get(method.getAnnotation(SubEventHandler.class).priority()));

                        methods.add(method);

                        priorityMap.put(method.getAnnotation(SubEventHandler.class).priority(), methods);
                        eventMap.put(EventType.valueOf(method.getParameterTypes()[0].getSimpleName()), priorityMap);
                    } catch (IllegalArgumentException e) {
                        Bukkit.getLogger().severe(SubPlugin.lprefix + "\"" + Plugin.getName() + "\" Tried to register Invalid EventHandler in class");
                        Bukkit.getLogger().severe("\"" + Listener.getClass().getCanonicalName() + "\" using method \"" + method.getName() + "\"!");
                        Bukkit.getLogger().severe("Caused by: Invalid Event Class \"" + method.getParameterTypes()[0].getCanonicalName() + "\"");
                        Bukkit.getLogger().severe("");
                    }
                } else {
                    Bukkit.getLogger().severe(SubPlugin.lprefix + "\"" + Plugin.getName() + "\" Tried to register Invalid EventHandler in class");
                    Bukkit.getLogger().severe("\"" + Listener.getClass().getCanonicalName() + "\" using method \"" + method.getName() + "\"!");
                    Bukkit.getLogger().severe("Caused by: Too many Parameters for SubEvent to be Executed");
                    Bukkit.getLogger().severe("");
                }
            }
        }

        listenerMap.put(Listener, eventMap);
        if (!listenerMap.isEmpty()) {
            SubPlugin.Listeners.put(Plugin, listenerMap);
        } else {
            SubPlugin.Listeners.remove(Plugin);
        }
    }
    /**
     * Removes a Listener Class from SubServers
     *
     * @param Listener The Class that contains SubEvents (Must be the same object)
     * @param Plugin The Plugin to remove the SubListener from
     */
    public static void unRegisterListener(Listener Listener, JavaPlugin Plugin) {
        HashMap<Listener, HashMap<EventType, HashMap<EventPriority, List<Method>>>> map = new HashMap<Listener, HashMap<EventType, HashMap<EventPriority, List<Method>>>>();
        if (SubPlugin.Listeners.keySet().contains(Plugin)) map.putAll(SubPlugin.Listeners.get(Plugin));
        map.remove(Listener);
        if (!map.isEmpty()) {
            SubPlugin.Listeners.put(Plugin, map);
        } else {
            SubPlugin.Listeners.remove(Plugin);
        }
    }

    /**
     * Gets the Lang File
     *
     * @return The lang file's nodes
     */
    public static ConfigFile getLang() { return SubPlugin.lang; }


    /**
     * Executes an Event
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
        Result Cancelled = Result.ALLOW;
        for (EventPriority priority : Arrays.asList(EventPriority.values())) {
            for (JavaPlugin plugin : SubAPI.SubPlugin.Listeners.keySet()) {
                for (Listener listener : SubAPI.SubPlugin.Listeners.get((Object)plugin).keySet()) {
                    if (!SubAPI.SubPlugin.Listeners.get((Object)plugin).get((Object)listener).keySet().contains((Object)Event) || !SubAPI.SubPlugin.Listeners.get((Object)plugin).get((Object)listener).get((Object)Event).keySet().contains((Object)priority)) continue;
                    for (Method method : SubAPI.SubPlugin.Listeners.get((Object)plugin).get((Object)listener).get((Object)Event).get((Object)priority)) {
                        Event event;
                        if (Event == EventType.SubCreateEvent) {
                            event = new SubCreateEvent(SubPlugin, (Player)args[0], (SubCreator.ServerType)((Object)args[1]));
                            try {
                                method.invoke((Object)listener, event);
                                if (event.getStatus() != Result.DENY && (!((SubEventHandler)method.getAnnotation(SubEventHandler.class)).override() || event.getStatus() == Result.DEFAULT)) continue;
                                Cancelled = event.getStatus();
                            }
                            catch (InvocationTargetException e) {
                                Bukkit.getLogger().severe(SubAPI.SubPlugin.lprefix + "Method \"" + method.getName() + "\" in Class \"" + listener.getClass().getCanonicalName() + "\" for Plugin \"" + plugin.getName() + "\"");
                                Bukkit.getLogger().severe("had the following Unhandled Exception while running SubEvents:");
                                e.getTargetException().printStackTrace();
                                Bukkit.getLogger().severe("");
                            }
                            continue;
                        }
                        if (Event == EventType.SubStartEvent) {
                            event = new SubStartEvent(SubPlugin, (SubServer)args[0], (OfflinePlayer)args[1]);
                            try {
                                method.invoke((Object)listener, event);
                                if (event.getStatus() != Result.DENY && (!((SubEventHandler)method.getAnnotation(SubEventHandler.class)).override() || event.getStatus() == Result.DEFAULT)) continue;
                                Cancelled = event.getStatus();
                            }
                            catch (InvocationTargetException e) {
                                Bukkit.getLogger().severe(SubAPI.SubPlugin.lprefix + "Method \"" + method.getName() + "\" in Class \"" + listener.getClass().getCanonicalName() + "\" for Plugin \"" + plugin.getName() + "\"");
                                Bukkit.getLogger().severe("had the following Unhandled Exception while running SubEvents:");
                                e.getTargetException().printStackTrace();
                                Bukkit.getLogger().severe("");
                            }
                            continue;
                        }
                        if (Event == EventType.SubStopEvent) {
                            event = new SubStopEvent(SubPlugin, (SubServer)args[0], (OfflinePlayer)args[1]);
                            try {
                                method.invoke((Object)listener, event);
                                if (event.getStatus() != Result.DENY && (!((SubEventHandler)method.getAnnotation(SubEventHandler.class)).override() || event.getStatus() == Result.DEFAULT)) continue;
                                Cancelled = event.getStatus();
                            }
                            catch (InvocationTargetException e) {
                                Bukkit.getLogger().severe(SubAPI.SubPlugin.lprefix + "Method \"" + method.getName() + "\" in Class \"" + listener.getClass().getCanonicalName() + "\" for Plugin \"" + plugin.getName() + "\"");
                                Bukkit.getLogger().severe("had the following Unhandled Exception while running SubEvents:");
                                e.getTargetException().printStackTrace();
                                Bukkit.getLogger().severe("");
                            }
                            continue;
                        }
                        if (Event == EventType.SubShellExitEvent) {
                            event = new SubShellExitEvent(SubPlugin, (SubServer)args[0]);
                            try {
                                method.invoke((Object)listener, event);
                            }
                            catch (InvocationTargetException e) {
                                Bukkit.getLogger().severe(SubAPI.SubPlugin.lprefix + "Method \"" + method.getName() + "\" in Class \"" + listener.getClass().getCanonicalName() + "\" for Plugin \"" + plugin.getName() + "\"");
                                Bukkit.getLogger().severe("had the following Unhandled Exception while running SubEvents:");
                                e.getTargetException().printStackTrace();
                                Bukkit.getLogger().severe("");
                            }
                            continue;
                        }
                        if (Event != EventType.SubRunCommandEvent) continue;
                        event = new SubRunCommandEvent(SubPlugin, (SubServer)args[0], (OfflinePlayer)args[1], (String)args[2]);
                        try {
                            method.invoke((Object)listener, event);
                            if (event.getStatus() != Result.DENY && (!((SubEventHandler)method.getAnnotation(SubEventHandler.class)).override() || event.getStatus() == Result.DEFAULT)) continue;
                            Cancelled = event.getStatus();
                        }
                        catch (InvocationTargetException e) {
                            Bukkit.getLogger().severe(SubAPI.SubPlugin.lprefix + "Method \"" + method.getName() + "\" in Class \"" + listener.getClass().getCanonicalName() + "\" for Plugin \"" + plugin.getName() + "\"");
                            Bukkit.getLogger().severe("had the following Unhandled Exception while running SubEvents:");
                            e.getTargetException().printStackTrace();
                            Bukkit.getLogger().severe("");
                        }
                    }
                }
            }
        }
        return Cancelled != Result.DENY;
    }

    /**
     * Gets the SubServers Version
     *
     * @return The SubServers Version
     */
    public static Version getPluginVersion() { return SubPlugin.PluginVersion; }

    /**
     * Gets the Server's Minecraft Version
     *
     * @return The Minecraft Version
     */
    public static Version getMinecraftVersion() { return SubPlugin.MCVersion; }

}
