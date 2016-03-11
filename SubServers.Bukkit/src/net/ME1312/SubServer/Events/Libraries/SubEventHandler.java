package net.ME1312.SubServer.Events.Libraries;

import org.bukkit.event.EventPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
/**
 * Event Handler Annotation
 *
 * @author ME1312
 */
public @interface SubEventHandler {
    /**
     * The Event's Priority: Events will be ran in the same order <a href="https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/EventHandler.html#priority()">Bukkit runs theirs</a>
     */
    EventPriority priority() default EventPriority.NORMAL;

    /**
     * Override settings passed by other EventHandlers before this one
     */
    boolean override() default false;
}
