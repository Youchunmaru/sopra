package messages.gameplay.outgoing;

import enums.MessageType;
import messages.Message;

public class EndgameMessage extends Message {
    public EndgameMessage(String version) {
        this.type = MessageType.ENDGAME;
        this.version=version;
    }
}
