package net.ME1312.SubServer.Commands;

import net.ME1312.SubServer.FakeProxyServer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * SubProxy Command
 */
public class ProxyCMD extends Command {
    private net.ME1312.SubServer.FakeProxyServer FakeProxyServer;

    public ProxyCMD(FakeProxyServer FakeProxyServer, String Command) {
        super(Command);
        this.FakeProxyServer = FakeProxyServer;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender sender, String[] args) throws ArrayIndexOutOfBoundsException {
        if (sender instanceof ProxiedPlayer) {
            sender.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + " " + FakeProxyServer.getPluginInfo().getName() + " v" + FakeProxyServer.getPluginInfo().getVersion() + ChatColor.GREEN + ChatColor.ITALIC + " © ME1312 EPIC 2015");
            sender.sendMessage(" ");
            sender.sendMessage(ChatColor.AQUA + " Project Page:" + ChatColor.ITALIC + " https://www.spigotmc.org/resources/subservers.11264/");
        } else {
            sender.sendMessage(" " + FakeProxyServer.getPluginInfo().getName() + " v" + FakeProxyServer.getPluginInfo().getVersion() + " © ME1312 EPIC 2015");
            sender.sendMessage(" ");
            sender.sendMessage(" Project Page: https://www.spigotmc.org/resources/subservers.11264/");
        }
    }
}
