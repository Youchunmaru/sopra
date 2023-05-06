package messages.gameplay.incoming;

import enums.MessageType;
import messages.Message;


public class PauseRequest extends Message {
    public final boolean pause;

    public PauseRequest(String version, boolean pause) {
        this.type = MessageType.PAUSE_REQUEST;
        this.version=version;
        this.pause = pause;
    }
}
