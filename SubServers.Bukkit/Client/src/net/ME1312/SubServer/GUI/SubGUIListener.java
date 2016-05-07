package net.ME1312.SubServer.GUI;

import net.ME1312.SubServer.SubAPI;
import net.ME1312.SubServer.SubPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

/**
 * GUI Listener
 *
 * @author ME1312
 *
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
        if (event.getInventory().getName().contains(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-List-Title").replace("$Int$", ""))) {
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().hasItemMeta()) {
                if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.DARK_RED.toString() + SubPlugin.lang.get("Lang.GUI.Exit"))) {
                    player.closeInventory();
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.DARK_GREEN.toString() + SubPlugin.lang.get("Lang.GUI.Back"))) {
                    new SubGUI(player, (Integer.parseInt(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN.toString() + SubPlugin.lang.get("Lang.GUI.Server-List-Title").replace("$Int$", ""), "")) - 2), null, SubPlugin);
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.DARK_GREEN.toString() + SubPlugin.lang.get("Lang.GUI.Next"))) {
                    new SubGUI(player, Integer.parseInt(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN.toString() + SubPlugin.lang.get("Lang.GUI.Server-List-Title").replace("$Int$", ""), "")), null, SubPlugin);
                } else if (!event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GRAY + SubPlugin.Plugin.getDescription().getName() + " v" + SubPlugin.Plugin.getDescription().getVersion())) {
                    new SubGUI(player, 0, event.getCurrentItem().getItemMeta().getDisplayName().replace(ChatColor.YELLOW.toString(), ""), SubPlugin);
                }
            }
            event.setCancelled(true);
        }

        /**
         * Server Editor Listener
         */
        if (event.getInventory().getName().contains(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title"))) {
            final Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().hasItemMeta()) {
                String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
                if ((ChatColor.DARK_GREEN.toString() + SubPlugin.lang.get("Lang.GUI.Start")).equals(displayName)) {
                    if (player.hasPermission("SubServer.Command.start.*") || player.hasPermission("SubServer.Command.start." + event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""))) {
                        SubAPI.getSubServer(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "")).start((Player)event.getWhoClicked());
                        final SubGUI SubGUI;
                        (SubGUI = new SubGUI(SubPlugin)).openLoader(player, event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""), "openServerWindow");
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                SubAPI.getSubServer(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "")).Running = true;
                                SubGUI.stopLoader = true;
                            }
                        }.runTaskAsynchronously(SubPlugin.Plugin);
                    }
                } else if ((ChatColor.RED.toString() + SubPlugin.lang.get("Lang.GUI.Stop")).equals(displayName)) {
                    if (player.hasPermission("SubServer.Command.stop.*") || player.hasPermission("SubServer.Command.stop." + event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""))) {
                        SubAPI.getSubServer(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "")).stop((Player)event.getWhoClicked());
                        final SubGUI SubGUI;
                        (SubGUI = new SubGUI(SubPlugin)).openLoader(player, event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""), "openServerWindow");
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                SubAPI.getSubServer(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "")).Running = false;
                                SubGUI.stopLoader = true;
                            }
                        }.runTaskAsynchronously(SubPlugin.Plugin);
                    }
                } else if ((ChatColor.DARK_RED.toString() + SubPlugin.lang.get("Lang.GUI.Terminate")).equals(displayName)) {
                    if (player.hasPermission("SubServer.Command.kill.*") || player.hasPermission("SubServer.Command.kill." + event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""))) {
                        SubAPI.getSubServer(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "")).terminate((Player)event.getWhoClicked());
                        final SubGUI SubGUI;
                        (SubGUI = new SubGUI(SubPlugin)).openLoader(player, event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""), "openServerWindow");
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                SubGUI.stopLoader = true;
                                SubAPI.getSubServer(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "")).Running = false;
                                new SubGUI(player, 0, event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""), SubPlugin);
                            }
                        }.runTaskAsynchronously(SubPlugin.Plugin);
                    }
                } else if ((ChatColor.YELLOW.toString() + SubPlugin.lang.get("Lang.GUI.Back")).equals(displayName)) {
                    player.closeInventory();
                    int i = (int) Math.floor(SubPlugin.SubServers.indexOf(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "")) / 18);
                    new SubGUI(player, i, null, SubPlugin);
                } else if ((ChatColor.DARK_RED.toString() + SubPlugin.lang.get("Lang.GUI.Exit")).equals(displayName)) {
                    player.closeInventory();
                } else if ((ChatColor.AQUA.toString() + SubPlugin.lang.get("Lang.GUI.Send-CMD")).equals(displayName)) {
                    if (player.hasPermission("SubServer.Command.send.*") || player.hasPermission("SubServer.Command.send." + event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""))) {
                        player.closeInventory();
                        final SubGUIChat chat = new SubGUIChat(player, SubPlugin);
                        chat.chatEnabled = false;
                        player.sendMessage(ChatColor.AQUA + SubPlugin.lprefix + SubPlugin.lang.get("Lang.GUI.Enter-CMD"));
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

                                SubAPI.getSubServer(event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "")).sendCommand((Player)event.getWhoClicked(), chat.chatText);
                                chat.chatText = "";
                                new SubGUI(SubPlugin).openSentCommand(player, event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, ""));
                            }
                        }.runTaskAsynchronously(SubPlugin.Plugin);
                    }
                } else if ((ChatColor.DARK_GREEN.toString() + SubPlugin.lang.get("Lang.GUI.Online")).equals(displayName)) {
                    String server = event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW, "");
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
        if (event.getInventory().getName().contains(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Success"))) {
            final Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().hasItemMeta()) {
                player.closeInventory();
                new SubGUI(player, 0, event.getClickedInventory().getName().replace(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Success") + ChatColor.YELLOW, ""), SubPlugin);
            }
            event.setCancelled(true);
        }
        if (event.getInventory().getName().equals(SubPlugin.lang.get("Lang.GUI.Loading"))) {
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