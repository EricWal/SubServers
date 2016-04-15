package net.ME1312.SubServer;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import net.ME1312.SubServer.Events.Libraries.EventType;
import net.ME1312.SubServer.Executable.SubProxy;
import net.ME1312.SubServer.GUI.SubGUIListener;
import net.ME1312.SubServer.Libraries.SQL.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.Files;

import net.ME1312.SubServer.Executable.SubCreator;
import net.ME1312.SubServer.Executable.Executable;
import net.ME1312.SubServer.Executable.SubServer;
import net.ME1312.SubServer.Libraries.Config.ConfigFile;
import net.ME1312.SubServer.Libraries.Config.ConfigManager;
import net.ME1312.SubServer.Libraries.Version.Version;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * SubServers' Main Class &amp; Internal database<br><br>
 * You should only mess with this if you know what you're doing, and the code in here is likely to change.
 *
 * @author ME1312
 */
public class SubPlugin {
    public HashMap<Integer, SubServer> Servers = new HashMap<Integer, SubServer>();
    public HashMap<String, Integer> PIDs = new HashMap<String, Integer>();
    public HashMap<JavaPlugin, HashMap<Listener, HashMap<EventType, HashMap<EventPriority, List<Method>>>>> Listeners = new HashMap<JavaPlugin, HashMap<Listener, HashMap<EventType, HashMap<EventPriority, List<Method>>>>>();
    public List<String> SubServers = new ArrayList<String>();
    public JavaPlugin Plugin;
    public SubCreator ServerCreator;

    public String lprefix;
    public ConfigFile config;
    public ConfigFile lang;
    public MySQL sql;

    public Version PluginVersion;
    public Version MCVersion;

    private ConfigManager confmanager;
    private SubPlugin instance;

    // Main Plugin Class
    protected SubPlugin(JavaPlugin plugin) throws IllegalArgumentException {
        if (plugin != null && plugin.getDescription().getName().equalsIgnoreCase("SubServers")) {
            Plugin = plugin;
        } else {
            throw new IllegalArgumentException("Main Should only be called by SubServers Plugin.");
        }

        instance = this;
    }

