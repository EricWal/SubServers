package net.ME1312.SubServer.Executable;

import net.ME1312.SubServer.SubPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;

public class SubProxy extends SubServer {
    private net.ME1312.SubServer.SubPlugin SubPlugin;
    /**
     * Creates a Proxy Instance
     *
     * @param Enabled     If the proxy is Enabled
     * @param SubPlugin
     */
    public SubProxy(Boolean Enabled, SubPlugin SubPlugin) {
        super(Enabled, "~Proxy", 0, false, false, false, SubPlugin);
        this.SubPlugin = SubPlugin;
    }

    public void sendPlayer(SubServer server, OfflinePlayer player) {
        try {
            Statement update = SubPlugin.sql.getConnection().createStatement();
            update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`, `Args`) VALUES ('-1', '4', 'go " + server.Name + " " + player.getName() + "')");
            update.close();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Problem Syncing Database!");
            e.printStackTrace();
        }
    }

    public void sendPlayer(String server, OfflinePlayer player) {
        try {
            Statement update = SubPlugin.sql.getConnection().createStatement();
            update.executeUpdate("INSERT INTO `SubQueue` (`PID`, `Type`, `Args`) VALUES ('-1', '4', 'go " + server + " " + player.getName() + "')");
            update.close();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Problem Syncing Database!");
            e.printStackTrace();
        }
    }
}
