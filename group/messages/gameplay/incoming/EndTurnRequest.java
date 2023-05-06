package messages.gameplay.incoming;

import enums.MessageType;
import messages.Message;

public class EndTurnRequest extends Message {
    public final int clientID;
    public final int characterID;

    public EndTurnRequest(String version, int clientID, int characterID) {
        this.type = MessageType.END_TURN_REQUEST;
        this.version = version;
        this.clientID = clientID;
        this.characterID = characterID;
    }
}
