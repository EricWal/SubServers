package net.ME1312.SubServer;

import net.ME1312.SubServer.Libraries.SubServerInfo;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener Class
 */
public class PlayerListener implements Listener {
    FakeProxyServer FakeProxyServer;

    public PlayerListener(FakeProxyServer FakeProxyServer) {
        this.FakeProxyServer = FakeProxyServer;
    }

    /**
     * Kick Listener
     * @param event
     */
    @EventHandler
    public void onServerKick(ServerKickEvent event) {
        ServerInfo kickedFrom = null;
        if (event.getPlayer().getServer() != null) {
            kickedFrom = event.getPlayer().getServer().getInfo();
        } else if (FakeProxyServer.getReconnectHandler() != null) {
            kickedFrom = FakeProxyServer.getReconnectHandler().getServer(event.getPlayer());
        } else {
            kickedFrom = AbstractReconnectHandler.getForcedHost(event.getPlayer().getPendingConnection());
            if (kickedFrom == null) {
                kickedFrom = FakeProxyServer.getServerInfo(event.getPlayer().getPendingConnection().getListener().getDefaultServer());
            }
        }

        String server = FakeProxyServer.getConfig().getListeners().toArray(new ListenerInfo[FakeProxyServer.getConfig().getListeners().size()])[0].getFallbackServer();

        if (FakeProxyServer.getServers().keySet().contains(server)) {
            ServerInfo kickTo = FakeProxyServer.getServers().get(server);
            if (kickedFrom != null && !kickedFrom.equals(kickTo)) {
                event.getPlayer().setReconnectServer(kickTo);
                event.setCancelled(true);
                event.getPlayer().sendMessage(new TextComponent(ChatColor.GRAY.toString() + ChatColor.BOLD.toString() + "Connection Lost: " + ChatColor.RESET.toString() + event.getKickReasonComponent()[0].toLegacyText()));
            }
        }
    }

    /**
     * Shared Chat Listener
     * @param event
     */
    @EventHandler
    public void onMessageSend(ChatEvent event) {
        if (!event.isCancelled() && (event.getSender() instanceof ProxiedPlayer) && !event.getMessage().startsWith("/") && ((SubServerInfo) FakeProxyServer.getServerInfo(((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())).isSharedChat()) {
            ProxiedPlayer player = (ProxiedPlayer) event.getSender();

            for(Iterator<ServerInfo> servers = FakeProxyServer.getServers().values().iterator(); servers.hasNext(); ) {
                SubServerInfo server = (SubServerInfo)servers.next();
                if (server.isSharedChat()) {
                    if (player.getServer().getInfo().getAddress() != server.getAddress()) {
                        for (Iterator<ProxiedPlayer> players = server.getPlayers().iterator(); players.hasNext(); ) {
                            String message = FakeProxyServer.lang.get("Lang.Proxy.Chat-Format").replace("$displayname$", player.getDisplayName()).replace("$message$", ((player.hasPermission("subserver.chat.color"))?ChatColor.translateAlternateColorCodes('&', event.getMessage()):event.getMessage())).replace("$server$", player.getServer().getInfo().getName());
                            TextComponent text = new TextComponent("");

                            if (player.hasPermission("subserver.chat.url")) {
                                Matcher regex = Pattern.compile("(.*?)((http|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?)").matcher(message);
                                String end = "";
                                while (regex.find()) {
                                    text.addExtra(regex.group(1));
                                    end = end + regex.group(1);
                                    TextComponent url = new TextComponent(regex.group(2));
                                    url.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, regex.group(2)));
                                    text.addExtra(url);
                                    end = end + regex.group(2);
                                }
                                text.addExtra(message.replace(end, ""));
                            } else {
                                text.addExtra(new TextComponent(message));
                            }

                            players.next().sendMessage(text);
                        }
                    }
                }
            }
        }
    }
}
