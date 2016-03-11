package net.ME1312.SubServer.Executable;

import net.ME1312.SubServer.SubPlugin;
import net.ME1312.SubServer.SubAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;

public class SubProxy extends SubServer {
    private SubPlugin SubPlugin;
    /**
     * Creates a Proxy Instance
     *
     * @param Enabled     If the proxy is Enabled
     * @param Log         Toggle Console Log
     * @param Dir         Runtime Directory
     * @param Exec        Executable File
     * @param AutoRestart Restart when Stopped
     * @param SubPlugin
     */
    public SubProxy(Boolean Enabled, boolean Log, File Dir, Executable Exec, boolean AutoRestart, SubPlugin SubPlugin) {
        super(Enabled, "~Proxy", 0, 25565, Log, false, Dir, Exec, AutoRestart, false, SubPlugin);
        this.SubPlugin = SubPlugin;
    }

    public void sendPlayer(SubServer server, OfflinePlayer player) {
        if (SubPlugin.sql != null) {
            try {
                Statement update = SubPlugin.sql.getConnection().createStatement();
                update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`, `Args`) VALUES ('-1', '4', 'go "+ server.Name +" "+ player.getName() +"')");
                update.close();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Problem Syncing Database!");
                e.printStackTrace();
            }
        } else if (isRunning()) sendCommandSilently("go " + server.Name + " " + player.getName());
    }

    public void sendPlayer(String server, OfflinePlayer player) {
        if (SubPlugin.sql != null) {
            try {
                Statement update = SubPlugin.sql.getConnection().createStatement();
                update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`, `Args`) VALUES ('-1', '4', 'go "+ server +" "+ player.getName() +"')");
                update.close();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Problem Syncing Database!");
                e.printStackTrace();
            }
        } else if (isRunning()) sendCommandSilently("go " + server + " " + player.getName());
    }


}
