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
    EventPriority priority() default EventPriority.NORMAL;
}
