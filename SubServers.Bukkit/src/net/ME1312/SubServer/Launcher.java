package net.ME1312.SubServer;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * SubServers Launcher
 *
 * @author ME1312
 */
public class Launcher extends JavaPlugin {
	private SubPlugin SubPlugin;
	
	@Override
	public void onEnable() {
		try {
			SubPlugin = new SubPlugin(this);
			SubPlugin.EnablePlugin();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			setEnabled(false);
		}
	}
	
	@Override
	public void onDisable() {
		if (SubPlugin != null) {
			SubPlugin.DisablePlugin();
		}
	}

}
