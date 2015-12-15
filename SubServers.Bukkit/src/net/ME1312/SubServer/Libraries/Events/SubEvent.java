package net.ME1312.SubServer.Libraries.Events;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import net.ME1312.SubServer.SubPlugin;
import net.ME1312.SubServer.Executable.SubCreator.ServerTypes;
import net.ME1312.SubServer.Executable.SubServer;
import org.bukkit.OfflinePlayer;

/**
 * SubEvents class is the main Class for SubServer Events
 * 
 * @author ME1312
 *
 */
public class SubEvent {
	protected boolean isCancelled = false;
	protected SubServer Server;
	protected SubPlugin SubPlugin;
	private Events Event;
	public static enum Events {
		SubCreateEvent,
		SubStartEvent,
		SubRunCommandEvent,
		SubShellExitEvent,
		SubStopEvent,
	}
	
	public SubEvent(SubPlugin SubPlugin, Events Event, SubServer Server) {
		this.SubPlugin = SubPlugin;
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
	
	/**
	 * Check if the Event was Cancelled
	 * @return Event Cancellation Status (true/false)
	 */
	public boolean isCancelled() { return isCancelled; }
	
	/**
	 * Set the Event's Cancellation Status
	 * 
	 * @param value Cancel Event? (true/false)
	 */
	public void setCancelled(boolean value) { isCancelled = value; }
	
	/**
	 * Runs a SubEvent
	 * 
	 * @param SubPlugin SubServers Main Class
	 * @param Event The event to Trigger
	 * @param args A List of Objects to be Cast to the proper Arguments
	 * @return False if Cancelled
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static boolean RunEvent(SubPlugin SubPlugin, Events Event, Object... args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		boolean EventStatus = true;
		for(Iterator<List<SubListener>> List = SubPlugin.EventHandlers.values().iterator(); List.hasNext(); ) {
			List<SubListener> item = List.next();


			for(Iterator<SubListener> Listeners = item.iterator(); Listeners.hasNext(); ) {
				SubListener Listener = Listeners.next();

                if (Event.equals(Events.SubCreateEvent)) {
                    SubCreateEvent event = new SubCreateEvent(SubPlugin, (SubServer) args[0], (OfflinePlayer) args[1], (ServerTypes) args[2]);
                    Listener.onSubServerCreate(event);
                    if (event.isCancelled) EventStatus = false;
                } else if (Event.equals(Events.SubStartEvent)) {
                    SubPlayerEvent event = new SubPlayerEvent(SubPlugin, Event, (SubServer) args[0], (OfflinePlayer) args[1]);
                    Listener.onSubServerStart(event);
                    if (event.isCancelled) EventStatus = false;
                } else if (Event.equals(Events.SubRunCommandEvent)) {
                    SubRunCommandEvent event = new SubRunCommandEvent(SubPlugin, (SubServer) args[0], (OfflinePlayer) args[1], (String) args[2]);
                    Listener.onSubServerStop(event);
                    if (event.isCancelled) EventStatus = false;
                } else if (Event.equals(Events.SubShellExitEvent)) {
                    Listener.onSubServerShellExit(new SubEvent(SubPlugin, Event, (SubServer) args[0]));
                } else if (Event.equals(Events.SubStopEvent)) {
                    SubPlayerEvent event = new SubPlayerEvent(SubPlugin, Event, (SubServer) args[0], (OfflinePlayer) args[1]);
                    Listener.onSubServerStop(event);
                    if (event.isCancelled) EventStatus = false;
                } else {
                    throw new IllegalArgumentException("an Invalid Event was Called");
                }
			}
		}
		
		return EventStatus;
	}
}
