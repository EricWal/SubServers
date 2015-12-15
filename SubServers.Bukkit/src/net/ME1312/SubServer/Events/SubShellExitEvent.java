package net.ME1312.SubServer.Events;

import net.ME1312.SubServer.Events.Libraries.EventType;
import net.ME1312.SubServer.Events.Libraries.SubEvent;
import net.ME1312.SubServer.Executable.SubServer;

/**
 * Shell Exit Event Class
 */
public class SubShellExitEvent extends SubEvent {

    public SubShellExitEvent(SubServer Server) {
        super(EventType.SubShellExitEvent, Server);
    }
}
