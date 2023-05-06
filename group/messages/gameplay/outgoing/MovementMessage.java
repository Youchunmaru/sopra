package messages.gameplay.outgoing;

import enums.MessageType;
import messages.util.PathConfig;
import messages.Message;

public class MovementMessage extends Message {
    public final int clientID;
    public final int characterID;
    public PathConfig specs;

    public MovementMessage(String version,int clientID, int characterID, PathConfig specs) {
        this.type = MessageType.MOVEMENT_DEMAND;
        this.version = version;
        this.clientID = clientID;
        this.characterID = characterID;
        this.specs =  specs;
    }
}