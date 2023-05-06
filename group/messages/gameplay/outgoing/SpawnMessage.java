package messages.gameplay.outgoing;

import enums.MessageType;
import messages.util.Point;
import messages.util.SpawnMessageCharacter;
import messages.Message;

public class SpawnMessage extends Message {
    public final int clientID;
    public final int characterID;
    public final String characterName;
    public final Point position;
    public final SpawnMessageCharacter attributes;


    public SpawnMessage(String version, int clientID, int characterID, String characterName, Point position, SpawnMessageCharacter attributes) {
        this.type = MessageType.SPAWN_CHARACTER_DEMAND;
        this.version=version;
        this.clientID = clientID;
        this.characterID = characterID;
        this.characterName = characterName;
        this.position = position;
        this.attributes = attributes;
    }
}