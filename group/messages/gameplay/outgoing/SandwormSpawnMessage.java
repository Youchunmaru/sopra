package messages.gameplay.outgoing;

import enums.MessageType;
import messages.util.Point;
import messages.Message;

public class SandwormSpawnMessage extends Message {
    public final int clientID;
    public final int characterID;
    public final Point position;


    public SandwormSpawnMessage(String version, int clientID, int characterID, Point position) {
        this.type = MessageType.SANDWORM_SPAWN_DEMAND;
        this.version=version;
        this.clientID = clientID;
        this.characterID = characterID;
        this.position = position;
    }
}
