package net.ME1312.SubServer.GUI;

import net.ME1312.SubServer.SubAPI;
import net.ME1312.SubServer.SubPlugin;
import net.ME1312.SubServer.Executable.SubCreator;
import net.ME1312.SubServer.Executable.SubCreator.ServerType;
import net.ME1312.SubServer.Libraries.Version.Version;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * GUI Listener
 *
 * @author ME1312
 */
@SuppressWarnings("deprecation")
public class SubGUIListener implements Listener {
    private SubPlugin SubPlugin;

    public SubGUIListener(SubPlugin SubPlugin) {
        this.SubPlugin = SubPlugin;
    }

    /**
     * GUI Trigger Item Listener
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action a = event.getAction();
        ItemStack is = event.getItem();

        if ((a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) && is != null && is.getType() == new ItemStack(SubPlugin.config.getBlockID("Settings.GUI.Trigger-Item")[0], 1, (short) SubPlugin.config.getBlockID("Settings.GUI.Trigger-Item")[1]).getType() && event.getPlayer().hasPermission("SubServer.Command") && SubPlugin.config.getBoolean("Settings.GUI.Enabled")) {
            new SubGUI(event.getPlayer(), 0, null, SubPlugin);
            event.setCancelled(true);
        }
    }

    /**
     * Server Selector Listener
     */
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.getInventory().getName().contains(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-List-Title").replace("$Int$", ""))) {
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().hasItemMeta()) {
                if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.DARK_RED.toString() + SubPlugin.lang.getString("Lang.GUI.Exit"))) {
                    player.closeInventory();
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.DARK_GREEN.toString() + SubPlugin.lang.getString("Lang.GUI.Back"))) {
                    new SubGUI(player, (Integer.parseInt(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN.toString() + SubPlugin.lang.getString("Lang.GUI.Server-List-Title").replace("$Int$", ""), "")) - 2), null, SubPlugin);
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.DARK_GREEN.toString() + SubPlugin.lang.getString("Lang.GUI.Next"))) {
                    new SubGUI(player, Integer.parseInt(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN.toString() + SubPlugin.lang.getString("Lang.GUI.Server-List-Title").replace("$Int$", ""), "")), null, SubPlugin);
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.AQUA.toString() + SubPlugin.lang.getString("Lang.GUI.Create-Server"))) {
                    if (player.hasPermission("SubServer.Command.create")) {
                        new SubGUI(SubPlugin).openMojangAgreement(player);
                    } else {
                        player.sendMessage("Fail!");
                    }
                } else if (!event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GRAY + SubPlugin.Plugin.getDescription().getName() + " v" + SubPlugin.Plugin.getDescription().getVersion())) {
                    new SubGUI(player, 0, event.getCurrentItem().getItemMeta().getDisplayName().replace(ChatColor.YELLOW.toString(), ""), SubPlugin);
                }
            }
            event.setCancelled(true);
        }

        /**
         * Server Editor Listener
         */
        if (event.getInventory().getName().contains(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title"))) {
            final Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().hasItemMeta()) {
                String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
                if ((ChatColor.DARK_GREEN.toString() + SubPlugin.lang.getString("Lang.GUI.Start")).equals(displayName)) {
                    if (player.hasPermission("SubServer.Command.start.*") || player.hasPermission("SubServer.Command.start." + event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""))) {
                        SubAPI.getSubServer(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "")).start((Player)event.getWhoClicked());
                        new SubGUI(SubPlugin).openLoader(player, event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""), "openServerWindow");
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                SubGUI.stopLoader = true;
                            }
                        }.runTaskAsynchronously(SubPlugin.Plugin);
                    }
                } else if ((ChatColor.RED.toString() + SubPlugin.lang.getString("Lang.GUI.Stop")).equals(displayName)) {
                    if (player.hasPermission("SubServer.Command.stop.*") || player.hasPermission("SubServer.Command.stop." + event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""))) {
                        final boolean stopped = SubAPI.getSubServer(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "")).stop((Player)event.getWhoClicked());
                        new SubGUI(SubPlugin).openLoader(player, event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""), "openServerWindow");
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    if (stopped) SubPlugin.Servers.get(SubPlugin.PIDs.get(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""))).waitFor();
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                SubGUI.stopLoader = true;
                            }
                        }.runTaskAsynchronously(SubPlugin.Plugin);
                    }
                } else if ((ChatColor.DARK_RED.toString() + SubPlugin.lang.getString("Lang.GUI.Terminate")).equals(displayName)) {
                    if (player.hasPermission("SubServer.Command.kill.*") || player.hasPermission("SubServer.Command.kill." + event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""))) {
                        SubAPI.getSubServer(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "")).terminate((Player)event.getWhoClicked());
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                new SubGUI(player, 0, event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""), SubPlugin);
                            }
                        }.runTaskAsynchronously(SubPlugin.Plugin);
                    }
                } else if ((ChatColor.YELLOW.toString() + SubPlugin.lang.getString("Lang.GUI.Back")).equals(displayName)) {
                    int i = (int) Math.floor(SubPlugin.SubServers.indexOf(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "")) / 18);
                    new SubGUI(player, i, null, SubPlugin);
                } else if ((ChatColor.DARK_RED.toString() + SubPlugin.lang.getString("Lang.GUI.Exit")).equals(displayName)) {
                    player.closeInventory();
                } else if ((ChatColor.AQUA.toString() + SubPlugin.lang.getString("Lang.GUI.Send-CMD")).equals(displayName)) {
                    if (player.hasPermission("SubServer.Command.send.*") || player.hasPermission("SubServer.Command.send." + event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""))) {
                        player.closeInventory();
                        final SubGUIChat chat = new SubGUIChat(player, SubPlugin);
                        chat.chatEnabled = false;
                        player.sendMessage(ChatColor.AQUA + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.GUI.Enter-CMD"));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    do {
                                        Thread.sleep(25);
                                    } while (chat.chatEnabled == false);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (!chat.overridden) {
                                    SubAPI.getSubServer(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "")).sendCommand((Player) event.getWhoClicked(), ((chat.chatText.startsWith("/")) ? chat.chatText.substring(1) : chat.chatText));
                                    chat.chatText = "";
                                    new SubGUI(SubPlugin).openSentCommand(player, event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""));
                                }
                            }
                        }.runTaskAsynchronously(SubPlugin.Plugin);
                    }
                } else if ((ChatColor.DARK_GREEN.toString() + SubPlugin.lang.getString("Lang.GUI.Online")).equals(displayName)) {
                    String server = event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "");
                    if ((player.hasPermission("SubServer.Command.teleport." + server) || player.hasPermission("SubServer.Command.teleport.*")) && !server.equalsIgnoreCase("~Proxy")) {
                        player.closeInventory();
                        SubAPI.getSubServer(server).sendPlayer(player);
                    }
                }
            }
            event.setCancelled(true);
        }

        /**
         * Other Listeners
         */
        if (event.getInventory().getName().contains(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Success"))) {
            final Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().hasItemMeta() && event.getClickedInventory().contains(event.getCurrentItem())) {
                new SubGUI(player, 0, event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.getString("Lang.GUI.Success") + ChatColor.YELLOW, ""), SubPlugin);
            }
            event.setCancelled(true);
        }
        if (event.getInventory().getName().contains(SubPlugin.lang.getString("Lang.GUI.Loading"))) {
            event.setCancelled(true);
        }

        /**
         * CreateServer Listeners
         */

        if (event.getInventory().getName().contains(SubPlugin.lang.getString("Lang.Create-Server.Mojang-Agreement"))) {
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().hasItemMeta()) {
                String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
                if (displayName.equals(ChatColor.GREEN + SubPlugin.lang.getString("Lang.Create-Server.Mojang-Agreement-Accept"))) {
                    new SubGUI(SubPlugin).openServerTypeSelector(player);

                } else if (displayName.equals(ChatColor.AQUA + SubPlugin.lang.getString("Lang.Create-Server.Mojang-Agreement-Link"))) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.AQUA + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Mojang-Agreement-Link-Message"));
                    player.sendMessage(ChatColor.AQUA + "https://account.mojang.com/documents/minecraft_eula");

                } else if (displayName.equals(ChatColor.RED + SubPlugin.lang.getString("Lang.Create-Server.Mojang-Agreement-Decline"))) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Mojang-Agreement-Decline-Message"));
                }
            }
            event.setCancelled(true);
        }

        if (event.getInventory().getName().contains(SubPlugin.lang.getString("Lang.Create-Server.Server-Type"))) {
            final Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().hasItemMeta() && event.getClickedInventory().contains(event.getCurrentItem())) {
                final ServerType Type = ServerType.valueOf(event.getCurrentItem().getItemMeta().getDisplayName().replace(ChatColor.GRAY.toString(), "").toLowerCase());
                player.closeInventory();

                final SubGUIChat chat = new SubGUIChat(player, SubPlugin);

                chat.chatEnabled = false;
                player.sendMessage(ChatColor.YELLOW + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Server-Version"));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            do {
                                Thread.sleep(25);
                            } while (chat.chatEnabled == false);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (!chat.overridden) {
                            Version Version = new Version(chat.chatText);
                            if (Version.compareTo(new Version("1.8")) < 0) {
                                player.sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Server-Version-Unsupported"));
                            } else {
                                player.sendMessage(ChatColor.YELLOW + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Server-Name"));
                                chat.chatEnabled = false;
                                try {
                                    do {
                                        Thread.sleep(25);
                                    } while (chat.chatEnabled == false);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (!chat.overridden) {
                                    String Name = chat.chatText;
                                    if (!StringUtils.isAlphanumericSpace(Name)) {
                                        player.sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Server-Name-Alphanumeric"));
                                    } else {
                                        player.sendMessage(ChatColor.YELLOW + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Server-Memory"));
                                        chat.chatEnabled = false;
                                        try {
                                            do {
                                                Thread.sleep(25);
                                            } while (chat.chatEnabled == false);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        if (!chat.overridden) {
                                            int Memory = 0;
                                            try {
                                                Memory = Integer.parseInt(chat.chatText);
                                            } catch (NumberFormatException e) {
                                                player.sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Server-Memory-Invalid"));
                                            }
                                            if (Memory == 0) {
                                                player.sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Server-Memory-Invalid"));
                                            } else {
                                                chat.chatEnabled = false;
                                                player.sendMessage(ChatColor.YELLOW + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Server-Port"));
                                                try {
                                                    do {
                                                        Thread.sleep(25);
                                                    } while (chat.chatEnabled == false);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }

                                                if (!chat.overridden) {
                                                    int Port = 0;
                                                    try {
                                                        Port = Integer.parseInt(chat.chatText);
                                                    } catch (NumberFormatException e) {
                                                        player.sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Server-Port-Invalid"));
                                                    }
                                                    if (Port == 0 || Port > 65535) {
                                                        player.sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Create-Server.Server-Port-Invalid"));
                                                    } else {
                                                        SubPlugin.ServerCreator = new SubCreator(Name, Port, new File("./" + Name), Type, Version, Memory, player, SubPlugin);
                                                        SubPlugin.ServerCreator.run();
                                                        new SubGUI(player, 0, null, SubPlugin);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.runTaskAsynchronously(SubPlugin.Plugin);

            }
            event.setCancelled(true);
        }

        /**
         * Seecret Listener
         */
        if (event.getInventory().getName().contains(":S:")) {
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getClickedInventory().contains(event.getCurrentItem())) {
                new SubGUI(SubPlugin).openSeecretWindow((Player) event.getWhoClicked());
                event.setCancelled(true);
            }
        }
    }
}