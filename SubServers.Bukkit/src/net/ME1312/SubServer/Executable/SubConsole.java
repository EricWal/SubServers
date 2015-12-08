package net.ME1312.SubServer.Executable;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import net.ME1312.SubServer.SubPlugin;
import org.bukkit.Bukkit;

public class SubConsole extends Thread {
    InputStream is;
    String type;
    boolean log;
    String id;
    SubPlugin SubPlugin;
    PrintWriter writer = null;

    SubConsole(InputStream is, String type, boolean log, File file, String id, SubPlugin SubPlugin) {
        this.is = is;
        this.type = type;
        this.log = log;
        this.id = id;
        this.SubPlugin = SubPlugin;
        if (file != null)
            try {
                this.writer = new PrintWriter(file, "UTF-8");
                this.writer.println("---------- LOG START: " + id + " ----------");
                this.writer.flush();
            } catch (UnsupportedEncodingException | FileNotFoundException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (id.equalsIgnoreCase("~Proxy")) {
                    if (log && !line.startsWith(">") && !line.contains("subconf@")) {
                        Bukkit.getLogger().info(SubPlugin.lang.getString("Lang.Debug.Server-Logging-Prefix").replace("$Server$", "Proxy") +
                                line.replace(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + " ", ""));
                    }
                } else {
                    if (log && !line.startsWith(">") && !line.contains("subconf@")) {
                        Bukkit.getLogger().info(SubPlugin.lang.getString("Lang.Debug.Server-Logging-Prefix").replace("$Server$", id) + line
                                .replace("[" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + " ", "[")
                                .replace("[" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + "] [Server thread/", "[")
                                .replace("[" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + "] ", ""));
                    }
                }
                if (writer != null) {
                    writer.println(line);
                    writer.flush();
                }
            }
        } catch (IOException ioe) {} finally {
            if (writer != null) {
                writer.println("---------- END LOG ----------");
                writer.close();
            }
        }
    }
}