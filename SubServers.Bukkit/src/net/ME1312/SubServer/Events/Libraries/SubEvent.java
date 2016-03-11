package net.ME1312.SubServer.Events.Libraries;

import net.ME1312.SubServer.Executable.SubServer;
import net.ME1312.SubServer.SubPlugin;

/**
 * SubServer Events
 * 
 * @author ME1312
 */
public class SubEvent extends Event {
	private SubServer Server;
	
	public SubEvent(SubPlugin SubPlugin, EventType Event, SubServer Server) {
        super(SubPlugin, Event);
		this.Server = Server;
	}

    /**
	 * Gets the Server Effected
	 * @return The Server Effected
	 */
	public SubServer getServer() { return Server; }

}
