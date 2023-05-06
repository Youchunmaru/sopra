package messages.gameplay.incoming;

import enums.MessageType;
import messages.util.PathConfig;
import messages.Message;

public class MovementRequest extends Message {

    public final int clientID;
    public final int characterID;
    public PathConfig specs;

    public MovementRequest(String version,int clientID, int characterID, PathConfig specs) {
        this.type = MessageType.MOVEMENT_REQUEST;
        this.version = version;
        this.clientID = clientID;
        this.characterID = characterID;
        this.specs =  specs;
    }
}
