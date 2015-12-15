package net.ME1312.SubServer.Events.Libraries;

import net.ME1312.SubServer.Executable.SubServer;

/**
 * SubEvents class is the main Class for SubServer Events
 * 
 * @author ME1312
 */
public class SubEvent {
	protected SubServer Server;
	private EventType Event;
	
	public SubEvent(EventType Event, SubServer Server) {
		this.Server = Server;
		this.Event = Event;
	}
	
	@Override 
	public String toString() { return Event.toString(); }
	
	/**
	 * Gets the Event Name
	 * @return Event Name
	 */
	public String getEventName() { return Event.toString(); }

    /**
	 * Gets the Server Effected
	 * @return The Server Effected
	 */
	public SubServer getServer() { return Server; }
}
