package net.ME1312.SubServer.Events;

import net.ME1312.SubServer.Events.Libraries.EventType;
import net.ME1312.SubServer.Events.Libraries.SubEvent;
import net.ME1312.SubServer.Executable.SubServer;
import net.ME1312.SubServer.SubPlugin;

/**
 * Shell Exit Event Class
 */
public class SubShellExitEvent extends SubEvent {

    /**
     * Shell Exit Event (Not Cancellable)
     *
     * @param Server Server that shell is exiting for
     */
    public SubShellExitEvent(SubPlugin SubPlugin, SubServer Server) {
        super(SubPlugin, EventType.SubShellExitEvent, Server);
    }
}
