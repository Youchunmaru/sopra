package messages.gameplay.outgoing;

import enums.MessageType;
import messages.Message;

public class StrikeMessage extends Message {
    public final int clientID;
    public final String wrongMessage;
    public final int count;


    public StrikeMessage(String version, int clientID, String wrongMessage, int count) {
        this.type = MessageType.STRIKE;
        this.version=version;
        this.clientID = clientID;
        this.wrongMessage = wrongMessage;
        this.count = count;
    }
}
