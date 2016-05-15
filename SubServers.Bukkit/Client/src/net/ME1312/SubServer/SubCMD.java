package net.ME1312.SubServer;

import net.ME1312.SubServer.GUI.SubGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;

public class SubCMD implements CommandExecutor {
	SubPlugin SubPlugin;
	
	public SubCMD(SubPlugin SubPlugin) {
		this.SubPlugin = SubPlugin;
	}
	
	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) throws ArrayIndexOutOfBoundsException {
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("start")) {
				if (args.length == 2) {
					if (!(sender instanceof Player) || (((Player) sender).hasPermission("SubServer.Command.Start." + args[1])) || ((Player) sender).hasPermission("SubServer.Command.Start.*")) {
						if (SubAPI.getSubServer(args[1]).Enabled && !SubAPI.getSubServer(args[1]).isRunning()) {
							if (sender instanceof Player) {
								SubAPI.getSubServer(args[1]).start((Player) sender);
								((Player) sender).sendMessage(ChatColor.AQUA + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Server-Start"));
							} else {
								SubAPI.getSubServer(args[1]).start();
							}
						} else if (!SubPlugin.SubServers.contains(args[1])) {
							if (sender instanceof Player) {
								((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Start-Config-Error"));
							} else {
								Bukkit.getLogger().info(SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Start-Config-Error"));
							}
						} else {
							if (sender instanceof Player) {
								((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Server-Start-Error"));
							} else {
								Bukkit.getLogger().info(SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Server-Start-Error"));
							}
						}
					} else if (sender instanceof Player) {
						((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Start-Permission-Error"));
					}
				} else {
					if (sender instanceof Player) {
						((Player) sender).sendMessage("Usage:");
						((Player) sender).sendMessage("/SubServer Start <Server>");
					} else {
						Bukkit.getLogger().info("Usage:");
						Bukkit.getLogger().info("/SubServer Start <Server>");
					}
				}
			} else if (args[0].equalsIgnoreCase("cmd") || args[0].equalsIgnoreCase("command")) {
				if (args.length >= 3) {
					if (!(sender instanceof Player) || (((Player) sender).hasPermission("SubServer.Command.Send." + args[1])) || ((Player) sender).hasPermission("SubServer.Command.Send.*")) {
						if (args[1].equals("~All")) {
                            int i = 2;
                            String str = args[2];
                            if (args.length != 3) {
                                do {
                                    i++;
                                    str = str + " " + args[i];
                                } while ((i + 1) != args.length);
                            }
                            if (sender instanceof Player) {
                                SubAPI.sendCommandToAll((Player) sender, str);
                                ((Player) sender).sendMessage(ChatColor.AQUA + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Send-Command-Server"));
                            } else {
                                SubAPI.sendCommandToAll(str);
                            }
                        } else if (SubPlugin.SubServers.contains(args[1])) {
							if (SubAPI.getSubServer(args[1]).isRunning()) {
								int i = 2;
								String str = args[2];
								if ((i + 1) != args.length) {
									do {
										i++;
										str = str + " " + args[i];
									} while ((i + 1) != args.length);
								}
								if (sender instanceof Player) {
									SubAPI.getSubServer(args[1]).sendCommand((Player) sender, str);
									((Player) sender).sendMessage(ChatColor.AQUA + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Send-Command-Server"));
								} else {
									SubAPI.getSubServer(args[1]).sendCommand(str);
								}
							} else {
								if (sender instanceof Player) {
									((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Send-Command-Server-Error"));
								} else {
									Bukkit.getLogger().info(SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Send-Command-Server-Error"));
								}
							}
						} else {
							if (sender instanceof Player) {
								((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Send-Command-Config-Error"));
							} else {
								Bukkit.getLogger().info(SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Send-Command-Config-Error"));
							}
						}
					} else if (sender instanceof Player) {
						((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Send-Command-Permission-Error"));
					}
				} else {
					if (sender instanceof Player) {
						((Player) sender).sendMessage("Usage:");
						((Player) sender).sendMessage("/SubServer Cmd <Server> <Command> [Args...]");
					} else {
						Bukkit.getLogger().info("Usage:");
						Bukkit.getLogger().info("/SubServer Cmd <Server> <Command> [Args...]");
					}
				}
			} else if (args[0].equalsIgnoreCase("stop")) {
				if (args.length == 2) {
					if (!(sender instanceof Player) || (((Player) sender).hasPermission("SubServer.Command.Stop." + args[1])) || ((Player) sender).hasPermission("SubServer.Command.Stop.*")) {
						if (SubPlugin.SubServers.contains(args[1])) {
							if (SubAPI.getSubServer(args[1]).isRunning()) {
								if (sender instanceof Player) {
									SubAPI.getSubServer(args[1]).stop((Player) sender);
									((Player) sender).sendMessage(ChatColor.AQUA + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Server-Stop"));
								} else {
									SubAPI.getSubServer(args[1]).stop();
								}
							} else {
								if (sender instanceof Player) {
									((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Server-Stop-Error"));
								} else {
									Bukkit.getLogger().info(SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Server-Stop-Error"));
								}
							}
						} else {
							if (sender instanceof Player) {
								((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Stop-Config-Error"));
							} else {
								Bukkit.getLogger().info(SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Stop-Config-Error"));
							}
						}
					} else if (sender instanceof Player) {
						((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Stop-Permission-Error"));
					}
				} else {
					if (sender instanceof Player) {
						((Player) sender).sendMessage("Usage:");
						((Player) sender).sendMessage("/SubServer Stop <Server>");
					} else {
						Bukkit.getLogger().info("Usage:");
						Bukkit.getLogger().info("/SubServer Stop <Server>");
					}
				}
			} else if (args[0].equalsIgnoreCase("kill")) {
				if (args.length == 2) {
					if (!(sender instanceof Player) || (((Player) sender).hasPermission("SubServer.Command.Kill." + args[1])) || ((Player) sender).hasPermission("SubServer.Command.Kill.*")) {
						if (SubPlugin.SubServers.contains(args[1])) {
							if (SubAPI.getSubServer(args[1]).isRunning()) {
								if (sender instanceof Player) {
									((Player) sender).sendMessage(ChatColor.AQUA + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Server-Kill"));
									SubAPI.getSubServer(args[1]).terminate((Player) sender);
								} else {
									SubAPI.getSubServer(args[1]).terminate();
								}
							} else {
								if (sender instanceof Player) {
									((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Server-Kill-Error"));
								} else {
									Bukkit.getLogger().info(SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Server-Kill-Error"));
								}
							}
						} else {
							if (sender instanceof Player) {
								((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Kill-Config-Error"));
							} else {
								Bukkit.getLogger().info(SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Kill-Config-Error"));
							}
						}
					} else if (sender instanceof Player) {
						((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Kill-Permission-Error"));
					}
				} else {
					if (sender instanceof Player) {
						((Player) sender).sendMessage("Usage:");
						((Player) sender).sendMessage("/SubServer Kill <Server>");
					} else {
						Bukkit.getLogger().info("Usage:");
						Bukkit.getLogger().info("/SubServer Kill <Server>");
					}
				}
			} else if (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("teleport")) {
                if (args.length == 3) {
                    if (!(SubPlugin.SubServers.contains(args[1]) || args[1].equalsIgnoreCase("~Lobby"))) {
                        if (sender instanceof Player) {
                            ((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Teleport-Config-Error"));
                        } else {
                            Bukkit.getLogger().info(String.valueOf(SubPlugin.lprefix) + SubPlugin.lang.get("Lang.Commands.Teleport-Config-Error"));
                        }
                    } else if ((sender instanceof Player) && (!((Player) sender).hasPermission("subserver.command.teleport.others.*")) && (!((Player) sender).hasPermission("subserver.command.teleport.others." + args[1]))) {
                        if (sender instanceof Player) {
                            ((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Teleport-Permission-Error"));
                        } else {
                            Bukkit.getLogger().info(String.valueOf(SubPlugin.lprefix) + SubPlugin.lang.get("Lang.Commands.Teleport-Permission-Error"));
                        }
                    } else if (!args[1].equalsIgnoreCase("~Lobby") && !(SubAPI.getSubServer(args[1]).isRunning())) {
                        if (sender instanceof Player) {
                            ((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Teleport-Offline-Error"));
                        } else {
                            Bukkit.getLogger().info(String.valueOf(SubPlugin.lprefix) + SubPlugin.lang.get("Lang.Commands.Teleport-Offline-Error"));
                        }
                    } else {
                        if (args[1].equalsIgnoreCase("~Lobby")) {
                            if (SubPlugin.sql != null) {
                                try {
                                    Statement update = SubPlugin.sql.getConnection().createStatement();
                                    update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`, `Args`) VALUES ('-1', '4', 'go ~Lobby "+ args[2] +"')");
                                    update.close();
                                } catch (SQLException e) {
                                    Bukkit.getLogger().severe("Problem Syncing Database!");
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            if (SubPlugin.sql != null) {
                                try {
                                    Statement update = SubPlugin.sql.getConnection().createStatement();
                                    update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`, `Args`) VALUES ('-1', '4', 'go "+ args[1] +" "+ args[2] +"')");
                                    update.close();
                                } catch (SQLException e) {
                                    Bukkit.getLogger().severe("Problem Syncing Database!");
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } else if (args.length == 2) {
                    if (!(SubPlugin.SubServers.contains(args[1]) || args[1].equalsIgnoreCase("~Lobby"))) {
                        if (sender instanceof Player) {
                            ((Player)sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Teleport-Config-Error"));
                        } else {
                            Bukkit.getLogger().info(String.valueOf(SubPlugin.lprefix) + SubPlugin.lang.get("Lang.Commands.Teleport-Config-Error"));
                        }
                    } else if ((sender instanceof Player) && (!((Player) sender).hasPermission("subserver.command.teleport." + args[1])) && (!((Player) sender).hasPermission("subserver.command.teleport.*"))) {
                        if (sender instanceof Player) {
                            ((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Teleport-Permission-Error"));
                        } else {
                            Bukkit.getLogger().info(String.valueOf(SubPlugin.lprefix) + SubPlugin.lang.get("Lang.Commands.Teleport-Permission-Error"));
                        }
                    } else if (!args[1].equalsIgnoreCase("~Lobby") && !(SubAPI.getSubServer(args[1]).isRunning())) {
                        if (sender instanceof Player) {
                            ((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Teleport-Offline-Error"));
                        } else {
                            Bukkit.getLogger().info(String.valueOf(SubPlugin.lprefix) + SubPlugin.lang.get("Lang.Commands.Teleport-Offline-Error"));
                        }
                    } else if (!(sender instanceof Player)) {
                        if (sender instanceof Player) {
                            ((Player)sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Teleport-Console-Error"));
                        } else {
                            Bukkit.getLogger().info(String.valueOf(SubPlugin.lprefix) + SubPlugin.lang.get("Lang.Commands.Teleport-Console-Error"));
                        }
                    } else {
                        if (args[1].equalsIgnoreCase("~Lobby")) {
                            if (SubPlugin.sql != null) {
                                try {
                                    Statement update = SubPlugin.sql.getConnection().createStatement();
                                    update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`, `Args`) VALUES ('-1', '4', 'go ~Lobby "+ ((Player) sender).getName() +"')");
                                    update.close();
                                } catch (SQLException e) {
                                    Bukkit.getLogger().severe("Problem Syncing Database!");
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            if (SubPlugin.sql != null) {
                                try {
                                    Statement update = SubPlugin.sql.getConnection().createStatement();
                                    update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`, `Args`) VALUES ('-1', '4', 'go "+ args[1] +" "+ ((Player) sender).getName() +"')");
                                    update.close();
                                } catch (SQLException e) {
                                    Bukkit.getLogger().severe("Problem Syncing Database!");
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } else if (sender instanceof Player) {
                    ((Player)sender).sendMessage("Usage:");
                    ((Player)sender).sendMessage("/SubServer Tp <Server> [Player]");
                } else {
                    Bukkit.getLogger().info("Usage:");
                    Bukkit.getLogger().info("/SubServer Tp <Server> <Player>");
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                if (sender instanceof Player) {
                    boolean Spigot = false;
                    try {
                        if (Class.forName("org.spigotmc.SpigotConfig") != null) {
                            Spigot = true;
                        }
                    } catch (ClassNotFoundException e) {}

                    if (!(Spigot && ((Player) sender).hasPermission("subserver.command"))) {
                        ((Player) sender).sendMessage(new String[]{
                                ChatColor.AQUA + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Teleport-Server-List").split("\\|\\|\\|")[1].replace("$online$", Integer.toString(SubPlugin.SubServers.size())),
                                SubPlugin.SubServers.toString().replace("[", ChatColor.DARK_AQUA.toString()).replace(", ", ", " + ChatColor.DARK_AQUA).replace("]", "")
                        });
                    } else {
                        net.md_5.bungee.api.chat.TextComponent String = new net.md_5.bungee.api.chat.TextComponent("");
                        for (Iterator<String> servers = SubPlugin.SubServers.iterator(); servers.hasNext(); ) {
                            String server = servers.next();

                            net.md_5.bungee.api.chat.TextComponent text = new net.md_5.bungee.api.chat.TextComponent(server);
                            text.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
                            text.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/sub " + server));
                            String.addExtra(text);
                            String.addExtra(net.md_5.bungee.api.ChatColor.DARK_AQUA + ", ");
                        }

                        ((Player) sender).sendMessage(ChatColor.AQUA + SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Teleport-Server-List").split("\\|\\|\\|")[1].replace("$online$", Integer.toString(SubPlugin.SubServers.size())));
                        ((Player) sender).spigot().sendMessage(String);
                    }
                } else {
                    Bukkit.getLogger().info(SubPlugin.lprefix + SubPlugin.lang.get("Lang.Commands.Teleport-Server-List").split("\\|\\|\\|")[1].replace("$online$", Integer.toString(SubPlugin.SubServers.size())));
                    Bukkit.getLogger().info(SubPlugin.SubServers.toString().replace("[", "").replace("]", ""));
                }
			} else if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("ver")) {
				if (sender instanceof Player) {
					((Player) sender).sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + " " + SubPlugin.Plugin.getDescription().getName() + " v" + SubPlugin.Plugin.getDescription().getVersion() + ChatColor.GREEN + ChatColor.ITALIC + " © ME1312 EPIC 2015");
					((Player) sender).sendMessage(" ");
					((Player) sender).sendMessage(ChatColor.AQUA + " Project Page:" + ChatColor.ITALIC + " " + SubPlugin.Plugin.getDescription().getWebsite());
				} else {
					Bukkit.getLogger().info(SubPlugin.Plugin.getDescription().getName() + " v" + SubPlugin.Plugin.getDescription().getVersion() + " © ME1312 EPIC 2015");
					Bukkit.getLogger().info("Project Page: " + SubPlugin.Plugin.getDescription().getWebsite());
				}
			} else if (args[0].equalsIgnoreCase("help")) {
				if (sender instanceof Player) {
                    ((Player) sender).sendMessage(getHelp(true));
				} else {
                    for (Iterator<String> str = Arrays.<String>asList(getHelp(false)).iterator(); str.hasNext(); ) {
                        Bukkit.getLogger().info(str.next());
                    }
				}
			} else if ((sender instanceof Player) && ((Player) sender).hasPermission("SubServer.Command") && SubPlugin.config.getBoolean("Settings.GUI.Enabled")) {
				if (SubPlugin.SubServers.contains(args[0])) {
					new SubGUI((Player) sender, 0, args[0], SubPlugin);
				} else {
					((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + "Invalid Server Name");
				}
			} else {
				if (sender instanceof Player) {
					((Player) sender).sendMessage(ChatColor.RED + SubPlugin.lprefix + "Invalid Command Usage: /Subserver help");
				} else {
					Bukkit.getLogger().info("Invalid Command Usage: /Subserver help");
				}
			}
		} else {
			if ((sender instanceof Player) && ((Player) sender).hasPermission("SubServer.Command") && SubPlugin.config.getBoolean("Settings.GUI.Enabled")) {
                new SubGUI((Player) sender, 0, null, SubPlugin);
			} else {
				if (sender instanceof Player) {
                    ((Player) sender).sendMessage(getHelp(true));
				} else {
					for (Iterator<String> str = Arrays.<String>asList(getHelp(false)).iterator(); str.hasNext(); ) {
                        Bukkit.getLogger().info(str.next());
                    }
				}
			}
		}
		return true;
	}

    private String[] getHelp(boolean isPlayer) {
        return new String[]{
                ((isPlayer) ? ChatColor.AQUA.toString() : "") + "SubServers Command List:",
                ((isPlayer) ? ChatColor.AQUA.toString() : "") + "GUI: /SubServer [Server]",
                ((isPlayer) ? ChatColor.AQUA.toString() : "") + "Help: /SubServer Help",
                ((isPlayer) ? ChatColor.AQUA.toString() : "") + "List Servers: /SubServer List",
                ((isPlayer) ? ChatColor.AQUA.toString() : "") + "Plugin Version: /SubServer Version",
                ((isPlayer) ? ChatColor.AQUA.toString() : "") + "Stop Server: /SubServer Stop <Server>",
                ((isPlayer) ? ChatColor.AQUA.toString() : "") + "Kill Server: /SubServer Kill <Server>",
                ((isPlayer) ? ChatColor.AQUA.toString() : "") + "Start Server: /SubServer Start <Server>",
                ((isPlayer) ? ChatColor.AQUA.toString() : "") + "Send Command: /SubServer Cmd <Server> <Command> " + ((isPlayer) ? ChatColor.ITALIC.toString() : "") + "[Args...]",
                ((isPlayer) ? ChatColor.AQUA + "Teleport: /Go <Server> [Player]" : "Teleport: /Sub TP <Server> <Player>"),
        };
    }
}
