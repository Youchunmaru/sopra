package messages.gameplay.outgoing;

import enums.MessageType;
import messages.Message;

public class PauseGameMessage extends Message {
    public final int requestedByClientID;
    public final boolean pause;

    public PauseGameMessage(String version, int requestedByClientID, boolean pause) {
        this.type = MessageType.GAME_PAUSE_DEMAND;
        this.version=version;
        this.requestedByClientID = requestedByClientID;
        this.pause = pause;
    }
}