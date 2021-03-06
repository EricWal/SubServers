package net.ME1312.SubServer.Commands;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.ME1312.SubServer.FakeProxyServer;
import net.ME1312.SubServer.Libraries.SubServerInfo;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class SubDebugCMD extends Command {
    private net.ME1312.SubServer.FakeProxyServer FakeProxyServer;

    public SubDebugCMD(FakeProxyServer FakeProxyServer, String Command){
        super(Command, "SubServers.debug");
        this.FakeProxyServer = FakeProxyServer;
    }

    @Override
    public void execute(CommandSender sender, String[] args) throws ArrayIndexOutOfBoundsException {
        if (args[0].equalsIgnoreCase("addserver")) {
            if (!args[1].contains("!")) {
                FakeProxyServer.ServerInfo.put(args[1],
                        new SubServerInfo((BungeeServerInfo)FakeProxyServer.constructServerInfo(args[1], new InetSocketAddress(args[2], Integer.parseInt(args[3])), ((FakeProxyServer.ConfigServers.keySet().contains(args[1]))?FakeProxyServer.ConfigServers.get(args[1]).getMotd():"SubServer-" + args[1]), false), Boolean.parseBoolean(args[4])));
            } else if (args[1].contains("!")) {
                FakeProxyServer.HiddenServerInfo.put(args[1].replace("!", ""),
                        new SubServerInfo((BungeeServerInfo)FakeProxyServer.constructServerInfo(args[1].replace("!", ""), new InetSocketAddress(args[2], Integer.parseInt(args[3])), ((FakeProxyServer.ConfigServers.keySet().contains(args[1]))?FakeProxyServer.ConfigServers.get(args[1]).getMotd():"PlayerServer-" + args[1].replace("!", "")), false), Boolean.parseBoolean(args[4])));
            }

            FakeProxyServer.SubServers.add(args[1]);
            FakeProxyServer.getLogger().info(FakeProxyServer.lang.get("Lang.Proxy.Register-Server") + args[1]);
        } else if (args[0].equalsIgnoreCase("sendplayer")) {
            if (FakeProxyServer.getPlayer(args[1]) != null) {
                if (args[2].contains("!") && FakeProxyServer.HiddenServerInfo.keySet().contains(args[2].replace("!", ""))) {
                    FakeProxyServer.getPlayer(args[1]).connect(FakeProxyServer.HiddenServerInfo.get(
                            args[2].replace("!", "")));
                } else {
                    FakeProxyServer.getPlayer(args[1]).connect(FakeProxyServer.ServerInfo.get(args[2]));
                }
                FakeProxyServer.getLogger().info(FakeProxyServer.lang.get("Lang.Proxy.Teleport").replace("$Player$", args[1]).replace("$Server$", args[2]));
            }
        } else if (args[0].equalsIgnoreCase("removeserver")) {
            if (!args[1].contains("!")) {
                FakeProxyServer.ServerInfo.get(args[1]).destroy();
                FakeProxyServer.ServerInfo.remove(args[1]);
            } else {
                FakeProxyServer.HiddenServerInfo.get(args[1].replace("!", "")).destroy();
                FakeProxyServer.HiddenServerInfo.remove(args[1].replace("!", ""));
            }
            FakeProxyServer.SubServers.remove(args[1]);
            FakeProxyServer.getLogger().info(FakeProxyServer.lang.get("Lang.Proxy.Remove-Server") + args[1]);
        } else if (args[0].equalsIgnoreCase("resetplugin")) {
            List<String> SubServersStore = new ArrayList<String>();
            SubServersStore.addAll(FakeProxyServer.SubServers);
            for(Iterator<String> str = SubServersStore.iterator(); str.hasNext(); ) {
                String item = str.next();
                FakeProxyServer.SubServers.remove(item);
                if (FakeProxyServer.ServerInfo.keySet().contains(item)) {
                    FakeProxyServer.ServerInfo.get(item).destroy();
                    FakeProxyServer.ServerInfo.remove(item);
                }
                if (FakeProxyServer.HiddenServerInfo.keySet().contains(item)) {
                    FakeProxyServer.HiddenServerInfo.get(item).destroy();
                    FakeProxyServer.HiddenServerInfo.remove(item.replace("!", ""));
                }
            }
            String str = FakeProxyServer.lang.get("Lang.Proxy.Reset-Storage");
            FakeProxyServer.lang.clear();
            FakeProxyServer.getLogger().info(str);
            FakeProxyServer.getLogger().info("Waiting for new configuration...");
        } else if (args[0].equalsIgnoreCase("lang")) {
            try {
                FakeProxyServer.lang.put(args[1], URLDecoder.decode(args[2], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

}