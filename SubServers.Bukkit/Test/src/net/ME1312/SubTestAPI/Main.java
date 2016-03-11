package net.ME1312.SubTestAPI;

import net.ME1312.SubServer.Events.*;
import net.ME1312.SubServer.Events.Libraries.SubEventHandler;
import net.ME1312.SubServer.SubAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        SubAPI.registerListener(this, this);
    }

    @SubEventHandler
    public void subEventTest(SubStartEvent e) {
        Bukkit.getLogger().info(e.getServer().Name + "Started!");
    }
    @SubEventHandler
     public void subEventTest(SubStopEvent e) {
        Bukkit.getLogger().info(e.getServer().Name + "Stopped!");
    }
    @SubEventHandler
    public void subEventTest(SubRunCommandEvent e) {
        Bukkit.getLogger().info(e.getServer().Name + "Commanded to " + e.getCommand() + "!");
    }
    @SubEventHandler
    public void subEventTest(SubShellExitEvent e) {
        Bukkit.getLogger().info(e.getServer().Name + "Exited!");
    }
    @SubEventHandler
    public void subEventTest(SubCreateEvent e) {
        Bukkit.getLogger().info(e.getType() + "Started!");
    }
}
