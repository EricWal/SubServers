package net.ME1312.SubServer;

import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.md_5.bungee.Bootstrap;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.command.ConsoleCommandSender;

public class Launcher {

    public static void main(String[] args) throws Exception {
        System.out.println("");
        System.out.println("*******************************************");
        System.out.println("***  Warning, this build is Unofficial  ***");
        System.out.println("***                                     ***");
        System.out.println("*** Please report all issues to ME1312, ***");
        System.out.println("***  NOT the Spigot Staff!  Thank You!  ***");
        System.out.println("*******************************************");
        System.out.println("");

        Security.setProperty("networkaddress.cache.ttl", "30");
        Security.setProperty("networkaddress.cache.negative.ttl", "10");
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("v");
        parser.accepts("version");
        parser.accepts("noconsole");
        OptionSet options = parser.parse(args);
        if(options.has("version") || options.has("v")) {
            System.out.println(Bootstrap.class.getPackage().getImplementationVersion());
        } else {

            if(BungeeCord.class.getPackage().getSpecificationVersion() != null && System.getProperty("IReallyKnowWhatIAmDoingISwear") == null) {
                Date bungee = (new SimpleDateFormat("yyyyMMdd")).parse(BungeeCord.class.getPackage().getSpecificationVersion());
                Calendar line = Calendar.getInstance();
                line.add(3, -4);
                if(bungee.before(line.getTime())) {
                    System.out.println("*******************************************");
                    System.out.println("***   Warning, this build is outdated   ***");
                    System.out.println("***  Please download a new build from:  ***");
                    System.out.println("***  http://ci.md-5.net/job/BungeeCord  ***");
                    System.out.println("***  Errors May Arise on old versions!  ***");
                    System.out.println("*******************************************");
                    System.out.println("");
                }
            }

            FakeProxyServer bungee = new FakeProxyServer();
            ProxyServer.setInstance(bungee);
            bungee.getLogger().info("Enabled BungeeCord version " + bungee.getVersion());
            bungee.start();

            String line;
            if(!options.has("noconsole")) {
                while(bungee.isRunning && (line = bungee.getConsoleReader().readLine(">")) != null) {
                    if(!bungee.getPluginManager().dispatchCommand(ConsoleCommandSender.getInstance(), line)) {
                        bungee.getConsole().sendMessage(ChatColor.RED + "Command not found");
                    }
                }
            }

        }
    }
}
