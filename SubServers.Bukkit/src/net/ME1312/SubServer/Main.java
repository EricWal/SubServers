package net.ME1312.SubServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.Files;

import net.ME1312.SubServer.Executable.SubServerCreator;
import net.ME1312.SubServer.Executable.Executable;
import net.ME1312.SubServer.Executable.SubServer;
import net.ME1312.SubServer.Libraries.Config.ConfigFile;
import net.ME1312.SubServer.Libraries.Config.ConfigManager;
import net.ME1312.SubServer.Libraries.Events.SubListener;
import net.ME1312.SubServer.Libraries.Version.Version;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;

public class Main {
    public HashMap<Integer, SubServer> Servers = new HashMap<Integer, SubServer>();
    public HashMap<String, Integer> PIDs = new HashMap<String, Integer>();
    public HashMap<JavaPlugin, List<SubListener>> EventHandlers = new HashMap<JavaPlugin, List<SubListener>>();
    public List<String> SubServers = new ArrayList<String>();
    public JavaPlugin Plugin;
    public SubServerCreator ServerCreator;

    public String lprefix;
    public ConfigFile config;
    public ConfigFile lang;

    public Version PluginVersion;
    public Version MCVersion;

    private ConfigManager confmanager;
    private Main instance;

    protected Main(JavaPlugin plugin) throws IllegalArgumentException {
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
            Bukkit.getLogger().warning(lprefix + "Problem grabbing Minecraft Version! Assuming 1.8!");
            MCVersion = new Version("1.8");
        }

        Bukkit.getLogger().info(lprefix + "Loading Libraries for " + MCVersion);

