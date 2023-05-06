package messages.gameplay.incoming;

import enums.MessageType;
import messages.Message;

public class RequestGameState extends Message {
    public final int clientID;

    public RequestGameState(String version, int clientID) {
        this.type = MessageType.REQUEST_GAMESTATE;
        this.version=version;
        this.clientID = clientID;
    }
}
