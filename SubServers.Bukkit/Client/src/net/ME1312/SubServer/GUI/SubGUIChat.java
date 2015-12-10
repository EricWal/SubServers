package net.ME1312.SubServer.GUI;

import net.ME1312.SubServer.SubPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by ME1312 on 10/15/15.
 */
@SuppressWarnings("deprecation")
public class SubGUIChat implements Listener {
    public boolean chatEnabled = true;
    public String chatText = "";

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
    public void onPlayerCommand(org.bukkit.event.player.PlayerChatEvent event) {
        if (chatEnabled == false && event.getPlayer() == Player) {
            chatText = event.getMessage();
            chatEnabled = true;
            event.setCancelled(true);
        }
    }
}
