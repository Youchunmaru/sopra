package messages.gameplay.outgoing;

import enums.MessageType;
import messages.Message;

public class TransferMessage extends Message {
    public final int clientID;
    public final int characterID;
    public final int targetID;

    public TransferMessage(String version, int clientID, int characterID, int targetID) {
        this.type = MessageType.TRANSFER_DEMAND;
        this.version = version;
        this.clientID = clientID;
        this.characterID = characterID;
        this.targetID = targetID;
    }
}
