package net.ME1312.SubServer.Executable;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.ME1312.SubServer.SubAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.ME1312.SubServer.SubPlugin;
import net.ME1312.SubServer.Libraries.Events.SubEvent;
import net.ME1312.SubServer.Libraries.Version.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class SubCreator {
    public enum ServerTypes {
        spigot,
        bukkit,
        vanilla,
        sponge,
    }

    private String Name;
    private int Port;
    private File Dir;
    private Executable Exec;
    private int Memory;
    private String Jar;
    private Version Version;
    private Player Player;
    private ServerTypes Type;
    private SubPlugin SubPlugin;
    private boolean Running;
    private Process Process;

    public SubCreator(String Name, int Port, File Dir, ServerTypes Type, Version Version, int Memory, Player Player, SubPlugin SubPlugin) {
        this.Name = Name;
        this.Port = Port;
        this.Dir = Dir;
        this.Memory = Memory;
        this.Version = Version;
        this.Player = Player;
        this.Type = Type;
        this.SubPlugin = SubPlugin;

        if (!Dir.exists()) Dir.mkdirs();

        if (Type == ServerTypes.spigot) {
            this.Jar = "Spigot.jar";
            this.Exec = new Executable("java -Xmx" + Memory + "M -Djline.terminal=jline.UnsupportedTerminal -Dcom.mojang.eula.agree=true -jar " + Jar);

            try {
                if (new File(SubPlugin.Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "Spigot-Plugins").exists() && new File(SubPlugin.Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "Bukkit-Plugins").exists()) CopyPlugins(ServerTypes.spigot);
                GenerateSpigotYAML();
                GenerateProperties();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (Type == ServerTypes.bukkit) {
            this.Jar = "Craftbukkit.jar";
            this.Exec = new Executable("java -Xmx" + Memory + "M -Djline.terminal=jline.UnsupportedTerminal -jar " + Jar + " -o false");

            try {
                if (new File(SubPlugin.Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "Bukkit-Plugins").exists()) CopyPlugins(ServerTypes.bukkit);
                GenerateEULA();
                GenerateProperties();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (Type == ServerTypes.vanilla) {
            this.Jar = "Vanilla.jar";
            this.Exec = new Executable("java -Xmx" + Memory + "M -jar " + Jar + " nogui");

            try {
                GenerateEULA();
                GenerateProperties();
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (Type == ServerTypes.sponge) {
            try {
                Document spongexml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(readAll(new BufferedReader(new InputStreamReader(new URL("http://files.minecraftforge.net/maven/org/spongepowered/spongeforge/maven-metadata.xml").openStream(), Charset.forName("UTF-8")))))));
                Document forgexml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(readAll(new BufferedReader(new InputStreamReader(new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/maven-metadata.xml").openStream(), Charset.forName("UTF-8")))))));

                NodeList spnodeList = spongexml.getElementsByTagName("version");
                Version spversion = null;
                for (int i = 0; i < spnodeList.getLength(); i++) {
                    Node node = spnodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        if (node.getTextContent().contains(Version.toString()) && (spversion == null || new Version(node.getTextContent()).compareTo(spversion) >= 0)) {
                            spversion = new Version(node.getTextContent());
                        }
                    }
                }

                NodeList mcfnodeList = forgexml.getElementsByTagName("version");
                Version mcfversion = null;
                for (int i = 0; i < mcfnodeList.getLength(); i++) {
                    Node node = mcfnodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        if (node.getTextContent().contains(spversion.toString().split("\\-")[1]) && (mcfversion == null || new Version(node.getTextContent()).compareTo(mcfversion) >= 0)) {
                            mcfversion = new Version(node.getTextContent());
                        }
                    }
                }

                if (new File(SubPlugin.Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "Sponge-Mods").exists() && new File(SubPlugin.Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "Sponge-Config").exists()) CopyPlugins(ServerTypes.sponge);
                this.Jar = "forge-" + mcfversion.toString() + "-universal.jar";
                this.Exec = new Executable("java -Xmx" + Memory + "M -jar " + Jar);
                this.Version = new Version(mcfversion.toString() + "::" + spversion.toString());

                GenerateEULA();
                GenerateProperties();
                GenerateSpongeConf();
            } catch (ParserConfigurationException | IOException | SAXException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void CopyPlugins(final ServerTypes type) throws IOException {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (type == ServerTypes.bukkit || type == ServerTypes.spigot) {
                    copyFolder(new File(SubPlugin.Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "Bukkit-Plugins"), new File(Dir, "plugins"));
                    if (type == ServerTypes.spigot) {
                        copyFolder(new File(SubPlugin.Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "Spigot-Plugins"), new File(Dir, "plugins"));
                    }
                } else if (type == ServerTypes.sponge) {
                    copyFolder(new File(SubPlugin.Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "Sponge-Mods"), new File(Dir, "mods"));
                    copyFolder(new File(SubPlugin.Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "Sponge-Config"), new File(Dir, "config"));
                }
            }
        }.runTaskAsynchronously(SubPlugin.Plugin);
    }

    private void GenerateEULA() throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(new File(Dir, "eula.txt"), "UTF-8");

        writer.println("#By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).");
        writer.println("#" + new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy").format(Calendar.getInstance().getTime()));
        writer.println("eula=true");

        writer.close();
    }

    private void GenerateProperties() throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(new File(Dir, "server.properties"), "UTF-8");

        writer.println("#Minecraft server properties");
        writer.println("#" + new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy").format(Calendar.getInstance().getTime()));
        writer.println("generator-settings=");
        writer.println("op-permission-level=4");
        writer.println("allow-nether=true");
        writer.println("resource-pack-hash=");
        writer.println("level-name=world");
        writer.println("enable-query=true");
        writer.println("allow-flight=false");
        writer.println("announce-player-achievements=false");
        writer.println("server-port=" + Port);
        writer.println("max-world-size=29999984");
        writer.println("level-type=DEFAULT");
        writer.println("enable-rcon=false");
        writer.println("level-seed=");
        writer.println("force-gamemode=false");
        writer.println("server-ip=" + SubPlugin.config.getString("Settings.Server-IP"));
        writer.println("network-compression-threshold=256");
        writer.println("max-build-height=256");
        writer.println("spawn-npcs=true");
        writer.println("white-list=false");
        writer.println("spawn-animals=true");
        writer.println("snooper-enabled=true");
        writer.println("online-mode=false");
        writer.println("resource-pack=");
        writer.println("pvp=true");
        writer.println("difficulty=1");
        writer.println("enable-command-block=true");
        writer.println("gamemode=0");
        writer.println("player-idle-timeout=0");
        writer.println("max-players=20");
        writer.println("max-tick-time=60000");
        writer.println("spawn-monsters=true");
        writer.println("generate-structures=true");
        writer.println("view-distance=10");
        writer.println("motd=A Generated SubServer");

        writer.close();
    }

    private void GenerateSpigotYAML() throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(new File(Dir, "spigot.yml"), "UTF-8");

        writer.println("# This is the main configuration file for Spigot.");
        writer.println("# As you can see, there's tons to configure. Some options may impact gameplay, so use");
        writer.println("# with caution, and make sure you know what each option does before configuring.");
        writer.println("# For a reference for any variable inside this file, check out the Spigot wiki at");
        writer.println("# http://www.spigotmc.org/wiki/spigot-configuration/");
        writer.println("#");
        writer.println("# If you need help with the configuration or have any questions related to Spigot,");
        writer.println("# join us at the IRC or drop by our forums and leave a post.");
        writer.println("#");
        writer.println("# IRC: #spigot @ irc.spi.gt ( http://www.spigotmc.org/pages/irc/ )");
        writer.println("# Forums: http://www.spigotmc.org/");
        writer.println();
        writer.println("config-version: 8");
        writer.println("settings:");
        writer.println("  debug: false");
        writer.println("  save-user-cache-on-stop-only: false");
        writer.println("  bungeecord: true");
        writer.println("  late-bind: false");
        writer.println("  sample-count: 12");
        writer.println("  player-shuffle: 0");
        writer.println("  filter-creative-items: true");
        writer.println("  user-cache-size: 1000");
        writer.println("  int-cache-limit: 1024");
        writer.println("  moved-wrongly-threshold: 0.0625");
        writer.println("  moved-too-quickly-threshold: 100.0");
        writer.println("  timeout-time: 60");
        writer.println("  restart-on-crash: false");
        writer.println("  restart-script: ./start.sh");
        writer.println("  netty-threads: 4");
        writer.println("  attribute:");
        writer.println("    maxHealth:");
        writer.println("      max: 2048.0");
        writer.println("    movementSpeed:");
        writer.println("      max: 2048.0");
        writer.println("    attackDamage:");
        writer.println("      max: 2048.0");
        writer.println("commands:");
        writer.println("  tab-complete: 0");
        writer.println("  log: true");
        writer.println("  spam-exclusions:");
        writer.println("  - /skill");
        writer.println("  silent-commandblock-console: true");
        writer.println("  replace-commands:");
        writer.println("  - setblock");
        writer.println("  - summon");
        writer.println("  - testforblock");
        writer.println("  - tellraw");
        writer.println("messages:");
        writer.println("  whitelist: You are not whitelisted on this server!");
        writer.println("  unknown-command: Unknown command. Type \"/help\" for help.");
        writer.println("  server-full: The server is full!");
        writer.println("  outdated-client: Outdated client! Please use {0}");
        writer.println("  outdated-server: Outdated server! I'm still on {0}");
        writer.println("  restart: Server is restarting");
        writer.println("stats:");
        writer.println("  disable-saving: false");
        writer.println("  forced-stats: {}");
        writer.println("world-settings:");
        writer.println("  default:");
        writer.println("    verbose: true");
        writer.println("    wither-spawn-sound-radius: 0");
        writer.println("    view-distance: 10");
        writer.println("    item-despawn-rate: 6000");
        writer.println("    merge-radius:");
        writer.println("      item: 2.5");
        writer.println("      exp: 3.0");
        writer.println("    arrow-despawn-rate: 1200");
        writer.println("    enable-zombie-pigmen-portal-spawns: true");
        writer.println("    zombie-aggressive-towards-villager: true");
        writer.println("    hanging-tick-frequency: 100");
        writer.println("    max-bulk-chunks: 10");
        writer.println("    max-entity-collisions: 8");
        writer.println("    random-light-updates: false");
        writer.println("    save-structure-info: true");
        writer.println("    mob-spawn-range: 4");
        writer.println("    anti-xray:");
        writer.println("      enabled: true");
        writer.println("      engine-mode: 1");
        writer.println("      hide-blocks:");
        writer.println("      - 14");
        writer.println("      - 15");
        writer.println("      - 16");
        writer.println("      - 21");
        writer.println("      - 48");
        writer.println("      - 49");
        writer.println("      - 54");
        writer.println("      - 56");
        writer.println("      - 73");
        writer.println("      - 74");
        writer.println("      - 82");
        writer.println("      - 129");
        writer.println("      - 130");
        writer.println("      replace-blocks:");
        writer.println("      - 1");
        writer.println("      - 5");
        writer.println("    dragon-death-sound-radius: 0");
        writer.println("    seed-village: 10387312");
        writer.println("    seed-feature: 14357617");
        writer.println("    hunger:");
        writer.println("      walk-exhaustion: 0.2");
        writer.println("      sprint-exhaustion: 0.8");
        writer.println("      combat-exhaustion: 0.3");
        writer.println("      regen-exhaustion: 3.0");
        writer.println("    max-tnt-per-tick: 100");
        writer.println("    max-tick-time:");
        writer.println("      tile: 50");
        writer.println("      entity: 50");
        writer.println("    entity-activation-range:");
        writer.println("      animals: 32");
        writer.println("      monsters: 32");
        writer.println("      misc: 16");
        writer.println("    entity-tracking-range:");
        writer.println("      players: 48");
        writer.println("      animals: 48");
        writer.println("      monsters: 48");
        writer.println("      misc: 32");
        writer.println("      other: 64");
        writer.println("    ticks-per:");
        writer.println("      hopper-transfer: 8");
        writer.println("      hopper-check: 8");
        writer.println("    hopper-amount: 1");
        writer.println("    growth:");
        writer.println("      cactus-modifier: 100");
        writer.println("      cane-modifier: 100");
        writer.println("      melon-modifier: 100");
        writer.println("      mushroom-modifier: 100");
        writer.println("      pumpkin-modifier: 100");
        writer.println("      sapling-modifier: 100");
        writer.println("      wheat-modifier: 100");
        writer.println("      netherwart-modifier: 100");
        writer.println("    nerf-spawner-mobs: false");
        writer.println("    chunks-per-tick: 650");
        writer.println("    clear-tick-list: false");
        writer.println();

        writer.close();
    }

    private void GenerateSpongeConf() throws FileNotFoundException, UnsupportedEncodingException {
        new File(Dir, "config" + File.separator + "sponge").mkdirs();
        PrintWriter writer = new PrintWriter(new File(Dir, "config" + File.separator + "sponge" + File.separator + "global.conf"), "UTF-8");
        writer.println("# 1.0");
        writer.println("#");
        writer.println("# # If you need help with the configuration or have any questions related to Sponge,");
        writer.println("# # join us at the IRC or drop by our forums and leave a post.");
        writer.println("#");
        writer.println("# # IRC: #sponge @ irc.esper.net ( http://webchat.esper.net/?channel=sponge )");
        writer.println("# # Forums: https://forums.spongepowered.org/");
        writer.println("#");
        writer.println();
        writer.println("sponge {");
        writer.println("    block-tracking {");
        writer.println("        # If enabled, adds player tracking support for block positions. Note: This should only be disabled if you do not care who caused a block to change.");
        writer.println("        enabled=true");
        writer.println("    }");
        writer.println("    bungeecord {");
        writer.println("        # If enabled, allows BungeeCord to forward IP address, UUID, and Game Profile to this server");
        writer.println("        ip-forwarding=true");
        writer.println("    }");
        writer.println("    commands {}");
        writer.println("    debug {");
        writer.println("        # Dump chunks in the event of a deadlock");
        writer.println("        dump-chunks-on-deadlock=false");
        writer.println("        # Dump the heap in the event of a deadlock");
        writer.println("        dump-heap-on-deadlock=false");
        writer.println("        # Dump the server thread on deadlock warning");
        writer.println("        dump-threads-on-warn=false");
        writer.println("        # Enable Java's thread contention monitoring for thread dumps");
        writer.println("        thread-contention-monitoring=false");
        writer.println("    }");
        writer.println("    entity {");
        writer.println("        # Number of colliding entities in one spot before logging a warning. Set to 0 to disable");
        writer.println("        collision-warn-size=200");
        writer.println("        # Number of entities in one dimension before logging a warning. Set to 0 to disable");
        writer.println("        count-warn-size=0");
        writer.println("        # Number of ticks before a painting is respawned on clients when their art is changed");
        writer.println("        entity-painting-respawn-delay=2");
        writer.println("        # Number of ticks before the fake player entry of a human is removed from the tab list (range of 0 to 100 ticks).");
        writer.println("        human-player-list-remove-delay=10");
        writer.println("        # Controls the time in ticks for when an item despawns.");
        writer.println("        item-despawn-rate=6000");
        writer.println("        # Max size of an entity's bounding box before removing it. Set to 0 to disable");
        writer.println("        max-bounding-box-size=1000");
        writer.println("        # Square of the max speed of an entity before removing it. Set to 0 to disable");
        writer.println("        max-speed=100");
        writer.println("    }");
        writer.println("    entity-activation-range {");
        writer.println("        ambient-activation-range=32");
        writer.println("        aquatic-activation-range=32");
        writer.println("        creature-activation-range=32");
        writer.println("        minecraft {");
        writer.println("            creature {");
        writer.println("                entityhorse=true");
        writer.println("                pig=true");
        writer.println("                sheep=true");
        writer.println("            }");
        writer.println("            enabled=true");
        writer.println("            misc {");
        writer.println("                item=true");
        writer.println("                minecartchest=true");
        writer.println("            }");
        writer.println("            monster {");
        writer.println("                guardian=true");
        writer.println("            }");
        writer.println("        }");
        writer.println("        misc-activation-range=16");
        writer.println("        monster-activation-range=32");
        writer.println("    }");
        writer.println("    general {");
        writer.println("        # Forces Chunk Loading on provide requests (speedup for mods that don't check if a chunk is loaded)");
        writer.println("        chunk-load-override=false");
        writer.println("        # Disable warning messages to server admins");
        writer.println("        disable-warnings=false");
        writer.println("    }");
        writer.println("    logging {");
        writer.println("        # Log when blocks are broken");
        writer.println("        block-break=false");
        writer.println("        # Log when blocks are modified");
        writer.println("        block-modify=false");
        writer.println("        # Log when blocks are placed");
        writer.println("        block-place=false");
        writer.println("        # Log when blocks are populated in a chunk");
        writer.println("        block-populate=false");
        writer.println("        # Log when blocks are placed by players and tracked");
        writer.println("        block-tracking=false");
        writer.println("        # Log when chunks are loaded");
        writer.println("        chunk-load=false");
        writer.println("        # Log when chunks are unloaded");
        writer.println("        chunk-unload=false");
        writer.println("        # Whether to log entity collision/count checks");
        writer.println("        entity-collision-checks=false");
        writer.println("        # Log when living entities are destroyed");
        writer.println("        entity-death=false");
        writer.println("        # Log when living entities are despawned");
        writer.println("        entity-despawn=false");
        writer.println("        # Log when living entities are spawned");
        writer.println("        entity-spawn=false");
        writer.println("        # Whether to log entity removals due to speed");
        writer.println("        entity-speed-removal=false");
        writer.println("        # Add stack traces to dev logging");
        writer.println("        log-stacktraces=false");
        writer.println("    }");
        writer.println("    modules {");
        writer.println("        bungeecord=true");
        writer.println("        entity-activation-range=true");
        writer.println("        timings=true");
        writer.println("    }");
        writer.println("    # Configuration options related to the Sql service, including connection aliases etc");
        writer.println("    sql {}");
        writer.println("    timings {");
        writer.println("        enabled=true");
        writer.println("        hidden-config-entries=[");
        writer.println("            \"sponge.sql\"");
        writer.println("        ]");
        writer.println("        history-interval=300");
        writer.println("        history-length=3600");
        writer.println("        server-name-privacy=false");
        writer.println("        verbose=false");
        writer.println("    }");
        writer.println("    world {");
        writer.println("        # Lava behaves like vanilla water when source block is removed");
        writer.println("        flowing-lava-decay=false");
        writer.println("        # Vanilla water source behavior - is infinite");
        writer.println("        infinite-water-source=false");
        writer.println("    }");
        writer.println("}");
        writer.println();

        writer.close();

    }

    public boolean run() {
        try {
            if (SubEvent.RunEvent(SubPlugin, SubEvent.Events.SubCreateEvent, new SubServer(true, Name, -1, Port, true, true, Dir, Exec, false, false, SubPlugin), Player, Type)) {
                run(true);
                return true;
            } else {
                return false;
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void run(boolean value) {
        if (value) {
            Running = true;
            new BukkitRunnable() {

                @Override
                public void run() {
                    Player.sendMessage(ChatColor.GOLD + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Server-Create-Loading"));
                    try {
                        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                            String GitBash = new File(new File(SubPlugin.config.getRawString("Settings.Server-Creation.git-dir")), "bin" + File.separatorChar + "bash.exe").getAbsolutePath();
                            InputStream input = null;
                            OutputStream output = null;
                            try {
                                input = new FileInputStream(new File(SubPlugin.Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "build-subserver.sh"));
                                output = new FileOutputStream(new File(Dir, "build-subserver.sh"));
                                byte[] buf = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = input.read(buf)) > 0) {
                                    output.write(buf, 0, bytesRead);
                                }
                            } finally {
                                if (input != null)
                                    input.close();
                                if (output != null)
                                    output.close();
                            }

                            if (!(new File(Dir, "build-subserver.sh").exists())) {
                                Bukkit.getLogger().severe(SubPlugin.lprefix + "Problem Copying Script!");
                            } else {
                                Process = Runtime.getRuntime().exec("\"" + GitBash + "\" --login -i -c \"bash build-subserver.sh " + Version.toString() + " " + Type.toString() + "\"", null, Dir);
                                SubConsole read = new SubConsole(Process.getInputStream(), "OUTPUT", SubPlugin.config.getBoolean("Settings.Server-Creation.log"), new File(Dir, "SubCreator-" + Type.toString() + "-" + Version.toString().replace("::", "@") + ".log"), SubPlugin.lang.getString("Lang.Create-Server.Log-Prefix") + Name, SubPlugin);
                                read.start();
                                try {
                                    Process.waitFor();
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (Process.exitValue() == 0) {
                                    Player.sendMessage(ChatColor.AQUA + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Server-Create-Done"));
                                    final int PID = (SubPlugin.SubServers.size() + 1);
                                    SubPlugin.Servers.put(PID, new SubServer(true, Name, PID, Port, true, true, Dir, Exec, false, false, SubPlugin));
                                    SubPlugin.PIDs.put(Name, PID);
                                    SubPlugin.SubServers.add(Name);

                                    if (SubPlugin.sql != null) {
                                        try {
                                            Statement update = SubPlugin.sql.getConnection().createStatement();
                                            update.executeUpdate("INSERT INTO `SubServers` (`Name`, `IP`, `PID`, `Enabled`, `Shared_Chat`, `Temp`, `Running`) VALUES " +
                                                    "('"+ Name +"', '"+ SubPlugin.config.getString("Settings.Server-IP")+":"+Integer.toString(Port) +"', '"+ PID +"', '1', '1', '0', '0')");
                                            update.close();
                                        } catch (SQLException e) {
                                            Bukkit.getLogger().severe("Problem Syncing Database!");
                                            e.printStackTrace();
                                        }
                                    } else if (SubAPI.getSubServer(0).isRunning()) SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy addserver " + Name + " " + SubPlugin.config.getString("Settings.Server-IP") + " " + Port + " true");
                                    SubPlugin.Servers.get(PID).start();

                                    SubPlugin.config.set("Servers." + Name + ".enabled", true);
                                    SubPlugin.config.set("Servers." + Name + ".port", Port);
                                    SubPlugin.config.set("Servers." + Name + ".run-on-launch", false);
                                    SubPlugin.config.set("Servers." + Name + ".log", true);
                                    SubPlugin.config.set("Servers." + Name + ".use-shared-chat", true);
                                    SubPlugin.config.set("Servers." + Name + ".dir", Dir.getPath());
                                    SubPlugin.config.set("Servers." + Name + ".exec", Exec.toString());
                                    SubPlugin.config.set("Servers." + Name + ".auto-restart", false);
                                    SubPlugin.config.saveConfig();
                                } else {
                                    Bukkit.getLogger().severe(SubPlugin.lprefix + "build-subserver.sh exited with an errors. Please try again.");
                                }
                            }
                        } else {
                            InputStream input = null;
                            OutputStream output = null;
                            try {
                                input = new FileInputStream(new File(SubPlugin.Plugin.getDataFolder() + File.separator + "SubCreator" + File.separator + "build-subserver.sh"));
                                output = new FileOutputStream(new File(Dir, "build-subserver.sh"));
                                byte[] buf = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = input.read(buf)) > 0) {
                                    output.write(buf, 0, bytesRead);
                                }
                            } finally {
                                if (input != null)
                                    input.close();
                                if (output != null)
                                    output.close();
                            }

                            if (!(new File(Dir, "build-subserver.sh").exists())) {
                                Bukkit.getLogger().severe(SubPlugin.lprefix + "Problem Copying Script!");
                            } else {
                                Process Process1 = Runtime.getRuntime().exec("chmod +x build-subserver.sh", null, Dir);
                                try {
                                    Process1.waitFor();
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (Process1.exitValue() != 0) {
                                    Bukkit.getLogger().warning(SubPlugin.lprefix + "Problem Setting Executable Permissions for build-subserver.sh");
                                    Bukkit.getLogger().warning("This may cause errors in the Build Process");
                                }

                                Process = Runtime.getRuntime().exec("bash build-subserver.sh " + Version.toString() + " " + Type.toString() + " " + System.getProperty("user.home"), null, Dir);
                                SubConsole read = new SubConsole(Process.getInputStream(), "OUTPUT", SubPlugin.config.getBoolean("Settings.Server-Creation.log"), new File(Dir, "SubCreator-" + Type.toString() + "-" + Version.toString().replace("::", "@") + ".log"), SubPlugin.lang.getString("Lang.Create-Server.Log-Prefix") + Name, SubPlugin);
                                read.start();
                                try {
                                    Process.waitFor();
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (Process.exitValue() == 0) {
                                    Player.sendMessage(ChatColor.AQUA + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Server-Create-Done").replace("$Server$", Name));
                                    final int PID = (SubPlugin.SubServers.size() + 1);
                                    SubPlugin.Servers.put(PID, new SubServer(true, Name, PID, Port, true, true, Dir, Exec, false, false, SubPlugin));
                                    SubPlugin.PIDs.put(Name, PID);
                                    SubPlugin.SubServers.add(Name);

                                    if (SubPlugin.sql != null) {
                                        try {
                                            Statement update = SubPlugin.sql.getConnection().createStatement();
                                            update.executeUpdate("INSERT INTO `SubServers` (`Name`, `IP`, `PID`, `Enabled`, `Shared_Chat`, `Temp`, `Running`) VALUES " +
                                                    "('"+ Name +"', '"+ SubPlugin.config.getString("Settings.Server-IP")+":"+Integer.toString(Port) +"', '"+ PID +"', '1', '1', '0', '0')");
                                            update.close();
                                        } catch (SQLException e) {
                                            Bukkit.getLogger().severe("Problem Syncing Database!");
                                            e.printStackTrace();
                                        }
                                    } else if (SubAPI.getSubServer(0).isRunning()) SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy addserver " + Name + " " + SubPlugin.config.getString("Settings.Server-IP") + " " + Port + " true");
                                    SubPlugin.Servers.get(PID).start();

                                    SubPlugin.config.set("Servers." + Name + ".enabled", true);
                                    SubPlugin.config.set("Servers." + Name + ".port", Port);
                                    SubPlugin.config.set("Servers." + Name + ".run-on-launch", false);
                                    SubPlugin.config.set("Servers." + Name + ".log", true);
                                    SubPlugin.config.set("Servers." + Name + ".use-shared-chat", true);
                                    SubPlugin.config.set("Servers." + Name + ".dir", Dir.getPath());
                                    SubPlugin.config.set("Servers." + Name + ".exec", Exec.toString());
                                    SubPlugin.config.set("Servers." + Name + ".auto-restart", false);
                                    SubPlugin.config.saveConfig();
                                } else {
                                    Bukkit.getLogger().severe(SubPlugin.lprefix + "build-subserver.sh exited with an errors. Please try again.");
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Running = false;
                }
            }.runTaskAsynchronously(SubPlugin.Plugin);
        }
    }

    public void waitFor() throws InterruptedException {
        Process.waitFor();
    }

    public boolean isRunning() {
        return Running;
    }

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private void copyFolder(File source, File destination) {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }

            String files[] = source.list();

            for (String file : files) {
                File srcFile = new File(source, file);
                File destFile = new File(destination, file);

                copyFolder(srcFile, destFile);
            }
        } else {
            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(source);
                out = new FileOutputStream(destination);

                byte[] buffer = new byte[1024];

                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } catch (Exception e) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}