package net.ME1312.SubServer.Commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.ChatColor;
import net.ME1312.SubServer.FakeProxyServer;

import java.util.Iterator;

public class NavCMD extends Command {
	private FakeProxyServer FakeProxyServer;
	
	public NavCMD(FakeProxyServer FakeProxyServer, String Command) {
        super(Command, "bungeecord.command.server");
		this.FakeProxyServer = FakeProxyServer;
    }
	
	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) throws ArrayIndexOutOfBoundsException {
		if (FakeProxyServer.getPlayer(sender.getName()) == null && args.length == 1) {
			FakeProxyServer.getLogger().info(FakeProxyServer.lang.get("Lang.Commands.Teleport-Console-Error"));
		} else if (args.length < 1) {
            int online = 0;
			TextComponent String = new TextComponent("");
			if (FakeProxyServer.ServerInfo.keySet().size() > 0) {
                for (Iterator<String> servers = FakeProxyServer.ServerInfo.keySet().iterator(); servers.hasNext(); ) {
                    String server = servers.next();
                    if (FakeProxyServer.ServerInfo.get(server).isOnline()) {
                        TextComponent text = new TextComponent(server);
                        text.setColor(ChatColor.DARK_AQUA);
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/go " + server));
                        String.addExtra(text);
                        String.addExtra(ChatColor.DARK_AQUA + ", ");
                        online++;
                    }
                }
            }
			if (FakeProxyServer.ConfigServers.keySet().size() > 0) {
                for (Iterator<String> servers = FakeProxyServer.ConfigServers.keySet().iterator(); servers.hasNext(); ) {
                    String server = servers.next();
                    if (!server.equalsIgnoreCase("~Lobby") && FakeProxyServer.ConfigServers.get(server).isOnline()) {
                        TextComponent text = new TextComponent(server);
                        text.setColor(ChatColor.DARK_AQUA);
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/go " + server));
                        String.addExtra(text);
                        String.addExtra(ChatColor.DARK_AQUA + ", ");
                        online++;
                    }
                }
            }
			if (FakeProxyServer.PlayerServerInfo.keySet().size() > 0) {
                int ponline = 0;
                for (Iterator<String> servers = FakeProxyServer.PlayerServerInfo.keySet().iterator(); servers.hasNext(); ) {
                    String server = servers.next();
                    if (FakeProxyServer.PlayerServerInfo.get(server).isOnline()) {
                        ponline++;
                        online++;
                    }
                }
                String.addExtra(FakeProxyServer.lang.get("Lang.Commands.Teleport-Server-List").split("\\|\\|\\|")[2].replace("$int$", Integer.toString(ponline)));
            }

			if (FakeProxyServer.getPlayer(sender.getName()) != null) {
				FakeProxyServer.getPlayer(sender.getName()).sendMessages(ChatColor.AQUA + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport-Server-List").replace("$online$", Integer.toString(online)).split("\\|\\|\\|")[0] +
						FakeProxyServer.getPlayer(sender.getName()).getServer().getInfo().getName(),
						"", ChatColor.AQUA + FakeProxyServer.lang.get("Lang.Commands.Teleport-Server-List").replace("$online$", Integer.toString(online)).split("\\|\\|\\|")[1]);
                FakeProxyServer.getPlayer(sender.getName()).sendMessage(String);
			} else {
				FakeProxyServer.getLogger().info(FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport-Server-List").replace("$online$", Integer.toString(online)).split("\\|\\|\\|")[1]);
				FakeProxyServer.getLogger().info(ChatColor.stripColor(String.toLegacyText()));
			}

		} else if (FakeProxyServer.ConfigServers.keySet().contains(args[0]) && !args[0].equalsIgnoreCase("~Lobby")) {
			if (args.length > 1) {
				if (FakeProxyServer.getPlayer(sender.getName()) == null || FakeProxyServer.getPlayer(sender.getName()).hasPermission("bungeecord.command.send") || FakeProxyServer.getPlayer(sender.getName()).hasPermission("SubServers.Teleport.Others")) {
					if (FakeProxyServer.getPlayer(args[1]) == null) {
						if (FakeProxyServer.getPlayer(sender.getName()) == null) {
							FakeProxyServer.getLogger().info(FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport-Player-Error"));
						} else {
							FakeProxyServer.getPlayer(sender.getName()).sendMessage(ChatColor.RED + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport-Player-Error"));
						}
					} else {
						if (FakeProxyServer.getPlayer(sender.getName()) == null) {
							FakeProxyServer.getLogger().info(FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport"));
						} else {
							FakeProxyServer.getPlayer(sender.getName()).sendMessage(ChatColor.AQUA + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport"));
						}
						FakeProxyServer.getPlayer(args[1]).sendMessage(ChatColor.AQUA + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport"));
						FakeProxyServer.getPlayer(args[1]).connect(FakeProxyServer.ConfigServers.get(args[0]));
					}
				} else {
					FakeProxyServer.getPlayer(sender.getName()).sendMessage(ChatColor.RED + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport-Permission-Error"));
				}
			} else {
				FakeProxyServer.getPlayer(sender.getName()).sendMessage(ChatColor.AQUA + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport"));
				FakeProxyServer.getPlayer(sender.getName()).connect(FakeProxyServer.ConfigServers.get(args[0]));
			}
		} else if (FakeProxyServer.ServerInfo.keySet().contains(args[0])) {
			if (args.length > 1) {
				if (FakeProxyServer.getPlayer(sender.getName()) == null || FakeProxyServer.getPlayer(sender.getName()).hasPermission("bungeecord.command.send") || FakeProxyServer.getPlayer(sender.getName()).hasPermission("SubServers.Teleport.Others")) {
					if (FakeProxyServer.getPlayer(args[1]) == null) {
						if (FakeProxyServer.getPlayer(sender.getName()) == null) {
							FakeProxyServer.getLogger().info(FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport-Player-Error"));
						} else {
							FakeProxyServer.getPlayer(sender.getName()).sendMessage(ChatColor.RED + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport-Player-Error"));
						}
					} else {
						if (FakeProxyServer.getPlayer(sender.getName()) == null) {
							FakeProxyServer.getLogger().info(FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport"));
						} else {
							FakeProxyServer.getPlayer(sender.getName()).sendMessage(ChatColor.AQUA + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport"));
						}
						FakeProxyServer.getPlayer(args[1]).sendMessage(ChatColor.AQUA + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport"));
						FakeProxyServer.getPlayer(args[1]).connect(FakeProxyServer.ServerInfo.get(args[0]));
					}
				} else {
					FakeProxyServer.getPlayer(sender.getName()).sendMessage(ChatColor.RED + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport-Permission-Error"));
				}
			} else {
				FakeProxyServer.getPlayer(sender.getName()).sendMessage(ChatColor.AQUA + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport"));
				FakeProxyServer.getPlayer(sender.getName()).connect(FakeProxyServer.ServerInfo.get(args[0]));
			}
		} else if (args[0].contains("!") && FakeProxyServer.PlayerServerInfo.keySet().contains(args[0].replace("!", ""))) {
			if (args.length > 1) {
				if (FakeProxyServer.getPlayer(sender.getName()) == null || FakeProxyServer.getPlayer(sender.getName()).hasPermission("bungeecord.command.send") || FakeProxyServer.getPlayer(sender.getName()).hasPermission("SubServers.Teleport.Others")) {
					if (FakeProxyServer.getPlayer(args[1]) == null) {
						if (FakeProxyServer.getPlayer(sender.getName()) == null) {
							FakeProxyServer.getLogger().info(FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport-Player-Error"));
						} else {
							FakeProxyServer.getPlayer(sender.getName()).sendMessage(ChatColor.RED + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport-Player-Error"));
						}
					} else {
						if (FakeProxyServer.getPlayer(sender.getName()) == null) {
							FakeProxyServer.getLogger().info(FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport"));
						} else {
							FakeProxyServer.getPlayer(sender.getName()).sendMessage(ChatColor.AQUA + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport"));
						}
						FakeProxyServer.getPlayer(args[1]).sendMessage(ChatColor.AQUA + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport"));
						FakeProxyServer.getPlayer(args[1]).connect(FakeProxyServer.PlayerServerInfo.get(args[0].replace("!", "")));
					}
				} else {
					FakeProxyServer.getPlayer(sender.getName()).sendMessage(ChatColor.RED + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport-Permission-Error"));
				}
			} else {
				FakeProxyServer.getPlayer(sender.getName()).sendMessage(ChatColor.AQUA + FakeProxyServer.lprefix + FakeProxyServer.lang.get("Lang.Commands.Teleport"));
				FakeProxyServer.getPlayer(sender.getName()).connect(FakeProxyServer.PlayerServerInfo.get(args[0].replace("!", "")));
			}
		} else {
			FakeProxyServer.getPlayer(sender.getName()).sendMessage(ChatColor.RED + FakeProxyServer.lang.get("Lang.Commands.Teleport-Config-Error"));
		}
	}
	
}
