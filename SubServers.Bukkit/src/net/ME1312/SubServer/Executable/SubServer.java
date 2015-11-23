package net.ME1312.SubServer.Executable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.ME1312.SubServer.Libraries.ServerPing;
import net.ME1312.SubServer.Libraries.Version.Version;
import net.ME1312.SubServer.SubAPI;
import net.ME1312.SubServer.Main;
import net.ME1312.SubServer.Libraries.Events.SubEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
	
	protected Main Main;
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
	public SubServer(final Boolean Enabled, final String Name, final int PID, final int Port, final boolean Log, final boolean SharedChat, final File Dir, final Executable Exec, final boolean AutoRestart, final boolean Temporary, final Main Main) {
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
		this.Main = Main;

        if (Main.MCVersion.compareTo(new Version("1.8")) >= 0) {
            tasks.add(new BukkitRunnable() {
                @Override
                public void run() {
                    if (isRunning() && PID != 0)
                        try {
                            query = new ServerPing(new InetSocketAddress(Main.config.getString("Settings.Server-IP"), Port)).fetchData();
                        } catch (NullPointerException | IOException e) {
                            query = null;
                        }
                }
            }.runTaskTimerAsynchronously(Main.Plugin, 20 * 30, 20 * 30));
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
							Bukkit.getLogger().info(Main.lprefix + Main.lang.getString("Lang.Debug.Server-Logging-Start").replace("$Server$", "The Proxy").replace("$Shell$", Exec.toString()));
							final StreamGobbler read = new StreamGobbler(Process.getInputStream(), "OUTPUT", Log, null, Name, Main);
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
							}.runTaskAsynchronously(Main.Plugin);
							new BukkitRunnable() {
								@Override
								public void run() {
									try {
										sendCommandSilently("subconf@proxy lang Lang.Commands.Teleport " + Main.lang.getString("Lang.Commands.Teleport").replace(" ", "%20"));
										Thread.sleep(500);
										sendCommandSilently("subconf@proxy lang Lang.Commands.Teleport-Server-List " + Main.lang.getString("Lang.Commands.Teleport-Server-List").replace(" ", "%20"));
										Thread.sleep(500);
										sendCommandSilently("subconf@proxy lang Lang.Commands.Teleport-Player-Error " + Main.lang.getString("Lang.Commands.Teleport-Player-Error").replace(" ", "%20"));
										Thread.sleep(500);
										sendCommandSilently("subconf@proxy lang Lang.Commands.Teleport-Config-Error " + Main.lang.getString("Lang.Commands.Teleport-Config-Error").replace(" ", "%20"));
										Thread.sleep(500);
										sendCommandSilently("subconf@proxy lang Lang.Commands.Teleport-Permission-Error " + Main.lang.getString("Lang.Commands.Teleport-Permission-Error").replace(" ", "%20"));
										Thread.sleep(500);
										sendCommandSilently("subconf@proxy lang Lang.Commands.Teleport-Offline-Error " + Main.lang.getString("Lang.Commands.Teleport-Offline-Error").replace(" ", "%20"));
										Thread.sleep(500);
										sendCommandSilently("subconf@proxy lang Lang.Commands.Teleport-Console-Error " + Main.lang.getString("Lang.Commands.Teleport-Console-Error").replace(" ", "%20"));
										Thread.sleep(500);
										
										sendCommandSilently("subconf@proxy lang Lang.Proxy.Register-Server " + Main.lang.getString("Lang.Proxy.Register-Server").replace(" ", "%20"));
										Thread.sleep(500);
										sendCommandSilently("subconf@proxy lang Lang.Proxy.Remove-Server " + Main.lang.getString("Lang.Proxy.Remove-Server").replace(" ", "%20"));
										Thread.sleep(500);
										sendCommandSilently("subconf@proxy lang Lang.Proxy.Reset-Storage " + Main.lang.getString("Lang.Proxy.Reset-Storage").replace(" ", "%20"));
										Thread.sleep(500);
                                        sendCommandSilently("subconf@proxy lang Lang.Proxy.Chat-Format " + Main.lang.getString("Lang.Proxy.Chat-Format").replace(" ", "%20"));
                                        Thread.sleep(500);
										sendCommandSilently("subconf@proxy lang Lang.Proxy.Teleport " + Main.lang.getString("Lang.Proxy.Teleport").replace(" ", "%20"));
										Thread.sleep(500);
										
										sendCommandSilently("subconf@proxy addserver ~Lobby " + Main.config.getString("Settings.Server-IP") + " " + Main.config.getString("Settings.Lobby-Port") + " true");
										Thread.sleep(500);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									for(Iterator<String> str = Main.SubServers.iterator(); str.hasNext(); ) {
										String item = str.next();
										sendCommandSilently("subconf@proxy addserver " + item + " " + Main.config.getString("Settings.Server-IP") + " " + SubAPI.getSubServer(item).Port + " " + SubAPI.getSubServer(item).SharedChat);
										try {
											Thread.sleep(500);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
								};
							}.runTaskAsynchronously(Main.Plugin);
							try {
								Process.waitFor();
								SubEvent.RunEvent(Main, SubEvent.Events.SubShellExitEvent, Server);
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
						Bukkit.getLogger().info(Main.lprefix + Main.lang.getString("Lang.Debug.Server-Logging-End").replace("$Server$", "Proxy"));
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
				}.runTaskAsynchronously(Main.Plugin);
			} else if (Enabled) {
				new BukkitRunnable() {
					@Override
					public void run() {
						try {
							/**
							 * Process Creator,
							 * StreamGobbler Starter
							 */
							Process = Runtime.getRuntime().exec(Exec.toString(), null, Dir); //Whatever you want to execute
							Bukkit.getLogger().info(Main.lprefix + Main.lang.getString("Lang.Debug.Server-Logging-Start").replace("$Server$", Name).replace("$Shell$", Exec.toString()));
							final StreamGobbler read = new StreamGobbler(Process.getInputStream(), "OUTPUT", Log, null, Name, Main);
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
							}.runTaskAsynchronously(Main.Plugin);
							try {
								Process.waitFor();
								SubEvent.RunEvent(Main, SubEvent.Events.SubShellExitEvent, Server);
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
						Bukkit.getLogger().info(Main.lprefix + Main.lang.getString("Lang.Debug.Server-Logging-End").replace("$Server$", Name));	
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
					};
				}.runTaskAsynchronously(Main.Plugin);
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
			if (SubEvent.RunEvent(Main, SubEvent.Events.SubStartEvent, this, null)) {
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
	public boolean start(final Player sender) {
		try {
			if (SubEvent.RunEvent(Main, SubEvent.Events.SubStartEvent, this, sender)) {
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
			if (SubEvent.RunEvent(Main, SubEvent.Events.SubRunCommandEvent, this, null, cmd)) {
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
	public boolean sendCommand(Player sender, String cmd) {
		try {
			if (SubEvent.RunEvent(Main, SubEvent.Events.SubRunCommandEvent, this, sender, cmd)) {
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
			if (SubEvent.RunEvent(Main, SubEvent.Events.SubStopEvent, this, null)) {
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
                    }.runTaskAsynchronously(Main.Plugin);
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
	public boolean stop(Player sender) {
		try {
			if (SubEvent.RunEvent(Main, SubEvent.Events.SubStopEvent, this, sender)) {
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
                    }.runTaskAsynchronously(Main.Plugin);
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
			if (SubEvent.RunEvent(Main, SubEvent.Events.SubStopEvent, this, null)) {
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
                    }.runTaskAsynchronously(Main.Plugin);
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
	public boolean terminate(Player sender) {
		try {
			if (SubEvent.RunEvent(Main, SubEvent.Events.SubStopEvent, this, sender)) {
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
                    }.runTaskAsynchronously(Main.Plugin);
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
	public void sendPlayer(Player player) {
		if (SubAPI.getSubServer(0).isRunning()) {
			SubAPI.getSubServer("~Proxy").sendCommandSilently("subconf@proxy sendplayer " + player.getName() + " " + Name);
		}
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
