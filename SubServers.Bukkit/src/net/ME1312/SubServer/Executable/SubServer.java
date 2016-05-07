package net.ME1312.SubServer.Executable;

import java.io.*;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.ME1312.SubServer.Events.Libraries.EventType;
import net.ME1312.SubServer.SubAPI;
import net.ME1312.SubServer.SubPlugin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Subserver Object Class
 * 
 * @author ME1312
 */
@SuppressWarnings("serial")
public class SubServer implements Serializable {
    protected SubPlugin SubPlugin;
    protected Executable Exec;

	private String Name;
    private int PID;
    private boolean Log;
    private boolean SharedChat;
    private boolean Temporary;
    private int Port;
    private File Dir;
    private boolean Enabled;
    private boolean AutoRestart;
    private List<BukkitTask> tasks = new ArrayList<BukkitTask>();
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
                                        SubPlugin.sync(2);
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
								SubAPI.executeEvent(EventType.SubShellExitEvent, Server);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
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
                                        SubPlugin.sync(2);
									} while (read.isAlive() == true);
								};
							}.runTaskAsynchronously(SubPlugin.Plugin);
							try {
								Process.waitFor();
								SubAPI.executeEvent(EventType.SubShellExitEvent, Server);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
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
			if (SubAPI.executeEvent(EventType.SubStartEvent, this, null)) {
				start(true);
				return true;
			} else {
				return false;
			}
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | IllegalStateException e) {
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
			if (SubAPI.executeEvent(EventType.SubStartEvent, this, sender)) {
				start(true);
				return true;
			} else {
				return false;
			}
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
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
			if (SubAPI.executeEvent(EventType.SubRunCommandEvent, this, null, cmd)) {
				StdIn = cmd;
				return true;
			} else {
				return false;
			}
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
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
			if (SubAPI.executeEvent(EventType.SubRunCommandEvent, this, sender, cmd)) {
				StdIn = cmd;
				return true;
			} else {
				return false;
			}
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
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
			if (SubAPI.executeEvent(EventType.SubStopEvent, this, null)) {
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
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
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
			if (SubAPI.executeEvent(EventType.SubStopEvent, this, sender)) {
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
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Terminates the Server
	 */
	public boolean terminate() {
		try {
			if (SubAPI.executeEvent(EventType.SubStopEvent, this, null)) {
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
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
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
			if (SubAPI.executeEvent(EventType.SubStopEvent, this, sender)) {
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
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
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

    /**
     * Gets the name of the SubServer
     *
     * @return SubServer's Name
     */
    public String getName() {
        return Name;
    }

    /**
     * Gets the PID of the SubServer
     *
     * @return SubServer's PID
     */
    public int getPID() {
        return PID;
    }

    /**
     * Gets if the SubServer is Logging to console
     *
     * @return Logging Status
     */
    public boolean isLogging() {
        return Log;
    }

    /**
     * Gets if the SubServer uses Shared Chat
     *
     * @return Shared Chat Status
     */
    public boolean usesSharedChat() {
        return SharedChat;
    }

    /**
     * Gets if this server is Temporary
     *
     * @return Temporary Status
     */
    public boolean isTemporary() {
        return Temporary;
    }

    /**
     * Gets the Port of the SubServer
     *
     * @return The SubServer's Port Number
     */
    public int getPort() {
        return Port;
    }

    /**
     * Gets the Directory of the SubServer
     *
     * @return The SubServer's Directory
     */
    public File getDir() {
        return Dir;
    }

    /**
     * Gets if the SubServer is Enabled
     *
     * @return SubServer's Status
     */
    public boolean isEnabled() {
        return Enabled;
    }

    /**
     * Sets if the SubServer is Enabled
     *
     * @param value Enabled Value
     */
    public void setEnabled(boolean value) {
        Enabled = value;
    }

    /**
     * Gets if the SubServer will restart automatically
     *
     * @return AutoRestart Status
     */
    public boolean willAutoRestart() {
        return AutoRestart;
    }

    /**
     * Sets if the SubServer will restart automatically
     *
     * @param value AutoRestart Status
     */
    public void setAutoRestart(boolean value) {
        AutoRestart = value;
    }
}
