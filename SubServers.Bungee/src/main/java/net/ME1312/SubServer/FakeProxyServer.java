package net.ME1312.SubServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;

import net.ME1312.SubServer.Commands.FindCMD;
import net.ME1312.SubServer.Commands.ListCMD;
import net.ME1312.SubServer.Commands.NavCMD;
import net.ME1312.SubServer.Commands.SubDebugCMD;
import net.ME1312.SubServer.Libraries.SQL.MySQL;
import net.ME1312.SubServer.Libraries.SubServerInfo;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class FakeProxyServer extends BungeeCord {
    public List<String> SubServers = new ArrayList<String>();
    public HashMap<String, SubServerInfo> ConfigServers = new HashMap<String, SubServerInfo>();
    public HashMap<String, SubServerInfo> ServerInfo = new HashMap<String, SubServerInfo>();
    public HashMap<String, SubServerInfo> PlayerServerInfo = new HashMap<String, SubServerInfo>();
    
    public String lprefix;
    public Configuration configuration;
    public HashMap<String, String> lang = new HashMap<String, String>();
    public MySQL sql;
    public Timer sqltimer = new Timer("SQL Refresh");

    private final PluginDescription Plugin;
    private boolean running = false;

    protected FakeProxyServer() throws Exception {
        super();

        PluginDescription Plugin = new PluginDescription();
        Plugin.setName("SubServers");
        Plugin.setAuthor("ME1312");
        Plugin.setVersion("1.8.9b");
        this.Plugin = Plugin;

        EnablePlugin();
    }

    // Plugin Methods

    protected void EnablePlugin() {
        if (!running) {
            lprefix = Plugin.getName() + " \u00BB ";
            running = true;
            System.out.println("Enabled " + Plugin.getName() + " v" + Plugin.getVersion() + " by " + Plugin.getAuthor());

            try {
                configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File("./config.yml"));
            } catch (IOException e) {
                copyFromJar("net/ME1312/SubServer/config.yml", "./config.yml");
                try {
                    configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File("./config.yml"));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            if (!(new File("./modules.yml").exists())) copyFromJar("net/ME1312/SubServer/modules.yml", "./modules.yml");
            if (!(new File("./modules").exists())) new File("./modules").mkdirs();

            if (!(new File("./sql.yml").exists())) copyFromJar("net/ME1312/SubServer/sql.yml", "./sql.yml");
            try {
                Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File("./sql.yml"));
                sql = new MySQL(config.getString("SQL.hostname"), Integer.toString(config.getInt("SQL.port")), config.getString("SQL.database"),
                        config.getString("SQL.username"), config.getString("SQL.password"));
                sql.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException | SQLException e) {
                System.out.println("Could not connect to Database:");
                System.out.println(e.getLocalizedMessage());
                sql = null;
            }

            for (Iterator<String> str = configuration.getSection("servers").getKeys().iterator(); str.hasNext(); ) {
                String item = str.next();
                ConfigServers.put(item, new SubServerInfo((BungeeServerInfo) constructServerInfo(item, new InetSocketAddress(configuration.getString("servers." + item + ".address").split(":")[0],
                                Integer.parseInt(configuration.getString("servers." + item + ".address").split(":")[1])), ChatColor.translateAlternateColorCodes('&', configuration.getString("servers." + item + ".motd")),
                        configuration.getBoolean("servers." + item + ".restricted")), configuration.getBoolean("servers." + item + ".use-shared-chat")));
            }

            getPluginManager().registerListener(null, new PlayerListener(this));
            if (sql == null) getPluginManager().registerCommand(null, new SubDebugCMD(this, "subconf@proxy"));

            if (!configuration.getStringList("disabled_commands").contains("/go")) getPluginManager().registerCommand(null, new NavCMD(this, "go"));
            if (!configuration.getStringList("disabled_commands").contains("/server")) getPluginManager().registerCommand(null, new NavCMD(this, "server"));
            if (!configuration.getStringList("disabled_commands").contains("/glist")) getPluginManager().registerCommand(null, new ListCMD(this, "glist"));
            if (!configuration.getStringList("disabled_commands").contains("/find")) getPluginManager().registerCommand(null, new FindCMD(this, "find"));

            if (sql != null) {
                sqltimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            Statement update = sql.getConnection().createStatement();
                            ResultSet results = update.executeQuery("SELECT * FROM `SubServers`");
                            for(Iterator<SubServerInfo> servers = ServerInfo.values().iterator(); servers.hasNext(); ) {
                                servers.next().destroy();
                            }
                            ServerInfo.clear();
                            for(Iterator<SubServerInfo> servers = PlayerServerInfo.values().iterator(); servers.hasNext(); ) {
                                servers.next().destroy();
                            }
                            PlayerServerInfo.clear();
                            while (results.next()) {
                                if (!results.getString("Name").contains("!")) {
                                    ServerInfo.put(results.getString("Name"), new SubServerInfo((BungeeServerInfo)constructServerInfo(results.getString("Name"),
                                            new InetSocketAddress(results.getString("IP").split("\\:")[0], Integer.parseInt(results.getString("IP").split("\\:")[1])),
                                            ((ConfigServers.keySet().contains(results.getString("Name")))?ConfigServers.get(results.getString("Name")).getMotd():"SubServer-" + results.getString("Name")), false),
                                            results.getBoolean("Shared_Chat")));
                                } else {
                                    PlayerServerInfo.put(results.getString("Name").replace("!", ""), new SubServerInfo((BungeeServerInfo)constructServerInfo(results.getString("Name").replace("!", ""),
                                            new InetSocketAddress(results.getString("IP").split("\\:")[0], Integer.parseInt(results.getString("IP").split("\\:")[1])),
                                            ((ConfigServers.keySet().contains(results.getString("Name")))?ConfigServers.get(results.getString("Name")).getMotd():"SubServer-" + results.getString("Name").replace("!", "")), false),
                                            results.getBoolean("Shared_Chat")));
                                }
                                SubServers.add(results.getString("Name"));
                            }
                            results.close();
                            results = update.executeQuery("SELECT * FROM `SubLang`");
                            lang.clear();
                            try {
                                while (results.next()) {
                                    lang.put(results.getString("Key"), URLDecoder.decode(results.getString("Value"), "UTF-8"));
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            results.close();
                            results = update.executeQuery("SELECT * FROM `SubQueue` WHERE PID='-1'");
                            while (results.next()) {
                                if (results.getInt("Type") == 4) {
                                    getPluginManager().dispatchCommand(getConsole(), results.getString("Args"));
                                }
                            }
                            results.close();
                            update.executeUpdate("DELETE FROM `SubQueue` WHERE PID='-1'");
                            update.close();
                        } catch (SQLException e) {
                            System.out.println("Problem Syncing Database");
                            e.printStackTrace();
                        }
                    }
                }, 0L, TimeUnit.SECONDS.toMillis(5));
            }
        }
    }

    protected void DisablePlugin() {
        if (running) {
            for(Iterator<SubServerInfo> servers = ConfigServers.values().iterator(); servers.hasNext(); ) {
                servers.next().destroy();
            }

            for(Iterator<SubServerInfo> servers = ServerInfo.values().iterator(); servers.hasNext(); ) {
                servers.next().destroy();
            }

            for(Iterator<SubServerInfo> servers = PlayerServerInfo.values().iterator(); servers.hasNext(); ) {
                servers.next().destroy();
            }
            sqltimer.cancel();
            running = false;
            System.out.println(Plugin.getName() + " Proxy Shutting Down...");
        }
    }

    private void copyFromJar(String resource, String destination) {
        InputStream resStreamIn = FakeProxyServer.class.getClassLoader().getResourceAsStream(resource);
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

    // API Methods

    @Override
    public String getName() {
        return Plugin.getName() + "@BungeeCord";
    }

    public PluginDescription getPluginInfo() {
        return Plugin;
    }

    @Override
    public Map<String, ServerInfo> getServers() {
        HashMap<String, ServerInfo> map = new HashMap<String, ServerInfo>();
        map.putAll(ConfigServers);
        map.putAll(ServerInfo);
        map.putAll(PlayerServerInfo);
        return map;
    }

    public HashMap<String, SubServerInfo> getSubServers() {
        HashMap<String, SubServerInfo> map = new HashMap<String, SubServerInfo>();
        map.putAll(ConfigServers);
        map.putAll(ServerInfo);
        map.putAll(PlayerServerInfo);
        return map;
    }

    @Override
    public ServerInfo getServerInfo(String server) {
        return getServers().get(server);
    }

    public SubServerInfo getSubServerInfo(String server) {
        return getSubServers().get(server);
    }

    public String getLangValue(String key) {
        return lang.get(key);
    }

    public HashMap<String, String> getLang() {
        return lang;
    }

    @Override
    public void stop() {
        DisablePlugin();
        super.stop();
    }

    @Override
    public void stop(String reason) {
        DisablePlugin();
        super.stop(reason);
    }
}