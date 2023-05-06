package messages.gameplay.incoming;

import enums.MessageType;
import messages.Message;
import messages.util.Point;

public class HeliRequest extends Message {
    public int clientID;
    public int characterID;
    public Point target;

    public HeliRequest(String version, int clientID, int characterID, Point target) {
        this.type = MessageType.HELI_REQUEST;
        this.version = version;
        this.clientID = clientID;
        this.characterID = characterID;
        this.target = target;
    }
}
