package net.ME1312.SubServer.Executable;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                if (log && !line.startsWith(">") && !line.contains("subconf@")) {
                    String msg = line;
                    // REGEX Formatting
                    String type = "INFO";
                    Matcher matcher = Pattern.compile("^((?:\\s*\\[?[0-9]{2}:[0-9]{2}:[0-9]{2}]?)?\\s*(?:\\[|\\[.*\\/)?(INFO|WARN|WARNING|ERROR|ERR|SEVERE)\\]:?\\s*)").matcher(msg);
                    while (matcher.find()) {
                        type = matcher.group(2);
                    }
                    msg = msg.replaceAll("^((?:\\s*\\[?[0-9]{2}:[0-9]{2}:[0-9]{2}]?)?\\s*(?:\\[|\\[.*\\/)?(INFO|WARN|WARNING|ERROR|ERR|SEVERE)\\]:?\\s*)", "");

                    // Log Level
                    msg = SubPlugin.lang.getString("Lang.Debug.Server-Logging-Prefix").replace("$Server$", "Proxy") + msg;
                    switch (type) {
                        case "INFO":
                            Bukkit.getLogger().info(msg);
                            break;
                        case "WARNING":
                        case "WARN":
                            Bukkit.getLogger().warning(msg);
                            break;
                        case "SEVERE":
                        case "ERROR":
                        case "ERR":
                            Bukkit.getLogger().severe(msg);
                            break;
                    }

                    if (writer != null) {
                        writer.println(line);
                        writer.flush();
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