package net.ME1312.SubServer.Events.Libraries;

import net.ME1312.SubServer.SubPlugin;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Base Events Class
 *
 * @author ME1312
 */
public class Event {
    private net.ME1312.SubServer.SubPlugin SubPlugin;
    private org.bukkit.event.Event.Result Status = org.bukkit.event.Event.Result.DEFAULT;
    private EventType Event;

    public Event(SubPlugin SubPlugin, EventType Event) {
        this.SubPlugin = SubPlugin;
        this.Event = Event;
    }

    @Override
    public String toString() { return Event.toString(); }

    /**
     * Gets the status of this event, passed by this listener and the listeners before this one
     * @return an Event Result representing the status of this event
     */
    public org.bukkit.event.Event.Result getStatus() { return Status; }

    /**
     * Cancel/Uncancel this event
     */
    public void setCancelled(boolean value) {
        if (value) {
            Status = org.bukkit.event.Event.Result.ALLOW;
        } else {
            Status = org.bukkit.event.Event.Result.DENY;
        }
    }

    /**
     * Gets the Event Name
     * @return Event Name
     */
    public String getEventName() { return Event.toString(); }

    /**
     * Gets Methods handling event
     * @return This Method's Handler Method List, Sorted by Priority
     */
    public HashMap<EventPriority, Method> getEventHandlers() {
        HashMap<EventPriority, Method> handlers = new HashMap<EventPriority, Method>();
        for (Iterator<JavaPlugin> Plugins = SubPlugin.Listeners.keySet().iterator(); Plugins.hasNext(); ) {
            JavaPlugin plugin = Plugins.next();
            for (Iterator<Listener> Listeners = SubPlugin.Listeners.get(plugin).keySet().iterator(); Listeners.hasNext(); ) {
                Listener listener = Listeners.next();
                if (SubPlugin.Listeners.get(plugin).get(listener).keySet().contains(Event)) {
                    for (Iterator<EventPriority> Priorities = SubPlugin.Listeners.get(plugin).get(listener).get(Event).keySet().iterator(); Priorities.hasNext(); ) {
                        EventPriority priority = Priorities.next();
                        for (Iterator<Method> Methods = SubPlugin.Listeners.get(plugin).get(listener).get(Event).get(priority).iterator(); Methods.hasNext(); ) {
                            handlers.put(priority, Methods.next());
                        }
                    }
                }
            }
        }
        return handlers;
    }
}
