package messages.gameplay.outgoing;

import enums.MessageType;
import messages.Message;
import messages.util.Point;

public class HeliMessage extends Message {
    public int clientID;
    public int characterID;
    public Point target;
    public boolean crash;

    public HeliMessage(String version, int clientID, int characterID, Point target, boolean crash) {
        this.type = MessageType.HELI_DEMAND;
        this.version = version;
        this.clientID = clientID;
        this.characterID = characterID;
        this.target = target;
        this.crash = crash;
    }
}
