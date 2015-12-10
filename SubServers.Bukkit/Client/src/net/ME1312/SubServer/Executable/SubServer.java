package net.ME1312.SubServer.Executable;
import net.ME1312.SubServer.SubPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.*;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Subserver Class
 * 
 * @author ME1312
 *
 */
@SuppressWarnings("serial")
public class SubServer implements Serializable {
	public String Name;
	public int PID;
    public boolean SharedChat;
	public boolean Temporary;
	public boolean Enabled;
    public boolean Running;
	
	protected SubPlugin SubPlugin;
	
	/** 
	 * Creates a SubServer
	 * 
	 * @param Enabled If the server is Enabled
	 * @param Name Server Name
	 * @param PID Server PID
     * @param SharedChat Toggle Shared Chat
	 * @param Temporary Toggle Temporary Server Options
	 */
	public SubServer(Boolean Enabled, String Name, final int PID, boolean SharedChat, boolean Temporary, boolean Running, SubPlugin SubPlugin) {
		this.Enabled = Enabled;
		this.Name = Name;
		this.PID = PID;
        this.SharedChat = SharedChat;
		this.Temporary = Temporary;
		this.SubPlugin = SubPlugin;
        this.Running = Running;
	}
	
	/**
	 * Outputs Server Name
	 */
	@Override
	public String toString() {
		return Name;
	}
	
	/**
	 * Starts a Subserver
	 */
	public void start() {
        if (SubPlugin.sql != null) {
            try {
                Statement update = SubPlugin.sql.getConnection().createStatement();
                update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`) VALUES ('"+ PID +"', '1')");
                update.close();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Problem Syncing Database!");
                e.printStackTrace();
            }
        }
	}
	
	/**
	 * Starts a Subserver
	 * 
	 * @param sender Player that sent this command
	 */
	public void start(final OfflinePlayer sender) {
        if (SubPlugin.sql != null) {
            try {
                Statement update = SubPlugin.sql.getConnection().createStatement();
                update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`, `Player`) VALUES ('"+ PID +"', '1', '"+ sender.getUniqueId().toString() +"')");
                update.close();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Problem Syncing Database!");
                e.printStackTrace();
            }
        }
	}
	
	/**
	 * Sends Command to Server
	 * @param cmd The Command to send
	 */
	public void sendCommand(String cmd) {
        if (SubPlugin.sql != null) {
            try {
                Statement update = SubPlugin.sql.getConnection().createStatement();
                update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`, `Args`) VALUES ('"+ PID +"', '4', '"+ cmd +"')");
                update.close();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Problem Syncing Database!");
                e.printStackTrace();
            }
        }
	}
	
	/**
	 * Sends Command to Server
	 * @param sender Player that sent this command
	 * @param cmd The Command to send
	 */
	public void sendCommand(OfflinePlayer sender, String cmd) {
        if (SubPlugin.sql != null) {
            try {
                Statement update = SubPlugin.sql.getConnection().createStatement();
                update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`, `Player`, `Args`) VALUES ('"+ PID +"', '4', '"+ sender.getUniqueId().toString() +"', '"+ cmd +"')");
                update.close();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Problem Syncing Database!");
                e.printStackTrace();
            }
        }
	}
	
	/**
	 * Stops the Server
	 */
	public void stop() {
        if (SubPlugin.sql != null) {
            try {
                Statement update = SubPlugin.sql.getConnection().createStatement();
                update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`) VALUES ('"+ PID +"', '2')");
                update.close();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Problem Syncing Database!");
                e.printStackTrace();
            }
        }
	}
	
	/**
	 * Stops a Server
	 * @param sender Player that sent this command
	 */
	public void stop(OfflinePlayer sender) {
        if (SubPlugin.sql != null) {
            try {
                Statement update = SubPlugin.sql.getConnection().createStatement();
                update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`, `Player`) VALUES ('"+ PID +"', '2', '"+ sender.getUniqueId().toString() +"')");
                update.close();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Problem Syncing Database!");
                e.printStackTrace();
            }
        }
	}
	
	/**
	 * Terminates the Server
	 */
	public void terminate() {
        if (SubPlugin.sql != null) {
            try {
                Statement update = SubPlugin.sql.getConnection().createStatement();
                update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`) VALUES ('"+ PID +"', '3')");
                update.close();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Problem Syncing Database!");
                e.printStackTrace();
            }
        }
	}
	
	/**
	 * Terminates the Server
	 * @param sender Player that sent this command
	 */
	public void terminate(OfflinePlayer sender) {
        if (SubPlugin.sql != null) {
            try {
                Statement update = SubPlugin.sql.getConnection().createStatement();
                update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`) VALUES ('"+ PID +"', '3')");
                update.close();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Problem Syncing Database!");
                e.printStackTrace();
            }
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
        }
    }
	
	/**
	 * Test if a Server is Running
	 *
	 * @return True if Server is Running, False if Offline
	 */
	public boolean isRunning() {
        return Running;
	}

    /**
     * Cleanup method for a SubServer
     *
     * @return success value
     */
    public void destroy() {
        return;
    }
}
