package messages.gameplay.outgoing;

import enums.MessageType;
import messages.Message;

public class TurnMessage extends Message {
    public final int clientID;
    public final int characterID;


    public TurnMessage(String version, int clientID, int characterID) {
        this.type = MessageType.TURN_DEMAND;
        this.version = version;
        this.clientID = clientID;
        this.characterID = characterID;
    }
}
