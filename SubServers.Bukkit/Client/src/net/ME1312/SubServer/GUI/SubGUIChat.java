package net.ME1312.SubServer.GUI;

import net.ME1312.SubServer.SubPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * GUI Chat Listener &amp; Parser
 */
@SuppressWarnings("deprecation")
public class SubGUIChat implements Listener {
    public boolean chatEnabled = true;
    public String chatText = "";
    public boolean overridden;

    private SubPlugin SubPlugin;
    private Player Player;

    protected SubGUIChat(Player Player, SubPlugin SubPlugin) {
        this.SubPlugin = SubPlugin;
        this.Player = Player;
        Bukkit.getPluginManager().registerEvents(this, SubPlugin.Plugin);
    }

    /**
     * Chat Listener
     */
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerChat(org.bukkit.event.player.PlayerChatEvent event) {
        if (!chatEnabled && event.getPlayer() == Player) {
            overridden = false;
            event.setCancelled(true);
            chatEnabled = true;
            if (checkText(event.getMessage())) {
                chatText = event.getMessage();
            } else {
                overridden = true;
            }
        }
    }

    /**
     * Command Listener
     */
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!chatEnabled && event.getPlayer() == Player) {
            overridden = false;
            event.setCancelled(true);
            chatEnabled = true;
            if (checkText(event.getMessage())) {
                chatText = event.getMessage();
            } else {
                overridden = true;
            }
        }
    }

    private boolean checkText(String message) {
        if (message.replace(" ", "").equalsIgnoreCase("/\\/\\\\/\\/<><>BASTART") ||
                message.replace(" ", "").equalsIgnoreCase("UPUPDOWNDOWNRIGHTLEFTRIGHTLEFTBASTART") ||
                message.replace(" ", "").equalsIgnoreCase("^^\\/\\/<><>BASTART")) {
            new SubGUI(SubPlugin).openSeecretWindow(Player);
            return false;
        } else {
            return true;
        }
    }
}
