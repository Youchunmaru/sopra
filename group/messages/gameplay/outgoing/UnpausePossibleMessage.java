package messages.gameplay.outgoing;

import enums.MessageType;
import messages.Message;

public class UnpausePossibleMessage extends Message {

    public int requestedByClientID;

    public UnpausePossibleMessage(String version, int requestedByClientID) {
        this.type = MessageType.UNPAUSE_GAME_OFFER;
        this.version=version;
        this.requestedByClientID = requestedByClientID;
    }
}
