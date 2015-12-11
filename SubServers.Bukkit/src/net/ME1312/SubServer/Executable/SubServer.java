package net.ME1312.SubServer.Executable;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.ME1312.SubServer.Libraries.ServerPing;
import net.ME1312.SubServer.Libraries.Version.Version;
import net.ME1312.SubServer.SubAPI;
import net.ME1312.SubServer.SubPlugin;
import net.ME1312.SubServer.Libraries.Events.SubEvent;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Subserver Creator Class
 * 
 * @author ME1312
 *
 */
@SuppressWarnings("serial")
public class SubServer implements Serializable {
	public String Name;
	public int PID;
	public boolean Log;
    public boolean SharedChat;
	public boolean Temporary;
	public boolean Enabled;
	public int Port;
    public boolean AutoRestart;
	
	protected SubPlugin SubPlugin;
	protected File Dir;
	protected Executable Exec;

    private List<BukkitTask> tasks = new ArrayList<BukkitTask>();
    private ServerPing.StatusResponse query;
	private Process Process;
	private String StdIn;
	private SubServer Server = this;
	
	/** 
	 * Creates a SubServer
	 * 
	 * @param Enabled If the server is Enabled
	 * @param Name Server Name
	 * @param PID Server PID
	 * @param Port Server Port
	 * @param Log Toggle Console Log
     * @param SharedChat Toggle Shared Chat
	 * @param Dir Runtime Directory
	 * @param Exec Executable File
	 * @param AutoRestart Restart when Stopped
	 * @param Temporary Toggle Temporary Server Options
	 */
	public SubServer(final Boolean Enabled, final String Name, final int PID, final int Port, final boolean Log, final boolean SharedChat, final File Dir, final Executable Exec, final boolean AutoRestart, final boolean Temporary, final SubPlugin SubPlugin) {
		this.Enabled = Enabled;
		this.Name = Name;
		this.PID = PID;
		this.Port = Port;
		this.Log = Log;
        this.SharedChat = SharedChat;
		this.Temporary = Temporary;
		this.Dir = Dir;
		this.Exec = Exec;
		this.AutoRestart = AutoRestart;
		this.SubPlugin = SubPlugin;

        if (SubPlugin.MCVersion.compareTo(new Version("1.8")) >= 0) {
            tasks.add(new BukkitRunnable() {
                @Override
                public void run() {
                    if (isRunning() && PID != 0)
                        try {
                            query = new ServerPing(new InetSocketAddress(SubPlugin.config.getString("Settings.Server-IP"), Port)).fetchData();
                        } catch (NullPointerException | IOException e) {
                            query = null;
                        }
                }
            }.runTaskTimerAsynchronously(SubPlugin.Plugin, 20 * 10, 20 * 10));
        }
        if (SubPlugin.sql != null) {
            tasks.add(new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        Statement update = SubPlugin.sql.getConnection().createStatement();
                        ResultSet results = update.executeQuery("SELECT * FROM `SubQueue` WHERE PID='" + PID + "'");
                        while (results.next()) {
                            boolean sent = false;
                            if (results.getInt("Type") == 1) {
                                if (!isRunning()) {
                                    if (results.getString("Player") != null) {
                                        start(Bukkit.getOfflinePlayer(UUID.fromString(results.getString("Player"))));
                                    } else {
                                        start();
                                    }
                                }
                                sent = true;
                            } else if (results.getInt("Type") == 2) {
                                if (isRunning()) {
                                    if (results.getString("Player") != null) {
                                        stop(Bukkit.getOfflinePlayer(UUID.fromString(results.getString("Player"))));
                                    } else {
                                        stop();
                                    }
                                }
                                sent = true;
                            } else if (results.getInt("Type") == 3) {
                                if (isRunning()) {
                                    if (results.getString("Player") != null) {
                                        terminate(Bukkit.getOfflinePlayer(UUID.fromString(results.getString("Player"))));
                                    } else {
                                        terminate();
                                    }
                                }
                                sent = true;
                            } else if (results.getInt("Type") == 4) {
                                if (isRunning()) {
                                    if (results.getString("Player") != null) {
                                        sendCommand(Bukkit.getOfflinePlayer(UUID.fromString(results.getString("Player"))), results.getString("Args"));
                                    } else {
                                        sendCommand(results.getString("Args"));
                                    }
                                }
                                sent = true;
                            }
                        }
                        update.executeUpdate("DELETE FROM `SubQueue` WHERE PID='" + PID + "'");
                        results.close();
                        update.close();
                    } catch (SQLException e) {
                        Bukkit.getLogger().severe("Problem Syncing Database!");
                        e.printStackTrace();
                    }
                }

            }.runTaskTimerAsynchronously(SubPlugin.Plugin, 20 * 5, 20 * 5));
        }
	}
	
	private void start(boolean value) {
		if (value) {
			if (Name.equalsIgnoreCase("~Proxy") && Enabled) {
				new BukkitRunnable() {
					@Override
					public void run() {
						try {
							/**
							 * Process Creator,
							 * StreamGobbler Starter
							 */
							Process = Runtime.getRuntime().exec(Exec.toString(), null, Dir); //Whatever you want to execute
							Bukkit.getLogger().info(SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Debug.Server-Logging-Start").replace("$Server$", "The Proxy").replace("$Shell$", Exec.toString()));
							final SubConsole read = new SubConsole(Process.getInputStream(), "OUTPUT", Log, null, Name, SubPlugin);
							read.start();
							final BufferedWriter cmd = new BufferedWriter(new OutputStreamWriter(Process.getOutputStream()));
							new BukkitRunnable() {
								@Override
								public void run() {
									do {
										/**
										 * StdIn Functions
										 */
										if (StdIn != null) {
											  try {
												cmd.write(StdIn);
												cmd.newLine();
												cmd.flush();
											  } catch (IOException e) {
													e.printStackTrace();
											  }
								              StdIn = null;
										}
									} while (read.isAlive() == true);
								};
							}.runTaskAsynchronously(SubPlugin.Plugin);
                            if (SubPlugin.sql == null) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            try {
                                                for (Iterator<String> keys = SubPlugin.lang.getConfigurationSection("Lang").getKeys(false).iterator(); keys.hasNext(); ) {
                                                    String key = keys.next();
                                                    for (Iterator<String> str = SubPlugin.lang.getConfigurationSection("Lang." + key).getKeys(false).iterator(); str.hasNext(); ) {
                                                        String item = str.next();
                                                        SubAPI.getSubServer(0).sendCommandSilently("subconf@proxy lang Lang." + key + "." + item + " " + URLEncoder.encode(SubPlugin.lang.getRawString("Lang." + key + "." + item), "UTF-8"));
                                                        Thread.sleep(100);
                                                    }
                                                }
                                            } catch (UnsupportedEncodingException | InterruptedException e2) {
                                                e2.printStackTrace();
                                            }
                                            for (Iterator<String> str = SubPlugin.SubServers.iterator(); str.hasNext(); ) {
                                                String item = str.next();
                                                sendCommandSilently("subconf@proxy addserver " + item + " " + SubPlugin.config.getString("Settings.Server-IP") + " " + SubAPI.getSubServer(item).Port + " " + SubAPI.getSubServer(item).SharedChat);
                                                Thread.sleep(100);
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    ;
                                }.runTaskAsynchronously(SubPlugin.Plugin);
                            }
							try {
								Process.waitFor();
								SubEvent.RunEvent(SubPlugin, SubEvent.Events.SubShellExitEvent, Server);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
								e.printStackTrace();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						/**
						 * Reset the Server's PID for future use
						 */
						Bukkit.getLogger().info(SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Debug.Server-Logging-End").replace("$Server$", "Proxy"));
						Process = null;
						StdIn = null;
                        if (AutoRestart) {
                            try {
                                Thread.sleep(2500);
                                start(true);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
					}
				}.runTaskAsynchronously(SubPlugin.Plugin);
			} else if (Enabled) {
				new BukkitRunnable() {
					@Override
					public void run() {
                        if (SubPlugin.sql != null) {
                            try {
                                Statement update = SubPlugin.sql.getConnection().createStatement();
                                update.executeUpdate("UPDATE `SubServers` SET Running='1' WHERE PID='" + PID + "'");
                                update.close();
                            } catch (SQLException e) {
                                Bukkit.getLogger().severe("Problem Syncing Database!");
                                e.printStackTrace();
                            }
                        }
						try {
							/**
							 * Process Creator,
							 * StreamGobbler Starter
							 */
							Process = Runtime.getRuntime().exec(Exec.toString(), null, Dir); //Whatever you want to execute
							Bukkit.getLogger().info(SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Debug.Server-Logging-Start").replace("$Server$", Name).replace("$Shell$", Exec.toString()));
							final SubConsole read = new SubConsole(Process.getInputStream(), "OUTPUT", Log, null, Name, SubPlugin);
							read.start();
							final BufferedWriter cmd = new BufferedWriter(new OutputStreamWriter(Process.getOutputStream()));
							new BukkitRunnable() {
								@Override
								public void run() {
									do {
										/**
										 * StdIn Functions
										 */
										if (StdIn != null) {
										  try {
											cmd.write(StdIn);
											cmd.newLine();
							              	cmd.flush();
										  } catch (IOException e) {
												e.printStackTrace();
											}
							              StdIn = null;
										}
									} while (read.isAlive() == true);
								};
							}.runTaskAsynchronously(SubPlugin.Plugin);
							try {
								Process.waitFor();
								SubEvent.RunEvent(SubPlugin, SubEvent.Events.SubShellExitEvent, Server);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
								e.printStackTrace();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						/**
						 * Reset the Server's PID for future use
						 */
						Bukkit.getLogger().info(SubPlugin.lprefix + SubPlugin.lang.getString("Lang.Debug.Server-Logging-End").replace("$Server$", Name));
						Process = null;
						StdIn = null;

                        if (SubPlugin.sql != null) {
                            try {
                                Statement update = SubPlugin.sql.getConnection().createStatement();
                                update.executeUpdate("UPDATE `SubServers` SET Running='0' WHERE PID='" + PID + "'");
                                update.close();
                            } catch (SQLException e) {
                                Bukkit.getLogger().severe("Problem Syncing Database!");
                                e.printStackTrace();
                            }
                        }
                        if (AutoRestart) {
                            try {
                                Thread.sleep(2500);
                                start(true);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
					};
				}.runTaskAsynchronously(SubPlugin.Plugin);
			}
		}
	}
	
	/**
	 * Outputs Server Name
	 */
	@Override
	public String toString() {
		return Name;
	}
	
	/**
	 * Waits for Shell to End
	 * 
	 * @throws InterruptedException
	 */
	public void waitFor() throws InterruptedException {
		Process.waitFor();
	}
	
	/**
	 * Starts a Subserver
	 */
	public boolean start() {
		try {
			if (SubEvent.RunEvent(SubPlugin, SubEvent.Events.SubStartEvent, this, null)) {
				start(true);
				return true;
			} else {
				return false;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | IllegalStateException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Starts a Subserver
	 * 
	 * @param sender Player that sent this command
	 */
	public boolean start(final OfflinePlayer sender) {
		try {
			if (SubEvent.RunEvent(SubPlugin, SubEvent.Events.SubStartEvent, this, sender)) {
				start(true);
				return true;
			} else {
				return false;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Sends Command to Server
	 * @param cmd The Command to send
	 */
	public boolean sendCommand(String cmd) {
		try {
			if (SubEvent.RunEvent(SubPlugin, SubEvent.Events.SubRunCommandEvent, this, null, cmd)) {
				StdIn = cmd;
				return true;
			} else {
				return false;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Sends Command to Server
	 * @param sender Player that sent this command
	 * @param cmd The Command to send
	 */
	public boolean sendCommand(OfflinePlayer sender, String cmd) {
		try {
			if (SubEvent.RunEvent(SubPlugin, SubEvent.Events.SubRunCommandEvent, this, sender, cmd)) {
				StdIn = cmd;
				return true;
			} else {
				return false;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Sends Command to Server without executing the Event
	 * @param cmd The Command to send
	 */
	public void sendCommandSilently(String cmd) {
		StdIn = cmd;
	}
	
	/**
	 * Stops the Server
	 */
	public boolean stop() {
		try {
			if (SubEvent.RunEvent(SubPlugin, SubEvent.Events.SubStopEvent, this, null)) {
				if (Name.equalsIgnoreCase("~Proxy")) {
					StdIn = "end";
				} else {
					StdIn = "stop";
				}

                if (AutoRestart) {
                    AutoRestart = false;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                waitFor();
                                Thread.sleep(2500);
                                AutoRestart = true;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.runTaskAsynchronously(SubPlugin.Plugin);
                }
				return true;
			} else {
				return false;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Stops a Server
	 * @param sender Player that sent this command
	 */
	public boolean stop(OfflinePlayer sender) {
		try {
			if (SubEvent.RunEvent(SubPlugin, SubEvent.Events.SubStopEvent, this, sender)) {
				if (Name.equalsIgnoreCase("~Proxy")) {
					StdIn = "end";
				} else {
					StdIn = "stop";
				}

                if (AutoRestart) {
                    AutoRestart = false;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                waitFor();
                                Thread.sleep(2500);
                                AutoRestart = true;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.runTaskAsynchronously(SubPlugin.Plugin);
                }
				return true;
			} else {
				return false;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Terminates the Server
	 */
	public boolean terminate() {
		try {
			if (SubEvent.RunEvent(SubPlugin, SubEvent.Events.SubStopEvent, this, null)) {
				Process.destroy();

                if (AutoRestart) {
                    AutoRestart = false;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2500);
                                AutoRestart = true;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.runTaskAsynchronously(SubPlugin.Plugin);
                }
				return true;
			} else {
				return false;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Terminates the Server
	 * @param sender Player that sent this command
	 */
	public boolean terminate(OfflinePlayer sender) {
		try {
			if (SubEvent.RunEvent(SubPlugin, SubEvent.Events.SubStopEvent, this, sender)) {
				Process.destroy();

                if (AutoRestart) {
                    AutoRestart = false;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2500);
                                AutoRestart = true;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.runTaskAsynchronously(SubPlugin.Plugin);
                }
				return true;
			} else {
				return false;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Sends player to SubServer if Proxy and SubServer is Online
	 * 
	 * @param player
	 */
	public void sendPlayer(OfflinePlayer player) {
        if (SubPlugin.sql != null) {
            try {
                Statement update = SubPlugin.sql.getConnection().createStatement();
                update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`, `Args`) VALUES ('-1', '4', 'go "+ Name +" "+ player.getName() +"')");
                update.close();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Problem Syncing Database!");
                e.printStackTrace();
            }
        } else if (SubAPI.getSubServer(0).isRunning()) SubAPI.getSubServer(0).sendCommandSilently("go "+ Name +" "+ player.getName());
	}
	
	/**
	 * Test if a Server is Running
	 *
	 * @return True if Server is Running, False if Offline
	 */
	public boolean isRunning() {
		boolean running = false;
		
		if (Process != null) {
			running = true;
		}
		
		return running;
	}

    /**
     * Get the Server's Query
     *
     * @return the query or null if offline.
     */
    public ServerPing.StatusResponse getServer() {
        return query;
    }

    /**
     * Cleanup method for a SubServer
     *
     * @return success value
     */
    public boolean destroy() {
        if (!isRunning()) {
            for(Iterator<BukkitTask> tasks = this.tasks.iterator(); tasks.hasNext(); ) {
                BukkitTask task = tasks.next();
                task.cancel();
            }
            return true;
        } else {
            return false;
        }
    }
}
