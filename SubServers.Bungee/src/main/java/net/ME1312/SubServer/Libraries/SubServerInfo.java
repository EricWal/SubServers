package net.ME1312.SubServer.Libraries;

import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by ME1312 on 10/20/15.
 */
public class SubServerInfo extends BungeeServerInfo {
    private boolean sharedChat;
    private ServerPing server;
    private Timer ping = new Timer("Server Ping");

    public SubServerInfo(BungeeServerInfo connection, boolean sharedChat) {
        super(connection.getName(), connection.getAddress(), connection.getMotd(), connection.isRestricted());
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
        }, 0L, TimeUnit.SECONDS.toMillis(30));
    }

    public boolean isSharedChat() {
        return sharedChat;
    }

    public void setSharedChat(boolean value) {
        sharedChat = value;
    }

    public boolean isOnline() {
        if (server == null) {
            return false;
        } else {
            return true;
        }
    }

    public ServerPing getServer() {
        return server;
    }

    public void destroy() {
        ping.cancel();
    }

}
