package net.ME1312.SubServer;

import net.ME1312.SubServer.Executable.SubServer;
import net.ME1312.SubServer.GUI.SubGUIListener;
import net.ME1312.SubServer.Libraries.Config.ConfigFile;
import net.ME1312.SubServer.Libraries.Config.ConfigManager;
import net.ME1312.SubServer.Libraries.SQL.MySQL;
import net.ME1312.SubServer.Libraries.Version.Version;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SubPlugin {
    public HashMap<Integer, SubServer> Servers = new HashMap<Integer, SubServer>();
    public HashMap<String, Integer> PIDs = new HashMap<String, Integer>();
    public List<String> SubServers = new ArrayList<String>();
    public JavaPlugin Plugin;

    public String lprefix;
    public ConfigFile config;
    public HashMap<String, String> lang = new HashMap<String, String>();
    public MySQL sql;
    private BukkitTask sqltimer;

    public Version PluginVersion;
    public Version MCVersion;

    private ConfigManager confmanager;
    private SubPlugin instance;

    // Main Plugin Class
    protected SubPlugin(JavaPlugin plugin) throws IllegalArgumentException {
        if (plugin != null && plugin.getDescription().getName().equalsIgnoreCase("SubServersClient")) {
            Plugin = plugin;
        } else {
            throw new IllegalArgumentException("Main Should only be called by SubServers Plugin.");
        }

        instance = this;
    }

    protected void EnablePlugin() {
        confmanager = new ConfigManager(Plugin);
        PluginManager pm = Bukkit.getServer().getPluginManager();
        lprefix = "SubServers \u00BB ";
        if (!(new File(Plugin.getDataFolder().toString())).exists()) {
            new File(Plugin.getDataFolder().toString()).mkdirs();
        }

        new SubAPI(this);

        PluginVersion = new Version(Plugin.getDescription().getVersion());
        try {
            MCVersion = new Version(Bukkit.getServer().getVersion().split("\\(MC\\: ")[1].split("\\)")[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            Bukkit.getLogger().warning(lprefix + "Problem grabbing Minecraft Version! Assuming 1.8!");
            MCVersion = new Version("1.8");
        }

        Bukkit.getLogger().info(lprefix + "Loading Libraries for " + MCVersion);

        /**
         * Config stuffs
         */
        if (!(new File(Plugin.getDataFolder() + File.separator + "config.yml").exists())) {
            copyFromJar("config.yml", Plugin.getDataFolder() + File.separator + "config.yml");
            Bukkit.getLogger().info(lprefix + "Created Config.yml!");
        }

        config = confmanager.getNewConfig("config.yml");

        /**
         * SQL Sync
         */
        try {
            sql = new MySQL(config.getRawString("Settings.SQL.hostname"), Integer.toString(config.getInt("Settings.SQL.port")), config.getRawString("Settings.SQL.database"),
                    config.getRawString("Settings.SQL.username"), config.getRawString("Settings.SQL.password"));
            sql.openConnection();
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Could not connect to Database!");
            e.printStackTrace();
            sql = null;
        }

        if (sql != null) {
            sqltimer = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        Statement update = sql.getConnection().createStatement();
                        ResultSet results = update.executeQuery("SELECT * FROM `SubServers`");
                        for(Iterator<Integer> servers = Servers.keySet().iterator(); servers.hasNext(); ) {
                            int server = servers.next();
                            SubAPI.getSubServer(server).destroy();
                            SubServers.remove(SubAPI.getSubServer(server).Name);
                        }
                        Servers.clear();
                        PIDs.clear();
                        while (results.next()) {
                            SubServers.add(results.getString("Name"));
                            PIDs.put(results.getString("Name"), results.getInt("PID"));
                            Servers.put(results.getInt("PID"), new SubServer(results.getBoolean("Enabled"), results.getString("Name"), results.getInt("PID"),
                                    results.getBoolean("Shared_Chat"), results.getBoolean("Temp"), results.getBoolean("Running"), instance));
                        }
                        results.close();
                        results = update.executeQuery("SELECT * FROM `SubLang`");
                        lang.clear();
                        try {
                            while (results.next()) {
                                lang.put(results.getString("Key"), StringEscapeUtils.unescapeJava(URLDecoder.decode(results.getString("Value"), "UTF-8")));
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        results.close();
                        update.close();
                    } catch (SQLException e) {
                        System.out.println("Problem Syncing Database");
                        e.printStackTrace();
                    }
                }
            }.runTaskTimerAsynchronously(Plugin, 0L, 20 * 5);
        }

        /**
         * Registers Listeners
         */
        pm.registerEvents(new SubGUIListener(this), Plugin);

        /**
         * Registers Commands
         */
        Plugin.getCommand("subserver").setExecutor(new SubCMD(this));
        Plugin.getCommand("sub").setExecutor(new SubCMD(this));

        /**
         * ME1312.net Stats
         */
        new Metrics(3, Plugin);
    }

    protected void DisablePlugin() {

        if (sql != null) {
            sqltimer.cancel();
            try {
                sql.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        List<String> SubServersStore = new ArrayList<String>();
        SubServersStore.addAll(SubServers);

        for(Iterator<String> str = SubServersStore.iterator(); str.hasNext(); ) {
            String item = str.next();
            SubAPI.getSubServer(item).destroy();
        }
        Bukkit.getLogger().info(lprefix + " Plugin Disabled.");
    }

    public void copyFromJar(String resource, String destination) {
        InputStream resStreamIn = SubPlugin.class.getClassLoader().getResourceAsStream(resource);
        File resDestFile = new File(destination);
        try {
            OutputStream resStreamOut = new FileOutputStream(resDestFile);
            int readBytes;
            byte[] buffer = new byte[4096];
            while ((readBytes = resStreamIn.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
            resStreamOut.close();
            resStreamIn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
