package messages.gameplay.incoming;

import enums.MessageType;
import messages.Message;

public class GameStateRequest extends Message {
    public final int clientID;

    public GameStateRequest(String version, int clientID) {
        this.type = MessageType.GAMESTATE_REQUEST;
        this.version=version;
        this.clientID = clientID;
    }


}