        /**
         * Updates Configs if needed
         */
        if (!(new File(Plugin.getDataFolder() + File.separator + "config.yml").exists())) {
            copyFromJar("config.yml", Plugin.getDataFolder() + File.separator + "config.yml");
            Bukkit.getLogger().info(lprefix + "Created Config.yml!");
        } else if (!confmanager.getNewConfig("config.yml").getString("Settings.config-version").equalsIgnoreCase("1.8.8s+")) {
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
        } else if (!confmanager.getNewConfig("lang.yml").getString("config-version").equalsIgnoreCase("1.8.8j+")) {
            try {
                Files.move(new File(Plugin.getDataFolder() + File.separator + "lang.yml"), new File(Plugin.getDataFolder() + File.separator + "old-lang." + Math.round(Math.random() * 100000) + ".yml"));
                copyFromJar("lang.yml", Plugin.getDataFolder() + File.separator + "lang.yml");
                Bukkit.getLogger().info(lprefix + "Updated Lang.yml!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = confmanager.getNewConfig("config.yml");
        lang = confmanager.getNewConfig("lang.yml");
        SubServers.addAll(config.getConfigurationSection("Servers").getKeys(false));

        /**
         * Registers Listeners
         */
        pm.registerEvents(new net.ME1312.SubServer.GUI.GUIListener(this), Plugin);

        /**
         * Auto-Starts Servers,
         * Registers PIDs & Shells
         */
        PIDs.put("~Proxy", 0);
        Servers.put(0, new SubServer(config.getBoolean("Proxy.enabled"), "~Proxy", 0, 25565, config.getBoolean("Proxy.log"), false, new File(config.getRawString("Proxy.dir")),
                new Executable(config.getRawString("Proxy.exec")), config.getBoolean("Proxy.auto-restart"), false, this));

        int i = 0;
        for(Iterator<String> str = SubServers.iterator(); str.hasNext(); ) {
            String item = str.next();
            i++;
            PIDs.put(item, i);
            Servers.put(i, new SubServer(config.getBoolean("Servers." + item + ".enabled"), item, i, config.getInt("Servers." + item + ".port"), config.getBoolean("Servers." + item + ".log"),
                    config.getBoolean("Servers." + item + ".use-shared-chat"), new File(config.getRawString("Servers." + item + ".dir")), new Executable(config.getRawString("Servers." + item + ".exec")),
                    config.getBoolean("Servers." + item + ".auto-restart"), false, this));
            if (config.getBoolean("Servers." + item + ".enabled") && config.getBoolean("Servers." + item + ".run-on-launch")) {
                Servers.get(i).start();
            }
        }

        if ((config.getBoolean("Proxy.enabled") == true) && (config.getBoolean("Proxy.run-on-launch") == true)) {
            Servers.get(0).start();
        }

        /**
         * Registers Commands
         */
        Plugin.getCommand("subserver").setExecutor(new SubServersCMD(this));
        Plugin.getCommand("sub").setExecutor(new SubServersCMD(this));

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
                Servers.get(0).AutoRestart = false;
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
                    SubAPI.getSubServer(item).AutoRestart = false;
                    SubAPI.getSubServer(item).stop();
                    SubAPI.getSubServer(item).waitFor();
                    if (SubAPI.getSubServer(item).Temporary) {
                        Thread.sleep(500);
                    }
                    Thread.sleep(1000);
                    SubAPI.getSubServer(item).destroy();
                }
            }
            Bukkit.getLogger().info(lprefix + " Plugin Disabled.");
        } catch (InterruptedException e) {
            Bukkit.getLogger().severe(lprefix + "Problem Stopping Subservers.");
            Bukkit.getLogger().severe(lprefix + "Subservers will stay as Background Processes if not Stopped");
            e.printStackTrace();
            Bukkit.getLogger().warning(lprefix + "Config Not Saved: Preserved config from Invalid Changes.");
            Bukkit.getLogger().warning(lprefix + " Plugin Partially Disabled.");
        }
    }

    public void ReloadPlugin(@Nullable final Player sender) {
        if (!Servers.get(0).isRunning()) {
            Servers.get(0).destroy();
            Servers.remove(0);
        } else {
            SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy resetplugin");
        }

        int i = 0;
        List<String> SubServersStore = new ArrayList<String>();
        SubServersStore.addAll(SubServers);

        for(Iterator<String> str = SubServersStore.iterator(); str.hasNext(); ) {
            String item = str.next();
            i++;
            if (!Servers.get(i).isRunning()) {
                Servers.get(i).destroy();
                Servers.remove(i);
                PIDs.remove(item);
                SubServers.remove(item);
            }
        }
        config.reloadConfig();
        lang.reloadConfig();

        if (Servers.get(0) == null) {
            Servers.put(0, new SubServer(config.getBoolean("Proxy.enabled"), "~Proxy", 0, 25565, config.getBoolean("Proxy.log"), false, new File(config.getRawString("Proxy.dir")),
                    new Executable(config.getRawString("Proxy.exec")), config.getBoolean("Proxy.auto-restart"), false, this));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                int i = 0;
                if (SubAPI.getSubServer(0).isRunning()) {
                    try {
                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy lang Lang.Commands.Teleport " + lang.getString("Lang.Commands.Teleport").replace(" ", "%20"));
                        Thread.sleep(500);
                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy lang Lang.Commands.Teleport-Server-List " + lang.getString("Lang.Commands.Teleport-Server-List").replace(" ", "%20"));
                        Thread.sleep(500);
                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy lang Lang.Commands.Teleport-Player-Error " + lang.getString("Lang.Commands.Teleport-Player-Error").replace(" ", "%20"));
                        Thread.sleep(500);
                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy lang Lang.Commands.Teleport-Config-Error " + lang.getString("Lang.Commands.Teleport-Config-Error").replace(" ", "%20"));
                        Thread.sleep(500);
                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy lang Lang.Commands.Teleport-Permission-Error " + lang.getString("Lang.Commands.Teleport-Permission-Error").replace(" ", "%20"));
                        Thread.sleep(500);
                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy lang Lang.Commands.Teleport-Offline-Error " + lang.getString("Lang.Commands.Teleport-Offline-Error").replace(" ", "%20"));
                        Thread.sleep(500);
                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy lang Lang.Commands.Teleport-Console-Error " + lang.getString("Lang.Commands.Teleport-Console-Error").replace(" ", "%20"));
                        Thread.sleep(500);

                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy lang Lang.Proxy.Register-Server " + lang.getString("Lang.Proxy.Register-Server").replace(" ", "%20"));
                        Thread.sleep(500);
                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy lang Lang.Proxy.Remove-Server " + lang.getString("Lang.Proxy.Remove-Server").replace(" ", "%20"));
                        Thread.sleep(500);
                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy lang Lang.Proxy.Reset-Storage " + lang.getString("Lang.Proxy.Reset-Storage").replace(" ", "%20"));
                        Thread.sleep(500);
                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy lang Lang.Proxy.Chat-Format " + lang.getString("Lang.Proxy.Chat-Format").replace(" ", "%20"));
                        Thread.sleep(500);
                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy lang Lang.Proxy.Teleport " + lang.getString("Lang.Proxy.Teleport").replace(" ", "%20"));
                        Thread.sleep(500);

                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy addserver ~Lobby " + config.getString("Settings.Server-IP") + " " + config.getString("Settings.Lobby-Port") + " true");
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for(Iterator<String> str = config.getConfigurationSection("Servers").getKeys(false).iterator(); str.hasNext(); ) {
                    String item = str.next();
                    do {
                        i++;
                    } while (Servers.keySet().contains(i));

                    if (SubServers.contains(item)) {
                        i--;
                    } else {
                        SubServers.add(item);
                        PIDs.put(item, i);
                        Servers.put(i, new SubServer(config.getBoolean("Servers." + item + ".enabled"), item, i, config.getInt("Servers." + item + ".port"),
                                config.getBoolean("Servers." + item + ".log"), config.getBoolean("Servers." + item + ".use-shared-chat"), new File(config.getRawString("Servers." + item + ".dir")),
                                new Executable(config.getRawString("Servers." + item + ".exec")), config.getBoolean("Servers." + item + ".auto-restart"), false, instance));
                    }
                }

                for(Iterator<String> str = SubServers.iterator(); str.hasNext(); ) {
                    String item = str.next();
                    if (SubAPI.getSubServer(0).isRunning()) {
                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy addserver " + item + " " + config.getString("Settings.Server-IP") + " " + SubAPI.getSubServer(item).Port + " " + SubAPI.getSubServer(item).SharedChat);
                        try {
                            Thread.sleep(500);
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
        InputStream resStreamIn = Main.class.getClassLoader().getResourceAsStream(resource);
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
