package messages.gameplay.incoming;

import enums.MessageType;
import messages.Message;

public class TransferRequest extends Message {
    public final int clientID;
    public final int characterID;
    public final int targetID;
    public final int amount;

    public TransferRequest(String version, int clientID, int characterID, int targetID, int amount) {
        this.type = MessageType.TRANSFER_REQUEST;
        this.version = version;
        this.clientID = clientID;
        this.characterID = characterID;
        this.targetID = targetID;
        this.amount = amount;
    }
}