    protected void EnablePlugin() {
        confmanager = new ConfigManager(Plugin);
        PluginManager pm = Bukkit.getServer().getPluginManager();
        lprefix = Plugin.getDescription().getName() + " \u00BB ";
        if (!(new File(Plugin.getDataFolder().toString())).exists()) {
            new File(Plugin.getDataFolder().toString()).mkdirs();
        }

        new SubAPI(this);

        PluginVersion = new Version(Plugin.getDescription().getVersion());
        try {
            MCVersion = new Version(Bukkit.getServer().getVersion().split("\\(MC\\: ")[1].split("\\)")[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            Bukkit.getLogger().warning(lprefix + "Problem grabbing Minecraft Version! Assuming 1.9!");
            MCVersion = new Version("1.9");
        }

        Bukkit.getLogger().info(lprefix + "Loading Libraries for " + MCVersion);

        /**
         * Updates Configs if needed
         */
        if (!(new File(Plugin.getDataFolder() + File.separator + "config.yml").exists())) {
            copyFromJar("config.yml", Plugin.getDataFolder() + File.separator + "config.yml");
            Bukkit.getLogger().info(lprefix + "Created Config.yml!");
        } else if (!confmanager.getNewConfig("config.yml").getString("Settings.config-version").equalsIgnoreCase("1.8.9a+")) {
            try {
                Files.move(new File(Plugin.getDataFolder() + File.separator + "config.yml"), new File(Plugin.getDataFolder() + File.separator + "old-config." + Math.round(Math.random() * 100000) + ".yml"));
                copyFromJar("config.yml", Plugin.getDataFolder() + File.separator + "config.yml");
                Bukkit.getLogger().info(lprefix + "Updated Config.yml!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!(new File(Plugin.getDataFolder() + File.separator + "lang.yml").exists())) {
            copyFromJar("lang.yml", Plugin.getDataFolder() + File.separator + "lang.yml");
            Bukkit.getLogger().info(lprefix + "Created Lang.yml!");
        } else if (!confmanager.getNewConfig("lang.yml").getString("config-version").equalsIgnoreCase("1.9.2a+")) {
            try {
                Files.move(new File(Plugin.getDataFolder() + File.separator + "lang.yml"), new File(Plugin.getDataFolder() + File.separator + "old-lang." + Math.round(Math.random() * 100000) + ".yml"));
                copyFromJar("lang.yml", Plugin.getDataFolder() + File.separator + "lang.yml");
                Bukkit.getLogger().info(lprefix + "Updated Lang.yml!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!(new File(Plugin.getDataFolder(), "SubCreator").exists())) new File(Plugin.getDataFolder(), "SubCreator").mkdir();
        if (!(new File(Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "build-subserver.sh").exists())) {
            copyFromJar("build-subserver.sh", Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "build-subserver.sh");
            Bukkit.getLogger().info(lprefix + "Created Build Script!");
        } else {
            try {
                String Version = "null";
                BufferedReader brText = new BufferedReader(new FileReader(new File(Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "build-subserver.sh")));
                try {
                     Version = brText.readLine().split("Version: ")[1];
                } catch (NullPointerException e) {}
                brText.close();

                if (!Version.equalsIgnoreCase("1.8.9h+")) {
                    Files.move(new File(Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "build-subserver.sh"), new File(Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "old-build-subserver." + Math.round(Math.random() * 100000) + ".sh"));
                    copyFromJar("lang.yml", Plugin.getDataFolder() + File.separator + "lang.yml");
                    Bukkit.getLogger().info(lprefix + "Updated Build Script!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!(new File(Plugin.getDataFolder(), "SubCreator" + File.separator + "Bukkit-Plugins").exists()))
            new File(Plugin.getDataFolder(), "SubCreator" + File.separator + "Bukkit-Plugins").mkdir();
        if (!(new File(Plugin.getDataFolder(), "SubCreator" + File.separator + "Spigot-Plugins").exists()))
            new File(Plugin.getDataFolder(), "SubCreator" + File.separator + "Spigot-Plugins").mkdir();
        if (!(new File(Plugin.getDataFolder(), "SubCreator" + File.separator + "Sponge-Mods").exists()))
            new File(Plugin.getDataFolder(), "SubCreator" + File.separator + "Sponge-Mods").mkdir();
        if (!(new File(Plugin.getDataFolder(), "SubCreator" + File.separator + "Sponge-Config").exists()))
            new File(Plugin.getDataFolder(), "SubCreator" + File.separator + "Sponge-Config").mkdir();

        config = confmanager.getNewConfig("config.yml");
        lang = confmanager.getNewConfig("lang.yml");
        SubServers.addAll(config.getConfigurationSection("Servers").getKeys(false));

        /**
         * Re-Sync to SQL
         */
        try {
            sql = new MySQL(config.getRawString("Settings.SQL.hostname"), Integer.toString(config.getInt("Settings.SQL.port")), config.getRawString("Settings.SQL.database"),
                    config.getRawString("Settings.SQL.username"), config.getRawString("Settings.SQL.password"));
            sql.openConnection();
            Statement update = sql.getConnection().createStatement();

            update.executeUpdate("CREATE TABLE IF NOT EXISTS `SubServers` (`Name` VARCHAR(32), `IP` VARCHAR(32), `PID` INTEGER, `Enabled` BOOLEAN, `Shared_Chat` BOOLEAN, `Temp` BOOLEAN, `Running` BOOLEAN)");
            update.executeUpdate("DELETE FROM `SubServers`");
            update.executeUpdate("CREATE TABLE IF NOT EXISTS `SubQueue` (`PID` INTEGER, `Type` INTEGER, `Player` VARCHAR(64), `Args` VARCHAR(64))");
            update.executeUpdate("DELETE FROM `SubQueue`");
            update.executeUpdate("CREATE TABLE IF NOT EXISTS `SubLang` (`Key` VARCHAR(64), `Value` VARCHAR(128))");
            update.executeUpdate("DELETE FROM `SubLang`");
            try {
                for (Iterator<String> keys = lang.getConfigurationSection("Lang").getKeys(false).iterator(); keys.hasNext(); ) {
                    String key = keys.next();
                    for (Iterator<String> str = lang.getConfigurationSection("Lang." + key).getKeys(false).iterator(); str.hasNext(); ) {
                        String item = str.next();
                        update.executeUpdate("INSERT INTO `SubLang` (`Key`, `Value`) VALUES ('Lang." + key + "." + item + "', '" + URLEncoder.encode(lang.getRawString("Lang." + key + "." + item), "UTF-8") + "')");
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            update.close();
        } catch (SQLException | ClassNotFoundException e) {
            Bukkit.getLogger().severe(lprefix + "Could not setup Database:");
            Bukkit.getLogger().severe(e.getLocalizedMessage());
            sql = null;
        }


        /**
         * Registers Listeners
         */
        pm.registerEvents(new SubGUIListener(this), Plugin);

        /**
         * Auto-Starts Servers,
         * Registers PIDs & Shells
         */
        PIDs.put("~Proxy", 0);
        Servers.put(0, new SubProxy(config.getBoolean("Proxy.enabled"), config.getBoolean("Proxy.log"), new File(config.getRawString("Proxy.dir")),
                new Executable(config.getRawString("Proxy.exec")), config.getBoolean("Proxy.auto-restart"), this));

        int i = 0;
        for(Iterator<String> str = SubServers.iterator(); str.hasNext(); ) {
            String item = str.next();
            i++;
            PIDs.put(item, i);
            Servers.put(i, new SubServer(config.getBoolean("Servers." + item + ".enabled"), item, i, config.getInt("Servers." + item + ".port"), config.getBoolean("Servers." + item + ".log"),
                    config.getBoolean("Servers." + item + ".use-shared-chat"), new File(config.getRawString("Servers." + item + ".dir")), new Executable(config.getRawString("Servers." + item + ".exec")),
                    config.getBoolean("Servers." + item + ".auto-restart"), false, this));
            if (sql != null) {
                try {
                    Statement update = sql.getConnection().createStatement();
                    update.executeUpdate("INSERT INTO `SubServers` (`Name`, `IP`, `PID`, `Enabled`, `Shared_Chat`, `Temp`, `Running`) VALUES " +
                            "('"+ item +"', '"+ config.getString("Settings.Server-IP")+":"+config.getInt("Servers." + item + ".port") +"', '" + i +"', '"+ ((config.getBoolean("Servers." + item + ".enabled"))?"1":"0") +"', '"+ ((config.getBoolean("Servers." + item + ".use-shared-chat"))?"1":"0") +"', '0', '0')");
                    update.close();
                } catch (SQLException e) {
                    Bukkit.getLogger().severe(lprefix + "Problem Syncing Database!");
                    e.printStackTrace();
                }
            }
            if (config.getBoolean("Servers." + item + ".enabled") && config.getBoolean("Servers." + item + ".run-on-launch")) {
                Servers.get(i).start();
            }
        }

        if ((config.getBoolean("Proxy.enabled")) && (config.getBoolean("Proxy.run-on-launch"))) {
            Servers.get(0).start();
        }

        /**
         * Registers Commands
         */
        Plugin.getCommand("subserver").setExecutor(new SubCMD(this));
        Plugin.getCommand("sub").setExecutor(new SubCMD(this));

        /**
         * ME1312.net Stats
         */
        new Metrics(1, Plugin);

    }

    protected void DisablePlugin() {
        Bukkit.getLogger().info(lprefix + "Stopping SubServers...");

        try {
            if (ServerCreator != null && ServerCreator.isRunning()) {
                ServerCreator.waitFor();
                Thread.sleep(1000);
            }

            if (SubAPI.getSubServer(0).isRunning()) {
                Servers.get(0).setAutoRestart(false);
                Servers.get(0).stop();
                Servers.get(0).waitFor();
                Thread.sleep(1000);
                Servers.get(0).destroy();
            }

            List<String> SubServersStore = new ArrayList<String>();
            SubServersStore.addAll(SubServers);

            for(Iterator<String> str = SubServersStore.iterator(); str.hasNext(); ) {
                String item = str.next();
                if (SubAPI.getSubServer(item).isRunning()) {
                    SubAPI.getSubServer(item).setAutoRestart(false);
                    SubAPI.getSubServer(item).stop();
                    SubAPI.getSubServer(item).waitFor();
                    if (SubAPI.getSubServer(item).isTemporary()) {
                        Thread.sleep(500);
                    }
                    Thread.sleep(1000);
                }
                SubAPI.getSubServer(item).destroy();
            }
            Bukkit.getLogger().info(lprefix + " Plugin Disabled.");
        } catch (InterruptedException e) {
            Bukkit.getLogger().severe(lprefix + "Problem Stopping Subservers.");
            Bukkit.getLogger().severe(lprefix + "Subservers will stay as Background Processes if not Stopped");
            e.printStackTrace();
            Bukkit.getLogger().warning(lprefix + "Config Not Saved: Preserved config from Invalid Changes.");
            Bukkit.getLogger().warning(lprefix + " Plugin Partially Disabled.");
        }
        if (sql != null) {
            try {
                sql.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void ReloadPlugin(final Player sender) {
        if (!Servers.get(0).isRunning()) {
            Servers.get(0).destroy();
            Servers.remove(0);
        }
        List<String> SubServersStore = new ArrayList<String>();
        SubServersStore.addAll(SubServers);

        for(Iterator<String> str = SubServersStore.iterator(); str.hasNext(); ) {
            String item = str.next();
            if (!SubAPI.getSubServer(item).isRunning()) {
                SubAPI.getSubServer(item).destroy();
                Servers.remove(SubAPI.getSubServer(item).getPID());
                PIDs.remove(item);
                SubServers.remove(item);
            }
        }

        config.reloadConfig();
        lang.reloadConfig();

        if (Servers.get(0) == null) {
            Servers.put(0, new SubProxy(config.getBoolean("Proxy.enabled"), config.getBoolean("Proxy.log"), new File(config.getRawString("Proxy.dir")),
                    new Executable(config.getRawString("Proxy.exec")), config.getBoolean("Proxy.auto-restart"), this));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (sql != null) sql.closeConnection();

                    sql = new MySQL(config.getRawString("Settings.SQL.hostname"), Integer.toString(config.getInt("Settings.SQL.port")), config.getRawString("Settings.SQL.database"),
                            config.getRawString("Settings.SQL.username"), config.getRawString("Settings.SQL.password"));
                    sql.openConnection();
                    Statement update = sql.getConnection().createStatement();

                    update.executeUpdate("CREATE TABLE IF NOT EXISTS `SubServers` (`Name` VARCHAR(32), `IP` VARCHAR(32), `PID` INTEGER, `Enabled` BOOLEAN, `Shared_Chat` BOOLEAN, `Temp` BOOLEAN, `Running` BOOLEAN)");
                    update.executeUpdate("DELETE FROM `SubServers`");
                    update.executeUpdate("CREATE TABLE IF NOT EXISTS `SubQueue` (`PID` INTEGER, `Type` INTEGER, `Player` VARCHAR(64), `Args` VARCHAR(64))");
                    update.executeUpdate("DELETE FROM `SubQueue`");
                    update.executeUpdate("CREATE TABLE IF NOT EXISTS `SubLang` (`Key` VARCHAR(64), `Value` VARCHAR(128))");
                    update.executeUpdate("DELETE FROM `SubLang`");
                    try {
                        for (Iterator<String> keys = lang.getConfigurationSection("Lang").getKeys(false).iterator(); keys.hasNext(); ) {
                            String key = keys.next();
                            for (Iterator<String> str = lang.getConfigurationSection("Lang." + key).getKeys(false).iterator(); str.hasNext(); ) {
                                String item = str.next();
                                update.executeUpdate("INSERT INTO `SubLang` (`Key`, `Value`) VALUES ('Lang." + key + "." + item + "', '" + URLEncoder.encode(lang.getRawString("Lang." + key + "." + item), "UTF-8") + "')");
                            }
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    update.close();
                } catch (SQLException | ClassNotFoundException e1) {
                    Bukkit.getLogger().severe(lprefix + "Could not setup Database!");
                    Bukkit.getLogger().severe(e1.getLocalizedMessage());
                    sql = null;

                    if (SubAPI.getSubServer(0).isRunning()) {
                        try {
                            SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy resetplugin");
                            Thread.sleep(100);
                            for (Iterator<String> keys = lang.getConfigurationSection("Lang").getKeys(false).iterator(); keys.hasNext(); ) {
                                String key = keys.next();
                                for (Iterator<String> str = lang.getConfigurationSection("Lang." + key).getKeys(false).iterator(); str.hasNext(); ) {
                                    String item = str.next();
                                    SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy lang Lang." + key + "." + item + " " + URLEncoder.encode(lang.getRawString("Lang." + key + "." + item), "UTF-8"));
                                    Thread.sleep(100);
                                }
                            }
                        } catch (UnsupportedEncodingException | InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                }

                int i = 0;
                for(Iterator<String> str = config.getConfigurationSection("Servers").getKeys(false).iterator(); str.hasNext(); ) {
                    String item = str.next();
                    do {
                        i++;
                    } while (Servers.keySet().contains((Object)i));

                    if (!SubServers.contains(item)) {
                        SubServers.add(item);
                        PIDs.put(item, i);
                        Servers.put(i, new SubServer(config.getBoolean("Servers." + item + ".enabled"), item, i, config.getInt("Servers." + item + ".port"),
                                config.getBoolean("Servers." + item + ".log"), config.getBoolean("Servers." + item + ".use-shared-chat"), new File(config.getRawString("Servers." + item + ".dir")),
                                new Executable(config.getRawString("Servers." + item + ".exec")), config.getBoolean("Servers." + item + ".auto-restart"), false, instance));
                    }
                }

                for(Iterator<String> str = SubServers.iterator(); str.hasNext(); ) {
                    String item = str.next();
                    if (sql != null) {
                        try {
                            Statement update = sql.getConnection().createStatement();
                            update.executeUpdate("INSERT INTO `SubServers` (`Name`, `IP`, `PID`, `Enabled`, `Shared_Chat`, `Temp`, `Running`) VALUES " +
                                    "('"+ item +"', '"+ config.getString("Settings.Server-IP")+":"+SubAPI.getSubServer(item).getPort() +"', '" + SubAPI.getSubServer(item).getPID() +"', '" + ((SubAPI.getSubServer(item).isEnabled())?"1":"0") +"', '"+ ((SubAPI.getSubServer(item).usesSharedChat())?"1":"0") +"', '"+ ((SubAPI.getSubServer(item).isTemporary())?"1":"0") +"', '"+ ((SubAPI.getSubServer(item).isRunning())?"1":"0") +"')");
                            update.close();
                        } catch (SQLException e) {
                            Bukkit.getLogger().severe(lprefix + "Problem Syncing Database!");
                            e.printStackTrace();
                        }
                    } else if (SubAPI.getSubServer(0).isRunning()) {
                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy addserver " + item + " " + config.getString("Settings.Server-IP") + " " + SubAPI.getSubServer(item).getPort() + " " + SubAPI.getSubServer(item).usesSharedChat());
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (sender != null) {
                    (sender).sendMessage(ChatColor.AQUA + lprefix + lang.getString("Lang.Debug.Config-Reload"));
                }
                Bukkit.getLogger().info(lprefix + lang.getString("Lang.Debug.Config-Reload"));
            }
        }.runTaskAsynchronously(Plugin);
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
