package net.ME1312.SubServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;

import net.ME1312.SubServer.Commands.*;
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

/**
 * Main Class &amp; API Class
 */
public class FakeProxyServer extends BungeeCord {
    public List<String> SubServers = new ArrayList<String>();
    public HashMap<String, SubServerInfo> ConfigServers = new HashMap<String, SubServerInfo>();
    public HashMap<String, SubServerInfo> ServerInfo = new HashMap<String, SubServerInfo>();
    public HashMap<String, SubServerInfo> HiddenServerInfo = new HashMap<String, SubServerInfo>();
    
    public String lprefix;
    public Configuration configuration;
    public HashMap<String, String> lang = new HashMap<String, String>();
    public List<MySQL> sql = new ArrayList<MySQL>();
    public Timer sqltimer = new Timer("SQL Refresh");

    private final PluginDescription Plugin;
    private boolean running = false;

    protected FakeProxyServer() throws Exception {
        super();

        PluginDescription Plugin = new PluginDescription();
        Plugin.setName("SubServers");
        Plugin.setAuthor("ME1312");
        Plugin.setVersion("1.9.2b");
        this.Plugin = Plugin;

        EnablePlugin();
    }

    // Plugin Methods

    protected void EnablePlugin() {
        if (!running) {
            lprefix = Plugin.getName() + " \u00BB ";
            running = true;
            System.out.println("Enabled " + Plugin.getName() + " v" + Plugin.getVersion() + " by " + Plugin.getAuthor());

            if (!(new File("./config.yml").exists())) copyFromJar("net/ME1312/SubServer/config.yml", "./config.yml");
            try {
                configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File("./config.yml"));
                if (configuration.getString("stats").equalsIgnoreCase("")) {
                    configuration.set("stats", UUID.randomUUID().toString());
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File("./config.yml"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!(new File("./modules.yml").exists())) copyFromJar("net/ME1312/SubServer/modules.yml", "./modules.yml");
            if (!(new File("./modules").exists())) new File("./modules").mkdirs();

            if (!(new File("./sql.yml").exists())) copyFromJar("net/ME1312/SubServer/sql.yml", "./sql.yml");
            try {
                Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File("./sql.yml"));
                Iterator iterator = config.get("SQL", Arrays.asList(new Map[]{new HashMap()})).iterator();
                int i = 0;

                while(iterator.hasNext()) {
                    Map val = (Map) iterator.next();
                    MySQL connection = null;
                    i++;
                    try {
                        connection = new MySQL(getValueFromArray(config, "hostname", "127.0.0.1", val), Integer.toString(getValueFromArray(config, "port", 3306, val)), getValueFromArray(config, "database", "minecraft", val),
                                getValueFromArray(config, "username", "root", val), getValueFromArray(config, "password", "", val));
                        connection.openConnection();
                        sql.add(connection);
                    } catch (ClassNotFoundException | SQLException e) {
                        System.out.println("Could not connect to Database #"+ i +":");
                        System.out.println(e.getLocalizedMessage());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (Iterator<String> str = configuration.getSection("servers").getKeys().iterator(); str.hasNext(); ) {
                String item = str.next();
                ConfigServers.put(item, new SubServerInfo((BungeeServerInfo) constructServerInfo(item, new InetSocketAddress(configuration.getString("servers." + item + ".address").split(":")[0],
                                Integer.parseInt(configuration.getString("servers." + item + ".address").split(":")[1])), ChatColor.translateAlternateColorCodes('&', configuration.getString("servers." + item + ".motd")),
                        configuration.getBoolean("servers." + item + ".restricted")), configuration.getBoolean("servers." + item + ".use-shared-chat")));
            }

            getPluginManager().registerListener(null, new PlayerListener(this));
            getPluginManager().registerCommand(null, new ProxyCMD(this, "subproxy"));
            if (sql.isEmpty()) getPluginManager().registerCommand(null, new SubDebugCMD(this, "subconf@proxy"));

            if (!configuration.getStringList("disabled_commands").contains("/go")) getPluginManager().registerCommand(null, new NavCMD(this, "go"));
            if (!configuration.getStringList("disabled_commands").contains("/server")) getPluginManager().registerCommand(null, new NavCMD(this, "server"));
            if (!configuration.getStringList("disabled_commands").contains("/glist")) getPluginManager().registerCommand(null, new ListCMD(this, "glist"));
            if (!configuration.getStringList("disabled_commands").contains("/find")) getPluginManager().registerCommand(null, new FindCMD(this, "find"));

            if (!sql.isEmpty()) {
                sqltimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            Statement update = sql.get(0).getConnection().createStatement();
                            ResultSet results = update.executeQuery("SELECT * FROM `SubLang`");
                            lang.clear();
                            try {
                                while (results.next()) {
                                    lang.put(results.getString("Key"), URLDecoder.decode(results.getString("Value"), "UTF-8"));
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            results.close();
                            update.close();

                            List<String> uservers = new ArrayList<String>();

                            for (Iterator<MySQL> connections = sql.iterator(); connections.hasNext(); ) {
                                MySQL item = connections.next();
                                update = item.getConnection().createStatement();
                                results = update.executeQuery("SELECT * FROM `SubServers`");
                                while (results.next()) {
                                    if (!results.getString("Name").contains("!")) {
                                        if (!(ServerInfo.keySet().contains(results.getString("Name")) || ConfigServers.keySet().contains(results.getString("Name")))) {
                                            ServerInfo.put(results.getString("Name"), new SubServerInfo((BungeeServerInfo) constructServerInfo(results.getString("Name"),
                                                    new InetSocketAddress(results.getString("IP").split("\\:")[0], Integer.parseInt(results.getString("IP").split("\\:")[1])),
                                                    ((ConfigServers.keySet().contains(results.getString("Name"))) ? ConfigServers.get(results.getString("Name")).getMotd() : "SubServer-" + results.getString("Name")), false),
                                                    results.getBoolean("Shared_Chat")));
                                            SubServers.add(results.getString("Name"));
                                        }
                                    } else {
                                        if (!(HiddenServerInfo.keySet().contains(results.getString("Name").replace("!", "")) || ConfigServers.keySet().contains(results.getString("Name")))) {
                                            HiddenServerInfo.put(results.getString("Name").replace("!", ""), new SubServerInfo((BungeeServerInfo) constructServerInfo(results.getString("Name").replace("!", ""),
                                                    new InetSocketAddress(results.getString("IP").split("\\:")[0], Integer.parseInt(results.getString("IP").split("\\:")[1])),
                                                    ((ConfigServers.keySet().contains(results.getString("Name"))) ? ConfigServers.get(results.getString("Name")).getMotd() : "SubServer-" + results.getString("Name").replace("!", "")), false),
                                                    results.getBoolean("Shared_Chat")));
                                            SubServers.add(results.getString("Name"));
                                        }
                                    }
                                    uservers.add(results.getString("Name"));
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
                            }

                            List<String> oservers = new ArrayList<String>();
                            oservers.addAll(SubServers);
                            for (String server : oservers) {
                                if (!ConfigServers.keySet().contains(server) && !uservers.contains(server)) {
                                    if (!server.contains("!")) {
                                        ServerInfo.get(server).destroy();
                                        ServerInfo.remove(server);
                                    } else {
                                        HiddenServerInfo.get(server.replace("!", "")).destroy();
                                        HiddenServerInfo.remove(server.replace("!", ""));
                                    }
                                    SubServers.remove(server);
                                }
                            }
                        } catch (SQLException e) {
                            System.out.println("Problem Syncing Database(s)!");
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

            for(Iterator<SubServerInfo> servers = HiddenServerInfo.values().iterator(); servers.hasNext(); ) {
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

    private <T> T getValueFromArray(Configuration config, String path, T def, Map submap) {
        int index = path.indexOf(46);
        if(index == -1) {
            Object first1 = submap.get(path);
            if(first1 == null && def != null) {
                first1 = def;
            }

            return (T) first1;
        } else {
            String first = path.substring(0, index);
            String second = path.substring(index + 1, path.length());
            Object sub = (Map)submap.get(first);
            if(sub == null) {
                sub = new LinkedHashMap();
                submap.put(first, sub);
            }

            return getValueFromArray(config, second, def, (Map)sub);
        }
    }

    // API Methods

    /**
     * Gets name of Software
     *
     * @return Software name
     */
    @Override
    public String getName() {
        return Plugin.getName() + "@BungeeCord";
    }

    /**
     * Gets the SubServers Proxy Version
     *
     * @return The Version as a String
     */
    public String getWrapperVersion() {
        return Plugin.getVersion();
    }

    /**
     * Gets SubServer plugin info
     *
     * @return Plugin info
     */
    public PluginDescription getWrapperInfo() {
        return Plugin;
    }

    /**
     * Gets Servers
     *
     * @return Servers (In Bungee Format)
     */
    @Override
    public Map<String, ServerInfo> getServers() {
        HashMap<String, ServerInfo> map = new HashMap<String, ServerInfo>();
        map.putAll(ConfigServers);
        map.putAll(ServerInfo);
        map.putAll(HiddenServerInfo);
        return map;
    }

    /**
     * Gets Servers
     *
     * @return Servers (In SubServers Format)
     */
    public HashMap<String, SubServerInfo> getSubServers() {
        HashMap<String, SubServerInfo> map = new HashMap<String, SubServerInfo>();
        map.putAll(ConfigServers);
        map.putAll(ServerInfo);
        map.putAll(HiddenServerInfo);
        return map;
    }

    /**
     * Gets Info for a Specific Server
     *
     * @param server Server's Name
     * @return Server Info (In Bungee Format)
     */
    @Override
    public ServerInfo getServerInfo(String server) {
        return getServers().get(server);
    }

    /**
     * Gets Info for a Specific Server
     *
     * @param server Server's Name
     * @return Server Info (In SubServers Format)
     */
    public SubServerInfo getSubServerInfo(String server) {
        return getSubServers().get(server);
    }

    /**
     * Adds a Server to BungeeCord
     *
     * @param info Server Info to add
     */
    public void addServerInfo(SubServerInfo info) {
        if (!info.getName().contains("!")) {
            ServerInfo.put(info.getName(), info);
        } else {
            HiddenServerInfo.put(info.getName().replace("!", ""), info);
        }
    }

    /**
     * Adds a Server to BungeeCord
     *
     * @param name Key Name
     * @param info Server Info to add
     */
    public void addServerInfo(String name, SubServerInfo info) {
        if (!name.contains("!")) {
            ServerInfo.put(name, info);
        } else {
            HiddenServerInfo.put(name.replace("!", ""), info);
        }
    }

    /**
     * Grabs a Lang value
     *
     * @param key Key to use
     * @return Lang Value (or null if there's no such value)
     */
    public String getLangValue(String key) {
        return lang.get(key);
    }

    /**
     * Get Lang Values in Hashmap form
     *
     * @return Lang Values
     */
    public HashMap<String, String> getLang() {
        return lang;
    }

    /**
     * Stops BungeeCord
     */
    @Override
    public void stop() {
        DisablePlugin();
        super.stop();
    }

    /**
     * Stops BungeeCord with a Message
     */
    @Override
    public void stop(String reason) {
        DisablePlugin();
        super.stop(reason);
    }
}