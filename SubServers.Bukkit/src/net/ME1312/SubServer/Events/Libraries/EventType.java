package net.ME1312.SubServer.Events.Libraries;

import net.ME1312.SubServer.SubPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Event Types Class
 *
 * @author ME1312
 */
public enum EventType {
    SubCreateEvent(net.ME1312.SubServer.Events.SubCreateEvent.class),
    SubShellExitEvent(net.ME1312.SubServer.Events.SubShellExitEvent.class),
    SubStartEvent(net.ME1312.SubServer.Events.SubStartEvent.class),
    SubStopEvent(net.ME1312.SubServer.Events.SubStopEvent.class),
    SubRunCommandEvent(net.ME1312.SubServer.Events.SubRunCommandEvent.class);

    private final Class<? extends Event> value;

    EventType(Class<? extends Event> value) {
        this.value = value;
    }

    public Class<? extends Event> getValue() {
        return value;
    }

    public Event create(SubPlugin game, Object[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<Object> objList = new ArrayList<Object>();
        objList.add(game);
        objList.addAll(Arrays.<Object>asList(args));

        List<Class<?>> classList = new ArrayList<Class<?>>();
        for (Iterator<Object> objects = objList.iterator(); objects.hasNext(); ) {
            classList.add(objects.next().getClass());
        }
        return value.getDeclaredConstructor(classList.toArray(new Class<?>[classList.size()])).newInstance(objList.toArray());
    }
}
