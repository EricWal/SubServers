package net.ME1312.SubServer.Libraries;

import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * SubServerInfo Class
 */
public class SubServerInfo extends BungeeServerInfo {
    private boolean sharedChat;
    private ServerPing server;
    private Timer ping = new Timer("Server Ping");

    /**
     * Generate a SubServerInfo Variable
     *
     * @param connection Bungee's Server Information
     * @param sharedChat SubServer Shared Chat Toggle
     */
    public SubServerInfo(ServerInfo connection, boolean sharedChat) {
        super(connection.getName(), connection.getAddress(), connection.getMotd(), ((BungeeServerInfo) connection).isRestricted());
        this.sharedChat = sharedChat;
        ping.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    ping(new Callback<ServerPing>() {
                        @Override
                        public void done(ServerPing serverPing, Throwable throwable) {
                            if (throwable == null) {
                                server = serverPing;
                            } else {
                                server = null;
                            }
                        }
                    });
                } catch (NullPointerException e) {}
            }
        }, 0L, TimeUnit.SECONDS.toMillis(10));
    }

    /**
     * Tests if Shared Chat is Enabled
     *
     * @return Shared Chat value
     */
    public boolean isSharedChat() {
        return sharedChat;
    }

    /**
     * Toggle Shared Chat
     *
     * @param value Toggle Value
     */
    public void setSharedChat(boolean value) {
        sharedChat = value;
    }

    /**
     * Check if the SubServer is Online
     *
     * @return Online value
     */
    public boolean isOnline() {
        if (server == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Gets Ping Info For this server
     *
     * @return Server info (or null if offline)
     */
    public ServerPing getServer() {
        return server;
    }

    /**
     * SubServer Cleanup Method
     */
    public void destroy() {
        ping.cancel();
    }

}
